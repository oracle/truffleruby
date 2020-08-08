/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import java.util.Collection;
import java.util.Objects;

import org.truffleruby.RubyContext;
import org.truffleruby.core.MarkingService.ExtensionCallStack;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

/** Finalizers are implemented with phantom references and reference queues, and are run in a dedicated Ruby thread. */
public class FinalizationService extends ReferenceProcessingService<FinalizerReference> {

    static class Finalizer {

        private final Class<?> owner;
        private final Runnable action;
        private final RubyDynamicObject root;

        public Finalizer(Class<?> owner, Runnable action, RubyDynamicObject root) {
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

        public RubyDynamicObject getRoot() {
            return root;
        }
    }

    /** The finalizer Ruby thread, spawned lazily. */
    public FinalizationService(RubyContext context, ReferenceProcessor referenceProcessor) {
        super(context, referenceProcessor);
    }

    @TruffleBoundary
    public FinalizerReference addFinalizer(Object object, Class<?> owner, Runnable action, RubyDynamicObject root) {
        final FinalizerReference newRef = new FinalizerReference(object, referenceProcessor.processingQueue, this);
        // No need to synchronize since called on a new private object
        newRef.addFinalizer(owner, action, root);

        add(newRef);
        referenceProcessor.processReferenceQueue(owner);

        return newRef;
    }

    @TruffleBoundary
    public void addAdditionalFinalizer(FinalizerReference existingRef, Object object, Class<?> owner, Runnable action,
            RubyDynamicObject root) {
        Objects.requireNonNull(existingRef);

        assert Thread.holdsLock(object) : "caller must synchronize access to the FinalizerReference";
        existingRef.addFinalizer(owner, action, root);

        referenceProcessor.processReferenceQueue(owner);
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
        ExtensionCallStack stack = context.getMarkingService().getThreadLocalData().getExtensionCallStack();
        stack.push(stack.getBlock());
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
        } finally {
            stack.pop();
        }
    }

    public synchronized void collectRoots(Collection<Object> roots) {
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
