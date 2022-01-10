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

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.frame.Frame;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.locals.FindDeclarationVariableNodesFactory.FindAndReadDeclarationVariableNodeGen;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;

public class FindDeclarationVariableNodes {
    public static class FrameSlotAndDepth {
        public final FrameSlot slot;
        public final int depth;

        public FrameSlotAndDepth(FrameSlot slot, int depth) {
            this.slot = slot;
            this.depth = depth;
        }

        public FrameSlot getSlot() {
            return slot;
        }
    }

    public static FrameSlotAndDepth findFrameSlotOrNull(String identifier, Frame frame) {
        CompilerAsserts.neverPartOfCompilation("Must not be called in PE code as the frame would escape");
        int depth = 0;
        while (frame != null) {
            final FrameSlot frameSlot = frame.getFrameDescriptor().findFrameSlot(identifier);
            if (frameSlot != null) {
                return new FrameSlotAndDepth(frameSlot, depth);
            }

            frame = RubyArguments.getDeclarationFrame(frame);
            depth++;
        }
        return null;
    }

    public static FrameSlotAndDepth findFrameSlot(String identifier, FrameDescriptor framedescriptor) {
        final FrameSlot frameSlot = framedescriptor.findFrameSlot(identifier);
        assert frameSlot != null;

        return new FrameSlotAndDepth(frameSlot, 0);
    }

    @ReportPolymorphism
    @GenerateUncached
    @ImportStatic(FindDeclarationVariableNodes.class)
    public abstract static class FindAndReadDeclarationVariableNode extends RubyBaseNode {

        public abstract Object execute(Frame frame, String name, Object defaultValue);

        public static FindAndReadDeclarationVariableNode create() {
            return FindAndReadDeclarationVariableNodeGen.create();
        }

        @Specialization(
                guards = { "name == cachedName", "frame.getFrameDescriptor() == cachedDescriptor", "readNode != null" },
                assumptions = "cachedDescriptor.getVersion()")
        protected Object getVariable(Frame frame, String name, Object defaultValue,
                @Cached("name") String cachedName,
                @Cached("frame.getFrameDescriptor()") FrameDescriptor cachedDescriptor,
                @Cached("findFrameSlotOrNull(name, frame)") FrameSlotAndDepth slotAndDepth,
                @Cached("createReadNode(slotAndDepth)") ReadFrameSlotNode readNode) {
            final Frame storageFrame = RubyArguments.getDeclarationFrame(frame, slotAndDepth.depth);
            return readNode.executeRead(storageFrame);
        }

        @Specialization(
                guards = { "name == cachedName", "frame.getFrameDescriptor() == cachedDescriptor", "readNode == null" },
                assumptions = "cachedDescriptor.getVersion()")
        protected Object getVariableDefaultValue(Frame frame, String name, Object defaultValue,
                @Cached("name") String cachedName,
                @Cached("frame.getFrameDescriptor()") FrameDescriptor cachedDescriptor,
                @Cached("findFrameSlotOrNull(name, frame)") FrameSlotAndDepth slotAndDepth,
                @Cached("createReadNode(slotAndDepth)") ReadFrameSlotNode readNode) {
            return defaultValue;
        }

        @TruffleBoundary
        @Specialization(replaces = { "getVariable", "getVariableDefaultValue" })
        protected Object getVariableSlow(Frame frame, String name, Object defaultValue) {
            FrameSlotAndDepth slotAndDepth = findFrameSlotOrNull(name, frame);
            if (slotAndDepth == null) {
                return defaultValue;
            } else {
                final Frame storageFrame = RubyArguments.getDeclarationFrame(frame, slotAndDepth.depth);
                return storageFrame.getValue(slotAndDepth.slot);
            }
        }

        protected ReadFrameSlotNode createReadNode(FrameSlotAndDepth frameSlot) {
            if (frameSlot == null) {
                return null;
            } else {
                return ReadFrameSlotNodeGen.create(frameSlot.slot);
            }
        }


    }
}
