/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import static org.truffleruby.core.rope.CodeRange.CR_UNKNOWN;

import com.oracle.truffle.api.strings.InternalByteArray;
import com.oracle.truffle.api.strings.TruffleString;
import org.jcodings.Encoding;
import org.truffleruby.collections.ByteArrayBuilder;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;
import org.truffleruby.core.string.RubyString;

public class RopeBuilder extends ByteArrayBuilder {

    private RubyEncoding encoding = Encodings.BINARY;

    public RopeBuilder() {
        super();
    }

    public RopeBuilder(int size) {
        super(size);
    }

    public static RopeBuilder createRopeBuilder(int size) {
        return new RopeBuilder(size);
    }

    public static RopeBuilder createRopeBuilder(byte[] bytes, RubyEncoding encoding) {
        final RopeBuilder builder = new RopeBuilder(bytes.length);
        builder.append(bytes);
        builder.setEncoding(encoding);
        return builder;
    }

    public static RopeBuilder createRopeBuilder(RubyString rubyString) {
        final RubyEncoding enc = rubyString.encoding;
        return createRopeBuilder(rubyString.tstring.getInternalByteArrayUncached(enc.tencoding), enc);
    }

    public static RopeBuilder createRopeBuilder(byte[] bytes) {
        final RopeBuilder builder = new RopeBuilder(bytes.length);
        builder.append(bytes);
        return builder;
    }

    public static RopeBuilder createRopeBuilder(byte[] bytes, int index, int len) {
        final RopeBuilder builder = new RopeBuilder(len);
        builder.append(bytes, index, len);
        return builder;
    }

    public static RopeBuilder createRopeBuilder(InternalByteArray bytes) {
        return createRopeBuilder(bytes.getArray(), bytes.getOffset(), bytes.getLength());
    }

    public static RopeBuilder createRopeBuilder(InternalByteArray bytes, RubyEncoding encoding) {
        var builder = createRopeBuilder(bytes);
        builder.setEncoding(encoding);
        return builder;
    }

    public static RopeBuilder createRopeBuilder(TStringWithEncoding str) {
        return createRopeBuilder(str.getInternalByteArray(), str.encoding);
    }

    public RubyEncoding getRubyEncoding() {
        return encoding;
    }

    public Encoding getEncoding() {
        return encoding.jcoding;
    }

    public void setEncoding(RubyEncoding encoding) {
        this.encoding = encoding;
    }

    public void append(Rope other) {
        append(other.getBytes());
    }

    public Rope toRope() {
        return toRope(CR_UNKNOWN);
    }

    public Rope toRope(CodeRange codeRange) {
        // TODO CS 17-Jan-16 can we take the bytes from the RopeBuilder and set its bytes to null so it can't use them again
        return RopeOperations.create(getBytes(), encoding.jcoding, codeRange);
    }

    public TruffleString toTString() {
        return TStringUtils.fromByteArray(getBytes(), encoding);
    }

    public TStringWithEncoding toTStringWithEnc() {
        return new TStringWithEncoding(TStringUtils.fromByteArray(getBytes(), encoding), encoding);
    }

}
