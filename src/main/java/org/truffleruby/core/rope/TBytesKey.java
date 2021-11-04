/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import java.util.Arrays;
import java.util.Objects;

import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.core.encoding.RubyEncoding;

public class TBytesKey {

    private final byte[] bytes;
    private RubyEncoding encoding;
    private final int bytesHashCode;

    public TBytesKey(byte[] bytes, RubyEncoding encoding) {
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
        if (o instanceof TBytesKey) {
            final TBytesKey other = (TBytesKey) o;
            if (encoding == null) {
                if (Arrays.equals(bytes, other.bytes)) {
                    // For getMatchedEncoding()
                    this.encoding = Objects.requireNonNull(other.encoding);
                    return true;
                } else {
                    return false;
                }
            } else {
                return encoding == other.encoding && Arrays.equals(bytes, other.bytes);
            }
        }

        return false;
    }

    public RubyEncoding getMatchedEncoding() {
        return encoding;
    }

    @Override
    public String toString() {
        var encoding = this.encoding != null ? this.encoding.tencoding : TruffleString.Encoding.BYTES;
        return TruffleString.fromByteArrayUncached(bytes, encoding).toString();
    }

}
