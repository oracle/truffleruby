/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import org.truffleruby.core.basicobject.BasicObjectLayout;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.ObjectType;
import com.oracle.truffle.api.object.dsl.Layout;
import com.oracle.truffle.api.object.dsl.Nullable;

@Layout
public interface HashLayout extends BasicObjectLayout {

    DynamicObjectFactory createHashShape(DynamicObject logicalClass,
            DynamicObject metaClass);

    Object[] build(
            @Nullable Object store,
            int size,
            @Nullable Entry firstInSequence,
            @Nullable Entry lastInSequence,
            DynamicObject defaultBlock,
            Object defaultValue,
            boolean compareByIdentity);

    boolean isHash(ObjectType objectType);

    boolean isHash(DynamicObject object);

    boolean isHash(Object object);

    DynamicObject getDefaultBlock(DynamicObject object);

    void setDefaultBlock(DynamicObject object, DynamicObject value);

    Object getDefaultValue(DynamicObject object);

    void setDefaultValue(DynamicObject object, Object value);

    Object getStore(DynamicObject object);

    void setStore(DynamicObject object, Object value);

    int getSize(DynamicObject object);

    void setSize(DynamicObject object, int value);

    Entry getFirstInSequence(DynamicObject object);

    void setFirstInSequence(DynamicObject object, Entry value);

    Entry getLastInSequence(DynamicObject object);

    void setLastInSequence(DynamicObject object, Entry value);

    boolean getCompareByIdentity(DynamicObject object);

    void setCompareByIdentity(DynamicObject object, boolean value);

}
