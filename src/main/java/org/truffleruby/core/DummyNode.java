/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import com.oracle.truffle.api.nodes.DenyReplace;
import com.oracle.truffle.api.nodes.Node;

/** When a Node is needed but none is available */
@DenyReplace
public final class DummyNode extends Node {
    public static final Node INSTANCE = new DummyNode();

    private DummyNode() {
    }

    @Override
    public boolean isAdoptable() {
        return false;
    }
}
