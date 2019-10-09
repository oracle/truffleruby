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

import java.util.EnumSet;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseWithoutContextNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.backtrace.BacktraceFormatter.FormattingFlags;
import org.truffleruby.language.backtrace.BacktraceInterleaver;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.TruffleFatalException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.TruffleStackTrace;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@GenerateUncached
public abstract class TranslateExceptionNode extends RubyBaseWithoutContextNode {

    public static TranslateExceptionNode create() {
        return TranslateExceptionNodeGen.create();
    }

    public abstract RuntimeException executeTranslation(Throwable throwable,
            UnsupportedOperationBehavior unsupportedOperationBehavior);

    @Specialization
    protected RuntimeException translate(Throwable throwable, UnsupportedOperationBehavior unsupportedOperationBehavior,
            @Cached BranchProfile controlProfile,
            @Cached BranchProfile raiseProfile,
            @Cached BranchProfile arithmeticProfile,
            @Cached BranchProfile unsupportedProfile,
            @Cached BranchProfile errorProfile,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        try {
            // Only throwing to use the pattern matching of catch
            throw throwable;
        } catch (ControlFlowException exception) {
            controlProfile.enter();
            return exception;
        } catch (RaiseException exception) {
            raiseProfile.enter();
            return exception;
        } catch (ArithmeticException exception) {
            arithmeticProfile.enter();
            return new RaiseException(context, translateArithmeticException(context, exception));
        } catch (UnsupportedSpecializationException exception) {
            unsupportedProfile.enter();
            return new RaiseException(
                    context,
                    translateUnsupportedSpecialization(context, exception, unsupportedOperationBehavior));
        } catch (TruffleFatalException exception) {
            errorProfile.enter();
            return exception;
        } catch (StackOverflowError error) {
            errorProfile.enter();
            return new RaiseException(context, translateStackOverflow(context, error));
        } catch (OutOfMemoryError error) {
            errorProfile.enter();
            return new RaiseException(context, translateOutOfMemory(context, error));
        } catch (IllegalArgumentException e) {
            errorProfile.enter();
            return new RaiseException(context, translateIllegalArgument(context, e));
        } catch (ThreadDeath exception) {
            errorProfile.enter();
            // it cannot be returned and we want to propagate it always anyway
            throw exception;
        } catch (Throwable exception) {
            errorProfile.enter();
            return new RaiseException(context, translateThrowable(context, exception), true);
        }
    }

    @TruffleBoundary
    private DynamicObject translateArithmeticException(RubyContext context, ArithmeticException exception) {
        if (context.getOptions().EXCEPTIONS_PRINT_JAVA) {
            exception.printStackTrace();

            if (context.getOptions().EXCEPTIONS_PRINT_RUBY_FOR_JAVA) {
                context.getDefaultBacktraceFormatter().printBacktraceOnEnvStderr(this);
            }
        }

        return context.getCoreExceptions().zeroDivisionError(this, exception);
    }

    @TruffleBoundary
    private DynamicObject translateStackOverflow(RubyContext context, StackOverflowError error) {
        if (context.getOptions().EXCEPTIONS_WARN_STACKOVERFLOW) {
            // We cannot afford to initialize the Log class
            System.err.print("[ruby] WARNING StackOverflowError\n");
        }

        if (context.getOptions().EXCEPTIONS_PRINT_JAVA) {
            error.printStackTrace();

            if (context.getOptions().EXCEPTIONS_PRINT_RUBY_FOR_JAVA) {
                context.getDefaultBacktraceFormatter().printBacktraceOnEnvStderr(this);
            }
        }

        return context.getCoreExceptions().systemStackErrorStackLevelTooDeep(this, error);
    }

    @TruffleBoundary
    private DynamicObject translateOutOfMemory(RubyContext context, OutOfMemoryError error) {
        if (context.getOptions().EXCEPTIONS_WARN_OUT_OF_MEMORY) {
            // We cannot afford to initialize the Log class
            System.err.print("[ruby] WARNING OutOfMemoryError\n");
        }

        if (context.getOptions().EXCEPTIONS_PRINT_JAVA) {
            error.printStackTrace();

            if (context.getOptions().EXCEPTIONS_PRINT_RUBY_FOR_JAVA) {
                context.getDefaultBacktraceFormatter().printBacktraceOnEnvStderr(this);
            }
        }

        return context.getCoreExceptions().noMemoryError(this, error);
    }

