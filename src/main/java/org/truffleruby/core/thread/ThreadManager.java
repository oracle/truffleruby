/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.thread;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;

import org.truffleruby.Layouts;
import org.truffleruby.Log;
import org.truffleruby.RubyContext;
import org.truffleruby.core.InterruptMode;
import org.truffleruby.core.fiber.FiberManager;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.SafepointManager;
import org.truffleruby.language.backtrace.BacktraceFormatter;
import org.truffleruby.language.control.ExitException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.ReturnException;
import org.truffleruby.language.control.KillException;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.objects.ObjectIDOperations;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.platform.NativeConfiguration;
import org.truffleruby.platform.TruffleNFIPlatform;
import org.truffleruby.platform.TruffleNFIPlatform.NativeFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

public class ThreadManager {

    public static final String NAME_PREFIX = "Ruby Thread";

    private final RubyContext context;

    private final DynamicObject rootThread;
    @CompilationFinal private Thread rootJavaThread;

    private final Map<Thread, DynamicObject> foreignThreadMap = new ConcurrentHashMap<>();
    private final ThreadLocal<DynamicObject> currentThread = ThreadLocal.withInitial(() -> foreignThreadMap.get(Thread.currentThread()));

    private final Set<DynamicObject> runningRubyThreads =
            Collections.newSetFromMap(new ConcurrentHashMap<DynamicObject, Boolean>());

    private final Set<Thread> rubyManagedThreads =
            Collections.newSetFromMap(new ConcurrentHashMap<Thread, Boolean>());

    private final Map<Thread, UnblockingAction> unblockingActions = new ConcurrentHashMap<>();
    private static final UnblockingAction EMPTY_UNBLOCKING_ACTION = () -> {
    };

    private final ThreadLocal<UnblockingAction> blockingNativeCallUnblockingAction = ThreadLocal.withInitial(() -> EMPTY_UNBLOCKING_ACTION);

    private int SIGVTALRM;
    private NativeFunction pthread_self;
    private NativeFunction pthread_kill;

    private final ExecutorService fiberPool;

    public ThreadManager(RubyContext context) {
        this.context = context;
        this.rootThread = createBootThread("main");
        this.rootJavaThread = Thread.currentThread();
        this.fiberPool = Executors.newCachedThreadPool(this::createJavaThread);
    }

    public void initialize(TruffleNFIPlatform nfi, NativeConfiguration nativeConfiguration) {
        if (context.getOptions().NATIVE_INTERRUPT && nfi != null) {
            setupSignalHandler(nfi, nativeConfiguration);
            setupNativeThreadSupport(nfi, nativeConfiguration);
        }

        rubyManagedThreads.add(rootJavaThread);
        start(rootThread, rootJavaThread);
    }

    public void resetMainThread() {
        cleanup(rootThread, rootJavaThread);
        rubyManagedThreads.remove(rootJavaThread);

        rootJavaThread = null;
    }

    public void restartMainThread(Thread mainJavaThread) {
        rootJavaThread = mainJavaThread;

        start(rootThread, mainJavaThread);
        Layouts.THREAD.setStatus(rootThread, ThreadStatus.RUN);
        Layouts.THREAD.setFinishedLatch(rootThread, new CountDownLatch(1));

        final DynamicObject rootFiber = Layouts.THREAD.getFiberManager(rootThread).getRootFiber();
        Layouts.FIBER.setAlive(rootFiber, true);
        Layouts.FIBER.setFinishedLatch(rootFiber, new CountDownLatch(1));
    }

    public Thread createJavaThread(Runnable runnable) {
        if (context.getOptions().SINGLE_THREADED) {
            throw new RaiseException(context.getCoreExceptions().securityError("threads not allowed in single-threaded mode", null));
        }

        if (context.isPreInitializing()) {
            throw new UnsupportedOperationException("threads should not be created while pre-initializing the context");
        }

        final Thread thread = context.getEnv().createThread(runnable);
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
        final DynamicObject thread = context.getCoreLibrary().getThreadFactory().newInstance(packThreadFields(nil(), info));
        setFiberManager(thread);
        return thread;
    }

