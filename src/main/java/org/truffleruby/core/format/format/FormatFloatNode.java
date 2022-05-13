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

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.truffleruby.collections.ByteArrayBuilder;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.printf.PrintfSimpleTreeBuilder;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.core.rope.RopeOperations;

@NodeChild("width")
@NodeChild("precision")
@NodeChild("value")
@ImportStatic(Double.class)
public abstract class FormatFloatNode extends FormatNode {

    private static final byte[] NAN_VALUE = { 'N', 'a', 'N' };
    private static final byte[] INFINITY_VALUE = { 'I', 'n', 'f' };

    private final char format;
    private final boolean hasSpaceFlag;
    private final boolean hasZeroFlag;
    private final boolean hasPlusFlag;
    private final boolean hasMinusFlag;
    private final boolean hasFSharpFlag;

    public FormatFloatNode(
            char format,
            boolean hasSpaceFlag,
            boolean hasZeroFlag,
            boolean hasPlusFlag,
            boolean hasMinusFlag,
            boolean hasFSharpFlag) {
        this.format = format;
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

    @TruffleBoundary
    @Specialization(guards = "isFinite(dval)" )
    protected byte[] format(int width, int precision, double dval) {
        //        if (arg == null || name != null) {
        //            arg = args.next(name);
        //            name = null;
        //        }

        //        if (!(arg instanceof RubyFloat)) {
        //            // FIXME: what is correct 'recv' argument?
        //            // (this does produce the desired behavior)
        //            if (usePrefixForZero) {
        //                arg = RubyKernel.new_float(arg,arg);
        //            } else {
        //                arg = RubyKernel.new_float19(arg,arg);
        //            }
        //        }
        //        double dval = ((RubyFloat)arg).getDoubleValue();
        boolean negative = Double.compare(dval, 0.0) == -1;

        byte[] digits;
        int nDigits = 0;
        int exponent = 0;

        NumberFormat nf = getNumberFormat(Locale.ENGLISH);
        nf.setMaximumFractionDigits(Integer.MAX_VALUE);
        String str = nf.format(dval);

        // grrr, arghh, want to subclass sun.misc.FloatingDecimal, but can't,
        // so we must do all this (the next 70 lines of code), which has already
        // been done by FloatingDecimal.
        int strlen = str.length();
        digits = new byte[strlen];
        int nTrailingZeroes = 0;
        int i = Double.compare(dval, 0.0) == -1 ? 1 : 0;
        int decPos = 0;
        byte ival;
        int_loop: while (i < strlen) {
            switch (ival = (byte) str.charAt(i++)) {
                case '0':
                    if (nDigits > 0) {
                        nTrailingZeroes++;
                    }

                    break; // switch
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    for (; nTrailingZeroes > 0; nTrailingZeroes--) {
                        digits[nDigits++] = '0';
                    }
                    digits[nDigits++] = ival;
                    break; // switch
                case '.':
                    break int_loop;
            }
        }
        decPos = nDigits + nTrailingZeroes;
        dec_loop: while (i < strlen) {
            switch (ival = (byte) str.charAt(i++)) {
                case '0':
                    if (nDigits > 0) {
                        nTrailingZeroes++;
                    } else {
                        exponent--;
                    }
                    break; // switch
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    for (; nTrailingZeroes > 0; nTrailingZeroes--) {
                        digits[nDigits++] = '0';
                    }
                    digits[nDigits++] = ival;
                    break; // switch
                case 'E':
                    break dec_loop;
            }
        }
        if (i < strlen) {
            int expSign;
            int expVal = 0;
            if (str.charAt(i) == '-') {
                expSign = -1;
                i++;
            } else {
                expSign = 1;
            }
            while (i < strlen) {
                expVal = expVal * 10 + (str.charAt(i++) - '0');
            }
            exponent += expVal * expSign;
        }
        exponent += decPos - nDigits;

        // gotta have at least a zero...
        if (nDigits == 0) {
            digits[0] = '0';
            nDigits = 1;
            exponent = 0;
        }

        // OK, we now have the significand in digits[0...nDigits]
        // and the exponent in exponent.  We're ready to format.

        return formatCases(width, precision, dval, digits, nDigits, exponent);
    }

    private byte[] formatCases(int width, int precision, double dval,
            byte[] digits, int nDigits, int exponent) {
        byte signChar;
        int intDigits, intZeroes, intLength;
        int decDigits, decZeroes, decLength;
        byte expChar;
        int len = 0;

        final char fchar = this.format;
        final boolean hasPrecisionFlag = precision != PrintfSimpleTreeBuilder.DEFAULT;
        final ByteArrayBuilder buf = new ByteArrayBuilder();
        final Locale locale = Locale.ENGLISH;
        final boolean negative = Double.compare(dval, 0.0) == -1;

        if (negative) {
            signChar = '-';
            width--;
        } else if (hasPlusFlag) {
            signChar = '+';
            width--;
        } else if (hasSpaceFlag) {
            signChar = ' ';
            width--;
        } else {
            signChar = 0;
        }
        if (!hasPrecisionFlag) {
            precision = 6;
        }

        switch (fchar) {
            case 'E':
            case 'G':
                expChar = 'E';
                break;
            case 'e':
            case 'g':
                expChar = 'e';
                break;
            default:
                expChar = 0;
        }

        final byte decimalSeparator = (byte) getDecimalFormat(locale).getDecimalSeparator();

        switch (fchar) {
            case 'A':
            case 'a':
                String floatingPointLiteral;
                final boolean isUpper = fchar == 'A';
                final String p = isUpper ? "P" : "p";
                final String pMinus = p + "-";
                final String pPlus = p + "+";
                final String precisionFormat = hasPrecisionFlag ? "." + precision : "";
                final String formatString = "%" + precisionFormat + (isUpper ? "A" : "a");
                floatingPointLiteral = String.format(formatString, dval);
                if (!floatingPointLiteral.contains(pMinus)) {
                    floatingPointLiteral = floatingPointLiteral.replace(p, pPlus);
                }
                if (hasPrecisionFlag && precision == 0) {
                    throw new UnsupportedOperationException("format flags a/A do not support precision 0");
                }
                width -= floatingPointLiteral.length();
                if (width > 0 && !hasZeroFlag && !hasMinusFlag) {
                    buf.append(' ', width);
                    width = 0;
                }
                if (signChar != 0 && signChar != '-') {
                    buf.append(signChar);
                }
                final String hexPrefix = isUpper ? "0X" : "0x";
                if (width > 0 && !hasMinusFlag) {
                    StringBuilder padded = new StringBuilder();
                    padded.append(hexPrefix);
                    while (width > 0) {
                        padded.append('0');
                        width--;
                    }
                    floatingPointLiteral = floatingPointLiteral.replace(hexPrefix, padded.toString());
                    width = 0;
                }
                buf.append(RopeOperations.encodeAsciiBytes(floatingPointLiteral));
                if (width > 0 && !hasMinusFlag) {
                    buf.append(' ', width);
                }
        }
        return buf.getBytes();
    }

    private static final ThreadLocal<Map<Locale, DecimalFormatSymbols>> LOCALE_DECIMAL_FORMATS = new ThreadLocal<>();

    public static DecimalFormatSymbols getDecimalFormat(Locale locale) {
        Map<Locale, DecimalFormatSymbols> decimalFormats = LOCALE_DECIMAL_FORMATS.get();
        if (decimalFormats == null) {
            decimalFormats = new HashMap<>(4);
            LOCALE_DECIMAL_FORMATS.set(decimalFormats);
        }
        DecimalFormatSymbols format = decimalFormats.get(locale);
        if (format == null) {
            format = new DecimalFormatSymbols(locale);
            decimalFormats.put(locale, format);
        }
        return format;
    }

    protected boolean isFormatF() {
        return this.format == 'f';
    }

    private static final ThreadLocal<Map<Locale, NumberFormat>> LOCALE_NUMBER_FORMATS = new ThreadLocal<>();

    private static NumberFormat getNumberFormat(Locale locale) {
        Map<Locale, NumberFormat> numberFormats = LOCALE_NUMBER_FORMATS.get();
        if (numberFormats == null) {
            numberFormats = new HashMap<>(4);
            LOCALE_NUMBER_FORMATS.set(numberFormats);
        }
        NumberFormat format = numberFormats.get(locale);
        if (format == null) {
            format = NumberFormat.getNumberInstance(locale);
            numberFormats.put(locale, format);
        }
        return format;
    }

    private static int round(byte[] bytes, int nDigits, int roundPos, boolean roundDown) {
        int next = roundPos + 1;
        if (next >= nDigits || bytes[next] < '5' ||
                // MRI rounds up on nnn5nnn, but not nnn5 --
                // except for when they do
                (roundDown && bytes[next] == '5' && next == nDigits - 1)) {
            return nDigits;
        }
        if (roundPos < 0) { // "%.0f" % 0.99
            System.arraycopy(bytes, 0, bytes, 1, nDigits);
            bytes[0] = '1';
            return nDigits + 1;
        }
        bytes[roundPos] += 1;
        while (bytes[roundPos] > '9') {
            bytes[roundPos] = '0';
            roundPos--;
            if (roundPos >= 0) {
                bytes[roundPos] += 1;
            } else {
                System.arraycopy(bytes, 0, bytes, 1, nDigits);
                bytes[0] = '1';
                return nDigits + 1;
            }
        }
        return nDigits;
    }

    private static void writeExp(ByteArrayBuilder buf, int exponent, byte expChar) {
        // Unfortunately, the number of digits in the exponent is
        // not clearly defined in Ruby documentation. This is a
        // platform/version-dependent behavior. On Linux/Mac/Cygwin/*nix,
        // two digits are used. On Windows, 3 digits are used.
        // It is desirable for JRuby to have consistent behavior, and
        // the two digits behavior was selected. This is also in sync
        // with "Java-native" sprintf behavior (java.util.Formatter).
        buf.append(expChar); // E or e
        buf.append(exponent >= 0 ? '+' : '-');
        if (exponent < 0) {
            exponent = -exponent;
        }
        if (exponent > 99) {
            buf.append(exponent / 100 + '0');
            buf.append(exponent % 100 / 10 + '0');
        } else {
            buf.append(exponent / 10 + '0');
        }
        buf.append(exponent % 10 + '0');
    }

}
