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

import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;

public class RopeConstants {

    public static final Map<String, LeafRope> ROPE_CONSTANTS = new HashMap<>();

    public static final byte[] EMPTY_BYTES = new byte[0];

    public static final LeafRope EMPTY_ASCII_8BIT_ROPE = withHashCode(
            new AsciiOnlyLeafRope(EMPTY_BYTES, ASCIIEncoding.INSTANCE));
    public static final LeafRope EMPTY_US_ASCII_ROPE = withHashCode(
            new AsciiOnlyLeafRope(EMPTY_BYTES, USASCIIEncoding.INSTANCE));
    public static final LeafRope EMPTY_UTF8_ROPE = withHashCode(
            new AsciiOnlyLeafRope(EMPTY_BYTES, UTF8Encoding.INSTANCE));

    @CompilationFinal(dimensions = 1) public static final LeafRope[] UTF8_SINGLE_BYTE_ROPES = new LeafRope[256];
    @CompilationFinal(dimensions = 1) public static final LeafRope[] US_ASCII_SINGLE_BYTE_ROPES = new LeafRope[256];
    @CompilationFinal(dimensions = 1) public static final LeafRope[] ASCII_8BIT_SINGLE_BYTE_ROPES = new LeafRope[256];

    static {
        for (int i = 0; i < 128; i++) {
            final byte[] bytes = new byte[]{ (byte) i };

            UTF8_SINGLE_BYTE_ROPES[i] = withHashCode(new AsciiOnlyLeafRope(bytes, UTF8Encoding.INSTANCE));
            US_ASCII_SINGLE_BYTE_ROPES[i] = withHashCode(new AsciiOnlyLeafRope(bytes, USASCIIEncoding.INSTANCE));
            ASCII_8BIT_SINGLE_BYTE_ROPES[i] = withHashCode(new AsciiOnlyLeafRope(bytes, ASCIIEncoding.INSTANCE));
        }

        for (int i = 128; i < 256; i++) {
            final byte[] bytes = new byte[]{ (byte) i };

            UTF8_SINGLE_BYTE_ROPES[i] = withHashCode(new InvalidLeafRope(bytes, UTF8Encoding.INSTANCE, 1));
            US_ASCII_SINGLE_BYTE_ROPES[i] = withHashCode(new InvalidLeafRope(bytes, USASCIIEncoding.INSTANCE, 1));
            ASCII_8BIT_SINGLE_BYTE_ROPES[i] = withHashCode(new ValidLeafRope(bytes, ASCIIEncoding.INSTANCE, 1));
        }
    }


    public static LeafRope lookupUSASCII(String string) {
        if (string.length() == 1) {
            return US_ASCII_SINGLE_BYTE_ROPES[string.charAt(0)];
        } else {
            return ROPE_CONSTANTS.get(string);
        }
    }

    private static <T> T withHashCode(T object) {
        object.hashCode();
        return object;
    }

}
