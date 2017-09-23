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


import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

public class CheckSafeLevelNode extends RubyNode {

    @Child private CallDispatchHeadNode checkSafeLevelNode;
    @Child private RubyNode rhs;

    public CheckSafeLevelNode(RubyNode rhs) {
        this.rhs = rhs;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object value = rhs.execute(frame);
        if (checkSafeLevelNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            checkSafeLevelNode = insert(CallDispatchHeadNode.create());
        }
        return checkSafeLevelNode.call(frame, getContext().getCoreLibrary().getRubiniusTypeModule(), "check_safe_level", value);
    }

}
