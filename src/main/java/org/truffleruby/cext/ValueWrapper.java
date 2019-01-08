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

import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.TruffleObject;

/**
 * This object represents a VALUE in C which wraps the raw Ruby object. This allows foreign access
 * methods to be set up which convert these value wrappers to native pointers without affecting the
 * semantics of the wrapped objects.
 */
public class ValueWrapper implements TruffleObject {

    private final Object object;
    private long handle;

    public ValueWrapper(Object object, long handle) {
        this.object = object;
        this.handle = handle;
    }

    public ForeignAccess getForeignAccess() {
        return ValueWrapperMessageResolutionForeign.ACCESS;
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
}
