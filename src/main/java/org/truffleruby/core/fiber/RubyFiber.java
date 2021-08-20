/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.fiber;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import com.oracle.truffle.api.CompilerAsserts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.cext.ValueWrapperManager;
import org.truffleruby.core.MarkingService;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.basicobject.RubyBasicObject;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.thread.RubyThread;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.ObjectGraphNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.Shape;

public final class RubyFiber extends RubyDynamicObject implements ObjectGraphNode {

    public final RubyBasicObject fiberLocals;
    public final RubyArray catchTags;
    public final CountDownLatch initializedLatch = new CountDownLatch(1);
    public CountDownLatch finishedLatch = new CountDownLatch(1);
    final BlockingQueue<FiberManager.FiberMessage> messageQueue = newMessageQueue();
    public final RubyThread rubyThread;
    volatile RubyFiber lastResumedByFiber = null;
    public volatile boolean resumed = false;
    public volatile boolean alive = true;
    public Thread thread = null;
    volatile boolean transferred = false;
    public volatile Throwable uncaughtException = null;
    String sourceLocation;
    public final MarkingService.ExtensionCallStack extensionCallStack;
    public final ValueWrapperManager.HandleBlockHolder handleData;

    public RubyFiber(
            RubyClass rubyClass,
            Shape shape,
            RubyContext context,
            RubyLanguage language,
            RubyThread rubyThread,
            String sourceLocation) {
        super(rubyClass, shape);
        assert rubyThread != null;
        CompilerAsserts.partialEvaluationConstant(language);
        this.fiberLocals = new RubyBasicObject(
                context.getCoreLibrary().objectClass,
                language.basicObjectShape);
        this.catchTags = ArrayHelpers.createEmptyArray(context, language);
        this.rubyThread = rubyThread;
        this.sourceLocation = sourceLocation;
        extensionCallStack = new MarkingService.ExtensionCallStack(Nil.INSTANCE);
        handleData = new ValueWrapperManager.HandleBlockHolder();
    }

    public boolean isRootFiber() {
        return rubyThread.getRootFiber() == this;
    }

    public String getStatus() {
        if (!resumed) {
            return "created";
        } else if (alive) {
            if (rubyThread.getCurrentFiberRacy() == this) {
                return "resumed";
            } else {
                return "suspended";
            }
        } else {
            return "terminated";
        }
    }

    @TruffleBoundary
    private static LinkedBlockingQueue<FiberManager.FiberMessage> newMessageQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        reachable.add(fiberLocals);
        reachable.add(rubyThread);
    }

}
