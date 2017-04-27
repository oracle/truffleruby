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
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.frame.FrameInstance;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import org.truffleruby.core.kernel.TraceManager;
import org.truffleruby.language.LazyRubyNode;
import org.truffleruby.language.LazyRubyRootNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.platform.Platform;
import org.truffleruby.platform.graal.Graal;
import org.truffleruby.stdlib.CoverageManager;

@TruffleLanguage.Registration(
        name = "Ruby",
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

    public static final String PLATFORM = String.format("%s-%s", Platform.getArchitecture(), Platform.getOSName());
    public static final String RUBY_VERSION = "2.3.3";
    public static final int    RUBY_REVISION = 0;
    public static final String COMPILE_DATE = "2017";
    public static final String ENGINE = "truffleruby";

    public static final String MIME_TYPE = "application/x-ruby";
    public static final String EXTENSION = ".rb";

    public static final String CEXT_MIME_TYPE = "application/x-sulong-library";
    public static final String CEXT_EXTENSION = ".su";

    private RubyLanguage() {
    }

    public static final RubyLanguage INSTANCE = new RubyLanguage();

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
        return new RubyContext(env);
    }

    @Override
    protected void disposeContext(RubyContext context) {
        context.shutdown();
    }

    @Override
    protected CallTarget parse(ParsingRequest request) throws Exception {
        return Truffle.getRuntime().createCallTarget(new LazyRubyRootNode(null, null, request.getSource(), request.getArgumentNames()));
    }

    @Override
    protected Object findExportedSymbol(RubyContext context, String s, boolean b) {
        return context.getInteropManager().findExportedObject(s);
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

    public Node unprotectedCreateFindContextNode() {
        return super.createFindContextNode();
    }

    public RubyContext unprotectedFindContext(Node node) {
        return super.findContext(node);
    }

    public static String getVersionString() {

        return String.format(
                "truffleruby %s, like ruby %s <%s %s %s> [%s-%s]",
                System.getProperty("graalvm.version", "unknown version"),
                RUBY_VERSION,
                TruffleOptions.AOT ? "AOT" : System.getProperty("java.vm.name", "unknown JVM"),
                TruffleOptions.AOT ? "build" : System.getProperty("java.runtime.version", System.getProperty("java.version", "unknown runtime version")),
                Graal.isGraal() ? "with Graal" : "without Graal",
                Platform.getOSName(),
                Platform.getArchitecture()
        );
    }

    public static String getCopyrightString() {
        return "truffleruby - Copyright (c) 2013-2017 Oracle and/or its affiliates";
    }

}
