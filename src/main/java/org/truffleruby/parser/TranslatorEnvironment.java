/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import org.truffleruby.RubyContext;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.control.BreakID;
import org.truffleruby.language.control.ReturnID;
import org.truffleruby.language.locals.LocalVariableType;
import org.truffleruby.language.locals.ReadDeclarationVariableNode;
import org.truffleruby.language.locals.ReadLocalVariableNode;
import org.truffleruby.language.methods.SharedMethodInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TranslatorEnvironment {

    private final ParseEnvironment parseEnvironment;

    private final FrameDescriptor frameDescriptor;

    private final List<FrameSlot> flipFlopStates = new ArrayList<>();

    private final ReturnID returnID;
    private final int blockDepth;
    private BreakID breakID;

    private final boolean ownScopeForAssignments;
    private final boolean neverAssignInParentScope;
    private final boolean isModuleBody;

    protected final TranslatorEnvironment parent;
    private final SharedMethodInfo sharedMethodInfo;

    private final String namedMethodName;

    // TODO(CS): overflow? and it should be per-context, or even more local
    private static AtomicInteger tempIndex = new AtomicInteger();

    public TranslatorEnvironment(TranslatorEnvironment parent, ParseEnvironment parseEnvironment,
                                 ReturnID returnID, boolean ownScopeForAssignments, boolean neverAssignInParentScope,
                                 boolean isModuleBody, SharedMethodInfo sharedMethodInfo, String namedMethodName, int blockDepth,
                                 BreakID breakID, FrameDescriptor frameDescriptor) {
        this.parent = parent;
        this.frameDescriptor = frameDescriptor;
        this.parseEnvironment = parseEnvironment;
        this.returnID = returnID;
        this.ownScopeForAssignments = ownScopeForAssignments;
        this.neverAssignInParentScope = neverAssignInParentScope;
        this.isModuleBody = isModuleBody;
        this.sharedMethodInfo = sharedMethodInfo;
        this.namedMethodName = namedMethodName;
        this.blockDepth = blockDepth;
        this.breakID = breakID;
    }

    public static FrameDescriptor newFrameDescriptor(RubyContext context) {
        return new FrameDescriptor(context.getCoreLibrary().getNil());
    }

    public boolean isDynamicConstantLookup() {
        return parseEnvironment.isDynamicConstantLookup();
    }

    public LexicalScope getLexicalScope() {
        assert !isDynamicConstantLookup();
        return parseEnvironment.getLexicalScope();
    }

    public LexicalScope getLexicalScopeOrNull() {
        if (isDynamicConstantLookup()) {
            // TODO (eregon, 4 Dec. 2016): we should return null here.
            return parseEnvironment.getLexicalScope();
        } else {
            return parseEnvironment.getLexicalScope();
        }
    }

    public LexicalScope pushLexicalScope() {
        return parseEnvironment.pushLexicalScope();
    }

    public void popLexicalScope() {
        parseEnvironment.popLexicalScope();
    }

    public TranslatorEnvironment getParent() {
        return parent;
    }

    public FrameSlot declareVar(String name) {
        assert name != null && !name.isEmpty();
        return getFrameDescriptor().findOrAddFrameSlot(name);
    }

    public ReadLocalNode findOrAddLocalVarNodeDangerous(String name, SourceIndexLength sourceSection) {
        ReadLocalNode localVar = findLocalVarNode(name, sourceSection);

        if (localVar == null) {
            declareVar(name);
            localVar = findLocalVarNode(name, sourceSection);
        }

        return localVar;
    }

    public ReadLocalNode findLocalVarNode(String name, SourceIndexLength sourceSection) {
        TranslatorEnvironment current = this;
        int level = 0;

        while (current != null) {
            final FrameSlot slot = current.getFrameDescriptor().findFrameSlot(name);

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

            current = current.parent;
            level++;
        }

        return null;
    }

    public FrameDescriptor getFrameDescriptor() {
        return frameDescriptor;
    }

    public String allocateLocalTemp(String indicator) {
        final String name = "rubytruffle_temp_" + indicator + "_" + tempIndex.getAndIncrement();
        declareVar(name);
        return name;
    }

    public ReturnID getReturnID() {
        return returnID;
    }

    public ParseEnvironment getParseEnvironment() {
        return parseEnvironment;
    }

    public boolean hasOwnScopeForAssignments() {
        return ownScopeForAssignments;
    }

    public boolean getNeverAssignInParentScope() {
        return neverAssignInParentScope;
    }

    public boolean isModuleBody() {
        return isModuleBody;
    }

    public SharedMethodInfo getSharedMethodInfo() {
        return sharedMethodInfo;
    }

    public List<FrameSlot> getFlipFlopStates() {
        return flipFlopStates;
    }

    public String getNamedMethodName() {
        return namedMethodName;
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

    public LexicalScope unsafeGetLexicalScope() {
        return parseEnvironment.getLexicalScope();
    }
}