    public DynamicObject createThread(DynamicObject rubyClass, AllocateObjectNode allocateObjectNode) {
        final DynamicObject currentGroup = Layouts.THREAD.getThreadGroup(getCurrentThread());
        final DynamicObject thread = allocateObjectNode.allocate(rubyClass,
                packThreadFields(currentGroup, "<uninitialized>"));
        setFiberManager(thread);
        return thread;
    }

    public DynamicObject createForeignThread() {
        final DynamicObject currentGroup = Layouts.THREAD.getThreadGroup(rootThread);
        final DynamicObject thread = context.getCoreLibrary().getThreadFactory().newInstance(
                packThreadFields(currentGroup, "<foreign thread>"));
        setFiberManager(thread);
        return thread;
    }

    private void setFiberManager(DynamicObject thread) {
        // Because it is cyclic
        Layouts.THREAD.setFiberManagerUnsafe(thread, new FiberManager(context, thread));
    }

    private Object[] packThreadFields(DynamicObject currentGroup, String info) {
        return Layouts.THREAD.build(
                createThreadLocals(),
                InterruptMode.IMMEDIATE,
                ThreadStatus.RUN,
                new ArrayList<>(),
                null,
                new CountDownLatch(1),
                getGlobalAbortOnException(),
                null,
                null,
                null,
                new AtomicBoolean(false),
                Thread.NORM_PRIORITY,
                currentGroup,
                info,
                nil());
    }

    private boolean getGlobalAbortOnException() {
        final DynamicObject threadClass = context.getCoreLibrary().getThreadClass();
        return (boolean) ReadObjectFieldNode.read(threadClass, "@abort_on_exception", null);
    }

    private DynamicObject createThreadLocals() {
        final DynamicObject threadLocals = Layouts.BASIC_OBJECT.createBasicObject(context.getCoreLibrary().getObjectFactory());
        threadLocals.define("$!", nil());
        threadLocals.define("$?", nil());
        threadLocals.define("$SAFE", 0);
        return threadLocals;
    }

    private void setupSignalHandler(TruffleNFIPlatform nfi, NativeConfiguration config) {
        SIGVTALRM = (int) config.get("platform.signal.SIGVTALRM");

        final TruffleObject libC = nfi.getDefaultLibrary();

        // We use abs() as a function taking a int and having no side effects
        final TruffleObject abs = nfi.lookup(libC, "abs");
        final NativeFunction sigaction = nfi.getFunction("sigaction", 3, "(sint32,pointer,pointer):sint32");

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

        pthread_self = nfi.getFunction("pthread_self", 0, "():" + pthread_t);
        pthread_kill = nfi.getFunction("pthread_kill", 2, "(" + pthread_t + ",sint32):sint32");
    }

    public void initialize(DynamicObject rubyThread, Node currentNode, String info, Supplier<Object> task) {
        Layouts.THREAD.setSourceLocation(rubyThread, info);

        final Thread thread = createJavaThread(() -> threadMain(rubyThread, currentNode, task));

        thread.setName(NAME_PREFIX + " id=" + thread.getId() + " from " + info);

        final CompletableFuture<Throwable> thrown = new CompletableFuture<>();

        thread.setUncaughtExceptionHandler((t, e) -> {
            thrown.complete(e);
        });

        thread.start();

        if (!FiberManager.waitForInitialization(context, Layouts.THREAD.getFiberManager(rubyThread).getRootFiber(), currentNode)) {
            final String message;

            try {
                if (thrown.isDone()
                        && thrown.get() instanceof IllegalStateException
                        && thrown.get().getMessage().startsWith("Multi threaded access requested")) {
                    message = thrown.get().getMessage() + " Are you attempting to create a Ruby thread after you have used a single-threaded language?";
                } else {
                    message = "creating thread timed out";
                }
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }

            throw new RaiseException(context.getCoreExceptions().securityError(message, currentNode));
        }
    }

