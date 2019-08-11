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

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

public class EnsureNode extends RubyNode {

    @Child private RubyNode tryPart;
    @Child private RubyNode ensurePart;

    @Child private SetExceptionVariableNode setExceptionVariableNode;

    private final BranchProfile rubyExceptionPath = BranchProfile.create();
    private final BranchProfile javaExceptionPath = BranchProfile.create();

    public EnsureNode(RubyNode tryPart, RubyNode ensurePart) {
        this.tryPart = tryPart;
        this.ensurePart = ensurePart;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object value;

        try {
            value = tryPart.execute(frame);
        } catch (RaiseException exception) {
            rubyExceptionPath.enter();
            setLastExceptionAndRunEnsure(frame, exception);
            throw exception;
        } catch (Throwable throwable) {
            javaExceptionPath.enter();
            ensurePart.doExecuteVoid(frame);
            throw throwable;
        }

        ensurePart.doExecuteVoid(frame);

        return value;
    }

    @Override
    public void doExecuteVoid(VirtualFrame frame) {
        try {
            tryPart.doExecuteVoid(frame);
        } catch (RaiseException exception) {
            rubyExceptionPath.enter();
            setLastExceptionAndRunEnsure(frame, exception);
            throw exception;
        } catch (Throwable throwable) {
            javaExceptionPath.enter();
            ensurePart.doExecuteVoid(frame);
            throw throwable;
        }

        ensurePart.doExecuteVoid(frame);
    }

    private void setLastExceptionAndRunEnsure(VirtualFrame frame, RaiseException exception) {
        if (setExceptionVariableNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            setExceptionVariableNode = insert(new SetExceptionVariableNode());
        }

        setExceptionVariableNode.setLastExceptionAndRun(frame, exception, ensurePart);
    }

}
