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

import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;

import com.oracle.truffle.api.profiles.ConditionProfile;
import org.jcodings.Encoding;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public abstract class Rope implements Comparable<Rope> {

    public final Encoding encoding;
    private final int byteLength;
    private int hashCode = 0;
    protected byte[] bytes;

    protected Rope(Encoding encoding, int byteLength, byte[] bytes) {
        assert encoding != null;

        this.encoding = encoding;
        this.byteLength = byteLength;
        this.bytes = bytes;
    }

    /** Only used internally by WithEncodingNode. Returns a Rope with the given Encoding. Both the original and new
     * Encodings must be ASCII-compatible and the rope must be {@link #isAsciiOnly()}. */
    abstract Rope withEncoding7bit(Encoding newEncoding, ConditionProfile bytesNotNull);

    /** Only used internally by WithEncodingNode. Returns a Rope with the BINARY Encoding. The original Encoding must be
     * ASCII-compatible and {@link #getCodeRange()} must be {@link CodeRange#CR_VALID} to call this. */
    abstract Rope withBinaryEncoding(ConditionProfile bytesNotNull);

    public abstract int characterLength();

    public final int byteLength() {
        return byteLength;
    }

    public final boolean isEmpty() {
        return byteLength == 0;
    }

    protected abstract byte getByteSlow(int index);

    public final byte[] getRawBytes() {
        return bytes;
    }

    public abstract byte[] getBytes();

    /** The caller of this method will cache the resulting byte[]. */
    protected byte[] getBytesSlow() {
        return RopeOperations.flattenBytes(this);
    }

    public final byte[] getBytesCopy() {
        return getBytes().clone();
    }

    public final Encoding getEncoding() {
        return encoding;
    }

    public abstract CodeRange getCodeRange();

    public final boolean isSingleByteOptimizable() {
        return getCodeRange() == CodeRange.CR_7BIT || getEncoding().isSingleByte();
    }

    public final boolean isAsciiOnly() {
        return getCodeRange() == CodeRange.CR_7BIT;
    }

    @Override
    @TruffleBoundary
    public int hashCode() {
        if (!isHashCodeCalculated()) {
            hashCode = RopeOperations.hashForRange(this, 1, 0, byteLength);
        }

        return hashCode;
    }

    public final boolean isHashCodeCalculated() {
        return hashCode != 0;
    }

    public final int calculatedHashCode() {
        return hashCode;
    }

    @TruffleBoundary
    public boolean bytesEqual(Rope other) {
        /* What is the right strategy to compare ropes for byte equality? There are lots of options. We're going to
         * force and compare the hash codes, and then flatten for a byte equality. Both the intermediate hash
         * generations of the nodes, and the final Array.equals if needed, should have good inner-loop
         * implementations. */
        return this.hashCode() == other.hashCode() && Arrays.equals(this.getBytes(), other.getBytes());
    }

    @Override
    @TruffleBoundary
    public int compareTo(Rope other) {
        final byte[] selfBytes = getBytes();
        final byte[] otherBytes = other.getBytes();
        final int selfLen = selfBytes.length;
        final int otherLen = otherBytes.length;
        final int compareLen = Math.min(selfLen, otherLen);
        int i = 0;
        while (i < compareLen) {
            final byte selfByte = selfBytes[i];
            final byte otherByte = otherBytes[i];
            if (selfByte != otherByte) {
                return selfByte - otherByte;
            }
            i++;
        }
        return selfLen - otherLen;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof Rope) {
            final Rope other = (Rope) o;

            if (isHashCodeCalculated() && other.isHashCodeCalculated() && (hashCode != other.hashCode)) {
                return false;
            }

            return encoding == other.getEncoding() && byteLength() == other.byteLength() &&
                    Arrays.equals(getBytes(), other.getBytes());
        }

        return false;
    }

    public byte get(int index) {
        if (bytes != null) {
            return bytes[index];
        }

        return getByteSlow(index);
    }

    private static boolean isJavaDebuggerAttached() {
        final List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String arg : inputArguments) {
            if (arg.contains("jdwp")) {
                return true;
            }
        }
        return false;
    }

    static final boolean JAVA_DEBUGGER = isJavaDebuggerAttached();

    /** This is designed to not have any side effects - compare to {@link #getJavaString} - but this makes it
     * inefficient - for debugging only */
    @Override
    public String toString() {
        assert JAVA_DEBUGGER
                : "Rope#toString() should only be called by Java debuggers, use RubyStringLibrary or RopeOperations.decodeRope() instead";
        return RopeOperations.decode(encoding, RopeOperations.flattenBytes(this));
    }

    /** Should only be used by the parser - it has side effects */
    public final String getJavaString() {
        return RopeOperations.decodeRope(this);
    }

}
