/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.queue;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.core.cast.BooleanCastWithDefaultNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.thread.ThreadManager.BlockingAction;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

/** We do not reuse much of class Queue since we need to be able to replace the queue in this case and methods are small
 * anyway. */
@CoreModule(value = "SizedQueue", isClass = true)
public abstract class SizedQueueNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubySizedQueue allocate(RubyClass rubyClass) {
            final RubySizedQueue instance = new RubySizedQueue(rubyClass, getLanguage().sizedQueueShape, null);
            AllocationTracing.trace(instance, this);
            return instance;
        }

    }

    @CoreMethod(names = "initialize", visibility = Visibility.PRIVATE, required = 1, lowerFixnum = 1)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubySizedQueue initialize(RubySizedQueue self, int capacity,
                @Cached BranchProfile errorProfile) {
            if (capacity <= 0) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentError("queue size must be positive", this));
            }

            final SizedQueue blockingQueue = new SizedQueue(capacity);
            self.queue = blockingQueue;
            return self;
        }

    }

    @CoreMethod(names = "max=", required = 1, lowerFixnum = 1)
    public abstract static class SetMaxNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int setMax(RubySizedQueue self, int newCapacity,
                @Cached BranchProfile errorProfile) {
            if (newCapacity <= 0) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentError("queue size must be positive", this));
            }

            final SizedQueue queue = self.queue;
            queue.changeCapacity(newCapacity);
            return newCapacity;
        }

    }

    @CoreMethod(names = "max")
    public abstract static class MaxNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int max(RubySizedQueue self) {
            final SizedQueue queue = self.queue;
            return queue.getCapacity();
        }

    }

    @CoreMethod(names = { "push", "<<", "enq" }, required = 1, optional = 1)
    @NodeChild(value = "queueNode", type = RubyNode.class)
    @NodeChild(value = "valueNode", type = RubyNode.class)
    @NodeChild(value = "nonBlockingNode", type = RubyBaseNodeWithExecute.class)
    public abstract static class PushNode extends CoreMethodNode {

        @Child PropagateSharingNode propagateSharingNode = PropagateSharingNode.create();

        @CreateCast("nonBlockingNode")
        protected RubyBaseNodeWithExecute coerceToBoolean(RubyBaseNodeWithExecute nonBlocking) {
            return BooleanCastWithDefaultNode.create(false, nonBlocking);
        }

        abstract RubyNode getQueueNode();

        abstract RubyNode getValueNode();

        abstract RubyBaseNodeWithExecute getNonBlockingNode();

        public static PushNode create(RubyNode queue, RubyNode value, RubyBaseNodeWithExecute nonBlocking) {
            return SizedQueueNodesFactory.PushNodeFactory.create(queue, value, nonBlocking);
        }

        @Specialization(guards = "!nonBlocking")
        protected RubySizedQueue pushBlocking(RubySizedQueue self, final Object value, boolean nonBlocking) {
            final SizedQueue queue = self.queue;

            propagateSharingNode.executePropagate(self, value);
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
        protected RubySizedQueue pushNonBlock(RubySizedQueue self, final Object value, boolean nonBlocking,
                @Cached BranchProfile errorProfile) {
            final SizedQueue queue = self.queue;

            propagateSharingNode.executePropagate(self, value);

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

        private RubyBaseNodeWithExecute getNonBlockingNodeBeforeCasting() {
            return ((BooleanCastWithDefaultNode) getNonBlockingNode()).getValueNode();
        }

        @Override
        public RubyNode cloneUninitialized() {
            var copy = create(
                    getQueueNode().cloneUninitialized(),
                    getValueNode().cloneUninitialized(),
                    getNonBlockingNodeBeforeCasting().cloneUninitialized());
            copy.copyFlags(this);
            return copy;
        }

    }

    @CoreMethod(names = { "pop", "shift", "deq" }, optional = 1)
    @NodeChild(value = "queueNode", type = RubyNode.class)
    @NodeChild(value = "nonBlockingNode", type = RubyBaseNodeWithExecute.class)
    public abstract static class PopNode extends CoreMethodNode {

        @CreateCast("nonBlockingNode")
        protected RubyBaseNodeWithExecute coerceToBoolean(RubyBaseNodeWithExecute nonBlocking) {
            return BooleanCastWithDefaultNode.create(false, nonBlocking);
        }

        abstract RubyNode getQueueNode();

        abstract RubyBaseNodeWithExecute getNonBlockingNode();

        public static PopNode create(RubyNode queue, RubyBaseNodeWithExecute nonBlocking) {
            return SizedQueueNodesFactory.PopNodeFactory.create(queue, nonBlocking);
        }

        @Specialization(guards = "!nonBlocking")
        protected Object popBlocking(RubySizedQueue self, boolean nonBlocking) {
            final SizedQueue queue = self.queue;

            final Object value = doPop(queue);

            if (value == SizedQueue.CLOSED) {
                return nil;
            } else {
                return value;
            }
        }

        @TruffleBoundary
        private Object doPop(SizedQueue queue) {
            return getContext().getThreadManager().runUntilResult(this, queue::take);
        }

        @Specialization(guards = "nonBlocking")
        protected Object popNonBlock(RubySizedQueue self, boolean nonBlocking,
                @Cached BranchProfile errorProfile) {
            final SizedQueue queue = self.queue;

            final Object value = queue.poll();

            if (value == null) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().threadError("queue empty", this));
            }

            return value;
        }

        private RubyBaseNodeWithExecute getNonBlockingNodeBeforeCasting() {
            return ((BooleanCastWithDefaultNode) getNonBlockingNode()).getValueNode();
        }

        @Override
        public RubyNode cloneUninitialized() {
            var copy = create(
                    getQueueNode().cloneUninitialized(),
                    getNonBlockingNodeBeforeCasting().cloneUninitialized());
            copy.copyFlags(this);
            return copy;
        }

    }

    @CoreMethod(names = "empty?")
    public abstract static class EmptyNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean empty(RubySizedQueue self) {
            final SizedQueue queue = self.queue;
            return queue.isEmpty();
        }

    }

    @CoreMethod(names = { "size", "length" })
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int size(RubySizedQueue self) {
            final SizedQueue queue = self.queue;
            return queue.size();
        }

    }

    @CoreMethod(names = "clear")
    public abstract static class ClearNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubySizedQueue clear(RubySizedQueue self) {
            final SizedQueue queue = self.queue;
            queue.clear();
            return self;
        }

    }

    @CoreMethod(names = "num_waiting")
    public abstract static class NumWaitingNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int num_waiting(RubySizedQueue self) {
            final SizedQueue queue = self.queue;
            return queue.getNumberWaiting();
        }

    }

    @CoreMethod(names = "close")
    public abstract static class CloseNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubySizedQueue close(RubySizedQueue self) {
            self.queue.close();
            return self;
        }

    }

}
