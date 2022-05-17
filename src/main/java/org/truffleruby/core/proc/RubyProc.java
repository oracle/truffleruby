/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved. This
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
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.interop.ForeignToRubyArgumentsNode;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.control.FrameOnStackMarker;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;
import org.truffleruby.language.yield.CallBlockNode;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.parser.ArgumentDescriptor;

@ExportLibrary(InteropLibrary.class)
public class RubyProc extends RubyDynamicObject implements ObjectGraphNode {

    // Accessed for calling a RubyProc
    public final RootCallTarget callTarget;
    public final MaterializedFrame declarationFrame;
    public final InternalMethod method;
    public final FrameOnStackMarker frameOnStackMarker;
    public final DeclarationContext declarationContext;
    // Not accessed for calling a RubyProc
    public final ProcType type;
    public final ProcCallTargets callTargets;
    /** Can differ from {@link SharedMethodInfo#getArity()} */
    public final Arity arity;
    /** Can differ from {@link SharedMethodInfo#getRawArgumentDescriptors()} */
    public final ArgumentDescriptor[] argumentDescriptors;
    public final SpecialVariableStorage declarationVariables;
    public final Object block;

    public RubyProc(
            RubyClass rubyClass,
            Shape shape,
            ProcType type,
            Arity arity,
            ArgumentDescriptor[] argumentDescriptors,
            ProcCallTargets callTargets,
            RootCallTarget callTarget,
            MaterializedFrame declarationFrame,
            SpecialVariableStorage declarationVariables,
            InternalMethod method,
            Object block,
            FrameOnStackMarker frameOnStackMarker,
            DeclarationContext declarationContext) {
        super(rubyClass, shape);
        assert block instanceof Nil || block instanceof RubyProc : StringUtils.toString(block);
        this.type = type;
        this.arity = arity;
        this.argumentDescriptors = argumentDescriptors;
        this.callTargets = callTargets;
        this.callTarget = callTarget;
        this.declarationFrame = declarationFrame;
        this.declarationVariables = declarationVariables;
        this.method = method;
        this.block = block;
        this.frameOnStackMarker = frameOnStackMarker;
        this.declarationContext = declarationContext;
    }

    public RubyProc withArity(Arity newArity, ArgumentDescriptor[] newArgumentDescriptors, RubyClass newRubyClass,
            Shape newShape) {
        return new RubyProc(
                newRubyClass,
                newShape,
                type,
                newArity,
                newArgumentDescriptors,
                callTargets,
                callTarget,
                declarationFrame,
                declarationVariables,
                method,
                block,
                frameOnStackMarker,
                declarationContext);
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        ObjectGraph.getObjectsInFrame(declarationFrame, reachable);
        ObjectGraph.addProperty(reachable, method);
        ObjectGraph.addProperty(reachable, block);
    }

    public boolean isLambda() {
        // The field is public, but the function is needed for use in guards.
        return type == ProcType.LAMBDA;
    }

    public boolean isProc() {
        // The field is public, but the function is needed for use in guards.
        return type == ProcType.PROC;
    }

    public SharedMethodInfo getSharedMethodInfo() {
        return RubyRootNode.of(callTarget).getSharedMethodInfo();
    }

    public int getArityNumber() {
        return arity.getArityNumber(type);
    }

    public ArgumentDescriptor[] getArgumentDescriptors() {
        return argumentDescriptors == null ? arity.toAnonymousArgumentDescriptors() : argumentDescriptors;
    }

    // region SourceLocation
    @ExportMessage
    public boolean hasSourceLocation() {
        return true;
    }

    @ExportMessage
    public SourceSection getSourceLocation() {
        return getSharedMethodInfo().getSourceSection();
    }
    // endregion

    // region Executable
    @ExportMessage
    public boolean isExecutable() {
        return true;
    }

    @ExportMessage
    public Object execute(Object[] arguments,
            @Cached CallBlockNode yieldNode,
            @Cached ForeignToRubyArgumentsNode foreignToRubyArgumentsNode) {
        return yieldNode.yield(this, foreignToRubyArgumentsNode.executeConvert(arguments));
    }
    // endregion
}
