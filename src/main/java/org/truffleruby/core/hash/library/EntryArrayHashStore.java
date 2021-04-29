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
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.BiFunctionNode;
import org.truffleruby.core.hash.BucketsStrategy;
import org.truffleruby.core.hash.Entry;
import org.truffleruby.core.hash.FreezeHashKeyIfNeededNode;
import org.truffleruby.core.hash.HashLookupResult;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.LookupEntryNode;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

@ExportLibrary(value = HashStoreLibrary.class)
@GenerateUncached
public class EntryArrayHashStore {

    public Entry[] entries;

    public EntryArrayHashStore(Entry[] entries) {
        this.entries = entries;
    }

    @ExportMessage
    protected Object lookupOrDefault(Frame frame, RubyHash hash, Object key, BiFunctionNode defaultNode,
            @Cached @Shared("lookup") LookupEntryNode lookup,
            @Cached BranchProfile notInHash) {

        final HashLookupResult hashLookupResult = lookup.lookup(hash, key);

        if (hashLookupResult.getEntry() != null) {
            return hashLookupResult.getEntry().getValue();
        }

        notInHash.enter();
        return defaultNode.accept((VirtualFrame) frame, hash, key);
    }

    @ExportMessage
    protected boolean set(RubyHash hash, Object key, Object value, boolean byIdentity,
            @Cached ConditionProfile byIdentityProfile,
            @Cached FreezeHashKeyIfNeededNode freezeHashKeyIfNeeded,
            @Cached PropagateSharingNode propagateSharingKey,
            @Cached PropagateSharingNode propagateSharingValue,
            @Cached @Shared("lookup") LookupEntryNode lookup,
            @Cached ConditionProfile found,
            @Cached ConditionProfile bucketCollision,
            @Cached ConditionProfile appending,
            @Cached ConditionProfile resize,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        assert HashOperations.verifyStore(context, hash);
        final boolean compareByIdentity = byIdentityProfile.profile(byIdentity);
        final Object key2 = freezeHashKeyIfNeeded.executeFreezeIfNeeded(key, compareByIdentity);

        propagateSharingKey.executePropagate(hash, key2);
        propagateSharingValue.executePropagate(hash, value);

        final HashLookupResult result = lookup.lookup(hash, key2);
        final Entry entry = result.getEntry();

        if (found.profile(entry == null)) {
            final Entry[] entries = ((EntryArrayHashStore) hash.store).entries;

            final Entry newEntry = new Entry(result.getHashed(), key2, value);

            if (bucketCollision.profile(result.getPreviousEntry() == null)) {
                entries[result.getIndex()] = newEntry;
            } else {
                result.getPreviousEntry().setNextInLookup(newEntry);
            }

            final Entry lastInSequence = hash.lastInSequence;

            if (appending.profile(lastInSequence == null)) {
                hash.firstInSequence = newEntry;
            } else {
                lastInSequence.setNextInSequence(newEntry);
                newEntry.setPreviousInSequence(lastInSequence);
            }

            hash.lastInSequence = newEntry;

            final int newSize = (hash.size += 1);

            // TODO CS 11-May-15 could store the next size for resize instead of doing a float operation each time

            if (resize.profile(newSize / (double) entries.length > BucketsStrategy.LOAD_FACTOR)) {
                BucketsStrategy.resize(context, hash);
            }
            assert HashOperations.verifyStore(context, hash);
            return true;
        } else {
            entry.setValue(value);
            assert HashOperations.verifyStore(context, hash);
            return false;
        }
    }
}
