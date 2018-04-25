/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.fiber;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.core.thread.ThreadManager.BlockingAction;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.BreakException;
import org.truffleruby.language.control.ExitException;
import org.truffleruby.language.control.KillException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.ReturnException;
import org.truffleruby.language.control.TerminationException;
import org.truffleruby.language.objects.ObjectIDOperations;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.source.SourceSection;

/**
 * Manages Ruby {@code Fiber} objects for a given Ruby thread.
 */
public class FiberManager {

    public static final String NAME_PREFIX = "Ruby Fiber";

    private final RubyContext context;
    private final DynamicObject rootFiber;
    private DynamicObject currentFiber;
    private final Set<DynamicObject> runningFibers = newFiberSet();

    private final Map<Thread, DynamicObject> rubyFiberForeignMap = new ConcurrentHashMap<>();
    private final ThreadLocal<DynamicObject> rubyFiber = ThreadLocal.withInitial(() -> rubyFiberForeignMap.get(Thread.currentThread()));

    public FiberManager(RubyContext context, DynamicObject rubyThread) {
        this.context = context;
        this.rootFiber = createRootFiber(context, rubyThread);
        this.currentFiber = rootFiber;
    }

    @TruffleBoundary
    private static Set<DynamicObject> newFiberSet() {
        return Collections.newSetFromMap(new ConcurrentHashMap<DynamicObject, Boolean>());
    }

    public DynamicObject getRootFiber() {
        return rootFiber;
    }

    public DynamicObject getCurrentFiber() {
        assert Layouts.THREAD.getFiberManager(context.getThreadManager().getCurrentThread()) == this :
            "Trying to read the current Fiber of another Thread which is inherently racy";
        return currentFiber;
    }

    // If the currentFiber is read from another Ruby Thread,
    // there is no guarantee that fiber will remain the current one
    // as it could switch to another Fiber before the actual operation on the returned fiber.
    public DynamicObject getCurrentFiberRacy() {
        return currentFiber;
    }

    @TruffleBoundary
    public DynamicObject getRubyFiberFromCurrentJavaThread() {
        return rubyFiber.get();
    }

    private void setCurrentFiber(DynamicObject fiber) {
        assert RubyGuards.isRubyFiber(fiber);
        currentFiber = fiber;
    }

    private DynamicObject createRootFiber(RubyContext context, DynamicObject thread) {
        return createFiber(context, thread, context.getCoreLibrary().getFiberFactory(), "root Fiber for Thread");
    }

    public DynamicObject createFiber(RubyContext context, DynamicObject thread, DynamicObjectFactory factory, String name) {
        assert RubyGuards.isRubyThread(thread);
        CompilerAsserts.partialEvaluationConstant(context);
        final DynamicObject fiberLocals = Layouts.BASIC_OBJECT.createBasicObject(context.getCoreLibrary().getObjectFactory());
        final DynamicObject catchTags = ArrayHelpers.createArray(context, null, 0);

        return Layouts.FIBER.createFiber(
                factory,
                fiberLocals,
                catchTags,
                new CountDownLatch(1),
                new CountDownLatch(1),
                newMessageQueue(),
                thread,
                null,
                true,
                null,
                false);
    }

    @TruffleBoundary
    private static LinkedBlockingQueue<FiberMessage> newMessageQueue() {
        return new LinkedBlockingQueue<>();
    }

    public void initialize(DynamicObject fiber, DynamicObject block, Node currentNode) {
        context.getThreadManager().spawnFiber(() -> fiberMain(context, fiber, block, currentNode));

        if (!waitForInitialization(context, fiber, currentNode)) {
            throw new RuntimeException("the root fiber for a thread did not initialize in reasonable time");
        }
    }

    /** Wait for full initialization of the new fiber */
    public static boolean waitForInitialization(RubyContext context, DynamicObject fiber, Node currentNode) {
        final CountDownLatch initializedLatch = Layouts.FIBER.getInitializedLatch(fiber);

        return context.getThreadManager().runUntilResultKeepStatus(currentNode, () ->
            initializedLatch.await(3, TimeUnit.SECONDS)
        );
    }

