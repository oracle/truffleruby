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
import java.util.Collections;
import java.util.List;

import com.oracle.truffle.api.CompilerDirectives;
import org.prism.AbstractNodeVisitor;
import org.prism.Nodes;
import org.truffleruby.Layouts;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.hash.ConcatHashLiteralNode;
import org.truffleruby.core.hash.HashLiteralNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.MissingArgumentBehavior;
import org.truffleruby.language.arguments.ReadPreArgumentNode;
import org.truffleruby.language.literal.ObjectLiteralNode;


/** Produces code to reload arguments from local variables back into the arguments array. Only works for simple cases.
 * Used for zsuper calls which pass the same arguments, but will pick up modifications made to them in the method so
 * far.
 *
 * Parameters should be iterated in the same order {@link org.truffleruby.parser.YARPLoadArgumentsTranslator} iterates
 * to handle multiple "_" parameters (and parameters with "_" prefix) correctly. */
public final class YARPReloadArgumentsTranslator extends AbstractNodeVisitor<RubyNode> {

    private final RubyLanguage language;
    private final YARPTranslator yarpTranslator;
    private final boolean hasKeywordArguments;

    private int index = 0;
    private int restParameterIndex = -1;
    private int repeatedParameterCounter = 2;

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

    public RubyNode[] reload(Nodes.ParametersNode parameters) {
        final List<RubyNode> sequence = new ArrayList<>();

        for (var node : parameters.requireds) {
            sequence.add(node.accept(this)); // Nodes.RequiredParameterNode, Nodes.MultiTargetNode are expected here
            index++;
        }

        for (var node : parameters.optionals) {
            sequence.add(node.accept(this)); // Nodes.OptionalParameterNode is expected here
            index++;
        }

        if (parameters.rest != null) {
            restParameterIndex = index;
            sequence.add(parameters.rest.accept(this)); // Nodes.RestParameterNode is expected here
        }

        index = -1;
        // post parameters were translated in reverse order
        // iterate them in the same order to handle multiple "_" properly
        ArrayList<RubyNode> postsSequence = new ArrayList<>();
        for (int i = parameters.posts.length - 1; i >= 0; i--) {
            postsSequence.add(parameters.posts[i].accept(this)); // Nodes.RequiredParameterNode, Nodes.MultiTargetNode are expected here
            index--;
        }
        // but we need to pass parameters to super call in direct order
        Collections.reverse(postsSequence);
        sequence.addAll(postsSequence);

        RubyNode kwArgsNode = null;

        if (parameters.keywords.length > 0) {
            final int keywordsCount = parameters.keywords.length;
            RubyNode[] keysAndValues = new RubyNode[keywordsCount * 2];

            for (int i = 0; i < keywordsCount; i++) {
                // Nodes.RequiredKeywordParameterNode/Nodes.OptionalKeywordParameterNode are expected here
                final Nodes.Node keyword = parameters.keywords[i];

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

        if (parameters.keyword_rest != null) {
            if (parameters.keyword_rest instanceof Nodes.KeywordRestParameterNode) {
                final RubyNode keyRest = parameters.keyword_rest.accept(this);

                if (kwArgsNode == null) {
                    kwArgsNode = keyRest;
                } else {
                    kwArgsNode = new ConcatHashLiteralNode(new RubyNode[]{ kwArgsNode, keyRest });
                }
            } else if (parameters.keyword_rest instanceof Nodes.NoKeywordsParameterNode) {
                // do nothing
            } else if (parameters.keyword_rest instanceof Nodes.ForwardingParameterNode) {
                // do nothing - it's already handled in the #reload method
                // NOTE: don't handle '&' for now as far as anonymous & isn't supported yet
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }
        }

        if (kwArgsNode != null) {
            sequence.add(kwArgsNode);
        }

        if (parameters.keyword_rest instanceof Nodes.ForwardingParameterNode) {
            // ... parameter (so-called "forward arguments") means there is implicit * parameter
            restParameterIndex = parameters.requireds.length + parameters.optionals.length;
            final var readRestNode = yarpTranslator.getEnvironment().findLocalVarNode(
                    TranslatorEnvironment.FORWARDED_REST_NAME,
                    null);
            sequence.add(readRestNode);

            // ... parameter (so-called "forward arguments") means there is implicit ** parameter
            final var readKeyRestNode = yarpTranslator.getEnvironment()
                    .findLocalVarNode(TranslatorEnvironment.FORWARDED_KEYWORD_REST_NAME, null);
            sequence.add(readKeyRestNode);
        }

        return sequence.toArray(RubyNode.EMPTY_ARRAY);
    }

    @Override
    public RubyNode visitRequiredParameterNode(Nodes.RequiredParameterNode node) {
        final String name;

        if (node.isRepeatedParameter()) {
            name = createNameForRepeatedParameter(node.name);
        } else {
            name = node.name;
        }

        return yarpTranslator.getEnvironment().findLocalVarNode(name, null);
    }

    @Override
    public RubyNode visitOptionalParameterNode(Nodes.OptionalParameterNode node) {
        final String name;

        if (node.isRepeatedParameter()) {
            name = createNameForRepeatedParameter(node.name);
        } else {
            name = node.name;
        }

        return yarpTranslator.getEnvironment().findLocalVarNode(name, null);
    }

    @Override
    public RubyNode visitMultiTargetNode(Nodes.MultiTargetNode node) {
        return Translator.profileArgument(language,
                new ReadPreArgumentNode(index, hasKeywordArguments, MissingArgumentBehavior.NIL));
    }

    @Override
    public RubyNode visitRestParameterNode(Nodes.RestParameterNode node) {
        final String name;

        if (node.name != null) {
            if (node.isRepeatedParameter()) {
                name = createNameForRepeatedParameter(node.name);
            } else {
                name = node.name;
            }
        } else {
            name = TranslatorEnvironment.DEFAULT_REST_NAME;
        }

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

    private String createNameForRepeatedParameter(String name) {
        int count = repeatedParameterCounter++;
        return Layouts.TEMP_PREFIX + name + count;
    }

}
