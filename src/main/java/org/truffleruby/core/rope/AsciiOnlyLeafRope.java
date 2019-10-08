/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
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

public class AsciiOnlyLeafRope extends LeafRope {

    public AsciiOnlyLeafRope(byte[] bytes, Encoding encoding) {
        super(bytes, encoding, CodeRange.CR_7BIT, bytes.length);

        assert RopeOperations.isAsciiOnly(bytes, encoding) : "MBC string incorrectly marked as CR_7BIT";
    }

    @Override
    public Rope withEncoding(Encoding newEncoding, CodeRange newCodeRange) {
        if (newCodeRange != getCodeRange()) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw new UnsupportedOperationException("Cannot fast-path updating encoding with different code range.");
        }

        return new AsciiOnlyLeafRope(getRawBytes(), newEncoding);
    }
}
