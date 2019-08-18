/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.mutex;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.truffleruby.Layouts;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.thread.GetCurrentRubyThreadNode;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.core.thread.ThreadManager.BlockingAction;
import org.truffleruby.core.thread.ThreadStatus;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.objects.AllocateObjectNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@CoreClass("ConditionVariable")
public abstract class ConditionVariableNodes {

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass) {
            ReentrantLock lock = new ReentrantLock();
            return allocateNode.allocate(rubyClass, lock, lock.newCondition(), 0, 0);
        }

    }

    @Primitive(name = "condition_variable_wait")
    public static abstract class WaitNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isNil(timeout)")
        protected DynamicObject waitTimeoutNil(VirtualFrame frame, DynamicObject self, DynamicObject mutex, DynamicObject timeout,
                @Cached GetCurrentRubyThreadNode getCurrentRubyThreadNode,
                @Cached BranchProfile errorProfile) {
            final DynamicObject thread = getCurrentRubyThreadNode.executeGetRubyThread(frame);
            final ReentrantLock mutexLock = Layouts.MUTEX.getLock(mutex);

            MutexOperations.checkOwnedMutex(getContext(), mutexLock, this, errorProfile);
            waitInternal(self, mutexLock, thread, -1);
            return self;
        }

        @Specialization
        protected DynamicObject waitTimeout(VirtualFrame frame, DynamicObject self, DynamicObject mutex, long durationInNanos,
                @Cached GetCurrentRubyThreadNode getCurrentRubyThreadNode,
                @Cached BranchProfile errorProfile) {
            final DynamicObject thread = getCurrentRubyThreadNode.executeGetRubyThread(frame);
            final ReentrantLock mutexLock = Layouts.MUTEX.getLock(mutex);

            MutexOperations.checkOwnedMutex(getContext(), mutexLock, this, errorProfile);
            waitInternal(self, mutexLock, thread, durationInNanos);
            return self;
        }

        @TruffleBoundary
        private void waitInternal(DynamicObject self, ReentrantLock mutexLock, DynamicObject thread, long durationInNanos) {
            final ReentrantLock condLock = Layouts.CONDITION_VARIABLE.getLock(self);
            final Condition condition = Layouts.CONDITION_VARIABLE.getCondition(self);
            final long endNanoTime;
            if (durationInNanos >= 0) {
                endNanoTime = System.nanoTime() + durationInNanos;
            } else {
                endNanoTime = 0;
            }

            // condLock must be locked before unlocking mutexLock, to avoid losing potential signals
            getContext().getThreadManager().runUntilResult(this, () -> {
                condLock.lockInterruptibly();
                return BlockingAction.SUCCESS;
            });
            mutexLock.unlock();

            Layouts.CONDITION_VARIABLE.setWaiters(self, Layouts.CONDITION_VARIABLE.getWaiters(self) + 1);
            try {
                awaitSignal(self, thread, durationInNanos, condLock, condition, endNanoTime);
            } catch (Error | RuntimeException e) {
                /*
                 * Consume a signal if one was waiting. We do this because the error may have
                 * occurred while we were waiting, or at some point after exiting a safepoint that
                 * throws an exception and another thread has attempted to signal us. It is valid
                 * for us to consume this signal because we are still marked as waiting for it.
                 */
                consumeSignal(self);
                throw e;
            } finally {
                Layouts.CONDITION_VARIABLE.setWaiters(self, Layouts.CONDITION_VARIABLE.getWaiters(self) - 1);
                condLock.unlock();
                MutexOperations.internalLockEvenWithException(mutexLock, this, getContext());
            }
        }

        /** This duplicates {@link ThreadManager#runUntilResult} because it needs fine grained control when polling for safepoints. */
        @SuppressFBWarnings(value = "UL")
        private void awaitSignal(DynamicObject self, DynamicObject thread, long durationInNanos, ReentrantLock condLock, Condition condition, long endNanoTime) {
            final ThreadStatus status = Layouts.THREAD.getStatus(thread);
            while (true) {
                Layouts.THREAD.setStatus(thread, ThreadStatus.SLEEP);
                try {
                    try {
                        if (consumeSignal(self)) {
                            return;
                        }
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

    @Primitive(name = "condition_variable_signal")
    public static abstract class SignalNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject signal(DynamicObject self) {
            final ReentrantLock condLock = Layouts.CONDITION_VARIABLE.getLock(self);
            final Condition condition = Layouts.CONDITION_VARIABLE.getCondition(self);

            if (!condLock.tryLock()) {
                getContext().getThreadManager().runUntilResult(this, () -> {
                    condLock.lockInterruptibly();
                    return BlockingAction.SUCCESS;
                });
            }

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

    @Primitive(name = "condition_variable_broadcast")
    public static abstract class BroadCastNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject broadcast(DynamicObject self) {
            final ReentrantLock condLock = Layouts.CONDITION_VARIABLE.getLock(self);
            final Condition condition = Layouts.CONDITION_VARIABLE.getCondition(self);

            if (!condLock.tryLock()) {
                getContext().getThreadManager().runUntilResult(this, () -> {
                    condLock.lockInterruptibly();
                    return BlockingAction.SUCCESS;
                });
            }

            try {
                if (Layouts.CONDITION_VARIABLE.getWaiters(self) > 0) {
                    Layouts.CONDITION_VARIABLE.setSignals(self, Layouts.CONDITION_VARIABLE.getSignals(self) + Layouts.CONDITION_VARIABLE.getWaiters(self));
                    condition.signalAll();
                }
            } finally {
                condLock.unlock();
            }

            return self;
        }
    }
}
