/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.fiber;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import com.oracle.truffle.api.TruffleContext;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.basicobject.BasicObjectNodes.ObjectIDNode;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.thread.RubyThread;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.core.thread.ThreadManager.BlockingAction;
import org.truffleruby.language.control.BreakException;
import org.truffleruby.language.control.DynamicReturnException;
import org.truffleruby.language.control.ExitException;
import org.truffleruby.language.control.KillException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.TerminationException;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.source.SourceSection;

/** Manages Ruby {@code Fiber} objects for a given Ruby thread. */
public class FiberManager {

    public static final String NAME_PREFIX = "Ruby Fiber";

    private final RubyContext context;
    private final RubyFiber rootFiber;
    private RubyFiber currentFiber;
    private final Set<RubyFiber> runningFibers = newFiberSet();

    public FiberManager(RubyLanguage language, RubyContext context, RubyThread rubyThread) {
        this.context = context;
        this.rootFiber = createRootFiber(language, context, rubyThread);
        this.currentFiber = rootFiber;
    }

    @TruffleBoundary
    private static Set<RubyFiber> newFiberSet() {
        return Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public RubyFiber getRootFiber() {
        return rootFiber;
    }

    public RubyFiber getCurrentFiber() {
        assert context
                .getThreadManager()
                .getCurrentThread().fiberManager == this : "Trying to read the current Fiber of another Thread which is inherently racy";
        return currentFiber;
    }

    // If the currentFiber is read from another Ruby Thread,
    // there is no guarantee that fiber will remain the current one
    // as it could switch to another Fiber before the actual operation on the returned fiber.
    public RubyFiber getCurrentFiberRacy() {
        return currentFiber;
    }

    private void setCurrentFiber(RubyFiber fiber) {
        currentFiber = fiber;
    }

    private RubyFiber createRootFiber(RubyLanguage language, RubyContext context, RubyThread thread) {
        return createFiber(language, context, thread, context.getCoreLibrary().fiberClass, language.fiberShape);
    }

    public RubyFiber createFiber(RubyLanguage language, RubyContext context, RubyThread thread, RubyClass rubyClass,
            Shape shape) {
        CompilerAsserts.partialEvaluationConstant(language);
        final RubyBasicObject fiberLocals = new RubyBasicObject(
                context.getCoreLibrary().objectClass,
                language.basicObjectShape);
        final RubyArray catchTags = ArrayHelpers.createEmptyArray(context, language);

        return new RubyFiber(rubyClass, shape, fiberLocals, catchTags, thread);
    }

    public void initialize(RubyFiber fiber, RubyProc block, Node currentNode) {
        final TruffleContext truffleContext = context.getEnv().getContext();

        ThreadManager.FIBER_BEING_SPAWNED.set(fiber);
        try {
            context.getThreadManager().leaveAndEnter(truffleContext, currentNode, () -> {
                context.getThreadManager().spawnFiber(fiber, () -> fiberMain(context, fiber, block, currentNode));
                waitForInitialization(context, fiber, currentNode);
                return BlockingAction.SUCCESS;
            }, context.getThreadManager().isRubyManagedThread(Thread.currentThread()));
        } finally {
            ThreadManager.FIBER_BEING_SPAWNED.remove();
        }
    }

    /** Wait for full initialization of the new fiber */
    public static void waitForInitialization(RubyContext context, RubyFiber fiber, Node currentNode) {
        final CountDownLatch initializedLatch = fiber.initializedLatch;

        final BlockingAction<Boolean> blockingAction = () -> {
            initializedLatch.await();
            return BlockingAction.SUCCESS;
        };

        if (context.getEnv().getContext().isEntered()) {
            context.getThreadManager().runUntilResultKeepStatus(currentNode, blockingAction);
        } else {
            context.getThreadManager().retryWhileInterrupted(blockingAction);
        }

        final Throwable uncaughtException = fiber.uncaughtException;
        if (uncaughtException != null) {
            ExceptionOperations.rethrow(uncaughtException);
        }
    }

    private static final BranchProfile UNPROFILED = BranchProfile.getUncached();

    private void fiberMain(RubyContext context, RubyFiber fiber, RubyProc block, Node currentNode) {
        assert fiber != rootFiber : "Root Fibers execute threadMain() and not fiberMain()";

        final Thread thread = Thread.currentThread();
        final SourceSection sourceSection = block.sharedMethodInfo.getSourceSection();
        final String oldName = thread.getName();
        thread.setName(NAME_PREFIX + " id=" + thread.getId() + " from " + RubyLanguage.fileLine(sourceSection));

        start(fiber, thread, false);

        final TruffleContext truffleContext = context.getEnv().getContext();
        assert !truffleContext.isEntered();

        final Object prev = truffleContext.enter(currentNode); // enter and leave now to workaround GR-29773
        context.getSafepointManager().enterThread(); // not done in start() above because the context was not entered
        final FiberMessage message = context.getThreadManager().leaveAndEnter(truffleContext, currentNode, () -> {
            // fully initialized
            fiber.initializedLatch.countDown();
            return waitMessage(fiber);
        }, true);

        try {
            final Object result;
            try {
                final Object[] args = handleMessage(fiber, message);
                result = ProcOperations.rootCall(block, args);
            } finally {
                // Make sure that other fibers notice we are dead before they gain control back
                fiber.alive = false;
                // Leave before resume/sendExceptionToParentFiber -> addToMessageQueue() -> parent Fiber starts executing
                context.getSafepointManager().leaveThread();
                truffleContext.leave(currentNode, prev);
            }
            resume(fiber, getReturnFiber(fiber, currentNode, UNPROFILED), FiberOperation.YIELD, result);

            // Handlers in the same order as in ThreadManager
        } catch (KillException | ExitException | RaiseException e) {
            // Propagate the exception until it reaches the root Fiber
            sendExceptionToParentFiber(fiber, e, currentNode);
        } catch (FiberShutdownException e) {
            // Ends execution of the Fiber
        } catch (BreakException e) {
            sendExceptionToParentFiber(
                    fiber,
                    new RaiseException(context, context.getCoreExceptions().breakFromProcClosure(currentNode)),
                    currentNode);
        } catch (DynamicReturnException e) {
            sendExceptionToParentFiber(
                    fiber,
                    new RaiseException(context, context.getCoreExceptions().unexpectedReturn(currentNode)),
                    currentNode);
        } catch (Throwable e) {
            final RuntimeException exception = ThreadManager.printInternalError(e);
            sendExceptionToParentFiber(fiber, exception, currentNode);
        } finally {
            cleanup(fiber, thread, false);
            thread.setName(oldName);
        }
    }

    private void sendExceptionToParentFiber(RubyFiber fiber, RuntimeException exception, Node currentNode) {
        addToMessageQueue(getReturnFiber(fiber, currentNode, UNPROFILED), new FiberExceptionMessage(exception));
    }

    public RubyFiber getReturnFiber(RubyFiber currentFiber, Node currentNode, BranchProfile errorProfile) {
        assert currentFiber == this.currentFiber;

        if (currentFiber == rootFiber) {
            errorProfile.enter();
            throw new RaiseException(context, context.getCoreExceptions().yieldFromRootFiberError(currentNode));
        }

        final RubyFiber parentFiber = currentFiber.lastResumedByFiber;
        if (parentFiber != null) {
            currentFiber.lastResumedByFiber = null;
            return parentFiber;
        } else {
            return rootFiber;
        }
    }

    @TruffleBoundary
    private void addToMessageQueue(RubyFiber fiber, FiberMessage message) {
        assert !context.getEnv().getContext().isEntered() : "should have left context when sending message to fiber";
        fiber.messageQueue.add(message);
    }

    /** Send the Java thread that represents this fiber to sleep until it receives a message. */
    @TruffleBoundary
    private FiberMessage waitMessage(RubyFiber fiber) {
        assert !context.getEnv().getContext().isEntered() : "should have left context while waiting fiber message";
        return context.getThreadManager().retryWhileInterrupted(fiber.messageQueue::take);
    }

    @TruffleBoundary
    private Object[] handleMessage(RubyFiber fiber, FiberMessage message) {
        setCurrentFiber(fiber);

        if (message instanceof FiberShutdownMessage) {
            throw new FiberShutdownException();
        } else if (message instanceof FiberExceptionMessage) {
            throw ((FiberExceptionMessage) message).getException();
        } else if (message instanceof FiberResumeMessage) {
            final FiberResumeMessage resumeMessage = (FiberResumeMessage) message;
            assert context.getThreadManager().getCurrentThread() == resumeMessage.getSendingFiber().rubyThread;
            if (resumeMessage.getOperation() == FiberOperation.RESUME) {
                fiber.lastResumedByFiber = resumeMessage.getSendingFiber();
            }
            return resumeMessage.getArgs();
        } else {
            throw CompilerDirectives.shouldNotReachHere();
        }
    }

    /** Send a resume message to a fiber by posting into its message queue. Doesn't explicitly notify the Java thread
     * (although the queue implementation may) and doesn't wait for the message to be received. */
    private void resume(RubyFiber fromFiber, RubyFiber fiber, FiberOperation operation, Object... args) {
        addToMessageQueue(fiber, new FiberResumeMessage(operation, fromFiber, args));
    }

    @TruffleBoundary
    public Object[] transferControlTo(RubyFiber fromFiber, RubyFiber fiber, FiberOperation operation, Object[] args) {
        final TruffleContext truffleContext = context.getEnv().getContext();
        final boolean isRubyManagedThread = context.getThreadManager().isRubyManagedThread(Thread.currentThread());

        final FiberMessage message = context.getThreadManager().leaveAndEnter(truffleContext, null, () -> {
            resume(fromFiber, fiber, operation, args);
            return waitMessage(fromFiber);
        }, isRubyManagedThread);

        return handleMessage(fromFiber, message);
    }

    public void start(RubyFiber fiber, Thread javaThread, boolean entered) {
        assert entered == context.getEnv().getContext().isEntered();
        final ThreadManager threadManager = context.getThreadManager();

        if (Thread.currentThread() == javaThread) {
            context.getThreadManager().rubyFiber.set(fiber);
        }
        if (!threadManager.isRubyManagedThread(javaThread)) {
            context.getThreadManager().rubyFiberForeignMap.put(javaThread, fiber);
        }

        fiber.thread = javaThread;

        final RubyThread rubyThread = fiber.rubyThread;
        threadManager.initializeValuesForJavaThread(rubyThread, javaThread);

        runningFibers.add(fiber);

        if (threadManager.isRubyManagedThread(javaThread) && Thread.currentThread() == javaThread && entered) {
            context.getSafepointManager().enterThread();
        }
    }

    public void cleanup(RubyFiber fiber, Thread javaThread, boolean entered) {
        assert entered == context.getEnv().getContext().isEntered();
        final ThreadManager threadManager = context.getThreadManager();

        fiber.alive = false;

        if (threadManager.isRubyManagedThread(javaThread) && Thread.currentThread() == javaThread && entered) {
            context.getSafepointManager().leaveThread();
        }

        threadManager.cleanupValuesForJavaThread(javaThread);

        runningFibers.remove(fiber);

        fiber.thread = null;

        if (Thread.currentThread() == javaThread) {
            threadManager.rubyFiber.remove();
        }
        threadManager.rubyFiberForeignMap.remove(javaThread);

        fiber.finishedLatch.countDown();
    }

    @TruffleBoundary
    public void killOtherFibers() {
        // All Fibers except the current one are in waitForResume(),
        // so sending a FiberShutdownMessage is enough to finish them.
        // This also avoids the performance cost of a safepoint.

        // This method might not be executed on the rootFiber Java Thread but possibly on another Java Thread.

        final TruffleContext truffleContext = context.getEnv().getContext();
        context.getThreadManager().leaveAndEnter(truffleContext, null, () -> {
            doKillOtherFibers();
            return BlockingAction.SUCCESS;
        }, context.getThreadManager().isRubyManagedThread(Thread.currentThread()));
    }

    private void doKillOtherFibers() {
        for (RubyFiber fiber : runningFibers) {
            if (fiber != rootFiber) {
                addToMessageQueue(fiber, new FiberShutdownMessage());

                // Wait for the Fiber to finish so we only run one Fiber at a time
                final CountDownLatch finishedLatch = fiber.finishedLatch;
                context.getThreadManager().retryWhileInterrupted(() -> {
                    finishedLatch.await();
                    return BlockingAction.SUCCESS;
                });

                final Throwable uncaughtException = fiber.uncaughtException;
                if (uncaughtException != null) {
                    ExceptionOperations.rethrow(uncaughtException);
                }
            }
        }
    }

    @TruffleBoundary
    public void shutdown(Thread javaThread) {
        killOtherFibers();
        cleanup(rootFiber, javaThread, true);
    }

    public String getFiberDebugInfo() {
        final StringBuilder builder = new StringBuilder();

        for (RubyFiber fiber : runningFibers) {
            builder.append("  fiber @");
            builder.append(ObjectIDNode.getUncached().execute(fiber));
            builder.append(" #");

            final Thread thread = fiber.thread;

            if (thread == null) {
                builder.append("(no Java thread)");
            } else {
                builder.append(thread.getId());
            }

            if (fiber == rootFiber) {
                builder.append(" (root)");
            }

            if (fiber == currentFiber) {
                builder.append(" (current)");
            }

            builder.append("\n");
        }

        if (builder.length() == 0) {
            return "  no fibers\n";
        } else {
            return builder.toString();
        }
    }

    public interface FiberMessage {
    }

    private static class FiberResumeMessage implements FiberMessage {

        private final FiberOperation operation;
        private final RubyFiber sendingFiber;
        private final Object[] args;

        public FiberResumeMessage(FiberOperation operation, RubyFiber sendingFiber, Object[] args) {
            this.operation = operation;
            this.sendingFiber = sendingFiber;
            this.args = args;
        }

        public FiberOperation getOperation() {
            return operation;
        }

        public RubyFiber getSendingFiber() {
            return sendingFiber;
        }

        public Object[] getArgs() {
            return args;
        }

    }

    /** Used to cleanup and terminate Fibers when the parent Thread dies. */
    private static class FiberShutdownException extends TerminationException {
        private static final long serialVersionUID = 1522270454305076317L;
    }

    private static class FiberShutdownMessage implements FiberMessage {
    }

    private static class FiberExceptionMessage implements FiberMessage {

        private final RuntimeException exception;

        public FiberExceptionMessage(RuntimeException exception) {
            this.exception = exception;
        }

        public RuntimeException getException() {
            return exception;
        }

    }

}
