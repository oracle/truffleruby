/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * The contents of this file are subject to the Eclipse Public
 * License Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.eclipse.org/legal/epl-v20.html
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Copyright (C) 2002-2004 Anders Bengtsson <ndrsbngtssn@yahoo.se>
 * Copyright (C) 2002-2004 Jan Arne Petersen <jpetersen@uni-bonn.de>
 * Copyright (C) 2004 Thomas E Enebo <enebo@acm.org>
 * Copyright (C) 2004 Stefan Matthias Aust <sma@3plus4.de>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either of the GNU General Public License Version 2 or later (the "GPL"),
 * or the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the EPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the EPL, the GPL or the LGPL.
 */
package org.truffleruby.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.oracle.truffle.api.CompilerDirectives;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.aot.ParserCache;
import org.truffleruby.collections.Memo;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.kernel.AutoSplitNode;
import org.truffleruby.core.kernel.ChompLoopNode;
import org.truffleruby.core.kernel.KernelGetsNode;
import org.truffleruby.core.kernel.KernelPrintLastLineNode;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.DataNode;
import org.truffleruby.language.EmitWarningsNode;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyMethodRootNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.SetTopLevelBindingNode;
import org.truffleruby.language.SourceIndexLength;
import org.truffleruby.language.arguments.MissingArgumentBehavior;
import org.truffleruby.language.arguments.ReadPreArgumentNode;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.control.WhileNode;
import org.truffleruby.language.locals.WriteLocalVariableNode;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.methods.CatchNextNode;
import org.truffleruby.language.methods.CatchRetryAsErrorNode;
import org.truffleruby.language.methods.CatchReturnAsErrorNode;
import org.truffleruby.language.methods.ExceptionTranslatingNode;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.methods.Split;
import org.truffleruby.language.threadlocal.MakeSpecialVariableStorageNode;
import org.truffleruby.parser.ast.RootParseNode;
import org.truffleruby.parser.lexer.LexerSource;
import org.truffleruby.parser.lexer.SyntaxException;
import org.truffleruby.parser.parser.ParserConfiguration;
import org.truffleruby.parser.parser.RubyParser;
import org.truffleruby.parser.parser.RubyParserResult;
import org.truffleruby.parser.scope.StaticScope;
import org.truffleruby.shared.Metrics;

import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;

public class TranslatorDriver {

    /** May be null, see {@link ParserCache#parse} */
    private final RubyContext context;
    private final RubyLanguage language;
    private final ParseEnvironment parseEnvironment;

    public TranslatorDriver(RubyContext context, RubySource rubySource) {
        this.context = context;
        this.language = context.getLanguageSlow();
        this.parseEnvironment = new ParseEnvironment(language, rubySource);
    }

