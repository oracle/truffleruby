/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

@ImportStatic(HashGuards.class)
public abstract class SetNode extends RubyBaseNode {

    @Child private HashNode hashNode = new HashNode();
    @Child private LookupEntryNode lookupEntryNode;
    @Child private CompareHashKeysNode compareHashKeysNode = new CompareHashKeysNode();
    @Child private FreezeHashKeyIfNeededNode freezeHashKeyIfNeededNode = FreezeHashKeyIfNeededNodeGen.create();
    @Child private PropagateSharingNode propagateSharingKeyNode = PropagateSharingNode.create();
    @Child private PropagateSharingNode propagateSharingValueNode = PropagateSharingNode.create();
    private final ConditionProfile byIdentityProfile = ConditionProfile.createBinaryProfile();

    public static SetNode create() {
        return SetNodeGen.create();
    }

    public abstract Object executeSet(DynamicObject hash, Object key, Object value, boolean byIdentity);

    @Specialization(guards = "isNullHash(hash)")
    protected Object setNull(DynamicObject hash, Object originalKey, Object value, boolean byIdentity) {
        assert HashOperations.verifyStore(getContext(), hash);
        boolean compareByIdentity = byIdentityProfile.profile(byIdentity);
        final Object key = freezeHashKeyIfNeededNode.executeFreezeIfNeeded(originalKey, compareByIdentity);

        final int hashed = hashNode.hash(key, compareByIdentity);

        propagateSharingKeyNode.propagate(hash, key);
        propagateSharingValueNode.propagate(hash, value);

        Object store = PackedArrayStrategy.createStore(getContext(), hashed, key, value);
        Layouts.HASH.setStore(hash, store);
        Layouts.HASH.setSize(hash, 1);
        Layouts.HASH.setFirstInSequence(hash, null);
        Layouts.HASH.setLastInSequence(hash, null);

        assert HashOperations.verifyStore(getContext(), hash);
        return value;
    }

    @ExplodeLoop
    @Specialization(guards = "isPackedHash(hash)")
    protected Object setPackedArray(DynamicObject hash, Object originalKey, Object value, boolean byIdentity,
            @Cached("createBinaryProfile()") ConditionProfile strategyProfile) {
        assert HashOperations.verifyStore(getContext(), hash);
        final boolean compareByIdentity = byIdentityProfile.profile(byIdentity);
        final Object key = freezeHashKeyIfNeededNode.executeFreezeIfNeeded(originalKey, compareByIdentity);

        final int hashed = hashNode.hash(key, compareByIdentity);

        propagateSharingKeyNode.propagate(hash, key);
        propagateSharingValueNode.propagate(hash, value);

        final Object[] store = (Object[]) Layouts.HASH.getStore(hash);
        final int size = Layouts.HASH.getSize(hash);

        // written very carefully to allow PE
        for (int n = 0; n < getContext().getOptions().HASH_PACKED_ARRAY_MAX; n++) {
            if (n < size) {
                final int otherHashed = PackedArrayStrategy.getHashed(store, n);
                final Object otherKey = PackedArrayStrategy.getKey(store, n);
                if (equalKeys(compareByIdentity, key, hashed, otherKey, otherHashed)) {
                    PackedArrayStrategy.setValue(store, n, value);
                    assert HashOperations.verifyStore(getContext(), hash);
                    return value;
                }
            }
        }

        if (strategyProfile.profile(size < getContext().getOptions().HASH_PACKED_ARRAY_MAX)) {
            PackedArrayStrategy.setHashedKeyValue(store, size, hashed, key, value);
            Layouts.HASH.setSize(hash, size + 1);
            return value;
        } else {
            PackedArrayStrategy.promoteToBuckets(getContext(), hash, store, size);
            BucketsStrategy.addNewEntry(getContext(), hash, hashed, key, value);
        }

        assert HashOperations.verifyStore(getContext(), hash);

        return value;
    }

    @Specialization(guards = "isBucketHash(hash)")
    protected Object setBuckets(DynamicObject hash, Object originalKey, Object value, boolean byIdentity,
            @Cached("createBinaryProfile()") ConditionProfile foundProfile,
            @Cached("createBinaryProfile()") ConditionProfile bucketCollisionProfile,
            @Cached("createBinaryProfile()") ConditionProfile appendingProfile,
            @Cached("createBinaryProfile()") ConditionProfile resizeProfile) {
        assert HashOperations.verifyStore(getContext(), hash);
        final boolean compareByIdentity = byIdentityProfile.profile(byIdentity);
        final Object key = freezeHashKeyIfNeededNode.executeFreezeIfNeeded(originalKey, compareByIdentity);

        propagateSharingKeyNode.propagate(hash, key);
        propagateSharingValueNode.propagate(hash, value);

        final HashLookupResult result = lookup(hash, key);
        final Entry entry = result.getEntry();

        if (foundProfile.profile(entry == null)) {
            final Entry[] entries = (Entry[]) Layouts.HASH.getStore(hash);

            final Entry newEntry = new Entry(result.getHashed(), key, value);

            if (bucketCollisionProfile.profile(result.getPreviousEntry() == null)) {
                entries[result.getIndex()] = newEntry;
            } else {
                result.getPreviousEntry().setNextInLookup(newEntry);
            }

            final Entry lastInSequence = Layouts.HASH.getLastInSequence(hash);

            if (appendingProfile.profile(lastInSequence == null)) {
                Layouts.HASH.setFirstInSequence(hash, newEntry);
            } else {
                lastInSequence.setNextInSequence(newEntry);
                newEntry.setPreviousInSequence(lastInSequence);
            }

            Layouts.HASH.setLastInSequence(hash, newEntry);

            final int newSize = Layouts.HASH.getSize(hash) + 1;

            Layouts.HASH.setSize(hash, newSize);

            // TODO CS 11-May-15 could store the next size for resize instead of doing a float operation each time

            if (resizeProfile.profile(newSize / (double) entries.length > BucketsStrategy.LOAD_FACTOR)) {
                BucketsStrategy.resize(getContext(), hash);
            }
        } else {
            entry.setValue(value);
        }

        assert HashOperations.verifyStore(getContext(), hash);

        return value;
    }

    private HashLookupResult lookup(DynamicObject hash, Object key) {
        if (lookupEntryNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            lookupEntryNode = insert(new LookupEntryNode());
        }
        return lookupEntryNode.lookup(hash, key);
    }

    protected boolean equalKeys(boolean compareByIdentity, Object key, int hashed, Object otherKey, int otherHashed) {
        return compareHashKeysNode.equalKeys(compareByIdentity, key, hashed, otherKey, otherHashed);
    }

}
