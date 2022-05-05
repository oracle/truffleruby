/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

import org.truffleruby.collections.ByteArrayBuilder;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.printf.PrintfSimpleTreeBuilder;

@NodeChild("width")
@NodeChild("precision")
@NodeChild("value")
@ImportStatic(Double.class)
public abstract class FormatFFloatNode extends FormatNode {

    private static final byte[] NAN_VALUE = { 'N', 'a', 'N' };
    private static final byte[] INFINITY_VALUE = { 'I', 'n', 'f' };

    private static final ThreadLocal<DecimalFormat> formatters = new ThreadLocal<>() {
        @Override
        protected DecimalFormat initialValue() {
            final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
            return new DecimalFormat("", formatSymbols);
        }
    };

    private final boolean hasSpaceFlag;
    private final boolean hasZeroFlag;
    private final boolean hasPlusFlag;
    private final boolean hasMinusFlag;
    private final boolean hasFSharpFlag;

    public FormatFFloatNode(
            boolean hasSpaceFlag,
            boolean hasZeroFlag,
            boolean hasPlusFlag,
            boolean hasMinusFlag,
            boolean hasFSharpFlag) {
        this.hasSpaceFlag = hasSpaceFlag;
        this.hasZeroFlag = hasZeroFlag;
        this.hasPlusFlag = hasPlusFlag;
        this.hasMinusFlag = hasMinusFlag;
        this.hasFSharpFlag = hasFSharpFlag;
    }

    @Specialization(guards = "value == POSITIVE_INFINITY")
    protected byte[] formatPositiveInfinity(int width, int precision, double value) {
        final byte[] digits;
        final int len;
        final byte signChar;
        final ByteArrayBuilder buf = new ByteArrayBuilder();

        digits = INFINITY_VALUE;
        len = INFINITY_VALUE.length;
        if (hasPlusFlag) {
            signChar = '+';
            width--;
        } else if (hasSpaceFlag) {
            signChar = ' ';
            width--;
        } else {
            signChar = 0;
        }
        width -= len;

        if (width > 0 && !hasMinusFlag) {
            buf.append(' ', width);
            width = 0;
        }
        if (signChar != 0) {
            buf.append(signChar);
        }

        if (width > 0 && !hasMinusFlag) {
            buf.append('0', width);
            width = 0;
        }
        buf.append(digits);
        if (width > 0) {
            buf.append(' ', width);
        }

        return buf.getBytes();
    }

    @Specialization(guards = "value == NEGATIVE_INFINITY")
    protected byte[] formatNegativeInfinity(int width, int precision, double value) {
        final byte[] digits;
        final int len;
        final byte signChar;
        final ByteArrayBuilder buf = new ByteArrayBuilder();

        digits = INFINITY_VALUE;
        len = INFINITY_VALUE.length;
        signChar = '-';
        width--;

        width -= len;

        if (width > 0 && !hasMinusFlag) {
            buf.append(' ', width);
            width = 0;
        }
        buf.append(signChar);

        if (width > 0 && !hasMinusFlag) {
            buf.append('0', width);
            width = 0;
        }
        buf.append(digits);
        if (width > 0) {
            buf.append(' ', width);
        }

        return buf.getBytes();
    }

    @Specialization(guards = "isNaN(value)")
    protected byte[] formatNaN(int width, int precision, double value) {
        final byte[] digits;
        final int len;
        final byte signChar;
        final ByteArrayBuilder buf = new ByteArrayBuilder();

        digits = NAN_VALUE;
        len = NAN_VALUE.length;
        if (hasPlusFlag) {
            signChar = '+';
            width--;
        } else if (hasSpaceFlag) {
            signChar = ' ';
            width--;
        } else {
            signChar = 0;
        }
        width -= len;

        if (width > 0 && !hasMinusFlag) {
            buf.append(' ', width);
            width = 0;
        }
        if (signChar != 0) {
            buf.append(signChar);
        }

        if (width > 0 && !hasMinusFlag) {
            buf.append('0', width);
            width = 0;
        }
        buf.append(digits);
        if (width > 0) {
            buf.append(' ', width);
        }

        return buf.getBytes();
    }

    @Specialization(guards = { "isFinite(dval)"})
    protected byte[] formatFGeneric(int width, int precision, double dval) {
        if (precision == PrintfSimpleTreeBuilder.DEFAULT) {
            precision = 6;
        }

        final byte[] digits = doFormat(precision, dval);

        final ByteArrayBuilder buf = new ByteArrayBuilder();

        width -= digits.length;

        if (width > 0 && !hasMinusFlag) {
            if (hasZeroFlag) {
                boolean firstDigit = digits[0] >= '0' && digits[0] <= '9';
                if (firstDigit) {
                    buf.append(digits, 0, 1);
                }
                buf.append('0', width);
                if (firstDigit) {
                    buf.append(digits, 1, digits.length - 1);
                } else {
                    buf.append(digits, 0, digits.length);
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
        final byte[] digits;
        final DecimalFormat format = formatters.get();
        if (hasPlusFlag) {
            format.setPositivePrefix("+");
        } else if (hasSpaceFlag) {
            format.setPositivePrefix(" ");
        } else {
            format.setPositivePrefix("");
        }

        if (precision == 0 && hasFSharpFlag) {
            format.setPositiveSuffix(".");
            format.setNegativeSuffix(".");
        } else {
            format.setPositiveSuffix("");
            format.setNegativeSuffix("");
        }

        format.setMinimumFractionDigits(precision);
        format.setMaximumFractionDigits(precision);
        digits = format.format(dval).getBytes();

        return digits;
    }
}
