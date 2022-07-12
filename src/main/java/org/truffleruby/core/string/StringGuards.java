/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
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

    public static boolean isSingleByteOptimizable(RubyString string,
            SingleByteOptimizableNode singleByteOptimizableNode) {
        return singleByteOptimizableNode.execute(string.tstring, string.encoding);
    }

    public static boolean isSingleByteOptimizable(AbstractTruffleString tString, RubyEncoding encoding,
            SingleByteOptimizableNode singleByteOptimizableNode) {
        return singleByteOptimizableNode.execute(tString, encoding);
    }

    public static boolean isAsciiCompatible(RubyString string) {
        return string.encoding.jcoding.isAsciiCompatible();
    }

    public static boolean isAsciiCompatible(RubyEncoding encoding) {
        return encoding.jcoding.isAsciiCompatible();
    }

    public static boolean isFixedWidthEncoding(RubyEncoding encoding) {
        return encoding.jcoding.isFixedWidth();
    }

    public static boolean isEmpty(AbstractTruffleString string) {
        return string.isEmpty();
    }

    /** The case mapping is simple (ASCII-only or full Unicode): no complex option like Turkic, case-folding, etc. */
    public static boolean isAsciiCompatMapping(int caseMappingOptions) {
        return caseMappingOptions == CASE_FULL_UNICODE || caseMappingOptions == Config.CASE_ASCII_ONLY;
    }

    /** The string can be optimized to single-byte representation and is a simple case mapping (ASCII-only or full
     * Unicode). */
    public static boolean isSingleByteCaseMapping(RubyString string, int caseMappingOptions,
            SingleByteOptimizableNode singleByteOptimizableNode) {
        return isSingleByteOptimizable(string, singleByteOptimizableNode) && isAsciiCompatMapping(caseMappingOptions);
    }

    /** The string's encoding is ASCII-compatible, the mapping is ASCII-only and {@link #isSingleByteCaseMapping} is not
     * applicable. */
    public static boolean isSimpleAsciiCaseMapping(RubyString string, int caseMappingOptions,
            SingleByteOptimizableNode singleByteOptimizableNode) {
        return !isSingleByteOptimizable(string, singleByteOptimizableNode) &&
                caseMappingOptions == Config.CASE_ASCII_ONLY && isAsciiCompatible(string);
    }

    /** Both {@link #isSingleByteCaseMapping} and {@link #isSimpleAsciiCaseMapping} are not applicable. */
    public static boolean isComplexCaseMapping(RubyString string, int caseMappingOptions,
            SingleByteOptimizableNode singleByteOptimizableNode) {
        return !isSingleByteCaseMapping(string, caseMappingOptions, singleByteOptimizableNode) &&
                !isSimpleAsciiCaseMapping(string, caseMappingOptions, singleByteOptimizableNode);
    }
}