    public RubyRootNode parse(RubySource rubySource, ParserContext parserContext, String[] argumentNames,
            MaterializedFrame parentFrame, RubyModule wrap, boolean ownScopeForAssignments, Node currentNode) {
        if (rubySource.getSource() != parseEnvironment.source) {
            throw CompilerDirectives.shouldNotReachHere("TranslatorDriver used with a different Source");
        }

        if (parserContext.isTopLevel() != (parentFrame == null)) {
            throw CompilerDirectives.shouldNotReachHere(
                    "A frame should be given iff the context is not toplevel: " + parserContext + " " + parentFrame);
        }

        final Source source = rubySource.getSource();

        final StaticScope staticScope = new StaticScope(StaticScope.Type.LOCAL, null);

        /* Note that jruby-parser will be mistaken about how deep the existing variables are, but that doesn't matter as
         * we look them up ourselves after being told they're in some parent scope. */

        final TranslatorEnvironment parentEnvironment;

        if (parentFrame != null) {
            MaterializedFrame frame = parentFrame;

            while (frame != null) {
                for (FrameSlot slot : frame.getFrameDescriptor().getSlots()) {
                    if (slot.getIdentifier() instanceof String) {
                        final String name = (String) slot.getIdentifier();
                        staticScope.addVariableThisScope(name.intern()); // StaticScope expects interned var names
                    }
                }

                frame = RubyArguments.getDeclarationFrame(frame);
            }

            parentEnvironment = environmentForFrame(context, parentFrame);
        } else {
            parentEnvironment = null;
        }

        if (argumentNames != null) {
            for (String name : argumentNames) {
                staticScope.addVariableThisScope(name.intern()); // StaticScope expects interned var names
            }
        }

        boolean isInlineSource = rubySource.getSourcePath().equals("-e");
        boolean isEvalParse = parserContext == ParserContext.EVAL || parserContext == ParserContext.INLINE ||
                parserContext == ParserContext.MODULE;
        final ParserConfiguration parserConfiguration = new ParserConfiguration(
                context,
                isInlineSource,
                !isEvalParse,
                false);

        if (language.options.FROZEN_STRING_LITERALS) {
            parserConfiguration.setFrozenStringLiteral(true);
        }

        if (rubySource.getRope() != null) {
            parserConfiguration.setDefaultEncoding(rubySource.getRope().getEncoding());
        } else {
            parserConfiguration.setDefaultEncoding(UTF8Encoding.INSTANCE);
        }

        // Parse to the JRuby AST

        final RootParseNode node;
        final RubyDeferredWarnings rubyWarnings = new RubyDeferredWarnings();

        // Only use the cache while loading top-level core library files, as eval() later could use
        // the same Source name but should not use the cache. For instance,
        // TOPLEVEL_BINDING.eval("self") would use the cache which is wrong.
        if (ParserCache.INSTANCE != null && parserContext == ParserContext.TOP_LEVEL &&
                ParserCache.INSTANCE.containsKey(source.getName())) {
            node = ParserCache.INSTANCE.get(source.getName());
        } else {
            printParseTranslateExecuteMetric("before-parsing", context, source);
            node = context.getMetricsProfiler().callWithMetrics(
                    "parsing",
                    source.getName(),
                    () -> parseToJRubyAST(context, rubySource, staticScope, parserConfiguration, rubyWarnings));
            printParseTranslateExecuteMetric("after-parsing", context, source);
        }

        final SourceSection sourceSection = source.createSection(0, source.getCharacters().length());
        final SourceIndexLength sourceIndexLength = new SourceIndexLength(sourceSection);

        final InternalMethod parentMethod = parentFrame == null ? null : RubyArguments.getMethod(parentFrame);
        LexicalScope lexicalScope;
        if (parentMethod != null && parentMethod.getSharedMethodInfo().getLexicalScope() != null) {
            lexicalScope = parentMethod.getSharedMethodInfo().getLexicalScope();
        } else {
            if (wrap == null) {
                lexicalScope = context.getRootLexicalScope();
            } else {
                lexicalScope = new LexicalScope(context.getRootLexicalScope(), wrap);
            }
        }
        if (parserContext == ParserContext.MODULE) {
            Object module = RubyArguments.getSelf(context.getCallStack().getCurrentFrame(FrameAccess.READ_ONLY));
            lexicalScope = new LexicalScope(lexicalScope, (RubyModule) module);
        }
        parseEnvironment.resetLexicalScope(lexicalScope);
        parseEnvironment.allowTruffleRubyPrimitives = parserConfiguration.allowTruffleRubyPrimitives;

        final String modulePath = lexicalScope == context.getRootLexicalScope()
                ? null
                : lexicalScope.getLiveModule().getName();
        final String methodName = getMethodName(parserContext, parentFrame);
        final SharedMethodInfo sharedMethodInfo = new SharedMethodInfo(
                sourceSection,
                lexicalScope,
                Arity.NO_ARGUMENTS,
                methodName,
                0,
                methodName,
                null,
                null);

        final boolean topLevel = parserContext.isTopLevel();
        final boolean isModuleBody = topLevel;
        final TranslatorEnvironment environment = new TranslatorEnvironment(
                parentEnvironment,
                parseEnvironment,
                parseEnvironment.allocateReturnID(),
                ownScopeForAssignments,
                false,
                isModuleBody,
                sharedMethodInfo,
                sharedMethodInfo.getMethodNameForNotBlock(),
                0,
                null,
                TranslatorEnvironment.newFrameDescriptor(),
                modulePath);

        // Declare arguments as local variables in the top-level environment - we'll put the values there in a prelude

        if (argumentNames != null) {
            for (String name : argumentNames) {
                environment.declareVar(name);
            }
        }

        // Translate to Ruby Truffle nodes

        // Like MRI, do not track coverage of eval's. This also avoids having to merge multiple Sources with the same RubyContext.getPath().
        if (!rubySource.isEval()) {
            context.getCoverageManager().loadingSource(source);
        }

        final BodyTranslator translator = new BodyTranslator(
                language,
                null,
                environment,
                source,
                parserContext,
                currentNode,
                rubyWarnings);

        final Memo<RubyNode> beginNodeMemo = new Memo<>(null);
        RubyNode truffleNode;
        printParseTranslateExecuteMetric("before-translate", context, source);
        try {
            truffleNode = context.getMetricsProfiler().callWithMetrics(
                    "translating",
                    source.getName(),
                    () -> {
                        if (node.getBeginNode() != null) {
                            beginNodeMemo.set(translator.translateNodeOrNil(sourceIndexLength, node.getBeginNode()));
                        }
                        return translator.translateNodeOrNil(sourceIndexLength, node.getBodyNode());
                    });
        } finally {
            printParseTranslateExecuteMetric("after-translate", context, source);
        }
        RubyNode beginNode = beginNodeMemo.get();

        // Load arguments
        if (argumentNames != null && argumentNames.length > 0) {
            final List<RubyNode> sequence = new ArrayList<>();

            for (int n = 0; n < argumentNames.length; n++) {
                final String name = argumentNames[n];
                final RubyNode readNode = Translator
                        .profileArgument(
                                language,
                                new ReadPreArgumentNode(n, MissingArgumentBehavior.NIL));
                final FrameSlot slot = environment.getFrameDescriptor().findFrameSlot(name);
                sequence.add(new WriteLocalVariableNode(slot, readNode));
            }

            sequence.add(truffleNode);
            truffleNode = Translator.sequence(sourceIndexLength, sequence);
        }

        // Load flip-flop states

        if (environment.getFlipFlopStates().size() > 0) {
            truffleNode = Translator.sequence(
                    sourceIndexLength,
                    Arrays.asList(BodyTranslator.initFlipFlopStates(environment, sourceIndexLength), truffleNode));
        }

        if (parserContext == ParserContext.TOP_LEVEL_FIRST && context.getOptions().GETS_LOOP) {
            if (context.getOptions().PRINT_LOOP) {
                truffleNode = Translator.sequence(
                        sourceIndexLength,
                        Arrays.asList(truffleNode, new KernelPrintLastLineNode()));
            }
            if (context.getOptions().SPLIT_LOOP) {
                truffleNode = Translator.sequence(
                        sourceIndexLength,
                        Arrays.asList(new AutoSplitNode(), truffleNode));
            }

            if (context.getOptions().CHOMP_LOOP) {
                truffleNode = Translator.sequence(
                        sourceIndexLength,
                        Arrays.asList(new ChompLoopNode(), truffleNode));
            }
            truffleNode = new WhileNode(new WhileNode.WhileRepeatingNode(new KernelGetsNode(), truffleNode));
        }

        if (beginNode != null) {
            truffleNode = Translator.sequence(
                    sourceIndexLength,
                    Arrays.asList(beginNode, truffleNode));
        }


        final RubyNode writeSelfNode = Translator.loadSelf(language, environment);
        truffleNode = Translator.sequence(sourceIndexLength, Arrays.asList(writeSelfNode, truffleNode));

        if (!rubyWarnings.warnings.isEmpty()) {
            truffleNode = Translator.sequence(
                    sourceIndexLength,
                    Arrays.asList(new EmitWarningsNode(rubyWarnings), truffleNode));
        }

        // Catch next

        truffleNode = new CatchNextNode(truffleNode);

        // Catch return

        if (!parserContext.isTopLevel() && parserContext != ParserContext.INLINE) {
            truffleNode = new CatchReturnAsErrorNode(truffleNode);
        }

        // Catch retry

        if (!parserContext.isTopLevel()) { // Already done by RubyMethodRootNode
            truffleNode = new CatchRetryAsErrorNode(truffleNode);
        }

        // Top-level exception handling

        if (parserContext == ParserContext.TOP_LEVEL_FIRST) {
            truffleNode = Translator
                    .sequence(sourceIndexLength, Arrays.asList(
                            new SetTopLevelBindingNode(),
                            truffleNode));

            if (node.hasEndPosition()) {
                truffleNode = Translator.sequence(sourceIndexLength, Arrays.asList(
                        new DataNode(node.getEndPosition()),
                        truffleNode));
            }
        }

        if (!parserContext.isTopLevel()) { // Already done by RubyMethodRootNode
            truffleNode = new ExceptionTranslatingNode(truffleNode);
        }

        if (parserContext.isTopLevel()) {
            truffleNode = Translator
                    .sequence(sourceIndexLength, Arrays.asList(new MakeSpecialVariableStorageNode(), truffleNode));
        }

        if (parserContext.isTopLevel()) {
            return new RubyMethodRootNode(
                    language,
                    sourceIndexLength.toSourceSection(source),
                    environment.getFrameDescriptor(),
                    sharedMethodInfo,
                    truffleNode,
                    Split.HEURISTIC,
                    environment.getReturnID(),
                    Arity.ANY_ARGUMENTS);
        } else {
            return new RubyRootNode(
                    language,
                    sourceIndexLength.toSourceSection(source),
                    environment.getFrameDescriptor(),
                    sharedMethodInfo,
                    truffleNode,
                    Split.HEURISTIC,
                    environment.getReturnID());
        }
    }

