/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import com.oracle.truffle.api.CompilerDirectives;
import org.prism.AbstractNodeVisitor;
import org.prism.Nodes;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.AssignableNode;
import org.truffleruby.core.array.MultipleAssignmentNode;
import org.truffleruby.core.array.NoopAssignableNode;
import org.truffleruby.core.cast.SplatCastNode;
import org.truffleruby.core.cast.SplatCastNodeGen;
import org.truffleruby.language.RubyNode;

import java.util.ArrayList;
import java.util.List;

/** Translate Nodes.MultiWriteNode node.
 *
 * NOTE: cannot inherit from YARPBaseTranslator because it returns AssignableNode instead of RubyNode. */
public final class YARPMultiWriteNodeTranslator extends AbstractNodeVisitor<AssignableNode> {

    private final Nodes.MultiWriteNode node;
    private final RubyLanguage language;
    private final YARPTranslator yarpTranslator;
    /** Nodes to initialize local variables before assign values. Store in the local variables receivers of attribute
     * (a.b = ...) and reference (a[b] = ...) assignments as well as fully qualified constants' modules (A::B = ...).
     * They should be evaluated before values to conform with the left-to-right order semantic. */
    private final List<RubyNode> prolog;

    public YARPMultiWriteNodeTranslator(
            Nodes.MultiWriteNode node,
            RubyLanguage language,
            YARPTranslator yarpTranslator) {
        this.node = node;
        this.language = language;
        this.yarpTranslator = yarpTranslator;
        this.prolog = new ArrayList<>();
    }

    public RubyNode translate() {
        final RubyNode rubyNode;
        final RubyNode rhsNode = node.value.accept(yarpTranslator);
        final SplatCastNode splatCastNode = SplatCastNodeGen.create(
                language,
                SplatCastNode.NilBehavior.ARRAY_WITH_NIL,
                true,
                null);

        final AssignableNode[] preNodes = new AssignableNode[node.lefts.length];
        for (int i = 0; i < node.lefts.length; i++) {
            preNodes[i] = node.lefts[i].accept(this);
        }

        final AssignableNode restNode;
        if (node.rest != null) {
            if (node.rest instanceof Nodes.ImplicitRestNode) {
                // a, = []
                // do nothing
                restNode = null;
            } else {
                restNode = node.rest.accept(this);
            }
        } else {
            restNode = null;
        }

        final AssignableNode[] postNodes = new AssignableNode[node.rights.length];
        for (int i = 0; i < node.rights.length; i++) {
            postNodes[i] = node.rights[i].accept(this);
        }

        RubyNode multipleAssignmentNode = new MultipleAssignmentNode(
                preNodes,
                restNode,
                postNodes,
                splatCastNode,
                rhsNode);

        if (prolog.isEmpty()) {
            rubyNode = multipleAssignmentNode;
        } else {
            RubyNode prologSequence = YARPBaseTranslator.sequence(prolog.toArray(RubyNode.EMPTY_ARRAY));
            rubyNode = YARPBaseTranslator.sequence(prologSequence, multipleAssignmentNode);
        }

        return rubyNode;
    }

    @Override
    public AssignableNode visitClassVariableTargetNode(Nodes.ClassVariableTargetNode node) {
        final RubyNode rubyNode = node.accept(yarpTranslator);
        return ((AssignableNode) rubyNode).toAssignableNode();
    }

    @Override
    public AssignableNode visitCallTargetNode(Nodes.CallTargetNode node) {
        // store receiver in a local variable to evaluate before assigned value
        var receiverExpression = new YARPExecutedOnceExpression("receiver", node.receiver, yarpTranslator);
        RubyNode writeReceiverNode = receiverExpression.getWriteNode();
        Nodes.Node readReceiver = receiverExpression.getReadYARPNode();

        prolog.add(writeReceiverNode);
        node = new Nodes.CallTargetNode(node.flags, readReceiver, node.name, node.startOffset, node.length);

        final RubyNode rubyNode = node.accept(yarpTranslator);
        return ((AssignableNode) rubyNode).toAssignableNode();
    }

