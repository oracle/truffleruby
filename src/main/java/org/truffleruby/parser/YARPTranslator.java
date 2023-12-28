/*
 * Copyright (c) 2022, 2024 Oracle and/or its affiliates. All rights reserved. This
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
import org.jcodings.specific.EUCJPEncoding;
import org.jcodings.specific.Windows_31JEncoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.annotations.Split;
import org.truffleruby.builtins.PrimitiveNodeConstructor;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.IsNilNode;
import org.truffleruby.core.array.ArrayConcatNode;
import org.truffleruby.core.array.ArrayLiteralNode;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.AssignableNode;
import org.truffleruby.core.cast.HashCastNodeGen;
import org.truffleruby.core.cast.SplatCastNode;
import org.truffleruby.core.cast.SplatCastNodeGen;
import org.truffleruby.core.cast.StringToSymbolNodeGen;
import org.truffleruby.core.cast.ToProcNodeGen;
import org.truffleruby.core.cast.ToSNode;
import org.truffleruby.core.cast.ToSNodeGen;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;
import org.truffleruby.core.hash.ConcatHashLiteralNode;
import org.truffleruby.core.hash.HashLiteralNode;
import org.truffleruby.core.module.ModuleNodes;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.range.RangeNodesFactory;
import org.truffleruby.core.range.RubyIntRange;
import org.truffleruby.core.range.RubyLongRange;
import org.truffleruby.core.regexp.InterpolatedRegexpNode;
import org.truffleruby.core.regexp.MatchDataNodes;
import org.truffleruby.core.regexp.RegexpOptions;
import org.truffleruby.core.regexp.RubyRegexp;
import org.truffleruby.core.rescue.AssignRescueVariableNode;
import org.truffleruby.core.string.ConvertBytes;
import org.truffleruby.core.string.FrozenStrings;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.core.string.InterpolatedStringNode;
import org.truffleruby.core.string.KCode;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.debug.ChaosNode;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.RubyTopLevelRootNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.arguments.ArgumentsDescriptor;
import org.truffleruby.language.arguments.KeywordArgumentsDescriptorManager;
import org.truffleruby.language.arguments.NoKeywordArgumentsDescriptor;
import org.truffleruby.language.arguments.ProfileArgumentNodeGen;
import org.truffleruby.language.arguments.ReadSelfNode;
import org.truffleruby.language.constants.ReadConstantNode;
import org.truffleruby.language.constants.ReadConstantWithDynamicScopeNode;
import org.truffleruby.language.constants.ReadConstantWithLexicalScopeNode;
import org.truffleruby.language.constants.WriteConstantNode;
import org.truffleruby.language.control.AndNodeGen;
import org.truffleruby.language.control.BreakNode;
import org.truffleruby.language.control.DeferredRaiseException;
import org.truffleruby.language.control.DynamicReturnNode;
import org.truffleruby.language.control.FrameOnStackNode;
import org.truffleruby.language.control.IfElseNode;
import org.truffleruby.language.control.IfElseNodeGen;
import org.truffleruby.language.control.IfNodeGen;
import org.truffleruby.language.control.InvalidReturnNode;
import org.truffleruby.language.control.LocalReturnNode;
import org.truffleruby.language.control.NextNode;
import org.truffleruby.language.control.NotNodeGen;
import org.truffleruby.language.control.OnceNode;
import org.truffleruby.language.control.OrLazyValueDefinedNodeGen;
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
import org.truffleruby.language.defined.DefinedWrapperNode;
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
import org.truffleruby.language.literal.LongFixnumLiteralNode;
import org.truffleruby.language.literal.NilLiteralNode;
import org.truffleruby.language.literal.ObjectClassLiteralNode;
import org.truffleruby.language.literal.ObjectLiteralNode;
import org.truffleruby.language.literal.StringLiteralNode;
import org.truffleruby.language.literal.TruffleInternalModuleLiteralNode;
import org.truffleruby.language.locals.FindDeclarationVariableNodes.FrameSlotAndDepth;
import org.truffleruby.language.locals.FlipFlopNodeGen;
import org.truffleruby.language.locals.InitFlipFlopSlotNode;
import org.truffleruby.language.locals.ReadLocalNode;
import org.truffleruby.language.locals.WriteLocalNode;
import org.truffleruby.language.locals.WriteLocalVariableNode;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.methods.BlockDefinitionNode;
import org.truffleruby.language.methods.CachedLazyCallTargetSupplier;
import org.truffleruby.language.methods.CatchBreakNode;
import org.truffleruby.language.methods.LiteralMethodDefinitionNode;
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
import org.truffleruby.language.objects.SingletonClassNode.SingletonClassASTNode;
import org.truffleruby.language.objects.SingletonClassNodeGen.SingletonClassASTNodeGen;
import org.truffleruby.language.objects.WriteInstanceVariableNodeGen;
import org.truffleruby.language.objects.classvariables.ReadClassVariableNode;
import org.truffleruby.language.objects.classvariables.WriteClassVariableNode;
import org.prism.AbstractNodeVisitor;
import org.prism.Nodes;
import org.truffleruby.language.supercall.ReadSuperArgumentsNode;
import org.truffleruby.language.supercall.ReadZSuperArgumentsNode;
import org.truffleruby.language.supercall.SuperCallNode;
import org.truffleruby.language.supercall.ZSuperOutsideMethodNode;
import org.truffleruby.language.yield.YieldExpressionNode;
import org.truffleruby.parser.Translator.ArgumentsAndBlockTranslation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

import static org.truffleruby.parser.TranslatorEnvironment.DEFAULT_KEYWORD_REST_NAME;
import static org.truffleruby.parser.TranslatorEnvironment.FORWARDED_BLOCK_NAME;
import static org.truffleruby.parser.TranslatorEnvironment.FORWARDED_KEYWORD_REST_NAME;
import static org.truffleruby.parser.TranslatorEnvironment.FORWARDED_REST_NAME;

// NOTE: we should avoid SourceIndexLength in YARPTranslator, instead pass a Nodes.Node as location, because
// * it does not copy the newline flag properly,
// * it is inefficient,
// * there is typically no need for such an object since YARP location info is correct.

/** Translate (or convert) AST provided by a parser (YARP parser) to Truffle AST */
public class YARPTranslator extends AbstractNodeVisitor<RubyNode> {

    protected final RubyLanguage language;
    protected final TranslatorEnvironment environment;
    // TODO: Since these fields don't seem to change per translator instance we could/should store them in ParseEnvironment
    private final ParserContext parserContext;
    private final byte[] sourceBytes;
    private final Source source;
    private final Node currentNode;
    private final RubyEncoding sourceEncoding;

    public Deque<Integer> frameOnStackMarkerSlotStack = new ArrayDeque<>();

    public static final int NO_FRAME_ON_STACK_MARKER = Translator.NO_FRAME_ON_STACK_MARKER;

    public static final Nodes.Node[] EMPTY_NODE_ARRAY = Nodes.Node.EMPTY_ARRAY;
    public static final Nodes.ParametersNode ZERO_PARAMETERS_NODE = new Nodes.ParametersNode(EMPTY_NODE_ARRAY,
            EMPTY_NODE_ARRAY, null, EMPTY_NODE_ARRAY, EMPTY_NODE_ARRAY, null, null, 0, 0);

    public static final RescueNode[] EMPTY_RESCUE_NODE_ARRAY = new RescueNode[0];

    protected static final short NO_FLAGS = 0;

    private boolean translatingWhile = false;

    private boolean translatingNextExpression = false;
    @SuppressWarnings("unused") private boolean translatingForStatement = false;

    private static final String[] numberedParameterNames = {
            null,
            "_1",
            "_2",
            "_3",
            "_4",
            "_5",
            "_6",
            "_7",
            "_8",
            "_9"
    };

    // all the encountered BEGIN {} blocks
    private final ArrayList<RubyNode> beginBlocks = new ArrayList<>();

    public YARPTranslator(
            RubyLanguage language,
            TranslatorEnvironment environment,
            byte[] sourceBytes,
            Source source,
            ParserContext parserContext,
            Node currentNode) {
        this.language = language;
        this.environment = environment;
        this.sourceBytes = sourceBytes;
        this.source = source;
        this.parserContext = parserContext;
        this.currentNode = currentNode;
        this.sourceEncoding = Encodings.UTF_8; // TODO
    }

