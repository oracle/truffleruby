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
import org.truffleruby.collections.BiFunctionNode;
import org.truffleruby.core.hash.HashingNodes;
import org.truffleruby.core.hash.LookupPackedEntryNode;
import org.truffleruby.core.hash.RubyHash;

@ExportLibrary(value = HashStoreLibrary.class, receiverType = Object[].class)
@GenerateUncached
public class PackedHashStoreLibrary {

    @ExportMessage
    protected static Object lookupOrDefault(
            Object[] store, Frame frame, RubyHash hash, Object key, BiFunctionNode defaultNode,
            @Cached LookupPackedEntryNode lookupPackedEntryNode,
            @Cached HashingNodes.ToHash hashNode) {

        int hashed = hashNode.execute(key, hash.compareByIdentity);
        return lookupPackedEntryNode.executePackedLookup((VirtualFrame) frame, hash, key, hashed, defaultNode);
    }
}
