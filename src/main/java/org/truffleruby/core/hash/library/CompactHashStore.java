/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash.library;

import static com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import static com.oracle.truffle.api.CompilerDirectives.shouldNotReachHere;
import static com.oracle.truffle.api.dsl.Cached.Exclusive;
import static org.truffleruby.core.hash.library.HashStoreLibrary.EachEntryCallback;

import java.util.Set;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.annotations.SuppressFBWarnings;
import org.truffleruby.collections.PEBiFunction;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.hash.CompareHashKeysNode;
import org.truffleruby.core.hash.FreezeHashKeyIfNeededNode;
import org.truffleruby.core.hash.HashLiteralNode;
import org.truffleruby.core.hash.HashingNodes;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.InlinedConditionProfile;
import com.oracle.truffle.api.profiles.InlinedLoopConditionProfile;

/** The Compact hash strategy from Hash Maps That Don't Hate You. See
 * https://blog.toit.io/hash-maps-that-dont-hate-you-1a96150b492a for more details (archived at
 * https://archive.ph/wip/sucRY). Specifically that blog post has good visualizations of the index and kvStore arrays.
 * We do not currently implement the deletion optimization. */
@ExportLibrary(value = HashStoreLibrary.class)
@GenerateUncached
public final class CompactHashStore {
    //  ---------------------------------------------------------------------------------------------------------------
    // Big Picture:
    //  (1) A Compact Hash's big idea is storing all key-values in a contiguous array (the KV array) in insertion order
    //      This enables us to do sequential iteration on all key-values in insertion order, which is what we want
    //      But it doesn't allow constant-time lookups
    //  -----------------------------------------------
    //  (2) Thus, a Compact Hash also stores the position of every key in the KV array in another array called the index
    //      That KV position is stored in the index array at a location that depends on the respective key's hash
    //      Therefore, given a key we can always quickly find where it's in the KV array by:
    //                                    -----------------------------------------------
    //              1- Hashing it
    //              2- Calculating the position of the hash from its value
    //              3- Looking up the index array at that position,
    //              4- Where we find the KV position
    //              5- Then looking up the KV array by that position
    //              6- Where we find the key and then the value right next to it
    //                                    -----------------------------------------------
    //      (step 3 is oversimplified a bit, the position calculated in (2) is where we store the hash itself in the
    //       index array, the KV position is stored next to it. The reason we store the hash is that step 2 is lossy,
    //       multiple not-equal hashes can map to the same position in the index array, that's a different sort of
    //       "collision" in addition to the usual collision of equal hash values coming from different keys. Both kinds
    //       of collisions actually result in the same outcome: contention on an index slot. So both are automatically
    //       handled by the same collision-resolution strategy: open-addressing with linear probing.)
    //  -----------------------------------------------
    //  (3) tl;dr: Compact Hashes give you both insertion-order iteration AND the usual const-time guarantees
    //  ---------------------------------------------------------------------------------------------------------------

    /** We view the index array as consisting of "slots", each slot is 2 array positions. index[0] and index[1] is one
     * slot, index[2] and index[3] is another,... The first position of a slot holds the hash of a key and the second
     * holds an offsetted index into the KV store (index + 1) where the key is stored. The reason we store an offsetted
     * index and not the index itself is so that 0 is not a valid index, thus we can use it as unused slot marker, and
     * since all int[] arrays start out zeroed this means that new index arrays are marked unused "for free" */
    private int[] index;
    /** An array of key and value pairs, in insertion order */
    private Object[] kvStore;
    /** This tracks the next valid insertion position into kvStore, it starts at 0 and always increases by 2. We can't
     * use the size of the RubyHash for that, because deletion reduces its hash.size. Whereas the insertion pos into
     * kvStore can never decrease, we don't reuse empty slots (without also resizing the whole hash). */
    private int kvStoreInsertionPos;
    /** This tracks the number of occupied slots at which we should rebuild the index array. For example, at a load
     * factor of 0.75 and an index of total size 64 slots, that number is 48 slots. (Technically, that's redundant
     * information that is derived from the load factor and the index size, but deriving it requires a float
     * multiplication or division, and we want to check for it on every insertion, so it's inefficient to keep
     * calculating it every time it is needed) */
    private int indexGrowthThreshold;

