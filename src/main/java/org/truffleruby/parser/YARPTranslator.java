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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.annotations.Split;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.array.ArrayLiteralNode;
import org.truffleruby.core.array.AssignableNode;
import org.truffleruby.core.cast.HashCastNodeGen;
import org.truffleruby.core.cast.SplatCastNode;
import org.truffleruby.core.cast.SplatCastNodeGen;
import org.truffleruby.core.cast.StringToSymbolNodeGen;
import org.truffleruby.core.cast.ToSNode;
import org.truffleruby.core.cast.ToSNodeGen;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;
import org.truffleruby.core.hash.ConcatHashLiteralNode;
import org.truffleruby.core.hash.HashLiteralNode;
import org.truffleruby.core.module.ModuleNodes;
import org.truffleruby.core.rescue.AssignRescueVariableNode;
import org.truffleruby.core.string.FrozenStrings;
import org.truffleruby.core.string.InterpolatedStringNode;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.debug.ChaosNode;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.RubyTopLevelRootNode;
import org.truffleruby.language.arguments.EmptyArgumentsDescriptor;
import org.truffleruby.language.arguments.ProfileArgumentNodeGen;
import org.truffleruby.language.arguments.ReadSelfNode;
import org.truffleruby.language.constants.ReadConstantNode;
import org.truffleruby.language.constants.ReadConstantWithDynamicScopeNode;
import org.truffleruby.language.constants.ReadConstantWithLexicalScopeNode;
import org.truffleruby.language.constants.WriteConstantNode;
import org.truffleruby.language.control.AndNodeGen;
import org.truffleruby.language.control.BreakNode;
import org.truffleruby.language.control.IfElseNodeGen;
import org.truffleruby.language.control.IfNodeGen;
import org.truffleruby.language.control.NextNode;
import org.truffleruby.language.control.NotNodeGen;
import org.truffleruby.language.control.OrNodeGen;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.RedoNode;
import org.truffleruby.language.control.RetryNode;
import org.truffleruby.language.control.SequenceNode;
import org.truffleruby.language.control.ReturnID;
import org.truffleruby.language.control.UnlessNodeGen;
import org.truffleruby.language.control.BreakID;
import org.truffleruby.language.control.WhileNode;
import org.truffleruby.language.control.WhileNodeFactory;
import org.truffleruby.language.defined.DefinedNode;
import org.truffleruby.language.dispatch.RubyCallNode;
import org.truffleruby.language.dispatch.RubyCallNodeParameters;
import org.truffleruby.language.exceptions.EnsureNodeGen;
import org.truffleruby.language.exceptions.RescueClassesNode;
import org.truffleruby.language.exceptions.RescueNode;
import org.truffleruby.language.exceptions.RescueSplatNode;
import org.truffleruby.language.exceptions.RescueStandardErrorNode;
import org.truffleruby.language.exceptions.TryNodeGen;
import org.truffleruby.language.globals.AliasGlobalVarNode;
import org.truffleruby.language.globals.ReadGlobalVariableNodeGen;
import org.truffleruby.language.globals.ReadMatchReferenceNodes;
import org.truffleruby.language.globals.WriteGlobalVariableNodeGen;
import org.truffleruby.language.literal.BooleanLiteralNode;
import org.truffleruby.language.literal.FloatLiteralNode;
import org.truffleruby.language.literal.FrozenStringLiteralNode;
import org.truffleruby.language.literal.IntegerFixnumLiteralNode;
import org.truffleruby.language.literal.NilLiteralNode;
import org.truffleruby.language.literal.ObjectClassLiteralNode;
import org.truffleruby.language.literal.ObjectLiteralNode;
import org.truffleruby.language.literal.StringLiteralNode;
import org.truffleruby.language.locals.InitFlipFlopSlotNode;
import org.truffleruby.language.locals.ReadLocalNode;
import org.truffleruby.language.locals.WriteLocalNode;
import org.truffleruby.language.locals.WriteLocalVariableNode;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.methods.CatchBreakNode;
import org.truffleruby.language.methods.ModuleBodyDefinition;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.objects.DefineClassNode;
import org.truffleruby.language.objects.DefineModuleNode;
import org.truffleruby.language.objects.DefineModuleNodeGen;
import org.truffleruby.language.objects.DynamicLexicalScopeNode;
import org.truffleruby.language.objects.GetDynamicLexicalScopeNode;
import org.truffleruby.language.objects.InsideModuleDefinitionNode;
import org.truffleruby.language.objects.LexicalScopeNode;
import org.truffleruby.language.objects.ReadInstanceVariableNode;
import org.truffleruby.language.objects.RunModuleDefinitionNode;
import org.truffleruby.language.objects.SelfNode;
import org.truffleruby.language.objects.WriteInstanceVariableNodeGen;
import org.truffleruby.language.objects.classvariables.ReadClassVariableNode;
import org.truffleruby.language.objects.classvariables.WriteClassVariableNode;
import org.yarp.AbstractNodeVisitor;
import org.yarp.Nodes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

// NOTE: we should avoid SourceIndexLength in YARPTranslator, instead pass a Nodes.Node as location, because
// * it does not copy the newline flag properly,
// * it is inefficient,
// * there is typically no need for such an object since YARP location info is correct.

/** Translate (or convert) AST provided by a parser (YARP parser) to Truffle AST */
public final class YARPTranslator extends AbstractNodeVisitor<RubyNode> {

    private final RubyLanguage language;
    private final YARPTranslator parent;
    private final TranslatorEnvironment environment;
    private final byte[] sourceBytes;
    private final Source source;
    private final ParserContext parserContext;
    private final Node currentNode;
    private final RubyDeferredWarnings rubyWarnings;
    private final RubyEncoding sourceEncoding;

