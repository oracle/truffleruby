/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import java.util.Locale;

import org.truffleruby.core.rope.RopeOperations;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public abstract class StringUtils {

    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    @TruffleBoundary
    public static String toString(Object value) {
        return String.valueOf(value);
    }

    @TruffleBoundary
    public static String format(Locale locale, String format, Object... args) {
        return String.format(locale, format, args);
    }

    @TruffleBoundary
    public static String format(String format, Object... args) {
        return String.format(format, args);
    }

    public static byte[] formatASCIIBytes(String format, Object... args) {
        return RopeOperations.encodeAsciiBytes(format(format, args));
    }

    @TruffleBoundary
    public static String replace(String string, char oldChar, char newChar) {
        return string.replace(oldChar, newChar);
    }

    @TruffleBoundary
    public static String replace(String string, CharSequence target, CharSequence replacement) {
        return string.replace(target, replacement);
    }

    @TruffleBoundary
    public static String toLowerCase(String string) {
        return string.toLowerCase(Locale.ENGLISH);
    }

    @TruffleBoundary
    public static String toUpperCase(String string) {
        return string.toUpperCase(Locale.ENGLISH);
    }

}
