/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.control;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.language.RubyNode;

public class NotNode extends RubyNode {

    @Child private BooleanCastNode child;

    public NotNode(RubyNode child) {
        this.child = BooleanCastNodeGen.create(child);
    }

    @Override
    public boolean executeBoolean(VirtualFrame frame) {
        return !child.executeBoolean(frame);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return executeBoolean(frame);
    }

}
