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
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.collections.BiFunctionNode;
import org.truffleruby.core.hash.Entry;
import org.truffleruby.core.hash.HashLookupResult;
import org.truffleruby.core.hash.LookupEntryNode;
import org.truffleruby.core.hash.RubyHash;

@ExportLibrary(value = HashStoreLibrary.class)
@GenerateUncached
public class EntryArrayHashStore {

    public Entry[] entries;

    public EntryArrayHashStore(Entry[] entries) {
        this.entries = entries;
    }

    @ExportMessage
    protected Object lookupOrDefault(Frame frame, RubyHash hash, Object key, BiFunctionNode defaultNode,
            // TODO remove allowUncached after rebasing on top of hash interop
            @Cached(value = "new()", allowUncached = true) LookupEntryNode lookupEntryNode,
            @Cached BranchProfile notInHashProfile) {

        final HashLookupResult hashLookupResult = lookupEntryNode.lookup(hash, key);

        if (hashLookupResult.getEntry() != null) {
            return hashLookupResult.getEntry().getValue();
        }

        notInHashProfile.enter();
        return defaultNode.accept((VirtualFrame) frame, hash, key);
    }
}
