/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.exception;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.module.ModuleFields;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.thread.ThreadNodes.ThreadGetExceptionNode;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.SourceSection;

public abstract class ExceptionOperations {

    public static final String SUPER_METHOD_ERROR = "SUPER_METHOD_ERROR";
    public static final String PROTECTED_METHOD_ERROR = "PROTECTED_METHOD_ERROR";
    public static final String PRIVATE_METHOD_ERROR = "PRIVATE_METHOD_ERROR";
    public static final String NO_METHOD_ERROR = "NO_METHOD_ERROR";
    public static final String NO_LOCAL_VARIABLE_OR_METHOD_ERROR = "NO_LOCAL_VARIABLE_OR_METHOD_ERROR";

    @TruffleBoundary
    public static String getMessage(Throwable throwable) {
        return throwable.getMessage();
    }

    @TruffleBoundary
    private static String messageFieldToString(RubyContext context, RubyException exception) {
        Object message = exception.message;
        if (message == null || message == Nil.INSTANCE) {
            final ModuleFields exceptionClass = Layouts.MODULE
                    .getFields(Layouts.BASIC_OBJECT.getLogicalClass(exception));
            return exceptionClass.getName(); // What Exception#message would return if no message is set
        } else if (RubyGuards.isRubyString(message)) {
            return StringOperations.getString((DynamicObject) message);
        } else {
            return message.toString();
        }
    }

    @TruffleBoundary
    public static String messageToString(RubyContext context, RubyException exception) {
        try {
            final Object messageObject = context.send(exception, "message");
            if (RubyGuards.isRubyString(messageObject)) {
                return StringOperations.getString((DynamicObject) messageObject);
            }
        } catch (Throwable e) {
            // Fall back to the internal message field
        }
        return messageFieldToString(context, exception);
    }

    public static RubyException createRubyException(RubyContext context, DynamicObject rubyClass, Object message,
            Node node, Throwable javaException) {
        return createRubyException(context, rubyClass, message, node, null, javaException);
    }

    public static RubyException createRubyException(RubyContext context, DynamicObject rubyClass, Object message,
            Node node, SourceSection sourceLocation, Throwable javaException) {
        final Backtrace backtrace = context.getCallStack().getBacktrace(node, sourceLocation, javaException);
        return createRubyException(context, rubyClass, message, backtrace);
    }

    @TruffleBoundary
    public static RubyException createRubyException(RubyContext context, DynamicObject rubyClass, Object message,
            Backtrace backtrace) {
        final Object cause = ThreadGetExceptionNode.getLastException(context);
        context.getCoreExceptions().showExceptionIfDebug(rubyClass, message, backtrace);
        final Shape shape = Layouts.CLASS.getInstanceFactory(rubyClass).getShape();
        return new RubyException(shape, message, backtrace, cause);
    }

    @TruffleBoundary
    public static RubySystemCallError createSystemCallError(RubyContext context, DynamicObject rubyClass,
            Object message, int errno, Backtrace backtrace) {
        final Object cause = ThreadGetExceptionNode.getLastException(context);
        context.getCoreExceptions().showExceptionIfDebug(rubyClass, message, backtrace);
        final Shape shape = Layouts.CLASS.getInstanceFactory(rubyClass).getShape();
        return new RubySystemCallError(shape, message, backtrace, cause, errno);
    }

    @TruffleBoundary // Exception#initCause is blacklisted in TruffleFeature
    public static void initCause(RaiseException exception, Throwable cause) {
        exception.initCause(cause);
    }

    public static DynamicObject getFormatter(String name, RubyContext context) {
        return (DynamicObject) Layouts.MODULE
                .getFields(context.getCoreLibrary().truffleExceptionOperationsModule)
                .getConstant(name)
                .getValue();
    }

    public static RuntimeException rethrow(Throwable throwable) {
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        } else if (throwable instanceof Error) {
            throw (Error) throwable;
        } else {
            throw new JavaException(throwable);
        }
    }

}
