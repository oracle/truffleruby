/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringNodesFactory.StringSubstringPrimitiveNodeFactory;

@NodeChild(value = "string", type = FormatNode.class)
public abstract class TruncateStringNode extends FormatNode {
    @Child private StringNodes.StringSubstringPrimitiveNode substringNode = StringSubstringPrimitiveNodeFactory
            .create(null);
    private final int size;

    public abstract Object execute(Object string);

    public TruncateStringNode(Integer size) {
        this.size = size;
    }

    @Specialization
    protected Object truncate(RubyString string) {
        return substringNode.execute(string, 0, size);
    }
}
