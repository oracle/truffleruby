/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import org.truffleruby.language.RubyContextNode;

import com.oracle.truffle.api.profiles.ConditionProfile;

public class LookupEntryNode extends RubyContextNode {

    @Child HashingNodes.ToHash hashNode = HashingNodes.ToHash.create();
    @Child CompareHashKeysNode compareHashKeysNode = new CompareHashKeysNode();

    private final ConditionProfile byIdentityProfile = ConditionProfile.create();

    public HashLookupResult lookup(RubyHash hash, Object key) {
        final boolean compareByIdentity = byIdentityProfile.profile(hash.compareByIdentity);
        int hashed = hashNode.execute(key, compareByIdentity);

        final Entry[] entries = (Entry[]) hash.store;
        final int index = BucketsStrategy.getBucketIndex(hashed, entries.length);
        Entry entry = entries[index];

        Entry previousEntry = null;

        while (entry != null) {
            if (equalKeys(compareByIdentity, key, hashed, entry.getKey(), entry.getHashed())) {
                return new HashLookupResult(hashed, index, previousEntry, entry);
            }

            previousEntry = entry;
            entry = entry.getNextInLookup();
        }

        return new HashLookupResult(hashed, index, previousEntry, null);
    }

    protected boolean equalKeys(boolean compareByIdentity, Object key, int hashed, Object otherKey, int otherHashed) {
        return compareHashKeysNode.equalKeys(compareByIdentity, key, hashed, otherKey, otherHashed);
    }

}
