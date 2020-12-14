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
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyContextNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;

import com.oracle.truffle.api.profiles.ConditionProfile;

@ImportStatic(HashGuards.class)
public abstract class DeleteLastNode extends RubyContextNode {
    /* For BucketHash, behaves almost identical to DeleteNode. Otherwise, deletes the most recently added key, because
     * we are using the hash like a stack and guarantee that the last we added is the one we'll want to delete. When
     * assertions are enabled checks that the deleted key is the one expected by the user. Does not handle blocks. A
     * helper for detect_recursion. */

    @Child private LookupEntryNode lookupEntryNode = new LookupEntryNode();

    public static DeleteLastNode create() {
        return DeleteLastNodeGen.create();
    }

    public abstract Object executeDeleteLast(RubyHash hash, Object key);

    @Specialization(guards = "isNullHash(hash)")
    protected Object deleteNull(RubyHash hash, Object key) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw new UnsupportedOperationException("Cannot delete the last node of an empty hash");
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

        final HashLookupResult hashLookupResult = lookupEntryNode.lookup(hash, key);

        final Entry entry = hashLookupResult.getEntry();
        assert hash.lastInSequence == entry;

        // Remove from the sequence chain

        if (entry.getPreviousInSequence() == null) {
            assert hash.firstInSequence == entry;
            hash.firstInSequence = entry.getNextInSequence();
        } else {
            assert hash.firstInSequence != entry;
            entry.getPreviousInSequence().setNextInSequence(null);
        }

        hash.lastInSequence = entry.getPreviousInSequence();

        // Remove from the lookup chain

        if (hashLookupResult.getPreviousEntry() == null) {
            ((Entry[]) hash.store)[hashLookupResult.getIndex()] = entry.getNextInLookup();
        } else {
            hashLookupResult.getPreviousEntry().setNextInLookup(entry.getNextInLookup());
        }

        hash.size -= 1;

        assert HashOperations.verifyStore(getContext(), hash);

        return entry.getValue();
    }
}
