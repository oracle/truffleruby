/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.graalvm.collections.Pair;
import org.truffleruby.RubyLanguage;
import org.truffleruby.collections.CachedSupplier;
import org.truffleruby.core.IsNilNode;
import org.truffleruby.core.cast.SplatCastNode;
import org.truffleruby.core.cast.SplatCastNode.NilBehavior;
import org.truffleruby.core.cast.SplatCastNodeGen;
import org.truffleruby.core.proc.ProcCallTargets;
import org.truffleruby.core.proc.ProcType;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyLambdaRootNode;
import org.truffleruby.language.RubyMethodRootNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyProcRootNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.arguments.CheckKeywordArgumentNode;
import org.truffleruby.language.arguments.MissingArgumentBehavior;
import org.truffleruby.language.arguments.NewReadPreArgumentNode;
import org.truffleruby.language.arguments.ReadArgumentsNode;
import org.truffleruby.language.arguments.ReadPreArgumentNode;
import org.truffleruby.language.arguments.ShouldDestructureNode;
import org.truffleruby.language.control.AndNode;
import org.truffleruby.language.control.DynamicReturnNode;
import org.truffleruby.language.control.IfElseNode;
import org.truffleruby.language.control.InvalidReturnNode;
import org.truffleruby.language.control.NotNode;
import org.truffleruby.language.control.ReturnID;
import org.truffleruby.language.control.SequenceNode;
import org.truffleruby.language.locals.FlipFlopStateNode;
import org.truffleruby.language.locals.LocalVariableType;
import org.truffleruby.language.locals.ReadLocalVariableNode;
import org.truffleruby.language.locals.WriteFrameSlotNode;
import org.truffleruby.language.locals.WriteFrameSlotNodeGen;
import org.truffleruby.language.locals.WriteLocalVariableNode;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.methods.BlockDefinitionNode;
import org.truffleruby.language.methods.Split;
import org.truffleruby.language.supercall.ReadSuperArgumentsNode;
import org.truffleruby.language.supercall.ReadZSuperArgumentsNode;
import org.truffleruby.language.supercall.SuperCallNode;
import org.truffleruby.language.supercall.ZSuperOutsideMethodNode;
import org.truffleruby.language.threadlocal.MakeSpecialVariableStorageNode;
import org.truffleruby.parser.ast.ArgsParseNode;
import org.truffleruby.parser.ast.MethodDefParseNode;
import org.truffleruby.parser.ast.ParseNode;
import org.truffleruby.parser.ast.SuperParseNode;
import org.truffleruby.parser.ast.UnnamedRestArgParseNode;
import org.truffleruby.parser.ast.ZSuperParseNode;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

public class MethodTranslator extends BodyTranslator {

    private final ArgsParseNode argsNode;
    private final boolean isBlock;
    private final boolean shouldLazyTranslate;

    /** If this translates a literal block (but not a stabby lambda), this holds the name of the method to which the
     * block was passed. */
    private final String methodNameForBlock;

    public MethodTranslator(
            RubyLanguage language,
            BodyTranslator parent,
            TranslatorEnvironment environment,
            boolean isBlock,
            Source source,
            ParserContext parserContext,
            Node currentNode,
            ArgsParseNode argsNode,
            String methodNameForBlock,
            RubyDeferredWarnings rubyWarnings) {
        super(language, parent, environment, source, parserContext, currentNode, rubyWarnings);
        this.isBlock = isBlock;
        this.argsNode = argsNode;
        this.methodNameForBlock = methodNameForBlock;

        if (parserContext == ParserContext.EVAL || environment.getParseEnvironment().isCoverageEnabled()) {
            shouldLazyTranslate = false;
        } else if (language.getSourcePath(source).startsWith(language.coreLoadPath)) {
            shouldLazyTranslate = language.options.LAZY_TRANSLATION_CORE;
        } else {
            shouldLazyTranslate = language.options.LAZY_TRANSLATION_USER;
        }
    }

