/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.yarp;

import org.truffleruby.RubyLanguage;
import org.truffleruby.annotations.Split;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.RubyTopLevelRootNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.arguments.EmptyArgumentsDescriptor;
import org.truffleruby.language.dispatch.RubyCallNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;
import org.truffleruby.language.literal.IntegerFixnumLiteralNode;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.objects.SelfNode;
import org.truffleruby.parser.Translator;
import org.truffleruby.parser.TranslatorEnvironment;
import org.yarp.AbstractNodeVisitor;
import org.yarp.Nodes;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class YARPTranslator extends AbstractNodeVisitor<RubyNode> {

    private final RubyLanguage language;
    final byte[] source;

    public YARPTranslator(RubyLanguage language, byte[] source) {
        this.language = language;
        this.source = source;
    }

    public RubyRootNode translate(Nodes.Node node) {
        var body = node.accept(this);
        var frameDescriptor = TranslatorEnvironment.newFrameDescriptorBuilder(null, true).build();
        var sourceSection = CoreLibrary.JAVA_CORE_SOURCE_SECTION;
        var sharedMethodInfo = new SharedMethodInfo(sourceSection, null, Arity.NO_ARGUMENTS, "<main>", 0, "<main>",
                null, null);
        return new RubyTopLevelRootNode(language, sourceSection, frameDescriptor, sharedMethodInfo, body,
                Split.HEURISTIC, null, Arity.NO_ARGUMENTS);
    }

    @Override
    public RubyNode visitProgramNode(Nodes.ProgramNode node) {
        return node.statements.accept(this);
    }

    @Override
    public RubyNode visitStatementsNode(Nodes.StatementsNode node) {
        var location = new SourceIndexLength(node.startOffset, node.endOffset - node.startOffset);

        var body = node.body;
        var translated = new RubyNode[body.length];
        for (int i = 0; i < body.length; i++) {
            translated[i] = body[i].accept(this);
        }
        return Translator.sequence(location, Arrays.asList(translated));
    }

    @Override
    public RubyNode visitCallNode(Nodes.CallNode node) {
        var methodName = new String(node.name, StandardCharsets.UTF_8);
        var receiver = node.receiver == null ? new SelfNode() : node.receiver.accept(this);
        var argumentsNode = (Nodes.ArgumentsNode) node.arguments;
        var arguments = argumentsNode.arguments;
        var translatedArguments = new RubyNode[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            translatedArguments[i] = arguments[i].accept(this);
        }

        boolean ignoreVisibility = node.receiver == null;
        return new RubyCallNode(new RubyCallNodeParameters(receiver, methodName, null,
                EmptyArgumentsDescriptor.INSTANCE, translatedArguments, ignoreVisibility));
    }

    @Override
    public RubyNode visitIntegerNode(Nodes.IntegerNode node) {
        String string = new String(source, node.startOffset, node.endOffset - node.startOffset,
                StandardCharsets.US_ASCII);
        int value = Integer.parseInt(string);
        return new IntegerFixnumLiteralNode(value);
    }

    @Override
    protected RubyNode defaultVisit(Nodes.Node node) {
        throw new Error("Unknown node: " + node);
    }
}
