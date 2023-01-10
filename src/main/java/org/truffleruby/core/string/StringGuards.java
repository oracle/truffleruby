/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.string;

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.TruffleString;
import org.jcodings.Config;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.string.StringHelperNodes.SingleByteOptimizableNode;

import static com.oracle.truffle.api.strings.TruffleString.CodeRange.ASCII;
import static com.oracle.truffle.api.strings.TruffleString.CodeRange.BROKEN;
import static com.oracle.truffle.api.strings.TruffleString.CodeRange.VALID;

public class StringGuards {

    private static final int CASE_FULL_UNICODE = 0;

    // Also known as isAsciiOnly()
    public static boolean is7Bit(AbstractTruffleString tstring, RubyEncoding encoding,
            TruffleString.GetByteCodeRangeNode codeRangeNode) {
        return codeRangeNode.execute(tstring, encoding.tencoding) == ASCII;
    }

    public static boolean is7BitUncached(AbstractTruffleString tstring, RubyEncoding encoding) {
        CompilerAsserts.neverPartOfCompilation("Only behind @TruffleBoundary");
        return tstring.getByteCodeRangeUncached(encoding.tencoding) == ASCII;
    }

    public static boolean isValid(AbstractTruffleString tstring, RubyEncoding encoding,
            TruffleString.GetByteCodeRangeNode codeRangeNode) {
        return codeRangeNode.execute(tstring, encoding.tencoding) == VALID;
    }

    public static boolean isBrokenCodeRange(AbstractTruffleString string, RubyEncoding encoding,
            TruffleString.GetByteCodeRangeNode codeRangeNode) {
        return codeRangeNode.execute(string, encoding.tencoding) == BROKEN;
    }

    public static boolean isBrokenCodeRange(AbstractTruffleString string, TruffleString.Encoding encoding,
            TruffleString.GetByteCodeRangeNode codeRangeNode) {
        return codeRangeNode.execute(string, encoding) == BROKEN;
    }

    public static boolean isSingleByteOptimizable(AbstractTruffleString tString, RubyEncoding encoding,
            SingleByteOptimizableNode singleByteOptimizableNode) {
        return singleByteOptimizableNode.execute(tString, encoding);
    }

    public static boolean isAsciiCompatible(RubyEncoding encoding) {
        return encoding.isAsciiCompatible;
    }

    public static boolean isFixedWidthEncoding(RubyEncoding encoding) {
        return encoding.isFixedWidth;
    }

    public static boolean isEmpty(AbstractTruffleString string) {
        return string.isEmpty();
    }

    /** The case mapping is simple (ASCII-only or full Unicode): no complex option like Turkic, case-folding, etc. */
    private static boolean isAsciiCompatMapping(int caseMappingOptions) {
        return caseMappingOptions == CASE_FULL_UNICODE || caseMappingOptions == Config.CASE_ASCII_ONLY;
    }

    /** The mapping is ASCII-only or effectively ASCII-only based on the string properties. */
    private static boolean isAsciiCodePointsMapping(AbstractTruffleString tstring, RubyEncoding encoding,
            int caseMappingOptions, SingleByteOptimizableNode singleByteOptimizableNode) {
        return isSingleByteOptimizable(tstring, encoding, singleByteOptimizableNode)
                ? isAsciiCompatMapping(caseMappingOptions)
                : caseMappingOptions == Config.CASE_ASCII_ONLY && isAsciiCompatible(encoding);
    }

    public static boolean isComplexCaseMapping(AbstractTruffleString tstring, RubyEncoding encoding,
            int caseMappingOptions, SingleByteOptimizableNode singleByteOptimizableNode) {
        return !isAsciiCodePointsMapping(tstring, encoding, caseMappingOptions, singleByteOptimizableNode);
    }
}
