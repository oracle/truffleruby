/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.binding;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.cast.NameToJavaStringNodeGen;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.locals.ReadFrameSlotNode;
import org.truffleruby.language.locals.ReadFrameSlotNodeGen;
import org.truffleruby.language.locals.WriteFrameSlotNode;
import org.truffleruby.language.locals.WriteFrameSlotNodeGen;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.threadlocal.ThreadLocalObject;
import org.truffleruby.parser.Translator;

import java.util.LinkedHashSet;
import java.util.Set;

@CoreClass("Binding")
public abstract class BindingNodes {

    @ImportStatic(BindingNodes.class)
    public abstract static class CreateBindingNode extends RubyBaseNode {

        public abstract DynamicObject execute(MaterializedFrame frame);

        @Specialization
        public DynamicObject createBinding(MaterializedFrame frame) {
            return Layouts.BINDING.createBinding(getContext().getCoreLibrary().getBindingFactory(), frame, null);
        }
    }

    public static DynamicObject createBinding(RubyContext context, MaterializedFrame frame) {
        return Layouts.BINDING.createBinding(context.getCoreLibrary().getBindingFactory(), frame, null);
    }

    @TruffleBoundary
    public static FrameDescriptor newFrameDescriptor(RubyContext context) {
        return new FrameDescriptor(context.getCoreLibrary().getNilObject());
    }

    public static FrameDescriptor getFrameDescriptor(DynamicObject binding) {
        assert RubyGuards.isRubyBinding(binding);
        return Layouts.BINDING.getFrame(binding).getFrameDescriptor();
    }

    public static FrameDescriptor getExtrasDescriptorOrNull(DynamicObject binding) {
        assert RubyGuards.isRubyBinding(binding);
        final MaterializedFrame frame = Layouts.BINDING.getExtras(binding);
        return frame == null ? null : frame.getFrameDescriptor();
    }

    public static FrameDescriptor getExtrasDescriptor(RubyContext context, DynamicObject binding) {
        assert RubyGuards.isRubyBinding(binding);
        MaterializedFrame frame = getExtrasFrame(context, binding);
        return frame.getFrameDescriptor();
    }

    public static MaterializedFrame getDeclarationFrame(DynamicObject binding) {
        assert RubyGuards.isRubyBinding(binding);
        return Layouts.BINDING.getFrame(binding);
    }

    public static MaterializedFrame getExtrasFrame(RubyContext context, DynamicObject binding) {
        assert RubyGuards.isRubyBinding(binding);
        MaterializedFrame frame = Layouts.BINDING.getExtras(binding);
        if (frame == null) {
            MaterializedFrame parent = Layouts.BINDING.getFrame(binding);
            frame = Truffle.getRuntime().createMaterializedFrame(
                    RubyArguments.pack(
                            parent,
                            null,
                            RubyArguments.getMethod(parent),
                            RubyArguments.getDeclarationContext(parent),
                            null,
                            RubyArguments.getSelf(parent),
                            RubyArguments.getBlock(parent),
                            RubyArguments.getArguments(parent)),
                    newFrameDescriptor(context));
            Layouts.BINDING.setExtras(binding, frame);
        }
        return frame;
    }

    protected static class FrameSlotAndDepth {
        private final FrameSlot slot;
        private final int depth;

        public FrameSlotAndDepth(FrameSlot slot, int depth) {
            this.slot = slot;
            this.depth = depth;
        }

        public FrameSlot getSlot() {
            return slot;
        }
    }

    public static FrameSlotAndDepth findFrameSlotOrNull(DynamicObject binding, String identifier) {
        MaterializedFrame frame = Layouts.BINDING.getFrame(binding);

        return findFrameSlotOrNull(identifier, frame);
    }

    public static FrameSlotAndDepth findExtrasSlotOrNull(DynamicObject binding, String identifier) {
        MaterializedFrame frame = Layouts.BINDING.getExtras(binding);

        if (frame == null) {
            return null;
        } else {
            FrameSlot slot = frame.getFrameDescriptor().findFrameSlot(identifier);
            return slot == null ? null : new FrameSlotAndDepth(slot, 0);
        }
    }

