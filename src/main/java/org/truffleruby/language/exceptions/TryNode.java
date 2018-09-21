/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.exceptions;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleStackTraceElement;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.profiles.BranchProfile;

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.RetryException;
import org.truffleruby.language.methods.ExceptionTranslatingNode;

public class TryNode extends RubyNode {

    @Child private ExceptionTranslatingNode tryPart;
    @Children private final RescueNode[] rescueParts;
    @Child private RubyNode elsePart;
    private final boolean canOmitBacktrace;

    @Child private SetExceptionVariableNode setExceptionVariableNode;

    private final BranchProfile elseProfile = BranchProfile.create();
    private final BranchProfile controlFlowProfile = BranchProfile.create();
    private final BranchProfile raiseExceptionProfile = BranchProfile.create();

    public TryNode(ExceptionTranslatingNode tryPart, RescueNode[] rescueParts, RubyNode elsePart, boolean canOmitBacktrace) {
        this.tryPart = tryPart;
        this.rescueParts = rescueParts;
        this.elsePart = elsePart;
        this.canOmitBacktrace = canOmitBacktrace;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        while (true) {
            Object result;

            try {
                result = tryPart.execute(frame);
            } catch (RaiseException exception) {
                raiseExceptionProfile.enter();

                try {
                    return handleException(frame, exception);
                } catch (RetryException e) {
                    getContext().getSafepointManager().poll(this);
                    continue;
                }
            } catch (ControlFlowException exception) {
                controlFlowProfile.enter();
                throw exception;
            }

            elseProfile.enter();

            if (elsePart != null) {
                result = elsePart.execute(frame);
            }

            return result;
        }
    }

    @ExplodeLoop
    private Object handleException(VirtualFrame frame, RaiseException exception) {
        for (RescueNode rescue : rescueParts) {
            if (rescue.canHandle(frame, exception.getException())) {
                if (canOmitBacktrace) {
                    /* If we're in this branch, we've already determined that the rescue body doesn't access `$!`.
                     * Thus, we can safely skip writing that value. Writing to `$!` is quite expensive, so we want
                     * to avoid it wherever we can.
                     */
                    return rescue.execute(frame);
                } else {
                    /*
                     * We materialize the backtrace eagerly here, as the exception is being rescued and
                     * therefore the exception is no longer being thrown on the exception path and the
                     * lazy stacktrace is no longer filled.
                     */
                    TruffleStackTraceElement.fillIn(exception);

                    return setLastExceptionAndRunRescue(frame, exception, rescue);
                }
            }
        }

        throw exception;
    }

    private Object setLastExceptionAndRunRescue(VirtualFrame frame, RaiseException exception, RubyNode rescue) {
        if (setExceptionVariableNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            setExceptionVariableNode = insert(new SetExceptionVariableNode(getContext()));
        }

        return setExceptionVariableNode.setLastExceptionAndRun(frame, exception, rescue);
    }

}
