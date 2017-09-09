/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.fiber;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.objects.ObjectIDOperations;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages Ruby {@code Fiber} objects for a given Ruby thread.
 */
public class FiberManager {

    private final RubyContext context;
    private final DynamicObject rootFiber;
    private DynamicObject currentFiber;
    private final Set<DynamicObject> runningFibers = Collections.newSetFromMap(new ConcurrentHashMap<DynamicObject, Boolean>());

    public FiberManager(RubyContext context, DynamicObject rubyThread) {
        this.context = context;
        this.rootFiber = FiberNodes.createRootFiber(context, rubyThread);
        this.currentFiber = rootFiber;
    }

    public DynamicObject getRootFiber() {
        return rootFiber;
    }

    public DynamicObject getCurrentFiber() {
        return currentFiber;
    }

    public void setCurrentFiber(DynamicObject fiber) {
        assert RubyGuards.isRubyFiber(fiber);
        currentFiber = fiber;
    }

    public void start(DynamicObject fiber) {
        Layouts.FIBER.setThread(fiber, Thread.currentThread());

        final long pThreadID = context.getNativePlatform().getThreads().pthread_self();
        Layouts.FIBER.setPThreadID(fiber, pThreadID);

        final DynamicObject rubyThread = Layouts.FIBER.getRubyThread(fiber);
        context.getThreadManager().initializeValuesBasedOnCurrentJavaThread(rubyThread, pThreadID);

        runningFibers.add(fiber);
        context.getSafepointManager().enterThread();

        // fully initialized
        Layouts.FIBER.getInitializedLatch(fiber).countDown();
    }

    public void cleanup(ThreadManager threadManager, DynamicObject fiber) {
        Layouts.FIBER.setAlive(fiber, false);

        threadManager.cleanupValuesBasedOnCurrentJavaThread();

        context.getSafepointManager().leaveThread();
        runningFibers.remove(fiber);

        Layouts.FIBER.setThread(fiber, null);
    }

    @TruffleBoundary
    public void shutdown() {
        for (DynamicObject fiber : runningFibers) {
            if (!Layouts.FIBER.getRootFiber(fiber)) {
                FiberNodes.exit(fiber);
            }
        }
    }

    public String getFiberDebugInfo() {
        final StringBuilder builder = new StringBuilder();

        for (DynamicObject fiber : runningFibers) {
            builder.append("  fiber @");
            builder.append(ObjectIDOperations.verySlowGetObjectID(context, fiber));
            builder.append(" #");

            final Thread thread = Layouts.FIBER.getThread(fiber);

            if (thread == null) {
                builder.append("(no Java thread)");
            } else {
                builder.append(thread.getId());
            }

            if (fiber == rootFiber) {
                builder.append(" (root)");
            }

            if (fiber == currentFiber) {
                builder.append(" (current)");
            }

            builder.append("\n");
        }

        if (builder.length() == 0) {
            return "  no fibers\n";
        } else {
            return builder.toString();
        }
    }

}
