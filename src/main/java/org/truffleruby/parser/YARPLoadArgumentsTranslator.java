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

import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.CheckNoKeywordArgumentsNode;
import org.truffleruby.language.arguments.MissingArgumentBehavior;
import org.truffleruby.language.arguments.ReadKeywordArgumentNode;
import org.truffleruby.language.arguments.ReadKeywordRestArgumentNode;
import org.truffleruby.language.arguments.ReadOptionalArgumentNode;
import org.truffleruby.language.arguments.ReadPostArgumentNode;
import org.truffleruby.language.arguments.ReadPreArgumentNode;
import org.truffleruby.language.arguments.ReadRestArgumentNode;
import org.truffleruby.language.arguments.SaveMethodBlockNode;
import org.truffleruby.language.locals.WriteLocalVariableNode;

import org.truffleruby.language.methods.Arity;
import org.prism.AbstractNodeVisitor;
import org.prism.Nodes;

public final class YARPLoadArgumentsTranslator extends AbstractNodeVisitor<RubyNode> {

    private final Arity arity;
    private final boolean isProc; // block or lambda/method
    private final boolean isMethod; // method or proc
    private final YARPTranslator yarpTranslator;

    private enum State {
        PRE,
        OPT,
        POST
    }

    private final Nodes.ParametersNode parametersNode;
    private int index; // position of actual argument in a frame that is being evaluated/read
                      // to match a read node and actual argument
    private State state; // to distinguish pre and post Nodes.RequiredParameterNode parameters

    private final RubyLanguage language;
    private final TranslatorEnvironment environment;

    public YARPLoadArgumentsTranslator(
            Nodes.ParametersNode parametersNode,
            RubyLanguage language,
            TranslatorEnvironment environment,
            Arity arity,
            boolean isProc,
            boolean isMethod,
            YARPTranslator yarpTranslator) {
        this.language = language;
        this.environment = environment;
        this.arity = arity;
        this.isProc = isProc;
        this.isMethod = isMethod;
        this.yarpTranslator = yarpTranslator;
        this.parametersNode = parametersNode;
    }

    public RubyNode translate() {
        if (parametersNode != null) {
            return translateWithParameters();
        } else {
            return translateWithoutParameters();
        }
    }

    private RubyNode translateWithParameters() {
        final List<RubyNode> sequence = new ArrayList<>();

        sequence.add(Translator.loadSelf(language));

        if (parametersNode.requireds.length > 0) {
            state = State.PRE;
            index = 0;
            for (var node : parametersNode.requireds) {
                sequence.add(node.accept(this)); // Nodes.RequiredParameterNode is expected here
                index++;
            }
        }

        // Do this before handling optional arguments as one might get
        // its default value via a `yield`.
        if (isMethod) {
            sequence.add(saveMethodBlockArg());
        }

        if (parametersNode.optionals.length > 0) {
            index = parametersNode.requireds.length;
            for (var node : parametersNode.optionals) {
                sequence.add(node.accept(this)); // Nodes.OptionalParameterNode is expected here
                index++;
            }
        }

        if (parametersNode.rest != null) {
            sequence.add(parametersNode.rest.accept(this)); // Nodes.RestParameterNode is expected here
        }

        if (parametersNode.posts.length > 0) {
            state = State.POST;
            index = -1;

            for (int i = parametersNode.posts.length - 1; i >= 0; i--) {
                sequence.add(parametersNode.posts[i].accept(this)); // Nodes.RequiredParameterNode is expected here
                index--;
            }
        }

        if (hasKeywordArguments()) {
            for (var node : parametersNode.keywords) {
                sequence.add(node.accept(this)); // Nodes.RequiredKeywordParameterNode/Nodes.OptionalKeywordParameterNode are expected here
            }
        }

        if (parametersNode.keyword_rest != null) {
            // Nodes.KeywordRestParameterNode/Nodes.NoKeywordsParameterNode are expected here
            sequence.add(parametersNode.keyword_rest.accept(this));
        }

        if (parametersNode.block != null) {
            sequence.add(parametersNode.block.accept(this));
        }

        return YARPTranslator.sequence(sequence);
    }

    private RubyNode translateWithoutParameters() {
        final List<RubyNode> sequence = new ArrayList<>();

        sequence.add(Translator.loadSelf(language));
        if (isMethod) {
            sequence.add(saveMethodBlockArg());
        }

        return YARPTranslator.sequence(sequence);
    }

    public RubyNode saveMethodBlockArg() {
        final int slot = environment.declareVar(TranslatorEnvironment.METHOD_BLOCK_NAME);
        return new SaveMethodBlockNode(slot);
    }

    @Override
    public RubyNode visitMultiTargetNode(Nodes.MultiTargetNode node) {
        final RubyNode readNode;

        if (state == YARPLoadArgumentsTranslator.State.PRE) {
            readNode = Translator.profileArgument(
                    language,
                    new ReadPreArgumentNode(
                            index,
                            hasKeywordArguments(),
                            isProc ? MissingArgumentBehavior.NIL : MissingArgumentBehavior.RUNTIME_ERROR));

        } else if (state == YARPLoadArgumentsTranslator.State.POST) {
            readNode = new ReadPostArgumentNode(-index, hasKeywordArguments(), getRequiredCount());
        } else {
            throw new IllegalStateException();
        }

        final var translator = new YARPMultiTargetNodeTranslator(node, language, yarpTranslator, readNode);
        final RubyNode rubyNode = translator.translate();
        return rubyNode;
    }

