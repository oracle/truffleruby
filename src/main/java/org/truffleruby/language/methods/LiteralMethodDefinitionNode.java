/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.methods;

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

/**
 * Define a method from a method literal (def mymethod ... end).
 * That is, create an InternalMethod and add it to the current module (default definee).
 */
public class LiteralMethodDefinitionNode extends RubyNode {

    private final String name;
    private final SharedMethodInfo sharedMethodInfo;
    private final RootCallTarget callTarget;
    private final boolean isDefSingleton;

    @Child private RubyNode moduleNode;
    @Child private GetCurrentVisibilityNode visibilityNode;

    @Child private AddMethodNode addMethodNode;

    public LiteralMethodDefinitionNode(
            RubyNode moduleNode,
            String name,
            SharedMethodInfo sharedMethodInfo,
            RootCallTarget callTarget,
            boolean isDefSingleton) {
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
            visibility = visibilityNode.getVisibility(frame);
        }

        final DeclarationContext declarationContext = RubyArguments
                .getDeclarationContext(frame)
                .withVisibility(Visibility.PUBLIC);
        final InternalMethod currentMethod = RubyArguments.getMethod(frame);

        final InternalMethod method = new InternalMethod(
                getContext(),
                sharedMethodInfo,
                currentMethod.getLexicalScope(),
                declarationContext,
                name,
                module,
                visibility,
                false,
                null,
                callTarget,
                null);

        addMethodNode.executeAddMethod(module, method, visibility);

        return getSymbol(name);
    }

}
