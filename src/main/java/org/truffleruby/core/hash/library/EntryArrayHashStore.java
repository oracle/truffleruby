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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.CachedLanguage;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.PEBiConsumer;
import org.truffleruby.collections.PEBiFunction;
import org.truffleruby.core.array.ArrayBuilderNode;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.hash.BucketsStrategy;
import org.truffleruby.core.hash.CompareHashKeysNode;
import org.truffleruby.core.hash.Entry;
import org.truffleruby.core.hash.FreezeHashKeyIfNeededNode;
import org.truffleruby.core.hash.HashLookupResult;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.HashingNodes;
import org.truffleruby.core.hash.LookupEntryNode;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

import java.util.Arrays;

@ExportLibrary(value = HashStoreLibrary.class)
@GenerateUncached
public class EntryArrayHashStore {

    public Entry[] entries;

    public EntryArrayHashStore(Entry[] entries) {
        this.entries = entries;
    }

    @ExportMessage
    protected Object lookupOrDefault(Frame frame, RubyHash hash, Object key, PEBiFunction defaultNode,
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
            @Cached FreezeHashKeyIfNeededNode freezeHashKeyIfNeeded,
            @Cached @Exclusive PropagateSharingNode propagateSharingKey,
            @Cached @Exclusive PropagateSharingNode propagateSharingValue,
            @Cached @Shared("lookup") LookupEntryNode lookup,
            @Cached @Exclusive ConditionProfile found,
            @Cached @Exclusive ConditionProfile bucketCollision,
            @Cached @Exclusive ConditionProfile appending,
            @Cached @Exclusive ConditionProfile resize,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        assert HashOperations.verifyStore(context, hash);
        final Object key2 = freezeHashKeyIfNeeded.executeFreezeIfNeeded(key, byIdentity);

        propagateSharingKey.executePropagate(hash, key2);
        propagateSharingValue.executePropagate(hash, value);

        final HashLookupResult result = lookup.lookup(hash, key2);
        final Entry entry = result.getEntry();

        if (found.profile(entry == null)) {

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

    @ExportMessage
    protected Object delete(RubyHash hash, Object key,
            @Cached @Shared("lookup") LookupEntryNode lookup,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        assert HashOperations.verifyStore(context, hash);
        final HashLookupResult lookupResult = lookup.lookup(hash, key);
        final Entry entry = lookupResult.getEntry();

        if (entry == null) {
            return null;
        }

        BucketsStrategy.removeFromSequenceChain(hash, entry);
        BucketsStrategy.removeFromLookupChain(hash, lookupResult.getIndex(), entry, lookupResult.getPreviousEntry());
        hash.size -= 1;
        assert HashOperations.verifyStore(context, hash);
        return entry.getValue();
    }

    @ExportMessage
    protected Object eachEntry(Frame frame, RubyHash hash, PEBiConsumer callback, Object state,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        assert HashOperations.verifyStore(context, hash);

        Entry entry = hash.firstInSequence;
        while (entry != null) {
            callback.accept((VirtualFrame) frame, entry.getKey(), entry.getValue(), state);
            entry = entry.getNextInSequence();
        }

        return state;
    }

    @ExportMessage
    protected void each(RubyHash hash, RubyProc block,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached @Shared("yield") HashStoreLibrary.YieldPairNode yieldPair) {

        assert HashOperations.verifyStore(context, hash);
        Entry entry = hash.firstInSequence;
        while (entry != null) {
            yieldPair.execute(block, entry.getKey(), entry.getValue());
            entry = entry.getNextInSequence();
        }
    }

    @TruffleBoundary
    @ExportMessage
    protected void replace(RubyHash hash, RubyHash dest,
            @Cached @Exclusive PropagateSharingNode propagateSharing,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        if (hash == dest) {
            return;
        }

        propagateSharing.executePropagate(dest, hash);
        BucketsStrategy.copyInto(context, hash, dest);
        dest.defaultBlock = hash.defaultBlock;
        dest.defaultValue = hash.defaultValue;
        dest.compareByIdentity = hash.compareByIdentity;

        assert HashOperations.verifyStore(context, dest);
    }

    @ExportMessage
    protected RubyArray map(RubyHash hash, RubyProc block,
            @Cached ArrayBuilderNode arrayBuilder,
            @Cached @Shared("yield") HashStoreLibrary.YieldPairNode yieldPair,
            @CachedLibrary("this") HashStoreLibrary self,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        assert HashOperations.verifyStore(context, hash);

        final int length = hash.size;
        ArrayBuilderNode.BuilderState state = arrayBuilder.start(length);

        int index = 0;

        try {
            Entry entry = hash.firstInSequence;
            while (entry != null) {
                arrayBuilder.appendValue(state, index, yieldPair.execute(block, entry.getKey(), entry.getValue()));
                index++;
                entry = entry.getNextInSequence();
            }
        } finally {
            HashStoreLibrary.reportLoopCount(self, length);
        }

        return ArrayHelpers.createArray(context, language, arrayBuilder.finish(state, length), length);
    }

    @ExportMessage
    protected RubyArray shift(RubyHash hash,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        assert HashOperations.verifyStore(context, hash);

        final Entry first = hash.firstInSequence;
        assert first.getPreviousInSequence() == null;

        final Object key = first.getKey();
        final Object value = first.getValue();

        hash.firstInSequence = first.getNextInSequence();

        if (first.getNextInSequence() != null) {
            first.getNextInSequence().setPreviousInSequence(null);
            hash.firstInSequence = first.getNextInSequence();
        }

        if (hash.lastInSequence == first) {
            hash.lastInSequence = null;
        }

        final int index = BucketsStrategy.getBucketIndex(first.getHashed(), this.entries.length);

        Entry previous = null;
        Entry entry = entries[index];
        while (entry != null) {
            if (entry == first) {
                if (previous == null) {
                    entries[index] = first.getNextInLookup();
                } else {
                    previous.setNextInLookup(first.getNextInLookup());
                }
                break;
            }

            previous = entry;
            entry = entry.getNextInLookup();
        }

        hash.size -= 1;

        assert HashOperations.verifyStore(context, hash);
        return ArrayHelpers.createArray(context, language, new Object[]{ key, value });
    }

    @ExportMessage
    protected void rehash(RubyHash hash,
            @Cached CompareHashKeysNode compareHashKeys,
            @Cached HashingNodes.ToHash hashNode,
            @CachedLanguage RubyLanguage language,
            @CachedContext(RubyLanguage.class) RubyContext context) {

        assert HashOperations.verifyStore(context, hash);

        Arrays.fill(entries, null);

        Entry entry = hash.firstInSequence;
        while (entry != null) {
            final int newHash = hashNode.execute(entry.getKey(), hash.compareByIdentity);
            entry.setHashed(newHash);
            entry.setNextInLookup(null);

            final int index = BucketsStrategy.getBucketIndex(newHash, entries.length);
            Entry bucketEntry = entries[index];

            if (bucketEntry == null) {
                entries[index] = entry;
            } else {
                Entry previousEntry = entry;

                int size = hash.size;
                do {
                    if (compareHashKeys.execute(
                            hash.compareByIdentity,
                            entry.getKey(),
                            newHash,
                            bucketEntry.getKey(),
                            bucketEntry.getHashed())) {
                        BucketsStrategy.removeFromSequenceChain(hash, entry);
                        size--;
                        break;
                    }
                    previousEntry = bucketEntry;
                    bucketEntry = bucketEntry.getNextInLookup();
                } while (bucketEntry != null);

                previousEntry.setNextInLookup(entry);
                hash.size = size;
            }
            entry = entry.getNextInSequence();
        }

        assert HashOperations.verifyStore(context, hash);
    }
}
