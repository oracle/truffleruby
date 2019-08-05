/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.locals;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.MaterializedFrame;

import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.locals.FindDeclarationVariableNodesFactory.FindAndReadDeclarationVariableNodeGen;

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
    @ImportStatic(FindDeclarationVariableNodes.class)
    public static abstract class FindAndReadDeclarationVariableNode extends RubyBaseNode {
        public abstract Object execute(MaterializedFrame frame, String name);
        private final Object defaultValue;

        protected FindAndReadDeclarationVariableNode(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Specialization(guards = { "name == cachedName", "getFrameDescriptor(frame) == cachedDescriptor", "readNode != null" })
        public Object getVariable(MaterializedFrame frame, String name,
                @Cached("name") String cachedName,
                @Cached("getFrameDescriptor(frame)") FrameDescriptor cachedDescriptor,
                @Cached("findFrameSlotOrNull(name, frame)") FrameSlotAndDepth slotAndDepth,
                @Cached("createReadNode(slotAndDepth)") ReadFrameSlotNode readNode) {
            return readNode.executeRead(RubyArguments.getDeclarationFrame(frame, slotAndDepth.depth));
        }

        @Specialization(guards = { "name == cachedName", "getFrameDescriptor(frame) == cachedDescriptor", "readNode == null" })
        public Object getVariableDefaultValue(MaterializedFrame frame, String name,
                @Cached("name") String cachedName,
                @Cached("getFrameDescriptor(frame)") FrameDescriptor cachedDescriptor,
                @Cached("findFrameSlotOrNull(name, frame)") FrameSlotAndDepth slotAndDepth,
                @Cached("createReadNode(slotAndDepth)") ReadFrameSlotNode readNode) {
            return defaultValue;
        }

        @Specialization
        @TruffleBoundary
        public Object getVariableSlow(MaterializedFrame frame, String name) {
            FrameSlotAndDepth slotAndDepth = findFrameSlotOrNull(name, frame);
            if (slotAndDepth == null) {
                return defaultValue;
            } else {
                return RubyArguments.getDeclarationFrame(frame, slotAndDepth.depth).getValue(slotAndDepth.slot);
            }
        }

        protected static FrameDescriptor getFrameDescriptor(Frame frame) {
            return frame.getFrameDescriptor();
        }

        protected ReadFrameSlotNode createReadNode(FrameSlotAndDepth frameSlot) {
            if (frameSlot == null) {
                return null;
            } else {
                return ReadFrameSlotNodeGen.create(frameSlot.slot);
            }
        }

        public static FindAndReadDeclarationVariableNode create(Object defaultValue) {
            return FindAndReadDeclarationVariableNodeGen.create(defaultValue);
        }

        public static FindAndReadDeclarationVariableNode create() {
            return FindAndReadDeclarationVariableNodeGen.create(null);
        }
    }
}