    public BlockDefinitionNode compileBlockNode(SourceIndexLength sourceSection, ParseNode bodyNode,
            boolean isStabbyLambda, String[] variables) {
        declareArguments();
        final Arity arity = argsNode.getArity();
        final Arity arityForCheck;

        /* If you have a block with parameters |a,| Ruby checks the arity as if was minimum 1, maximum 1 like |a|.
         * That's counter-intuitive - as you'd expect the anonymous rest argument to cause it to have no maximum. It
         * differs from |a| for non-lambda Procs where it causes destructuring. See
         * https://github.com/jruby/jruby/blob/324cd78c/core/src/main/java/org/jruby/runtime/Signature.java#L117-L120 */
        if (argsNode.getRestArgNode() instanceof UnnamedRestArgParseNode &&
                !((UnnamedRestArgParseNode) argsNode.getRestArgNode()).isStar()) {
            arityForCheck = arity.withRest(false);
        } else {
            arityForCheck = arity;
        }

        final RubyNode loadArguments = loadArguments(sourceSection, new LoadArgumentsTranslator(
                currentNode,
                argsNode,
                language,
                source,
                parserContext,
                !isStabbyLambda,
                false,
                this));

        final RubyNode preludeProc = !isStabbyLambda
                ? preludeProc(sourceSection, isStabbyLambda, arity, loadArguments)
                : null; // proc will never compiled for stabby lambdas

        if (!translatingForStatement) {
            // Make sure to declare block-local variables
            for (String var : variables) {
                environment.declareVar(var);
            }
        }

        final RubyNode body = translateNodeOrNil(sourceSection, bodyNode).simplifyAsTailExpression();

        final boolean methodCalledLambda = !isStabbyLambda && methodNameForBlock.equals("lambda");
        final boolean emitLambda = isStabbyLambda || methodCalledLambda;

        final Supplier<RootCallTarget> procCompiler = procCompiler(
                sourceSection,
                source,
                preludeProc,
                body,
                methodCalledLambda,
                language,
                environment);

        final Supplier<RootCallTarget> lambdaCompiler = lambdaCompiler(
                sourceSection,
                source,
                isStabbyLambda,
                arityForCheck,
                loadArguments,
                body,
                emitLambda,
                language,
                environment);

        Object frameOnStackMarkerSlot;

        if (emitLambda || frameOnStackMarkerSlotStack.isEmpty()) {
            frameOnStackMarkerSlot = null;
        } else {
            frameOnStackMarkerSlot = frameOnStackMarkerSlotStack.peek();

            if (frameOnStackMarkerSlot == BAD_FRAME_SLOT) {
                frameOnStackMarkerSlot = null;
            }
        }

        final ProcCallTargets callTargets;
        if (isStabbyLambda) {
            final RootCallTarget callTarget = lambdaCompiler.get();
            callTargets = new ProcCallTargets(callTarget, callTarget, null);
        } else if (methodNameForBlock.equals("lambda")) {
            callTargets = new ProcCallTargets(null, lambdaCompiler.get(), procCompiler);
        } else {
            callTargets = new ProcCallTargets(procCompiler.get(), null, lambdaCompiler);
        }

        final BlockDefinitionNode ret = new BlockDefinitionNode(
                emitLambda ? ProcType.LAMBDA : ProcType.PROC,
                environment.getSharedMethodInfo(),
                callTargets,
                environment.getBreakID(),
                (FrameSlot) frameOnStackMarkerSlot);
        ret.unsafeSetSourceSection(sourceSection);
        return ret;
    }

