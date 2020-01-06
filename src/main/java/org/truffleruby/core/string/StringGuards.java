/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.string;

import org.jcodings.Config;
import org.truffleruby.Layouts;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;

import com.oracle.truffle.api.object.DynamicObject;

public class StringGuards {

    private static final int CASE_FULL_UNICODE = 0;

    public static boolean isSingleByteOptimizable(DynamicObject string,
            RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
        final Rope rope = StringOperations.rope(string);

        return singleByteOptimizableNode.execute(rope);
    }

    public static boolean is7Bit(DynamicObject string, RopeNodes.CodeRangeNode codeRangeNode) {
        final Rope rope = StringOperations.rope(string);

        return codeRangeNode.execute(rope) == CodeRange.CR_7BIT;
    }

    public static boolean isAsciiCompatible(DynamicObject string) {
        return Layouts.STRING.getRope(string).getEncoding().isAsciiCompatible();
    }

    public static boolean isFixedWidthEncoding(DynamicObject string) {
        return Layouts.STRING.getRope(string).getEncoding().isFixedWidth();
    }

    public static boolean isValidUtf8(DynamicObject string, RopeNodes.CodeRangeNode codeRangeNode) {
        final Rope rope = StringOperations.rope(string);

        return rope.getEncoding().isUTF8() && codeRangeNode.execute(rope) == CodeRange.CR_VALID;
    }

    public static boolean isEmpty(DynamicObject string) {
        return Layouts.STRING.getRope(string).isEmpty();
    }

    public static boolean isBrokenCodeRange(DynamicObject string, RopeNodes.CodeRangeNode codeRangeNode) {
        final Rope rope = StringOperations.rope(string);

        return codeRangeNode.execute(rope) == CodeRange.CR_BROKEN;
    }

    public static boolean isSingleByteString(DynamicObject string) {
        return Layouts.STRING.getRope(string).byteLength() == 1;
    }

    public static boolean canMemcmp(DynamicObject first, DynamicObject second,
            RopeNodes.SingleByteOptimizableNode singleByteNode) {
        final Rope sourceRope = Layouts.STRING.getRope(first);
        final Rope patternRope = Layouts.STRING.getRope(second);

        return (singleByteNode.execute(sourceRope) || sourceRope.getEncoding().isUTF8()) &&
                (singleByteNode.execute(patternRope) || patternRope.getEncoding().isUTF8());
    }

    /**
     * The case mapping is simple (ASCII-only or full Unicode): no complex option like Turkic, case-folding, etc.
     */
    public static boolean isAsciiCompatMapping(int caseMappingOptions) {
        return caseMappingOptions == CASE_FULL_UNICODE || caseMappingOptions == Config.CASE_ASCII_ONLY;
    }

    /**
     * The string can be optimized to single-byte representation and is a simple case mapping
     * (ASCII-only or full Unicode).
     */
    public static boolean isSingleByteCaseMapping(DynamicObject string, int caseMappingOptions,
            RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
        return isSingleByteOptimizable(string, singleByteOptimizableNode) && isAsciiCompatMapping(caseMappingOptions);
    }

    /**
     * The string's encoding is ASCII-compatible, the mapping is ASCII-only and {@link
     * #isSingleByteCaseMapping} is not applicable.
     */
    public static boolean isSimpleAsciiCaseMapping(DynamicObject string, int caseMappingOptions,
            RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
        return !isSingleByteOptimizable(string, singleByteOptimizableNode) &&
                caseMappingOptions == Config.CASE_ASCII_ONLY && isAsciiCompatible(string);
    }

    /**
     * Both {@link #isSingleByteCaseMapping} and {@link #isSimpleAsciiCaseMapping} are not applicable.
     */
    public static boolean isComplexCaseMapping(DynamicObject string, int caseMappingOptions,
            RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
        return !isSingleByteCaseMapping(string, caseMappingOptions, singleByteOptimizableNode) &&
                !isSimpleAsciiCaseMapping(string, caseMappingOptions, singleByteOptimizableNode);
    }

    public static boolean isFullCaseMapping(DynamicObject string, int caseMappingOptions,
            RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode) {
        return (StringGuards.isSingleByteOptimizable(string, singleByteOptimizableNode) &&
                !isAsciiCompatMapping(caseMappingOptions)) ||
                (!StringGuards.isSingleByteOptimizable(string, singleByteOptimizableNode) &&
                        caseMappingOptions != Config.CASE_ASCII_ONLY);
    }
}
