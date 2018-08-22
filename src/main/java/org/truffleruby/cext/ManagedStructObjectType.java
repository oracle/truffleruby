/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.ObjectType;

public class ManagedStructObjectType extends ObjectType {

    public static final ManagedStructLayout MANAGED_STRUCT = ManagedStructLayoutImpl.INSTANCE;

    public static DynamicObject createManagedStruct() {
        return MANAGED_STRUCT.createManagedStruct();
    }

    public static boolean isInstance(TruffleObject receiver) {
        return MANAGED_STRUCT.isManagedStruct(receiver);
    }

    @Override
    @TruffleBoundary
    public String toString(DynamicObject object) {
        final StringBuilder builder = new StringBuilder("ManagedStruct{");
        boolean first = true;

        for (Object key : object.getShape().getKeys()) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append(key).append(": ").append(object.get(key));
        }

        builder.append('}');
        return builder.toString();
    }

    @Override
    public ForeignAccess getForeignAccessFactory(DynamicObject object) {
        return ManagedStructMessageResolutionForeign.ACCESS;
    }

}
