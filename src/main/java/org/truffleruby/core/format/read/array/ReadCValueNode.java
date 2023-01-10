/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.read.array;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

import org.truffleruby.cext.UnwrapNode;
import org.truffleruby.core.format.FormatNode;

@NodeChild(value = "source")
public abstract class ReadCValueNode extends FormatNode {

    @Specialization
    protected Object read(Object source,
            @Cached UnwrapNode unwrapNode) {
        return unwrapNode.execute(source);
    }
}
