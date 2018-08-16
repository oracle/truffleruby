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
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocateObjectNode;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

@CoreClass("ConditionVariable")
public abstract class ConditionVarNodes {

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            ReentrantLock lock = new ReentrantLock();
            return allocateNode.allocate(rubyClass, lock, lock.newCondition());
        }

    }

    @Primitive(name = "condition_variable_wait")
    public static abstract class WaitNode extends PrimitiveArrayArgumentsNode {
        @Child GetCurrentRubyThreadNode getCurrentRubyThreadNode = GetCurrentRubyThreadNode.create();

        @Specialization(guards = "isNil(timeout)")
        public DynamicObject waitTimeoutNil(VirtualFrame frame, DynamicObject self, DynamicObject mutex, DynamicObject timeout) {
            return waitTimeoutNotProived(frame, self, mutex, NotProvided.INSTANCE);
        }

        @Specialization
        public DynamicObject waitTimeoutNotProived(VirtualFrame frame, DynamicObject self, DynamicObject mutex, NotProvided notProvided) {
            final ReentrantLock mutexLock = Layouts.MUTEX.getLock(mutex);
            final ReentrantLock condLock = Layouts.CONDITION_VARIABLE.getLock(self);
            final Condition condition = Layouts.CONDITION_VARIABLE.getCondition(self);
            final DynamicObject thread = getCurrentRubyThreadNode.executeGetRubyThread(frame);

            InterruptMode interruptMode = Layouts.THREAD.getInterruptMode(thread);
            try {
                getConditionAndRleleaseMutex(mutexLock, condLock, thread);
                try {
                    getContext().getThreadManager().runUntilResultWithResumeAction(this, () -> {
                        try {
                            condition.await();
                            return BlockingAction.SUCCESS;
                        } finally {
                            condLock.unlock();
                        }
                    }, () -> {
                        condLock.lock();
                    });
                } finally {
                    reacquireMutex(mutexLock, thread);
                }
            } finally {
                Layouts.THREAD.setInterruptMode(thread, interruptMode);
            }
            return self;
        }

        @Specialization
        public DynamicObject waitTimeout(VirtualFrame frame, DynamicObject self, DynamicObject mutex, long durationInMillis) {
            final long endTIme = System.nanoTime() + 1_000_000 * durationInMillis;
            final ReentrantLock mutexLock = Layouts.MUTEX.getLock(mutex);
            final ReentrantLock condLock = Layouts.CONDITION_VARIABLE.getLock(self);
            final Condition condition = Layouts.CONDITION_VARIABLE.getCondition(self);
            final DynamicObject thread = getCurrentRubyThreadNode.executeGetRubyThread(frame);

            InterruptMode interruptMode = Layouts.THREAD.getInterruptMode(thread);
            try {
                getConditionAndRleleaseMutex(mutexLock, condLock, thread);
                try {
                    getContext().getThreadManager().runUntilResultWithResumeAction(this, () -> {
                        try {
                            final long currentTime = System.nanoTime();
                            if (currentTime < endTIme) {
                                condition.await(endTIme - currentTime, TimeUnit.NANOSECONDS);
                            }
                            return BlockingAction.SUCCESS;
                        } finally {
                            condLock.unlock();
                        }
                    }, () -> {
                        condLock.lock();
                    });
                } finally {
                    reacquireMutex(mutexLock, thread);
                }
            } finally {
                Layouts.THREAD.setInterruptMode(thread, interruptMode);
            }

            return self;
        }

        @Fallback
        public Object waitFailure(Object string, Object range, Object length) {
            return FAILURE;
        }

        protected void getConditionAndRleleaseMutex(final ReentrantLock mutexLock, final ReentrantLock condLock, final DynamicObject thread) {
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

        protected void reacquireMutex(final ReentrantLock mutexLock, final DynamicObject thread) throws Error {
            Throwable throwable = null;
            while (true) {
                try {
                    getContext().getThreadManager().runUntilResult(this, () -> {
                        mutexLock.lockInterruptibly();
                        return BlockingAction.SUCCESS;
                    });
                    break;
                } catch (Throwable t) {
                    throwable = t;
                }
            }

            if (throwable != null) {
                if (throwable instanceof RuntimeException) {
                    throw (RuntimeException) throwable;
                } else if (throwable instanceof Error) {
                    throw (Error) throwable;
                } else {
                    throw new JavaException(throwable);
                }
            }
        }
    }

    @Primitive(name = "condition_variable_signal")
    public static abstract class SignalNode extends PrimitiveArrayArgumentsNode {
        @Child GetCurrentRubyThreadNode getCurrentRubyThreadNode = GetCurrentRubyThreadNode.create();

        @Specialization
        public DynamicObject signal(VirtualFrame frame, DynamicObject self) {
            final ReentrantLock condLock = Layouts.CONDITION_VARIABLE.getLock(self);
            final Condition condition = Layouts.CONDITION_VARIABLE.getCondition(self);
            getContext().getThreadManager().runUntilResult(this, () -> {
                condLock.lockInterruptibly();
                try {
                    condition.signal();
                } finally {
                    condLock.unlock();
                }
                return BlockingAction.SUCCESS;
            });

            return self;
        }
    }

    @Primitive(name = "condition_variable_broadcast")
    public static abstract class BroadCastNode extends PrimitiveArrayArgumentsNode {
        @Child GetCurrentRubyThreadNode getCurrentRubyThreadNode = GetCurrentRubyThreadNode.create();

        @Specialization
        public DynamicObject broadcast(VirtualFrame frame, DynamicObject self) {
            final ReentrantLock condLock = Layouts.CONDITION_VARIABLE.getLock(self);
            final Condition condition = Layouts.CONDITION_VARIABLE.getCondition(self);

            getContext().getThreadManager().runUntilResult(this, () -> {
                condLock.lockInterruptibly();
                try {
                    condition.signalAll();
                } finally {
                    condLock.unlock();
                }
                return BlockingAction.SUCCESS;
            });

            return self;
        }
    }
}
