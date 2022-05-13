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
import org.truffleruby.core.format.printf.PrintfSimpleTreeBuilder;

@ImportStatic(Double.class)
public abstract class FormatGFloatNode extends FormatFloatGenericNode {

    private static final ThreadLocal<DecimalFormat> simpleFormatters = new ThreadLocal<>() {
        @Override
        protected DecimalFormat initialValue() {
            final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
            return new DecimalFormat("0.0", formatSymbols);
        }
    };

    private static final ThreadLocal<DecimalFormat> exponentialFormatters = new ThreadLocal<>() {
        @Override
        protected DecimalFormat initialValue() {
            final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
            return new DecimalFormat("0.0E00", formatSymbols);
        }
    };

    private final char expSeparator;

    public FormatGFloatNode(
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
    protected byte[] formatGExponential(int width, int precision, double dval) {
        if (precision == PrintfSimpleTreeBuilder.DEFAULT) {
            precision = 6;
        }

        final boolean simple = inSimpleRange(precision, dval);

        final byte[] digits = doFormat(precision, simple, dval);

        final ByteArrayBuilder buf = new ByteArrayBuilder();

        int expPos = 0;

        if (simple) {
            width -= digits.length;
        } else {
            if (precision == 0 && hasFSharpFlag) {
                for (int i = 0; i < digits.length; i++) {
                    if ('e' == digits[i] || 'E' == digits[i]) {
                        expPos = i;
                    }
                }
            }
            width -= digits.length;
            width -= 1;
        }

        if (width > 0 && !hasMinusFlag) {
            if (hasZeroFlag) {
                boolean firstDigit = digits[0] >= '0' && digits[0] <= '9';
                if (!firstDigit) {
                    buf.append(digits, 0, 1);
                }
                buf.append('0', width);
                appendNumber(digits, buf, firstDigit ? 0 : 1, expPos);
            } else {
                buf.append(' ', width);
                appendNumber(digits, buf, 0, expPos);
            }
            width = 0;
        } else {
            appendNumber(digits, buf, 0, expPos);
            if (width > 0) {
                buf.append(' ', width);
            }
        }
        return buf.getBytes();
    }

    private static void appendNumber(byte[] digits, ByteArrayBuilder buf, int start, int expPos) {
        if (expPos == 0) {
            buf.append(digits, start, digits.length - start);
        } else {
            buf.append(digits, start, expPos - start);
            buf.append('.');
            buf.append(digits, expPos, digits.length - expPos);
        }
    }

    protected static boolean inSimpleRange(int precision, double value) {
        return value == 0.0 || (Math.abs(value) >= 0.0001 && Math.abs(value) <= Math.pow(10, precision));
    }

    @TruffleBoundary
    private byte[] doFormat(int precision, boolean simple, double dval) {
        final byte[] digits;
        final DecimalFormat format = simple ? simpleFormatters.get() : exponentialFormatters.get();
        if (hasPlusFlag) {
            format.setPositivePrefix("+");
        } else if (hasSpaceFlag) {
            format.setPositivePrefix(" ");
        } else {
            format.setPositivePrefix("");
        }

        if (simple) {
            if (precision == 0 && hasFSharpFlag) {
                format.setPositiveSuffix(".");
                format.setNegativeSuffix(".");
            } else {
                format.setPositiveSuffix("");
                format.setNegativeSuffix("");
            }
        }

        if (!simple) {
            DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
            if (expSeparator == 'g') {
                symbols.setExponentSeparator(Math.abs(dval) >= 1.0 ? "e+" : "e");
            } else {
                symbols.setExponentSeparator(Math.abs(dval) >= 1.0 ? "E+" : "E");
            }
            format.setDecimalFormatSymbols(symbols);
        }

        format.setMinimumIntegerDigits(1);
        format.setMinimumFractionDigits(0);
        if (!simple) {
            format.setMaximumFractionDigits(precision - 1);
        } else {
            long lval = (long) Math.abs(dval);
            long pow = 1;
            int intDigits = 0;
            while (lval >= pow) {
                pow = 10 * pow;
                intDigits++;
            }
            format.setMaximumFractionDigits(precision - intDigits);
        }
        digits = format.format(dval).getBytes();

        return digits;
    }
}
