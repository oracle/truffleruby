/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */

package org.truffleruby.core.rope;

import org.jcodings.Encoding;

public class SubstringRope extends ManagedRope {

    private final Rope child;
    private final int byteOffset;

    public SubstringRope(Encoding encoding, Rope child, boolean singleByteOptimizable, int offset, int byteLength, int characterLength, CodeRange codeRange) {
        // TODO (nirvdrum 07-Jan-16) Verify that this rope is only used for character substrings and not arbitrary byte slices. The former should always have the child's code range while the latter may not.
        this(child, encoding, singleByteOptimizable, offset, byteLength, characterLength, codeRange);
    }

    private SubstringRope(Rope child, Encoding encoding, boolean singleByteOptimizable, int byteOffset, int byteLength, int characterLength, CodeRange codeRange) {
        // TODO (nirvdrum 07-Jan-16) Verify that this rope is only used for character substrings and not arbitrary byte slices. The former should always have the child's code range while the latter may not.
        super(encoding, codeRange, singleByteOptimizable, byteLength, characterLength, child.depth() + 1, null);
        this.child = child;
        this.byteOffset = byteOffset;

        assert byteLength <= child.byteLength();
    }

    @Override
    public Rope withEncoding(Encoding newEncoding, CodeRange newCodeRange) {
        if (newCodeRange != getCodeRange()) {
            throw new UnsupportedOperationException("Cannot fast-path updating encoding with different code range.");
        }

        return new SubstringRope(getChild(), newEncoding, getChild().isSingleByteOptimizable(), getByteOffset(), byteLength(), characterLength(), newCodeRange);
    }

    @Override
    protected byte[] getBytesSlow() {
        if (child.getRawBytes() != null) {
            final byte[] ret = new byte[byteLength()];

            System.arraycopy(child.getRawBytes(), byteOffset, ret, 0, byteLength());

            return ret;
        }

        return RopeOperations.flattenBytes(this);
    }

    @Override
    public byte getByteSlow(int index) {
        return child.getByteSlow(index + byteOffset);
    }

    public Rope getChild() {
        return child;
    }

    public int getByteOffset() {
        return byteOffset;
    }

    @Override
    public String toString() {
        assert ALLOW_TO_STRING;
        return RopeOperations.decodeRopeSegment(child, byteOffset, byteLength());
    }

}
