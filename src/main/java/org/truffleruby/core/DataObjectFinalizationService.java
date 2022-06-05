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
import java.util.concurrent.locks.ReentrantLock;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.cext.DataHolder;
import org.truffleruby.core.MarkingService.ExtensionCallStack;
import org.truffleruby.core.mutex.MutexOperations;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseRootNode;
import org.truffleruby.language.backtrace.InternalRootNode;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.frame.VirtualFrame;

/** C-ext data finalizers are implemented with phantom references and reference queues, and are run in a dedicated Ruby
 * thread. */
public class DataObjectFinalizationService extends ReferenceProcessingService<DataObjectFinalizerReference> {

    // We need a base node here, it should extend ruby base root node and implement internal root node.
    public static class DataObjectFinalizerRootNode extends RubyBaseRootNode implements InternalRootNode {

        @Child private InteropLibrary nullNode;
        @Child private InteropLibrary callNode;
        private final ConditionProfile ownedProfile = ConditionProfile.create();

        public DataObjectFinalizerRootNode(
                RubyLanguage language) {
            super(language, RubyLanguage.EMPTY_FRAME_DESCRIPTOR, null);

            nullNode = InteropLibrary.getFactory().createDispatched(1);
            callNode = InteropLibrary.getFactory().createDispatched(1);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            return execute((DataObjectFinalizerReference) frame.getArguments()[0]);
        }

        public Object execute(DataObjectFinalizerReference ref) {
            if (getContext().getOptions().CEXT_LOCK) {
                final ReentrantLock lock = getContext().getCExtensionsLock();
                boolean owned = ownedProfile.profile(lock.isHeldByCurrentThread());

                if (!owned) {
                    MutexOperations.lockInternal(getContext(), lock, this);
                }
                try {
                    runFinalizer(ref);
                } finally {
                    if (!owned) {
                        MutexOperations.unlockInternal(lock);
                    }
                }
            } else {
                runFinalizer(ref);
            }
            return Nil.get();
        }

        private void runFinalizer(DataObjectFinalizerReference ref) throws Error {
            try {
                if (!getContext().isFinalizing()) {
                    Object data = ref.dataHolder.getPointer();
                    if (!nullNode.isNull(data)) {
                        final ExtensionCallStack stack = getLanguage().getCurrentThread()
                                .getCurrentFiber().extensionCallStack;
                        stack.push(false, stack.getSpecialVariables(), stack.getBlock());
                        try {
                            callNode.execute(ref.callable, data);
                        } finally {
                            stack.pop();
                        }
                    }
                }
            } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                throw CompilerDirectives.shouldNotReachHere("Data holder finalization on invalid object");
            }
        }
    }

    private final CallTarget callTarget;

    public DataObjectFinalizationService(RubyLanguage language, ReferenceQueue<Object> processingQueue) {
        super(processingQueue);
        callTarget = new DataObjectFinalizerRootNode(language).getCallTarget();
    }

    public DataObjectFinalizationService(RubyLanguage language, ReferenceProcessor referenceProcessor) {
        this(language, referenceProcessor.processingQueue);
    }

    public DataObjectFinalizerReference addFinalizer(RubyContext context, Object object, Object callable,
            DataHolder dataHolder) {
        final DataObjectFinalizerReference newRef = createRef(object, callable, dataHolder);

        add(newRef);
        context.getReferenceProcessor().processReferenceQueue(this);

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

    @TruffleBoundary
    protected void processReferenceInternal(RubyContext context, RubyLanguage language,
            DataObjectFinalizerReference ref) {
        callTarget.call(new Object[]{ ref });
    }
}
