/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.exceptions;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleStackTrace;
import com.oracle.truffle.api.exception.AbstractTruffleException;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.exception.RubySystemExit;
import org.truffleruby.core.kernel.AtExitManager;
import org.truffleruby.core.thread.GetCurrentRubyThreadNode;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.backtrace.BacktraceInterleaver;
import org.truffleruby.language.control.ExitException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;

import java.io.PrintStream;
import java.util.EnumSet;

public class TopLevelRaiseHandler extends RubyContextNode {

    @Child private GetCurrentRubyThreadNode getCurrentRubyThreadNode;

    public int execute(Runnable body) {
        int exitCode = 0;
        RubyException caughtException = null;

        // Execute the main script
        try {
            body.run();
        } catch (RaiseException e) {
            caughtException = e.getException();
            exitCode = statusFromException(caughtException);
            setLastException(caughtException); // Set $! for at_exit
            // printing the main script exception is delayed after at_exit hooks
        } catch (ExitException e) {
            // hard #exit!, return immediately, skip at_exit hooks
            return e.getCode();
        } catch (RuntimeException | Error e) {
            printInternalError(e);
            return 1;
        }

        // Execute at_exit hooks (except if hard #exit!)
        try {
            RubyException atExitException = getContext().getAtExitManager().runAtExitHooks();
            if (atExitException != null) {
                exitCode = statusFromException(atExitException);
            }

            if (caughtException != null) {
                // print the main script exception now
                if (!AtExitManager.isSilentException(getContext(), caughtException)) {
                    getContext().getDefaultBacktraceFormatter().printTopLevelRubyExceptionOnEnvStderr(caughtException);
                }

                handleSignalException(caughtException);
            }
        } catch (ExitException e) {
            // hard #exit! during at_exit: ignore the main script exception
            exitCode = e.getCode();
        } catch (RuntimeException | Error e) { // Internal error
            printInternalError(e);
            return 1;
        }

        return exitCode;
    }

    private int statusFromException(RubyException exception) {
        if (exception instanceof RubySystemExit) {
            return ((RubySystemExit) exception).exitStatus;
        } else {
            return 1;
        }
    }

    private void setLastException(RubyException exception) {
        if (getCurrentRubyThreadNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            getCurrentRubyThreadNode = insert(GetCurrentRubyThreadNode.create());
        }

        getCurrentRubyThreadNode.execute().threadLocalGlobals.exception = exception;
    }

    private void handleSignalException(RubyException exception) {
        if (exception.getLogicalClass() == coreLibrary().signalExceptionClass) {
            // Calls raise(3) or no-op
            DispatchNode.getUncached().call(exception, "reached_top_level");
        }
    }

    @TruffleBoundary
    private void printInternalError(Throwable throwable) {
        final PrintStream stream = BacktraceFormatter.printStreamFor(getContext().getEnv().err());
        stream.println();
        stream.println("truffleruby: an internal exception escaped out of the interpreter,");
        stream.println("please report it to https://github.com/oracle/truffleruby/issues.");
        stream.println();
        stream.println("```");

        boolean firstException = true;
        Throwable t = throwable;

        while (t != null) {
            if (t.getClass().getSimpleName().equals("LazyStackTrace")) {
                // Truffle's lazy stracktrace support, not a real exception
                break;
            }

            if (!firstException) {
                stream.println("Caused by:");
            }

            if (t instanceof RaiseException) {
                // A Ruby exception as a cause of a Java or C-ext exception
                final RubyException rubyException = ((RaiseException) t).getException();

                final BacktraceFormatter formatter = new BacktraceFormatter(
                        getContext(),
                        getLanguage(),
                        EnumSet.noneOf(BacktraceFormatter.FormattingFlags.class));
                final String formattedBacktrace = formatter
                        .formatBacktrace(rubyException, rubyException.backtrace);
                stream.println(formattedBacktrace);
            } else {
                stream.println(BacktraceFormatter.formatJavaThrowableMessage(t));

                if (t instanceof AbstractTruffleException) {
                    // Foreign exception
                    printTruffleStackTrace(stream, new Backtrace((AbstractTruffleException) t));
                } else {
                    // Internal error, print it formatted like a Ruby exception
                    printJavaStackTrace(stream, t);

                    if (TruffleStackTrace.getStackTrace(t) != null) {
                        printTruffleStackTrace(stream, new Backtrace(t));
                    }
                }
            }

            t = t.getCause();
            firstException = false;
        }

        stream.println("```");
    }

    private void printTruffleStackTrace(PrintStream stream, Backtrace backtrace) {
        final BacktraceFormatter formatter = new BacktraceFormatter(
                getContext(),
                getLanguage(),
                EnumSet.noneOf(BacktraceFormatter.FormattingFlags.class));
        stream.println(formatter.formatBacktrace(null, backtrace));
    }

    private void printJavaStackTrace(PrintStream stream, Throwable t) {
        final StackTraceElement[] stackTrace = t.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            stream.println("\tfrom " + stackTraceElement);
            if (BacktraceInterleaver.isCallBoundary(stackTraceElement)) {
                break;
            }
        }
    }

}
