/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.Frame;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.dispatch.DispatchingNode;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;

public class InlinedDispatchNode extends RubyBaseNode implements DispatchingNode {

    @CompilationFinal(dimensions = 1) private final Assumption[] assumptions;

    @Child private LookupMethodOnSelfNode lookupNode;

    @Child private InlinedMethodNode inlinedMethod;

    private DispatchNode replacedBy = null;

    public InlinedDispatchNode(
            RubyLanguage language,
            InlinedMethodNode inlinedMethod,
            Assumption... assumptions) {

        this.assumptions = new Assumption[1 + assumptions.length];
        this.assumptions[0] = language.traceFuncUnusedAssumption.getAssumption();
        ArrayUtils.arraycopy(assumptions, 0, this.assumptions, 1, assumptions.length);

        lookupNode = LookupMethodOnSelfNode.create();

        this.inlinedMethod = inlinedMethod;
    }

    public Object call(Object receiver, String method) {
        final Object[] rubyArgs = RubyArguments.allocate(0);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        return dispatch(null, method, rubyArgs);
    }

    public Object call(Object receiver, String method, Object arg0) {
        final Object[] rubyArgs = RubyArguments.allocate(1);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setArgument(rubyArgs, 0, arg0);
        return dispatch(null, method, rubyArgs);
    }

    public Object call(Object receiver, String method, Object arg0, Object arg1) {
        final Object[] rubyArgs = RubyArguments.allocate(2);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setArgument(rubyArgs, 0, arg0);
        RubyArguments.setArgument(rubyArgs, 1, arg1);
        return dispatch(null, method, rubyArgs);
    }

    public Object call(Object receiver, String method, Object arg0, Object arg1, Object arg2) {
        final Object[] rubyArgs = RubyArguments.allocate(3);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setArgument(rubyArgs, 0, arg0);
        RubyArguments.setArgument(rubyArgs, 1, arg1);
        RubyArguments.setArgument(rubyArgs, 2, arg2);
        return dispatch(null, method, rubyArgs);
    }

    public Object call(Object receiver, String method, Object[] arguments) {
        return dispatch(null, method, RubyArguments.pack(null, null, null, null, null, receiver, nil, arguments));
    }

    public Object callWithFrame(Frame frame, Object receiver, String method) {
        final Object[] rubyArgs = RubyArguments.allocate(0);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        return dispatch(frame, method, rubyArgs);
    }

    public Object callWithFrame(Frame frame, Object receiver, String method, Object arg0) {
        final Object[] rubyArgs = RubyArguments.allocate(1);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setArgument(rubyArgs, 0, arg0);
        return dispatch(frame, method, rubyArgs);
    }

    public Object callWithFrame(Frame frame, Object receiver, String method, Object arg0, Object arg1) {
        final Object[] rubyArgs = RubyArguments.allocate(2);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setArgument(rubyArgs, 0, arg0);
        RubyArguments.setArgument(rubyArgs, 1, arg1);
        return dispatch(frame, method, rubyArgs);
    }

    public Object callWithFrame(Frame frame, Object receiver, String method, Object arg0, Object arg1, Object arg2) {
        final Object[] rubyArgs = RubyArguments.allocate(3);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, nil);
        RubyArguments.setArgument(rubyArgs, 0, arg0);
        RubyArguments.setArgument(rubyArgs, 1, arg1);
        RubyArguments.setArgument(rubyArgs, 2, arg2);
        return dispatch(frame, method, rubyArgs);
    }

    public Object callWithFrame(Frame frame, Object receiver, String method, Object[] arguments) {
        return dispatch(frame, method, RubyArguments.pack(null, null, null, null, null, receiver, nil, arguments));
    }

    public Object callWithBlock(Object receiver, String method, Object block) {
        final Object[] rubyArgs = RubyArguments.allocate(0);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, block);
        return dispatch(null, method, rubyArgs);
    }

    public Object callWithBlock(Object receiver, String method, Object block, Object arg0) {
        final Object[] rubyArgs = RubyArguments.allocate(1);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, block);
        RubyArguments.setArgument(rubyArgs, 0, arg0);
        return dispatch(null, method, rubyArgs);
    }

    public Object callWithBlock(Object receiver, String method, Object block, Object arg0, Object arg1) {
        final Object[] rubyArgs = RubyArguments.allocate(2);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, block);
        RubyArguments.setArgument(rubyArgs, 0, arg0);
        RubyArguments.setArgument(rubyArgs, 1, arg1);
        return dispatch(null, method, rubyArgs);
    }

    public Object callWithBlock(Object receiver, String method, Object block, Object arg0, Object arg1, Object arg2) {
        final Object[] rubyArgs = RubyArguments.allocate(3);
        RubyArguments.setSelf(rubyArgs, receiver);
        RubyArguments.setBlock(rubyArgs, block);
        RubyArguments.setArgument(rubyArgs, 0, arg0);
        RubyArguments.setArgument(rubyArgs, 1, arg1);
        RubyArguments.setArgument(rubyArgs, 2, arg2);
        return dispatch(null, method, rubyArgs);
    }

    public Object callWithBlock(Object receiver, String method, Object block, Object[] arguments) {
        return dispatch(null, method, RubyArguments.pack(null, null, null, null, null, receiver, block, arguments));
    }

    public Object dispatch(Frame frame, String methodName, Object[] rubyArgs) {
        Object receiver = RubyArguments.getSelf(rubyArgs);
        if ((lookupNode.lookupIgnoringVisibility(frame, receiver, methodName) != inlinedMethod.getMethod()) ||
                !Assumption.isValidAssumption(assumptions)) {
            return rewriteAndCallWithBlock(frame, methodName, rubyArgs);
        } else {
            try {
                return inlinedMethod.inlineExecute(frame, rubyArgs);
            } catch (InlinedMethodNode.RewriteException e) {
                return rewriteAndCallWithBlock(frame, methodName, rubyArgs);
            }
        }
    }

    private DispatchNode rewriteToDispatchNode() {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        synchronized (this) {
            if (replacedBy != null) {
                return replacedBy;
            } else {
                DispatchNode dispatchNode = DispatchNode.create();
                replacedBy = dispatchNode;
                return replace(dispatchNode, this + " could not be executed inline");
            }
        }
    }

    protected Object rewriteAndCallWithBlock(Frame frame, String methodName, Object[] rubyArgs) {
        return rewriteToDispatchNode().dispatch(frame, methodName, rubyArgs);
    }

}
