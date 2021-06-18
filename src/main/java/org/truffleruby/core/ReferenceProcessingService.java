/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.function.BiConsumer;

import com.oracle.truffle.api.CompilerDirectives;
import org.truffleruby.RubyContext;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.core.thread.RubyThread;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.language.control.KillException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.TerminationException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public abstract class ReferenceProcessingService<R extends ReferenceProcessingService.ProcessingReference<R>> {

    public static interface ProcessingReference<R extends ProcessingReference<R>> {
        public R getPrevious();

        public void setPrevious(R previous);

        public R getNext();

        public void setNext(R next);

        public void remove();

        public ReferenceProcessingService<R> service();
    }

    public abstract static class WeakProcessingReference<R extends ProcessingReference<R>, T> extends WeakReference<T>
            implements ProcessingReference<R> {

        private R next;
        private R previous;
        private ReferenceProcessingService<R> service;

        public WeakProcessingReference(
                T object,
                ReferenceQueue<? super Object> queue,
                ReferenceProcessingService<R> service) {
            super(object, queue);
            this.service = service;
        }

        private void check(R previous, R next) {
            if (next != null && next == previous) {
                throw CompilerDirectives.shouldNotReachHere("broken doubly-linked list of WeakProcessingReference");
            }
        }

        public R getPrevious() {
            return previous;
        }

        public void setPrevious(R previous) {
            check(previous, next);
            this.previous = previous;
        }

        public R getNext() {
            return next;
        }

        public void setNext(R next) {
            check(previous, next);
            this.next = next;
        }

        @SuppressWarnings("unchecked")
        public void remove() {
            this.next = (R) this;
            this.previous = (R) this;
        }

        public ReferenceProcessingService<R> service() {
            return service;
        }
    }

    public abstract static class PhantomProcessingReference<R extends ProcessingReference<R>, T>
            extends
            PhantomReference<T> implements ProcessingReference<R> {

        /** Doubly linked list of references to keep to allow the reference service to traverse them and to keep the
         * references alive for processing. */
        private R next;
        private R previous;
        private ReferenceProcessingService<R> service;

        public PhantomProcessingReference(
                T object,
                ReferenceQueue<? super Object> queue,
                ReferenceProcessingService<R> service) {
            super(object, queue);
            this.service = service;
        }

        public R getPrevious() {
            return previous;
        }

        public void setPrevious(R previous) {
            this.previous = previous;
        }

        public R getNext() {
            return next;
        }

        public void setNext(R next) {
            this.next = next;
        }

        @SuppressWarnings("unchecked")
        public void remove() {
            this.next = (R) this;
            this.previous = (R) this;
        }

        public ReferenceProcessingService<R> service() {
            return service;
        }
    }

    public static class ReferenceProcessor {
        protected final ReferenceQueue<Object> processingQueue = new ReferenceQueue<>();

        private volatile boolean shutdown = false;
        protected RubyThread processingThread;
        protected final RubyContext context;

        public ReferenceProcessor(RubyContext context) {
            this.context = context;
        }

        @TruffleBoundary
        protected void processReferenceQueue(Class<?> owner) {
            if (context.getOptions().SINGLE_THREADED || context.hasOtherPublicLanguages()) {
                drainReferenceQueue();
            } else {
                /* We can't create a new thread while the context is initializing or finalizing, as the polyglot API
                 * locks on creating new threads, and some core loading does things such as stat files which could
                 * allocate memory that is marked to be automatically freed and so would want to start the finalization
                 * thread. So don't start the finalization thread if we are initializing. We will rely on some other
                 * finalizer to be created to ever free this memory allocated during startup, but that's a reasonable
                 * assumption and a low risk of leaking a tiny number of bytes if it doesn't hold. */
                if (processingThread == null && !context.isPreInitializing() && context.isInitialized() &&
                        !context.isFinalizing()) {
                    createProcessingThread(owner);
                }
            }
        }

        protected void createProcessingThread(Class<?> owner) {
            final ThreadManager threadManager = context.getThreadManager();
            RubyThread newThread;
            synchronized (this) {
                if (processingThread != null) {
                    return;
                }
                newThread = threadManager.createBootThread(threadName());
                processingThread = newThread;
            }
            final String sharingReason = "creating " + threadName() + " thread for " + owner.getSimpleName();

            threadManager.initialize(newThread, DummyNode.INSTANCE, threadName(), sharingReason, () -> {
                while (true) {
                    final ProcessingReference<?> reference = (ProcessingReference<?>) threadManager
                            .runUntilResult(DummyNode.INSTANCE, () -> {
                                try {
                                    return processingQueue.remove();
                                } catch (InterruptedException interrupted) {
                                    if (shutdown) {
                                        throw new KillException(DummyNode.INSTANCE);
                                    } else {
                                        throw interrupted;
                                    }
                                }
                            });

                    reference.service().processReference(context, reference);
                }
            });
        }

        public boolean shutdownProcessingThread() {
            final Thread javaThread = processingThread == null ? null : processingThread.thread;
            if (javaThread == null) {
                return false;
            }

            shutdown = true;
            javaThread.interrupt();

            context.getThreadManager().runUntilResultKeepStatus(DummyNode.INSTANCE, t -> t.join(1000), javaThread);
            return true;
        }

        public RubyThread getProcessingThread() {
            return processingThread;
        }

        protected final String threadName() {
            return "Ruby-reference-processor";
        }

        protected final void drainReferenceQueue() {
            while (true) {
                @SuppressWarnings("unchecked")
                final ProcessingReference<?> reference = (ProcessingReference<?>) processingQueue.poll();

                if (reference == null) {
                    break;
                }

                reference.service().processReference(context, reference);
            }
        }

    }

    /** The head of a doubly-linked list of FinalizerReference, needed to collect finalizer Procs for ObjectSpace. */
    private R first = null;

    protected final ReferenceQueue<Object> processingQueue;

    public ReferenceProcessingService(ReferenceQueue<Object> processingQueue) {
        this.processingQueue = processingQueue;
    }

    @SuppressWarnings("unchecked")
    protected void processReference(RubyContext context, ProcessingReference<?> reference) {
        remove((R) reference);
    }

    protected void runCatchingErrors(RubyContext context, BiConsumer<RubyContext, R> action, R reference) {
        try {
            action.accept(context, reference);
        } catch (TerminationException e) {
            throw e;
        } catch (RaiseException e) {
            context.getCoreExceptions().showExceptionIfDebug(e.getException());
        } catch (Exception e) {
            // Do nothing, the finalizer thread must continue to process objects.
            if (context.getCoreLibrary().getDebug() == Boolean.TRUE) {
                e.printStackTrace();
            }
        }
    }

    @TruffleBoundary
    protected synchronized void remove(R ref) {
        if (ref.getNext() == ref) {
            // Already removed.
            return;
        }

        if (first == ref) {
            if (ref.getNext() != null) {
                first = ref.getNext();
            } else {
                // The list becomes empty
                first = null;
            }
        }

        R next = ref.getNext();
        R prev = ref.getPrevious();

        if (next != null) {
            next.setPrevious(prev);
        }
        if (prev != null) {
            prev.setNext(next);
        }

        // Mark that this ref has been removed.
        ref.remove();
    }

    @TruffleBoundary
    protected synchronized void add(R newRef) {
        if (first != null) {
            newRef.setNext(first);
            first.setPrevious(newRef);
        }
        first = newRef;
    }

    @SuppressFBWarnings("IS")
    protected R getFirst() {
        return first;
    }
}
