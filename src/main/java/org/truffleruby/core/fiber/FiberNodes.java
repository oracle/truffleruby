/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.fiber;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.cast.SingleValueCastNode;
import org.truffleruby.core.cast.SingleValueCastNodeGen;
import org.truffleruby.core.thread.GetCurrentRubyThreadNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.UnsupportedOperationBehavior;

@CoreClass("Fiber")
public abstract class FiberNodes {

    public abstract static class FiberTransferNode extends CoreMethodArrayArgumentsNode {

        @Child private SingleValueCastNode singleValueCastNode;

        protected Object singleValue(VirtualFrame frame, Object[] args) {
            if (singleValueCastNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                singleValueCastNode = insert(SingleValueCastNodeGen.create(null));
            }
            return singleValueCastNode.executeSingleValue(frame, args);
        }

        public abstract Object executeTransferControlTo(VirtualFrame frame, DynamicObject fiber, boolean isYield, Object[] args);

        @Specialization(guards = "isRubyFiber(fiber)")
        protected Object transfer(VirtualFrame frame, DynamicObject fiber, boolean isYield, Object[] args,
                @Cached("create()") GetCurrentRubyThreadNode getCurrentRubyThreadNode,
                @Cached("create()") BranchProfile sameFiberProfile,
                @Cached("create()") BranchProfile errorProfile) {
            if (!Layouts.FIBER.getAlive(fiber)) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().deadFiberCalledError(this));
            }

            final DynamicObject currentThread = getCurrentRubyThreadNode.executeGetRubyThread(frame);
            final FiberManager fiberManager = Layouts.THREAD.getFiberManager(currentThread);
            final DynamicObject sendingFiber = fiberManager.getCurrentFiber();

            if (sendingFiber == fiber) {
                sameFiberProfile.enter();
                throw new RaiseException(coreExceptions().fiberError("double resume", this));
            }

            if (Layouts.FIBER.getRubyThread(fiber) != currentThread) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().fiberError("fiber called across threads", this));
            }


            return singleValue(frame, fiberManager.transferControlTo(sendingFiber, fiber, isYield, args));
        }

    }

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            final DynamicObject thread = getContext().getThreadManager().getCurrentThread();
            final DynamicObjectFactory factory = Layouts.CLASS.getInstanceFactory(rubyClass);
            return Layouts.THREAD.getFiberManager(thread).createFiber(getContext(), thread, factory, null);
        }

    }

    @CoreMethod(names = "initialize", needsBlock = true, unsupportedOperationBehavior = UnsupportedOperationBehavior.ARGUMENT_ERROR)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject initialize(DynamicObject fiber, DynamicObject block) {
            final DynamicObject thread = getContext().getThreadManager().getCurrentThread();
            Layouts.THREAD.getFiberManager(thread).initialize(fiber, block, this);
            return nil();
        }

    }

    @CoreMethod(names = "resume", rest = true)
    public abstract static class ResumeNode extends CoreMethodArrayArgumentsNode {

        @Child private FiberTransferNode fiberTransferNode = FiberNodesFactory.FiberTransferNodeFactory.create(null);

        @Specialization
        public Object resume(VirtualFrame frame, DynamicObject fiberBeingResumed, Object[] args) {
            return fiberTransferNode.executeTransferControlTo(frame, fiberBeingResumed, false, args);
        }

    }

    @CoreMethod(names = "yield", onSingleton = true, rest = true)
    public abstract static class YieldNode extends CoreMethodArrayArgumentsNode {

        @Child private FiberTransferNode fiberTransferNode = FiberNodesFactory.FiberTransferNodeFactory.create(null);

        @Specialization
        public Object yield(VirtualFrame frame, Object[] args,
                @Cached("create()") GetCurrentRubyThreadNode getCurrentRubyThreadNode,
                @Cached("create()") BranchProfile errorProfile) {
            final DynamicObject currentThread = getCurrentRubyThreadNode.executeGetRubyThread(frame);
            final FiberManager fiberManager = Layouts.THREAD.getFiberManager(currentThread);
            final DynamicObject yieldingFiber = fiberManager.getCurrentFiber();
            final DynamicObject fiberYieldedTo = Layouts.FIBER.getLastResumedByFiber(yieldingFiber);

            if (yieldingFiber == fiberManager.getRootFiber() || fiberYieldedTo == null) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().yieldFromRootFiberError(this));
            }

            return fiberTransferNode.executeTransferControlTo(frame, fiberYieldedTo, true, args);
        }

    }

    @CoreMethod(names = "alive?")
    public abstract static class AliveNode extends UnaryCoreMethodNode {

        @Specialization
        public boolean alive(DynamicObject fiber) {
            return Layouts.FIBER.getAlive(fiber);
        }

    }

    @CoreMethod(names = "current", onSingleton = true)
    public abstract static class CurrentNode extends CoreMethodNode {

        @Specialization
        public DynamicObject current(VirtualFrame frame,
                @Cached("create()") GetCurrentRubyThreadNode getCurrentRubyThreadNode) {
            final DynamicObject currentThread = getCurrentRubyThreadNode.executeGetRubyThread(frame);
            return Layouts.THREAD.getFiberManager(currentThread).getCurrentFiber();
        }

    }

    @Primitive(name = "fiber_get_catch_tags", needsSelf = false)
    public static abstract class FiberGetCatchTagsNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public DynamicObject getCatchTags(VirtualFrame frame,
                @Cached("create()") GetCurrentRubyThreadNode getCurrentRubyThreadNode) {
            final DynamicObject currentThread = getCurrentRubyThreadNode.executeGetRubyThread(frame);
            final DynamicObject currentFiber = Layouts.THREAD.getFiberManager(currentThread).getCurrentFiber();
            return Layouts.FIBER.getCatchTags(currentFiber);
        }
    }

}
