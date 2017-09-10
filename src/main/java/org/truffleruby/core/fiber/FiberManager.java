/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.fiber;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.ControlFlowException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.source.SourceSection;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.thread.ThreadManager.BlockingAction;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.BreakException;
import org.truffleruby.language.control.ExitException;
import org.truffleruby.language.control.KillException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.ReturnException;
import org.truffleruby.language.objects.ObjectIDOperations;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Manages Ruby {@code Fiber} objects for a given Ruby thread.
 */
public class FiberManager {

    public static final String NAME_PREFIX = "Ruby Fiber@";

    private final RubyContext context;
    private final DynamicObject rootFiber;
    private DynamicObject currentFiber;
    private final Set<DynamicObject> runningFibers = Collections.newSetFromMap(new ConcurrentHashMap<DynamicObject, Boolean>());

    private final ThreadLocal<DynamicObject> rubyFiber = new ThreadLocal<>();

    public FiberManager(RubyContext context, DynamicObject rubyThread) {
        this.context = context;
        this.rootFiber = createRootFiber(rubyThread);
        this.currentFiber = rootFiber;
    }

    public DynamicObject getRootFiber() {
        return rootFiber;
    }

    public DynamicObject getCurrentFiber() {
        assert Layouts.THREAD.getFiberManager(context.getThreadManager().getCurrentThread()) == this :
            "FiberManager#getCurrentFiber() must be called on a Fiber belonging to the FiberManager";
        return currentFiber;
    }

    // If the currentFiber is read from another Ruby Thread,
    // there is no guarantee that fiber will remain the current one
    // as it could switch to another Fiber before the actual operation on the fiber.
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

    private DynamicObject createRootFiber(DynamicObject thread) {
        return createFiber(thread, context.getCoreLibrary().getFiberFactory(), "root Fiber for Thread", true);
    }

    public DynamicObject createFiber(DynamicObject thread, DynamicObjectFactory factory, String name) {
        return createFiber(thread, factory, name, false);
    }

    private DynamicObject createFiber(DynamicObject thread, DynamicObjectFactory factory, String name, boolean isRootFiber) {
        assert RubyGuards.isRubyThread(thread);
        final DynamicObject fiberLocals = Layouts.BASIC_OBJECT.createBasicObject(context.getCoreLibrary().getObjectFactory());
        final DynamicObject catchTags = ArrayHelpers.createArray(context, null, 0);

        return Layouts.FIBER.createFiber(
                factory,
                fiberLocals,
                catchTags,
                isRootFiber,
                new CountDownLatch(1),
                new CountDownLatch(1),
                new LinkedBlockingQueue<>(),
                thread,
                null,
                true,
                null,
                0L);
    }

    public void initialize(DynamicObject fiber, DynamicObject block, Node currentNode) {
        final SourceSection sourceSection = Layouts.PROC.getSharedMethodInfo(block).getSourceSection();
        final String name = NAME_PREFIX + RubyLanguage.fileLine(sourceSection);
        final Thread thread = context.getLanguage().createThread(context,
                () -> fiberMain(context, fiber, block, currentNode));
        thread.setName(name);
        thread.start();

        waitForInitialization(context, fiber, currentNode);
    }

    /** Wait for full initialization of the new fiber */
    public static void waitForInitialization(RubyContext context, DynamicObject fiber, Node currentNode) {
        final CountDownLatch initializedLatch = Layouts.FIBER.getInitializedLatch(fiber);

        context.getThreadManager().runUntilSuccessKeepRunStatus(currentNode, new BlockingAction<Boolean>() {
            @Override
            public Boolean block() throws InterruptedException {
                initializedLatch.await();
                return SUCCESS;
            }
        });
    }

    private void fiberMain(RubyContext context, DynamicObject fiber, DynamicObject block, Node currentNode) {
        assert !Layouts.FIBER.getRootFiber(fiber) : "Root Fibers execute threadMain() and not fiberMain()";

        start(fiber);
        try {

            final Object[] args = waitForResume(fiber);
            final Object result;
            try {
                result = ProcOperations.rootCall(block, args);
            } finally {
                // Make sure that other fibers notice we are dead before they gain control back
                Layouts.FIBER.setAlive(fiber, false);
            }
            resume(fiber, Layouts.FIBER.getLastResumedByFiber(fiber), true, result);

        } catch (KillException e) {
            // Propagate the kill exception until it reaches the root Fiber
            sendExceptionToParentFiber(fiber, e);
        } catch (FiberShutdownException e) {
            // Ends execution of the Fiber
        } catch (ExitException e) {
            sendExceptionToParentFiber(fiber, e);
        } catch (RaiseException e) {
            sendExceptionToParentFiber(fiber, e);
        } catch (BreakException e) {
            sendExceptionToParentFiber(fiber, new RaiseException(context.getCoreExceptions().breakFromProcClosure(null)));
        } catch (ReturnException e) {
            sendExceptionToParentFiber(fiber, new RaiseException(context.getCoreExceptions().unexpectedReturn(null)));
        } finally {
            cleanup(fiber);
        }
    }

