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

public class InvalidLeafRope extends LeafRope {

    public InvalidLeafRope(byte[] bytes, Encoding encoding, int characterLength) {
        super(bytes, encoding, CodeRange.CR_BROKEN, characterLength);

        assert RopeOperations.isInvalid(bytes, encoding) : "valid string incorrectly marked as CR_BROKEN";
    }

    @Override
    public Rope withEncoding(Encoding newEncoding, CodeRange newCodeRange) {
        if (newCodeRange != getCodeRange()) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw new UnsupportedOperationException("Cannot fast-path updating encoding with different code range.");
        }

        final int newCharacterLength = RopeOperations.strLength(newEncoding, getRawBytes(), 0, byteLength());
        return new InvalidLeafRope(getRawBytes(), newEncoding, newCharacterLength);
    }
}
