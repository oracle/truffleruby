/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.binding;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.NameToJavaStringNode;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes.MakeStringNode;
import org.truffleruby.language.CallStackManager;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubySourceNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.ReadCallerFrameNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.locals.FindDeclarationVariableNodes;
import org.truffleruby.language.locals.FindDeclarationVariableNodes.FindAndReadDeclarationVariableNode;
import org.truffleruby.language.locals.FindDeclarationVariableNodes.FrameSlotAndDepth;
import org.truffleruby.language.locals.WriteFrameSlotNode;
import org.truffleruby.language.locals.WriteFrameSlotNodeGen;
import org.truffleruby.parser.TranslatorEnvironment;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;

@CoreModule(value = "Binding", isClass = true)
public abstract class BindingNodes {

    /** Creates a Binding without a SourceSection, only for Binding used internally and not exposed to the user. */
    public static RubyBinding createBinding(RubyContext context, RubyLanguage language, MaterializedFrame frame) {
        return createBinding(context, language, frame, null);
    }

    public static RubyBinding createBinding(RubyContext context, RubyLanguage language, MaterializedFrame frame,
            SourceSection sourceSection) {
        return new RubyBinding(
                context.getCoreLibrary().bindingClass,
                language.bindingShape,
                frame,
                sourceSection);
    }

    @TruffleBoundary
    public static FrameDescriptor newFrameDescriptor() {
        return new FrameDescriptor(Nil.INSTANCE);
    }

    @TruffleBoundary
    public static FrameDescriptor newFrameDescriptor(String name) {
        final FrameDescriptor frameDescriptor = new FrameDescriptor(Nil.INSTANCE);
        assert name != null && !name.isEmpty();
        frameDescriptor.addFrameSlot(name);
        return frameDescriptor;
    }

    public static FrameDescriptor getFrameDescriptor(RubyBinding binding) {
        return binding.getFrame().getFrameDescriptor();
    }

    public static MaterializedFrame newFrame(RubyBinding binding, FrameDescriptor frameDescriptor) {
        final MaterializedFrame frame = binding.getFrame();
        final MaterializedFrame newFrame = newFrame(frame, frameDescriptor);
        binding.setFrame(newFrame);
        return newFrame;
    }

    public static MaterializedFrame newFrame(MaterializedFrame parent, FrameDescriptor descriptor) {
        return Truffle.getRuntime().createVirtualFrame(
                RubyArguments.pack(
                        parent,
                        null,
                        RubyArguments.getMethod(parent),
                        RubyArguments.getDeclarationContext(parent),
                        null,
                        RubyArguments.getSelf(parent),
                        RubyArguments.getBlock(parent),
                        RubyArguments.getArguments(parent)),
                descriptor).materialize();
    }

    public static void insertAncestorFrame(RubyBinding binding, MaterializedFrame ancestorFrame) {
        MaterializedFrame frame = binding.getFrame();
        while (RubyArguments.getDeclarationFrame(frame) != null) {
            frame = RubyArguments.getDeclarationFrame(frame);
        }
        RubyArguments.setDeclarationFrame(frame, ancestorFrame);

        // We need to invalidate caches depending on the top frame, so create a new empty frame
        newFrame(binding, newFrameDescriptor());
    }

