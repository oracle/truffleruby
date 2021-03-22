/*
 * Copyright (c) 2021, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.truffleruby.RubyContext;
import org.truffleruby.core.exception.RubyException;

public class DeferredRaiseException extends Exception {

    private static final long serialVersionUID = -9202513314613691124L;

    public final ExceptionGetter exceptionGetter;

    public DeferredRaiseException(ExceptionGetter exceptionGetter) {
        this.exceptionGetter = exceptionGetter;
    }

    @TruffleBoundary
    public RaiseException getException(RubyContext context) {
        return new RaiseException(context, exceptionGetter.getException(context));
    }

    public interface ExceptionGetter {
        RubyException getException(RubyContext context);
    }

    public Throwable fillInStackTrace() {
        return this;
    }

}
