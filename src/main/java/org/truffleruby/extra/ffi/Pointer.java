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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import jnr.ffi.Runtime;
import org.truffleruby.core.FinalizationService;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class Pointer implements AutoCloseable {

    public static final jnr.ffi.Pointer JNR_NULL = Runtime.getSystemRuntime().getMemoryManager().newOpaquePointer(0);

    public static final Pointer NULL = new Pointer(0);

    public static Pointer malloc(long size) {
        return new Pointer(UNSAFE.allocateMemory(size));
    }

    public static Pointer calloc(long size) {
        final Pointer pointer = malloc(size);
        pointer.writeBytes(0, size, (byte) 0);
        return pointer;
    }

    private final long address;
    private boolean autorelease;

    public Pointer(long address) {
        this.address = address;
    }

    public void writeByte(long offset, byte b) {
        UNSAFE.putByte(address + offset, b);
    }

    public void writeShort(long offset, short value) {
        UNSAFE.putShort(address + offset, value);
    }

    public void writeInt(long offset, int value) {
        UNSAFE.putInt(address + offset, value);
    }

    public void writeLong(long offset, long value) {
        UNSAFE.putLong(address + offset, value);
    }

    public void writePointer(long offset, Pointer value) {
        writeLong(offset, value.getAddress());
    }

    private void writeZeroTerminatedBytes(long offset, byte[] bytes, int start, int length) {
        writeBytes(offset, bytes, start, length);
        writeByte(offset + length, (byte) 0);
    }

    @TruffleBoundary
    public void writeString(long offset, String string, int maxLength, Charset cs) {
        ByteBuffer buf = cs.encode(string);
        int len = Math.min(maxLength, buf.remaining());
        writeZeroTerminatedBytes(offset, buf.array(), buf.arrayOffset() + buf.position(), len);
    }

    public void writeBytes(long offset, long size, byte value) {
        for (long n = 0; n < size; n++) {
            writeByte(offset + n, value);
        }
    }

    public void writeBytes(long offset, byte[] bytes, int index, int length) {
        for (int n = 0; n < length; n++) {
            writeByte(offset + n, bytes[index + n]);
        }
    }

    public byte readByte(long offset) {
        return UNSAFE.getByte(address + offset);
    }

    public void readBytes(long from, byte[] buffer, int bufferPos, int i) {
        for (int n = 0; n < i; n++) {
            buffer[bufferPos + n] = readByte(from + n);
        }
    }

    public short readShort(long offset) {
        return UNSAFE.getShort(address + offset);
    }

    public int readInt(long offset) {
        return UNSAFE.getInt(address + offset);
    }

    public long readLong(long offset) {
        return UNSAFE.getLong(address + offset);
    }

    public byte[] readZeroTerminatedByteArray(long offset) {
        final int length = findByte(offset, (byte) 0);
        final byte[] bytes = new byte[length];
        readBytes(offset, bytes, 0, length);
        return bytes;
    }

    @TruffleBoundary
    public String readString(long offset) {
        return Charset.defaultCharset().decode(ByteBuffer.wrap(readZeroTerminatedByteArray(offset))).toString();
    }

    public Pointer readPointer(long offset) {
        final long p = readLong(offset);
        if (p == 0) {
            return null;
        } else {
            return new Pointer(p);
        }
    }

    public int findByte(long offset, byte value) {
        int n = 0;
        while (true) {
            if (readByte(offset + n) == value) {
                return n;
            }
            n++;
        }
    }

    public void free() {
        UNSAFE.freeMemory(address);
    }

    @Override
    public void close() {
        free();
    }

    public long getAddress() {
        return address;
    }

    public jnr.ffi.Pointer toJNRPointer() {
        return Runtime.getSystemRuntime().getMemoryManager().newPointer(address);
    }

    @TruffleBoundary
    public synchronized void enableAutorelease(FinalizationService finalizationService) {
        if (autorelease) {
            return;
        }

        finalizationService.addFinalizer(this, Pointer.class, () -> free());

        autorelease = true;
    }

    @TruffleBoundary
    public synchronized void disableAutorelease(FinalizationService finalizationService) {
        if (!autorelease) {
            return;
        }

        finalizationService.removeFinalizers(this, Pointer.class);

        autorelease = false;
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
