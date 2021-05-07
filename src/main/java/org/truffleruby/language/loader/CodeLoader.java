/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.loader;

import org.truffleruby.RubyContext;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubySource;
import org.truffleruby.parser.TranslatorDriver;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;

public class CodeLoader {

    private final RubyContext context;

    public CodeLoader(RubyContext context) {
        this.context = context;
    }

    @TruffleBoundary
    public RootCallTarget parse(RubySource source,
            ParserContext parserContext,
            MaterializedFrame parentFrame,
            RubyModule wrap,
            boolean ownScopeForAssignments,
            Node currentNode) {
        final TranslatorDriver translator = new TranslatorDriver(context, source);
        return translator.parse(source, parserContext, null, parentFrame, wrap, ownScopeForAssignments, currentNode);
    }

    @TruffleBoundary
    public DeferredCall prepareExecute(RootCallTarget callTarget,
            ParserContext parserContext,
            DeclarationContext declarationContext,
            MaterializedFrame parentFrame,
            Object self) {
        final RubyRootNode rootNode = RubyRootNode.of(callTarget);

        final RubyModule declaringModule;
        if (parserContext == ParserContext.EVAL && parentFrame != null) {
            declaringModule = RubyArguments.getMethod(parentFrame).getDeclaringModule();
        } else if (parserContext == ParserContext.MODULE) {
            declaringModule = (RubyModule) self;
        } else {
            declaringModule = context.getCoreLibrary().objectClass;
        }

        final LexicalScope lexicalScope;
        if (parentFrame != null) {
            lexicalScope = RubyArguments.getMethod(parentFrame).getLexicalScope();
        } else {
            lexicalScope = context.getRootLexicalScope();
        }

        final InternalMethod method = new InternalMethod(
                context,
                rootNode.getSharedMethodInfo(),
                lexicalScope,
                declarationContext,
                rootNode.getSharedMethodInfo().getMethodNameForNotBlock(),
                declaringModule,
                Visibility.PUBLIC,
                callTarget);

        return new DeferredCall(callTarget, RubyArguments.pack(
                parentFrame,
                null,
                method,
                null,
                self,
                Nil.INSTANCE,
                RubyNode.EMPTY_ARGUMENTS));
    }

    public static class DeferredCall {

        private final RootCallTarget callTarget;
        private final Object[] arguments;

        public DeferredCall(RootCallTarget callTarget, Object[] arguments) {
            this.callTarget = callTarget;
            this.arguments = arguments;
        }

        public Object call(IndirectCallNode callNode) {
            return callNode.call(callTarget, arguments);
        }

        public Object callWithoutCallNode() {
            return callTarget.call(arguments);
        }

    }

}
