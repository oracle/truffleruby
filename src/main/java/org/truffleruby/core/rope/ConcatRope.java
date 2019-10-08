/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import org.jcodings.Encoding;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class ConcatRope extends ManagedRope {

    private final ManagedRope left;
    private final ManagedRope right;
    private final boolean balanced;

    public ConcatRope(
            ManagedRope left,
            ManagedRope right,
            Encoding encoding,
            CodeRange codeRange,
            int depth,
            boolean balanced) {
        this(left, right, encoding, codeRange, depth, null, balanced);
    }

    private ConcatRope(
            ManagedRope left,
            ManagedRope right,
            Encoding encoding,
            CodeRange codeRange,
            int depth,
            byte[] bytes,
            boolean balanced) {
        super(
                encoding,
                codeRange,
                left.byteLength() + right.byteLength(),
                left.characterLength() + right.characterLength(),
                depth,
                bytes);
        this.left = left;
        this.right = right;
        this.balanced = balanced;
    }

    @Override
    public Rope withEncoding(Encoding newEncoding, CodeRange newCodeRange) {
        if (newCodeRange != getCodeRange()) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw new UnsupportedOperationException("Cannot fast-path updating encoding with different code range.");
        }

        return new ConcatRope(getLeft(), getRight(), newEncoding, newCodeRange, depth(), getRawBytes(), balanced);
    }

    @Override
    @TruffleBoundary
    public byte getByteSlow(int index) {
        if (index < left.byteLength()) {
            return left.getByteSlow(index);
        }

        return right.getByteSlow(index - left.byteLength());
    }

    public ManagedRope getLeft() {
        return left;
    }

    public ManagedRope getRight() {
        return right;
    }

    public boolean isBalanced() {
        return balanced;
    }

}
