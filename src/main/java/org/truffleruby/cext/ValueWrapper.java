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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.CanResolve;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.MessageResolution;
import com.oracle.truffle.api.interop.Resolve;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;

/**
 * This object represents a VALUE in C which wraps the raw Ruby object. This allows foreign access
 * methods to be set up which convert these value wrappers to native pointers without affecting the
 * semantics of the wrapped objects.
 */
@MessageResolution(receiverType = ValueWrapper.class)
public class ValueWrapper implements TruffleObject {

    private final Object object;
    private long handle;

    public ValueWrapper(Object object, long handle) {
        this.object = object;
        this.handle = handle;
    }

    public Object getObject() {
        return object;
    }

    public long getHandle() {
        return handle;
    }

    public void setHandle(long handle) {
        this.handle = handle;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ValueWrapper)) {
            return false;
        }
        ValueWrapper otherWrapper = (ValueWrapper) other;
        if (handle != ValueWrapperManager.UNSET_HANDLE && otherWrapper.handle != ValueWrapperManager.UNSET_HANDLE) {
            return handle == otherWrapper.handle;
        }
        return (this.object.equals(otherWrapper.object));
    }

    @Override
    public int hashCode() {
        return this.object.hashCode();
    }


    @CanResolve
    public abstract static class IsInstance extends Node {

        protected boolean test(TruffleObject receiver) {
            return receiver instanceof ValueWrapper;
        }
    }

    @Resolve(message = "IS_POINTER")
    public static abstract class IsPointerNode extends Node {

        protected boolean access(VirtualFrame frame, ValueWrapper wrapper) {
            return true;
        }
    }

    @Resolve(message = "TO_NATIVE")
    public static abstract class ToNativeNode extends Node {
        protected Object access(VirtualFrame frame, ValueWrapper receiver) {
            return receiver;
        }
    }

    @Resolve(message = "AS_POINTER")
    public static abstract class AsPointerNode extends Node {

        @CompilationFinal private RubyContext context;
        private final BranchProfile createHandleProfile = BranchProfile.create();

        protected long access(VirtualFrame frame, ValueWrapper wrapper) {
            long handle = wrapper.getHandle();
            if (handle == ValueWrapperManager.UNSET_HANDLE) {
                createHandleProfile.enter();
                handle = getContext().getValueWrapperManager().createNativeHandle(wrapper);
            }
            return handle;
        }

        public RubyContext getContext() {
            if (context == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                context = RubyLanguage.getCurrentContext();
            }

            return context;
        }

    }

    @Override
    public ForeignAccess getForeignAccess() {
        return ValueWrapperForeign.ACCESS;
    }

}
