/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.mutex;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.truffleruby.core.basicobject.BasicObjectLayout;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;

@Layout
public interface ConditionVariableLayout extends BasicObjectLayout {

    DynamicObjectFactory createConditionVariableShape(
            DynamicObject logicalClass,
            DynamicObject metaClass);

    DynamicObject createConditionVariable(
            DynamicObjectFactory factory,
            ReentrantLock lock,
            Condition condition,
            int waiters,
            int signals);

    boolean isConditionVariable(DynamicObject object);

    ReentrantLock getLock(DynamicObject object);

    Condition getCondition(DynamicObject object);

    int getWaiters(DynamicObject object);

    void setWaiters(DynamicObject object, int value);

    int getSignals(DynamicObject object);

    void setSignals(DynamicObject object, int value);
}
