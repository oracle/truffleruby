/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */

package org.truffleruby.extra.ffi;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.truffleruby.platform.Pointer;

import java.nio.charset.Charset;

public class PointerOperations {

    @TruffleBoundary
    public static void readPointer(Pointer ptr, byte[] out, int length) {
        ptr.get(0, out, 0, length);
    }

    @TruffleBoundary
    public static Pointer getPointer(Pointer ptr, long offset) {
        final jnr.ffi.Pointer p = ptr.getPointer(offset);
        if (p == null) {
            return null;
        } else {
            return new Pointer(p);
        }
    }

    @TruffleBoundary
    public static byte getByte(Pointer ptr, long offset) {
        return ptr.getByte(offset);
    }

    @TruffleBoundary
    public static short getShort(Pointer ptr, long offset) {
        return ptr.getShort(offset);
    }

    @TruffleBoundary
    public static int getInt(Pointer ptr, long offset) {
        return ptr.getInt(offset);
    }

    @TruffleBoundary
    public static long getLong(Pointer ptr, long offset) {
        return ptr.getLong(offset);
    }

    @TruffleBoundary
    public static long getLongLong(Pointer ptr, long offset) {
        return ptr.getLongLong(offset);
    }

    @TruffleBoundary
    public static String getString(Pointer ptr, long offset) {
        return ptr.getString(offset);
    }

    @TruffleBoundary
    public static void putByte(Pointer ptr, long offset, byte value) {
        ptr.putByte(offset, value);
    }

    @TruffleBoundary
    public static void putShort(Pointer ptr, long offset, short value) {
        ptr.putShort(offset, value);
    }

    @TruffleBoundary
    public static void putInt(Pointer ptr, long offset, int value) {
        ptr.putInt(offset, value);
    }

    @TruffleBoundary
    public static void putLong(Pointer ptr, long offset, long value) {
        ptr.putLong(offset, value);
    }

    @TruffleBoundary
    public static void putLongLong(Pointer ptr, long offset, long value) {
        ptr.putLongLong(offset, value);
    }

    @TruffleBoundary
    public static void putPointer(Pointer ptr, long offset, Pointer value) {
        ptr.putPointer(offset, value);
    }

    @TruffleBoundary
    public static void putString(Pointer ptr, long offset, String value, Charset cs) {
        ptr.putString(offset, value, value.length(), cs);
    }

    @TruffleBoundary
    public static void put(Pointer ptr, long offset, byte[] src, int index, int length) {
        ptr.put(offset, src, index, length);
    }

}