    @TruffleBoundary
    private DynamicObject translateIllegalArgument(RubyContext context, IllegalArgumentException exception) {
        if (context.getOptions().EXCEPTIONS_PRINT_JAVA) {
            exception.printStackTrace();

            if (context.getOptions().EXCEPTIONS_PRINT_RUBY_FOR_JAVA) {
                context.getDefaultBacktraceFormatter().printBacktraceOnEnvStderr(this);
            }
        }

        String message = exception.getMessage();

        if (message == null) {
            message = exception.toString();
        }

        return context.getCoreExceptions().argumentError(message, this, exception);
    }

    @TruffleBoundary
    private DynamicObject translateUnsupportedSpecialization(
            RubyContext context,
            UnsupportedSpecializationException exception,
            UnsupportedOperationBehavior unsupportedOperationBehavior) {

        if (context.getOptions().EXCEPTIONS_PRINT_JAVA) {
            exception.printStackTrace();

            if (context.getOptions().EXCEPTIONS_PRINT_RUBY_FOR_JAVA) {
                context.getDefaultBacktraceFormatter().printBacktraceOnEnvStderr(this);
            }
        }

        final StringBuilder builder = new StringBuilder();
        builder.append("TruffleRuby doesn't have a case for the ");
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

        builder.append('\n');
        appendJavaStackTrace(exception, builder);
        String message = builder.toString().trim();

        switch (unsupportedOperationBehavior) {
            case TYPE_ERROR:
                return context.getCoreExceptions().typeError(message, this, exception);
            case ARGUMENT_ERROR:
                return context.getCoreExceptions().argumentError(message, this, exception);
            default:
                throw new UnsupportedOperationException();
        }
    }

    @TruffleBoundary
    private DynamicObject translateThrowable(RubyContext context, Throwable throwable) {
        if (throwable instanceof AssertionError && !context.getOptions().EXCEPTIONS_TRANSLATE_ASSERT) {
            throw (AssertionError) throwable;
        }

        final boolean truffleException = throwable instanceof TruffleException;

        if (context.getOptions().EXCEPTIONS_PRINT_JAVA ||
                (!truffleException && context.getOptions().EXCEPTIONS_PRINT_UNCAUGHT_JAVA)) {
            throwable.printStackTrace();

            if (context.getOptions().EXCEPTIONS_PRINT_RUBY_FOR_JAVA) {
                context.getDefaultBacktraceFormatter().printBacktraceOnEnvStderr(this);
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
                appendTruffleStackTrace(context, builder, lastBacktrace);
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
                final BacktraceFormatter formatter = new BacktraceFormatter(
                        context,
                        EnumSet.noneOf(FormattingFlags.class));
                final String formattedBacktrace = formatter
                        .formatBacktrace(rubyException, Layouts.EXCEPTION.getBacktrace(rubyException));
                builder.append(formattedBacktrace).append('\n');
            } else {
                // Java exception, print it formatted like a Ruby exception
                final String message = t.getMessage();
                builder.append(message != null ? message : "<no message>");
                builder.append(" (").append(t.getClass().getSimpleName()).append(")\n");

                if (t instanceof TruffleException) {
                    lastBacktrace = new Backtrace((TruffleException) t);
                } else {
                    appendJavaStackTrace(t, builder);

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
            return context.getCoreExceptions().runtimeError(builder.toString(), lastBacktrace);
        } else {
            return context.getCoreExceptions().runtimeError(builder.toString(), this, throwable);
        }
    }

    private void appendTruffleStackTrace(RubyContext context, StringBuilder builder, Backtrace backtrace) {
        final BacktraceFormatter formatter = new BacktraceFormatter(context, EnumSet.noneOf(FormattingFlags.class));
        final String formattedBacktrace = formatter.formatBacktrace(null, backtrace);
        builder.append(formattedBacktrace).append('\n');
    }

    private void appendJavaStackTrace(Throwable t, StringBuilder builder) {
        final StackTraceElement[] stackTrace = t.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            builder.append('\t').append("from ").append(stackTraceElement).append('\n');
            if (BacktraceInterleaver.isCallBoundary(stackTraceElement)) {
                break;
            }
        }
    }

}
