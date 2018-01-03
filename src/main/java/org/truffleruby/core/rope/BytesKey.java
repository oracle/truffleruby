/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.rope;

import org.jcodings.Encoding;
import org.truffleruby.core.Hashing;

import java.util.Arrays;

public class BytesKey {

    private final byte[] bytes;
    private final Encoding encoding;
    private int hashCode;

    public BytesKey(byte[] bytes, Encoding encoding, Hashing hashing) {
        this.bytes = bytes;
        this.encoding = encoding;
        this.hashCode = hashing.hash(Arrays.hashCode(bytes));
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BytesKey) {
            final BytesKey other = (BytesKey) o;
            return ((encoding == other.encoding) || (encoding == null)) && Arrays.equals(bytes, other.bytes);
        }

        return false;
    }

    @Override
    public String toString() {
        return RopeOperations.create(bytes, encoding, CodeRange.CR_UNKNOWN).toString();
    }

}
