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
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.CachedLanguage;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
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
import org.truffleruby.language.objects.shared.PropagateSharingNode;

@ExportLibrary(value = HashStoreLibrary.class, receiverType = Object[].class)
@GenerateUncached
public class PackedHashStoreLibrary {

    @ExportMessage
    protected static Object lookupOrDefault(
            Object[] store, Frame frame, RubyHash hash, Object key, BiFunctionNode defaultNode,
            @Cached LookupPackedEntryNode lookupPackedEntryNode,
            @Cached @Exclusive HashingNodes.ToHash hashNode) {

        int hashed = hashNode.execute(key, hash.compareByIdentity);
        return lookupPackedEntryNode.executePackedLookup((VirtualFrame) frame, hash, key, hashed, defaultNode);
    }

    @ExportMessage
    protected static boolean set(Object[] store, RubyHash hash, Object key, Object value, boolean byIdentity,
            @Cached ConditionProfile byIdentityProfile,
            @Cached FreezeHashKeyIfNeededNode freezeHashKeyIfNeeded,
            @Cached @Exclusive HashingNodes.ToHash hashNode,
            @Cached PropagateSharingNode propagateSharingKey,
            @Cached PropagateSharingNode propagateSharingValue,
            @Cached CompareHashKeysNode compareHashKeys,
            @Cached ConditionProfile strategy,
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
}
