/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.dispatch;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.methods.LookupMethodNode;

public abstract class DispatchNode extends RubyBaseNode {

    private final DispatchAction dispatchAction;

    private static final class Missing {
    }

    public static final Object MISSING = new Missing();

    public DispatchNode(DispatchAction dispatchAction) {
        this.dispatchAction = dispatchAction;
        assert dispatchAction != null;
    }

    protected abstract boolean guard(Object methodName, Object receiver);

    protected DispatchNode getNext() {
        return null;
    }

    public abstract Object executeDispatch(
            VirtualFrame frame,
            Object receiverObject,
            Object methodName,
            DynamicObject blockObject,
            Object[] argumentsObjects);

    protected MethodLookupResult lookup(
            VirtualFrame frame,
            Object receiver,
            String name,
            boolean ignoreVisibility,
            boolean onlyCallPublic) {
        final MethodLookupResult method = LookupMethodNode.lookupMethodCachedWithVisibility(getContext(),
                frame, receiver, name, ignoreVisibility, onlyCallPublic);
        if (dispatchAction == DispatchAction.RESPOND_TO_METHOD && method.isDefined() && method.getMethod().isUnimplemented()) {
            return method.withNoMethod();
        }
        return method;
    }

    protected Object resetAndDispatch(
            VirtualFrame frame,
            Object receiverObject,
            Object methodName,
            DynamicObject blockObject,
            Object[] argumentsObjects,
            String reason) {
        final DispatchHeadNode head = getHeadNode();
        head.reset(reason);
        return head.dispatch(
                frame,
                receiverObject,
                methodName,
                blockObject,
                argumentsObjects);
    }

    protected DispatchHeadNode getHeadNode() {
        return NodeUtil.findParent(this, DispatchHeadNode.class);
    }

    public RubyCallNode findRubyCallNode() {
        DispatchHeadNode headNode = getHeadNode();
        Node parent = headNode.getParent();
        if (parent instanceof RubyCallNode) {
            return (RubyCallNode) parent;
        } else {
            return null;
        }
    }

    public DispatchAction getDispatchAction() {
        return dispatchAction;
    }
}
