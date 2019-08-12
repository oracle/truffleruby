/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.nodes.NodeInfo;

/**
 * This node has a pair of children. One has side effects and the other returns the
 * result. If the result isn't needed all we execute is the side effects.
 */
@NodeInfo(cost = NodeCost.NONE)
public class ElidableResultNode extends RubyNode {

    @Child private RubyNode required;
    @Child private RubyNode elidableResult;

    public ElidableResultNode(RubyNode required, RubyNode elidableResult) {
        this.required = required;
        this.elidableResult = elidableResult;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        required.doExecuteVoid(frame);
        return elidableResult.execute(frame);
    }

    @Override
    public void doExecuteVoid(VirtualFrame frame) {
        required.execute(frame);
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        return elidableResult.isDefined(frame);
    }

}
