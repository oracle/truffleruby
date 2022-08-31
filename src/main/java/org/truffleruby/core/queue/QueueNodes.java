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

import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.core.cast.BooleanCastWithDefaultNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;

@CoreModule(value = "Queue", isClass = true)
public abstract class QueueNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyQueue allocate(RubyClass rubyClass) {
            final RubyQueue instance = new RubyQueue(rubyClass, getLanguage().queueShape, new UnsizedQueue());
            AllocationTracing.trace(instance, this);
            return instance;
        }

    }

    @CoreMethod(names = { "push", "<<", "enq" }, required = 1)
    public abstract static class PushNode extends CoreMethodArrayArgumentsNode {

        @Child private PropagateSharingNode propagateSharingNode = PropagateSharingNode.create();

        @Specialization
        protected RubyQueue push(RubyQueue self, final Object value) {
            final UnsizedQueue queue = self.queue;

            propagateSharingNode.executePropagate(self, value);

            if (queue.add(value)) {
                return self;
            } else {
                throw new RaiseException(getContext(), coreExceptions().closedQueueError(this));
            }
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
            return QueueNodesFactory.PopNodeFactory.create(queue, nonBlocking);
        }

        @Specialization(guards = "!nonBlocking")
        protected Object popBlocking(RubyQueue self, boolean nonBlocking,
                @Cached BranchProfile closedProfile) {
            final UnsizedQueue queue = self.queue;

            final Object value = doPop(queue);

            if (value == UnsizedQueue.CLOSED) {
                closedProfile.enter();
                return nil;
            } else {
                return value;
            }
        }

        @TruffleBoundary
        private Object doPop(UnsizedQueue queue) {
            return getContext().getThreadManager().runUntilResult(this, queue::take);
        }

        @Specialization(guards = "nonBlocking")
        protected Object popNonBlock(RubyQueue self, boolean nonBlocking,
                @Cached BranchProfile errorProfile) {
            final UnsizedQueue queue = self.queue;

            final Object value = queue.poll();

            if (value == null) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().threadError("queue empty", this));
            } else {
                return value;
            }
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

    @NonStandard
    @CoreMethod(names = "receive_timeout", required = 1, visibility = Visibility.PRIVATE, lowerFixnum = 1)
    public abstract static class ReceiveTimeoutNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object receiveTimeout(RubyQueue self, int duration) {
            return receiveTimeout(self, (double) duration);
        }

        @TruffleBoundary
        @Specialization
        protected Object receiveTimeout(RubyQueue self, double duration) {
            final UnsizedQueue queue = self.queue;

            final long durationInMillis = (long) (duration * 1000.0);
            final long start = System.currentTimeMillis();

            return getContext().getThreadManager().runUntilResult(this, () -> {
                long now = System.currentTimeMillis();
                long waited = now - start;
                if (waited >= durationInMillis) {
                    // Try again to make sure we at least tried once
                    final Object result = queue.poll();
                    return translateResult(result);
                }

                final Object result = queue.poll(durationInMillis);
                return translateResult(result);
            });
        }

        private Object translateResult(Object result) {
            if (result == null) {
                return false;
            } else if (result == UnsizedQueue.CLOSED) {
                return nil;
            } else {
                return result;
            }
        }

    }

    @CoreMethod(names = "empty?")
    public abstract static class EmptyNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean empty(RubyQueue self) {
            final UnsizedQueue queue = self.queue;
            return queue.isEmpty();
        }

    }

    @CoreMethod(names = { "size", "length" })
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int size(RubyQueue self) {
            final UnsizedQueue queue = self.queue;
            return queue.size();
        }

    }

    @CoreMethod(names = "clear")
    public abstract static class ClearNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyQueue clear(RubyQueue self) {
            final UnsizedQueue queue = self.queue;
            queue.clear();
            return self;
        }

    }

    @CoreMethod(names = "marshal_dump")
    public abstract static class MarshalDumpNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object marshal_dump(RubyQueue self) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorCantDump(self, this));
        }

    }

    @CoreMethod(names = "num_waiting")
    public abstract static class NumWaitingNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int num_waiting(RubyQueue self) {
            final UnsizedQueue queue = self.queue;
            return queue.getNumberWaitingToTake();
        }

    }

    @CoreMethod(names = "close")
    public abstract static class CloseNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyQueue close(RubyQueue self) {
            self.queue.close();
            return self;
        }

    }

    @CoreMethod(names = "closed?")
    public abstract static class ClosedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean closed(RubyQueue self) {
            return self.queue.isClosed();
        }

        @Specialization
        protected boolean closed(RubySizedQueue self) {
            return self.queue.isClosed();
        }

    }

}
