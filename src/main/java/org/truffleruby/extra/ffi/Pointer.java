/*
 * Copyright (c) 2017, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra.ffi;

import java.lang.ref.Cleaner.Cleanable;
import java.lang.reflect.Field;

import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.SuppressFBWarnings;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

import sun.misc.Unsafe;

@SuppressFBWarnings("Nm")
public final class Pointer implements AutoCloseable {

    public static final Pointer NULL = new Pointer(0);
    public static final long SIZE = Long.BYTES;
    public static final long UNBOUNDED = Long.MAX_VALUE;

    public static final Pointer[] EMPTY_ARRAY = new Pointer[0];

    /** Allocates memory and produces a pointer to it. Does not clear or initialize the memory, so it will contain
     * arbitrary values. Use {@link #calloc} to get cleared memory. */
    public static Pointer malloc(long size) {
        return new Pointer(UNSAFE.allocateMemory(size), size);
    }

    /** Includes {@link #enableAutorelease(RubyLanguage)} and avoids locking for it */
    public static Pointer mallocAutoRelease(long size, RubyLanguage langauge) {
        return new Pointer(UNSAFE.allocateMemory(size), size, langauge);
    }

    /** Allocates memory and produces a pointer to it. Clears the memory before returning it. Use {@link #malloc} if you
     * do not need the memory to be cleared. */
    public static Pointer calloc(long size) {
        final Pointer pointer = malloc(size);
        pointer.writeBytes(0, size, (byte) 0);
        return pointer;
    }

    /** Includes {@link #enableAutorelease(RubyLanguage)} and avoids locking for it */
    public static Pointer callocAutoRelease(long size, RubyLanguage langauge) {
        final Pointer pointer = mallocAutoRelease(size, langauge);
        pointer.writeBytes(0, size, (byte) 0);
        return pointer;
    }

    private final long address;
    private final long size;
    /** Non-null iff autorelease */
    private Cleanable cleanable = null;
    private AutoReleaseState autoReleaseState = null;

    /** This is needed because we need a mutable object to hold the pointer address so that it can be freed or marked as
     * not to be freed if auto-release is disabled. THis can't be the address field on the pointer itself as that would
     * prevent the pointer from being collected, and we can't retrieve this from the cleaner as it is effectively
     * opaque. */
    private static class AutoReleaseState implements Runnable {

        long address;

        AutoReleaseState(long address) {
            this.address = address;
        }

        public void run() {
            if (address != 0) {
                UNSAFE.freeMemory(address);
            }
        }

        public void markFreed() {
            address = 0;
        }
    }

    public Pointer(long address) {
        this(address, UNBOUNDED);
    }

    public Pointer(long address, long size) {
        this.address = address;
        this.size = size;
    }

    private Pointer(long address, long size, RubyLanguage langauge) {
        this.address = address;
        this.size = size;
        enableAutoreleaseUnsynchronized(langauge);
    }

    public boolean isNull() {
        return address == 0;
    }

    public long getAddress() {
        return address;
    }

    public long getEndAddress() {
        assert isBounded();
        return address + size;
    }

    public long getSize() {
        return size;
    }

    public boolean isBounded() {
        return size != UNBOUNDED;
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

    @TruffleBoundary
    public boolean readBytesCheck8Bit(byte[] buffer, int length) {
        assert address != 0 || length == 0;
        assert buffer != null;
        assert length >= 0;

        long base = address;
        boolean highBitUsed = false;
        for (int i = 0; i < length; i++) {
            byte aByte = UNSAFE.getByte(null, base + i);
            if (aByte < 0) {
                highBitUsed = true;
            }
            buffer[i] = aByte;
        }
        return highBitUsed;
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

    public byte[] readZeroTerminatedByteArray(RubyContext context, InteropLibrary interopLibrary, long offset) {
        return readBytes(offset, checkStringSize(findNullByte(
                context,
                interopLibrary,
                offset)));
    }

    public byte[] readZeroTerminatedByteArray(RubyContext context, InteropLibrary interopLibrary, long offset,
            long limit) {
        return readBytes(offset, checkStringSize(findNullByte(
                context,
                interopLibrary,
                offset,
                limit)));
    }

    public long findNullByte(RubyContext context, InteropLibrary interopLibrary, long offset) {
        if (context.getOptions().NATIVE_PLATFORM) {
            try {
                return (long) interopLibrary.execute(context.getTruffleNFI().getStrlen(), address + offset);
            } catch (InteropException e) {
                throw CompilerDirectives.shouldNotReachHere(e);
            }
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

    private long findNullByte(RubyContext context, InteropLibrary interopLibrary, long offset, long limit) {
        if (context.getOptions().NATIVE_PLATFORM) {
            try {
                return (long) interopLibrary.execute(context.getTruffleNFI().getStrnlen(), address + offset, limit);
            } catch (InteropException e) {
                throw CompilerDirectives.shouldNotReachHere(e);
            }
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
    public synchronized void free() {
        if (cleanable != null) {
            cleanable.clean();
        } else {
            UNSAFE.freeMemory(address);
        }
    }

    @TruffleBoundary
    public synchronized void freeNoAutorelease() {
        if (cleanable != null) {
            throw new UnsupportedOperationException("Calling freeNoAutorelease() on a autorelease Pointer");
        }
        UNSAFE.freeMemory(address);
    }

    @Override
    public void close() {
        freeNoAutorelease();
    }

    public synchronized boolean isAutorelease() {
        return cleanable != null;
    }

    @TruffleBoundary
    public synchronized void enableAutorelease(RubyLanguage language) {
        if (cleanable != null) {
            return;
        }

        enableAutoreleaseUnsynchronized(language);
    }

    @TruffleBoundary
    private void enableAutoreleaseUnsynchronized(RubyLanguage language) {
        // We must be careful here that the cleaner does not capture
        // the Pointer itself, and that we a form of mutable state
        // that will allow use to disable autorelease later.
        autoReleaseState = new AutoReleaseState(address);
        cleanable = language.cleaner.register(this, autoReleaseState);

    }

    @TruffleBoundary
    public synchronized void disableAutorelease() {
        if (cleanable == null) {
            return;
        }

        autoReleaseState.markFreed();
        cleanable.clean();
        cleanable = null;
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
        return "Pointer@0x" + Long.toHexString(address) + "(size=" + (isBounded() ? size : "UNBOUNDED") + ")";
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
