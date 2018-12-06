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

public class MarkingService {

    public static interface MarkerAction {
        public abstract void mark(DynamicObject owner);
    }

    public static class MarkerReference extends WeakReference<DynamicObject> {

        /**
         * All accesses to this Deque must be synchronized by taking the {@link FinalizationService}
         * monitor, to avoid concurrent access.
         */
        private final MarkerAction action;
        private MarkerReference next = null;
        private MarkerReference prev = null;

        private MarkerReference(DynamicObject object, ReferenceQueue<? super Object> queue, MarkerAction action) {
            super(object, queue);
            this.action = action;
        }
    }

    private static final int KEPT_COUNT_SIZE = 10_000;

    private final ThreadLocal<Deque<ArrayList<Object>>> stackPreservation = ThreadLocal.withInitial(() -> new ArrayDeque<>());

    private final RubyContext context;

    private final ReferenceQueue<Object> finalizerQueue = new ReferenceQueue<>();

    private DynamicObject finalizerThread;

    private Object[] keptObjects = new Object[KEPT_COUNT_SIZE];

    private int counter = 0;

    private MarkerReference first = null;

    public MarkingService(RubyContext context) {
        this.context = context;
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
        MarkerReference currentMarker = first;
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
        add(new MarkerReference(object, finalizerQueue, action));
        if (context.getOptions().SINGLE_THREADED) {

            drainFinalizationQueue();

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

            if (finalizerThread == null && !context.isPreInitializing() && context.isInitialized() && !context.isFinalizing()) {
                createFinalizationThread();
            }

        }
    }

    private void createFinalizationThread() {
        final ThreadManager threadManager = context.getThreadManager();
        finalizerThread = threadManager.createBootThread("marker-finalizer");
        context.send(finalizerThread, "internal_thread_initialize");

        threadManager.initialize(finalizerThread, null, "marker-finalizer", () -> {
            while (true) {
                final MarkerReference markerReference = (MarkerReference) threadManager.runUntilResult(null,
                        finalizerQueue::remove);
                remove(markerReference);
            }
        });
    }

    private void drainFinalizationQueue() {
        while (true) {
            final MarkerReference markerReference = (MarkerReference) finalizerQueue.poll();

            if (markerReference == null) {
                break;
            }

            remove(markerReference);
        }
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

    synchronized void remove(MarkerReference ref) {
        if (ref.next == ref) {
            // Already removed.
            return;
        }

        if (first == ref) {
            if (ref.next != null) {
                first = ref.next;
            } else {
                first = ref.prev;
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

    synchronized void add(MarkerReference newRef) {
        if (first != null) {
            newRef.next = first;
            first.prev = newRef;
        }
        first = newRef;
    }

}
