/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import com.oracle.truffle.api.object.DynamicObjectLibrary;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.collections.ConcurrentOperations;
import org.truffleruby.core.basicobject.BasicObjectNodes.ObjectIDNode;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.fiber.FiberManager;
import org.truffleruby.core.fiber.RubyFiber;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.support.RandomizerNodes;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.Nil;
import org.truffleruby.language.SafepointManager;
import org.truffleruby.language.control.DynamicReturnException;
import org.truffleruby.language.control.ExitException;
import org.truffleruby.language.control.KillException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.platform.NativeConfiguration;
import org.truffleruby.platform.TruffleNFIPlatform;
import org.truffleruby.platform.TruffleNFIPlatform.NativeFunction;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.TruffleStackTrace;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.Shape;

public class ThreadManager {

    public static final String NAME_PREFIX = "Ruby Thread";

    private final RubyContext context;

    private final RubyThread rootThread;
    @CompilationFinal private Thread rootJavaThread;

    private final Map<Thread, RubyThread> foreignThreadMap = new ConcurrentHashMap<>();
    private final ThreadLocal<RubyThread> currentThread = ThreadLocal
            .withInitial(() -> foreignThreadMap.get(Thread.currentThread()));

    private final Set<RubyThread> runningRubyThreads = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final Set<Thread> rubyManagedThreads = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public final Map<Thread, RubyFiber> rubyFiberForeignMap = new ConcurrentHashMap<>();
    public final ThreadLocal<RubyFiber> rubyFiber = ThreadLocal
            .withInitial(() -> rubyFiberForeignMap.get(Thread.currentThread()));

    public static class UnblockingActionHolder {

        private final Thread owner;
        private volatile UnblockingAction action;

        UnblockingActionHolder(Thread owner, UnblockingAction action) {
            this.owner = owner;
            this.action = action;
        }

        public UnblockingAction get() {
            return action;
        }

        // No need for an atomic swap here, only the current thread is allowed to change its UnblockingAction
        UnblockingAction changeTo(UnblockingAction newAction) {
            assert Thread.currentThread() == owner;
            UnblockingAction oldAction = action;
            this.action = newAction;
            return oldAction;
        }

        void restore(UnblockingAction action) {
            assert Thread.currentThread() == owner;
            this.action = action;
        }
    }

    private final Map<Thread, UnblockingActionHolder> unblockingActions = new ConcurrentHashMap<>();
    public static final UnblockingAction EMPTY_UNBLOCKING_ACTION = () -> {
    };

    private final ThreadLocal<UnblockingAction> blockingNativeCallUnblockingAction = ThreadLocal
            .withInitial(() -> EMPTY_UNBLOCKING_ACTION);

    private int SIGVTALRM;
    private NativeFunction pthread_self;
    private NativeFunction pthread_kill;

    private final ExecutorService fiberPool;

    public ThreadManager(RubyContext context) {
        this.context = context;
        this.rootThread = createBootThread("main");
        this.fiberPool = Executors.newCachedThreadPool(this::createFiberJavaThread);
    }

