/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import java.util.Arrays;

import org.truffleruby.RubyContext;
import org.truffleruby.collections.CachedSupplier;
import org.truffleruby.core.IsNilNode;
import org.truffleruby.core.cast.ArrayCastNodeGen;
import org.truffleruby.core.proc.ProcType;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.arguments.MissingArgumentBehavior;
import org.truffleruby.language.arguments.ReadPreArgumentNode;
import org.truffleruby.language.arguments.ShouldDestructureNode;
import org.truffleruby.language.control.AndNode;
import org.truffleruby.language.control.IfElseNode;
import org.truffleruby.language.control.NotNode;
import org.truffleruby.language.control.SequenceNode;
import org.truffleruby.language.locals.FlipFlopStateNode;
import org.truffleruby.language.locals.LocalVariableType;
import org.truffleruby.language.locals.ReadLocalVariableNode;
import org.truffleruby.language.locals.WriteLocalVariableNode;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.methods.BlockDefinitionNode;
import org.truffleruby.language.methods.CatchForLambdaNode;
import org.truffleruby.language.methods.CatchForMethodNode;
import org.truffleruby.language.methods.CatchForProcNode;
import org.truffleruby.language.methods.ExceptionTranslatingNode;
import org.truffleruby.language.methods.Split;
import org.truffleruby.language.methods.UnsupportedOperationBehavior;
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
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

public class MethodTranslator extends BodyTranslator {

    private final ArgsParseNode argsNode;
    private boolean isBlock;
    private final boolean shouldLazyTranslate;

    public MethodTranslator(
            RubyContext context,
            BodyTranslator parent,
            TranslatorEnvironment environment,
            boolean isBlock,
            Source source,
            ParserContext parserContext,
            Node currentNode,
            ArgsParseNode argsNode) {
        super(context, parent, environment, source, parserContext, currentNode);
        this.isBlock = isBlock;
        this.argsNode = argsNode;

        if (parserContext == ParserContext.EVAL || context.getCoverageManager().isEnabled()) {
            shouldLazyTranslate = false;
        } else if (context.getSourcePath(source).startsWith(context.getCoreLibrary().coreLoadPath)) {
            shouldLazyTranslate = language.options.LAZY_TRANSLATION_CORE;
        } else {
            shouldLazyTranslate = language.options.LAZY_TRANSLATION_USER;
        }
    }

