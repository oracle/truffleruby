/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.write.bytes;

import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.Nil;

@NodeChild("value")
public abstract class WriteBinaryStringNode extends FormatNode {

    private final boolean pad;
    private final boolean padOnNil;
    private final int width;
    private final byte padding;
    private final boolean takeAll;
    private final boolean appendNull;

    public WriteBinaryStringNode(
            boolean pad,
            boolean padOnNil,
            int width,
            byte padding,
            boolean takeAll,
            boolean appendNull) {
        this.pad = pad;
        this.padOnNil = padOnNil;
        this.width = width;
        this.padding = padding;
        this.takeAll = takeAll;
        this.appendNull = appendNull;
    }

    @Specialization
    protected Object write(VirtualFrame frame, Nil nil) {
        if (padOnNil) {
            for (int n = 0; n < width; n++) {
                writeByte(frame, padding);
            }
        } else if (appendNull) {
            writeByte(frame, (byte) 0);
        }

        return null;
    }

    @Specialization
    protected Object write(VirtualFrame frame, byte[] bytes) {
        final int lengthFromBytes;

        if (takeAll) {
            lengthFromBytes = bytes.length;
        } else {
            lengthFromBytes = Math.min(width, bytes.length);
        }

        if (pad) {
            final int lengthFromPadding = width - lengthFromBytes;

            writeBytes(frame, bytes, lengthFromBytes);

            for (int n = 0; n < lengthFromPadding; n++) {
                writeByte(frame, padding);
            }
        } else {
            writeBytes(frame, bytes, lengthFromBytes);
        }

        if (appendNull) {
            writeByte(frame, (byte) 0);
        }

        return null;
    }

    @Specialization
    protected Object write(VirtualFrame frame, Rope rope,
            @Cached RopeNodes.BytesNode bytesNode) {
        return write(frame, bytesNode.execute(rope));
    }

}
