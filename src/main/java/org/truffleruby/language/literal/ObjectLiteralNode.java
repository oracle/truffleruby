/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.literal;

import org.truffleruby.Layouts;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.NodeCost;
import com.oracle.truffle.api.nodes.NodeInfo;
import com.oracle.truffle.api.object.DynamicObject;

@NodeInfo(cost = NodeCost.NONE)
public class ObjectLiteralNode extends RubyNode {

    private final Object object;

    public ObjectLiteralNode(Object object) {
        assert objectInRopeTable(object);
        this.object = object;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return object;
    }

    public Object getObject() {
        return object;
    }

    private boolean objectInRopeTable(Object object) {
        final Rope rope;

        if (RubyGuards.isRubyString(object)) {
            rope = Layouts.STRING.getRope((DynamicObject) object);
        } else if (RubyGuards.isRubySymbol(object)) {
            rope = Layouts.SYMBOL.getRope((DynamicObject) object);
        } else {
            return true;
        }

        assert getContext().getRopeCache().contains(rope);

        return true;
    }

}