    /** Each slot in the index array can be in one of 3 states, depending on the value of its second (offset) field:
     * <li>Offset >= 1: Filled, the data in the hash field and the offset field is valid. Subtracting one from the
     * offset will yield a valid index into the KV array.</li>
     * <li>Offset == 0: Unused, the data in the hash field and the offset field is NOT valid, and the slot was never
     * filled with valid data. The value 0 is used for unused slots so `new int[]` automatically makes all slots unused
     * without an extra Arrays.fill().</li>
     * <li>Offset == -1: Deleted, the data in the hash field and the offset field is NOT valid, but the slot was
     * occupied with valid data before.</li> */
    private static final int INDEX_SLOT_UNUSED = 0;
    private static final int INDEX_SLOT_DELETED = -1;

    // Returned by methods doing array search which don't find what they are looking for
    static final int KEY_NOT_FOUND = -2;
    static final int HASH_NOT_FOUND = KEY_NOT_FOUND;

    public static final float THRESHOLD_LOAD_FACTOR_FOR_INDEX_REBUILD = 0.75f;

    public CompactHashStore(int capacity) {
        if (capacity < 1 || capacity >= (1 << 28)) {
            throw shouldNotReachHere();
        }

        int kvCapacity = roundUpwardsToNearestPowerOf2(capacity);
        // the index array needs to be a little sparse for good performance (i.e. low load factor)
        // so to store 8 key-value entries an index of exactly 8 slots is too crowded.
        // This way the initial load factor (for capacity entries) is between and 0.25 and 0.5.
        int indexCapacity = 2 * kvCapacity;

        // All zeros by default, so all slots are marked empty for free
        this.index = new int[2 * indexCapacity];
        this.kvStore = new Object[2 * kvCapacity];
        this.kvStoreInsertionPos = 0;
        this.indexGrowthThreshold = (int) (indexCapacity * THRESHOLD_LOAD_FACTOR_FOR_INDEX_REBUILD);
    }

    // private non-allocating constructor for .copy()
    private CompactHashStore(int[] index, Object[] kvStore, int kvStoreInsertionPos, int indexGrowthThreshold) {
        this.index = index;
        this.kvStore = kvStore;
        this.kvStoreInsertionPos = kvStoreInsertionPos;
        this.indexGrowthThreshold = indexGrowthThreshold;
    }

    /** Rounds up powers of 2 themselves : 1 => 2, 2 => 4, 3 => 4, 4 => 8, ... */
    private static int roundUpwardsToNearestPowerOf2(int num) {
        return Integer.highestOneBit(num) << 1;
    }

    // For promoting from packed to compact
    public void putHashKeyValue(int hashcode, Object key, Object value) {
        int pos = kvStoreInsertionPos;
        SetKvAtNode.insertIntoKv(this, key, value);
        SetKvAtNode.insertIntoIndex(hashcode, pos + 1, index,
                InlinedLoopConditionProfile.getUncached(), null);
    }

    @ExportMessage
    Object lookupOrDefault(Frame frame, RubyHash hash, Object key, PEBiFunction defaultNode,
            @Cached @Shared GetHashPosAndKvPosForKeyNode getHashPosAndKvPos,
            @Cached @Shared HashingNodes.ToHash hashFunction,
            @Cached @Exclusive InlinedConditionProfile keyNotFound,
            @Bind("$node") Node node) {
        int keyHash = hashFunction.execute(key, hash.compareByIdentity);
        int keyKvPos = IntPair.second(
                getHashPosAndKvPos.execute(key, keyHash, hash.compareByIdentity, index, kvStore));

        if (keyNotFound.profile(node, keyKvPos == KEY_NOT_FOUND)) {
            return defaultNode.accept(frame, hash, key);
        }

        return kvStore[keyKvPos + 1];
    }

