/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.TruffleStackTrace;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.Layouts;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.backtrace.BacktraceFormatter.FormattingFlags;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.TruffleFatalException;

import java.util.EnumSet;

public class ExceptionTranslatingNode extends RubyNode {

    private final UnsupportedOperationBehavior unsupportedOperationBehavior;

    @Child private RubyNode child;

    private final BranchProfile exceptionProfile = BranchProfile.create();
    private final BranchProfile controlProfile = BranchProfile.create();
    private final BranchProfile raiseProfile = BranchProfile.create();
    private final BranchProfile arithmeticProfile = BranchProfile.create();
    private final BranchProfile unsupportedProfile = BranchProfile.create();
    private final BranchProfile errorProfile = BranchProfile.create();

    public ExceptionTranslatingNode(RubyNode child,
                                    UnsupportedOperationBehavior unsupportedOperationBehavior) {
        this.child = child;
        this.unsupportedOperationBehavior = unsupportedOperationBehavior;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        try {
            return child.execute(frame);
        } catch (Throwable t) {
            exceptionProfile.enter();
            throw translate(t);
        }
    }

    public RuntimeException translate(Throwable throwable) {
        try {
            // Only throwing to use the pattern matching of catch
            throw throwable;
        } catch (ControlFlowException exception) {
            controlProfile.enter();
            throw exception;
        } catch (RaiseException exception) {
            raiseProfile.enter();
            throw exception;
        } catch (ArithmeticException exception) {
            arithmeticProfile.enter();
            throw new RaiseException(getContext(), translateArithmeticException(exception));
        } catch (UnsupportedSpecializationException exception) {
            unsupportedProfile.enter();
            throw new RaiseException(getContext(), translateUnsupportedSpecialization(exception));
        } catch (TruffleFatalException exception) {
            errorProfile.enter();
            throw exception;
        } catch (StackOverflowError error) {
            errorProfile.enter();
            throw new RaiseException(getContext(), translateStackOverflow(error));
        } catch (OutOfMemoryError error) {
            errorProfile.enter();
            throw new RaiseException(getContext(), translateOutOfMemory(error));
        } catch (ThreadDeath death) {
            errorProfile.enter();
            throw death;
        } catch (IllegalArgumentException e) {
            errorProfile.enter();
            throw new RaiseException(getContext(), translateIllegalArgument(e));
        } catch (Throwable exception) {
            errorProfile.enter();
            throw new RaiseException(getContext(), translateThrowable(exception), true);
        }
    }

    @TruffleBoundary
    private DynamicObject translateArithmeticException(ArithmeticException exception) {
        if (getContext().getOptions().EXCEPTIONS_PRINT_JAVA) {
            exception.printStackTrace();

            if (getContext().getOptions().EXCEPTIONS_PRINT_RUBY_FOR_JAVA) {
                getContext().getDefaultBacktraceFormatter().printBacktraceOnEnvStderr(this);
            }
        }

        return coreExceptions().zeroDivisionError(this, exception);
    }

    @TruffleBoundary
    private DynamicObject translateStackOverflow(StackOverflowError error) {
        if (getContext().getOptions().EXCEPTIONS_WARN_STACKOVERFLOW) {
            // We cannot afford to initialize the Log class
            System.err.print("[ruby] WARNING StackOverflowError\n");
        }

        if (getContext().getOptions().EXCEPTIONS_PRINT_JAVA) {
            error.printStackTrace();

            if (getContext().getOptions().EXCEPTIONS_PRINT_RUBY_FOR_JAVA) {
                getContext().getDefaultBacktraceFormatter().printBacktraceOnEnvStderr(this);
            }
        }

        return coreExceptions().systemStackErrorStackLevelTooDeep(this, error);
    }

    @TruffleBoundary
    private DynamicObject translateOutOfMemory(OutOfMemoryError error) {
        if (getContext().getOptions().EXCEPTIONS_WARN_OUT_OF_MEMORY) {
            // We cannot afford to initialize the Log class
            System.err.print("[ruby] WARNING OutOfMemoryError\n");
        }

        if (getContext().getOptions().EXCEPTIONS_PRINT_JAVA) {
            error.printStackTrace();

            if (getContext().getOptions().EXCEPTIONS_PRINT_RUBY_FOR_JAVA) {
                getContext().getDefaultBacktraceFormatter().printBacktraceOnEnvStderr(this);
            }
        }

        return coreExceptions().noMemoryError(this, error);
    }

    @TruffleBoundary
    private DynamicObject translateIllegalArgument(IllegalArgumentException exception) {
        if (getContext().getOptions().EXCEPTIONS_PRINT_JAVA) {
            exception.printStackTrace();

            if (getContext().getOptions().EXCEPTIONS_PRINT_RUBY_FOR_JAVA) {
                getContext().getDefaultBacktraceFormatter().printBacktraceOnEnvStderr(this);
            }
        }

        String message = exception.getMessage();

        if (message == null) {
            message = exception.toString();
        }

        return coreExceptions().argumentError(message, this, exception);
    }

