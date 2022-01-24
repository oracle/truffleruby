/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.frame.Frame;
import org.truffleruby.language.CallStackManager;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;

/** A core method that should always be executed inline, without going through a CallTarget. That enables accessing the
 * caller frame efficiently and reliably.
 * <p>
 * If called from a foreign language, then the caller frame will be null. The node should check using
 * {@link #needCallerFrame(Frame, RootCallTarget)} that the caller frame is not null before using it (if it uses it), in
 * order to provide a useful exception in that case.
 * <p>
 * Such a method will not appear in backtraces. However, Ruby exceptions emitted from this node will be resent through a
 * CallTarget to get the proper backtrace.
 * <p>
 * Such a core method should not emit significantly more Graal nodes than a non-inlined call, as Truffle cannot decide
 * to not inline it, and that could lead to too big methods to compile. */
@GenerateNodeFactory
public abstract class AlwaysInlinedMethodNode extends RubyBaseNode {

    protected abstract Object executeMethod(Frame callerFrame, Object self, Object[] rubyArgs, RootCallTarget target);

    public Object callMethod(Frame callerFrame, Object[] rubyArgs, RootCallTarget target) {
        return executeMethod(callerFrame, RubyArguments.getSelf(rubyArgs), rubyArgs, target);
    }

    protected void needCallerFrame(Frame callerFrame, RootCallTarget target) {
        if (callerFrame == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw buildException(target);
        }

        assert CallStackManager.isRubyFrame(callerFrame);
    }

    protected void needCallerFrame(Frame callerFrame, String method) {
        if (callerFrame == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw buildException(method);
        }

        assert CallStackManager.isRubyFrame(callerFrame);
    }

    @TruffleBoundary
    private RaiseException buildException(RootCallTarget target) {
        return buildException(target.getRootNode().getName());
    }

    @TruffleBoundary
    private RaiseException buildException(String method) {
        return new RaiseException(getContext(), coreExceptions().runtimeError(
                method + " needs the caller frame but it was not passed (cannot be called directly from a foreign language)",
                getNode()));
    }

}
