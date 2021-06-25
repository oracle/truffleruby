/*
 * Copyright (c) 2014, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import java.util.EnumSet;

import com.oracle.truffle.api.dsl.CachedLanguage;
import com.oracle.truffle.api.exception.AbstractTruffleException;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.VMPrimitiveNodes.InitStackOverflowClassesEagerlyNode;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.backtrace.BacktraceFormatter.FormattingFlags;
import org.truffleruby.language.backtrace.BacktraceInterleaver;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleStackTrace;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.UnsupportedSpecializationException;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.language.control.TerminationException;

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

    @SuppressWarnings("deprecation")
    @Specialization
    protected RuntimeException translate(Throwable throwable,
            @Cached BranchProfile controlProfile,
            @Cached BranchProfile raiseProfile,
            @Cached BranchProfile terminationProfile,
            @Cached BranchProfile arithmeticProfile,
            @Cached BranchProfile unsupportedProfile,
            @Cached BranchProfile errorProfile,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @CachedLanguage RubyLanguage language) {
        try {
            // Only throwing to use the pattern matching of catch
            throw throwable;
        } catch (ControlFlowException exception) {
            controlProfile.enter();
            return exception;
        } catch (RaiseException exception) {
            raiseProfile.enter();
            return exception;
        } catch (TerminationException exception) {
            terminationProfile.enter();
            return exception;
        } catch (ArithmeticException exception) {
            arithmeticProfile.enter();
            return new RaiseException(context, translateArithmeticException(context, exception));
        } catch (UnsupportedSpecializationException exception) {
            unsupportedProfile.enter();
            return new RaiseException(
                    context,
                    translateUnsupportedSpecialization(context, exception));
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
            if (context.getEnv().isHostException(exception)) {
                // rethrow host exceptions to get the interleaved host and guest stacktrace of PolyglotException
                logJavaException(context, this, exception);
                throw ExceptionOperations.rethrow(exception);
            } else if (exception instanceof com.oracle.truffle.api.TruffleException) {
                // A foreign exception
                return new RaiseException(
                        context,
                        translateForeignException(context, language, exception));
            } else {
                // An internal exception
                CompilerDirectives.transferToInterpreter(/* internal exceptions are fatal */);
                logUncaughtJavaException(context, this, exception);
                throw ExceptionOperations.rethrow(exception);
            }
        }
    }

    @TruffleBoundary
    private RubyException translateArithmeticException(RubyContext context, ArithmeticException exception) {
        logJavaException(context, this, exception);
        return context.getCoreExceptions().zeroDivisionError(this, exception);
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
    private RubyException translateIllegalArgument(RubyContext context, IllegalArgumentException exception) {
        logJavaException(context, this, exception);

        String message = exception.getMessage();

        if (message == null) {
            message = exception.toString();
        }

        return context.getCoreExceptions().argumentError(message, this, exception);
    }

    @TruffleBoundary
    private RubyException translateUnsupportedSpecialization(RubyContext context,
            UnsupportedSpecializationException exception) {

        logJavaException(context, this, exception);

        final StringBuilder builder = new StringBuilder();
        builder.append("TruffleRuby doesn't have a case for the ");
        builder.append(exception.getNode().getClass().getName());
        builder.append(" node with values of type");

        for (Object value : exception.getSuppliedValues()) {
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

                    if (array.store == null) {
                        builder.append("null");
                    } else {
                        builder.append(array.store.getClass().getName());
                    }

                    builder.append(",");
                    builder.append(array.size);
                    builder.append("]");
                } else if (RubyGuards.isRubyHash(value)) {
                    final Object store = ((RubyHash) value).store;

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
        return context.getCoreExceptions().typeError(message, this, exception);
    }

    @TruffleBoundary
    private RubyException translateForeignException(RubyContext context, RubyLanguage language, Throwable exception) {
        logJavaException(context, this, exception);

        // NOTE (eregon, 2 Feb. 2018): This could maybe be modeled as translating each exception to
        // a Ruby one and linking them with Ruby Exception#cause.
        // But currently we and MRI do not display the cause message or backtrace by default.

        final StringBuilder builder = new StringBuilder();
        boolean firstException = true;
        Backtrace lastBacktrace = null;
        Throwable t = exception;

        while (t != null) {
            if (t.getClass().getSimpleName().equals("LazyStackTrace")) {
                // Truffle's lazy stracktrace support, not a real exception
                break;
            }

            if (lastBacktrace != null) {
                appendTruffleStackTrace(context, language, builder, lastBacktrace);
                lastBacktrace = null;
            }

            if (!firstException) {
                builder.append("Caused by:\n");
            }

            if (t instanceof RaiseException) {
                // A Ruby exception as a cause of a Java or C-ext exception
                final RubyException rubyException = ((RaiseException) t).getException();

                // Add the backtrace in the message as otherwise we would only see the
                // internalError() backtrace.
                final BacktraceFormatter formatter = new BacktraceFormatter(
                        context,
                        language,
                        EnumSet.noneOf(FormattingFlags.class));
                final String formattedBacktrace = formatter
                        .formatBacktrace(rubyException, rubyException.backtrace);
                builder.append(formattedBacktrace).append('\n');
            } else {
                // Java exception, print it formatted like a Ruby exception
                builder.append(BacktraceFormatter.formatJavaThrowableMessage(t)).append('\n');

                if (t instanceof AbstractTruffleException) {
                    lastBacktrace = new Backtrace((AbstractTruffleException) t);
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
            return context.getCoreExceptions().runtimeError(builder.toString(), this, exception);
        }
    }

    private void appendTruffleStackTrace(RubyContext context, RubyLanguage language, StringBuilder builder,
            Backtrace backtrace) {
        final BacktraceFormatter formatter = new BacktraceFormatter(
                context,
                language,
                EnumSet.noneOf(FormattingFlags.class));
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

    @TruffleBoundary
    private static void printStackTrace(Throwable exception) {
        exception.printStackTrace();
    }

}
