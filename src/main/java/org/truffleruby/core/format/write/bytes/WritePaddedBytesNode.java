/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.write.bytes;

import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.printf.PrintfSimpleTreeBuilder;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.StringNodes;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

/** Simply write bytes. */
@NodeChild("width")
@NodeChild("precision")
@NodeChild("value")
public abstract class WritePaddedBytesNode extends FormatNode {

    private final ConditionProfile leftJustifiedProfile = ConditionProfile.create();
    private final ConditionProfile paddingProfile = ConditionProfile.create();
    private final ConditionProfile precisionProfile = ConditionProfile.create();
    private final boolean leftJustified;

    public WritePaddedBytesNode(boolean leftJustified) {
        this.leftJustified = leftJustified;
    }

    @Specialization
    protected Object write(VirtualFrame frame, int padding, int precision, Rope rope,
            @Cached RopeNodes.BytesNode bytesNode,
            @Cached RopeNodes.CharacterLengthNode charLengthNode,
            @Cached StringNodes.ByteIndexFromCharIndexNode indexNode) {
        if (padding == PrintfSimpleTreeBuilder.DEFAULT) {
            padding = 0;
        }
        final byte[] bytes = bytesNode.execute(rope);
        if (leftJustifiedProfile.profile(leftJustified || padding < 0)) {
            writeStringBytes(frame, precision, rope, bytesNode, indexNode);
            writePaddingBytes(frame, Math.abs(padding), precision, rope, charLengthNode);
        } else {
            writePaddingBytes(frame, padding, precision, rope, charLengthNode);
            writeStringBytes(frame, precision, rope, bytesNode, indexNode);
        }
        return null;
    }

    private void writeStringBytes(VirtualFrame frame, int precision, Rope rope,
            RopeNodes.BytesNode bytesNode,
            StringNodes.ByteIndexFromCharIndexNode indexNode) {
        byte[] bytes = bytesNode.execute(rope);
        int length;
        if (precisionProfile.profile(precision >= 0 && bytes.length > precision)) {
            int index = indexNode.execute(rope, 0, precision);
            if (index >= 0) {
                length = index;
            } else {
                length = bytes.length;
            }
        } else {
            length = bytes.length;
        }
        writeBytes(frame, bytes, length);
    }

    private void writePaddingBytes(VirtualFrame frame, int padding, int precision, Rope rope,
            RopeNodes.CharacterLengthNode lengthNode) {
        if (paddingProfile.profile(padding > 0)) {
            int ropeLength = lengthNode.execute(rope);
            int padBytes;
            if (precision > 0 && ropeLength > precision) {
                padBytes = padding - precision;
            } else if (padding > 0 && padding > ropeLength) {
                padBytes = padding - ropeLength;
            } else {
                padBytes = 0;
            }

            for (int n = 0; n < padBytes; n++) {
                writeByte(frame, (byte) ' ');
            }
        }
    }
}