    private RubyNode preludeProc(
            SourceIndexLength sourceSection,
            boolean isStabbyLambda,
            Arity arity,
            RubyNode loadArguments) {

        final RubyNode preludeProc;
        if (shouldConsiderDestructuringArrayArg(arity)) {
            final RubyNode readArrayNode = profileArgument(
                    language,
                    ReadPreArgumentNode.create(0, MissingArgumentBehavior.RUNTIME_ERROR));
            final SplatCastNode castArrayNode = SplatCastNodeGen
                    .create(language, NilBehavior.NIL, true, readArrayNode);
            castArrayNode.doNotCopy();

            final FrameSlot arraySlot = environment.declareVar(environment.allocateLocalTemp("destructure"));
            final RubyNode writeArrayNode = new WriteLocalVariableNode(arraySlot, castArrayNode);

            final LoadArgumentsTranslator destructureArgumentsTranslator = new LoadArgumentsTranslator(
                    currentNode,
                    argsNode,
                    language,
                    source,
                    parserContext,
                    !isStabbyLambda,
                    false,
                    this);
            destructureArgumentsTranslator.pushArraySlot(arraySlot);

            final RubyNode allDestructureArguments = loadArguments(sourceSection, destructureArgumentsTranslator);

            final RubyNode arrayWasNotNil = sequence(
                    sourceSection,
                    Arrays.asList(
                            writeArrayNode,
                            new NotNode(
                                    new IsNilNode(
                                            new ReadLocalVariableNode(LocalVariableType.FRAME_LOCAL, arraySlot)))));

            final RubyNode shouldDestructureAndArrayWasNotNil = new AndNode(
                    ShouldDestructureNode.create(),
                    arrayWasNotNil);

            preludeProc = new IfElseNode(
                    shouldDestructureAndArrayWasNotNil,
                    allDestructureArguments,
                    loadArguments);
        } else {
            preludeProc = loadArguments;
        }
        return preludeProc;
    }

    private static Supplier<RootCallTarget> procCompiler(
            SourceIndexLength sourceSection,
            Source source,
            RubyNode preludeProc,
            RubyNode body,
            boolean methodCalledLambda,
            RubyLanguage language,
            TranslatorEnvironment environment) {

        // We construct the supplier in a static method to make sure we do not accidentally capture the
        // translator and other unwanted objects.

        return () -> {
            final RubyNode bodyForProc = methodCalledLambda
                    ? NodeUtil.cloneNode(body) // previously compiled as lambda, must copy
                    : body;

            final RubyNode bodyProc = composeBody(environment, sourceSection, preludeProc, bodyForProc);

            final RubyProcRootNode newRootNodeForProcs = new RubyProcRootNode(
                    language,
                    translateSourceSection(source, sourceSection),
                    environment.getFrameDescriptor(),
                    environment.getSharedMethodInfo(),
                    bodyProc,
                    Split.HEURISTIC,
                    environment.getReturnID());

            final RootCallTarget callTarget = newRootNodeForProcs.getCallTarget();

            if (methodCalledLambda) {
                // The block was previously compiled as lambda, we must rewrite the return nodes to InvalidReturnNode,
                // but only if the proc is within a lambda body (otherwise the returns are still valid, but return from
                // the surrounding function instead of returning from the lambda).
                //
                // Note that the compilation to lambda does not alter the original returnID (instead it's "hijacked"
                // and used in RubyLambdaRootNode).
                //
                // This needs to run after nodes are adopted for replace() to work and nodes to know their parent.
                for (DynamicReturnNode returnNode : NodeUtil
                        .findAllNodeInstances(bodyForProc, DynamicReturnNode.class)) {
                    if (returnNode.returnID == ReturnID.MODULE_BODY) {
                        returnNode.replace(new InvalidReturnNode(returnNode.value));
                    }
                }
            }

            return callTarget;
        };
    }

