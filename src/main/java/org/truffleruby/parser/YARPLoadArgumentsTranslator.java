/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates. All rights reserved. This
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
import java.util.Objects;

import com.oracle.truffle.api.CompilerDirectives;
import org.truffleruby.Layouts;
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
import org.prism.Nodes;

/** Translates method/block parameters and assign local variables.
 *
 * Parameters should be iterated in the same order {@link org.truffleruby.parser.YARPReloadArgumentsTranslator} iterates
 * to handle multiple "_" parameters (and parameters with "_" prefix) correctly. */
public final class YARPLoadArgumentsTranslator extends YARPBaseTranslator {

    private final Arity arity;
    private final boolean isProc; // block or lambda/method
    private final boolean isMethod; // method or proc
    private final YARPTranslator yarpTranslator;

    private enum State {
        PRE,
        OPT,
        POST
    }

    private final Nodes.ParametersNode parameters;
    /** position of actual argument in a frame that is being evaluated/read to match a read node and actual argument */
    private int index = 0;
    /** to distinguish pre and post Nodes.RequiredParameterNode parameters */
    private State state;
    private int repeatedParameterCounter = 2;

    public YARPLoadArgumentsTranslator(
            TranslatorEnvironment environment,
            Nodes.ParametersNode parameters,
            Arity arity,
            boolean isProc,
            boolean isMethod,
            YARPTranslator yarpTranslator) {
        super(environment);
        this.arity = arity;
        this.isProc = isProc;
        this.isMethod = isMethod;
        this.yarpTranslator = yarpTranslator;
        this.parameters = Objects.requireNonNull(parameters);
    }

    public RubyNode translate() {
        final List<RubyNode> sequence = new ArrayList<>();

        sequence.add(YARPTranslator.loadSelf(language));

        if (parameters.requireds.length > 0) {
            state = State.PRE;
            for (var node : parameters.requireds) {
                sequence.add(node.accept(this)); // Nodes.RequiredParameterNode, Nodes.MultiTargetNode are expected here
                index++;
            }
        }

        // Do this before handling optional arguments as one might get
        // its default value via a `yield`.
        if (isMethod) {
            sequence.add(saveMethodBlockArg());
        }

        // Early return for the common case of zero parameters
        if (parameters == ZERO_PARAMETERS_NODE) {
            return sequence(sequence);
        }

        if (parameters.optionals.length > 0) {
            for (var node : parameters.optionals) {
                sequence.add(node.accept(this)); // Nodes.OptionalParameterNode is expected here
                index++;
            }
        }

        if (parameters.rest != null) {
            if (parameters.rest instanceof Nodes.ImplicitRestNode) {
                // do nothing

                // The only reason to save anonymous rest parameter in a local variable is to be able to forward it.
                // Implicit rest is allowed only in blocks but anonymous rest forwarding works only in methods/lambdas.
            } else {
                sequence.add(parameters.rest.accept(this)); // Nodes.RestParameterNode is expected here
            }
        }

        if (parameters.posts.length > 0) {
            state = State.POST;
            index = -1;

            for (int i = parameters.posts.length - 1; i >= 0; i--) {
                sequence.add(parameters.posts[i].accept(this)); // Nodes.RequiredParameterNode, Nodes.MultiTargetNode are expected here
                index--;
            }
        }

        for (var node : parameters.keywords) {
            sequence.add(node.accept(this)); // Nodes.RequiredKeywordParameterNode/Nodes.OptionalKeywordParameterNode are expected here
        }

        if (parameters.keyword_rest != null) {
            // Nodes.KeywordRestParameterNode/Nodes.NoKeywordsParameterNode/Nodes.ForwardingParameterNode are expected here
            sequence.add(parameters.keyword_rest.accept(this));
        }

        if (parameters.block != null) {
            sequence.add(parameters.block.accept(this));
        }

        return sequence(sequence);
    }

    public RubyNode saveMethodBlockArg() {
        final int slot = environment.declareVar(TranslatorEnvironment.METHOD_BLOCK_NAME);
        return new SaveMethodBlockNode(slot);
    }

    @Override
    public RubyNode visitMultiTargetNode(Nodes.MultiTargetNode node) {
        final RubyNode readNode;

        if (state == YARPLoadArgumentsTranslator.State.PRE) {
            readNode = YARPTranslator.profileArgument(
                    language,
                    new ReadPreArgumentNode(
                            index,
                            hasKeywordArguments(),
                            isProc ? MissingArgumentBehavior.NIL : MissingArgumentBehavior.RUNTIME_ERROR));

        } else if (state == YARPLoadArgumentsTranslator.State.POST) {
            readNode = new ReadPostArgumentNode(-index, getRequiredCount(), getOptionalCount(), hasRest(),
                    hasKeywordArguments());
        } else {
            throw CompilerDirectives.shouldNotReachHere();
        }

        final var translator = new YARPMultiTargetNodeTranslator(node, language, yarpTranslator, readNode);
        final RubyNode rubyNode = translator.translate();
        return rubyNode;
    }

