/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.encoding;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.RubyBaseNode;

/** Whether the position at byteOffset is the start of a character and not in the middle of a character */
public abstract class IsCharacterHeadNode extends RubyBaseNode {

    public static IsCharacterHeadNode create() {
        return IsCharacterHeadNodeGen.create();
    }

    public abstract boolean execute(RubyEncoding enc, byte[] bytes, int byteOffset, int end);

    @Specialization(guards = "enc.jcoding.isSingleByte()")
    protected boolean singleByte(RubyEncoding enc, byte[] bytes, int byteOffset, int end) {
        // return offset directly (org.jcodings.SingleByteEncoding#leftAdjustCharHead)
        return true;
    }

    @Specialization(guards = { "!enc.jcoding.isSingleByte()", "enc.jcoding.isUTF8()" })
    protected boolean utf8(RubyEncoding enc, byte[] bytes, int byteOffset, int end) {
        // based on org.jcodings.specific.BaseUTF8Encoding#leftAdjustCharHead
        return utf8IsLead(bytes[byteOffset] & 0xff);

    }

    @TruffleBoundary
    @Specialization(guards = { "!enc.jcoding.isSingleByte()", "!enc.jcoding.isUTF8()" })
    protected boolean other(RubyEncoding enc, byte[] bytes, int byteOffset, int end) {
        return enc.jcoding.leftAdjustCharHead(bytes, 0, byteOffset, end) == byteOffset;
    }

    /** Copied from org.jcodings.specific.BaseUTF8Encoding */
    private static boolean utf8IsLead(int c) {
        return ((c & 0xc0) & 0xff) != 0x80;
    }

}
