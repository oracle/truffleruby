/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.proc;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.object.dsl.Nullable;
import org.truffleruby.interop.messages.ProcMessages;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.control.FrameOnStackMarker;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import java.util.Set;

public class RubyProc extends RubyDynamicObject implements ObjectGraphNode {

    public final ProcType type;
    public final SharedMethodInfo sharedMethodInfo;
    public final RootCallTarget callTargetForType;
    public final RootCallTarget callTargetForLambdas;
    public final MaterializedFrame declarationFrame;
    public final InternalMethod method;
    public final RubyProc block;
    public final FrameOnStackMarker frameOnStackMarker;
    public final DeclarationContext declarationContext;

    public RubyProc(
            Shape shape,
            ProcType type,
            SharedMethodInfo sharedMethodInfo,
            RootCallTarget callTargetForType,
            RootCallTarget callTargetForLambdas,
            MaterializedFrame declarationFrame,
            @Nullable InternalMethod method,
            @Nullable RubyProc block,
            @Nullable FrameOnStackMarker frameOnStackMarker,
            DeclarationContext declarationContext) {
        super(shape);
        this.type = type;
        this.sharedMethodInfo = sharedMethodInfo;
        this.callTargetForType = callTargetForType;
        this.callTargetForLambdas = callTargetForLambdas;
        this.declarationFrame = declarationFrame;
        this.method = method;
        this.block = block;
        this.frameOnStackMarker = frameOnStackMarker;
        this.declarationContext = declarationContext;
    }

    @Override
    public Class<?> dispatch() {
        return ProcMessages.class;
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        ObjectGraph.addProperty(reachable, declarationFrame);
        ObjectGraph.addProperty(reachable, method);
        ObjectGraph.addProperty(reachable, block);
    }
}