    @Override
    public RubyNode visitRequiredKeywordParameterNode(Nodes.RequiredKeywordParameterNode node) {
        final int slot = environment.findFrameSlot(node.name);
        final var name = language.getSymbol(node.name);
        final var readNode = ReadKeywordArgumentNode.create(name, null);

        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitRequiredParameterNode(Nodes.RequiredParameterNode node) {
        final RubyNode readNode;

        if (state == YARPLoadArgumentsTranslator.State.PRE) {
            readNode = YARPTranslator.profileArgument(
                    language,
                    new ReadPreArgumentNode(
                            index,
                            hasKeywordArguments(),
                            isProc ? MissingArgumentBehavior.NIL : MissingArgumentBehavior.RUNTIME_ERROR));

        } else if (state == YARPLoadArgumentsTranslator.State.POST) {
            readNode = new ReadPostArgumentNode(-index, getRequiredCount(), getOptionalCount(), hasRest(),
                    hasKeywordArguments());
        } else {
            throw CompilerDirectives.shouldNotReachHere();
        }

        final int slot;
        if (node.isRepeatedParameter()) {
            String name = createNameForRepeatedParameter(node.name);
            slot = environment.declareVar(name);
        } else {
            slot = environment.findFrameSlot(node.name);
        }

        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitOptionalKeywordParameterNode(Nodes.OptionalKeywordParameterNode node) {
        final int slot = environment.findFrameSlot(node.name);
        final var name = language.getSymbol(node.name);
        final var value = node.value.accept(this);
        final var readNode = ReadKeywordArgumentNode.create(name, value);

        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitOptionalParameterNode(Nodes.OptionalParameterNode node) {
        final RubyNode readNode;
        final RubyNode defaultValue = node.value.accept(this);
        int minimum = index + 1 + parameters.posts.length;

        readNode = new ReadOptionalArgumentNode(
                index,
                minimum,
                hasKeywordArguments(),
                defaultValue);

        final int slot;
        if (node.isRepeatedParameter()) {
            String name = createNameForRepeatedParameter(node.name);
            slot = environment.declareVar(name);
        } else {
            slot = environment.findFrameSlot(node.name);
        }

        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitRestParameterNode(Nodes.RestParameterNode node) {
        final RubyNode readNode;

        int from = parameters.requireds.length + parameters.optionals.length;
        int to = -parameters.posts.length;
        readNode = new ReadRestArgumentNode(from, -to, hasKeywordArguments());

        final int slot;

        if (node.name != null) {
            if (node.isRepeatedParameter()) {
                String name = createNameForRepeatedParameter(node.name);
                slot = environment.declareVar(name);
            } else {
                slot = environment.findFrameSlot(node.name);
            }
        } else {
            // When a rest parameter in a block is nameless then YARP doesn't add '*' to block's locals
            // (what is expected as far as arguments forwarding doesn't work in blocks), and we don't
            // declare this hidden variable beforehand. So declare it here right before usage.
            String name = TranslatorEnvironment.DEFAULT_REST_NAME;
            slot = environment.declareVar(name);
        }

        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitImplicitRestNode(Nodes.ImplicitRestNode node) {
        throw CompilerDirectives.shouldNotReachHere("handled in #translateWithParameters");
    }

    @Override
    public RubyNode visitKeywordRestParameterNode(Nodes.KeywordRestParameterNode node) {
        final RubyNode readNode = new ReadKeywordRestArgumentNode(language, arity);
        final int slot;

        if (node.name != null) {
            slot = environment.findFrameSlot(node.name);
        } else {
            // When a keyword rest parameter in a block is nameless then YARP doesn't add '**' to block's locals
            // (what is expected as far as arguments forwarding doesn't work in blocks), and we don't declare this
            // hidden variable beforehand. So declare it here right before usage.
            final String name = TranslatorEnvironment.DEFAULT_KEYWORD_REST_NAME;
            slot = environment.declareVar(name);
        }

        return new WriteLocalVariableNode(slot, readNode);
    }

    @Override
    public RubyNode visitNoKeywordsParameterNode(Nodes.NoKeywordsParameterNode node) {
        return new CheckNoKeywordArgumentsNode();
    }

    @Override
    public RubyNode visitBlockParameterNode(Nodes.BlockParameterNode node) {
        final String name;

        if (node.name == null) {
            // def a(&)
            name = TranslatorEnvironment.FORWARDED_BLOCK_NAME;
        } else {
            name = node.name;
        }

        final int slot = environment.findFrameSlot(name);
        return new SaveMethodBlockNode(slot);
    }

    @Override
    public RubyNode visitForwardingParameterNode(Nodes.ForwardingParameterNode node) {
        // is allowed in method parameters only
        ArrayList<RubyNode> sequence = new ArrayList<>();

        // desugar ... to *, **, and & parameters
        final var rest = new Nodes.RestParameterNode(NO_FLAGS, TranslatorEnvironment.FORWARDED_REST_NAME, 0, 0);
        final var keyrest = new Nodes.KeywordRestParameterNode(NO_FLAGS,
                TranslatorEnvironment.FORWARDED_KEYWORD_REST_NAME, 0, 0);
        final var block = new Nodes.BlockParameterNode(NO_FLAGS, TranslatorEnvironment.FORWARDED_BLOCK_NAME, 0, 0);

        sequence.add(rest.accept(this));
        sequence.add(keyrest.accept(this));
        sequence.add(block.accept(this));

        return sequence(sequence);
    }

    @Override
    protected RubyNode defaultVisit(Nodes.Node node) {
        // For normal expressions in the default value for optional arguments, use the normal body translator
        return node.accept(yarpTranslator);
    }

    private int getRequiredCount() {
        return parameters.requireds.length + parameters.posts.length;
    }

    private int getOptionalCount() {
        return parameters.optionals.length;
    }

    private boolean hasKeywordArguments() {
        return parameters.keywords.length != 0 || parameters.keyword_rest != null;
    }

    private boolean hasRest() {
        return parameters.rest != null;
    }

    private String createNameForRepeatedParameter(String name) {
        int count = repeatedParameterCounter++;
        return Layouts.TEMP_PREFIX + name + count;
    }

}
