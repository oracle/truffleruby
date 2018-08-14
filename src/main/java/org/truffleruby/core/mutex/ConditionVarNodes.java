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
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.InterruptMode;
import org.truffleruby.core.thread.GetCurrentRubyThreadNode;
import org.truffleruby.core.thread.ThreadManager.BlockingAction;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.ReadObjectFieldNodeGen;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.objects.WriteObjectFieldNodeGen;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.HiddenKey;

import sun.util.resources.cldr.khq.LocaleNames_khq;

@CoreClass("Truffle::ConditionVariableOperations")
public abstract class ConditionVarNodes {

    private static final HiddenKey lockName = new HiddenKey("lock");
    private static final HiddenKey condName = new HiddenKey("condition");

    @Primitive(name = "condition_variable_initialize")
    public static abstract class InitializeConditionVariableNode extends PrimitiveArrayArgumentsNode {
        @Child WriteObjectFieldNode lockFieldNode = WriteObjectFieldNodeGen.create(lockName);
        @Child WriteObjectFieldNode condFieldNode = WriteObjectFieldNodeGen.create(condName);

        @Specialization
        public DynamicObject initializeConditionVariable(DynamicObject variable) {
            ReentrantLock lock = new ReentrantLock();
            lockFieldNode.write(variable, lock);
            condFieldNode.write(variable, lock.newCondition());
            return variable;
        }
    }

    @Primitive(name = "condition_variable_wait")
    public static abstract class WaitNode extends PrimitiveArrayArgumentsNode {
        @Child ReadObjectFieldNode lockFieldNode = ReadObjectFieldNodeGen.create(lockName, null);
        @Child ReadObjectFieldNode condFieldNode = ReadObjectFieldNodeGen.create(condName, null);
        @Child GetCurrentRubyThreadNode getCurrentRubyThreadNode = GetCurrentRubyThreadNode.create();

        @Specialization(guards = "isNil(timeout)")
        public DynamicObject waitTimeoutNil(VirtualFrame frame, DynamicObject self, DynamicObject mutex, DynamicObject timeout) {
            return waitTimeoutNotProived(frame, self, mutex, NotProvided.INSTANCE);
        }

        @Specialization
        public DynamicObject waitTimeoutNotProived(VirtualFrame frame, DynamicObject self, DynamicObject mutex, NotProvided notProvided) {
            final ReentrantLock mutexLock = Layouts.MUTEX.getLock(mutex);
            final ReentrantLock condLock = (ReentrantLock) lockFieldNode.execute(self);
            final Condition condition = (Condition) condFieldNode.execute(self);
            final DynamicObject thread = getCurrentRubyThreadNode.executeGetRubyThread(frame);

            InterruptMode interruptMode = Layouts.THREAD.getInterruptMode(thread);
            Layouts.THREAD.setInterruptMode(thread, InterruptMode.ON_BLOCKING);
            getContext().getThreadManager().runUntilResult(this, () -> {
                condLock.lockInterruptibly();
                return BlockingAction.SUCCESS;
            });
            try {
                mutexLock.unlock();
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
                getContext().getThreadManager().runUntilResult(this, () -> {
                    mutexLock.lockInterruptibly();
                    return BlockingAction.SUCCESS;
                });
                Layouts.THREAD.setInterruptMode(thread, interruptMode);
            }
            return self;
        }

        @Specialization
        public DynamicObject waitTimeout(VirtualFrame frame, DynamicObject self, DynamicObject mutex, long durationInMillis) {
            final long endTIme = System.nanoTime() + 1_000_000 * durationInMillis;
            final ReentrantLock mutexLock = Layouts.MUTEX.getLock(mutex);
            final ReentrantLock condLock = (ReentrantLock) lockFieldNode.execute(self);
            final Condition condition = (Condition) condFieldNode.execute(self);
            final DynamicObject thread = getCurrentRubyThreadNode.executeGetRubyThread(frame);

            InterruptMode interruptMode = Layouts.THREAD.getInterruptMode(thread);
            Layouts.THREAD.setInterruptMode(thread, InterruptMode.ON_BLOCKING);
            getContext().getThreadManager().runUntilResult(this, () -> {
                condLock.lockInterruptibly();
                return BlockingAction.SUCCESS;
            });
            try {
                mutexLock.unlock();
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
                getContext().getThreadManager().runUntilResult(this, () -> {
                    mutexLock.lockInterruptibly();
                    return BlockingAction.SUCCESS;
                });
                Layouts.THREAD.setInterruptMode(thread, interruptMode);
            }

            return self;
        }
    }

    @Primitive(name = "condition_variable_signal")
    public static abstract class SignalNode extends PrimitiveArrayArgumentsNode {
        @Child ReadObjectFieldNode lockFieldNode = ReadObjectFieldNodeGen.create(lockName, null);
        @Child ReadObjectFieldNode condFieldNode = ReadObjectFieldNodeGen.create(condName, null);
        @Child GetCurrentRubyThreadNode getCurrentRubyThreadNode = GetCurrentRubyThreadNode.create();

        @Specialization
        public DynamicObject signal(VirtualFrame frame, DynamicObject self) {
            final ReentrantLock condLock = (ReentrantLock) lockFieldNode.execute(self);
            final Condition condition = (Condition) condFieldNode.execute(self);
            getContext().getThreadManager().runUntilResult(this, () -> {
                try {
                    condLock.lockInterruptibly();
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
        @Child ReadObjectFieldNode lockFieldNode = ReadObjectFieldNodeGen.create(lockName, null);
        @Child ReadObjectFieldNode condFieldNode = ReadObjectFieldNodeGen.create(condName, null);
        @Child GetCurrentRubyThreadNode getCurrentRubyThreadNode = GetCurrentRubyThreadNode.create();

        @Specialization
        public DynamicObject broadcast(VirtualFrame frame, DynamicObject self) {
            final ReentrantLock condLock = (ReentrantLock) lockFieldNode.execute(self);
            final Condition condition = (Condition) condFieldNode.execute(self);

            getContext().getThreadManager().runUntilResult(this, () -> {
                try {
                    condLock.lockInterruptibly();
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
