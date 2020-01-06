/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.fiber;

import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.cast.SingleValueCastNode;
import org.truffleruby.core.cast.SingleValueCastNodeGen;
import org.truffleruby.core.fiber.FiberNodesFactory.FiberTransferNodeFactory;
import org.truffleruby.core.thread.GetCurrentRubyThreadNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.UnsupportedOperationBehavior;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreModule(value = "Fiber", isClass = true)
public abstract class FiberNodes {

    public abstract static class FiberTransferNode extends CoreMethodArrayArgumentsNode {

        @Child private SingleValueCastNode singleValueCastNode;

        public Object singleValue(VirtualFrame frame, Object[] args) {
            if (singleValueCastNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                singleValueCastNode = insert(SingleValueCastNodeGen.create());
            }
            return singleValueCastNode.executeSingleValue(frame, args);
        }

        public abstract Object executeTransferControlTo(VirtualFrame frame,
                DynamicObject currentThread, DynamicObject currentFiber, DynamicObject fiber,
                FiberOperation operation, Object[] args);

        @Specialization(guards = "isRubyFiber(fiber)")
        protected Object transfer(VirtualFrame frame,
                DynamicObject currentThread, DynamicObject currentFiber, DynamicObject fiber,
                FiberOperation operation, Object[] args,
                @Cached BranchProfile errorProfile) {

            if (!Layouts.FIBER.getAlive(fiber)) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().deadFiberCalledError(this));
            }

            if (Layouts.FIBER.getRubyThread(fiber) != currentThread) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().fiberError("fiber called across threads", this));
            }

            final FiberManager fiberManager = Layouts.THREAD.getFiberManager(currentThread);
            return singleValue(frame, fiberManager.transferControlTo(currentFiber, fiber, operation, args));
        }

    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass) {
            final DynamicObject thread = getContext().getThreadManager().getCurrentThread();
            final DynamicObjectFactory factory = Layouts.CLASS.getInstanceFactory(rubyClass);
            return Layouts.THREAD.getFiberManager(thread).createFiber(getContext(), thread, factory, null);
        }

    }

    @CoreMethod(
            names = "initialize",
            needsBlock = true,
            unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected DynamicObject initialize(DynamicObject fiber, DynamicObject block) {
            final DynamicObject thread = getContext().getThreadManager().getCurrentThread();
            Layouts.THREAD.getFiberManager(thread).initialize(fiber, block, this);
            return nil();
        }

    }

    @CoreMethod(names = "transfer", rest = true)
    public abstract static class TransferNode extends CoreMethodArrayArgumentsNode {

        @Child private FiberTransferNode fiberTransferNode = FiberTransferNodeFactory.create(null);

        @Specialization
        protected Object resume(VirtualFrame frame, DynamicObject fiber, Object[] args,
                @Cached GetCurrentRubyThreadNode getCurrentRubyThreadNode,
                @Cached("createBinaryProfile()") ConditionProfile sameFiberProfile) {

            Layouts.FIBER.setTransferred(fiber, true);

            final DynamicObject currentThread = getCurrentRubyThreadNode.executeGetRubyThread(frame);
            final FiberManager fiberManager = Layouts.THREAD.getFiberManager(currentThread);
            final DynamicObject currentFiber = fiberManager.getCurrentFiber();

            if (sameFiberProfile.profile(currentFiber == fiber)) {
                // A Fiber can transfer to itself
                return fiberTransferNode.singleValue(frame, args);
            }

            return fiberTransferNode
                    .executeTransferControlTo(frame, currentThread, currentFiber, fiber, FiberOperation.TRANSFER, args);
        }

    }

    @CoreMethod(names = "resume", rest = true)
    public abstract static class ResumeNode extends CoreMethodArrayArgumentsNode {

        @Child private FiberTransferNode fiberTransferNode = FiberTransferNodeFactory.create(null);

        @Specialization
        protected Object resume(VirtualFrame frame, DynamicObject fiber, Object[] args,
                @Cached GetCurrentRubyThreadNode getCurrentRubyThreadNode,
                @Cached("createBinaryProfile()") ConditionProfile doubleResumeProfile,
                @Cached("createBinaryProfile()") ConditionProfile transferredProfile) {

            final DynamicObject parentFiber = Layouts.FIBER.getLastResumedByFiber(fiber);
            final FiberManager fiberToResumeManager = Layouts.THREAD
                    .getFiberManager(Layouts.FIBER.getRubyThread(fiber));

            if (doubleResumeProfile.profile(parentFiber != null || fiber == fiberToResumeManager.getRootFiber())) {
                throw new RaiseException(getContext(), coreExceptions().fiberError("double resume", this));
            }

            if (transferredProfile.profile(Layouts.FIBER.getTransferred(fiber))) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().fiberError("cannot resume transferred Fiber", this));
            }

            final DynamicObject currentThread = getCurrentRubyThreadNode.executeGetRubyThread(frame);
            final FiberManager fiberManager = Layouts.THREAD.getFiberManager(currentThread);
            final DynamicObject currentFiber = fiberManager.getCurrentFiber();

            return fiberTransferNode
                    .executeTransferControlTo(frame, currentThread, currentFiber, fiber, FiberOperation.RESUME, args);
        }

    }

    @CoreMethod(names = "yield", onSingleton = true, rest = true)
    public abstract static class YieldNode extends CoreMethodArrayArgumentsNode {

        @Child private FiberTransferNode fiberTransferNode = FiberTransferNodeFactory.create(null);

        @Specialization
        protected Object yield(VirtualFrame frame, Object[] args,
                @Cached GetCurrentRubyThreadNode getCurrentRubyThreadNode,
                @Cached BranchProfile errorProfile) {

            final DynamicObject currentThread = getCurrentRubyThreadNode.executeGetRubyThread(frame);
            final FiberManager fiberManager = Layouts.THREAD.getFiberManager(currentThread);
            final DynamicObject currentFiber = fiberManager.getCurrentFiber();

            final DynamicObject fiberYieldedTo = fiberManager.getReturnFiber(currentFiber, this, errorProfile);

            return fiberTransferNode.executeTransferControlTo(
                    frame,
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
        protected boolean alive(DynamicObject fiber) {
            return Layouts.FIBER.getAlive(fiber);
        }

    }

    @CoreMethod(names = "current", onSingleton = true)
    public abstract static class CurrentNode extends CoreMethodNode {

        @Specialization
        protected DynamicObject current(VirtualFrame frame,
                @Cached GetCurrentRubyThreadNode getCurrentRubyThreadNode) {
            final DynamicObject currentThread = getCurrentRubyThreadNode.executeGetRubyThread(frame);
            return Layouts.THREAD.getFiberManager(currentThread).getCurrentFiber();
        }

    }

    @Primitive(name = "fiber_get_catch_tags")
    public static abstract class FiberGetCatchTagsNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected DynamicObject getCatchTags(VirtualFrame frame,
                @Cached GetCurrentRubyThreadNode getCurrentRubyThreadNode) {
            final DynamicObject currentThread = getCurrentRubyThreadNode.executeGetRubyThread(frame);
            final DynamicObject currentFiber = Layouts.THREAD.getFiberManager(currentThread).getCurrentFiber();
            return Layouts.FIBER.getCatchTags(currentFiber);
        }
    }

}
