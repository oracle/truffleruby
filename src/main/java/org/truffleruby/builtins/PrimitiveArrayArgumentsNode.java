/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.builtins;

import org.truffleruby.annotations.Primitive;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.dsl.NodeChild;

@NodeChild(value = "argumentNodes", type = RubyNode[].class)
public abstract class PrimitiveArrayArgumentsNode extends PrimitiveNode {

    public abstract RubyNode[] getArgumentNodes();

    @Override
    public RubyNode cloneUninitialized() {
        String primitiveName = getClass().getSuperclass().getAnnotation(Primitive.class).name();
        var factory = getLanguage().primitiveManager.getPrimitive(primitiveName).getFactory();
        RubyNode[] copiedArguments = cloneUninitialized(getArgumentNodes());
        var copy = (PrimitiveArrayArgumentsNode) CoreMethodNodeManager.createNodeFromFactory(factory, copiedArguments);
        return copy.copyFlags(this);
    }

}
