/*
 * Copyright (c) 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import java.util.Arrays;

import com.oracle.truffle.api.CompilerDirectives;
import org.truffleruby.RubyLanguage;
import org.truffleruby.annotations.Split;
import org.truffleruby.language.RubyMethodRootNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.methods.Arity;
import org.truffleruby.language.methods.CachedLazyCallTargetSupplier;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.source.Source;
import org.prism.Nodes;
import org.truffleruby.parser.parser.ParserSupport;

public final class YARPDefNodeTranslator extends YARPTranslator {
    static final String DEFAULT_KEYWORD_REST_NAME = ParserSupport.KWREST_VAR;
    static final String DEFAULT_REST_NAME = ParserSupport.REST_VAR;

    private final boolean shouldLazyTranslate;

    public YARPDefNodeTranslator(
            RubyLanguage language,
            YARPTranslator parent,
            TranslatorEnvironment environment,
            byte[] sourceBytes,
            Source source,
            ParserContext parserContext,
            Node currentNode,
            RubyDeferredWarnings rubyWarnings) {
        super(language, parent, environment, sourceBytes, source, parserContext, currentNode, rubyWarnings);

        if (parserContext.isEval() || environment.getParseEnvironment().isCoverageEnabled()) {
            shouldLazyTranslate = false;
        } else if (language.getSourcePath(source).startsWith(language.coreLoadPath)) {
            shouldLazyTranslate = language.options.LAZY_TRANSLATION_CORE;
        } else {
            shouldLazyTranslate = language.options.LAZY_TRANSLATION_USER;
        }
    }

    private RubyNode compileMethodBody(Nodes.DefNode node, Arity arity) {
        declareLocalVariables(node);

        final RubyNode loadArguments = new YARPLoadArgumentsTranslator(
                node.parameters,
                language,
                environment,
                arity,
                false,
                true,
                this).translate();

        RubyNode body = translateNodeOrNil(node.body).simplifyAsTailExpression();
        body = sequence(Arrays.asList(loadArguments, body));

        if (environment.getFlipFlopStates().size() > 0) {
            body = sequence(Arrays.asList(initFlipFlopStates(environment), body));
        }

        return body;
    }

    private RubyMethodRootNode translateMethodNode(Nodes.DefNode node, Arity arity) {
        RubyNode body = compileMethodBody(node, arity);

        return new RubyMethodRootNode(
                language,
                getSourceSection(node),
                environment.computeFrameDescriptor(),
                environment.getSharedMethodInfo(),
                body,
                Split.HEURISTIC,
                environment.getReturnID(),
                arity);
    }

    public CachedLazyCallTargetSupplier buildMethodNodeCompiler(Nodes.DefNode node, Arity arity) {
        if (shouldLazyTranslate) {
            return new CachedLazyCallTargetSupplier(
                    () -> translateMethodNode(node, arity).getCallTarget());
        } else {
            final RubyMethodRootNode root = translateMethodNode(node, arity);
            return new CachedLazyCallTargetSupplier(() -> root.getCallTarget());
        }
    }

    private void declareLocalVariables(Nodes.DefNode node) {
        // YARP adds hidden locals when there are rest/keyrest/block anonymous parameters or ... declared
        for (String name : node.locals) {
            switch (name) {
                case "*" -> environment.declareVar(DEFAULT_REST_NAME);
                case "**" -> environment.declareVar(DEFAULT_KEYWORD_REST_NAME);
                case "&" ->
                    // we don't support yet Ruby 3.1's anonymous block parameter
                    throw CompilerDirectives.shouldNotReachHere("Anonymous block parameters aren't supported yet");
                case "..." -> { // YARP adds "..." to locals by some reason
                }
                default -> environment.declareVar(name);
            }
        }
    }

}
