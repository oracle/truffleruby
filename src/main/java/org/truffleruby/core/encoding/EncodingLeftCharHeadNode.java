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
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.language.RubyBaseNode;

public abstract class EncodingLeftCharHeadNode extends RubyBaseNode {

    public static EncodingLeftCharHeadNode create() {
        return EncodingLeftCharHeadNodeGen.create();
    }

    public abstract int execute(RubyEncoding enc, byte[] bytes, int p, int s, int end);

    @Specialization(guards = "enc.jcoding.isSingleByte()")
    protected int leftAdjustCharHeadSingleByte(RubyEncoding enc, byte[] bytes, int p, int s, int end) {
        // return offset directly (org.jcodings.SingleByteEncoding#leftAdjustCharHead)
        return s;
    }

    @Specialization(guards = { "!enc.jcoding.isSingleByte()", "enc.jcoding.isUTF8()" })
    protected int leftAdjustCharHeadUtf8(RubyEncoding enc, byte[] bytes, int p, int s, int end) {
        return UTF8Encoding.INSTANCE.leftAdjustCharHead(bytes, p, s, end);
    }

    @TruffleBoundary
    @Specialization(guards = { "!enc.jcoding.isSingleByte()", "!enc.jcoding.isUTF8()" })
    protected int leftAdjustCharHead(RubyEncoding enc, byte[] bytes, int p, int s, int end) {
        return enc.jcoding.leftAdjustCharHead(bytes, p, s, end);
    }

}
