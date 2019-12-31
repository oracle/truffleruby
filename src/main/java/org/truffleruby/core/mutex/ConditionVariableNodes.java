/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.mutex;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.truffleruby.Layouts;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.thread.GetCurrentRubyThreadNode;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.core.thread.ThreadStatus;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.objects.AllocateObjectNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@CoreModule(value = "ConditionVariable", isClass = true)
public abstract class ConditionVariableNodes {

    @CoreMethod(names = {"__allocate__", "__dynamic_object_factory__"}, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass) {
            // condLock is only held for a short number of non-blocking instructions,
            // so there is no need to poll for safepoints while locking it.
            // It is an internal lock and so locking should be done with condLock.lock()
            // to avoid changing the Ruby Thread status and consume Java thread interrupts.
            final ReentrantLock condLock = new ReentrantLock();
            return allocateNode.allocate(rubyClass, condLock, condLock.newCondition(), 0, 0);
        }

    }

    @Primitive(name = "condition_variable_wait")
    public static abstract class WaitNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isNil(timeout)")
        protected DynamicObject waitTimeoutNil(VirtualFrame frame, DynamicObject conditionVariable, DynamicObject mutex,
                DynamicObject timeout,
                @Cached GetCurrentRubyThreadNode getCurrentRubyThreadNode,
                @Cached BranchProfile errorProfile) {
            final DynamicObject thread = getCurrentRubyThreadNode.executeGetRubyThread(frame);
            final ReentrantLock mutexLock = Layouts.MUTEX.getLock(mutex);

            MutexOperations.checkOwnedMutex(getContext(), mutexLock, this, errorProfile);
            waitInternal(conditionVariable, mutexLock, thread, -1);
            return conditionVariable;
        }

        @Specialization
        protected DynamicObject waitTimeout(VirtualFrame frame, DynamicObject conditionVariable, DynamicObject mutex,
                long timeout,
                @Cached GetCurrentRubyThreadNode getCurrentRubyThreadNode,
                @Cached BranchProfile errorProfile) {
            final DynamicObject thread = getCurrentRubyThreadNode.executeGetRubyThread(frame);
            final ReentrantLock mutexLock = Layouts.MUTEX.getLock(mutex);

            MutexOperations.checkOwnedMutex(getContext(), mutexLock, this, errorProfile);
            waitInternal(conditionVariable, mutexLock, thread, timeout);
            return conditionVariable;
        }

        @TruffleBoundary
        private void waitInternal(DynamicObject conditionVariable, ReentrantLock mutexLock, DynamicObject thread,
                long durationInNanos) {
            final ReentrantLock condLock = Layouts.CONDITION_VARIABLE.getLock(conditionVariable);
            final Condition condition = Layouts.CONDITION_VARIABLE.getCondition(conditionVariable);
            final long endNanoTime;
            if (durationInNanos >= 0) {
                endNanoTime = System.nanoTime() + durationInNanos;
            } else {
                endNanoTime = 0;
            }

            // Clear the wakeUp flag, following Ruby semantics:
            // it should only be considered if we are inside Mutex#sleep when Thread#{run,wakeup} is called.
            Layouts.THREAD.getWakeUp(thread).set(false);

            // condLock must be locked before unlocking mutexLock, to avoid losing potential signals.
            // We must not change the Ruby Thread status and not consume a Java thread interrupt while locking condLock.
            // If there is an interrupt, it should be consumed by condition.await() and the Ruby Thread sleep status
            // must imply being ready to be interrupted by Thread#{run,wakeup}.
            condLock.lock();
            try {
                mutexLock.unlock();

                Layouts.CONDITION_VARIABLE.setWaiters(
                        conditionVariable,
                        Layouts.CONDITION_VARIABLE.getWaiters(conditionVariable) + 1);
                try {
                    awaitSignal(conditionVariable, thread, durationInNanos, condLock, condition, endNanoTime);
                } catch (Error | RuntimeException e) {
                    /*
                     * Consume a signal if one was waiting. We do this because the error may have
                     * occurred while we were waiting, or at some point after exiting a safepoint that
                     * throws an exception and another thread has attempted to signal us. It is valid
                     * for us to consume this signal because we are still marked as waiting for it.
                     */
                    consumeSignal(conditionVariable);
                    throw e;
                } finally {
                    Layouts.CONDITION_VARIABLE.setWaiters(
                            conditionVariable,
                            Layouts.CONDITION_VARIABLE.getWaiters(conditionVariable) - 1);
                }
            } finally {
                condLock.unlock();
                MutexOperations.internalLockEvenWithException(mutexLock, this, getContext());
            }
        }

        /** This duplicates {@link ThreadManager#runUntilResult} because it needs fine grained control when polling for safepoints. */
        @SuppressFBWarnings(value = "UL")
        private void awaitSignal(DynamicObject self, DynamicObject thread, long durationInNanos, ReentrantLock condLock,
                Condition condition, long endNanoTime) {
            final ThreadStatus status = Layouts.THREAD.getStatus(thread);
            while (true) {
                Layouts.THREAD.setStatus(thread, ThreadStatus.SLEEP);
                try {
                    try {
                        /*
                         * We must not consumeSignal() here, as we should only consume a signal after being awaken by
                         * condition.signal() or condition.signalAll(). Otherwise, ConditionVariable#signal might
                         * condition.signal() a waiting thread, and then if the current thread calls ConditionVariable#wait
                         * before the waiting thread awakes, we might steal that waiting thread's signal with consumeSignal().
                         * So, we must await() first. spec/ruby/library/conditionvariable/signal_spec.rb is a good spec
                         * for this (run with repeats = 10000).
                         */
                        if (durationInNanos >= 0) {
                            final long currentTime = System.nanoTime();
                            if (currentTime >= endNanoTime) {
                                return;
                            }

                            condition.await(endNanoTime - currentTime, TimeUnit.NANOSECONDS);
                        } else {
                            condition.await();
                        }
                        if (consumeSignal(self)) {
                            return;
                        }
                    } finally {
                        Layouts.THREAD.setStatus(thread, status);
                    }
                } catch (InterruptedException e) {
                    /*
                     * Working with ConditionVariables is tricky because of safepoints. To call
                     * await or signal on a condition variable we must hold the lock, and that lock
                     * is released when we start waiting. However if the wait is interrupted then
                     * the lock will be reacquired before control returns to us. If we are
                     * interrupted for a safepoint then we must release the lock so that all threads
                     * can enter the safepoint, and acquire it again before resuming waiting.
                     */
                    condLock.unlock();
                    try {
                        getContext().getSafepointManager().pollFromBlockingCall(this);
                    } finally {
                        condLock.lock();
                    }

                    // Thread#{wakeup,run} might have woken us. In that a case, no signal is consumed.
                    if (Layouts.THREAD.getWakeUp(thread).getAndSet(false)) {
                        return;
                    }

                    // Check if a signal are available now, since another thread might have used
                    // ConditionVariable#signal while we released condLock to check for safepoints.
                    if (consumeSignal(self)) {
                        return;
                    }
                }
            }
        }

        private boolean consumeSignal(DynamicObject self) {
            if (Layouts.CONDITION_VARIABLE.getSignals(self) > 0) {
                Layouts.CONDITION_VARIABLE.setSignals(self, Layouts.CONDITION_VARIABLE.getSignals(self) - 1);
                return true;
            }
            return false;
        }

    }

    @CoreMethod(names = "signal")
    public static abstract class SignalNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject signal(DynamicObject self) {
            final ReentrantLock condLock = Layouts.CONDITION_VARIABLE.getLock(self);
            final Condition condition = Layouts.CONDITION_VARIABLE.getCondition(self);

            condLock.lock();
            try {
                if (Layouts.CONDITION_VARIABLE.getWaiters(self) > 0) {
                    Layouts.CONDITION_VARIABLE.setSignals(self, Layouts.CONDITION_VARIABLE.getSignals(self) + 1);
                    condition.signal();
                }
            } finally {
                condLock.unlock();
            }

            return self;
        }
    }

    @CoreMethod(names = "broadcast")
    public static abstract class BroadCastNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject broadcast(DynamicObject self) {
            final ReentrantLock condLock = Layouts.CONDITION_VARIABLE.getLock(self);
            final Condition condition = Layouts.CONDITION_VARIABLE.getCondition(self);

            condLock.lock();
            try {
                if (Layouts.CONDITION_VARIABLE.getWaiters(self) > 0) {
                    Layouts.CONDITION_VARIABLE.setSignals(
                            self,
                            Layouts.CONDITION_VARIABLE.getSignals(self) + Layouts.CONDITION_VARIABLE.getWaiters(self));
                    condition.signalAll();
                }
            } finally {
                condLock.unlock();
            }

            return self;
        }
    }
}
