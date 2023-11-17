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
import org.truffleruby.core.array.ArrayConcatNode;
import org.truffleruby.core.array.ArrayLiteralNode;
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
import org.truffleruby.core.rescue.AssignRescueVariableNode;
import org.truffleruby.core.string.FrozenStrings;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.core.string.InterpolatedStringNode;
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
import org.truffleruby.language.arguments.NoKeywordArgumentsDescriptor;
import org.truffleruby.language.arguments.ProfileArgumentNodeGen;
import org.truffleruby.language.arguments.ReadSelfNode;
import org.truffleruby.language.constants.ReadConstantNode;
import org.truffleruby.language.constants.ReadConstantWithDynamicScopeNode;
import org.truffleruby.language.constants.ReadConstantWithLexicalScopeNode;
import org.truffleruby.language.constants.WriteConstantNode;
import org.truffleruby.language.control.AndNodeGen;
import org.truffleruby.language.control.BreakNode;
import org.truffleruby.language.control.DynamicReturnNode;
import org.truffleruby.language.control.FrameOnStackNode;
import org.truffleruby.language.control.IfElseNode;
import org.truffleruby.language.control.IfElseNodeGen;
import org.truffleruby.language.control.IfNodeGen;
import org.truffleruby.language.control.InvalidReturnNode;
import org.truffleruby.language.control.LocalReturnNode;
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
import org.truffleruby.parser.parser.ParserSupport;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

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
    private boolean translatingForStatement = false;

    private String currentCallMethodName = null;

    public YARPTranslator(
            RubyLanguage language,
            TranslatorEnvironment environment,
            byte[] sourceBytes,
            Source source,
            ParserContext parserContext,
            Node currentNode,
            RubyDeferredWarnings rubyWarnings) {
        this.language = language;
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

    @Override
    public RubyNode visitAlternationPatternNode(Nodes.AlternationPatternNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitAndNode(Nodes.AndNode node) {
        RubyNode left = node.left.accept(this);
        RubyNode right = node.right.accept(this);

        RubyNode rubyNode = AndNodeGen.create(left, right);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitArgumentsNode(Nodes.ArgumentsNode node) {
        final Nodes.Node[] values = node.arguments;

        if (values.length == 1) {
            return values[0].accept(this);
        }

        final RubyNode[] translatedValues = translate(values);
        final RubyNode rubyNode = ArrayLiteralNode.create(language, translatedValues);
        assignNodePositionInSource(node, rubyNode);

        return rubyNode;
    }

    @Override
    public RubyNode visitArrayNode(Nodes.ArrayNode node) {
        RubyNode[] elements = translate(node.elements);
        RubyNode rubyNode = ArrayLiteralNode.create(language, elements);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitArrayPatternNode(Nodes.ArrayPatternNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitBackReferenceReadNode(Nodes.BackReferenceReadNode node) {
        final RubyNode rubyNode = ReadGlobalVariableNodeGen.create(node.name);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
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
        final RubyNode[] handlingClasses = translate(exceptionNodesArray);

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

    @Override
    public RubyNode visitBlockArgumentNode(Nodes.BlockArgumentNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitBlockNode(Nodes.BlockNode node) {
        return translateBlockAndLambda(node, node.parameters, node.body, node.locals);
    }

    private RubyNode translateBlockAndLambda(Nodes.Node node, Nodes.BlockParametersNode blockParameters,
            Nodes.Node body, String[] locals) {
        final boolean isStabbyLambda = node instanceof Nodes.LambdaNode;
        final boolean hasOwnScope = true;

        TranslatorEnvironment methodParent = environment.getSurroundingMethodEnvironment();
        final String methodName = methodParent.getMethodName();

        final int blockDepth = environment.getBlockDepth() + 1;

        final Nodes.ParametersNode parameters;
        if (blockParameters != null) {
            parameters = blockParameters.parameters;
        } else {
            // handle numbered parameters
            int max = 0;

            // don't rely on locals order and find the largest index
            for (var name : locals) {
                if (ParserSupport.isNumberedParameter(name)) {
                    int n = name.charAt(1) - '0';
                    if (n > max) {
                        max = n;
                    }
                }
            }

            if (max > 0) {
                final var requireds = new Nodes.RequiredParameterNode[max];
                for (int i = 1; i <= max; i++) {
                    requireds[i - 1] = new Nodes.RequiredParameterNode("_" + i, 0, 0);
                }
                parameters = new Nodes.ParametersNode(requireds, Nodes.Node.EMPTY_ARRAY, null, Nodes.Node.EMPTY_ARRAY,
                        Nodes.Node.EMPTY_ARRAY, null, null, 0, 0);
            } else {
                // no numbered parameters
                parameters = null;
            }
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
        final YARPBlockNodeTranslator methodCompiler = new YARPBlockNodeTranslator(
                language,
                newEnvironment,
                sourceBytes,
                source,
                parameters,
                arity,
                currentCallMethodName);

        methodCompiler.frameOnStackMarkerSlotStack = frameOnStackMarkerSlotStack;

        final RubyNode rubyNode = methodCompiler.compileBlockNode(
                body,
                locals,
                isStabbyLambda,
                getSourceSection(node));

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
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
        assignNodePositionInSource(node, rubyNode);

        return rubyNode;
    }

    @Override
    public RubyNode visitCallAndWriteNode(Nodes.CallAndWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitCallNode(Nodes.CallNode node) {
        var methodName = node.name;
        var receiver = node.receiver == null ? new SelfNode() : node.receiver.accept(this);
        final Nodes.Node[] arguments;

        if (node.arguments == null) {
            arguments = Nodes.Node.EMPTY_ARRAY;
        } else {
            arguments = node.arguments.arguments;
        }

        var translatedArguments = translate(arguments);
        var argumentsDescriptor = getKeywordArgumentsDescriptor(arguments);

        // If the receiver is explicit or implicit 'self' then we can call private methods
        final boolean ignoreVisibility = node.receiver == null || node.receiver instanceof Nodes.SelfNode;
        final boolean isVariableCall = node.isVariableCall();
        // this check isn't accurate and doesn't handle cases like #===, #!=, a.foo=(42)
        // the issue is tracked in https://github.com/ruby/prism/issues/1715
        final boolean isAttrAssign = methodName.endsWith("=");
        final boolean isSafeNavigation = node.isSafeNavigation();

        boolean isSplatted = false;
        for (var n : arguments) { // check if there is splat operator in the arguments list
            if (n instanceof Nodes.SplatNode) {
                isSplatted = true;
                break;
            }
        }

        // No need to copy the array for call(*splat), the elements will be copied to the frame arguments
        if (isSplatted && translatedArguments.length == 1 &&
                translatedArguments[0] instanceof SplatCastNode splatNode) {
            splatNode.doNotCopy();
        }

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

        final RubyNode blockNode;
        final int frameOnStackMarkerSlot;
        if (node.block != null) {
            if (node.block instanceof Nodes.BlockNode) {
                // a() {}
                final String oldCurrentCallMethodName = currentCallMethodName;
                currentCallMethodName = methodName;

                frameOnStackMarkerSlot = environment.declareLocalTemp("frame_on_stack_marker");
                frameOnStackMarkerSlotStack.push(frameOnStackMarkerSlot);

                try {
                    blockNode = node.block.accept(this);
                } finally {
                    frameOnStackMarkerSlotStack.pop();
                }

                currentCallMethodName = oldCurrentCallMethodName;
            } else if (node.block instanceof Nodes.BlockArgumentNode blockArgument) {
                // def a(&) b(&) end
                assert blockArgument.expression != null; // Ruby 3.1's anonymous block parameter, that we don't support yet

                // a(&:b)
                blockNode = ToProcNodeGen.create(blockArgument.expression.accept(this));
                frameOnStackMarkerSlot = NO_FRAME_ON_STACK_MARKER;
            } else {
                throw CompilerDirectives.shouldNotReachHere();
            }
        } else {
            blockNode = null;
            frameOnStackMarkerSlot = NO_FRAME_ON_STACK_MARKER;
        }

        final var callNodeParameters = new RubyCallNodeParameters(
                receiver,
                methodName,
                blockNode,
                argumentsDescriptor,
                translatedArguments,
                isSplatted,
                ignoreVisibility,
                isVariableCall,
                isSafeNavigation,
                isAttrAssign);
        final RubyNode callNode = language.coreMethodAssumptions.createCallNode(callNodeParameters);
        final RubyNode rubyNode;

        // wrap call node with literal block
        if (blockNode instanceof BlockDefinitionNode) {
            // if we have a literal block, `break` breaks out of this call site
            final var frameOnStackNode = new FrameOnStackNode(callNode, frameOnStackMarkerSlot);
            final var blockDef = (BlockDefinitionNode) blockNode;
            rubyNode = new CatchBreakNode(blockDef.getBreakID(), frameOnStackNode, false);
        } else {
            rubyNode = callNode;
        }

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    private ArgumentsDescriptor getKeywordArgumentsDescriptor(Nodes.Node[] arguments) {
        if (arguments.length == 0) {
            return NoKeywordArgumentsDescriptor.INSTANCE;
        }

        Nodes.Node last = arguments[arguments.length - 1];

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
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitCallOrWriteNode(Nodes.CallOrWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitCapturePatternNode(Nodes.CapturePatternNode node) {
        return defaultVisit(node);
    }

    @Override
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
                    final RubyNode predicateNode = createCallNode(receiver, "any?");

                    // create `if` node
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

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitClassVariableAndWriteNode(Nodes.ClassVariableAndWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitClassVariableOperatorWriteNode(Nodes.ClassVariableOperatorWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitClassVariableOrWriteNode(Nodes.ClassVariableOrWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitClassVariableReadNode(Nodes.ClassVariableReadNode node) {
        final RubyNode rubyNode = new ReadClassVariableNode(
                getLexicalScopeNode("class variable lookup", node),
                node.name);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitClassVariableWriteNode(Nodes.ClassVariableWriteNode node) {
        final RubyNode rhs = node.value.accept(this);
        final RubyNode rubyNode = new WriteClassVariableNode(
                getLexicalScopeNode("set dynamic class variable", node),
                node.name,
                rhs);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitClassVariableTargetNode(Nodes.ClassVariableTargetNode node) {
        final RubyNode rubyNode = new WriteClassVariableNode(
                getLexicalScopeNode("set dynamic class variable", node),
                node.name,
                null);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitConstantAndWriteNode(Nodes.ConstantAndWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitConstantOperatorWriteNode(Nodes.ConstantOperatorWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitConstantOrWriteNode(Nodes.ConstantOrWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitConstantPathAndWriteNode(Nodes.ConstantPathAndWriteNode node) {
        return defaultVisit(node);
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

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitConstantPathOperatorWriteNode(Nodes.ConstantPathOperatorWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitConstantPathOrWriteNode(Nodes.ConstantPathOrWriteNode node) {
        return defaultVisit(node);
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

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
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

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
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

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitConstantWriteNode(Nodes.ConstantWriteNode node) {
        final RubyNode value = node.value.accept(this);
        final RubyNode moduleNode = getLexicalScopeModuleNode("set dynamic constant", node);
        final RubyNode rubyNode = new WriteConstantNode(node.name, moduleNode, value);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitConstantTargetNode(Nodes.ConstantTargetNode node) {
        final RubyNode moduleNode = getLexicalScopeModuleNode("set dynamic constant", node);
        final RubyNode rubyNode = new WriteConstantNode(node.name, moduleNode, null);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
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

        final var defNodeTranslator = new YARPDefNodeTranslator(
                language,
                newEnvironment,
                sourceBytes,
                source,
                parserContext,
                currentNode,
                rubyWarnings);
        final CachedLazyCallTargetSupplier callTargetSupplier = defNodeTranslator.buildMethodNodeCompiler(node, arity);

        final boolean isDefSingleton = singletonClassNode != null;

        RubyNode rubyNode = new LiteralMethodDefinitionNode(
                singletonClassNode,
                node.name,
                sharedMethodInfo,
                isDefSingleton,
                callTargetSupplier);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitDefinedNode(Nodes.DefinedNode node) {
        // Handle defined?(yield) explicitly otherwise it would raise SyntaxError
        if (node.value instanceof Nodes.YieldNode && isInvalidYield()) {
            final var nilNode = new NilLiteralNode();
            assignNodePositionInSource(node, nilNode);

            return nilNode;
        }

        final RubyNode value = node.value.accept(this);
        final RubyNode rubyNode = new DefinedNode(value);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitElseNode(Nodes.ElseNode node) {
        if (node.statements == null) {
            final RubyNode rubyNode = new NilLiteralNode();
            assignNodePositionInSource(node, rubyNode);
            return rubyNode;
        }
        return node.statements.accept(this);
    }

    @Override
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

    @Override
    public RubyNode visitEmbeddedVariableNode(Nodes.EmbeddedVariableNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitEnsureNode(Nodes.EnsureNode node) {
        if (node.statements == null) {
            final RubyNode rubyNode = new NilLiteralNode();
            assignNodePositionInSource(node, rubyNode);
            return rubyNode;
        }

        return node.statements.accept(this);
    }

    @Override
    public RubyNode visitFalseNode(Nodes.FalseNode node) {
        RubyNode rubyNode = new BooleanLiteralNode(false);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
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

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
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
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitForNode(Nodes.ForNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitForwardingArgumentsNode(Nodes.ForwardingArgumentsNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitForwardingSuperNode(Nodes.ForwardingSuperNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitGlobalVariableAndWriteNode(Nodes.GlobalVariableAndWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitGlobalVariableOperatorWriteNode(Nodes.GlobalVariableOperatorWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitGlobalVariableOrWriteNode(Nodes.GlobalVariableOrWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitGlobalVariableReadNode(Nodes.GlobalVariableReadNode node) {
        final RubyNode rubyNode = ReadGlobalVariableNodeGen.create(node.name);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitGlobalVariableWriteNode(Nodes.GlobalVariableWriteNode node) {
        final RubyNode value = node.value.accept(this);
        final RubyNode rubyNode = WriteGlobalVariableNodeGen.create(node.name, value);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitGlobalVariableTargetNode(Nodes.GlobalVariableTargetNode node) {
        final RubyNode rubyNode = WriteGlobalVariableNodeGen.create(node.name, null);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
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
            rubyNode = sequence(node, Arrays.asList(conditionNode, new NilLiteralNode()));
        }

        return rubyNode;
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

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitImplicitNode(Nodes.ImplicitNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitInNode(Nodes.InNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitIndexAndWriteNode(Nodes.IndexAndWriteNode node) {
        return super.visitIndexAndWriteNode(node);
    }

    @Override
    public RubyNode visitIndexOperatorWriteNode(Nodes.IndexOperatorWriteNode node) {
        return super.visitIndexOperatorWriteNode(node);
    }

    @Override
    public RubyNode visitIndexOrWriteNode(Nodes.IndexOrWriteNode node) {
        return super.visitIndexOrWriteNode(node);
    }

    @Override
    public RubyNode visitInstanceVariableAndWriteNode(Nodes.InstanceVariableAndWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitInstanceVariableOperatorWriteNode(Nodes.InstanceVariableOperatorWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitInstanceVariableOrWriteNode(Nodes.InstanceVariableOrWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitInstanceVariableReadNode(Nodes.InstanceVariableReadNode node) {
        final RubyNode rubyNode = new ReadInstanceVariableNode(node.name);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitInstanceVariableWriteNode(Nodes.InstanceVariableWriteNode node) {
        final RubyNode value = node.value.accept(this);
        final RubyNode rubyNode = WriteInstanceVariableNodeGen.create(node.name, value);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitInstanceVariableTargetNode(Nodes.InstanceVariableTargetNode node) {
        final RubyNode rubyNode = WriteInstanceVariableNodeGen.create(node.name, null);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitIntegerNode(Nodes.IntegerNode node) {
        // parse Integer literal ourselves
        // See https://github.com/ruby/yarp/issues/1098
        final String string = toString(node).replaceAll("_", "");

        final int radix;
        final int offset;

        if (node.isBinary()) {
            radix = 2;
            offset = 2;
        } else if (node.isHexadecimal()) {
            radix = 16;
            offset = 2;
        } else if (node.isDecimal()) {
            radix = 10;
            offset = (string.startsWith("0d") || string.startsWith("0D")) ? 2 : 0;
        } else if (node.isOctal()) {
            radix = 8;
            offset = (string.startsWith("0o") || string.startsWith("0O")) ? 2 : 1; // 0oXX, 0OXX, 0XX
        } else {
            throw CompilerDirectives.shouldNotReachHere();
        }

        final long value = Long.parseLong(string.substring(offset), radix);
        final RubyNode rubyNode = Translator.integerOrLongLiteralNode(value);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitInterpolatedMatchLastLineNode(Nodes.InterpolatedMatchLastLineNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitInterpolatedRegularExpressionNode(Nodes.InterpolatedRegularExpressionNode node) {
        return defaultVisit(node);
    }

    @Override
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

    @Override
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

    @Override
    public RubyNode visitInterpolatedXStringNode(Nodes.InterpolatedXStringNode node) {
        var stringNode = new Nodes.InterpolatedStringNode(node.parts, node.startOffset, node.length);
        final RubyNode string = stringNode.accept(this);
        final RubyNode rubyNode = createCallNode(new SelfNode(), "`", string);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitKeywordHashNode(Nodes.KeywordHashNode node) {
        // translate it like a HashNode, whether it is keywords or not is checked in getKeywordArgumentsDescriptor()
        final var hash = new Nodes.HashNode(node.elements, node.startOffset, node.length);
        return hash.accept(this);
    }

    @Override
    public RubyNode visitLambdaNode(Nodes.LambdaNode node) {
        return translateBlockAndLambda(node, node.parameters, node.body, node.locals);
    }

    @Override
    public RubyNode visitLocalVariableReadNode(Nodes.LocalVariableReadNode node) {
        final String name = node.name;

        final RubyNode rubyNode = environment.findLocalVarNode(name, null);
        assert rubyNode != null : name;

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitLocalVariableAndWriteNode(Nodes.LocalVariableAndWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitLocalVariableOperatorWriteNode(Nodes.LocalVariableOperatorWriteNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitLocalVariableOrWriteNode(Nodes.LocalVariableOrWriteNode node) {
        return defaultVisit(node);
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

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitLocalVariableTargetNode(Nodes.LocalVariableTargetNode node) {
        // TODO: this could be done more directly but the logic of visitLocalVariableWriteNode() needs to be simpler first
        return visitLocalVariableWriteNode(
                new Nodes.LocalVariableWriteNode(node.name, node.depth, null, node.startOffset, node.length));
    }

    @Override
    public RubyNode visitMatchLastLineNode(Nodes.MatchLastLineNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitMatchPredicateNode(Nodes.MatchPredicateNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitMatchRequiredNode(Nodes.MatchRequiredNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitMatchWriteNode(Nodes.MatchWriteNode node) {
        return defaultVisit(node);
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

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitMultiWriteNode(Nodes.MultiWriteNode node) {
        final RubyNode rubyNode;
        var translator = new YARPMultiWriteNodeTranslator(node, language, this);
        rubyNode = translator.translate();

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
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
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitNilNode(Nodes.NilNode node) {
        final RubyNode rubyNode = new NilLiteralNode();
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitNumberedReferenceReadNode(Nodes.NumberedReferenceReadNode node) {
        final RubyNode lastMatchNode = ReadGlobalVariableNodeGen.create("$~");
        final RubyNode rubyNode = new ReadMatchReferenceNodes.ReadNthMatchNode(lastMatchNode, node.number);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitOrNode(Nodes.OrNode node) {
        final RubyNode left = node.left.accept(this);
        final RubyNode right = node.right.accept(this);

        final RubyNode rubyNode = OrNodeGen.create(left, right);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitParenthesesNode(Nodes.ParenthesesNode node) {
        if (node.body == null) {
            final RubyNode rubyNode = new NilLiteralNode();
            assignNodePositionInSource(node, rubyNode);
            return rubyNode;
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
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitPreExecutionNode(Nodes.PreExecutionNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitProgramNode(Nodes.ProgramNode node) {
        return node.statements.accept(this);
    }

    @Override
    public RubyNode visitRangeNode(Nodes.RangeNode node) {
        return defaultVisit(node);
    }

    @Override
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
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitRegularExpressionNode(Nodes.RegularExpressionNode node) {
        return defaultVisit(node);
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

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitRescueNode(Nodes.RescueNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitRetryNode(Nodes.RetryNode node) {
        final RubyNode rubyNode = new RetryNode();
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
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

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitSelfNode(Nodes.SelfNode node) {
        final RubyNode rubyNode = new SelfNode();
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitSingletonClassNode(Nodes.SingletonClassNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitSourceEncodingNode(Nodes.SourceEncodingNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitSourceFileNode(Nodes.SourceFileNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitSourceLineNode(Nodes.SourceLineNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitSplatNode(Nodes.SplatNode node) {
        final RubyNode value = translateNodeOrNil(node.expression);
        final RubyNode rubyNode = SplatCastNodeGen.create(language, SplatCastNode.NilBehavior.CONVERT, false, value);
        assignNodePositionInSource(node, rubyNode);

        return rubyNode;
    }

    @Override
    public RubyNode visitStatementsNode(Nodes.StatementsNode node) {
        RubyNode[] rubyNodes = translate(node.body);
        return sequence(node, Arrays.asList(rubyNodes));
    }

    @Override
    public RubyNode visitStringConcatNode(Nodes.StringConcatNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitStringNode(Nodes.StringNode node) {
        final RubyNode rubyNode;
        final TruffleString tstring = TStringUtils.fromByteArray(node.unescaped, sourceEncoding);

        if (!node.isFrozen()) {
            final TruffleString cachedTString = language.tstringCache.getTString(tstring, sourceEncoding);
            rubyNode = new StringLiteralNode(cachedTString, sourceEncoding);
        } else {
            final ImmutableRubyString frozenString = language.getFrozenStringLiteral(tstring, sourceEncoding);
            rubyNode = new FrozenStringLiteralNode(frozenString, FrozenStrings.EXPRESSION);
        }

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitSuperNode(Nodes.SuperNode node) {
        return defaultVisit(node);
    }

    @Override
    public RubyNode visitSymbolNode(Nodes.SymbolNode node) {
        var tstring = TStringUtils.fromByteArray(node.unescaped, sourceEncoding);
        final RubySymbol symbol = language.getSymbol(tstring, sourceEncoding);
        final RubyNode rubyNode = new ObjectLiteralNode(symbol);

        assignNodePositionInSource(node, rubyNode);

        return rubyNode;
    }

    @Override
    public RubyNode visitTrueNode(Nodes.TrueNode node) {
        final RubyNode rubyNode = new BooleanLiteralNode(true);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitUndefNode(Nodes.UndefNode node) {
        final RubyNode[] names = translate(node.names);
        final RubyNode rubyNode = new ModuleNodes.UndefNode(names);
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
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
            rubyNode = sequence(node, Arrays.asList(conditionNode, new NilLiteralNode()));
        }

        return rubyNode;
    }

    @Override
    public RubyNode visitUntilNode(Nodes.UntilNode node) {
        final RubyNode rubyNode = translateWhileNode(node, node.predicate, node.statements, true,
                !node.isBeginModifier());
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitWhileNode(Nodes.WhileNode node) {
        final RubyNode rubyNode = translateWhileNode(node, node.predicate, node.statements, false,
                !node.isBeginModifier());
        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitXStringNode(Nodes.XStringNode node) {
        // TODO: pass flags, needs https://github.com/ruby/yarp/issues/1567
        var stringNode = new Nodes.StringNode((short) 0, node.unescaped, node.startOffset, node.length);
        final RubyNode string = stringNode.accept(this);
        final RubyNode rubyNode = createCallNode(new SelfNode(), "`", string);

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    @Override
    public RubyNode visitYieldNode(Nodes.YieldNode node) {
        return defaultVisit(node);
    }

    @Override
    protected RubyNode defaultVisit(Nodes.Node node) {
        throw new Error("Unknown node: " + node);
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
        assert nodes.length > 0;

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

        assignNodePositionInSource(node, rubyNode);
        return rubyNode;
    }

    private RubyNode translateRescueException(Nodes.Node exception) {
        final RubyNode rubyNode;

        if (exception instanceof Nodes.CallNode callNode) {
            rubyNode = translateCallTargetNode(callNode);
        } else {
            rubyNode = exception.accept(this);
        }

        final AssignableNode assignableNode = (AssignableNode) rubyNode;
        return new AssignRescueVariableNode(assignableNode);
    }

    public RubyNode translateCallTargetNode(Nodes.CallNode node) {
        // extra argument should be added before node translation
        // to trigger correctly replacement with inlined nodes (e.g. InlinedIndexSetNodeGen)
        // that relies on arguments count
        if (node.name.endsWith("=")) {
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
                argumentsNode = new Nodes.ArgumentsNode(arguments, (short) 0, 0, 0);
            } else {
                argumentsNode = new Nodes.ArgumentsNode(arguments, node.arguments.flags, node.arguments.startOffset,
                        node.arguments.length);
            }
            node = new Nodes.CallNode(node.receiver, argumentsNode, node.block, node.flags, node.name, node.startOffset,
                    node.length);
        }

        return node.accept(this);
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
                NoKeywordArgumentsDescriptor.INSTANCE,
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

    protected String toString(Nodes.Node node) {
        return TStringUtils.toJavaStringOrThrow(TruffleString.fromByteArrayUncached(sourceBytes, node.startOffset,
                node.length, sourceEncoding.tencoding, false), sourceEncoding);
    }

    protected String toString(byte[] bytes) {
        return TStringUtils.toJavaStringOrThrow(
                TruffleString.fromByteArrayUncached(bytes, sourceEncoding.tencoding, false), sourceEncoding);
    }

    protected SourceSection getSourceSection(Nodes.Node yarpNode) {
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

    protected static RubyNode sequence(Nodes.Node yarpNode, List<RubyNode> sequence) {
        assert !yarpNode.hasNewLineFlag() : "Expected node passed to sequence() to not have a newline flag";

        RubyNode sequenceNode = sequence(sequence);

        if (!sequenceNode.hasSource()) {
            assignNodePositionInSource(yarpNode, sequenceNode);
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

    // assign position based on a list of nodes (arguments list, exception classes list in a rescue section, etc)
    private void assignNodePositionInSource(Nodes.Node[] nodes, RubyNode rubyNode) {
        final Nodes.Node first = nodes[0];
        final Nodes.Node last = nodes[nodes.length - 1];

        final int length = last.startOffset - first.startOffset + last.length;
        rubyNode.unsafeSetSourceSection(first.startOffset, length);
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

        if (parametersNode.rest != null) {
            if (parametersNode.rest.name == null) {
                descriptors.add(new ArgumentDescriptor(ArgumentType.anonrest));
            } else {
                var descriptor = new ArgumentDescriptor(ArgumentType.rest, parametersNode.rest.name);
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
            // we don't support yet Ruby 3.1's anonymous block parameter
            assert parametersNode.block.name != null;
            descriptors.add(new ArgumentDescriptor(ArgumentType.block, parametersNode.block.name));
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

        // NOTE: when ... parameter is present then YARP keeps ForwardingParameterNode in ParametersNode#keyword_rest field.
        //      So `parametersNode.keyword_rest != null` works correctly to check if there is a keyword rest argument.
        return new Arity(
                parametersNode.requireds.length,
                parametersNode.optionals.length,
                hasRest,
                parametersNode.posts.length,
                keywordArguments,
                requiredKeywordArgumentsCount,
                parametersNode.keyword_rest != null);
    }

}