    private static Supplier<RootCallTarget> lambdaCompiler(
            SourceIndexLength sourceSection,
            Source source,
            boolean isStabbyLambda,
            Arity arityForCheck,
            RubyNode loadArguments,
            RubyNode body,
            boolean emitLambda,
            RubyLanguage language,
            TranslatorEnvironment environment) {

        // We construct the supplier in a static method to make sure we do not accidentally capture the
        // translator and other unwanted objects.

        return () -> {
            final RubyNode bodyForLambda = emitLambda
                    // Stabby lambda: the proc compiler will never be called, safe to not copy.
                    // Method named lambda: if conversion to proc needed, will copy in the proc compiler & reverse the
                    //   return transformation.
                    ? body
                    : NodeUtil.cloneNode(body);

            final RubyNode preludeLambda = isStabbyLambda ? loadArguments : NodeUtil.cloneNode(loadArguments);

            final RubyNode bodyLambda = composeBody(environment, sourceSection, preludeLambda, bodyForLambda);

            final RubyLambdaRootNode newRootNodeForLambdas = new RubyLambdaRootNode(
                    language,
                    translateSourceSection(source, sourceSection),
                    environment.getFrameDescriptor(),
                    environment.getSharedMethodInfo(),
                    bodyLambda,
                    Split.HEURISTIC,
                    environment.getReturnID(), // "hijack" return ID
                    environment.getBreakID(),
                    arityForCheck);

            final RootCallTarget callTarget = newRootNodeForLambdas.getCallTarget();

            if (!isStabbyLambda) {
                // If we end up executing this block as a lambda, but don't know it statically, e.g., `lambda {}` or
                // `define_method(:foo, proc {})`), then returns are always valid and return from that lambda.
                // This needs to run after nodes are adopted for replace() to work and nodes to know their parent.
                for (InvalidReturnNode returnNode : NodeUtil
                        .findAllNodeInstances(bodyForLambda, InvalidReturnNode.class)) {
                    returnNode.replace(new DynamicReturnNode(environment.getReturnID(), returnNode.value));
                }
            }

            return callTarget;
        };
    }

    private boolean shouldConsiderDestructuringArrayArg(Arity arity) {
        if (arity.hasKeywordsRest()) {
            return true;
        }
        // If we do not accept any arguments or only one required, there's never any need to destructure
        if (!arity.hasRest() && arity.getOptional() == 0 && arity.getRequired() <= 1) {
            return false;
            // If there are only a rest argument and optional arguments, there is no need to destructure.
            // Because the first optional argument (or the rest if no optional) will take the whole array.
        } else {
            return !arity.hasRest() || arity.getRequired() != 0;
        }
    }

    private static RubyNode composeBody(TranslatorEnvironment environment, SourceIndexLength preludeSourceSection,
            RubyNode prelude, RubyNode body) {
        final SourceIndexLength sourceSection = enclosing(preludeSourceSection, body);

        body = sequence(sourceSection, Arrays.asList(prelude, body));

        if (environment.getFlipFlopStates().size() > 0) {
            body = sequence(sourceSection, Arrays.asList(initFlipFlopStates(environment, sourceSection), body));
        }

        return body;
    }

    // This is the root of all the argument unload - including keyword arguments
    public RubyNode compileMethodBody(SourceIndexLength sourceSection, ParseNode bodyNode) {
        declareArguments();

        final LoadArgumentsTranslator translator = new LoadArgumentsTranslator(
                currentNode,
                argsNode,
                language,
                source,
                parserContext,
                false,
                true,
                this);

        RubyNode loadArguments = loadArguments(sourceSection, translator);

        RubyNode body = translateNodeOrNil(sourceSection, bodyNode).simplifyAsTailExpression();

        body = sequence(sourceSection, Arrays.asList(new MakeSpecialVariableStorageNode(), loadArguments, body));

        if (environment.getFlipFlopStates().size() > 0) {
            body = sequence(sourceSection, Arrays.asList(initFlipFlopStates(environment, sourceSection), body));
        }

        return body;
    }

