/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.bigdecimal;

import java.math.BigDecimal;

import org.truffleruby.core.basicobject.BasicObjectLayout;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;

@Layout
public interface BigDecimalLayout extends BasicObjectLayout {

    DynamicObjectFactory createBigDecimalShape(
            DynamicObject logicalClass,
            DynamicObject metaClass);

    Object[] build(BigDecimal value, BigDecimalType type);

    boolean isBigDecimal(Object object);

    boolean isBigDecimal(DynamicObject object);

    BigDecimal getValue(DynamicObject object);

    BigDecimalType getType(DynamicObject object);

}
