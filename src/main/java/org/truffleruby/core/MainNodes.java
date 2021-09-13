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
import com.oracle.truffle.api.dsl.GenerateUncached;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.core.inlined.AlwaysInlinedMethodNode;
import org.truffleruby.core.module.ModuleNodes;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.UsingNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.profiles.BranchProfile;

@CoreModule(value = "main", isClass = true)
public abstract class MainNodes {

    @GenerateUncached
    @CoreMethod(names = "public", rest = true, visibility = Visibility.PRIVATE, alwaysInlined = true)
    public abstract static class PublicNode extends AlwaysInlinedMethodNode {
        @Specialization
        protected Object forward(Frame callerFrame, Object self, Object[] args, Object block, RootCallTarget target,
                @Cached ModuleNodes.PublicNode publicNode) {
            return publicNode.execute(callerFrame, coreLibrary().objectClass, args, block, target);
        }
    }

    @GenerateUncached
    @CoreMethod(names = "private", rest = true, visibility = Visibility.PRIVATE, alwaysInlined = true)
    public abstract static class PrivateNode extends AlwaysInlinedMethodNode {
        @Specialization
        protected Object forward(Frame callerFrame, Object self, Object[] args, Object block, RootCallTarget target,
                @Cached ModuleNodes.PrivateNode privateNode) {
            return privateNode.execute(callerFrame, coreLibrary().objectClass, args, block, target);
        }
    }

    @GenerateUncached
    @CoreMethod(names = "using", required = 1, alwaysInlined = true)
    public abstract static class MainUsingNode extends UsingNode {
        @Specialization
        protected Object mainUsing(Frame callerFrame, Object self, Object[] args, Object block, RootCallTarget target,
                @Cached BranchProfile errorProfile) {
            needCallerFrame(callerFrame, target);
            final Object refinementModule = args[0];
            final InternalMethod callerMethod = RubyArguments.getMethod(callerFrame);
            if (!isCalledFromTopLevel(callerMethod)) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().runtimeError("main.using is permitted only at toplevel", this));
            }
            using(callerFrame, refinementModule, errorProfile);
            return nil;
        }

        @TruffleBoundary
        private boolean isCalledFromTopLevel(InternalMethod callerMethod) {
            final String name = callerMethod.getSharedMethodInfo().getBacktraceName();
            return name.equals("<main>") || name.startsWith("<top ");
        }
    }

}
