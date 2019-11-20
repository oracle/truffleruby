/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import java.util.Set;

import com.oracle.truffle.api.object.DynamicObject;

import org.truffleruby.cext.UnwrapNodeGen.UnwrapNativeNodeGen;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.objects.ObjectGraphNode;

public final class NativeArrayStorage implements ObjectGraphNode {

    public final Pointer pointer;
    /** Used to keep elements alive */
    private final Object[] markedObjects;
    public final int length;

    public NativeArrayStorage(Pointer pointer, int length) {
        this.pointer = pointer;
        this.length = length;
        this.markedObjects = new Object[length];
    }

    @Override
    public void getAdjacentObjects(Set<DynamicObject> reachable) {
        for (int i = 0; i < length; i++) {
            final Object value = UnwrapNativeNodeGen.getUncached().execute(pointer.readLong(8 * i));
            if (value instanceof DynamicObject) {
                reachable.add((DynamicObject) value);
            }
        }
    }

    public void preserveMembers() {
        for (int i = 0; i < length; i++) {
            final Object value = UnwrapNativeNodeGen.getUncached().execute(pointer.readLong(8 * i));
            markedObjects[i] = value;
        }
    }
}
