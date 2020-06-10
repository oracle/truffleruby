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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.ConcurrentOperations;
import org.truffleruby.core.InterruptMode;
import org.truffleruby.core.fiber.FiberManager;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.support.RandomizerNodes;
import org.truffleruby.core.tracepoint.TracePointState;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.SafepointManager;
import org.truffleruby.language.control.DynamicReturnException;
import org.truffleruby.language.control.ExitException;
import org.truffleruby.language.control.KillException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.objects.ObjectIDOperations;
import org.truffleruby.language.objects.ReadObjectFieldNodeGen;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.language.threadlocal.ThreadLocalGlobals;
import org.truffleruby.platform.NativeConfiguration;
import org.truffleruby.platform.TruffleNFIPlatform;
import org.truffleruby.platform.TruffleNFIPlatform.NativeFunction;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleException;
import com.oracle.truffle.api.TruffleStackTrace;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;

public class ThreadManager {

    public static final String NAME_PREFIX = "Ruby Thread";

    private final RubyContext context;

    private final DynamicObject rootThread;
    @CompilationFinal private Thread rootJavaThread;

    private final Map<Thread, DynamicObject> foreignThreadMap = new ConcurrentHashMap<>();
    private final ThreadLocal<DynamicObject> currentThread = ThreadLocal
            .withInitial(() -> foreignThreadMap.get(Thread.currentThread()));

    private final Set<DynamicObject> runningRubyThreads = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final Set<Thread> rubyManagedThreads = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public final Map<Thread, DynamicObject> rubyFiberForeignMap = new ConcurrentHashMap<>();
    public final ThreadLocal<DynamicObject> rubyFiber = ThreadLocal
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

        Layouts.THREAD.setStatus(rootThread, ThreadStatus.RUN);
        Layouts.THREAD.setFinishedLatch(rootThread, new CountDownLatch(1));

        final DynamicObject rootFiber = Layouts.THREAD.getFiberManager(rootThread).getRootFiber();
        Layouts.FIBER.setAlive(rootFiber, true);
        Layouts.FIBER.setFinishedLatch(rootFiber, new CountDownLatch(1));

