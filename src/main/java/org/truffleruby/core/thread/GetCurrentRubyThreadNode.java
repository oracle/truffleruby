/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.thread;

import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

public abstract class GetCurrentRubyThreadNode extends RubyNode {

    public static GetCurrentRubyThreadNode create() {
        return GetCurrentRubyThreadNodeGen.create();
    }

    public abstract DynamicObject executeGetRubyThread(VirtualFrame frame);

    @Specialization(
            guards = "getCurrentJavaThread(frame) == cachedJavaThread",
            limit = "getCacheLimit()"
    )
    protected DynamicObject getRubyThreadCached(VirtualFrame frame,
            @Cached("getCurrentJavaThread(frame)") Thread cachedJavaThread,
            @Cached("getCurrentRubyThread(frame)") DynamicObject cachedRubyThread) {
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

    protected int getCacheLimit() {
        return getContext().getOptions().THREAD_CACHE;
    }

}
