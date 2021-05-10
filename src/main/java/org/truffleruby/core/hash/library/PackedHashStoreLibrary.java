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
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.PEBiConsumer;
import org.truffleruby.collections.PEBiFunction;
import org.truffleruby.core.array.ArrayBuilderNode;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.basicobject.BasicObjectNodes;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.hash.CompareHashKeysNode;
import org.truffleruby.core.hash.Entry;
import org.truffleruby.core.hash.FreezeHashKeyIfNeededNode;
import org.truffleruby.core.hash.FreezeHashKeyIfNeededNodeGen;
import org.truffleruby.core.hash.HashGuards;
import org.truffleruby.core.hash.HashLiteralNode;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.HashingNodes;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.hash.library.HashStoreLibrary.YieldPairNode;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

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

    public static Object getKey(Object[] store, int n) {
        return store[n * ELEMENTS_PER_ENTRY + 1];
    }

    public static Object getValue(Object[] store, int n) {
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
    private static void promoteToBuckets(RubyContext context, RubyHash hash, Object[] store, int size) {
        final Entry[] buckets = new Entry[EntryArrayHashStore.capacityGreaterThan(size)];

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

            final int bucketIndex = EntryArrayHashStore.getBucketIndex(hashed, buckets.length);

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

        hash.store = new EntryArrayHashStore(buckets);
        hash.size = size;
        hash.firstInSequence = firstInSequence;
        hash.lastInSequence = lastInSequence;

        assert HashOperations.verifyStore(context, hash);
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
    protected static boolean set(Object[] store, RubyHash hash, Object key, Object value, boolean byIdentity,
            @Cached FreezeHashKeyIfNeededNode freezeHashKeyIfNeeded,
            @Cached @Shared("toHash") HashingNodes.ToHash hashNode,
            @Cached @Exclusive PropagateSharingNode propagateSharingKey,
            @Cached @Exclusive PropagateSharingNode propagateSharingValue,
            @Cached @Shared("compareHashKeys") CompareHashKeysNode compareHashKeys,
            @Cached @Exclusive ConditionProfile strategy,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        assert HashOperations.verifyStore(context, hash);
        final Object key2 = freezeHashKeyIfNeeded.executeFreezeIfNeeded(key, byIdentity);

        final int hashed = hashNode.execute(key2, byIdentity);

        propagateSharingKey.executePropagate(hash, key2);
        propagateSharingValue.executePropagate(hash, value);

        final int size = hash.size;

        // written very carefully to allow PE
        for (int n = 0; n < language.options.HASH_PACKED_ARRAY_MAX; n++) {
            if (n < size) {
                final int otherHashed = getHashed(store, n);
                final Object otherKey = getKey(store, n);
                if (compareHashKeys.execute(byIdentity, key2, hashed, otherKey, otherHashed)) {
                    setValue(store, n, value);
                    assert HashOperations.verifyStore(context, hash);
                    return false;
                }
            }
        }

        if (strategy.profile(size < language.options.HASH_PACKED_ARRAY_MAX)) {
            setHashedKeyValue(store, size, hashed, key2, value);
            hash.size += 1;
            return true;
        } else {
            promoteToBuckets(context, hash, store, size);
            EntryArrayHashStore.addNewEntry(context, hash, hashed, key2, value);
        }

        assert HashOperations.verifyStore(context, hash);
        return true;
    }

    @ExportMessage
    protected static Object delete(Object[] store, RubyHash hash, Object key,
            @Cached @Shared("toHash") HashingNodes.ToHash hashNode,
            @Cached @Shared("compareHashKeys") CompareHashKeysNode compareHashKeys,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        assert HashOperations.verifyStore(context, hash);
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
                    assert HashOperations.verifyStore(context, hash);
                    return value;
                }
            }
        }
        assert HashOperations.verifyStore(context, hash);
        return null;
    }

    @ExportMessage
    protected static Object deleteLast(Object[] store, RubyHash hash, Object key,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        assert HashOperations.verifyStore(context, hash);
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
        assert HashOperations.verifyStore(context, hash);
        return value;
    }

    @ExportMessage
    @ImportStatic(HashGuards.class)
    static class EachEntry {

        @Specialization(guards = "hash.size == cachedSize", limit = "packedHashLimit()")
        protected static Object eachEntry(
                Object[] store, Frame frame, RubyHash hash, PEBiConsumer callback, Object state,
                @Cached("hash.size") int cachedSize,
                @CachedContext(RubyLanguage.class) RubyContext context) {

            assert HashOperations.verifyStore(context, hash);
            iterate(store, frame, callback, state, cachedSize);
            return state;
        }

        @Specialization(replaces = "eachEntry")
        protected static Object eachEntryUncached(
                Object[] store, Frame frame, RubyHash hash, PEBiConsumer callback, Object state,
                @CachedContext(RubyLanguage.class) RubyContext context) {

            assert HashOperations.verifyStore(context, hash);
            iterate(store, frame, callback, state, hash.size);
            return state;
        }

        @ExplodeLoop
        private static void iterate(Object[] store, Frame frame, PEBiConsumer callback, Object state, int size) {
            for (int i = 0; i < size; i++) {
                callback.accept(
                        (VirtualFrame) frame,
                        getKey(store, i),
                        getValue(store, i),
                        state);
            }
        }
    }

    @ExportMessage
    protected static void each(Object[] store, RubyHash hash, RubyProc block,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached @Shared("yield") YieldPairNode yieldPair,
            @CachedLibrary("store") HashStoreLibrary self) {

        assert HashOperations.verifyStore(context, hash);
        // Iterate on a copy to allow Hash#delete while iterating, MRI explicitly allows this behavior
        final Object[] storeCopy = copyStore(language, store);
        // TODO: should we pass the block as the state and cache the EachCallback instance instead?
        self.eachEntry(storeCopy, null, hash, new EachCallback(yieldPair, block), null);

        // TODO: old implementation, minus verifyStore
        //   Is there some part of that (including the peculiar loop, use of HASH_PACKED_ARRAY_MAX and reportLoop)
        //   that needs to be ported to eachEntry? (which explodes on all possible sizes, excepted for uncached)

        //        // Iterate on a copy to allow Hash#delete while iterating, MRI explicitly allows this behavior
        //        final int size = hash.size;
        //        final Object[] storeCopy = PackedArrayStrategy.copyStore(language, store);
        //        int n = 0;
        //        try {
        //            for (; n < language.options.HASH_PACKED_ARRAY_MAX; n++) {
        //                if (n < size) {
        //                    yieldPair.execute(
        //                            block,
        //                            PackedArrayStrategy.getKey(storeCopy, n),
        //                            PackedArrayStrategy.getValue(storeCopy, n));
        //                }
        //            }
        //        } finally {
        //            HashStoreLibrary.reportLoopCount(self, n);
        //        }
    }

    public static final class EachCallback implements PEBiConsumer {
        private final YieldPairNode yieldPair;
        private final RubyProc block;

        public EachCallback(YieldPairNode yieldPair, RubyProc block) {
            this.yieldPair = yieldPair;
            this.block = block;
        }

        @Override
        public final void accept(VirtualFrame frame, Object key, Object value, Object state) {
            yieldPair.execute(block, key, value);
        }
    }

    @ExportMessage
    protected static void replace(Object[] store, RubyHash hash, RubyHash dest,
            @Cached @Exclusive PropagateSharingNode propagateSharing,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context) {
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

        assert HashOperations.verifyStore(context, dest);
    }

    @ExportMessage
    protected static RubyArray map(Object[] store, RubyHash hash, RubyProc block,
            @Cached ArrayBuilderNode arrayBuilder,
            @Cached @Shared("yield") YieldPairNode yieldPair,
            @CachedLibrary("store") HashStoreLibrary self,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        assert HashOperations.verifyStore(context, hash);
        // Iterate on a copy to allow Hash#delete while iterating, MRI explicitly allows this behavior
        final Object[] storeCopy = copyStore(language, store);
        final int size = hash.size;
        final ArrayBuilderNode.BuilderState state = arrayBuilder.start(size);
        // TODO: - should we pass the parameters as a state and cache the EachCallback instance instead?
        //       - should we pass make a builder state a MapCallback field?
        self.eachEntry(storeCopy, null, hash, new MapCallback(yieldPair, block, arrayBuilder), state);
        return ArrayHelpers.createArray(context, language, arrayBuilder.finish(state, size), size);

        // TODO: old implementation, minus verifyStore
        //   Is there some part of that (including the peculiar loop, use of HASH_PACKED_ARRAY_MAX and reportLoop)
        //   that needs to be ported to eachEntry? (which explodes on all possible sizes, excepted for uncached)

        //        // Iterate on a copy to allow Hash#delete while iterating, MRI explicitly allows this behavior
        //        final int size = hash.size;
        //        final Object[] storeCopy = PackedArrayStrategy.copyStore(language, store);
        //        final ArrayBuilderNode.BuilderState state = arrayBuilder.start(size);
        //        try {
        //            for (int n = 0; n < language.options.HASH_PACKED_ARRAY_MAX; n++) {
        //                if (n < size) {
        //                    final Object key = PackedArrayStrategy.getKey(storeCopy, n);
        //                    final Object value = PackedArrayStrategy.getValue(storeCopy, n);
        //                    arrayBuilder.appendValue(state, n, yieldPair.execute(block, key, value));
        //                }
        //            }
        //        } finally {
        //            HashStoreLibrary.reportLoopCount(self, size);
        //        }
        //
        //        return ArrayHelpers.createArray(context, language, arrayBuilder.finish(state, size), size);
    }

    public static final class MapCallback implements PEBiConsumer {
        private final YieldPairNode yieldPair;
        private final RubyProc block;
        private final ArrayBuilderNode arrayBuilder;
        private int i = 0;

        public MapCallback(YieldPairNode yieldPair, RubyProc block, ArrayBuilderNode arrayBuilder) {
            this.yieldPair = yieldPair;
            this.block = block;
            this.arrayBuilder = arrayBuilder;
        }

        @Override
        public final void accept(VirtualFrame frame, Object key, Object value, Object state) {
            // TODO: will the i++ here not ruin PE?
            arrayBuilder.appendValue((ArrayBuilderNode.BuilderState) state, i++, yieldPair.execute(block, key, value));
        }
    }

    @ExportMessage
    protected static RubyArray shift(Object[] store, RubyHash hash,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        assert HashOperations.verifyStore(context, hash);
        final Object key = getKey(store, 0);
        final Object value = getValue(store, 0);
        removeEntry(language, store, 0);
        hash.size -= 1;
        assert HashOperations.verifyStore(context, hash);
        return ArrayHelpers.createArray(context, language, new Object[]{ key, value });
    }

    @ExportMessage
    protected static void rehash(Object[] store, RubyHash hash,
            @Cached @Shared("compareHashKeys") CompareHashKeysNode compareHashKeys,
            @Cached @Shared("toHash") HashingNodes.ToHash hashNode,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        assert HashOperations.verifyStore(context, hash);
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
        assert HashOperations.verifyStore(context, hash);
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
                @Cached BasicObjectNodes.ReferenceEqualNode refEqual,
                @Cached("isCompareByIdentity(hash)") boolean cachedByIdentity,
                @Cached("index(refEqual, hash, key, hashed, cachedByIdentity)") int cachedIndex) {

            final Object[] store = (Object[]) hash.store;
            return getValue(store, cachedIndex);
        }

        protected int index(BasicObjectNodes.ReferenceEqualNode refEqual, RubyHash hash, Object key, int hashed,
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

        protected boolean sameKeysAtIndex(BasicObjectNodes.ReferenceEqualNode refEqual, RubyHash hash, Object key,
                int hashed,
                int cachedIndex, boolean cachedByIdentity) {

            final Object[] store = (Object[]) hash.store;
            final Object otherKey = getKey(store, cachedIndex);
            final int otherHashed = getHashed(store, cachedIndex);
            return sameKeys(refEqual, cachedByIdentity, key, hashed, otherKey, otherHashed);
        }

        private boolean sameKeys(BasicObjectNodes.ReferenceEqualNode refEqual, boolean compareByIdentity, Object key,
                int hashed,
                Object otherKey, int otherHashed) {
            return CompareHashKeysNode
                    .referenceEqualKeys(refEqual, compareByIdentity, key, hashed, otherKey, otherHashed);
        }

        @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.FULL_UNROLL_UNTIL_RETURN)
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
            // frame should be virtual or null
            return defaultValueNode.accept((VirtualFrame) frame, hash, key);
        }

        protected boolean equalKeys(CompareHashKeysNode compareHashKeys, boolean compareByIdentity, Object key,
                int hashed,
                Object otherKey, int otherHashed) {
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
