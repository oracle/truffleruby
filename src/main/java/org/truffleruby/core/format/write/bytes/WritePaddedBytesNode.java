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

import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.printf.PrintfSimpleTreeBuilder;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.library.RubyStringLibrary;

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

    @Specialization(guards = "libString.isRubyString(string)")
    protected Object write(VirtualFrame frame, int padding, int precision, Object string,
            @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString,
            @Cached RopeNodes.BytesNode bytesNode,
            @Cached TruffleString.CodePointLengthNode codePointLengthNode,
            @Cached TruffleString.CodePointIndexToByteIndexNode codePointIndexToByteIndexNode) {
        if (padding == PrintfSimpleTreeBuilder.DEFAULT) {
            padding = 0;
        }

        var rope = libString.getRope(string);
        var tstring = libString.getTString(string);
        var encoding = libString.getEncoding(string);
        if (leftJustifiedProfile.profile(leftJustified || padding < 0)) {
            writeStringBytes(frame, precision, rope, tstring, encoding, bytesNode, codePointIndexToByteIndexNode);
            writePaddingBytes(frame, Math.abs(padding), precision, tstring, encoding, codePointLengthNode);
        } else {
            writePaddingBytes(frame, padding, precision, tstring, encoding, codePointLengthNode);
            writeStringBytes(frame, precision, rope, tstring, encoding, bytesNode, codePointIndexToByteIndexNode);
        }
        return null;
    }

    private void writeStringBytes(VirtualFrame frame, int precision, Rope rope,
            AbstractTruffleString tstring, RubyEncoding encoding, RopeNodes.BytesNode bytesNode,
            TruffleString.CodePointIndexToByteIndexNode codePointIndexToByteIndexNode) {
        byte[] bytes = bytesNode.execute(rope);
        int length;
        if (precisionProfile.profile(precision >= 0 && bytes.length > precision)) {
            int index = codePointIndexToByteIndexNode.execute(tstring, 0, precision, encoding.tencoding);
            if (index >= 0) {
                length = index;
            } else {
                length = bytes.length;
            }
        } else {
            length = bytes.length;
        }
        writeBytes(frame, bytes, 0, length);
    }

    private void writePaddingBytes(VirtualFrame frame, int padding, int precision, AbstractTruffleString tstring,
            RubyEncoding encoding,
            TruffleString.CodePointLengthNode codePointLengthNode) {
        if (paddingProfile.profile(padding > 0)) {
            int ropeLength = codePointLengthNode.execute(tstring, encoding.tencoding);
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
