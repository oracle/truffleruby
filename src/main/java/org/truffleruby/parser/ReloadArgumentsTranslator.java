/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import java.util.ArrayList;
import java.util.List;

import org.truffleruby.RubyLanguage;
import org.truffleruby.core.hash.ConcatHashLiteralNode;
import org.truffleruby.core.hash.HashLiteralNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.arguments.MissingArgumentBehavior;
import org.truffleruby.language.arguments.ReadPreArgumentNode;
import org.truffleruby.language.control.SequenceNode;
import org.truffleruby.language.literal.ObjectLiteralNode;
import org.truffleruby.parser.ast.ArgsParseNode;
import org.truffleruby.parser.ast.ArgumentParseNode;
import org.truffleruby.parser.ast.AssignableParseNode;
import org.truffleruby.parser.ast.KeywordArgParseNode;
import org.truffleruby.parser.ast.KeywordRestArgParseNode;
import org.truffleruby.parser.ast.MultipleAsgnParseNode;
import org.truffleruby.parser.ast.NoKeywordsArgParseNode;
import org.truffleruby.parser.ast.OptArgParseNode;
import org.truffleruby.parser.ast.ParseNode;
import org.truffleruby.parser.ast.RestArgParseNode;
import org.truffleruby.parser.ast.types.INameNode;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;

/** Produces code to reload arguments from local variables back into the arguments array. Only works for simple cases.
 * Used for zsuper calls which pass the same arguments, but will pick up modifications made to them in the method so
 * far. */
public class ReloadArgumentsTranslator extends Translator {

    private final BodyTranslator methodBodyTranslator;
    private final boolean hasKeywordArguments;

    private int index = 0;
    private int restParameterIndex = -1;

    public ReloadArgumentsTranslator(
            RubyLanguage language,
            Source source,
            ParserContext parserContext,
            Node currentNode,
            BodyTranslator methodBodyTranslator,
            ArgsParseNode argsNode) {
        super(language, source, parserContext, currentNode);
        this.methodBodyTranslator = methodBodyTranslator;
        this.hasKeywordArguments = argsNode.hasKwargs();
    }

    public int getRestParameterIndex() {
        return restParameterIndex;
    }

    @Override
    public RubyNode visitArgsNode(ArgsParseNode node) {
        final List<RubyNode> sequence = new ArrayList<>();
        final ParseNode[] args = node.getArgs();
        final int preCount = node.getPreCount();

        if (preCount > 0) {
            for (int i = 0; i < preCount; i++) {
                sequence.add(args[i].accept(this));
                index++;
            }
        }

        final int optArgsCount = node.getOptionalArgsCount();
        if (optArgsCount > 0) {
            final int optArgsIndex = node.getOptArgIndex();
            for (int i = 0; i < optArgsCount; i++) {
                sequence.add(args[optArgsIndex + i].accept(this));
                index++;
            }
        }

        if (node.hasRestArg()) {
            restParameterIndex = node.getPostIndex();
            sequence.add(node.getRestArgNode().accept(this));
        }

        int postCount = node.getPostCount();

        if (postCount > 0) {
            index = -postCount;
            int postIndex = node.getPostIndex();
            for (int i = 0; i < postCount; i++) {
                sequence.add(args[postIndex + i].accept(this));
                index++;
            }
        }

        RubyNode kwArgsNode = null;

        if (node.hasKwargs()) {
            final int keywordIndex = node.getKeywordsIndex();
            final int keywordCount = node.getKeywordCount();
            RubyNode[] keyValues = new RubyNode[keywordCount * 2];

            for (int i = 0; i < keywordCount; i++) {
                final KeywordArgParseNode kwArg = (KeywordArgParseNode) args[keywordIndex + i];
                final RubyNode value = kwArg.accept(this);
                final String name = ((INameNode) kwArg.getAssignable()).getName();
                RubyNode key = new ObjectLiteralNode(language.getSymbol(name));
                keyValues[2 * i] = key;
                keyValues[2 * i + 1] = value;
            }
            kwArgsNode = HashLiteralNode.create(keyValues);
        }

        if (node.hasKeyRest()) {
            final RubyNode keyRest = node.getKeyRest().accept(this);
            if (kwArgsNode == null) {
                kwArgsNode = keyRest;
            } else {
                kwArgsNode = new ConcatHashLiteralNode(new RubyNode[]{ kwArgsNode, keyRest });
            }

        }

        if (kwArgsNode != null) {
            sequence.add(kwArgsNode);
        }

        return new SequenceNode(sequence.toArray(RubyNode.EMPTY_ARRAY));
    }

    @Override
    public RubyNode visitArgumentNode(ArgumentParseNode node) {
        final SourceIndexLength sourceSection = node.getPosition();
        return methodBodyTranslator.getEnvironment().findLocalVarNode(node.getName(), sourceSection);
    }

    @Override
    public RubyNode visitOptArgNode(OptArgParseNode node) {
        final SourceIndexLength sourceSection = node.getPosition();
        return methodBodyTranslator.getEnvironment().findLocalVarNode(node.getName(), sourceSection);
    }

    @Override
    public RubyNode visitMultipleAsgnNode(MultipleAsgnParseNode node) {
        return profileArgument(language,
                new ReadPreArgumentNode(index, hasKeywordArguments, MissingArgumentBehavior.NIL));
    }

    @Override
    public RubyNode visitRestArgNode(RestArgParseNode node) {
        final SourceIndexLength sourceSection = node.getPosition();
        return methodBodyTranslator.getEnvironment().findLocalVarNode(node.getName(), sourceSection);
    }

    @Override
    public RubyNode visitKeywordArgNode(KeywordArgParseNode node) {
        final SourceIndexLength sourceSection = node.getPosition();
        final AssignableParseNode asgnNode = node.getAssignable();
        final String name = ((INameNode) asgnNode).getName();
        return methodBodyTranslator.getEnvironment().findLocalVarNode(name, sourceSection);
    }

    @Override
    public RubyNode visitKeywordRestArgNode(KeywordRestArgParseNode node) {
        final SourceIndexLength sourceSection = node.getPosition();
        return methodBodyTranslator.getEnvironment().findLocalVarNode(node.getName(), sourceSection);
    }

    @Override
    public RubyNode visitNoKeywordsArgNode(NoKeywordsArgParseNode node) {
        return defaultVisit(node);
    }

    @Override
    protected RubyNode defaultVisit(ParseNode node) {
        final SourceIndexLength sourceSection = node.getPosition();
        return nilNode(sourceSection);
    }

}
