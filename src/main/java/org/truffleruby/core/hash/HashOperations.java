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

import java.util.Collections;
import java.util.Iterator;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.collections.BoundaryIterable;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.objects.shared.SharedObjects;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class HashOperations {

    public static DynamicObject newEmptyHash(RubyContext context) {
        final DynamicObject nil = context.getCoreLibrary().getNil();
        return context.getCoreLibrary().getHashFactory().newInstance(
                Layouts.HASH.build(null, 0, null, null, nil, nil, false));
    }

    public static boolean verifyStore(RubyContext context, DynamicObject hash) {
        final Object store = Layouts.HASH.getStore(hash);
        final int size = Layouts.HASH.getSize(hash);
        final Entry firstInSequence = Layouts.HASH.getFirstInSequence(hash);
        final Entry lastInSequence = Layouts.HASH.getLastInSequence(hash);

        assert store == null || store.getClass() == Object[].class || store instanceof Entry[];

        if (store == null) {
            assert size == 0;
            assert firstInSequence == null;
            assert lastInSequence == null;
        } else if (store instanceof Entry[]) {
            assert lastInSequence == null || lastInSequence.getNextInSequence() == null;

            final Entry[] entryStore = (Entry[]) store;

            Entry foundFirst = null;
            Entry foundLast = null;
            int foundSizeBuckets = 0;

            for (int n = 0; n < entryStore.length; n++) {
                Entry entry = entryStore[n];

                while (entry != null) {
                    assert SharedObjects.assertPropagateSharing(
                            context,
                            hash,
                            entry.getKey()) : "unshared key in shared Hash: " + entry.getKey();
                    assert SharedObjects.assertPropagateSharing(
                            context,
                            hash,
                            entry.getValue()) : "unshared value in shared Hash: " + entry.getValue();

                    foundSizeBuckets++;

                    if (entry == firstInSequence) {
                        assert foundFirst == null;
                        foundFirst = entry;
                    }

                    if (entry == lastInSequence) {
                        assert foundLast == null;
                        foundLast = entry;
                    }

                    entry = entry.getNextInLookup();
                }
            }

            assert foundSizeBuckets == size;
            assert firstInSequence == foundFirst;
            assert lastInSequence == foundLast;

            int foundSizeSequence = 0;
            Entry entry = firstInSequence;

            while (entry != null) {
                foundSizeSequence++;

                if (entry.getNextInSequence() == null) {
                    assert entry == lastInSequence;
                } else {
                    assert entry.getNextInSequence().getPreviousInSequence() == entry;
                }

                entry = entry.getNextInSequence();

                assert entry != firstInSequence;
            }

            assert foundSizeSequence == size : StringUtils.format("%d %d", foundSizeSequence, size);
        } else if (store.getClass() == Object[].class) {
            assert ((Object[]) store).length == context.getOptions().HASH_PACKED_ARRAY_MAX *
                    PackedArrayStrategy.ELEMENTS_PER_ENTRY : ((Object[]) store).length;

            final Object[] packedStore = (Object[]) store;

            for (int i = 0; i < size * PackedArrayStrategy.ELEMENTS_PER_ENTRY; i++) {
                assert packedStore[i] != null;
            }

            for (int n = 0; n < size; n++) {
                final Object key = PackedArrayStrategy.getKey(packedStore, n);
                final Object value = PackedArrayStrategy.getValue(packedStore, n);

                assert SharedObjects.assertPropagateSharing(context, hash, key) : "unshared key in shared Hash: " + key;
                assert SharedObjects.assertPropagateSharing(context, hash, value) : "unshared value in shared Hash: " +
                        value;
            }

            assert firstInSequence == null;
            assert lastInSequence == null;
        }

        return true;
    }

    @TruffleBoundary
    public static Iterator<KeyValue> iterateKeyValues(DynamicObject hash) {
        assert RubyGuards.isRubyHash(hash);

        if (HashGuards.isNullHash(hash)) {
            return Collections.emptyIterator();
        } else if (HashGuards.isPackedHash(hash)) {
            return PackedArrayStrategy
                    .iterateKeyValues((Object[]) Layouts.HASH.getStore(hash), Layouts.HASH.getSize(hash));
        } else if (HashGuards.isBucketHash(hash)) {
            return BucketsStrategy.iterateKeyValues(Layouts.HASH.getFirstInSequence(hash));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @TruffleBoundary
    public static BoundaryIterable<KeyValue> iterableKeyValues(final DynamicObject hash) {
        assert RubyGuards.isRubyHash(hash);

        return BoundaryIterable.wrap(() -> iterateKeyValues(hash));
    }

}
