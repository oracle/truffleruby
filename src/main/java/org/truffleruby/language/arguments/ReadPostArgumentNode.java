/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.frame.VirtualFrame;

import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;

public class ReadPostArgumentNode extends RubyNode {

    private final int indexFromCount;
    private final boolean keywordArguments;

    public ReadPostArgumentNode(int indexFromCount, boolean keywordArguments) {
        this.indexFromCount = indexFromCount;
        this.keywordArguments = keywordArguments;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        int count = RubyArguments.getArgumentsCount(frame);

        if (keywordArguments) {
            final Object lastArgument = RubyArguments.getArgument(frame, count - 1);
            if (!RubyGuards.isRubyHash(lastArgument)) {
                count++;
            }
        }

        final int effectiveIndex = count - indexFromCount;
        return RubyArguments.getArgument(frame, effectiveIndex);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " -" + indexFromCount;
    }

}
