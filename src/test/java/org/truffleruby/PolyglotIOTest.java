/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.junit.Test;
import org.truffleruby.launcher.RubyLauncher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;

public class PolyglotIOTest extends RubyTest {

    @Test
    public void testPolyglotIn() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ByteArrayInputStream in = new ByteArrayInputStream("abc".getBytes());

        try (Context context = Context.newBuilder().out(out).in(in).build()) {
            context.eval(Source.create(RubyLauncher.LANGUAGE_ID, "puts STDIN.read(3)"));
        }

        assertEquals("abc\n", out.toString());
    }

    @Test
    public void testPolyglotOut() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (Context context = Context.newBuilder().out(out).build()) {
            context.eval(Source.create(RubyLauncher.LANGUAGE_ID, "puts 'abc'"));
        }

        assertEquals("abc\n", out.toString());
    }

    @Test
    public void testPolyglotErr() {
        final ByteArrayOutputStream err = new ByteArrayOutputStream();

        try (Context context = Context.newBuilder().err(err).build()) {
            context.eval(Source.create(RubyLauncher.LANGUAGE_ID, "STDERR.puts 'abc'"));
        }

        assertEquals("abc\n", err.toString());
    }

}
