/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class NotNode extends RubyContextSourceNode {

    @Child private BooleanCastNode child;

    public NotNode(RubyNode child) {
        this.child = BooleanCastNodeGen.create(child);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return !child.execute(frame);
    }

    private RubyNode getChildBeforeCasting() {
        return child.getValueNode();
    }

    public RubyNode cloneUninitialized() {
        var copy = new NotNode(getChildBeforeCasting().cloneUninitialized());
        return copy.copyFlags(this);
    }

}
