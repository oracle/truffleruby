/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.proc;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import org.truffleruby.Layouts;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.FrameOnStackMarker;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;

public abstract class ProcOperations {

    public static Object[] packArguments(DynamicObject proc, Object... args) {
        return RubyArguments.pack(
                Layouts.PROC.getDeclarationFrame(proc),
                null,
                Layouts.PROC.getMethod(proc),
                DeclarationContext.BLOCK,
                Layouts.PROC.getFrameOnStackMarker(proc),
                Layouts.PROC.getSelf(proc),
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
            CallTarget callTargetForProcs,
            CallTarget callTargetForLambdas,
            MaterializedFrame declarationFrame,
            InternalMethod method,
            Object self, DynamicObject block,
            FrameOnStackMarker frameOnStackMarker) {
        assert block == null || RubyGuards.isRubyProc(block);

        final CallTarget callTargetForType;

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

        return Layouts.PROC.createProc(
                instanceFactory,
                type,
                sharedMethodInfo,
                callTargetForType,
                callTargetForLambdas,
                declarationFrame,
                method,
                self,
                block,
                frameOnStackMarker);
    }

}
