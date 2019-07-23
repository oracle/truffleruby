/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.queue;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.core.cast.BooleanCastWithDefaultNodeGen;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.objects.shared.PropagateSharingNode;

@CoreClass("Queue")
public abstract class QueueNodes {

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            return allocateNode.allocate(rubyClass, new UnsizedQueue());
        }

    }

    @CoreMethod(names = { "push", "<<", "enq" }, required = 1)
    public abstract static class PushNode extends CoreMethodArrayArgumentsNode {

        @Child private PropagateSharingNode propagateSharingNode = PropagateSharingNode.create();

        @Specialization
        public DynamicObject push(DynamicObject self, final Object value) {
            final UnsizedQueue queue = Layouts.QUEUE.getQueue(self);

            propagateSharingNode.propagate(self, value);

            if (queue.add(value)) {
                return self;
            } else {
                throw new RaiseException(getContext(), coreExceptions().closedQueueError(this));
            }
        }

    }

    @CoreMethod(names = { "pop", "shift", "deq" }, optional = 1)
    @NodeChild(value = "queue", type = RubyNode.class)
    @NodeChild(value = "nonBlocking", type = RubyNode.class)
    public abstract static class PopNode extends CoreMethodNode {

        @CreateCast("nonBlocking")
        public RubyNode coerceToBoolean(RubyNode nonBlocking) {
            return BooleanCastWithDefaultNodeGen.create(false, nonBlocking);
        }

        @Specialization(guards = "!nonBlocking")
        public Object popBlocking(DynamicObject self, boolean nonBlocking,
                                  @Cached BranchProfile closedProfile) {
            final UnsizedQueue queue = Layouts.QUEUE.getQueue(self);

            final Object value = doPop(queue);

            if (value == UnsizedQueue.CLOSED) {
                closedProfile.enter();
                return nil();
            } else {
                return value;
            }
        }

        @TruffleBoundary
        private Object doPop(final UnsizedQueue queue) {
            return getContext().getThreadManager().runUntilResult(this, queue::take);
        }

        @Specialization(guards = "nonBlocking")
        public Object popNonBlock(DynamicObject self, boolean nonBlocking,
                                  @Cached BranchProfile errorProfile) {
            final UnsizedQueue queue = Layouts.QUEUE.getQueue(self);

            final Object value = queue.poll();

            if (value == null) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().threadError("queue empty", this));
            } else {
                return value;
            }
        }

    }

    @NonStandard
    @CoreMethod(names = "receive_timeout", required = 1, visibility = Visibility.PRIVATE, lowerFixnum = 1)
    public abstract static class ReceiveTimeoutNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object receiveTimeout(DynamicObject self, int duration) {
            return receiveTimeout(self, (double) duration);
        }

        @TruffleBoundary
        @Specialization
        public Object receiveTimeout(DynamicObject self, double duration) {
            final UnsizedQueue queue = Layouts.QUEUE.getQueue(self);

            final long durationInMillis = (long) (duration * 1000.0);
            final long start = System.currentTimeMillis();

            return getContext().getThreadManager().runUntilResult(this, () -> {
                long now = System.currentTimeMillis();
                long waited = now - start;
                if (waited >= durationInMillis) {
                    // Try again to make sure we at least tried once
                    final Object result = queue.poll();
                    if (result == null) {
                        return false;
                    } else {
                        return result;
                    }
                }

                final Object result = queue.poll(durationInMillis);
                if (result == null) {
                    return false;
                } else {
                    return result;
                }
            });
        }

    }

    @CoreMethod(names = "empty?")
    public abstract static class EmptyNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean empty(DynamicObject self) {
            final UnsizedQueue queue = Layouts.QUEUE.getQueue(self);
            return queue.isEmpty();
        }

    }

    @CoreMethod(names = { "size", "length" })
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int size(DynamicObject self) {
            final UnsizedQueue queue = Layouts.QUEUE.getQueue(self);
            return queue.size();
        }

    }

    @CoreMethod(names = "clear")
    public abstract static class ClearNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject clear(DynamicObject self) {
            final UnsizedQueue queue = Layouts.QUEUE.getQueue(self);
            queue.clear();
            return self;
        }

    }

    @CoreMethod(names = "marshal_dump")
    public abstract static class MarshalDumpNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object marshal_dump(DynamicObject self) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorCantDump(self, this));
        }

    }

    @CoreMethod(names = "num_waiting")
    public abstract static class NumWaitingNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int num_waiting(DynamicObject self) {
            final UnsizedQueue queue = Layouts.QUEUE.getQueue(self);
            return queue.getNumberWaitingToTake();
        }

    }

    @CoreMethod(names = "close")
    public abstract static class CloseNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject close(DynamicObject self) {
            Layouts.QUEUE.getQueue(self).close();
            return self;
        }

    }

    @CoreMethod(names = "closed?")
    public abstract static class ClosedNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean closed(DynamicObject self) {
            return Layouts.QUEUE.getQueue(self).isClosed();
        }

    }

}