    @ExportMessage
    boolean set(RubyHash hash, Object key, Object value, boolean byIdentity,
            @Cached @Shared HashingNodes.ToHash hashFunction,
            @Cached @Shared GetHashPosAndKvPosForKeyNode getHashPosAndKvPos,
            @Cached FreezeHashKeyIfNeededNode freezeKey,
            @Cached @Exclusive PropagateSharingNode propagateSharingForKey,
            @Cached @Exclusive PropagateSharingNode propagateSharingForVal,
            @Cached SetKvAtNode setKv,
            @Bind("$node") Node node) {
        var frozenKey = freezeKey.executeFreezeIfNeeded(node, key, byIdentity);
        int keyHash = hashFunction.execute(frozenKey, byIdentity);
        int keyKvPos = IntPair.second(
                getHashPosAndKvPos.execute(frozenKey, keyHash, byIdentity, index, kvStore));

        propagateSharingForKey.execute(node, hash, frozenKey);
        propagateSharingForVal.execute(node, hash, value);

        return setKv.execute(hash, this, keyKvPos, keyHash, frozenKey, value);
    }

    @ExportMessage
    Object delete(RubyHash hash, Object key,
            @Cached @Shared GetHashPosAndKvPosForKeyNode getHashPosAndKvPos,
            @Cached @Shared HashingNodes.ToHash hashFunction,
            @Cached @Exclusive InlinedConditionProfile keyNotFound,
            @Bind("$node") Node node) {
        int keyHash = hashFunction.execute(key, hash.compareByIdentity);
        long hashPosAndKeyKvPos = getHashPosAndKvPos.execute(key, keyHash, hash.compareByIdentity, index, kvStore);
        int hashPos = IntPair.first(hashPosAndKeyKvPos);
        int keyKvPos = IntPair.second(hashPosAndKeyKvPos);

        if (keyNotFound.profile(node, keyKvPos == KEY_NOT_FOUND)) {
            return null;
        }

        return deleteKvAndGetV(hash, hashPos, keyKvPos);
    }

    @ExportMessage
    Object deleteLast(RubyHash hash, Object key,
            @Cached @Shared HashingNodes.ToHash hashFunction,
            @Cached @Shared GetHashPosForKeyAtKvPosNode getHashPos,
            @Cached @Exclusive InlinedConditionProfile kvAllNulls,
            @Cached @Exclusive InlinedConditionProfile keyGivenIsNotLastKey,
            @Cached @Exclusive InlinedLoopConditionProfile nonNullKeyNotYetFound,
            @Bind("$node") Node node) {
        int lastKeyPos = firstNonNullKeyPosFromEnd(nonNullKeyNotYetFound, node);
        if (kvAllNulls.profile(node, lastKeyPos == KEY_NOT_FOUND)) {
            return null;
        }
        if (keyGivenIsNotLastKey.profile(node, key != kvStore[lastKeyPos])) {
            return null;
        }

        int keyHash = hashFunction.execute(key, hash.compareByIdentity);
        int hashPos = getHashPos.execute(keyHash, lastKeyPos, index);

        return deleteKvAndGetV(hash, hashPos, lastKeyPos);
    }

    @ExportMessage
    Object eachEntry(RubyHash hash, EachEntryCallback callback, Object state,
            @Cached @Exclusive InlinedConditionProfile keyNotNull,
            @Cached @Exclusive InlinedLoopConditionProfile loopProfile,
            @Bind("$node") Node node) {
        int i = 0;
        int callbackIdx = 0;
        try {
            for (; loopProfile.inject(node, i < kvStoreInsertionPos); i += 2) {
                if (keyNotNull.profile(node, kvStore[i] != null)) {
                    callback.accept(callbackIdx, kvStore[i], kvStore[i + 1], state);
                    callbackIdx++;
                }
            }
        } finally {
            RubyBaseNode.profileAndReportLoopCount(node, loopProfile, i >> 1);
        }
        return state;
    }

