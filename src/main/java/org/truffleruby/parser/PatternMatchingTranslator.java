/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayIndexNodes;
import org.truffleruby.core.array.ArrayLiteralNode;
import org.truffleruby.core.array.ArrayPatternLengthCheckNode;
import org.truffleruby.core.array.ArraySliceNodeGen;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.control.AndNode;
import org.truffleruby.language.control.ExecuteAndReturnTrueNode;
import org.truffleruby.language.control.NotNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.literal.NilLiteralNode;
import org.truffleruby.language.literal.TruffleInternalModuleLiteralNode;
import org.truffleruby.language.locals.ReadLocalNode;
import org.truffleruby.language.locals.WriteLocalNode;
import org.truffleruby.parser.ast.ArrayParseNode;
import org.truffleruby.parser.ast.ArrayPatternParseNode;
import org.truffleruby.parser.ast.CallParseNode;
import org.truffleruby.parser.ast.ConstParseNode;
import org.truffleruby.parser.ast.DAsgnParseNode;
import org.truffleruby.parser.ast.DStrParseNode;
import org.truffleruby.parser.ast.DotParseNode;
import org.truffleruby.parser.ast.FalseParseNode;
import org.truffleruby.parser.ast.FindPatternParseNode;
import org.truffleruby.parser.ast.FixnumParseNode;
import org.truffleruby.parser.ast.HashPatternParseNode;
import org.truffleruby.parser.ast.IfParseNode;
import org.truffleruby.parser.ast.LambdaParseNode;
import org.truffleruby.parser.ast.ListParseNode;
import org.truffleruby.parser.ast.LocalAsgnParseNode;
import org.truffleruby.parser.ast.NilParseNode;
import org.truffleruby.parser.ast.ParseNode;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.source.Source;
import org.truffleruby.parser.ast.RegexpParseNode;
import org.truffleruby.parser.ast.StarParseNode;
import org.truffleruby.parser.ast.StrParseNode;
import org.truffleruby.parser.ast.TrueParseNode;

import java.util.Arrays;

public class PatternMatchingTranslator extends BaseTranslator {

    final ListParseNode cases;
    final TranslatorEnvironment environment;
    final BodyTranslator bodyTranslator;

    RubyNode currentValueToMatch;

    public PatternMatchingTranslator(
            RubyLanguage language,
            Source source,
            ParserContext parserContext,
            Node currentNode,
            ListParseNode cases, // cases to check
            TranslatorEnvironment environment,
            BodyTranslator bodyTranslator) {
        super(language, source, parserContext, currentNode, environment);
        this.cases = cases;
        this.environment = environment;
        this.bodyTranslator = bodyTranslator;
    }

    public RubyNode translatePatternNode(ParseNode patternNode, RubyNode expressionValue) {
        currentValueToMatch = expressionValue;

        // TODO move the other cases to the visitor pattern
        switch (patternNode.getNodeType()) {
            case ARRAYPATTERNNODE:
                return this.visitArrayPatternNode((ArrayPatternParseNode) patternNode);
            case HASHPATTERNNODE: // both of these throw the same exception, hence this is skipped.
            case FINDPATTERNNODE:
                final RubyContext context = RubyLanguage.getCurrentContext();
                throw new RaiseException(
                        context,
                        context.getCoreExceptions().syntaxError(
                                "not yet handled in pattern matching: " + patternNode + " " + patternNode.getPosition(),
                                currentNode,
                                patternNode.getPosition().toSourceSection(source)));
            case IFNODE: // handles both if and unless
                var ifNode = (IfParseNode) patternNode;
                RubyNode pattern;
                RubyNode condition = ifNode.getCondition().accept(bodyTranslator);
                if (ifNode.getThenBody() != null) {
                    pattern = translatePatternNode(ifNode.getThenBody(), expressionValue);
                } else {
                    pattern = translatePatternNode(ifNode.getElseBody(), expressionValue);
                    condition = new NotNode(condition);
                }

                return new AndNode(pattern, condition);
            default:
                return createCallNode(patternNode.accept(this), "===", NodeUtil.cloneNode(expressionValue));
        }
    }

    @Override
    protected RubyNode defaultVisit(ParseNode node) {
        final RubyContext context = RubyLanguage.getCurrentContext();
        throw new RaiseException(
                context,
                context.getCoreExceptions().syntaxError(
                        "not yet handled in pattern matching: " + node.toString() + " " + node.getPosition(),
                        currentNode,
                        node.getPosition().toSourceSection(source)));
    }

