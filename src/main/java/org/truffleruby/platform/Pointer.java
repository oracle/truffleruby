/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform;

import jnr.ffi.Runtime;
import jnr.ffi.provider.MemoryManager;
import org.truffleruby.extra.ffi.PointerNodes;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.nio.charset.Charset;

public class Pointer {

    public static final Pointer NULL = new Pointer(PointerNodes.NULL_POINTER);

    public static Pointer malloc(long size) {
        return new Pointer(UNSAFE.allocateMemory(size));
    }

    public static Pointer mallocAutorelease(MemoryManager memoryManager, int size) {
        return new Pointer(memoryManager.allocateDirect(size));
    }

    private final jnr.ffi.Pointer pointer;

    public Pointer(long address) {
        this(Runtime.getSystemRuntime().getMemoryManager().newPointer(address));
    }

    public Pointer(jnr.ffi.Pointer pointer) {
        this.pointer = pointer;
    }

    public Pointer add(long offset) {
        return new Pointer(pointer.address() + offset);
    }

    public void put(long i, byte[] bytes, int i1, int length) {
        pointer.put(i, bytes, i1, length);
    }

    public void putByte(long length, byte b) {
        pointer.putByte(length, b);
    }

    public byte getByte(long index) {
        return pointer.getByte(index);
    }

    public void get(int from, byte[] buffer, int bufferPos, int i) {
        pointer.get(from, buffer, bufferPos, i);
    }

    public void putLong(long value) {
        pointer.putLong(0, value);
    }

    public void putLong(long offset, long value) {
        pointer.putLong(offset, value);
    }

    public short getShort(long offset) {
        return pointer.getShort(offset);
    }

    public int getInt(long offset) {
        return pointer.getInt(offset);
    }

    public long getLong(long offset) {
        return pointer.getLong(offset);
    }

    public long getLongLong(long offset) {
        return pointer.getLongLong(offset);
    }

    public String getString(long offset) {
        return pointer.getString(offset);
    }

    public void putShort(long offset, short value) {
        pointer.putShort(offset, value);
    }

    public void putInt(long offset, int value) {
        pointer.putInt(offset, value);
    }

    public void putLongLong(long offset, long value) {
        pointer.putLongLong(offset, value);
    }

    public void putPointer(long offset, Pointer value) {
        pointer.putPointer(offset, value.pointer);
    }

    public void putString(long offset, String value, int length, Charset cs) {
        pointer.putString(offset, value, length, cs);
    }

    public jnr.ffi.Pointer getPointer(long offset) {
        return pointer.getPointer(offset);
    }

    public void free() {
        UNSAFE.freeMemory(pointer.address());
    }

    public long getAddress() {
        return pointer.address();
    }

    public jnr.ffi.Pointer getPointer() {
        return pointer;
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
