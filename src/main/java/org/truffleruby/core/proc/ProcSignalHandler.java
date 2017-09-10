/*
 * Copyright (c) 2014, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.proc;

import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.fiber.FiberManager;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.platform.signal.Signal;
import org.truffleruby.platform.signal.SignalHandler;

public class ProcSignalHandler implements SignalHandler {

    private final RubyContext context;
    private final DynamicObject proc;

    public ProcSignalHandler(RubyContext context, DynamicObject proc) {
        assert RubyGuards.isRubyProc(proc);

        this.context = context;
        this.proc = proc;
    }

    @Override
    public void handle(Signal signal) {
        final DynamicObject rootThread = context.getThreadManager().getRootThread();
        final FiberManager fiberManager = Layouts.THREAD.getFiberManager(rootThread);

        context.getSafepointManager().pauseAllThreadsAndExecuteFromNonRubyThread(true, (rubyThread, currentNode) -> {
            if (rubyThread == rootThread &&
                    fiberManager.getRubyFiberFromCurrentJavaThread() == fiberManager.getCurrentFiber()) {
                ProcOperations.rootCall(proc);
            }
        });
    }

}
