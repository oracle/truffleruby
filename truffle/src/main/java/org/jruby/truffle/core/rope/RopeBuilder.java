/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.core.rope;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jruby.truffle.collections.ByteArrayBuilder;

import static org.jruby.truffle.core.rope.CodeRange.CR_UNKNOWN;

public class RopeBuilder extends ByteArrayBuilder {

    private Encoding encoding = ASCIIEncoding.INSTANCE;

    public static RopeBuilder createRopeBuilder(int size) {
        final RopeBuilder byteList = new RopeBuilder();
        byteList.unsafeEnsureSpace(size);
        return byteList;
    }

    public static RopeBuilder createRopeBuilder(byte[] bytes, Encoding encoding) {
        final RopeBuilder byteList = new RopeBuilder();
        byteList.append(bytes);
        byteList.setEncoding(encoding);
        return byteList;
    }

    public static RopeBuilder createRopeBuilder(byte[] wrap) {
        final RopeBuilder byteList = new RopeBuilder();
        byteList.append(wrap);
        return byteList;
    }

    public static RopeBuilder createRopeBuilder(byte[] wrap, int index, int len) {
        final RopeBuilder byteList = new RopeBuilder();
        byteList.append(wrap, index, len);
        return byteList;
    }

    public static RopeBuilder createRopeBuilder(byte[] wrap, int index, int len, Encoding encoding) {
        final RopeBuilder byteList = new RopeBuilder();
        byteList.append(wrap, index, len);
        byteList.setEncoding(encoding);
        return byteList;
    }

    public static RopeBuilder createRopeBuilder(RopeBuilder wrap, int index, int len) {
        final RopeBuilder byteList = new RopeBuilder();
        if (index + len > wrap.getLength()) {
            // TODO S 17-Jan-16 fix this use beyond the known length
            byteList.append(wrap.getUnsafeBytes(), index, len);
        } else {
            byteList.append(wrap.getBytes(), index, len);
        }
        return byteList;
    }

    public Encoding getEncoding() {
        return encoding;
    }

    public void setEncoding(Encoding encoding) {
        this.encoding = encoding;
    }

    public void append(Rope other) {
        append(other.getBytes());
    }

    public Rope toRope() {
        return RopeOperations.create(getBytes(), encoding, CR_UNKNOWN);
    }

}
