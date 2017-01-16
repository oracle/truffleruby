/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.collections;

import org.jruby.truffle.core.rope.Rope;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ByteArrayBuilder {

    private ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    public int getLength() {
        return bytes.size();
    }

    public void append(int b) {
        bytes.write(b);
    }

    public void append(byte b) {
        bytes.write(b);
    }

    public void append(byte[] appendBytes) {
        append(appendBytes, 0, appendBytes.length);
    }

    public void append(Rope other) {
        append(other.getBytes());
    }

    public void append(byte[] appendBytes, int appendStart, int appendLength) {
        bytes.write(appendBytes, appendStart, appendLength);
    }

    public byte[] getBytes() {
        return bytes.toByteArray();
    }

    public void clear() {
        bytes = new ByteArrayOutputStream();
    }

    public String toString() {
        return toString(StandardCharsets.ISO_8859_1);
    }

    private String toString(Charset charset) {
        return charset.decode(ByteBuffer.wrap(getBytes())).toString();
    }

}
