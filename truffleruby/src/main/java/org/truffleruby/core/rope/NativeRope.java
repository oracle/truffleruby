/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.rope;

import jnr.ffi.Pointer;
import jnr.ffi.provider.MemoryManager;
import org.jcodings.Encoding;

public class NativeRope extends Rope {

    private Pointer pointer;

    public NativeRope(MemoryManager memoryManager, byte[] bytes, Encoding encoding, int characterLength) {
        super(encoding, CodeRange.CR_UNKNOWN, false, bytes.length, characterLength, 1, null);

        pointer = memoryManager.allocateDirect(bytes.length + 1 /* trailing \0 */, false);
        pointer.put(0, bytes, 0, bytes.length);
        pointer.putByte(bytes.length, (byte) 0);
    }

    @Override
    public byte[] getBytes() {
        // Always re-read bytes from the native pointer as they might have changed.
        final byte[] bytes = new byte[byteLength()];
        copyTo(0, bytes, 0);
        return bytes;
    }

    public void copyTo(int from, byte[] buffer, int bufferPos) {
        pointer.get(from, buffer, bufferPos, byteLength());
    }

    @Override
    public byte getByteSlow(int index) {
        return get(index);
    }

    @Override
    public byte get(int index) {
        assert 0 <= index && index <= byteLength();
        return pointer.getByte(index);
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Rope withEncoding(Encoding newEncoding, CodeRange newCodeRange) {
        throw new UnsupportedOperationException();
    }

    public Pointer getNativePointer() {
        return pointer;
    }

    public LeafRope toLeafRope() {
        return RopeOperations.create(getBytes(), getEncoding(), CodeRange.CR_UNKNOWN);
    }
}
