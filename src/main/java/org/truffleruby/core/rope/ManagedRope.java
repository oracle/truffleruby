/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import org.jcodings.Encoding;

public abstract class ManagedRope extends Rope {

    private final CodeRange codeRange;

    protected ManagedRope(Encoding encoding, CodeRange codeRange, boolean singleByteOptimizable, int byteLength, int characterLength, int ropeDepth, byte[] bytes) {
        super(encoding, singleByteOptimizable, byteLength, characterLength, ropeDepth, bytes);

        this.codeRange = codeRange;
    }

    @Override
    public final CodeRange getCodeRange() {
        return this.codeRange;
    }

    @Override
    public final byte[] getBytes() {
        if (bytes == null) {
            bytes = getBytesSlow();
        }

        return bytes;
    }

    @Override
    public final String toString() {
        assert ALLOW_TO_STRING;

        final byte[] bytesBefore = bytes;
        final String string = RopeOperations.decodeOrEscapeBinaryRope(this, RopeOperations.flattenBytes(this));
        assert bytes == bytesBefore : "bytes should not be modified by Rope#toString() as otherwise inspecting a Rope would have a side effect";
        return string;
    }

}
