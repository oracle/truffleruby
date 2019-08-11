/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;

import org.truffleruby.RubyContext;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.TerminationException;

import com.oracle.truffle.api.object.DynamicObject;

public abstract class ReferenceProcessingService<R extends ReferenceProcessingService.ProcessingReference<R>> {

    public static interface ProcessingReference<R extends ProcessingReference<R>> {
        public R getPrevious();

        public void setPrevious(R previous);

        public R getNext();

        public void setNext(R next);

        public ReferenceProcessingService<R> service();
    }

    public abstract static class WeakProcessingReference<R extends ProcessingReference<R>, T> extends WeakReference<T> implements ProcessingReference<R> {

        private R next;
        private R previous;
        private ReferenceProcessingService<R> service;

        public WeakProcessingReference(T object, ReferenceQueue<? super Object> queue, ReferenceProcessingService<R> service) {
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

        public ReferenceProcessingService<R> service() {
            return service;
        }
    }

    public abstract static class PhantomProcessingReference<R extends ProcessingReference<R>, T> extends PhantomReference<T> implements ProcessingReference<R> {

        /**
         * Doubly linked list of references to keep to allow the reference service to traverse them
         * and to keep the references alive for processing.
         */
        private R next;
        private R previous;
        private ReferenceProcessingService<R> service;

        public PhantomProcessingReference(T object, ReferenceQueue<? super Object> queue, ReferenceProcessingService<R> service) {
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

        public ReferenceProcessingService<R> service() {
            return service;
        }
    }

    public static class ReferenceProcessor {
        protected final ReferenceQueue<Object> processingQueue = new ReferenceQueue<>();

        protected DynamicObject processingThread;
        protected final RubyContext context;

        public ReferenceProcessor(RubyContext context) {
            this.context = context;
        }

        protected void processReferenceQueue(Class<?> owner) {
            if (context.getOptions().SINGLE_THREADED) {

                drainReferenceQueue();

            } else {

                /*
                 * We can't create a new thread while the context is initializing or finalizing, as
                 * the polyglot API locks on creating new threads, and some core loading does things
                 * such as stat files which could allocate memory that is marked to be automatically
                 * freed and so would want to start the finalization thread. So don't start the
                 * finalization thread if we are initializing. We will rely on some other finalizer
                 * to be created to ever free this memory allocated during startup, but that's a
                 * reasonable assumption and a low risk of leaking a tiny number of bytes if it
                 * doesn't hold.
                 */

                if (processingThread == null && !context.isPreInitializing() && context.isInitialized() && !context.isFinalizing()) {
                    createProcessingThread(owner);
                }

            }
        }

        protected void createProcessingThread(Class<?> owner) {
            final ThreadManager threadManager = context.getThreadManager();
            processingThread = threadManager.createBootThread(threadName());
            context.send(processingThread, "internal_thread_initialize");
            final String sharingReason = "creating " + threadName() + " thread for " + owner.getSimpleName();

            threadManager.initialize(processingThread, null, threadName(), sharingReason, () -> {
                while (true) {
                    final ProcessingReference<?> reference = (ProcessingReference<?>) threadManager.runUntilResult(null, processingQueue::remove);

                    reference.service().processReference(reference);
                }
            });
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

                reference.service().processReference(reference);
            }
        }

    }

    /**
     * The head of a doubly-linked list of FinalizerReference, needed to collect finalizer Procs for
     * ObjectSpace.
     */
    private R first = null;

    protected final ReferenceProcessor referenceProcessor;
    protected final RubyContext context;

    public ReferenceProcessingService(RubyContext context, ReferenceProcessor referenceProcessor) {
        this.context = context;
        this.referenceProcessor = referenceProcessor;
    }

    @SuppressWarnings("unchecked")
    protected void processReference(ProcessingReference<?> reference) {
        remove((R) reference);
    }

    protected void runCatchingErrors(Consumer<R> action, R reference) {
        try {
            action.accept(reference);
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

        if (ref.getNext() != null) {
            ref.getNext().setPrevious(ref.getPrevious());
        }
        if (ref.getPrevious() != null) {
            ref.getPrevious().setNext(ref.getNext());
        }

        // Mark that this ref has been removed.
        ref.setNext(ref);
        ref.setPrevious(ref);
    }

    protected synchronized void add(R newRef) {
        if (first != null) {
            newRef.setNext(first);
            first.setPrevious(newRef);
        }
        first = newRef;
    }

    protected R getFirst() {
        return first;
    }
}