    private static FrameSlotAndDepth findFrameSlotOrNull(String identifier, MaterializedFrame frame) {
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

    public static boolean hiddenVariable(String name) {
        return name.startsWith("$") || name.startsWith("rubytruffle_temp");
    }

    @CoreMethod(names = { "dup", "clone" })
    public abstract static class DupNode extends UnaryCoreMethodNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject dup(DynamicObject binding) {
            DynamicObject copy = allocateObjectNode.allocate(
                    Layouts.BASIC_OBJECT.getLogicalClass(binding),
                    Layouts.BINDING.getFrame(binding),
                    copyFrame(Layouts.BINDING.getExtras(binding)));
            return copy;
        }

        private MaterializedFrame copyFrame(MaterializedFrame frame) {
            if (frame == null) {
                return null;
            }
            final MaterializedFrame copy = Truffle.getRuntime().createMaterializedFrame(frame.getArguments(), frame.getFrameDescriptor().copy());
            for (FrameSlot slot : frame.getFrameDescriptor().getSlots()) {
                copy.setObject(copy.getFrameDescriptor().findFrameSlot(slot.getIdentifier()), frame.getValue(slot));
            }
            return copy;
        }

    }

    @ImportStatic(BindingNodes.class)
    @CoreMethod(names = "local_variable_defined?", required = 1)
    @NodeChildren({
        @NodeChild(type = RubyNode.class, value = "binding"),
        @NodeChild(type = RubyNode.class, value = "name")
    })
    public abstract static class LocalVariableDefinedNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @TruffleBoundary
        @Specialization(guards = "!hiddenVariable(name)")
        public boolean localVariableDefinedUncached(DynamicObject binding, String name) {
            return findFrameSlotOrNull(binding, name) != null || findExtrasSlotOrNull(binding, name) != null;
        }

