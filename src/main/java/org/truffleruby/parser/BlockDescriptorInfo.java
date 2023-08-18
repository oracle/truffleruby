/*
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;

/** This is the {@link FrameDescriptor#getInfo() descriptor info} for blocks. The descriptor info for methods is an
 * {@link SpecialVariableStorage#getAssumption(FrameDescriptor) Assumption}. */
public final class BlockDescriptorInfo {

    @ExplodeLoop
    public static FrameDescriptor getDeclarationFrameDescriptor(FrameDescriptor topDescriptor, int depth) {
        assert depth > 0;
        FrameDescriptor descriptor = topDescriptor;
        for (int i = 0; i < depth; i++) {
            descriptor = ((BlockDescriptorInfo) descriptor.getInfo()).getParentDescriptor();
        }
        return descriptor;
    }

    @CompilationFinal private FrameDescriptor parentDescriptor;
    private final Assumption specialVariableAssumption;

    public BlockDescriptorInfo(Assumption specialVariableAssumption) {
        assert SpecialVariableStorage.isSpecialVariableAssumption(specialVariableAssumption);
        this.specialVariableAssumption = specialVariableAssumption;
    }

    public BlockDescriptorInfo(FrameDescriptor parentDescriptor) {
        this.parentDescriptor = parentDescriptor;
        this.specialVariableAssumption = getSpecialVariableAssumptionFromDescriptor(parentDescriptor);
    }

    private Assumption getSpecialVariableAssumptionFromDescriptor(FrameDescriptor descriptor) {
        if (descriptor.getInfo() instanceof BlockDescriptorInfo blockDescriptorInfo) {
            return blockDescriptorInfo.getSpecialVariableAssumption();
        } else {
            return SpecialVariableStorage.getAssumption(descriptor);
        }
    }

    public FrameDescriptor getParentDescriptor() {
        assert parentDescriptor != null;
        return parentDescriptor;
    }

    void setParentDescriptor(FrameDescriptor parentDescriptor) {
        assert this.parentDescriptor == null;
        this.parentDescriptor = parentDescriptor;
    }

    public Assumption getSpecialVariableAssumption() {
        assert specialVariableAssumption != null;
        return specialVariableAssumption;
    }
}
