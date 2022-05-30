/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import org.jcodings.Encoding;
import org.truffleruby.core.string.StringAttributes;
import org.truffleruby.extra.ffi.Pointer;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class NativeRope extends Rope {

    public static final int UNKNOWN_CHARACTER_LENGTH = -1;

    private CodeRange codeRange;
    private int characterLength;
    private final Pointer pointer;

    public NativeRope(Pointer pointer, int byteLength, Encoding encoding, int characterLength, CodeRange codeRange) {
        super(encoding, byteLength, null);

        assert (codeRange == CodeRange.CR_UNKNOWN) == (characterLength == UNKNOWN_CHARACTER_LENGTH);
        this.codeRange = codeRange;
        this.characterLength = characterLength;
        this.pointer = pointer;
    }

    @Override
    public ManagedRope asManaged() {
        return RopeOperations.create(getBytes(), getEncoding(), CodeRange.CR_UNKNOWN);
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
            final StringAttributes attributes = RopeOperations
                    .calculateCodeRangeAndLength(getEncoding(), getBytes(), 0, byteLength());
            updateAttributes(attributes);
            return attributes.getCodeRange();
        } else {
            return codeRange;
        }
    }

    @Override
    public int characterLength() {
        if (characterLength == UNKNOWN_CHARACTER_LENGTH) {
            final StringAttributes attributes = RopeOperations
                    .calculateCodeRangeAndLength(getEncoding(), getBytes(), 0, byteLength());
            updateAttributes(attributes);
            return attributes.getCharacterLength();
        } else {
            return characterLength;
        }
    }

    // TODO
    public void clearCodeRange() {
        this.characterLength = UNKNOWN_CHARACTER_LENGTH;
        this.codeRange = CodeRange.CR_UNKNOWN;
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

    @Override
    public int hashCode() {
        // TODO (pitr-ch 16-May-2017): this forces Rope#hashCode to be non-final, which is bad for performance
        return RopeOperations.hashForRange(this, 1, 0, byteLength());
    }

    public Pointer getNativePointer() {
        return pointer;
    }

}
