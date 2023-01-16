/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import com.oracle.truffle.api.profiles.CountingConditionProfile;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class OrNode extends RubyContextSourceNode {

    @Child private RubyNode left;
    @Child private RubyNode right;

    @Child private BooleanCastNode leftCast = BooleanCastNode.create();

    private final CountingConditionProfile conditionProfile = CountingConditionProfile.create();

    public OrNode(RubyNode left, RubyNode right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object leftValue = left.execute(frame);

        if (conditionProfile.profile(leftCast.execute(leftValue))) {
            return leftValue;
        } else {
            return right.execute(frame);
        }
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new OrNode(
                left.cloneUninitialized(),
                right.cloneUninitialized());
        return copy.copyFlags(this);
    }

}
