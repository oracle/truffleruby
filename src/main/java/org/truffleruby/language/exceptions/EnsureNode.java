/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.exceptions;

import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.language.control.KillException;
import org.truffleruby.language.threadlocal.ThreadLocalGlobals;

public class EnsureNode extends RubyContextSourceNode {

    @Child private RubyNode tryPart;
    @Child private RubyNode ensurePart;

    private final BranchProfile killExceptionProfile = BranchProfile.create();
    private final BranchProfile guestExceptionProfile = BranchProfile.create();
    private final BranchProfile controlFlowExceptionProfile = BranchProfile.create();
    private final ConditionProfile raiseExceptionProfile = ConditionProfile.create();

    public EnsureNode(RubyNode tryPart, RubyNode ensurePart) {
        this.tryPart = tryPart;
        this.ensurePart = ensurePart;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return executeCommon(frame, false);
    }

    @Override
    public void doExecuteVoid(VirtualFrame frame) {
        executeCommon(frame, true);
    }

    /** Based on {@link InteropLibrary#throwException(Object)}'s {@code TryCatchNode}. It only runs code in ensure for
     * guest exceptions (AbstractTruffleException), ControlFlowException or no exception. */
    public Object executeCommon(VirtualFrame frame, boolean executeVoid) {
        Object value = nil;
        RuntimeException rethrowException = null;
        AbstractTruffleException guestException = null;

        try {
            if (executeVoid) {
                tryPart.doExecuteVoid(frame);
            } else {
                value = tryPart.execute(frame);
            }
        } catch (KillException e) { // an AbstractTruffleException but must not set $!
            killExceptionProfile.enter();
            rethrowException = e;
        } catch (AbstractTruffleException e) {
            guestExceptionProfile.enter();
            guestException = e;
            rethrowException = e;
        } catch (ControlFlowException e) {
            controlFlowExceptionProfile.enter();
            rethrowException = e;
        }

        ThreadLocalGlobals threadLocalGlobals = null;
        Object previousException = null;

        if (guestException != null) {
            var exceptionObject = ExceptionOperations.getExceptionObject(guestException, raiseExceptionProfile);
            threadLocalGlobals = getLanguage().getCurrentThread().threadLocalGlobals;
            previousException = threadLocalGlobals.getLastException();
            threadLocalGlobals.setLastException(exceptionObject);
        }
        try {
            ensurePart.doExecuteVoid(frame);
        } finally {
            if (guestException != null) {
                threadLocalGlobals.setLastException(previousException);
            }
        }

        if (rethrowException != null) {
            throw rethrowException;
        } else {
            return value;
        }
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new EnsureNode(
                tryPart.cloneUninitialized(),
                ensurePart.cloneUninitialized());
        return copy.copyFlags(this);
    }

}
