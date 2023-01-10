/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.RubyBaseNode;

/** An AssignableNode to represent the * in <code>* = 1, 2</code> */
public class NoopAssignableNode extends RubyBaseNode implements AssignableNode {
    @Override
    public void assign(VirtualFrame frame, Object value) {
        // The RHS is executed now, nothing else to do
    }

    @Override
    public AssignableNode toAssignableNode() {
        return this;
    }

    @Override
    public AssignableNode cloneUninitializedAssignable() {
        return new NoopAssignableNode();
    }

}
