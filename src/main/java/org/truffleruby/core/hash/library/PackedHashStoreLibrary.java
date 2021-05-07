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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.CachedLanguage;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.BiConsumerNode;
import org.truffleruby.collections.BiFunctionNode;
import org.truffleruby.core.array.ArrayBuilderNode;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.hash.BucketsStrategy;
import org.truffleruby.core.hash.CompareHashKeysNode;
import org.truffleruby.core.hash.FreezeHashKeyIfNeededNode;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.HashingNodes;
import org.truffleruby.core.hash.LookupPackedEntryNode;
import org.truffleruby.core.hash.PackedArrayStrategy;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.hash.library.HashStoreLibrary.YieldPairNode;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

@ExportLibrary(value = HashStoreLibrary.class, receiverType = Object[].class)
@GenerateUncached
public class PackedHashStoreLibrary {

    @ExportMessage
    protected static Object lookupOrDefault(
            Object[] store, Frame frame, RubyHash hash, Object key, BiFunctionNode defaultNode,
            @Cached LookupPackedEntryNode lookupPackedEntryNode,
            @Cached @Shared("toHash") HashingNodes.ToHash hashNode) {

        int hashed = hashNode.execute(key, hash.compareByIdentity);
        return lookupPackedEntryNode.executePackedLookup(frame, hash, key, hashed, defaultNode);
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
                final int otherHashed = PackedArrayStrategy.getHashed(store, n);
                final Object otherKey = PackedArrayStrategy.getKey(store, n);
                if (compareHashKeys.execute(byIdentity, key2, hashed, otherKey, otherHashed)) {
                    PackedArrayStrategy.setValue(store, n, value);
                    assert HashOperations.verifyStore(context, hash);
                    return false;
                }
            }
        }

        if (strategy.profile(size < language.options.HASH_PACKED_ARRAY_MAX)) {
            PackedArrayStrategy.setHashedKeyValue(store, size, hashed, key2, value);
            hash.size += 1;
            return true;
        } else {
            PackedArrayStrategy.promoteToBuckets(context, hash, store, size);
            BucketsStrategy.addNewEntry(context, hash, hashed, key2, value);
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
                final int otherHashed = PackedArrayStrategy.getHashed(store, n);
                final Object otherKey = PackedArrayStrategy.getKey(store, n);

                if (compareHashKeys.execute(hash.compareByIdentity, key, hashed, otherKey, otherHashed)) {
                    final Object value = PackedArrayStrategy.getValue(store, n);
                    PackedArrayStrategy.removeEntry(language, store, n);
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
    @ImportStatic(HashGuards.class)
    static class EachEntry {

        @Specialization(guards = "hash.size == cachedSize", limit = "packedHashLimit()")
        protected static Object eachEntry(
                Object[] store, Frame frame, RubyHash hash, BiConsumerNode callback, Object state,
                @Cached("hash.size") int cachedSize,
                @CachedContext(RubyLanguage.class) RubyContext context) {

            assert HashOperations.verifyStore(context, hash);
            iterate(store, frame, callback, state, cachedSize);
            return state;
        }

        @Specialization(replaces = "eachEntry")
        protected static Object eachEntryUncached(
                Object[] store, Frame frame, RubyHash hash, BiConsumerNode callback, Object state,
                @CachedContext(RubyLanguage.class) RubyContext context) {

            assert HashOperations.verifyStore(context, hash);
            iterate(store, frame, callback, state, hash.size);
            return state;
        }

        @ExplodeLoop
        private static void iterate(Object[] store, Frame frame, BiConsumerNode callback, Object state, int size) {
            for (int i = 0; i < size; i++) {
                callback.accept(
                        (VirtualFrame) frame,
                        PackedArrayStrategy.getKey(store, i),
                        PackedArrayStrategy.getValue(store, i),
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
        final Object[] storeCopy = PackedArrayStrategy.copyStore(language, store);
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

    public static final class EachCallback implements BiConsumerNode {
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

        Object storeCopy = PackedArrayStrategy.copyStore(language, store);
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
        final Object[] storeCopy = PackedArrayStrategy.copyStore(language, store);
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

    public static final class MapCallback implements BiConsumerNode {
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
        final Object key = PackedArrayStrategy.getKey(store, 0);
        final Object value = PackedArrayStrategy.getValue(store, 0);
        PackedArrayStrategy.removeEntry(language, store, 0);
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
            final Object key = PackedArrayStrategy.getKey(store, n);
            final int newHash = hashNode.execute(PackedArrayStrategy.getKey(store, n), hash.compareByIdentity);
            PackedArrayStrategy.setHashed(store, n, newHash);

            for (int m = n - 1; m >= 0; m--) {
                if (PackedArrayStrategy.getHashed(store, m) == newHash && compareHashKeys.execute(
                        hash.compareByIdentity,
                        key,
                        newHash,
                        PackedArrayStrategy.getKey(store, m),
                        PackedArrayStrategy.getHashed(store, m))) {
                    PackedArrayStrategy.removeEntry(language, store, n);
                    size--;
                    n--;
                    break;
                }
            }
        }
        hash.size = size;
        assert HashOperations.verifyStore(context, hash);
    }
}
