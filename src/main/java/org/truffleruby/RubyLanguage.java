/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import org.graalvm.options.OptionDescriptor;
import org.graalvm.options.OptionDescriptors;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.core.kernel.TraceManager;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.LazyRubyNode;
import org.truffleruby.language.LazyRubyRootNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.options.OptionDescription;
import org.truffleruby.options.OptionsCatalog;
import org.truffleruby.platform.Platform;
import org.truffleruby.stdlib.CoverageManager;

import java.util.ArrayList;
import java.util.List;

@TruffleLanguage.Registration(
        name = RubyLanguage.NAME,
        id = RubyLanguage.ID,
        version = RubyLanguage.RUBY_VERSION,
        mimeType = RubyLanguage.MIME_TYPE)
@ProvidedTags({
        CoverageManager.LineTag.class,
        TraceManager.CallTag.class,
        TraceManager.ClassTag.class,
        TraceManager.LineTag.class,
        LazyRubyNode.LazyTag.class,
        StandardTags.RootTag.class,
        StandardTags.StatementTag.class,
        StandardTags.CallTag.class
})
public class RubyLanguage extends TruffleLanguage<RubyContext> {

    public static final String NAME = "Ruby";
    public static final String ID = Main.LANGUAGE_ID;

    public static final String PLATFORM = String.format("%s-%s", Platform.getArchitecture(), Platform.getOSName());
    public static final String RUBY_VERSION = Main.LANGUAGE_VERSION;
    public static final int    RUBY_REVISION = 0;
    public static final String COMPILE_DATE = "2017";
    public static final String ENGINE = "truffleruby";
    public static final String ENGINE_VERSION = System.getProperty("graal.version", "0.0");

    public static final String MIME_TYPE = "application/x-ruby";
    public static final String EXTENSION = ".rb";

    public static final String SULONG_BITCODE_BASE64_MIME_TYPE = "application/x-llvm-ir-bitcode-base64";

    public static final String CEXT_MIME_TYPE = "application/x-ruby-cext-library";
    public static final String CEXT_EXTENSION = ".su";

    public RubyLanguage() {
    }

    @TruffleBoundary
    public static String fileLine(FrameInstance frameInstance) {
        if (frameInstance == null) {
            return "no frame";
        } else if (frameInstance.getCallNode() == null) {
            return "no call node";
        } else {
            final SourceSection sourceSection = frameInstance.getCallNode().getEncapsulatingSourceSection();

            if (sourceSection == null) {
                return "no source section (" + frameInstance.getCallNode().getRootNode().getClass() + ")";
            } else {
                return fileLine(sourceSection);
            }
        }
    }

    @TruffleBoundary
    public static String fileLine(SourceSection section) {
        if (section == null) {
            return "no source section";
        } else {
            final Source source = section.getSource();

            if (section.isAvailable()) {
                return source.getName() + ":" + section.getStartLine();
            } else {
                return source.getName();
            }
        }
    }

    @Override
    public RubyContext createContext(Env env) {
        // TODO CS 3-Dec-16 need to parse RUBY_OPT here if it hasn't been already?
        return new RubyContext(this, env);
    }

    @Override
    protected void initializeContext(RubyContext context) throws Exception {
        context.initialize();
    }

    @Override
    protected void disposeContext(RubyContext context) {
        context.shutdown();
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

        return lookupSymbol(context, symbolName);
    }

    @Override
    protected Object lookupSymbol(RubyContext context, String symbolName) {
        return context.send(context.getCoreLibrary().getMainObject(), "method", null,  StringOperations.createString(context, StringOperations.encodeRope(symbolName, UTF8Encoding.INSTANCE)));
    }

    @Override
    protected Object getLanguageGlobal(RubyContext context) {
        return context.getCoreLibrary().getObjectClass();
    }

    @Override
    protected boolean isObjectOfLanguage(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String toString(RubyContext context, Object value) {
        if (value == null) {
            return "<null>";
        } else if (RubyGuards.isBoxedPrimitive(value) ||  RubyGuards.isRubyBasicObject(value)) {
            return context.send(value, "inspect", null).toString();
        } else if (value instanceof String) {
            return (String) value;
        } else {
            return "<foreign>";
        }
    }

    @Override
    protected Object findMetaObject(RubyContext context, Object value) {
        return context.getCoreLibrary().getMetaClass(value);
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
        final List<OptionDescriptor> options = new ArrayList<>();

        for (OptionDescription<?> option : OptionsCatalog.allDescriptions()) {
            options.add(option.toDescriptor());
        }

        return OptionDescriptors.create(options);
    }

}