    public Deque<Integer> frameOnStackMarkerSlotStack = new ArrayDeque<>();

    public static final int NO_FRAME_ON_STACK_MARKER = -1;

    public static final RescueNode[] EMPTY_RESCUE_NODE_ARRAY = new RescueNode[0];

    private boolean translatingWhile = false;

    private boolean translatingNextExpression = false;

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
        this.sourceEncoding = Encodings.UTF_8; // TODO
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
        final Nodes.Node[] values = node.arguments;

        if (values.length == 1) {
            return values[0].accept(this);
        }

        final RubyNode[] translatedValues = createArray(values.length);

        for (int n = 0; n < values.length; n++) {
            translatedValues[n] = values[n].accept(this);
        }

        final RubyNode rubyNode = ArrayLiteralNode.create(language, translatedValues);
        assignNodePositionInSource(node, rubyNode);

        return rubyNode;
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
        RubyNode rubyNode;

        // empty begin/end block
        if (node.statements == null) {
            return new NilLiteralNode(true);
        }

        rubyNode = node.statements.accept(this);

        // fast path
        if (node.rescue_clause == null && node.ensure_clause == null) {
            return rubyNode;
        }

        // begin/end block with rescue section(s)
        // ignore else section if there is no a rescue one,
        // because else branch without rescue branch is a syntax error
        if (node.rescue_clause != null) {

            final List<RescueNode> rescueNodes = new ArrayList<>();
            Nodes.RescueNode rescueClause = node.rescue_clause;

            // rescue clauses are organized into a linked list
            // traverse this linked list to translate and accumulate rescue nodes
            while (rescueClause != null) { // each rescue clause
                if (rescueClause.exceptions.length != 0) {
                    // TODO: this duplicate rescue body 3 times for e.g. `rescue A, *b, C`, but we should avoid duplicating code
                    final ArrayList<Nodes.Node> exceptionNodes = new ArrayList<>();

                    for (Nodes.Node exceptionNode : rescueClause.exceptions) {

                        if (exceptionNode instanceof Nodes.SplatNode splatNode) {
                            if (!exceptionNodes.isEmpty()) {
                                // dump all the accumulated so far exception classes and clear the list
                                final RescueNode rescueNode = translateExceptionNodes(exceptionNodes, rescueClause);
                                rescueNodes.add(rescueNode);
                                exceptionNodes.clear();
                            }

                            final RubyNode splatTranslated = translateNodeOrNil(splatNode.expression);
                            RubyNode translatedBody = translateNodeOrNil(rescueClause.statements);

                            if (rescueClause.reference != null) {
                                final RubyNode exceptionWriteNode = translateRescueException(rescueClause.reference);
                                translatedBody = sequence(rescueClause,
                                        Arrays.asList(exceptionWriteNode, translatedBody));
                            }

                            final RescueNode rescueNode = new RescueSplatNode(language, splatTranslated,
                                    translatedBody);
                            assignNodePositionInSource(splatNode, rescueNode);

                            rescueNodes.add(rescueNode);
                        } else {
                            // accumulate a list of exception classes (before a splat operator)
                            exceptionNodes.add(exceptionNode);
                        }
                    }

                    // process exception classes after the last splat operator
                    // or all the exception classes in a list if there is no splat operator
                    if (!exceptionNodes.isEmpty()) {
                        final RescueNode rescueNode = translateExceptionNodes(exceptionNodes, rescueClause);
                        rescueNodes.add(rescueNode);
                    }
                } else {
                    // exception class isn't specified explicitly so use Ruby StandardError class
                    RubyNode translatedBody = translateNodeOrNil(rescueClause.statements);

                    if (rescueClause.reference != null) {
                        final RubyNode exceptionWriteNode = translateRescueException(rescueClause.reference);
                        translatedBody = sequence(rescueClause, Arrays.asList(exceptionWriteNode, translatedBody));
                    }

                    final RescueStandardErrorNode rescueNode = new RescueStandardErrorNode(translatedBody);
                    assignNodePositionInSource(rescueClause, rescueNode);

                    rescueNodes.add(rescueNode);
                }

                rescueClause = rescueClause.consequent;
            }

            RubyNode elsePart;

            if (node.else_clause == null) {
                elsePart = null;
            } else {
                elsePart = node.else_clause.accept(this);
            }

            rescueClause = node.rescue_clause;

            // TODO: this flag should be per RescueNode, not per TryNode
            boolean canOmitBacktrace = language.options.BACKTRACES_OMIT_UNUSED &&
                    rescueClause.reference == null &&
                    rescueClause.consequent == null &&
                    (rescueClause.statements == null || rescueClause.statements.body.length == 1 &&
                            isSideEffectFreeRescueExpression(rescueClause.statements.body[0]));

            rubyNode = TryNodeGen.create(
                    rubyNode,
                    rescueNodes.toArray(EMPTY_RESCUE_NODE_ARRAY),
                    elsePart,
                    canOmitBacktrace);
            assignNodePositionInSource(node, rubyNode);
        }

        // with ensure section
        if (node.ensure_clause != null && node.ensure_clause.statements != null) {
            final RubyNode ensureBlock = node.ensure_clause.accept(this);
            rubyNode = EnsureNodeGen.create(rubyNode, ensureBlock);
            assignNodePositionInSource(node, rubyNode);
        }

