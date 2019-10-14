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

import org.truffleruby.language.arguments.RubyArguments;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotTypeException;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;

public class DeclarationFlipFlopStateNode extends FlipFlopStateNode {

    private final int frameLevel;
    private final FrameSlot frameSlot;

    public DeclarationFlipFlopStateNode(int frameLevel, FrameSlot frameSlot) {
        this.frameLevel = frameLevel;
        this.frameSlot = frameSlot;
    }

    @Override
    public boolean getState(VirtualFrame frame) {
        final MaterializedFrame declarationFrame = RubyArguments.getDeclarationFrame(frame, frameLevel);

        try {
            return declarationFrame.getBoolean(frameSlot);
        } catch (FrameSlotTypeException e) {
            throw new IllegalStateException();
        }
    }

    @Override
    public void setState(VirtualFrame frame, boolean state) {
        final MaterializedFrame declarationFrame = RubyArguments.getDeclarationFrame(frame, frameLevel);
        declarationFrame.setBoolean(frameSlot, state);
    }

}
