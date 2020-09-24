/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.instrumentation.AllocationReporter;
import org.graalvm.options.OptionDescriptors;
import org.truffleruby.core.kernel.TraceManager;
import org.truffleruby.core.objectspace.ObjectSpaceManager;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeCache;
import org.truffleruby.core.string.CoreStrings;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.symbol.CoreSymbols;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.symbol.SymbolTable;
import org.truffleruby.core.thread.RubyThread;
import org.truffleruby.debug.GlobalScope;
import org.truffleruby.debug.LexicalScope;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyEvalInteractiveRootNode;
import org.truffleruby.language.RubyInlineParsingRequestNode;
import org.truffleruby.language.RubyParsingRequestNode;
import org.truffleruby.platform.Platform;
import org.truffleruby.shared.Metrics;
import org.truffleruby.shared.TruffleRuby;
import org.truffleruby.shared.options.OptionsCatalog;
import org.truffleruby.stdlib.CoverageManager;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Scope;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLanguage.ContextPolicy;
import com.oracle.truffle.api.TruffleLogger;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.nodes.ExecutableNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.utilities.CyclicAssumption;

@TruffleLanguage.Registration(
        name = "Ruby",
        contextPolicy = ContextPolicy.EXCLUSIVE,
        id = TruffleRuby.LANGUAGE_ID,
        implementationName = TruffleRuby.FORMAL_NAME,
        version = TruffleRuby.LANGUAGE_VERSION,
        characterMimeTypes = TruffleRuby.MIME_TYPE,
        defaultMimeType = TruffleRuby.MIME_TYPE,
        dependentLanguages = { "nfi", "llvm" },
        fileTypeDetectors = RubyFileTypeDetector.class)
@ProvidedTags({
        CoverageManager.LineTag.class,
        TraceManager.CallTag.class,
        TraceManager.ClassTag.class,
        TraceManager.LineTag.class,
        TraceManager.NeverTag.class,
        StandardTags.RootTag.class,
        StandardTags.StatementTag.class,
        StandardTags.ReadVariableTag.class,
        StandardTags.WriteVariableTag.class,
})
public class RubyLanguage extends TruffleLanguage<RubyContext> {

    public static final String PLATFORM = String.format(
            "%s-%s%s",
            Platform.getArchName(),
            Platform.getOSName(),
            Platform.getKernelMajorVersion());

    public static final String LLVM_BITCODE_MIME_TYPE = "application/x-llvm-ir-bitcode";

    public static final String CEXT_EXTENSION = Platform.CEXT_SUFFIX;

    public static final String RESOURCE_SCHEME = "resource:";

    public static final TruffleLogger LOGGER = TruffleLogger.getLogger(TruffleRuby.LANGUAGE_ID);

    private final CyclicAssumption tracingCyclicAssumption = new CyclicAssumption("object-space-tracing");
    @CompilationFinal private volatile Assumption tracingAssumption = tracingCyclicAssumption.getAssumption();

    public final CoreStrings coreStrings;
    public final CoreSymbols coreSymbols;
    public final RopeCache ropeCache;
    public final SymbolTable symbolTable;

    @CompilationFinal private AllocationReporter allocationReporter;

    private final AtomicLong nextObjectID = new AtomicLong(ObjectSpaceManager.INITIAL_LANGUAGE_OBJECT_ID);

    public RubyLanguage() {
        coreStrings = new CoreStrings(this);
        coreSymbols = new CoreSymbols();
        ropeCache = new RopeCache(coreSymbols);
        symbolTable = new SymbolTable(ropeCache, coreSymbols);
    }

    @TruffleBoundary
    public RubySymbol getSymbol(String string) {
        return symbolTable.getSymbol(string);
    }


    @TruffleBoundary
    public RubySymbol getSymbol(Rope rope) {
        return symbolTable.getSymbol(rope);
    }

    public Assumption getTracingAssumption() {
        return tracingAssumption;
    }

    public void invalidateTracingAssumption() {
        tracingCyclicAssumption.invalidate();
        tracingAssumption = tracingCyclicAssumption.getAssumption();
    }

    @Override
    protected void initializeMultipleContexts() {
        // TODO Make Symbol.all_symbols per context, by having a SymbolTable per context and creating new symbols with
        //  the per-language SymbolTable.
    }

    @Override
    public RubyContext createContext(Env env) {
        // We need to initialize the Metrics class of the language classloader
        Metrics.initializeOption();

        if (allocationReporter == null) {
            allocationReporter = env.lookup(AllocationReporter.class);
        }

        LOGGER.fine("createContext()");
        Metrics.printTime("before-create-context");
        // TODO CS 3-Dec-16 need to parse RUBYOPT here if it hasn't been already?
        final RubyContext context = new RubyContext(this, env);
        Metrics.printTime("after-create-context");
        return context;
    }