    private static final BranchProfile UNPROFILED = BranchProfile.create();

    private void fiberMain(RubyContext context, DynamicObject fiber, DynamicObject block, Node currentNode) {
        assert fiber != rootFiber : "Root Fibers execute threadMain() and not fiberMain()";

        final Thread thread = Thread.currentThread();
        final SourceSection sourceSection = Layouts.PROC.getSharedMethodInfo(block).getSourceSection();
        final String oldName = thread.getName();
        thread.setName(NAME_PREFIX + " id=" + thread.getId() + " from " + context.getSourceLoader().fileLine(sourceSection));

        start(fiber, thread);
        try {

            final Object[] args = waitForResume(fiber);
            final Object result;
            try {
                result = ProcOperations.rootCall(block, args);
            } finally {
                // Make sure that other fibers notice we are dead before they gain control back
                Layouts.FIBER.setAlive(fiber, false);
            }
            resume(fiber, getReturnFiber(fiber, currentNode, UNPROFILED), FiberOperation.YIELD, result);

        // Handlers in the same order as in ThreadManager
        } catch (KillException | ExitException | RaiseException e) {
            // Propagate the exception until it reaches the root Fiber
            sendExceptionToParentFiber(fiber, e, currentNode);
        } catch (FiberShutdownException e) {
            // Ends execution of the Fiber
        } catch (BreakException e) {
            sendExceptionToParentFiber(fiber, new RaiseException(context.getCoreExceptions().breakFromProcClosure(currentNode)), currentNode);
        } catch (ReturnException e) {
            sendExceptionToParentFiber(fiber, new RaiseException(context.getCoreExceptions().unexpectedReturn(currentNode)), currentNode);
        } finally {
            cleanup(fiber, thread);
            thread.setName(oldName);
        }
    }

    private void sendExceptionToParentFiber(DynamicObject fiber, RuntimeException exception, Node currentNode) {
        addToMessageQueue(getReturnFiber(fiber, currentNode, UNPROFILED), new FiberExceptionMessage(exception));
    }

    public DynamicObject getReturnFiber(DynamicObject currentFiber, Node currentNode, BranchProfile errorProfile) {
        assert currentFiber == this.currentFiber;

        if (currentFiber == rootFiber) {
            errorProfile.enter();
            throw new RaiseException(context.getCoreExceptions().yieldFromRootFiberError(currentNode));
        }

        final DynamicObject parentFiber = Layouts.FIBER.getLastResumedByFiber(currentFiber);
        if (parentFiber != null) {
            Layouts.FIBER.setLastResumedByFiber(currentFiber, null);
            return parentFiber;
        } else {
            return rootFiber;
        }
    }

    @TruffleBoundary
    private void addToMessageQueue(DynamicObject fiber, FiberMessage message) {
        Layouts.FIBER.getMessageQueue(fiber).add(message);
    }

