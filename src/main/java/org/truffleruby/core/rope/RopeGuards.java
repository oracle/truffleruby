/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
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

public class RopeGuards {

    public static boolean isSingleByteString(Rope rope) {
        return rope.byteLength() == 1;
    }

    public static boolean isLeafRope(Rope rope) {
        return rope instanceof LeafRope;
    }

    public static boolean isEmpty(byte[] bytes) {
        return bytes.length == 0;
    }

    public static boolean isBinaryString(Encoding encoding) {
        return encoding == ASCIIEncoding.INSTANCE;
    }

    public static boolean isAsciiCompatible(Encoding encoding) {
        return encoding.isAsciiCompatible();
    }

    public static boolean isFixedWidthEncoding(Rope rope) {
        return rope.getEncoding().isFixedWidth();
    }

    public static boolean is7Bit(Rope rope, RopeNodes.CodeRangeNode codeRangeNode) {
        return codeRangeNode.execute(rope) == CodeRange.CR_7BIT;
    }

    public static boolean isBroken(Rope rope, RopeNodes.CodeRangeNode codeRangeNode) {
        return codeRangeNode.execute(rope) == CodeRange.CR_BROKEN;
    }

    public static boolean isAsciiCompatible(Rope rope) {
        return rope.getEncoding().isAsciiCompatible();
    }

}
