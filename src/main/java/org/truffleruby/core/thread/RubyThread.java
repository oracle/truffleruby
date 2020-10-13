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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.InterruptMode;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.fiber.FiberManager;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.support.RandomizerNodes;
import org.truffleruby.core.support.RubyRandomizer;
import org.truffleruby.core.tracepoint.TracePointState;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;
import org.truffleruby.language.threadlocal.ThreadLocalGlobals;

import com.oracle.truffle.api.object.Shape;

public class RubyThread extends RubyDynamicObject implements ObjectGraphNode {

    public final ThreadLocalGlobals threadLocalGlobals;
    public volatile InterruptMode interruptMode;
    public volatile ThreadStatus status;
    public final List<Lock> ownedLocks;
    public FiberManager fiberManager;
    CountDownLatch finishedLatch;
    final RubyHash threadLocalVariables;
    final RubyHash recursiveObjects;
    final RubyRandomizer randomizer;
    public final TracePointState tracePointState;
    boolean reportOnException;
    boolean abortOnException;
    public volatile Thread thread;
    volatile RubyException exception;
    volatile Object value;
    public final AtomicBoolean wakeUp;
    volatile int priority;
    public ThreadLocalBuffer ioBuffer;
    Object threadGroup;
    String sourceLocation;
    Object name;

    public RubyThread(
            RubyClass rubyClass,
            Shape shape,
            RubyContext context,
            RubyLanguage language,
            boolean reportOnException,
            boolean abortOnException,
            Object threadGroup,
            String sourceLocation) {
        super(rubyClass, shape);
        this.threadLocalGlobals = new ThreadLocalGlobals();
        this.interruptMode = InterruptMode.IMMEDIATE;
        this.status = ThreadStatus.RUN;
        this.ownedLocks = new ArrayList<>();
        this.finishedLatch = new CountDownLatch(1);
        this.threadLocalVariables = HashOperations.newEmptyHash(context);
        this.recursiveObjects = HashOperations.newEmptyHash(context);
        this.randomizer = RandomizerNodes.newRandomizer(context);
        this.tracePointState = new TracePointState();
        this.reportOnException = reportOnException;
        this.abortOnException = abortOnException;
        this.thread = null;
        this.exception = null;
        this.value = null;
        this.wakeUp = new AtomicBoolean(false);
        this.priority = Thread.NORM_PRIORITY;
        this.ioBuffer = ThreadLocalBuffer.NULL_BUFFER;
        this.threadGroup = threadGroup;
        this.sourceLocation = sourceLocation;
        this.name = Nil.INSTANCE;
        // Initialized last as it captures `this`
        this.fiberManager = new FiberManager(language, context, this);
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        ObjectGraph.addProperty(reachable, threadLocalVariables);
        ObjectGraph.addProperty(reachable, name);
    }

}
