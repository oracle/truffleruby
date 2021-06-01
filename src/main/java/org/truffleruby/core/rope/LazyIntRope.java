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
        assert Integer.toString(value).length() == length;
    }

    private static int length(int value) {
        final int sign;

        if (value < 0) {
            /* We can't represent -Integer.MIN_VALUE, and we're about to multiple by 10 to add the space needed for the
             * negative character, so handle both of those out-of-range cases. */

            if (value <= -1000000000) {
                return 11;
            }

            value = -value;
            sign = 1;
        } else {
            sign = 0;
        }

        return sign + (value < 1E5
                ? value < 1E2 ? value < 1E1 ? 1 : 2 : value < 1E3 ? 3 : value < 1E4 ? 4 : 5
                : value < 1E7 ? value < 1E6 ? 6 : 7 : value < 1E8 ? 8 : value < 1E9 ? 9 : 10);
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
