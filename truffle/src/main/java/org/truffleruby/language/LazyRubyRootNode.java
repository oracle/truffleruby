/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
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
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.TranslatorDriver;

import java.util.List;

public class LazyRubyRootNode extends RootNode implements InternalRootNode {

    private final Source source;
    private final List<String> argumentNames;

    @Child private Node findContextNode = RubyLanguage.INSTANCE.unprotectedCreateFindContextNode();

    @CompilationFinal private RubyContext cachedContext;
    @CompilationFinal private DynamicObject mainObject;
    @CompilationFinal private InternalMethod method;

    @Child private DirectCallNode callNode;

    public LazyRubyRootNode(SourceSection sourceSection, FrameDescriptor frameDescriptor, Source source,
                            List<String> argumentNames) {
        super(RubyLanguage.class, sourceSection, frameDescriptor);
        this.source = source;
        this.argumentNames = argumentNames;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final RubyContext context = RubyLanguage.INSTANCE.unprotectedFindContext(findContextNode);

        if (cachedContext == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            cachedContext = context;
        }

        if (callNode == null || context != cachedContext) {
            CompilerDirectives.transferToInterpreterAndInvalidate();

            final TranslatorDriver translator = new TranslatorDriver(context);

            final ParserContext parserContext;

            if (source.getName().equals("main") && source.isInternal()) {
                parserContext = ParserContext.TOP_LEVEL_FIRST;
            } else {
                parserContext = ParserContext.TOP_LEVEL;
            }

            final RubyRootNode rootNode = translator.parse(context, source, UTF8Encoding.INSTANCE,
                    parserContext, argumentNames.toArray(new String[argumentNames.size()]), null, null, true, null);

            final CallTarget callTarget = Truffle.getRuntime().createCallTarget(rootNode);

            callNode = insert(Truffle.getRuntime().createDirectCallNode(callTarget));
            callNode.forceInlining();

            mainObject = context.getCoreLibrary().getMainObject();
            method = new InternalMethod(context, rootNode.getSharedMethodInfo(), rootNode.getSharedMethodInfo().getLexicalScope(),
                    rootNode.getSharedMethodInfo().getName(), context.getCoreLibrary().getObjectClass(), Visibility.PUBLIC, callTarget);
        }

        Object[] arguments = RubyArguments.pack(
                null,
                null,
                method,
                DeclarationContext.TOP_LEVEL,
                null,
                mainObject,
                null,
                frame.getArguments());
        final Object value = callNode.call(frame, arguments);

        // The return value will be leaked to Java, share it.
        if (context.getOptions().SHARED_OBJECTS_ENABLED) {
            SharedObjects.writeBarrier(context, value);
        }

        return value;
    }

}
