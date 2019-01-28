/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.core.module.ModuleFields;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.objects.ReadObjectFieldNode;

/**
 * A ControlFlowException holding a Ruby exception.
 */
public class RaiseException extends RuntimeException implements TruffleException {

    private static final long serialVersionUID = -4128190563044417424L;

    private final DynamicObject exception;
    private final boolean internal;

    public RaiseException(RubyContext context, DynamicObject exception) {
        this(context, exception, false);
    }

    public RaiseException(RubyContext context, DynamicObject exception, boolean internal) {
        this.exception = exception;
        this.internal = internal;

        final Backtrace backtrace = Layouts.EXCEPTION.getBacktrace(exception);
        if (backtrace != null) { // The backtrace could be null if for example a user backtrace was passed to Kernel#raise
            backtrace.setRaiseException(this);
        }

        assert !isSyntaxError() || getSourceLocation() != null;

        if (context.getOptions().BACKTRACE_ON_RAISE) {
            context.getDefaultBacktraceFormatter().printRubyExceptionOnEnvStderr(exception);
        }
    }

    public DynamicObject getException() {
        return exception;
    }

    @SuppressWarnings("sync-override")
    @Override
    public final Throwable fillInStackTrace() {
        return null;
    }

    @Override
    @TruffleBoundary
    public String getMessage() {
        final ModuleFields exceptionClass = Layouts.MODULE.getFields(Layouts.BASIC_OBJECT.getLogicalClass(exception));
        final String message = ExceptionOperations.messageToString(exceptionClass.getContext(), exception);
        return String.format("%s (%s)", message, exceptionClass.getName());
    }

    @Override
    public Node getLocation() {
        final Backtrace backtrace = Layouts.EXCEPTION.getBacktrace(exception);

        if (backtrace == null) {
            // The backtrace could be null if for example a user backtrace was passed to Kernel#raise
            return null;
        } else {
            return backtrace.getLocation();
        }
    }

    @Override
    public Object getExceptionObject() {
        return exception;
    }

    @Override
    public boolean isSyntaxError() {
        final RubyContext context = RubyLanguage.getCurrentContext();
        return isA(context, context.getCoreLibrary().getSyntaxErrorClass());
    }

    @Override
    public SourceSection getSourceLocation() {
        if (isSyntaxError() && Layouts.EXCEPTION.getBacktrace(exception) != null) {
            return Layouts.EXCEPTION.getBacktrace(exception).getSourceLocation();
        } else {
            return TruffleException.super.getSourceLocation();
        }
    }

    @Override
    public boolean isInternalError() {
        return internal;
    }

    @Override
    public boolean isExit() {
        final RubyContext context = RubyLanguage.getCurrentContext();
        return isA(context, context.getCoreLibrary().getSystemExitClass());
    }

    @Override
    public int getExitStatus() {
        final Object status = ReadObjectFieldNode.read(exception, "@status", 1);

        if (status instanceof Integer) {
            return (int) status;
        }

        throw new UnsupportedOperationException(String.format("Ruby exit exception status is not an integer (%s)", status.getClass()));
    }

    private boolean isA(RubyContext context, DynamicObject rubyClass) {
        return context.send(exception, "is_a?", rubyClass) == Boolean.TRUE;
    }

}
