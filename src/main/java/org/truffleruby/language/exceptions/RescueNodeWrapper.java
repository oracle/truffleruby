/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.exceptions;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.InstrumentableNode.WrapperNode;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.object.DynamicObject;

// Defined manually rather than generated because we need to delegate canHandle() and isDefined()
public class RescueNodeWrapper extends RescueNode implements WrapperNode {

    @Child private RescueNode delegateNode;
    @Child private ProbeNode probeNode;

    RescueNodeWrapper(RescueNode delegateNode, ProbeNode probeNode) {
        super(null);
        this.delegateNode = delegateNode;
        this.probeNode = probeNode;
    }

    @Override
    public RescueNode getDelegateNode() {
        return delegateNode;
    }

    @Override
    public ProbeNode getProbeNode() {
        return probeNode;
    }

    @Override
    public NodeCost getCost() {
        return NodeCost.NONE;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Object returnValue;
        for (;;) {
            boolean wasOnReturnExecuted = false;
            try {
                probeNode.onEnter(frame);
                returnValue = delegateNode.execute(frame);
                wasOnReturnExecuted = true;
                probeNode.onReturnValue(frame, returnValue);
                break;
            } catch (Throwable t) {
                Object result = probeNode.onReturnExceptionalOrUnwind(frame, t, wasOnReturnExecuted);
                if (result == ProbeNode.UNWIND_ACTION_REENTER) {
                    continue;
                } else if (result != null) {
                    returnValue = result;
                    break;
                }
                throw t;
            }
        }
        return returnValue;
    }

    @Override
    public boolean canHandle(VirtualFrame frame, DynamicObject exception) {
        return delegateNode.canHandle(frame, exception);
    }

    @Override
    public Object isDefined(VirtualFrame frame) {
        return delegateNode.isDefined(frame);
    }
}