    @Override
    protected void initializeContext(RubyContext context) throws Exception {
        LOGGER.fine("initializeContext()");

        try {
            Metrics.printTime("before-initialize-context");
            context.initialize();
            Metrics.printTime("after-initialize-context");
        } catch (Throwable e) {
            if (context.getOptions().EXCEPTIONS_PRINT_JAVA || context.getOptions().EXCEPTIONS_PRINT_UNCAUGHT_JAVA) {
                e.printStackTrace();
            }
            throw e;
        }
    }

    @Override
    protected boolean patchContext(RubyContext context, Env newEnv) {
        // We need to initialize the Metrics class of the language classloader
        Metrics.initializeOption();

        LOGGER.fine("patchContext()");
        Metrics.printTime("before-patch-context");
        boolean patched = context.patchContext(newEnv);
        Metrics.printTime("after-patch-context");
        return patched;
    }

    @Override
    protected void finalizeContext(RubyContext context) {
        LOGGER.fine("finalizeContext()");
        context.finalizeContext();
    }

    @Override
    protected void disposeContext(RubyContext context) {
        LOGGER.fine("disposeContext()");
        context.disposeContext();
    }

    public static RubyContext getCurrentContext() {
        return getCurrentContext(RubyLanguage.class);
    }

    @Override
    protected RootCallTarget parse(ParsingRequest request) {
        if (request.getSource().isInteractive()) {
            return Truffle.getRuntime().createCallTarget(new RubyEvalInteractiveRootNode(this, request.getSource()));
        } else {
            final RubyContext context = Objects.requireNonNull(getCurrentContext());
            return Truffle.getRuntime().createCallTarget(
                    new RubyParsingRequestNode(
                            this,
                            context,
                            request.getSource(),
                            request.getArgumentNames().toArray(StringUtils.EMPTY_STRING_ARRAY)));
        }
    }

    @Override
    protected ExecutableNode parse(InlineParsingRequest request) {
        final RubyContext context = Objects.requireNonNull(getCurrentContext());
        return new RubyInlineParsingRequestNode(this, context, request.getSource(), request.getFrame());
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Object findExportedSymbol(RubyContext context, String symbolName, boolean onlyExplicit) {
        final Object explicit = context.getInteropManager().findExportedObject(symbolName);

        if (explicit != null) {
            return explicit;
        }

        if (onlyExplicit) {
            return null;
        }

        Object implicit = context.send(
                context.getCoreLibrary().truffleInteropModule,
                "lookup_symbol",
                symbolTable.getSymbol(symbolName));
        if (implicit == NotProvided.INSTANCE) {
            return null;
        } else {
            return implicit;
        }
    }

    @Override
    protected OptionDescriptors getOptionDescriptors() {
        return OptionDescriptors.create(Arrays.asList(OptionsCatalog.allDescriptors()));
    }

    @Override
    protected boolean isThreadAccessAllowed(Thread thread, boolean singleThreaded) {
        return true;
    }

    @Override
    protected void initializeThread(RubyContext context, Thread thread) {
        if (thread == context.getThreadManager().getOrInitializeRootJavaThread()) {
            // Already initialized when creating the context
            return;
        }

        if (context.getThreadManager().isRubyManagedThread(thread)) {
            // Already initialized by the Ruby-provided Runnable
            return;
        }

        final RubyThread foreignThread = context.getThreadManager().createForeignThread();
        context.getThreadManager().startForeignThread(foreignThread, thread);
    }

    @Override
    protected void disposeThread(RubyContext context, Thread thread) {
        if (thread == context.getThreadManager().getRootJavaThread()) {
            // Let the context shutdown cleanup the main thread
            return;
        }

        if (context.getThreadManager().isRubyManagedThread(thread)) {
            // Already disposed by the Ruby-provided Runnable
            return;
        }

        final RubyThread rubyThread = context.getThreadManager().getForeignRubyThread(thread);
        context.getThreadManager().cleanup(rubyThread, thread);
    }

    @Override
    protected Iterable<Scope> findLocalScopes(RubyContext context, Node node, Frame frame) {
        return LexicalScope.getLexicalScopeFor(context, node, frame == null ? null : frame.materialize());
    }

    @Override
    protected Iterable<Scope> findTopScopes(RubyContext context) {
        return Arrays.asList(
                GlobalScope.getGlobalScope(context.getCoreLibrary().globalVariables),
                Scope.newBuilder("main", context.getCoreLibrary().mainObject).build());
    }

    public String getTruffleLanguageHome() {
        return getLanguageHome();
    }

    public AllocationReporter getAllocationReporter() {
        return allocationReporter;
    }

    public long getNextObjectID() {
        final long id = nextObjectID.getAndAdd(ObjectSpaceManager.OBJECT_ID_INCREMENT_BY);

        if (id == ObjectSpaceManager.INITIAL_LANGUAGE_OBJECT_ID - ObjectSpaceManager.OBJECT_ID_INCREMENT_BY) {
            throw CompilerDirectives.shouldNotReachHere("Language Object IDs exhausted");
        }

        return id;
    }

}
