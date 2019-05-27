/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.language.objects.ObjectGraphNode;

import java.util.Set;

public final class DelegatedArrayStorage implements ObjectGraphNode {

    public final Object storage;
    public final int offset;
    public final int length;

    public DelegatedArrayStorage(Object storage, int offset, int length) {
        assert offset >= 0;
        assert length >= 0;
        this.storage = storage;
        this.offset = offset;
        this.length = length;
    }

    public boolean hasObjectArrayStorage() {
        return storage != null && storage.getClass() == Object[].class;
    }

    @Override
    public void getAdjacentObjects(Set<DynamicObject> reachable) {
        if (hasObjectArrayStorage()) {
            final Object[] objectArray = (Object[]) storage;

            for (int i = offset; i < offset + length; i++) {
                final Object value = objectArray[i];
                if (value instanceof DynamicObject) {
                    reachable.add((DynamicObject) value);
                }
            }
        }
    }

}