    public TranslatorEnvironment getEnvironment() {
        return environment;
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

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitAliasMethodNode(Nodes.AliasMethodNode node) {
        // expected InterpolatedSymbolNode (that should be evaluated in runtime)
        // or SymbolNode
        RubyNode rubyNode = new ModuleNodes.AliasKeywordNode(
                node.new_name.accept(this),
                node.old_name.accept(this));

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitAlternationPatternNode(Nodes.AlternationPatternNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitAndNode(Nodes.AndNode node) {
        RubyNode left = node.left.accept(this);
        RubyNode right = node.right.accept(this);

        RubyNode rubyNode = AndNodeGen.create(left, right);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitArgumentsNode(Nodes.ArgumentsNode node) {
        final Nodes.Node[] values = node.arguments;

        if (values.length == 1) {
            return values[0].accept(this);
        }

        final RubyNode[] translatedValues = translate(values);
        final RubyNode rubyNode = ArrayLiteralNode.create(language, translatedValues);

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitArrayNode(Nodes.ArrayNode node) {
        // handle splat operator properly (e.g [a, *b, c])
        final RubyNode rubyNode = translateExpressionsList(node.elements);

        // there are edge cases when node is already assigned a source section and flags (e.g. [*a])
        if (!rubyNode.hasSource()) {
            assignPositionAndFlags(node, rubyNode);
        }

        return rubyNode;
    }

    @Override
    public RubyNode visitArrayPatternNode(Nodes.ArrayPatternNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitBackReferenceReadNode(Nodes.BackReferenceReadNode node) {
        final RubyNode rubyNode = ReadGlobalVariableNodeGen.create(node.name);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitBeginNode(Nodes.BeginNode node) {
        RubyNode rubyNode;

        // empty begin/end block
        if (node.statements == null) {
            return new NilLiteralNode();
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
                            assignPositionAndFlags(splatNode, rescueNode);

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
                    assignPositionAndFlags(rescueClause, rescueNode);

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
            assignPositionAndFlags(node, rubyNode);
        }

        // with ensure section
        if (node.ensure_clause != null && node.ensure_clause.statements != null) {
            final RubyNode ensureBlock = node.ensure_clause.accept(this);
            rubyNode = EnsureNodeGen.create(rubyNode, ensureBlock);
            assignPositionOnly(node, rubyNode);
        }

        return rubyNode;
    }

    private RescueNode translateExceptionNodes(ArrayList<Nodes.Node> exceptionNodes, Nodes.RescueNode rescueClause) {

        final Nodes.Node[] exceptionNodesArray = exceptionNodes.toArray(EMPTY_NODE_ARRAY);
        final RubyNode[] handlingClasses = translate(exceptionNodesArray);

        RubyNode translatedBody;
        if (rescueClause.reference != null) {
            // We need to translate the reference before the statements,
            // because the statements can use the variable defined by the reference.
            final RubyNode exceptionWriteNode = translateRescueException(rescueClause.reference);
            var translatedStatements = translateNodeOrNil(rescueClause.statements);
            translatedBody = sequence(rescueClause,
                    Arrays.asList(exceptionWriteNode, translatedStatements));
        } else {
            translatedBody = translateNodeOrNil(rescueClause.statements);
        }

        final RescueNode rescueNode = new RescueClassesNode(handlingClasses, translatedBody);
        assignPositionOnly(exceptionNodesArray, rescueNode);
        return rescueNode;
    }

    @Override
    public RubyNode visitBlockLocalVariableNode(Nodes.BlockLocalVariableNode node) {
        return super.visitBlockLocalVariableNode(node);
    }

    @Override
    public RubyNode visitBlockNode(Nodes.BlockNode node) {
        throw CompilerDirectives.shouldNotReachHere(
                "BlockNode should be translated specially by its parent node to pass the method name to which the block is passed");
    }

    private RubyNode visitBlockNode(Nodes.BlockNode node, String literalBlockPassedToMethod) {
        return translateBlockAndLambda(node, node.parameters, node.body, node.locals, literalBlockPassedToMethod);
    }

    private RubyNode translateBlockAndLambda(Nodes.Node node, Nodes.Node parametersNode,
            Nodes.Node body, String[] locals, String literalBlockPassedToMethod) {
        final boolean isStabbyLambda = node instanceof Nodes.LambdaNode;

        // Unset this flag for a `for`-loop's block
        final boolean hasOwnScope = !translatingForStatement;

        TranslatorEnvironment methodParent = environment.getSurroundingMethodEnvironment();
        final String methodName = methodParent.getMethodName();

        final int blockDepth = environment.getBlockDepth() + 1;

        final Nodes.ParametersNode parameters;
        if (parametersNode instanceof Nodes.BlockParametersNode blockParameters) {
            parameters = blockParameters.parameters;
        } else if (parametersNode instanceof Nodes.NumberedParametersNode numberedParameters) {
            // build Nodes.BlockParametersNode with required parameters _1, _2, etc
            final int maximum = numberedParameters.maximum;
            final var requireds = new Nodes.RequiredParameterNode[maximum];

            for (int i = 1; i <= maximum; i++) {
                String name = numberedParameterNames[i];
                requireds[i - 1] = new Nodes.RequiredParameterNode(name, 0, 0);
            }

            parameters = new Nodes.ParametersNode(requireds, EMPTY_NODE_ARRAY, null, EMPTY_NODE_ARRAY,
                    EMPTY_NODE_ARRAY, null, null, 0, 0);
        } else if (parametersNode == null) {
            parameters = null;
        } else {
            throw CompilerDirectives.shouldNotReachHere();
        }

        final Arity arity = createArity(parameters);
        final ArgumentDescriptor[] argumentDescriptors = parametersNodeToArgumentDescriptors(parameters);

        // "block in foo"
        String originalName = SharedMethodInfo.getBlockName(blockDepth, methodName);
        // "block (2 levels) in M::C.foo"
        String parseName = SharedMethodInfo.getBlockName(blockDepth, methodParent.getSharedMethodInfo().getParseName());
        final SharedMethodInfo sharedMethodInfo = new SharedMethodInfo(
                getSourceSection(node),
                environment.getStaticLexicalScopeOrNull(),
                arity,
                originalName,
                blockDepth,
                parseName,
                methodName,
                argumentDescriptors);

        final ParseEnvironment parseEnvironment = environment.getParseEnvironment();
        // "stabby lambda" is a common name for the `->() {}` lambda syntax
        final ReturnID returnID = isStabbyLambda ? parseEnvironment.allocateReturnID() : environment.getReturnID();

        final TranslatorEnvironment newEnvironment = new TranslatorEnvironment(
                environment,
                parseEnvironment,
                returnID,
                hasOwnScope,
                false,
                sharedMethodInfo,
                environment.getMethodName(),
                blockDepth,
                parseEnvironment.allocateBreakID(),
                null,
                environment.modulePath);
        newEnvironment.literalBlockPassedToMethod = literalBlockPassedToMethod;

        final YARPBlockNodeTranslator methodCompiler = new YARPBlockNodeTranslator(
                language,
                newEnvironment,
                sourceBytes,
                source,
                parserContext,
                currentNode,
                arity);

        methodCompiler.frameOnStackMarkerSlotStack = frameOnStackMarkerSlotStack;

        final RubyNode rubyNode = methodCompiler.compileBlockNode(
                body,
                parameters,
                locals,
                isStabbyLambda,
                getSourceSection(node));

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitBlockArgumentNode(Nodes.BlockArgumentNode node) {
        final RubyNode rubyNode;
        final RubyNode valueNode;

        if (node.expression == null) {
            // def foo(&) a(&) end
            valueNode = environment.findLocalVarNode(FORWARDED_BLOCK_NAME, null);
            assert valueNode != null : "block forwarding local variable should be declared";
        } else {
            // a(&:b)
            valueNode = node.expression.accept(this);
        }

        rubyNode = ToProcNodeGen.create(valueNode);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
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
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitCallAndWriteNode(Nodes.CallAndWriteNode node) {
        // `a.b &&= value` is translated into `a.b || a.b = value`
        // receiver (a) should be executed only once that's why it's cached into a local variable

        assert node.receiver != null; // without receiver `a &&= b` leads to Nodes.LocalVariableAndWriteNode

        final var receiverExpression = new YARPExecutedOnceExpression("value", node.receiver, this);
        final var writeReceiverNode = receiverExpression.getWriteNode();
        final var readReceiver = receiverExpression.getReadYARPNode();

        // Use Prism nodes and rely on CallNode translation to automatically set RubyCallNode attributes.
        // Prism doesn't set ATTRIBUTE_WRITE flag, so we should add it manually
        // safe navigation flag is handled separately, so as optimisation remove it from the flags
        short writeFlags = (short) (node.flags | Nodes.CallNodeFlags.ATTRIBUTE_WRITE);
        writeFlags = (short) (writeFlags & ~Nodes.CallNodeFlags.SAFE_NAVIGATION);
        short readFlags = (short) (node.flags & ~Nodes.CallNodeFlags.SAFE_NAVIGATION);
        final RubyNode readNode = callNode(node, readFlags, readReceiver, node.read_name, Nodes.Node.EMPTY_ARRAY)
                .accept(this);
        final RubyNode writeNode = callNode(node, writeFlags, readReceiver, node.write_name,
                node.value).accept(this);
        final RubyNode andNode = AndNodeGen.create(readNode, writeNode);

        final RubyNode sequence;
        if (node.isSafeNavigation()) {
            // immediately return `nil` if receiver is `nil`
            final RubyNode unlessNode = UnlessNodeGen.create(new IsNilNode(receiverExpression.getReadNode()),
                    andNode);
            sequence = sequence(Arrays.asList(writeReceiverNode, unlessNode));
        } else {
            sequence = sequence(Arrays.asList(writeReceiverNode, andNode));
        }

        final RubyNode rubyNode = new DefinedWrapperNode(language.coreStrings.ASSIGNMENT, sequence);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitCallNode(Nodes.CallNode node) {
        var methodName = node.name;
        var receiver = node.receiver == null ? new SelfNode() : node.receiver.accept(this);

        var argumentsAndBlock = translateArgumentsAndBlock(node.arguments, node.block, methodName);
        var translatedArguments = argumentsAndBlock.arguments();

        if (environment.getParseEnvironment().inCore() && node.isVariableCall() && methodName.equals("undefined")) {
            // translate undefined
            final RubyNode rubyNode = new ObjectLiteralNode(NotProvided.INSTANCE);
            return assignPositionAndFlags(node, rubyNode);
        }

        if (node.receiver instanceof Nodes.StringNode stringNode &&
                (methodName.equals("freeze") || methodName.equals("-@") || methodName.equals("dedup"))) {
            final TruffleString tstring = TStringUtils.fromByteArray(stringNode.unescaped, sourceEncoding);
            final ImmutableRubyString frozenString = language.getFrozenStringLiteral(tstring, sourceEncoding);
            final RubyNode rubyNode = new FrozenStringLiteralNode(frozenString, FrozenStrings.METHOD);

            return assignPositionAndFlags(node, rubyNode);
        }

        // Translates something that looks like
        //   Primitive.foo arg1, arg2, argN
        // into
        //   FooPrimitiveNode(arg1, arg2, ..., argN)
        if (environment.getParseEnvironment().canUsePrimitives() &&
                node.receiver instanceof Nodes.ConstantReadNode constantReadNode &&
                constantReadNode.name.equals("Primitive")) {

            final PrimitiveNodeConstructor constructor = language.primitiveManager.getPrimitive(methodName);
            // TODO: avoid SourceIndexLength
            final SourceIndexLength sourceSection = new SourceIndexLength(node.startOffset, node.length);
            return constructor.createInvokePrimitiveNode(source, sourceSection, translatedArguments);
        }

        final var callNodeParameters = new RubyCallNodeParameters(
                receiver,
                methodName,
                argumentsAndBlock.block(),
                argumentsAndBlock.argumentsDescriptor(),
                translatedArguments,
                argumentsAndBlock.isSplatted(),
                node.isIgnoreVisibility(),
                node.isVariableCall(),
                node.isSafeNavigation(),
                node.isAttributeWrite());
        final RubyNode callNode = language.coreMethodAssumptions.createCallNode(callNodeParameters);

        final var rubyNode = wrapCallWithLiteralBlock(argumentsAndBlock, callNode);
        return assignPositionAndFlags(node, rubyNode);
    }

    private static RubyNode wrapCallWithLiteralBlock(ArgumentsAndBlockTranslation argumentsAndBlock,
            RubyNode callNode) {
        // wrap call node with literal block
        if (argumentsAndBlock.block() instanceof BlockDefinitionNode blockDef) {
            // if we have a literal block, `break` breaks out of this call site
            final var frameOnStackNode = new FrameOnStackNode(callNode, argumentsAndBlock.frameOnStackMarkerSlot());
            return new CatchBreakNode(blockDef.getBreakID(), frameOnStackNode, false);
        } else {
            return callNode;
        }
    }

    private ArgumentsAndBlockTranslation translateArgumentsAndBlock(Nodes.ArgumentsNode argumentsNode, Nodes.Node block,
            String methodName) {
        Nodes.Node[] arguments;
        if (argumentsNode == null) {
            arguments = EMPTY_NODE_ARRAY;
        } else {
            arguments = argumentsNode.arguments;
        }

        boolean isForwardArguments = (arguments.length > 0 &&
                ArrayUtils.getLast(arguments) instanceof Nodes.ForwardingArgumentsNode);

        if (isForwardArguments) {
            // use depth = 0 as far as it's ignored
            final var readRest = new Nodes.LocalVariableReadNode(FORWARDED_REST_NAME, 0, 0, 0);
            final var readKeyRest = new Nodes.LocalVariableReadNode(FORWARDED_KEYWORD_REST_NAME, 0, 0, 0);

            final var splat = new Nodes.SplatNode(readRest, 0, 0);
            final var keywordHash = new Nodes.KeywordHashNode((short) 0,
                    new Nodes.Node[]{new Nodes.AssocSplatNode(readKeyRest, 0, 0)}, 0, 0);

            // replace '...' argument with rest and keyrest arguments
            final var forwarding = new Nodes.Node[arguments.length + 1];
            System.arraycopy(arguments, 0, forwarding, 0, arguments.length - 1);
            forwarding[forwarding.length - 2] = splat;
            forwarding[forwarding.length - 1] = keywordHash;

            arguments = forwarding;
        }

        // should be after handling of forward-argument as far as ... means there is a splatted argument
        boolean isSplatted = containYARPSplatNode(arguments);
        var argumentsDescriptor = getKeywordArgumentsDescriptor(arguments);

        final RubyNode[] translatedArguments;
        if (isSplatted) {
            translatedArguments = new RubyNode[]{ translateExpressionsList(arguments) };
        } else {
            translatedArguments = translate(arguments);
        }

        // No need to copy the array for call(*splat), the elements will be copied to the frame arguments
        if (isSplatted && translatedArguments.length == 1 &&
                translatedArguments[0] instanceof SplatCastNode splatNode) {
            splatNode.doNotCopy();
        }

        final RubyNode blockNode;
        final int frameOnStackMarkerSlot;
        if (block != null) {
            if (block instanceof Nodes.BlockNode b) {
                // a() {}
                frameOnStackMarkerSlot = environment.declareLocalTemp("frame_on_stack_marker");
                frameOnStackMarkerSlotStack.push(frameOnStackMarkerSlot);
                try {
                    blockNode = visitBlockNode(b, methodName);
                } finally {
                    frameOnStackMarkerSlotStack.pop();
                }
            } else if (block instanceof Nodes.BlockArgumentNode blockArgument) {
                // a(&:b)
                blockNode = blockArgument.accept(this);
                frameOnStackMarkerSlot = NO_FRAME_ON_STACK_MARKER;
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }
        } else if (isForwardArguments) {
            // a(...)
            // use depth = 0 as far as it's ignored
            final var readBlock = new Nodes.LocalVariableReadNode(FORWARDED_BLOCK_NAME, 0, 0, 0);
            final var readBlockNode = readBlock.accept(this);
            blockNode = ToProcNodeGen.create(readBlockNode);
            frameOnStackMarkerSlot = NO_FRAME_ON_STACK_MARKER;
        } else {
            blockNode = null;
            frameOnStackMarkerSlot = NO_FRAME_ON_STACK_MARKER;
        }

        return new ArgumentsAndBlockTranslation(blockNode, translatedArguments, isSplatted, argumentsDescriptor,
                frameOnStackMarkerSlot);
    }

    private ArgumentsDescriptor getKeywordArgumentsDescriptor(Nodes.Node[] arguments) {
        if (arguments.length == 0) {
            return NoKeywordArgumentsDescriptor.INSTANCE;
        }

        // consider there are keyword arguments if the last argument is either ... or a Hash
        Nodes.Node last = arguments[arguments.length - 1];

        // a(...) means there are potentially forwarded keyword arguments
        if (last instanceof Nodes.ForwardingArgumentsNode) {
            return language.keywordArgumentsDescriptorManager
                    .getArgumentsDescriptor(StringUtils.EMPTY_STRING_ARRAY);
        }

        if (!(last instanceof Nodes.KeywordHashNode keywords)) {
            return NoKeywordArgumentsDescriptor.INSTANCE;
        }

        final List<String> names = new ArrayList<>();
        boolean splat = false;          // splat operator, e.g. foo(a: 1, *h)
        boolean nonKeywordKeys = false; // not Symbol keys, e.g. foo("a" => 1)

        for (var n : keywords.elements) {
            if (n instanceof Nodes.AssocNode assoc && assoc.key instanceof Nodes.SymbolNode symbol) {
                names.add(toString(symbol.unescaped));
            } else if (n instanceof Nodes.AssocNode assoc && !(assoc.key instanceof Nodes.SymbolNode)) {
                nonKeywordKeys = true;
            } else if (n instanceof Nodes.AssocSplatNode) {
                splat = true;
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }
        }

        if (splat || nonKeywordKeys || !names.isEmpty()) {
            return language.keywordArgumentsDescriptorManager
                    .getArgumentsDescriptor(names.toArray(StringUtils.EMPTY_STRING_ARRAY));
        } else {
            return NoKeywordArgumentsDescriptor.INSTANCE;
        }
    }

    @Override
    public RubyNode visitCallOperatorWriteNode(Nodes.CallOperatorWriteNode node) {
        // e.g. `a.b += value` is translated into `a.b = a.b + value`,
        // receiver (a) should be executed only once - that's why it's cached in a local variable

        assert node.receiver != null; // without receiver `a += b` leads to Nodes.LocalVariableOperatorWriteNode

        final var receiverExpression = new YARPExecutedOnceExpression("value", node.receiver, this);
        final var writeReceiverNode = receiverExpression.getWriteNode();
        final var readReceiver = receiverExpression.getReadYARPNode();

        // Use Prism nodes and rely on CallNode translation to automatically set CallNode flags
        // safe navigation flag is handled separately, so as optimisation remove it from the flags
        short writeFlags = (short) (node.flags | Nodes.CallNodeFlags.ATTRIBUTE_WRITE);
        writeFlags = (short) (writeFlags & ~Nodes.CallNodeFlags.SAFE_NAVIGATION);
        short readFlags = (short) (node.flags & ~Nodes.CallNodeFlags.SAFE_NAVIGATION);
        final Nodes.Node read = callNode(node, readFlags, readReceiver, node.read_name, Nodes.Node.EMPTY_ARRAY);
        final Nodes.Node executeOperator = callNode(node, read, node.operator, node.value);
        final Nodes.Node write = callNode(node, writeFlags, readReceiver, node.write_name,
                executeOperator);

        final RubyNode writeNode = write.accept(this);
        final RubyNode rubyNode;

        if (node.isSafeNavigation()) {
            // immediately return `nil` if receiver is `nil`
            final RubyNode unlessNode = UnlessNodeGen.create(new IsNilNode(receiverExpression.getReadNode()),
                    writeNode);
            rubyNode = sequence(Arrays.asList(writeReceiverNode, unlessNode));
        } else {
            rubyNode = sequence(Arrays.asList(writeReceiverNode, writeNode));
        }

        // rubyNode may be already assigned source code in case writeReceiverNode is null
        return assignPositionAndFlagsIfMissing(node, rubyNode);
    }

    @Override
    public RubyNode visitCallOrWriteNode(Nodes.CallOrWriteNode node) {
        // `a.b ||= value` is translated into `a.b || a.b = value`
        // receiver (a) should be executed only once that's why it's cached into a local variable

        assert node.receiver != null; // without receiver `a ||= b` leads to Nodes.LocalVariableOrWriteNode

        final var receiverExpression = new YARPExecutedOnceExpression("value", node.receiver, this);
        final var writeReceiverNode = receiverExpression.getWriteNode();
        final var readReceiver = receiverExpression.getReadYARPNode();

        // Use Prism nodes and rely on CallNode translation to automatically set CallNode flags
        // Prism doesn't set ATTRIBUTE_WRITE flag, so we should add it manually
        // safe navigation flag is handled separately, so as optimisation remove it from the flags
        short writeFlags = (short) (node.flags | Nodes.CallNodeFlags.ATTRIBUTE_WRITE);
        writeFlags = (short) (writeFlags & ~Nodes.CallNodeFlags.SAFE_NAVIGATION);
        short readFlags = (short) (node.flags & ~Nodes.CallNodeFlags.SAFE_NAVIGATION);
        final RubyNode readNode = callNode(node, readFlags, readReceiver, node.read_name, Nodes.Node.EMPTY_ARRAY)
                .accept(this);
        final RubyNode writeNode = callNode(node, writeFlags, readReceiver, node.write_name,
                node.value).accept(this);
        final RubyNode orNode = OrNodeGen.create(readNode, writeNode);

        final RubyNode sequence;
        if (node.isSafeNavigation()) {
            // return `nil` if receiver is `nil`
            final RubyNode unlessNode = UnlessNodeGen.create(new IsNilNode(receiverExpression.getReadNode()),
                    orNode);
            sequence = sequence(Arrays.asList(writeReceiverNode, unlessNode));
        } else {
            sequence = sequence(Arrays.asList(writeReceiverNode, orNode));
        }

        final RubyNode rubyNode = new DefinedWrapperNode(language.coreStrings.ASSIGNMENT, sequence);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitCallTargetNode(Nodes.CallTargetNode node) {
        // extra argument should be added before node translation
        // to trigger correctly replacement with inlined nodes (e.g. InlinedIndexSetNodeGen)
        // that relies on arguments count
        assert node.name.endsWith("=");

        final Nodes.Node[] arguments = { new Nodes.NilNode(0, 0) };
        final var argumentsNode = new Nodes.ArgumentsNode(NO_FLAGS, arguments, 0, 0);

        // Prism may set SAFE_NAVIGATION flag
        short flags = (short) (node.flags | Nodes.CallNodeFlags.ATTRIBUTE_WRITE);

        final var callNode = new Nodes.CallNode(flags, node.receiver, node.name, argumentsNode, null,
                node.startOffset, node.length);
        return callNode.accept(this);
    }

    @Override
    public RubyNode visitCapturePatternNode(Nodes.CapturePatternNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitCaseMatchNode(Nodes.CaseMatchNode node) {
        return super.visitCaseMatchNode(node);
    }

    @Override
    public RubyNode visitCaseNode(Nodes.CaseNode node) {
        // There are two sorts of case
        // - one compares a list of expressions against a value,
        // - the other just checks a list of expressions for truth.

        final RubyNode rubyNode;

        if (node.predicate != null) {
            // Evaluate the case expression and store it in a local
            final int tempSlot = environment.declareLocalTemp("case");
            final ReadLocalNode readTemp = environment.readNode(tempSlot, null);
            final RubyNode assignTemp = readTemp.makeWriteNode(node.predicate.accept(this));

            // Build an if expression from `when` and `else` branches.
            // Work backwards to make the first if contain all the others in its `else` clause.
            RubyNode elseNode = translateNodeOrNil(node.consequent);

            final Nodes.Node[] conditions = node.conditions;

            for (int n = conditions.length - 1; n >= 0; n--) {
                // condition is either WhenNode or InNode
                // don't handle InNode for now
                assert conditions[n] instanceof Nodes.WhenNode;

                final Nodes.WhenNode when = (Nodes.WhenNode) conditions[n];
                final Nodes.Node[] whenConditions = when.conditions;
                boolean containSplatOperator = containYARPSplatNode(whenConditions);

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
                    // translate `when` with multiple expressions into a single `if` operator, e.g.
                    //   case x
                    //     when a, b, c
                    //  is translated into
                    //    if x === a || x === b || x === c

                    RubyNode predicateNode = null;

                    for (var whenCondition : whenConditions) {
                        final RubyNode receiver = whenCondition.accept(this);
                        final RubyNode[] arguments = new RubyNode[]{ NodeUtil.cloneNode(readTemp) };
                        final RubyNode nextPredicateNode = createCallNode(receiver, "===", arguments);

                        if (predicateNode == null) {
                            predicateNode = nextPredicateNode;
                        } else {
                            predicateNode = OrNodeGen.create(predicateNode, nextPredicateNode);
                        }
                    }

                    // create `if` node
                    final RubyNode thenNode = translateNodeOrNil(when.statements);
                    final IfElseNode ifNode = IfElseNodeGen.create(predicateNode, thenNode, elseNode);

                    // this `if` becomes `else` branch of the outer `if`
                    elseNode = ifNode;
                }
            }

            final RubyNode ifNode = elseNode;

            // A top-level block assigns the temp then runs the `if`
            rubyNode = sequence(Arrays.asList(assignTemp, ifNode));
        } else {
            // Build an if expression from `when` and `else` branches.
            // Work backwards to make the first if contain all the others in its `else` clause.

            RubyNode elseNode = translateNodeOrNil(node.consequent);

            final Nodes.Node[] conditions = node.conditions;

            for (int n = node.conditions.length - 1; n >= 0; n--) {
                // condition is either WhenNode or InNode
                // don't handle InNode for now
                assert conditions[n] instanceof Nodes.WhenNode;

                final Nodes.WhenNode when = (Nodes.WhenNode) conditions[n];
                final Nodes.Node[] whenConditions = when.conditions;
                boolean containSplatOperator = containYARPSplatNode(whenConditions);

                if (!containSplatOperator) {
                    // translate `when` with multiple expressions into a single `if` operator, e.g.
                    //   case
                    //     when a, b, c
                    //  is translated into
                    //    if a || b || c

                    RubyNode predicateNode = null;

                    for (var whenCondition : whenConditions) {
                        final RubyNode nextPredicateNode = whenCondition.accept(this);

                        if (predicateNode == null) {
                            predicateNode = nextPredicateNode;
                        } else {
                            predicateNode = OrNodeGen.create(predicateNode, nextPredicateNode);
                        }
                    }

                    // create `if` node
                    final RubyNode thenNode = translateNodeOrNil(when.statements);
                    final IfElseNode ifNode = IfElseNodeGen.create(predicateNode, thenNode, elseNode);

                    // this `if` becomes `else` branch of the outer `if`
                    elseNode = ifNode;
                } else {
                    // use Array#any? to check whether there is any truthy value
                    // whenConditions are translated into an array-producing node
                    // so `when a, *b, c` is translated into `[a, *b, c].any?`
                    final RubyNode whenConditionNode = translateExpressionsList(whenConditions);
                    final RubyNode receiver = whenConditionNode;
                    final RubyNode predicateNode = createCallNode(receiver, "any?", RubyNode.EMPTY_ARRAY);

                    // create `if` node
                    final RubyNode thenNode = translateNodeOrNil(when.statements);
                    final IfElseNode ifNode = IfElseNodeGen.create(predicateNode, thenNode, elseNode);

                    // this `if` becomes `else` branch of the outer `if`
                    elseNode = ifNode;
                }
            }

            rubyNode = elseNode;
        }

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
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

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitClassVariableAndWriteNode(Nodes.ClassVariableAndWriteNode node) {
        // `@@a &&= value` is translated into @@a && @@a = value`
        // don't check whether variable is defined so exception will be raised otherwise

        int startOffset = node.startOffset;
        int length = node.length;

        var writeNode = new Nodes.ClassVariableWriteNode(node.name, node.value, startOffset, length).accept(this);
        var readNode = new Nodes.ClassVariableReadNode(node.name, startOffset, length).accept(this);
        var andNode = AndNodeGen.create(readNode, writeNode);

        final RubyNode rubyNode = new DefinedWrapperNode(language.coreStrings.ASSIGNMENT, andNode);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitClassVariableOperatorWriteNode(Nodes.ClassVariableOperatorWriteNode node) {
        // e.g. `@@a += value` is translated into @@a = @@a + value`
        // don't check whether variable is initialized so exception will be raised otherwise

        int startOffset = node.startOffset;
        int length = node.length;
        var readNode = new Nodes.ClassVariableReadNode(node.name, startOffset, length);
        var desugared = new Nodes.ClassVariableWriteNode(node.name,
                callNode(node, readNode, node.operator, node.value), startOffset, length);
        return desugared.accept(this);
    }

    @Override
    public RubyNode visitClassVariableOrWriteNode(Nodes.ClassVariableOrWriteNode node) {
        // `@@a ||= value` is translated into (defined?(@@a) && @@a) || @@a = value`
        // so we check whether variable is defined and no exception will be raised otherwise

        int startOffset = node.startOffset;
        int length = node.length;

        var writeNode = new Nodes.ClassVariableWriteNode(node.name, node.value, startOffset, length).accept(this);
        var readNode = new Nodes.ClassVariableReadNode(node.name, startOffset, length).accept(this);
        var andNode = AndNodeGen.create(new DefinedNode(readNode), readNode);

        final RubyNode rubyNode = OrLazyValueDefinedNodeGen.create(andNode, writeNode);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitClassVariableReadNode(Nodes.ClassVariableReadNode node) {
        final RubyNode rubyNode = new ReadClassVariableNode(
                getLexicalScopeNode("class variable lookup", node),
                node.name);

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitClassVariableWriteNode(Nodes.ClassVariableWriteNode node) {
        final RubyNode rhs = node.value.accept(this);
        final RubyNode rubyNode = new WriteClassVariableNode(
                getLexicalScopeNode("set dynamic class variable", node),
                node.name,
                rhs);

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitClassVariableTargetNode(Nodes.ClassVariableTargetNode node) {
        final RubyNode rubyNode = new WriteClassVariableNode(
                getLexicalScopeNode("set dynamic class variable", node),
                node.name,
                null);

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitConstantAndWriteNode(Nodes.ConstantAndWriteNode node) {
        // `A &&= value` is translated into `A && A = value`
        // don't check whether constant is defined and so exception will be raised otherwise

        int startOffset = node.startOffset;
        int length = node.length;

        var readNode = new Nodes.ConstantReadNode(node.name, startOffset, length).accept(this);
        var writeNode = new Nodes.ConstantWriteNode(node.name, node.value, startOffset, length).accept(this);
        final RubyNode andNode = AndNodeGen.create(readNode, writeNode);

        final RubyNode rubyNode = new DefinedWrapperNode(language.coreStrings.ASSIGNMENT, andNode);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitConstantOperatorWriteNode(Nodes.ConstantOperatorWriteNode node) {
        // e.g. `A += value` is translated into A = A + value`
        // don't check whether constant is initialized so warnings will be emitted otherwise

        int startOffset = node.startOffset;
        int length = node.length;

        // Use Nodes.CallNode and translate it to produce inlined operator nodes
        final var readNode = new Nodes.ConstantReadNode(node.name, startOffset, length);
        final var operatorNode = callNode(node, readNode, node.operator, node.value);
        final var writeNode = new Nodes.ConstantWriteNode(node.name, operatorNode, startOffset, length);

        return writeNode.accept(this);
    }

    @Override
    public RubyNode visitConstantOrWriteNode(Nodes.ConstantOrWriteNode node) {
        // `A ||= value` is translated into `(defined?(A) && A) || A = value`
        // so we check whether constant is defined and no exception will be raised otherwise

        int startOffset = node.startOffset;
        int length = node.length;

        var writeNode = new Nodes.ConstantWriteNode(node.name, node.value, startOffset, length).accept(this);
        var readNode = new Nodes.ConstantReadNode(node.name, startOffset, length).accept(this);
        var andNode = AndNodeGen.create(new DefinedNode(readNode), readNode);

        final RubyNode rubyNode = OrLazyValueDefinedNodeGen.create(andNode, writeNode);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitConstantPathAndWriteNode(Nodes.ConstantPathAndWriteNode node) {
        // `A::B &&= value` is translated into `A::B && A::B = value`
        // don't check whether constant is defined and so exception will be raised otherwise
        // A module/class (A::) should be executed only once - that's why it is cached in a local variable.

        final Nodes.ConstantPathNode target; // use instead of node.target
        final RubyNode writeParentNode;

        if (node.target.parent != null) {
            // A::B &&= 1
            var parentExpression = new YARPExecutedOnceExpression("value", node.target.parent, this);
            Nodes.Node readParent = parentExpression.getReadYARPNode();
            target = new Nodes.ConstantPathNode(readParent, node.target.child, node.target.startOffset,
                    node.target.startOffset);

            writeParentNode = parentExpression.getWriteNode();
        } else {
            // ::A &&= 1
            target = node.target;
            writeParentNode = null;
        }

        var value = node.value.accept(this);

        var readNode = (ReadConstantNode) target.accept(this);
        var writeNode = (WriteConstantNode) readNode.makeWriteNode(value);
        final RubyNode andNode = AndNodeGen.create(readNode, writeNode);

        final RubyNode rubyNode;

        if (writeParentNode != null) {
            RubyNode sequence = sequence(Arrays.asList(writeParentNode, andNode));
            rubyNode = new DefinedWrapperNode(language.coreStrings.ASSIGNMENT, sequence);
        } else {
            rubyNode = new DefinedWrapperNode(language.coreStrings.ASSIGNMENT, andNode);
        }

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
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

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitConstantPathOperatorWriteNode(Nodes.ConstantPathOperatorWriteNode node) {
        // e.g. `A::B += value` is translated into A::B = A::B + value`
        // don't check whether constant is initialized so warnings will be emitted otherwise.
        // A module/class (A::) should be executed only once - that's why it is cached in a local variable.

        final Nodes.ConstantPathNode target; // use instead of node.target
        final RubyNode writeParentNode;

        if (node.target.parent != null) {
            // A::B += 1
            var parentExpression = new YARPExecutedOnceExpression("value", node.target.parent, this);
            Nodes.Node readParent = parentExpression.getReadYARPNode();
            target = new Nodes.ConstantPathNode(readParent, node.target.child, node.target.startOffset,
                    node.target.startOffset);

            writeParentNode = parentExpression.getWriteNode();
        } else {
            // ::A += 1
            target = node.target;
            writeParentNode = null;
        }

        int startOffset = node.startOffset;
        int length = node.length;

        // Use Nodes.CallNode and translate it to produce inlined operator nodes
        final var operatorNode = callNode(node, target, node.operator, node.value);
        final var writeNode = new Nodes.ConstantPathWriteNode(target, operatorNode, startOffset, length);

        final RubyNode rubyNode;

        if (writeParentNode != null) {
            rubyNode = sequence(Arrays.asList(writeParentNode, writeNode.accept(this)));
            // rubyNode may be already assigned source code in case writeParentNode is null
            assignPositionAndFlagsIfMissing(node, rubyNode);
        } else {
            rubyNode = writeNode.accept(this);
        }

        return rubyNode;
    }

    @Override
    public RubyNode visitConstantPathOrWriteNode(Nodes.ConstantPathOrWriteNode node) {
        // `A::B ||= value` is translated into `(defined?(A::B) && A::B) || A::B = value`
        // check whether constant is defined so no exception will be raised otherwise
        // A module/class (A::) should be executed only once - that's why it is cached in a local variable.

        final Nodes.ConstantPathNode target; // use instead of node.target
        final RubyNode writeParentNode;

        if (node.target.parent != null) {
            // A::B ||= 1
            var parentExpression = new YARPExecutedOnceExpression("value", node.target.parent, this);
            Nodes.Node readParent = parentExpression.getReadYARPNode();
            target = new Nodes.ConstantPathNode(readParent, node.target.child, node.target.startOffset,
                    node.target.startOffset);

            writeParentNode = parentExpression.getWriteNode();
        } else {
            // ::A ||= 1
            target = node.target;
            writeParentNode = null;
        }

        var value = node.value.accept(this);

        var readNode = (ReadConstantNode) target.accept(this);
        var writeNode = (WriteConstantNode) readNode.makeWriteNode(value);
        final RubyNode orNode = OrNodeGen.create(readNode, writeNode);

        final RubyNode rubyNode;

        if (writeParentNode != null) {
            RubyNode sequence = sequence(Arrays.asList(writeParentNode, orNode));
            rubyNode = new DefinedWrapperNode(language.coreStrings.ASSIGNMENT, sequence);
        } else {
            rubyNode = new DefinedWrapperNode(language.coreStrings.ASSIGNMENT, orNode);
        }

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
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

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
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

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
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

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitConstantWriteNode(Nodes.ConstantWriteNode node) {
        final RubyNode value = node.value.accept(this);
        final RubyNode moduleNode = getLexicalScopeModuleNode("set dynamic constant", node);
        final RubyNode rubyNode = new WriteConstantNode(node.name, moduleNode, value);

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitConstantTargetNode(Nodes.ConstantTargetNode node) {
        final RubyNode moduleNode = getLexicalScopeModuleNode("set dynamic constant", node);
        final RubyNode rubyNode = new WriteConstantNode(node.name, moduleNode, null);

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitDefNode(Nodes.DefNode node) {
        final SingletonClassASTNode singletonClassNode;

        if (node.receiver != null) {
            final RubyNode receiver = node.receiver.accept(this);
            singletonClassNode = SingletonClassASTNodeGen.create(receiver);
        } else {
            singletonClassNode = null;
        }

        final Arity arity = createArity(node.parameters);
        final ArgumentDescriptor[] argumentDescriptors = parametersNodeToArgumentDescriptors(node.parameters);
        final boolean isReceiverSelf = node.receiver instanceof Nodes.SelfNode;

        final String parseName = modulePathAndMethodName(node.name, node.receiver != null, isReceiverSelf);

        final SharedMethodInfo sharedMethodInfo = new SharedMethodInfo(
                getSourceSection(node),
                environment.getStaticLexicalScopeOrNull(),
                arity,
                node.name,
                0,
                parseName,
                null,
                argumentDescriptors);

        final TranslatorEnvironment newEnvironment = new TranslatorEnvironment(
                environment,
                environment.getParseEnvironment(),
                environment.getParseEnvironment().allocateReturnID(),
                true,
                false,
                sharedMethodInfo,
                node.name,
                0,
                null,
                null,
                environment.modulePath);
        newEnvironment.parametersNode = node.parameters;

        final var defNodeTranslator = new YARPDefNodeTranslator(
                language,
                newEnvironment,
                sourceBytes,
                source,
                parserContext,
                currentNode);
        final CachedLazyCallTargetSupplier callTargetSupplier = defNodeTranslator.buildMethodNodeCompiler(node, arity);

        final boolean isDefSingleton = singletonClassNode != null;

        RubyNode rubyNode = new LiteralMethodDefinitionNode(
                singletonClassNode,
                node.name,
                sharedMethodInfo,
                isDefSingleton,
                callTargetSupplier);

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitDefinedNode(Nodes.DefinedNode node) {
        // Handle defined?(yield) explicitly otherwise it would raise SyntaxError
        if (node.value instanceof Nodes.YieldNode && isInvalidYield()) {
            final var nilNode = new NilLiteralNode();
            return assignPositionAndFlags(node, nilNode);
        }

        final RubyNode value = node.value.accept(this);
        final RubyNode rubyNode = new DefinedNode(value);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitElseNode(Nodes.ElseNode node) {
        if (node.statements == null) {
            final RubyNode rubyNode = new NilLiteralNode();
            return assignPositionAndFlags(node, rubyNode);
        }
        return node.statements.accept(this);
    }

    @Override
    public RubyNode visitEmbeddedStatementsNode(Nodes.EmbeddedStatementsNode node) {
        // empty interpolation expression, e.g. in "a #{} b"
        if (node.statements == null) {
            RubyNode rubyNode = new ObjectLiteralNode(
                    language.getFrozenStringLiteral(sourceEncoding.tencoding.getEmpty(), sourceEncoding));
            return assignPositionAndFlags(node, rubyNode);
        }

        return node.statements.accept(this);
    }

    @Override
    public RubyNode visitEmbeddedVariableNode(Nodes.EmbeddedVariableNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitEnsureNode(Nodes.EnsureNode node) {
        if (node.statements == null) {
            final RubyNode rubyNode = new NilLiteralNode();
            return assignPositionAndFlags(node, rubyNode);
        }

        return node.statements.accept(this);
    }

    @Override
    public RubyNode visitFalseNode(Nodes.FalseNode node) {
        RubyNode rubyNode = new BooleanLiteralNode(false);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitFindPatternNode(Nodes.FindPatternNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitFlipFlopNode(Nodes.FlipFlopNode node) {
        final RubyNode begin = node.left.accept(this);
        final RubyNode end = node.right.accept(this);

        final var slotAndDepth = createFlipFlopState();
        final RubyNode rubyNode = FlipFlopNodeGen.create(begin, end, node.isExcludeEnd(), slotAndDepth.depth,
                slotAndDepth.slot);

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
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
        return assignPositionAndFlags(node, rubyNode);
    }

    /** A Ruby for-loop, such as:
     *
     * <pre>
     * for x in y
     *     z = 0
     *     puts x
     * end
     * </pre>
     *
     * naively desugars to:
     *
     * <pre>
     * y.each do |x|
     *     z = 0
     *     puts x
     * end
     * </pre>
     *
     * The main difference is that z and x are always going to be local to the scope outside the block, so it's a bit
     * more like:
     *
     * <pre>
     * z = nil  #  unless z is already defined
     * x = nil  #  unless x is already defined
     *
     * y.each do |temp|
     *    x = temp
     *    z = 0
     *    puts x
     * end
     * </pre>
     *
     * Assigning x the temporary variable instead of defining x as a block parameter forces x to be defined in a proper
     * scope.
     *
     * It also handles cases when the expression assigned in the for could is index assignment (a[b] in []), attribute
     * assignment (a.b in []), multiple assignment (a, b, c in []), or whatever:
     *
     * <pre>
     * for x[0] in y
     *     puts x[0]
     * end
     * </pre>
     *
     * http://blog.grayproductions.net/articles/the_evils_of_the_for_loop
     * http://stackoverflow.com/questions/3294509/for-vs-each-in-ruby
     *
     * The other complication is that normal locals should be defined in the enclosing scope, unlike a normal block. We
     * do that by setting a flag on this translator object when we visit the new iter, translatingForStatement, which we
     * recognise when visiting a block node. */
    @Override
    public RubyNode visitForNode(Nodes.ForNode node) {
        final String parameterName = environment.allocateLocalTemp("for");

        final var requireds = new Nodes.Node[]{ new Nodes.RequiredParameterNode(parameterName, 0, 0) };
        final var parameters = new Nodes.ParametersNode(requireds, Nodes.Node.EMPTY_ARRAY, null, Nodes.Node.EMPTY_ARRAY,
                Nodes.Node.EMPTY_ARRAY, null, null, 0, 0);
        final var blockParameters = new Nodes.BlockParametersNode(parameters, Nodes.Node.EMPTY_ARRAY, 0, 0);

        final var readParameter = new Nodes.LocalVariableReadNode(parameterName, 0, 0, 0);
        final Nodes.Node writeIndex;

        // replace -Target node with a -Write one
        if (node.index instanceof Nodes.LocalVariableTargetNode target) {
            writeIndex = new Nodes.LocalVariableWriteNode(target.name, target.depth, readParameter, 0, 0);
        } else if (node.index instanceof Nodes.InstanceVariableTargetNode target) {
            writeIndex = new Nodes.InstanceVariableWriteNode(target.name, readParameter, 0, 0);
        } else if (node.index instanceof Nodes.ClassVariableTargetNode target) {
            writeIndex = new Nodes.ClassVariableWriteNode(target.name, readParameter, 0, 0);
        } else if (node.index instanceof Nodes.GlobalVariableTargetNode target) {
            writeIndex = new Nodes.GlobalVariableWriteNode(target.name, readParameter, 0, 0);
        } else if (node.index instanceof Nodes.ConstantTargetNode target) {
            writeIndex = new Nodes.ConstantWriteNode(target.name, readParameter, 0, 0);
        } else if (node.index instanceof Nodes.ConstantPathTargetNode target) {
            final var constantPath = new Nodes.ConstantPathNode(target.parent, target.child, 0, 0);
            writeIndex = new Nodes.ConstantPathWriteNode(constantPath, readParameter, 0, 0);
        } else if (node.index instanceof Nodes.CallTargetNode target) {
            final var arguments = new Nodes.ArgumentsNode(NO_FLAGS, new Nodes.Node[]{ readParameter }, 0, 0);

            // preserve target flags because they can contain SAFE_NAVIGATION flag
            short flags = (short) (target.flags | Nodes.CallNodeFlags.ATTRIBUTE_WRITE);

            writeIndex = new Nodes.CallNode(flags, target.receiver, target.name, arguments, null, 0, 0);
        } else if (node.index instanceof Nodes.IndexTargetNode target) {
            final Nodes.ArgumentsNode arguments;
            final Nodes.Node[] statements;

            if (target.arguments != null) {
                statements = new Nodes.Node[target.arguments.arguments.length + 1];
                System.arraycopy(target.arguments.arguments, 0, statements, 0, target.arguments.arguments.length);
            } else {
                statements = new Nodes.Node[1];
            }

            statements[statements.length - 1] = readParameter;
            arguments = new Nodes.ArgumentsNode(NO_FLAGS, statements, 0, 0);

            writeIndex = new Nodes.CallNode(target.flags, target.receiver, "[]=", arguments, target.block, 0, 0);
        } else if (node.index instanceof Nodes.MultiTargetNode target) {
            writeIndex = new Nodes.MultiWriteNode(target.lefts, target.rest, target.rights, readParameter, 0, 0);
        } else {
            throw CompilerDirectives.shouldNotReachHere("Unexpected node class for for-loop index");
        }

        final Nodes.Node body;

        if (node.statements != null) {
            Nodes.Node[] statements = new Nodes.Node[1 + node.statements.body.length];
            statements[0] = writeIndex;
            System.arraycopy(node.statements.body, 0, statements, 1, node.statements.body.length);

            body = new Nodes.StatementsNode(statements, node.statements.startOffset, node.statements.length);
        } else {
            // for loop with empty body
            var statements = new Nodes.Node[]{ writeIndex };
            body = new Nodes.StatementsNode(statements, 0, 0);
        }

        // in the block environment declare local variable only for parameter
        // and skip declaration all the local variables defined in the block
        String[] locals = new String[]{ parameterName };
        final var block = new Nodes.BlockNode(locals, 0, blockParameters, body, 0, 0);
        final var eachCall = new Nodes.CallNode(NO_FLAGS, node.collection, "each", null, block, node.startOffset,
                node.length);

        final RubyNode rubyNode;
        final boolean translatingForStatement = this.translatingForStatement;
        this.translatingForStatement = true;

        try {
            rubyNode = eachCall.accept(this);
        } finally {
            this.translatingForStatement = translatingForStatement;
        }

        return rubyNode;
    }

    @Override
    public RubyNode visitForwardingArgumentsNode(Nodes.ForwardingArgumentsNode node) {
        throw CompilerDirectives.shouldNotReachHere("handled in visitCallNode");
    }

    @Override
    public RubyNode visitForwardingSuperNode(Nodes.ForwardingSuperNode node) {
        var argumentsAndBlock = translateArgumentsAndBlock(null, node.block, environment.getMethodName());

        boolean insideDefineMethod = false;
        var environment = this.environment;
        while (environment.isBlock()) {
            if (Objects.equals(environment.literalBlockPassedToMethod, "define_method")) {
                insideDefineMethod = true;
            }
            environment = environment.getParent();
        }

        if (environment.isModuleBody()) {
            return assignPositionAndFlags(node, new ZSuperOutsideMethodNode(insideDefineMethod));
        }

        // TODO: could we use the ArgumentDescriptor[] stored in the SharedMethodInfo instead?
        var parametersNode = environment.parametersNode;
        if (parametersNode == null) {
            // parametersNode == null for a method means zero parameters: https://github.com/ruby/prism/issues/1915
            parametersNode = ZERO_PARAMETERS_NODE;
        }
        var reloadTranslator = new YARPReloadArgumentsTranslator(language, this, parametersNode);

        final RubyNode[] reloadSequence = reloadTranslator.reload(parametersNode);

        var descriptor = (parametersNode.keywords.length > 0 || parametersNode.keyword_rest != null)
                ? KeywordArgumentsDescriptorManager.EMPTY
                : NoKeywordArgumentsDescriptor.INSTANCE;
        final int restParamIndex = reloadTranslator.getRestParameterIndex();
        final RubyNode arguments = new ReadZSuperArgumentsNode(restParamIndex, reloadSequence);
        final RubyNode block = executeOrInheritBlock(argumentsAndBlock.block());
        final boolean isSplatted = reloadTranslator.getRestParameterIndex() != -1;

        RubyNode callNode = new SuperCallNode(isSplatted, arguments, block, descriptor);
        callNode = wrapCallWithLiteralBlock(argumentsAndBlock, callNode);

        return assignPositionAndFlags(node, callNode);
    }

    @Override
    public RubyNode visitGlobalVariableAndWriteNode(Nodes.GlobalVariableAndWriteNode node) {
        // `$a &&= value` is translated into `$a && $a = value`
        // don't check whether variable is defined so a warning will be emitted otherwise

        int startOffset = node.startOffset;
        int length = node.length;

        var writeNode = new Nodes.GlobalVariableWriteNode(node.name, node.value, startOffset, length).accept(this);
        var readNode = new Nodes.GlobalVariableReadNode(node.name, startOffset, length).accept(this);
        var andNode = AndNodeGen.create(readNode, writeNode);

        final RubyNode rubyNode = new DefinedWrapperNode(language.coreStrings.ASSIGNMENT, andNode);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitGlobalVariableOperatorWriteNode(Nodes.GlobalVariableOperatorWriteNode node) {
        // e.g. `$a += value` is translated into $a = $a + value`
        // don't check whether variable is initialized so exception will be raised otherwise

        int startOffset = node.startOffset;
        int length = node.length;
        var readNode = new Nodes.GlobalVariableReadNode(node.name, startOffset, length);
        var desugared = new Nodes.GlobalVariableWriteNode(node.name,
                callNode(node, readNode, node.operator, node.value), startOffset, length);
        return desugared.accept(this);
    }

    @Override
    public RubyNode visitGlobalVariableOrWriteNode(Nodes.GlobalVariableOrWriteNode node) {
        // `$a ||= value` is translated into `(defined?($a) && $a) || $a = value`
        // check whether variable is defined so no warnings will be emitted otherwise

        int startOffset = node.startOffset;
        int length = node.length;

        var writeNode = new Nodes.GlobalVariableWriteNode(node.name, node.value, startOffset, length).accept(this);
        var readNode = new Nodes.GlobalVariableReadNode(node.name, startOffset, length).accept(this);
        var definedCheck = AndNodeGen.create(new DefinedNode(readNode), readNode);

        final RubyNode rubyNode = OrLazyValueDefinedNodeGen.create(definedCheck, writeNode);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitGlobalVariableReadNode(Nodes.GlobalVariableReadNode node) {
        final RubyNode rubyNode = ReadGlobalVariableNodeGen.create(node.name);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitGlobalVariableWriteNode(Nodes.GlobalVariableWriteNode node) {
        final RubyNode value = node.value.accept(this);
        final RubyNode rubyNode = WriteGlobalVariableNodeGen.create(node.name, value);

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitGlobalVariableTargetNode(Nodes.GlobalVariableTargetNode node) {
        final RubyNode rubyNode = WriteGlobalVariableNodeGen.create(node.name, null);

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitHashNode(Nodes.HashNode node) {
        if (node.elements.length == 0) { // an empty Hash literal like h = {}
            final RubyNode rubyNode = HashLiteralNode.create(RubyNode.EMPTY_ARRAY, language);
            return assignPositionAndFlags(node, rubyNode);
        }

        final List<RubyNode> hashConcats = new ArrayList<>();
        final List<RubyNode> keyValues = new ArrayList<>();

        for (Nodes.Node pair : node.elements) {
            if (pair instanceof Nodes.AssocSplatNode assocSplatNode) {
                // This case is for splats {a: 1, **{b: 2}, c: 3}
                if (!keyValues.isEmpty()) {
                    final RubyNode hashLiteralSoFar = HashLiteralNode
                            .create(keyValues.toArray(RubyNode.EMPTY_ARRAY), language);
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

                if (assocNode.value instanceof Nodes.ImplicitNode implicit) {
                    if (implicit.value instanceof Nodes.CallNode call) {
                        // Prism doesn't set VARIABLE_CALL flag
                        int flags = call.flags | Nodes.CallNodeFlags.VARIABLE_CALL;
                        final var copy = new Nodes.CallNode((short) flags, call.receiver, call.name, call.arguments,
                                call.block, call.startOffset, call.length);

                        final RubyNode valueNode = copy.accept(this);
                        keyValues.add(valueNode);
                    } else if (implicit.value instanceof Nodes.LocalVariableReadNode localVariableRead) {
                        final RubyNode valueNode = localVariableRead.accept(this);
                        keyValues.add(valueNode);
                    } else {
                        throw CompilerDirectives.shouldNotReachHere();
                    }
                } else {
                    keyValues.add(assocNode.value.accept(this));
                }
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }
        }

        if (!keyValues.isEmpty()) {
            final RubyNode hashLiteralSoFar = HashLiteralNode.create(keyValues.toArray(RubyNode.EMPTY_ARRAY), language);
            hashConcats.add(hashLiteralSoFar);
        }

        if (hashConcats.size() == 1) {
            final RubyNode rubyNode = hashConcats.get(0);
            return assignPositionAndFlags(node, rubyNode);
        }

        final RubyNode rubyNode = new ConcatHashLiteralNode(hashConcats.toArray(RubyNode.EMPTY_ARRAY));
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitHashPatternNode(Nodes.HashPatternNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitIfNode(Nodes.IfNode node) {
        final RubyNode conditionNode = node.predicate.accept(this);
        final RubyNode thenNode = node.statements == null ? null : node.statements.accept(this);
        final RubyNode elseNode = node.consequent == null ? null : node.consequent.accept(this);
        final RubyNode rubyNode;

        if (thenNode != null && elseNode != null) {
            rubyNode = IfElseNodeGen.create(conditionNode, thenNode, elseNode);
            return assignPositionAndFlags(node, rubyNode);
        } else if (thenNode != null) {
            rubyNode = IfNodeGen.create(conditionNode, thenNode);
            return assignPositionAndFlags(node, rubyNode);
        } else if (elseNode != null) {
            rubyNode = UnlessNodeGen.create(conditionNode, elseNode);
            return assignPositionAndFlags(node, rubyNode);
        } else {
            // if (condition)
            // end
            return sequence(node, Arrays.asList(conditionNode, new NilLiteralNode()));
        }
    }

    @Override
    public RubyNode visitImaginaryNode(Nodes.ImaginaryNode node) {
        // Translate as Complex.convert(0, b) ignoring visibility

        final RubyNode objectClassNode = new ObjectClassLiteralNode();
        final ReadConstantNode complexModuleNode = new ReadConstantNode(objectClassNode, "Complex");
        final RubyNode realComponentNode = new IntegerFixnumLiteralNode(0);
        final RubyNode imaginaryComponentNode = node.numeric.accept(this);
        RubyNode[] arguments = new RubyNode[]{ realComponentNode, imaginaryComponentNode };

        RubyNode rubyNode = createCallNode(complexModuleNode, "convert", arguments);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitImplicitNode(Nodes.ImplicitNode node) {
        throw CompilerDirectives.shouldNotReachHere("handled in #visitHashNode");
    }

    @Override
    public RubyNode visitImplicitRestNode(Nodes.ImplicitRestNode node) {
        return super.visitImplicitRestNode(node);
    }

    @Override
    public RubyNode visitInNode(Nodes.InNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitIndexAndWriteNode(Nodes.IndexAndWriteNode node) {
        // arguments
        final Nodes.Node[] arguments;
        if (node.arguments != null) {
            arguments = node.arguments.arguments;
        } else {
            arguments = Nodes.Node.EMPTY_ARRAY;
        }

        final RubyNode rubyNode = translateIndexOrAndIndexAndWriteNodes(true, node.receiver, arguments, node.block,
                node.value, node.flags);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitIndexOperatorWriteNode(Nodes.IndexOperatorWriteNode node) {
        // e.g. `a[b] += value` is translated into `a[b] = a[b] + value`,

        // receiver (a) and arguments (b) should be executed only once -
        // that's why they are cached in local variables

        assert node.receiver != null;

        // receiver
        final var receiverExpression = new YARPExecutedOnceExpression("opelementassign", node.receiver, this);
        final var writeReceiverNode = receiverExpression.getWriteNode();
        final var readReceiver = receiverExpression.getReadYARPNode();

        // arguments
        final Nodes.Node[] arguments;
        if (node.arguments != null) {
            arguments = node.arguments.arguments;
        } else {
            arguments = Nodes.Node.EMPTY_ARRAY;
        }
        final int argumentsCount = arguments.length;

        // block argument
        final RubyNode writeBlockNode;
        final Nodes.BlockArgumentNode blockArgument;

        if (node.block != null) {
            var expression = new YARPExecutedOnceExpression("value", node.block, this);
            writeBlockNode = expression.getWriteNode();
            Nodes.Node readBlock = expression.getReadYARPNode();
            // imitate Nodes.CallNode structure with &block argument
            blockArgument = new Nodes.BlockArgumentNode(readBlock, 0, 0);
        } else {
            writeBlockNode = null;
            blockArgument = null;
        }

        final RubyNode[] writeArgumentsNodes = new RubyNode[argumentsCount];
        final Nodes.Node[] readArguments = new Nodes.Node[argumentsCount];
        for (int i = 0; i < argumentsCount; i++) {
            final var expression = new YARPExecutedOnceExpression("value", arguments[i], this);
            writeArgumentsNodes[i] = expression.getWriteNode();
            readArguments[i] = expression.getReadYARPNode();
        }

        final Nodes.Node read = new Nodes.CallNode(node.flags, readReceiver, "[]",
                new Nodes.ArgumentsNode(NO_FLAGS, readArguments, 0, 0), blockArgument, 0, 0);
        final Nodes.Node executeOperator = callNode(node, read, node.operator, node.value);

        final Nodes.Node[] readArgumentsAndResult = new Nodes.Node[argumentsCount + 1];
        System.arraycopy(readArguments, 0, readArgumentsAndResult, 0, argumentsCount);
        readArgumentsAndResult[argumentsCount] = executeOperator;

        short writeFlags = (short) (node.flags | Nodes.CallNodeFlags.ATTRIBUTE_WRITE);
        final Nodes.Node write = new Nodes.CallNode(writeFlags, readReceiver, "[]=",
                new Nodes.ArgumentsNode(NO_FLAGS, readArgumentsAndResult, 0, 0), blockArgument, 0, 0);
        final RubyNode writeNode = write.accept(this);
        final RubyNode writeArgumentsNode = sequence(Arrays.asList(writeArgumentsNodes));
        final RubyNode rubyNode;

        if (node.block != null) {
            // add block argument write node
            rubyNode = sequence(Arrays.asList(writeArgumentsNode, writeBlockNode, writeReceiverNode, writeNode));
        } else {
            rubyNode = sequence(Arrays.asList(writeArgumentsNode, writeReceiverNode, writeNode));
        }

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitIndexOrWriteNode(Nodes.IndexOrWriteNode node) {
        // arguments
        final Nodes.Node[] arguments;
        if (node.arguments != null) {
            arguments = node.arguments.arguments;
        } else {
            arguments = Nodes.Node.EMPTY_ARRAY;
        }

        final RubyNode rubyNode = translateIndexOrAndIndexAndWriteNodes(false, node.receiver, arguments, node.block,
                node.value, node.flags);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitIndexTargetNode(Nodes.IndexTargetNode node) {
        // extra argument should be added before node translation
        // to trigger correctly replacement with inlined nodes (e.g. InlinedIndexSetNodeGen)
        // that relies on arguments count

        final Nodes.Node[] arguments;
        final Nodes.ArgumentsNode argumentsNode;

        if (node.arguments == null) {
            arguments = new Nodes.Node[1];
        } else {
            arguments = new Nodes.Node[node.arguments.arguments.length + 1];
            for (int i = 0; i < node.arguments.arguments.length; i++) {
                arguments[i] = node.arguments.arguments[i];
            }
        }

        arguments[arguments.length - 1] = new Nodes.NilNode(0, 0);

        if (node.arguments == null) {
            argumentsNode = new Nodes.ArgumentsNode(NO_FLAGS, arguments, 0, 0);
        } else {
            argumentsNode = new Nodes.ArgumentsNode(node.arguments.flags, arguments, node.arguments.startOffset,
                    node.arguments.length);
        }

        short writeFlags = (short) (node.flags | Nodes.CallNodeFlags.ATTRIBUTE_WRITE);
        final var callNode = new Nodes.CallNode(writeFlags, node.receiver, "[]=", argumentsNode, node.block,
                node.startOffset,
                node.length);
        return callNode.accept(this);
    }


    private RubyNode translateIndexOrAndIndexAndWriteNodes(boolean isAndOperator, Nodes.Node receiver,
            Nodes.Node[] arguments, Nodes.Node block, Nodes.Node value, short flags) {
        // Handle both &&= and ||= operators:
        //   `a[b] ||= c` is translated into `a[b] || a[b] = c`
        //   `a[b] &&= c` is translated into `a[b] && a[b] = c`

        // receiver (a) and arguments (b) should be executed only once
        // that's why they are cached in local variables

        assert receiver != null;

        // receiver
        final var receiverExpression = new YARPExecutedOnceExpression("opelementassign", receiver, this);
        final var writeReceiverNode = receiverExpression.getWriteNode();
        final var readReceiver = receiverExpression.getReadYARPNode();

        final RubyNode[] writeArgumentsNodes = new RubyNode[arguments.length];
        final Nodes.Node[] readArguments = new Nodes.Node[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            final var expression = new YARPExecutedOnceExpression("value", arguments[i], this);
            writeArgumentsNodes[i] = expression.getWriteNode();
            readArguments[i] = expression.getReadYARPNode();
        }

        // block argument
        final RubyNode writeBlockNode;
        final Nodes.BlockArgumentNode blockArgument;

        if (block != null) {
            var expression = new YARPExecutedOnceExpression("value", block, this);
            writeBlockNode = expression.getWriteNode();
            Nodes.Node readBlock = expression.getReadYARPNode();
            // imitate Nodes.CallNode structure with &block argument
            blockArgument = new Nodes.BlockArgumentNode(readBlock, 0, 0);
        } else {
            writeBlockNode = null;
            blockArgument = null;
        }

        final Nodes.Node read = new Nodes.CallNode(flags, readReceiver, "[]",
                new Nodes.ArgumentsNode(NO_FLAGS, readArguments, 0, 0), blockArgument, 0, 0);
        final RubyNode readNode = read.accept(this);

        final Nodes.Node[] readArgumentsAndValue = new Nodes.Node[arguments.length + 1];
        System.arraycopy(readArguments, 0, readArgumentsAndValue, 0, arguments.length);
        readArgumentsAndValue[arguments.length] = value;

        short writeFlags = (short) (flags | Nodes.CallNodeFlags.ATTRIBUTE_WRITE);
        final Nodes.Node write = new Nodes.CallNode(writeFlags, readReceiver, "[]=",
                new Nodes.ArgumentsNode(NO_FLAGS, readArgumentsAndValue, 0, 0), blockArgument, 0, 0);
        final RubyNode writeNode = write.accept(this);

        final RubyNode operatorNode;
        if (isAndOperator) {
            operatorNode = AndNodeGen.create(readNode, writeNode);
        } else {
            operatorNode = OrNodeGen.create(readNode, writeNode);
        }

        final RubyNode writeArgumentsNode = sequence(Arrays.asList(writeArgumentsNodes));
        final RubyNode rubyNode;

        if (block != null) {
            // add block argument write node
            rubyNode = sequence(Arrays.asList(writeArgumentsNode, writeBlockNode, writeReceiverNode, operatorNode));
        } else {
            rubyNode = sequence(Arrays.asList(writeArgumentsNode, writeReceiverNode, operatorNode));
        }

        return rubyNode;
    }

    @Override
    public RubyNode visitInstanceVariableAndWriteNode(Nodes.InstanceVariableAndWriteNode node) {
        // `@a &&= value` is translated into `@a && @a = value`
        // don't check whether variable is initialized because even if an instance variable
        // is not set then it returns nil and does not have side effects (warnings or exceptions)

        int startOffset = node.startOffset;
        int length = node.length;

        var writeNode = new Nodes.InstanceVariableWriteNode(node.name, node.value, startOffset, length).accept(this);
        var readNode = new Nodes.InstanceVariableReadNode(node.name, startOffset, length).accept(this);
        var andNode = AndNodeGen.create(readNode, writeNode);

        final RubyNode rubyNode = new DefinedWrapperNode(language.coreStrings.ASSIGNMENT, andNode);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitInstanceVariableOperatorWriteNode(Nodes.InstanceVariableOperatorWriteNode node) {
        // e.g. `@a += value` is translated into @a = @a + value`
        // don't check whether variable is defined so exception will be raised otherwise

        int startOffset = node.startOffset;
        int length = node.length;

        var readNode = new Nodes.InstanceVariableReadNode(node.name, startOffset, length);
        var desugared = new Nodes.InstanceVariableWriteNode(node.name,
                callNode(node, readNode, node.operator, node.value), startOffset, length);
        return desugared.accept(this);
    }

    @Override
    public RubyNode visitInstanceVariableOrWriteNode(Nodes.InstanceVariableOrWriteNode node) {
        // `@a ||= value` is translated into `@a || @a = value`

        // No need to check `defined?(@ivar)` before reading, as `@ivar` even if not set returns nil and does not have
        // side effects (warnings or exceptions)

        int startOffset = node.startOffset;
        int length = node.length;

        var writeNode = new Nodes.InstanceVariableWriteNode(node.name, node.value, startOffset, length).accept(this);
        var readNode = new Nodes.InstanceVariableReadNode(node.name, startOffset, length).accept(this);

        final RubyNode rubyNode = OrLazyValueDefinedNodeGen.create(readNode, writeNode);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitInstanceVariableReadNode(Nodes.InstanceVariableReadNode node) {
        final RubyNode rubyNode = new ReadInstanceVariableNode(node.name);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitInstanceVariableWriteNode(Nodes.InstanceVariableWriteNode node) {
        final RubyNode value = node.value.accept(this);
        final RubyNode rubyNode = WriteInstanceVariableNodeGen.create(node.name, value);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitInstanceVariableTargetNode(Nodes.InstanceVariableTargetNode node) {
        final RubyNode rubyNode = WriteInstanceVariableNodeGen.create(node.name, null);

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitIntegerNode(Nodes.IntegerNode node) {
        final RubyNode rubyNode = translateIntegerLiteralString(toString(node));
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitInterpolatedMatchLastLineNode(Nodes.InterpolatedMatchLastLineNode node) {
        // replace regexp with /.../ =~ $_

        final var regexp = new Nodes.InterpolatedRegularExpressionNode(node.flags, node.parts, node.startOffset,
                node.length);
        final var regexpNode = regexp.accept(this);
        final var lastLineNode = ReadGlobalVariableNodeGen.create("$_");

        final RubyNode rubyNode = createCallNode(false, regexpNode, "=~", lastLineNode);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitInterpolatedRegularExpressionNode(Nodes.InterpolatedRegularExpressionNode node) {
        final ToSNode[] children = translateInterpolatedParts(node.parts);

        var encodingAndOptions = getRegexpEncodingAndOptions(new Nodes.RegularExpressionFlags(node.flags));

        RubyNode rubyNode = new InterpolatedRegexpNode(children, encodingAndOptions.options);

        if (node.isOnce()) {
            rubyNode = new OnceNode(rubyNode);
        }

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitInterpolatedStringNode(Nodes.InterpolatedStringNode node) {
        if (allPartsAreStringNodes(node)) {
            return visitStringNode(concatStringNodes(node));
        }

        final ToSNode[] children = translateInterpolatedParts(node.parts);

        final RubyNode rubyNode = new InterpolatedStringNode(children, sourceEncoding.jcoding);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitInterpolatedSymbolNode(Nodes.InterpolatedSymbolNode node) {
        final ToSNode[] children = translateInterpolatedParts(node.parts);

        final RubyNode stringNode = new InterpolatedStringNode(children, sourceEncoding.jcoding);
        final RubyNode rubyNode = StringToSymbolNodeGen.create(stringNode);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitInterpolatedXStringNode(Nodes.InterpolatedXStringNode node) {
        var stringNode = new Nodes.InterpolatedStringNode(node.parts, node.startOffset, node.length);
        final RubyNode string = stringNode.accept(this);

        final RubyNode rubyNode = createCallNode(new SelfNode(), "`", string);
        return assignPositionAndFlags(node, rubyNode);
    }

    private static boolean allPartsAreStringNodes(Nodes.InterpolatedStringNode node) {
        for (var part : node.parts) {
            if (!(part instanceof Nodes.StringNode)) {
                return false;
            }
        }
        return true;
    }

    private static Nodes.StringNode concatStringNodes(Nodes.InterpolatedStringNode node) {
        Nodes.Node[] parts = node.parts;
        assert parts.length > 0;

        int totalSize = 0;
        for (var part : parts) {
            totalSize += ((Nodes.StringNode) part).unescaped.length;
        }

        byte[] concatenated = new byte[totalSize];
        int i = 0;
        for (var part : parts) {
            byte[] bytes = ((Nodes.StringNode) part).unescaped;
            System.arraycopy(bytes, 0, concatenated, i, bytes.length);
            i += bytes.length;
        }

        int start = parts[0].startOffset;
        var last = parts[parts.length - 1];
        int length = last.endOffset() - start;
        return new Nodes.StringNode(NO_FLAGS, concatenated, start, length);
    }

    private ToSNode[] translateInterpolatedParts(Nodes.Node[] parts) {
        final ToSNode[] children = new ToSNode[parts.length];

        for (int i = 0; i < parts.length; i++) {
            RubyNode expression = parts[i].accept(this);
            children[i] = ToSNodeGen.create(expression);
        }
        return children;
    }

    @Override
    public RubyNode visitKeywordHashNode(Nodes.KeywordHashNode node) {
        // translate it like a HashNode, whether it is keywords or not is checked in getKeywordArgumentsDescriptor()
        final var hash = new Nodes.HashNode(node.elements, node.startOffset, node.length);
        return hash.accept(this);
    }

    @Override
    public RubyNode visitLambdaNode(Nodes.LambdaNode node) {
        return translateBlockAndLambda(node, node.parameters, node.body, node.locals, null);
    }

    @Override
    public RubyNode visitLocalVariableReadNode(Nodes.LocalVariableReadNode node) {
        final String name = node.name;

        final RubyNode rubyNode = environment.findLocalVarNode(name, null);
        assert rubyNode != null : name;

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitLocalVariableAndWriteNode(Nodes.LocalVariableAndWriteNode node) {
        // `a &&= value` is translated into `a && a = value`
        // don't check whether variable is initialized because even if a local variable
        // is not set then it returns nil and does not have side effects (warnings or exceptions)

        int startOffset = node.startOffset;
        int length = node.length;

        var writeNode = new Nodes.LocalVariableWriteNode(node.name, node.depth, node.value, startOffset, length)
                .accept(this);
        var readNode = new Nodes.LocalVariableReadNode(node.name, node.depth, startOffset, length).accept(this);
        var andNode = AndNodeGen.create(readNode, writeNode);

        final RubyNode rubyNode = new DefinedWrapperNode(language.coreStrings.ASSIGNMENT, andNode);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitLocalVariableOperatorWriteNode(Nodes.LocalVariableOperatorWriteNode node) {
        // e.g. `a += value` is translated into a = a + value`
        // don't check whether variable is initialized so exception will be raised otherwise

        int startOffset = node.startOffset;
        int length = node.length;
        var readNode = new Nodes.LocalVariableReadNode(node.name, node.depth, startOffset, length);
        var desugared = new Nodes.LocalVariableWriteNode(node.name, node.depth,
                callNode(node, readNode, node.operator, node.value), startOffset, length);
        return desugared.accept(this);
    }

    @Override
    public RubyNode visitLocalVariableOrWriteNode(Nodes.LocalVariableOrWriteNode node) {
        // `a ||= value` is translated into `a || a = value`

        // No need to check `defined?(var)` before reading, as `var` even if not set returns nil and does not have
        // side effects (warnings or exceptions)
        int startOffset = node.startOffset;
        int length = node.length;

        var writeNode = new Nodes.LocalVariableWriteNode(node.name, node.depth, node.value, startOffset, length)
                .accept(this);
        var readNode = new Nodes.LocalVariableReadNode(node.name, node.depth, startOffset, length).accept(this);

        final RubyNode rubyNode = OrLazyValueDefinedNodeGen.create(readNode, writeNode);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
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

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitLocalVariableTargetNode(Nodes.LocalVariableTargetNode node) {
        // TODO: this could be done more directly but the logic of visitLocalVariableWriteNode() needs to be simpler first
        return visitLocalVariableWriteNode(
                new Nodes.LocalVariableWriteNode(node.name, node.depth, null, node.startOffset, node.length));
    }

    @Override
    public RubyNode visitMatchLastLineNode(Nodes.MatchLastLineNode node) {
        // replace regexp with /.../ =~ $_

        final var regexp = new Nodes.RegularExpressionNode(node.flags, node.unescaped, node.startOffset, node.length);
        final var regexpNode = regexp.accept(this);
        final var lastLineNode = ReadGlobalVariableNodeGen.create("$_");

        final RubyNode rubyNode = createCallNode(false, regexpNode, "=~", lastLineNode);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitMatchPredicateNode(Nodes.MatchPredicateNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitMatchRequiredNode(Nodes.MatchRequiredNode node) {
        return defaultVisit(node);
    }

    // See BodyTranslator#visitMatch2Node
    @Override
    public RubyNode visitMatchWriteNode(Nodes.MatchWriteNode node) {
        // node.call already contains an AST for the =~ method call - /.../.=~(s)
        assert node.call.receiver instanceof Nodes.RegularExpressionNode;
        assert node.call.name.equals("=~");
        assert node.call.arguments != null;
        assert node.call.arguments.arguments.length == 1;
        assert node.targets.length > 0;

        RubyNode matchNode = node.call.accept(this);

        final int numberOfNames = node.targets.length;
        String[] names = new String[numberOfNames];

        for (int i = 0; i < numberOfNames; i++) {
            // Nodes.LocalVariableTargetNode is the only expected node here
            names[i] = ((Nodes.LocalVariableTargetNode) node.targets[i]).name;
        }

        final RubyNode[] setters = new RubyNode[numberOfNames];
        final RubyNode[] nilSetters = new RubyNode[numberOfNames];
        final int tempSlot = environment.declareLocalTemp("match_data");
        final var sourceSection = new SourceIndexLength(node.startOffset, node.length);

        for (int i = 0; i < numberOfNames; i++) {
            final String name = names[i];

            TranslatorEnvironment environmentToDeclareIn = environment;

            // TODO: use Nodes.LocalVariableTargetNode#depth field
            while (!environmentToDeclareIn.hasOwnScopeForAssignments()) {
                environmentToDeclareIn = environmentToDeclareIn.getParent();
            }
            environmentToDeclareIn.declareVar(name);
            nilSetters[i] = match2NilSetter(name, sourceSection);
            setters[i] = match2NonNilSetter(name, tempSlot, sourceSection);
        }

        final RubyNode readNode = ReadGlobalVariableNodeGen.create("$~");
        final ReadLocalNode tempVarReadNode = environment.readNode(tempSlot, sourceSection);
        final RubyNode readMatchDataNode = tempVarReadNode.makeWriteNode(readNode);
        final RubyNode rubyNode = new ReadMatchReferenceNodes.SetNamedVariablesMatchNode(matchNode, readMatchDataNode,
                setters, nilSetters);

        return assignPositionAndFlags(node, rubyNode);
    }

    private RubyNode match2NilSetter(String name, SourceIndexLength position) {
        return environment.findLocalVarNode(name, position).makeWriteNode(new NilLiteralNode());
    }

    private RubyNode match2NonNilSetter(String name, int tempSlot, SourceIndexLength position) {
        ReadLocalNode varNode = environment.findLocalVarNode(name, position);
        ReadLocalNode tempVarNode = environment.readNode(tempSlot, position);
        MatchDataNodes.GetFixedNameMatchNode getIndexNode = new MatchDataNodes.GetFixedNameMatchNode(tempVarNode,
                language.getSymbol(name));
        return varNode.makeWriteNode(getIndexNode);
    }

    @Override
    public RubyNode visitMissingNode(Nodes.MissingNode node) {
        return defaultVisit(node);
    }

    @Override
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

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitMultiWriteNode(Nodes.MultiWriteNode node) {
        final RubyNode rubyNode;
        var translator = new YARPMultiWriteNodeTranslator(node, language, this);
        rubyNode = translator.translate();

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
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
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitNilNode(Nodes.NilNode node) {
        final RubyNode rubyNode = new NilLiteralNode();
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitNumberedReferenceReadNode(Nodes.NumberedReferenceReadNode node) {
        final RubyNode lastMatchNode = ReadGlobalVariableNodeGen.create("$~");
        final RubyNode rubyNode = new ReadMatchReferenceNodes.ReadNthMatchNode(lastMatchNode, node.number);

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitOrNode(Nodes.OrNode node) {
        final RubyNode left = node.left.accept(this);
        final RubyNode right = node.right.accept(this);

        final RubyNode rubyNode = OrNodeGen.create(left, right);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitParenthesesNode(Nodes.ParenthesesNode node) {
        if (node.body == null) {
            final RubyNode rubyNode = new NilLiteralNode();
            return assignPositionAndFlags(node, rubyNode);
        }
        return node.body.accept(this);
    }

    @Override
    public RubyNode visitPinnedExpressionNode(Nodes.PinnedExpressionNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitPinnedVariableNode(Nodes.PinnedVariableNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitPostExecutionNode(Nodes.PostExecutionNode node) {
        // END blocks run after any other code - not just code in the same file
        // Turn into a call to Truffle::KernelOperations.at_exit

        // Create Prism CallNode to avoid duplication block literal related logic
        final var receiver = new Nodes.ConstantReadNode("KernelOperations", 0, 0);
        final var arguments = new Nodes.ArgumentsNode(NO_FLAGS, new Nodes.Node[]{ new Nodes.FalseNode(0, 0) }, 0, 0);
        final var block = new Nodes.BlockNode(StringUtils.EMPTY_STRING_ARRAY, 0, null, node.statements, 0, 0);

        final var callNode = new Nodes.CallNode(NO_FLAGS, receiver, "at_exit", arguments, block, 0, 0).accept(this);
        final RubyNode rubyNode = new OnceNode(callNode);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitPreExecutionNode(Nodes.PreExecutionNode node) {
        // BEGIN should be located in the top-level context, so it safe to evaluate a block right now
        RubyNode sequence = node.statements.accept(this);
        beginBlocks.add(sequence);
        return null;
    }

    @Override
    public RubyNode visitProgramNode(Nodes.ProgramNode node) {
        final RubyNode sequence = node.statements.accept(this);

        // add BEGIN {} blocks at the very beginning of the program
        ArrayList<RubyNode> nodes = new ArrayList<>(beginBlocks);
        nodes.add(sequence);

        return sequence(node, nodes);
    }

    @Override
    public RubyNode visitRangeNode(Nodes.RangeNode node) {
        var left = translateNodeOrNil(node.left);
        var right = translateNodeOrNil(node.right);

        final RubyNode rubyNode;
        if (left instanceof IntegerFixnumLiteralNode l && right instanceof IntegerFixnumLiteralNode r) {
            final Object range = new RubyIntRange(node.isExcludeEnd(), l.getValue(), r.getValue());
            rubyNode = new ObjectLiteralNode(range);
        } else if (left instanceof LongFixnumLiteralNode l && right instanceof LongFixnumLiteralNode r) {
            final Object range = new RubyLongRange(node.isExcludeEnd(), l.getValue(), r.getValue());
            rubyNode = new ObjectLiteralNode(range);
        } else {
            rubyNode = RangeNodesFactory.RangeLiteralNodeGen.create(left, right, node.isExcludeEnd());
        }
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitRationalNode(Nodes.RationalNode node) {
        // Translate as Rational.convert private method call

        final RubyNode objectClassNode = new ObjectClassLiteralNode();
        final ReadConstantNode rationalModuleNode = new ReadConstantNode(objectClassNode, "Rational");
        final RubyNode numeratorNode;
        final RubyNode denominatorNode;

        // Handle float literals differently and avoid Java float/double types to not lose precision.
        // So normalize numerator and denominator, e.g 3.14r literal is translated into Rational(314, 100).
        // The other option is to represent float literal as a String and rely on parsing String literals in
        // the Kernel#Rational() method. The only downside is worse performance as far as Kernel#Rational()
        // is implemented in Ruby.
        if (node.numeric instanceof Nodes.FloatNode floatNode) {
            // Translate as Rational.convert(numerator, denominator).

            // Assume float literal is in the ddd.ddd format and
            // scientific format (e.g. 1.23e10) is not valid in Rational literals
            String string = toString(floatNode).replaceAll("_", ""); // remove '_' characters
            int pointIndex = string.indexOf('.');
            assert pointIndex != -1; // float literal in Ruby must contain '.'

            int fractionLength = string.length() - pointIndex - 1;
            assert fractionLength > 0;

            String numerator = string.replace(".", ""); // remove float point
            numeratorNode = translateIntegerLiteralString(numerator);

            String denominator = "1" + "0".repeat(fractionLength);
            denominatorNode = translateIntegerLiteralString(denominator);
        } else {
            // Translate as Rational.convert(n, 1)
            numeratorNode = node.numeric.accept(this);
            denominatorNode = new IntegerFixnumLiteralNode(1);
        }

        RubyNode[] arguments = new RubyNode[]{ numeratorNode, denominatorNode };

        RubyNode rubyNode = createCallNode(rationalModuleNode, "convert", arguments);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
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
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitRegularExpressionNode(Nodes.RegularExpressionNode node) {
        var encodingAndOptions = getRegexpEncodingAndOptions(new Nodes.RegularExpressionFlags(node.flags));
        var encoding = encodingAndOptions.encoding;
        var source = TruffleString.fromByteArrayUncached(node.unescaped, encoding.tencoding, false);
        try {
            final RubyRegexp regexp = RubyRegexp.create(language, source, encoding,
                    encodingAndOptions.options, currentNode);
            final ObjectLiteralNode literalNode = new ObjectLiteralNode(regexp);
            return assignPositionAndFlags(node, literalNode);
        } catch (DeferredRaiseException dre) {
            throw dre.getException(RubyLanguage.getCurrentContext());
        }
    }

    private record RegexpEncodingAndOptions(RubyEncoding encoding, RegexpOptions options) {
    }

    private RegexpEncodingAndOptions getRegexpEncodingAndOptions(Nodes.RegularExpressionFlags flags) {
        RubyEncoding regexpEncoding;

        // regexp options
        final KCode kcode;
        final boolean fixed;
        boolean explicitEncoding = true;

        if (flags.isAscii8bit()) {
            fixed = false;
            kcode = KCode.NONE;
            regexpEncoding = Encodings.BINARY;
        } else if (flags.isUtf8()) {
            fixed = true;
            kcode = KCode.UTF8;
            regexpEncoding = Encodings.UTF_8;
        } else if (flags.isEucJp()) {
            fixed = true;
            kcode = KCode.EUC;
            regexpEncoding = Encodings.getBuiltInEncoding(EUCJPEncoding.INSTANCE);
        } else if (flags.isWindows31j()) {
            fixed = true;
            kcode = KCode.SJIS;
            regexpEncoding = Encodings.getBuiltInEncoding(Windows_31JEncoding.INSTANCE);
        } else {
            fixed = false;
            kcode = KCode.NONE;
            regexpEncoding = sourceEncoding;
            explicitEncoding = false;
        }

        if (!explicitEncoding) {
            if (flags.isForcedBinaryEncoding()) {
                regexpEncoding = Encodings.BINARY;
            } else if (flags.isForcedUsAsciiEncoding()) {
                regexpEncoding = Encodings.US_ASCII;
            } else if (flags.isForcedUtf8Encoding()) {
                regexpEncoding = Encodings.UTF_8;
            }
        }

        final RegexpOptions options = new RegexpOptions(kcode, fixed, flags.isOnce(), flags.isExtended(),
                flags.isMultiLine(), flags.isIgnoreCase(), flags.isAscii8bit(), !explicitEncoding, true);
        return new RegexpEncodingAndOptions(regexpEncoding, options);
    }

    @Override
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

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitRescueNode(Nodes.RescueNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitRetryNode(Nodes.RetryNode node) {
        final RubyNode rubyNode = new RetryNode();
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitReturnNode(Nodes.ReturnNode node) {
        final RubyNode rubyNode;
        final RubyNode argumentsNode = translateControlFlowArguments(node.arguments);

        // either in block or lambda
        if (environment.isBlock()) {
            // Lambda behaves a bit differently from block and "return" to a class/module body is correct -
            // so DynamicReturnNode should be used instead of InvalidReturnNode.
            // It's handled later and InvalidReturnNode is replaced with DynamicReturnNode in YARPBlockTranslator.
            final ReturnID returnID = environment.getReturnID();
            if (returnID == ReturnID.MODULE_BODY) {
                rubyNode = new InvalidReturnNode(argumentsNode);
            } else {
                rubyNode = new DynamicReturnNode(returnID, argumentsNode);
            }
        } else {
            rubyNode = new LocalReturnNode(argumentsNode);
        }

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitSelfNode(Nodes.SelfNode node) {
        final RubyNode rubyNode = new SelfNode();
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitSingletonClassNode(Nodes.SingletonClassNode node) {
        final RubyNode receiverNode = node.expression.accept(this);
        var singletonClassNode = SingletonClassASTNodeGen.create(receiverNode);
        assignPositionOnly(node, singletonClassNode);

        boolean dynamicConstantLookup = environment.isDynamicConstantLookup();

        String modulePath = "<singleton class>";
        if (!dynamicConstantLookup) {
            if (environment.isModuleBody() && node.expression instanceof Nodes.SelfNode) {
                // Common case of class << self in a module body, the constant lookup scope is still static.
                if (environment.isTopLevelObjectScope()) {
                    // Special pattern recognized by #modulePathAndMethodName:
                    modulePath = "main::<singleton class>";
                } else {
                    modulePath = TranslatorEnvironment.composeModulePath(environment.modulePath, "<singleton class>");
                }
            } else if (environment.isTopLevelScope()) {
                // At the top-level of a file, opening the singleton class of an expression executed only once
            } else {
                // Switch to dynamic constant lookup
                dynamicConstantLookup = true;
                if (language.options.LOG_DYNAMIC_CONSTANT_LOOKUP) {
                    RubyLanguage.LOGGER.info(
                            () -> "start dynamic constant lookup at " +
                                    RubyLanguage.getCurrentContext().fileLine(getSourceSection(node)));
                }
            }
        }

        final RubyNode rubyNode = openModule(
                node,
                singletonClassNode,
                modulePath,
                node.body,
                OpenModule.SINGLETON_CLASS,
                dynamicConstantLookup);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitSourceEncodingNode(Nodes.SourceEncodingNode node) {
        final RubyNode rubyNode = new ObjectLiteralNode(sourceEncoding);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitSourceFileNode(Nodes.SourceFileNode node) {
        // Note: ideally we would use the filesystem encoding here, but it is too early to get that.
        // The filesystem encoding on Linux and macOS is UTF-8 anyway, so keep it simple.
        RubyEncoding encoding = Encodings.UTF_8;
        String path = language.getSourcePath(source);
        var tstring = TruffleString.fromJavaStringUncached(path, encoding.tencoding);
        var rubyNode = new StringLiteralNode(tstring, encoding);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitSourceLineNode(Nodes.SourceLineNode node) {
        // Note: we use the YARP source here, notably to account for the lineOffset.
        // Instead of getSourceSection(node).getStartLine() which would also create the TextMap early.
        int line = environment.getParseEnvironment().yarpSource.line(node.startOffset);
        var rubyNode = new IntegerFixnumLiteralNode(line);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitSplatNode(Nodes.SplatNode node) {
        final RubyNode value = translateNodeOrNil(node.expression);
        final RubyNode rubyNode = SplatCastNodeGen.create(language, SplatCastNode.NilBehavior.CONVERT, false, value);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitStatementsNode(Nodes.StatementsNode node) {
        RubyNode[] rubyNodes = translate(node.body);
        return sequence(node, Arrays.asList(rubyNodes));
    }

    @Override
    public RubyNode visitStringNode(Nodes.StringNode node) {
        final RubyNode rubyNode;
        final RubyEncoding encoding;

        if (node.isForcedUtf8Encoding()) {
            encoding = Encodings.UTF_8;
        } else if (node.isForcedBinaryEncoding()) {
            encoding = Encodings.BINARY;
        } else {
            encoding = sourceEncoding;
        }

        final TruffleString tstring = TStringUtils.fromByteArray(node.unescaped, encoding);

        if (!node.isFrozen()) {
            final TruffleString cachedTString = language.tstringCache.getTString(tstring, encoding);
            rubyNode = new StringLiteralNode(cachedTString, encoding);
        } else {
            final ImmutableRubyString frozenString = language.getFrozenStringLiteral(tstring, encoding);
            rubyNode = new FrozenStringLiteralNode(frozenString, FrozenStrings.EXPRESSION);
        }

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitSuperNode(Nodes.SuperNode node) {
        var argumentsAndBlock = translateArgumentsAndBlock(node.arguments, node.block, environment.getMethodName());

        final RubyNode arguments = new ReadSuperArgumentsNode(
                argumentsAndBlock.arguments(),
                argumentsAndBlock.isSplatted());
        final RubyNode block = executeOrInheritBlock(argumentsAndBlock.block());

        RubyNode callNode = new SuperCallNode(argumentsAndBlock.isSplatted(), arguments, block,
                argumentsAndBlock.argumentsDescriptor());
        callNode = wrapCallWithLiteralBlock(argumentsAndBlock, callNode);

        return assignPositionAndFlags(node, callNode);
    }

    private RubyNode executeOrInheritBlock(RubyNode blockNode) {
        if (blockNode != null) {
            return blockNode;
        } else {
            return environment.findLocalVarOrNilNode(TranslatorEnvironment.METHOD_BLOCK_NAME, null);
        }
    }

    @Override
    public RubyNode visitSymbolNode(Nodes.SymbolNode node) {
        final RubyEncoding encoding;

        if (node.isForcedUtf8Encoding()) {
            encoding = Encodings.UTF_8;
        } else if (node.isForcedUsAsciiEncoding()) {
            encoding = Encodings.US_ASCII;
        } else if (node.isForcedBinaryEncoding()) {
            encoding = Encodings.BINARY;
        } else {
            encoding = sourceEncoding;
        }

        var tstring = TStringUtils.fromByteArray(node.unescaped, encoding);
        final RubySymbol symbol = language.getSymbol(tstring, encoding);

        final RubyNode rubyNode = new ObjectLiteralNode(symbol);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitTrueNode(Nodes.TrueNode node) {
        final RubyNode rubyNode = new BooleanLiteralNode(true);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitUndefNode(Nodes.UndefNode node) {
        final RubyNode[] names = translate(node.names);
        final RubyNode rubyNode = new ModuleNodes.UndefNode(names);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitUnlessNode(Nodes.UnlessNode node) {
        final RubyNode conditionNode = node.predicate.accept(this);
        final RubyNode thenNode = node.statements == null ? null : node.statements.accept(this);
        final RubyNode elseNode = node.consequent == null ? null : node.consequent.accept(this);
        final RubyNode rubyNode;

        if (thenNode != null && elseNode != null) {
            rubyNode = IfElseNodeGen.create(conditionNode, elseNode, thenNode);
            return assignPositionAndFlags(node, rubyNode);
        } else if (thenNode != null) {
            rubyNode = UnlessNodeGen.create(conditionNode, thenNode);
            return assignPositionAndFlags(node, rubyNode);
        } else if (elseNode != null) {
            rubyNode = IfNodeGen.create(conditionNode, elseNode);
            return assignPositionAndFlags(node, rubyNode);
        } else {
            // unless (condition)
            // end
            rubyNode = sequence(node, Arrays.asList(conditionNode, new NilLiteralNode()));
            return rubyNode;
        }
    }

    @Override
    public RubyNode visitUntilNode(Nodes.UntilNode node) {
        final RubyNode rubyNode = translateWhileNode(node, node.predicate, node.statements, true,
                !node.isBeginModifier());
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitWhileNode(Nodes.WhileNode node) {
        final RubyNode rubyNode = translateWhileNode(node, node.predicate, node.statements, false,
                !node.isBeginModifier());
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitXStringNode(Nodes.XStringNode node) {
        // convert EncodingFlags to StringFlags
        int flags = Nodes.StringFlags.FROZEN; // it's always frozen

        if (node.isForcedBinaryEncoding()) {
            flags |= Nodes.StringFlags.FORCED_BINARY_ENCODING;
        }

        if (node.isForcedUtf8Encoding()) {
            flags |= Nodes.StringFlags.FORCED_UTF8_ENCODING;
        }

        var stringNode = new Nodes.StringNode((short) flags, node.unescaped, node.startOffset, node.length);
        final RubyNode string = stringNode.accept(this);

        final RubyNode rubyNode = createCallNode(new SelfNode(), "`", string);
        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    public RubyNode visitYieldNode(Nodes.YieldNode node) {
        var argumentsAndBlock = translateArgumentsAndBlock(node.arguments, null, "<yield>");

        RubyNode readBlock = environment.findLocalVarOrNilNode(TranslatorEnvironment.METHOD_BLOCK_NAME, null);

        var rubyNode = new YieldExpressionNode(
                argumentsAndBlock.isSplatted(),
                argumentsAndBlock.argumentsDescriptor(),
                argumentsAndBlock.arguments(),
                readBlock);

        return assignPositionAndFlags(node, rubyNode);
    }

    @Override
    protected RubyNode defaultVisit(Nodes.Node node) {
        String code = toString(node);
        throw new Error(
                this.getClass().getSimpleName() + " does not know how to translate " + node.getClass().getSimpleName() +
                        " at " + RubyLanguage.getCurrentContext().fileLine(getSourceSection(node)) +
                        "\nCode snippet:\n" + code + "\nPrism AST:\n" + node);
    }

    /** Declare variable in the nearest non-block outer lexical scope - either method, class or top-level */
    protected FrameSlotAndDepth createFlipFlopState() {
        final var target = environment.getSurroundingMethodEnvironment();
        final int frameSlot = target.declareLocalTemp("flipflop");
        target.getFlipFlopStates().add(frameSlot);

        return new FrameSlotAndDepth(frameSlot, environment.getBlockDepth());
    }

    /** Translate a list of nodes, e.g. break/return operands, into an array producing node. It returns ArrayLiteralNode
     * subclass in the simplest case (when there is no splat operator) or combination of
     * ArrayConcatNode/ArrayLiteralNode/SplatCastNodeGen nodes to "join" destructured by splat operator array with other
     * operands. */
    private RubyNode translateExpressionsList(Nodes.Node[] nodes) {
        assert nodes != null;

        if (nodes.length == 0) {
            return ArrayLiteralNode.create(language, RubyNode.EMPTY_ARRAY);
        }

        boolean containSplatOperator = containYARPSplatNode(nodes);

        // fast path (no SplatNode)

        if (!containSplatOperator) {
            RubyNode[] rubyNodes = translate(nodes);
            return ArrayLiteralNode.create(language, rubyNodes);
        }

        // generic path

        ArrayList<RubyNode> arraysToConcat = new ArrayList<>();
        ArrayList<RubyNode> current = new ArrayList<>();

        // group nodes before/after/between splat operators into array literals and concat them, e.g.
        //   a, b, *c, d
        // is translated into
        //   ArrayConcatNode(ArrayLiteralNode(a, b), "splat-operator-node", ArrayLiteralNode(d))
        for (Nodes.Node node : nodes) {
            if (node instanceof Nodes.SplatNode) {
                if (!current.isEmpty()) {
                    arraysToConcat.add(ArrayLiteralNode.create(language, current.toArray(RubyNode.EMPTY_ARRAY)));
                    current = new ArrayList<>();
                }
                arraysToConcat.add(node.accept(this));
            } else {
                current.add(node.accept(this));
            }
        }

        if (!current.isEmpty()) {
            arraysToConcat.add(ArrayLiteralNode.create(language, current.toArray(RubyNode.EMPTY_ARRAY)));
        }

        final RubyNode rubyNode;

        if (arraysToConcat.size() == 1) {
            rubyNode = arraysToConcat.get(0);
        } else {
            rubyNode = new ArrayConcatNode(arraysToConcat.toArray(RubyNode.EMPTY_ARRAY));
        }

        return rubyNode;
    }

    private String modulePathAndMethodName(String methodName, boolean onSingleton, boolean isReceiverSelf) {
        String modulePath = environment.modulePath;
        if (modulePath == null) {
            if (onSingleton) {
                if (environment.isTopLevelObjectScope() && isReceiverSelf) {
                    modulePath = "main";
                } else {
                    modulePath = "<singleton class>"; // method of an unknown singleton class
                    onSingleton = false;
                }
            } else {
                if (environment.isTopLevelObjectScope()) {
                    modulePath = "Object";
                } else {
                    modulePath = ""; // instance method of an unknown module
                }
            }
        }

        if (modulePath.endsWith("::<singleton class>") && !onSingleton) {
            modulePath = modulePath.substring(0, modulePath.length() - "::<singleton class>".length());
            onSingleton = true;
        }

        return SharedMethodInfo.modulePathAndMethodName(modulePath, methodName, onSingleton);
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
                newEnvironment,
                sourceBytes,
                source,
                parserContext,
                currentNode);

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
            rubyNode = new NilLiteralNode();
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
            return new NilLiteralNode();
        }

        final Nodes.Node[] values = node.arguments;

        if (values.length == 1) {
            return values[0].accept(this);
        }

        final RubyNode rubyNode = translateExpressionsList(values);

        return assignPositionAndFlags(node, rubyNode);
    }

    private RubyNode translateRescueException(Nodes.Node target) {
        final RubyNode rubyNode = target.accept(this);
        final AssignableNode assignableNode = (AssignableNode) rubyNode;
        return new AssignRescueVariableNode(assignableNode);
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
        return createCallNode(true, receiver, method, arguments);
    }

    protected RubyContextSourceNode createCallNode(boolean ignoreVisibility, RubyNode receiver, String method,
            RubyNode... arguments) {
        var parameters = new RubyCallNodeParameters(
                receiver,
                method,
                null,
                NoKeywordArgumentsDescriptor.INSTANCE,
                arguments,
                ignoreVisibility);
        return language.coreMethodAssumptions.createCallNode(parameters);
    }

    protected boolean isSideEffectFreeRescueExpression(Nodes.Node node) {
        return node instanceof Nodes.InstanceVariableReadNode ||
                node instanceof Nodes.LocalVariableReadNode ||
                node instanceof Nodes.ClassVariableReadNode ||
                node instanceof Nodes.SourceFileNode ||
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

    protected TruffleString toTString(Nodes.Node node) {
        return TruffleString.fromByteArrayUncached(sourceBytes, node.startOffset, node.length, sourceEncoding.tencoding,
                false);
    }

    protected TruffleString toTString(String string) {
        return TStringUtils.fromJavaString(string, sourceEncoding);
    }

    protected String toString(Nodes.Node node) {
        return TStringUtils.toJavaStringOrThrow(toTString(node), sourceEncoding);
    }

    protected TruffleString toTString(byte[] bytes) {
        return TruffleString.fromByteArrayUncached(bytes, sourceEncoding.tencoding, false);
    }

    protected String toString(byte[] bytes) {
        return TStringUtils.toJavaStringOrThrow(
                toTString(bytes), sourceEncoding);
    }

    protected SourceSection getSourceSection(Nodes.Node yarpNode) {
        return source.createSection(yarpNode.startOffset, yarpNode.length);
    }

    public static RubyNode assignPositionAndFlags(Nodes.Node yarpNode, RubyNode rubyNode) {
        assignPositionOnly(yarpNode, rubyNode);
        copyNewlineFlag(yarpNode, rubyNode);
        return rubyNode;
    }

    public static RubyNode assignPositionAndFlagsIfMissing(Nodes.Node yarpNode, RubyNode rubyNode) {
        if (rubyNode.hasSource()) {
            return rubyNode;
        }

        assignPositionOnly(yarpNode, rubyNode);
        copyNewlineFlag(yarpNode, rubyNode);
        return rubyNode;
    }

    private static void assignPositionOnly(Nodes.Node yarpNode, RubyNode rubyNode) {
        rubyNode.unsafeSetSourceSection(yarpNode.startOffset, yarpNode.length);
    }

    // assign position based on a list of nodes (arguments list, exception classes list in a rescue section, etc)
    private void assignPositionOnly(Nodes.Node[] nodes, RubyNode rubyNode) {
        final Nodes.Node first = nodes[0];
        final Nodes.Node last = nodes[nodes.length - 1];

        final int length = last.endOffset() - first.startOffset;
        rubyNode.unsafeSetSourceSection(first.startOffset, length);
    }

    private static void copyNewlineFlag(Nodes.Node yarpNode, RubyNode rubyNode) {
        if (yarpNode.hasNewLineFlag()) {
            rubyNode.unsafeSetIsNewLine();
        }
    }

    protected static RubyNode sequence(Nodes.Node yarpNode, List<RubyNode> sequence) {
        assert !yarpNode.hasNewLineFlag() : "Expected node passed to sequence() to not have a newline flag";

        RubyNode sequenceNode = sequence(sequence);

        if (!sequenceNode.hasSource()) {
            assignPositionOnly(yarpNode, sequenceNode);
        }

        return sequenceNode;
    }

    protected static RubyNode sequence(List<RubyNode> sequence) {
        final List<RubyNode> flattened = Translator.flatten(sequence, true);

        if (flattened.isEmpty()) {
            final RubyNode nilNode = new NilLiteralNode();
            return nilNode;
        } else if (flattened.size() == 1) {
            return flattened.get(0);
        } else {
            final RubyNode[] flatSequence = flattened.toArray(RubyNode.EMPTY_ARRAY);
            var sequenceNode = new SequenceNode(flatSequence);
            return sequenceNode;
        }
    }

    protected static Nodes.CallNode callNode(Nodes.Node location, Nodes.Node receiver, String methodName,
            Nodes.Node... arguments) {
        return callNode(location, NO_FLAGS, receiver, methodName, arguments);
    }

    protected static Nodes.CallNode callNode(Nodes.Node location, short flags, Nodes.Node receiver, String methodName,
            Nodes.Node... arguments) {
        return new Nodes.CallNode(flags, receiver, methodName,
                new Nodes.ArgumentsNode(NO_FLAGS, arguments, location.startOffset, location.length), null,
                location.startOffset, location.length);
    }

    private boolean containYARPSplatNode(Nodes.Node[] nodes) {
        for (var n : nodes) {
            if (n instanceof Nodes.SplatNode) {
                return true;
            }
        }

        return false;
    }

    private RubyNode[] translate(Nodes.Node[] nodes) {
        if (nodes.length == 0) {
            return RubyNode.EMPTY_ARRAY;
        }

        RubyNode[] rubyNodes = new RubyNode[nodes.length];

        for (int i = 0; i < nodes.length; i++) {
            rubyNodes[i] = nodes[i].accept(this);
        }

        return rubyNodes;
    }

    private ArgumentDescriptor[] parametersNodeToArgumentDescriptors(Nodes.ParametersNode parametersNode) {
        if (parametersNode == null) {
            return ArgumentDescriptor.EMPTY_ARRAY;
        }

        ArrayList<ArgumentDescriptor> descriptors = new ArrayList<>();

        for (var node : parametersNode.requireds) {
            if (node instanceof Nodes.MultiTargetNode) {
                descriptors.add(new ArgumentDescriptor(ArgumentType.anonreq));
            } else {
                String name = ((Nodes.RequiredParameterNode) node).name;
                var descriptor = new ArgumentDescriptor(ArgumentType.req, name);
                descriptors.add(descriptor);
            }
        }

        for (var node : parametersNode.optionals) {
            String name = ((Nodes.OptionalParameterNode) node).name;
            var descriptor = new ArgumentDescriptor(ArgumentType.opt, name);
            descriptors.add(descriptor);
        }

        // Proc#parameters doesn't report anonymous rest parameter for implicit rest parameter (|a,|).
        // So just ignore Nodes.ImplicitRestNode (that is only available in blocks).
        if (parametersNode.rest instanceof Nodes.RestParameterNode restParameterNode) {
            if (restParameterNode.name == null) {
                descriptors.add(new ArgumentDescriptor(ArgumentType.anonrest));
            } else {
                var descriptor = new ArgumentDescriptor(ArgumentType.rest, restParameterNode.name);
                descriptors.add(descriptor);
            }
        }

        for (var node : parametersNode.posts) {
            if (node instanceof Nodes.MultiTargetNode) {
                descriptors.add(new ArgumentDescriptor(ArgumentType.anonreq));
            } else {
                String name = ((Nodes.RequiredParameterNode) node).name;
                var descriptor = new ArgumentDescriptor(ArgumentType.req, name);
                descriptors.add(descriptor);
            }
        }

        for (var node : parametersNode.keywords) {
            final ArgumentDescriptor descriptor;

            if (node instanceof Nodes.RequiredKeywordParameterNode required) {
                descriptor = new ArgumentDescriptor(ArgumentType.keyreq, required.name);
            } else if (node instanceof Nodes.OptionalKeywordParameterNode optional) {
                descriptor = new ArgumentDescriptor(ArgumentType.key, optional.name);
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }

            descriptors.add(descriptor);
        }

        if (parametersNode.keyword_rest != null) {
            if (parametersNode.keyword_rest instanceof Nodes.KeywordRestParameterNode) {
                final var keywordRestParameterNode = (Nodes.KeywordRestParameterNode) parametersNode.keyword_rest;

                if (keywordRestParameterNode.name == null) {
                    descriptors.add(new ArgumentDescriptor(ArgumentType.anonkeyrest, DEFAULT_KEYWORD_REST_NAME));
                } else {
                    descriptors.add(new ArgumentDescriptor(ArgumentType.keyrest, keywordRestParameterNode.name));
                }
            } else if (parametersNode.keyword_rest instanceof Nodes.ForwardingParameterNode) {
                // ... => *, **, &
                descriptors.add(new ArgumentDescriptor(ArgumentType.rest, FORWARDED_REST_NAME));
                descriptors.add(new ArgumentDescriptor(ArgumentType.keyrest, FORWARDED_KEYWORD_REST_NAME));
                descriptors.add(new ArgumentDescriptor(ArgumentType.block, FORWARDED_BLOCK_NAME));
            } else if (parametersNode.keyword_rest instanceof Nodes.NoKeywordsParameterNode) {
                final var descriptor = new ArgumentDescriptor(ArgumentType.nokey);
                descriptors.add(descriptor);
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }
        }

        if (parametersNode.block != null) {
            final String name;

            if (parametersNode.block.name == null) {
                // def a(&) ... end
                name = FORWARDED_BLOCK_NAME;
            } else {
                name = parametersNode.block.name;
            }

            descriptors.add(new ArgumentDescriptor(ArgumentType.block, name));
        }

        return descriptors.toArray(ArgumentDescriptor.EMPTY_ARRAY);
    }

    private Arity createArity(Nodes.ParametersNode parametersNode) {
        if (parametersNode == null) {
            return new Arity(0, 0, false);
        }

        final String[] keywordArguments;
        final int requiredKeywordArgumentsCount;

        if (parametersNode.keywords.length > 0) {
            final List<String> requiredKeywords = new ArrayList<>();
            final List<String> optionalKeywords = new ArrayList<>();

            for (var node : parametersNode.keywords) {
                if (node instanceof Nodes.RequiredKeywordParameterNode required) {
                    requiredKeywords.add(required.name);
                } else if (node instanceof Nodes.OptionalKeywordParameterNode optional) {
                    optionalKeywords.add(optional.name);
                } else {
                    throw CompilerDirectives.shouldNotReachHere();
                }
            }

            final List<String> keywords = new ArrayList<>(requiredKeywords);
            keywords.addAll(optionalKeywords);

            keywordArguments = keywords.toArray(StringUtils.EMPTY_STRING_ARRAY);
            requiredKeywordArgumentsCount = requiredKeywords.size();
        } else {
            keywordArguments = Arity.NO_KEYWORDS;
            requiredKeywordArgumentsCount = 0;
        }

        final boolean hasRest;
        if (parametersNode.keyword_rest instanceof Nodes.ForwardingParameterNode) {
            hasRest = true;
        } else {
            hasRest = parametersNode.rest != null;
        }

        // blocks only can have implicit rest parameter (|a,|)
        final boolean isImplicitRest = parametersNode.rest instanceof Nodes.ImplicitRestNode;

        // NOTE: when ... parameter is present then YARP keeps ForwardingParameterNode in ParametersNode#keyword_rest field.
        //      So `parametersNode.keyword_rest != null` works correctly to check if there is a keyword rest argument.
        return new Arity(
                parametersNode.requireds.length,
                parametersNode.optionals.length,
                hasRest,
                isImplicitRest,
                parametersNode.posts.length,
                keywordArguments,
                requiredKeywordArgumentsCount,
                parametersNode.keyword_rest != null);
    }

    // parse Integer literal ourselves
    // See https://github.com/ruby/yarp/issues/1098
    private RubyNode translateIntegerLiteralString(String string) {
        final RubyNode rubyNode;
        TruffleString tstring = toTString(string);

        Object numeratorInteger = ConvertBytes.bytesToInum(RubyLanguage.getCurrentContext(), null, tstring,
                sourceEncoding,
                0,
                true);

        if (numeratorInteger instanceof Integer i) {
            rubyNode = new IntegerFixnumLiteralNode(i);
        } else if (numeratorInteger instanceof Long l) {
            rubyNode = new LongFixnumLiteralNode(l);
        } else if (numeratorInteger instanceof RubyBignum bignum) {
            rubyNode = new ObjectLiteralNode(bignum);
        } else {
            throw CompilerDirectives.shouldNotReachHere(numeratorInteger.getClass().getName());
        }

        return rubyNode;
    }

}
