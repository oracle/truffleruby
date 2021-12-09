/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.frame.FrameSlot;
import org.truffleruby.language.RubyContextSourceNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class SaveMethodBlockNode extends RubyContextSourceNode {

    /* todo this should be handled by the ReadDescriptorArgumentNode */

    private final FrameSlot slot;

    public SaveMethodBlockNode(FrameSlot slot) {
        this.slot = slot;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        frame.setObject(slot, RubyArguments.getBlock(frame));
        return nil;
    }

}