    @Override
    public RubyNode visitRequiredKeywordParameterNode(Nodes.RequiredKeywordParameterNode node) {
        final int slot = environment.declareVar(node.name);
        final var name = language.getSymbol(node.name);
        final var readNode = ReadKeywordArgumentNode.create(name, null);

        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitRequiredParameterNode(Nodes.RequiredParameterNode node) {
        final RubyNode readNode;

        if (state == YARPLoadArgumentsTranslator.State.PRE) {
            readNode = Translator.profileArgument(
                    language,
                    new ReadPreArgumentNode(
                            index,
                            hasKeywordArguments(),
                            isProc ? MissingArgumentBehavior.NIL : MissingArgumentBehavior.RUNTIME_ERROR));

        } else if (state == YARPLoadArgumentsTranslator.State.POST) {
            readNode = new ReadPostArgumentNode(-index, hasKeywordArguments(), getRequiredCount());
        } else {
            throw new IllegalStateException();
        }

        final int slot = environment.findFrameSlot(node.name);
        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitOptionalKeywordParameterNode(Nodes.OptionalKeywordParameterNode node) {
        final int slot = environment.declareVar(node.name);
        final var name = language.getSymbol(node.name);
        final var value = node.value.accept(this);
        final var readNode = ReadKeywordArgumentNode.create(name, value);

        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitOptionalParameterNode(Nodes.OptionalParameterNode node) {
        final RubyNode readNode;
        final RubyNode defaultValue = node.value.accept(this);
        final int slot = environment.declareVar(node.name);
        int minimum = index + 1 + parametersNode.posts.length;

        readNode = new ReadOptionalArgumentNode(
                index,
                minimum,
                hasKeywordArguments(),
                defaultValue);

        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitRestParameterNode(Nodes.RestParameterNode node) {
        final RubyNode readNode;

        int from = parametersNode.requireds.length + parametersNode.optionals.length;
        int to = -parametersNode.posts.length;
        readNode = new ReadRestArgumentNode(from, -to, hasKeywordArguments());

        final String name = (node.name != null) ? node.name : TranslatorEnvironment.DEFAULT_REST_NAME;

        // When a rest parameter in a block is nameless then YARP doesn't add '*' to block's locals
        // (what is expected as far as arguments forwarding doesn't work in blocks), and we don't
        // declare this hidden variable beforehand. So declare it here right before usage.
        final int slot = environment.declareVar(name);

        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitKeywordRestParameterNode(Nodes.KeywordRestParameterNode node) {
        final RubyNode readNode = new ReadKeywordRestArgumentNode(language, arity);
        final String name = (node.name != null) ? node.name : TranslatorEnvironment.DEFAULT_KEYWORD_REST_NAME;

        // When a keyword rest parameter in a block is nameless then YARP doesn't add '**' to block's locals
        // (what is expected as far as arguments forwarding doesn't work in blocks), and we don't declare this
        // hidden variable beforehand. So declare it here right before usage.
        final int slot = environment.declareVar(name);

        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitNoKeywordsParameterNode(Nodes.NoKeywordsParameterNode node) {
        return new CheckNoKeywordArgumentsNode();
    }

    @Override
    public RubyNode visitBlockParameterNode(Nodes.BlockParameterNode node) {
        // we don't support yet Ruby 3.1's anonymous block parameter
        assert node.name != null;

        final int slot = environment.findFrameSlot(node.name);
        return new SaveMethodBlockNode(slot);
    }

    @Override
    public RubyNode visitForwardingParameterNode(Nodes.ForwardingParameterNode node) {
        ArrayList<RubyNode> sequence = new ArrayList<>();

        // desugar ... to *, **, and & parameters
        final var rest = new Nodes.RestParameterNode(TranslatorEnvironment.FORWARDED_REST_NAME, 0, 0);
        final var keyrest = new Nodes.KeywordRestParameterNode(TranslatorEnvironment.FORWARDED_KEYWORD_REST_NAME, 0, 0);
        final var block = new Nodes.BlockParameterNode(TranslatorEnvironment.FORWARDED_BLOCK_NAME, 0, 0);

        sequence.add(rest.accept(this));
        sequence.add(keyrest.accept(this));
        sequence.add(block.accept(this));

        return YARPTranslator.sequence(sequence);
    }

    @Override
    protected RubyNode defaultVisit(Nodes.Node node) {
        // For normal expressions in the default value for optional arguments, use the normal body translator
        return node.accept(yarpTranslator);
    }

    private int getRequiredCount() {
        return parametersNode.requireds.length + parametersNode.posts.length;
    }

    private boolean hasKeywordArguments() {
        return parametersNode.keywords.length != 0 || parametersNode.keyword_rest != null;
    }

}
