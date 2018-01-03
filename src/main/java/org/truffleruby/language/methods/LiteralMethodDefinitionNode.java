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

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

/**
 * Define a method from a method literal (def mymethod ... end).
 * That is, create an InternalMethod and add it to the current module (default definee).
 */
public class LiteralMethodDefinitionNode extends RubyNode {

    private final boolean isDefSingleton;

    @Child private RubyNode moduleNode;
    @Child private MethodDefinitionNode methodDefinitionNode;
    @Child private RubyNode visibilityNode;

    @Child private AddMethodNode addMethodNode;

    public LiteralMethodDefinitionNode(boolean isDefSingleton, RubyNode moduleNode, MethodDefinitionNode methodDefinitionNode) {
        this.isDefSingleton = isDefSingleton;
        this.moduleNode = moduleNode;
        this.methodDefinitionNode = methodDefinitionNode;
        if (!isDefSingleton) {
            this.visibilityNode = new GetCurrentVisibilityNode();
        }
        this.addMethodNode = AddMethodNodeGen.create(isDefSingleton, null, null, null);
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

        final InternalMethod method = (InternalMethod) methodDefinitionNode.execute(frame);

        addMethodNode.executeAddMethod(module, method.withDeclaringModule(module), visibility);

        return getSymbol(method.getName());
    }

}
