/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.dsl.NodeChild;

@NodeChild(value = "left", type = RubyNode.class)
@NodeChild(value = "right", type = RubyNode.class)
public abstract class BinaryInlinedOperationNode extends InlinedOperationNode {

    public BinaryInlinedOperationNode(RubyCallNodeParameters callNodeParameters, Assumption... assumptions) {
        super(callNodeParameters, assumptions);
    }

    protected abstract RubyNode getLeft();

    protected abstract RubyNode getRight();

    @Override
    protected RubyNode getReceiverNode() {
        return getLeft();
    }

    @Override
    protected RubyNode[] getArgumentNodes() {
        return new RubyNode[]{ getRight() };
    }

}
