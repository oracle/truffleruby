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

import org.truffleruby.core.array.AssignableNode;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;

@ImportStatic({ FrameSlotKind.class, RubyGuards.class })
public abstract class WriteFrameSlotNode extends RubyBaseNode implements AssignableNode {

    private final FrameSlot frameSlot;

    public WriteFrameSlotNode(FrameSlot frameSlot) {
        this.frameSlot = frameSlot;
    }

    public abstract void executeWrite(Frame frame, Object value);

    @Override
    public void assign(VirtualFrame frame, Object value) {
        executeWrite(frame, value);
    }

    @Specialization(guards = "isExpectedOrIllegal(frame, Boolean)")
    protected void writeBoolean(Frame frame, boolean value) {
        frame.setBoolean(frameSlot, value);
    }

    @Specialization(guards = "isExpectedOrIllegal(frame, Int)")
    protected void writeInt(Frame frame, int value) {
        frame.setInt(frameSlot, value);
    }

    @Specialization(guards = "isExpectedOrIllegal(frame, Long)")
    protected void writeLong(Frame frame, long value) {
        frame.setLong(frameSlot, value);
    }

    @Specialization(guards = "isExpectedOrIllegal(frame, Double)")
    protected void writeDouble(Frame frame, double value) {
        frame.setDouble(frameSlot, value);
    }

    @Specialization(replaces = { "writeBoolean", "writeInt", "writeLong", "writeDouble" })
    protected void writeObject(Frame frame, Object value) {
        /* No-op if kind is already Object. */
        frame.getFrameDescriptor().setFrameSlotKind(frameSlot, FrameSlotKind.Object);

        frame.setObject(frameSlot, value);
    }

    protected boolean isExpectedOrIllegal(Frame frame, FrameSlotKind expectedKind) {
        final FrameDescriptor descriptor = frame.getFrameDescriptor();
        final FrameSlotKind kind = descriptor.getFrameSlotKind(frameSlot);
        if (kind == expectedKind) {
            return true;
        } else if (kind == FrameSlotKind.Illegal) {
            descriptor.setFrameSlotKind(frameSlot, expectedKind);
            return true;
        }
        return false;
    }

    public final FrameSlot getFrameSlot() {
        return frameSlot;
    }

    @Override
    public AssignableNode toAssignableNode() {
        return this;
    }
}
