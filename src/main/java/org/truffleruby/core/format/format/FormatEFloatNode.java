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
public abstract class FormatEFloatNode extends FormatFloatGenericNode {

    private static final ThreadLocal<DecimalFormat> formatters = new ThreadLocal<>() {
        @Override
        protected DecimalFormat initialValue() {
            final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
            return new DecimalFormat("0.0E00", formatSymbols);
        }
    };

    private final char expSeparator;

    public FormatEFloatNode(
            char expSeparator,
            boolean hasSpaceFlag,
            boolean hasZeroFlag,
            boolean hasPlusFlag,
            boolean hasMinusFlag,
            boolean hasFSharpFlag) {
        super(hasSpaceFlag, hasZeroFlag, hasPlusFlag, hasMinusFlag, hasFSharpFlag);
        this.expSeparator = expSeparator;
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

        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        if (expSeparator == 'e') {
            symbols.setExponentSeparator(Math.abs(dval) >= 1.0 ? "e+" : "e");
        } else {
            symbols.setExponentSeparator(Math.abs(dval) >= 1.0 ? "E+" : "E");
        }
        format.setDecimalFormatSymbols(symbols);

        format.setMinimumFractionDigits(precision);
        format.setMaximumFractionDigits(precision);
        digits = format.format(dval).getBytes();

        return digits;
    }
}
