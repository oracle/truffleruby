/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;

public class ConcatRope extends ManagedRope {

    private final ManagedRope left;
    private final ManagedRope right;

    public ConcatRope(
            ManagedRope left,
            ManagedRope right,
            Encoding encoding,
            CodeRange codeRange) {
        this(left, right, encoding, codeRange, left.characterLength() + right.characterLength(), null);
    }

    private ConcatRope(
            ManagedRope left,
            ManagedRope right,
            Encoding encoding,
            CodeRange codeRange,
            int characterLength,
            byte[] bytes) {
        super(
                encoding,
                codeRange,
                left.byteLength() + right.byteLength(),
                characterLength,
                bytes);
        this.left = left;
        this.right = right;
    }

    @Override
    Rope withEncoding7bit(Encoding newEncoding) {
        assert getCodeRange() == CodeRange.CR_7BIT;
        return new ConcatRope(
                getLeft(),
                getRight(),
                newEncoding,
                CodeRange.CR_7BIT,
                characterLength(),
                getRawBytes());
    }

    @Override
    Rope withBinaryEncoding() {
        assert getCodeRange() == CodeRange.CR_VALID;
        return new ConcatRope(
                getLeft(),
                getRight(),
                ASCIIEncoding.INSTANCE,
                CodeRange.CR_VALID,
                byteLength(),
                getRawBytes());
    }

    public ManagedRope getLeft() {
        return left;
    }

    public ManagedRope getRight() {
        return right;
    }
}
