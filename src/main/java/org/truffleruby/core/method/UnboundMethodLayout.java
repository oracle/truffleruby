/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.method;

import org.truffleruby.core.basicobject.BasicObjectLayout;
import org.truffleruby.language.methods.InternalMethod;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;

@Layout
public interface UnboundMethodLayout extends BasicObjectLayout {

    DynamicObjectFactory createUnboundMethodShape(DynamicObject logicalClass,
            DynamicObject metaClass);

    DynamicObject createUnboundMethod(DynamicObjectFactory factory,
            DynamicObject origin,
            InternalMethod method);

    boolean isUnboundMethod(DynamicObject object);

    boolean isUnboundMethod(Object object);

    DynamicObject getOrigin(DynamicObject object);

    InternalMethod getMethod(DynamicObject object);

}
