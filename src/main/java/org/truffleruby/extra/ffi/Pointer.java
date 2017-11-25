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

import jnr.ffi.Address;
import jnr.ffi.Runtime;
import jnr.ffi.Type;
import org.truffleruby.core.FinalizationService;
import org.truffleruby.parser.parser.SuppressFBWarnings;

import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.charset.Charset;

@SuppressFBWarnings("Nm")
public class Pointer extends jnr.ffi.Pointer implements AutoCloseable {

    public static final jnr.ffi.Pointer JNR_NULL = Runtime.getSystemRuntime().getMemoryManager().newOpaquePointer(0);

    public static final Pointer NULL = new Pointer(0);

    /**
     * Allocates memory and produces a pointer to it. Does not clear or
     * initialize the memory, so it will contain arbitrary values. Use
     * {@link #calloc} to get cleared memory.
     */
    public static Pointer malloc(long size) {
        return new Pointer(UNSAFE.allocateMemory(size), size);
    }

    /**
     * Allocates memory and produces a pointer to it. Clears the memory
     * before returning it. Use {@link #malloc} if you do not need the memory
     * to be cleared.
     */
    public static Pointer calloc(long size) {
        final Pointer pointer = malloc(size);
        pointer.writeBytes(0, size, (byte) 0);
        return pointer;
    }

    private final long address;
    private final long size;
    private boolean autorelease;

    public Pointer(long address) {
        this(address, 0);
    }

    public Pointer(long address, long size) {
        super(null, address, true);
        this.address = address;
        this.size = size;
    }

    public void writeByte(long offset, byte b) {
        assert address + offset != 0;
        UNSAFE.putByte(address + offset, b);
    }

    public void writeShort(long offset, short value) {
        assert address + offset != 0;
        UNSAFE.putShort(address + offset, value);
    }

    public void writeInt(long offset, int value) {
        assert address + offset != 0;
        UNSAFE.putInt(address + offset, value);
    }

    public void writeLong(long offset, long value) {
        assert address + offset != 0;
        UNSAFE.putLong(address + offset, value);
    }

    public void writePointer(long offset, Pointer value) {
        writeLong(offset, value.getAddress());
    }

    public void writeZeroTerminatedBytes(long offset, byte[] bytes, int start, int length) {
        writeBytes(offset, bytes, start, length);
        writeByte(offset + length, (byte) 0);
    }

    public void writeBytes(long offset, long size, byte value) {
        assert address + offset != 0;
        UNSAFE.setMemory(address + offset, size, value);
    }

    public void writeBytes(long offset, byte[] bytes, int index, int length) {
        for (int n = 0; n < length; n++) {
            writeByte(offset + n, bytes[index + n]);
        }
    }

    public byte readByte(long offset) {
        assert address + offset != 0;
        return UNSAFE.getByte(address + offset);
    }

    public void readBytes(long from, byte[] buffer, int bufferPos, int i) {
        for (int n = 0; n < i; n++) {
            buffer[bufferPos + n] = readByte(from + n);
        }
    }

    public short readShort(long offset) {
        assert address + offset != 0;
        return UNSAFE.getShort(address + offset);
    }

    public int readInt(long offset) {
        assert address + offset != 0;
        return UNSAFE.getInt(address + offset);
    }

    public long readLong(long offset) {
        assert address + offset != 0;
        return UNSAFE.getLong(address + offset);
    }

    public double readDouble(long offset) {
        assert address + offset != 0;
        return UNSAFE.getDouble(address + offset);
    }

    public byte[] readZeroTerminatedByteArray(long offset) {
        final int length = findByte(offset, (byte) 0);
        final byte[] bytes = new byte[length];
        readBytes(offset, bytes, 0, length);
        return bytes;
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

    public boolean isNull() {
        return address == 0;
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

    @Override
    public long size() {
        return size;
    }

    @Override
    public boolean hasArray() {
        return false;
    }

    @Override
    public Object array() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int arrayOffset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int arrayLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte getByte(long offset) {
        return readByte(offset);
    }

    @Override
    public short getShort(long offset) {
        return readShort(offset);
    }

    @Override
    public int getInt(long offset) {
        return readInt(offset);
    }

    @Override
    public long getLong(long offset) {
        return readLong(offset);
    }

    @Override
    public long getLongLong(long offset) {
        return readLong(offset);
    }

    @Override
    public float getFloat(long offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(long offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getNativeLong(long offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getInt(Type type, long offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putByte(long offset, byte value) {
        writeByte(offset, value);
    }

    @Override
    public void putShort(long offset, short value) {
        writeShort(offset, value);
    }

    @Override
    public void putInt(long offset, int value) {
        writeInt(offset, value);
    }

    @Override
    public void putLong(long offset, long value) {
        writeLong(offset, value);
    }

    @Override
    public void putLongLong(long offset, long value) {
        writeLong(offset, value);
    }

    @Override
    public void putFloat(long offset, float value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putDouble(long offset, double value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putNativeLong(long offset, long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putInt(Type type, long offset, long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getAddress(long offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAddress(long offset, long value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAddress(long offset, Address value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void get(long offset, byte[] dst, int idx, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(long offset, byte[] src, int idx, int len) {
        writeBytes(offset, src, idx, len);
    }

    @Override
    public void get(long offset, short[] dst, int idx, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(long offset, short[] src, int idx, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void get(long offset, int[] dst, int idx, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(long offset, int[] src, int idx, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void get(long offset, long[] dst, int idx, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(long offset, long[] src, int idx, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void get(long offset, float[] dst, int idx, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(long offset, float[] src, int idx, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void get(long offset, double[] dst, int idx, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(long offset, double[] src, int idx, int len) {
        throw new UnsupportedOperationException();
    }

    @Override
    public jnr.ffi.Pointer getPointer(long offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public jnr.ffi.Pointer getPointer(long offset, long size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putPointer(long offset, jnr.ffi.Pointer value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString(long offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString(long offset, int maxLength, Charset cs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putString(long offset, String string, int maxLength, Charset cs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public jnr.ffi.Pointer slice(long offset) {
        throw new UnsupportedOperationException();
    }

    @Override
    public jnr.ffi.Pointer slice(long offset, long size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void transferTo(long offset, jnr.ffi.Pointer dst, long dstOffset, long count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void transferFrom(long offset, jnr.ffi.Pointer src, long srcOffset, long count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkBounds(long offset, long length) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setMemory(long offset, long size, byte value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(long offset, byte value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(long offset, byte value, int maxlen) {
        throw new UnsupportedOperationException();
    }

}
