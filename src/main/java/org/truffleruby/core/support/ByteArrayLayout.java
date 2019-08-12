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

import org.truffleruby.collections.ByteArrayBuilder;
import org.truffleruby.core.basicobject.BasicObjectLayout;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;

@Layout
public interface ByteArrayLayout extends BasicObjectLayout {

    DynamicObjectFactory createByteArrayShape(DynamicObject logicalClass,
            DynamicObject metaClass);

    DynamicObject createByteArray(DynamicObjectFactory factory,
            ByteArrayBuilder bytes);

    boolean isByteArray(DynamicObject object);

    ByteArrayBuilder getBytes(DynamicObject object);

}
