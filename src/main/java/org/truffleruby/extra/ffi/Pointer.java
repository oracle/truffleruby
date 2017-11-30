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

import org.truffleruby.core.FinalizationService;
import org.truffleruby.parser.parser.SuppressFBWarnings;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

@SuppressFBWarnings("Nm")
public class Pointer implements AutoCloseable {

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
        assert address + offset != 0 || size == 0;
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
        return new Pointer(readLong(offset));
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
        if (!autorelease) {
            UNSAFE.freeMemory(address);
        }
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

    public long getSize() {
        return size;
    }

    @TruffleBoundary
    public synchronized void enableAutorelease(FinalizationService finalizationService) {
        if (autorelease) {
            return;
        }

        // We must be careful here that the finalizer does not capture the Pointer itself that we'd
        // like to finalize.
        addFinalizer(finalizationService, this, address);

        autorelease = true;
    }

    private static void addFinalizer(FinalizationService finalizationService, Pointer pointer, long address) {
        finalizationService.addFinalizer(pointer, Pointer.class, () -> UNSAFE.freeMemory(address));
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
