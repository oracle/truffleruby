/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import java.util.Collection;

import org.truffleruby.RubyContext;

import com.oracle.truffle.api.object.DynamicObject;

/**
 * Finalizers are implemented with phantom references and reference queues, and are run in a
 * dedicated Ruby thread.
 */
public class FinalizationService extends ReferenceProcessingService<FinalizerReference> {

    static class Finalizer {

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

    /** The finalizer Ruby thread, spawned lazily. */
    public FinalizationService(RubyContext context, ReferenceProcessor referenceProcessor) {
        super(context, referenceProcessor);
    }

    public synchronized FinalizerReference addFinalizer(Object object, FinalizerReference finalizerReference, Class<?> owner, Runnable action, DynamicObject root) {
        if (finalizerReference == null) {
            finalizerReference = new FinalizerReference(object, referenceProcessor.processingQueue, this);
            add(finalizerReference);
        }

        finalizerReference.addFinalizer(owner, action, root);

        referenceProcessor.processReferenceQueue(owner);
        return finalizerReference;
    }

    public final void drainFinalizationQueue() {
        referenceProcessor.drainReferenceQueue();
    }

    @Override
    protected void processReference(ProcessingReference<?> finalizerReference) {
        super.processReference(finalizerReference);

        runCatchingErrors(this::processReferenceInternal, (FinalizerReference) finalizerReference);
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
            finalizerReference = finalizerReference.getNext();
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
