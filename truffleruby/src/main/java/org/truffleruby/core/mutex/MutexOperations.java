/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.mutex;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;

import java.util.concurrent.locks.ReentrantLock;

public abstract class MutexOperations {

    @TruffleBoundary
    protected static void lock(ReentrantLock lock, DynamicObject thread, RubyNode currentNode) {
        final RubyContext context = currentNode.getContext();

        if (lock.isHeldByCurrentThread()) {
            throw new RaiseException(context.getCoreExceptions().threadErrorRecursiveLocking(currentNode));
        }

        context.getThreadManager().runUntilResult(currentNode, new ThreadManager.BlockingAction<Boolean>() {

            @Override
            public Boolean block() throws InterruptedException {
                lock.lockInterruptibly();
                Layouts.THREAD.getOwnedLocks(thread).add(lock);
                return SUCCESS;
            }

        });
    }

    @TruffleBoundary
    protected static void lockEvenWithExceptions(ReentrantLock lock, DynamicObject thread, RubyNode currentNode) {
        final RubyContext context = currentNode.getContext();

        if (lock.isHeldByCurrentThread()) {
            throw new RaiseException(context.getCoreExceptions().threadErrorRecursiveLocking(currentNode));
        }

        // We need to re-lock this lock after a Mutex#sleep, no matter what, even if another thread throw us an exception.
        // Yet, we also need to allow safepoints to happen otherwise the thread that could unlock could be blocked.
        Throwable throwable = null;
        try {
            while (true) {
                try {
                    context.getThreadManager().runUntilResult(currentNode, new ThreadManager.BlockingAction<Boolean>() {

                        @Override
                        public Boolean block() throws InterruptedException {
                            lock.lockInterruptibly();
                            Layouts.THREAD.getOwnedLocks(thread).add(lock);
                            return SUCCESS;
                        }

                    });
                } catch (Throwable t) {
                    throwable = t;
                }
                break;
            }
        } finally {
            if (!lock.isHeldByCurrentThread()) {
                throw new AssertionError("the lock could not be reacquired after Mutex#sleep");
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

    @TruffleBoundary
    protected static void unlock(ReentrantLock lock, DynamicObject thread, RubyNode currentNode) {
        final RubyContext context = currentNode.getContext();

        try {
            lock.unlock();
        } catch (IllegalMonitorStateException e) {
            if (!lock.isLocked()) {
                throw new RaiseException(context.getCoreExceptions().threadErrorUnlockNotLocked(currentNode));
            } else {
                throw new RaiseException(context.getCoreExceptions().threadErrorAlreadyLocked(currentNode));
            }
        }

        Layouts.THREAD.getOwnedLocks(thread).remove(lock);
    }

}
