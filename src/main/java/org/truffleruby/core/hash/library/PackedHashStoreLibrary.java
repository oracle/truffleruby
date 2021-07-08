/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash.library;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.CachedLanguage;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.ExplodeLoop.LoopExplosionKind;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.PEBiFunction;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.basicobject.BasicObjectNodes.ReferenceEqualNode;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.hash.CompareHashKeysNode;
import org.truffleruby.core.hash.Entry;
import org.truffleruby.core.hash.FreezeHashKeyIfNeededNode;
import org.truffleruby.core.hash.FreezeHashKeyIfNeededNodeGen;
import org.truffleruby.core.hash.HashGuards;
import org.truffleruby.core.hash.HashLiteralNode;
import org.truffleruby.core.hash.HashingNodes;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.hash.library.HashStoreLibrary.EachEntryCallback;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.objects.shared.PropagateSharingNode;
import org.truffleruby.language.objects.shared.SharedObjects;

@ExportLibrary(value = HashStoreLibrary.class, receiverType = Object[].class)
@GenerateUncached
public class PackedHashStoreLibrary {

    public static final int ELEMENTS_PER_ENTRY = 3;

    // region Utilities

    public static Object[] createStore(RubyLanguage language) {
        return new Object[language.options.HASH_PACKED_ARRAY_MAX * ELEMENTS_PER_ENTRY];
    }

    private static Object[] copyStore(RubyLanguage language, Object[] store) {
        final Object[] copied = createStore(language);
        System.arraycopy(store, 0, copied, 0, language.options.HASH_PACKED_ARRAY_MAX * ELEMENTS_PER_ENTRY);
        return copied;
    }

    private static int getHashed(Object[] store, int n) {
        return (int) store[n * ELEMENTS_PER_ENTRY];
    }

    private static Object getKey(Object[] store, int n) {
        return store[n * ELEMENTS_PER_ENTRY + 1];
    }

    private static Object getValue(Object[] store, int n) {
        return store[n * ELEMENTS_PER_ENTRY + 2];
    }

    private static void setHashed(Object[] store, int n, int hashed) {
        store[n * ELEMENTS_PER_ENTRY] = hashed;
    }

    private static void setKey(Object[] store, int n, Object key) {
        store[n * ELEMENTS_PER_ENTRY + 1] = key;
    }

    private static void setValue(Object[] store, int n, Object value) {
        store[n * ELEMENTS_PER_ENTRY + 2] = value;
    }

    public static void setHashedKeyValue(Object[] store, int n, int hashed, Object key, Object value) {
        setHashed(store, n, hashed);
        setKey(store, n, key);
        setValue(store, n, value);
    }

    private static void removeEntry(RubyLanguage language, Object[] store, int n) {
        assert verifyIntegerHashes(language, store);

        final int index = n * ELEMENTS_PER_ENTRY;
        System.arraycopy(
                store,
                index + ELEMENTS_PER_ENTRY,
                store,
                index,
                language.options.HASH_PACKED_ARRAY_MAX * ELEMENTS_PER_ENTRY - ELEMENTS_PER_ENTRY - index);

        assert verifyIntegerHashes(language, store);
    }

    private static boolean verifyIntegerHashes(RubyLanguage language, Object[] store) {
        for (int i = 0; i < language.options.HASH_PACKED_ARRAY_MAX *
                ELEMENTS_PER_ENTRY; i += ELEMENTS_PER_ENTRY) {
            assert store[i] == null || store[i] instanceof Integer;
        }
        return true;
    }

