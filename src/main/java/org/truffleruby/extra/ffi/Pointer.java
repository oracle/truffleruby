/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra.ffi;

import java.lang.reflect.Field;

import org.truffleruby.RubyContext;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.core.FinalizationService;
import org.truffleruby.core.FinalizerReference;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import sun.misc.Unsafe;

@SuppressFBWarnings("Nm")
public class Pointer implements AutoCloseable {

    public static final Pointer NULL = new Pointer(0);
    public static final long SIZE = Long.BYTES;
    public static final long UNBOUNDED = Long.MAX_VALUE;

    public static final Pointer[] EMPTY_ARRAY = new Pointer[0];

    /** Allocates memory and produces a pointer to it. Does not clear or initialize the memory, so it will contain
     * arbitrary values. Use {@link #calloc} to get cleared memory. */
    public static Pointer malloc(long size) {
        return new Pointer(UNSAFE.allocateMemory(size), size);
    }

    /** Allocates memory and produces a pointer to it. Clears the memory before returning it. Use {@link #malloc} if you
     * do not need the memory to be cleared. */
    public static Pointer calloc(long size) {
        final Pointer pointer = malloc(size);
        pointer.writeBytes(0, size, (byte) 0);
        return pointer;
    }

    private final long address;
    private final long size;
    /** Non-null iff autorelease */
    private FinalizerReference finalizerRef = null;

    public Pointer(long address) {
        this(address, UNBOUNDED);
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

    public void writeFloat(long offset, float value) {
        assert address + offset != 0;
        UNSAFE.putFloat(address + offset, value);
    }

    public void writeDouble(long offset, double value) {
        assert address + offset != 0;
        UNSAFE.putDouble(address + offset, value);
    }

    public void writePointer(long offset, long address) {
        writeLong(offset, address);
    }

    public void writeZeroTerminatedBytes(long offset, byte[] bytes, int start, int length) {
        writeBytes(offset, bytes, start, length);
        writeByte(offset + length, (byte) 0);
    }

    @TruffleBoundary
    public void writeBytes(long offset, long size, byte value) {
        assert address + offset != 0 || size == 0;
        UNSAFE.setMemory(address + offset, size, value);
    }

    @TruffleBoundary
    public void writeBytes(long offset, Pointer buffer, int bufferPos, long length) {
        assert address + offset != 0 || length == 0;
        assert buffer != null;
        assert bufferPos >= 0;
        assert length >= 0;

        UNSAFE.copyMemory(buffer.getAddress() + bufferPos, address + offset, length);
    }

    @TruffleBoundary
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

    public byte[] readBytes(long offset, int length) {
        final byte[] bytes = new byte[length];
        readBytes(offset, bytes, 0, length);
        return bytes;
    }

    @TruffleBoundary
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

    public float readFloat(long offset) {
        assert address + offset != 0;
        return UNSAFE.getFloat(address + offset);
    }

    public double readDouble(long offset) {
        assert address + offset != 0;
        return UNSAFE.getDouble(address + offset);
    }

    public byte[] readZeroTerminatedByteArray(RubyContext context, long offset) {
        return readBytes(offset, checkStringSize(findNullByte(context, offset)));
    }

    public byte[] readZeroTerminatedByteArray(RubyContext context, long offset, long limit) {
        return readBytes(offset, checkStringSize(findNullByte(context, offset, limit)));
    }

    private long findNullByte(RubyContext context, long offset) {
        if (context.getOptions().NATIVE_PLATFORM) {
            return (long) context.getTruffleNFI().getStrlen().call(address + offset);
        } else {
            int n = 0;
            while (true) {
                if (readByte(offset + n) == 0) {
                    return n;
                }
                n++;
            }
        }
    }

    private long findNullByte(RubyContext context, long offset, long limit) {
        if (context.getOptions().NATIVE_PLATFORM) {
            return (long) context.getTruffleNFI().getStrnlen().call(address + offset, limit);
        } else {
            int n = 0;
            while (n < limit) {
                if (readByte(offset + n) == 0) {
                    return n;
                }
                n++;
            }
            return limit;
        }
    }

    private int checkStringSize(long size) {
        if (size > Integer.MAX_VALUE) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw new UnsupportedOperationException("native string is too long to read into managed code");
        }

        return (int) size;
    }

    public Pointer readPointer(long offset) {
        return new Pointer(readLong(offset));
    }

    @TruffleBoundary
    public synchronized void free(FinalizationService finalizationService) {
        if (finalizerRef != null) {
            disableAutorelease(finalizationService);
        }
        UNSAFE.freeMemory(address);
    }

    @TruffleBoundary
    public synchronized void freeNoAutorelease() {
        if (finalizerRef != null) {
            throw new UnsupportedOperationException("Calling freeNoAutorelease() on a autorelease Pointer");
        }
        UNSAFE.freeMemory(address);
    }

    @Override
    public void close() {
        freeNoAutorelease();
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

    public synchronized boolean isAutorelease() {
        return finalizerRef != null;
    }

    @TruffleBoundary
    public synchronized void enableAutorelease(FinalizationService finalizationService) {
        if (finalizerRef != null) {
            return;
        }

        // We must be careful here that the finalizer does not capture the Pointer itself that we'd
        // like to finalize.
        finalizerRef = finalizationService.addFinalizer(this, Pointer.class, new FreeAddressFinalizer(address), null);
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
            return "free(0x" + Long.toHexString(address) + ")";
        }

    }

    @TruffleBoundary
    public synchronized void disableAutorelease(FinalizationService finalizationService) {
        if (finalizerRef == null) {
            return;
        }

        FinalizerReference left = finalizationService.removeFinalizers(this, finalizerRef, Pointer.class);
        assert left == null;
        finalizerRef = null;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Pointer)) {
            return false;
        }

        return ((Pointer) other).getAddress() == address;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(address);
    }

    @TruffleBoundary
    @Override
    public String toString() {
        return "Pointer@0x" + Long.toHexString(address);
    }

    public static long rawMalloc(long size) {
        return UNSAFE.allocateMemory(size);
    }

    public static long rawRealloc(long address, long size) {
        return UNSAFE.reallocateMemory(address, size);
    }

    public static void rawFree(long address) {
        UNSAFE.freeMemory(address);
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
