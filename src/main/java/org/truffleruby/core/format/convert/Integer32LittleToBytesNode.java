/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.convert;

import com.oracle.truffle.api.memory.ByteArraySupport;
import org.truffleruby.core.format.FormatNode;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class Integer32LittleToBytesNode extends FormatNode {

    @Specialization
    protected byte[] encode(long value) {
        byte[] bytes = new byte[4];
        ByteArraySupport.littleEndian().putInt(bytes, 0, (int) value);
        return bytes;
    }

}
