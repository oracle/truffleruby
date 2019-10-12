/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.regexp;

import org.joni.Region;
import org.truffleruby.core.basicobject.BasicObjectLayout;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;
import com.oracle.truffle.api.object.dsl.Nullable;

@Layout
public interface MatchDataLayout extends BasicObjectLayout {

    DynamicObjectFactory createMatchDataShape(DynamicObject logicalClass,
            DynamicObject metaClass);

    Object[] build(DynamicObject source, DynamicObject regexp, Region region, @Nullable Region charOffsets);

    boolean isMatchData(DynamicObject object);

    boolean isMatchData(Object object);

    DynamicObject getSource(DynamicObject object);

    DynamicObject getRegexp(DynamicObject object);

    Region getRegion(DynamicObject object);

    Region getCharOffsets(DynamicObject object);

    void setCharOffsets(DynamicObject object, Region value);

}
