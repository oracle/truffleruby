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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;

public class RopeConstants {

    public static final Map<String, LeafRope> ROPE_CONSTANTS = new HashMap<>();

    public static final byte[] EMPTY_BYTES = new byte[0];

    public static final LeafRope EMPTY_ASCII_8BIT_ROPE = withHashCode(
            new AsciiOnlyLeafRope(EMPTY_BYTES, ASCIIEncoding.INSTANCE));
    public static final LeafRope EMPTY_US_ASCII_ROPE = withHashCode(
            new AsciiOnlyLeafRope(EMPTY_BYTES, USASCIIEncoding.INSTANCE));
    public static final LeafRope EMPTY_UTF8_ROPE = withHashCode(
            new AsciiOnlyLeafRope(EMPTY_BYTES, UTF8Encoding.INSTANCE));

    @CompilationFinal(dimensions = 1) public static final LeafRope[] UTF8_SINGLE_BYTE_ROPES = new LeafRope[256];
    @CompilationFinal(dimensions = 1) public static final LeafRope[] US_ASCII_SINGLE_BYTE_ROPES = new LeafRope[256];
    @CompilationFinal(dimensions = 1) public static final LeafRope[] ASCII_8BIT_SINGLE_BYTE_ROPES = new LeafRope[256];

    static {
        for (int i = 0; i < 128; i++) {
            final byte[] bytes = new byte[]{ (byte) i };

            UTF8_SINGLE_BYTE_ROPES[i] = withHashCode(new AsciiOnlyLeafRope(bytes, UTF8Encoding.INSTANCE));
            US_ASCII_SINGLE_BYTE_ROPES[i] = withHashCode(new AsciiOnlyLeafRope(bytes, USASCIIEncoding.INSTANCE));
            ASCII_8BIT_SINGLE_BYTE_ROPES[i] = withHashCode(new AsciiOnlyLeafRope(bytes, ASCIIEncoding.INSTANCE));
        }

        for (int i = 128; i < 256; i++) {
            final byte[] bytes = new byte[]{ (byte) i };

            UTF8_SINGLE_BYTE_ROPES[i] = withHashCode(new InvalidLeafRope(bytes, UTF8Encoding.INSTANCE, 1));
            US_ASCII_SINGLE_BYTE_ROPES[i] = withHashCode(new InvalidLeafRope(bytes, USASCIIEncoding.INSTANCE, 1));
            ASCII_8BIT_SINGLE_BYTE_ROPES[i] = withHashCode(new ValidLeafRope(bytes, ASCIIEncoding.INSTANCE, 1));
        }
    }

    public static final Rope AMPERSAND = ascii("&");
    public static final Rope AMPERSAND_AMPERSAND = ascii("&&");
    public static final Rope AMPERSAND_DOT = ascii("&.");
    public static final Rope BACKTICK = ascii("`");
    public static final Rope BACKSLASH = ascii("\\");
    public static final Rope BANG = ascii("!");
    public static final Rope BANG_EQ = ascii("!=");
    public static final Rope BANG_TILDE = ascii("!~");
    public static final Rope CALL = ascii("call");
    public static final Rope CARET = ascii("^");
    public static final Rope COLON = ascii(":");
    public static final Rope COLON_COLON = ascii("::");
    public static final Rope COMMA = ascii(",");
    public static final Rope DOT = ascii(".");
    public static final Rope DOT_DOT = ascii("..");
    public static final Rope DOT_DOT_DOT = ascii("...");
    public static final Rope DOLLAR_BANG = ascii("$!");
    public static final Rope DOLLAR_ZERO = ascii("$0");
    public static final Rope EQ = ascii("=");
    public static final Rope EQ_EQ = ascii("==");
    public static final Rope EQ_EQ_EQ = ascii("===");
    public static final Rope EQ_GT = ascii("=>");
    public static final Rope EQ_TILDE = ascii("=~");
    public static final Rope GT = ascii(">");
    public static final Rope GT_EQ = ascii(">=");
    public static final Rope GT_GT = ascii(">>");
    public static final Rope LBRACKET = ascii("[");
    public static final Rope LBRACKET_RBRACKET = ascii("[]");
    public static final Rope LBRACKET_RBRACKET_EQ = ascii("[]=");
    public static final Rope LCURLY = ascii("{");
    public static final Rope LT = ascii("<");
    public static final Rope LT_EQ = ascii("<=");
    public static final Rope LT_EQ_GT = ascii("<=>");
    public static final Rope LT_LT = ascii("<<");
    public static final Rope MINUS = ascii("-");
    public static final Rope MINUS_AT = ascii("-@");
    public static final Rope MINUS_GT = ascii("->");
    public static final Rope OR = ascii("|");
    public static final Rope OR_OR = ascii("||");
    public static final Rope PERCENT = ascii("%");
    public static final Rope PLUS = ascii("+");
    public static final Rope PLUS_AT = ascii("+@");
    public static final Rope Q = ascii("'");
    public static final Rope QQ = ascii("\"");
    public static final Rope QUESTION = ascii("?");
    public static final Rope RBRACKET = ascii("]");
    public static final Rope RCURLY = ascii("}");
    public static final Rope RPAREN = ascii(")");
    public static final Rope SEMICOLON = ascii(";");
    public static final Rope SLASH = ascii("/");
    public static final Rope STAR = ascii("*");
    public static final Rope STAR_STAR = ascii("**");
    public static final Rope TILDE = ascii("~");


