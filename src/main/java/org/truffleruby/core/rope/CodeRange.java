/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 *
 * Some of the code in this class is modified from org.jruby.util.StringSupport,
 * licensed under the same EPL 2.0/GPL 2.0/LGPL 2.1 used throughout.
 */

package org.truffleruby.core.rope;

import com.oracle.truffle.api.CompilerDirectives;

public enum CodeRange {
    /** Used for NativeRope, where the bytes can change from real native code. Also used when building a new
     * {@link Rope} and the code range is unknown. */
    CR_UNKNOWN(0),
    /** Only used for ASCII-compatible encodings, when all characters are US-ASCII (7-bit). */
    CR_7BIT(1),
    /** All characters are valid, but at least one non-7-bit character. */
    CR_VALID(2),
    /** At least one character is not valid in the encoding of that Rope. */
    CR_BROKEN(3);

    private final int value;

    CodeRange(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }

    public static CodeRange fromInt(int codeRange) {
        switch (codeRange) {
            case 0:
                return CR_UNKNOWN;
            case 1:
                return CR_7BIT;
            case 2:
                return CR_VALID;
            case 3:
                return CR_BROKEN;
            default:
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw new UnsupportedOperationException("Don't know how to convert code range: " + codeRange);
        }
    }
}
