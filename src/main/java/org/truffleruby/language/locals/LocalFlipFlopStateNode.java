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

import com.oracle.truffle.api.frame.VirtualFrame;

public class LocalFlipFlopStateNode extends FlipFlopStateNode {

    private final int frameSlot;

    public LocalFlipFlopStateNode(int frameSlot) {
        this.frameSlot = frameSlot;
    }

    @Override
    public boolean getState(VirtualFrame frame) {
        return frame.getBoolean(frameSlot);
    }

    @Override
    public void setState(VirtualFrame frame, boolean state) {
        frame.setBoolean(frameSlot, state);
    }

}
