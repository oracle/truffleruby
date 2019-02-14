/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.truffleruby.core.FinalizationService;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.truffleruby.core.string.StringAttributes;
import org.truffleruby.core.string.StringSupport;
import org.truffleruby.extra.ffi.Pointer;

public class NativeRope extends Rope {

    public static final int UNKNOWN_CHARACTER_LENGTH = -1;

    private CodeRange codeRange;
    private int characterLength;
    private final Pointer pointer;

    public NativeRope(FinalizationService finalizationService, byte[] bytes, Encoding encoding, int characterLength, CodeRange codeRange) {
        this(allocateNativePointer(finalizationService, bytes), bytes.length, encoding, characterLength, codeRange);
    }

    private NativeRope(Pointer pointer, int byteLength, Encoding encoding, int characterLength, CodeRange codeRange) {
        super(encoding, false, byteLength, 1, null);

        this.codeRange = codeRange;
        this.characterLength = characterLength;
        this.pointer = pointer;
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

    public static NativeRope newBuffer(FinalizationService finalizationService, int byteLength) {
        return newBuffer(finalizationService, byteLength, byteLength);
    }

    public static NativeRope newBuffer(FinalizationService finalizationService, int byteCapacity, int byteLength) {
        assert byteCapacity >= byteLength;

        final Pointer pointer = Pointer.calloc(byteCapacity + 1);
        pointer.enableAutorelease(finalizationService);

        return new NativeRope(pointer, byteLength, ASCIIEncoding.INSTANCE, byteLength, CodeRange.CR_UNKNOWN);
    }

    public NativeRope withByteLength(int newByteLength, int characterLength, CodeRange codeRange) {
        assert (codeRange != CodeRange.CR_UNKNOWN && characterLength != UNKNOWN_CHARACTER_LENGTH) ||
                (codeRange == CodeRange.CR_UNKNOWN && characterLength == UNKNOWN_CHARACTER_LENGTH);

        pointer.writeByte(newByteLength, (byte) 0); // Like MRI
        return new NativeRope(pointer, newByteLength, getEncoding(), characterLength, codeRange);
    }

    public NativeRope makeCopy(FinalizationService finalizationService) {
        final Pointer newPointer = copyNativePointer(finalizationService, pointer);
        return new NativeRope(newPointer, byteLength(), getEncoding(), characterLength(), getCodeRange());
    }

    public NativeRope grow(FinalizationService finalizationService, int newByteLength) {
        assert newByteLength > this.byteLength();

        final NativeRope ret = newBuffer(finalizationService, newByteLength, byteLength());
        ret.pointer.writeBytes(0, this.pointer, 0, byteLength());
        ret.codeRange = this.codeRange;

        return ret;
    }

    @Override
    public byte[] getBytes() {
        // Always re-read bytes from the native pointer as they might have changed.
        final byte[] bytes = new byte[byteLength()];
        copyTo(0, bytes, 0, byteLength());
        return bytes;
    }

    public CodeRange getRawCodeRange() {
        return codeRange;
    }

    @Override
    public CodeRange getCodeRange() {
        if (codeRange == CodeRange.CR_UNKNOWN) {
            final StringAttributes attributes = RopeOperations.calculateCodeRangeAndLength(getEncoding(), getBytes(), 0, byteLength());
            updateAttributes(attributes);
        }

        return codeRange;
    }

    public int rawCharacterLength() {
        return characterLength;
    }

    @Override
    public int characterLength() {
        if (characterLength == UNKNOWN_CHARACTER_LENGTH) {
            final StringAttributes attributes = RopeOperations.calculateCodeRangeAndLength(getEncoding(), getBytes(), 0, byteLength());
            updateAttributes(attributes);
        }

        return characterLength;
    }

    public void setCharacterLength(int characterLength) {
        this.characterLength = characterLength;
    }

    public void setCodeRange(CodeRange codeRange) {
        if (codeRange != this.codeRange) {
            characterLength = UNKNOWN_CHARACTER_LENGTH;
        }

        this.codeRange = codeRange;
    }

    public void updateAttributes(StringAttributes attributes) {
        this.characterLength = attributes.getCharacterLength();
        this.codeRange = attributes.getCodeRange();
    }

    public byte[] getBytes(int byteOffset, int byteLength) {
        final byte[] bytes = new byte[byteLength];
        copyTo(byteOffset, bytes, 0, byteLength);
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
        assert value >= -128 && value < 256;

        if (codeRange == CodeRange.CR_7BIT && StringSupport.isAsciiCodepoint(value)) {
            codeRange = CodeRange.CR_7BIT;
        } else {
            setCodeRange(CodeRange.CR_UNKNOWN);
        }

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

    public long getCapacity() {
        final long nativeBufferSize = pointer.getSize();
        assert nativeBufferSize > 0;
        // Do not count the extra byte for \0, like MRI.
        return nativeBufferSize - 1;
    }

    @TruffleBoundary
    public LeafRope toLeafRope() {
        return RopeOperations.create(getBytes(), getEncoding(), CodeRange.CR_UNKNOWN);
    }

}
