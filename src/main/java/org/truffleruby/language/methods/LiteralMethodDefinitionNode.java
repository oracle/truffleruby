/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.methods;

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

/**
 * Define a method from a method literal (def mymethod ... end).
 * That is, create an InternalMethod and add it to the current module (default definee).
 */
public class LiteralMethodDefinitionNode extends RubyNode {

    private final String name;
    private final SharedMethodInfo sharedMethodInfo;
    private final CallTarget callTarget;
    private final boolean isDefSingleton;

    @Child private GetDefaultDefineeNode getDefaultDefineeNode;
    @Child private RubyNode moduleNode;
    @Child private RubyNode visibilityNode;

    @Child private AddMethodNode addMethodNode;

    public LiteralMethodDefinitionNode(RubyNode moduleNode, String name, SharedMethodInfo sharedMethodInfo, CallTarget callTarget, boolean isDefSingleton) {
        this.name = name;
        this.sharedMethodInfo = sharedMethodInfo;
        this.callTarget = callTarget;
        this.isDefSingleton = isDefSingleton;
        this.moduleNode = moduleNode;
        if (!isDefSingleton) {
            this.visibilityNode = new GetCurrentVisibilityNode();
        }
        this.addMethodNode = AddMethodNode.create(isDefSingleton);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final DynamicObject module = (DynamicObject) moduleNode.execute(frame);

        final Visibility visibility;
        if (isDefSingleton) {
            visibility = Visibility.PUBLIC;
        } else {
            visibility = (Visibility) visibilityNode.execute(frame);
        }

        final DeclarationContext declarationContext = RubyArguments.getDeclarationContext(frame);
        final InternalMethod currentMethod = RubyArguments.getMethod(frame);
        final Object self = RubyArguments.getSelf(frame);
        final DynamicObject capturedDefaultDefinee = captureDefaultDefinee(declarationContext, currentMethod, self);

        final InternalMethod method = new InternalMethod(getContext(),
                sharedMethodInfo, currentMethod.getLexicalScope(), name, module, visibility, false, null, callTarget, null, capturedDefaultDefinee);

        addMethodNode.executeAddMethod(module, method, visibility);

        return getSymbol(name);
    }

    @TruffleBoundary
    private DynamicObject captureDefaultDefinee(DeclarationContext declarationContext, InternalMethod currentMethod, Object self) {
        if (declarationContext == DeclarationContext.INSTANCE_EVAL) {
            if (getDefaultDefineeNode == null) {
                getDefaultDefineeNode = insert(new GetDefaultDefineeNode());
            }
            return getDefaultDefineeNode.getDefaultDefinee(currentMethod, self, declarationContext);
        } else {
            return null;
        }
    }

}
