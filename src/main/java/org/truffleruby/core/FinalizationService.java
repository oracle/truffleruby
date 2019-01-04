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
public class FinalizationService extends ReferenceProcessingService<FinalizationService.FinalizerReference> {

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

    public static class FinalizerReference extends PhantomReference<Object> implements ReferenceProcessingService.ProcessingReference<FinalizerReference> {

        /**
         * All accesses to this Deque must be synchronized by taking the
         * {@link FinalizationService} monitor, to avoid concurrent access.
         */
        private final Deque<Finalizer> finalizers = new LinkedList<>();

        /** The doubly-linked list of FinalizerReference, needed to collect finalizer Procs for ObjectSpace. */
        FinalizerReference next = null;
        FinalizerReference prev = null;

        private FinalizerReference(Object object, ReferenceQueue<? super Object> queue) {
            super(object, queue);
        }

        public FinalizerReference getPrevious() {
            return prev;
        }

        public void setPrevious(FinalizerReference previous) {
            prev = previous;
        }

        public FinalizerReference getNext() {
            return next;
        }

        public void setNext(FinalizerReference next) {
            this.next = next;
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

    /** The finalizer Ruby thread, spawned lazily. */
    public FinalizationService(RubyContext context) {
        super(context);
    }

    public synchronized FinalizerReference addFinalizer(Object object, FinalizerReference finalizerReference, Class<?> owner, Runnable action, DynamicObject root) {

        if (finalizerReference == null) {
            finalizerReference = new FinalizerReference(object, processingQueue);
            add(finalizerReference);
        }

        finalizerReference.addFinalizer(owner, action, root);

        processReferenceQueue();
        return finalizerReference;
    }

    @Override
    protected void createProcessingThread() {
        final ThreadManager threadManager = context.getThreadManager();
        processingThread = threadManager.createBootThread(getThreadName());
        context.send(processingThread, "internal_thread_initialize");

        threadManager.initialize(processingThread, null, getThreadName(), () -> {
            while (true) {
                final FinalizerReference finalizerReference =
                        (FinalizerReference) threadManager.runUntilResult(null, processingQueue::remove);

                processReference(finalizerReference);
            }
        });
    }

    @Override
    protected String getThreadName() {
        return "finalizer";
    }

    @Override
    protected void processReference(FinalizerReference finalizerReference) {
        super.processReference(finalizerReference);

        runCatchingErrors(this::processReferenceInternal, finalizerReference);
    }

    protected void processReferenceInternal(FinalizerReference finalizerReference) {
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
    }

    public synchronized void collectRoots(Collection<DynamicObject> roots) {
        FinalizerReference finalizerReference = getFirst();
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

}
