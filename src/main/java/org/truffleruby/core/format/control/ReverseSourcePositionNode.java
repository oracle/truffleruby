/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.control;

import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.exceptions.OutsideOfStringException;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class ReverseSourcePositionNode extends FormatNode {

    private boolean star;

    private final ConditionProfile rangeProfile = ConditionProfile.create();

    public ReverseSourcePositionNode(boolean star) {
        this.star = star;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final int position = getSourcePosition(frame);

        if (star) {
            final int remaining = getSourceEnd(frame) - position;

            final int target = position - remaining;

            if (rangeProfile.profile(target < 0)) {
                throw new OutsideOfStringException();
            }

            setSourcePosition(frame, target);
        } else {
            if (rangeProfile.profile(position == 0)) {
                throw new OutsideOfStringException();
            }

            setSourcePosition(frame, position - 1);
        }

        return null;
    }

}
