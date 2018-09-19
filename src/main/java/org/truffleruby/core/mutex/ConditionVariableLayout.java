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
            Condition condition);

    boolean isConditionVariable(DynamicObject object);

    ReentrantLock getLock(DynamicObject object);

    Condition getCondition(DynamicObject object);
}
