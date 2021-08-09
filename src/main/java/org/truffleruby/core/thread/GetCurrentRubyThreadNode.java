/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.thread;

import org.truffleruby.core.fiber.RubyFiber;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public abstract class GetCurrentRubyThreadNode extends RubyBaseNode {

    public static GetCurrentRubyThreadNode create() {
        return GetCurrentRubyThreadNodeGen.create();
    }

    public final RubyThread execute() {
        return executeInternal(Boolean.TRUE);
    }

    /* We need to include a seemingly useless dynamic parameter, otherwise the Truffle DSL will assume calls with no
     * arguments such as getCurrentJavaThread() never change, but for instance the thread might change. */
    protected abstract RubyThread executeInternal(Object dynamicParameter);

    /* Note: we need to check that the Fiber is still running on a Java thread to cache based on the Java thread. If the
     * Fiber finished its execution, the Java thread can be reused for another Fiber belonging to another Ruby Thread,
     * due to using a thread pool for Fibers. */
    @Specialization(
            guards = {
                    "getCurrentJavaThread(dynamicParameter) == cachedJavaThread",
                    "hasThread(dynamicParameter, cachedFiber)",
                    /* Cannot cache a Thread instance when pre-initializing */
                    "!preInitializing" },
            limit = "getCacheLimit()")
    protected RubyThread getRubyThreadCached(Object dynamicParameter,
            @Cached("isPreInitializing()") boolean preInitializing,
            @Cached("getCurrentJavaThread(dynamicParameter)") Thread cachedJavaThread,
            @Cached("getCurrentRubyThread(dynamicParameter)") RubyThread cachedRubyThread,
            @Cached("getCurrentFiber(cachedRubyThread)") RubyFiber cachedFiber) {
        return cachedRubyThread;
    }

    @Specialization(replaces = "getRubyThreadCached")
    protected RubyThread getRubyThreadUncached(Object dynamicParameter) {
        return getCurrentRubyThread(dynamicParameter);
    }

    protected Thread getCurrentJavaThread(Object dynamicParameter) {
        return Thread.currentThread();
    }

    protected RubyThread getCurrentRubyThread(Object dynamicParameter) {
        return getContext().getThreadManager().getCurrentThread();
    }

    protected RubyFiber getCurrentFiber(RubyThread currentRubyThread) {
        return currentRubyThread.fiberManager.getCurrentFiber();
    }

    protected boolean hasThread(Object dynamicParameter, RubyFiber fiber) {
        return fiber.thread != null;
    }

    protected boolean isPreInitializing() {
        return getContext().isPreInitializing();
    }

    protected int getCacheLimit() {
        return getLanguage().options.THREAD_CACHE;
    }

}
