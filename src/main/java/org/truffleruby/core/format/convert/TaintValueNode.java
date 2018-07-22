/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.convert;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.MissingValue;
import org.truffleruby.language.objects.TaintNode;

@NodeChildren({
        @NodeChild(value = "value", type = FormatNode.class),
})
public abstract class TaintValueNode extends FormatNode {

    @Specialization
    public MissingValue taint(MissingValue missingValue) {
        return missingValue;
    }

    @Specialization
    public Object taint(Object value,
                        @Cached("create()") TaintNode taintNode) {
        return taintNode.executeTaint(value);
    }

}
