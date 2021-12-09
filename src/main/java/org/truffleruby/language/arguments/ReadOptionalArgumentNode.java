/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
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
import org.truffleruby.language.arguments.keywords.KeywordDescriptor;

public class ReadOptionalArgumentNode extends RubyContextSourceNode {

    private final int index;
    private final int minimum;
    private final boolean acceptsKeywords;

    @Child private RubyNode defaultValue;

    private final BranchProfile defaultValueProfile = BranchProfile.create();

    public ReadOptionalArgumentNode(
            int index,
            int minimum,
            boolean acceptsKeywords,
            RubyNode defaultValue) {
        this.index = index;
        this.minimum = minimum;
        this.acceptsKeywords = acceptsKeywords;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return execute(frame, RubyArguments.getKeywordArgumentsDescriptorUnsafe(frame));
    }

    public Object execute(VirtualFrame frame, KeywordDescriptor descriptor) {
        if (RubyArguments.getPositionalArgumentsCount(frame, descriptor, acceptsKeywords) >= minimum) {
            return RubyArguments.getArgument(frame, index, descriptor);
        }

        defaultValueProfile.enter();
        return defaultValue.execute(frame);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + index;
    }

}
