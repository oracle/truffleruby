/*
 * Copyright (c) 2015, 2024 Oracle and/or its affiliates. All rights reserved. This
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

import com.oracle.truffle.api.CompilerDirectives;
import org.prism.AbstractNodeVisitor;
import org.prism.Nodes;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.hash.ConcatHashLiteralNode;
import org.truffleruby.core.hash.HashLiteralNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.MissingArgumentBehavior;
import org.truffleruby.language.arguments.ReadPreArgumentNode;
import org.truffleruby.language.literal.ObjectLiteralNode;


/** Produces code to reload arguments from local variables back into the arguments array. Only works for simple cases.
 * Used for zsuper calls which pass the same arguments, but will pick up modifications made to them in the method so
 * far. */
public final class YARPReloadArgumentsTranslator extends AbstractNodeVisitor<RubyNode> {

    private final RubyLanguage language;
    private final YARPTranslator yarpTranslator;
    private final boolean hasKeywordArguments;

    private int index = 0;
    private int restParameterIndex = -1;

    public YARPReloadArgumentsTranslator(
            RubyLanguage language,
            YARPTranslator yarpTranslator,
            Nodes.ParametersNode parametersNode) {
        this.language = language;
        this.yarpTranslator = yarpTranslator;
        this.hasKeywordArguments = parametersNode.keywords.length > 0 || parametersNode.keyword_rest != null;
    }

    public int getRestParameterIndex() {
        return restParameterIndex;
    }

    public RubyNode[] reload(Nodes.ParametersNode parametersNode) {
        final List<RubyNode> sequence = new ArrayList<>();

        for (var node : parametersNode.requireds) {
            sequence.add(node.accept(this)); // Nodes.RequiredParameterNode is expected here
            index++;
        }

        for (var node : parametersNode.optionals) {
            sequence.add(node.accept(this)); // Nodes.OptionalParameterNode is expected here
            index++;
        }

        if (parametersNode.rest != null) {
            restParameterIndex = index;
            sequence.add(parametersNode.rest.accept(this)); // Nodes.RestParameterNode is expected here
        }

        // ... parameter (so-called "forward arguments") means there is implicit * parameter
        if (parametersNode.keyword_rest instanceof Nodes.ForwardingParameterNode) {
            restParameterIndex = parametersNode.requireds.length + parametersNode.optionals.length;
            final var readRestNode = yarpTranslator.getEnvironment().findLocalVarNode(
                    TranslatorEnvironment.FORWARDED_REST_NAME,
                    null);
            sequence.add(readRestNode);
        }

        int postCount = parametersNode.posts.length;
        if (postCount > 0) {
            index = -postCount;
            for (var node : parametersNode.posts) {
                sequence.add(node.accept(this)); // Nodes.RequiredParameterNode is expected here
                index++;
            }
        }

        RubyNode kwArgsNode = null;

        if (parametersNode.keywords.length > 0) {
            final int keywordsCount = parametersNode.keywords.length;
            RubyNode[] keysAndValues = new RubyNode[keywordsCount * 2];

            for (int i = 0; i < keywordsCount; i++) {
                // Nodes.RequiredKeywordParameterNode/Nodes.OptionalKeywordParameterNode are expected here
                final Nodes.Node keyword = parametersNode.keywords[i];

                final String name;
                if (keyword instanceof Nodes.OptionalKeywordParameterNode optional) {
                    name = optional.name;
                } else if (keyword instanceof Nodes.RequiredKeywordParameterNode required) {
                    name = required.name;
                } else {
                    throw CompilerDirectives.shouldNotReachHere();
                }

                final RubyNode nameNode = new ObjectLiteralNode(language.getSymbol(name));
                final RubyNode readValueNode = keyword.accept(this);

                keysAndValues[2 * i] = nameNode;
                keysAndValues[2 * i + 1] = readValueNode;
            }
            kwArgsNode = HashLiteralNode.create(keysAndValues, language);
        }

        if (parametersNode.keyword_rest != null) {
            if (parametersNode.keyword_rest instanceof Nodes.KeywordRestParameterNode) {
                final RubyNode keyRest = parametersNode.keyword_rest.accept(this);

                if (kwArgsNode == null) {
                    kwArgsNode = keyRest;
                } else {
                    kwArgsNode = new ConcatHashLiteralNode(new RubyNode[]{ kwArgsNode, keyRest });
                }
            } else if (parametersNode.keyword_rest instanceof Nodes.NoKeywordsParameterNode) {
                // do nothing
            } else if (parametersNode.keyword_rest instanceof Nodes.ForwardingParameterNode) {
                // do nothing - it's already handled in the #reload method
                // NOTE: don't handle '&' for now as far as anonymous & isn't supported yet
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }
        }

        if (kwArgsNode != null) {
            sequence.add(kwArgsNode);
        }

        // ... parameter (so-called "forward arguments") means there is implicit ** parameter
        if (parametersNode.keyword_rest instanceof Nodes.ForwardingParameterNode) {
            final var readKeyRestNode = yarpTranslator.getEnvironment()
                    .findLocalVarNode(TranslatorEnvironment.FORWARDED_KEYWORD_REST_NAME, null);
            sequence.add(readKeyRestNode);
        }

        return sequence.toArray(RubyNode.EMPTY_ARRAY);
    }

    @Override
    public RubyNode visitRequiredParameterNode(Nodes.RequiredParameterNode node) {
        return yarpTranslator.getEnvironment().findLocalVarNode(node.name, null);
    }

    @Override
    public RubyNode visitOptionalParameterNode(Nodes.OptionalParameterNode node) {
        return yarpTranslator.getEnvironment().findLocalVarNode(node.name, null);
    }

    @Override
    public RubyNode visitMultiTargetNode(Nodes.MultiTargetNode node) {
        return Translator.profileArgument(language,
                new ReadPreArgumentNode(index, hasKeywordArguments, MissingArgumentBehavior.NIL));
    }

    @Override
    public RubyNode visitRestParameterNode(Nodes.RestParameterNode node) {
        final String name = node.name != null ? node.name : TranslatorEnvironment.DEFAULT_REST_NAME;
        return yarpTranslator.getEnvironment().findLocalVarNode(name, null);
    }

    @Override
    public RubyNode visitRequiredKeywordParameterNode(Nodes.RequiredKeywordParameterNode node) {
        return yarpTranslator.getEnvironment().findLocalVarNode(node.name, null);
    }

    @Override
    public RubyNode visitOptionalKeywordParameterNode(Nodes.OptionalKeywordParameterNode node) {
        return yarpTranslator.getEnvironment().findLocalVarNode(node.name, null);
    }

    @Override
    public RubyNode visitKeywordRestParameterNode(Nodes.KeywordRestParameterNode node) {
        final String name = node.name != null ? node.name : TranslatorEnvironment.DEFAULT_KEYWORD_REST_NAME;
        return yarpTranslator.getEnvironment().findLocalVarNode(name, null);
    }

    @Override
    public RubyNode visitNoKeywordsParameterNode(Nodes.NoKeywordsParameterNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitForwardingParameterNode(Nodes.ForwardingParameterNode node) {
        // it's handled in the #reload method
        throw CompilerDirectives.shouldNotReachHere();
    }

    @Override
    protected RubyNode defaultVisit(Nodes.Node node) {
        return yarpTranslator.defaultVisit(node);
    }

}
