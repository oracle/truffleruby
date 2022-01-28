/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.locals;

import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;

public abstract class ReadFrameSlotNode extends RubyBaseNode {

    protected final int frameSlot;

    public ReadFrameSlotNode(int slot) {
        this.frameSlot = slot;
    }

    public abstract Object executeRead(Frame frame);

    @Specialization(guards = "frame.isBoolean(frameSlot)")
    protected boolean readBoolean(Frame frame) {
        return frame.getBoolean(frameSlot);
    }

    @Specialization(guards = "frame.isInt(frameSlot)")
    protected int readInt(Frame frame) {
        return frame.getInt(frameSlot);
    }

    @Specialization(guards = "frame.isLong(frameSlot)")
    protected long readLong(Frame frame) {
        return frame.getLong(frameSlot);
    }

    @Specialization(guards = "frame.isDouble(frameSlot)")
    protected double readDouble(Frame frame) {
        return frame.getDouble(frameSlot);
    }

    @Specialization(guards = "frame.isObject(frameSlot)")
    protected Object readObject(Frame frame) {
        return frame.getObject(frameSlot);
    }

}
