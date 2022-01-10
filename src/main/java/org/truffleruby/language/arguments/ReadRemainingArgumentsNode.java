/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import org.truffleruby.language.RubyContextSourceNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class ReadRemainingArgumentsNode extends RubyContextSourceNode {

    private final int start;
    private final ConditionProfile remainingArguments = ConditionProfile.create();

    public ReadRemainingArgumentsNode(int start) {
        this.start = start;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final int count = RubyArguments.getArgumentsCount(frame);

        if (remainingArguments.profile(start < count)) {
            return RubyArguments.getArguments(frame, start);
        } else {
            return EMPTY_ARGUMENTS;
        }
    }

}
