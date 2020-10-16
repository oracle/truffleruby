/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.rope;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;

/** A RepeatingRope always has the same encoding as its child */
public class RepeatingRope extends ManagedRope {

    private final ManagedRope child;
    private final int times;

    public RepeatingRope(ManagedRope child, int times, int byteLength) {
        super(
                child.getEncoding(),
                child.getCodeRange(),
                byteLength,
                child.characterLength() * times,
                null);
        this.child = child;
        this.times = times;
    }

    @Override
    Rope withEncoding7bit(Encoding newEncoding) {
        assert getCodeRange() == CodeRange.CR_7BIT;
        return new RepeatingRope((ManagedRope) RopeOperations.withEncoding(child, newEncoding), times, byteLength());
    }

    @Override
    Rope withBinaryEncoding() {
        assert getCodeRange() == CodeRange.CR_VALID;
        return new RepeatingRope(
                (ManagedRope) RopeOperations.withEncoding(child, ASCIIEncoding.INSTANCE),
                times,
                byteLength());
    }

    @Override
    protected byte[] getBytesSlow() {
        if (child.getRawBytes() != null) {
            final byte[] childBytes = child.getRawBytes();
            int len = childBytes.length * times;
            final byte[] ret = new byte[len];

            int n = childBytes.length;

            System.arraycopy(childBytes, 0, ret, 0, n);
            while (n <= len / 2) {
                System.arraycopy(ret, 0, ret, n, n);
                n *= 2;
            }
            System.arraycopy(ret, 0, ret, n, len - n);

            return ret;
        }

        return super.getBytesSlow();
    }

    public ManagedRope getChild() {
        return child;
    }

    public int getTimes() {
        return times;
    }

}
