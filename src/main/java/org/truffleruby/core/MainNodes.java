/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.core.module.ModuleNodes;
import org.truffleruby.core.module.ModuleNodesFactory;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.UsingNode;
import org.truffleruby.language.methods.UsingNodeGen;

@CoreClass("main")
public abstract class MainNodes {

    @CoreMethod(names = "public", rest = true, needsSelf = false, visibility = Visibility.PRIVATE)
    public abstract static class PublicNode extends CoreMethodArrayArgumentsNode {

        @Child private ModuleNodes.PublicNode publicNode = ModuleNodesFactory.PublicNodeFactory.create(null);

        @Specialization
        public DynamicObject doPublic(VirtualFrame frame, Object[] args) {
            final DynamicObject object = coreLibrary().getObjectClass();
            return publicNode.executePublic(frame, object, args);
        }
    }

    @CoreMethod(names = "private", rest = true, needsSelf = false, visibility = Visibility.PRIVATE)
    public abstract static class PrivateNode extends CoreMethodArrayArgumentsNode {

        @Child private ModuleNodes.PrivateNode privateNode = ModuleNodesFactory.PrivateNodeFactory.create(null);

        @Specialization
        public DynamicObject doPrivate(VirtualFrame frame, Object[] args) {
            final DynamicObject object = coreLibrary().getObjectClass();
            return privateNode.executePrivate(frame, object, args);
        }
    }

    @CoreMethod(names = "using", required = 1, needsSelf = false)
    public abstract static class MainUsingNode extends CoreMethodArrayArgumentsNode {

        @Child private UsingNode usingNode = UsingNodeGen.create();

        @Specialization(guards = "isRubyModule(refinementModule)")
        public DynamicObject mainUsing(DynamicObject refinementModule,
                @Cached("create()") BranchProfile errorProfile) {
            if (!isCalledFromTopLevel()) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().runtimeError("main.using is permitted only at toplevel", this));
            }
            usingNode.executeUsing(refinementModule);
            return nil();
        }

        @TruffleBoundary
        private boolean isCalledFromTopLevel() {
            final Frame callerFrame = getContext().getCallStack().getCallerFrameIgnoringSend().getFrame(FrameAccess.READ_ONLY);
            final String name = RubyArguments.getMethod(callerFrame).getSharedMethodInfo().getName();
            return name.equals("<main>") || name.startsWith("<top ");
        }

    }

}
