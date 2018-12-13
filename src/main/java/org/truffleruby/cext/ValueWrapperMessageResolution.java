/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.extra.ffi.Pointer;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;

@MessageResolution(receiverType = ValueWrapperObjectType.class)
public class ValueWrapperMessageResolution {

    @Resolve(message = "IS_POINTER")
    public static abstract class ForeignIsPointerNode extends Node {

        protected boolean access(VirtualFrame frame, DynamicObject wrapper) {
            return true;
        }
    }

    @Resolve(message = "TO_NATIVE")
    public static abstract class ForeignToNativeNode extends Node {
        protected Object access(VirtualFrame frame, DynamicObject receiver) {
            return receiver;
        }
    }

    @Resolve(message = "AS_POINTER")
    public static abstract class ForeignAsPointerNode extends Node {

        @CompilationFinal private RubyContext context;

        protected long access(VirtualFrame frame, DynamicObject wrapper) {
            long handle = Layouts.VALUE_WRAPPER.getHandle(wrapper);
            if (handle == ValueWrapperObjectType.UNSET_HANDLE) {
                handle = createHandle(wrapper);
            }
            return handle;
        }

        @TruffleBoundary
        protected long createHandle(DynamicObject wrapper) {
            synchronized (wrapper) {
                Pointer handlePointer = Pointer.malloc(8);
                long handleAddress = handlePointer.getAddress();
                Layouts.VALUE_WRAPPER.setHandle(wrapper, handleAddress);
                getContext().getValueWrapperManager().addToHandleMap(handleAddress, wrapper);
                // Add a finaliser to remove the map entry.
                getContext().getFinalizationService().addFinalizer(
                        wrapper, null, ValueWrapperObjectType.class,
                        finalizer(getContext().getValueWrapperManager(), handlePointer), null);
                return handleAddress;
            }
        }

        private static Runnable finalizer(ValueWrapperManager manager, Pointer handle) {
            return () -> {
                manager.removeFromHandleMap(handle.getAddress());
                handle.free();
            };

        }
        public RubyContext getContext() {
            if (context == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                context = RubyLanguage.getCurrentContext();
            }

            return context;
        }

    }

}
