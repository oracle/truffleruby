/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;

public class ConcatHashLiteralNode extends RubyContextSourceNode {

    @Children private final RubyNode[] children;
    @Child private DispatchNode hashMergeNode;

    public ConcatHashLiteralNode(RubyNode[] children) {
        assert children.length > 1;
        this.children = children;
    }

    @Override
    @ExplodeLoop
    public Object execute(VirtualFrame frame) {
        final RubyHash hash = HashOperations.newEmptyHash(getContext(), getLanguage());
        if (hashMergeNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            hashMergeNode = insert(DispatchNode.create());
        }
        for (int i = 0; i < children.length; i++) {
            hashMergeNode.call(hash, "merge!", children[i].execute(frame));
        }
        return hash;
    }

}
