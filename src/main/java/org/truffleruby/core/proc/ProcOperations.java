/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.proc;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.FrameOnStackMarker;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;

public abstract class ProcOperations {

    private static Object[] packArguments(DynamicObject proc, Object... args) {
        return RubyArguments.pack(
                Layouts.PROC.getDeclarationFrame(proc),
                null,
                Layouts.PROC.getMethod(proc),
                Layouts.PROC.getFrameOnStackMarker(proc),
                getSelf(proc),
                Layouts.PROC.getBlock(proc),
                args);
    }

    /** Only use for Proc called with no Truffle frame above, i.e. a root call. */
    public static Object rootCall(DynamicObject proc, Object... args) {
        // We cannot break out of a block without a frame above. This is particularly important for
        // Thread.new as otherwise the flag could be set too late (after returning from Thread.new
        // and not when the new Thread starts executing).
        final FrameOnStackMarker frameOnStackMarker = Layouts.PROC.getFrameOnStackMarker(proc);
        if (frameOnStackMarker != null) {
            frameOnStackMarker.setNoLongerOnStack();
        }

        return Layouts.PROC.getCallTargetForType(proc).call(packArguments(proc, args));
    }

    public static DynamicObject createRubyProc(
            DynamicObjectFactory instanceFactory,
            ProcType type,
            SharedMethodInfo sharedMethodInfo,
            RootCallTarget callTargetForProcs,
            RootCallTarget callTargetForLambdas,
            MaterializedFrame declarationFrame,
            InternalMethod method,
            DynamicObject block,
            FrameOnStackMarker frameOnStackMarker,
            DeclarationContext declarationContext) {
        assert block == null || RubyGuards.isRubyProc(block);

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

        return instanceFactory.newInstance(Layouts.PROC.build(
                type,
                sharedMethodInfo,
                callTargetForType,
                callTargetForLambdas,
                declarationFrame,
                method,
                block,
                frameOnStackMarker,
                declarationContext));
    }

    public static DynamicObject createLambdaFromBlock(RubyContext context, DynamicObject block) {
        return ProcOperations.createRubyProc(
                context.getCoreLibrary().getProcFactory(),
                ProcType.LAMBDA,
                Layouts.PROC.getSharedMethodInfo(block),
                Layouts.PROC.getCallTargetForLambdas(block),
                Layouts.PROC.getCallTargetForLambdas(block),
                Layouts.PROC.getDeclarationFrame(block),
                Layouts.PROC.getMethod(block),
                Layouts.PROC.getBlock(block),
                null,
                Layouts.PROC.getDeclarationContext(block));
    }

    public static Object getSelf(DynamicObject proc) {
        return RubyArguments.getSelf(Layouts.PROC.getDeclarationFrame(proc));
    }
}
