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

import com.oracle.truffle.api.CompilerAsserts;
import org.jcodings.Encoding;

public abstract class LeafRope extends Rope {

    private final CodeRange codeRange;
    private final int characterLength;

    public LeafRope(byte[] bytes, Encoding encoding, CodeRange codeRange, int characterLength) {
        super(encoding, bytes.length, bytes);

        this.codeRange = codeRange;
        this.characterLength = characterLength;

        assert !encoding.isSingleByte() || bytes.length == characterLength;
    }

    @Override
    public byte getByteSlow(int index) {
        return getRawBytes()[index];
    }

    @Override
    public final CodeRange getCodeRange() {
        return this.codeRange;
    }

    @Override
    public final int characterLength() {
        return characterLength;
    }

    @Override
    public final byte[] getBytes() {
        CompilerAsserts.neverPartOfCompilation("Use RopeNodes.ByteNodes instead, or add a @TruffleBoundary.");
        if (bytes == null) {
            bytes = getBytesSlow();
        }

        return bytes;
    }
}
