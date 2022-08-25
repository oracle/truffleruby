/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.nodes.NodeInfo;

/** This node has a pair of children. One has side effects and the other returns the result. If the result isn't needed
 * all we execute is the side effects. */
@NodeInfo(cost = NodeCost.NONE)
public class ElidableResultNode extends RubyContextSourceNode {

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
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        return elidableResult.isDefined(frame, language, context);
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new ElidableResultNode(
                required.cloneUninitialized(),
                elidableResult.cloneUninitialized());
        copy.copyFlags(this);
        return copy;
    }

}
