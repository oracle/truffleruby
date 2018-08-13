/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;

@NodeChild(type = RubyNode.class)
public abstract class ToSNode extends RubyNode {

    @Child private KernelNodes.ToSNode kernelToSNode;

    protected DynamicObject kernelToS(Object object) {
        if (kernelToSNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            kernelToSNode = insert(KernelNodes.ToSNode.create());
        }
        return kernelToSNode.executeToS(object);
    }

    @Specialization(guards = "isRubyString(string)")
    public DynamicObject toS(DynamicObject string) {
        return string;
    }

    @Specialization(guards = "!isRubyString(object)")
    public DynamicObject toSFallback(VirtualFrame frame, Object object,
            @Cached("createPrivate()") CallDispatchHeadNode callToSNode) {
        final Object value = callToSNode.call(object, "to_s");

        if (RubyGuards.isRubyString(value)) {
            return (DynamicObject) value;
        } else {
            return kernelToS(object);
        }
    }
}
