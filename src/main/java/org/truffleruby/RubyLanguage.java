/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.SourceSection;
import org.graalvm.options.OptionDescriptor;
import org.graalvm.options.OptionDescriptors;
import org.truffleruby.core.kernel.TraceManager;
import org.truffleruby.core.module.ModuleFields;
import org.truffleruby.interop.MetaObject;
import org.truffleruby.language.LazyRubyRootNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.launcher.Launcher;
import org.truffleruby.launcher.options.OptionDescription;
import org.truffleruby.launcher.options.OptionsCatalog;
import org.truffleruby.platform.Platform;
import org.truffleruby.stdlib.CoverageManager;

import java.util.ArrayList;
import java.util.List;

@TruffleLanguage.Registration(
        name = "Ruby",
        id = Launcher.LANGUAGE_ID,
        version = Launcher.LANGUAGE_VERSION,
        mimeType = RubyLanguage.MIME_TYPE,
        dependentLanguages = "llvm")
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

    public static final String MIME_TYPE = "application/x-ruby";
    public static final String EXTENSION = ".rb";

    public static final String SULONG_BITCODE_BASE64_MIME_TYPE = "application/x-llvm-ir-bitcode-base64";

    public static final String CEXT_MIME_TYPE = "application/x-ruby-cext-library";
    public static final String CEXT_EXTENSION = ".su";

    @Override
    public RubyContext createContext(Env env) {
        Log.LOGGER.fine("createContext()");
        Launcher.printTruffleTimeMetric("before-create-context");
        // TODO CS 3-Dec-16 need to parse RUBYOPT here if it hasn't been already?
        final RubyContext context = new RubyContext(this, env);
        Launcher.printTruffleTimeMetric("after-create-context");
        return context;
    }

    @Override
    protected void initializeContext(RubyContext context) throws Exception {
        Log.LOGGER.fine("initializeContext()");
        Launcher.printTruffleTimeMetric("before-initialize-context");
        context.initialize();
        Launcher.printTruffleTimeMetric("after-initialize-context");
    }

    @Override
    protected boolean patchContext(RubyContext context, Env newEnv) {
        Log.LOGGER.fine("patchContext()");
        Launcher.printTruffleTimeMetric("before-patch-context");
        boolean patched = context.patchContext(newEnv);
        Launcher.printTruffleTimeMetric("after-patch-context");
        return patched;
    }

    @Override
    protected void finalizeContext(RubyContext context) {
        Log.LOGGER.fine("finalizeContext()");
        context.finalizeContext();
    }

    @Override
    protected void disposeContext(RubyContext context) {
        Log.LOGGER.fine("disposeContext()");
        context.disposeContext();
    }

    public static RubyContext getCurrentContext() {
        return getCurrentContext(RubyLanguage.class);
    }

    @Override
    protected CallTarget parse(ParsingRequest request) throws Exception {
        return Truffle.getRuntime().createCallTarget(new LazyRubyRootNode(this, null, null, request.getSource(), request.getArgumentNames()));
    }

    @Override
    protected Object findExportedSymbol(RubyContext context, String symbolName, boolean onlyExplicit) {
        final Object explicit = context.getInteropManager().findExportedObject(symbolName);

        if (explicit != null) {
            return explicit;
        }

        if (onlyExplicit) {
            return null;
        }

        return context.send(context.getCoreLibrary().getTruffleInteropModule(), "lookup_symbol", context.getSymbolTable().getSymbol(symbolName));
    }

    @Override
    protected Object getLanguageGlobal(RubyContext context) {
        return context.getCoreLibrary().getObjectClass();
    }

    @Override
    protected boolean isObjectOfLanguage(Object object) {
        return RubyGuards.isRubyBasicObject(object);
    }

    @Override
    protected String toString(RubyContext context, Object value) {
        if (value == null) {
            return "<null>";
        } else if (RubyGuards.isBoxedPrimitive(value) ||  RubyGuards.isRubyBasicObject(value)) {
            return context.send(value, "inspect").toString();
        } else if (value instanceof String) {
            return (String) value;
        } else {
            return "<foreign>";
        }
    }

    @Override
    public Object findMetaObject(RubyContext context, Object value) {
        final DynamicObject rubyClass = context.getCoreLibrary().getLogicalClass(value);
        final ModuleFields rubyClassFields = Layouts.CLASS.getFields(rubyClass);
        return new MetaObject(rubyClassFields.getName(), rubyClassFields.getName(), toString(context, value));
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
        final OptionDescription<?>[] allDescriptions = OptionsCatalog.allDescriptions();
        final List<OptionDescriptor> options = new ArrayList<>(allDescriptions.length);

        for (OptionDescription<?> option : allDescriptions) {
            options.add(option.toDescriptor());
        }

        return OptionDescriptors.create(options);
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
        context.getThreadManager().start(foreignThread, thread);
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

}
