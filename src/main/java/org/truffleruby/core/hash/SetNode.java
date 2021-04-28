/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.ExplodeLoop.LoopExplosionKind;
import com.oracle.truffle.api.profiles.ConditionProfile;

/** On execution SetNode performs Hash#[]= and returns true if the key is newly added to hash, and returns false if the
 * key was found in the hash prior. This is different from the return value of Hash#[]=, which returns the right hand
 * side value, however this change does a set and returns the result of get without an extra lookup. */
@ImportStatic(HashGuards.class)
public abstract class SetNode extends RubyContextNode {

    @Child private HashingNodes.ToHash hashNode = HashingNodes.ToHash.create();
    @Child private LookupEntryNode lookupEntryNode;
    @Child private CompareHashKeysNode compareHashKeysNode = CompareHashKeysNode.create();
    @Child private FreezeHashKeyIfNeededNode freezeHashKeyIfNeededNode = FreezeHashKeyIfNeededNodeGen.create();
    @Child private PropagateSharingNode propagateSharingKeyNode = PropagateSharingNode.create();
    @Child private PropagateSharingNode propagateSharingValueNode = PropagateSharingNode.create();
    private final ConditionProfile byIdentityProfile = ConditionProfile.create();

    public static SetNode create() {
        return SetNodeGen.create();
    }

    public abstract boolean executeSet(RubyHash hash, Object key, Object value, boolean byIdentity);

    @Specialization(guards = "isNullHash(hash)")
    protected boolean setNull(RubyHash hash, Object originalKey, Object value, boolean byIdentity) {
        assert HashOperations.verifyStore(getContext(), hash);
        boolean compareByIdentity = byIdentityProfile.profile(byIdentity);
        final Object key = freezeHashKeyIfNeededNode.executeFreezeIfNeeded(originalKey, compareByIdentity);

        final int hashed = hashNode.execute(key, compareByIdentity);

        propagateSharingKeyNode.executePropagate(hash, key);
        propagateSharingValueNode.executePropagate(hash, value);

        Object store = PackedArrayStrategy.createStore(getLanguage(), hashed, key, value);
        hash.store = store;
        hash.size = 1;
        hash.firstInSequence = null;
        hash.lastInSequence = null;

        assert HashOperations.verifyStore(getContext(), hash);
        return true;
    }

    @ExplodeLoop(kind = LoopExplosionKind.FULL_UNROLL_UNTIL_RETURN)
    @Specialization(guards = "isPackedHash(hash)")
    protected boolean setPackedArray(RubyHash hash, Object originalKey, Object value, boolean byIdentity,
            @Cached ConditionProfile strategyProfile) {
        assert HashOperations.verifyStore(getContext(), hash);
        final boolean compareByIdentity = byIdentityProfile.profile(byIdentity);
        final Object key = freezeHashKeyIfNeededNode.executeFreezeIfNeeded(originalKey, compareByIdentity);

        final int hashed = hashNode.execute(key, compareByIdentity);

        propagateSharingKeyNode.executePropagate(hash, key);
        propagateSharingValueNode.executePropagate(hash, value);

        final Object[] store = (Object[]) hash.store;
        final int size = hash.size;

        // written very carefully to allow PE
        for (int n = 0; n < getLanguage().options.HASH_PACKED_ARRAY_MAX; n++) {
            if (n < size) {
                final int otherHashed = PackedArrayStrategy.getHashed(store, n);
                final Object otherKey = PackedArrayStrategy.getKey(store, n);
                if (equalKeys(compareByIdentity, key, hashed, otherKey, otherHashed)) {
                    PackedArrayStrategy.setValue(store, n, value);
                    assert HashOperations.verifyStore(getContext(), hash);
                    return false;
                }
            }
        }

        if (strategyProfile.profile(size < getLanguage().options.HASH_PACKED_ARRAY_MAX)) {
            PackedArrayStrategy.setHashedKeyValue(store, size, hashed, key, value);
            hash.size += 1;
            return true;
        } else {
            PackedArrayStrategy.promoteToBuckets(getContext(), hash, store, size);
            BucketsStrategy.addNewEntry(getContext(), hash, hashed, key, value);
        }

        assert HashOperations.verifyStore(getContext(), hash);

        return true;
    }

    @Specialization(guards = "isBucketHash(hash)")
    protected boolean setBuckets(RubyHash hash, Object originalKey, Object value, boolean byIdentity,
            @Cached ConditionProfile foundProfile,
            @Cached ConditionProfile bucketCollisionProfile,
            @Cached ConditionProfile appendingProfile,
            @Cached ConditionProfile resizeProfile) {
        assert HashOperations.verifyStore(getContext(), hash);
        final boolean compareByIdentity = byIdentityProfile.profile(byIdentity);
        final Object key = freezeHashKeyIfNeededNode.executeFreezeIfNeeded(originalKey, compareByIdentity);

        propagateSharingKeyNode.executePropagate(hash, key);
        propagateSharingValueNode.executePropagate(hash, value);

        final HashLookupResult result = lookup(hash, key);
        final Entry entry = result.getEntry();

        if (foundProfile.profile(entry == null)) {
            final Entry[] entries = (Entry[]) hash.store;

            final Entry newEntry = new Entry(result.getHashed(), key, value);

            if (bucketCollisionProfile.profile(result.getPreviousEntry() == null)) {
                entries[result.getIndex()] = newEntry;
            } else {
                result.getPreviousEntry().setNextInLookup(newEntry);
            }

            final Entry lastInSequence = hash.lastInSequence;

            if (appendingProfile.profile(lastInSequence == null)) {
                hash.firstInSequence = newEntry;
            } else {
                lastInSequence.setNextInSequence(newEntry);
                newEntry.setPreviousInSequence(lastInSequence);
            }

            hash.lastInSequence = newEntry;

            final int newSize = (hash.size += 1);

            // TODO CS 11-May-15 could store the next size for resize instead of doing a float operation each time

            if (resizeProfile.profile(newSize / (double) entries.length > BucketsStrategy.LOAD_FACTOR)) {
                BucketsStrategy.resize(getContext(), hash);
            }
            assert HashOperations.verifyStore(getContext(), hash);
            return true;
        } else {
            entry.setValue(value);
            assert HashOperations.verifyStore(getContext(), hash);
            return false;
        }
    }

    private HashLookupResult lookup(RubyHash hash, Object key) {
        if (lookupEntryNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            lookupEntryNode = insert(LookupEntryNode.create());
        }
        return lookupEntryNode.lookup(hash, key);
    }

    protected boolean equalKeys(boolean compareByIdentity, Object key, int hashed, Object otherKey, int otherHashed) {
        return compareHashKeysNode.execute(compareByIdentity, key, hashed, otherKey, otherHashed);
    }

}
