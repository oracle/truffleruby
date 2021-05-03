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
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.EncapsulatingNodeReference;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.BiFunctionNode;
import org.truffleruby.core.hash.BucketsStrategy;
import org.truffleruby.core.hash.CompareHashKeysNode;
import org.truffleruby.core.hash.FreezeHashKeyIfNeededNode;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.HashingNodes;
import org.truffleruby.core.hash.LookupPackedEntryNode;
import org.truffleruby.core.hash.PackedArrayStrategy;
import org.truffleruby.core.hash.RubyHash;
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
            @Cached @Shared("byIdentity") ConditionProfile byIdentityProfile,
            @Cached FreezeHashKeyIfNeededNode freezeHashKeyIfNeeded,
            @Cached @Shared("toHash") HashingNodes.ToHash hashNode,
            @Cached PropagateSharingNode propagateSharingKey,
            @Cached PropagateSharingNode propagateSharingValue,
            @Cached @Shared("compareHashKeys") CompareHashKeysNode compareHashKeys,
            @Cached @Exclusive ConditionProfile strategy,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        assert HashOperations.verifyStore(context, hash);
        final boolean compareByIdentity = byIdentityProfile.profile(byIdentity);
        final Object key2 = freezeHashKeyIfNeeded.executeFreezeIfNeeded(key, compareByIdentity);

        final int hashed = hashNode.execute(key2, compareByIdentity);

        propagateSharingKey.executePropagate(hash, key2);
        propagateSharingValue.executePropagate(hash, value);

        final int size = hash.size;

        // written very carefully to allow PE
        for (int n = 0; n < language.options.HASH_PACKED_ARRAY_MAX; n++) {
            if (n < size) {
                final int otherHashed = PackedArrayStrategy.getHashed(store, n);
                final Object otherKey = PackedArrayStrategy.getKey(store, n);
                if (compareHashKeys.execute(compareByIdentity, key2, hashed, otherKey, otherHashed)) {
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
            @Cached @Shared("byIdentity") ConditionProfile byIdentityProfile,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        assert HashOperations.verifyStore(context, hash);
        final boolean compareByIdentity = byIdentityProfile.profile(hash.compareByIdentity);
        final int hashed = hashNode.execute(key, compareByIdentity);
        final int size = hash.size;

        // written very carefully to allow PE
        for (int n = 0; n < language.options.HASH_PACKED_ARRAY_MAX; n++) {
            if (n < size) {
                final int otherHashed = PackedArrayStrategy.getHashed(store, n);
                final Object otherKey = PackedArrayStrategy.getKey(store, n);

                if (compareHashKeys.execute(compareByIdentity, key, hashed, otherKey, otherHashed)) {
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
    protected static void each(Object[] store, RubyHash hash, RubyProc block,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached HashStoreLibrary.YieldPairNode yieldPair,
            @CachedLibrary("store") HashStoreLibrary self) {

        assert HashOperations.verifyStore(context, hash);

        // Iterate on a copy to allow Hash#delete while iterating, MRI explicitly allows this behavior
        final int size = hash.size;
        final Object[] storeCopy = PackedArrayStrategy.copyStore(language, store);

        int n = 0;
        try {
            for (; n < language.options.HASH_PACKED_ARRAY_MAX; n++) {
                if (n < size) {
                    yieldPair.execute(
                            block,
                            PackedArrayStrategy.getKey(storeCopy, n),
                            PackedArrayStrategy.getValue(storeCopy, n));
                }
            }
        } finally {
            final Node node = self.isAdoptable() ? self : EncapsulatingNodeReference.getCurrent().get();
            LoopNode.reportLoopCount(node, n);
        }
    }
}
