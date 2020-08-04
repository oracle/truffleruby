/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
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

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.control.ExitException;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;

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

    public DynamicObject runAtExitHooks() {
        return runExitHooks(atExitHooks, "at_exit");
    }

    public void runSystemExitHooks() {
        runExitHooks(systemExitHooks, "system at_exit");
    }

    @TruffleBoundary
    private DynamicObject runExitHooks(Deque<RubyProc> stack, String name) {
        DynamicObject lastException = null;

        while (true) {
            RubyProc block = stack.poll();
            if (block == null) {
                return lastException;
            }

            try {
                ProcOperations.rootCall(block);
            } catch (RaiseException e) {
                handleAtExitException(context, e.getException());
                lastException = e.getException();
            } catch (ExitException e) {
                throw e;
            } catch (Exception e) {
                System.err.println("Unexpected internal exception in " + name + ":");
                e.printStackTrace();
            }
        }
    }

    public List<RubyProc> getHandlers() {
        final List<RubyProc> handlers = new ArrayList<>();
        handlers.addAll(atExitHooks);
        handlers.addAll(systemExitHooks);
        return handlers;
    }

    public static boolean isSilentException(RubyContext context, RubyException rubyException) {
        final DynamicObject logicalClass = Layouts.BASIC_OBJECT.getLogicalClass(rubyException);
        return logicalClass == context.getCoreLibrary().systemExitClass ||
                logicalClass == context.getCoreLibrary().signalExceptionClass;
    }

    private static void handleAtExitException(RubyContext context, RubyException rubyException) {
        if (!isSilentException(context, rubyException)) {
            context.getDefaultBacktraceFormatter().printRubyExceptionOnEnvStderr("", rubyException);
        }
    }
}
