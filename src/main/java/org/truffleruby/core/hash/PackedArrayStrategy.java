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

import org.truffleruby.RubyContext;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.hash.library.EntryArrayHashStore;

public abstract class PackedArrayStrategy {

    public static final int ELEMENTS_PER_ENTRY = 3;

    public static Object[] createStore(RubyLanguage language, int hashed, Object key, Object value) {
        final Object[] store = createStore(language);
        setHashedKeyValue(store, 0, hashed, key, value);
        return store;
    }

    public static Object[] createStore(RubyLanguage language) {
        return new Object[language.options.HASH_PACKED_ARRAY_MAX * ELEMENTS_PER_ENTRY];
    }

    public static Object[] copyStore(RubyLanguage language, Object[] store) {
        final Object[] copied = createStore(language);
        System.arraycopy(store, 0, copied, 0, language.options.HASH_PACKED_ARRAY_MAX * ELEMENTS_PER_ENTRY);
        return copied;
    }

    public static int getHashed(Object[] store, int n) {
        return (int) store[n * ELEMENTS_PER_ENTRY];
    }

    public static Object getKey(Object[] store, int n) {
        return store[n * ELEMENTS_PER_ENTRY + 1];
    }

    public static Object getValue(Object[] store, int n) {
        return store[n * ELEMENTS_PER_ENTRY + 2];
    }

    public static void setHashed(Object[] store, int n, int hashed) {
        store[n * ELEMENTS_PER_ENTRY] = hashed;
    }

    public static void setKey(Object[] store, int n, Object key) {
        store[n * ELEMENTS_PER_ENTRY + 1] = key;
    }

    public static void setValue(Object[] store, int n, Object value) {
        store[n * ELEMENTS_PER_ENTRY + 2] = value;
    }

    public static void setHashedKeyValue(Object[] store, int n, int hashed, Object key, Object value) {
        setHashed(store, n, hashed);
        setKey(store, n, key);
        setValue(store, n, value);
    }

    public static void removeEntry(RubyLanguage language, Object[] store, int n) {
        assert verifyIntegerHashes(language, store);

        final int index = n * ELEMENTS_PER_ENTRY;
        System.arraycopy(
                store,
                index + ELEMENTS_PER_ENTRY,
                store,
                index,
                language.options.HASH_PACKED_ARRAY_MAX * ELEMENTS_PER_ENTRY - ELEMENTS_PER_ENTRY - index);

        assert verifyIntegerHashes(language, store);
    }

    private static boolean verifyIntegerHashes(RubyLanguage language, Object[] store) {
        for (int i = 0; i < language.options.HASH_PACKED_ARRAY_MAX *
                ELEMENTS_PER_ENTRY; i += ELEMENTS_PER_ENTRY) {
            assert store[i] == null || store[i] instanceof Integer;
        }
        return true;
    }

    @TruffleBoundary
    public static void promoteToBuckets(RubyContext context, RubyHash hash, Object[] store, int size) {
        final Entry[] buckets = new Entry[BucketsStrategy.capacityGreaterThan(size)];

        Entry firstInSequence = null;
        Entry previousInSequence = null;
        Entry lastInSequence = null;

        for (int n = 0; n < size; n++) {
            final int hashed = getHashed(store, n);
            final Entry entry = new Entry(hashed, getKey(store, n), getValue(store, n));

            if (previousInSequence == null) {
                firstInSequence = entry;
            } else {
                previousInSequence.setNextInSequence(entry);
                entry.setPreviousInSequence(previousInSequence);
            }

            previousInSequence = entry;
            lastInSequence = entry;

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
        }

        hash.store = new EntryArrayHashStore(buckets);
        hash.size = size;
        hash.firstInSequence = firstInSequence;
        hash.lastInSequence = lastInSequence;

        assert HashOperations.verifyStore(context, hash);
    }
}
