/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import org.truffleruby.core.kernel.TruffleKernelNodes.GetSpecialVariableStorage;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.proc.ProcCallTargets;
import org.truffleruby.core.proc.ProcType;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.BreakID;
import org.truffleruby.language.control.FrameOnStackMarker;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.parser.BodyTranslator;

/** Create a Ruby Proc to pass as a block to the called method. The literal block is represented as call targets and a
 * SharedMethodInfo. This is executed at the call site just before dispatch. */
public class BlockDefinitionNode extends RubyContextSourceNode {

    private final ProcType type;
    private final SharedMethodInfo sharedMethodInfo;
    private final ProcCallTargets callTargets;
    private final BreakID breakID;
    private final int frameOnStackMarkerSlot;

    @Child private GetSpecialVariableStorage readSpecialVariableStorageNode;
    @Child private WithoutVisibilityNode withoutVisibilityNode;

    public BlockDefinitionNode(
            ProcType type,
            SharedMethodInfo sharedMethodInfo,
            ProcCallTargets callTargets,
            BreakID breakID,
            int frameOnStackMarkerSlot) {
        assert (type == ProcType.PROC) == (frameOnStackMarkerSlot != BodyTranslator.NO_FRAME_ON_STACK_MARKER);
        this.type = type;
        this.sharedMethodInfo = sharedMethodInfo;
        this.callTargets = callTargets;
        this.breakID = breakID;

        this.frameOnStackMarkerSlot = frameOnStackMarkerSlot;
        readSpecialVariableStorageNode = GetSpecialVariableStorage.create();
    }

    public BreakID getBreakID() {
        return breakID;
    }

    @Override
    public RubyProc execute(VirtualFrame frame) {
        final FrameOnStackMarker frameOnStackMarker;
        if (frameOnStackMarkerSlot != BodyTranslator.NO_FRAME_ON_STACK_MARKER) {
            frameOnStackMarker = (FrameOnStackMarker) frame.getObject(frameOnStackMarkerSlot);
            assert frameOnStackMarker != null;
        } else {
            frameOnStackMarker = null;
        }

        return ProcOperations.createRubyProc(
                coreLibrary().procClass,
                getLanguage().procShape,
                type,
                sharedMethodInfo,
                callTargets,
                frame.materialize(),
                readSpecialVariableStorageNode.execute(frame),
                RubyArguments.getMethod(frame),
                frameOnStackMarker,
                executeWithoutVisibility(RubyArguments.getDeclarationContext(frame)));
    }

    private DeclarationContext executeWithoutVisibility(DeclarationContext ctxIn) {
        if (withoutVisibilityNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            withoutVisibilityNode = insert(WithoutVisibilityNodeGen.create());
        }
        return withoutVisibilityNode.executeWithoutVisibility(ctxIn);
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new BlockDefinitionNode(type, sharedMethodInfo, callTargets, breakID, frameOnStackMarkerSlot);
        return copy.copyFlags(this);
    }

}
