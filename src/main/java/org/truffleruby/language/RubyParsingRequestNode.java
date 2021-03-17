/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.TruffleLanguage.ContextReference;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.source.Source;

public class RubyParsingRequestNode extends RubyBaseRootNode implements InternalRootNode {

    private final ContextReference<RubyContext> contextReference;
    private final InternalMethod method;

    @Child private DirectCallNode callNode;

    public RubyParsingRequestNode(RubyLanguage language, RubyContext context, Source source, String[] argumentNames) {
        super(language, null, null);
        this.contextReference = lookupContextReference(RubyLanguage.class);

        final TranslatorDriver translator = new TranslatorDriver(context);

        final RubyRootNode rootNode = translator.parse(
                new RubySource(source, language.getSourcePath(source)),
                ParserContext.TOP_LEVEL,
                argumentNames,
                null,
                null,
                true,
                null);

        final RootCallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);

        callNode = insert(Truffle.getRuntime().createDirectCallNode(callTarget));
        callNode.forceInlining();

        final SharedMethodInfo sharedMethodInfo = rootNode.getSharedMethodInfo();
        method = new InternalMethod(
                context,
                sharedMethodInfo,
                sharedMethodInfo.getLexicalScope(),
                DeclarationContext.topLevel(context),
                sharedMethodInfo.getMethodNameForNotBlock(),
                context.getCoreLibrary().objectClass,
                Visibility.PUBLIC,
                callTarget);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final RubyLanguage language = getLanguage(RubyLanguage.class);
        final RubyContext context = contextReference.get();

        printTimeMetric("before-script");
        try {
            final Object value = callNode.call(RubyArguments.pack(
                    null,
                    null,
                    method,
                    null,
                    context.getCoreLibrary().mainObject,
                    Nil.INSTANCE,
                    frame.getArguments()));

            // The return value will be leaked to Java, so share it if the Context API is used.
            // We share conditionally on EMBEDDED to avoid sharing return values used in RubyLauncher.
            if (language.options.SHARED_OBJECTS_ENABLED && context.getOptions().EMBEDDED) {
                SharedObjects.writeBarrier(language, value);
            }

            return value;
        } finally {
            printTimeMetric("after-script");
        }
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
