/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.inlined;

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.dsl.NodeChild;

@NodeChild(value = "self", type = RubyNode.class)
public abstract class UnaryInlinedOperationNode extends InlinedOperationNode {

    public UnaryInlinedOperationNode(RubyCallNodeParameters callNodeParameters, Assumption... assumptions) {
        super(callNodeParameters, assumptions);
    }

    protected abstract RubyNode getSelf();

    @Override
    protected RubyNode getReceiverNode() {
        return getSelf();
    }

    private static final RubyNode[] EMPTY_ARRAY = new RubyNode[0];

    @Override
    protected RubyNode[] getArgumentNodes() {
        return EMPTY_ARRAY;
    }

}
