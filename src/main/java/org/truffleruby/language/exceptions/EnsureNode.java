/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.exceptions;

import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.TerminationException;
import org.truffleruby.language.threadlocal.ThreadLocalGlobals;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

public class EnsureNode extends RubyContextSourceNode {

    @Child private RubyNode tryPart;
    @Child private RubyNode ensurePart;

    private final BranchProfile terminationProfile = BranchProfile.create();
    private final BranchProfile guestExceptionProfile = BranchProfile.create();
    private final BranchProfile javaExceptionProfile = BranchProfile.create();
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

    /** The reason this is so complicated is to avoid duplication of the ensurePart so it is PE'd only once, no matter
     * which execution paths are taken (GR-25608). */
    public Object executeCommon(VirtualFrame frame, boolean executeVoid) {
        Object value = nil;
        AbstractTruffleException guestException = null;
        Throwable javaException = null;

        try {
            if (executeVoid) {
                tryPart.doExecuteVoid(frame);
            } else {
                value = tryPart.execute(frame);
            }
        } catch (TerminationException e) {
            terminationProfile.enter();
            javaException = e;
        } catch (AbstractTruffleException e) {
            guestExceptionProfile.enter();
            guestException = e;
        } catch (Throwable throwable) {
            javaExceptionProfile.enter();
            javaException = throwable;
        }

        ThreadLocalGlobals threadLocalGlobals = null;
        Object previousException = null;
        if (guestException != null) {
            threadLocalGlobals = getLanguage().getCurrentThread().threadLocalGlobals;
            previousException = threadLocalGlobals.getLastException();
            threadLocalGlobals.setLastException(ExceptionOperations.getExceptionObject(guestException,
                    raiseExceptionProfile));
        }
        try {
            ensurePart.doExecuteVoid(frame);
        } finally {
            if (guestException != null) {
                threadLocalGlobals.setLastException(previousException);
            }
        }

        if (guestException != null) {
            throw guestException;
        } else if (javaException != null) {
            throw ExceptionOperations.rethrow(javaException);
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
