/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.locals;

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;

public class WriteDeclarationVariableNode extends WriteLocalNode {

    private final int frameDepth;

    @Child private WriteFrameSlotNode writeFrameSlotNode;

    public WriteDeclarationVariableNode(FrameSlot frameSlot, int frameDepth, RubyNode valueNode) {
        super(frameSlot, valueNode);
        this.frameDepth = frameDepth;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (writeFrameSlotNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            writeFrameSlotNode = insert(WriteFrameSlotNodeGen.create(frameSlot));
        }

        final MaterializedFrame declarationFrame = RubyArguments.getDeclarationFrame(frame, frameDepth);
        final Object value = valueNode.execute(frame);
        return writeFrameSlotNode.executeWrite(declarationFrame, value);
    }

}
