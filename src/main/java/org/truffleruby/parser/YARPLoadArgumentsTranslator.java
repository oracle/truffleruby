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
    private final boolean isProc;
    private final boolean isMethod;
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
                sequence.add(node.accept(this)); // Nodes.KeywordParameterNode is expected here
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

    public RubyNode visitRestParameterNode(Nodes.RestParameterNode node) {
        final RubyNode readNode;

        int from = parametersNode.requireds.length + parametersNode.optionals.length;
        int to = -parametersNode.posts.length;
        readNode = new ReadRestArgumentNode(from, -to, hasKeywordArguments());

        final String name = (node.name != null) ? node.name : YARPDefNodeTranslator.DEFAULT_REST_NAME;
        final int slot = environment.findFrameSlot(name);
        return new WriteLocalVariableNode(slot, readNode);
    }

    public RubyNode visitKeywordParameterNode(Nodes.KeywordParameterNode node) {
        final int slot = environment.declareVar(node.name);
        final RubyNode defaultValue;

        if (node.value != null) {
            defaultValue = yarpTranslator.translateNodeOrNil(node.value);
        } else {
            defaultValue = null;
        }

        final RubyNode readNode = ReadKeywordArgumentNode.create(language.getSymbol(node.name), defaultValue);
        return new WriteLocalVariableNode(slot, readNode);
    }

    public RubyNode visitKeywordRestParameterNode(Nodes.KeywordRestParameterNode node) {
        final RubyNode readNode = new ReadKeywordRestArgumentNode(language, arity);
        final String name = (node.name != null) ? node.name : YARPDefNodeTranslator.DEFAULT_KEYWORD_REST_NAME;
        final int slot = environment.declareVar(name);

        return new WriteLocalVariableNode(slot, readNode);
    }

    public RubyNode visitNoKeywordsParameterNode(Nodes.NoKeywordsParameterNode node) {
        return new CheckNoKeywordArgumentsNode();
    }

    public RubyNode visitBlockParameterNode(Nodes.BlockParameterNode node) {
        // we don't support yet Ruby 3.1's anonymous block parameter
        assert node.name != null;

        final int slot = environment.findFrameSlot(node.name);
        return new SaveMethodBlockNode(slot);
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

    public TranslatorEnvironment getEnvironment() {
        return environment;
    }

}
