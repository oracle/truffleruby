/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
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
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;

import org.truffleruby.RubyContext;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.TerminationException;

import com.oracle.truffle.api.object.DynamicObject;

/**
 * Finalizers are implemented with phantom references and reference queues, and are run in a
 * dedicated Ruby thread.
 */
public class FinalizationService {

    private static class Finalizer {

        private final Class<?> owner;
        private final Runnable action;
        private final DynamicObject root;

        public Finalizer(Class<?> owner, Runnable action, DynamicObject root) {
            this.owner = owner;
            this.action = action;
            this.root = root;
        }

        public Class<?> getOwner() {
            return owner;
        }

        public Runnable getAction() {
            return action;
        }

        public DynamicObject getRoot() {
            return root;
        }
    }

    public static class FinalizerReference extends PhantomReference<Object> {

        /**
         * All accesses to this Deque must be synchronized by taking the
         * {@link FinalizationService} monitor, to avoid concurrent access.
         */
        private final Deque<Finalizer> finalizers = new LinkedList<>();

        /** The doubly-linked list of FinalizerReference, needed to collect finalizer Procs for ObjectSpace. */
        private FinalizerReference next = null;
        private FinalizerReference prev = null;

        private FinalizerReference(Object object, ReferenceQueue<? super Object> queue) {
            super(object, queue);
        }

        private void addFinalizer(Class<?> owner, Runnable action, DynamicObject root) {
            finalizers.addLast(new Finalizer(owner, action, root));
        }

        private FinalizerReference removeFinalizers(FinalizationService finalizationService, Class<?> owner) {
            finalizers.removeIf(f -> f.getOwner() == owner);

            if (finalizers.isEmpty()) {
                finalizationService.remove(this);
                return null;
            } else {
                return this;
            }
        }

        private Finalizer getFirstFinalizer() {
            return finalizers.pollFirst();
        }

        private void collectRoots(Collection<DynamicObject> roots) {
            for (Finalizer finalizer : finalizers) {
                final DynamicObject root = finalizer.getRoot();
                if (root != null) {
                    roots.add(root);
                }
            }
        }
    }

    private final RubyContext context;
    private final ReferenceQueue<Object> finalizerQueue = new ReferenceQueue<>();
    /** The finalizer Ruby thread, spawned lazily. */
    private DynamicObject finalizerThread;
    /** The head of a doubly-linked list of FinalizerReference, needed to collect finalizer Procs for ObjectSpace. */
    private FinalizerReference first = null;

    public FinalizationService(RubyContext context) {
        this.context = context;
    }

    public synchronized FinalizerReference addFinalizer(Object object, FinalizerReference finalizerReference, Class<?> owner, Runnable action, DynamicObject root) {

        if (finalizerReference == null) {
            finalizerReference = new FinalizerReference(object, finalizerQueue);
            add(finalizerReference);
        }

        finalizerReference.addFinalizer(owner, action, root);

        if (context.getOptions().SINGLE_THREADED) {

            drainFinalizationQueue();

        } else {

            /*
             * We can't create a new thread while the context is initializing or finalizing, as the
             * polyglot API locks on creating new threads, and some core loading does things such as
             * stat files which could allocate memory that is marked to be automatically freed and so
             * would want to start the finalization thread. So don't start the finalization thread if we
             * are initializing. We will rely on some other finalizer to be created to ever free this
             * memory allocated during startup, but that's a reasonable assumption and a low risk of
             * leaking a tiny number of bytes if it doesn't hold.
             */

            if (finalizerThread == null && !context.isPreInitializing() && context.isInitialized() && !context.isFinalizing()) {
                createFinalizationThread();
            }

        }
        return finalizerReference;
    }

    private final void drainFinalizationQueue() {
        while (true) {
            final FinalizerReference finalizerReference = (FinalizerReference) finalizerQueue.poll();

            if (finalizerReference == null) {
                break;
            }

            runFinalizer(finalizerReference);
        }
    }

    private void createFinalizationThread() {
        final ThreadManager threadManager = context.getThreadManager();
        finalizerThread = threadManager.createBootThread("finalizer");
        context.send(finalizerThread, "internal_thread_initialize");

        threadManager.initialize(finalizerThread, null, "finalizer", () -> {
            while (true) {
                final FinalizerReference finalizerReference =
                        (FinalizerReference) threadManager.runUntilResult(null, finalizerQueue::remove);

                runFinalizer(finalizerReference);
            }
        });
    }

    private void runFinalizer(FinalizerReference finalizerReference) {
        remove(finalizerReference);

        try {
            while (!context.isFinalizing()) {
                final Finalizer finalizer;
                synchronized (this) {
                    finalizer = finalizerReference.getFirstFinalizer();
                }
                if (finalizer == null) {
                    break;
                }
                final Runnable action = finalizer.getAction();
                action.run();
            }
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

    public synchronized void collectRoots(Collection<DynamicObject> roots) {
        FinalizerReference finalizerReference = first;
        while (finalizerReference != null) {
            finalizerReference.collectRoots(roots);
            finalizerReference = finalizerReference.next;
        }
    }

    public synchronized FinalizerReference removeFinalizers(Object object, FinalizerReference ref, Class<?> owner) {
        if (ref != null) {
            return ref.removeFinalizers(this, owner);
        } else {
            return null;
        }
    }

    private synchronized void remove(FinalizerReference ref) {
        if (ref.next == ref) {
            // Already removed.
            return;
        }

        if (first == ref) {
            if (ref.next != null) {
                first = ref.next;
            } else {
                // The list becomes empty
                first = null;
            }
        }

        if (ref.next != null) {
            ref.next.prev = ref.prev;
        }
        if (ref.prev != null) {
            ref.prev.next = ref.next;
        }

        // Mark that this ref has been removed.
        ref.next = ref;
        ref.prev = ref;
    }

    private synchronized void add(FinalizerReference newRef) {
        if (first != null) {
            newRef.next = first;
            first.prev = newRef;
        }
        first = newRef;
    }

}
