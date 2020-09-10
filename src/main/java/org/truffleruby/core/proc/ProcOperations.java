/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.proc;

import com.oracle.truffle.api.object.Shape;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.FrameOnStackMarker;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.MaterializedFrame;

public abstract class ProcOperations {

    private static Object[] packArguments(RubyProc proc, Object... args) {
        return RubyArguments.pack(
                proc.declarationFrame,
                null,
                null,
                proc.method,
                proc.frameOnStackMarker,
                getSelf(proc),
                proc.block,
                args);
    }

    /** Only use for Proc called with no Truffle frame above, i.e. a root call. */
    public static Object rootCall(RubyProc proc, Object... args) {
        // We cannot break out of a block without a frame above. This is particularly important for
        // Thread.new as otherwise the flag could be set too late (after returning from Thread.new
        // and not when the new Thread starts executing).
        final FrameOnStackMarker frameOnStackMarker = proc.frameOnStackMarker;
        if (frameOnStackMarker != null) {
            frameOnStackMarker.setNoLongerOnStack();
        }

        return proc.callTargetForType.call(packArguments(proc, args));
    }

    public static RubyProc createRubyProc(
            RubyClass rubyClass,
            Shape procShape,
            ProcType type,
            SharedMethodInfo sharedMethodInfo,
            RootCallTarget callTargetForProcs,
            RootCallTarget callTargetForLambdas,
            MaterializedFrame declarationFrame,
            SpecialVariableStorage storage,
            InternalMethod method,
            RubyProc block,
            FrameOnStackMarker frameOnStackMarker,
            DeclarationContext declarationContext) {

        final RootCallTarget callTargetForType;

        switch (type) {
            case PROC:
                callTargetForType = callTargetForProcs;
                break;
            case LAMBDA:
                callTargetForType = callTargetForLambdas;
                break;
            default:
                throw new IllegalArgumentException();
        }

        return new RubyProc(
                rubyClass,
                procShape,
                type,
                sharedMethodInfo,
                callTargetForType,
                callTargetForLambdas,
                declarationFrame,
                storage,
                method,
                block,
                frameOnStackMarker,
                declarationContext);

        // TODO(norswap, 04 Aug 2020): do allocation tracing (normally via AllocateHelper)?
    }

    public static RubyProc createLambdaFromBlock(RubyContext context, RubyProc block) {
        return ProcOperations.createRubyProc(
                context.getCoreLibrary().procClass,
                RubyLanguage.procShape,
                ProcType.LAMBDA,
                block.sharedMethodInfo,
                block.callTargetForLambdas,
                block.callTargetForLambdas,
                block.declarationFrame,
                block.declarationStorage,
                block.method,
                block.block,
                null,
                block.declarationContext);
    }

    public static Object getSelf(RubyProc proc) {
        return RubyArguments.getSelf(proc.declarationFrame);
    }
}
