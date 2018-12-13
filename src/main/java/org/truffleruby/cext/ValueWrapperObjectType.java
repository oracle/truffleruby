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

import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;

public class ValueWrapperObjectType extends ObjectType {

    public static DynamicObject createValueWrapper(Object value) {
        return Layouts.VALUE_WRAPPER.createValueWrapper(value, ValueWrapperManager.UNSET_HANDLE);
    }

    public static boolean isInstance(TruffleObject receiver) {
        return Layouts.VALUE_WRAPPER.isValueWrapper(receiver);
    }

    @Override
    public boolean equals(DynamicObject object, Object other) {
        if (!(other instanceof DynamicObject) || Layouts.VALUE_WRAPPER.isValueWrapper(other)) {
            return false;
        }
        DynamicObject otherWrapper = (DynamicObject) other;

        final long objectHandle = Layouts.VALUE_WRAPPER.getHandle(object);
        final long otherHandle = Layouts.VALUE_WRAPPER.getHandle(otherWrapper);
        if (objectHandle != ValueWrapperManager.UNSET_HANDLE &&
            otherHandle != ValueWrapperManager.UNSET_HANDLE) {
            return objectHandle == otherHandle;
        }
        return Layouts.VALUE_WRAPPER.getObject(object).equals(Layouts.VALUE_WRAPPER.getObject(otherWrapper));
    }

    @Override
    public ForeignAccess getForeignAccessFactory(DynamicObject object) {
        return ValueWrapperMessageResolutionForeign.ACCESS;
    }
}