    @ExportMessage
    Object eachEntrySafe(RubyHash hash, EachEntryCallback callback, Object state,
            @CachedLibrary("this") HashStoreLibrary hashlib) {
        return hashlib.eachEntry(this, hash, callback, state);
    }

    @ExportMessage
    void replace(RubyHash hash, RubyHash dest,
            @Cached @Exclusive PropagateSharingNode propagateSharing,
            @Cached @Exclusive InlinedConditionProfile noReplaceNeeded,
            @Bind("$node") Node node) {
        if (noReplaceNeeded.profile(node, hash == dest)) {
            return;
        }

        propagateSharing.execute(node, dest, hash);

        CompactHashStore copy = this.copy();
        dest.size = hash.size;
        dest.store = copy;
        dest.compareByIdentity = hash.compareByIdentity;
        dest.defaultBlock = hash.defaultBlock;
        dest.defaultValue = hash.defaultValue;
    }

    @ExportMessage
    RubyArray shift(RubyHash hash,
            @Cached @Shared HashingNodes.ToHash hashFunction,
            @Cached @Shared GetHashPosForKeyAtKvPosNode getHashPos,
            @Cached @Exclusive InlinedConditionProfile noKvToShift,
            @Cached @Exclusive InlinedLoopConditionProfile nonNullKeyNotYetFound,
            @Cached @Exclusive InlinedConditionProfile nonNullKeyFound,
            @Bind("$node") Node node) {
        int lastKeyPos = firstNonNullKeyPosFromBeginning(
                nonNullKeyNotYetFound, nonNullKeyFound, node);

        if (noKvToShift.profile(node, lastKeyPos == KEY_NOT_FOUND)) {
            return null;
        }
        Object key = kvStore[lastKeyPos];
        int keyHash = hashFunction.execute(key, hash.compareByIdentity);
        int hashPos = getHashPos.execute(keyHash, lastKeyPos, index);
        Object val = deleteKvAndGetV(hash, hashPos, lastKeyPos);

        return ArrayHelpers.createArray(RubyContext.get(node), RubyLanguage.get(node), new Object[]{ key, val });
    }

    @TruffleBoundary
    @ExportMessage
    void rehash(RubyHash hash,
            @Cached @Exclusive InlinedConditionProfile slotUsed,
            @Cached @Exclusive InlinedLoopConditionProfile loopProfile,
            @CachedLibrary("this") HashStoreLibrary hashlib,
            @Bind("$node") Node node) {
        Object[] oldKvStore = this.kvStore;
        int oldKvStoreInsertionPos = this.kvStoreInsertionPos;

        this.kvStore = new Object[oldKvStore.length];
        this.kvStoreInsertionPos = 0;
        this.index = new int[this.index.length];
        hash.size = 0;

        int i = 0;
        try {
            for (; loopProfile.inject(node, i < oldKvStoreInsertionPos); i += 2) {
                if (slotUsed.profile(node, oldKvStore[i] != null)) {
                    hashlib.set(this, hash, oldKvStore[i], oldKvStore[i + 1], hash.compareByIdentity);
                }
            }
        } finally {
            RubyBaseNode.profileAndReportLoopCount(node, loopProfile, i >> 1);
        }
    }

    @ExportMessage
    boolean verify(RubyHash hash) {
        assert kvStoreInsertionPos > 0 && kvStoreInsertionPos < kvStore.length;
        assert kvStoreInsertionPos % 2 == 0;
        assert kvStoreInsertionPos >= hash.size; // because deletes only decrease hash.size

        assert ((float) (2 * hash.size)) / index.length <= THRESHOLD_LOAD_FACTOR_FOR_INDEX_REBUILD;

        assert (index.length & (index.length - 1)) == 0; //index.length is always a power of 2

        assertAllKvPositionsAreValid();

        return true;
    }

