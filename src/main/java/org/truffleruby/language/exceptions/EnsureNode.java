/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.exceptions;

import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.threadlocal.ThreadLocalGlobals;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

public class EnsureNode extends RubyContextSourceNode {

    @Child private RubyNode tryPart;
    @Child private RubyNode ensurePart;

    private final BranchProfile rubyExceptionPath = BranchProfile.create();
    private final BranchProfile javaExceptionPath = BranchProfile.create();

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

    /** The reason this is so complicated is to avoid duplication of the ensurePart so it is PE'd only once, no matter
     * which execution paths are taken (GR-25608). */
    public Object executeCommon(VirtualFrame frame, boolean executeVoid) {
        Object value = nil;
        RaiseException raiseException = null;
        Throwable javaException = null;

        try {
            if (executeVoid) {
                tryPart.doExecuteVoid(frame);
            } else {
                value = tryPart.execute(frame);
            }
        } catch (RaiseException exception) {
            rubyExceptionPath.enter();
            raiseException = exception;
        } catch (Throwable throwable) {
            javaExceptionPath.enter();
            javaException = throwable;
        }

        ThreadLocalGlobals threadLocalGlobals = null;
        Object previousException = null;
        if (raiseException != null) {
            threadLocalGlobals = getLanguage().getCurrentThread().threadLocalGlobals;
            previousException = threadLocalGlobals.exception;
            threadLocalGlobals.exception = raiseException.getException();
        }
        try {
            ensurePart.doExecuteVoid(frame);
        } finally {
            if (raiseException != null) {
                threadLocalGlobals.exception = previousException;
            }
        }

        if (raiseException != null) {
            throw raiseException;
        } else if (javaException != null) {
            throw ExceptionOperations.rethrow(javaException);
        } else {
            return value;
        }
    }

}