    public void initialize(TruffleNFIPlatform nfi, NativeConfiguration nativeConfiguration) {
        if (context.getOptions().NATIVE_INTERRUPT && nfi != null) {
            setupSignalHandler(nfi, nativeConfiguration);
            setupNativeThreadSupport(nfi, nativeConfiguration);
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

        RandomizerNodes.resetSeed(context, rootThread.randomizer);
    }

    // spawning Thread => Fiber object
    public static final ThreadLocal<RubyFiber> FIBER_BEING_SPAWNED = new ThreadLocal<>();

    private Thread createFiberJavaThread(Runnable runnable) {
        RubyFiber fiber = FIBER_BEING_SPAWNED.get();
        assert fiber != null;
        return createJavaThread(runnable, fiber);
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

        assert fiber != null;
        thread.setUncaughtExceptionHandler((javaThread, throwable) -> {
            try {
                fiber.uncaughtException = throwable;
                fiber.initializedLatch.countDown();
            } catch (Throwable t) {
                t.initCause(throwable);
                t.printStackTrace();
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(javaThread, t);
            }
        });

        rubyManagedThreads.add(thread);
        return thread;
    }

    @SuppressFBWarnings("RV")
    public void spawnFiber(Runnable task) {
        fiberPool.submit(task);
    }

    @TruffleBoundary
    public boolean isRubyManagedThread(Thread thread) {
        return rubyManagedThreads.contains(thread);
    }

    @TruffleBoundary
    public RubyThread createBootThread(String info) {
        return createThread(
                context.getCoreLibrary().threadClass,
                RubyLanguage.threadShape,
                context.getLanguageSlow(),
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
                RubyLanguage.threadShape,
                context.getLanguageSlow(),
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

    private void setupSignalHandler(TruffleNFIPlatform nfi, NativeConfiguration config) {
        SIGVTALRM = (int) config.get("platform.signal.SIGVTALRM");

        final Object libC = nfi.getDefaultLibrary();

        // We use abs() as a function taking a int and having no side effects
        final Object abs = nfi.lookup(libC, "abs");
        final NativeFunction sigaction = nfi.getFunction("sigaction", "(sint32,pointer,pointer):sint32");

        final int sizeOfSigAction = (int) config.get("platform.sigaction.sizeof");
        final int handlerOffset = (int) config.get("platform.sigaction.sa_handler.offset");

        try (Pointer structSigAction = Pointer.calloc(sizeOfSigAction)) {
            structSigAction.writeLong(handlerOffset, nfi.asPointer(abs));

            // flags = 0 is OK as we want no SA_RESTART so we can interrupt blocking syscalls.
            final int result = (int) sigaction.call(SIGVTALRM, structSigAction.getAddress(), 0L);
            if (result != 0) {
                // TODO (eregon, 24 Nov. 2017): we should show the NFI errno here.
                throw new UnsupportedOperationException("sigaction() failed");
            }
        }
    }

    private void setupNativeThreadSupport(TruffleNFIPlatform nfi, NativeConfiguration nativeConfiguration) {
        final String pthread_t = nfi.resolveType(nativeConfiguration, "pthread_t");

        pthread_self = nfi.getFunction("pthread_self", "():" + pthread_t);
        pthread_kill = nfi.getFunction("pthread_kill", "(" + pthread_t + ",sint32):sint32");
    }

    public void initialize(RubyThread rubyThread, Node currentNode, String info, String sharingReason,
            Supplier<Object> task) {
        startSharing(rubyThread, sharingReason);

        rubyThread.sourceLocation = info;
        final RubyFiber rootFiber = rubyThread.fiberManager.getRootFiber();

        final Thread thread = createJavaThread(() -> threadMain(rubyThread, currentNode, task), rootFiber);
        thread.setName(NAME_PREFIX + " id=" + thread.getId() + " from " + info);

        thread.start();
        FiberManager.waitForInitialization(context, rootFiber, currentNode);
    }

    private void threadMain(RubyThread thread, Node currentNode, Supplier<Object> task) {
        assert task != null;

        start(thread, Thread.currentThread());
        try {
            final Object result = task.get();
            setThreadValue(context, thread, result);
            // Handlers in the same order as in FiberManager
        } catch (KillException e) {
            setThreadValue(context, thread, Nil.INSTANCE);
        } catch (RaiseException e) {
            setException(context, thread, e.getException(), currentNode);
        } catch (DynamicReturnException e) {
            setException(context, thread, context.getCoreExceptions().unexpectedReturn(currentNode), currentNode);
        } catch (ExitException e) {
            rethrowOnMainThread(currentNode, e);
            setThreadValue(context, thread, Nil.INSTANCE);
        } catch (Throwable e) {
            final String message = StringUtils
                    .format("%s terminated with internal error:", Thread.currentThread().getName());
            final RuntimeException runtimeException = new RuntimeException(message, e);
            // Immediately print internal exceptions, in case they would cause a deadlock
            runtimeException.printStackTrace();
            rethrowOnMainThread(currentNode, runtimeException);
            setThreadValue(context, thread, Nil.INSTANCE);
        } finally {
            assert thread.value != null || thread.exception != null;
            cleanup(thread, Thread.currentThread());
        }
    }

    private void rethrowOnMainThread(Node currentNode, RuntimeException e) {
        context.getSafepointManager().pauseRubyThreadAndExecute(
                "rethrow " + e.getClass() + " to main thread",
                getRootThread(),
                currentNode,
                (rubyThread, actionCurrentNode) -> {
                    throw e;
                });
    }

    private static void setThreadValue(RubyContext context, RubyThread thread, Object value) {
        // A Thread is always shared (Thread.list)
        assert value != null;
        SharedObjects.propagate(context, thread, value);
        thread.value = value;
    }

    private static void setException(RubyContext context, RubyThread thread, RubyException exception,
            Node currentNode) {
        // A Thread is always shared (Thread.list)
        SharedObjects.propagate(context, thread, exception);

        // We materialize the backtrace eagerly here, as the exception escapes the thread and needs
        // to capture the backtrace from this thread.
        final TruffleException truffleException = exception.backtrace.getRaiseException();
        if (truffleException != null) {
            TruffleStackTrace.fillIn((Throwable) truffleException);
        }

        final RubyThread mainThread = context.getThreadManager().getRootThread();

        if (thread != mainThread) {
            final boolean isSystemExit = exception.getLogicalClass() == context
                    .getCoreLibrary().systemExitClass;

            if (!isSystemExit && thread.reportOnException) {
                context.send(
                        context.getCoreLibrary().truffleThreadOperationsModule,
                        "report_exception",
                        thread,
                        exception);
            }

            if (isSystemExit || thread.abortOnException) {
                ThreadNodes.ThreadRaisePrimitiveNode.raiseInThread(context, mainThread, exception, currentNode);
            }
        }
        thread.exception = exception;
    }

    // Share the Ruby Thread before it can be accessed concurrently, and before it is added to Thread.list
    public void startSharing(RubyThread rubyThread, String reason) {
        if (context.getOptions().SHARED_OBJECTS_ENABLED) {
            // TODO (eregon, 22 Sept 2017): no need if singleThreaded in isThreadAccessAllowed()
            context.getSharedObjects().startSharing(reason);
            SharedObjects.writeBarrier(context, rubyThread);
        }
    }

    public void startForeignThread(RubyThread rubyThread, Thread javaThread) {
        startSharing(rubyThread, "creating a foreign thread");
        start(rubyThread, javaThread);
    }

    private void start(RubyThread thread, Thread javaThread) {
        thread.thread = javaThread;
        registerThread(thread);

        final FiberManager fiberManager = thread.fiberManager;
        fiberManager.start(fiberManager.getRootFiber(), javaThread);
    }

    public void cleanup(RubyThread thread, Thread javaThread) {
        // First mark as dead for Thread#status
        thread.status = ThreadStatus.DEAD;

        final FiberManager fiberManager = thread.fiberManager;
        fiberManager.shutdown(javaThread);

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

    public interface UnblockingAction {
        void unblock();
    }

    /** Only use when no context is available. */
    @TruffleBoundary
    public static <T> T retryWhileInterrupted(BlockingAction<T> action) {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    return action.block();
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
    public <T> T runUntilResultKeepStatus(Node currentNode, BlockingAction<T> action) {
        T result = null;

        do {
            try {
                result = action.block();
            } catch (InterruptedException e) {
                // We were interrupted, possibly by the SafepointManager.
                context.getSafepointManager().pollFromBlockingCall(currentNode);
            }
        } while (result == null);

        return result;
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
        final RubyThread runningThread = getCurrentThread();
        T result = null;

        do {
            final ThreadStatus status = runningThread.status;
            runningThread.status = ThreadStatus.SLEEP;

            try {
                try {
                    result = action.block();
                } finally {
                    runningThread.status = status;
                }
            } catch (InterruptedException e) {
                // We were interrupted, possibly by the SafepointManager.
                context.getSafepointManager().pollFromBlockingCall(currentNode);
            }
        } while (result == null);

        return result;
    }

    @TruffleBoundary
    UnblockingActionHolder getActionHolder(Thread thread) {
        return ConcurrentOperations.getOrCompute(unblockingActions, thread, t -> new UnblockingActionHolder(t, null));
    }

    @TruffleBoundary
    UnblockingAction getNativeCallUnblockingAction() {
        return blockingNativeCallUnblockingAction.get();
    }

    public void initializeValuesForJavaThread(RubyThread rubyThread, Thread thread) {

        if (Thread.currentThread() == thread) {
            currentThread.set(rubyThread);
        }
        if (!isRubyManagedThread(thread)) {
            foreignThreadMap.put(thread, rubyThread);
        }

        if (pthread_self != null && isRubyManagedThread(thread)) {
            final Object pThreadID = pthread_self.call();

            blockingNativeCallUnblockingAction.set(() -> pthread_kill.call(pThreadID, SIGVTALRM));
        }

        unblockingActions.put(thread, new UnblockingActionHolder(thread, () -> thread.interrupt()));
    }

    public void cleanupValuesForJavaThread(Thread thread) {
        if (Thread.currentThread() == thread) {
            currentThread.remove();
        }
        foreignThreadMap.remove(thread);

        unblockingActions.remove(thread);
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
    public RubyFiber getRubyFiberFromCurrentJavaThread() {
        return rubyFiber.get();
    }

    @TruffleBoundary
    public RubyThread getForeignRubyThread(Thread javaThread) {
        return foreignThreadMap.get(javaThread);
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

    private void checkCalledInMainThreadRootFiber() {
        final RubyThread currentThread = getCurrentThread();
        if (currentThread != rootThread) {
            throw new UnsupportedOperationException(StringUtils.format(
                    "ThreadManager.shutdown() must be called on the root Ruby Thread (%s) but was called on %s",
                    rootThread,
                    currentThread));
        }

        final FiberManager fiberManager = rootThread.fiberManager;

        if (getRubyFiberFromCurrentJavaThread() != fiberManager.getRootFiber()) {
            throw new UnsupportedOperationException(
                    "ThreadManager.shutdown() must be called on the root Fiber of the main Thread");
        }
    }

    @TruffleBoundary
    public void killAndWaitOtherThreads() {
        checkCalledInMainThreadRootFiber();

        // Disallow new Fibers to be created
        fiberPool.shutdown();

        // Kill all Ruby Threads and Fibers

        // The logic below avoids using the SafepointManager if there is
        // only the root thread and the reference processing thread.
        boolean otherThreads = false;
        RubyThread referenceProcessingThread = null;
        for (RubyThread thread : runningRubyThreads) {
            if (thread == rootThread) {
                // clean up later in #cleanupMainThread
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
        rootThread.fiberManager.killOtherFibers();

        // Wait and join all Java threads we created
        for (Thread thread : rubyManagedThreads) {
            if (thread != Thread.currentThread()) {
                runUntilResultKeepStatus(null, () -> {
                    thread.join();
                    return BlockingAction.SUCCESS;
                });
            }
        }
    }

    @TruffleBoundary
    public void cleanupMainThread() {
        checkCalledInMainThreadRootFiber();
        cleanup(rootThread, rootJavaThread);
    }

    /** Kill all Ruby threads, except the current Ruby Thread. Each Ruby Threads kills its fibers. */
    @TruffleBoundary
    private void doKillOtherThreads() {
        final Thread initiatingJavaThread = Thread.currentThread();

        while (true) {
            try {
                final String reason = "kill other threads for shutdown";
                context.getSafepointManager().pauseAllThreadsAndExecute(reason, null, false, (thread, currentNode) -> {
                    if (Thread.currentThread() != initiatingJavaThread) {
                        final FiberManager fiberManager = thread.fiberManager;
                        final RubyFiber fiber = getRubyFiberFromCurrentJavaThread();

                        if (fiberManager.getCurrentFiber() == fiber) {
                            thread.status = ThreadStatus.ABORTING;
                            throw new KillException();
                        }
                    }
                });
                break; // Successfully executed the safepoint and sent the exceptions.
            } catch (RaiseException e) {
                final RubyException rubyException = e.getException();
                context.getDefaultBacktraceFormatter().printRubyExceptionOnEnvStderr(
                        "Exception while killing other threads:\n",
                        rubyException);
            }
        }
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

    @TruffleBoundary
    public void interrupt(Thread thread) {
        final UnblockingAction action = getActionHolder(thread).get();
        if (action != null) {
            action.unblock();
        }
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
            if (thread == currentThread.get()) {
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
