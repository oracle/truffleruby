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

import org.truffleruby.core.format.printf.PrintfSimpleTreeBuilder;

@ImportStatic(Double.class)
public abstract class FormatEFloatNode extends FormatFloatGenericNode {

    private static final LinkedBlockingDeque<DecimalFormat> formatters = new LinkedBlockingDeque<>();

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
            format = new DecimalFormat("0.0E00", formatSymbols);
        }

        try {
            if (hasPlusFlag) {
                format.setPositivePrefix("+");
            } else if (hasSpaceFlag) {
                format.setPositivePrefix(" ");
            } else {
                format.setPositivePrefix("");
            }

            DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
            String separator;
            if (precision == 0 && hasFSharpFlag) {
                format.setDecimalSeparatorAlwaysShown(true);
            } else {
                format.setDecimalSeparatorAlwaysShown(false);
            }

            separator = Character.toString(expSeparator);

            if ((Math.abs(dval) >= 1.0 || dval == 0.0)) {
                separator += '+';
            }

            symbols.setExponentSeparator(separator);

            format.setDecimalFormatSymbols(symbols);

            format.setMinimumFractionDigits(precision);
            format.setMaximumFractionDigits(precision);
            digits = format.format(dval).getBytes();

            return digits;
        } finally {
            formatters.offerFirst(format);
        }
    }
}
