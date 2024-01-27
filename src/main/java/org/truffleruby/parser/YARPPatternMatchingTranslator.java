/*
 * Copyright (c) 2013, 2024 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import java.util.Arrays;

import com.oracle.truffle.api.CompilerDirectives;
import org.prism.Nodes;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayIndexNodes;
import org.truffleruby.core.array.ArrayPatternLengthCheckNode;
import org.truffleruby.core.array.ArraySliceNodeGen;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.AndNodeGen;
import org.truffleruby.language.control.ExecuteAndReturnTrueNode;
import org.truffleruby.language.control.NotNodeGen;
import org.truffleruby.language.literal.TruffleInternalModuleLiteralNode;
import org.truffleruby.language.locals.ReadLocalNode;
import org.truffleruby.language.locals.WriteLocalNode;


/** Translate the pattern of pattern matching. Executing the translated node must result in true or false. Every visit
 * method here should return a node which is the condition of whether it matched. Visit methods can change & restore
 * {@code currentValueToMatch} to change which expression is being matched against. */
public final class YARPPatternMatchingTranslator extends YARPBaseTranslator {

    private final YARPTranslator yarpTranslator;

    private RubyNode currentValueToMatch;

    public YARPPatternMatchingTranslator(
            RubyLanguage language,
            TranslatorEnvironment environment,
            RubySource rubySource,
            YARPTranslator yarpTranslator) {
        super(language, environment, rubySource);
        this.yarpTranslator = yarpTranslator;
    }

    public RubyNode translatePatternNode(Nodes.Node patternNode, RubyNode expressionValue) {
        currentValueToMatch = expressionValue;
        return patternNode.accept(this);
    }

    @Override
    public RubyNode visitIfNode(Nodes.IfNode node) { // a guard like `in [a] if a.even?`
        assert node.statements.body.length == 1;
        var pattern = node.statements.body[0].accept(this);
        var condition = node.predicate.accept(yarpTranslator); // translate after the pattern which might introduce new variables
        return AndNodeGen.create(pattern, condition);
    }

    @Override
    public RubyNode visitUnlessNode(Nodes.UnlessNode node) { // a guard like `in [a] unless a.even?`
        assert node.statements.body.length == 1;
        var pattern = node.statements.body[0].accept(this);
        var condition = NotNodeGen.create(node.predicate.accept(yarpTranslator)); // translate after the pattern which might introduce new variables
        return AndNodeGen.create(pattern, condition);
    }

    @Override
    public RubyNode visitArrayPatternNode(Nodes.ArrayPatternNode node) {
        var preNodes = node.requireds;
        var restNode = node.rest;
        var postNodes = node.posts;

        int preSize = preNodes.length;
        int postSize = postNodes.length;

        var deconstructed = createCallNode(new TruffleInternalModuleLiteralNode(), "deconstruct_checked",
                currentValueToMatch);

        final int deconstructedSlot = environment.declareLocalTemp("pattern_deconstruct_array");
        final ReadLocalNode readTemp = environment.readNode(deconstructedSlot, node);
        final RubyNode assignTemp = readTemp.makeWriteNode(deconstructed);
        currentValueToMatch = readTemp;

        RubyNode condition = new ArrayPatternLengthCheckNode(preSize + postSize,
                currentValueToMatch, restNode != null);

        if (node.constant != null) { // Constant[a]
            condition = AndNodeGen.create(matchValue(node.constant), condition);
        }

        for (int i = 0; i < preNodes.length; i++) {
            var preNode = preNodes[i];

            RubyNode prev = currentValueToMatch;
            currentValueToMatch = ArrayIndexNodes.ReadConstantIndexNode.create(currentValueToMatch, i);
            try {
                condition = AndNodeGen.create(condition, preNode.accept(this));
            } finally {
                currentValueToMatch = prev;
            }
        }

        if (restNode != null) {
            if (restNode instanceof Nodes.SplatNode splatNode) {
                if (splatNode.expression != null) {
                    RubyNode prev = currentValueToMatch;
                    currentValueToMatch = ArraySliceNodeGen.create(preSize, -postSize, currentValueToMatch);
                    try {
                        condition = AndNodeGen.create(condition, splatNode.expression.accept(this));
                    } finally {
                        currentValueToMatch = prev;
                    }
                } else { // in [1, *, 2]
                    // Nothing
                }
            } else if (restNode instanceof Nodes.ImplicitRestNode) { // in [0, 1,]
                // Nothing
            } else {
                throw CompilerDirectives.shouldNotReachHere(node.getClass().getName());
            }
        }

        for (int i = 0; i < postNodes.length; i++) {
            var postNode = postNodes[i];
            int index = -postNodes.length + i;
            RubyNode prev = currentValueToMatch;
            currentValueToMatch = ArrayIndexNodes.ReadConstantIndexNode.create(currentValueToMatch, index);
            try {
                condition = AndNodeGen.create(condition, postNode.accept(this));
            } finally {
                currentValueToMatch = prev;
            }
        }

        return YARPTranslator.sequence(node, Arrays.asList(assignTemp, condition));
    }

    @Override
    public RubyNode visitHashPatternNode(Nodes.HashPatternNode node) {
        // var deconstructed = createCallNode(currentValueToMatch, "deconstruct_keys", new NilLiteralNode());

        // Not correct, the pattern cannot always be represented as a runtime value and this overflows the stack:
        //        return createCallNode(
        //                new TruffleInternalModuleLiteralNode(),
        //                "hash_pattern_matches?",
        //                node.accept(this),
        //                NodeUtil.cloneNode(deconstructed));

        return defaultVisit(node);
    }

    @Override
    public RubyNode visitLocalVariableTargetNode(Nodes.LocalVariableTargetNode node) {
        WriteLocalNode writeLocalNode = yarpTranslator.visitLocalVariableTargetNode(node);
        writeLocalNode.setValueNode(currentValueToMatch);
        return new ExecuteAndReturnTrueNode(writeLocalNode);
    }

    @Override
    public RubyNode visitPinnedVariableNode(Nodes.PinnedVariableNode node) {
        return matchValue(node.variable);
    }

    @Override
    public RubyNode visitPinnedExpressionNode(Nodes.PinnedExpressionNode node) {
        return matchValue(node.expression);
    }

    @Override
    protected RubyNode defaultVisit(Nodes.Node node) {
        return matchValue(node);
    }

    private RubyNode matchValue(Nodes.Node value) {
        RubyNode translatedValue = value.accept(yarpTranslator);
        return createCallNode(translatedValue, "===", currentValueToMatch);
    }

}