    @TruffleBoundary
    private static void promoteToBuckets(RubyHash hash, Object[] store, int size) {
        final Entry[] buckets = new Entry[BucketsHashStore.capacityGreaterThan(size)];

        Entry firstInSequence = null;
        Entry previousInSequence = null;
        Entry lastInSequence = null;

        for (int n = 0; n < size; n++) {
            final int hashed = getHashed(store, n);
            final Entry entry = new Entry(hashed, getKey(store, n), getValue(store, n));

            if (previousInSequence == null) {
                firstInSequence = entry;
            } else {
                previousInSequence.setNextInSequence(entry);
                entry.setPreviousInSequence(previousInSequence);
            }

            previousInSequence = entry;
            lastInSequence = entry;

            final int bucketIndex = BucketsHashStore.getBucketIndex(hashed, buckets.length);

            Entry previousInLookup = buckets[bucketIndex];

            if (previousInLookup == null) {
                buckets[bucketIndex] = entry;
            } else {
                while (previousInLookup.getNextInLookup() != null) {
                    previousInLookup = previousInLookup.getNextInLookup();
                }

                previousInLookup.setNextInLookup(entry);
            }
        }

        hash.store = new BucketsHashStore(buckets);
        hash.size = size;
        hash.firstInSequence = firstInSequence;
        hash.lastInSequence = lastInSequence;
    }

    // endregion
    // region Messages

    @ExportMessage
    protected static Object lookupOrDefault(
            Object[] store, Frame frame, RubyHash hash, Object key, PEBiFunction defaultNode,
            @Cached LookupPackedEntryNode lookupPackedEntryNode,
            @Cached @Shared("toHash") HashingNodes.ToHash hashNode) {

        int hashed = hashNode.execute(key, hash.compareByIdentity);
        return lookupPackedEntryNode.execute(frame, hash, key, hashed, defaultNode);
    }

    @ExportMessage
    protected static class Set {
        @Specialization(guards = "hash.size == 0")
        protected static boolean setFirst(Object[] store, RubyHash hash, Object key, Object value, boolean byIdentity,
                @Cached @Shared("freeze") FreezeHashKeyIfNeededNode freezeHashKeyIfNeeded,
                @Cached @Shared("toHash") HashingNodes.ToHash hashNode,
                @Cached @Shared("propagateKey") PropagateSharingNode propagateSharingKey,
                @Cached @Shared("propagateValue") PropagateSharingNode propagateSharingValue) {

            final Object key2 = freezeHashKeyIfNeeded.executeFreezeIfNeeded(key, byIdentity);
            propagateSharingKey.executePropagate(hash, key2);
            propagateSharingValue.executePropagate(hash, value);
            setHashedKeyValue(store, 0, hashNode.execute(key2, byIdentity), key2, value);
            hash.size = 1;
            assert verify(store, hash);
            return true;
        }

        @Specialization(guards = "hash.size > 0")
        protected static boolean set(Object[] store, RubyHash hash, Object key, Object value, boolean byIdentity,
                @Cached @Shared("freeze") FreezeHashKeyIfNeededNode freezeHashKeyIfNeeded,
                @Cached @Shared("toHash") HashingNodes.ToHash hashNode,
                @Cached @Shared("propagateKey") PropagateSharingNode propagateSharingKey,
                @Cached @Shared("propagateValue") PropagateSharingNode propagateSharingValue,
                @Cached @Shared("compareHashKeys") CompareHashKeysNode compareHashKeys,
                @CachedLibrary(limit = "2") HashStoreLibrary hashes,
                @Cached ConditionProfile withinCapacity,
                @CachedLanguage RubyLanguage language) {

            assert verify(store, hash);
            final int size = hash.size;
            final Object key2 = freezeHashKeyIfNeeded.executeFreezeIfNeeded(key, byIdentity);
            final int hashed = hashNode.execute(key2, byIdentity);
            propagateSharingKey.executePropagate(hash, key2);
            propagateSharingValue.executePropagate(hash, value);

            // written very carefully to allow PE
            for (int n = 0; n < language.options.HASH_PACKED_ARRAY_MAX; n++) {
                if (n < size) {
                    final int otherHashed = getHashed(store, n);
                    final Object otherKey = getKey(store, n);
                    if (compareHashKeys.execute(byIdentity, key2, hashed, otherKey, otherHashed)) {
                        setValue(store, n, value);
                        return false;
                    }
                }
            }

            if (withinCapacity.profile(size < language.options.HASH_PACKED_ARRAY_MAX)) {
                setHashedKeyValue(store, size, hashed, key2, value);
                hash.size += 1;
                return true;
            }

            promoteToBuckets(hash, store, size);
            hashes.set(hash.store, hash, key2, value, byIdentity);
            return true;
        }
    }

