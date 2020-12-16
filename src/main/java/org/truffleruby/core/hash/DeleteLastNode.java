/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import org.truffleruby.language.RubyContextNode;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;

/** A helper for detect_recursion. For BucketHash, behaves almost identical to DeleteNode. Otherwise, deletes the most
 * recently added key, because we are using the hash like a stack and guarantee that the last we added is the one we'll
 * want to delete. When assertions are enabled, checks that the deleted key is the one expected by the user. Does not
 * handle blocks. */
@ImportStatic(HashGuards.class)
public abstract class DeleteLastNode extends RubyContextNode {

    public static DeleteLastNode create() {
        return DeleteLastNodeGen.create();
    }

    public abstract Object executeDeleteLast(RubyHash hash, Object key);

    @Specialization(guards = "isNullHash(hash)")
    protected Object deleteNull(RubyHash hash, Object key) {
        throw CompilerDirectives.shouldNotReachHere("Cannot delete the last node of an empty hash");
    }

    @Specialization(guards = "isPackedHash(hash)")
    protected Object deletePackedArray(RubyHash hash, Object key) {
        assert HashOperations.verifyStore(getContext(), hash);

        final Object[] store = (Object[]) hash.store;
        final int size = hash.size;

        int n = size - 1;

        // removable
        final Object otherKey = PackedArrayStrategy.getKey(store, n);
        assert key == otherKey;

        final Object value = PackedArrayStrategy.getValue(store, n);
        PackedArrayStrategy.removeEntry(getLanguage(), store, n);
        hash.size -= 1;
        assert HashOperations.verifyStore(getContext(), hash);
        return value;
    }

    @Specialization(guards = "isBucketHash(hash)")
    protected Object delete(RubyHash hash, Object key) {
        assert HashOperations.verifyStore(getContext(), hash);

        final Entry lastEntry = hash.lastInSequence;
        assert key == lastEntry.getKey();
        int hashed = lastEntry.getHashed();

        final Entry[] entries = (Entry[]) hash.store;
        final int index = BucketsStrategy.getBucketIndex(hashed, entries.length);
        Entry entry = entries[index];

        Entry previousEntry = null;

        // Lookup previous entry

        while (entry != null) {
            if (lastEntry == entry) {
                break;
            }

            previousEntry = entry;
            entry = entry.getNextInLookup();
        }

        // Remove entry from the sequence chain

        if (entry.getPreviousInSequence() == null) {
            assert hash.firstInSequence == entry;
            hash.firstInSequence = entry.getNextInSequence();
        } else {
            assert hash.firstInSequence != entry;
            entry.getPreviousInSequence().setNextInSequence(null);
        }

        hash.lastInSequence = entry.getPreviousInSequence();

        // Remove entry from the lookup chain

        if (previousEntry == null) {
            ((Entry[]) hash.store)[index] = entry.getNextInLookup();
        } else {
            previousEntry.setNextInLookup(entry.getNextInLookup());
        }

        hash.size -= 1;

        assert HashOperations.verifyStore(getContext(), hash);

        return entry.getValue();
    }
}
