/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.fiber;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.dsl.Layout;
import com.oracle.truffle.api.object.dsl.Nullable;
import com.oracle.truffle.api.object.dsl.Volatile;
import org.truffleruby.core.basicobject.BasicObjectLayout;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

@Layout
public interface FiberLayout extends BasicObjectLayout {

    DynamicObjectFactory createFiberShape(DynamicObject logicalClass,
                                          DynamicObject metaClass);

    DynamicObject createFiber(DynamicObjectFactory factory,
                              DynamicObject fiberLocals,
                              DynamicObject catchTags,
                              CountDownLatch initializedLatch,
                              CountDownLatch finishedLatch,
                              BlockingQueue<FiberManager.FiberMessage> messageQueue,
                              DynamicObject rubyThread,
                              @Volatile @Nullable DynamicObject lastResumedByFiber,
                              @Volatile boolean alive,
                              @Nullable Thread thread,
                              @Volatile boolean transferred,
                              @Volatile @Nullable Throwable uncaughtException);

    boolean isFiber(DynamicObject object);

    DynamicObject getFiberLocals(DynamicObject object);

    DynamicObject getCatchTags(DynamicObject object);

    CountDownLatch getInitializedLatch(DynamicObject object);

    CountDownLatch getFinishedLatch(DynamicObject object);
    void setFinishedLatch(DynamicObject object, CountDownLatch value);

    BlockingQueue<FiberManager.FiberMessage> getMessageQueue(DynamicObject object);

    DynamicObject getRubyThread(DynamicObject object);

    DynamicObject getLastResumedByFiber(DynamicObject object);
    void setLastResumedByFiber(DynamicObject object, DynamicObject value);

    boolean getAlive(DynamicObject object);
    void setAlive(DynamicObject object, boolean value);

    Thread getThread(DynamicObject object);
    void setThread(DynamicObject object, Thread value);

    boolean getTransferred(DynamicObject object);
    void setTransferred(DynamicObject object, boolean value);

    Throwable getUncaughtException(DynamicObject object);
    void setUncaughtException(DynamicObject object, Throwable value);

}
