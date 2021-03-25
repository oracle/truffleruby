/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import com.oracle.truffle.api.dsl.CachedLanguage;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.BiFunctionNode;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.ExplodeLoop.LoopExplosionKind;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

@GenerateUncached
@ImportStatic(HashGuards.class)
public abstract class LookupPackedEntryNode extends RubyBaseNode {

    public static LookupPackedEntryNode create() {
        return LookupPackedEntryNodeGen.create();
    }

    public abstract Object executePackedLookup(Frame frame, RubyHash hash, Object key, int hashed,
            BiFunctionNode defaultValueNode);

    @Specialization(
            guards = {
                    "isCompareByIdentity(hash) == cachedByIdentity",
                    "cachedIndex >= 0",
                    "cachedIndex < getSize(hash)",
                    "sameKeysAtIndex(compareHashKeys, hash, key, hashed, cachedIndex, cachedByIdentity)" },
            limit = "1")
    protected Object getConstantIndexPackedArray(RubyHash hash, Object key, int hashed, BiFunctionNode defaultValueNode,
            @Cached CompareHashKeysNode compareHashKeys,
            @Cached("isCompareByIdentity(hash)") boolean cachedByIdentity,
            @Cached("index(compareHashKeys, hash, key, hashed, cachedByIdentity)") int cachedIndex) {
        final Object[] store = (Object[]) hash.store;
        return PackedArrayStrategy.getValue(store, cachedIndex);
    }

    protected int index(CompareHashKeysNode compareHashKeys, RubyHash hash, Object key, int hashed,
            boolean compareByIdentity) {

        if (!HashGuards.isPackedHash(hash)) {
            return -1;
        }

        final Object[] store = (Object[]) hash.store;
        final int size = hash.size;

        for (int n = 0; n < size; n++) {
            final int otherHashed = PackedArrayStrategy.getHashed(store, n);
            final Object otherKey = PackedArrayStrategy.getKey(store, n);
            if (sameKeys(compareHashKeys, compareByIdentity, key, hashed, otherKey, otherHashed)) {
                return n;
            }
        }

        return -1;
    }

    protected boolean sameKeysAtIndex(CompareHashKeysNode compareHashKeys, RubyHash hash, Object key, int hashed,
            int cachedIndex, boolean cachedByIdentity) {
        final Object[] store = (Object[]) hash.store;
        final Object otherKey = PackedArrayStrategy.getKey(store, cachedIndex);
        final int otherHashed = PackedArrayStrategy.getHashed(store, cachedIndex);

        return sameKeys(compareHashKeys, cachedByIdentity, key, hashed, otherKey, otherHashed);
    }

    private boolean sameKeys(CompareHashKeysNode compareHashKeys, boolean compareByIdentity, Object key, int hashed,
            Object otherKey, int otherHashed) {
        return compareHashKeys.referenceEqualKeys(compareByIdentity, key, hashed, otherKey, otherHashed);
    }

    protected int getSize(RubyHash hash) {
        return hash.size;
    }

    @ExplodeLoop(kind = LoopExplosionKind.FULL_UNROLL_UNTIL_RETURN)
    @Specialization(replaces = "getConstantIndexPackedArray")
    protected Object getPackedArray(Frame frame, RubyHash hash, Object key, int hashed, BiFunctionNode defaultValueNode,
            @Cached CompareHashKeysNode compareHashKeys,
            @Cached BranchProfile notInHashProfile,
            @Cached ConditionProfile byIdentityProfile,
            @CachedLanguage RubyLanguage language) {
        final boolean compareByIdentity = byIdentityProfile.profile(hash.compareByIdentity);

        final Object[] store = (Object[]) hash.store;
        final int size = hash.size;

        for (int n = 0; n < language.options.HASH_PACKED_ARRAY_MAX; n++) {
            if (n < size) {
                final int otherHashed = PackedArrayStrategy.getHashed(store, n);
                final Object otherKey = PackedArrayStrategy.getKey(store, n);
                if (equalKeys(compareHashKeys, compareByIdentity, key, hashed, otherKey, otherHashed)) {
                    return PackedArrayStrategy.getValue(store, n);
                }
            }
        }

        notInHashProfile.enter();
        // frame should be virtual or null
        return defaultValueNode.accept((VirtualFrame) frame, hash, key);
    }

    protected boolean equalKeys(CompareHashKeysNode compareHashKeys, boolean compareByIdentity, Object key, int hashed,
            Object otherKey, int otherHashed) {
        return compareHashKeys.equalKeys(compareByIdentity, key, hashed, otherKey, otherHashed);
    }

}
