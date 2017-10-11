/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.regexp;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;
import com.oracle.truffle.api.object.dsl.Nullable;

import java.util.HashMap;

import org.jcodings.Encoding;
import org.joni.Regex;
import org.truffleruby.core.basicobject.BasicObjectLayout;
import org.truffleruby.core.rope.Rope;

@Layout
public interface RegexpLayout extends BasicObjectLayout {

    DynamicObjectFactory createRegexpShape(DynamicObject logicalClass,
                                           DynamicObject metaClass);

    DynamicObject createRegexp(DynamicObjectFactory factory,
                               @Nullable Regex regex,
                               @Nullable Rope source,
                               RegexpOptions options,
                               @Nullable Object cachedEncodings);

    boolean isRegexp(DynamicObject object);
    boolean isRegexp(Object object);

    Regex getRegex(DynamicObject object);
    void setRegex(DynamicObject object, Regex value);

    Rope getSource(DynamicObject object);
    void setSource(DynamicObject object, Rope value);

    RegexpOptions getOptions(DynamicObject object);
    void setOptions(DynamicObject object, RegexpOptions value);

    Object getCachedEncodings(DynamicObject object);

    void setCachedEncodings(DynamicObject object, Object value);
}
