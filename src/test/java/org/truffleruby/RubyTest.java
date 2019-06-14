/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.NodeUtil;
import com.oracle.truffle.api.source.Source;

import org.graalvm.polyglot.Context;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.shared.options.OptionsCatalog;
import org.truffleruby.parser.ParserContext;
import org.truffleruby.parser.RubySource;
import org.truffleruby.shared.TruffleRuby;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public abstract class RubyTest {

    protected <T extends Node> void testWithNode(String text, Class<T> nodeClass, Consumer<T> test) {
        testWithAST(text, (root) -> {
            final List<T> instances = NodeUtil.findAllNodeInstances(root, nodeClass);
            assertEquals(1, instances.size());
            final T node = instances.get(0);
            test.accept(node);
        });
    }

    protected void testWithAST(String text, Consumer<RubyRootNode> test) {
        final Source source = Source.newBuilder(TruffleRuby.LANGUAGE_ID, text, "test.rb").build();

        testInContext(() -> {
            final RubyRootNode rootNode = RubyLanguage.getCurrentContext().getCodeLoader().parse(new RubySource(source), ParserContext.TOP_LEVEL, null, true, null);
            rootNode.adoptChildren();
            test.accept(rootNode);
        });
    }

    protected void testInContext(Runnable test) {
        try (Context context = createContext()) {
            context.eval(org.graalvm.polyglot.Source.create(TruffleRuby.LANGUAGE_ID, "-> test { test.call }"))
                    .execute(test);
        }
    }

    public static Context.Builder setupContext(Context.Builder builder) {
        return builder
                .allowAllAccess(true)
                .option(OptionsCatalog.EXCEPTIONS_TRANSLATE_ASSERT.getName(), Boolean.FALSE.toString())
                .option(OptionsCatalog.BASICOPS_INLINE.getName(), Boolean.FALSE.toString());
    }

    public static Context createContext() {
        return setupContext(Context.newBuilder()).build();
    }

}
