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

/**
 * This layout represents a VALUE in C which wraps the raw Ruby object. This allows foreign access
 * methods to be set up which convert these value wrappers to native pointers without affecting the
 * semantics of the wrapped objects.
 */
@Layout(objectTypeSuperclass = ValueWrapperObjectType.class)
public interface ValueWrapperLayout {

    DynamicObject createValueWrapper(Object object,
            long handle);

    boolean isValueWrapper(Object object);

    boolean isValueWrapper(DynamicObject object);

    Object getObject(DynamicObject object);

    long getHandle(DynamicObject object);

    void setHandle(DynamicObject object, long value);
}
