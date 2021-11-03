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

import com.oracle.truffle.api.CompilerAsserts;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.ValueType;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;

import java.lang.invoke.VarHandle;

public class ConcatRope extends ManagedRope {

    /** Wrapper for the current state of the concat rope, including null children and a a byte array, or a null byte
     * array and the children. Accessing the state through {@link #getState()} avoids race conditions. */
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

        public boolean isFlattened() {
            return bytes != null;
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
        assert left != null;
        assert right != null;
        this.left = left;
        this.right = right;
    }

    @Override
    Rope withEncoding7bit(Encoding newEncoding, ConditionProfile bytesNotNull) {
        assert getCodeRange() == CodeRange.CR_7BIT;
        return withEncoding(newEncoding, CodeRange.CR_7BIT, characterLength(), bytesNotNull);
    }

    @Override
    Rope withBinaryEncoding(ConditionProfile bytesNotNull) {
        assert getCodeRange() == CodeRange.CR_VALID;
        return withEncoding(ASCIIEncoding.INSTANCE, CodeRange.CR_VALID, byteLength(), bytesNotNull);
    }

    private Rope withEncoding(Encoding encoding, CodeRange codeRange, int characterLength,
            ConditionProfile bytesNotNull) {
        final ConcatState state = getState(bytesNotNull);
        if (state.isFlattened()) {
            return RopeOperations.create(state.bytes, encoding, codeRange);
        } else {
            return new ConcatRope(state.left, state.right, encoding, codeRange, byteLength(), characterLength, null);
        }
    }

    @Override
    protected byte[] getBytesSlow() {
        flatten();
        return bytes;
    }

    private void flatten() {
        bytes = RopeOperations.flattenBytes(this);
        VarHandle.storeStoreFence();
        left = null;
        right = null;
    }

    /** Access the state in a way that prevents race conditions.
     *
     * <p>
     * This version is not allowed in compiled code, use {@link #getState(ConditionProfile)} there instead. */
    public ConcatState getState() {
        CompilerAsserts.neverPartOfCompilation("Use #getState(ConditionProfile) instead.");
        return getState(ConditionProfile.getUncached());
    }

    /** Access the state in a way that prevents race conditions.
     *
     * <p>
     * Outside compiled code, you can use {@link #getState()}. */
    public ConcatState getState(ConditionProfile bytesNotNull) {
        final ManagedRope left = this.left;
        final ManagedRope right = this.right;
        VarHandle.loadLoadFence();
        final byte[] bytes = this.bytes;
        if (bytesNotNull.profile(bytes != null)) {
            return new ConcatState(null, null, bytes);
        } else if (left != null && right != null) {
            return new ConcatState(left, right, null);
        } else {
            throw CompilerDirectives
                    .shouldNotReachHere("our assumptions about reordering and memory barriers seem incorrect");
        }
    }
}
