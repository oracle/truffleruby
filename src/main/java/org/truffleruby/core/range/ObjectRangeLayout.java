/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.range;

import org.truffleruby.core.basicobject.BasicObjectLayout;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;
import com.oracle.truffle.api.object.dsl.Nullable;

@Layout
public interface ObjectRangeLayout extends BasicObjectLayout {

    DynamicObjectFactory createObjectRangeShape(DynamicObject logicalClass,
            DynamicObject metaClass);

    DynamicObject createObjectRange(DynamicObjectFactory factory,
            boolean excludedEnd,
            @Nullable Object begin,
            @Nullable Object end);

    boolean isObjectRange(Object object);

    boolean isObjectRange(DynamicObject object);

    boolean getExcludedEnd(DynamicObject object);

    void setExcludedEnd(DynamicObject object, boolean value);

    Object getBegin(DynamicObject object);

    void setBegin(DynamicObject object, Object value);

    Object getEnd(DynamicObject object);

    void setEnd(DynamicObject object, Object value);

}