    @TruffleBoundary
    public static boolean assignsNewUserVariables(FrameDescriptor descriptor) {
        for (FrameSlot slot : descriptor.getSlots()) {
            if (!BindingNodes.isHiddenVariable(slot.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHiddenVariable(Object name) {
        if (name instanceof String) {
            return isHiddenVariable((String) name);
        } else {
            return true;
        }
    }

    private static boolean isHiddenVariable(String name) {
        assert !name.isEmpty();
        return name.charAt(0) == '$' || // Frame-local global variable
                name.charAt(0) == TranslatorEnvironment.TEMP_PREFIX;
    }

    @CoreMethod(names = { "dup", "clone" })
    public abstract static class DupNode extends UnaryCoreMethodNode {
        @Specialization
        protected RubyBinding dup(RubyBinding binding) {
            return new RubyBinding(
                    coreLibrary().bindingClass,
                    getLanguage().bindingShape,
                    binding.getFrame(),
                    binding.sourceSection);
        }
    }

    /** Same as {@link LocalVariableDefinedNode} but returns false instead of raising an exception for hidden
     * variables. */
    @ImportStatic({ BindingNodes.class, FindDeclarationVariableNodes.class })
    @GenerateUncached
    public abstract static class HasLocalVariableNode extends RubyBaseNode {

        public static HasLocalVariableNode create() {
            return BindingNodesFactory.HasLocalVariableNodeGen.create();
        }

        public abstract boolean execute(RubyBinding binding, String name);

        @Specialization(
                guards = {
                        "name == cachedName",
                        "getFrameDescriptor(binding) == descriptor" },
                limit = "getCacheLimit()")
        protected boolean localVariableDefinedCached(RubyBinding binding, String name,
                @Cached("name") String cachedName,
                @Cached("getFrameDescriptor(binding)") FrameDescriptor descriptor,
                @Cached("findFrameSlotOrNull(name, binding.getFrame())") FrameSlotAndDepth cachedFrameSlot) {
            return cachedFrameSlot != null;
        }

        @TruffleBoundary
        @Specialization(replaces = "localVariableDefinedCached")
        protected boolean localVariableDefinedUncached(RubyBinding binding, String name) {
            return FindDeclarationVariableNodes.findFrameSlotOrNull(name, binding.getFrame()) != null;
        }

        protected int getCacheLimit() {
            return getLanguage().options.BINDING_LOCAL_VARIABLE_CACHE;
        }

    }

    @ImportStatic({ BindingNodes.class, FindDeclarationVariableNodes.class })
    @GenerateUncached
    @GenerateNodeFactory
    @CoreMethod(names = "local_variable_defined?", required = 1)
    @NodeChild(value = "binding", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    public abstract static class LocalVariableDefinedNode extends RubySourceNode {

        public abstract boolean execute(RubyBinding binding, String name);

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization(
                guards = {
                        "name == cachedName",
                        "!isHiddenVariable(cachedName)",
                        "getFrameDescriptor(binding) == descriptor" },
                limit = "getCacheLimit()")
        protected boolean localVariableDefinedCached(RubyBinding binding, String name,
                @Cached("name") String cachedName,
                @Cached("getFrameDescriptor(binding)") FrameDescriptor descriptor,
                @Cached("findFrameSlotOrNull(name, binding.getFrame())") FrameSlotAndDepth cachedFrameSlot) {
            return cachedFrameSlot != null;
        }

        @TruffleBoundary
        @Specialization(guards = "!isHiddenVariable(name)", replaces = "localVariableDefinedCached")
        protected boolean localVariableDefinedUncached(RubyBinding binding, String name) {
            return FindDeclarationVariableNodes.findFrameSlotOrNull(name, binding.getFrame()) != null;
        }

        @TruffleBoundary
        @Specialization(guards = "isHiddenVariable(name)")
        protected Object localVariableDefinedLastLine(RubyBinding binding, String name) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().nameError("Bad local variable name", binding, name, this));
        }

        protected int getCacheLimit() {
            return getLanguage().options.BINDING_LOCAL_VARIABLE_CACHE;
        }

    }

    @GenerateUncached
    @GenerateNodeFactory
    @NodeChild(value = "binding", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    @ImportStatic(BindingNodes.class)
    @CoreMethod(names = "local_variable_get", required = 1)
    public abstract static class LocalVariableGetNode extends RubySourceNode {

        public abstract Object execute(RubyBinding binding, String name);

        public static LocalVariableGetNode create() {
            return BindingNodesFactory.LocalVariableGetNodeFactory.create(null, null);
        }

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization(guards = "!isHiddenVariable(name)")
        protected Object localVariableGet(RubyBinding binding, String name,
                @Cached FindAndReadDeclarationVariableNode readNode) {
            MaterializedFrame frame = binding.getFrame();
            Object result = readNode.execute(frame, name, null);
            if (result == null) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().nameErrorLocalVariableNotDefined(name, binding, this));
            }
            return result;
        }

        @TruffleBoundary
        @Specialization(guards = "isHiddenVariable(name)")
        protected Object localVariableGetLastLine(RubyBinding binding, String name) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().nameError("Bad local variable name", binding, name, this));
        }

        protected int getCacheLimit() {
            return getLanguage().options.BINDING_LOCAL_VARIABLE_CACHE;
        }

    }

