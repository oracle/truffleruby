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
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.dispatch.DispatchingNode;
import org.truffleruby.language.methods.LookupMethodOnSelfNode;

public class InlinedDispatchNode extends RubyContextNode implements DispatchingNode {

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

    public Object call(Object receiver, String method, Object... arguments) {
        return dispatch(null, receiver, method, nil, arguments);
    }

    public Object callWithBlock(Object receiver, String method, Object block, Object... arguments) {
        return dispatch(null, receiver, method, block, arguments);
    }

    public Object dispatch(Frame frame, Object receiver, String methodName, Object block, Object[] arguments) {
        if ((lookupNode.lookupIgnoringVisibility(frame, receiver, methodName) != inlinedMethod.getMethod()) ||
                !Assumption.isValidAssumption(assumptions)) {
            return rewriteAndCallWithBlock(frame, receiver, methodName, block, arguments);
        } else {
            try {
                return inlinedMethod.inlineExecute(frame, receiver, arguments, block);
            } catch (InlinedMethodNode.RewriteException e) {
                return rewriteAndCallWithBlock(frame, receiver, methodName, block, arguments);
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

    protected Object rewriteAndCallWithBlock(Frame frame, Object receiver, String methodName, Object block,
            Object... arguments) {
        return rewriteToDispatchNode().dispatch(frame, receiver, methodName, block, arguments);
    }

}
