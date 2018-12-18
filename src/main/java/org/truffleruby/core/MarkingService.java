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
import java.util.Deque;

import org.truffleruby.RubyContext;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.TerminationException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;

/**
 * Class to provide GC marking and other facilities to keep objects alive for native extensions.
 *
 * Native extensions expect object on the stack to be kept alive even when they have been stored in
 * native structures on the stack. They also expect structs in objects with custom mark functions
 * keep marked objects alive.
 *
 * Since we are not running on a VM that allows us to add custom ark functions to our garbage
 * collector we keep objects alive in 2 ways. Any object converted to a native handle can be kept
 * alive by calling keepAlive(). This will add the object to two lists, a list of all objects
 * converted to native during in this call to a C extension which will be popped when the we return
 * to Ruby code, and a fixed sized list of objects converted to native handles. When the latter of
 * these two lists is full all mark functions will be run.
 *
 * Markers only keep a week reference to their owning object to ensure they don't themselves stop
 * the object from being garbage collected.
 *
 */
public class MarkingService extends ReferenceProcessingService<MarkingService.MarkerReference> {

    public static interface MarkerAction {
        public abstract void mark(DynamicObject owner);
    }

    public static class MarkerReference extends WeakReference<DynamicObject> implements ReferenceProcessingService.ProcessingReference<MarkerReference> {

        private final MarkerAction action;
        private MarkerReference next = null;
        private MarkerReference prev = null;

        private MarkerReference(DynamicObject object, ReferenceQueue<? super Object> queue, MarkerAction action) {
            super(object, queue);
            this.action = action;
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
    }

    private static final int KEPT_COUNT_SIZE = 10_000;

    private final ThreadLocal<Deque<ArrayList<Object>>> stackPreservation = ThreadLocal.withInitial(() -> new ArrayDeque<>());

    private Object[] keptObjects = new Object[KEPT_COUNT_SIZE];

    private int counter = 0;

    public MarkingService(RubyContext context) {
        super(context);
    }

    public void keepObject(Object object) {
        Object[] oldKeptObjects = addToKeptObjects(object);
        if (oldKeptObjects != null) {
            runAllMarkers();
        }
    }

    private synchronized Object[] addToKeptObjects(Object object) {
        final ArrayList<Object> keepList = stackPreservation.get().peekFirst();
        if (keepList != null) {
            keepList.add(object);
        }
        keptObjects[counter++] = object;
        if (counter == KEPT_COUNT_SIZE) {
            counter = 0;
            Object[] tmp = keptObjects;
            keptObjects = new Object[KEPT_COUNT_SIZE];
            return tmp;
        }
        return null;
    }

    @TruffleBoundary
    public void pushStackPreservationFrame() {
        stackPreservation.get().push(new ArrayList<>());
    }

    @TruffleBoundary
    public void popStackPreservationFrame() {
        stackPreservation.get().pop();
    }

    private synchronized void runAllMarkers() {
        MarkerReference currentMarker = getFirst();
        MarkerReference nextMarker;
        while (currentMarker != null) {
            nextMarker = currentMarker.next;
            runMarker(currentMarker);
            if (nextMarker == currentMarker) {
                throw new Error("Something went badly wrong.");
            }
            currentMarker = nextMarker;
        }
    }

    public void addMarker(DynamicObject object, MarkerAction action) {
        add(new MarkerReference(object, processingQueue, action));
        processReferenceQueue();
    }

    private void runMarker(MarkerReference markerReference) {
        try {
            if (!context.isFinalizing()) {
                DynamicObject owner = markerReference.get();
                if (owner != null) {
                    final MarkerAction action = markerReference.action;
                    action.mark(owner);
                }
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

    @Override
    protected String getThreadName() {
        return "marker-finalizer";
    }
}
