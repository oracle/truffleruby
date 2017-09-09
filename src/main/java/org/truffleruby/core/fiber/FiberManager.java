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

    public FiberManager(RubyContext context, DynamicObject rubyThread) {
        this.context = context;
        this.rootFiber = createRootFiber(rubyThread);
        this.currentFiber = rootFiber;
    }

    public DynamicObject getRootFiber() {
        return rootFiber;
    }

    public DynamicObject getCurrentFiber() {
        return currentFiber;
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
                new LinkedBlockingQueue<>(2),
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
            // Naturally exit the Fiber on catching this
        } catch (BreakException e) {
            sendExceptionToParentFiber(fiber, context.getCoreExceptions().breakFromProcClosure(null));
        } catch (ReturnException e) {
            sendExceptionToParentFiber(fiber, context.getCoreExceptions().unexpectedReturn(null));
        } catch (RaiseException e) {
            sendExceptionToParentFiber(fiber, e.getException());
        } finally {
            cleanup(fiber);
        }
    }

    private void sendExceptionToParentFiber(DynamicObject fiber, DynamicObject exception) {
        addToMessageQueue(Layouts.FIBER.getLastResumedByFiber(fiber), new FiberExceptionMessage(exception));
    }

    @TruffleBoundary
    private static void addToMessageQueue(DynamicObject fiber, FiberMessage message) {
        Layouts.FIBER.getMessageQueue(fiber).add(message);
    }

    /**
     * Send the Java thread that represents this fiber to sleep until it receives a resume or exit
     * message.
     */
    @TruffleBoundary
    private Object[] waitForResume(DynamicObject fiber) {
        assert RubyGuards.isRubyFiber(fiber);

        final FiberMessage message = context.getThreadManager().runUntilResult(null,
                () -> Layouts.FIBER.getMessageQueue(fiber).take());

        setCurrentFiber(fiber);

        if (message instanceof FiberExitMessage) {
            throw new KillException();
        } else if (message instanceof FiberExceptionMessage) {
            throw new RaiseException(((FiberExceptionMessage) message).getException());
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
    private static void resume(DynamicObject fromFiber, DynamicObject fiber, boolean yield, Object... args) {
        addToMessageQueue(fiber, new FiberResumeMessage(yield, fromFiber, args));
    }

    public Object[] transferControlTo(DynamicObject fromFiber, DynamicObject fiber, boolean yield, Object[] args) {
        resume(fromFiber, fiber, yield, args);
        return waitForResume(fromFiber);
    }

    public void start(DynamicObject fiber) {
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

        Layouts.FIBER.getFinishedLatch(fiber).countDown();
    }

    private void exit(DynamicObject fiber) {
        assert RubyGuards.isRubyFiber(fiber);
        assert !Layouts.FIBER.getRootFiber(fiber);

        addToMessageQueue(fiber, new FiberExitMessage());
    }

    @TruffleBoundary
    public void shutdown() {
        for (DynamicObject fiber : runningFibers) {
            if (!Layouts.FIBER.getRootFiber(fiber)) {
                exit(fiber);
            }
        }
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

    public static class FiberResumeMessage implements FiberMessage {

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

    public static class FiberExitMessage implements FiberMessage {
    }

    public static class FiberExceptionMessage implements FiberMessage {

        private final DynamicObject exception;

        public FiberExceptionMessage(DynamicObject exception) {
            this.exception = exception;
        }

        public DynamicObject getException() {
            return exception;
        }

    }

}
