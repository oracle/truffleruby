/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.truffleruby.core.array.ArrayBuilderNode;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.shared.TruffleRuby;
import org.truffleruby.shared.options.OptionsCatalog;

import jline.internal.InputStreamReader;

public class ArrayBuilderTest {

    private Context context;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @Test
    public void emptyyBuilderTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            Object store = builder.start();
            store = builder.finish(store, 0);
            assertEquals(int[].class, store.getClass());
        });
    }

    @Test
    public void arrayBuilderAppendNothingTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            Object store = builder.start(10);
            store = builder.finish(store, 0);
            assertEquals(int[].class, store.getClass());
        });
    }

    @Test
    public void arrayBuilderAppendIntTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            Object store = builder.start(10);
            for (int i = 0; i < 10; i++) {
                store = builder.appendValue(store, i, i);
            }
            store = builder.finish(store, 10);
            assertEquals(int[].class, store.getClass());
        });
    }

    @Test
    public void arrayBuilderAppendLongTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            Object store = builder.start(10);
            for (int i = 0; i < 10; i++) {
                store = builder.appendValue(store, i, ((long) i) << 33);
            }
            store = builder.finish(store, 10);
            assertEquals(long[].class, store.getClass());
        });
    }

    @Test
    public void arrayBuilderAppendDoubleTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            Object store = builder.start(10);
            for (int i = 0; i < 10; i++) {
                store = builder.appendValue(store, i, i * 0.0d);
            }
            assertEquals(Object[].class, store.getClass());
        });
    }

    @Test
    public void arrayBuilderAppendObjectTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            Object store = builder.start(10);
            for (int i = 0; i < 10; i++) {
                store = builder.appendValue(store, i, null);
            }
            store = builder.finish(store, 10);
            assertEquals(Object[].class, store.getClass());
        });
    }

    @Test
    public void arrayBuilderAppendIntArrayTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            Object store = builder.start(10);
            DynamicObject otherStore = Layouts.ARRAY
                    .createArray(RubyLanguage.getCurrentContext().getCoreLibrary().arrayFactory, new int[10], 10);
            store = builder.appendArray(store, 0, otherStore);
            store = builder.finish(store, 10);
            assertEquals(int[].class, store.getClass());
        });
    }

    @Test
    public void arrayBuilderAppendLongArrayTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            Object store = builder.start(10);
            DynamicObject otherStore = Layouts.ARRAY
                    .createArray(RubyLanguage.getCurrentContext().getCoreLibrary().arrayFactory, new long[10], 10);
            store = builder.appendArray(store, 0, otherStore);
            store = builder.finish(store, 10);
            assertEquals(long[].class, store.getClass());
        });
    }

    @Test
    public void arrayBuilderAppendObjectArrayTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            Object store = builder.start(10);
            DynamicObject otherStore = Layouts.ARRAY
                    .createArray(RubyLanguage.getCurrentContext().getCoreLibrary().arrayFactory, new Object[10], 10);
            store = builder.appendArray(store, 0, otherStore);
            store = builder.finish(store, 10);
            assertEquals(Object[].class, store.getClass());
        });
    }

    private ArrayBuilderNode createBuilder() {
        return adopt(ArrayBuilderNode.create());
    }

    private void testInContext(Runnable test) {
        context.enter();
        try {
            test.run();
        } finally {
            context.leave();
        }
    }

    private static Source getSource(String path) {
        InputStream stream = ClassLoader.getSystemResourceAsStream(path);
        Reader reader = new InputStreamReader(stream);
        try {
            return Source.newBuilder(TruffleRuby.LANGUAGE_ID, reader, new File(path).getName()).build();
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    @Before
    public void before() {
        context = RubyTest
                .setupContext(Context.newBuilder())
                // We also want to test instrumentation works well with lazy nodes
                .option(OptionsCatalog.LAZY_TRANSLATION_USER.getName(), Boolean.TRUE.toString())
                .out(out)
                .err(err)
                .build();

        context.eval(getSource("init.rb"));

    }

    @After
    public void dispose() {
        if (context != null) {
            context.close();
        }
    }

    static <T extends Node> T adopt(T node) {
        RootNode root = new RootNode(null) {
            {
                insert(node);
            }

            @Override
            public Object execute(VirtualFrame frame) {
                return null;
            }
        };
        root.adoptChildren();
        return node;
    }


}