    @ReportPolymorphism
    @GenerateUncached
    @GenerateNodeFactory
    @CoreMethod(names = "local_variable_set", required = 2)
    @NodeChild(value = "binding", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyBaseNodeWithExecute.class)
    @NodeChild(value = "value", type = RubyNode.class)
    @ImportStatic({ BindingNodes.class, FindDeclarationVariableNodes.class })
    public abstract static class LocalVariableSetNode extends RubySourceNode {

        public static LocalVariableSetNode create() {
            return BindingNodesFactory.LocalVariableSetNodeFactory.create(null, null, null);
        }

        public abstract Object execute(RubyBinding binding, String name, Object value);

        @CreateCast("name")
        protected RubyBaseNodeWithExecute coerceToString(RubyBaseNodeWithExecute name) {
            return NameToJavaStringNode.create(name);
        }

        @Specialization(
                guards = {
                        "name == cachedName",
                        "!isHiddenVariable(cachedName)",
                        "getFrameDescriptor(binding) == cachedFrameDescriptor",
                        "cachedFrameSlot != null" },
                assumptions = "cachedFrameDescriptor.getVersion()",
                limit = "getCacheLimit()")
        protected Object localVariableSetCached(RubyBinding binding, String name, Object value,
                @Cached("name") String cachedName,
                @Cached("getFrameDescriptor(binding)") FrameDescriptor cachedFrameDescriptor,
                @Cached("findFrameSlotOrNull(name, binding.getFrame())") FrameSlotAndDepth cachedFrameSlot,
                @Cached("createWriteNode(cachedFrameSlot)") WriteFrameSlotNode writeLocalVariableNode) {
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
                assumptions = "cachedFrameDescriptor.getVersion()",
                limit = "getCacheLimit()")
        protected Object localVariableSetNewCached(RubyBinding binding, String name, Object value,
                @Cached("name") String cachedName,
                @Cached("getFrameDescriptor(binding)") FrameDescriptor cachedFrameDescriptor,
                @Cached("findFrameSlotOrNull(name, binding.getFrame())") FrameSlotAndDepth cachedFrameSlot,
                @Cached("newFrameDescriptor(name)") FrameDescriptor newDescriptor,
                @Cached("findFrameSlot(name, newDescriptor)") FrameSlotAndDepth newFrameSlot,
                @Cached("createWriteNode(newFrameSlot)") WriteFrameSlotNode writeLocalVariableNode) {
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
            final FrameSlotAndDepth frameSlot = FindDeclarationVariableNodes.findFrameSlotOrNull(name, frame);
            final FrameSlot slot;
            if (frameSlot != null) {
                frame = RubyArguments.getDeclarationFrame(frame, frameSlot.depth);
                slot = frameSlot.slot;
            } else {
                frame = newFrame(binding, newFrameDescriptor(name));
                slot = frame.getFrameDescriptor().findFrameSlot(name);
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

        protected WriteFrameSlotNode createWriteNode(FrameSlotAndDepth frameSlot) {
            return WriteFrameSlotNodeGen.create(frameSlot.slot);
        }

        protected int getCacheLimit() {
            return getLanguage().options.BINDING_LOCAL_VARIABLE_CACHE;
        }
    }

    @Primitive(name = "local_variable_names")
    @ImportStatic(BindingNodes.class)
    public abstract static class LocalVariablesNode extends PrimitiveArrayArgumentsNode {

        @Specialization(
                guards = "getFrameDescriptor(binding) == cachedFrameDescriptor",
                assumptions = "cachedFrameDescriptor.getVersion()",
                limit = "getCacheLimit()")
        protected RubyArray localVariablesCached(RubyBinding binding,
                @Cached("getFrameDescriptor(binding)") FrameDescriptor cachedFrameDescriptor,
                @Cached("listLocalVariablesAsSymbols(getContext(), binding.getFrame())") RubyArray names) {
            return names;
        }

        @Specialization(replaces = "localVariablesCached")
        protected RubyArray localVariables(RubyBinding binding) {
            return listLocalVariablesAsSymbols(getContext(), binding.getFrame());
        }

        @TruffleBoundary
        public RubyArray listLocalVariablesAsSymbols(RubyContext context, MaterializedFrame frame) {
            final Set<Object> names = new LinkedHashSet<>();
            while (frame != null) {
                addNamesFromFrame(frame, names);
                frame = RubyArguments.getDeclarationFrame(frame);
            }
            return ArrayHelpers.createArray(context, getLanguage(), names.toArray());
        }

        private void addNamesFromFrame(Frame frame, Set<Object> names) {
            for (FrameSlot slot : frame.getFrameDescriptor().getSlots()) {
                if (!isHiddenVariable(slot.getIdentifier())) {
                    names.add(getSymbol((String) slot.getIdentifier()));
                }
            }
        }

        @TruffleBoundary
        public static List<String> listLocalVariablesWithDuplicates(MaterializedFrame frame, String receiverName) {
            List<String> members = new ArrayList<>();
            Frame currentFrame = frame;
            while (currentFrame != null) {
                final FrameDescriptor frameDescriptor = currentFrame.getFrameDescriptor();
                for (FrameSlot slot : frameDescriptor.getSlots()) {
                    if (!isHiddenVariable(slot.getIdentifier())) {
                        members.add(slot.getIdentifier().toString());
                    }
                }
                if (receiverName != null) {
                    members.add(receiverName);
                }
                currentFrame = RubyArguments.getDeclarationFrame(currentFrame);
            }
            return members;
        }

        protected int getCacheLimit() {
            return getLanguage().options.BINDING_LOCAL_VARIABLE_CACHE;
        }
    }

    @CoreMethod(names = "receiver")
    public abstract static class ReceiverNode extends UnaryCoreMethodNode {

        @Specialization
        protected Object receiver(RubyBinding binding) {
            return RubyArguments.getSelf(binding.getFrame());
        }
    }

    // NOTE: Introduced in Ruby 2.6, but already useful for Binding#eval
    @CoreMethod(names = "source_location")
    public abstract static class SourceLocationNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        protected Object sourceLocation(RubyBinding binding,
                @Cached MakeStringNode makeStringNode) {
            final SourceSection sourceSection = binding.sourceSection;

            if (sourceSection == null) {
                return nil;
            } else {
                final RubyString file = makeStringNode.executeMake(
                        getLanguage().getSourcePath(sourceSection.getSource()),
                        Encodings.UTF_8,
                        CodeRange.CR_UNKNOWN);
                return createArray(new Object[]{ file, sourceSection.getStartLine() });
            }
        }
    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @Specialization
        protected Object allocate(RubyClass rubyClass) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }

    }

