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

import java.util.Set;

import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.interop.ForeignToRubyArgumentsNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.control.FrameOnStackMarker;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;
import org.truffleruby.language.yield.YieldNode;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.SourceSection;

@ExportLibrary(InteropLibrary.class)
public class RubyProc extends RubyDynamicObject implements ObjectGraphNode {

    public final ProcType type;
    public final SharedMethodInfo sharedMethodInfo;
    public final RootCallTarget callTargetForType;
    public final RootCallTarget callTargetForLambdas;
    public final MaterializedFrame declarationFrame;
    public final SpecialVariableStorage declarationStorage;
    public final InternalMethod method;
    public final RubyProc block;
    public final FrameOnStackMarker frameOnStackMarker;
    public final DeclarationContext declarationContext;

    public RubyProc(
            RubyClass rubyClass,
            Shape shape,
            ProcType type,
            SharedMethodInfo sharedMethodInfo,
            RootCallTarget callTargetForType,
            RootCallTarget callTargetForLambdas,
            MaterializedFrame declarationFrame,
            SpecialVariableStorage declarationStorage,
            InternalMethod method,
            RubyProc block,
            FrameOnStackMarker frameOnStackMarker,
            DeclarationContext declarationContext) {
        super(rubyClass, shape);
        this.type = type;
        this.sharedMethodInfo = sharedMethodInfo;
        this.callTargetForType = callTargetForType;
        this.callTargetForLambdas = callTargetForLambdas;
        this.declarationFrame = declarationFrame;
        this.declarationStorage = declarationStorage;
        this.method = method;
        this.block = block;
        this.frameOnStackMarker = frameOnStackMarker;
        this.declarationContext = declarationContext;
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        ObjectGraph.getObjectsInFrame(declarationFrame, reachable);
        ObjectGraph.addProperty(reachable, method);
        ObjectGraph.addProperty(reachable, block);
    }

    // region SourceLocation
    @ExportMessage
    public boolean hasSourceLocation() {
        return true;
    }

    @ExportMessage
    public SourceSection getSourceLocation() {
        return sharedMethodInfo.getSourceSection();
    }
    // endregion

    public int getArityNumber() {
        return sharedMethodInfo.getArity().getArityNumber(type);
    }

    // region Executable
    @ExportMessage
    public boolean isExecutable() {
        return true;
    }

    @ExportMessage
    public Object execute(Object[] arguments,
            @Cached YieldNode yieldNode,
            @Cached ForeignToRubyArgumentsNode foreignToRubyArgumentsNode) {
        return yieldNode.executeDispatch(this, foreignToRubyArgumentsNode.executeConvert(arguments));
    }
    // endregion

}
