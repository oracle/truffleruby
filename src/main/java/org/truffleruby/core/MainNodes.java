/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.core.inlined.AlwaysInlinedMethodNode;
import org.truffleruby.core.module.ModuleNodes;
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
import com.oracle.truffle.api.profiles.BranchProfile;

@CoreModule(value = "main", isClass = true)
public abstract class MainNodes {

    @GenerateUncached
    @CoreMethod(names = "public", rest = true, visibility = Visibility.PRIVATE, alwaysInlined = true)
    public abstract static class PublicNode extends AlwaysInlinedMethodNode {
        @Specialization
        protected Object forward(Frame callerFrame, Object self, Object[] args, Object block, RootCallTarget target,
                @Cached ModuleNodes.PublicNode publicNode,
                @CachedContext(RubyLanguage.class) RubyContext context) {
            return publicNode.execute(callerFrame, context.getCoreLibrary().objectClass, args, block, target);
        }
    }

    @GenerateUncached
    @CoreMethod(names = "private", rest = true, visibility = Visibility.PRIVATE, alwaysInlined = true)
    public abstract static class PrivateNode extends AlwaysInlinedMethodNode {
        @Specialization
        protected Object forward(Frame callerFrame, Object self, Object[] args, Object block, RootCallTarget target,
                @Cached ModuleNodes.PrivateNode privateNode,
                @CachedContext(RubyLanguage.class) RubyContext context) {
            return privateNode.execute(callerFrame, context.getCoreLibrary().objectClass, args, block, target);
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
            final Frame callerFrame = getContext().getCallStack().getCallerFrame(FrameAccess.READ_ONLY);
            final String name = RubyArguments.getMethod(callerFrame).getSharedMethodInfo().getBacktraceName();
            return name.equals("<main>") || name.startsWith("<top ");
        }

    }

}
