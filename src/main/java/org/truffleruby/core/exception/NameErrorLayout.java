/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.exception;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;
import com.oracle.truffle.api.object.dsl.Nullable;
import org.truffleruby.language.backtrace.Backtrace;

@Layout
public interface NameErrorLayout extends ExceptionLayout {

    DynamicObjectFactory createNameErrorShape(
        DynamicObject logicalClass,
        DynamicObject metaClass);

    Object[] build(
        @Nullable Object message,
        @Nullable DynamicObject formatter,
        @Nullable Backtrace backtrace,
        DynamicObject cause,
        @Nullable Object receiver,
        Object name);

    boolean isNameError(DynamicObject object);

    Object getName(DynamicObject object);
    void setName(DynamicObject object, Object value);

    Object getReceiver(DynamicObject object);
    void setReceiver(DynamicObject object, Object value);

}
