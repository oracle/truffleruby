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
import org.truffleruby.cext.DataHolder;
import org.truffleruby.core.MarkingService.ExtensionCallStack;
import org.truffleruby.language.RubyBaseRootNode;
import org.truffleruby.language.backtrace.InternalRootNode;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;

/** Finalizers are implemented with phantom references and reference queues, and are run in a dedicated Ruby thread. */
public class DataObjectFinalizationService extends ReferenceProcessingService<DataObjectFinalizerReference> {

    // We need a base node here, it shoudl extend ruby base root node and implement internal root node.
    public static class DataObjectFinalizerRootNode extends RubyBaseRootNode implements InternalRootNode {

        private static final FrameDescriptor FINALIZER_FRAME = FrameDescriptor.newBuilder().build();

        @Child private InteropLibrary nullNode;
        @Child private InteropLibrary callNode;

        public DataObjectFinalizerRootNode(
                TruffleLanguage<?> language) {
            super(language, FINALIZER_FRAME, null);

            nullNode = insert(InteropLibrary.getFactory().createDispatched(2));
            callNode = insert(InteropLibrary.getFactory().createDispatched(2));
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return null;
        }

        public Object execute(DataObjectFinalizerReference ref) {
            try {
                if (!getContext().isFinalizing()) {
                    Object data = ref.dataHolder.getAddress();
                    if (!nullNode.isNull(data)) {
                        callNode.execute(ref.callable, data);
                    }
                }
            } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                throw new Error(e);
            }
            return null;
        }
    }

    private final DataObjectFinalizerRootNode rootNode;

    public DataObjectFinalizationService(ReferenceQueue<Object> processingQueue) {
        super(processingQueue);
        rootNode = new DataObjectFinalizerRootNode(RubyLanguage.getCurrentLanguage());
    }

    public DataObjectFinalizationService(ReferenceProcessor referenceProcessor) {
        this(referenceProcessor.processingQueue);
    }

    public DataObjectFinalizerReference addFinalizer(RubyContext context, Object object, Object callable,
            DataHolder dataHolder) {
        final DataObjectFinalizerReference newRef = createRef(object, callable, dataHolder);

        add(newRef);
        context.getReferenceProcessor().processReferenceQueue();

        return newRef;
    }

    public DataObjectFinalizerReference createRef(Object object, Object callable, DataHolder dataHolder) {
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
            rootNode.execute(ref);
        } finally {
            stack.pop();
        }
    }
}
