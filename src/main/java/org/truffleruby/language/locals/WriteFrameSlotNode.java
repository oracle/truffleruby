/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.locals;

import org.truffleruby.language.RubyBaseWithoutContextNode;
import org.truffleruby.language.RubyGuards;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;

@ImportStatic(RubyGuards.class)
public abstract class WriteFrameSlotNode extends RubyBaseWithoutContextNode {

    private final FrameSlot frameSlot;

    public WriteFrameSlotNode(FrameSlot frameSlot) {
        this.frameSlot = frameSlot;
    }

    public abstract Object executeWrite(Frame frame, Object value);

    @Specialization(guards = "checkBooleanKind(frame)")
    protected boolean writeBoolean(Frame frame, boolean value) {
        frame.setBoolean(frameSlot, value);
        return value;
    }

    @Specialization(guards = "checkIntegerKind(frame)")
    protected int writeInteger(Frame frame, int value) {
        frame.setInt(frameSlot, value);
        return value;
    }

    @Specialization(guards = "checkLongKind(frame)")
    protected long writeLong(Frame frame, long value) {
        frame.setLong(frameSlot, value);
        return value;
    }

    @Specialization(guards = "checkDoubleKind(frame)")
    protected double writeDouble(Frame frame, double value) {
        frame.setDouble(frameSlot, value);
        return value;
    }

    @Specialization(guards = "checkObjectKind(frame)", replaces = { "writeBoolean", "writeInteger", "writeLong", "writeDouble" })
    protected Object writeObject(Frame frame, Object value) {
        frame.setObject(frameSlot, value);
        return value;
    }

    protected boolean checkBooleanKind(Frame frame) {
        return checkKind(frame, FrameSlotKind.Boolean);
    }

    protected boolean checkIntegerKind(Frame frame) {
        return checkKind(frame, FrameSlotKind.Int);
    }

    protected boolean checkLongKind(Frame frame) {
        return checkKind(frame, FrameSlotKind.Long);
    }

    protected boolean checkDoubleKind(Frame frame) {
        return checkKind(frame, FrameSlotKind.Double);
    }

    protected boolean checkObjectKind(Frame frame) {
        frame.getFrameDescriptor().setFrameSlotKind(frameSlot, FrameSlotKind.Object);
        return true;
    }

    private boolean checkKind(Frame frame, FrameSlotKind kind) {
        if (frame.getFrameDescriptor().getFrameSlotKind(frameSlot) == kind) {
            return true;
        } else {
            return initialSetKind(frame, kind);
        }
    }

    private boolean initialSetKind(Frame frame, FrameSlotKind kind) {
        if (frame.getFrameDescriptor().getFrameSlotKind(frameSlot) == FrameSlotKind.Illegal) {
            frame.getFrameDescriptor().setFrameSlotKind(frameSlot, kind);
            return true;
        }

        return false;
    }

    public final FrameSlot getFrameSlot() {
        return frameSlot;
    }

}
