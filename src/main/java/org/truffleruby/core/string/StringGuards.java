/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.string;

import com.oracle.truffle.api.object.DynamicObject;
import org.jcodings.Config;
import org.truffleruby.Layouts;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.language.RubyGuards;

public class StringGuards {

    public static boolean isSingleByteOptimizable(DynamicObject string) {
        assert RubyGuards.isRubyString(string);
        return Layouts.STRING.getRope(string).isSingleByteOptimizable();
    }

    public static boolean is7Bit(DynamicObject string) {
        assert RubyGuards.isRubyString(string);
        return StringOperations.codeRange(string) == CodeRange.CR_7BIT;
    }

    public static boolean isAsciiCompatible(DynamicObject string) {
        assert RubyGuards.isRubyString(string);
        return Layouts.STRING.getRope(string).getEncoding().isAsciiCompatible();
    }

    public static boolean isFixedWidthEncoding(DynamicObject string) {
        assert RubyGuards.isRubyString(string);
        return Layouts.STRING.getRope(string).getEncoding().isFixedWidth();
    }

    public static boolean isValidUtf8(DynamicObject string) {
        assert RubyGuards.isRubyString(string);
        return isValidCodeRange(string) && Layouts.STRING.getRope(string).getEncoding().isUTF8();
    }

    public static boolean isEmpty(DynamicObject string) {
        assert RubyGuards.isRubyString(string);
        return Layouts.STRING.getRope(string).isEmpty();
    }

    public static boolean isBrokenCodeRange(DynamicObject string) {
        assert RubyGuards.isRubyString(string);
        return StringOperations.codeRange(string) == CodeRange.CR_BROKEN;
    }

    public static boolean isValidCodeRange(DynamicObject string) {
        return StringOperations.codeRange(string) == CodeRange.CR_VALID;
    }

    public static boolean isSingleByteString(DynamicObject string) {
        return Layouts.STRING.getRope(string).byteLength() == 1;
    }

    public static boolean canMemcmp(DynamicObject first, DynamicObject second) {
        final Rope sourceRope = Layouts.STRING.getRope(first);
        final Rope patternRope = Layouts.STRING.getRope(second);

        return (sourceRope.isSingleByteOptimizable() || sourceRope.getEncoding().isUTF8()) &&
                (patternRope.isSingleByteOptimizable() || patternRope.getEncoding().isUTF8());
    }

    public static boolean isAsciiCompatMapping(int caseMappingOptions) {
        return caseMappingOptions == 0 || caseMappingOptions == Config.CASE_ASCII_ONLY;
    }

    public static boolean isFullCaseMapping(DynamicObject string, int caseMappingOptions) {
        return (StringGuards.isSingleByteOptimizable(string) && !isAsciiCompatMapping(caseMappingOptions)) ||
                (!StringGuards.isSingleByteOptimizable(string) && caseMappingOptions != Config.CASE_ASCII_ONLY);
    }

}
