/*
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.annotations.Split;
import org.truffleruby.builtins.PrimitiveNodeConstructor;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.array.ArrayAppendOneNodeGen;
import org.truffleruby.core.array.ArrayConcatNode;
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
import org.truffleruby.core.kernel.KernelNodesFactory;
import org.truffleruby.core.module.ModuleNodes;
import org.truffleruby.core.rescue.AssignRescueVariableNode;
import org.truffleruby.core.string.FrozenStrings;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.core.string.InterpolatedStringNode;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.debug.ChaosNode;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.RubyTopLevelRootNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.arguments.EmptyArgumentsDescriptor;
import org.truffleruby.language.arguments.ProfileArgumentNodeGen;
import org.truffleruby.language.arguments.ReadSelfNode;
import org.truffleruby.language.constants.ReadConstantNode;
import org.truffleruby.language.constants.ReadConstantWithDynamicScopeNode;
import org.truffleruby.language.constants.ReadConstantWithLexicalScopeNode;
import org.truffleruby.language.constants.WriteConstantNode;
import org.truffleruby.language.control.AndNodeGen;
import org.truffleruby.language.control.BreakNode;
import org.truffleruby.language.control.IfElseNode;
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
import org.truffleruby.language.literal.TruffleInternalModuleLiteralNode;
import org.truffleruby.language.locals.FindDeclarationVariableNodes;
import org.truffleruby.language.locals.FlipFlopNodeGen;
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
        var frameDescriptor = TranslatorEnvironment.newFrameDescriptorBuilderForMethod().build();
        var sourceSection = CoreLibrary.JAVA_CORE_SOURCE_SECTION;
        var sharedMethodInfo = new SharedMethodInfo(sourceSection, null, Arity.NO_ARGUMENTS, "<main>", 0, "<main>",
                null, null);
        return new RubyTopLevelRootNode(language, sourceSection, frameDescriptor, sharedMethodInfo, body,
                Split.HEURISTIC, null, Arity.NO_ARGUMENTS);
    }

    @Override
    public RubyNode visitAliasGlobalVariableNode(Nodes.AliasGlobalVariableNode node) {
        RubyNode rubyNode = new AliasGlobalVarNode(toString(node.old_name), toString(node.new_name));

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitAliasMethodNode(Nodes.AliasMethodNode node) {
        // expected InterpolatedSymbolNode (that should be evaluated in runtime)
        // or SymbolNode
        RubyNode rubyNode = new ModuleNodes.AliasKeywordNode(
                node.new_name.accept(this),
                node.old_name.accept(this));

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
        var methodName = toString(node.name);
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

        // If the receiver is explicit or implicit 'self' then we can call private methods
        final boolean ignoreVisibility = node.receiver == null || node.receiver instanceof Nodes.SelfNode;
        final boolean isVariableCall = node.isVariableCall();
        final boolean isAttrAssign = methodName.endsWith("=");
        final boolean isSafeNavigation = node.isSafeNavigation();

        // TODO (pitr-ch 02-Dec-2019): replace with a primitive
        if (environment.getParseEnvironment().inCore() && isVariableCall && methodName.equals("undefined")) {
            // translate undefined
            final RubyNode rubyNode = new ObjectLiteralNode(NotProvided.INSTANCE);
            assignNodePositionInSource(node, rubyNode);
            return rubyNode;
        }

        if (node.receiver instanceof Nodes.StringNode stringNode &&
                (methodName.equals("freeze") || methodName.equals("-@") || methodName.equals("dedup"))) {
            final TruffleString tstring = TStringUtils.fromByteArray(stringNode.unescaped, sourceEncoding);
            final ImmutableRubyString frozenString = language.getFrozenStringLiteral(tstring, sourceEncoding);
            final RubyNode rubyNode = new FrozenStringLiteralNode(frozenString, FrozenStrings.METHOD);

            assignNodePositionInSource(node, rubyNode);
            return rubyNode;
        }

        // Translates something that looks like
        //   Primitive.foo arg1, arg2, argN
        // into
        //   InvokePrimitiveNode(FooNode(arg1, arg2, ..., argN))
        if (environment.getParseEnvironment().canUsePrimitives() &&
                node.receiver instanceof Nodes.ConstantReadNode constantReadNode &&
                toString(constantReadNode).equals("Primitive")) {

            final PrimitiveNodeConstructor constructor = language.primitiveManager.getPrimitive(methodName);
            // TODO: avoid SourceIndexLength
            final SourceIndexLength sourceSection = new SourceIndexLength(node.startOffset, node.length);
            final RubyNode rubyNode = constructor.createInvokePrimitiveNode(source, sourceSection, translatedArguments);

            return rubyNode;
        }

        final var callNodeParameters = new RubyCallNodeParameters(
                receiver,
                methodName,
                null,
                EmptyArgumentsDescriptor.INSTANCE,
                translatedArguments,
                false,
                ignoreVisibility,
                isVariableCall,
                isSafeNavigation,
                isAttrAssign);
        final RubyNode rubyNode = language.coreMethodAssumptions.createCallNode(callNodeParameters);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitCallOperatorWriteNode(Nodes.CallOperatorWriteNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitCapturePatternNode(Nodes.CapturePatternNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitCaseNode(Nodes.CaseNode node) {
        // There are two sorts of case
        // - one compares a list of expressions against a value,
        // - the other just checks a list of expressions for truth.

        final RubyNode rubyNode;
        RubyNode elseNode = translateNodeOrNil(node.consequent);

        if (node.predicate != null) {
            // Evaluate the case expression and store it in a local
            final int tempSlot = environment.declareLocalTemp("case");
            final ReadLocalNode readTemp = environment.readNode(tempSlot, null);
            final RubyNode assignTemp = readTemp.makeWriteNode(node.predicate.accept(this));

            // Build an if expression from `when` and `else` branches.
            // Work backwards to make the first if contain all the others in its `else` clause.
            final Nodes.Node[] conditions = node.conditions;

            for (int n = conditions.length - 1; n >= 0; n--) {
                // condition is either WhenNode or InNode
                // don't handle InNode for now
                assert conditions[n] instanceof Nodes.WhenNode;

                final Nodes.WhenNode when = (Nodes.WhenNode) conditions[n];
                final Nodes.Node[] whenConditions = when.conditions;

                boolean containSplatOperator = false;
                for (Nodes.Node value : whenConditions) {
                    if (value instanceof Nodes.SplatNode) {
                        containSplatOperator = true;
                    }
                }

                if (containSplatOperator) {
                    final RubyNode receiver = new TruffleInternalModuleLiteralNode();
                    final RubyNode whenConditionNode = translateExpressionsList(whenConditions);
                    final RubyNode[] arguments = new RubyNode[]{ whenConditionNode, NodeUtil.cloneNode(readTemp) };
                    final RubyNode predicateNode = createCallNode(receiver, "when_splat", arguments);

                    // create `if` node
                    final RubyNode thenNode = translateNodeOrNil(when.statements);
                    final IfElseNode ifNode = IfElseNodeGen.create(predicateNode, thenNode, elseNode);

                    // this `if` becomes `else` branch of the outer `if`
                    elseNode = ifNode;
                } else {
                    // TODO: we duplicate `then` for each when' condition, does it make sense to avoid it?
                    for (int k = whenConditions.length - 1; k >= 0; k--) {
                        final var whenCondition = whenConditions[k];
                        final RubyNode receiver = whenCondition.accept(this);
                        final RubyNode[] arguments = new RubyNode[]{ NodeUtil.cloneNode(readTemp) };
                        final RubyNode predicateNode = createCallNode(receiver, "===", arguments);

                        // create `if` node
                        final RubyNode thenNode = translateNodeOrNil(when.statements);
                        final IfElseNode ifNode = IfElseNodeGen.create(predicateNode, thenNode, elseNode);

                        // this `if` becomes `else` branch of the outer `if`
                        elseNode = ifNode;
                    }
                }
            }

            final RubyNode ifNode = elseNode;

            // A top-level block assigns the temp then runs the `if`
            rubyNode = sequence(Arrays.asList(assignTemp, ifNode));
        } else {
            // Build an if expression from `when` and `else` branches.
            // Work backwards to make the first if contain all the others in its `else` clause.

            final Nodes.Node[] conditions = node.conditions;

            for (int n = node.conditions.length - 1; n >= 0; n--) {
                // condition is either WhenNode or InNode
                // don't handle InNode for now
                assert conditions[n] instanceof Nodes.WhenNode;

                final Nodes.WhenNode when = (Nodes.WhenNode) conditions[n];
                final Nodes.Node[] whenConditions = when.conditions;

                boolean containSplatOperator = false;
                for (Nodes.Node value : whenConditions) {
                    if (value instanceof Nodes.SplatNode) {
                        containSplatOperator = true;
                    }
                }

                if (!containSplatOperator) {
                    for (int k = whenConditions.length - 1; k >= 0; k--) {
                        final var whenCondition = whenConditions[k];

                        // create `if` node
                        final RubyNode predicateNode = whenCondition.accept(this);
                        // TODO: we duplicate `then` for each when' condition, does it make sense to avoid it?
                        final RubyNode thenNode = translateNodeOrNil(when.statements);
                        final IfElseNode ifNode = IfElseNodeGen.create(predicateNode, thenNode, elseNode);

                        // this `if` becomes `else` branch of the outer `if`
                        elseNode = ifNode;
                    }
                } else {
                    // NOTE: ArrayConcatNode/ArrayAppendOneNodeGen nodes become if-node's condition
                    //       I don't understand why it works correctly - ArrayConcatNode as a condition in `if` should be truthy always

                    // create `if` node
                    final RubyNode predicateNode = translateExpressionsList(whenConditions);
                    // TODO: we duplicate `then` for each when' condition, does it make sense to avoid it?
                    final RubyNode thenNode = translateNodeOrNil(when.statements);
                    final IfElseNode ifNode = IfElseNodeGen.create(predicateNode, thenNode, elseNode);

                    // this `if` becomes `else` branch of the outer `if`
                    elseNode = ifNode;
                }


            }

            rubyNode = elseNode;
        }

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitClassNode(Nodes.ClassNode node) {
        final RubyNode lexicalParent = translateCPath(node.constant_path);
        final RubyNode superClass;

        if (node.superclass != null) {
            superClass = node.superclass.accept(this);
        } else {
            superClass = null;
        }

        final DefineClassNode defineOrGetClass = new DefineClassNode(node.name, lexicalParent, superClass);

        final RubyNode rubyNode = openModule(
                node,
                defineOrGetClass,
                node.name,
                node.body,
                OpenModule.CLASS,
                shouldUseDynamicConstantLookupForModuleBody(node));

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitClassVariableReadNode(Nodes.ClassVariableReadNode node) {
        final RubyNode rubyNode = new ReadClassVariableNode(
                getLexicalScopeNode("class variable lookup", node),
                node.name);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitClassVariableWriteNode(Nodes.ClassVariableWriteNode node) {
        final RubyNode rhs = node.value.accept(this);
        final RubyNode rubyNode = new WriteClassVariableNode(
                getLexicalScopeNode("set dynamic class variable", node),
                node.name,
                rhs);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitClassVariableTargetNode(Nodes.ClassVariableTargetNode node) {
        final RubyNode rubyNode = new WriteClassVariableNode(
                getLexicalScopeNode("set dynamic class variable", node),
                node.name,
                null);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitConstantPathNode(Nodes.ConstantPathNode node) {
        // The child field should always be ConstantReadNode if there are no syntax errors.
        // MissingNode could be assigned as well as an error recovery means,
        // but we don't handle this case as far as it means there is a syntax error and translation is skipped at all.
        assert node.child instanceof Nodes.ConstantReadNode;

        final String name = ((Nodes.ConstantReadNode) node.child).name;
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

    public RubyNode visitConstantPathWriteNode(Nodes.ConstantPathWriteNode node) {
        final Nodes.ConstantPathNode constantPathNode = node.target;

        final RubyNode moduleNode;
        if (constantPathNode.parent != null) {
            // FOO::BAR = 1
            moduleNode = constantPathNode.parent.accept(this);
        } else {
            // ::FOO = 1
            moduleNode = new ObjectClassLiteralNode();
        }

        final String name = ((Nodes.ConstantReadNode) constantPathNode.child).name;
        final RubyNode value = node.value.accept(this);
        final RubyNode rubyNode = new WriteConstantNode(name, moduleNode, value);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitConstantPathTargetNode(Nodes.ConstantPathTargetNode node) {
        final RubyNode moduleNode;
        if (node.parent != null) {
            // FOO::BAR = 1
            moduleNode = node.parent.accept(this);
        } else {
            // ::FOO = 1
            moduleNode = new ObjectClassLiteralNode();
        }

        final String name = ((Nodes.ConstantReadNode) node.child).name;
        final RubyNode rubyNode = new WriteConstantNode(name, moduleNode, null);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitConstantReadNode(Nodes.ConstantReadNode node) {
        final RubyNode rubyNode;

        if (environment.isDynamicConstantLookup()) {
            if (language.options.LOG_DYNAMIC_CONSTANT_LOOKUP) {
                RubyLanguage.LOGGER.info(() -> "dynamic constant lookup at " +
                        RubyLanguage.getCurrentContext().fileLine(getSourceSection(node)));
            }

            rubyNode = new ReadConstantWithDynamicScopeNode(node.name);
        } else {
            final LexicalScope lexicalScope = environment.getStaticLexicalScope();
            rubyNode = new ReadConstantWithLexicalScopeNode(lexicalScope, node.name);
        }

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitConstantWriteNode(Nodes.ConstantWriteNode node) {
        final RubyNode value = node.value.accept(this);
        final RubyNode moduleNode = getLexicalScopeModuleNode("set dynamic constant", node);
        final RubyNode rubyNode = new WriteConstantNode(node.name, moduleNode, value);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitConstantTargetNode(Nodes.ConstantTargetNode node) {
        final RubyNode moduleNode = getLexicalScopeModuleNode("set dynamic constant", node);
        final RubyNode rubyNode = new WriteConstantNode(node.name, moduleNode, null);

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

    public RubyNode visitFlipFlopNode(Nodes.FlipFlopNode node) {
        final RubyNode begin = node.left.accept(this);
        final RubyNode end = node.right.accept(this);

        final FindDeclarationVariableNodes.FrameSlotAndDepth slotAndDepth = createFlipFlopState(0);
        final RubyNode rubyNode = FlipFlopNodeGen.create(begin, end, node.isExcludeEnd(), slotAndDepth.depth,
                slotAndDepth.slot);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
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

    public RubyNode visitGlobalVariableReadNode(Nodes.GlobalVariableReadNode node) {
        final RubyNode rubyNode = ReadGlobalVariableNodeGen.create(node.name);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitGlobalVariableWriteNode(Nodes.GlobalVariableWriteNode node) {
        final RubyNode value = node.value.accept(this);
        final RubyNode rubyNode = WriteGlobalVariableNodeGen.create(node.name, value);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitGlobalVariableTargetNode(Nodes.GlobalVariableTargetNode node) {
        final RubyNode rubyNode = WriteGlobalVariableNodeGen.create(node.name, null);

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

    public RubyNode visitInstanceVariableReadNode(Nodes.InstanceVariableReadNode node) {
        final RubyNode rubyNode = new ReadInstanceVariableNode(node.name);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitInstanceVariableWriteNode(Nodes.InstanceVariableWriteNode node) {
        final RubyNode value = node.value.accept(this);
        final RubyNode rubyNode = WriteInstanceVariableNodeGen.create(node.name, value);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitInstanceVariableTargetNode(Nodes.InstanceVariableTargetNode node) {
        final RubyNode rubyNode = WriteInstanceVariableNodeGen.create(node.name, null);

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
        } else if (string.startsWith("0") && string.length() > 1) {
            // check length to distinguish `0` from octal literal `0...`
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
        var stringNode = new Nodes.InterpolatedStringNode(node.parts, node.startOffset, node.length);
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

    public RubyNode visitLocalVariableReadNode(Nodes.LocalVariableReadNode node) {
        final String name = node.name;

        final RubyNode rubyNode = environment.findLocalVarNode(name, null);
        assert rubyNode != null : name;

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitLocalVariableWriteNode(Nodes.LocalVariableWriteNode node) {
        final String name = node.name;

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

        // TODO simplify once visitLocalVariableTargetNode does not reuse this method
        final RubyNode rhs = translateNodeOrDeadNode(node.value, "YARPTranslator#visitLocalVariableWriteNode");
        final WriteLocalNode rubyNode = lhs.makeWriteNode(rhs);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitLocalVariableTargetNode(Nodes.LocalVariableTargetNode node) {
        // TODO: this could be done more directly but the logic of visitLocalVariableWriteNode() needs to be simpler first
        return visitLocalVariableWriteNode(
                new Nodes.LocalVariableWriteNode(node.name, node.depth, null, node.startOffset, node.length));
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
        final RubyNode lexicalParent = translateCPath(node.constant_path);

        final DefineModuleNode defineModuleNode = DefineModuleNodeGen.create(node.name, lexicalParent);

        final RubyNode rubyNode = openModule(
                node,
                defineModuleNode,
                node.name,
                node.body,
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
        final RubyNode lastMatchNode = ReadGlobalVariableNodeGen.create("$~");
        final RubyNode rubyNode = new ReadMatchReferenceNodes.ReadNthMatchNode(lastMatchNode, node.number);

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
        if (node.body == null) {
            final RubyNode rubyNode = new NilLiteralNode(true);
            assignNodePositionInSource(node, rubyNode);
            return rubyNode;
        }
        return node.body.accept(this);
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
        final RubyNode rubyNode = translateWhileNode(node, node.predicate, node.statements, true,
                !node.isBeginModifier());
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    // handled in #visitCaseNode method
    public RubyNode visitWhenNode(Nodes.WhenNode node) {
        return defaultVisit(node);
    }

    public RubyNode visitWhileNode(Nodes.WhileNode node) {
        final RubyNode rubyNode = translateWhileNode(node, node.predicate, node.statements, false,
                !node.isBeginModifier());
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    public RubyNode visitXStringNode(Nodes.XStringNode node) {
        // TODO: pass flags, needs https://github.com/ruby/yarp/issues/1567
        var stringNode = new Nodes.StringNode((short) 0, null, null, node.unescaped, node.startOffset, node.length);
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

    protected FindDeclarationVariableNodes.FrameSlotAndDepth createFlipFlopState(int depth) {
        final int frameSlot = environment.declareLocalTemp("flipflop");
        environment.getFlipFlopStates().add(frameSlot);

        if (depth == 0) {
            return new FindDeclarationVariableNodes.FrameSlotAndDepth(frameSlot, 0);
        } else {
            return new FindDeclarationVariableNodes.FrameSlotAndDepth(frameSlot, depth);
        }
    }

    private RubyNode translateExpressionsList(Nodes.Node[] nodes) {
        assert nodes != null;
        assert nodes.length > 0;

        // fast path (no SplatNode)
        boolean isSplatNodePresent = false;
        for (var n : nodes) {
            if (n instanceof Nodes.SplatNode) {
                isSplatNodePresent = true;
                break;
            }
        }

        if (!isSplatNodePresent) {
            RubyNode[] rubyNodes = new RubyNode[nodes.length];

            for (int i = 0; i < nodes.length; i++) {
                rubyNodes[i] = nodes[i].accept(this);
            }

            return ArrayLiteralNode.create(language, rubyNodes);
        }

        // long path (SplatNode is present)
        ArrayList<ArrayList<Nodes.Node>> groups = new ArrayList<>();
        ArrayList<Nodes.Node> current = new ArrayList<>();

        for (Nodes.Node node : nodes) {
            if (node instanceof Nodes.SplatNode) {
                if (!current.isEmpty()) {
                    groups.add(current);
                    current = new ArrayList<>();
                }

                ArrayList<Nodes.Node> single = new ArrayList<>();
                single.add(node);
                groups.add(single);
            } else {
                current.add(node);
            }
        }

        if (!current.isEmpty()) {
            groups.add(current);
        }

        final RubyNode rubyNode;
        final ArrayList<Nodes.Node> lastGroup = groups.get(groups.size() - 1);
        final boolean singleElementInTail = groups.size() > 1 && lastGroup.size() == 1 &&
                !(lastGroup.get(0) instanceof Nodes.SplatNode);

        if (!singleElementInTail) {
            var arraysToConcat = new RubyNode[groups.size()];
            int i = 0;

            for (var group : groups) {
                if (group.size() == 1 && group.get(0) instanceof Nodes.SplatNode splatNode) {
                    arraysToConcat[i++] = splatNode.accept(this);
                } else {
                    RubyNode[] rubyNodes = new RubyNode[group.size()];
                    int j = 0;
                    for (var e : group) {
                        rubyNodes[j++] = e.accept(this);
                    }

                    final RubyNode arrayLiteralNode = ArrayLiteralNode.create(language, rubyNodes);
                    arraysToConcat[i++] = arrayLiteralNode;
                }
            }

            if (arraysToConcat.length == 1) {
                rubyNode = arraysToConcat[0];
            } else {
                rubyNode = new ArrayConcatNode(arraysToConcat);
            }
        } else {
            var arraysToConcat = new RubyNode[groups.size() - 1];

            // TODO: don't duplicate this code chunk
            //       the only difference here - we ignore the last groups' element
            for (int i = 0; i < groups.size() - 1; i++) {
                var group = groups.get(i);

                if (group.size() == 1 && group.get(0) instanceof Nodes.SplatNode splatNode) {
                    arraysToConcat[i] = splatNode.accept(this);
                } else {
                    RubyNode[] rubyNodes = new RubyNode[group.size()];
                    int j = 0;
                    for (var e : group) {
                        rubyNodes[j++] = e.accept(this);
                    }

                    final RubyNode arrayLiteralNode = ArrayLiteralNode.create(language, rubyNodes);
                    arraysToConcat[i] = arrayLiteralNode;
                }
            }

            final RubyNode arrayToAppendTo;
            if (arraysToConcat.length == 1) {
                arrayToAppendTo = arraysToConcat[0];
            } else {
                arrayToAppendTo = new ArrayConcatNode(arraysToConcat);
            }

            final RubyNode value = groups.get(groups.size() - 1).get(0).accept(this);

            // NOTE: actually ArrayAppendOneNodeGen seems wierd here - why not just simple Array with 1 element?
            rubyNode = ArrayAppendOneNodeGen.create(
                    KernelNodesFactory.DupASTNodeFactory.create(arrayToAppendTo), // TODO: duplication is not needed
                    value);
        }

        return rubyNode;
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

        final RubyNode rubyNode = translateExpressionsList(values);

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
            boolean conditionInversed, boolean evaluateConditionBeforeBody) {
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

        // in case of `begin ... end while ()`
        // the begin/end block is executed before condition
        if (evaluateConditionBeforeBody) {
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
                node instanceof Nodes.IntegerNode ||
                node instanceof Nodes.FloatNode ||
                node instanceof Nodes.ImaginaryNode ||
                node instanceof Nodes.RationalNode ||
                node instanceof Nodes.SelfNode ||
                node instanceof Nodes.TrueNode ||
                node instanceof Nodes.FalseNode ||
                node instanceof Nodes.NilNode;
    }

    private String toString(byte[] bytes) {
        return TStringUtils.toJavaStringOrThrow(
                TruffleString.fromByteArrayUncached(bytes, sourceEncoding.tencoding, false), sourceEncoding);
    }

    protected String toString(Nodes.Node node) {
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
