/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import java.util.Arrays;

import org.jcodings.Encoding;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.TStringUtils;

public class BytesKey {

    private final byte[] bytes;
    private final Encoding encoding;
    private final int bytesHashCode;

    public BytesKey(byte[] bytes, Encoding encoding) {
        this.bytes = bytes;
        this.encoding = encoding;
        this.bytesHashCode = Arrays.hashCode(bytes);
    }

    @Override
    public int hashCode() {
        return bytesHashCode;
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
        return TStringUtils.fromByteArray(bytes, Encodings.getBuiltInEncoding(encoding)).toString();
    }

}