    private String getMethodName(ParserContext parserContext, MaterializedFrame parentFrame) {
        switch (parserContext) {
            case TOP_LEVEL_FIRST:
                return "<main>";
            case TOP_LEVEL:
                return "<top (required)>";
            default:
                if (parentFrame != null) {
                    return RubyArguments.getMethod(parentFrame).getName();
                } else {
                    throw new UnsupportedOperationException(
                            "Could not determine the method name for parser context " + parserContext);
                }
        }
    }

    public static RootParseNode parseToJRubyAST(RubyContext context, RubySource rubySource, StaticScope blockScope,
            ParserConfiguration configuration, RubyDeferredWarnings rubyWarnings) {
        LexerSource lexerSource = new LexerSource(rubySource, configuration.getDefaultEncoding());
        // We only need to pass in current scope if we are evaluating as a block (which
        // is only done for evals).  We need to pass this in so that we can appropriately scope
        // down to captured scopes when we are parsing.
        if (blockScope != null) {
            configuration.parseAsBlock(blockScope);
        }

        RubyParser parser = new RubyParser(lexerSource, rubyWarnings);
        RubyParserResult result;
        try {
            result = parser.parse(configuration);
        } catch (SyntaxException e) {
            if (!rubyWarnings.warnings.isEmpty()) {
                EmitWarningsNode.printWarnings(context, rubyWarnings);
            }
            switch (e.getPid()) {
                case UNKNOWN_ENCODING:
                case NOT_ASCII_COMPATIBLE:
                    if (context != null) {
                        throw new RaiseException(
                                context,
                                context.getCoreExceptions().argumentError(e.getMessage(), null));
                    } else {
                        throw e;
                    }
                default:
                    StringBuilder buffer = new StringBuilder(100);
                    buffer.append(e.getFile()).append(':');
                    buffer.append(e.getLine() + rubySource.getLineOffset()).append(": ");
                    buffer.append(e.getMessage());

                    if (context != null) {
                        throw new RaiseException(
                                context,
                                context.getCoreExceptions().syntaxError(
                                        buffer.toString(),
                                        null,
                                        rubySource.getSource().createSection(e.getLine())));
                    } else {
                        throw new UnsupportedOperationException(buffer.toString(), e);
                    }
            }
        }

        return (RootParseNode) result.getAST();
    }

