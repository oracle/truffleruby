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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.MarkingServiceNodes;

/**
 * This object represents a VALUE in C which wraps the raw Ruby object. This allows foreign access
 * methods to be set up which convert these value wrappers to native pointers without affecting the
 * semantics of the wrapped objects.
 */
@ExportLibrary(InteropLibrary.class)
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

    @ExportMessage
    public boolean isPointer() {
        return true;
    }

    @ExportMessage
    public void toNative() {
    }

    @ExportMessage
    public static long asPointer(
            ValueWrapper wrapper,
            @CachedContext(RubyLanguage.class) RubyContext context,
            @Cached MarkingServiceNodes.KeepAliveNode keepAliveNode,
            @Cached BranchProfile createHandleProfile,
            @Cached BranchProfile taggedObjBranchProfile) {

        long handle = wrapper.getHandle();
        if (handle == ValueWrapperManager.UNSET_HANDLE) {
            createHandleProfile.enter();
            handle = context.getValueWrapperManager().createNativeHandle(wrapper);
        }
        if (ValueWrapperManager.isTaggedObject(handle)) {
            taggedObjBranchProfile.enter();
            keepAliveNode.execute(wrapper);
        }
        return handle;
    }
}
