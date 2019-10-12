/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.nodes.NodeInfo;

@NodeInfo(cost = NodeCost.NONE)
public final class SequenceNode extends RubyNode {

    @Children private final RubyNode[] body;

    public SequenceNode(RubyNode... body) {
        this.body = body;
    }

    @ExplodeLoop
    @Override
    public Object execute(VirtualFrame frame) {
        for (int n = 0; n < body.length - 1; n++) {
            body[n].doExecuteVoid(frame);
        }

        return body[body.length - 1].execute(frame);
    }

    @ExplodeLoop
    @Override
    public void doExecuteVoid(VirtualFrame frame) {
        for (int n = 0; n < body.length; n++) {
            body[n].doExecuteVoid(frame);
        }
    }

    public RubyNode[] getSequence() {
        return body;
    }

}
