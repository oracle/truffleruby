/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.module;

import org.truffleruby.core.basicobject.BasicObjectLayout;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.object.dsl.Layout;

@Layout
public interface ModuleLayout extends BasicObjectLayout {

    DynamicObjectFactory createModuleShape(DynamicObject logicalClass,
            DynamicObject metaClass);

    DynamicObject createModule(DynamicObjectFactory factory,
            ModuleFields fields);

    boolean isModule(ObjectType objectType);

    boolean isModule(DynamicObject object);

    boolean isModule(Object object);

    ModuleFields getFields(DynamicObject object);

}
