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


public class PatternMatchingTranslator extends Translator {

    ParseNode data;
    ListParseNode cases;
    TranslatorEnvironment environment;

    public PatternMatchingTranslator(
            RubyLanguage language,
            Source source,
            ParserContext parserContext,
            Node currentNode,
            ParseNode data,
            ListParseNode cases,
            TranslatorEnvironment environment) {
        super(language, source, parserContext, currentNode);
        this.data = data; // data to match on
        this.cases = cases; // cases to check.
        this.environment = environment;
    }

    @Override
    protected RubyNode defaultVisit(ParseNode node) {
        throw new UnsupportedOperationException(node.toString() + " " + node.getPosition());
    }

    public RubyNode translatePatternNode(ParseNode patternNode, ParseNode expressionNode, RubyNode expressionValue,
            SourceIndexLength sourceSection) {
        final RubyCallNodeParameters deconstructCallParameters;
        final RubyCallNodeParameters matcherCallParameters;
        final RubyNode receiver;
        final RubyNode deconstructed;

        switch (patternNode.getNodeType()) {
            case ARRAYNODE:
                // Pattern-match element-wise recursively if possible.
                final int size = ((ArrayParseNode) patternNode).size();
                if (expressionNode.getNodeType() == NodeType.ARRAYNODE &&
                        ((ArrayParseNode) expressionNode).size() == size) {
                    final ParseNode[] patternElements = ((ArrayParseNode) patternNode).children();
                    final ParseNode[] expressionElements = ((ArrayParseNode) expressionNode).children();

                    final RubyNode[] matches = new RubyNode[size];

                    // For each element of the case expression, evaluate and assign it, then run the pattern-matching
                    // on the element
                    for (int n = 0; n < size; n++) {
                        final int tempSlot = environment.declareLocalTemp("caseElem" + n);
                        final ReadLocalNode readTemp = environment.readNode(tempSlot, sourceSection);
                        final RubyNode assignTemp = readTemp.makeWriteNode(expressionElements[n].accept(this));
                        matches[n] = sequence(sourceSection, Arrays.asList(
                                assignTemp,
                                translatePatternNode(
                                        patternElements[n],
                                        expressionElements[n],
                                        readTemp,
                                        sourceSection)));
                    }

                    // Incorporate the element-wise pattern-matching into the AST, with the longer right leg since
                    // AndNode is visited left to right
                    RubyNode match = matches[size - 1];
                    for (int n = size - 2; n >= 0; n--) {
                        match = new AndNode(matches[n], match);
                    }
                    return match;
                }

                deconstructCallParameters = new RubyCallNodeParameters(
                        expressionValue,
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

                matcherCallParameters = new RubyCallNodeParameters(
                        receiver,
                        "array_pattern_matches?",
                        null,
                        EmptyArgumentsDescriptor.INSTANCE,
                        new RubyNode[]{ patternNode.accept(this), NodeUtil.cloneNode(deconstructed) },
                        false,
                        true);
                return language.coreMethodAssumptions
                        .createCallNode(matcherCallParameters, environment);
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

    public RubyNode translateArrayPatternNode(ArrayPatternParseNode node, ArrayParseNode data) {
        // For now, we are assuming that only preArgs exist, and the pattern only consists of simple constant values.
        final int size = node.minimumArgsNum();
        ListParseNode pre = node.getPreArgs();
        ParseNode[] ch = pre.children();
        for (int i = 0; i < pre.size(); i++) {
            if (ch[i] === data )
        }
    }


}
