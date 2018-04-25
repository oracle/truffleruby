/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.extra.ffi;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import org.truffleruby.RubyContext;
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

    public void writeBytes(long offset, byte[] buffer, int bufferPos, int length) {
        assert address + offset != 0 || length == 0;
        assert buffer != null;
        assert bufferPos >= 0;
        assert length >= 0;

        UNSAFE.copyMemory(buffer, Unsafe.ARRAY_BYTE_BASE_OFFSET + bufferPos, null, address + offset, length);
    }

    public byte readByte(long offset) {
        assert address + offset != 0;
        return UNSAFE.getByte(address + offset);
    }

    public void readBytes(long offset, byte[] buffer, int bufferPos, int length) {
        assert address + offset != 0 || length == 0;
        assert buffer != null;
        assert bufferPos >= 0;
        assert length >= 0;

        UNSAFE.copyMemory(null, address + offset, buffer, Unsafe.ARRAY_BYTE_BASE_OFFSET + bufferPos, length);
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

    public byte[] readZeroTerminatedByteArray(RubyContext context, long offset) {
        final int length = findNullByte(context, offset);
        final byte[] bytes = new byte[length];
        readBytes(offset, bytes, 0, length);
        return bytes;
    }


    public Pointer readPointer(long offset) {
        return new Pointer(readLong(offset));
    }

    public int findNullByte(RubyContext context, long offset) {
        if (context.getOptions().NATIVE_PLATFORM) {
            return (int) (long) context.getTruffleNFI().getStrlen().call(address + offset);
        }

        int n = 0;
        while (true) {
            if (readByte(offset + n) == 0) {
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
        finalizationService.addFinalizer(false, this, Pointer.class, new FreeAddressFinalizer(address), null);

        autorelease = true;
    }

    private static class FreeAddressFinalizer implements Runnable {

        private final long address;

        public FreeAddressFinalizer(long address) {
            this.address = address;
        }

        public void run() {
            UNSAFE.freeMemory(address);
        }

        @Override
        public String toString() {
            return "free(" + address + ")";
        }

    }

    @TruffleBoundary
    public synchronized void disableAutorelease(FinalizationService finalizationService) {
        if (!autorelease) {
            return;
        }

        finalizationService.removeFinalizers(false, this, Pointer.class);

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

    public static final Unsafe UNSAFE = getUnsafe();

}