    @ExportMessage
    protected static Object delete(Object[] store, RubyHash hash, Object key,
            @Cached @Shared("toHash") HashingNodes.ToHash hashNode,
            @Cached @Shared("compareHashKeys") CompareHashKeysNode compareHashKeys,
            @CachedLanguage RubyLanguage language) {

        assert verify(store, hash);
        final int hashed = hashNode.execute(key, hash.compareByIdentity);
        final int size = hash.size;
        // written very carefully to allow PE
        for (int n = 0; n < language.options.HASH_PACKED_ARRAY_MAX; n++) {
            if (n < size) {
                final int otherHashed = getHashed(store, n);
                final Object otherKey = getKey(store, n);

                if (compareHashKeys.execute(hash.compareByIdentity, key, hashed, otherKey, otherHashed)) {
                    final Object value = getValue(store, n);
                    removeEntry(language, store, n);
                    hash.size -= 1;
                    return value;
                }
            }
        }
        assert verify(store, hash);
        return null;
    }

    @ExportMessage
    protected static Object deleteLast(Object[] store, RubyHash hash, Object key,
            @CachedLanguage RubyLanguage language) {

        assert verify(store, hash);
        final int n = hash.size - 1;
        final Object lastKey = getKey(store, n);
        if (key != lastKey) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw CompilerDirectives
                    .shouldNotReachHere("The last key was not " + key + " as expected but was " + lastKey);
        }
        final Object value = getValue(store, n);
        removeEntry(language, store, n);
        hash.size -= 1;
        assert verify(store, hash);
        return value;
    }

    @ExportMessage
    @ImportStatic(HashGuards.class)
    static class EachEntry {

        @Specialization(guards = "hash.size == cachedSize", limit = "packedHashLimit()")
        @ExplodeLoop
        protected static Object eachEntry(Object[] store, RubyHash hash, EachEntryCallback callback, Object state,
                @CachedLibrary("store") HashStoreLibrary hashStoreLibrary,
                @Cached(value = "hash.size", allowUncached = true) int cachedSize,
                @Cached LoopConditionProfile loopProfile) {

            // Don't verify hash here, as `store != hash.store` when calling from `eachEntrySafe`.
            loopProfile.profileCounted(cachedSize);
            int i = 0;
            try {
                for (; loopProfile.inject(i < cachedSize); i++) {
                    callback.accept(i, getKey(store, i), getValue(store, i), state);
                }
            } finally {
                LoopNode.reportLoopCount(hashStoreLibrary.getNode(), i);
            }
            return state;
        }
    }

    @ExportMessage
    protected static Object eachEntrySafe(Object[] store, RubyHash hash, EachEntryCallback callback, Object state,
            @CachedLibrary("store") HashStoreLibrary self,
            @CachedLanguage RubyLanguage language) {

        return self.eachEntry(copyStore(language, store), hash, callback, state);
    }

    @ExportMessage
    protected static void replace(Object[] store, RubyHash hash, RubyHash dest,
            @Cached @Exclusive PropagateSharingNode propagateSharing,
            @CachedLanguage RubyLanguage language) {
        if (hash == dest) {
            return;
        }

        propagateSharing.executePropagate(dest, hash);

        Object storeCopy = copyStore(language, store);
        int size = hash.size;
        dest.store = storeCopy;
        dest.size = size;
        dest.firstInSequence = null;
        dest.lastInSequence = null;
        dest.defaultBlock = hash.defaultBlock;
        dest.defaultValue = hash.defaultValue;
        dest.compareByIdentity = hash.compareByIdentity;

        assert verify(store, hash);
    }

    @ExportMessage
    protected static RubyArray shift(Object[] store, RubyHash hash,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        assert verify(store, hash);
        final Object key = getKey(store, 0);
        final Object value = getValue(store, 0);
        removeEntry(language, store, 0);
        hash.size -= 1;
        assert verify(store, hash);
        return ArrayHelpers.createArray(context, language, new Object[]{ key, value });
    }

    @ExportMessage
    protected static void rehash(Object[] store, RubyHash hash,
            @Cached @Shared("compareHashKeys") CompareHashKeysNode compareHashKeys,
            @Cached @Shared("toHash") HashingNodes.ToHash hashNode,
            @CachedLanguage RubyLanguage language) {

        assert verify(store, hash);
        int size = hash.size;
        for (int n = 0; n < size; n++) {
            final Object key = getKey(store, n);
            final int newHash = hashNode.execute(getKey(store, n), hash.compareByIdentity);
            setHashed(store, n, newHash);

            for (int m = n - 1; m >= 0; m--) {
                if (getHashed(store, m) == newHash && compareHashKeys.execute(
                        hash.compareByIdentity,
                        key,
                        newHash,
                        getKey(store, m),
                        getHashed(store, m))) {
                    removeEntry(language, store, n);
                    size--;
                    n--;
                    break;
                }
            }
        }
        hash.size = size;
        assert verify(store, hash);
    }

    @TruffleBoundary
    @ExportMessage
    protected static boolean verify(Object[] store, RubyHash hash) {
        assert hash.store == store;
        final int size = hash.size;
        assert store.length == RubyLanguage.getCurrentLanguage().options.HASH_PACKED_ARRAY_MAX *
                ELEMENTS_PER_ENTRY : store.length;

        final Entry firstInSequence = hash.firstInSequence;
        final Entry lastInSequence = hash.lastInSequence;
        assert firstInSequence == null;
        assert lastInSequence == null;

        for (int i = 0; i < size * ELEMENTS_PER_ENTRY; i++) {
            assert store[i] != null;
        }

        for (int n = 0; n < size; n++) {
            final Object key = getKey(store, n);
            final Object value = getValue(store, n);
            assert SharedObjects.assertPropagateSharing(hash, key) : "unshared key in shared Hash: " + key;
            assert SharedObjects.assertPropagateSharing(hash, value) : "unshared value in shared Hash: " + value;
        }

        return true;
    }

    // endregion
    // region Nodes

    @GenerateUncached
    @ImportStatic(HashGuards.class)
    public abstract static class LookupPackedEntryNode extends RubyBaseNode {

        public abstract Object execute(Frame frame, RubyHash hash, Object key, int hashed, PEBiFunction defaultValue);

        @Specialization(
                guards = {
                        "isCompareByIdentity(hash) == cachedByIdentity",
                        "cachedIndex >= 0",
                        "cachedIndex < hash.size",
                        "sameKeysAtIndex(refEqual, hash, key, hashed, cachedIndex, cachedByIdentity)" },
                limit = "1")
        protected Object getConstantIndexPackedArray(
                RubyHash hash, Object key, int hashed, PEBiFunction defaultValueNode,
                @Cached ReferenceEqualNode refEqual,
                @Cached("isCompareByIdentity(hash)") boolean cachedByIdentity,
                @Cached("index(refEqual, hash, key, hashed, cachedByIdentity)") int cachedIndex) {

            final Object[] store = (Object[]) hash.store;
            return getValue(store, cachedIndex);
        }

        protected int index(ReferenceEqualNode refEqual, RubyHash hash, Object key, int hashed,
                boolean compareByIdentity) {

            final Object[] store = (Object[]) hash.store;
            final int size = hash.size;
            for (int n = 0; n < size; n++) {
                final int otherHashed = getHashed(store, n);
                final Object otherKey = getKey(store, n);
                if (sameKeys(refEqual, compareByIdentity, key, hashed, otherKey, otherHashed)) {
                    return n;
                }
            }
            return -1;
        }

        protected boolean sameKeysAtIndex(ReferenceEqualNode refEqual, RubyHash hash, Object key, int hashed,
                int cachedIndex, boolean cachedByIdentity) {

            final Object[] store = (Object[]) hash.store;
            final Object otherKey = getKey(store, cachedIndex);
            final int otherHashed = getHashed(store, cachedIndex);
            return sameKeys(refEqual, cachedByIdentity, key, hashed, otherKey, otherHashed);
        }

        private boolean sameKeys(ReferenceEqualNode refEqual, boolean compareByIdentity, Object key, int hashed,
                Object otherKey, int otherHashed) {
            return CompareHashKeysNode
                    .referenceEqualKeys(refEqual, compareByIdentity, key, hashed, otherKey, otherHashed);
        }

        @ExplodeLoop(kind = LoopExplosionKind.FULL_UNROLL_UNTIL_RETURN)
        @Specialization(replaces = "getConstantIndexPackedArray")
        protected Object getPackedArray(
                Frame frame, RubyHash hash, Object key, int hashed, PEBiFunction defaultValueNode,
                @Cached CompareHashKeysNode compareHashKeys,
                @Cached BranchProfile notInHashProfile,
                @Cached ConditionProfile byIdentityProfile,
                @CachedLanguage RubyLanguage language) {

            final boolean compareByIdentity = byIdentityProfile.profile(hash.compareByIdentity);
            final Object[] store = (Object[]) hash.store;
            final int size = hash.size;
            for (int n = 0; n < language.options.HASH_PACKED_ARRAY_MAX; n++) {
                if (n < size) {
                    final int otherHashed = getHashed(store, n);
                    final Object otherKey = getKey(store, n);
                    if (equalKeys(compareHashKeys, compareByIdentity, key, hashed, otherKey, otherHashed)) {
                        return getValue(store, n);
                    }
                }
            }

            notInHashProfile.enter();
            return defaultValueNode.accept(frame, hash, key);
        }

        protected boolean equalKeys(CompareHashKeysNode compareHashKeys, boolean compareByIdentity, Object key,
                int hashed, Object otherKey, int otherHashed) {
            return compareHashKeys.execute(compareByIdentity, key, hashed, otherKey, otherHashed);
        }
    }

    public static class SmallHashLiteralNode extends HashLiteralNode {

        @Child private HashingNodes.ToHashByHashCode hashNode;
        @Child private DispatchNode equalNode;
        @Child private BooleanCastNode booleanCastNode;
        @Child private FreezeHashKeyIfNeededNode freezeHashKeyIfNeededNode = FreezeHashKeyIfNeededNodeGen.create();
        private final BranchProfile duplicateKeyProfile = BranchProfile.create();

        public SmallHashLiteralNode(RubyLanguage language, RubyNode[] keyValues) {
            super(language, keyValues);
        }

        @ExplodeLoop
        @Override
        public Object execute(VirtualFrame frame) {
            final Object[] store = createStore(language);
            int size = 0;

            for (int n = 0; n < keyValues.length / 2; n++) {
                Object key = keyValues[n * 2].execute(frame);
                key = freezeHashKeyIfNeededNode.executeFreezeIfNeeded(key, false);

                final int hashed = hash(key);

                final Object value = keyValues[n * 2 + 1].execute(frame);
                boolean duplicateKey = false;

                for (int i = 0; i < n; i++) {
                    if (i < size &&
                            hashed == getHashed(store, i) &&
                            callEqual(key, getKey(store, i))) {
                        duplicateKeyProfile.enter();
                        setKey(store, i, key);
                        setValue(store, i, value);
                        duplicateKey = true;
                        break;
                    }
                }

                if (!duplicateKey) {
                    setHashedKeyValue(store, size, hashed, key, value);
                    size++;
                }
            }

            return new RubyHash(
                    coreLibrary().hashClass,
                    language.hashShape,
                    getContext(),
                    store,
                    size,
                    null,
                    null,
                    nil,
                    nil,
                    false);
        }

        private int hash(Object key) {
            if (hashNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                hashNode = insert(HashingNodes.ToHashByHashCode.create());
            }
            return hashNode.execute(key);
        }

        private boolean callEqual(Object receiver, Object key) {
            if (equalNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                equalNode = insert(DispatchNode.create());
            }

            if (booleanCastNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                booleanCastNode = insert(BooleanCastNode.create());
            }

            return booleanCastNode.executeToBoolean(equalNode.call(receiver, "eql?", key));
        }
    }

    // endregion
}
