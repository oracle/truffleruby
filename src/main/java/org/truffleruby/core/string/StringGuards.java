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

import org.jcodings.Config;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;

public class StringGuards {

    private static final int CASE_FULL_UNICODE = 0;

    public static boolean isSingleByteOptimizable(Rope rope,
            RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
        return singleByteOptimizableNode.execute(rope);
    }

    public static boolean isSingleByteOptimizable(RubyString string,
            RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {

        final Rope rope = string.rope;
        return singleByteOptimizableNode.execute(rope);
    }

    public static boolean is7Bit(Rope rope, RopeNodes.CodeRangeNode codeRangeNode) {
        return codeRangeNode.execute(rope) == CodeRange.CR_7BIT;
    }

    public static boolean isAsciiCompatible(Rope rope) {
        return rope.getEncoding().isAsciiCompatible();
    }

    public static boolean isAsciiCompatible(RubyString string) {
        return string.rope.getEncoding().isAsciiCompatible();
    }

    public static boolean isFixedWidthEncoding(Rope rope) {
        return rope.getEncoding().isFixedWidth();
    }

    public static boolean isValidUtf8(Rope rope, RopeNodes.CodeRangeNode codeRangeNode) {
        return rope.getEncoding().isUTF8() && codeRangeNode.execute(rope) == CodeRange.CR_VALID;
    }

    public static boolean isEmpty(Rope rope) {
        return rope.isEmpty();
    }

    public static boolean isBrokenCodeRange(Rope rope, RopeNodes.CodeRangeNode codeRangeNode) {
        return codeRangeNode.execute(rope) == CodeRange.CR_BROKEN;
    }

    public static boolean isSingleByteString(Rope rope) {
        return rope.byteLength() == 1;
    }

    public static boolean canMemcmp(Rope sourceRope, Rope patternRope,
            RopeNodes.SingleByteOptimizableNode singleByteNode) {

        return (singleByteNode.execute(sourceRope) || sourceRope.getEncoding().isUTF8()) &&
                (singleByteNode.execute(patternRope) || patternRope.getEncoding().isUTF8());
    }

    /** The case mapping is simple (ASCII-only or full Unicode): no complex option like Turkic, case-folding, etc. */
    public static boolean isAsciiCompatMapping(int caseMappingOptions) {
        return caseMappingOptions == CASE_FULL_UNICODE || caseMappingOptions == Config.CASE_ASCII_ONLY;
    }

    /** The string can be optimized to single-byte representation and is a simple case mapping (ASCII-only or full
     * Unicode). */
    public static boolean isSingleByteCaseMapping(RubyString string, int caseMappingOptions,
            RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
        return isSingleByteOptimizable(string, singleByteOptimizableNode) && isAsciiCompatMapping(caseMappingOptions);
    }

    /** The string's encoding is ASCII-compatible, the mapping is ASCII-only and {@link #isSingleByteCaseMapping} is not
     * applicable. */
    public static boolean isSimpleAsciiCaseMapping(RubyString string, int caseMappingOptions,
            RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
        return !isSingleByteOptimizable(string, singleByteOptimizableNode) &&
                caseMappingOptions == Config.CASE_ASCII_ONLY && isAsciiCompatible(string);
    }

    /** Both {@link #isSingleByteCaseMapping} and {@link #isSimpleAsciiCaseMapping} are not applicable. */
    public static boolean isComplexCaseMapping(RubyString string, int caseMappingOptions,
            RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
        return !isSingleByteCaseMapping(string, caseMappingOptions, singleByteOptimizableNode) &&
                !isSimpleAsciiCaseMapping(string, caseMappingOptions, singleByteOptimizableNode);
    }
}