        RandomizerNodes.resetSeed(context, Layouts.THREAD.getRandomizer(rootThread));
    }

    // spawning Thread => Fiber object
    public static final ThreadLocal<DynamicObject> FIBER_BEING_SPAWNED = new ThreadLocal<>();

    private Thread createFiberJavaThread(Runnable runnable) {
        DynamicObject fiber = FIBER_BEING_SPAWNED.get();
        assert fiber != null;
        return createJavaThread(runnable, fiber);
    }

    private Thread createJavaThread(Runnable runnable, DynamicObject fiber) {
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
                Layouts.FIBER.setUncaughtException(fiber, throwable);
                Layouts.FIBER.getInitializedLatch(fiber).countDown();
            } catch (Throwable t) {
                t.initCause(throwable);
                t.printStackTrace();
                Thread.getDefaultUncaughtExceptionHandler().uncaughtException(javaThread, t);
            }
        });

        rubyManagedThreads.add(thread);
        return thread;
    }

    public void spawnFiber(Runnable task) {
        fiberPool.submit(task);
    }

    public boolean isRubyManagedThread(Thread thread) {
        return rubyManagedThreads.contains(thread);
    }

    public DynamicObject createBootThread(String info) {
        final DynamicObject thread = context.getCoreLibrary().threadFactory
                .newInstance(packThreadFields(Nil.INSTANCE, info));
        return initializeThreadFields(thread);
    }

    public DynamicObject createThread(DynamicObject rubyClass, AllocateObjectNode allocateObjectNode) {
        final Object currentGroup = Layouts.THREAD.getThreadGroup(getCurrentThread());
        assert currentGroup != null;
        final DynamicObject thread = allocateObjectNode.allocate(
                rubyClass,
                packThreadFields(currentGroup, "<uninitialized>"));
        return initializeThreadFields(thread);
    }

    public DynamicObject createForeignThread() {
        final Object currentGroup = Layouts.THREAD.getThreadGroup(rootThread);
        assert currentGroup != null;
        final DynamicObject thread = context.getCoreLibrary().threadFactory.newInstance(
                packThreadFields(currentGroup, "<foreign thread>"));
        return initializeThreadFields(thread);
    }

    private DynamicObject initializeThreadFields(DynamicObject thread) {
        setFiberManager(thread);
        return thread;
    }

    private void setFiberManager(DynamicObject thread) {
        // Because it is cyclic
        Layouts.THREAD.setFiberManagerUnsafe(thread, new FiberManager(context, thread));
    }

    private Object[] packThreadFields(Object currentGroup, String info) {
        return Layouts.THREAD.build(
                createThreadLocals(),
                InterruptMode.IMMEDIATE,
                ThreadStatus.RUN,
                new ArrayList<>(),
                null,
                new CountDownLatch(1),
                HashOperations.newEmptyHash(context),
                HashOperations.newEmptyHash(context),
                RandomizerNodes.newRandomizer(context),
                new TracePointState(),
                getGlobalReportOnException(),
                getGlobalAbortOnException(),
                null,
                null,
                null,
                new AtomicBoolean(false),
                Thread.NORM_PRIORITY,
                ThreadLocalBuffer.NULL_BUFFER,
                currentGroup,
                info,
                Nil.INSTANCE);
    }

    private boolean getGlobalReportOnException() {
        final DynamicObject threadClass = context.getCoreLibrary().threadClass;
        return (boolean) ReadObjectFieldNodeGen.getUncached().execute(threadClass, "@report_on_exception", null);
    }

    private boolean getGlobalAbortOnException() {
        final DynamicObject threadClass = context.getCoreLibrary().threadClass;
        return (boolean) ReadObjectFieldNodeGen.getUncached().execute(threadClass, "@abort_on_exception", null);
    }

    private ThreadLocalGlobals createThreadLocals() {
        return new ThreadLocalGlobals(Nil.INSTANCE, Nil.INSTANCE);
    }

    private void setupSignalHandler(TruffleNFIPlatform nfi, NativeConfiguration config) {
        SIGVTALRM = (int) config.get("platform.signal.SIGVTALRM");

        final TruffleObject libC = nfi.getDefaultLibrary();

        // We use abs() as a function taking a int and having no side effects
        final TruffleObject abs = nfi.lookup(libC, "abs");
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

    public void initialize(DynamicObject rubyThread, Node currentNode, String info, String sharingReason,
            Supplier<Object> task) {
        startSharing(rubyThread, sharingReason);

        Layouts.THREAD.setSourceLocation(rubyThread, info);
        final DynamicObject rootFiber = Layouts.THREAD.getFiberManager(rubyThread).getRootFiber();

        final Thread thread = createJavaThread(() -> threadMain(rubyThread, currentNode, task), rootFiber);
        thread.setName(NAME_PREFIX + " id=" + thread.getId() + " from " + info);

        thread.start();
        FiberManager.waitForInitialization(context, rootFiber, currentNode);
    }

    private void threadMain(DynamicObject thread, Node currentNode, Supplier<Object> task) {
        assert task != null;

        start(thread, Thread.currentThread());
        try {
            final Object result = task.get();
            setThreadValue(context, thread, result);
            // Handlers in the same order as in FiberManager
        } catch (KillException e) {
            setThreadValue(context, thread, Nil.INSTANCE);
        } catch (ExitException e) {
            rethrowOnMainThread(currentNode, e);
            setThreadValue(context, thread, Nil.INSTANCE);
        } catch (RaiseException e) {
            setException(context, thread, e.getException(), currentNode);
        } catch (DynamicReturnException e) {
            setException(context, thread, context.getCoreExceptions().unexpectedReturn(currentNode), currentNode);
        } finally {
            assert Layouts.THREAD.getValue(thread) != null || Layouts.THREAD.getException(thread) != null;
            cleanup(thread, Thread.currentThread());
        }
    }

    private void rethrowOnMainThread(Node currentNode, ExitException e) {
        context.getSafepointManager().pauseRubyThreadAndExecute(
                getRootThread(),
                currentNode,
                (rubyThread, actionCurrentNode) -> {
                    throw e;
                });
    }

    private static void setThreadValue(RubyContext context, DynamicObject thread, Object value) {
        // A Thread is always shared (Thread.list)
        assert value != null;
        SharedObjects.propagate(context, thread, value);
        Layouts.THREAD.setValue(thread, value);
    }

    private static void setException(RubyContext context, DynamicObject thread, DynamicObject exception,
            Node currentNode) {
        // A Thread is always shared (Thread.list)
        SharedObjects.propagate(context, thread, exception);

        // We materialize the backtrace eagerly here, as the exception escapes the thread and needs
        // to capture the backtrace from this thread.
        final TruffleException truffleException = Layouts.EXCEPTION.getBacktrace(exception).getRaiseException();
        if (truffleException != null) {
            TruffleStackTrace.fillIn((Throwable) truffleException);
        }

        final DynamicObject mainThread = context.getThreadManager().getRootThread();

        if (thread != mainThread) {
            final boolean isSystemExit = Layouts.BASIC_OBJECT.getLogicalClass(exception) == context
                    .getCoreLibrary().systemExitClass;

            if (!isSystemExit && Layouts.THREAD.getReportOnException(thread)) {
                context.send(
                        context.getCoreLibrary().truffleThreadOperationsModule,
                        "report_exception",
                        thread,
                        exception);
            }

            if (isSystemExit || Layouts.THREAD.getAbortOnException(thread)) {
                ThreadNodes.ThreadRaisePrimitiveNode.raiseInThread(context, mainThread, exception, currentNode);
            }
        }

        Layouts.THREAD.setException(thread, exception);
    }

    // Share the Ruby Thread before it can be accessed concurrently, and before it is added to Thread.list
    public void startSharing(DynamicObject rubyThread, String reason) {
        if (context.getOptions().SHARED_OBJECTS_ENABLED) {
            // TODO (eregon, 22 Sept 2017): no need if singleThreaded in isThreadAccessAllowed()
            context.getSharedObjects().startSharing(reason);
            SharedObjects.writeBarrier(context, rubyThread);
        }
    }

    public void startForeignThread(DynamicObject rubyThread, Thread javaThread) {
        startSharing(rubyThread, "creating a foreign thread");
        start(rubyThread, javaThread);
    }

    private void start(DynamicObject thread, Thread javaThread) {
        Layouts.THREAD.setThread(thread, javaThread);
        registerThread(thread);

        final FiberManager fiberManager = Layouts.THREAD.getFiberManager(thread);
        fiberManager.start(fiberManager.getRootFiber(), javaThread);
    }

    public void cleanup(DynamicObject thread, Thread javaThread) {
        // First mark as dead for Thread#status
        Layouts.THREAD.setStatus(thread, ThreadStatus.DEAD);

        final FiberManager fiberManager = Layouts.THREAD.getFiberManager(thread);
        fiberManager.shutdown(javaThread);

        Layouts.THREAD.getIoBuffer(thread).freeAll();
        Layouts.THREAD.setIoBuffer(thread, ThreadLocalBuffer.NULL_BUFFER);

        unregisterThread(thread);
        Layouts.THREAD.setThread(thread, null);

        if (Thread.currentThread() == javaThread) {
            for (Lock lock : Layouts.THREAD.getOwnedLocks(thread)) {
                lock.unlock();
            }
        } else {
            if (!Layouts.THREAD.getOwnedLocks(thread).isEmpty()) {
                RubyLanguage.LOGGER.warning(
                        "could not release locks of " + javaThread + " as its cleanup happened on another Java Thread");
            }
        }

        Layouts.THREAD.getFinishedLatch(thread).countDown();
    }

    public Thread getRootJavaThread() {
        return rootJavaThread;
    }

    public synchronized Thread getOrInitializeRootJavaThread() {
        // rootJavaThread can be null with a pre-initialized context.
        // In such a case, the first Thread in is considered the root Thread.
        if (rootJavaThread == null) {
            rootJavaThread = Thread.currentThread();
        }
        return rootJavaThread;
    }

    public DynamicObject getRootThread() {
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
        final DynamicObject runningThread = getCurrentThread();
        T result = null;

        do {
            final ThreadStatus status = Layouts.THREAD.getStatus(runningThread);
            Layouts.THREAD.setStatus(runningThread, ThreadStatus.SLEEP);

            try {
                try {
                    result = action.block();
                } finally {
                    Layouts.THREAD.setStatus(runningThread, status);
                }
            } catch (InterruptedException e) {
                // We were interrupted, possibly by the SafepointManager.
                context.getSafepointManager().pollFromBlockingCall(currentNode);
            }
        } while (result == null);

        return result;
    }

    /** Runs {@code action} until it returns a non-null value. The blocking action might be {@link Thread#interrupted()}
     * , for instance by the {@link SafepointManager}, in which case it will be run again. The unblocking action is
     * registered with the thread manager and will be invoked if the {@link SafepointManager} needs to interrupt the
     * thread. If the blocking action is making a native call, simply interrupting the thread will not unblock the
     * action. It is the responsibility of the unblocking action to break out of the native call so the thread can be
     * interrupted.
     *
     * @param blockingAction must not touch any Ruby state
     * @param unblockingAction must not touch any Ruby state
     * @return the first non-null return value from {@code action} */
    @TruffleBoundary
    public <T> T runUntilResult(Node currentNode, BlockingAction<T> blockingAction, UnblockingAction unblockingAction) {
        assert unblockingAction != null;
        final UnblockingActionHolder holder = getActionHolder(Thread.currentThread());

        final UnblockingAction oldUnblockingAction = holder.changeTo(unblockingAction);
        try {
            return runUntilResult(currentNode, blockingAction);
        } finally {
            holder.restore(oldUnblockingAction);
        }
    }

    @TruffleBoundary
    UnblockingActionHolder getActionHolder(Thread thread) {
        return ConcurrentOperations.getOrCompute(unblockingActions, thread, t -> new UnblockingActionHolder(t, null));
    }

    @TruffleBoundary
    UnblockingAction getNativeCallUnblockingAction() {
        return blockingNativeCallUnblockingAction.get();
    }

    public void initializeValuesForJavaThread(DynamicObject rubyThread, Thread thread) {
        assert RubyGuards.isRubyThread(rubyThread);

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
    public DynamicObject getCurrentThread() {
        final DynamicObject rubyThread = currentThread.get();
        if (rubyThread == null) {
            throw new UnsupportedOperationException(
                    "No Ruby Thread is associated with this Java Thread: " + Thread.currentThread());
        }
        return rubyThread;
    }

    @TruffleBoundary
    public DynamicObject getRubyFiberFromCurrentJavaThread() {
        return rubyFiber.get();
    }

    @TruffleBoundary
    public DynamicObject getForeignRubyThread(Thread javaThread) {
        return foreignThreadMap.get(javaThread);
    }

    public void registerThread(DynamicObject thread) {
        assert RubyGuards.isRubyThread(thread);
        if (!runningRubyThreads.add(thread)) {
            throw new UnsupportedOperationException(thread + " was already registered");
        }
    }

    public void unregisterThread(DynamicObject thread) {
        assert RubyGuards.isRubyThread(thread);
        if (!runningRubyThreads.remove(thread)) {
            throw new UnsupportedOperationException(thread + " was not registered");
        }
    }

    private void checkCalledInMainThreadRootFiber() {
        final DynamicObject currentThread = getCurrentThread();
        if (currentThread != rootThread) {
            throw new UnsupportedOperationException(StringUtils.format(
                    "ThreadManager.shutdown() must be called on the root Ruby Thread (%s) but was called on %s",
                    rootThread,
                    currentThread));
        }

        final FiberManager fiberManager = Layouts.THREAD.getFiberManager(rootThread);

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
        if (runningRubyThreads.size() > 1) {
            doKillOtherThreads();
        }
        Layouts.THREAD.getFiberManager(rootThread).killOtherFibers();

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
                context.getSafepointManager().pauseAllThreadsAndExecute(null, false, (thread, currentNode) -> {
                    if (Thread.currentThread() != initiatingJavaThread) {
                        final FiberManager fiberManager = Layouts.THREAD.getFiberManager(thread);
                        final DynamicObject fiber = getRubyFiberFromCurrentJavaThread();

                        if (fiberManager.getCurrentFiber() == fiber) {
                            Layouts.THREAD.setStatus(thread, ThreadStatus.ABORTING);
                            throw new KillException();
                        }
                    }
                });
                break; // Successfully executed the safepoint and sent the exceptions.
            } catch (RaiseException e) {
                final DynamicObject rubyException = e.getException();
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
    public Iterable<DynamicObject> iterateThreads() {
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

        for (DynamicObject thread : runningRubyThreads) {
            builder.append("thread @");
            builder.append(ObjectIDOperations.verySlowGetObjectID(context, thread));

            if (thread == rootThread) {
                builder.append(" (root)");
            }

            if (thread == getCurrentThread()) {
                builder.append(" (current)");
            }

            builder.append("\n");

            final FiberManager fiberManager = Layouts.THREAD.getFiberManager(thread);
            builder.append(fiberManager.getFiberDebugInfo());
        }

        if (builder.length() == 0) {
            return "no ruby threads\n";
        } else {
            return builder.toString();
        }
    }

}
