/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.queue;

import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.core.cast.BooleanCastWithDefaultNodeGen;
import org.truffleruby.core.thread.ThreadManager.BlockingAction;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

/**
 * We do not reuse much of class Queue since we need to be able to replace the queue in this case
 * and methods are small anyway.
 */
@CoreModule(value = "SizedQueue", isClass = true)
public abstract class SizedQueueNodes {

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass) {
            Object queue = null;
            return allocateNode.allocate(rubyClass, queue);
        }

    }

    @CoreMethod(names = "initialize", visibility = Visibility.PRIVATE, required = 1, lowerFixnum = 1)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected DynamicObject initialize(DynamicObject self, int capacity,
                @Cached BranchProfile errorProfile) {
            if (capacity <= 0) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentError("queue size must be positive", this));
            }

            final SizedQueue blockingQueue = new SizedQueue(capacity);
            Layouts.SIZED_QUEUE.setQueue(self, blockingQueue);
            return self;
        }

    }

    @CoreMethod(names = "max=", required = 1, lowerFixnum = 1)
    public abstract static class SetMaxNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int setMax(DynamicObject self, int newCapacity,
                @Cached BranchProfile errorProfile) {
            if (newCapacity <= 0) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentError("queue size must be positive", this));
            }

            final SizedQueue queue = Layouts.SIZED_QUEUE.getQueue(self);
            queue.changeCapacity(newCapacity);
            return newCapacity;
        }

    }

    @CoreMethod(names = "max")
    public abstract static class MaxNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int max(DynamicObject self) {
            final SizedQueue queue = Layouts.SIZED_QUEUE.getQueue(self);
            return queue.getCapacity();
        }

    }

    @CoreMethod(names = { "push", "<<", "enq" }, required = 1, optional = 1)
    @NodeChild(value = "queue", type = RubyNode.class)
    @NodeChild(value = "value", type = RubyNode.class)
    @NodeChild(value = "nonBlocking", type = RubyNode.class)
    public abstract static class PushNode extends CoreMethodNode {

        @Child PropagateSharingNode propagateSharingNode = PropagateSharingNode.create();

        @CreateCast("nonBlocking")
        protected RubyNode coerceToBoolean(RubyNode nonBlocking) {
            return BooleanCastWithDefaultNodeGen.create(false, nonBlocking);
        }

        @Specialization(guards = "!nonBlocking")
        protected DynamicObject pushBlocking(DynamicObject self, final Object value, boolean nonBlocking) {
            final SizedQueue queue = Layouts.SIZED_QUEUE.getQueue(self);

            propagateSharingNode.propagate(self, value);
            doPushBlocking(value, queue);

            return self;
        }

        @TruffleBoundary
        private void doPushBlocking(final Object value, final SizedQueue queue) {
            getContext().getThreadManager().runUntilResult(this, () -> {
                if (queue.put(value)) {
                    return BlockingAction.SUCCESS;
                } else {
                    throw new RaiseException(getContext(), coreExceptions().closedQueueError(this));
                }
            });
        }

        @Specialization(guards = "nonBlocking")
        protected DynamicObject pushNonBlock(DynamicObject self, final Object value, boolean nonBlocking,
                @Cached BranchProfile errorProfile) {
            final SizedQueue queue = Layouts.SIZED_QUEUE.getQueue(self);

            propagateSharingNode.propagate(self, value);

            switch (queue.offer(value)) {
                case SUCCESS:
                    return self;
                case FULL:
                    errorProfile.enter();
                    throw new RaiseException(getContext(), coreExceptions().threadErrorQueueFull(this));
                case CLOSED:
                    errorProfile.enter();
                    throw new RaiseException(getContext(), coreExceptions().closedQueueError(this));
            }

            return self;
        }

    }

    @CoreMethod(names = { "pop", "shift", "deq" }, optional = 1)
    @NodeChild(value = "queue", type = RubyNode.class)
    @NodeChild(value = "nonBlocking", type = RubyNode.class)
    public abstract static class PopNode extends CoreMethodNode {

        @CreateCast("nonBlocking")
        protected RubyNode coerceToBoolean(RubyNode nonBlocking) {
            return BooleanCastWithDefaultNodeGen.create(false, nonBlocking);
        }

        @Specialization(guards = "!nonBlocking")
        protected Object popBlocking(DynamicObject self, boolean nonBlocking) {
            final SizedQueue queue = Layouts.SIZED_QUEUE.getQueue(self);

            final Object value = doPop(queue);

            if (value == SizedQueue.CLOSED) {
                return nil();
            } else {
                return value;
            }
        }

        @TruffleBoundary
        private Object doPop(final SizedQueue queue) {
            return getContext().getThreadManager().runUntilResult(this, queue::take);
        }

        @Specialization(guards = "nonBlocking")
        protected Object popNonBlock(DynamicObject self, boolean nonBlocking,
                @Cached BranchProfile errorProfile) {
            final SizedQueue queue = Layouts.SIZED_QUEUE.getQueue(self);

            final Object value = queue.poll();

            if (value == null) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().threadError("queue empty", this));
            }

            return value;
        }

    }

    @CoreMethod(names = "empty?")
    public abstract static class EmptyNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean empty(DynamicObject self) {
            final SizedQueue queue = Layouts.SIZED_QUEUE.getQueue(self);
            return queue.isEmpty();
        }

    }

    @CoreMethod(names = { "size", "length" })
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int size(DynamicObject self) {
            final SizedQueue queue = Layouts.SIZED_QUEUE.getQueue(self);
            return queue.size();
        }

    }

    @CoreMethod(names = "clear")
    public abstract static class ClearNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected DynamicObject clear(DynamicObject self) {
            final SizedQueue queue = Layouts.SIZED_QUEUE.getQueue(self);
            queue.clear();
            return self;
        }

    }

    @CoreMethod(names = "num_waiting")
    public abstract static class NumWaitingNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int num_waiting(DynamicObject self) {
            final SizedQueue queue = Layouts.SIZED_QUEUE.getQueue(self);
            return queue.getNumberWaiting();
        }

    }

    @CoreMethod(names = "close")
    public abstract static class CloseNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected DynamicObject close(DynamicObject self) {
            Layouts.SIZED_QUEUE.getQueue(self).close();
            return self;
        }

    }

    @CoreMethod(names = "closed?")
    public abstract static class ClosedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean closed(DynamicObject self) {
            return Layouts.SIZED_QUEUE.getQueue(self).isClosed();
        }

    }

}
