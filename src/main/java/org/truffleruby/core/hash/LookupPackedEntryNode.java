/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import org.truffleruby.Layouts;
import org.truffleruby.collections.BiFunctionNode;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

@ImportStatic(HashGuards.class)
public abstract class LookupPackedEntryNode extends RubyBaseNode {

    @Child CompareHashKeysNode compareHashKeysNode = new CompareHashKeysNode();

    public static LookupPackedEntryNode create() {
        return LookupPackedEntryNodeGen.create();
    }

    public abstract Object executePackedLookup(VirtualFrame frame, DynamicObject hash, Object key, int hashed, BiFunctionNode defaultValueNode);

    @Specialization(guards = {
            "isCompareByIdentity(hash) == cachedByIdentity",
            "cachedIndex >= 0",
            "cachedIndex < getSize(hash)",
            "sameKeysAtIndex(hash, key, hashed, cachedIndex, cachedByIdentity)"
    }, limit = "1")
    protected Object getConstantIndexPackedArray(DynamicObject hash, Object key, int hashed, BiFunctionNode defaultValueNode,
            @Cached("index(hash, key, hashed)") int cachedIndex,
            @Cached("isCompareByIdentity(hash)") boolean cachedByIdentity) {
        final Object[] store = (Object[]) Layouts.HASH.getStore(hash);
        return PackedArrayStrategy.getValue(store, cachedIndex);
    }

    protected int index(DynamicObject hash, Object key, int hashed) {
        if (!HashGuards.isPackedHash(hash)) {
            return -1;
        }

        boolean compareByIdentity = Layouts.HASH.getCompareByIdentity(hash);

        final Object[] store = (Object[]) Layouts.HASH.getStore(hash);
        final int size = Layouts.HASH.getSize(hash);

        for (int n = 0; n < size; n++) {
            final int otherHashed = PackedArrayStrategy.getHashed(store, n);
            final Object otherKey = PackedArrayStrategy.getKey(store, n);
            if (sameKeys(compareByIdentity, key, hashed, otherKey, otherHashed)) {
                return n;
            }
        }

        return -1;
    }

    protected boolean sameKeysAtIndex(DynamicObject hash, Object key, int hashed, int cachedIndex, boolean cachedByIdentity) {
        final Object[] store = (Object[]) Layouts.HASH.getStore(hash);
        final Object otherKey = PackedArrayStrategy.getKey(store, cachedIndex);
        final int otherHashed = PackedArrayStrategy.getHashed(store, cachedIndex);

        return sameKeys(cachedByIdentity, key, hashed, otherKey, otherHashed);
    }

    private boolean sameKeys(boolean compareByIdentity, Object key, int hashed, Object otherKey, int otherHashed) {
        return compareHashKeysNode.referenceEqualKeys(compareByIdentity, key, hashed, otherKey, otherHashed);
    }

    protected int getSize(DynamicObject hash) {
        return Layouts.HASH.getSize(hash);
    }

    @ExplodeLoop
    @Specialization(replaces = "getConstantIndexPackedArray")
    protected Object getPackedArray(VirtualFrame frame, DynamicObject hash, Object key, int hashed, BiFunctionNode defaultValueNode,
            @Cached BranchProfile notInHashProfile,
            @Cached("createBinaryProfile()") ConditionProfile byIdentityProfile) {
        final boolean compareByIdentity = byIdentityProfile.profile(Layouts.HASH.getCompareByIdentity(hash));

        final Object[] store = (Object[]) Layouts.HASH.getStore(hash);
        final int size = Layouts.HASH.getSize(hash);

        for (int n = 0; n < getContext().getOptions().HASH_PACKED_ARRAY_MAX; n++) {
            if (n < size) {
                final int otherHashed = PackedArrayStrategy.getHashed(store, n);
                final Object otherKey = PackedArrayStrategy.getKey(store, n);
                if (equalKeys(compareByIdentity, key, hashed, otherKey, otherHashed)) {
                    return PackedArrayStrategy.getValue(store, n);
                }
            }
        }

        notInHashProfile.enter();
        return defaultValueNode.accept(frame, hash, key);

    }

    protected boolean equalKeys(boolean compareByIdentity, Object key, int hashed, Object otherKey, int otherHashed) {
        return compareHashKeysNode.equalKeys(compareByIdentity, key, hashed, otherKey, otherHashed);
    }

}
