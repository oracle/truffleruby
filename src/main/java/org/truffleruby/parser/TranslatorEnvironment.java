/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import com.oracle.truffle.api.Assumption;
import org.truffleruby.Layouts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.FrameSlotKind;
import org.graalvm.collections.EconomicMap;
import org.truffleruby.core.binding.BindingNodes;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.control.BreakID;
import org.truffleruby.language.control.ReturnID;
import org.truffleruby.language.literal.NilLiteralNode;
import org.truffleruby.language.locals.LocalVariableType;
import org.truffleruby.language.locals.ReadDeclarationVariableNode;
import org.truffleruby.language.locals.ReadLocalNode;
import org.truffleruby.language.locals.ReadLocalVariableNode;
import org.truffleruby.language.methods.SharedMethodInfo;

import com.oracle.truffle.api.frame.FrameDescriptor;
import org.truffleruby.language.objects.SelfNode;
import org.truffleruby.language.threadlocal.SpecialVariableStorage;

public class TranslatorEnvironment {

    public static final String METHOD_BLOCK_NAME = Layouts.TEMP_PREFIX + "method_block_arg";

    private final ParseEnvironment parseEnvironment;

    private EconomicMap<Object, Integer> nameToIndex = EconomicMap.create();
    private FrameDescriptor.Builder frameDescriptorBuilder;
    private FrameDescriptor frameDescriptor;
    private final ParentFrameDescriptor parentFrameDescriptor;

    private final List<Integer> flipFlopStates = new ArrayList<>();

    private final ReturnID returnID;
    private final int blockDepth;
    private BreakID breakID;

    private final boolean ownScopeForAssignments;
    private final boolean isModuleBody;

    protected final TranslatorEnvironment parent;
    private final SharedMethodInfo sharedMethodInfo;

    public final String modulePath;
    public final String methodName;

    // TODO(CS): overflow? and it should be per-context, or even more local
    private static final AtomicInteger tempIndex = new AtomicInteger();

    public TranslatorEnvironment(
            TranslatorEnvironment parent,
            ParseEnvironment parseEnvironment,
            ReturnID returnID,
            boolean ownScopeForAssignments,
            boolean isModuleBody,
            SharedMethodInfo sharedMethodInfo,
            String methodName,
            int blockDepth,
            BreakID breakID,
            FrameDescriptor descriptor,
            String modulePath) {
        this.parent = parent;

        if (descriptor == null) {
            ParentFrameDescriptor parentDescriptor = blockDepth > 0
                    ? Objects.requireNonNull(parent.parentFrameDescriptor)
                    : null;
            this.frameDescriptorBuilder = newFrameDescriptorBuilder(parentDescriptor, blockDepth == 0);
            this.parentFrameDescriptor = new ParentFrameDescriptor();
        } else {
            this.frameDescriptor = descriptor;
            this.parentFrameDescriptor = new ParentFrameDescriptor(descriptor);

            assert descriptor.getNumberOfAuxiliarySlots() == 0;
            int slots = descriptor.getNumberOfSlots();
            for (int slot = 0; slot < slots; slot++) {
                Object identifier = descriptor.getSlotName(slot);
                if (!BindingNodes.isHiddenVariable(identifier)) {
                    nameToIndex.put(identifier, slot);
                }
            }
        }

        this.parseEnvironment = parseEnvironment;
        this.returnID = returnID;
        this.ownScopeForAssignments = ownScopeForAssignments;
        this.isModuleBody = isModuleBody;
        this.sharedMethodInfo = sharedMethodInfo;
        this.methodName = methodName;
        this.blockDepth = blockDepth;
        this.breakID = breakID;
        this.modulePath = modulePath;
    }

    public static String composeModulePath(String modulePath, String name) {
        return modulePath != null ? modulePath + "::" + name : name;
    }

    public boolean isDynamicConstantLookup() {
        return sharedMethodInfo.getStaticLexicalScopeOrNull() == null;
    }

    public LexicalScope getStaticLexicalScope() {
        return sharedMethodInfo.getStaticLexicalScope();
    }

    public LexicalScope getStaticLexicalScopeOrNull() {
        return sharedMethodInfo.getStaticLexicalScopeOrNull();
    }

    public TranslatorEnvironment getParent() {
        return parent;
    }

    /** Top-level scope, i.e. from main script/load/require. The lexical scope might not be Object in the case of
     * {@code load(file, wrap=true)}. */
    public boolean isTopLevelScope() {
        return parent == null && isModuleBody;
    }

    /** Top-level scope and the lexical scope is Object, and self is the "main" object */
    public boolean isTopLevelObjectScope() {
        return isTopLevelScope() && modulePath == null;
    }