    @TruffleBoundary
    private DynamicObject translateUnsupportedSpecialization(UnsupportedSpecializationException exception) {
        if (getContext().getOptions().EXCEPTIONS_PRINT_JAVA) {
            exception.printStackTrace();

            if (getContext().getOptions().EXCEPTIONS_PRINT_RUBY_FOR_JAVA) {
                getContext().getDefaultBacktraceFormatter().printBacktraceOnEnvStderr(this);
            }
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("Truffle doesn't have a case for the ");
        builder.append(exception.getNode().getClass().getName());
        builder.append(" node with values of type ");

        for (Object value : exception.getSuppliedValues()) {
            builder.append(" ");

            if (value == null) {
                builder.append("null");
            } else if (value instanceof DynamicObject) {
                final DynamicObject dynamicObject = (DynamicObject) value;

                builder.append(Layouts.MODULE.getFields(Layouts.BASIC_OBJECT.getLogicalClass(dynamicObject)).getName());
                builder.append("(");
                builder.append(value.getClass().getName());
                builder.append(")");

                if (RubyGuards.isRubyArray(value)) {
                    final DynamicObject array = (DynamicObject) value;
                    builder.append("[");

                    if (Layouts.ARRAY.getStore(array) == null) {
                        builder.append("null");
                    } else {
                        builder.append(Layouts.ARRAY.getStore(array).getClass().getName());
                    }

                    builder.append(",");
                    builder.append(Layouts.ARRAY.getSize(array));
                    builder.append("]");
                } else if (RubyGuards.isRubyHash(value)) {
                    final Object store = Layouts.HASH.getStore((DynamicObject) value);

                    if (store == null) {
                        builder.append("[null]");
                    } else {
                        builder.append("[");
                        builder.append(store.getClass().getName());
                        builder.append("]");
                    }
                }
            } else {
                builder.append(value.getClass().getName());
            }

            if (value instanceof Number || value instanceof Boolean) {
                builder.append("=");
                builder.append(value.toString());
            }
        }

        switch (unsupportedOperationBehavior) {
            case TYPE_ERROR:
                return coreExceptions().typeError(builder.toString(), this, exception);
            case ARGUMENT_ERROR:
                return coreExceptions().argumentError(builder.toString(), this, exception);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @TruffleBoundary
    private DynamicObject translateThrowable(Throwable throwable) {
        if (throwable instanceof AssertionError && !getContext().getOptions().EXCEPTIONS_TRANSLATE_ASSERT) {
            throw (AssertionError) throwable;
        }

        final boolean truffleException = throwable instanceof TruffleException;

        if (getContext().getOptions().EXCEPTIONS_PRINT_JAVA
                || (!truffleException && getContext().getOptions().EXCEPTIONS_PRINT_UNCAUGHT_JAVA)) {
            throwable.printStackTrace();

            if (getContext().getOptions().EXCEPTIONS_PRINT_RUBY_FOR_JAVA) {
                getContext().getDefaultBacktraceFormatter().printBacktraceOnEnvStderr(this);
            }
        }

        Throwable t = throwable;
        if (t instanceof JavaException) {
            t = t.getCause();
        }

        // NOTE (eregon, 2 Feb. 2018): This could maybe be modeled as translating each exception to
        // a Ruby one and linking them with Ruby Exception#cause.
        // But currently we and MRI do not display the cause message or backtrace by default.

        final StringBuilder builder = new StringBuilder();
        boolean firstException = true;
        Backtrace lastBacktrace = null;

        while (t != null) {
            if (t.getClass().getSimpleName().equals("LazyStackTrace")) {
                // Truffle's lazy stracktrace support, not a real exception
                break;
            }

            if (lastBacktrace != null) {
                appendTruffleStackTrace(builder, lastBacktrace);
                lastBacktrace = null;
            }

            if (!firstException) {
                builder.append("Caused by:\n");
            }

            if (t instanceof RaiseException) {
                // A Ruby exception as a cause of a Java or C-ext exception
                final DynamicObject rubyException = ((RaiseException) t).getException();

                // Add the backtrace in the message as otherwise we would only see the
                // internalError() backtrace.
                final BacktraceFormatter formatter = new BacktraceFormatter(getContext(), EnumSet.noneOf(FormattingFlags.class));
                final String formattedBacktrace = formatter.formatBacktrace(rubyException, Layouts.EXCEPTION.getBacktrace(rubyException));
                builder.append(formattedBacktrace).append('\n');
            } else {
                // Java exception, print it formatted like a Ruby exception
                final String message = t.getMessage();
                builder.append(message != null ? message : "<no message>");
                builder.append(" (").append(t.getClass().getSimpleName()).append(")\n");

                if (t instanceof TruffleException) {
                    lastBacktrace = new Backtrace((TruffleException) t);
                } else {
                    // Print the first 10 lines of the Java stacktrace
                    appendJavaStackTrace(t, builder, 10);

                    if (TruffleStackTrace.getStackTrace(t) != null) {
                        lastBacktrace = new Backtrace(t);
                    }
                }
            }

            t = t.getCause();
            firstException = false;
        }

        // When printing the backtrace of the exception, make it clear it's not a cause
        builder.append("Translated to internal error");

        if (lastBacktrace != null) {
            return coreExceptions().runtimeError(builder.toString(), lastBacktrace);
        } else {
            return coreExceptions().runtimeError(builder.toString(), this, throwable);
        }
    }

    private void appendTruffleStackTrace(StringBuilder builder, Backtrace backtrace) {
        final BacktraceFormatter formatter = new BacktraceFormatter(getContext(), EnumSet.noneOf(FormattingFlags.class));
        final String formattedBacktrace = formatter.formatBacktrace(null, backtrace);
        builder.append(formattedBacktrace).append('\n');
    }

    private void appendJavaStackTrace(Throwable t, StringBuilder builder, int limit) {
        final StackTraceElement[] stackTrace = t.getStackTrace();
        for (int i = 0; i < Math.min(stackTrace.length, limit); i++) {
            builder.append('\t').append("from ").append(stackTrace[i]).append('\n');
        }
    }

}