    // Encoding names, generated by:
    // names = Encoding.list.map { |e| e.name }
    // names.each { |n| puts "public static final Rope #{n.upcase.gsub('-','_')} = ascii(\"#{n}\");" }
    public static final Rope ASCII_8BIT = ascii("ASCII-8BIT");
    public static final Rope US_ASCII = ascii("US-ASCII");
    public static final Rope UTF_8 = ascii("UTF-8");
    public static final Rope BIG5 = ascii("Big5");
    public static final Rope BIG5_HKSCS = ascii("Big5-HKSCS");
    public static final Rope BIG5_UAO = ascii("Big5-UAO");
    public static final Rope CP949 = ascii("CP949");
    public static final Rope EMACS_MULE = ascii("Emacs-Mule");
    public static final Rope EUC_JP = ascii("EUC-JP");
    public static final Rope EUC_KR = ascii("EUC-KR");
    public static final Rope EUC_TW = ascii("EUC-TW");
    public static final Rope GB18030 = ascii("GB18030");
    public static final Rope GBK = ascii("GBK");
    public static final Rope ISO_8859_1 = ascii("ISO-8859-1");
    public static final Rope ISO_8859_2 = ascii("ISO-8859-2");
    public static final Rope ISO_8859_3 = ascii("ISO-8859-3");
    public static final Rope ISO_8859_4 = ascii("ISO-8859-4");
    public static final Rope ISO_8859_5 = ascii("ISO-8859-5");
    public static final Rope ISO_8859_6 = ascii("ISO-8859-6");
    public static final Rope ISO_8859_7 = ascii("ISO-8859-7");
    public static final Rope ISO_8859_8 = ascii("ISO-8859-8");
    public static final Rope ISO_8859_9 = ascii("ISO-8859-9");
    public static final Rope ISO_8859_10 = ascii("ISO-8859-10");
    public static final Rope ISO_8859_11 = ascii("ISO-8859-11");
    public static final Rope ISO_8859_13 = ascii("ISO-8859-13");
    public static final Rope ISO_8859_14 = ascii("ISO-8859-14");
    public static final Rope ISO_8859_15 = ascii("ISO-8859-15");
    public static final Rope ISO_8859_16 = ascii("ISO-8859-16");
    public static final Rope KOI8_R = ascii("KOI8-R");
    public static final Rope KOI8_U = ascii("KOI8-U");
    public static final Rope SHIFT_JIS = ascii("Shift_JIS");
    public static final Rope UTF_16BE = ascii("UTF-16BE");
    public static final Rope UTF_16LE = ascii("UTF-16LE");
    public static final Rope UTF_32BE = ascii("UTF-32BE");
    public static final Rope UTF_32LE = ascii("UTF-32LE");
    public static final Rope WINDOWS_31J = ascii("Windows-31J");
    public static final Rope WINDOWS_1250 = ascii("Windows-1250");
    public static final Rope WINDOWS_1251 = ascii("Windows-1251");
    public static final Rope WINDOWS_1252 = ascii("Windows-1252");
    public static final Rope WINDOWS_1253 = ascii("Windows-1253");
    public static final Rope WINDOWS_1254 = ascii("Windows-1254");
    public static final Rope WINDOWS_1257 = ascii("Windows-1257");
    public static final Rope IBM437 = ascii("IBM437");
    public static final Rope IBM737 = ascii("IBM737");
    public static final Rope IBM775 = ascii("IBM775");
    public static final Rope CP850 = ascii("CP850");
    public static final Rope IBM852 = ascii("IBM852");
    public static final Rope CP852 = ascii("CP852");
    public static final Rope IBM855 = ascii("IBM855");
    public static final Rope CP855 = ascii("CP855");
    public static final Rope IBM857 = ascii("IBM857");
    public static final Rope IBM860 = ascii("IBM860");
    public static final Rope IBM861 = ascii("IBM861");
    public static final Rope IBM862 = ascii("IBM862");
    public static final Rope IBM863 = ascii("IBM863");
    public static final Rope IBM864 = ascii("IBM864");
    public static final Rope IBM865 = ascii("IBM865");
    public static final Rope IBM866 = ascii("IBM866");
    public static final Rope IBM869 = ascii("IBM869");
    public static final Rope WINDOWS_1258 = ascii("Windows-1258");
    public static final Rope GB1988 = ascii("GB1988");
    public static final Rope MACCENTEURO = ascii("macCentEuro");
    public static final Rope MACCROATIAN = ascii("macCroatian");
    public static final Rope MACCYRILLIC = ascii("macCyrillic");
    public static final Rope MACGREEK = ascii("macGreek");
    public static final Rope MACICELAND = ascii("macIceland");
    public static final Rope MACROMAN = ascii("macRoman");
    public static final Rope MACROMANIA = ascii("macRomania");
    public static final Rope MACTHAI = ascii("macThai");
    public static final Rope MACTURKISH = ascii("macTurkish");
    public static final Rope MACUKRAINE = ascii("macUkraine");
    public static final Rope CP950 = ascii("CP950");
    public static final Rope CP951 = ascii("CP951");
    public static final Rope IBM037 = ascii("IBM037");
    public static final Rope STATELESS_ISO_2022_JP = ascii("stateless-ISO-2022-JP");
    public static final Rope EUCJP_MS = ascii("eucJP-ms");
    public static final Rope CP51932 = ascii("CP51932");
    public static final Rope EUC_JIS_2004 = ascii("EUC-JIS-2004");
    public static final Rope GB2312 = ascii("GB2312");
    public static final Rope GB12345 = ascii("GB12345");
    public static final Rope ISO_2022_JP = ascii("ISO-2022-JP");
    public static final Rope ISO_2022_JP_2 = ascii("ISO-2022-JP-2");
    public static final Rope CP50220 = ascii("CP50220");
    public static final Rope CP50221 = ascii("CP50221");
    public static final Rope WINDOWS_1256 = ascii("Windows-1256");
    public static final Rope WINDOWS_1255 = ascii("Windows-1255");
    public static final Rope TIS_620 = ascii("TIS-620");
    public static final Rope WINDOWS_874 = ascii("Windows-874");
    public static final Rope MACJAPANESE = ascii("MacJapanese");
    public static final Rope UTF_7 = ascii("UTF-7");
    public static final Rope UTF8_MAC = ascii("UTF8-MAC");
    public static final Rope UTF_16 = ascii("UTF-16");
    public static final Rope UTF_32 = ascii("UTF-32");
    public static final Rope UTF8_DOCOMO = ascii("UTF8-DoCoMo");
    public static final Rope SJIS_DOCOMO = ascii("SJIS-DoCoMo");
    public static final Rope UTF8_KDDI = ascii("UTF8-KDDI");
    public static final Rope SJIS_KDDI = ascii("SJIS-KDDI");
    public static final Rope ISO_2022_JP_KDDI = ascii("ISO-2022-JP-KDDI");
    public static final Rope STATELESS_ISO_2022_JP_KDDI = ascii("stateless-ISO-2022-JP-KDDI");
    public static final Rope UTF8_SOFTBANK = ascii("UTF8-SoftBank");
    public static final Rope SJIS_SOFTBANK = ascii("SJIS-SoftBank");