    @SuppressFBWarnings("IM_BAD_CHECK_FOR_ODD")
    private void assertAllKvPositionsAreValid() {
        for (int i = 1; i < index.length; i += 2) {
            if (index[i] != INDEX_SLOT_UNUSED && index[i] != INDEX_SLOT_DELETED) {
                assert index[i] > 0;
                assert index[i] < kvStoreInsertionPos;
                assert index[i] % 2 == 1;
                assert kvStore[index[i] - 1] != null;
            }
        }
    }

    private Object deleteKvAndGetV(RubyHash hash, int hashPos, int keyKvPos) {
        Object deletedValue = kvStore[keyKvPos + 1];

        index[hashPos + 1] = INDEX_SLOT_DELETED;
        // TODO: Instead of naively nulling out the key-value, which can produce long gaps of nulls in the kvStore,
        //       See if we can annotate each gap with its length so that iteration code can "jump" over it
        kvStore[keyKvPos] = null;
        kvStore[keyKvPos + 1] = null;

        hash.size--;
        return deletedValue;
    }

    private int firstNonNullKeyPosFromEnd(
            InlinedLoopConditionProfile nonNullKeyNotYetFound,
            Node node) {
        int lastKeyPos = kvStoreInsertionPos - 2;
        while (nonNullKeyNotYetFound.profile(node,
                lastKeyPos > 0 && kvStore[lastKeyPos] == null)) {
            lastKeyPos -= 2;
        }
        return lastKeyPos;
    }

    private int firstNonNullKeyPosFromBeginning(
            InlinedLoopConditionProfile nonNullKeyNotYetFound,
            InlinedConditionProfile nonNullKeyFound,
            Node node) {
        int firstKeyPos = 0;
        while (nonNullKeyNotYetFound.profile(node,
                firstKeyPos < kvStoreInsertionPos && kvStore[firstKeyPos] == null)) {
            firstKeyPos += 2;
        }
        // Means the loop must have exited because we found a non-null key
        if (nonNullKeyFound.profile(node, firstKeyPos < kvStoreInsertionPos)) {
            return firstKeyPos;
        }
        return KEY_NOT_FOUND;
    }

    private CompactHashStore copy() {
        return new CompactHashStore(index.clone(), kvStore.clone(), kvStoreInsertionPos, indexGrowthThreshold);
    }

    /** @param hash a hash code
     * @param indexLength the length of the index array
     * @return An even index ranging from 0 to max-1 */
    private static int getIndexPosFromHash(int hash, int indexLength) {
        // x & (m - 1) is equivalent to x % m if m is a power of 2 (which index.length is)
        // x & (m - 2) is equivalent to doing x % m then turning off the lowest bit to ensure the result is even
        return hash & (indexLength - 2);
    }

    /** @param pos current position
     * @param indexLength the length of the index array
     * @return the circularly next position after the current position */
    private static int incrementIndexPos(int pos, int indexLength) {
        return (pos + 2) & (indexLength - 1);
    }

    public void getAdjacentObjects(Set<Object> reachable) {
        for (int i = 0; i < kvStoreInsertionPos; i += 2) {
            if (kvStore[i] != null) {
                ObjectGraph.addProperty(reachable, kvStore[i]);
                ObjectGraph.addProperty(reachable, kvStore[i + 1]);
            }
        }
    }

    /** Given: A key and its hash
     * <p>
     * Returns: A pair of positions (array indices), the first of which is where the key's hash is stored in the index
     * array, and the second is where the key itself is stored in the KV Store array
     * <p>
     * If the key doesn't exist, returns KEY_NOT_FOUND
     * <p>
     * NOTE: The node encodes a pair of integers as a long, this avoids allocations at the expense of readability */
    @GenerateUncached
    abstract static class GetHashPosAndKvPosForKeyNode extends RubyBaseNode {

        public abstract long execute(Object key, int hash, boolean compareByIdentity, int[] index, Object[] kvStore);

