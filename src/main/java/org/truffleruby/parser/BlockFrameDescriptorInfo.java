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

public final class BlockFrameDescriptorInfo {

    @ExplodeLoop
    public static FrameDescriptor getDeclarationFrameDescriptor(FrameDescriptor topDescriptor, int depth) {
        assert depth > 0;
        FrameDescriptor descriptor = topDescriptor;
        for (int i = 0; i < depth; i++) {
            descriptor = ((BlockFrameDescriptorInfo) descriptor.getInfo()).getDescriptor();
        }
        return descriptor;
    }

    @CompilationFinal private FrameDescriptor descriptor;
    private final Assumption specialVariableAssumption;

    public BlockFrameDescriptorInfo(Assumption specialVariableAssumption) {
        assert SpecialVariableStorage.isSpecialVariableAssumption(specialVariableAssumption);
        this.specialVariableAssumption = specialVariableAssumption;
    }

    public BlockFrameDescriptorInfo(FrameDescriptor descriptor) {
        this.descriptor = descriptor;
        this.specialVariableAssumption = getSpecialVariableAssumptionFromDescriptor(descriptor);
    }

    private Assumption getSpecialVariableAssumptionFromDescriptor(FrameDescriptor descriptor) {
        if (descriptor.getInfo() instanceof BlockFrameDescriptorInfo blockFrameDescriptorInfo) {
            return blockFrameDescriptorInfo.getSpecialVariableAssumption();
        } else {
            return SpecialVariableStorage.getAssumption(descriptor);
        }
    }

    public FrameDescriptor getDescriptor() {
        assert descriptor != null;
        return descriptor;
    }

    void set(FrameDescriptor descriptor) {
        assert this.descriptor == null;
        this.descriptor = descriptor;
    }

    public Assumption getSpecialVariableAssumption() {
        assert specialVariableAssumption != null;
        return specialVariableAssumption;
    }
}
