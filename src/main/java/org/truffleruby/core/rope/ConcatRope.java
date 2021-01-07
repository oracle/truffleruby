/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.ValueType;
import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;

public class ConcatRope extends ManagedRope {

    /** Wrapper for the current state of the concat rope, including null children and a a byte array, or a null byte
     * array and the children. Accessing the state through {@link #getState()} guarantees the avoidance of race
     * conditions. */
    @ValueType
    public static class ConcatState {
        public final ManagedRope left, right;
        public final byte[] bytes;

        public ConcatState(ManagedRope left, ManagedRope right, byte[] bytes) {
            assert bytes == null && left != null && right != null || bytes != null && left == null && right == null;
            this.left = left;
            this.right = right;
            this.bytes = bytes;
        }

        public boolean isBytes() {
            return bytes != null;
        }

        public boolean isChildren() {
            return bytes == null;
        }
    }

    private ManagedRope left;
    private ManagedRope right;

    public ConcatRope(
            ManagedRope left,
            ManagedRope right,
            Encoding encoding,
            CodeRange codeRange) {
        this(
                left,
                right,
                encoding,
                codeRange,
                left.byteLength() + right.byteLength(),
                left.characterLength() + right.characterLength(),
                null);
    }

    private ConcatRope(
            ManagedRope left,
            ManagedRope right,
            Encoding encoding,
            CodeRange codeRange,
            int byteLength,
            int characterLength,
            byte[] bytes) {
        super(encoding, codeRange, byteLength, characterLength, bytes);
        this.left = left;
        this.right = right;
    }

    @Override
    Rope withEncoding7bit(Encoding newEncoding) {
        assert getCodeRange() == CodeRange.CR_7BIT;
        return withEncoding(newEncoding, CodeRange.CR_7BIT, characterLength());
    }

    @Override
    Rope withBinaryEncoding() {
        assert getCodeRange() == CodeRange.CR_VALID;
        return withEncoding(ASCIIEncoding.INSTANCE, CodeRange.CR_VALID, byteLength());
    }

    private ConcatRope withEncoding(Encoding encoding, CodeRange codeRange, int characterLength) {
        final ConcatState state = getState();
        return new ConcatRope(state.left, state.right, encoding, codeRange, byteLength(), characterLength, state.bytes);
    }

    @Override
    protected byte[] getBytesSlow() {
        final byte[] out = RopeOperations.flattenBytes(this);
        this.left = null;
        this.right = null;
        return out;
    }

    /** Access the state in a way that prevents race conditions. */
    public ConcatState getState() {
        if (this.bytes != null) {
            return new ConcatState(null, null, this.bytes);
        }

        final ManagedRope left = this.left;
        final ManagedRope right = this.right;
        if (left != null && right != null) {
            return new ConcatState(left, right, null);
        }

        CompilerDirectives.transferToInterpreterAndInvalidate();
        assert this.bytes != null;
        return new ConcatState(null, null, this.bytes);
    }
}
