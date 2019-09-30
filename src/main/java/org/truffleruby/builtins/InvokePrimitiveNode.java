/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.builtins;

import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class InvokePrimitiveNode extends RubyNode {

    @Child private RubyNode primitive;

    private final ConditionProfile primitiveSucceededCondition = ConditionProfile.createBinaryProfile();

    public InvokePrimitiveNode(RubyNode primitive) {
        this.primitive = primitive;
    }

    @Override
    public void doExecuteVoid(VirtualFrame frame) {
        primitive.execute(frame);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object value = primitive.execute(frame);
        assert value != null : primitive;

        if (primitiveSucceededCondition.profile(value != null)) {
            return value;
        } else {
            return nil();
        }
    }

}
