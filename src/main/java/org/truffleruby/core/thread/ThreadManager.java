/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.thread;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.ValueType;
import com.oracle.truffle.api.TruffleContext;
import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.TruffleSafepoint.CompiledInterruptible;
import com.oracle.truffle.api.TruffleSafepoint.Interrupter;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.collections.Memo;
import org.truffleruby.core.DummyNode;
import org.truffleruby.core.InterruptMode;
import org.truffleruby.core.basicobject.BasicObjectNodes.ObjectIDNode;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.exception.RubySystemExit;
import org.truffleruby.core.fiber.FiberManager;
import org.truffleruby.core.fiber.RubyFiber;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.support.PRNGRandomizerNodes;
import org.truffleruby.interop.InteropNodes;
import org.truffleruby.interop.TranslateInteropExceptionNode;
import org.truffleruby.language.Nil;
import org.truffleruby.language.SafepointAction;
import org.truffleruby.language.SafepointManager;
import org.truffleruby.language.SafepointPredicate;
import org.truffleruby.language.control.DynamicReturnException;
import org.truffleruby.language.control.ExitException;
import org.truffleruby.language.control.KillException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.shared.SharedObjects;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleStackTrace;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.signal.LibRubySignal;

public class ThreadManager {

    public static final String NAME_PREFIX = "Ruby Thread";

    private final RubyContext context;
    private final RubyLanguage language;

    private final RubyThread rootThread;
    @CompilationFinal private Thread rootJavaThread;

    private final Map<Thread, RubyThread> javaThreadToRubyThread = new ConcurrentHashMap<>();
    private final ThreadLocal<RubyThread> currentThread = ThreadLocal
            .withInitial(() -> javaThreadToRubyThread.get(Thread.currentThread()));

    private final Set<RubyThread> runningRubyThreads = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final Set<Thread> rubyManagedThreads = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public final Map<Thread, RubyFiber> rubyFiberForeignMap = new ConcurrentHashMap<>();
    public final ThreadLocal<RubyFiber> rubyFiber = ThreadLocal
            .withInitial(() -> rubyFiberForeignMap.get(Thread.currentThread()));

    private boolean nativeInterrupt;
    private Timer nativeInterruptTimer;
    private ThreadLocal<Interrupter> nativeCallInterrupter;

    private final ExecutorService fiberPool;

    public ThreadManager(RubyContext context, RubyLanguage language) {
        this.context = context;
        this.language = language;
        this.rootThread = createBootThread("main");
        this.fiberPool = Executors.newCachedThreadPool(this::createFiberJavaThread);
    }

    public void initialize() {
        nativeInterrupt = context.getOptions().NATIVE_INTERRUPT && context.getRubyHome() != null;
        if (nativeInterrupt) {
            LibRubySignal.loadLibrary(context.getRubyHome());
            LibRubySignal.setupSIGVTALRMEmptySignalHandler();

            nativeInterruptTimer = new Timer("Ruby-NativeCallInterrupt-Timer", true);
            nativeCallInterrupter = ThreadLocal.withInitial(
                    () -> new NativeCallInterrupter(nativeInterruptTimer, LibRubySignal.threadID()));
        }
    }

    public void dispose() {
        if (nativeInterrupt) {
            nativeInterruptTimer.cancel();
        }
    }

    public void initializeMainThread(Thread mainJavaThread) {
        rootJavaThread = mainJavaThread;
        rubyManagedThreads.add(rootJavaThread);
        start(rootThread, rootJavaThread);
    }

    public void resetMainThread() {
        cleanup(rootThread, rootJavaThread);
        rubyManagedThreads.remove(rootJavaThread);
        rootJavaThread = null;
    }

    public void restartMainThread(Thread mainJavaThread) {
        initializeMainThread(mainJavaThread);

        rootThread.status = ThreadStatus.RUN;
        rootThread.finishedLatch = new CountDownLatch(1);

        final RubyFiber rootFiber = rootThread.fiberManager.getRootFiber();
        rootFiber.alive = true;
        rootFiber.finishedLatch = new CountDownLatch(1);

        PRNGRandomizerNodes.resetSeed(context, rootThread.randomizer);
    }

    // spawning Thread => Fiber object
    public static final ThreadLocal<RubyFiber> FIBER_BEING_SPAWNED = new ThreadLocal<>();

