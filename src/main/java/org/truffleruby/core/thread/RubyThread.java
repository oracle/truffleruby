/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.thread;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

import org.truffleruby.RubyContext;
import org.truffleruby.core.InterruptMode;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.fiber.FiberManager;
import org.truffleruby.core.support.RubyRandomizer;
import org.truffleruby.core.tracepoint.TracePointState;
import org.truffleruby.interop.messages.RubyThreadMessages;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.threadlocal.ThreadLocalGlobals;

import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

public class RubyThread extends RubyDynamicObject {

    public final ThreadLocalGlobals threadLocalGlobals;
    public volatile InterruptMode interruptMode;
    public volatile ThreadStatus status;
    public final List<Lock> ownedLocks;
    public FiberManager fiberManager;
    CountDownLatch finishedLatch;
    final DynamicObject threadLocalVariables;
    final DynamicObject recursiveObjects;
    final RubyRandomizer randomizer;
    public final TracePointState tracePointState;
    boolean reportOnException;
    boolean abortOnException;
    volatile Thread thread;
    volatile RubyException exception;
    volatile Object value;
    public final AtomicBoolean wakeUp;
    volatile int priority;
    public ThreadLocalBuffer ioBuffer;
    Object threadGroup;
    String sourceLocation;
    Object name;

    public RubyThread(
            Shape shape,
            RubyContext context,
            ThreadLocalGlobals threadLocalGlobals,
            InterruptMode interruptMode,
            ThreadStatus status,
            List<Lock> ownedLocks,
            CountDownLatch finishedLatch,
            DynamicObject threadLocalVariables,
            DynamicObject recursiveObjects,
            RubyRandomizer randomizer,
            TracePointState tracePointState,
            boolean reportOnException,
            boolean abortOnException,
            Thread thread,
            RubyException exception,
            Object value,
            AtomicBoolean wakeUp,
            int priority,
            ThreadLocalBuffer ioBuffer,
            Object threadGroup,
            String sourceLocation,
            Object name) {
        super(shape);
        this.threadLocalGlobals = threadLocalGlobals;
        this.interruptMode = interruptMode;
        this.status = status;
        this.ownedLocks = ownedLocks;
        this.finishedLatch = finishedLatch;
        this.threadLocalVariables = threadLocalVariables;
        this.recursiveObjects = recursiveObjects;
        this.randomizer = randomizer;
        this.tracePointState = tracePointState;
        this.reportOnException = reportOnException;
        this.abortOnException = abortOnException;
        this.thread = thread;
        this.exception = exception;
        this.value = value;
        this.wakeUp = wakeUp;
        this.priority = priority;
        this.ioBuffer = ioBuffer;
        this.threadGroup = threadGroup;
        this.sourceLocation = sourceLocation;
        this.name = name;

        this.fiberManager = new FiberManager(context, this);
    }

    @Override
    @ExportMessage
    public Class<?> dispatch() {
        return RubyThreadMessages.class;
    }

}
