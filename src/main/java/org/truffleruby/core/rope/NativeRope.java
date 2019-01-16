/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import org.jcodings.Encoding;
import org.truffleruby.core.FinalizationService;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.truffleruby.extra.ffi.Pointer;

public class NativeRope extends Rope {

    private final Pointer pointer;

    public NativeRope(FinalizationService finalizationService, byte[] bytes, Encoding encoding, int characterLength, CodeRange codeRange) {
        this(allocateNativePointer(finalizationService, bytes), bytes.length, encoding, characterLength, codeRange);
    }

    private static Pointer allocateNativePointer(FinalizationService finalizationService, byte[] bytes) {
        final Pointer pointer = Pointer.malloc(bytes.length + 1);
        pointer.enableAutorelease(finalizationService);
        pointer.writeBytes(0, bytes, 0, bytes.length);
        pointer.writeByte(bytes.length, (byte) 0);
        return pointer;
    }

    private static Pointer copyNativePointer(FinalizationService finalizationService, Pointer existing) {
        final Pointer pointer = Pointer.malloc(existing.getSize());
        pointer.enableAutorelease(finalizationService);
        pointer.writeBytes(0, existing, 0, existing.getSize());
        return pointer;
    }

    private NativeRope(Pointer pointer, int byteLength, Encoding encoding, int characterLength, CodeRange codeRange) {
        super(encoding, codeRange, false, byteLength, characterLength, 1, null);
        this.pointer = pointer;
    }

    public NativeRope withByteLength(int newByteLength, int characterLength, CodeRange codeRange) {
        pointer.writeByte(newByteLength, (byte) 0); // Like MRI
        return new NativeRope(pointer, newByteLength, getEncoding(), characterLength, codeRange);
    }

    public NativeRope makeCopy(FinalizationService finalizationService) {
        final Pointer newPointer = copyNativePointer(finalizationService, pointer);
        return new NativeRope(newPointer, byteLength(), getEncoding(), characterLength(), getCodeRange());
    }

    @Override
    public byte[] getBytes() {
        // Always re-read bytes from the native pointer as they might have changed.
        final byte[] bytes = new byte[byteLength()];
        copyTo(0, bytes, 0, byteLength());
        return bytes;
    }

    @TruffleBoundary
    public void copyTo(int byteOffset, byte[] dest, int bufferPos, int byteLength) {
        pointer.readBytes(byteOffset, dest, bufferPos, byteLength);
    }

    @Override
    public byte getByteSlow(int index) {
        return get(index);
    }

    @Override
    public byte get(int index) {
        assert 0 <= index && index < pointer.getSize();
        return pointer.readByte(index);
    }

    public void set(int index, int value) {
        assert 0 <= index && index < pointer.getSize();
        pointer.writeByte(index, (byte) value);
    }

    @Override
    public int hashCode() {
        // TODO (pitr-ch 16-May-2017): this forces Rope#hashCode to be non-final, which is bad for performance
        return RopeOperations.hashForRange(this, 1, 0, byteLength());
    }

    @Override
    public String toString() {
        assert ALLOW_TO_STRING;
        return toLeafRope().toString();
    }

    @Override
    public Rope withEncoding(Encoding newEncoding, CodeRange newCodeRange) {
        return RopeOperations.create(getBytes(), newEncoding, newCodeRange);
    }

    public Pointer getNativePointer() {
        return pointer;
    }

    @TruffleBoundary
    public LeafRope toLeafRope() {
        return RopeOperations.create(getBytes(), getEncoding(), CodeRange.CR_UNKNOWN);
    }

}
