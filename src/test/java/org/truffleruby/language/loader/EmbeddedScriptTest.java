/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.loader;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class EmbeddedScriptTest {

    @Test
    public void testLineContainsRubyFalse() {
        final byte[] bytes = "foo bar baz".getBytes(StandardCharsets.US_ASCII);
        assertFalse(EmbeddedScript.lineContainsRuby(bytes, 0, bytes.length));
    }

    @Test
    public void testLineContainsRubyStart() {
        final byte[] bytes = "ruby foo bar".getBytes(StandardCharsets.US_ASCII);
        assertTrue(EmbeddedScript.lineContainsRuby(bytes, 0, bytes.length));
    }

    @Test
    public void testLineContainsRubyMiddle() {
        final byte[] bytes = "foo ruby bar".getBytes(StandardCharsets.US_ASCII);
        assertTrue(EmbeddedScript.lineContainsRuby(bytes, 0, bytes.length));
    }

    @Test
    public void testLineContainsRubyEnd() {
        final byte[] bytes = "foo bar ruby".getBytes(StandardCharsets.US_ASCII);
        assertTrue(EmbeddedScript.lineContainsRuby(bytes, 0, bytes.length));
    }

    @Test
    public void testLineContainsRubyInBounds() {
        final byte[] bytes = "foo bar rub".getBytes(StandardCharsets.US_ASCII);
        assertFalse(EmbeddedScript.lineContainsRuby(bytes, 0, bytes.length));
    }

    @Test
    public void testLineContainsRubyBeforeLine() {
        final byte[] bytes = "ruby foo bar".getBytes(StandardCharsets.US_ASCII);
        assertFalse(EmbeddedScript.lineContainsRuby(bytes, 5, bytes.length - 5));
    }

    @Test
    public void testLineContainsRubyAfterLine() {
        final byte[] bytes = "foo bar ruby".getBytes(StandardCharsets.US_ASCII);
        assertFalse(EmbeddedScript.lineContainsRuby(bytes, 0, bytes.length - 5));
    }

    @Test
    public void testLineContainsRubyOffset() {
        final byte[] bytes = "foo bar ruby".getBytes(StandardCharsets.US_ASCII);
        assertTrue(EmbeddedScript.lineContainsRuby(bytes, 4, bytes.length - 4));
    }

}