        @TruffleBoundary
        @Specialization(guards = "hiddenVariable(name)")
        public Object localVariableDefinedLastLine(DynamicObject binding, String name) {
            throw new RaiseException(coreExceptions().nameError("Bad local variable name", binding, name, this));
        }

    }

    @ImportStatic(BindingNodes.class)
    @CoreMethod(names = "local_variable_get", required = 1)
    @NodeChildren({
        @NodeChild(type = RubyNode.class, value = "binding"),
        @NodeChild(type = RubyNode.class, value = "name")
    })
    public abstract static class LocalVariableGetNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @Specialization(guards = {
                "name == cachedName",
                "!hiddenVariable(name)",
                "cachedFrameSlot != null",
                "getFrameDescriptor(binding) == descriptor"
        }, limit = "getCacheLimit()")
        public Object localVariableGetCached(DynamicObject binding, String name,
                @Cached("name") String cachedName,
                @Cached("getFrameDescriptor(binding)") FrameDescriptor descriptor,
                @Cached("findFrameSlotOrNull(binding, name)") FrameSlotAndDepth cachedFrameSlot,
                @Cached("createReadNode(cachedFrameSlot)") ReadFrameSlotNode readLocalVariableNode) {
            final MaterializedFrame frame = RubyArguments.getDeclarationFrame(Layouts.BINDING.getFrame(binding), cachedFrameSlot.depth);
            return readLocalVariableNode.executeRead(frame);
        }

        @Specialization(guards = {
                "name == cachedName",
                "!hiddenVariable(name)",
                "cachedFrameSlot != null",
                "getExtrasDescriptorOrNull(binding) == descriptor"
        }, limit = "getCacheLimit()")
        public Object extraVariableGetCached(DynamicObject binding, String name,
                @Cached("name") String cachedName,
                @Cached("getExtrasDescriptorOrNull(binding)") FrameDescriptor descriptor,
                @Cached("findExtrasSlotOrNull(binding, name)") FrameSlotAndDepth cachedFrameSlot,
                @Cached("createReadNode(cachedFrameSlot)") ReadFrameSlotNode readLocalVariableNode) {
            final MaterializedFrame frame = RubyArguments.getDeclarationFrame(Layouts.BINDING.getExtras(binding),
                    cachedFrameSlot.depth);
            return readLocalVariableNode.executeRead(frame);
        }

        @TruffleBoundary
        @Specialization(guards = "!hiddenVariable(name)")
        public Object localVariableGetUncached(DynamicObject binding, String name) {
            FrameSlotAndDepth frameSlot = findFrameSlotOrNull(binding, name);
            MaterializedFrame frame;
            if (frameSlot != null) {
                frame = RubyArguments.getDeclarationFrame(Layouts.BINDING.getFrame(binding), frameSlot.depth);
            } else {
                frameSlot = findExtrasSlotOrNull(binding, name);
                if (frameSlot != null) {
                    frame = Layouts.BINDING.getExtras(binding);
                } else {
                    throw new RaiseException(coreExceptions().nameErrorLocalVariableNotDefined(name, binding, this));
                }
            }
            return frame.getValue(frameSlot.slot);
        }

        @TruffleBoundary
        @Specialization(guards = "hiddenVariable(name)")
        public Object localVariableGetLastLine(DynamicObject binding, String name) {
            throw new RaiseException(coreExceptions().nameError("Bad local variable name", binding, name, this));
        }

        protected boolean compatibleFrames(DynamicObject binding, FrameDescriptor descriptor) {
            final FrameDescriptor bindingDescrptor = getFrameDescriptor(binding);
            return bindingDescrptor == descriptor;
        }

        protected ReadFrameSlotNode createReadNode(FrameSlotAndDepth frameSlot) {
            if (frameSlot == null) {
                return null;
            } else {
                return ReadFrameSlotNodeGen.create(frameSlot.slot);
            }
        }

        protected int getCacheLimit() {
            return getContext().getOptions().BINDING_LOCAL_VARIABLE_CACHE;
        }

    }

    @ImportStatic(BindingNodes.class)
    @CoreMethod(names = "local_variable_set", required = 2)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "binding"),
            @NodeChild(type = RubyNode.class, value = "name"),
            @NodeChild(type = RubyNode.class, value = "value")
    })
    public abstract static class LocalVariableSetNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @Specialization(guards = {
                "name == cachedName",
                "!hiddenVariable(name)",
                "getFrameDescriptor(binding) == cachedFrameDescriptor",
                "cachedFrameSlot != null"
        }, limit = "getCacheLimit()")
        public Object localVariableSetCached(DynamicObject binding, String name, Object value,
                @Cached("name") String cachedName,
                @Cached("getFrameDescriptor(binding)") FrameDescriptor cachedFrameDescriptor,
                @Cached("findFrameSlotOrNull(binding, name)") FrameSlotAndDepth cachedFrameSlot,
                @Cached("createWriteNode(cachedFrameSlot)") WriteFrameSlotNode writeLocalVariableNode) {
            final MaterializedFrame frame = RubyArguments.getDeclarationFrame(Layouts.BINDING.getFrame(binding), cachedFrameSlot.depth);
            return writeLocalVariableNode.executeWrite(frame, value);
        }

        @Specialization(guards = {
                "name == cachedName",
                "!hiddenVariable(name)",
                "getExtrasDescriptorOrNull(binding) == cachedFrameDescriptor",
                "cachedFrameSlot != null"
        }, limit = "getCacheLimit()")
        public Object extraVariableSetCached(DynamicObject binding, String name, Object value,
                @Cached("name") String cachedName,
                @Cached("getExtrasDescriptor(getContext(), binding)") FrameDescriptor cachedFrameDescriptor,
                @Cached("findExtrasSlotOrNull(binding, name)") FrameSlotAndDepth cachedFrameSlot,
                @Cached("createWriteNode(cachedFrameSlot)") WriteFrameSlotNode writeLocalVariableNode) {
            final MaterializedFrame frame = Layouts.BINDING.getExtras(binding);
            return writeLocalVariableNode.executeWrite(frame, value);
        }

        @TruffleBoundary
        @Specialization(guards = "!hiddenVariable(name)")
        public Object localVariableSetUncached(DynamicObject binding, String name, Object value) {
            FrameSlotAndDepth frameSlot = findFrameSlotOrNull(binding, name);
            final MaterializedFrame frame;
            if (frameSlot != null) {
                frame = RubyArguments.getDeclarationFrame(Layouts.BINDING.getFrame(binding), frameSlot.depth);
            } else {
                frameSlot = findExtrasSlot(getContext(), binding, name);
                frame = Layouts.BINDING.getExtras(binding);
            }
            frame.setObject(frameSlot.slot, value);
            return value;
        }

        @TruffleBoundary
        @Specialization(guards = "hiddenVariable(name)")
        public Object localVariableSetLastLine(DynamicObject binding, String name, Object value) {
            throw new RaiseException(coreExceptions().nameError("Bad local variable name", binding, name, this));
        }

        protected FrameSlotAndDepth findFrameSlot(DynamicObject binding, String name) {
            final FrameSlotAndDepth frameSlot = BindingNodes.findFrameSlotOrNull(binding, name);
            if (frameSlot == null) {
                final FrameSlot newSlot = Layouts.BINDING.getFrame(binding).getFrameDescriptor().addFrameSlot(name);
                return new FrameSlotAndDepth(newSlot, 0);
            } else {
                return frameSlot;
            }
        }

        protected FrameSlotAndDepth findExtrasSlot(RubyContext context, DynamicObject binding, String name) {
            final FrameSlotAndDepth frameSlot = BindingNodes.findExtrasSlotOrNull(binding, name);
            if (frameSlot == null) {
                final FrameSlot newSlot = getExtrasFrame(context, binding).getFrameDescriptor().addFrameSlot(name);
                return new FrameSlotAndDepth(newSlot, 0);
            } else {
                return frameSlot;
            }
        }

        protected WriteFrameSlotNode createWriteNode(FrameSlotAndDepth frameSlot) {
            return WriteFrameSlotNodeGen.create(frameSlot.slot);
        }

        protected int getCacheLimit() {
            return getContext().getOptions().BINDING_LOCAL_VARIABLE_CACHE;
        }
    }

    @CoreMethod(names = "local_variables")
    public abstract static class LocalVariablesNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject localVariables(DynamicObject binding) {
            MaterializedFrame extras = Layouts.BINDING.getExtras(binding);
            MaterializedFrame frame = Layouts.BINDING.getFrame(binding);

            return listLocalVariables(getContext(), frame, extras);
        }

        @TruffleBoundary
        public static DynamicObject listLocalVariables(RubyContext context, Frame frame, Frame extras) {
            final Set<Object> names = new LinkedHashSet<>();
            if (extras != null) {
                addNamesFromFrame(context, extras, names);
            }
            while (frame != null) {
                addNamesFromFrame(context, frame, names);

                frame = RubyArguments.getDeclarationFrame(frame);
            }
            final int size = names.size();
            return ArrayHelpers.createArray(context, names.toArray(new Object[size]), size);
        }

        private static void addNamesFromFrame(RubyContext context, Frame frame, final Set<Object> names) {
            for (FrameSlot slot : frame.getFrameDescriptor().getSlots()) {
                if (slot.getIdentifier() instanceof String &&
                        !hiddenVariable((String) slot.getIdentifier()) &&
                        !Translator.FRAME_LOCAL_GLOBAL_VARIABLES.contains(slot.getIdentifier())) {
                    names.add(context.getSymbolTable().getSymbol((String) slot.getIdentifier()));
                }
            }
        }

    }

    @CoreMethod(names = "receiver")
    public abstract static class ReceiverNode extends UnaryCoreMethodNode {

        @Specialization
        public Object receiver(DynamicObject binding) {
            return RubyArguments.getSelf(Layouts.BINDING.getFrame(binding));
        }
    }

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            throw new RaiseException(coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }

    }

}
