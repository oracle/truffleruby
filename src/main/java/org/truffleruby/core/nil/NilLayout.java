/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.nil;

import org.truffleruby.core.basicobject.BasicObjectLayout;
import org.truffleruby.interop.messages.NilMessages;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;

@Layout(dispatch = NilMessages.class)
public interface NilLayout extends BasicObjectLayout {

    DynamicObjectFactory createNilShape(DynamicObject logicalClass, DynamicObject metaClass);

    DynamicObject createNil(DynamicObjectFactory factory);
}
