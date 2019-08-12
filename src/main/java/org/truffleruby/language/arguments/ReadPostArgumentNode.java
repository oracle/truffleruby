/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ReadPostArgumentNode extends RubyNode {

    private final int indexFromCount;
    private final boolean keywordArguments;
    private final int minimumForKWargs;

    public ReadPostArgumentNode(int indexFromCount, boolean keywordArguments, int minimumForKWargs) {
        this.indexFromCount = indexFromCount;
        this.keywordArguments = keywordArguments;
        this.minimumForKWargs = minimumForKWargs;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        int count = RubyArguments.getArgumentsCount(frame);

        if (keywordArguments && count > minimumForKWargs) {
            final Object lastArgument = RubyArguments.getArgument(frame, count - 1);
            if (RubyGuards.isRubyHash(lastArgument)) {
                count--;
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
