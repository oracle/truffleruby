/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import org.truffleruby.core.hash.library.BucketsHashStore;
import org.truffleruby.core.hash.library.EmptyHashStore;
import org.truffleruby.core.hash.library.PackedHashStoreLibrary;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public abstract class HashLiteralNode extends RubyContextSourceNode {

    @Children protected final RubyNode[] keyValues;

    protected HashLiteralNode(RubyNode[] keyValues) {
        assert keyValues.length % 2 == 0;
        this.keyValues = keyValues;
    }

    public static HashLiteralNode create(RubyNode[] keyValues) {
        if (keyValues.length == 0) {
            return new EmptyHashStore.EmptyHashLiteralNode();
        } else if (keyValues.length <= PackedHashStoreLibrary.MAX_ENTRIES * 2) {
            return new PackedHashStoreLibrary.SmallHashLiteralNode(keyValues);
        } else {
            return new BucketsHashStore.GenericHashLiteralNode(keyValues);
        }
    }

    @ExplodeLoop
    @Override
    public void doExecuteVoid(VirtualFrame frame) {
        for (RubyNode child : keyValues) {
            child.doExecuteVoid(frame);
        }
    }
}
