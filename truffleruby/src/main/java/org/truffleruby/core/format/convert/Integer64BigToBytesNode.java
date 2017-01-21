/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.format.convert;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.core.format.FormatNode;

@NodeChildren({
        @NodeChild(value = "value", type = FormatNode.class),
})
public abstract class Integer64BigToBytesNode extends FormatNode {

    @Specialization
    public byte[] encode(long value) {
        if(value == 0x7fc00000 || value == 0x7ff8000000000000L){
            return new byte[]{
                (byte) 0x000000ff,
                (byte) 0x000000f8,
                (byte) 0x00000000,
                (byte) 0x00000000,
                (byte) 0x00000000,
                (byte) 0x00000000,
                (byte) 0x00000000,
                (byte) 0x00000000
            };
        }
        return new byte[]{
                (byte) (value >>> 56),
                (byte) (value >>> 48),
                (byte) (value >>> 40),
                (byte) (value >>> 32),
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value
        };
    }

}
