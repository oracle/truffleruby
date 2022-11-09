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

import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class ArrayPatternLengthCheckNode extends RubyContextSourceNode {
    @Child RubyNode currentValueToMatch;
    final int patternLength;
    final boolean hasRest;

    final ConditionProfile isArrayProfile = ConditionProfile.create();

    public ArrayPatternLengthCheckNode(int patternLength, RubyNode currentValueToMatch, boolean hasRest) {
        this.currentValueToMatch = currentValueToMatch;
        this.patternLength = patternLength;
        this.hasRest = hasRest;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        Object matchArray = currentValueToMatch.execute(frame);
        if (isArrayProfile.profile(matchArray instanceof RubyArray)) {
            long aSize = ((RubyArray) matchArray).getArraySize();
            if (hasRest) {
                return patternLength <= aSize;
            } else {
                return patternLength == aSize;
            }
        } else {
            return false;
        }
    }

    @Override
    public RubyNode cloneUninitialized() {
        return new ArrayPatternLengthCheckNode(patternLength, currentValueToMatch.cloneUninitialized(), hasRest)
                .copyFlags(this);
    }
}
