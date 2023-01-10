/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.kernel;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.oracle.truffle.api.exception.AbstractTruffleException;
import org.truffleruby.RubyContext;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.exception.RubySystemExit;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.arguments.EmptyArgumentsDescriptor;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.control.ExitException;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class AtExitManager {

    private final RubyContext context;

    private final Deque<RubyProc> atExitHooks = new ConcurrentLinkedDeque<>();
    private final Deque<RubyProc> systemExitHooks = new ConcurrentLinkedDeque<>();

    public AtExitManager(RubyContext context) {
        this.context = context;
    }

    public void add(RubyProc block, boolean always) {
        if (always) {
            systemExitHooks.push(block);
        } else {
            atExitHooks.push(block);
        }
    }

    public AbstractTruffleException runAtExitHooks() {
        return runExitHooks(atExitHooks, "at_exit");
    }

    public void runSystemExitHooks() {
        runExitHooks(systemExitHooks, "system at_exit");
    }

    @TruffleBoundary
    private AbstractTruffleException runExitHooks(Deque<RubyProc> stack, String name) {
        AbstractTruffleException lastException = null;

        while (true) {
            RubyProc block = stack.poll();
            if (block == null) {
                return lastException;
            }

            try {
                ProcOperations.rootCall(block, EmptyArgumentsDescriptor.INSTANCE, RubyBaseNode.EMPTY_ARGUMENTS);
            } catch (ExitException | ThreadDeath e) {
                throw e;
            } catch (AbstractTruffleException e) {
                handleAtExitException(context, e);
                lastException = e;
            } catch (RuntimeException | Error e) {
                BacktraceFormatter.printInternalError(context, e, "Unexpected internal exception in " + name);
            }
        }
    }

    public List<RubyProc> getHandlers() {
        final List<RubyProc> handlers = new ArrayList<>();
        handlers.addAll(atExitHooks);
        handlers.addAll(systemExitHooks);
        return handlers;
    }

    public static boolean isSilentException(RubyContext context, AbstractTruffleException exception) {
        if (!(exception instanceof RaiseException)) {
            return false;
        }

        final RubyException rubyException = ((RaiseException) exception).getException();
        // The checks are kind_of?(SystemExit) || instance_of?(SignalException),
        // see error_handle() in eval_error.c in CRuby. So Interrupt is not silent.
        return rubyException instanceof RubySystemExit ||
                rubyException.getLogicalClass() == context.getCoreLibrary().signalExceptionClass;
    }

    private static void handleAtExitException(RubyContext context, AbstractTruffleException exception) {
        if (!isSilentException(context, exception)) {
            context.getDefaultBacktraceFormatter().printRubyExceptionOnEnvStderr("", exception);
        }
    }
}
