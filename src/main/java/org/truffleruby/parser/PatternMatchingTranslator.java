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

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayIndexNodes;
import org.truffleruby.core.array.ArrayLiteralNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.arguments.EmptyArgumentsDescriptor;
import org.truffleruby.language.control.AndNode;
import org.truffleruby.language.control.OrNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;
import org.truffleruby.language.literal.BooleanLiteralNode;
import org.truffleruby.language.literal.NilLiteralNode;
import org.truffleruby.language.literal.TruffleInternalModuleLiteralNode;
import org.truffleruby.language.locals.WriteLocalNode;
import org.truffleruby.parser.ast.ArrayParseNode;
import org.truffleruby.parser.ast.ArrayPatternParseNode;
import org.truffleruby.parser.ast.FixnumParseNode;
import org.truffleruby.parser.ast.ListParseNode;
import org.truffleruby.parser.ast.LocalAsgnParseNode;
import org.truffleruby.parser.ast.LocalVarParseNode;
import org.truffleruby.parser.ast.ParseNode;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.source.Source;

public class PatternMatchingTranslator extends BaseTranslator {

    ParseNode data;
    ListParseNode cases;
    TranslatorEnvironment environment;
    BodyTranslator bodyTranslator;

    RubyNode currentValueToMatch;

    public PatternMatchingTranslator(
            RubyLanguage language,
            Source source,
            ParserContext parserContext,
            Node currentNode,
            ParseNode data,
            ListParseNode cases,
            TranslatorEnvironment environment,
            BodyTranslator bodyTranslator) {
        super(language, source, parserContext, currentNode, environment);
        this.data = data; // data to match on
        this.cases = cases; // cases to check.
        this.environment = environment;
        this.bodyTranslator = bodyTranslator;
    }

    @Override
    protected RubyNode defaultVisit(ParseNode node) {
        throw new UnsupportedOperationException(node.toString() + " " + node.getPosition());
    }

    @Override
    public RubyNode visitArrayPatternNode(ArrayPatternParseNode arrayPatternParseNode) {
        final RubyCallNodeParameters deconstructCallParameters;
        final RubyCallNodeParameters matcherCallParameters;
        final RubyCallNodeParameters matcherCallParametersPost;
        final RubyNode receiver;
        final RubyNode deconstructed;
        final SourceIndexLength sourceSection = arrayPatternParseNode.getPosition();

        // handle only preArgs and postArgs for now
//        if (arrayPatternParseNode.hasRestArg()) {
//            throw CompilerDirectives.shouldNotReachHere();
//        }

        ListParseNode arrayParseNode = arrayPatternParseNode.getPreArgs();
        ListParseNode arrayParseNodePost = arrayPatternParseNode.getPostArgs();
        var arrayParseNodeRest = arrayPatternParseNode.getRestArg();

        deconstructCallParameters = new RubyCallNodeParameters(
                currentValueToMatch,
                "deconstruct",
                null,
                EmptyArgumentsDescriptor.INSTANCE,
                RubyNode.EMPTY_ARRAY,
                false,
                true);
        deconstructed = language.coreMethodAssumptions
                .createCallNode(deconstructCallParameters, environment);

        receiver = new TruffleInternalModuleLiteralNode();
        receiver.unsafeSetSourceSection(sourceSection);

        var patternArray = arrayParseNode.accept(this);

        matcherCallParameters = new RubyCallNodeParameters(
                receiver,
                "array_pattern_matches?",
                null,
                EmptyArgumentsDescriptor.INSTANCE,
                new RubyNode[]{ patternArray, NodeUtil.cloneNode(deconstructed) },
                false,
                true);

        var preCallNode = language.coreMethodAssumptions
                .createCallNode(matcherCallParameters, environment);
        if(arrayParseNodePost != null) {
            var patternArrayPost = arrayParseNodePost.accept(this);
            matcherCallParametersPost = new RubyCallNodeParameters(
                    receiver,
                    "array_pattern_matches?",
                    null,
                    EmptyArgumentsDescriptor.INSTANCE,
                    new RubyNode[]{patternArrayPost, NodeUtil.cloneNode(deconstructed)},
                    false,
                    true);
            preCallNode = new AndNode(preCallNode,
                    language.coreMethodAssumptions
                            .createCallNode(matcherCallParametersPost, environment));
        }

        return preCallNode;
    }

    public RubyNode translatePatternNode(ParseNode patternNode,
            ParseNode expressionNode /* TODO should not be passed */, RubyNode expressionValue,
            SourceIndexLength sourceSection) {
        final RubyCallNodeParameters deconstructCallParameters;
        final RubyCallNodeParameters matcherCallParameters;
        final RubyCallNodeParameters matcherCallParametersPost;
        final RubyNode receiver;
        final RubyNode deconstructed;

        currentValueToMatch = expressionValue;

        // TODO move the other cases to the visitor pattern
        switch (patternNode.getNodeType()) {
            case ARRAYPATTERNNODE:
                return this.visitArrayPatternNode((ArrayPatternParseNode) patternNode);
            case HASHNODE:
                deconstructCallParameters = new RubyCallNodeParameters(
                        expressionValue,
                        "deconstruct_keys",
                        null,
                        EmptyArgumentsDescriptor.INSTANCE,
                        new RubyNode[]{ new NilLiteralNode(true) },
                        false,
                        true);
                deconstructed = language.coreMethodAssumptions
                        .createCallNode(deconstructCallParameters, environment);

                receiver = new TruffleInternalModuleLiteralNode();
                receiver.unsafeSetSourceSection(sourceSection);

                matcherCallParameters = new RubyCallNodeParameters(
                        receiver,
                        "hash_pattern_matches?",
                        null,
                        EmptyArgumentsDescriptor.INSTANCE,
                        new RubyNode[]{ patternNode.accept(this), NodeUtil.cloneNode(deconstructed) },
                        false,
                        true);

                return language.coreMethodAssumptions
                        .createCallNode(matcherCallParameters, environment);
            case FINDPATTERNNODE:

            case LOCALVARNODE:
                // Assigns the value of an existing variable pattern as the value of the expression.
                // May need to add a case with same/similar logic for new variables.
                final RubyNode assignmentNode = new LocalAsgnParseNode(
                        patternNode.getPosition(),
                        ((LocalVarParseNode) patternNode).getName(),
                        ((LocalVarParseNode) patternNode).getDepth(),
                        expressionNode).accept(this);
                return new OrNode(assignmentNode, new BooleanLiteralNode(true)); // TODO refactor to remove "|| true"
            default:
                matcherCallParameters = new RubyCallNodeParameters(
                        patternNode.accept(this),
                        "===",
                        null,
                        EmptyArgumentsDescriptor.INSTANCE,
                        new RubyNode[]{ NodeUtil.cloneNode(expressionValue) },
                        false,
                        true);
                return language.coreMethodAssumptions
                        .createCallNode(matcherCallParameters, environment);
        }
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
    public RubyNode visitFixnumNode(FixnumParseNode node) {
        return bodyTranslator.visitFixnumNode(node);
    }

    //    public RubyNode translateArrayPatternNode(ArrayPatternParseNode node, ArrayParseNode data) {
    //        // For now, we are assuming that only preArgs exist, and the pattern only consists of simple constant values.
    //        final int size = node.minimumArgsNum();
    //        ListParseNode pre = node.getPreArgs();
    //        ParseNode[] ch = pre.children();
    //        for (int i = 0; i < pre.size(); i++) {
    //            if (ch[i] === data ) {}
    //        }
    //    }


}
