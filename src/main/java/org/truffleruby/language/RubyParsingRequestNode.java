/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.backtrace.InternalRootNode;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.methods.SharedMethodInfo;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubySource;
import org.truffleruby.parser.TranslatorDriver;
import org.truffleruby.shared.Metrics;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;

public class RubyParsingRequestNode extends RubyBaseRootNode implements InternalRootNode {

    private final Source source;
    private final boolean interactive;
    private final String[] argumentNames;

    @CompilationFinal private TruffleLanguage.ContextReference<RubyContext> contextReference;
    @CompilationFinal private Rope sourceRope;
    @CompilationFinal private RubyContext cachedContext;
    @CompilationFinal private DynamicObject mainObject;
    @CompilationFinal private InternalMethod method;

    @Child private DirectCallNode callNode;

    public RubyParsingRequestNode(RubyLanguage language, Source source, String[] argumentNames) {
        super(language, null, null);
        this.source = source;
        this.interactive = source.isInteractive();
        this.argumentNames = argumentNames;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        printTimeMetric("before-script");
        if (contextReference == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            contextReference = lookupContextReference(RubyLanguage.class);
        }
        final RubyContext context = contextReference.get();

        if (interactive) {
            if (sourceRope == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                sourceRope = StringOperations.encodeRope(source.getCharacters().toString(), UTF8Encoding.INSTANCE);
            }

            // Just do Truffle::Boot::INTERACTIVE_BINDING.eval(code) for interactive sources.
            // It's the semantics we want and takes care of caching correctly based on the Binding's FrameDescriptor.
            final Object interactiveBinding = Layouts.MODULE
                    .getFields(context.getCoreLibrary().getTruffleBootModule())
                    .getConstant("INTERACTIVE_BINDING")
                    .getValue();
            return context.send(interactiveBinding, "eval", StringOperations.createString(context, sourceRope));
        }

        if (cachedContext == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            cachedContext = context;
        }

        if (callNode == null || context != cachedContext) {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            final TranslatorDriver translator = new TranslatorDriver(context);

            final RubyRootNode rootNode = translator.parse(
                    new RubySource(source),
                    ParserContext.TOP_LEVEL,
                    argumentNames,
                    null,
                    null,
                    true,
                    null);

            final RootCallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);

            callNode = insert(Truffle.getRuntime().createDirectCallNode(callTarget));
            callNode.forceInlining();

            mainObject = context.getCoreLibrary().getMainObject();

            final SharedMethodInfo sharedMethodInfo = rootNode.getSharedMethodInfo();
            method = new InternalMethod(
                    context,
                    sharedMethodInfo,
                    sharedMethodInfo.getLexicalScope(),
                    DeclarationContext.topLevel(context),
                    sharedMethodInfo.getName(),
                    context.getCoreLibrary().getObjectClass(),
                    Visibility.PUBLIC,
                    callTarget);
        }

        final Object value = callNode.call(RubyArguments.pack(
                null,
                null,
                method,
                null,
                mainObject,
                null,
                frame.getArguments()));

        // The return value will be leaked to Java, so share it if the Context API is used.
        // We share conditionally on EMBEDDED to avoid sharing return values used in RubyLauncher.
        if (context.getOptions().SHARED_OBJECTS_ENABLED && context.getOptions().EMBEDDED) {
            SharedObjects.writeBarrier(context, value);
        }

        printTimeMetric("after-script");
        return value;
    }

    @Override
    public String getName() {
        return "parsing-request";
    }

    @TruffleBoundary
    private void printTimeMetric(String id) {
        Metrics.printTime(id);
    }

}