        return rubyNode;
    }

    private RescueNode translateExceptionNodes(ArrayList<Nodes.Node> exceptionNodes, Nodes.RescueNode rescueClause) {
        RubyNode translatedBody = translateNodeOrNil(rescueClause.statements);

        final Nodes.Node[] exceptionNodesArray = exceptionNodes.toArray(Nodes.Node.EMPTY_ARRAY);
        final RubyNode[] handlingClasses = new RubyNode[exceptionNodesArray.length];

        for (int i = 0; i < exceptionNodesArray.length; i++) {
            handlingClasses[i] = exceptionNodesArray[i].accept(this);
        }

        if (rescueClause.reference != null) {
            final RubyNode exceptionWriteNode = translateRescueException(
                    rescueClause.reference);
            translatedBody = sequence(rescueClause,
                    Arrays.asList(exceptionWriteNode, translatedBody));
        }

        final RescueNode rescueNode = new RescueClassesNode(handlingClasses, translatedBody);
        assignNodePositionInSource(exceptionNodesArray, rescueNode);
        return rescueNode;
    }

    public RubyNode visitBlockArgumentNode(Nodes.BlockArgumentNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitBlockNode(Nodes.BlockNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitBreakNode(Nodes.BreakNode node) {
        // detect syntax error
        // YARP doesn't emit errors for incorrect usage of break/redo/next
        // See https://github.com/ruby/yarp/issues/913
        if (!environment.isBlock() && !translatingWhile) {
            final RubyContext context = RubyLanguage.getCurrentContext();
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().syntaxError(
                            "Invalid break",
                            currentNode,
                            getSourceSection(node)));
        }

        final RubyNode argumentsNode = translateControlFlowArguments(node.arguments);
        final RubyNode rubyNode = new BreakNode(environment.getBreakID(), translatingWhile, argumentsNode);
        assignNodePositionInSource(node, rubyNode);

        return rubyNode;
    }

    public RubyNode visitCallNode(Nodes.CallNode node) {
        var methodName = TStringUtils.toJavaStringOrThrow(node.name, sourceEncoding);
        var receiver = node.receiver == null ? new SelfNode() : node.receiver.accept(this);
        final Nodes.Node[] arguments;

        if (node.arguments == null) {
            arguments = Nodes.Node.EMPTY_ARRAY;
        } else {
            arguments = node.arguments.arguments;
        }

        var translatedArguments = new RubyNode[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            translatedArguments[i] = arguments[i].accept(this);
        }

        boolean ignoreVisibility = node.receiver == null;
        boolean isVCall = arguments.length == 0 && (node.opening_loc == null || node.closing_loc == null); // only `foo` without arguments and `()`
        boolean isAttrAssign = methodName.endsWith("=");
        var rubyCallNode = new RubyCallNode(new RubyCallNodeParameters(receiver, methodName, null,
                EmptyArgumentsDescriptor.INSTANCE, translatedArguments, false, ignoreVisibility, isVCall, false,
                isAttrAssign));

        assignNodePositionInSource(node, rubyCallNode);
        return rubyCallNode;
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
        final String name;
        final RubyNode lexicalParent = translateCPath(node.constant_path);
        final RubyNode superClass;

        if (node.constant_path instanceof Nodes.ConstantReadNode constantNode) {
            name = toString(constantNode);
        } else if (node.constant_path instanceof Nodes.ConstantPathNode pathNode) {
            name = toString(pathNode.child);
        } else {
            throw CompilerDirectives.shouldNotReachHere();
        }

        if (node.superclass != null) {
            superClass = node.superclass.accept(this);
        } else {
            superClass = null;
        }

        final DefineClassNode defineOrGetClass = new DefineClassNode(name, lexicalParent, superClass);

        final RubyNode rubyNode = openModule(
                node,
                defineOrGetClass,
                name,
                node.statements,
                OpenModule.CLASS,
                shouldUseDynamicConstantLookupForModuleBody(node));

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
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
        final RubyNode rubyNode = new ReadClassVariableNode(
                getLexicalScopeNode("class variable lookup", node),
                toString(node));

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitClassVariableWriteNode(Nodes.ClassVariableWriteNode node) {
        final RubyNode rhs = translateNodeOrDeadNode(node.value, "YARPTranslator#visitClassVariableWriteNode");
        final RubyNode rubyNode = new WriteClassVariableNode(
                getLexicalScopeNode("set dynamic class variable", node),
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
        assert node.child instanceof Nodes.ConstantReadNode;

        final String name = toString(node.child);
        final RubyNode moduleNode;

        if (node.parent != null) {
            // FOO
            moduleNode = node.parent.accept(this);
        } else {
            // ::FOO or FOO::BAR
            moduleNode = new ObjectClassLiteralNode();
        }

        final RubyNode rubyNode = new ReadConstantNode(moduleNode, name);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
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
        assert node.target instanceof Nodes.ConstantPathNode;

        final RubyNode rubyNode;
        final RubyNode value = translateNodeOrDeadNode(node.value, "YARPTranslator#visitConstantPathWriteNode");
        final var pathNode = (Nodes.ConstantPathNode) node.target;
        final String name = toString(pathNode.child);
        final RubyNode moduleNode;

        if (pathNode.parent != null) {
            // FOO::BAR = 1
            moduleNode = pathNode.parent.accept(this);
        } else {
            // ::FOO = 1
            moduleNode = new ObjectClassLiteralNode();
        }

        rubyNode = new WriteConstantNode(name, moduleNode, value);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitConstantReadNode(Nodes.ConstantReadNode node) {
        final RubyNode rubyNode;
        final String name = toString(node);

        if (environment.isDynamicConstantLookup()) {
            if (language.options.LOG_DYNAMIC_CONSTANT_LOOKUP) {
                RubyLanguage.LOGGER.info(() -> "dynamic constant lookup at " +
                        RubyLanguage.getCurrentContext().fileLine(getSourceSection(node)));
            }

            rubyNode = new ReadConstantWithDynamicScopeNode(name);
        } else {
            final LexicalScope lexicalScope = environment.getStaticLexicalScope();
            rubyNode = new ReadConstantWithLexicalScopeNode(lexicalScope, name);
        }

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitConstantWriteNode(Nodes.ConstantWriteNode node) {
        final String name = toString(node.name_loc);
        final RubyNode value = translateNodeOrDeadNode(node.value, "YARPTranslator#visitConstantWriteNode");
        final RubyNode moduleNode;

        if (environment.isDynamicConstantLookup()) {
            if (language.options.LOG_DYNAMIC_CONSTANT_LOOKUP) {
                RubyLanguage.LOGGER.info(() -> "set dynamic constant at " +
                        RubyLanguage.getCurrentContext().fileLine(getSourceSection(node)));
            }

            moduleNode = new DynamicLexicalScopeNode();
        } else {
            moduleNode = new LexicalScopeNode(environment.getStaticLexicalScope());
        }

        final RubyNode rubyNode = new WriteConstantNode(name, moduleNode, value);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
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
                    language.getFrozenStringLiteral(sourceEncoding.tencoding.getEmpty(), sourceEncoding));
            assignNodePositionInSource(node, rubyNode);
            return rubyNode;
        }

        return node.statements.accept(this);
    }

    public RubyNode visitEmbeddedVariableNode(Nodes.EmbeddedVariableNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitEnsureNode(Nodes.EnsureNode node) {
        if (node.statements == null) {
            final RubyNode rubyNode = new NilLiteralNode(true);
            assignNodePositionInSource(node, rubyNode);
            return rubyNode;
        }

        return node.statements.accept(this);
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
        // parse Integer literal ourselves
        // See https://github.com/ruby/yarp/issues/1098
        final String string = toString(node).replaceAll("_", "");
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
        final String name = toString(node.name_loc);
        final RubyNode value = translateNodeOrDeadNode(node.value, "YARPTranslator#visitGlobalVariableWriteNode");
        final RubyNode rubyNode = WriteGlobalVariableNodeGen.create(name, value);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitHashNode(Nodes.HashNode node) {
        if (node.elements.length == 0) { // an empty Hash literal like h = {}
            final RubyNode rubyNode = HashLiteralNode.create(RubyNode.EMPTY_ARRAY);
            assignNodePositionInSource(node, rubyNode);
            return rubyNode;
        }

        final List<RubyNode> hashConcats = new ArrayList<>();
        final List<RubyNode> keyValues = new ArrayList<>();

        for (Nodes.Node pair : node.elements) {
            if (pair instanceof Nodes.AssocSplatNode assocSplatNode) {
                // This case is for splats {a: 1, **{b: 2}, c: 3}
                if (!keyValues.isEmpty()) {
                    final RubyNode hashLiteralSoFar = HashLiteralNode
                            .create(keyValues.toArray(RubyNode.EMPTY_ARRAY));
                    hashConcats.add(hashLiteralSoFar);
                }
                hashConcats.add(HashCastNodeGen.HashCastASTNodeGen.create(assocSplatNode.value.accept(this)));
                keyValues.clear();
            } else if (pair instanceof Nodes.AssocNode assocNode) {
                RubyNode keyNode = assocNode.key.accept(this);

                // String literals in a key position become frozen
                if (keyNode instanceof StringLiteralNode stringNode) {
                    var frozenString = language.getFrozenStringLiteral(stringNode.getTString(),
                            stringNode.getEncoding());
                    keyNode = new FrozenStringLiteralNode(frozenString, FrozenStrings.EXPRESSION);
                }

                keyValues.add(keyNode);
                keyValues.add(assocNode.value.accept(this));
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }
        }

        if (!keyValues.isEmpty()) {
            final RubyNode hashLiteralSoFar = HashLiteralNode.create(keyValues.toArray(RubyNode.EMPTY_ARRAY));
            hashConcats.add(hashLiteralSoFar);
        }

        if (hashConcats.size() == 1) {
            final RubyNode rubyNode = hashConcats.get(0);
            assignNodePositionInSource(node, rubyNode);
            return rubyNode;
        }

        final RubyNode rubyNode = new ConcatHashLiteralNode(hashConcats.toArray(RubyNode.EMPTY_ARRAY));
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
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
            rubyNode = sequence(node, Arrays.asList(conditionNode, new NilLiteralNode(true)));
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
        final String name = toString(node);
        final RubyNode rubyNode = new ReadInstanceVariableNode(name);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitInstanceVariableWriteNode(Nodes.InstanceVariableWriteNode node) {
        final String name = toString(node.name_loc);
        final RubyNode value = translateNodeOrDeadNode(node.value, "YARPTranslator#visitInstanceVariableWriteNode");
        final RubyNode rubyNode = WriteInstanceVariableNodeGen.create(name, value);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitIntegerNode(Nodes.IntegerNode node) {
        // parse Integer literal ourselves
        // See https://github.com/ruby/yarp/issues/1098
        final String string = toString(node).replaceAll("_", "");

        final int radix;
        final int offset;

        if (string.startsWith("0b") || string.startsWith("0B")) {
            radix = 2;
            offset = 2;
        } else if (string.startsWith("0x") || string.startsWith("0X")) {
            radix = 16;
            offset = 2;
        } else if (string.startsWith("0d") || string.startsWith("0D")) {
            radix = 10;
            offset = 2;
        } else if (string.startsWith("0o") || string.startsWith("0O")) {
            radix = 8;
            offset = 2;
        } else if (string.startsWith("0")) {
            radix = 8;
            offset = 1;
        } else {
            radix = 10;
            offset = 0;
        }

        final long value = Long.parseLong(string.substring(offset), radix);
        final RubyNode rubyNode = Translator.integerOrLongLiteralNode(value);

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
            final TruffleString tstring = TStringUtils.fromByteArray(s.unescaped, sourceEncoding);
            final TruffleString cachedTString = language.tstringCache.getTString(tstring, sourceEncoding);
            final RubyNode rubyNode = new StringLiteralNode(cachedTString, sourceEncoding);

            assignNodePositionInSource(node, rubyNode);
            copyNewlineFlag(s, rubyNode);

            return rubyNode;
        }

        for (int i = 0; i < node.parts.length; i++) {
            var part = node.parts[i];

            children[i] = ToSNodeGen.create(part.accept(this));
        }

        final RubyNode rubyNode = new InterpolatedStringNode(children, sourceEncoding.jcoding);
        assignNodePositionInSource(node, rubyNode);

        return rubyNode;
    }

    public RubyNode visitInterpolatedSymbolNode(Nodes.InterpolatedSymbolNode node) {
        final ToSNode[] children = new ToSNode[node.parts.length];

        for (int i = 0; i < node.parts.length; i++) {
            final RubyNode expression = node.parts[i].accept(this);
            children[i] = ToSNodeGen.create(expression);
        }

        final RubyNode stringNode = new InterpolatedStringNode(children, sourceEncoding.jcoding);
        final RubyNode rubyNode = StringToSymbolNodeGen.create(stringNode);
        assignNodePositionInSource(node, rubyNode);

        return rubyNode;
    }

    public RubyNode visitInterpolatedXStringNode(Nodes.InterpolatedXStringNode node) {
        final Nodes.InterpolatedStringNode stringNode = new Nodes.InterpolatedStringNode(
                node.opening_loc, node.parts, node.closing_loc, node.startOffset, node.length);
        final RubyNode string = stringNode.accept(this);
        final RubyNode rubyNode = createCallNode(new SelfNode(), "`", string);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitKeywordHashNode(Nodes.KeywordHashNode node) {
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
        final String name = toString(node);

        final RubyNode rubyNode = environment.findLocalVarNode(name, null);
        assert rubyNode != null : name;

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitLocalVariableWriteNode(Nodes.LocalVariableWriteNode node) {
        final String name = toString(node.name_loc);

        if (environment.getNeverAssignInParentScope()) {
            environment.declareVar(name);
        }

        ReadLocalNode lhs = environment.findLocalVarNode(name, null);

        // TODO: it should always be present if we use byte[][] locals
        if (lhs == null) {
            TranslatorEnvironment environmentToDeclareIn = environment;
            while (!environmentToDeclareIn.hasOwnScopeForAssignments()) {
                environmentToDeclareIn = environmentToDeclareIn.getParent();
            }
            environmentToDeclareIn.declareVar(name);

            lhs = environment.findLocalVarNode(name, null);

            if (lhs == null) {
                throw CompilerDirectives.shouldNotReachHere();
            }
        }

        final RubyNode rhs = translateNodeOrDeadNode(node.value, "YARPTranslator#visitLocalVariableWriteNode");
        final WriteLocalNode rubyNode = lhs.makeWriteNode(rhs);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
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
        final String name;
        final RubyNode lexicalParent = translateCPath(node.constant_path);

        if (node.constant_path instanceof Nodes.ConstantReadNode constantNode) {
            name = toString(constantNode);
        } else if (node.constant_path instanceof Nodes.ConstantPathNode pathNode) {
            name = toString(pathNode.child);
        } else {
            throw CompilerDirectives.shouldNotReachHere();
        }

        final DefineModuleNode defineModuleNode = DefineModuleNodeGen.create(name, lexicalParent);

        final RubyNode rubyNode = openModule(
                node,
                defineModuleNode,
                name,
                node.statements,
                OpenModule.MODULE,
                shouldUseDynamicConstantLookupForModuleBody(node));

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitMultiWriteNode(Nodes.MultiWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitNextNode(Nodes.NextNode node) {
        // detect syntax error
        // YARP doesn't emit errors for incorrect usage of break/redo/next
        // See https://github.com/ruby/yarp/issues/913
        if (!environment.isBlock() && !translatingWhile) {
            final RubyContext context = RubyLanguage.getCurrentContext();
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().syntaxError(
                            "Invalid next",
                            currentNode,
                            getSourceSection(node)));
        }

        final RubyNode argumentsNode;

        final boolean t = translatingNextExpression;
        translatingNextExpression = true;
        try {
            argumentsNode = translateControlFlowArguments(node.arguments);
        } finally {
            translatingNextExpression = t;
        }

        final RubyNode rubyNode = new NextNode(argumentsNode);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitNilNode(Nodes.NilNode node) {
        final RubyNode rubyNode = new NilLiteralNode(false);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitNumberedReferenceReadNode(Nodes.NumberedReferenceReadNode node) {
        final String name = toString(node);
        final int index = Integer.parseInt(name.substring(1));
        final RubyNode lastMatchNode = ReadGlobalVariableNodeGen.create("$~");

        final RubyNode rubyNode = new ReadMatchReferenceNodes.ReadNthMatchNode(lastMatchNode, index);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitOrNode(Nodes.OrNode node) {
        final RubyNode left = node.left.accept(this);
        final RubyNode right = node.right.accept(this);

        final RubyNode rubyNode = OrNodeGen.create(left, right);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
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
        // detect syntax error
        // YARP doesn't emit errors for incorrect usage of break/redo/next
        // See https://github.com/ruby/yarp/issues/913
        if (!environment.isBlock() && !translatingWhile) {
            final RubyContext context = RubyLanguage.getCurrentContext();
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().syntaxError(
                            "Invalid redo",
                            currentNode,
                            getSourceSection(node)));
        }

        final RubyNode rubyNode = new RedoNode();
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitRegularExpressionNode(Nodes.RegularExpressionNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitRescueModifierNode(Nodes.RescueModifierNode node) {
        RubyNode tryNode = node.expression.accept(this);

        // use Ruby StandardError class as far as exception class cannot be specified
        final RubyNode rescueExpressionNode = node.rescue_expression.accept(this);
        final RescueStandardErrorNode rescueNode = new RescueStandardErrorNode(rescueExpressionNode);

        boolean canOmitBacktrace = language.options.BACKTRACES_OMIT_UNUSED &&
                isSideEffectFreeRescueExpression(node.rescue_expression);

        final RubyNode rubyNode = TryNodeGen.create(
                tryNode,
                new RescueNode[]{ rescueNode },
                null,
                canOmitBacktrace);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitRescueNode(Nodes.RescueNode node) {
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
        final RubyNode value = translateNodeOrNil(node.expression);
        final RubyNode rubyNode = SplatCastNodeGen.create(language, SplatCastNode.NilBehavior.CONVERT, false, value);
        assignNodePositionInSource(node, rubyNode);

        return rubyNode;
    }

    public RubyNode visitStatementsNode(Nodes.StatementsNode node) {
        var body = node.body;
        var translated = new RubyNode[body.length];
        for (int i = 0; i < body.length; i++) {
            translated[i] = body[i].accept(this);
        }
        return sequence(node, Arrays.asList(translated));
    }

    public RubyNode visitStringConcatNode(Nodes.StringConcatNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitStringNode(Nodes.StringNode node) {
        final TruffleString tstring = TStringUtils.fromByteArray(node.unescaped, sourceEncoding);
        final TruffleString cachedTString = language.tstringCache.getTString(tstring, sourceEncoding);
        final RubyNode rubyNode = new StringLiteralNode(cachedTString, sourceEncoding);

        assignNodePositionInSource(node, rubyNode);

        return rubyNode;
    }

    public RubyNode visitSuperNode(Nodes.SuperNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitSymbolNode(Nodes.SymbolNode node) {
        var tstring = TStringUtils.fromByteArray(node.unescaped, sourceEncoding);
        final RubySymbol symbol = language.getSymbol(tstring, sourceEncoding);
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
            rubyNode = sequence(node, Arrays.asList(conditionNode, new NilLiteralNode(true)));
        }

        return rubyNode;
    }

    public RubyNode visitUntilNode(Nodes.UntilNode node) {
        final RubyNode rubyNode = translateWhileNode(node, node.predicate, node.statements, true);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitWhenNode(Nodes.WhenNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitWhileNode(Nodes.WhileNode node) {
        final RubyNode rubyNode = translateWhileNode(node, node.predicate, node.statements, false);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitXStringNode(Nodes.XStringNode node) {
        final Nodes.StringNode stringNode = new Nodes.StringNode(
                node.opening_loc, node.content_loc, node.closing_loc, node.unescaped, node.startOffset, node.length);
        final RubyNode string = stringNode.accept(this);
        final RubyNode rubyNode = createCallNode(new SelfNode(), "`", string);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitYieldNode(Nodes.YieldNode node) {
        return defaultVisit(node);
    }

    @Override
    protected RubyNode defaultVisit(Nodes.Node node) {
        throw new Error("Unknown node: " + node);
    }

    private RubyNode getLexicalScopeNode(String kind, Nodes.Node yarpNode) {
        if (environment.isDynamicConstantLookup()) {
            if (language.options.LOG_DYNAMIC_CONSTANT_LOOKUP) {
                RubyLanguage.LOGGER.info(() -> kind + " at " +
                        RubyLanguage.getCurrentContext().fileLine(getSourceSection(yarpNode)));
            }
            return new GetDynamicLexicalScopeNode();
        } else {
            return new ObjectLiteralNode(environment.getStaticLexicalScope());
        }
    }

    private RubyNode openModule(Nodes.Node moduleNode, RubyNode defineOrGetNode, String moduleName,
            Nodes.Node bodyNode, OpenModule type, boolean dynamicConstantLookup) {
        final String methodName = type.format(moduleName);

        final LexicalScope newLexicalScope = dynamicConstantLookup
                ? null
                : new LexicalScope(environment.getStaticLexicalScope());

        final SharedMethodInfo sharedMethodInfo = new SharedMethodInfo(
                getSourceSection(moduleNode),
                newLexicalScope,
                Arity.NO_ARGUMENTS,
                methodName,
                0,
                methodName,
                null,
                null);

        final String modulePath;
        if (type == OpenModule.SINGLETON_CLASS) {
            modulePath = moduleName;
        } else {
            modulePath = TranslatorEnvironment.composeModulePath(environment.modulePath, moduleName);
        }
        final TranslatorEnvironment newEnvironment = new TranslatorEnvironment(
                environment,
                environment.getParseEnvironment(),
                ReturnID.MODULE_BODY,
                true,
                true,
                sharedMethodInfo,
                methodName,
                0,
                null,
                null,
                modulePath);

        final YARPTranslator moduleTranslator = new YARPTranslator(
                language,
                this,
                newEnvironment,
                sourceBytes,
                source,
                parserContext,
                currentNode,
                rubyWarnings);

        final ModuleBodyDefinition definition = moduleTranslator.compileClassNode(moduleNode, bodyNode);

        return new RunModuleDefinitionNode(definition, defineOrGetNode);
    }

    /** Translates module and class nodes.
     * <p>
     * In Ruby, a module or class definition is somewhat like a method. It has a local scope and a value for self, which
     * is the module or class object that is being defined. Therefore for a module or class definition we translate into
     * a special method. We run that method with self set to be the newly allocated module or class.
     * </p>
     */
    private ModuleBodyDefinition compileClassNode(Nodes.Node moduleNode, Nodes.Node bodyNode) {
        RubyNode body = translateNodeOrNil(bodyNode);
        body = new InsideModuleDefinitionNode(body);

        if (environment.getFlipFlopStates().size() > 0) {
            body = sequence(Arrays.asList(initFlipFlopStates(environment), body));
        }

        final RubyNode writeSelfNode = loadSelf(language);
        body = sequence(Arrays.asList(writeSelfNode, body));

        final RubyRootNode rootNode = new RubyRootNode(
                language,
                getSourceSection(moduleNode),
                environment.computeFrameDescriptor(),
                environment.getSharedMethodInfo(),
                body,
                Split.NEVER,
                environment.getReturnID());

        return new ModuleBodyDefinition(
                environment.getSharedMethodInfo().getOriginalName(),
                environment.getSharedMethodInfo(),
                rootNode.getCallTarget(),
                environment.getStaticLexicalScopeOrNull());
    }

    private RubyNode translateCPath(Nodes.Node node) {
        final RubyNode rubyNode;

        if (node instanceof Nodes.ConstantReadNode) { // use current lexical scope
            // bare class declaration (e.g. class Foo/module Foo)
            rubyNode = getLexicalScopeModuleNode("dynamic constant lookup", node);
        } else if (node instanceof Nodes.ConstantPathNode pathNode) {
            if (pathNode.parent != null) {
                // A::B
                rubyNode = pathNode.parent.accept(this);
            } else {
                // ::A
                rubyNode = new ObjectClassLiteralNode();
            }
        } else {
            throw CompilerDirectives.shouldNotReachHere();
        }

        return rubyNode;
    }

    private RubyNode getLexicalScopeModuleNode(String kind, Nodes.Node node) {
        if (environment.isDynamicConstantLookup()) {
            if (language.options.LOG_DYNAMIC_CONSTANT_LOOKUP) {
                RubyLanguage.LOGGER.info(() -> kind + " at " +
                        RubyLanguage.getCurrentContext().fileLine(getSourceSection(node)));
            }
            return new DynamicLexicalScopeNode();
        } else {
            return new LexicalScopeNode(environment.getStaticLexicalScope());
        }
    }

    protected static RubyNode initFlipFlopStates(TranslatorEnvironment environment) {
        final RubyNode[] initNodes = createArray(environment.getFlipFlopStates().size());

        for (int n = 0; n < initNodes.length; n++) {
            initNodes[n] = new InitFlipFlopSlotNode(environment.getFlipFlopStates().get(n));
        }

        return sequence(Arrays.asList(initNodes));
    }

    protected static RubyNode[] createArray(int size) {
        return size == 0 ? RubyNode.EMPTY_ARRAY : new RubyNode[size];
    }

    public static RubyNode loadSelf(RubyLanguage language) {
        return new WriteLocalVariableNode(SelfNode.SELF_INDEX, profileArgument(language, new ReadSelfNode()));
    }

    public static RubyNode profileArgument(RubyLanguage language, RubyNode argumentNode) {
        RubyNode node = argumentNode;

        if (language.options.PROFILE_ARGUMENTS) {
            node = ProfileArgumentNodeGen.create(node);
        }

        if (language.options.CHAOS_DATA) {
            node = ChaosNode.create(node);
        }

        return node;
    }

    private boolean shouldUseDynamicConstantLookupForModuleBody(Nodes.Node node) {
        if (environment.isDynamicConstantLookup()) {
            return true;
        }

        if (environment.isModuleBody()) { // A new class/module under another class/module
            return false;
        } else if (environment.isTopLevelScope()) {
            // At the top-level of a file, executing the module/class body only once
            return false;
        } else {
            // Switch to dynamic constant lookup
            if (language.options.LOG_DYNAMIC_CONSTANT_LOOKUP) {
                RubyLanguage.LOGGER.info(() -> "start dynamic constant lookup at " +
                        RubyLanguage.getCurrentContext().fileLine(getSourceSection(node)));
            }
            return true;
        }
    }

    protected RubyNode translateNodeOrNil(Nodes.Node node) {
        final RubyNode rubyNode;
        if (node == null) {
            rubyNode = new NilLiteralNode(false); // TODO: it should be `new NilLiteralNode(isImplicit: true)`
        } else {
            rubyNode = node.accept(this);
        }
        return rubyNode;
    }

    protected RubyNode translateNodeOrDeadNode(Nodes.Node node, String label) {
        if (node != null) {
            return node.accept(this);
        } else {
            return new DeadNode(label);
        }
    }

    private boolean isInvalidYield() {
        return environment.getSurroundingMethodEnvironment().isModuleBody();
    }

    // Arguments are represented by a single node:
    // - either by a single argument value
    // - ArrayLiteralNode or
    // - combination of ArrayConcatNode/ArrayAppendOneNodeGen nodes (if there is splat operator - `break 1, *a, 2`)
    //
    // break/next/return operators treat arguments in a bit different way than method call.
    private RubyNode translateControlFlowArguments(Nodes.ArgumentsNode node) {
        if (node == null) {
            return new NilLiteralNode(false);
        }

        final Nodes.Node[] values = node.arguments;

        if (values.length == 1) {
            return values[0].accept(this);
        }

        final RubyNode[] translatedValues = createArray(values.length);

        for (int n = 0; n < values.length; n++) {
            translatedValues[n] = values[n].accept(this);
        }

        final RubyNode rubyNode = ArrayLiteralNode.create(language, translatedValues);
        assignNodePositionInSource(node, rubyNode);

        return rubyNode;
    }

    private RubyNode translateRescueException(Nodes.Node exception) {
        RubyNode writeNode = exception.accept(this);

        if (writeNode instanceof RubyCallNode rubyNode) {
            if (rubyNode.getName().equals("[]=")) {
                // rescue => a[:foo]
                assert rubyNode.getArguments().length == 1;

                final RubyNode[] arguments = new RubyNode[2];
                arguments[0] = rubyNode.getArguments()[0];
                arguments[1] = new DeadNode("YARPTranslator#translateRescueException");
                writeNode = rubyNode.cloneUninitializedWithArguments(arguments);
            } else if (rubyNode.getName().endsWith("=")) {
                // rescue => a.foo
                assert rubyNode.getArguments().length == 0;

                final RubyNode[] arguments = new RubyNode[]{ new DeadNode("YARPTranslator#translateRescueException") };
                writeNode = rubyNode.cloneUninitializedWithArguments(arguments);
            }
        }

        return new AssignRescueVariableNode((AssignableNode) writeNode);
    }

    private RubyNode translateWhileNode(Nodes.Node node, Nodes.Node predicate, Nodes.StatementsNode statements,
            boolean conditionInversed) {
        RubyNode condition = predicate.accept(this);
        if (conditionInversed) {
            condition = NotNodeGen.create(condition);
        }

        final RubyNode body;
        final BreakID whileBreakID = environment.getParseEnvironment().allocateBreakID();

        final boolean oldTranslatingWhile = translatingWhile;
        translatingWhile = true;
        final BreakID oldBreakID = environment.getBreakID();
        environment.setBreakIDForWhile(whileBreakID);
        frameOnStackMarkerSlotStack.push(NO_FRAME_ON_STACK_MARKER);

        try {
            body = translateNodeOrNil(statements);
        } finally {
            frameOnStackMarkerSlotStack.pop();
            environment.setBreakIDForWhile(oldBreakID);
            translatingWhile = oldTranslatingWhile;
        }

        final RubyNode loop;
        final boolean evaluateAtStart = !(statements != null && statements.body[0] instanceof Nodes.BeginNode);

        // in case of `begin ... end while ()`
        // the begin/end block is executed before condition
        if (evaluateAtStart) {
            loop = new WhileNode(WhileNodeFactory.WhileRepeatingNodeGen.create(condition, body));
        } else {
            loop = new WhileNode(WhileNodeFactory.DoWhileRepeatingNodeGen.create(condition, body));
        }

        final RubyNode rubyNode = new CatchBreakNode(whileBreakID, loop, true);
        return rubyNode;
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

    protected boolean isSideEffectFreeRescueExpression(Nodes.Node node) {
        return node instanceof Nodes.InstanceVariableReadNode ||
                node instanceof Nodes.LocalVariableReadNode ||
                node instanceof Nodes.ClassVariableReadNode ||
                node instanceof Nodes.StringNode ||
                node instanceof Nodes.SymbolNode ||
                // See https://github.com/ruby/yarp/issues/1120
                (node instanceof Nodes.InterpolatedSymbolNode isn && // :"abc"
                        isn.parts.length == 1 &&
                        isn.parts[0] instanceof Nodes.StringNode) ||
                node instanceof Nodes.IntegerNode ||
                node instanceof Nodes.FloatNode ||
                node instanceof Nodes.ImaginaryNode ||
                node instanceof Nodes.RationalNode ||
                node instanceof Nodes.SelfNode ||
                node instanceof Nodes.TrueNode ||
                node instanceof Nodes.FalseNode ||
                node instanceof Nodes.NilNode;
    }

    private String toString(Nodes.Location location) {
        return TStringUtils.toJavaStringOrThrow(TruffleString.fromByteArrayUncached(sourceBytes, location.startOffset,
                location.length, sourceEncoding.tencoding, false), sourceEncoding);
    }

    private String toString(Nodes.Node node) {
        return TStringUtils.toJavaStringOrThrow(TruffleString.fromByteArrayUncached(sourceBytes, node.startOffset,
                node.length, sourceEncoding.tencoding, false), sourceEncoding);
    }

    private SourceSection getSourceSection(Nodes.Node yarpNode) {
        return source.createSection(yarpNode.startOffset, yarpNode.length);
    }

    private static void assignNodePositionInSource(Nodes.Node yarpNode, RubyNode rubyNode) {
        rubyNode.unsafeSetSourceSection(yarpNode.startOffset, yarpNode.length);
        copyNewlineFlag(yarpNode, rubyNode);
    }

    private static void copyNewlineFlag(Nodes.Node yarpNode, RubyNode rubyNode) {
        if (yarpNode.hasNewLineFlag()) {
            rubyNode.unsafeSetIsNewLine();
        }
    }

    private static RubyNode sequence(Nodes.Node yarpNode, List<RubyNode> sequence) {
        assert !yarpNode.hasNewLineFlag() : "Expected node passed to sequence() to not have a newline flag";

        RubyNode sequenceNode = sequence(sequence);

        if (!sequenceNode.hasSource()) {
            assignNodePositionInSource(yarpNode, sequenceNode);
        }

        return sequenceNode;
    }

    private static RubyNode sequence(List<RubyNode> sequence) {
        final List<RubyNode> flattened = Translator.flatten(sequence, true);

        if (flattened.isEmpty()) {
            final RubyNode nilNode = new NilLiteralNode(true);
            return nilNode;
        } else if (flattened.size() == 1) {
            return flattened.get(0);
        } else {
            final RubyNode[] flatSequence = flattened.toArray(RubyNode.EMPTY_ARRAY);
            var sequenceNode = new SequenceNode(flatSequence);
            return sequenceNode;
        }
    }

    // assign position based on a list of nodes (arguments list, exception classes list in a rescue section, etc)
    private void assignNodePositionInSource(Nodes.Node[] nodes, RubyNode rubyNode) {
        final Nodes.Node first = nodes[0];
        final Nodes.Node last = nodes[nodes.length - 1];

        final int length = last.startOffset - first.startOffset + last.length;
        rubyNode.unsafeSetSourceSection(first.startOffset, length);
    }

}
