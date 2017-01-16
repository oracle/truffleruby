/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.parser;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jruby.truffle.core.rope.Rope;
import org.jruby.truffle.core.rope.RopeOperations;

import java.io.ByteArrayOutputStream;

import static org.jruby.truffle.core.rope.CodeRange.CR_UNKNOWN;

public class ParserByteListBuilder {

    private ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    private Encoding encoding = ASCIIEncoding.INSTANCE;

    public int getLength() {
        return bytes.size();
    }

    public Encoding getEncoding() {
        return encoding;
    }

    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
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

    public Rope toRope() {
        return RopeOperations.create(getBytes(), encoding, CR_UNKNOWN);
    }

}
