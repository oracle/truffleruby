/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.inlined;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.proc.ProcType;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.methods.BlockDefinitionNode;
import org.truffleruby.parser.MethodTranslator;

/** Wraps a {@link BlockDefinitionNode} to convert the returned block (which should be a {@link ProcType#LAMBDA}) to be
 * a proc.
 *
 * <p>
 * When we encounter a function named {@code lambda} which is called with a block, we speculatively create a lambda call
 * target for that block in {@link MethodTranslator}. But if that method does not refer to {@code Kernel#lambda}, then a
 * proc call target is needed instead. This node is thus needed to "deoptimize" such cases. */
public class LambdaToProcNode extends RubyContextSourceNode {

    @Child private BlockDefinitionNode blockNode;

    public LambdaToProcNode(BlockDefinitionNode blockNode) {
        this.blockNode = blockNode;
    }

    @Override
    public RubyProc execute(VirtualFrame frame) {
        final RubyProc block = blockNode.execute(frame);
        assert block.type == ProcType.LAMBDA;
        return ProcOperations.createProcFromBlock(getContext(), getLanguage(), block);
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new LambdaToProcNode((BlockDefinitionNode) blockNode.cloneUninitialized());
        return copy.copyFlags(this);
    }

}
