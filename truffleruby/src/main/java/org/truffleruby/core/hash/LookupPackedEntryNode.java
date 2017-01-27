/*
 * Copyright (c) 2014, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.hash;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchHeadNodeFactory;

@NodeChildren({
        @NodeChild("hash"),
        @NodeChild("key"),
        @NodeChild("hashed"),
})
@ImportStatic(HashGuards.class)
public abstract class LookupPackedEntryNode extends RubyNode {

    @Child CompareHashKeysNode compareHashKeysNode = new CompareHashKeysNode();
    @Child private CallDispatchHeadNode callDefaultNode = DispatchHeadNodeFactory.createMethodCall();

    private final Object undefinedValue;

    public static LookupPackedEntryNode create(Object undefinedValue) {
        return LookupPackedEntryNodeGen.create(undefinedValue, null, null, null);
    }

    public LookupPackedEntryNode(Object undefinedValue) {
        this.undefinedValue = undefinedValue;
    }

    public abstract Object executePackedLookup(VirtualFrame frame, DynamicObject hash, Object key, int hashed);

    @Specialization(guards = {
            "isCompareByIdentity(hash) == cachedByIdentity",
            "cachedIndex >= 0",
            "cachedIndex < getSize(hash)",
            "compareKeysAtIndex(frame, hash, key, hashed, cachedIndex, cachedByIdentity)"
    }, limit = "1")
    public Object getConstantIndexPackedArray(VirtualFrame frame, DynamicObject hash, Object key, int hashed,
            @Cached("index(frame, hash, key, hashed)") int cachedIndex,
            @Cached("isCompareByIdentity(hash)") boolean cachedByIdentity) {
        final Object[] store = (Object[]) Layouts.HASH.getStore(hash);
        return PackedArrayStrategy.getValue(store, cachedIndex);
    }

    protected int index(VirtualFrame frame, DynamicObject hash, Object key, int hashed) {
        if (!HashGuards.isPackedHash(hash)) {
            return -1;
        }

        boolean compareByIdentity = Layouts.HASH.getCompareByIdentity(hash);

        final Object[] store = (Object[]) Layouts.HASH.getStore(hash);
        final int size = Layouts.HASH.getSize(hash);

        for (int n = 0; n < size; n++) {
            final int otherHashed = PackedArrayStrategy.getHashed(store, n);
            final Object otherKey = PackedArrayStrategy.getKey(store, n);
            if (equalKeys(frame, compareByIdentity, key, hashed, otherKey, otherHashed)) {
                return n;
            }
        }

        return -1;
    }

    protected boolean compareKeysAtIndex(VirtualFrame frame, DynamicObject hash, Object key, int hashed, int cachedIndex, boolean cachedByIdentity) {
        final Object[] store = (Object[]) Layouts.HASH.getStore(hash);
        Object other = PackedArrayStrategy.getKey(store, cachedIndex);
        int otherHashed = PackedArrayStrategy.getHashed(store, cachedIndex);
        return equalKeys(frame, cachedByIdentity, key, hashed, other, otherHashed);
    }

    protected int getSize(DynamicObject hash) {
        return Layouts.HASH.getSize(hash);
    }

    protected boolean equalKeys(VirtualFrame frame, boolean compareByIdentity, Object key, int hashed, Object otherKey, int otherHashed) {
        return compareHashKeysNode.equalKeys(frame, compareByIdentity, key, hashed, otherKey, otherHashed);
    }

    @ExplodeLoop
    @Specialization(replaces = "getConstantIndexPackedArray")
    public Object getPackedArray(VirtualFrame frame, DynamicObject hash, Object key, int hashed,
            @Cached("create()") BranchProfile notInHashProfile,
            @Cached("create()") BranchProfile useDefaultProfile,
            @Cached("createBinaryProfile()") ConditionProfile byIdentityProfile) {
        final boolean compareByIdentity = byIdentityProfile.profile(Layouts.HASH.getCompareByIdentity(hash));

        final Object[] store = (Object[]) Layouts.HASH.getStore(hash);
        final int size = Layouts.HASH.getSize(hash);

        for (int n = 0; n < getContext().getOptions().HASH_PACKED_ARRAY_MAX; n++) {
            if (n < size) {
                final int otherHashed = PackedArrayStrategy.getHashed(store, n);
                final Object otherKey = PackedArrayStrategy.getKey(store, n);
                if (equalKeys(frame, compareByIdentity, key, hashed, otherKey, otherHashed)) {
                    return PackedArrayStrategy.getValue(store, n);
        }
            }
        }

        notInHashProfile.enter();

        if (undefinedValue != null) {
            return undefinedValue;
        }

        useDefaultProfile.enter();
        return callDefaultNode.call(frame, hash, "default", key);

    }

}
