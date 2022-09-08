/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ArrayPatternLengthCheckNode extends RubyContextSourceNode {
    @Child RubyNode currentValueToMatch;
    int patternLength;

    public ArrayPatternLengthCheckNode(int patternLength, RubyNode currentValueToMatch) {
        this.currentValueToMatch = currentValueToMatch;
        this.patternLength = patternLength;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        RubyArray matchArray = (RubyArray) currentValueToMatch.execute(frame);
        long aSize = matchArray.getArraySize();
        return aSize == patternLength;
    }

}
