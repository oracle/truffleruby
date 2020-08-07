/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.basicobject;

import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.interop.RubyObjectType;
import org.truffleruby.interop.messages.RubyObjectMessages;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.object.dsl.Layout;
import com.oracle.truffle.api.object.dsl.Nullable;

@Layout(objectTypeSuperclass = RubyObjectType.class, implicitCastIntToLong = true, dispatch = RubyObjectMessages.class)
public interface BasicObjectLayout {

    DynamicObjectFactory createBasicObjectShape(@Nullable RubyClass logicalClass,
            @Nullable RubyClass metaClass);

    DynamicObject createBasicObject(DynamicObjectFactory factory);

    boolean isBasicObject(ObjectType objectType);

    boolean isBasicObject(Object object);

    RubyClass getLogicalClass(ObjectType objectType);

    RubyClass getLogicalClass(DynamicObject object);

    RubyClass getMetaClass(ObjectType objectType);

    RubyClass getMetaClass(DynamicObject object);

}
