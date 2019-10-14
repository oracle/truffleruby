/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.cext.ValueWrapperManager.AllocateHandleNode;
import org.truffleruby.core.MarkingServiceNodes.KeepAliveNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.interop.InteropLibrary;
import org.truffleruby.cext.ValueWrapperManager.HandleBlock;

import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.profiles.BranchProfile;

/**
 * This object represents a VALUE in C which wraps the raw Ruby object. This allows foreign access
 * methods to be set up which convert these value wrappers to native pointers without affecting the
 * semantics of the wrapped objects.
 */
@ExportLibrary(InteropLibrary.class)
public class ValueWrapper implements TruffleObject {

    private final Object object;
    volatile private long handle;
    @SuppressWarnings("unused")
    // The handleBlock is held here to stop it being GCed and the memory freed while wrappers still
    // exist with handles in it.
    volatile private HandleBlock handleBlock;

    public ValueWrapper(Object object, long handle, HandleBlock handleBlock) {
        this.object = object;
        this.handle = handle;
        this.handleBlock = handleBlock;
    }

    public Object getObject() {
        return object;
    }

    public long getHandle() {
        return handle;
    }

    public void setHandle(long handle, HandleBlock handleBlock) {
        this.handle = handle;
        this.handleBlock = handleBlock;
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
            @Cached KeepAliveNode keepAliveNode,
            @Cached AllocateHandleNode createNativeHandleNode,
            @Cached BranchProfile createHandleProfile,
            @Cached BranchProfile taggedObjBranchProfile) {

        long handle = wrapper.getHandle();
        if (handle == ValueWrapperManager.UNSET_HANDLE) {
            createHandleProfile.enter();
            handle = createNativeHandleNode.execute(wrapper);
        }
        if (ValueWrapperManager.isTaggedObject(handle)) {
            taggedObjBranchProfile.enter();
            keepAliveNode.execute(wrapper);
        }
        return handle;
    }
}
