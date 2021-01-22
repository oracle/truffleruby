/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import org.truffleruby.RubyContext;
import org.truffleruby.core.fiber.FiberManager;
import org.truffleruby.core.thread.RubyThread;
import org.truffleruby.core.thread.ThreadManager;

import java.util.function.Predicate;

public interface SafepointPredicate extends Predicate<RubyThread> {

    static final SafepointPredicate ALL_THREADS_AND_FIBERS = rubyThread -> true;

    static SafepointPredicate currentFiberOfThread(RubyContext context, RubyThread targetThread) {
        final ThreadManager threadManager = context.getThreadManager();
        final FiberManager fiberManager = targetThread.fiberManager;

        return thread -> thread == targetThread &&
                threadManager.getRubyFiberFromCurrentJavaThread() == fiberManager.getCurrentFiber();
    }

}