        @Specialization
        long getHashPosAndKvPos(Object key, int hash, boolean compareByIdentity, int[] index, Object[] kvStore,
                @Cached CompareHashKeysNode.AssumingEqualHashes compareHashKeysNode,
                @Cached GetHashNextPosInIndexNode getNextHashPos,
                @Cached @Exclusive InlinedConditionProfile passedKeyIsEqualToFoundKey,
                @Cached @Exclusive InlinedLoopConditionProfile keyHashFound,
                @Bind("$node") Node node) {
            int startPos = getIndexPosFromHash(hash, index.length);
            int nextHashPos = getNextHashPos.execute(startPos, hash, index, startPos);

            while (keyHashFound.profile(node, nextHashPos != HASH_NOT_FOUND)) {
                int kvPos = index[nextHashPos + 1] - 1;
                Object otherKey = kvStore[kvPos];

                if (passedKeyIsEqualToFoundKey.profile(node,
                        compareHashKeysNode.execute(compareByIdentity, key, otherKey))) {
                    return IntPair.mk(nextHashPos, kvPos);
                }

                int next = incrementIndexPos(nextHashPos, index.length);
                nextHashPos = getNextHashPos.execute(next, hash, index, startPos);
            }

            return KEY_NOT_FOUND;
        }
    }

    static final class IntPair {
        public static long mk(int first, int second) {
            return (((long) first) << 32) | (second & 0xffffffffL);
        }

        public static int first(long pair) {
            return (int) (pair >> 32);
        }

        public static int second(long pair) {
            return (int) pair;
        }
    }

    @GenerateUncached
    abstract static class GetHashNextPosInIndexNode extends RubyBaseNode {

        public abstract int execute(int startingFromPos, int hash, int[] index, int stop);

        @Specialization
        int getHashNextPos(int startingFromPos, int hash, int[] index, int stop,
                @Cached @Exclusive InlinedConditionProfile slotIsNotDeleted,
                @Cached @Exclusive InlinedConditionProfile slotIsUnused,
                @Cached @Exclusive InlinedConditionProfile hashFound,
                @Cached @Exclusive InlinedLoopConditionProfile stopNotYetReached,
                @Bind("$node") Node node) {
            int nextHashPos = startingFromPos;

            do {
                if (slotIsUnused.profile(node, index[nextHashPos + 1] == INDEX_SLOT_UNUSED)) {
                    return HASH_NOT_FOUND;
                }

                if (slotIsNotDeleted.profile(node, index[nextHashPos + 1] != INDEX_SLOT_DELETED)) {
                    if (hashFound.profile(node, index[nextHashPos] == hash)) {
                        return nextHashPos;
                    }
                }

                nextHashPos = incrementIndexPos(nextHashPos, index.length);
            } while (stopNotYetReached.profile(node, nextHashPos != stop));

            return HASH_NOT_FOUND;
        }
    }

    @GenerateUncached
    abstract static class GetHashPosForKeyAtKvPosNode extends RubyBaseNode {

        public abstract int execute(int hash, int kvPos, int[] index);

        @Specialization
        int getHashPos(int hash, int kvPos, int[] index,
                @Cached @Exclusive InlinedConditionProfile keyFound,
                @Cached @Exclusive InlinedLoopConditionProfile keyHashFound,
                @Cached GetHashNextPosInIndexNode getNextHashPos,
                @Bind("$node") Node node) {
            int startPos = getIndexPosFromHash(hash, index.length);
            int nextPos = getNextHashPos.execute(startPos, hash, index, startPos);

            while (keyHashFound.profile(node, nextPos != HASH_NOT_FOUND)) {
                int kvPosition = index[nextPos + 1] - 1;

                if (keyFound.profile(node, kvPos == kvPosition)) {
                    return nextPos;
                }

                int next = incrementIndexPos(nextPos, index.length);
                nextPos = getNextHashPos.execute(next, hash, index, startPos);
            }
            return HASH_NOT_FOUND;
        }
    }

    @GenerateUncached
    @ImportStatic(CompactHashStore.class)
    abstract static class SetKvAtNode extends RubyBaseNode {

        public abstract boolean execute(RubyHash hash, CompactHashStore store, int kvPos, int keyHash, Object frozenKey,
                Object value);

