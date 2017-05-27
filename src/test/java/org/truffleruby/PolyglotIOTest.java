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

import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.vm.PolyglotEngine;
import org.junit.Test;
import org.truffleruby.options.OptionsCatalog;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;

public class PolyglotIOTest extends RubyTest {

    @Test
    public void testPolyglotIn() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final ByteArrayInputStream in = new ByteArrayInputStream("abc".getBytes());

        final PolyglotEngine engine = PolyglotEngine.newBuilder()
                .config(RubyLanguage.MIME_TYPE, OptionsCatalog.EXCEPTIONS_TRANSLATE_ASSERT.getName(), false)
                .setOut(out)
                .setIn(in)
                .build();

        engine.eval(Source.newBuilder("puts STDIN.read(3)").name("test.rb").mimeType(RubyLanguage.MIME_TYPE).build());

        engine.dispose();

        assertEquals("abc\n", out.toString());
    }

    @Test
    public void testPolyglotOut() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final PolyglotEngine engine = PolyglotEngine.newBuilder()
                .config(RubyLanguage.MIME_TYPE, OptionsCatalog.EXCEPTIONS_TRANSLATE_ASSERT.getName(), false)
                .setOut(out)
                .build();

        engine.eval(Source.newBuilder("puts 'abc'").name("test.rb").mimeType(RubyLanguage.MIME_TYPE).build());

        engine.dispose();

        assertEquals("abc\n", out.toString());
    }

    @Test
    public void testPolyglotErr() {
        final ByteArrayOutputStream err = new ByteArrayOutputStream();

        final PolyglotEngine engine = PolyglotEngine.newBuilder()
                .config(RubyLanguage.MIME_TYPE, OptionsCatalog.EXCEPTIONS_TRANSLATE_ASSERT.getName(), false)
                .setErr(err)
                .build();

        engine.eval(Source.newBuilder("STDERR.puts 'abc'").name("test.rb").mimeType(RubyLanguage.MIME_TYPE).build());

        engine.dispose();

        assertEquals("abc\n", err.toString());
    }

}