    private static Rope ascii(String string) {
        if (string.length() == 1) {
            return US_ASCII_SINGLE_BYTE_ROPES[string.charAt(0)];
        } else {
            final byte[] bytes = RopeOperations.encodeAsciiBytes(string);
            final LeafRope rope = withHashCode(new AsciiOnlyLeafRope(bytes, USASCIIEncoding.INSTANCE));
            final Rope existing = ROPE_CONSTANTS.putIfAbsent(string, rope);
            if (existing != null) {
                throw new AssertionError("Duplicate Rope in RopeConstants: " + existing);
            }
            return rope;
        }
    }

    public static LeafRope lookupUSASCII(String string) {
        if (string.length() == 1) {
            return US_ASCII_SINGLE_BYTE_ROPES[string.charAt(0)];
        } else {
            return ROPE_CONSTANTS.get(string);
        }
    }

    @CompilationFinal(dimensions = 1) private static final LeafRope[] PADDED_NUMBERS = createPaddedNumbersTable();

    private static LeafRope[] createPaddedNumbersTable() {
        final LeafRope[] table = new LeafRope[100];

        for (int n = 0; n < table.length; n++) {
            table[n] = new AsciiOnlyLeafRope(
                    new byte[]{ (byte) ('0' + n / 10), (byte) ('0' + n % 10) },
                    UTF8Encoding.INSTANCE);
        }

        return table;
    }

    /*** Zero-padded numbers in the format %02d, between 00 and 99. */
    public static LeafRope paddedNumber(int n) {
        return PADDED_NUMBERS[n];
    }

    @CompilationFinal(dimensions = 1) private static final LeafRope[] PADDING_ZEROS = createPaddingZeroTable();

    private static LeafRope[] createPaddingZeroTable() {
        final LeafRope[] table = new LeafRope[6];

        for (int n = 0; n < table.length; n++) {
            final byte[] bytes = new byte[n];

            Arrays.fill(bytes, (byte) '0');

            table[n] = new AsciiOnlyLeafRope(bytes, UTF8Encoding.INSTANCE);
        }

        return table;
    }

    public static LeafRope paddingZeros(int n) {
        return PADDING_ZEROS[n];
    }

    private static <T> T withHashCode(T object) {
        object.hashCode();
        return object;
    }

}