    private void threadMain(DynamicObject thread, Node currentNode, Supplier<Object> task) {
        assert task != null;

        start(thread, Thread.currentThread());
        try {
            final Object result = task.get();
            setThreadValue(context, thread, result);
        // Handlers in the same order as in FiberManager
        } catch (KillException e) {
            setThreadValue(context, thread, nil());
        } catch (ExitException e) {
            rethrowOnMainThread(currentNode, e);
            setThreadValue(context, thread, nil());
        } catch (RaiseException e) {
            setException(context, thread, e.getException(), currentNode);
        } catch (ReturnException e) {
            setException(context, thread, context.getCoreExceptions().unexpectedReturn(currentNode), currentNode);
        } finally {
            assert Layouts.THREAD.getValue(thread) != null || Layouts.THREAD.getException(thread) != null;
            cleanup(thread, Thread.currentThread());
        }
    }

    private void rethrowOnMainThread(Node currentNode, ExitException e) {
        context.getSafepointManager().pauseRubyThreadAndExecute(getRootThread(), currentNode, (rubyThread, actionCurrentNode) -> {
            throw e;
        });
    }

    private static void setThreadValue(RubyContext context, DynamicObject thread, Object value) {
        // A Thread is always shared (Thread.list)
        assert value != null;
        SharedObjects.propagate(context, thread, value);
        Layouts.THREAD.setValue(thread, value);
    }

    private static void setException(RubyContext context, DynamicObject thread, DynamicObject exception, Node currentNode) {
        // A Thread is always shared (Thread.list)
        SharedObjects.propagate(context, thread, exception);
        final DynamicObject mainThread = context.getThreadManager().getRootThread();
        final boolean isSystemExit = Layouts.BASIC_OBJECT.getLogicalClass(exception) == context.getCoreLibrary().getSystemExitClass();
        if (thread != mainThread && (isSystemExit || Layouts.THREAD.getAbortOnException(thread))) {
            ThreadNodes.ThreadRaisePrimitiveNode.raiseInThread(context, mainThread, exception, currentNode);
        }
        Layouts.THREAD.setException(thread, exception);
    }