    RubyNode loadArguments(SourceIndexLength sourceSection, LoadArgumentsTranslator translator) {
        translator.acceptKeywordArguments();

        // Set keyword argument to :missing_default_keyword_argument
        final Map<String, WriteFrameSlotNode> expectedKeywords = new HashMap<>();
        final FrameSlot[] keywordParameterFrameSlots = new FrameSlot[translator.defaults.size()];
        final List<CheckKeywordArgumentNode> assignDefaultNodes = new ArrayList<>();
        List<Pair<FrameSlot, RubyNode>> defaults = translator.defaults;
        for (int i = 0, defaultsSize = defaults.size(); i < defaultsSize; i++) {
            Pair<FrameSlot, RubyNode> defaultPair = defaults.get(i);
            WriteFrameSlotNode writeFrameSlot = WriteFrameSlotNodeGen.create(defaultPair.getLeft());
            expectedKeywords.put((String) defaultPair.getLeft().getIdentifier(), writeFrameSlot);
            keywordParameterFrameSlots[i] = defaultPair.getLeft();
            final RubySymbol symbol = language.getSymbol((String) defaultPair.getLeft().getIdentifier());
            assignDefaultNodes.add(
                    new CheckKeywordArgumentNode(
                            defaultPair.getLeft(),
                            translator.getRequired(),
                            defaultPair.getRight(),
                            symbol));
        }

        // Load positional keyword arguments
        final RubyNode loadArguments = translator.translateNonKeywordArguments();

        final Arity arity = argsNode.getArity();
        final boolean needsExpandedHash = arity.hasRest() || arity.hasKeywordsRest() || arity.hasOptional() ||
                !arity.hasKeywords(); /* unfortunate that this is set by optionals? can we do better? todo */

        final WriteFrameSlotNode[] writePreArgumentNode = new WriteFrameSlotNode[translator.preArgumentFrameSlots
                .size()];
        for (int n = 0; n < writePreArgumentNode.length; n++) {
            writePreArgumentNode[n] = WriteFrameSlotNodeGen.create(translator.preArgumentFrameSlots.get(n));
        }

        final WriteFrameSlotNode[] writeLocalAssignmentNode = new WriteFrameSlotNode[translator.localAssignmentFrameSlot
                .size()];
        for (int n = 0; n < writeLocalAssignmentNode.length; n++) {
            writeLocalAssignmentNode[n] = WriteFrameSlotNodeGen.create(translator.localAssignmentFrameSlot.get(n));
        }

        final RubyNode newLoadKeywordArguments = ReadArgumentsNode
                .create(
                        translator.selfIdentifier,
                        translator.runBlockKWArgs,
                        translator.requiredPositionalArgs,
                        translator.keyRestArg,
                        translator.restArg,
                        translator.readPostArgument,
                        translator.optionalArg,
                        expectedKeywords,
                        needsExpandedHash,
                        arity,
                        keywordParameterFrameSlots,
                        assignDefaultNodes.toArray(CheckKeywordArgumentNode.EMPTY_ARRAY),
                        translator.localAssignmentReadNodes.toArray(RubyNode.EMPTY_ARRAY),
                        writePreArgumentNode,
                        translator.readPreArgumentNodes.toArray(NewReadPreArgumentNode.EMPTY_ARRAY),
                        writeLocalAssignmentNode,
                        translator.preOptNodes);

        final List<RubyNode> nodes = new ArrayList<>();
        nodes.add(newLoadKeywordArguments); // do the new-style load of keyword arguments
        nodes.add(loadArguments); // load positional arguments
        return sequence(sourceSection, nodes);
    }

    private RubyMethodRootNode translateMethodNode(SourceIndexLength sourceSection, MethodDefParseNode defNode,
            ParseNode bodyNode) {
        final SourceIndexLength sourceIndexLength = defNode.getPosition();
        final SourceSection fullMethodSourceSection = sourceIndexLength.toSourceSection(source);
        return new RubyMethodRootNode(
                language,
                fullMethodSourceSection,
                environment.getFrameDescriptor(),
                environment.getSharedMethodInfo(),
                compileMethodBody(sourceSection, bodyNode),
                Split.HEURISTIC,
                environment.getReturnID(),
                argsNode.getArity());
    }