        // key already exist, very fast because there is no need to touch the index
        @Specialization(guards = "kvPos != KEY_NOT_FOUND")
        boolean keyAlreadyExistsWithDifferentValue(
                RubyHash hash, CompactHashStore store, int kvPos, int keyHash, Object frozenKey, Object value) {
            store.kvStore[kvPos + 1] = value;
            return false;
        }

        // setting a new key is a relatively expensive insertion
        @Specialization(guards = "kvPos == KEY_NOT_FOUND")
        static boolean keyDoesntExist(
                RubyHash hash, CompactHashStore store, int kvPos, int keyHash, Object frozenKey, Object value,
                @Cached @Exclusive InlinedConditionProfile kvResizingIsNeeded,
                @Cached @Exclusive InlinedConditionProfile indexResizingIsNeeded,
                @Cached @Exclusive InlinedLoopConditionProfile indexSlotUnavailable,
                @Bind("$node") Node node) {
            if (indexResizingIsNeeded.profile(node,
                    hash.size >= store.indexGrowthThreshold)) {
                resizeIndex(store, node);
            }
            if (kvResizingIsNeeded.profile(node,
                    store.kvStoreInsertionPos >= store.kvStore.length)) {
                resizeKvStore(store);
            }
            int pos = store.kvStoreInsertionPos;
            insertIntoKv(store, frozenKey, value);
            insertIntoIndex(keyHash, pos + 1, store.index, indexSlotUnavailable, node);

            hash.size++;
            return true;
        }

        private static void insertIntoIndex(int keyHash, int kvPos, int[] index,
                InlinedLoopConditionProfile unavailableSlot, Node node) {
            int pos = getIndexPosFromHash(keyHash, index.length);

            while (unavailableSlot.profile(node, index[pos + 1] > INDEX_SLOT_UNUSED)) {
                pos = incrementIndexPos(pos, index.length);
            }

            index[pos] = keyHash;
            index[pos + 1] = kvPos;
        }

        private static void insertIntoKv(CompactHashStore store, Object key, Object value) {
            store.kvStore[store.kvStoreInsertionPos] = key;
            store.kvStore[store.kvStoreInsertionPos + 1] = value;
            store.kvStoreInsertionPos += 2;
        }

        @TruffleBoundary
        private static void resizeIndex(CompactHashStore store, Node node) {
            int[] oldIndex = store.index;
            store.index = new int[2 * oldIndex.length];

            int i = 0;
            for (; i < oldIndex.length; i += 2) {
                int hash = oldIndex[i];
                int kvPos = oldIndex[i + 1];

                insertIntoIndex(hash, kvPos, store.index, InlinedLoopConditionProfile.getUncached(), node);
            }
            store.indexGrowthThreshold = (int) (oldIndex.length * THRESHOLD_LOAD_FACTOR_FOR_INDEX_REBUILD);
        }

        private static void resizeKvStore(CompactHashStore store) {
            store.kvStore = ArrayUtils.grow(store.kvStore, 2 * store.kvStore.length);
        }
    }

    public static final class CompactHashLiteralNode extends HashLiteralNode {

        @Child HashStoreLibrary hashes;

        public CompactHashLiteralNode(RubyNode[] keyValues) {
            super(keyValues);
            hashes = insert(HashStoreLibrary.createDispatched());
        }

        @ExplodeLoop
        @Override
        public Object execute(VirtualFrame frame) {
            CompactHashStore store = new CompactHashStore(getNumberOfEntries());
            RubyHash hash = new RubyHash(coreLibrary().hashClass,
                    getLanguage().hashShape,
                    getContext(),
                    store,
                    0,
                    false);

            for (int i = 0; i < keyValues.length; i += 2) {
                Object key = keyValues[i].execute(frame);
                Object value = keyValues[i + 1].execute(frame);
                hashes.set(store, hash, key, value, false);
            }
            return hash;
        }

        @Override
        public RubyNode cloneUninitialized() {
            var copy = new CompactHashLiteralNode(cloneUninitialized(keyValues));
            return copy.copyFlags(this);
        }
    }
}
