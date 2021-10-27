/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.fiber;

import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.SingleValueCastNode;
import org.truffleruby.core.cast.SingleValueCastNodeGen;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.fiber.FiberNodesFactory.FiberTransferNodeFactory;
import org.truffleruby.core.fiber.RubyFiber.FiberStatus;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes.MakeStringNode;
import org.truffleruby.core.thread.RubyThread;
import org.truffleruby.language.Nil;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.objects.AllocationTracing;

@CoreModule(value = "Fiber", isClass = true)
public abstract class FiberNodes {

    public abstract static class FiberTransferNode extends CoreMethodArrayArgumentsNode {

        public static FiberTransferNode create() {
            return FiberTransferNodeFactory.create(null);
        }

        @Child private SingleValueCastNode singleValueCastNode;

        public Object singleValue(Object[] args) {
            if (singleValueCastNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                singleValueCastNode = insert(SingleValueCastNodeGen.create());
            }
            return singleValueCastNode.executeSingleValue(args);
        }

        public abstract Object executeTransferControlTo(RubyThread currentThread, RubyFiber currentFiber,
                RubyFiber fiber, FiberOperation operation, Object[] args);

        @Specialization
        protected Object transfer(
                RubyThread currentThread,
                RubyFiber currentFiber,
                RubyFiber fiber,
                FiberOperation operation,
                Object[] args,
                @Cached BranchProfile errorProfile) {

            if (fiber.isTerminated()) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().deadFiberCalledError(this));
            }

