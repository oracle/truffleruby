/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.HiddenKey;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.object.dsl.Layout;
import org.truffleruby.Layouts;
import org.truffleruby.core.basicobject.BasicObjectLayout;
import org.truffleruby.core.rope.Rope;

@Layout
public interface StringLayout extends BasicObjectLayout {

    HiddenKey TAINTED_IDENTIFIER = Layouts.TAINTED_IDENTIFIER;
    HiddenKey FROZEN_IDENTIFIER = Layouts.FROZEN_IDENTIFIER;

    DynamicObjectFactory createStringShape(DynamicObject logicalClass,
            DynamicObject metaClass);

    Object[] build(boolean frozen, boolean tainted, Rope rope);

    boolean isString(ObjectType objectType);

    boolean isString(DynamicObject object);

    boolean isString(Object object);

    boolean getFrozen(DynamicObject object);

    void setFrozen(DynamicObject object, boolean value);

    boolean getTainted(DynamicObject object);

    void setTainted(DynamicObject object, boolean value);

    Rope getRope(DynamicObject object);

    void setRope(DynamicObject object, Rope value);

}