    public BlockDefinitionNode compileBlockNode(SourceIndexLength sourceSection, ParseNode bodyNode, ProcType type,
            String[] variables) {
        declareArguments();
        final Arity arity = argsNode.getArity();
        final Arity arityForCheck;

        /* If you have a block with parameters |a,| Ruby checks the arity as if was minimum 1, maximum 1. That's
         * counter-intuitive - as you'd expect the anonymous rest argument to cause it to have no maximum. Indeed,
         * that's how JRuby reports it, and by the look of their failing spec they consider this to be correct. We'll
         * follow the specs for now until we see a reason to do something else. */

        if (argsNode.getRestArgNode() instanceof UnnamedRestArgParseNode &&
                !((UnnamedRestArgParseNode) argsNode.getRestArgNode()).isStar()) {
            arityForCheck = arity.withRest(false);
        } else {
            arityForCheck = arity;
        }

        final boolean isProc = type == ProcType.PROC;
        final RubyNode loadArguments = new LoadArgumentsTranslator(
                currentNode,
                argsNode,
                context,
                source,
                parserContext,
                isProc,
                false,
                this).translate();

        final RubyNode preludeProc;
        if (shouldConsiderDestructuringArrayArg(arity)) {
            final RubyNode readArrayNode = profileArgument(
                    language,
                    new ReadPreArgumentNode(0, MissingArgumentBehavior.RUNTIME_ERROR));
            final RubyNode castArrayNode = ArrayCastNodeGen.create(readArrayNode);

            final FrameSlot arraySlot = environment.declareVar(environment.allocateLocalTemp("destructure"));
            final RubyNode writeArrayNode = new WriteLocalVariableNode(arraySlot, castArrayNode);

            final LoadArgumentsTranslator destructureArgumentsTranslator = new LoadArgumentsTranslator(
                    currentNode,
                    argsNode,
                    context,
                    source,
                    parserContext,
                    isProc,
                    false,
                    this);
            destructureArgumentsTranslator.pushArraySlot(arraySlot);
            final RubyNode newDestructureArguments = destructureArgumentsTranslator.translate();

            final RubyNode arrayWasNotNil = sequence(
                    sourceSection,
                    Arrays.asList(
                            writeArrayNode,
                            new NotNode(
                                    new IsNilNode(
                                            new ReadLocalVariableNode(LocalVariableType.FRAME_LOCAL, arraySlot)))));

            final RubyNode shouldDestructureAndArrayWasNotNil = new AndNode(
                    new ShouldDestructureNode(),
                    arrayWasNotNil);

            preludeProc = new IfElseNode(
                    shouldDestructureAndArrayWasNotNil,
                    newDestructureArguments,
                    loadArguments);
        } else {
            preludeProc = loadArguments;
        }

        final RubyNode preludeLambda = createCheckArityNode(language, arityForCheck, NodeUtil.cloneNode(loadArguments));

        if (!translatingForStatement) {
            // Make sure to declare block-local variables
            for (String var : variables) {
                environment.declareVar(var);
            }
        }

        RubyNode body = translateNodeOrNil(sourceSection, bodyNode).simplifyAsTailExpression();

        body = new ExceptionTranslatingNode(body, UnsupportedOperationBehavior.TYPE_ERROR);

        // Procs
        final RubyNode bodyProc = new CatchForProcNode(
                composeBody(sourceSection, preludeProc, NodeUtil.cloneNode(body)));
        bodyProc.unsafeSetSourceSection(enclosing(sourceSection, body));

        final RubyRootNode newRootNodeForProcs = new RubyRootNode(
                context,
                translateSourceSection(source, sourceSection),
                environment.getFrameDescriptor(),
                environment.getSharedMethodInfo(),
                bodyProc,
                Split.HEURISTIC);

        // Lambdas
        RubyNode composed = composeBody(sourceSection, preludeLambda, body /* no copy, last usage */);

        composed = new CatchForLambdaNode(environment.getReturnID(), environment.getBreakID(), composed);

        final RubyRootNode newRootNodeForLambdas = new RubyRootNode(
                context,
                translateSourceSection(source, sourceSection),
                environment.getFrameDescriptor(),
                environment.getSharedMethodInfo(),
                composed,
                Split.HEURISTIC);

        // TODO CS 23-Nov-15 only the second one will get instrumented properly!
        final RootCallTarget callTargetAsLambda = Truffle.getRuntime().createCallTarget(newRootNodeForLambdas);
        final RootCallTarget callTargetAsProc = Truffle.getRuntime().createCallTarget(newRootNodeForProcs);


        Object frameOnStackMarkerSlot;

        if (frameOnStackMarkerSlotStack.isEmpty()) {
            frameOnStackMarkerSlot = null;
        } else {
            frameOnStackMarkerSlot = frameOnStackMarkerSlotStack.peek();

            if (frameOnStackMarkerSlot == BAD_FRAME_SLOT) {
                frameOnStackMarkerSlot = null;
            }
        }

        final BlockDefinitionNode ret = new BlockDefinitionNode(
                type,
                environment.getSharedMethodInfo(),
                callTargetAsProc,
                callTargetAsLambda,
                environment.getBreakID(),
                (FrameSlot) frameOnStackMarkerSlot);
        ret.unsafeSetSourceSection(sourceSection);
        return ret;
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
        } else if (arity.hasRest() && arity.getRequired() == 0) {
            return false;
        } else {
            return true;
        }
    }

    private RubyNode composeBody(SourceIndexLength preludeSourceSection, RubyNode prelude, RubyNode body) {
        final SourceIndexLength sourceSection = enclosing(preludeSourceSection, body);

        body = sequence(sourceSection, Arrays.asList(prelude, body));

        if (environment.getFlipFlopStates().size() > 0) {
            body = sequence(sourceSection, Arrays.asList(initFlipFlopStates(sourceSection), body));
        }

        return body;
    }

    public RubyNode compileMethodBody(SourceIndexLength sourceSection, ParseNode bodyNode) {
        declareArguments();
        final Arity arity = argsNode.getArity();

        final RubyNode loadArguments = new LoadArgumentsTranslator(
                currentNode,
                argsNode,
                context,
                source,
                parserContext,
                false,
                true,
                this).translate();

        RubyNode body = translateNodeOrNil(sourceSection, bodyNode).simplifyAsTailExpression();

        final SourceIndexLength bodySourceSection = body.getSourceIndexLength();

        body = createCheckArityNode(
                language,
                arity,
                sequence(bodySourceSection, Arrays.asList(loadArguments, body)));

        body.unsafeSetSourceSection(sourceSection);

        body = sequence(bodySourceSection, Arrays.asList(new MakeSpecialVariableStorageNode(), body));

        if (environment.getFlipFlopStates().size() > 0) {
            body = sequence(bodySourceSection, Arrays.asList(initFlipFlopStates(sourceSection), body));
        }

        body = new CatchForMethodNode(environment.getReturnID(), body);

        body = new ExceptionTranslatingNode(body, UnsupportedOperationBehavior.TYPE_ERROR);

        body.unsafeSetSourceSection(sourceSection);

        return body;
    }

    private RubyRootNode translateMethodNode(SourceIndexLength sourceSection, MethodDefParseNode defNode,
            ParseNode bodyNode) {
        final SourceIndexLength sourceIndexLength = defNode.getPosition();
        final SourceSection fullMethodSourceSection = sourceIndexLength.toSourceSection(source);
        return new RubyRootNode(
                context,
                fullMethodSourceSection,
                environment.getFrameDescriptor(),
                environment.getSharedMethodInfo(),
                compileMethodBody(sourceSection, bodyNode),
                Split.HEURISTIC);
    }

    public CachedSupplier<RootCallTarget> buildMethodNodeCompiler(SourceIndexLength sourceSection,
            MethodDefParseNode defNode, ParseNode bodyNode) {

        if (shouldLazyTranslate) {
            final TranslatorState state = getCurrentState();
            return new CachedSupplier<>(() -> {
                restoreState(state);
                return Truffle.getRuntime().createCallTarget(translateMethodNode(sourceSection, defNode, bodyNode));
            });
        } else {
            final RubyRootNode root = translateMethodNode(sourceSection, defNode, bodyNode);
            return new CachedSupplier<>(() -> Truffle.getRuntime().createCallTarget(root));
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
                environment.getNamedMethodName());

        final RubyNode arguments = new ReadSuperArgumentsNode(
                argumentsAndBlock.getArguments(),
                argumentsAndBlock.isSplatted());
        final RubyNode block = executeOrInheritBlock(argumentsAndBlock.getBlock(), node);

        RubyNode callNode = new SuperCallNode(arguments, block);
        callNode = wrapCallWithLiteralBlock(argumentsAndBlock, callNode);

        return withSourceSection(sourceSection, callNode);
    }

    @Override
    public RubyNode visitZSuperNode(ZSuperParseNode node) {
        final SourceIndexLength sourceSection = node.getPosition();

        currentCallMethodName = environment.getNamedMethodName();

        final ArgumentsAndBlockTranslation argumentsAndBlock = translateArgumentsAndBlock(
                sourceSection,
                node.getIterNode(),
                null,
                environment.getNamedMethodName());

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
                context,
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

        RubyNode callNode = new SuperCallNode(arguments, block);
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

    /* The following methods allow us to save and restore enough of the current state of the Translator to allow lazy
     * parsing. When the lazy parsing is actually performed, the state is restored to what it would have been if the
     * method had been parsed eagerly. */
    public TranslatorState getCurrentState() {
        return new TranslatorState(
                getEnvironment().unsafeGetLexicalScope(),
                getEnvironment().isDynamicConstantLookup());
    }

    public void restoreState(TranslatorState state) {
        getEnvironment().getParseEnvironment().setDynamicConstantLookup(state.dynamicConstantLookup);
        getEnvironment().getParseEnvironment().resetLexicalScope(state.scope);
    }

    public static class TranslatorState {
        private final LexicalScope scope;
        private final boolean dynamicConstantLookup;

        private TranslatorState(LexicalScope scope, boolean dynamicConstantLookup) {
            this.scope = scope;
            this.dynamicConstantLookup = dynamicConstantLookup;
        }
    }

}
