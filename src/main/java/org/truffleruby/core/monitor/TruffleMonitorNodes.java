/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.monitor;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;

import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.core.mutex.MutexOperations;
import org.truffleruby.core.mutex.RubyMutex;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.thread.RubyThread;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.yield.CallBlockNode;

@CoreModule("Truffle::MonitorOperations")
public abstract class TruffleMonitorNodes {

    @Primitive(name = "monitor_synchronize")
    public abstract static class SynchronizeNode extends CoreMethodArrayArgumentsNode {

        @Child private CallBlockNode yieldNode = CallBlockNode.create();

        @Specialization
        protected Object synchronizeOnMutex(RubyMutex mutex, RubyProc block,
                @Cached BranchProfile errorProfile) {
            /* Like Mutex#synchronize we must maintain the owned locks list here as the monitor might be exited inside
             * synchronize block and then re-entered again before the end, and we have to make sure the list of owned
             * locks remains consistent. */
            final RubyThread thread = getLanguage().getCurrentThread();
            MutexOperations.lock(getContext(), mutex.lock, thread, this);
            try {
                return yieldNode.yield(block);
            } finally {
                MutexOperations.checkOwnedMutex(getContext(), mutex.lock, this, errorProfile);
                MutexOperations.unlock(mutex.lock, thread);
            }
        }

        @Specialization(guards = "!isRubyProc(block)")
        protected Object synchronizeOnMutexNoBlock(RubyMutex mutex, Object block) {
            throw new RaiseException(getContext(), coreExceptions().localJumpError("no block given", this));
        }
    }

    @Primitive(name = "monitor_try_enter")
    public abstract static class MonitorTryEnter extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object tryEnter(RubyMutex mutex) {
            final RubyThread thread = getLanguage().getCurrentThread();
            return MutexOperations.tryLock(mutex.lock, thread);
        }
    }

    @Primitive(name = "monitor_enter")
    public abstract static class MonitorEnter extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object enter(RubyMutex mutex) {
            final RubyThread thread = getLanguage().getCurrentThread();
            MutexOperations.lock(getContext(), mutex.lock, thread, this);
            return nil();
        }
    }

    @Primitive(name = "monitor_exit")
    public abstract static class MonitorExit extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object exit(RubyMutex mutex) {
            final RubyThread thread = getLanguage().getCurrentThread();
            MutexOperations.unlock(mutex.lock, thread);
            return mutex;
        }
    }
}
