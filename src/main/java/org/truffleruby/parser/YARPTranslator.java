/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.RubyLanguage;
import org.truffleruby.annotations.Split;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.array.ArrayLiteralNode;
import org.truffleruby.core.cast.StringToSymbolNodeGen;
import org.truffleruby.core.cast.ToSNode;
import org.truffleruby.core.cast.ToSNodeGen;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;
import org.truffleruby.core.module.ModuleNodes;
import org.truffleruby.core.string.InterpolatedStringNode;
import org.truffleruby.core.string.TStringConstants;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.RubyTopLevelRootNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.arguments.EmptyArgumentsDescriptor;
import org.truffleruby.language.constants.ReadConstantNode;
import org.truffleruby.language.control.AndNodeGen;
import org.truffleruby.language.control.IfElseNodeGen;
import org.truffleruby.language.control.IfNodeGen;
import org.truffleruby.language.control.OrNodeGen;
import org.truffleruby.language.control.RetryNode;
import org.truffleruby.language.control.UnlessNodeGen;
import org.truffleruby.language.defined.DefinedNode;
import org.truffleruby.language.dispatch.RubyCallNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;
import org.truffleruby.language.globals.AliasGlobalVarNode;
import org.truffleruby.language.globals.ReadGlobalVariableNodeGen;
import org.truffleruby.language.globals.ReadMatchReferenceNodes;
import org.truffleruby.language.globals.WriteGlobalVariableNodeGen;
import org.truffleruby.language.literal.BooleanLiteralNode;
import org.truffleruby.language.literal.FloatLiteralNode;
import org.truffleruby.language.literal.IntegerFixnumLiteralNode;
import org.truffleruby.language.literal.NilLiteralNode;
import org.truffleruby.language.literal.ObjectClassLiteralNode;
import org.truffleruby.language.literal.ObjectLiteralNode;
import org.truffleruby.language.literal.StringLiteralNode;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.objects.GetDynamicLexicalScopeNode;
import org.truffleruby.language.objects.SelfNode;
import org.truffleruby.language.objects.classvariables.ReadClassVariableNode;
import org.truffleruby.language.objects.classvariables.WriteClassVariableNode;
import org.yarp.AbstractNodeVisitor;
import org.yarp.Nodes;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class YARPTranslator extends AbstractNodeVisitor<RubyNode> {

    private final RubyLanguage language;
    private final YARPTranslator parent;
    private final TranslatorEnvironment environment;
    private final byte[] sourceBytes;
    private final Source source;
    private final ParserContext parserContext;
    private final Node currentNode;
    private final RubyDeferredWarnings rubyWarnings;

    public YARPTranslator(
            RubyLanguage language,
            YARPTranslator parent,
            TranslatorEnvironment environment,
            byte[] sourceBytes,
            Source source,
            ParserContext parserContext,
            Node currentNode,
            RubyDeferredWarnings rubyWarnings) {
        this.language = language;
        this.parent = parent;
        this.environment = environment;
        this.sourceBytes = sourceBytes;
        this.source = source;
        this.parserContext = parserContext;
        this.currentNode = currentNode;
        this.rubyWarnings = rubyWarnings;
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

    public RubyNode visitAliasNode(Nodes.AliasNode node) {
        RubyNode rubyNode;

        if (node.new_name instanceof Nodes.GlobalVariableReadNode &&
                node.old_name instanceof Nodes.GlobalVariableReadNode) {
            rubyNode = new AliasGlobalVarNode(
                    toString(node.old_name),
                    toString(node.new_name));
        } else {
            // expected InterpolatedSymbolNode (that should be evaluated in runtime)
            // or SymbolNode
            rubyNode = new ModuleNodes.AliasKeywordNode(
                    node.new_name.accept(this),
                    node.old_name.accept(this));
        }

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitAlternationPatternNode(Nodes.AlternationPatternNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitAndNode(Nodes.AndNode node) {
        RubyNode left = node.left.accept(this);
        RubyNode right = node.right.accept(this);

        RubyNode rubyNode = AndNodeGen.create(left, right);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitArgumentsNode(Nodes.ArgumentsNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitArrayNode(Nodes.ArrayNode node) {
        RubyNode[] elements = new RubyNode[node.elements.length];
        for (int i = 0; i < node.elements.length; i++) {
            elements[i] = node.elements[i].accept(this);
        }

        RubyNode rubyNode = ArrayLiteralNode.create(language, elements);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitArrayPatternNode(Nodes.ArrayPatternNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitAssocNode(Nodes.AssocNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitAssocSplatNode(Nodes.AssocSplatNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitBackReferenceReadNode(Nodes.BackReferenceReadNode node) {
        final RubyNode rubyNode = ReadGlobalVariableNodeGen.create(toString(node));
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitBeginNode(Nodes.BeginNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitBlockArgumentNode(Nodes.BlockArgumentNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitBlockNode(Nodes.BlockNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitBlockParameterNode(Nodes.BlockParameterNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitBlockParametersNode(Nodes.BlockParametersNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitBreakNode(Nodes.BreakNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitCallNode(Nodes.CallNode node) {
        var methodName = new String(node.name, StandardCharsets.UTF_8);
        var receiver = node.receiver == null ? new SelfNode() : node.receiver.accept(this);
        var arguments = node.arguments.arguments;

        if (arguments == null) {
            arguments = Nodes.Node.EMPTY_ARRAY;
        }

        var translatedArguments = new RubyNode[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            translatedArguments[i] = arguments[i].accept(this);
        }

        boolean ignoreVisibility = node.receiver == null;
        return new RubyCallNode(new RubyCallNodeParameters(receiver, methodName, null,
                EmptyArgumentsDescriptor.INSTANCE, translatedArguments, ignoreVisibility));
    }

    public RubyNode visitCallOperatorAndWriteNode(Nodes.CallOperatorAndWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitCallOperatorOrWriteNode(Nodes.CallOperatorOrWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitCallOperatorWriteNode(Nodes.CallOperatorWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitCapturePatternNode(Nodes.CapturePatternNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitCaseNode(Nodes.CaseNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitClassNode(Nodes.ClassNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitClassVariableOperatorAndWriteNode(Nodes.ClassVariableOperatorAndWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitClassVariableOperatorOrWriteNode(Nodes.ClassVariableOperatorOrWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitClassVariableOperatorWriteNode(Nodes.ClassVariableOperatorWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitClassVariableReadNode(Nodes.ClassVariableReadNode node) {
        final SourceIndexLength sourceSection = new SourceIndexLength(node.startOffset, node.length);
        final RubyNode rubyNode = new ReadClassVariableNode(
                getLexicalScopeNode("class variable lookup", sourceSection),
                toString(node));

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitClassVariableWriteNode(Nodes.ClassVariableWriteNode node) {
        final SourceIndexLength sourceSection = new SourceIndexLength(node.startOffset, node.length);
        final RubyNode rhs = node.value.accept(this);

        final RubyNode rubyNode = new WriteClassVariableNode(
                getLexicalScopeNode("set dynamic class variable", sourceSection),
                toString(node.name_loc),
                rhs);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitConstantOperatorAndWriteNode(Nodes.ConstantOperatorAndWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitConstantOperatorOrWriteNode(Nodes.ConstantOperatorOrWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitConstantOperatorWriteNode(Nodes.ConstantOperatorWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitConstantPathNode(Nodes.ConstantPathNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitConstantPathOperatorAndWriteNode(Nodes.ConstantPathOperatorAndWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitConstantPathOperatorOrWriteNode(Nodes.ConstantPathOperatorOrWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitConstantPathOperatorWriteNode(Nodes.ConstantPathOperatorWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitConstantPathWriteNode(Nodes.ConstantPathWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitConstantReadNode(Nodes.ConstantReadNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitDefNode(Nodes.DefNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitDefinedNode(Nodes.DefinedNode node) {
        // Handle defined?(yield) explicitly otherwise it would raise SyntaxError
        if (node.value instanceof Nodes.YieldNode && isInvalidYield()) {
            final var nilNode = new NilLiteralNode(false);
            assignNodePositionInSource(node, nilNode);

            return nilNode;
        }

        final RubyNode value = node.value.accept(this);
        final RubyNode rubyNode = new DefinedNode(value);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitElseNode(Nodes.ElseNode node) {
        if (node.statements == null) {
            final RubyNode rubyNode = new NilLiteralNode(true);
            assignNodePositionInSource(node, rubyNode);
            return rubyNode;
        }
        return node.statements.accept(this);
    }

    public RubyNode visitEmbeddedStatementsNode(Nodes.EmbeddedStatementsNode node) {
        // empty interpolation expression, e.g. in "a #{} b"
        if (node.statements == null) {
            RubyNode rubyNode = new ObjectLiteralNode(
                    language.getFrozenStringLiteral(TStringConstants.EMPTY_BINARY, Encodings.BINARY));
            assignNodePositionInSource(node, rubyNode);
            return rubyNode;
        }

        return node.statements.accept(this);
    }

    public RubyNode visitEmbeddedVariableNode(Nodes.EmbeddedVariableNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitEnsureNode(Nodes.EnsureNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitFalseNode(Nodes.FalseNode node) {
        RubyNode rubyNode = new BooleanLiteralNode(false);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitFindPatternNode(Nodes.FindPatternNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitFloatNode(Nodes.FloatNode node) {
        String string = toString(node);
        double value;

        try {
            value = SafeDoubleParser.parseDouble(string);
        } catch (NumberFormatException e) {
            value = string.startsWith("-") ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }

        final RubyNode rubyNode = new FloatLiteralNode(value);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitForNode(Nodes.ForNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitForwardingArgumentsNode(Nodes.ForwardingArgumentsNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitForwardingParameterNode(Nodes.ForwardingParameterNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitForwardingSuperNode(Nodes.ForwardingSuperNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitGlobalVariableOperatorAndWriteNode(Nodes.GlobalVariableOperatorAndWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitGlobalVariableOperatorOrWriteNode(Nodes.GlobalVariableOperatorOrWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitGlobalVariableOperatorWriteNode(Nodes.GlobalVariableOperatorWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitGlobalVariableReadNode(Nodes.GlobalVariableReadNode node) {
        final RubyNode rubyNode = ReadGlobalVariableNodeGen.create(toString(node));
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitGlobalVariableWriteNode(Nodes.GlobalVariableWriteNode node) {
        final RubyNode value = node.value.accept(this);
        final String name = toString(node.name_loc);

        final RubyNode rubyNode = WriteGlobalVariableNodeGen.create(name, value);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitHashNode(Nodes.HashNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitHashPatternNode(Nodes.HashPatternNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitIfNode(Nodes.IfNode node) {
        final RubyNode conditionNode = node.predicate.accept(this);
        final RubyNode thenNode = node.statements == null ? null : node.statements.accept(this);
        final RubyNode elseNode = node.consequent == null ? null : node.consequent.accept(this);
        final RubyNode rubyNode;

        if (thenNode != null && elseNode != null) {
            rubyNode = IfElseNodeGen.create(conditionNode, thenNode, elseNode);
            assignNodePositionInSource(node, rubyNode);
        } else if (thenNode != null) {
            rubyNode = IfNodeGen.create(conditionNode, thenNode);
            assignNodePositionInSource(node, rubyNode);
        } else if (elseNode != null) {
            rubyNode = UnlessNodeGen.create(conditionNode, elseNode);
            assignNodePositionInSource(node, rubyNode);
        } else {
            // if (condition)
            // end
            final SourceIndexLength sourceSection = new SourceIndexLength(node.startOffset, node.length);
            rubyNode = Translator.sequence(sourceSection, Arrays.asList(conditionNode, new NilLiteralNode(true)));
        }

        return rubyNode;
    }

    public RubyNode visitImaginaryNode(Nodes.ImaginaryNode node) {
        // Translate as Complex.convert(0, b) ignoring visibility

        final RubyNode objectClassNode = new ObjectClassLiteralNode();
        final ReadConstantNode complexModuleNode = new ReadConstantNode(objectClassNode, "Complex");
        final RubyNode realComponentNode = new IntegerFixnumLiteralNode(0);
        final RubyNode imaginaryComponentNode = node.numeric.accept(this);

        RubyNode[] arguments = new RubyNode[]{ realComponentNode, imaginaryComponentNode };
        RubyNode rubyNode = createCallNode(complexModuleNode, "convert", arguments);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitInNode(Nodes.InNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitInstanceVariableOperatorAndWriteNode(Nodes.InstanceVariableOperatorAndWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitInstanceVariableOperatorOrWriteNode(Nodes.InstanceVariableOperatorOrWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitInstanceVariableOperatorWriteNode(Nodes.InstanceVariableOperatorWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitInstanceVariableReadNode(Nodes.InstanceVariableReadNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitInstanceVariableWriteNode(Nodes.InstanceVariableWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitIntegerNode(Nodes.IntegerNode node) {
        final String string = toString(node);
        final int value = Integer.parseInt(string);

        final RubyNode rubyNode = new IntegerFixnumLiteralNode(value);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitInterpolatedRegularExpressionNode(Nodes.InterpolatedRegularExpressionNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitInterpolatedStringNode(Nodes.InterpolatedStringNode node) {
        final ToSNode[] children = new ToSNode[node.parts.length];

        // a special case for `:"abc"` literal - convert to Symbol ourselves
        if (node.parts.length == 1 && node.parts[0] instanceof Nodes.StringNode s) {
            final TruffleString tstring = TStringUtils.fromByteArray(s.unescaped, Encodings.UTF_8);
            final TruffleString cachedTString = language.tstringCache.getTString(tstring, Encodings.UTF_8);
            final RubyNode rubyNode = new StringLiteralNode(cachedTString, Encodings.UTF_8);

            assignNodePositionInSource(node, rubyNode);
            copyNewlineFlag(s, rubyNode);

            return rubyNode;
        }

        // Don't handle a special case for `"a #{ "b" } c"` literal
        // JRuby parser handled it itself and returned plain StrParseNode

        for (int i = 0; i < node.parts.length; i++) {
            var part = node.parts[i];

            children[i] = ToSNodeGen.create(part.accept(this));
        }

        final RubyNode rubyNode = new InterpolatedStringNode(children, Encodings.UTF_8.jcoding);
        assignNodePositionInSource(node, rubyNode);

        return rubyNode;
    }

    public RubyNode visitInterpolatedSymbolNode(Nodes.InterpolatedSymbolNode node) {
        final ToSNode[] children = new ToSNode[node.parts.length];

        // a special case for `:"abc"` literal - convert to Symbol ourselves
        if (node.parts.length == 1 && node.parts[0] instanceof Nodes.StringNode s) {
            final RubySymbol symbol = language.getSymbol(toString(s));
            final RubyNode rubyNode = new ObjectLiteralNode(symbol);

            assignNodePositionInSource(node, rubyNode);
            copyNewlineFlag(s, rubyNode);

            return rubyNode;
        }

        // Don't handle a special case for `:"a #{ "b" } c"` literal
        // JRuby parser handled it itself and returned plain SymbolParseNode

        for (int i = 0; i < node.parts.length; i++) {
            var part = node.parts[i];

            children[i] = ToSNodeGen.create(part.accept(this));
        }

        final RubyNode stringNode = new InterpolatedStringNode(children, Encodings.UTF_8.jcoding);
        final RubyNode rubyNode = StringToSymbolNodeGen.create(stringNode);
        assignNodePositionInSource(node, rubyNode);

        return rubyNode;
    }

    public RubyNode visitInterpolatedXStringNode(Nodes.InterpolatedXStringNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitKeywordHashNode(Nodes.KeywordHashNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitKeywordParameterNode(Nodes.KeywordParameterNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitKeywordRestParameterNode(Nodes.KeywordRestParameterNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitLambdaNode(Nodes.LambdaNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitLocalVariableOperatorAndWriteNode(Nodes.LocalVariableOperatorAndWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitLocalVariableOperatorOrWriteNode(Nodes.LocalVariableOperatorOrWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitLocalVariableOperatorWriteNode(Nodes.LocalVariableOperatorWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitLocalVariableReadNode(Nodes.LocalVariableReadNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitLocalVariableWriteNode(Nodes.LocalVariableWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitMatchPredicateNode(Nodes.MatchPredicateNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitMatchRequiredNode(Nodes.MatchRequiredNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitMissingNode(Nodes.MissingNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitModuleNode(Nodes.ModuleNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitMultiWriteNode(Nodes.MultiWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitNextNode(Nodes.NextNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitNilNode(Nodes.NilNode node) {
        final RubyNode rubyNode = new NilLiteralNode(false);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitNoKeywordsParameterNode(Nodes.NoKeywordsParameterNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitNumberedReferenceReadNode(Nodes.NumberedReferenceReadNode node) {
        final String name = toString(node);
        final int index = Integer.parseInt(name.substring(1));
        final RubyNode lastMatchNode = ReadGlobalVariableNodeGen.create("$~");

        final RubyNode rubyNode = new ReadMatchReferenceNodes.ReadNthMatchNode(lastMatchNode, index);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitOptionalParameterNode(Nodes.OptionalParameterNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitOrNode(Nodes.OrNode node) {
        final RubyNode left = node.left.accept(this);
        final RubyNode right = node.right.accept(this);

        final RubyNode rubyNode = OrNodeGen.create(left, right);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitParametersNode(Nodes.ParametersNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitParenthesesNode(Nodes.ParenthesesNode node) {
        if (node.statements == null) {
            final RubyNode rubyNode = new NilLiteralNode(true);
            assignNodePositionInSource(node, rubyNode);
            return rubyNode;
        }
        return node.statements.accept(this);
    }

    public RubyNode visitPinnedExpressionNode(Nodes.PinnedExpressionNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitPinnedVariableNode(Nodes.PinnedVariableNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitPostExecutionNode(Nodes.PostExecutionNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitPreExecutionNode(Nodes.PreExecutionNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitProgramNode(Nodes.ProgramNode node) {
        return node.statements.accept(this);
    }

    public RubyNode visitRangeNode(Nodes.RangeNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitRationalNode(Nodes.RationalNode node) {
        // Translate as Rational.convert(n, 1) ignoring visibility

        // TODO(CS): use IntFixnumLiteralNode where possible

        final RubyNode objectClassNode = new ObjectClassLiteralNode();
        final ReadConstantNode rationalModuleNode = new ReadConstantNode(objectClassNode, "Rational");
        final RubyNode numeratorNode = node.numeric.accept(this);
        final RubyNode denominatorNode = new IntegerFixnumLiteralNode(1);

        RubyNode[] arguments = new RubyNode[]{ numeratorNode, denominatorNode };
        RubyNode rubyNode = createCallNode(rationalModuleNode, "convert", arguments);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitRedoNode(Nodes.RedoNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitRegularExpressionNode(Nodes.RegularExpressionNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitRequiredDestructuredParameterNode(Nodes.RequiredDestructuredParameterNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitRequiredParameterNode(Nodes.RequiredParameterNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitRescueModifierNode(Nodes.RescueModifierNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitRescueNode(Nodes.RescueNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitRestParameterNode(Nodes.RestParameterNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitRetryNode(Nodes.RetryNode node) {
        final RubyNode rubyNode = new RetryNode();
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitReturnNode(Nodes.ReturnNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitSelfNode(Nodes.SelfNode node) {
        final RubyNode rubyNode = new SelfNode();
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitSingletonClassNode(Nodes.SingletonClassNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitSourceEncodingNode(Nodes.SourceEncodingNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitSourceFileNode(Nodes.SourceFileNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitSourceLineNode(Nodes.SourceLineNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitSplatNode(Nodes.SplatNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitStatementsNode(Nodes.StatementsNode node) {
        var location = new SourceIndexLength(node.startOffset, node.length);

        var body = node.body;
        var translated = new RubyNode[body.length];
        for (int i = 0; i < body.length; i++) {
            translated[i] = body[i].accept(this);
        }
        return Translator.sequence(location, Arrays.asList(translated));
    }

    public RubyNode visitStringConcatNode(Nodes.StringConcatNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitStringNode(Nodes.StringNode node) {
        final RubyEncoding encoding = Encodings.UTF_8;
        final TruffleString tstring = TStringUtils.fromByteArray(node.unescaped, encoding);
        final TruffleString cachedTString = language.tstringCache.getTString(tstring, encoding);
        final RubyNode rubyNode = new StringLiteralNode(cachedTString, encoding);

        assignNodePositionInSource(node, rubyNode);

        return rubyNode;
    }

    public RubyNode visitSuperNode(Nodes.SuperNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitSymbolNode(Nodes.SymbolNode node) {
        final RubySymbol symbol = language.getSymbol(toString(node));
        final RubyNode rubyNode = new ObjectLiteralNode(symbol);

        assignNodePositionInSource(node, rubyNode);

        return rubyNode;
    }

    public RubyNode visitTrueNode(Nodes.TrueNode node) {
        final RubyNode rubyNode = new BooleanLiteralNode(true);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitUndefNode(Nodes.UndefNode node) {
        RubyNode[] names = new RubyNode[node.names.length];
        for (int i = 0; i < node.names.length; i++) {
            names[i] = node.names[i].accept(this);
        }

        final RubyNode rubyNode = new ModuleNodes.UndefNode(names);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitUnlessNode(Nodes.UnlessNode node) {
        final RubyNode conditionNode = node.predicate.accept(this);
        final RubyNode thenNode = node.statements == null ? null : node.statements.accept(this);
        final RubyNode elseNode = node.consequent == null ? null : node.consequent.accept(this);
        final RubyNode rubyNode;

        if (thenNode != null && elseNode != null) {
            rubyNode = IfElseNodeGen.create(conditionNode, elseNode, thenNode);
            assignNodePositionInSource(node, rubyNode);
        } else if (thenNode != null) {
            rubyNode = UnlessNodeGen.create(conditionNode, thenNode);
            assignNodePositionInSource(node, rubyNode);
        } else if (elseNode != null) {
            rubyNode = IfNodeGen.create(conditionNode, elseNode);
            assignNodePositionInSource(node, rubyNode);
        } else {
            // unless (condition)
            // end
            final SourceIndexLength sourceSection = new SourceIndexLength(node.startOffset, node.length);
            rubyNode = Translator.sequence(sourceSection, Arrays.asList(conditionNode, new NilLiteralNode(true)));
        }

        return rubyNode;
    }

    public RubyNode visitUntilNode(Nodes.UntilNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitWhenNode(Nodes.WhenNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitWhileNode(Nodes.WhileNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitXStringNode(Nodes.XStringNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitYieldNode(Nodes.YieldNode node) {
        return defaultVisit(node);
    }

    @Override
    protected RubyNode defaultVisit(Nodes.Node node) {
        throw new Error("Unknown node: " + node);
    }

    protected RubyNode translateNodeOrNil(SourceIndexLength sourceSection, Nodes.Node node) {
        final RubyNode rubyNode;
        if (node == null) {
            rubyNode = nilNode(sourceSection);
        } else {
            rubyNode = node.accept(this);
        }
        return rubyNode;
    }

    protected RubyNode nilNode(SourceIndexLength sourceSection) {
        final RubyNode literal = new NilLiteralNode(false);
        literal.unsafeSetSourceSection(sourceSection);
        return literal;
    }

    private RubyNode getLexicalScopeNode(String kind, SourceIndexLength sourceSection) {
        if (environment.isDynamicConstantLookup()) {
            if (language.options.LOG_DYNAMIC_CONSTANT_LOOKUP) {
                RubyLanguage.LOGGER.info(() -> kind + " at " +
                        RubyLanguage.getCurrentContext().fileLine(sourceSection.toSourceSection(source)));
            }
            return new GetDynamicLexicalScopeNode();
        } else {
            return new ObjectLiteralNode(environment.getStaticLexicalScope());
        }
    }

    private boolean isInvalidYield() {
        return environment.getSurroundingMethodEnvironment().isModuleBody();
    }

    protected RubyContextSourceNode createCallNode(RubyNode receiver, String method, RubyNode... arguments) {
        var parameters = new RubyCallNodeParameters(
                receiver,
                method,
                null,
                EmptyArgumentsDescriptor.INSTANCE,
                arguments,
                true);
        return language.coreMethodAssumptions.createCallNode(parameters);
    }

    private String toString(Nodes.Location location) {
        return new String(sourceBytes, location.startOffset, location.length, StandardCharsets.US_ASCII);
    }

    private String toString(Nodes.Node node) {
        return new String(sourceBytes, node.startOffset, node.length, StandardCharsets.US_ASCII);
    }

    private String toString(Nodes.SymbolNode node) {
        return new String(node.unescaped);
    }

    private void assignNodePositionInSource(Nodes.Node yarpNode, RubyNode rubyNode) {
        rubyNode.unsafeSetSourceSection(yarpNode.startOffset, yarpNode.length);
        copyNewlineFlag(yarpNode, rubyNode);
    }

    private void copyNewlineFlag(Nodes.Node yarpNode, RubyNode rubyNode) {
        if (yarpNode.hasNewLineFlag()) {
            rubyNode.unsafeSetIsNewLine();
        }
    }

}
