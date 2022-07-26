/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.exception.AbstractTruffleException;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.RubyContext;
import org.truffleruby.core.VMPrimitiveNodes.InitStackOverflowClassesEagerlyNode;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.nodes.Node;

@GenerateUncached
public abstract class TranslateExceptionNode extends RubyBaseNode {

    public static TranslateExceptionNode create() {
        return TranslateExceptionNodeGen.create();
    }

    public abstract RuntimeException executeTranslation(Throwable throwable);

    public static void logJavaException(RubyContext context, Node currentNode, Throwable exception) {
        if (context.getOptions().EXCEPTIONS_PRINT_JAVA) {
            printStackTrace(exception);

            if (context.getOptions().EXCEPTIONS_PRINT_RUBY_FOR_JAVA) {
                context.getDefaultBacktraceFormatter().printBacktraceOnEnvStderr(currentNode);
            }
        }
    }

    public static void logUncaughtJavaException(RubyContext context, Node currentNode, Throwable exception) {
        if (context.getOptions().EXCEPTIONS_PRINT_JAVA || context.getOptions().EXCEPTIONS_PRINT_UNCAUGHT_JAVA) {
            printStackTrace(exception);

            if (context.getOptions().EXCEPTIONS_PRINT_RUBY_FOR_JAVA) {
                context.getDefaultBacktraceFormatter().printBacktraceOnEnvStderr(currentNode);
            }
        }
    }

    @Specialization
    protected RuntimeException translate(ControlFlowException e) {
        throw e;
    }

    @Specialization
    protected RuntimeException translate(AbstractTruffleException e) {
        throw e;
    }

    @Specialization
    protected RuntimeException translate(ThreadDeath e) {
        throw e;
    }

    @Specialization(guards = "needsSpecialTranslation(e)")
    protected RuntimeException translateSpecial(Throwable e) {
        throw doTranslateSpecial(e);
    }

    @Fallback
    protected RuntimeException translate(Throwable e) {
        // An internal exception
        CompilerDirectives.transferToInterpreterAndInvalidate();
        logUncaughtJavaException(getContext(), this, e);
        throw ExceptionOperations.rethrow(e);
    }

    protected boolean needsSpecialTranslation(Throwable e) {
        return e instanceof TruffleString.IllegalByteArrayLengthException ||
                e instanceof UnsupportedSpecializationException ||
                e instanceof StackOverflowError ||
                e instanceof OutOfMemoryError;
    }

    @TruffleBoundary
    private RaiseException doTranslateSpecial(Throwable e) {
        if (e instanceof TruffleString.IllegalByteArrayLengthException) {
            return new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
        } else if (e instanceof UnsupportedSpecializationException) {
            return new RaiseException(getContext(),
                    translateUnsupportedSpecialization(getContext(), (UnsupportedSpecializationException) e));
        } else if (e instanceof StackOverflowError) {
            return new RaiseException(getContext(), translateStackOverflow(getContext(), (StackOverflowError) e));
        } else {
            return new RaiseException(getContext(), translateOutOfMemory(getContext(), (OutOfMemoryError) e));
        }
    }

    @TruffleBoundary
    private RubyException translateStackOverflow(RubyContext context, StackOverflowError error) {
        boolean ignore = InitStackOverflowClassesEagerlyNode.ignore(error);
        if (!ignore) {
            if (context.getOptions().EXCEPTIONS_WARN_STACKOVERFLOW) {
                // We cannot afford to initialize the Log class
                System.err.print("[ruby] WARNING StackOverflowError\n");
            }

            logJavaException(context, this, error);
        }

        return context.getCoreExceptions().systemStackErrorStackLevelTooDeep(this, error, !ignore);
    }

    @TruffleBoundary
    private RubyException translateOutOfMemory(RubyContext context, OutOfMemoryError error) {
        if (context.getOptions().EXCEPTIONS_WARN_OUT_OF_MEMORY) {
            // We cannot afford to initialize the Log class
            System.err.print("[ruby] WARNING OutOfMemoryError\n");
        }

        logJavaException(context, this, error);
        return context.getCoreExceptions().noMemoryError(this, error);
    }

    @TruffleBoundary
    private RubyException translateUnsupportedSpecialization(RubyContext context,
            UnsupportedSpecializationException exception) {

        logJavaException(context, this, exception);

        final StringBuilder builder = new StringBuilder();
        builder.append("TruffleRuby doesn't have a case for the ");
        builder.append(exception.getNode().getClass().getName());
        builder.append(" node with values of type");
        argumentsToString(builder, exception.getSuppliedValues());
        builder.append('\n');
        BacktraceFormatter.appendJavaStackTrace(exception, builder);
        String message = builder.toString().strip();
        return context.getCoreExceptions().typeError(message, this, exception);
    }

    public static StringBuilder argumentsToString(StringBuilder builder, Object[] arguments) {
        for (Object value : arguments) {
            builder.append(" ");

            if (value == null) {
                builder.append("null");
            } else if (value instanceof RubyDynamicObject) {
                final RubyDynamicObject dynamicObject = (RubyDynamicObject) value;

                builder.append(dynamicObject.getLogicalClass().fields.getName());
                builder.append("(");
                builder.append(value.getClass().getName());
                builder.append(")");

                if (value instanceof RubyArray) {
                    final RubyArray array = (RubyArray) value;
                    builder.append("[");

                    if (array.getStore() == null) {
                        builder.append("null");
                    } else {
                        builder.append(array.getStore().getClass().getName());
                    }

                    builder.append(",");
                    builder.append(array.size);
                    builder.append("]");
                } else if (RubyGuards.isRubyHash(value)) {
                    final Object store = ((RubyHash) value).store;
                    builder.append("[");
                    builder.append(store.getClass().getName());
                    builder.append("]");
                }
            } else {
                builder.append(value.getClass().getName());
            }

            if (value instanceof Number || value instanceof Boolean) {
                builder.append("=");
                builder.append(value.toString());
            }
        }
        return builder;
    }

    @TruffleBoundary
    private static void printStackTrace(Throwable exception) {
        exception.printStackTrace();
    }

}