    public void start(DynamicObject thread, Thread javaThread) {
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

        unregisterThread(thread);
        Layouts.THREAD.setThread(thread, null);

        if (Thread.currentThread() == javaThread) {
            for (Lock lock : Layouts.THREAD.getOwnedLocks(thread)) {
                lock.unlock();
            }
        } else {
            if (!Layouts.THREAD.getOwnedLocks(thread).isEmpty()) {
                Log.LOGGER.warning("could not release locks of " + javaThread + " as its cleanup happened on another Java Thread");
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

    /**
     * Runs {@code action} until it returns a non-null value. The given action should throw an
     * {@link InterruptedException} when {@link Thread#interrupt()} is called. Otherwise, the
     * {@link SafepointManager} will not be able to interrupt this action. See
     * {@link ThreadManager#runBlockingNFISystemCallUntilResult(Node, BlockingAction)} for blocking
     * native calls. If the action throws an {@link InterruptedException}, it will be retried until
     * it returns a non-null value.
     *
     * @param action must not touch any Ruby state
     * @return the first non-null return value from {@code action}
     */
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

    /**
     * Runs {@code action} until it returns a non-null value. The blocking action might be
     * {@link Thread#interrupted()}, for instance by the {@link SafepointManager}, in which case it
     * will be run again. The unblocking action is registered with the thread manager and will be
     * invoked if the {@link SafepointManager} needs to interrupt the thread. If the blocking action
     * is making a native call, simply interrupting the thread will not unblock the action. It is
     * the responsibility of the unblocking action to break out of the native call so the thread can
     * be interrupted.
     *
     * @param blockingAction must not touch any Ruby state
     * @param unblockingAction must not touch any Ruby state
     * @return the first non-null return value from {@code action}
     */
    @TruffleBoundary
    public <T> T runUntilResult(Node currentNode, BlockingAction<T> blockingAction, UnblockingAction unblockingAction) {
        assert unblockingAction != null;
        final Thread thread = Thread.currentThread();

        final UnblockingAction oldUnblockingAction = unblockingActions.put(thread, unblockingAction);
        try {
            return runUntilResult(currentNode, blockingAction);
        } finally {
            unblockingActions.put(thread, oldUnblockingAction);
        }
    }

    /**
     * Similar to {@link ThreadManager#runUntilResult(Node, BlockingAction)} but purposed for
     * blocking native calls. If the {@link SafepointManager} needs to interrupt the thread, it will
     * send a SIGVTALRM to abort the blocking syscall and the action will return NotProvided if the
     * syscall fails with errno=EINTR, meaning it was interrupted.
     */
    @TruffleBoundary
    public Object runBlockingNFISystemCallUntilResult(Node currentNode, BlockingAction<Object> action) {
        return runUntilResult(currentNode, () -> {
            final Object result = action.block();
            if (result == NotProvided.INSTANCE) {
                throw new InterruptedException("EINTR");
            }
            return result;
        }, blockingNativeCallUnblockingAction.get());
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

        unblockingActions.put(thread, EMPTY_UNBLOCKING_ACTION);
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
            throw new UnsupportedOperationException("No Ruby Thread is associated with this Java Thread: " + Thread.currentThread());
        }
        return rubyThread;
    }

    @TruffleBoundary
    public DynamicObject getForeignRubyThread(Thread javaThread) {
        return foreignThreadMap.get(javaThread);
    }

    public void registerThread(DynamicObject thread) {
        assert RubyGuards.isRubyThread(thread);
        runningRubyThreads.add(thread);

        if (context.getOptions().SHARED_OBJECTS_ENABLED && runningRubyThreads.size() > 1) {
            // TODO (eregon, 22 Sept 2017): no need if singleThreaded in isThreadAccessAllowed()
            context.getSharedObjects().startSharing();
            SharedObjects.writeBarrier(context, thread);
        }
    }

    public void unregisterThread(DynamicObject thread) {
        assert RubyGuards.isRubyThread(thread);
        runningRubyThreads.remove(thread);
    }

    private void checkCalledInMainThreadRootFiber() {
        final DynamicObject currentThread = getCurrentThread();
        if (currentThread != rootThread) {
            throw new UnsupportedOperationException(StringUtils.format(
                    "ThreadManager.shutdown() must be called on the root Ruby Thread (%s) but was called on %s",
                    rootThread, currentThread));
        }

        final FiberManager fiberManager = Layouts.THREAD.getFiberManager(rootThread);

        if (fiberManager.getRubyFiberFromCurrentJavaThread() != fiberManager.getRootFiber()) {
            throw new UnsupportedOperationException("ThreadManager.shutdown() must be called on the root Fiber of the main Thread");
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

    /**
     * Kill all Ruby threads, except the current Ruby Thread. Each Ruby Threads kills its fibers.
     */
    @TruffleBoundary
    private void doKillOtherThreads() {
        final Thread initiatingJavaThread = Thread.currentThread();

        while (true) {
            try {
                context.getSafepointManager().pauseAllThreadsAndExecute(null, false, (thread, currentNode) -> {
                    if (Thread.currentThread() != initiatingJavaThread) {
                        final FiberManager fiberManager = Layouts.THREAD.getFiberManager(thread);
                        final DynamicObject fiber = fiberManager.getRubyFiberFromCurrentJavaThread();

                        if (fiberManager.getCurrentFiber() == fiber) {
                            Layouts.THREAD.setStatus(thread, ThreadStatus.ABORTING);
                            throw new KillException();
                        }
                    }
                });
                break; // Successfully executed the safepoint and sent the exceptions.
            } catch (RaiseException e) {
                final DynamicObject rubyException = e.getException();
                BacktraceFormatter.createDefaultFormatter(context).printBacktrace(context, rubyException, Layouts.EXCEPTION.getBacktrace(rubyException));
            }
        }
    }

    @TruffleBoundary
    public Object[] getThreadList() {
        return runningRubyThreads.toArray(new Object[runningRubyThreads.size()]);
    }

    @TruffleBoundary
    public Iterable<DynamicObject> iterateThreads() {
        return runningRubyThreads;
    }

    @TruffleBoundary
    public void interrupt(Thread thread) {
        final UnblockingAction action = unblockingActions.get(thread);

        if (action != null) {
            action.unblock();
        }

        thread.interrupt();
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

    private DynamicObject nil() {
        return context.getCoreLibrary().getNil();
    }

}
