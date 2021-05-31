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
import org.truffleruby.core.thread.RubyThread;

public interface SafepointPredicate {

    SafepointPredicate ALL_THREADS_AND_FIBERS = (context, rubyThread, action) -> true;

    SafepointPredicate CURRENT_FIBER_OF_THREAD = (context, thread, action) -> thread == action.getTargetThread() &&
            context
                    .getThreadManager()
                    .getRubyFiberFromCurrentJavaThread() == action.getTargetThread().fiberManager.getCurrentFiber();

    boolean test(RubyContext context, RubyThread currentThread, SafepointAction action);

}
