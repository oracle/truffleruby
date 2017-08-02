/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.extra.ffi;

import com.oracle.truffle.api.CompilerDirectives;
import jnr.ffi.Runtime;
import org.truffleruby.Log;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Pointer {

    public static final jnr.ffi.Pointer JNR_NULL = Runtime.getSystemRuntime().getMemoryManager().newOpaquePointer(0);

    public static final Pointer NULL = new Pointer(0);

    public static Pointer malloc(long size) {
        return new Pointer(UNSAFE.allocateMemory(size));
    }

    private final long address;

    public Pointer(long address) {
        this.address = address;
    }

    public Pointer add(long offset) {
        return new Pointer(address + offset);
    }

    @CompilerDirectives.TruffleBoundary
    public void put(long i, byte[] bytes, int i1, int length) {
        for (int n = 0; n < length; n++) {
            putByte(i + n, bytes[i1 + n]);
        }
    }

    public void putByte(long offset, byte b) {
        UNSAFE.putByte(address + offset, b);
    }

    public byte getByte(long offset) {
        return UNSAFE.getByte(address + offset);
    }

    @CompilerDirectives.TruffleBoundary
    public void get(long from, byte[] buffer, int bufferPos, int i) {
        for (int n = 0; n < i; n++) {
            buffer[bufferPos + n] = getByte(from + n);
        }
    }

    public void putLong(long value) {
        UNSAFE.putLong(address, value);
    }

    public void putLong(long offset, long value) {
        UNSAFE.putLong(address + offset, value);
    }

    public short getShort(long offset) {
        return UNSAFE.getShort(address + offset);
    }

    public int getInt(long offset) {
        return UNSAFE.getInt(address + offset);
    }

    public long getLong(long offset) {
        return UNSAFE.getLong(address + offset);
    }

    public long getLongLong(long offset) {
        return getLong(offset);
    }

    private byte[] getZeroTerminatedByteArray(long offset) {
        final int length = indexOf(offset, (byte) 0);
        final byte[] bytes = new byte[length];
        get(offset, bytes, 0, length);
        return bytes;
    }

    @CompilerDirectives.TruffleBoundary
    public String getString(long offset) {
        return Charset.defaultCharset().decode(ByteBuffer.wrap(getZeroTerminatedByteArray(offset))).toString();
    }

    public void putShort(long offset, short value) {
        UNSAFE.putShort(address + offset, value);
    }

    public void putInt(long offset, int value) {
        UNSAFE.putInt(address + offset, value);
    }

    public void putLongLong(long offset, long value) {
        putLong(offset, value);
    }

    public void putPointer(long offset, Pointer value) {
        putLong(offset, value.getAddress());
    }

    private void putZeroTerminatedByteArray(long offset, byte[] bytes, int start, int length) {
        put(offset, bytes, start, length);
        putByte(offset + length, (byte) 0);
    }

    @CompilerDirectives.TruffleBoundary
    public void putString(long offset, String string, int maxLength, Charset cs) {
        ByteBuffer buf = cs.encode(string);
        int len = Math.min(maxLength, buf.remaining());
        putZeroTerminatedByteArray(offset, buf.array(), buf.arrayOffset() + buf.position(), len);
    }

    @CompilerDirectives.TruffleBoundary
    public Pointer readPointer(long offset) {
        final long p = getLong(offset);
        if (p == 0) {
            return null;
        } else {
            return new Pointer(p);
        }
    }

    @CompilerDirectives.TruffleBoundary
    public int indexOf(long offset, byte value) {
        int n = 0;
        while (true) {
            if (getByte(offset + n) == value) {
                return n;
            }
            n++;
        }
    }

    @CompilerDirectives.TruffleBoundary
    public void setMemory(long offset, long size, byte value) {
        for (long n = 0; n < size; n++) {
            putByte(offset + n, value);
        }
    }

    public void free() {
        UNSAFE.freeMemory(address);
    }

    public long getAddress() {
        return address;
    }

    public jnr.ffi.Pointer getPointer() {
        return Runtime.getSystemRuntime().getMemoryManager().newPointer(address);
    }

    @CompilerDirectives.TruffleBoundary
    public void setAutorelease(boolean autorelease) {
        // TODO CS 31-Jul-2017
        Log.LOGGER.warning("pointer autorelease after allocation is not implemented - memory will be leaking");
    }

    @SuppressWarnings("restriction")
    private static Unsafe getUnsafe() {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new Error(e);
        }
    }

    private static final Unsafe UNSAFE = getUnsafe();

}
