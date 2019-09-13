/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.thread;

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class GetCurrentRubyThreadNode extends RubyBaseNode {

    public static GetCurrentRubyThreadNode create() {
        return GetCurrentRubyThreadNodeGen.create();
    }

    public abstract DynamicObject executeGetRubyThread(VirtualFrame frame);

    /*
     * Note: the frame argument is used as a dynamic parameter here. Otherwise, The TruffleDSL
     * assumes cached parameters or calls with only cached arguments (including calls with no
     * arguments) never change.
     *
     * Note: we need to check that the Fiber is still running on a Java thread to cache based on the
     * Java thread. If the Fiber finished its execution, the Java thread can be reused for another
     * Fiber belonging to another Ruby Thread, due to using a thread pool for Fibers.
     */
    @Specialization(guards = {
            "getCurrentJavaThread(frame) == cachedJavaThread",
            "hasThread(frame, cachedFiber)",
            /* Cannot cache a Thread instance when pre-initializing */
            "!preInitializing"
    }, limit = "getCacheLimit()")
    protected DynamicObject getRubyThreadCached(VirtualFrame frame,
            @Cached("isPreInitializing()") boolean preInitializing,
            @Cached("getCurrentJavaThread(frame)") Thread cachedJavaThread,
            @Cached("getCurrentRubyThread(frame)") DynamicObject cachedRubyThread,
            @Cached("getCurrentFiber(cachedRubyThread)") DynamicObject cachedFiber) {
        return cachedRubyThread;
    }

    @Specialization(replaces = "getRubyThreadCached")
    protected DynamicObject getRubyThreadUncached(VirtualFrame frame) {
        return getCurrentRubyThread(frame);
    }

    protected Thread getCurrentJavaThread(VirtualFrame frame) {
        return Thread.currentThread();
    }

    protected DynamicObject getCurrentRubyThread(VirtualFrame frame) {
        return getContext().getThreadManager().getCurrentThread();
    }

    protected DynamicObject getCurrentFiber(DynamicObject currentRubyThread) {
        return Layouts.THREAD.getFiberManager(currentRubyThread).getCurrentFiber();
    }

    protected boolean hasThread(VirtualFrame frame, DynamicObject fiber) {
        return Layouts.FIBER.getThread(fiber) != null;
    }

    protected boolean isPreInitializing() {
        return getContext().isPreInitializing();
    }

    protected int getCacheLimit() {
        return getContext().getOptions().THREAD_CACHE;
    }

}
