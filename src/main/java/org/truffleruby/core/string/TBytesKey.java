/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import java.util.Arrays;
import java.util.Objects;

import com.oracle.truffle.api.strings.InternalByteArray;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;

public final class TBytesKey {

    private final byte[] bytes;
    private final int offset;
    private final int length;
    private RubyEncoding encoding;
    private final int bytesHashCode;

    public TBytesKey(
            byte[] bytes,
            int offset,
            int length,
            int bytesHashCode,
            RubyEncoding encoding) {
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
        this.bytesHashCode = bytesHashCode;
        this.encoding = encoding;
    }

    public TBytesKey(byte[] bytes, RubyEncoding encoding) {
        this(bytes, 0, bytes.length, Arrays.hashCode(bytes), encoding);
    }

    public TBytesKey(InternalByteArray byteArray, RubyEncoding encoding) {
        this(
                byteArray.getArray(),
                byteArray.getOffset(),
                byteArray.getLength(),
                hashCode(byteArray),
                encoding);
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
                if (equalBytes(this, other)) {
                    // For getMatchedEncoding()
                    this.encoding = Objects.requireNonNull(other.encoding);
                    return true;
                } else {
                    return false;
                }
            } else {
                return encoding == other.encoding && equalBytes(this, other);
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
        return TruffleString.fromByteArrayUncached(bytes, encoding, false).toString();
    }

    private static int hashCode(InternalByteArray byteArray) {
        return hashCode(byteArray.getArray(), byteArray.getOffset(), byteArray.getLength());
    }

    // A variant of <code>Arrays.hashCode</code> that allows for selecting a range within the array.
    private static int hashCode(byte[] bytes, int offset, int length) {
        if (bytes == null) {
            return 0;
        }

        int result = 1;
        for (int i = offset; i < offset + length; i++) {
            result = 31 * result + bytes[i];
        }

        return result;
    }

    private boolean equalBytes(TBytesKey a, TBytesKey b) {
        if (a.isPerfectFit() && b.isPerfectFit()) {
            return Arrays.equals(a.bytes, b.bytes);
        }

        return Arrays.equals(a.bytes, a.offset, a.offset + a.length, b.bytes, b.offset, b.offset + b.length);
    }

    private boolean isPerfectFit() {
        return offset == 0 && length == bytes.length;
    }

    public TBytesKey makeCacheable(boolean isImmutable) {
        if (isImmutable && isPerfectFit()) {
            return new TBytesKey(bytes, encoding);
        }

        var simplified = ArrayUtils.extractRange(this.bytes, this.offset, this.offset + this.length);
        return new TBytesKey(simplified, encoding);
    }

    public TBytesKey withNewEncoding(RubyEncoding encoding) {
        return new TBytesKey(bytes, offset, length, bytesHashCode, encoding);
    }

    public TruffleString toTruffleString() {
        return TStringUtils.fromByteArray(bytes, offset, length, encoding.tencoding);
    }

}
