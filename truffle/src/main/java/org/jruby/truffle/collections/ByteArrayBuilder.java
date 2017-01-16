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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ByteArrayBuilder {

    private byte[] bytes = new byte[16];
    private int length;

    public int getLength() {
        return length;
    }

    public void append(int b) {
        append((byte) b);
    }

    public void append(byte b) {
        ensureSpace(1);
        bytes[length] = b;
        length++;
    }

    public void append(byte[] appendBytes) {
        append(appendBytes, 0, appendBytes.length);
    }

    public void append(byte[] appendBytes, int appendStart, int appendLength) {
        ensureSpace(appendLength);
        System.arraycopy(appendBytes, 0, bytes, length, appendLength);
        length += appendLength;
    }

    private void ensureSpace(int space) {
        if (length + space > bytes.length) {
            bytes = Arrays.copyOf(bytes, (bytes.length + space) * 2);
        }
    }

    public byte[] getBytes() {
        return Arrays.copyOf(bytes, length);
    }

    public void clear() {
        bytes = new byte[]{};
        length = 0;
    }

    public String toString() {
        return toString(StandardCharsets.ISO_8859_1);
    }

    private String toString(Charset charset) {
        return charset.decode(ByteBuffer.wrap(getBytes())).toString();
    }

}
