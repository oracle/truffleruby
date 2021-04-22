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
import org.truffleruby.core.hash.library.EntryArrayHashStore;
import org.truffleruby.language.RubyContextNode;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;

/** A helper for detect_recursion. A variant of DeleteNode optimized for removing the most recently added key, because
 * we are using the hash like a stack and guarantee that the last entry we added is the one we'll want to delete. Checks
 * that the deleted key is the one expected by the user. */
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
        if (key != otherKey) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw CompilerDirectives
                    .shouldNotReachHere("The last key was not " + key + " as expected but was " + otherKey);
        }

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
        if (key != lastEntry.getKey()) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw CompilerDirectives
                    .shouldNotReachHere("The last key was not " + key + " as expected but was " + lastEntry.getKey());
        }
        int hashed = lastEntry.getHashed();

        final Entry[] entries = ((EntryArrayHashStore) hash.store).entries;
        final int index = BucketsStrategy.getBucketIndex(hashed, entries.length);

        // Lookup previous entry
        Entry entry = entries[index];
        Entry previousEntry = null;
        while (entry != lastEntry) {
            previousEntry = entry;
            entry = entry.getNextInLookup();
        }

        assert entry.getNextInSequence() == null;

        if (hash.firstInSequence == entry) {
            assert entry.getPreviousInSequence() == null;
            hash.firstInSequence = null;
            hash.lastInSequence = null;
        } else {
            assert entry.getPreviousInSequence() != null;
            final Entry previousInSequence = entry.getPreviousInSequence();
            previousInSequence.setNextInSequence(null);
            hash.lastInSequence = previousInSequence;
        }

        BucketsStrategy.removeFromLookupChain(hash, index, entry, previousEntry);

        hash.size -= 1;

        assert HashOperations.verifyStore(getContext(), hash);
        return entry.getValue();
    }

}
