/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.binding;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.MaterializedFrame;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.locals.FindDeclarationVariableNodes;
import org.truffleruby.language.locals.WriteFrameSlotNode;
import org.truffleruby.language.locals.WriteFrameSlotNodeGen;

import static org.truffleruby.core.binding.BindingNodes.NEW_VAR_INDEX;
import static org.truffleruby.core.binding.BindingNodes.getFrameDescriptor;
import static org.truffleruby.core.binding.BindingNodes.newFrame;
import static org.truffleruby.core.binding.BindingNodes.newFrameDescriptor;

@GenerateUncached
@ImportStatic({ BindingNodes.class, FindDeclarationVariableNodes.class })
public abstract class LocalVariableSetNode extends RubyBaseNode {

    public abstract Object execute(RubyBinding binding, String name, Object value);

    @Specialization(
            guards = {
                    "name == cachedName",
                    "!isHiddenVariable(cachedName)",
                    "getFrameDescriptor(binding) == cachedFrameDescriptor",
                    "cachedFrameSlot != null" },
            limit = "getCacheLimit()")
    protected Object localVariableSetCached(RubyBinding binding, String name, Object value,
            @Cached("name") String cachedName,
            @Cached("getFrameDescriptor(binding)") FrameDescriptor cachedFrameDescriptor,
            @Cached("findFrameSlotOrNull(name, binding.getFrame())") FindDeclarationVariableNodes.FrameSlotAndDepth cachedFrameSlot,
            @Cached("createWriteNode(cachedFrameSlot.slot)") WriteFrameSlotNode writeLocalVariableNode) {
        final MaterializedFrame frame = RubyArguments
                .getDeclarationFrame(binding.getFrame(), cachedFrameSlot.depth);
        writeLocalVariableNode.executeWrite(frame, value);
        return value;
    }

    @Specialization(
            guards = {
                    "name == cachedName",
                    "!isHiddenVariable(cachedName)",
                    "getFrameDescriptor(binding) == cachedFrameDescriptor",
                    "cachedFrameSlot == null" },
            limit = "getCacheLimit()")
    protected Object localVariableSetNewCached(RubyBinding binding, String name, Object value,
            @Cached("name") String cachedName,
            @Cached("getFrameDescriptor(binding)") FrameDescriptor cachedFrameDescriptor,
            @Cached("findFrameSlotOrNull(name, binding.getFrame())") FindDeclarationVariableNodes.FrameSlotAndDepth cachedFrameSlot,
            @Cached("newFrameDescriptor(cachedFrameDescriptor, name)") FrameDescriptor newDescriptor,
            @Cached("createWriteNode(NEW_VAR_INDEX)") WriteFrameSlotNode writeLocalVariableNode) {
        final MaterializedFrame frame = newFrame(binding, newDescriptor);
        writeLocalVariableNode.executeWrite(frame, value);
        return value;
    }

    @TruffleBoundary
    @Specialization(
            guards = "!isHiddenVariable(name)",
            replaces = { "localVariableSetCached", "localVariableSetNewCached" })
    protected Object localVariableSetUncached(RubyBinding binding, String name, Object value) {
        MaterializedFrame frame = binding.getFrame();
        final FindDeclarationVariableNodes.FrameSlotAndDepth frameSlot = FindDeclarationVariableNodes
                .findFrameSlotOrNull(name, frame);
        final int slot;
        if (frameSlot != null) {
            frame = RubyArguments.getDeclarationFrame(frame, frameSlot.depth);
            slot = frameSlot.slot;
        } else {
            var newDescriptor = newFrameDescriptor(getFrameDescriptor(binding), name);
            frame = newFrame(binding, newDescriptor);
            assert newDescriptor.getSlotName(NEW_VAR_INDEX) == name;
            slot = NEW_VAR_INDEX;
        }
        frame.setObject(slot, value);
        return value;
    }

    @TruffleBoundary
    @Specialization(guards = "isHiddenVariable(name)")
    protected Object localVariableSetLastLine(RubyBinding binding, String name, Object value) {
        throw new RaiseException(
                getContext(),
                coreExceptions().nameError("Bad local variable name", binding, name, this));
    }

    protected WriteFrameSlotNode createWriteNode(int frameSlot) {
        return WriteFrameSlotNodeGen.create(frameSlot);
    }

    protected int getCacheLimit() {
        return getLanguage().options.BINDING_LOCAL_VARIABLE_CACHE;
    }
}
