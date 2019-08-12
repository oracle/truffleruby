/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.symbol;

import org.truffleruby.core.basicobject.BasicObjectLayout;
import org.truffleruby.core.rope.Rope;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;

@Layout
public interface SymbolLayout extends BasicObjectLayout {

    DynamicObjectFactory createSymbolShape(
            DynamicObject logicalClass,
            DynamicObject metaClass);

    DynamicObject createSymbol(
            DynamicObjectFactory factory,
            String string,
            Rope rope,
            long hashCode);

    boolean isSymbol(Object object);

    boolean isSymbol(DynamicObject object);

    String getString(DynamicObject object);

    Rope getRope(DynamicObject object);

    long getHashCode(DynamicObject object);

    void setHashCode(DynamicObject object, long value);

}
