/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import org.prism.Nodes;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.locals.LocalVariableType;
import org.truffleruby.language.locals.ReadLocalVariableNode;
import org.truffleruby.language.locals.WriteLocalVariableNode;

/** Similar to ValueFromNode class but for YARP nodes */
public final class YARPExecutedOnceExpression {
    final String name;
    final int slot;
    final Nodes.Node node;
    final RubyNode valueNode;
    final YARPTranslator yarpTranslator;

    public YARPExecutedOnceExpression(String baseName, Nodes.Node node, YARPTranslator yarpTranslator) {
        this.node = node;
        this.yarpTranslator = yarpTranslator;

        TranslatorEnvironment environment = yarpTranslator.getEnvironment();
        name = environment.allocateLocalTemp(baseName);
        slot = environment.declareVar(name);
        valueNode = node.accept(yarpTranslator);
    }

    public Nodes.Node getReadYARPNode() {
        return new Nodes.LocalVariableReadNode(name, 0, node.startOffset, node.length);
    }

    public RubyNode getReadNode() {
        return new ReadLocalVariableNode(LocalVariableType.FRAME_LOCAL, slot);
    }

    public RubyNode getWriteNode() {
        return new WriteLocalVariableNode(slot, valueNode);
    }
}