    private TranslatorEnvironment environmentForFrame(RubyContext context, MaterializedFrame frame) {
        if (frame == null) {
            return null;
        } else {
            final SharedMethodInfo sharedMethodInfo = new SharedMethodInfo(
                    CoreLibrary.SOURCE_SECTION,
                    context.getRootLexicalScope(),
                    Arity.NO_ARGUMENTS,
                    "<unused>",
                    0,
                    "<unused>",
                    "external",
                    null);
            final MaterializedFrame parent = RubyArguments.getDeclarationFrame(frame);
            // TODO(CS): how do we know if the frame is a block or not?
            return new TranslatorEnvironment(
                    environmentForFrame(context, parent),
                    parseEnvironment,
                    parseEnvironment.allocateReturnID(),
                    true,
                    false,
                    false,
                    sharedMethodInfo,
                    sharedMethodInfo.getMethodName(),
                    0,
                    null,
                    frame.getFrameDescriptor(),
                    "<unused>");
        }
    }

    public static void printParseTranslateExecuteMetric(String id, RubyContext context, Source source) {
        if (Metrics.getMetricsTime()) {
            if (context.getOptions().METRICS_TIME_PARSING_FILE) {
                String name = context.getLanguageSlow().getSourcePath(source);
                int lastSlash = name.lastIndexOf('/');
                int lastDot = name.lastIndexOf('.');
                if (lastSlash >= 0 && lastDot >= 0 && lastSlash + 1 < lastDot) {
                    name = name.substring(lastSlash + 1, lastDot);
                }
                Metrics.printTime(id + "-" + name);
            } else if (context.getCoreLibrary().isLoadingRubyCore()) {
                // Only show times for core (the biggest contributor) to avoid multiple metrics with
                // the same name, which is not supported in mx_truffleruby_benchmark.py.
                Metrics.printTime(id + "-core");
            }
        }
    }

}
