/*
 * Copyright (c) 2014, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.nodes.InvalidAssumptionException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.RubyLanguage;
import org.truffleruby.RubyContext;
import org.truffleruby.core.InterruptMode;
import org.truffleruby.core.fiber.FiberManager;
import org.truffleruby.platform.Signals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

public class SafepointManager {

    private final RubyContext context;

    private final Set<Thread> runningThreads = Collections.newSetFromMap(new ConcurrentHashMap<Thread, Boolean>());

    private final ReentrantLock lock = new ReentrantLock();

    private final Phaser phaser = new Phaser() {
        @Override
        protected boolean onAdvance(int phase, int registeredParties) {
            // This Phaser should not be automatically terminated,
            // even when registeredParties drops to 0.
            // This notably happens when pre-initializing the context.
            return false;
        }
    };

    @CompilationFinal private Assumption assumption = Truffle.getRuntime().createAssumption("SafepointManager");

    private volatile SafepointAction action;
    private volatile boolean deferred;

    public SafepointManager(RubyContext context) {
        this.context = context;
    }

    @TruffleBoundary
    public void enterThread() {
        lock.lock();
        try {
            int phase = phaser.register();
            assert phase >= 0 : "Phaser terminated";
            runningThreads.add(Thread.currentThread());
        } finally {
            lock.unlock();
        }
    }

    @TruffleBoundary
    public void leaveThread() {
        phaser.arriveAndDeregister();
        runningThreads.remove(Thread.currentThread());
    }

    public void poll(Node currentNode) {
        poll(currentNode, false, null);
    }

    public void pollFromBlockingCall(Node currentNode, SafepointResumeAction resumeAction) {
        poll(currentNode, true, resumeAction);
    }

    private void poll(Node currentNode, boolean fromBlockingCall, SafepointResumeAction resumeAction) {
        try {
            assumption.check();
        } catch (InvalidAssumptionException e) {
            assumptionInvalidated(currentNode, fromBlockingCall, resumeAction);
        }
    }

    @TruffleBoundary
    private void assumptionInvalidated(Node currentNode, boolean fromBlockingCall, SafepointResumeAction resumeAction) {
        final DynamicObject thread = context.getThreadManager().getCurrentThread();
        final InterruptMode interruptMode = Layouts.THREAD.getInterruptMode(thread);

        final boolean interruptible = (interruptMode == InterruptMode.IMMEDIATE) ||
                (fromBlockingCall && interruptMode == InterruptMode.ON_BLOCKING);

        if (!interruptible) {
            Thread.currentThread().interrupt(); // keep the interrupt flag
            return; // interrupt me later
        }

        final SafepointAction deferredAction = step(currentNode, false, resumeAction);

        // We're now running again normally and can run deferred actions
        if (deferredAction != null) {
            deferredAction.accept(thread, currentNode);
        }
    }

    @TruffleBoundary
    private SafepointAction step(Node currentNode, boolean isDrivingThread, SafepointResumeAction resumeAction) {
        final DynamicObject thread = context.getThreadManager().getCurrentThread();
        boolean needsResumeAction = false;

        // Wait for other threads to reach their safepoint
        if (isDrivingThread) {
            driveArrivalAtPhaser();
            assumption = Truffle.getRuntime().createAssumption("SafepointManager");
        } else {
            phaser.arriveAndAwaitAdvance();
        }

        // Wait for the assumption to be renewed
        phaser.arriveAndAwaitAdvance();

        // Read these while in the safepoint
        final SafepointAction deferredAction = deferred ? action : null;

        try {
            try {
                if (!deferred && thread != null) {
                    action.accept(thread, currentNode);
                }
            } finally {
                // Wait for other threads to finish their action
                phaser.arriveAndAwaitAdvance();
            }
            if (resumeAction != null) {
                needsResumeAction = true;
            }
        } finally {
            if (needsResumeAction) {
                resumeAction.run();
                // Resume actions are designed to help resolve lock contention issues when leaving
                // leaving a safepoint.

                // These threads must reclaim the lock before other threads can resume to avoid race
                // conditions, but also need to release it and start waiting on the condition
                // variable as they cannot all hold the lock at the same time. For this reason these
                // threads much mark they have completed their safepoint but must not wait for the
                // other threads before continuing.
                phaser.arrive();
            } else {
                phaser.arriveAndAwaitAdvance();
            }
        }
        return deferredAction;
    }

    private static final int WAIT_TIME_IN_SECONDS = 5;
    private static final int MAX_WAIT_TIME_IN_SECONDS = 60;
    private static final int STEP_BACKTRACE_OFFSET = 6;

    private void driveArrivalAtPhaser() {
        int phase = phaser.arrive();
        long t0 = System.nanoTime();
        long max = t0 + TimeUnit.SECONDS.toNanos(WAIT_TIME_IN_SECONDS);
        long exitTime = t0 + TimeUnit.SECONDS.toNanos(MAX_WAIT_TIME_IN_SECONDS);
        int waits = 1;
        while (true) {
            try {
                phaser.awaitAdvanceInterruptibly(phase, 100, TimeUnit.MILLISECONDS);
                break;
            } catch (InterruptedException e) {
                // retry
            } catch (TimeoutException e) {
                if (System.nanoTime() >= max) {
                    RubyLanguage.LOGGER.severe(String.format("waited %d seconds in the SafepointManager but %d of %d threads did not arrive - a thread is likely making a blocking native call which should use runBlockingSystemCallUntilResult() - check with jstack",
                            waits * WAIT_TIME_IN_SECONDS, phaser.getUnarrivedParties(), phaser.getRegisteredParties()));
                    printStacktracesOfBlockedThreads();

                    if (waits == 1) {
                        restoreDefaultInterruptHandler();
                    }
                    if (max >= exitTime) {
                        RubyLanguage.LOGGER.severe("waited " + MAX_WAIT_TIME_IN_SECONDS + " seconds in the SafepointManager, terminating the process as it is unlikely to get unstuck");
                        System.exit(1);
                    }
                    max += TimeUnit.SECONDS.toNanos(WAIT_TIME_IN_SECONDS);
                    waits++;
                } else {
                    // Retry interrupting other threads, as they might not have been yet
                    // in the blocking call when the signal was sent.
                    interruptOtherThreads();
                }
            }
        }
    }

    private void printStacktracesOfBlockedThreads() {
        System.err.println("Dumping stacktraces of blocked threads:");
        for (Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            final Thread thread = entry.getKey();
            if (thread != Thread.currentThread() && runningThreads.contains(thread)) {
                final StackTraceElement[] stackTrace = entry.getValue();
                if (STEP_BACKTRACE_OFFSET < stackTrace.length &&
                        stackTrace[STEP_BACKTRACE_OFFSET].getClassName().equals(SafepointManager.class.getName()) &&
                        stackTrace[STEP_BACKTRACE_OFFSET].getMethodName().equals("step")) {
                    // In SafepointManager#step, ignore
                } else {
                    System.err.println(thread);
                    for (int i = 0; i < stackTrace.length; i++) {
                        System.err.println(stackTrace[i]);
                    }
                    System.err.println();
                }
            }
        }
    }

    private void restoreDefaultInterruptHandler() {
        RubyLanguage.LOGGER.warning("restoring default interrupt handler");

        try {
            Signals.restoreDefaultHandler("INT");
        } catch (Throwable t) {
            RubyLanguage.LOGGER.warning("failed to restore default interrupt handler\n" + t);
        }
    }

    @TruffleBoundary
    public void pauseAllThreadsAndExecute(Node currentNode, boolean deferred, SafepointAction action) {
        if (lock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Re-entered SafepointManager");
        }

        // Need to lock interruptibly since we are in the registered threads.
        while (!lock.tryLock()) {
            poll(currentNode);
        }

        try {
            pauseAllThreadsAndExecute(currentNode, action, deferred);
        } finally {
            lock.unlock();
        }

        // Run deferred actions after leaving the SafepointManager lock.
        if (deferred) {
            action.accept(context.getThreadManager().getCurrentThread(), currentNode);
        }
    }

    @TruffleBoundary
    public void pauseAllThreadsAndExecuteFromNonRubyThread(boolean deferred, SafepointAction action) {
        if (lock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Re-entered SafepointManager");
        }

        assert !runningThreads.contains(Thread.currentThread());

        // Just wait to grab the lock, since we are not in the registered threads.

        lock.lock();

        try {
            enterThread();
            try {
                pauseAllThreadsAndExecute(null, action, deferred);
            } finally {
                leaveThread();
            }
        } finally {
            lock.unlock();
        }
    }

    // Variant for a single thread

    @TruffleBoundary
    public void pauseRubyThreadAndExecute(DynamicObject rubyThread, Node currentNode, SafepointAction action) {
        final DynamicObject currentThread = context.getThreadManager().getCurrentThread();
        final FiberManager fiberManager = Layouts.THREAD.getFiberManager(rubyThread);

        if (currentThread == rubyThread) {
            if (fiberManager.getRubyFiberFromCurrentJavaThread() != fiberManager.getCurrentFiber()) {
                throw new IllegalStateException("The currently executing Java thread does not correspond to the currently active fiber for the current Ruby thread");
            }
            // fast path if we are already the right thread
            action.accept(rubyThread, currentNode);
        } else {
            pauseAllThreadsAndExecute(currentNode, false, (thread, currentNode1) -> {
                if (thread == rubyThread &&
                        fiberManager.getRubyFiberFromCurrentJavaThread() == fiberManager.getCurrentFiber()) {
                    action.accept(thread, currentNode1);
                }
            });
        }
    }

    private void pauseAllThreadsAndExecute(Node currentNode, SafepointAction action, boolean deferred) {
        this.action = action;
        this.deferred = deferred;

        /* this is a potential cause for race conditions,
         * but we need to invalidate first so the interrupted threads
         * see the invalidation in poll() in their catch(InterruptedException) clause
         * and wait on the barrier instead of retrying their blocking action. */
        assumption.invalidate();
        interruptOtherThreads();

        step(currentNode, true, null);
    }

    private void interruptOtherThreads() {
        Thread current = Thread.currentThread();
        for (Thread thread : runningThreads) {
            if (thread != current) {
                context.getThreadManager().interrupt(thread);
            }
        }
    }

    public void checkNoRunningThreads() {
        if (!runningThreads.isEmpty()) {
            RubyLanguage.LOGGER.warning("threads are still registered with safepoint manager at shutdown:\n" + context.getThreadManager().getThreadDebugInfo() + getSafepointDebugInfo());
        }
    }

    public String getSafepointDebugInfo() {
        final Thread[] threads = new Thread[Thread.activeCount() + 1024];
        final int threadsCount = Thread.enumerate(threads);
        final long appearRunning = Arrays.stream(threads).limit(threadsCount).filter(
                t -> t.getName().startsWith(FiberManager.NAME_PREFIX)).count();
        return String.format("safepoints: %d known threads, %d registered with phaser, %d arrived, %d appear to be running",
                runningThreads.size(), phaser.getRegisteredParties(), phaser.getArrivedParties(), appearRunning);
    }

}
