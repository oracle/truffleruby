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

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.MarkingService.ExtensionCallStack;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;

/** Finalizers are implemented with phantom references and reference queues, and are run in a dedicated Ruby thread. */
public class DataObjectFinalizationService extends ReferenceProcessingService<DataObjectFinalizerReference> {

    private final InteropLibrary nullNode = InteropLibrary.getFactory().createDispatched(2);
    private final DispatchNode dataNode = DispatchNode.create();
    private final InteropLibrary callNode = InteropLibrary.getFactory().createDispatched(2);

    public DataObjectFinalizationService(ReferenceQueue<Object> processingQueue) {
        super(processingQueue);
    }

    public DataObjectFinalizationService(ReferenceProcessor referenceProcessor) {
        this(referenceProcessor.processingQueue);
    }

    public DataObjectFinalizerReference addFinalizer(RubyContext context, Object object, Object callable,
            Object dataHolder) {
        final DataObjectFinalizerReference newRef = createRef(object, callable, dataHolder);

        add(newRef);
        context.getReferenceProcessor().processReferenceQueue();

        return newRef;
    }

    public DataObjectFinalizerReference createRef(Object object, Object callable, Object dataHolder) {
        return new DataObjectFinalizerReference(object, processingQueue, this, callable, dataHolder);
    }

    public final void drainFinalizationQueue(RubyContext context) {
        context.getReferenceProcessor().drainReferenceQueues();
    }

    @Override
    protected void processReference(RubyContext context, RubyLanguage language,
            ProcessingReference<?> finalizerReference) {
        super.processReference(context, language, finalizerReference);

        runCatchingErrors(context, language, this::processReferenceInternal,
                (DataObjectFinalizerReference) finalizerReference);
    }

    protected void processReferenceInternal(RubyContext context, RubyLanguage language,
            DataObjectFinalizerReference ref) {
        final ExtensionCallStack stack = language.getCurrentThread().getCurrentFiber().extensionCallStack;
        stack.push(stack.getSpecialVariables(), stack.getBlock());
        try {
            if (!context.isFinalizing()) {
                Object data = dataNode.call(ref.dataHolder, "data");
                if (!nullNode.isNull(data)) {
                    callNode.execute(ref.callable, data);
                }
            }
        } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
            throw new Error(e);
        } finally {
            stack.pop();
        }
    }
}