    private Thread createFiberJavaThread(Runnable runnable) {
        final RubyFiber fiber = FIBER_BEING_SPAWNED.get();
        assert fiber != null;

        if (context.isPreInitializing()) {
            throw new UnsupportedOperationException("fibers should not be created while pre-initializing the context");
        }

        final Thread thread;
        if (context.getOptions().FIBER_LEAVE_CONTEXT) {
            thread = new Thread(runnable); // context.getEnv().createUnenteredThread(runnable);
        } else {
            thread = context.getEnv().createThread(runnable);
        }
        thread.setUncaughtExceptionHandler((javaThread, throwable) -> {
            System.err.println("Throwable escaped Fiber pool thread:");
            throwable.printStackTrace();
        });
        rubyManagedThreads.add(thread);
        return thread;
    }

    private Thread createJavaThread(Runnable runnable, RubyFiber fiber) {
        if (context.getOptions().SINGLE_THREADED) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().securityError("threads not allowed in single-threaded mode", null));
        }

        if (context.isPreInitializing()) {
            throw new UnsupportedOperationException("threads should not be created while pre-initializing the context");
        }

        final Thread thread = context.getEnv().createThread(runnable);
        thread.setUncaughtExceptionHandler(uncaughtExceptionHandler(fiber));
        rubyManagedThreads.add(thread);
        return thread;
    }

    private static Thread.UncaughtExceptionHandler uncaughtExceptionHandler(RubyFiber fiber) {
        assert fiber != null;
        return (javaThread, throwable) -> {
            printInternalError(throwable);
            try {
                fiber.uncaughtException = throwable;

                // If an uncaught exception happens, we already left the context, so it is safe to let other Fibers run

                // the Fiber is not yet initialized, unblock the caller and rethrow the exception to it
                fiber.initializedLatch.countDown();
                // the Fiber thread is dying, unblock the caller
                fiber.finishedLatch.countDown();
            } catch (Throwable t) { // exception inside this UncaughtExceptionHandler
                t.initCause(throwable);
                printInternalError(t);
            }
        };
    }

    @SuppressFBWarnings("RV")
    public void spawnFiber(RubyFiber fiber, Runnable task) {
        fiberPool.submit(() -> {
            try {
                task.run();
            } catch (Throwable t) {
                // Fibers are run on a thread-pool, so make sure any exception escaping
                // is handled here as the thread pool ignores exceptions.
                uncaughtExceptionHandler(fiber).uncaughtException(Thread.currentThread(), t);
            }
        });
    }

    /** Whether the thread was created by TruffleRuby. */
    @TruffleBoundary
    public boolean isRubyManagedThread(Thread thread) {
        return rubyManagedThreads.contains(thread);
    }

    @TruffleBoundary
    public RubyThread createBootThread(String info) {
        return createThread(
                context.getCoreLibrary().threadClass,
                language.threadShape,
                language,
                Nil.INSTANCE,
                info);
    }

    public RubyThread createThread(RubyClass rubyClass, Shape shape, RubyLanguage language) {
        final Object currentGroup = getCurrentThread().threadGroup;
        assert currentGroup != null;
        return createThread(rubyClass, shape, language, currentGroup, "<uninitialized>");
    }

    @TruffleBoundary
    public RubyThread createForeignThread() {
        final Object currentGroup = rootThread.threadGroup;
        assert currentGroup != null;
        return createThread(
                context.getCoreLibrary().threadClass,
                language.threadShape,
                language,
                currentGroup,
                "<foreign thread>");
    }

    private RubyThread createThread(RubyClass rubyClass, Shape shape, RubyLanguage language, Object currentGroup,
            String info) {
        return new RubyThread(
                rubyClass,
                shape,
                context,
                language,
                getGlobalReportOnException(),
                getGlobalAbortOnException(),
                currentGroup,
                info);
    }

    private boolean getGlobalReportOnException() {
        final RubyClass threadClass = context.getCoreLibrary().threadClass;
        return (boolean) DynamicObjectLibrary.getUncached().getOrDefault(threadClass, "@report_on_exception", null);
    }

    private boolean getGlobalAbortOnException() {
        final RubyClass threadClass = context.getCoreLibrary().threadClass;
        return (boolean) DynamicObjectLibrary.getUncached().getOrDefault(threadClass, "@abort_on_exception", null);
    }

    public void initialize(RubyThread rubyThread, Node currentNode, String info, String sharingReason,
            Supplier<Object> task) {
        startSharing(rubyThread, sharingReason);

        rubyThread.sourceLocation = info;
        final RubyFiber rootFiber = rubyThread.fiberManager.getRootFiber();

        final Thread thread = createJavaThread(() -> threadMain(rubyThread, currentNode, task), rootFiber);
        thread.setName(NAME_PREFIX + " id=" + thread.getId() + " from " + info);
        rubyThread.thread = thread;
        javaThreadToRubyThread.put(thread, rubyThread);

        thread.start();

        // Must not leave the context here, to perform safepoint actions, if e.g. the new thread Thread#raise this one
        FiberManager.waitForInitialization(context, rootFiber, currentNode);
    }

    /** {@link RubyLanguage#initializeThread(RubyContext, Thread)} runs before this, and
     * {@link RubyLanguage#disposeThread(RubyContext, Thread)} runs after this. */
    private void threadMain(RubyThread thread, Node currentNode, Supplier<Object> task) {
        try {
            final Object result = task.get();
            setThreadValue(thread, result);
            // Handlers in the same order as in FiberManager
            // Each catch must either setThreadValue() (before rethrowing) or setException()
        } catch (KillException e) {
            setThreadValue(thread, Nil.INSTANCE);
        } catch (ThreadDeath e) { // Context#close(true)
            setThreadValue(thread, Nil.INSTANCE);
            throw e;
        } catch (RaiseException e) {
            setException(thread, e.getException(), currentNode);
        } catch (DynamicReturnException e) {
            setException(thread, context.getCoreExceptions().unexpectedReturn(currentNode), currentNode);
        } catch (ExitException e) {
            setThreadValue(thread, Nil.INSTANCE);
            rethrowOnMainThread(currentNode, e);
        } catch (Throwable e) {
            final RuntimeException runtimeException = printInternalError(e);
            setThreadValue(thread, Nil.INSTANCE);
            rethrowOnMainThread(currentNode, runtimeException);
        } finally {
            assert thread.value != null || thread.exception != null;
            cleanupKillOtherFibers(thread);
        }
    }

    public static RuntimeException printInternalError(Throwable e) {
        final String message = StringUtils
                .format("%s terminated with internal error:", Thread.currentThread().getName());
        final RuntimeException runtimeException = new RuntimeException(message, e);
        // Immediately print internal exceptions, in case they would cause a deadlock
        runtimeException.printStackTrace();
        return runtimeException;
    }

    private void rethrowOnMainThread(Node currentNode, RuntimeException e) {
        context.getSafepointManager().pauseRubyThreadAndExecute(
                currentNode,
                new SafepointAction("rethrow " + e.getClass() + " to main thread", getRootThread(), true, false) {
                    @Override
                    public void run(RubyThread rubyThread, Node currentNode) {
                        throw e;
                    }
                });
    }

    private void setThreadValue(RubyThread thread, Object value) {
        // A Thread is always shared (Thread.list)
        assert value != null;
        SharedObjects.propagate(language, thread, value);
        thread.value = value;
    }

    private void setException(RubyThread thread, RubyException exception, Node currentNode) {
        // We materialize the backtrace eagerly here, as the exception escapes the thread and needs
        // to capture the backtrace from this thread.
        final RaiseException truffleException = exception.backtrace.getRaiseException();
        if (truffleException != null) {
            TruffleStackTrace.fillIn(truffleException);
        }

        // A Thread is always shared (Thread.list)
        SharedObjects.propagate(language, thread, exception);
        thread.exception = exception;

        final RubyThread mainThread = context.getThreadManager().getRootThread();
        if (thread != mainThread) {
            final boolean isSystemExit = exception instanceof RubySystemExit;

            if (!isSystemExit && thread.reportOnException) {
                RubyContext.send(
                        context.getCoreLibrary().truffleThreadOperationsModule,
                        "report_exception",
                        thread,
                        exception);
            }

            if (isSystemExit || thread.abortOnException) {
                ThreadNodes.ThreadRaisePrimitiveNode
                        .raiseInThread(language, context, mainThread, exception, currentNode);
            }
        }
    }

    // Share the Ruby Thread before it can be accessed concurrently, and before it is added to Thread.list
    public void startSharing(RubyThread rubyThread, String reason) {
        if (language.options.SHARED_OBJECTS_ENABLED) {
            // TODO (eregon, 22 Sept 2017): no need if singleThreaded in isThreadAccessAllowed()
            context.getSharedObjects().startSharing(language, reason);
            SharedObjects.writeBarrier(language, rubyThread);
        }
    }

    public void startForeignThread(RubyThread rubyThread, Thread javaThread) {
        startSharing(rubyThread, "creating a foreign thread");
        start(rubyThread, javaThread);
    }

    public void start(RubyThread thread, Thread javaThread) {
        thread.thread = javaThread;
        registerThread(thread);

        final FiberManager fiberManager = thread.fiberManager;
        final RubyFiber rootFiber = fiberManager.getRootFiber();
        fiberManager.start(rootFiber, javaThread);
        // fully initialized
        rootFiber.initializedLatch.countDown();
    }

    public void cleanup(RubyThread thread, Thread javaThread) {
        cleanupKillOtherFibers(thread);
        cleanupThreadState(thread, javaThread);
    }

    /** We cannot call this from {@link RubyLanguage#disposeThread} because that's called under a context lock. */
    private void cleanupKillOtherFibers(RubyThread thread) {
        thread.status = ThreadStatus.DEAD;
        thread.fiberManager.killOtherFibers();
    }

    public void cleanupThreadState(RubyThread thread, Thread javaThread) {
        final FiberManager fiberManager = thread.fiberManager;
        fiberManager.cleanup(fiberManager.getRootFiber(), javaThread);

        thread.ioBuffer.freeAll();
        thread.ioBuffer = ThreadLocalBuffer.NULL_BUFFER;

        unregisterThread(thread);
        thread.thread = null;

        if (Thread.currentThread() == javaThread) {
            for (Lock lock : thread.ownedLocks) {
                lock.unlock();
            }
        } else {
            if (!thread.ownedLocks.isEmpty()) {
                RubyLanguage.LOGGER.warning(
                        "could not release locks of " + javaThread + " as its cleanup happened on another Java Thread");
            }
        }

        thread.finishedLatch.countDown();
    }

    public Thread getRootJavaThread() {
        return rootJavaThread;
    }

    @TruffleBoundary
    public synchronized Thread getOrInitializeRootJavaThread() {
        // rootJavaThread can be null with a pre-initialized context.
        // In such a case, the first Thread in is considered the root Thread.
        if (rootJavaThread == null) {
            rootJavaThread = Thread.currentThread();
        }
        return rootJavaThread;
    }

    public RubyThread getRootThread() {
        return rootThread;
    }

    public interface BlockingAction<T> {
        boolean SUCCESS = true;

        T block() throws InterruptedException;
    }

    /** Only leaves the context if FIBER_LEAVE_CONTEXT is true */
    public <T> T leaveAndEnter(TruffleContext truffleContext, Node currentNode, Supplier<T> runWhileOutsideContext) {
        assert truffleContext.isEntered();

        if (context.getOptions().FIBER_LEAVE_CONTEXT) {
            return truffleContext.leaveAndEnter(currentNode, runWhileOutsideContext);
        } else {
            return runWhileOutsideContext.get();
        }
    }

    /** Only use when the context is not entered. */
    @TruffleBoundary
    public <T> void retryWhileInterrupted(Node currentNode, TruffleSafepoint.Interruptible<T> interruptible, T object) {
        if (!context.getOptions().FIBER_LEAVE_CONTEXT) {
            runUntilResultKeepStatus(currentNode, interruptible, object);
            return;
        }

        assert !context.getEnv().getContext().isEntered() : "Use runUntilResult*() when entered";
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    interruptible.apply(object);
                    break;
                } catch (InterruptedException e) {
                    interrupted = true;
                    // retry
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @TruffleBoundary
    public <T> void runUntilResultKeepStatus(Node currentNode, TruffleSafepoint.Interruptible<T> action,
            T object) {
        assert context.getEnv().getContext().isEntered() : "Use retryWhileInterrupted() when not entered";
        TruffleSafepoint.setBlockedThreadInterruptible(currentNode, action, object);
    }

    public static Object executeBlockingCall(RubyThread thread, Interrupter interrupter, Object executable,
            Object[] args, BlockingCallInterruptible blockingCallInterruptible, Node currentNode) {
        final TruffleSafepoint safepoint = TruffleSafepoint.getCurrent();

        final BlockingCallInterruptible.State state = new BlockingCallInterruptible.State(thread, executable, args);
        safepoint.setBlocked(currentNode, interrupter, blockingCallInterruptible, state, null, null);
        return state.result;
    }

    public static class BlockingCallInterruptible implements CompiledInterruptible<BlockingCallInterruptible.State> {

        final InteropLibrary receivers;
        final TranslateInteropExceptionNode translateInteropExceptionNode;

        public BlockingCallInterruptible(
                InteropLibrary receivers,
                TranslateInteropExceptionNode translateInteropExceptionNode) {
            this.receivers = receivers;
            this.translateInteropExceptionNode = translateInteropExceptionNode;
        }

        @ValueType
        private static class State {
            final RubyThread thread;
            final Object executable;
            final Object[] args;
            Object result;

            private State(RubyThread thread, Object executable, Object[] args) {
                this.thread = thread;
                this.executable = executable;
                this.args = args;
            }
        }

        @Override
        public void apply(State state) {
            CompilerAsserts.partialEvaluationConstant(this);
            final RubyThread thread = state.thread;

            final ThreadStatus status = thread.status;
            thread.status = ThreadStatus.SLEEP;
            try {
                // NOTE: NFI uses CallTargets, so the TruffleSafepoint.poll() will happen before coming back from this call
                CompilerAsserts.partialEvaluationConstant(receivers);
                CompilerAsserts.partialEvaluationConstant(translateInteropExceptionNode);
                state.result = InteropNodes
                        .execute(state.executable, state.args, receivers, translateInteropExceptionNode);
            } finally {
                thread.status = status;
            }
        }
    }

    /** Runs {@code action} until it returns a non-null value. The given action should throw an
     * {@link InterruptedException} when {@link Thread#interrupt()} is called. Otherwise, the {@link SafepointManager}
     * will not be able to interrupt this action. See {@link ThreadNodes.ThreadRunBlockingSystemCallNode} for blocking
     * native calls. If the action throws an {@link InterruptedException}, it will be retried until it returns a
     * non-null value.
     *
     * @param action must not touch any Ruby state
     * @return the first non-null return value from {@code action} */
    @TruffleBoundary
    public <T> T runUntilResult(Node currentNode, BlockingAction<T> action) {
        return runUntilResult(currentNode, action, null, null);
    }

    @TruffleBoundary
    public <T> T runUntilResult(Node currentNode, BlockingAction<T> action, Runnable beforeInterrupt,
            Runnable afterInterrupt) {
        final TruffleSafepoint safepoint = TruffleSafepoint.getCurrent();
        final RubyThread runningThread = getCurrentThread();

        // For Thread.handle_interrupt(Exception => :on_blocking),
        // we want to allow side-effecting actions to interrupt this blocking action and run here.
        final boolean onBlocking = runningThread.interruptMode == InterruptMode.ON_BLOCKING;

        final Memo<T> result = new Memo<>(null);
        final ThreadStatus status = runningThread.status;
        boolean sideEffects = false;

        if (onBlocking) {
            sideEffects = safepoint.setAllowSideEffects(true);
        }
        try {
            safepoint.setBlocked(currentNode, Interrupter.THREAD_INTERRUPT, arg -> {
                runningThread.status = ThreadStatus.SLEEP;
                try {
                    result.set(action.block());
                } finally {
                    runningThread.status = status; // restore status for running the safepoint
                }
            }, null, beforeInterrupt, afterInterrupt);
        } finally {
            if (onBlocking) {
                safepoint.setAllowSideEffects(sideEffects);
            }
        }

        return result.get();
    }

    @TruffleBoundary
    Interrupter getNativeCallInterrupter() {
        if (nativeInterrupt) {
            return nativeCallInterrupter.get();
        } else {
            return Interrupter.THREAD_INTERRUPT;
        }
    }

    public void initializeValuesForJavaThread(RubyThread rubyThread, Thread thread) {
        if (Thread.currentThread() == thread) {
            currentThread.set(rubyThread);
        }
        javaThreadToRubyThread.put(thread, rubyThread);
    }

    public void cleanupValuesForJavaThread(Thread thread) {
        if (Thread.currentThread() == thread) {
            currentThread.remove();
            context.getMarkingService().cleanupThreadLocalData();
            context.getValueWrapperManager().cleanupBlockHolder();
        }
        javaThreadToRubyThread.remove(thread);
    }

    @TruffleBoundary
    public RubyThread getCurrentThread() {
        final RubyThread rubyThread = currentThread.get();
        if (rubyThread == null) {
            throw new UnsupportedOperationException(
                    "No Ruby Thread is associated with this Java Thread: " + Thread.currentThread());
        }
        return rubyThread;
    }

    @TruffleBoundary
    public RubyThread getCurrentThreadOrNull() {
        return currentThread.get();
    }

    @TruffleBoundary
    public RubyFiber getRubyFiberFromCurrentJavaThread() {
        return rubyFiber.get();
    }

    @TruffleBoundary
    public RubyThread getRubyThread(Thread javaThread) {
        return javaThreadToRubyThread.get(javaThread);
    }

    public void registerThread(RubyThread thread) {
        if (!runningRubyThreads.add(thread)) {
            throw new UnsupportedOperationException(thread + " was already registered");
        }
    }

    public void unregisterThread(RubyThread thread) {
        if (!runningRubyThreads.remove(thread)) {
            throw new UnsupportedOperationException(thread + " was not registered");
        }
    }


    public void checkNoRunningThreads() {
        if (!runningRubyThreads.isEmpty()) {
            RubyLanguage.LOGGER
                    .warning("threads are still registered with thread manager at shutdown:\n" + getThreadDebugInfo());
        }
    }

    @TruffleBoundary
    public void killAndWaitOtherThreads() {
        // Disallow new Fibers to be created
        fiberPool.shutdown();

        // Kill all Ruby Threads and Fibers

        // The logic below avoids using the SafepointManager if there is
        // only the current thread and the reference processing thread.
        final RubyThread currentThread = getCurrentThread();
        boolean otherThreads = false;
        RubyThread referenceProcessingThread = null;
        for (RubyThread thread : runningRubyThreads) {
            if (thread == currentThread) {
                // no need to kill the current thread
            } else if (thread == context.getReferenceProcessor().getProcessingThread()) {
                referenceProcessingThread = thread;
            } else {
                otherThreads = true;
                break;
            }
        }

        if (!otherThreads && referenceProcessingThread != null) {
            if (!context.getReferenceProcessor().shutdownProcessingThread()) {
                otherThreads = true;
            }
        }

        if (otherThreads) {
            doKillOtherThreads();
        }
        currentThread.fiberManager.killOtherFibers();

        // Wait and join all Java threads we created
        for (Thread thread : rubyManagedThreads) {
            if (thread != Thread.currentThread()) {
                runUntilResultKeepStatus(DummyNode.INSTANCE, Thread::join, thread);
            }
        }
    }

    /** Kill all Ruby threads, except the current Ruby Thread. Each Ruby Threads kills its fibers. */
    @TruffleBoundary
    private void doKillOtherThreads() {
        final Thread initiatingJavaThread = Thread.currentThread();
        SafepointPredicate predicate = (context, thread, action) -> Thread.currentThread() != initiatingJavaThread &&
                getRubyFiberFromCurrentJavaThread() == thread.fiberManager.getCurrentFiber();

        context.getSafepointManager().pauseAllThreadsAndExecute(
                DummyNode.INSTANCE,
                new SafepointAction("kill other threads for shutdown", predicate, true, false) {
                    @Override
                    public void run(RubyThread rubyThread, Node currentNode) {
                        rubyThread.status = ThreadStatus.ABORTING;
                        throw new KillException();
                    }
                });
    }

    @TruffleBoundary
    public Object[] getThreadList() {
        // This must not pre-allocate an array, or it could contain null's.
        return runningRubyThreads.toArray();
    }

    @TruffleBoundary
    public Iterable<RubyThread> iterateThreads() {
        return runningRubyThreads;
    }

    public String getThreadDebugInfo() {
        final StringBuilder builder = new StringBuilder();

        for (RubyThread thread : runningRubyThreads) {
            builder.append("thread @");
            builder.append(ObjectIDNode.getUncached().execute(thread));

            if (thread == rootThread) {
                builder.append(" (root)");
            }

            // cannot use getCurrentThread() as it might have been cleared
            if (thread == getCurrentThreadOrNull()) {
                builder.append(" (current)");
            }

            builder.append("\n");

            final FiberManager fiberManager = thread.fiberManager;
            builder.append(fiberManager.getFiberDebugInfo());
        }

        if (builder.length() == 0) {
            return "no ruby threads\n";
        } else {
            return builder.toString();
        }
    }

}
