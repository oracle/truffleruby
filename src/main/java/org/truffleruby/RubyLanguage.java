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
import java.util.Collections;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.utilities.CyclicAssumption;
import org.graalvm.options.OptionDescriptors;
import org.truffleruby.cext.ValueWrapper;
import org.truffleruby.core.kernel.TraceManager;
import org.truffleruby.debug.GlobalScope;
import org.truffleruby.debug.LexicalScope;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyInlineParsingRequestNode;
import org.truffleruby.language.RubyParsingRequestNode;
import org.truffleruby.platform.Platform;
import org.truffleruby.shared.BuildInformationImpl;
import org.truffleruby.shared.Metrics;
import org.truffleruby.shared.TruffleRuby;
import org.truffleruby.shared.options.OptionsCatalog;
import org.truffleruby.stdlib.CoverageManager;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Scope;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.TruffleLogger;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.nodes.ExecutableNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;

@TruffleLanguage.Registration(
        name = "Ruby",
        id = TruffleRuby.LANGUAGE_ID,
        implementationName = TruffleRuby.FORMAL_NAME,
        version = BuildInformationImpl.RUBY_VERSION,
        characterMimeTypes = TruffleRuby.MIME_TYPE,
        defaultMimeType = TruffleRuby.MIME_TYPE,
        dependentLanguages = { "nfi", "llvm" },
        fileTypeDetectors = RubyFileTypeDetector.class)
@ProvidedTags({
        CoverageManager.LineTag.class,
        TraceManager.CallTag.class,
        TraceManager.ClassTag.class,
        TraceManager.LineTag.class,
        StandardTags.RootTag.class,
        StandardTags.StatementTag.class,
        StandardTags.CallTag.class
})
public class RubyLanguage extends TruffleLanguage<RubyContext> {

    public static final String PLATFORM = String.format("%s-%s", Platform.getArchitecture(), Platform.getOSName());

    public static final String LLVM_BITCODE_MIME_TYPE = "application/x-llvm-ir-bitcode";

    public static final String CEXT_MIME_TYPE = "application/x-ruby-cext-library";
    public static final String CEXT_EXTENSION = "." + Platform.LIBSUFFIX;

    public static final String RESOURCE_SCHEME = "resource:";
    public static final String RUBY_HOME_SCHEME = "rubyHome:";

    public static final TruffleLogger LOGGER = TruffleLogger.getLogger(TruffleRuby.LANGUAGE_ID);

    private final CyclicAssumption tracingCyclicAssumption = new CyclicAssumption("object-space-tracing");
    @CompilationFinal private volatile Assumption tracingAssumption = tracingCyclicAssumption.getAssumption();

    public Assumption getTracingAssumption() {
        return tracingAssumption;
    }

    public void invalidateTracingAssumption() {
        tracingCyclicAssumption.invalidate();
        tracingAssumption = tracingCyclicAssumption.getAssumption();
    }

    @Override
    public RubyContext createContext(Env env) {
        // We need to initialize the Metrics class of the language classloader
        Metrics.initializeOption();

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

    public static ContextReference<RubyContext> getCurrentContextReference() {
        return getCurrentLanguage(RubyLanguage.class).getContextReference();
    }

    @Override
    protected RootCallTarget parse(ParsingRequest request) {
        return Truffle.getRuntime().createCallTarget(
                new RubyParsingRequestNode(
                        this,
                        request.getSource(),
                        request.getArgumentNames().toArray(new String[]{})));
    }

    @Override
    protected ExecutableNode parse(InlineParsingRequest request) {
        return new RubyInlineParsingRequestNode(this, request.getSource(), request.getFrame());
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
                context.getCoreLibrary().getTruffleInteropModule(),
                "lookup_symbol",
                context.getSymbolTable().getSymbol(symbolName));
        if (implicit == NotProvided.INSTANCE) {
            return null;
        } else {
            return implicit;
        }
    }

    @Override
    protected boolean isObjectOfLanguage(Object object) {
        return object instanceof ValueWrapper || object instanceof NotProvided || RubyGuards.isRubyBasicObject(object);
    }

    @Override
    protected String toString(RubyContext context, Object value) {
        if (value == null) {
            return "<null>";
        } else if (RubyGuards.isBoxedPrimitive(value) || RubyGuards.isRubyBasicObject(value)) {
            return context.send(value, "inspect").toString();
        } else if (value instanceof NotProvided) {
            return "<undefined>";
        } else if (value instanceof ValueWrapper) {
            return "VALUE: " + toString(context, ((ValueWrapper) value).getObject());
        } else if (value instanceof String) {
            return (String) value;
        } else {
            return "<foreign>";
        }
    }

    @Override
    public Object findMetaObject(RubyContext context, Object value) {
        return context.getCoreLibrary().getLogicalClass(value);
    }

    @Override
    protected SourceSection findSourceLocation(RubyContext context, Object value) {
        if (RubyGuards.isRubyModule(value)) {
            return Layouts.CLASS.getFields((DynamicObject) value).getSourceSection();
        } else if (RubyGuards.isRubyMethod(value)) {
            return Layouts.METHOD.getMethod((DynamicObject) value).getSharedMethodInfo().getSourceSection();
        } else if (RubyGuards.isRubyUnboundMethod(value)) {
            return Layouts.UNBOUND_METHOD.getMethod((DynamicObject) value).getSharedMethodInfo().getSourceSection();
        } else if (RubyGuards.isRubyProc(value)) {
            return Layouts.PROC.getMethod((DynamicObject) value).getSharedMethodInfo().getSourceSection();
        } else {
            return null;
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

        final DynamicObject foreignThread = context.getThreadManager().createForeignThread();
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

        final DynamicObject rubyThread = context.getThreadManager().getForeignRubyThread(thread);
        context.getThreadManager().cleanup(rubyThread, thread);
    }

    @Override
    protected Iterable<Scope> findLocalScopes(RubyContext context, Node node, Frame frame) {
        return LexicalScope.getLexicalScopeFor(context, node, frame);
    }

    @Override
    protected Iterable<Scope> findTopScopes(RubyContext context) {
        return Collections.singletonList(GlobalScope.getGlobalScope(context.getCoreLibrary().getGlobalVariables()));
    }

    public String getTruffleLanguageHome() {
        return getLanguageHome();
    }

}
