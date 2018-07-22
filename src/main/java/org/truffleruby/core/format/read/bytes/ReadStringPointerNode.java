/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.read.bytes;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.jcodings.specific.USASCIIEncoding;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.read.SourceNode;
import org.truffleruby.core.format.write.bytes.EncodeUM;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.extra.ffi.Pointer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@NodeChildren({
        @NodeChild(value = "value", type = FormatNode.class),
})
public abstract class ReadStringPointerNode extends FormatNode {

    @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

    private final int limit;

    public ReadStringPointerNode(int limit) {
        this.limit = limit;
    }

    @Specialization
    public Object read(long address) {
        final Pointer pointer = new Pointer(address);
        final byte[] bytes = pointer.readZeroTerminatedByteArray(getContext(), 0, limit);
        return makeStringNode.executeMake(bytes, USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
    }

}