    private void sendExceptionToParentFiber(DynamicObject fiber, RuntimeException exception) {
        final DynamicObject parentFiber = Layouts.FIBER.getLastResumedByFiber(fiber);
        assert parentFiber != null;
        addToMessageQueue(parentFiber, new FiberExceptionMessage(exception));
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

        final FiberMessage message = context.getThreadManager().runUntilSuccessKeepRunStatus(null,
                () -> Layouts.FIBER.getMessageQueue(fiber).take());

        setCurrentFiber(fiber);

        if (message instanceof FiberShutdownMessage) {
            throw new FiberShutdownException();
        } else if (message instanceof FiberExceptionMessage) {
            throw ((FiberExceptionMessage) message).getException();
        } else if (message instanceof FiberResumeMessage) {
            final FiberResumeMessage resumeMessage = (FiberResumeMessage) message;
            assert context.getThreadManager().getCurrentThread() == Layouts.FIBER.getRubyThread(resumeMessage.getSendingFiber());
            if (!resumeMessage.isYield()) {
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
    private void resume(DynamicObject fromFiber, DynamicObject fiber, boolean yield, Object... args) {
        addToMessageQueue(fiber, new FiberResumeMessage(yield, fromFiber, args));
    }

    public Object[] transferControlTo(DynamicObject fromFiber, DynamicObject fiber, boolean yield, Object[] args) {
        resume(fromFiber, fiber, yield, args);
        return waitForResume(fromFiber);
    }

    public void start(DynamicObject fiber) {
        rubyFiber.set(fiber);
        Layouts.FIBER.setThread(fiber, Thread.currentThread());

        final long pThreadID = context.getNativePlatform().getThreads().pthread_self();
        Layouts.FIBER.setPThreadID(fiber, pThreadID);

        final DynamicObject rubyThread = Layouts.FIBER.getRubyThread(fiber);
        context.getThreadManager().initializeValuesBasedOnCurrentJavaThread(rubyThread, pThreadID);

        runningFibers.add(fiber);
        context.getSafepointManager().enterThread();

        // fully initialized
        Layouts.FIBER.getInitializedLatch(fiber).countDown();
    }

    public void cleanup(DynamicObject fiber) {
        Layouts.FIBER.setAlive(fiber, false);

        context.getThreadManager().cleanupValuesBasedOnCurrentJavaThread();

        context.getSafepointManager().leaveThread();
        runningFibers.remove(fiber);

        Layouts.FIBER.setThread(fiber, null);
        rubyFiber.remove();

        Layouts.FIBER.getFinishedLatch(fiber).countDown();
    }

    @TruffleBoundary
    public void shutdown() {
        if (Thread.currentThread() != Layouts.FIBER.getThread(rootFiber)) {
            throw new UnsupportedOperationException("FiberManager.shutdown() must be called on the root Fiber");
        }

        // All Fibers except the current one are in waitForResume(),
        // so sending a FiberShutdownMessage is enough to finish them.
        // This also avoids the performance cost of a safepoint.
        for (DynamicObject fiber : runningFibers) {
            if (fiber != rootFiber) {
                addToMessageQueue(fiber, new FiberShutdownMessage());

                // Wait for the Fiber to finish so we only run one Fiber at a time
                final CountDownLatch finishedLatch = Layouts.FIBER.getFinishedLatch(fiber);
                context.getThreadManager().runUntilResult(null, () -> {
                    finishedLatch.await();
                    return BlockingAction.SUCCESS;
                });
            }
        }

        cleanup(rootFiber);
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

        private final boolean yield;
        private final DynamicObject sendingFiber;
        private final Object[] args;

        public FiberResumeMessage(boolean yield, DynamicObject sendingFiber, Object[] args) {
            assert RubyGuards.isRubyFiber(sendingFiber);
            this.yield = yield;
            this.sendingFiber = sendingFiber;
            this.args = args;
        }

        public boolean isYield() {
            return yield;
        }

        public DynamicObject getSendingFiber() {
            return sendingFiber;
        }

        public Object[] getArgs() {
            return args;
        }

    }

    private static class FiberShutdownException extends ControlFlowException {
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
