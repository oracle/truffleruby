/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */

package org.truffleruby.language.exceptions;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.InstrumentableFactory;
import com.oracle.truffle.api.instrumentation.ProbeNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.object.DynamicObject;

public class RescueNodeWrapper implements InstrumentableFactory<RescueNode> {

    @Override
    public WrapperNode createWrapper(RescueNode delegateNode, ProbeNode probeNode) {
        return new RescueNodeWrapper0(delegateNode, probeNode);
    }

    private static final class RescueNodeWrapper0 extends RescueNode implements WrapperNode {

        @Child private RescueNode delegateNode;
        @Child private ProbeNode probeNode;

        private RescueNodeWrapper0(RescueNode delegateNode, ProbeNode probeNode) {
            super(null);
            this.delegateNode = delegateNode;
            this.probeNode = probeNode;
        }

        @Override
        public Node getDelegateNode() {
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
            try {
                probeNode.onEnter(frame);
                Object returnValue = delegateNode.execute(frame);
                probeNode.onReturnValue(frame, returnValue);
                return returnValue;
            } catch (Throwable t) {
                probeNode.onReturnExceptional(frame, t);
                throw t;
            }
        }

        @Override
        public void executeVoid(VirtualFrame frame) {
            try {
                probeNode.onEnter(frame);
                delegateNode.executeVoid(frame);
                probeNode.onReturnValue(frame, null);
            } catch (Throwable t) {
                probeNode.onReturnExceptional(frame, t);
                throw t;
            }
        }

        @Override
        public boolean canHandle(VirtualFrame frame, DynamicObject exception) {
            return delegateNode.canHandle(frame, exception);
        }

    }

}
