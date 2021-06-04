/*
 * Copyright (c) 2016, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.jcodings.Encoding;
import org.jcodings.specific.USASCIIEncoding;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class LazyIntRope extends ManagedRope {

    final int value;

    public LazyIntRope(int value) {
        this(value, USASCIIEncoding.INSTANCE, length(value));
    }

    public LazyIntRope(int value, Encoding encoding, int length) {
        super(encoding, CodeRange.CR_7BIT, length, length, null);
        this.value = value;
        assert Integer.toString(value).length() == length : value + " " + length;
    }

    // @formatter:off
    @CompilationFinal(dimensions = 1) private static final long[] LENGTH_TABLE = {
            0x100000000L, 0x1FFFFFFF6L, 0x1FFFFFFF6L,
            0x1FFFFFFF6L, 0x2FFFFFF9CL, 0x2FFFFFF9CL,
            0x2FFFFFF9CL, 0x3FFFFFC18L, 0x3FFFFFC18L,
            0x3FFFFFC18L, 0x4FFFFD8F0L, 0x4FFFFD8F0L,
            0x4FFFFD8F0L, 0x4FFFFD8F0L, 0x5FFFE7960L,
            0x5FFFE7960L, 0x5FFFE7960L, 0x6FFF0BDC0L,
            0x6FFF0BDC0L, 0x6FFF0BDC0L, 0x7FF676980L,
            0x7FF676980L, 0x7FF676980L, 0x7FF676980L,
            0x8FA0A1F00L, 0x8FA0A1F00L, 0x8FA0A1F00L,
            0x9C4653600L, 0x9C4653600L, 0x9C4653600L,
            0xA00000000L, 0xA00000000L
    };
    // @formatter:on

    // From https://lemire.me/blog/2021/06/03/computing-the-number-of-digits-of-an-integer-even-faster/
    // and https://github.com/lemire/Code-used-on-Daniel-Lemire-s-blog/blob/4e6e171a7d/2021/06/03/digitcount.c (license: public domain)
    private static int length(int value) {
        final int sign;
        if (CompilerDirectives.injectBranchProbability(CompilerDirectives.UNLIKELY_PROBABILITY, value < 0)) {
            // We can't represent -Integer.MIN_VALUE (it results in Integer.MIN_VALUE), so we need to handle it explicitly
            if (CompilerDirectives
                    .injectBranchProbability(CompilerDirectives.SLOWPATH_PROBABILITY, value == Integer.MIN_VALUE)) {
                return 11;
            }

            sign = 1;
            value = -value;
        } else {
            sign = 0;
        }

        final int bits = 31 - Integer.numberOfLeadingZeros(value | 1);
        int digits = (int) ((value + LENGTH_TABLE[bits]) >>> 32);
        return sign + digits;
    }

    @Override
    Rope withEncoding7bit(Encoding newEncoding, ConditionProfile bytesNotNull) {
        assert getCodeRange() == CodeRange.CR_7BIT;
        return new LazyIntRope(value, newEncoding, length(value));
    }

    @Override
    Rope withBinaryEncoding(ConditionProfile bytesNotNull) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw new UnsupportedOperationException("Must only be called for CR_VALID Strings");
    }


    @Override
    protected byte[] getBytesSlow() {
        return RopeOperations.encodeAsciiBytes(valueToString(value));
    }

    @TruffleBoundary
    private String valueToString(int value) {
        return Integer.toString(value);
    }

    public int getValue() {
        return value;
    }

    @Override
    protected byte getByteSlow(int index) {
        return getBytes()[index];
    }

}
