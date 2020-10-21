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

import com.oracle.truffle.api.dsl.GenerateUncached;
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
import com.oracle.truffle.api.frame.MaterializedFrame;

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

    public static FrameSlotAndDepth findFrameSlotOrNull(String identifier, MaterializedFrame frame) {
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
    public static abstract class FindAndReadDeclarationVariableNode extends RubyBaseNode {

        public abstract Object execute(MaterializedFrame frame, String name, Object defaultValue);

        public static FindAndReadDeclarationVariableNode create() {
            return FindAndReadDeclarationVariableNodeGen.create();
        }

        @Specialization(
                guards = { "name == cachedName", "frame.getFrameDescriptor() == cachedDescriptor", "readNode != null" },
                assumptions = "cachedDescriptor.getVersion()")
        protected Object getVariable(MaterializedFrame frame, String name, Object defaultValue,
                @Cached("name") String cachedName,
                @Cached("frame.getFrameDescriptor()") FrameDescriptor cachedDescriptor,
                @Cached("findFrameSlotOrNull(name, frame)") FrameSlotAndDepth slotAndDepth,
                @Cached("createReadNode(slotAndDepth)") ReadFrameSlotNode readNode) {
            return readNode.executeRead(RubyArguments.getDeclarationFrame(frame, slotAndDepth.depth));
        }

        @Specialization(
                guards = { "name == cachedName", "frame.getFrameDescriptor() == cachedDescriptor", "readNode == null" },
                assumptions = "cachedDescriptor.getVersion()")
        protected Object getVariableDefaultValue(MaterializedFrame frame, String name, Object defaultValue,
                @Cached("name") String cachedName,
                @Cached("frame.getFrameDescriptor()") FrameDescriptor cachedDescriptor,
                @Cached("findFrameSlotOrNull(name, frame)") FrameSlotAndDepth slotAndDepth,
                @Cached("createReadNode(slotAndDepth)") ReadFrameSlotNode readNode) {
            return defaultValue;
        }

        @Specialization(replaces = { "getVariable", "getVariableDefaultValue" })
        @TruffleBoundary
        protected Object getVariableSlow(MaterializedFrame frame, String name, Object defaultValue) {
            FrameSlotAndDepth slotAndDepth = findFrameSlotOrNull(name, frame);
            if (slotAndDepth == null) {
                return defaultValue;
            } else {
                return RubyArguments.getDeclarationFrame(frame, slotAndDepth.depth).getValue(slotAndDepth.slot);
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
