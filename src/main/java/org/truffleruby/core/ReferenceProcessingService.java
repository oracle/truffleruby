package org.truffleruby.core;

import java.lang.ref.ReferenceQueue;

import org.truffleruby.RubyContext;
import org.truffleruby.core.thread.ThreadManager;

import com.oracle.truffle.api.object.DynamicObject;

public abstract class ReferenceProcessingService<T extends ReferenceProcessingService.ProcessingReference<T>> {

    public static interface ProcessingReference<U extends ProcessingReference<U>> {
        public U getPrevious();

        public void setPrevious(U previous);

        public U getNext();

        public void setNext(U next);
    }

    /** The head of a doubly-linked list of FinalizerReference, needed to collect finalizer Procs for ObjectSpace. */
    private T first = null;

    protected final ReferenceQueue<Object> processingQueue = new ReferenceQueue<>();

    protected DynamicObject processingThread;
    protected final RubyContext context;

    public ReferenceProcessingService(RubyContext context) {
        this.context = context;
    }

    protected final void drainReferenceQueue() {
        while (true) {
            @SuppressWarnings("unchecked")
            final T reference = (T) processingQueue.poll();

            if (reference == null) {
                break;
            }

            processReference(reference);
        }
    }

    protected void createProcessingThread() {
        final ThreadManager threadManager = context.getThreadManager();
        processingThread = threadManager.createBootThread(getThreadName());
        context.send(processingThread, "internal_thread_initialize");

        threadManager.initialize(processingThread, null, getThreadName(), () -> {
            while (true) {
                @SuppressWarnings("unchecked")
                final T reference = (T) threadManager.runUntilResult(null, processingQueue::remove);

                processReference(reference);
            }
        });
    }

    protected void processReference(T reference) {
        remove(reference);
    }

    protected abstract String getThreadName();

    protected void processReferenceQueue() {
        if (context.getOptions().SINGLE_THREADED) {

            drainReferenceQueue();

        } else {

            /*
             * We can't create a new thread while the context is initializing or finalizing, as the
             * polyglot API locks on creating new threads, and some core loading does things such as
             * stat files which could allocate memory that is marked to be automatically freed and
             * so would want to start the finalization thread. So don't start the finalization
             * thread if we are initializing. We will rely on some other finalizer to be created to
             * ever free this memory allocated during startup, but that's a reasonable assumption
             * and a low risk of leaking a tiny number of bytes if it doesn't hold.
             */

            if (processingThread == null && !context.isPreInitializing() && context.isInitialized() && !context.isFinalizing()) {
                createProcessingThread();
            }

        }
    }

    protected synchronized void remove(T ref) {
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

    protected synchronized void add(T newRef) {
        if (first != null) {
            newRef.setNext(first);
            first.setPrevious(newRef);
        }
        first = newRef;
    }

    protected T getFirst() {
        return first;
    }
}
