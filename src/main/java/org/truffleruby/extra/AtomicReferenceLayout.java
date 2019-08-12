/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra;

import org.truffleruby.core.basicobject.BasicObjectLayout;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;
import com.oracle.truffle.api.object.dsl.Volatile;

@Layout
public interface AtomicReferenceLayout extends BasicObjectLayout {

    DynamicObjectFactory createAtomicReferenceShape(DynamicObject logicalClass,
            DynamicObject metaClass);

    DynamicObject createAtomicReference(DynamicObjectFactory factory,
            @Volatile Object value);

    Object getValue(DynamicObject object);

    void setValue(DynamicObject object, Object value);

    boolean compareAndSetValue(DynamicObject object, Object expectedValue, Object value);

    Object getAndSetValue(DynamicObject object, Object value);

}
