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
import org.truffleruby.language.objects.SelfNode;

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

        if (!(node instanceof Nodes.SelfNode)) {
            TranslatorEnvironment environment = yarpTranslator.getEnvironment();
            name = environment.allocateLocalTemp(baseName);
            slot = environment.declareVar(name);
            valueNode = node.accept(yarpTranslator);
        } else {
            // keep self as-is so that the check for visibility when translating the call node works
            name = null;
            slot = -1;
            valueNode = null;
        }
    }

    public Nodes.Node getReadYARPNode() {
        if (node instanceof Nodes.SelfNode) {
            return node;
        } else {
            return new Nodes.LocalVariableReadNode(name, 0, node.startOffset, node.length);
        }
    }

    public RubyNode getReadNode() {
        if (node instanceof Nodes.SelfNode) {
            return new SelfNode();
        } else {
            return new ReadLocalVariableNode(LocalVariableType.FRAME_LOCAL, slot);
        }
    }

    public RubyNode getWriteNode() {
        if (node instanceof Nodes.SelfNode) {
            return null;
        } else {
            return new WriteLocalVariableNode(slot, valueNode);
        }
    }
}
