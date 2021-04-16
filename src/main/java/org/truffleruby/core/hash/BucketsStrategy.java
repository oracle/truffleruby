/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import java.util.Set;

import org.truffleruby.RubyContext;
import org.truffleruby.language.objects.ObjectGraph;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public abstract class BucketsStrategy {

    // If the size is more than this fraction of the number of buckets, resize
    public static final double LOAD_FACTOR = 0.75;

    // Create this many more buckets than there are entries when resizing or creating from scratch
    public static final int OVERALLOCATE_FACTOR = 4;

    public static final int SIGN_BIT_MASK = ~(1 << 31);

    private static final int MRI_PRIMES[] = {
            8 + 3,
            16 + 3,
            32 + 5,
            64 + 3,
            128 + 3,
            256 + 27,
            512 + 9,
            1024 + 9,
            2048 + 5,
            4096 + 3,
            8192 + 27,
            16384 + 43,
            32768 + 3,
            65536 + 45,
            131072 + 29,
            262144 + 3,
            524288 + 21,
            1048576 + 7,
            2097152 + 17,
            4194304 + 15,
            8388608 + 9,
            16777216 + 43,
            33554432 + 35,
            67108864 + 15,
            134217728 + 29,
            268435456 + 3,
            536870912 + 11,
            1073741824 + 85
    };

    private static final int[] CAPACITIES = MRI_PRIMES;

    @TruffleBoundary
    public static int capacityGreaterThan(int size) {
        for (int capacity : CAPACITIES) {
            if (capacity > size) {
                return capacity;
            }
        }

        return CAPACITIES[CAPACITIES.length - 1];
    }

    public static int getBucketIndex(int hashed, int bucketsCount) {
        return (hashed & SIGN_BIT_MASK) % bucketsCount;
    }

    public static void addNewEntry(RubyContext context, RubyHash hash, int hashed, Object key, Object value) {
        assert HashGuards.isBucketHash(hash);
        assert HashOperations.verifyStore(context, hash);

        final Entry[] buckets = (Entry[]) hash.store;

        final Entry entry = new Entry(hashed, key, value);

        if (hash.firstInSequence == null) {
            hash.firstInSequence = entry;
        } else {
            hash.lastInSequence.setNextInSequence(entry);
            entry.setPreviousInSequence(hash.lastInSequence);
        }

        hash.lastInSequence = entry;

        final int bucketIndex = BucketsStrategy.getBucketIndex(hashed, buckets.length);

        Entry previousInLookup = buckets[bucketIndex];

        if (previousInLookup == null) {
            buckets[bucketIndex] = entry;
        } else {
            while (previousInLookup.getNextInLookup() != null) {
                previousInLookup = previousInLookup.getNextInLookup();
            }

            previousInLookup.setNextInLookup(entry);
        }

        hash.size += 1;

        assert HashOperations.verifyStore(context, hash);
    }

    @TruffleBoundary
    public static void resize(RubyContext context, RubyHash hash) {
        assert HashGuards.isBucketHash(hash);
        assert HashOperations.verifyStore(context, hash);

        final int bucketsCount = capacityGreaterThan(hash.size) * OVERALLOCATE_FACTOR;
        final Entry[] newEntries = new Entry[bucketsCount];

        Entry entry = hash.firstInSequence;

        while (entry != null) {
            final int bucketIndex = getBucketIndex(entry.getHashed(), bucketsCount);
            Entry previousInLookup = newEntries[bucketIndex];

            if (previousInLookup == null) {
                newEntries[bucketIndex] = entry;
            } else {
                while (previousInLookup.getNextInLookup() != null) {
                    previousInLookup = previousInLookup.getNextInLookup();
                }

                previousInLookup.setNextInLookup(entry);
            }

            entry.setNextInLookup(null);
            entry = entry.getNextInSequence();
        }

        hash.store = newEntries;

        assert HashOperations.verifyStore(context, hash);
    }

    public static void getAdjacentObjects(Set<Object> reachable, Entry firstInSequence) {
        Entry entry = firstInSequence;
        while (entry != null) {
            ObjectGraph.addProperty(reachable, entry.getKey());
            ObjectGraph.addProperty(reachable, entry.getValue());
            entry = entry.getNextInSequence();
        }
    }

    public static void copyInto(RubyContext context, RubyHash from, RubyHash to) {
        assert HashGuards.isBucketHash(from);
        assert HashOperations.verifyStore(context, from);
        assert HashOperations.verifyStore(context, to);

        final Entry[] newEntries = new Entry[((Entry[]) from.store).length];

        Entry firstInSequence = null;
        Entry lastInSequence = null;

        Entry entry = from.firstInSequence;

        while (entry != null) {
            final Entry newEntry = new Entry(entry.getHashed(), entry.getKey(), entry.getValue());

            final int index = BucketsStrategy.getBucketIndex(entry.getHashed(), newEntries.length);

            newEntry.setNextInLookup(newEntries[index]);
            newEntries[index] = newEntry;

            if (firstInSequence == null) {
                firstInSequence = newEntry;
            }

            if (lastInSequence != null) {
                lastInSequence.setNextInSequence(newEntry);
                newEntry.setPreviousInSequence(lastInSequence);
            }

            lastInSequence = newEntry;

            entry = entry.getNextInSequence();
        }

        int size = from.size;
        to.store = newEntries;
        to.size = size;
        to.firstInSequence = firstInSequence;
        to.lastInSequence = lastInSequence;
        assert HashOperations.verifyStore(context, to);
    }

    public static void removeFromSequenceChain(RubyHash hash, Entry entry) {
        final Entry previousInSequence = entry.getPreviousInSequence();
        final Entry nextInSequence = entry.getNextInSequence();

        if (previousInSequence == null) {
            assert hash.firstInSequence == entry;
            hash.firstInSequence = nextInSequence;
        } else {
            assert hash.firstInSequence != entry;
            previousInSequence.setNextInSequence(nextInSequence);
        }

        if (nextInSequence == null) {
            assert hash.lastInSequence == entry;
            hash.lastInSequence = previousInSequence;
        } else {
            assert hash.lastInSequence != entry;
            nextInSequence.setPreviousInSequence(previousInSequence);
        }
    }

    public static void removeFromLookupChain(RubyHash hash, int index, Entry entry, Entry previousEntry) {
        if (previousEntry == null) {
            ((Entry[]) hash.store)[index] = entry.getNextInLookup();
        } else {
            previousEntry.setNextInLookup(entry.getNextInLookup());
        }
    }

}
