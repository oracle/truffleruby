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

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.Shape;

import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.mutex.MutexOperations;
import org.truffleruby.core.mutex.RubyConditionVariable;
import org.truffleruby.core.mutex.MutexOperations;
import org.truffleruby.core.mutex.RubyMutex;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.thread.GetCurrentRubyThreadNode;
import org.truffleruby.core.thread.RubyThread;
import org.truffleruby.language.objects.AllocationTracing;

@CoreModule("Truffle::MonitorOperations")
public abstract class TruffleMonitorNodes {

    @CoreMethod(names = "condition_var_for_mutex", onSingleton = true, required = 2)
    public abstract static class LinkedConditionVariableNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyConditionVariable linkedConditionVariable(RubyClass rubyClass, RubyMutex mutex) {
            final ReentrantLock condLock = mutex.lock;
            final Condition condition = MutexOperations.newCondition(condLock);
            final Shape shape = getLanguage().conditionVariableShape;
            final RubyConditionVariable instance = new RubyConditionVariable(rubyClass, shape, condLock, condition);
            AllocationTracing.trace(instance, this);
            return instance;
        }
    }

    @CoreMethod(names = "synchronize_on_mutex", onSingleton = true, required = 1, needsBlock = true)
    public abstract static class SynchronizeNode extends YieldingCoreMethodNode {

        @Specialization(guards = "isRubyProc(maybeBlock)")
        protected Object synchronizeOnMutex(RubyMutex mutex, Object maybeBlock,
                @Cached GetCurrentRubyThreadNode getCurrentRubyThreadNode) {
            final RubyThread thread = getCurrentRubyThreadNode.execute();
            MutexOperations.lock(getContext(), mutex.lock, thread, this);
            try {
                return callBlock((RubyProc) maybeBlock);
            } finally {
                MutexOperations.unlock(mutex.lock, thread);
            }
        }

        @Specialization(guards = "!isRubyProc(maybeBlock)")
        protected Object synchronizeOnMutexNoBlock(RubyMutex mutex, Object maybeBlock,
                @Cached GetCurrentRubyThreadNode getCurrentRubyThreadNode) {
            final RubyThread thread = getCurrentRubyThreadNode.execute();
            MutexOperations.lock(getContext(), mutex.lock, thread, this);
            try {
                return nil;
            } finally {
                MutexOperations.unlock(mutex.lock, thread);
            }
        }
    }

    @CoreMethod(names = "mon_try_enter", onSingleton = true, required = 1)
    public static abstract class MonitorTryEnter extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object tryEnter(RubyMutex mutex,
                @Cached GetCurrentRubyThreadNode getCurrentRubyThreadNode) {
            final RubyThread thread = getCurrentRubyThreadNode.execute();
            return MutexOperations.tryLock(mutex.lock, thread);
        }
    }

    @CoreMethod(names = "mon_enter", onSingleton = true, required = 1)
    public static abstract class MonitorEnter extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object enter(RubyMutex mutex,
                @Cached GetCurrentRubyThreadNode getCurrentRubyThreadNode) {
            final RubyThread thread = getCurrentRubyThreadNode.execute();
            MutexOperations.lock(getContext(), mutex.lock, thread, this);
            return mutex.lock.getHoldCount();
        }
    }

    @CoreMethod(names = "mon_exit", onSingleton = true, required = 1)
    public static abstract class MonitorExit extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object exit(RubyMutex mutex,
                @Cached GetCurrentRubyThreadNode getCurrentRubyThreadNode) {
            final RubyThread thread = getCurrentRubyThreadNode.execute();
            MutexOperations.unlock(mutex.lock, thread);
            return mutex;
        }
    }
}
