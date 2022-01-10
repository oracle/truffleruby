/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.truffleruby.core.array.ArrayBuilderNode;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.array.ArrayBuilderNode.BuilderState;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.shared.TruffleRuby;

public class ArrayBuilderTest {

    private Context context;
    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @Test
    public void emptyBuilderTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            BuilderState state = builder.start();
            assertEquals(ArrayStoreLibrary.INITIAL_STORE, builder.finish(state, 0));
        });
    }

    @Test
    public void arrayBuilderAppendNothingTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            BuilderState state = builder.start(10);
            assertEquals(ArrayStoreLibrary.INITIAL_STORE, builder.finish(state, 0));
        });
    }

    @Test
    public void arrayBuilderAppendIntTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            BuilderState state = builder.start(10);
            for (int i = 0; i < 10; i++) {
                builder.appendValue(state, i, i);
            }
            assertEquals(int[].class, builder.finish(state, 10).getClass());
        });
    }

    @Test
    public void arrayBuilderAppendLongTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            BuilderState state = builder.start(10);
            for (int i = 0; i < 10; i++) {
                builder.appendValue(state, i, ((long) i) << 33);
            }
            assertEquals(long[].class, builder.finish(state, 10).getClass());
        });
    }

    @Test
    public void arrayBuilderAppendDoubleTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            BuilderState state = builder.start(10);
            for (int i = 0; i < 10; i++) {
                builder.appendValue(state, i, i * 0.0d);
            }
            assertEquals(double[].class, builder.finish(state, 10).getClass());
        });
    }

    @Test
    public void arrayBuilderAppendObjectTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            BuilderState state = builder.start(10);
            for (int i = 0; i < 10; i++) {
                builder.appendValue(state, i, new Object());
            }
            assertEquals(Object[].class, builder.finish(state, 10).getClass());
        });
    }

    @Test
    public void arrayBuilderAppendEmptyArrayTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            BuilderState state = builder.start(10);
            RubyArray otherStore = new RubyArray(
                    RubyLanguage.getCurrentContext().getCoreLibrary().arrayClass,
                    RubyLanguage.getCurrentLanguage().arrayShape,
                    ArrayStoreLibrary.INITIAL_STORE,
                    0);
            builder.appendArray(state, 0, otherStore);
            assertEquals(ArrayStoreLibrary.INITIAL_STORE, builder.finish(state, 0));
        });
    }

    @Test
    public void arrayBuilderAppendIntArrayTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            BuilderState state = builder.start(10);
            RubyArray otherStore = new RubyArray(
                    RubyLanguage.getCurrentContext().getCoreLibrary().arrayClass,
                    RubyLanguage.getCurrentLanguage().arrayShape,
                    new int[10],
                    10);
            builder.appendArray(state, 0, otherStore);
            assertEquals(int[].class, builder.finish(state, 10).getClass());
        });
    }

    @Test
    public void arrayBuilderAppendLongArrayTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            BuilderState state = builder.start(10);
            RubyArray otherStore = new RubyArray(
                    RubyLanguage.getCurrentContext().getCoreLibrary().arrayClass,
                    RubyLanguage.getCurrentLanguage().arrayShape,
                    new long[10],
                    10);
            builder.appendArray(state, 0, otherStore);
            assertEquals(long[].class, builder.finish(state, 10).getClass());
        });
    }

    @Test
    public void arrayBuilderAppendDoubleArrayTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            BuilderState state = builder.start(10);
            RubyArray otherStore = new RubyArray(
                    RubyLanguage.getCurrentContext().getCoreLibrary().arrayClass,
                    RubyLanguage.getCurrentLanguage().arrayShape,
                    new double[10],
                    10);
            builder.appendArray(state, 0, otherStore);
            assertEquals(double[].class, builder.finish(state, 10).getClass());
        });
    }

    @Test
    public void arrayBuilderAppendObjectArrayTest() {
        testInContext(() -> {
            ArrayBuilderNode builder = createBuilder();
            BuilderState state = builder.start(10);
            RubyArray otherStore = new RubyArray(
                    RubyLanguage.getCurrentContext().getCoreLibrary().arrayClass,
                    RubyLanguage.getCurrentLanguage().arrayShape,
                    new Object[10],
                    10);
            builder.appendArray(state, 0, otherStore);
            assertEquals(Object[].class, builder.finish(state, 10).getClass());
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
        context = RubyTest.setupContext(Context.newBuilder()).out(out).err(err).build();
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
            @Child Node childNode;
            {
                childNode = insert(node);
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
