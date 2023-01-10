/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.RubyContextSourceNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.RubyNode;

public class ReadPostArgumentNode extends RubyContextSourceNode {

    private final int indexFromCount;
    private final boolean keywordArguments;
    private final int required;
    private final ConditionProfile enoughArguments = ConditionProfile.create();

    public ReadPostArgumentNode(int indexFromCount, boolean keywordArguments, int required) {
        this.indexFromCount = indexFromCount;
        this.keywordArguments = keywordArguments;
        this.required = required;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final int positionalArgumentsCount = RubyArguments.getPositionalArgumentsCount(frame, keywordArguments);

        if (enoughArguments.profile(positionalArgumentsCount >= required)) {
            final int effectiveIndex = positionalArgumentsCount - indexFromCount;
            return RubyArguments.getArgument(frame, effectiveIndex);
        } else {
            // CheckArityNode will prevent this case for methods & lambdas, but it is still possible for procs.
            return nil;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " -" + indexFromCount;
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new ReadPostArgumentNode(indexFromCount, keywordArguments, required);
        return copy.copyFlags(this);
    }

}
