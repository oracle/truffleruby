/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.builtins;

import com.oracle.truffle.api.dsl.NodeFactory;
import org.truffleruby.language.RubyContextSourceNode;

import com.oracle.truffle.api.dsl.GenerateNodeFactory;
import org.truffleruby.language.RubyNode;

@GenerateNodeFactory
public abstract class CoreMethodNode extends RubyContextSourceNode {

    @Override
    public RubyNode cloneUninitialized() {
        NodeFactory<RubyNode> factory = BuiltinsClasses.FACTORIES.get(getClass().getSuperclass());
        var copy = (CoreMethodNode) CoreMethodNodeManager.createNodeFromFactory(factory, RubyNode.EMPTY_ARRAY);
        copy.copyFlags(this);
        return copy;
    }

}