    @Override
    public RubyNode visitArrayPatternNode(ArrayPatternParseNode arrayPatternParseNode) {
        final SourceIndexLength sourceSection = arrayPatternParseNode.getPosition();

        var preNodes = arrayPatternParseNode.getPreArgs();
        var postNodes = arrayPatternParseNode.getPostArgs();
        var restNode = arrayPatternParseNode.getRestArg();

        var deconstructed = createCallNode(new TruffleInternalModuleLiteralNode(), "deconstruct_checked",
                currentValueToMatch);

        final int deconstructedSlot = environment.declareLocalTemp("p_decon_array");
        final ReadLocalNode readTemp = environment.readNode(deconstructedSlot, sourceSection);
        final RubyNode assignTemp = readTemp.makeWriteNode(deconstructed);
        currentValueToMatch = readTemp;

        int preSize = arrayPatternParseNode.preArgsNum();

        RubyNode condition = new ArrayPatternLengthCheckNode(arrayPatternParseNode.minimumArgsNum(),
                currentValueToMatch, arrayPatternParseNode.hasRestArg());

        // Constant === pattern.deconstruct
        if (arrayPatternParseNode.hasConstant()) {
            ConstParseNode constant = (ConstParseNode) arrayPatternParseNode.getConstant();
            RubyNode constVal = constant.accept(this);
            var isInstance = createCallNode(constVal, "===", currentValueToMatch);
            condition = new AndNode(isInstance, condition);
        }

        for (int i = 0; i < preSize; i++) {
            var preNode = preNodes.get(i);
            RubyNode translatedPatternElement;
            RubyNode prev = currentValueToMatch;
            var exprElement = ArrayIndexNodes.ReadConstantIndexNode.create(currentValueToMatch, i);
            currentValueToMatch = exprElement;
            try {
                translatedPatternElement = preNode.accept(this);
            } finally {
                currentValueToMatch = prev;
            }

            var callNode = createCallNode(translatedPatternElement, "===", NodeUtil.cloneNode(exprElement));
            condition = new AndNode(condition, callNode);
        }

        if (restNode != null) {
            if (!(restNode instanceof StarParseNode)) {
                RubyNode prev = currentValueToMatch;
                RubyNode restAccept;
                int postSize;
                if (postNodes == null) { // null does not have length, so assign 0 manually.
                    postSize = 0;
                } else {
                    postSize = postNodes.size();
                }
                var exprSlice = ArraySliceNodeGen.create(preSize, -postSize, currentValueToMatch);
                currentValueToMatch = exprSlice;
                try {
                    restAccept = restNode.accept(this);
                } finally {
                    currentValueToMatch = prev;
                }
                var seq = new ExecuteAndReturnTrueNode(restAccept);
                condition = new AndNode(condition, seq);
            }
        }

        if (postNodes != null) {
            for (int i = 0; i < postNodes.size(); i++) {
                ParseNode loopPostNode = postNodes.get(i);
                RubyNode translatedPatternElement;
                RubyNode prev = currentValueToMatch;
                var exprElement = ArrayIndexNodes.ReadConstantIndexNode.create(currentValueToMatch,
                        -postNodes.size() + i);
                currentValueToMatch = exprElement;
                try {
                    translatedPatternElement = loopPostNode.accept(this);
                } finally {
                    currentValueToMatch = prev;
                }

                var callNode = createCallNode(
                        translatedPatternElement,
                        "===",
                        NodeUtil.cloneNode(exprElement));

                condition = new AndNode(condition, callNode);
            }
        }

        return sequence(sourceSection, Arrays.asList(assignTemp, condition));
    }

    public RubyNode visitFindPatternNode(FindPatternParseNode findPatternParseNode) {
        return findPatternParseNode.accept(this);
    }

    @Override
    public RubyNode visitHashPatternNode(HashPatternParseNode node) {
        var deconstructed = createCallNode(currentValueToMatch, "deconstruct_keys", new NilLiteralNode(true));

        return createCallNode(
                new TruffleInternalModuleLiteralNode(),
                "hash_pattern_matches?",
                node.accept(this),
                NodeUtil.cloneNode(deconstructed));
    }

    @Override
    public RubyNode visitArrayNode(ArrayParseNode node) {
        final ParseNode[] values = node.children();

        final RubyNode[] translatedValues = createArray(values.length);

        for (int n = 0; n < values.length; n++) {
            RubyNode prev = currentValueToMatch;
            currentValueToMatch = ArrayIndexNodes.ReadConstantIndexNode.create(currentValueToMatch, n);
            try {
                translatedValues[n] = values[n].accept(this);
            } finally {
                currentValueToMatch = prev;
            }
        }

        final RubyNode ret = ArrayLiteralNode.create(language, translatedValues);
        ret.unsafeSetSourceSection(node.getPosition());
        return addNewlineIfNeeded(node, ret);
    }

    @Override
    public RubyNode visitLocalAsgnNode(LocalAsgnParseNode node) {
        WriteLocalNode writeLocalNode = bodyTranslator.visitLocalAsgnNode(node);
        writeLocalNode.setValueNode(currentValueToMatch);
        return writeLocalNode;
    }

    @Override
    public RubyNode visitDAsgnNode(DAsgnParseNode node) {
        WriteLocalNode writeLocalNode = bodyTranslator.visitDAsgnNode(node);
        writeLocalNode.setValueNode(currentValueToMatch);
        return writeLocalNode;
    }

    @Override
    public RubyNode visitFixnumNode(FixnumParseNode node) {
        return bodyTranslator.visitFixnumNode(node);
    }

    // Possible value patterns delegated to BodyTranslator explicitly to prevent errors.
    @Override
    public RubyNode visitTrueNode(TrueParseNode node) {
        return bodyTranslator.visitTrueNode(node);
    }

    @Override
    public RubyNode visitFalseNode(FalseParseNode node) {
        return bodyTranslator.visitFalseNode(node);
    }

    @Override
    public RubyNode visitStrNode(StrParseNode node) {
        return bodyTranslator.visitStrNode(node);
    }

    @Override
    public RubyNode visitDotNode(DotParseNode node) {
        return bodyTranslator.visitDotNode(node);
    }

    @Override
    public RubyNode visitNilNode(NilParseNode node) {
        return bodyTranslator.visitNilNode(node);
    }

    @Override
    public RubyNode visitConstNode(ConstParseNode node) {
        return bodyTranslator.visitConstNode(node);
    }

    @Override
    public RubyNode visitRegexpNode(RegexpParseNode node) {
        return bodyTranslator.visitRegexpNode(node);
    }

    @Override
    public RubyNode visitLambdaNode(LambdaParseNode node) {
        return bodyTranslator.visitLambdaNode(node);
    }

    @Override
    public RubyNode visitDStrNode(DStrParseNode node) {
        return bodyTranslator.visitDStrNode(node);
    }

    @Override
    public RubyNode visitCallNode(CallParseNode node) {
        return bodyTranslator.visitCallNode(node);
    }
}
