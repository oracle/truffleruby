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
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.InterruptMode;
import org.truffleruby.core.thread.GetCurrentRubyThreadNode;
import org.truffleruby.core.thread.ThreadManager.BlockingAction;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocateObjectNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

@CoreClass("ConditionVariable")
public abstract class ConditionVariableNodes {

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            ReentrantLock lock = new ReentrantLock();
            return allocateNode.allocate(rubyClass, lock, lock.newCondition(), 0, 0);
        }

    }

    @Primitive(name = "condition_variable_wait")
    public static abstract class WaitNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isNil(timeout)")
        public DynamicObject waitTimeoutNil(VirtualFrame frame, DynamicObject self, DynamicObject mutex, DynamicObject timeout,
                @Cached("create()") GetCurrentRubyThreadNode getCurrentRubyThreadNode) {
            return waitTimeoutNotProived(frame, self, mutex, NotProvided.INSTANCE, getCurrentRubyThreadNode);
        }

        @Specialization
        public DynamicObject waitTimeoutNotProived(VirtualFrame frame, DynamicObject self, DynamicObject mutex, NotProvided notProvided,
                @Cached("create()") GetCurrentRubyThreadNode getCurrentRubyThreadNode) {
            final DynamicObject thread = getCurrentRubyThreadNode.executeGetRubyThread(frame);
            waitInternal(self, mutex, thread, -1);
            return self;
        }

        @Specialization
        public DynamicObject waitTimeout(VirtualFrame frame, DynamicObject self, DynamicObject mutex, long durationInNanos,
                @Cached("create()") GetCurrentRubyThreadNode getCurrentRubyThreadNode) {
            final DynamicObject thread = getCurrentRubyThreadNode.executeGetRubyThread(frame);
            waitInternal(self, mutex, thread, durationInNanos);
            return self;
        }

        @TruffleBoundary
        private void waitInternal(DynamicObject self, DynamicObject mutex, DynamicObject thread, long durationInNanos) {
            final ReentrantLock mutexLock = Layouts.MUTEX.getLock(mutex);
            final ReentrantLock condLock = Layouts.CONDITION_VARIABLE.getLock(self);
            final Condition condition = Layouts.CONDITION_VARIABLE.getCondition(self);
            final long endNanoTime;
            if (durationInNanos >= 0) {
                endNanoTime = System.nanoTime() + durationInNanos;
            } else {
                endNanoTime = 0;
            }

            InterruptMode interruptMode = Layouts.THREAD.getInterruptMode(thread);
            try {
                getConditionAndReleaseMutex(mutexLock, condLock, thread);
                try {
                    Layouts.CONDITION_VARIABLE.setWaiters(self, Layouts.CONDITION_VARIABLE.getWaiters(self) + 1);
                    getContext().getThreadManager().runUntilResultWithResumeAction(this, () -> {
                        /*
                         * Working with ConditionVariables is tricky because of safepoints. To call
                         * await or signal on a condition variable we must hold the lock, and that
                         * lock is released when we start waiting. However if the wait is
                         * interrupted then the lock will be reacquired before control returns to
                         * us. If we are interrupted for a safepoint then we must release the lock
                         * so that all threads can enter the safepoint, and acquire it again before
                         * resuming waiting.
                         */
                        try {
                            if (signalConsumed(self)) {
                                return BlockingAction.SUCCESS;
                            }
                            if (durationInNanos >= 0) {
                                final long currentTime = System.nanoTime();
                                if (currentTime < endNanoTime) {
                                    condition.await(endNanoTime - currentTime, TimeUnit.NANOSECONDS);
                                } else {
                                    Layouts.CONDITION_VARIABLE.setWaiters(self, Layouts.CONDITION_VARIABLE.getWaiters(self) - 1);
                                    return BlockingAction.SUCCESS;
                                }
                            } else {
                                condition.await();
                            }
                            if (signalConsumed(self)) {
                                return BlockingAction.SUCCESS;
                            } else {
                                return null;
                            }
                        } finally {
                            condLock.unlock();
                        }
                    }, () -> {
                        condLock.lock();
                    });
                } catch (Error | RuntimeException e) {
                    // Remove ourselves as a waiter and consume a signal if there is one.
                    try {
                        reacquireMutex(condLock);
                    } finally {
                        if (!signalConsumed(self)) {
                            Layouts.CONDITION_VARIABLE.setWaiters(self, Layouts.CONDITION_VARIABLE.getWaiters(self) - 1);
                        }
                        condLock.unlock();
                    }
                    throw e;
                } finally {
                    reacquireMutex(mutexLock);
                }
            } finally {
                Layouts.THREAD.setInterruptMode(thread, interruptMode);
            }
        }

        protected boolean signalConsumed(DynamicObject self) {
            if (Layouts.CONDITION_VARIABLE.getSignals(self) > 0) {
                Layouts.CONDITION_VARIABLE.setSignals(self, Layouts.CONDITION_VARIABLE.getSignals(self) - 1);
                Layouts.CONDITION_VARIABLE.setWaiters(self, Layouts.CONDITION_VARIABLE.getWaiters(self) - 1);
                return true;
            }
            return false;
        }

        @Fallback
        public Object waitFailure(Object self, Object mutex, Object duration) {
            return FAILURE;
        }

        protected void getConditionAndReleaseMutex(ReentrantLock mutexLock, ReentrantLock condLock, DynamicObject thread) {
            if (!mutexLock.isHeldByCurrentThread()) {
                if (!mutexLock.isLocked()) {
                    throw new RaiseException(getContext(), getContext().getCoreExceptions().threadErrorUnlockNotLocked(this));
                } else {
                    throw new RaiseException(getContext(), getContext().getCoreExceptions().threadErrorAlreadyLocked(this));
                }
            }

            Layouts.THREAD.setInterruptMode(thread, InterruptMode.ON_BLOCKING);
            getContext().getThreadManager().runUntilResult(this, () -> {
                condLock.lockInterruptibly();
                return BlockingAction.SUCCESS;
            });
            mutexLock.unlock();
        }

        @TruffleBoundary
        protected void reacquireMutex(ReentrantLock mutexLock) {
            MutexOperations.internalLockEvenWithException(mutexLock, this, getContext());
        }
    }

    @Primitive(name = "condition_variable_signal")
    public static abstract class SignalNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public DynamicObject signal(VirtualFrame frame, DynamicObject self) {
            final ReentrantLock condLock = Layouts.CONDITION_VARIABLE.getLock(self);
            final Condition condition = Layouts.CONDITION_VARIABLE.getCondition(self);
            getContext().getThreadManager().runUntilResult(this, () -> {
                condLock.lockInterruptibly();
                return BlockingAction.SUCCESS;
            });

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

        @Specialization
        public DynamicObject broadcast(VirtualFrame frame, DynamicObject self) {
            final ReentrantLock condLock = Layouts.CONDITION_VARIABLE.getLock(self);
            final Condition condition = Layouts.CONDITION_VARIABLE.getCondition(self);

            getContext().getThreadManager().runUntilResult(this, () -> {
                condLock.lockInterruptibly();
                return BlockingAction.SUCCESS;
            });

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