    public CachedSupplier<RootCallTarget> buildMethodNodeCompiler(SourceIndexLength sourceSection,
            MethodDefParseNode defNode, ParseNode bodyNode) {

        if (shouldLazyTranslate) {
            return new CachedSupplier<>(() -> {
                return translateMethodNode(sourceSection, defNode, bodyNode).getCallTarget();
            });
        } else {
            final RubyMethodRootNode root = translateMethodNode(sourceSection, defNode, bodyNode);
            return new CachedSupplier<>(() -> root.getCallTarget());
        }
    }

    private void declareArguments() {
        final ParameterCollector parameterCollector = new ParameterCollector();
        argsNode.accept(parameterCollector);

        for (String parameter : parameterCollector.getParameters()) {
            environment.declareVar(parameter);
        }
    }

    @Override
    public RubyNode visitSuperNode(SuperParseNode node) {
        final SourceIndexLength sourceSection = node.getPosition();

        final ArgumentsAndBlockTranslation argumentsAndBlock = translateArgumentsAndBlock(
                sourceSection,
                node.getIterNode(),
                node.getArgsNode(),
                environment.getMethodName());

        final RubyNode arguments = new ReadSuperArgumentsNode(
                argumentsAndBlock.getArguments(),
                argumentsAndBlock.isSplatted());
        final RubyNode block = executeOrInheritBlock(argumentsAndBlock.getBlock(), node);

        RubyNode callNode = new SuperCallNode(arguments, block, argumentsAndBlock.getKeywordArgumentsDescriptor());
        callNode = wrapCallWithLiteralBlock(argumentsAndBlock, callNode);

        return withSourceSection(sourceSection, callNode);
    }

    @Override
    public RubyNode visitZSuperNode(ZSuperParseNode node) {
        final SourceIndexLength sourceSection = node.getPosition();

        currentCallMethodName = environment.getMethodName();

        final ArgumentsAndBlockTranslation argumentsAndBlock = translateArgumentsAndBlock(
                sourceSection,
                node.getIterNode(),
                null,
                environment.getMethodName());

        boolean insideDefineMethod = false;
        MethodTranslator methodArgumentsTranslator = this;
        while (methodArgumentsTranslator.isBlock) {
            if (!(methodArgumentsTranslator.parent instanceof MethodTranslator)) {
                return withSourceSection(sourceSection, new ZSuperOutsideMethodNode(insideDefineMethod));
            } else if (methodArgumentsTranslator.currentCallMethodName != null &&
                    methodArgumentsTranslator.currentCallMethodName.equals("define_method")) {
                insideDefineMethod = true;
            }
            methodArgumentsTranslator = (MethodTranslator) methodArgumentsTranslator.parent;
        }

        final ReloadArgumentsTranslator reloadTranslator = new ReloadArgumentsTranslator(
                language,
                source,
                parserContext,
                currentNode,
                this);

        final ArgsParseNode argsNode = methodArgumentsTranslator.argsNode;
        final SequenceNode reloadSequence = (SequenceNode) reloadTranslator.visitArgsNode(argsNode);

        final RubyNode arguments = new ReadZSuperArgumentsNode(
                reloadTranslator.getRestParameterIndex(),
                reloadSequence.getSequence());
        final RubyNode block = executeOrInheritBlock(argumentsAndBlock.getBlock(), node);

        RubyNode callNode = new SuperCallNode(arguments, block, argumentsAndBlock.getKeywordArgumentsDescriptor());
        callNode = wrapCallWithLiteralBlock(argumentsAndBlock, callNode);

        return withSourceSection(sourceSection, callNode);
    }

    private RubyNode executeOrInheritBlock(RubyNode blockNode, ParseNode callNode) {
        if (blockNode != null) {
            return blockNode;
        } else {
            return environment.findLocalVarOrNilNode(TranslatorEnvironment.METHOD_BLOCK_NAME, callNode.getPosition());
        }
    }

    @Override
    protected FlipFlopStateNode createFlipFlopState(SourceIndexLength sourceSection, int depth) {
        if (isBlock) {
            return parent.createFlipFlopState(sourceSection, depth + 1);
        } else {
            return super.createFlipFlopState(sourceSection, depth);
        }
    }

}