    @Override
    public AssignableNode visitConstantPathTargetNode(Nodes.ConstantPathTargetNode node) {
        // store parent lexical scope (e.g foo in foo::C = ...) in a local variable to evaluate before assigned value
        if (node.parent != null) {
            var parentExpression = new YARPExecutedOnceExpression("parent", node.parent, yarpTranslator);
            RubyNode writeParentNode = parentExpression.getWriteNode();
            Nodes.Node readParent = parentExpression.getReadYARPNode();

            prolog.add(writeParentNode);
            node = new Nodes.ConstantPathTargetNode(readParent, node.child, node.startOffset, node.length);
        }

        final RubyNode rubyNode = node.accept(yarpTranslator);
        return ((AssignableNode) rubyNode).toAssignableNode();
    }

    @Override
    public AssignableNode visitConstantTargetNode(Nodes.ConstantTargetNode node) {
        final RubyNode rubyNode = node.accept(yarpTranslator);
        return ((AssignableNode) rubyNode).toAssignableNode();
    }

    @Override
    public AssignableNode visitGlobalVariableTargetNode(Nodes.GlobalVariableTargetNode node) {
        final RubyNode rubyNode = node.accept(yarpTranslator);
        return ((AssignableNode) rubyNode).toAssignableNode();
    }

    @Override
    public AssignableNode visitImplicitRestNode(Nodes.ImplicitRestNode node) {
        throw CompilerDirectives.shouldNotReachHere("handled in #translate");
    }

    @Override
    public AssignableNode visitIndexTargetNode(Nodes.IndexTargetNode node) {
        // store receiver in a local variable to evaluate before assigned value
        var receiverExpression = new YARPExecutedOnceExpression("receiver", node.receiver, yarpTranslator);
        RubyNode writeReceiverNode = receiverExpression.getWriteNode();
        Nodes.Node readReceiver = receiverExpression.getReadYARPNode();

        prolog.add(writeReceiverNode);

        // store arguments in local variables to evaluate after receiver but before assigned values
        final Nodes.ArgumentsNode arguments;
        if (node.arguments != null) {
            var argumentsReads = new Nodes.Node[node.arguments.arguments.length];

            for (int i = 0; i < node.arguments.arguments.length; i++) {
                var e = new YARPExecutedOnceExpression("argument", node.arguments.arguments[i], yarpTranslator);
                prolog.add(e.getWriteNode());
                argumentsReads[i] = e.getReadYARPNode();
            }

            arguments = new Nodes.ArgumentsNode(node.arguments.flags, argumentsReads, node.arguments.startOffset,
                    node.arguments.length);
        } else {
            arguments = null;
        }

        node = new Nodes.IndexTargetNode(node.flags, readReceiver, arguments, node.block, node.startOffset,
                node.length);

        final RubyNode rubyNode = node.accept(yarpTranslator);
        return ((AssignableNode) rubyNode).toAssignableNode();
    }

    @Override
    public AssignableNode visitInstanceVariableTargetNode(Nodes.InstanceVariableTargetNode node) {
        final RubyNode rubyNode = node.accept(yarpTranslator);
        return ((AssignableNode) rubyNode).toAssignableNode();
    }

    @Override
    public AssignableNode visitLocalVariableTargetNode(Nodes.LocalVariableTargetNode node) {
        final RubyNode rubyNode = node.accept(yarpTranslator);
        return ((AssignableNode) rubyNode).toAssignableNode();
    }

    @Override
    public AssignableNode visitMultiTargetNode(Nodes.MultiTargetNode node) {
        final var translator = new YARPMultiTargetNodeTranslator(node, language, yarpTranslator, null);
        final MultipleAssignmentNode multipleAssignmentNode = translator.translate();

        prolog.addAll(translator.prolog);

        return multipleAssignmentNode.toAssignableNode();
    }

    @Override
    public AssignableNode visitSplatNode(Nodes.SplatNode node) {
        if (node.expression != null) {
            final RubyNode rubyNode = node.expression.accept(yarpTranslator);
            return ((AssignableNode) rubyNode).toAssignableNode();
        } else {
            return new NoopAssignableNode();
        }
    }

    @Override
    protected AssignableNode defaultVisit(Nodes.Node node) {
        throw new Error("Unknown node: " + node);
    }

}
