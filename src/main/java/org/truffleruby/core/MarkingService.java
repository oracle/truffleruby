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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;

import org.truffleruby.RubyContext;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;

/**
 * Class to provide GC marking and other facilities to keep objects alive for native extensions.
 *
 * Native extensions expect objects on the stack to be kept alive even when they have been stored in
 * native structures on the stack (e.g. pg keeps the VALUE of a ruby array in a structure on the
 * stack, and places other objects in that array to keep them alive). They also expect structs in
 * objects with custom mark functions to keep marked objects alive.
 *
 * Since we are not running on a VM that allows us to add custom mark functions to our garbage
 * collector we keep objects alive in 2 ways. Any object converted to a native handle can be kept
 * alive by calling {@link #keepObject(Object)}. This will add the object to two lists, a list of
 * all objects converted to native during this call to a C extension function which will be popped
 * when the we return to Ruby code, and a fixed sized list of objects converted to native handles.
 * When the latter of these two lists is full all mark functions will be run.
 *
 * Marker references only keep a week reference to their owning object to ensure they don't
 * themselves stop the object from being garbage collected.
 *
 */
public class MarkingService extends ReferenceProcessingService<MarkingService.MarkerReference> {

    public static interface MarkerAction {
        public abstract void mark(DynamicObject owner);
    }

    public static class MarkerReference extends WeakReference<DynamicObject> implements ReferenceProcessingService.ProcessingReference<MarkerReference> {

        private final MarkerAction action;
        private final MarkingService service;
        private MarkerReference next = null;
        private MarkerReference prev = null;

        private MarkerReference(DynamicObject object, ReferenceQueue<? super Object> queue, MarkerAction action, MarkingService service) {
            super(object, queue);
            this.action = action;
            this.service = service;
        }

        public MarkerReference getPrevious() {
            return prev;
        }

        public void setPrevious(MarkerReference previous) {
            prev = previous;
        }

        public MarkerReference getNext() {
            return next;
        }

        public void setNext(MarkerReference next) {
            this.next = next;
        }

        public ReferenceProcessingService<MarkerReference> service() {
            return service;
        }
    }

    private final int cacheSize;

    private final ThreadLocal<MarkerStack> stackPreservation = ThreadLocal.withInitial(() -> new MarkerStack());

    private Object[] keptObjects;
    private final ArrayDeque<Object[]> oldKeptObjects = new ArrayDeque<>();

    private int counter = 0;

    protected class MarkerStackEntry {
        protected final MarkerStackEntry previous;
        protected final ArrayList<Object> entries = new ArrayList<>();

        protected MarkerStackEntry(MarkerStackEntry previous) {
            this.previous = previous;
        }
    }

    public class MarkerStack {
        protected MarkerStackEntry current = new MarkerStackEntry(null);

        public ArrayList<Object> get() {
            return current.entries;
        }

        public void pop() {
            current = current.previous;
        }

        public void push() {
            current = new MarkerStackEntry(current);
        }
    }

    @TruffleBoundary
    public MarkerStack getStackFromThreadLocal() {
        return stackPreservation.get();
    }

    public MarkingService(RubyContext context, ReferenceProcessor referenceProcessor) {
        super(context, referenceProcessor);
        cacheSize = context.getOptions().CEXTS_MARKING_CACHE;
        keptObjects = new Object[cacheSize];
    }

    synchronized void addToKeptObjects(Object object) {
        /*
         * It is important to get the ordering of events correct to avoid references being garbage
         * collected too soon. If we are attempting to add a handle to a native structure then that
         * consists of two events. First we create the handle, and then the handle is stored in the
         * struct. If we run mark functions immediate after adding the handle to the list of kept
         * objects then the mark function will be run before that handle is stored in the structure,
         * and since it will be removed from the list of kept objects it could be collected before
         * the mark functions are run again.
         *
         * Instead we check for the kept list being full before adding an object to it, as those
         * handles are already stored in structs by this point.
         */
        if (counter == cacheSize) {
            runAllMarkers();
        }
        keptObjects[counter++] = object;
    }

    @TruffleBoundary
    public synchronized void runAllMarkers() {
        counter = 0;
        oldKeptObjects.push(keptObjects);
        try {
            keptObjects = new Object[cacheSize];
            MarkerReference currentMarker = getFirst();
            MarkerReference nextMarker;
            while (currentMarker != null) {
                nextMarker = currentMarker.next;
                runMarker(currentMarker);
                if (nextMarker == currentMarker) {
                    throw new Error("The MarkerReference linked list structure has become broken.");
                }
                currentMarker = nextMarker;
            }
        } finally {
            oldKeptObjects.pop();
        }
    }

    public void addMarker(DynamicObject object, MarkerAction action) {
        add(new MarkerReference(object, referenceProcessor.processingQueue, action, this));
    }

    private void runMarker(MarkerReference markerReference) {
        runCatchingErrors(this::runMarkerInternal, markerReference);
    }

    private void runMarkerInternal(MarkerReference markerReference) {
        if (!context.isFinalizing()) {
            DynamicObject owner = markerReference.get();
            if (owner != null) {
                final MarkerAction action = markerReference.action;
                action.mark(owner);
            }
        }
    }
}