            if (fiber.rubyThread != currentThread) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().fiberError("fiber called across threads", this));
            }

            return singleValue(getContext().fiberManager.transferControlTo(currentFiber, fiber, operation, args, this));
        }

    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyFiber allocate(RubyClass rubyClass) {
            if (getContext().getOptions().BACKTRACE_ON_NEW_FIBER) {
                getContext().getDefaultBacktraceFormatter().printBacktraceOnEnvStderr("fiber: ", this);
            }

            final RubyThread thread = getLanguage().getCurrentThread();
            final RubyFiber fiber = new RubyFiber(
                    rubyClass,
                    getLanguage().fiberShape,
                    getContext(),
                    getLanguage(),
                    thread,
                    "<uninitialized>",
                    false);
            AllocationTracing.trace(fiber, this);
            return fiber;
        }
    }

    @Primitive(name = "fiber_initialize")
    public abstract static class InitializeNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object initialize(RubyFiber fiber, boolean blocking, RubyProc block) {
            final RubyThread thread = getLanguage().getCurrentThread();
            getContext().fiberManager.initialize(fiber, blocking, block, this);
            return nil;
        }

        @Specialization
        protected Object noBlock(RubyFiber fiber, boolean blocking, Nil block) {
            throw new RaiseException(getContext(), coreExceptions().argumentErrorProcWithoutBlock(this));
        }

    }

    @CoreMethod(names = "transfer", rest = true)
    public abstract static class TransferNode extends CoreMethodArrayArgumentsNode {

        @Child private FiberTransferNode fiberTransferNode = FiberTransferNodeFactory.create(null);

        @Specialization
        protected Object transfer(RubyFiber toFiber, Object[] args,
                @Cached ConditionProfile sameFiberProfile,
                @Cached BranchProfile errorProfile) {

            if (toFiber.resumingFiber != null) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions()
                        .fiberError("attempt to transfer to a resuming fiber", this));
            }

            if (toFiber.yielding) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions()
                        .fiberError("attempt to transfer to a yielding fiber", this));
            }

            final RubyThread currentThread = getLanguage().getCurrentThread();
            final RubyFiber currentFiber = currentThread.getCurrentFiber();

            if (sameFiberProfile.profile(currentFiber == toFiber)) {
                // A Fiber can transfer to itself
                return fiberTransferNode.singleValue(args);
            }

            return fiberTransferNode
                    .executeTransferControlTo(currentThread, currentFiber, toFiber, FiberOperation.TRANSFER, args);
        }

    }


    public abstract static class FiberResumeNode extends CoreMethodArrayArgumentsNode {

        public static FiberResumeNode create() {
            return FiberNodesFactory.FiberResumeNodeFactory.create(null);
        }

        public abstract Object executeResume(FiberOperation operation, RubyFiber fiber, Object[] args);

        @Child private FiberTransferNode fiberTransferNode = FiberTransferNodeFactory.create(null);

        @Specialization
        protected Object resume(FiberOperation operation, RubyFiber toFiber, Object[] args,
                @Cached BranchProfile errorProfile) {

            final RubyThread currentThread = getLanguage().getCurrentThread();
            final RubyFiber currentFiber = currentThread.getCurrentFiber();

            if (toFiber.isTerminated()) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().fiberError("attempt to resume a terminated fiber", this));
            } else if (toFiber == currentFiber) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().fiberError("attempt to resume the current fiber", this));
            } else if (toFiber.lastResumedByFiber != null) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().fiberError("attempt to resume a resumed fiber (double resume)", this));
            } else if (toFiber.resumingFiber != null) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().fiberError("attempt to resume a resuming fiber", this));
            } else if (toFiber.lastResumedByFiber == null &&
                    (!toFiber.yielding && toFiber.status != FiberStatus.CREATED)) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().fiberError("attempt to resume a transferring fiber", this));
            }

            return fiberTransferNode
                    .executeTransferControlTo(currentThread, currentFiber, toFiber, operation, args);
        }

    }


    @Primitive(name = "fiber_raise")
    public abstract static class FiberRaiseNode extends PrimitiveArrayArgumentsNode {

        @Child private FiberResumeNode fiberResumeNode;
        @Child private FiberTransferNode fiberTransferNode;

        @Specialization
        protected Object raise(RubyFiber fiber, RubyException exception,
                @Cached BranchProfile errorProfile) {
            if (fiber.resumingFiber != null) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().fiberError("attempt to raise a resuming fiber", this));
            } else if (fiber.status == FiberStatus.CREATED) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().fiberError("cannot raise exception on unborn fiber", this));
            }

            if (fiber.status == FiberStatus.SUSPENDED && !fiber.yielding) {
                final RubyThread currentThread = getLanguage().getCurrentThread();
                final RubyFiber currentFiber = currentThread.getCurrentFiber();
                return getTransferNode().executeTransferControlTo(
                        currentThread,
                        currentFiber,
                        fiber,
                        FiberOperation.RAISE,
                        new Object[]{ exception });
            } else {
                return getResumeNode().executeResume(FiberOperation.RAISE, fiber, new Object[]{ exception });
            }
        }

        private FiberResumeNode getResumeNode() {
            if (fiberResumeNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                fiberResumeNode = insert(FiberResumeNode.create());
            }
            return fiberResumeNode;
        }

        private FiberTransferNode getTransferNode() {
            if (fiberTransferNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                fiberTransferNode = insert(FiberTransferNode.create());
            }
            return fiberTransferNode;
        }

    }

    @CoreMethod(names = "resume", rest = true)
    public abstract static class ResumeNode extends CoreMethodArrayArgumentsNode {

        @Child private FiberResumeNode fiberResumeNode = FiberResumeNode.create();

        @Specialization
        protected Object resume(RubyFiber fiber, Object[] args) {
            return fiberResumeNode.executeResume(FiberOperation.RESUME, fiber, args);
        }

    }

    @CoreMethod(names = "yield", onSingleton = true, rest = true)
    public abstract static class YieldNode extends CoreMethodArrayArgumentsNode {

        @Child private FiberTransferNode fiberTransferNode = FiberTransferNodeFactory.create(null);

        @Specialization
        protected Object fiberYield(Object[] args,
                @Cached BranchProfile errorProfile) {

            final RubyThread currentThread = getLanguage().getCurrentThread();
            final RubyFiber currentFiber = currentThread.getCurrentFiber();

            final RubyFiber fiberYieldedTo = getContext().fiberManager
                    .getReturnFiber(currentFiber, this, errorProfile);

            return fiberTransferNode.executeTransferControlTo(
                    currentThread,
                    currentFiber,
                    fiberYieldedTo,
                    FiberOperation.YIELD,
                    args);
        }

    }

    @CoreMethod(names = "alive?")
    public abstract static class AliveNode extends UnaryCoreMethodNode {

        @Specialization
        protected boolean alive(RubyFiber fiber) {
            return fiber.status != FiberStatus.TERMINATED;
        }

    }

    @Primitive(name = "fiber_current")
    public abstract static class CurrentNode extends CoreMethodNode {

        @Specialization
        protected RubyFiber current() {
            return getLanguage().getCurrentThread().getCurrentFiber();
        }

    }

    @Primitive(name = "fiber_source_location")
    public abstract static class FiberSourceLocationNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected RubyString sourceLocation(RubyFiber fiber,
                @Cached MakeStringNode makeStringNode) {
            return makeStringNode.executeMake(fiber.sourceLocation, Encodings.UTF_8, CodeRange.CR_UNKNOWN);
        }
    }

    @Primitive(name = "fiber_status")
    public abstract static class FiberStatusNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected RubyString status(RubyFiber fiber,
                @Cached MakeStringNode makeStringNode) {
            return makeStringNode.executeMake(fiber.status.label, Encodings.UTF_8, CodeRange.CR_UNKNOWN);
        }
    }

    @Primitive(name = "fiber_thread")
    public abstract static class FiberThreadNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected RubyThread thread(RubyFiber fiber) {
            return fiber.rubyThread;
        }
    }

    @Primitive(name = "fiber_get_catch_tags")
    public abstract static class FiberGetCatchTagsNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyArray getCatchTags() {
            final RubyFiber currentFiber = getLanguage().getCurrentThread().getCurrentFiber();
            return currentFiber.catchTags;
        }
    }

    @CoreMethod(names = "blocking?", onSingleton = true)
    public abstract static class IsBlockingNode extends CoreMethodNode {

        @Specialization
        protected Object isBlocking() {
            if (getLanguage().getCurrentThread().getCurrentFiber().blocking) {
                return 1;
            } else {
                return false;
            }
        }

    }

}
