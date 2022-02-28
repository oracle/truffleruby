/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Field;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import sun.misc.Unsafe;

/** Finalizers are implemented with phantom references and reference queues, and are run in a dedicated Ruby thread. */
public class PointerFinalizationService extends ReferenceProcessingService<PointerFinalizerReference> {

    public PointerFinalizationService(ReferenceQueue<Object> processingQueue) {
        super(processingQueue);
    }

    public PointerFinalizationService(ReferenceProcessor referenceProcessor) {
        this(referenceProcessor.processingQueue);
    }

    public PointerFinalizerReference addFinalizer(RubyContext context, Object object, long address) {
        final PointerFinalizerReference newRef = createRef(object, address);

        add(newRef);
        context.getReferenceProcessor().processReferenceQueue();

        return newRef;
    }

    @TruffleBoundary
    public PointerFinalizerReference createRef(Object object, long address) {
        return new PointerFinalizerReference(object, processingQueue, this, address);
    }

    public final void drainFinalizationQueue(RubyContext context) {
        context.getReferenceProcessor().drainReferenceQueues();
    }

    @Override
    protected void processReference(RubyContext context, RubyLanguage language,
            ProcessingReference<?> finalizerReference) {
        super.processReference(context, language, finalizerReference);

        runCatchingErrors(context, language, this::processReferenceInternal,
                (PointerFinalizerReference) finalizerReference);
    }

    protected void processReferenceInternal(RubyContext context, RubyLanguage language,
            PointerFinalizerReference ref) {
        long address = ref.address;
        if (address == 0) {
            UNSAFE.freeMemory(address);
        }
    }

    @SuppressWarnings("restriction")
    private static Unsafe getUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new Error(e);
        }
    }

    private static final Unsafe UNSAFE = getUnsafe();

}
