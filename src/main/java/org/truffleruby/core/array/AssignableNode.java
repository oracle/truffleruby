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

import com.oracle.truffle.api.nodes.NodeInterface;

import com.oracle.truffle.api.frame.VirtualFrame;

public interface AssignableNode extends NodeInterface {

    static final AssignableNode[] EMPTY_ARRAY = new AssignableNode[0];

    /** This should null out the valueNode field, as that node should not be executed */
    AssignableNode toAssignableNode();

    /** Execute the node and assign the given value */
    void assign(VirtualFrame frame, Object value);

    AssignableNode cloneUninitializedAssignable();

}
