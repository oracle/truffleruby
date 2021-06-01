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

    protected LazyIntRope(int value, Encoding encoding, int length) {
        super(encoding, CodeRange.CR_7BIT, length, length, null);
        this.value = value;
        assert Integer.toString(value).length() == length : value + " " + length;
    }

    @CompilationFinal(dimensions = 1) private static final int[] LENGTH_TABLE = {
            9,
            99,
            999,
            9999,
            99999,
            999999,
            9999999,
            99999999,
            999999999,
            Integer.MAX_VALUE };

    // From https://lemire.me/blog/2021/05/28/computing-the-number-of-digits-of-an-integer-quickly/
    // and https://github.com/lemire/Code-used-on-Daniel-Lemire-s-blog/blob/9d4cd21d0a/2021/05/28/digitcount.java (license: public domain)
    private static int length(int value) {
        final int sign;
        if (CompilerDirectives.injectBranchProbability(CompilerDirectives.UNLIKELY_PROBABILITY, value < 0)) {
            sign = 1;
            value = -value;
        } else {
            sign = 0;
        }

        final int bits = 31 - Integer.numberOfLeadingZeros(value | 1);
        int digits = ((9 * bits) >>> 5);

        if (value > LENGTH_TABLE[digits]) {
            digits += 1;
        }

        return sign + digits + 1;
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
