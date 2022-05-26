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
import java.util.concurrent.LinkedBlockingDeque;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;

import org.truffleruby.collections.ByteArrayBuilder;
import org.truffleruby.core.format.printf.PrintfSimpleTreeBuilder;

@ImportStatic(Double.class)
public abstract class FormatFFloatNode extends FormatFloatGenericNode {

    private static final LinkedBlockingDeque<DecimalFormat> formatters = new LinkedBlockingDeque<>();

    public FormatFFloatNode(
            boolean hasSpaceFlag,
            boolean hasZeroFlag,
            boolean hasPlusFlag,
            boolean hasMinusFlag,
            boolean hasFSharpFlag) {
        super(hasSpaceFlag, hasZeroFlag, hasPlusFlag, hasMinusFlag, hasFSharpFlag);
    }

    @Specialization(guards = { "isFinite(dval)" })
    protected byte[] formatFGeneric(int width, int precision, double dval) {
        if (precision == PrintfSimpleTreeBuilder.DEFAULT) {
            precision = 6;
        }

        return formatNumber(width, precision, dval);
    }

    @TruffleBoundary
    @Override
    protected byte[] doFormat(int precision, double dval) {
        final byte[] digits;
        DecimalFormat format = formatters.pollFirst();
        if (format == null) {
            final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
            format = new DecimalFormat("", formatSymbols);
        }

        try {
            format.setGroupingSize(0);
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

            format.setMinimumIntegerDigits(1);
            format.setMinimumFractionDigits(precision);
            format.setMaximumFractionDigits(precision);
            digits = format.format(dval).getBytes();

            if (precision <= 340) {
                return digits;
            } else {
                // Decimal format has a limit of 340 decimal places, and apparently people require more.

                final ByteArrayBuilder buf = new ByteArrayBuilder();
                buf.append(digits);
                buf.append('0', precision - 340);
                return buf.getBytes();
            }
        } finally {
            formatters.offerFirst(format);
        }
    }
}
