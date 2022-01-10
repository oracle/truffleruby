/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.rope;

import com.oracle.truffle.api.profiles.ConditionProfile;
import org.jcodings.Encoding;

import com.oracle.truffle.api.CompilerDirectives;

public class InvalidLeafRope extends LeafRope {

    public InvalidLeafRope(byte[] bytes, Encoding encoding, int characterLength) {
        super(bytes, encoding, CodeRange.CR_BROKEN, characterLength);

        assert RopeOperations.isInvalid(bytes, encoding) : "valid string incorrectly marked as CR_BROKEN";
    }

    @Override
    Rope withEncoding7bit(Encoding newEncoding, ConditionProfile bytesNotNull) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw new UnsupportedOperationException("Must only be called for ASCII-only Strings");
    }

    @Override
    Rope withBinaryEncoding(ConditionProfile bytesNotNull) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw new UnsupportedOperationException("Must only be called for CR_VALID Strings");
    }
}
