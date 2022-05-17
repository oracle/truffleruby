/*
 * Copyright (c) 2022 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Some of the code in this class is modified from org.jruby.util.Sprintf,
 * licensed under the same EPL 2.0/GPL 2.0/LGPL 2.1 used throughout.
 *
 * Contains code modified from Sprintf.java
 *
 * Copyright (C) 2007 William N Dortch <bill.dortch@gmail.com>
 */
package org.truffleruby.core.format.format;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;

import org.truffleruby.collections.ByteArrayBuilder;

@ImportStatic(Double.class)
public abstract class FormatAFloatNode extends FormatFloatGenericNode {

    private static final ThreadLocal<DecimalFormat> formatters = new ThreadLocal<>() {
        @Override
        protected DecimalFormat initialValue() {
            final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
            return new DecimalFormat("0.0E00", formatSymbols);
        }
    };

    private static final long SIGN_MASK = 1L << 63;
    private static final long BIASED_EXP_MASK = 0x7ffL << 52;
    private static final long MANTISSA_MASK = ~(SIGN_MASK | BIASED_EXP_MASK);
    private static final byte[] HEX_DIGITS = new byte[]{
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'a',
            'b',
            'c',
            'd',
            'e',
            'f' };
    private static final byte[] HEX_DIGITS_UPPER_CASE = new byte[]{
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F' };

    private final char expSeparator;

    public FormatAFloatNode(
            char expSeparator,
            boolean hasSpaceFlag,
            boolean hasZeroFlag,
            boolean hasPlusFlag,
            boolean hasMinusFlag,
            boolean hasFSharpFlag) {
        super(hasSpaceFlag, hasZeroFlag, hasPlusFlag, hasMinusFlag, hasFSharpFlag);
        this.expSeparator = expSeparator;
    }

    @Specialization(guards = { "isFinite(dval)" })
    protected byte[] formatFGeneric(int width, int precision, double dval) {
        final byte[] digits = doFormat(precision, dval);

        final ByteArrayBuilder buf = new ByteArrayBuilder();

        width -= digits.length;

        if (width > 0 && !hasMinusFlag) {
            if (hasZeroFlag) {
                boolean firstDigit = digits[0] >= '0' && digits[0] <= '9';
                if (!firstDigit) {
                    buf.append(digits, 0, 3);
                } else {
                    buf.append(digits, 0, 2);
                }
                buf.append('0', width);
                if (!firstDigit) {
                    buf.append(digits, 3, digits.length - 3);
                } else {
                    buf.append(digits, 2, digits.length - 2);
                }
            } else {
                buf.append(' ', width);
                buf.append(digits, 0, digits.length);
            }
            width = 0;
        } else {
            buf.append(digits, 0, digits.length);
            if (width > 0) {
                buf.append(' ', width);
            }
        }
        return buf.getBytes();
    }

    @TruffleBoundary
    private byte[] doFormat(int precision, double dval) {
        if (precision == 0) {
            throw new UnsupportedOperationException("format flags a/A do not support precision 0");
        }

        final ByteArrayBuilder buf = new ByteArrayBuilder();

        final long bits = Double.doubleToRawLongBits(dval);
        long biasedExp = ((bits & BIASED_EXP_MASK) >> 52);
        final long signBits = bits & SIGN_MASK;
        final long exponent;

        if (signBits != 0L) {
            buf.append('-');
        } else if (hasPlusFlag) {
            buf.append('+');
        } else if (hasSpaceFlag) {
            buf.append(' ');
        }
        buf.append('0');
        buf.append(expSeparator == 'a' ? 'x' : 'X');
        if (dval == 0.0) {
            buf.append('0');
            exponent = 0;
        } else {
            long mantissaBits = bits & MANTISSA_MASK;
            if (biasedExp == 0) {
                // Sub normal cases are a little special.
                // Find the most significant bit in the mantissa
                final int lz = Long.numberOfLeadingZeros(mantissaBits);
                // Shift the mantissa to make it a normal mantissa
                // and mask off the leading bit (now the implied 53 bit of the mantissa)..
                mantissaBits = (mantissaBits << (lz - 11)) & MANTISSA_MASK;
                // Adjust the exponent to reflect this.
                biasedExp = biasedExp + (lz - 11);
            }
            exponent = biasedExp - 1023;
            buf.append('1');
            if (mantissaBits != 0L) {
                buf.append('.');
            }
            while (mantissaBits != 0L || precision > 0) {
                int digit = (int) ((0xf000000000000L & mantissaBits) >> 48);
                mantissaBits = (mantissaBits << 4) & MANTISSA_MASK;
                buf.append((expSeparator == 'a' ? HEX_DIGITS : HEX_DIGITS_UPPER_CASE)[digit]);
                precision--;
            }
        }

        buf.append(expSeparator == 'a' ? 'p' : 'P');
        if (exponent >= 0) {
            buf.append('+');
        }
        buf.append(Long.toString(exponent).getBytes());

        return buf.getBytes();
    }
}
