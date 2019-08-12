/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.kernel;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;

public class AtExitManager {

    private final RubyContext context;

    private final Deque<DynamicObject> atExitHooks = new ConcurrentLinkedDeque<>();
    private final Deque<DynamicObject> systemExitHooks = new ConcurrentLinkedDeque<>();

    public AtExitManager(RubyContext context) {
        this.context = context;
    }

    public void add(DynamicObject block, boolean always) {
        if (always) {
            systemExitHooks.push(block);
        } else {
            atExitHooks.push(block);
        }
    }

    public DynamicObject runAtExitHooks() {
        return runExitHooks(atExitHooks);
    }

    public void runSystemExitHooks() {
        runExitHooks(systemExitHooks);
    }

    @TruffleBoundary
    private DynamicObject runExitHooks(Deque<DynamicObject> stack) {
        DynamicObject lastException = null;

        while (true) {
            DynamicObject block;
            try {
                block = stack.pop();
            } catch (NoSuchElementException e) {
                return lastException;
            }

            try {
                ProcOperations.rootCall(block);
            } catch (RaiseException e) {
                lastException = handleAtExitException(context, e);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public List<DynamicObject> getHandlers() {
        final List<DynamicObject> handlers = new ArrayList<>();
        handlers.addAll(atExitHooks);
        handlers.addAll(systemExitHooks);
        return handlers;
    }

    @TruffleBoundary
    public static DynamicObject handleAtExitException(RubyContext context, RaiseException raiseException) {
        final DynamicObject rubyException = raiseException.getException();
        if (Layouts.BASIC_OBJECT.getLogicalClass(rubyException) == context.getCoreLibrary().getSystemExitClass()) {
            // Do not show SystemExit errors, just track them for the exit status
        } else {
            context.getDefaultBacktraceFormatter().printRubyExceptionOnEnvStderr(rubyException);
        }
        return rubyException;
    }

}
