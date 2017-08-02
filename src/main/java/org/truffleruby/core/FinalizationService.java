/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core;

import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.RubyContext;
import org.truffleruby.core.thread.ThreadManager;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FinalizationService {

    private static class Finalizer {

        private final Class<?> owner;
        private final Runnable action;
        private final List<DynamicObject> roots;

        public Finalizer(Class<?> owner, Runnable action, List<DynamicObject> roots) {
            this.owner = owner;
            this.action = action;
            this.roots = roots;
        }

        public Class<?> getOwner() {
            return owner;
        }

        public Runnable getAction() {
            return action;
        }

        public Stream<DynamicObject> getRoots() {
            return roots.stream();
        }
    }

    private static class FinalizerReference extends WeakReference<Object> {

        public List<Finalizer> finalizers = new LinkedList<>();

        public FinalizerReference(Object object, ReferenceQueue<? super Object> queue) {
            super(object, queue);
        }

        public void addFinalizer(Class<?> owner, Runnable action, List<DynamicObject> roots) {
            finalizers.add(new Finalizer(owner, action, roots));
        }

        public void removeFinalizers(Class<?> owner) {
            finalizers.removeIf(f -> f.getOwner() == owner);
        }

        public List<Runnable> getFinalizerActions() {
            return finalizers.stream().map(f -> f.getAction()).collect(Collectors.toList());
        }

        public Stream<DynamicObject> getRoots() {
            return finalizers.stream().flatMap(f -> f.getRoots());
        }
    }

    private final RubyContext context;

    private final Map<Object, FinalizerReference> finalizerReferences = new WeakHashMap<>();
    private final ReferenceQueue<Object> finalizerQueue = new ReferenceQueue<>();

    private DynamicObject finalizerThread;

    public FinalizationService(RubyContext context) {
        this.context = context;
    }

    public synchronized void addFinalizer(Object object, Class<?> owner, Runnable action) {
        addFinalizer(object, owner, action, Collections.emptyList());
    }

    public synchronized void addFinalizer(Object object, Class<?> owner, Runnable action, List<DynamicObject> roots) {
        FinalizerReference finalizerReference = finalizerReferences.get(object);

        if (finalizerReference == null) {
            finalizerReference = new FinalizerReference(object, finalizerQueue);
            finalizerReferences.put(object, finalizerReference);
        }

        finalizerReference.addFinalizer(owner, action, roots);

        if (finalizerThread == null) {
            createFinalizationThread();
        }
    }

    private void createFinalizationThread() {
        finalizerThread = context.getThreadManager().createRubyThread("finalizer");
        context.send(finalizerThread, "internal_thread_initialize", null);

        ThreadManager.initialize(finalizerThread, context, null, "finalizer", () -> {
            while (true) {
                final FinalizerReference finalizerReference = (FinalizerReference) context.getThreadManager().runUntilResult(null,
                        () -> finalizerQueue.remove());
                finalizerReference.getFinalizerActions().forEach(action -> action.run());
            }
        });
    }

    public synchronized void removeFinalizers(Object object, Class<?> owner) {
        final FinalizerReference finalizerReference = finalizerReferences.get(object);

        if (finalizerReference != null) {
            finalizerReference.removeFinalizers(owner);
        }
    }

    public synchronized Stream<DynamicObject> getRoots() {
        return finalizerReferences.values().stream().flatMap(f -> f.getRoots());
    }

}
