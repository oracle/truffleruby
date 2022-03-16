/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

public class ReadOptionalArgumentNode extends RubyContextSourceNode {

    private final int index;
    private final int minimum;
    private final boolean keywordArguments;
    @Child private RubyNode defaultValue;
    private final BranchProfile defaultValueProfile = BranchProfile.create();

    public ReadOptionalArgumentNode(
            int index,
            int minimum,
            boolean keywordArguments,
            RubyNode defaultValue) {
        this.index = index;
        this.minimum = minimum;
        this.keywordArguments = keywordArguments;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final int positionalArgumentsCount = RubyArguments.getPositionalArgumentsCount(frame, keywordArguments);

        if (positionalArgumentsCount >= minimum) {
            return RubyArguments.getArgument(frame, index);
        } else {
            defaultValueProfile.enter();
            return defaultValue.execute(frame);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + index;
    }

}
