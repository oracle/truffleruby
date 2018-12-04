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

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.dsl.Layout;

@Layout(objectTypeSuperclass = ValueWrapperObjectType.class, implicitCastIntToLong = true)
public interface ValueWrapperLayout {

    DynamicObject createValueWrapper(Object object);

    boolean isValueWrapper(Object object);

    boolean isValueWrapper(DynamicObject object);

    Object getObject(DynamicObject object);
}
