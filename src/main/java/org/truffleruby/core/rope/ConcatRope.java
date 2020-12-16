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

    @ValueType
    public static class ConcatChildren {
        public final ManagedRope left, right;

        public ConcatChildren(ManagedRope left, ManagedRope right) {
            this.left = left;
            this.right = right;
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
        final ConcatChildren children = getChildrenOrNull();
        final ManagedRope left, right;
        final byte[] bytes;
        if (children != null) {
            left = children.left;
            right = children.right;
            bytes = null;
        } else {
            left = null;
            right = null;
            bytes = getRawBytes();
        }
        return new ConcatRope(left, right, encoding, codeRange, byteLength(), characterLength, bytes);
    }

    @Override
    protected byte[] getBytesSlow() {
        final byte[] out = RopeOperations.flattenBytes(this);
        this.left = null;
        this.right = null;
        return out;
    }

    public ConcatChildren getChildrenOrNull() {
        final ManagedRope left = this.left;
        final ManagedRope right = this.right;

        if (left != null && right != null) {
            return new ConcatChildren(left, right);
        } else if (this.bytes != null) {
            return null;
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            assert this.bytes != null;
            return null;
        }
    }

    public byte[] getBytesOrNull() {
        final byte[] bytes = this.bytes;

        if (bytes != null) {
            return bytes;
        } else if (this.left != null && this.right != null) {
            return null;
        } else {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            assert this.bytes != null;
            return this.bytes;
        }
    }
}
