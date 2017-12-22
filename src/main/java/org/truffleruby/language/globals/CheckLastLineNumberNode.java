/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.globals;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.language.RubyNode;

public class CheckLastLineNumberNode extends RubyNode {

    @Child private RubyNode child;
    @Child private ToIntNode toIntNode;

    public CheckLastLineNumberNode(RubyNode child) {
        this.child = child;
        this.toIntNode = ToIntNode.create();
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object childValue = child.execute(frame);

        return toIntNode.executeIntOrLong(frame, childValue);
    }
}
