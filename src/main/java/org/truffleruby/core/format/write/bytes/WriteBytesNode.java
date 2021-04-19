/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
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

@NodeChild("value")
public abstract class WriteBytesNode extends FormatNode {

    @Specialization
    protected Object write(VirtualFrame frame, byte[] bytes) {
        writeBytes(frame, bytes);
        return null;
    }

    @Specialization
    protected Object writeRope(VirtualFrame frame, Rope rope,
            @Cached RopeNodes.BytesNode bytesNode) {
        writeBytes(frame, bytesNode.execute(rope));
        return null;
    }

}
