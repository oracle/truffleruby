/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.backtrace.InternalRootNode;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.shared.SharedObjects;
import org.truffleruby.launcher.Launcher;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.TranslatorDriver;

import java.util.List;

public class LazyRubyRootNode extends RubyBaseRootNode implements InternalRootNode {

    private final TruffleLanguage.ContextReference<RubyContext> contextReference;
    private final SourceSection sourceSection;
    private final Source source;
    private final List<String> argumentNames;

    @CompilationFinal private RubyContext cachedContext;
    @CompilationFinal private DynamicObject mainObject;
    @CompilationFinal private InternalMethod method;

    @Child private DirectCallNode callNode;

    public LazyRubyRootNode(RubyLanguage language, SourceSection sourceSection, FrameDescriptor frameDescriptor, Source source,
                            List<String> argumentNames) {
        super(language, frameDescriptor);
        contextReference = language.getContextReference();
        this.sourceSection = sourceSection;
        this.source = source;
        this.argumentNames = argumentNames;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        printTimeMetric("before-script");
        final RubyContext context = contextReference.get();

        if (cachedContext == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            cachedContext = context;
        }

        if (callNode == null || context != cachedContext) {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            final TranslatorDriver translator = new TranslatorDriver(context);

            final RubyRootNode rootNode = translator.parse(source, UTF8Encoding.INSTANCE,
                    ParserContext.TOP_LEVEL, argumentNames.toArray(new String[argumentNames.size()]), null, null, true, null);

            final CallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);

            callNode = insert(Truffle.getRuntime().createDirectCallNode(callTarget));
            callNode.forceInlining();

            mainObject = context.getCoreLibrary().getMainObject();
            method = new InternalMethod(context, rootNode.getSharedMethodInfo(), rootNode.getSharedMethodInfo().getLexicalScope(), DeclarationContext.topLevel(context),
                    rootNode.getSharedMethodInfo().getName(), context.getCoreLibrary().getObjectClass(), Visibility.PUBLIC, callTarget);
        }

        Object[] arguments = RubyArguments.pack(
                null,
                null,
                method,
                null,
                mainObject,
                null,
                frame.getArguments());
        final Object value = callNode.call(arguments);

        // The return value will be leaked to Java, share it.
        if (context.getOptions().SHARED_OBJECTS_ENABLED) {
            SharedObjects.writeBarrier(context, value);
        }

        printTimeMetric("after-script");
        return value;
    }

    @TruffleBoundary
    private void printTimeMetric(String id) {
        Launcher.printTruffleTimeMetric(id);
    }

    @Override
    public SourceSection getSourceSection() {
        return sourceSection;
    }
}