    @Primitive(name = "caller_binding")
    public abstract static class CallerBindingNode extends PrimitiveArrayArgumentsNode {

        @Child ReadCallerFrameNode callerFrameNode = new ReadCallerFrameNode();

        @Specialization
        protected RubyBinding binding(VirtualFrame frame,
                @Cached ConditionProfile javaCoreMethodProfile) {
            MaterializedFrame callerFrame = callerFrameNode.execute(frame);

            if (javaCoreMethodProfile.profile(CallStackManager.isJavaCore(RubyArguments.tryGetMethod(callerFrame)))) {
                // we are called from a Java core method, e.g., Method#call, we need to find the actual caller
                callerFrame = getContext()
                        .getCallStack()
                        .getNonJavaCoreCallerFrame(FrameAccess.MATERIALIZE)
                        .materialize();
            }

            return BindingNodes.createBinding(getContext(), getLanguage(), callerFrame);
        }
    }

    @Primitive(name = "create_empty_binding")
    public abstract static class CreateEmptyBindingNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected RubyBinding binding(VirtualFrame frame) {
            // Use the current frame to initialize the arguments, etc, correctly
            final RubyBinding binding = BindingNodes
                    .createBinding(getContext(), getLanguage(), frame.materialize(), getEncapsulatingSourceSection());
            final MaterializedFrame newFrame = newFrame(binding, newFrameDescriptor());
            RubyArguments.setDeclarationFrame(newFrame, null); // detach from the current frame
            return binding;
        }
    }

}
