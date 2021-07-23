/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import com.oracle.truffle.api.CompilerDirectives.ValueType;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.InvalidArrayIndexException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.profiles.BranchProfile;

@ValueType
@ExportLibrary(InteropLibrary.class)
public final class Bytes implements TruffleObject {
    public final byte[] array;
    public final int offset;
    public final int length;

    public Bytes(byte[] array, int offset, int length) {
        assert offset >= 0 && length >= 0 && offset + length <= array.length;
        this.array = array;
        this.offset = offset;
        this.length = length;
    }

    public Bytes(byte[] array) {
        this(array, 0, array.length);
    }

    public static Bytes fromRange(byte[] array, int start, int end) {
        assert 0 <= start && start <= end && end <= array.length;
        return new Bytes(array, start, end - start);
    }

    /** Just like {@link #fromRange(byte[], int, int)}, but will clamp the length to stay within the bounds. */
    public static Bytes fromRangeClamped(byte[] array, int start, int end) {
        return fromRange(array, start, Math.min(array.length, end));
    }

    /** Returns the end offset, equal to {@link #offset} + {@link #length}. */
    public int end() {
        return offset + length;
    }

    public boolean isEmpty() {
        return length == 0;
    }

    public Bytes slice(int offset, int length) {
        assert offset >= 0 && length >= 0 && offset + length <= this.length;
        return new Bytes(this.array, this.offset + offset, length);
    }

    public Bytes sliceRange(int start, int end) {
        assert start >= 0 && end >= 0 && start <= end && end <= this.length;
        return new Bytes(this.array, this.offset + start, end - start);
    }

    /** Just like {@link #slice(int, int)}}, but will clamp the length to stay within the bounds. */
    public Bytes clampedSlice(int offset, int length) {
        return slice(offset, Math.min(length, this.length - offset));
    }

    /** Just like {@link #sliceRange(int, int)}}, but will clamp the end offset to stay within the bounds. */
    public Bytes clampedRange(int start, int end) {
        return sliceRange(start, Math.min(end, this.length));
    }

    public byte get(int i) {
        return array[offset + i];
    }

    // region Array messages for TRegex
    @ExportMessage
    public boolean hasArrayElements() {
        return true;
    }

    @ExportMessage
    public long getArraySize() {
        return length;
    }

    @ExportMessage
    public Object readArrayElement(long index,
            @Cached BranchProfile errorProfile) throws InvalidArrayIndexException {
        if (isArrayElementReadable(index)) {
            return get((int) index);
        } else {
            errorProfile.enter();
            throw InvalidArrayIndexException.create(index);
        }
    }

    @ExportMessage
    public boolean isArrayElementReadable(long index) {
        return index >= 0 && index < length;
    }
    // endregion
}
