/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class IsNilNode extends RubyContextSourceNode {

    @Child private RubyNode child;

    public IsNilNode(RubyNode child) {
        this.child = child;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return child.execute(frame) == nil;
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new IsNilNode(child.cloneUninitialized());
        copy.copyFlags(this);
        return copy;
    }

}
