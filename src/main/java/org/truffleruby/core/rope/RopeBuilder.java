/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import static org.truffleruby.core.rope.CodeRange.CR_UNKNOWN;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.truffleruby.collections.ByteArrayBuilder;

public class RopeBuilder extends ByteArrayBuilder {

    private Encoding encoding = ASCIIEncoding.INSTANCE;

    public RopeBuilder() {
        super();
    }

    public RopeBuilder(int size) {
        super(size);
    }

    public static RopeBuilder createRopeBuilder(int size) {
        return new RopeBuilder(size);
    }

    public static RopeBuilder createRopeBuilder(byte[] bytes, Encoding encoding) {
        final RopeBuilder builder = new RopeBuilder(bytes.length);
        builder.append(bytes);
        builder.setEncoding(encoding);
        return builder;
    }

    public static RopeBuilder createRopeBuilder(byte[] wrap) {
        final RopeBuilder builder = new RopeBuilder(wrap.length);
        builder.append(wrap);
        return builder;
    }

    public static RopeBuilder createRopeBuilder(byte[] wrap, int index, int len) {
        final RopeBuilder builder = new RopeBuilder(len);
        builder.append(wrap, index, len);
        return builder;
    }

    public Encoding getEncoding() {
        return encoding;
    }

    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    public void append(Rope other) {
        append(other.getBytes());
    }

    public ManagedRope toRope() {
        return toRope(CR_UNKNOWN);
    }

    public ManagedRope toRope(CodeRange codeRange) {
        // TODO CS 17-Jan-16 can we take the bytes from the RopeBuilder and set its bytes to null so it can't use them again
        return RopeOperations.create(getBytes(), encoding, codeRange);
    }

}
