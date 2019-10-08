/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.dsl.Layout;

@Layout(objectTypeSuperclass = ManagedStructObjectType.class, implicitCastIntToLong = true)
public interface ManagedStructLayout {

    DynamicObject createManagedStruct(Object type);

    boolean isManagedStruct(Object object);

    boolean isManagedStruct(DynamicObject object);

    Object getType(DynamicObject object);

}