    /**
     * Send the Java thread that represents this fiber to sleep until it receives a resume or exit
     * message.
     */
    @TruffleBoundary
    private Object[] waitForResume(DynamicObject fiber) {
        assert RubyGuards.isRubyFiber(fiber);

        final FiberMessage message = context.getThreadManager().runUntilResultKeepStatus(null,
                () -> Layouts.FIBER.getMessageQueue(fiber).take());

        setCurrentFiber(fiber);

        if (message instanceof FiberShutdownMessage) {
            throw new FiberShutdownException();
        } else if (message instanceof FiberExceptionMessage) {
            throw ((FiberExceptionMessage) message).getException();
        } else if (message instanceof FiberResumeMessage) {
            final FiberResumeMessage resumeMessage = (FiberResumeMessage) message;
            assert context.getThreadManager().getCurrentThread() == Layouts.FIBER.getRubyThread(resumeMessage.getSendingFiber());
            if (resumeMessage.getOperation() == FiberOperation.RESUME) {
                Layouts.FIBER.setLastResumedByFiber(fiber, resumeMessage.getSendingFiber());
            }
            return resumeMessage.getArgs();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Send a resume message to a fiber by posting into its message queue. Doesn't explicitly notify
     * the Java thread (although the queue implementation may) and doesn't wait for the message to
     * be received.
     */
    private void resume(DynamicObject fromFiber, DynamicObject fiber, FiberOperation operation, Object... args) {
        addToMessageQueue(fiber, new FiberResumeMessage(operation, fromFiber, args));
    }

    public Object[] transferControlTo(DynamicObject fromFiber, DynamicObject fiber, FiberOperation operation, Object[] args) {
        resume(fromFiber, fiber, operation, args);
        return waitForResume(fromFiber);
    }

    public void start(DynamicObject fiber, Thread javaThread) {
        final ThreadManager threadManager = context.getThreadManager();

        if (Thread.currentThread() == javaThread) {
            rubyFiber.set(fiber);
        }
        if (!threadManager.isRubyManagedThread(javaThread)) {
            rubyFiberForeignMap.put(javaThread, fiber);
        }

        Layouts.FIBER.setThread(fiber, javaThread);

        final DynamicObject rubyThread = Layouts.FIBER.getRubyThread(fiber);
        threadManager.initializeValuesForJavaThread(rubyThread, javaThread);

        runningFibers.add(fiber);

        if (threadManager.isRubyManagedThread(javaThread)) {
            context.getSafepointManager().enterThread();
        }

        // fully initialized
        Layouts.FIBER.getInitializedLatch(fiber).countDown();
    }

    public void cleanup(DynamicObject fiber, Thread javaThread) {
        Layouts.FIBER.setAlive(fiber, false);

        if (context.getThreadManager().isRubyManagedThread(javaThread)) {
            context.getSafepointManager().leaveThread();
        }

        context.getThreadManager().cleanupValuesForJavaThread(javaThread);

        runningFibers.remove(fiber);

        Layouts.FIBER.setThread(fiber, null);

        if (Thread.currentThread() == javaThread) {
            rubyFiber.remove();
        }
        rubyFiberForeignMap.remove(javaThread);

        Layouts.FIBER.getFinishedLatch(fiber).countDown();
    }

    @TruffleBoundary
    public void killOtherFibers() {
        // All Fibers except the current one are in waitForResume(),
        // so sending a FiberShutdownMessage is enough to finish them.
        // This also avoids the performance cost of a safepoint.
        for (DynamicObject fiber : runningFibers) {
            if (fiber != rootFiber) {
                addToMessageQueue(fiber, new FiberShutdownMessage());

                // Wait for the Fiber to finish so we only run one Fiber at a time
                final CountDownLatch finishedLatch = Layouts.FIBER.getFinishedLatch(fiber);
                context.getThreadManager().runUntilResultKeepStatus(null, () -> {
                    finishedLatch.await();
                    return BlockingAction.SUCCESS;
                });
            }
        }
    }

    @TruffleBoundary
    public void shutdown(Thread javaThread) {
        killOtherFibers();
        cleanup(rootFiber, javaThread);
    }

    public String getFiberDebugInfo() {
        final StringBuilder builder = new StringBuilder();

        for (DynamicObject fiber : runningFibers) {
            builder.append("  fiber @");
            builder.append(ObjectIDOperations.verySlowGetObjectID(context, fiber));
            builder.append(" #");

            final Thread thread = Layouts.FIBER.getThread(fiber);

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
        private final DynamicObject sendingFiber;
        private final Object[] args;

        public FiberResumeMessage(FiberOperation operation, DynamicObject sendingFiber, Object[] args) {
            assert RubyGuards.isRubyFiber(sendingFiber);
            this.operation = operation;
            this.sendingFiber = sendingFiber;
            this.args = args;
        }

        public FiberOperation getOperation() {
            return operation;
        }

        public DynamicObject getSendingFiber() {
            return sendingFiber;
        }

        public Object[] getArgs() {
            return args;
        }

    }

    /**
     * Used to cleanup and terminate Fibers when the parent Thread dies.
     */
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