    // region frame descriptor
    public static FrameDescriptor.Builder newFrameDescriptorBuilder(ParentFrameDescriptor parentDescriptor,
            boolean canHaveSpecialVariables) {
        if ((parentDescriptor != null) == canHaveSpecialVariables) {
            throw CompilerDirectives.shouldNotReachHere(
                    "A descriptor should either be a method and have special variables, or be a block and have no special variables");
        }

        var builder = FrameDescriptor.newBuilder().defaultValue(Nil.INSTANCE);

        if (parentDescriptor != null) {
            builder.info(parentDescriptor);
        } else if (canHaveSpecialVariables) {
            // We need to access this Assumption from the FrameDescriptor,
            // and there is no way to get a RootNode from a FrameDescriptor, so we store it in the descriptor info.
            // We do not store it as slot info for footprint, to avoid needing an info array per FrameDescriptor.
            final Assumption doesNotNeedSpecialVariableStorageAssumption = Assumption
                    .create(SpecialVariableStorage.ASSUMPTION_NAME);
            builder.info(doesNotNeedSpecialVariableStorageAssumption);
        }

        int selfIndex = builder.addSlot(FrameSlotKind.Illegal, SelfNode.SELF_IDENTIFIER, null);
        if (selfIndex != SelfNode.SELF_INDEX) {
            throw CompilerDirectives.shouldNotReachHere("(self) should be at index 0");
        }

        if (canHaveSpecialVariables) {
            int svarsSlot = builder.addSlot(FrameSlotKind.Illegal, SpecialVariableStorage.SLOT_NAME, null);
            if (svarsSlot != SpecialVariableStorage.SLOT_INDEX) {
                throw CompilerDirectives.shouldNotReachHere("svars should be at index 1");
            }
        }

        return builder;
    }

    public int declareVar(Object name) {
        assert name != null && !(name instanceof String && ((String) name).isEmpty());

        Integer existingSlot = nameToIndex.get(name);
        if (existingSlot != null) {
            return existingSlot;
        } else {
            int index = addSlot(name);
            nameToIndex.put(name, index);
            return index;
        }
    }

    private int addSlot(Object name) {
        return frameDescriptorBuilder.addSlot(FrameSlotKind.Illegal, name, null);
    }

    public String allocateLocalTemp(String indicator) {
        return Layouts.TEMP_PREFIX + indicator + "_" + tempIndex.getAndIncrement();
    }

    public int declareLocalTemp(String indicator) {
        final String name = allocateLocalTemp(indicator);
        // TODO: might not need to add to nameToIndex for temp vars
        return declareVar(name);
    }

    public Integer findFrameSlotOrNull(Object name) {
        return nameToIndex.get(name);
    }

    public int findFrameSlot(Object name) {
        Integer index = nameToIndex.get(name);
        if (index == null) {
            throw CompilerDirectives.shouldNotReachHere("Could not find slot " + name);
        }
        return index;
    }

    public ReadLocalNode findOrAddLocalVarNodeDangerous(String name, SourceIndexLength sourceSection) {
        ReadLocalNode localVar = findLocalVarNode(name, sourceSection);

        if (localVar == null) {
            declareVar(name);
            localVar = findLocalVarNode(name, sourceSection);
        }

        return localVar;
    }

    public ReadLocalVariableNode readNode(int slot, SourceIndexLength sourceSection) {
        var node = new ReadLocalVariableNode(LocalVariableType.FRAME_LOCAL, slot);
        node.unsafeSetSourceSection(sourceSection);
        return node;
    }

    public RubyNode findLocalVarOrNilNode(String name, SourceIndexLength sourceSection) {
        RubyNode node = findLocalVarNode(name, sourceSection);
        if (node == null) {
            node = new NilLiteralNode(true);
            node.unsafeSetSourceSection(sourceSection);
        }
        return node;
    }

    public ReadLocalNode findLocalVarNode(String name, SourceIndexLength sourceSection) {
        TranslatorEnvironment current = this;
        int level = 0;

        while (current != null) {
            final Integer slot = current.findFrameSlotOrNull(name);

            if (slot != null) {
                final ReadLocalNode node;
                if (level == 0) {
                    node = new ReadLocalVariableNode(LocalVariableType.FRAME_LOCAL, slot);
                } else {
                    node = new ReadDeclarationVariableNode(LocalVariableType.FRAME_LOCAL, level, slot);
                }

                node.unsafeSetSourceSection(sourceSection);
                return node;
            }

            if (current.getNeverAssignInParentScope()) {
                // Do not try to look above scope barriers (def, module)
                return null;
            }

            current = current.parent;
            level++;
        }

        return null;
    }

    public FrameDescriptor computeFrameDescriptor() {
        if (frameDescriptor != null) {
            return frameDescriptor;
        }

        frameDescriptor = frameDescriptorBuilder.build();
        parentFrameDescriptor.set(frameDescriptor);
        frameDescriptorBuilder = null;
        nameToIndex = null;
        return frameDescriptor;
    }
    // endregion

    public ReturnID getReturnID() {
        return returnID;
    }

    public ParseEnvironment getParseEnvironment() {
        return parseEnvironment;
    }

    public boolean hasOwnScopeForAssignments() {
        return ownScopeForAssignments;
    }

    /** Whether this is a lexical scope barrier (def, module, class) */
    public boolean getNeverAssignInParentScope() {
        return !isBlock();
    }

    public boolean isModuleBody() {
        return isModuleBody;
    }

    public SharedMethodInfo getSharedMethodInfo() {
        return sharedMethodInfo;
    }

    public List<Integer> getFlipFlopStates() {
        return flipFlopStates;
    }

    public String getMethodName() {
        return methodName;
    }

    public boolean isBlock() {
        return blockDepth > 0;
    }

    public int getBlockDepth() {
        return blockDepth;
    }

    public BreakID getBreakID() {
        return breakID;
    }

    public void setBreakIDForWhile(BreakID breakID) {
        this.breakID = breakID;
    }

    public TranslatorEnvironment getSurroundingMethodEnvironment() {
        TranslatorEnvironment methodParent = this;
        while (methodParent.isBlock()) {
            methodParent = methodParent.getParent();
        }
        return methodParent;
    }
}
