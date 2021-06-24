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
import java.util.concurrent.locks.ReentrantLock;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.InterruptMode;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.fiber.FiberManager;
import org.truffleruby.core.hash.HashOperations;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.support.PRNGRandomizerNodes;
import org.truffleruby.core.support.RubyPRNGRandomizer;
import org.truffleruby.core.tracepoint.TracePointState;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;
import org.truffleruby.language.threadlocal.ThreadLocalGlobals;

import com.oracle.truffle.api.object.Shape;

public class RubyThread extends RubyDynamicObject implements ObjectGraphNode {

    // Fields initialized here are initialized just after the super() call, and before the rest of the constructor
    public final ThreadLocalGlobals threadLocalGlobals = new ThreadLocalGlobals();
    public InterruptMode interruptMode = InterruptMode.IMMEDIATE; // only accessed by this Ruby Thread and its Fibers
    public volatile ThreadStatus status = ThreadStatus.RUN;
    public final List<ReentrantLock> ownedLocks = new ArrayList<>();
    public final FiberManager fiberManager;
    CountDownLatch finishedLatch = new CountDownLatch(1);
    final RubyHash threadLocalVariables;
    final RubyHash recursiveObjects;
    final RubyHash recursiveObjectsSingle;
    final RubyPRNGRandomizer randomizer;
    public final TracePointState tracePointState = new TracePointState();
    boolean reportOnException;
    boolean abortOnException;
    public volatile Thread thread = null;
    volatile RubyException exception = null;
    volatile Object value = null;
    public final AtomicBoolean wakeUp = new AtomicBoolean(false);
    volatile int priority = Thread.NORM_PRIORITY;
    public ThreadLocalBuffer ioBuffer = ThreadLocalBuffer.NULL_BUFFER;
    Object threadGroup;
    public String sourceLocation;
    Object name = Nil.INSTANCE;

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
        this.threadLocalVariables = HashOperations.newEmptyHash(context, language);
        this.recursiveObjects = HashOperations.newEmptyHash(context, language);
        this.recursiveObjectsSingle = HashOperations.newEmptyHash(context, language);
        this.recursiveObjectsSingle.compareByIdentity = true;
        // This random instance is only for this thread and thus does not need to be thread-safe
        this.randomizer = PRNGRandomizerNodes.newRandomizer(context, language, false);
        this.reportOnException = reportOnException;
        this.abortOnException = abortOnException;
        this.threadGroup = threadGroup;
        this.sourceLocation = sourceLocation;
        // Initialized last as it captures `this`
        this.fiberManager = new FiberManager(language, context, this);
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        ObjectGraph.addProperty(reachable, threadLocalVariables);
        ObjectGraph.addProperty(reachable, name);
    }

    @Override
    public String toString() {
        return super.toString() + " " + sourceLocation;
    }
}
