/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.support;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;
import org.truffleruby.core.basicobject.BasicObjectLayout;

@Layout
public interface IOLayout extends BasicObjectLayout {

    String DESCRIPTOR_IDENTIFIER = "@descriptor";

    DynamicObjectFactory createIOShape(DynamicObject logicalClass,
            DynamicObject metaClass);

    DynamicObject createIO(DynamicObjectFactory factory,
            int descriptor);

    boolean isIO(DynamicObject object);

    int getDescriptor(DynamicObject object);

    void setDescriptor(DynamicObject object, int value);

}
