/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.core.module.ModuleNodes;
import org.truffleruby.core.module.ModuleNodesFactory;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.UsingNode;
import org.truffleruby.language.methods.UsingNodeGen;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

@CoreModule(value = "main", isClass = true)
public abstract class MainNodes {

    @CoreMethod(names = "public", rest = true, needsSelf = false, visibility = Visibility.PRIVATE)
    public abstract static class PublicNode extends CoreMethodArrayArgumentsNode {

        @Child private ModuleNodes.PublicNode publicNode = ModuleNodesFactory.PublicNodeFactory.create(null);

        @Specialization
        protected RubyModule doPublic(VirtualFrame frame, Object[] args) {
            return publicNode.executePublic(frame, coreLibrary().objectClass, args);
        }
    }

    @CoreMethod(names = "private", rest = true, needsSelf = false, visibility = Visibility.PRIVATE)
    public abstract static class PrivateNode extends CoreMethodArrayArgumentsNode {

        @Child private ModuleNodes.PrivateNode privateNode = ModuleNodesFactory.PrivateNodeFactory.create(null);

        @Specialization
        protected RubyModule doPrivate(VirtualFrame frame, Object[] args) {
            return privateNode.executePrivate(frame, coreLibrary().objectClass, args);
        }
    }

    @CoreMethod(names = "using", required = 1, needsSelf = false)
    public abstract static class MainUsingNode extends CoreMethodArrayArgumentsNode {

        @Child private UsingNode usingNode = UsingNodeGen.create();

        @Specialization
        protected Object mainUsing(RubyModule refinementModule,
                @Cached BranchProfile errorProfile) {
            if (!isCalledFromTopLevel()) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().runtimeError("main.using is permitted only at toplevel", this));
            }
            usingNode.executeUsing(refinementModule);
            return nil;
        }

        @TruffleBoundary
        private boolean isCalledFromTopLevel() {
            final Frame callerFrame = getContext().getCallStack().getCallerFrameIgnoringSend(FrameAccess.READ_ONLY);
            final String name = RubyArguments.getMethod(callerFrame).getSharedMethodInfo().getName();
            return name.equals("<main>") || name.startsWith("<top ");
        }

    }

}
