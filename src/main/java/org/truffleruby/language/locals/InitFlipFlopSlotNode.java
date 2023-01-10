/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.locals;

import org.truffleruby.language.RubyContextSourceNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.RubyNode;

public class InitFlipFlopSlotNode extends RubyContextSourceNode {

    private final int frameSlot;

    public InitFlipFlopSlotNode(int frameSlot) {
        this.frameSlot = frameSlot;
    }

    @Override
    public void doExecuteVoid(VirtualFrame frame) {
        frame.setBoolean(frameSlot, false);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        doExecuteVoid(frame);
        return null;
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new InitFlipFlopSlotNode(frameSlot);
        return copy.copyFlags(this);
    }

}
