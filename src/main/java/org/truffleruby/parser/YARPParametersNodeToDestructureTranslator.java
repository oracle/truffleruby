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

import java.util.ArrayList;
import java.util.List;

import org.prism.AbstractNodeVisitor;
import org.prism.Nodes;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.array.ArrayIndexNodes;
import org.truffleruby.core.array.ArraySliceNodeGen;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.ArrayIsAtLeastAsLargeAsNode;
import org.truffleruby.language.arguments.CheckNoKeywordArgumentsNode;
import org.truffleruby.language.arguments.ReadKeywordArgumentNode;
import org.truffleruby.language.arguments.ReadKeywordRestArgumentNode;
import org.truffleruby.language.arguments.SaveMethodBlockNode;
import org.truffleruby.language.control.IfElseNodeGen;
import org.truffleruby.language.locals.WriteLocalVariableNode;
import org.truffleruby.language.methods.Arity;


// Translates Nodes.ParametersNode in a proc node
// Destructures a Proc's single Array argument
//
// Based on org.truffleruby.parser.YARPLoadArgumentsTranslator (logic when useArray() -> true)
public final class YARPParametersNodeToDestructureTranslator extends AbstractNodeVisitor<RubyNode> {

    private enum State {
        PRE,
        OPT,
        POST
    }

    private final Nodes.ParametersNode parameters;
    private final RubyNode readArrayNode;
    private final TranslatorEnvironment environment;
    private final RubyLanguage language;
    private final Arity arity;
    private final YARPTranslator yarpTranslator;

    private int index; // position of actual argument in a frame that is being evaluated/read
                      // to match a read node and actual argument
    private State state; // to distinguish pre and post Nodes.RequiredParameterNode parameters

    public YARPParametersNodeToDestructureTranslator(
            Nodes.ParametersNode parameters,
            RubyNode readArrayNode,
            TranslatorEnvironment environment,
            RubyLanguage language,
            Arity arity,
            YARPTranslator yarpTranslator) {
        this.parameters = parameters;
        this.readArrayNode = readArrayNode;
        this.environment = environment;
        this.language = language;
        this.arity = arity;
        this.yarpTranslator = yarpTranslator;
    }

    public RubyNode translate() {
        assert parameters != null;
        assert parameters.requireds.length + parameters.optionals.length + parameters.posts.length > 0;

        final List<RubyNode> sequence = new ArrayList<>();

        sequence.add(Translator.loadSelf(language));

        if (parameters.requireds.length > 0) {
            state = State.PRE;
            index = 0;
            for (var node : parameters.requireds) {
                sequence.add(node.accept(this)); // Nodes.RequiredParameterNode is expected here
                index++;
            }
        }

        if (parameters.optionals.length > 0) {
            index = parameters.requireds.length;
            for (var node : parameters.optionals) {
                sequence.add(node.accept(this)); // Nodes.OptionalParameterNode is expected here
                index++;
            }
        }

        if (parameters.rest != null) {
            sequence.add(parameters.rest.accept(this)); // Nodes.RestParameterNode is expected here
        }

        if (parameters.posts.length > 0) {
            state = State.POST;
            index = -1;

            for (int i = parameters.posts.length - 1; i >= 0; i--) {
                sequence.add(parameters.posts[i].accept(this)); // Nodes.RequiredParameterNode is expected here
                index--;
            }
        }

        if (parameters.keywords.length != 0) {
            for (var node : parameters.keywords) {
                sequence.add(node.accept(this)); // Nodes.KeywordParameterNode is expected here
            }
        }

        if (parameters.keyword_rest != null) {
            // Nodes.KeywordRestParameterNode/Nodes.NoKeywordsParameterNode are expected here
            sequence.add(parameters.keyword_rest.accept(this));
        }

        if (parameters.block != null) {
            sequence.add(parameters.block.accept(this));
        }

        return YARPTranslator.sequence(sequence);
    }

    @Override
    public RubyNode visitMultiTargetNode(Nodes.MultiTargetNode node) {
        final RubyNode readNode = ArrayIndexNodes.ReadConstantIndexNode.create(readArrayNode, index);
        final var translator = new YARPMultiTargetNodeTranslator(node, language, yarpTranslator, readNode);
        final RubyNode rubyNode = translator.translate();
        return rubyNode;
    }

    @Override
    public RubyNode visitRequiredKeywordParameterNode(Nodes.RequiredKeywordParameterNode node) {
        // Passing single array argument to a block with required keyword arguments should lead
        // to ArgumentError 'missing keyword' in runtime.
        // So use ReadKeywordArgumentNode to raise this exception.
        final var name = language.getSymbol(node.name);
        return ReadKeywordArgumentNode.create(name, null);
    }

    @Override
    public RubyNode visitRequiredParameterNode(Nodes.RequiredParameterNode node) {
        final RubyNode readNode = ArrayIndexNodes.ReadConstantIndexNode.create(readArrayNode, index);
        final int slot = environment.findFrameSlot(node.name);
        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitOptionalKeywordParameterNode(Nodes.OptionalKeywordParameterNode node) {
        final int slot = environment.declareVar(node.name);
        final var defaultValue = node.value.accept(this);
        final var readNode = ReadKeywordArgumentNode.create(language.getSymbol(node.name), defaultValue);
        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitOptionalParameterNode(Nodes.OptionalParameterNode node) {
        final RubyNode readNode;
        final RubyNode defaultValue = node.value.accept(this);
        final int slot = environment.declareVar(node.name);
        int minimum = index + 1 + parameters.posts.length;

        // TODO CS 10-Jan-16 we should really hoist this check, or see if Graal does it for us
        readNode = IfElseNodeGen.create(
                new ArrayIsAtLeastAsLargeAsNode(minimum, readArrayNode),
                ArrayIndexNodes.ReadConstantIndexNode.create(readArrayNode, index),
                defaultValue);

        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitRestParameterNode(Nodes.RestParameterNode node) {
        // NOTE: we actually could do nothing if parameter is anonymous
        final RubyNode readNode;

        int from = parameters.requireds.length + parameters.optionals.length;
        int to = -parameters.posts.length;
        readNode = ArraySliceNodeGen.create(from, to, readArrayNode);

        final String name = (node.name != null) ? node.name : TranslatorEnvironment.DEFAULT_REST_NAME;
        final int slot = environment.findFrameSlot(name);
        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitKeywordRestParameterNode(Nodes.KeywordRestParameterNode node) {
        // NOTE: we actually could do nothing if parameter is anonymous
        final RubyNode readNode = new ReadKeywordRestArgumentNode(language, arity);
        final String name = (node.name != null) ? node.name : TranslatorEnvironment.DEFAULT_KEYWORD_REST_NAME;
        final int slot = environment.declareVar(name);

        // NOTE: actually we can immediately assign `{}` value
        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitNoKeywordsParameterNode(Nodes.NoKeywordsParameterNode node) {
        return new CheckNoKeywordArgumentsNode();
    }

    @Override
    public RubyNode visitBlockParameterNode(Nodes.BlockParameterNode node) {
        assert node.name != null; // we don't support yet Ruby 3.1's anonymous block parameter

        final int slot = environment.findFrameSlot(node.name);
        return new SaveMethodBlockNode(slot);
    }

    @Override
    protected RubyNode defaultVisit(Nodes.Node node) {
        // For normal expressions in the default value for optional arguments, use the normal body translator
        return node.accept(yarpTranslator);
    }

}
