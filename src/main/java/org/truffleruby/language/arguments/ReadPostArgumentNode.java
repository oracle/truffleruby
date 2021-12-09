/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.arguments.keywords.KeywordDescriptor;

public class ReadPostArgumentNode extends RubyContextSourceNode {

    private final int indexFromCount;
    private final int required;
    private final boolean acceptsKeywords;
    private final ConditionProfile enoughArguments = ConditionProfile.create();

    public ReadPostArgumentNode(int indexFromCount, int required, boolean acceptsKeywords) {
        this.indexFromCount = indexFromCount;
        this.required = required;
        this.acceptsKeywords = acceptsKeywords;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return execute(frame, RubyArguments.getKeywordArgumentsDescriptorUnsafe(frame), acceptsKeywords);
    }

    public Object execute(VirtualFrame frame, KeywordDescriptor descriptor, boolean acceptsKeywords) {
        int count = RubyArguments.getPositionalArgumentsCount(frame, descriptor, acceptsKeywords);

        if (enoughArguments.profile(count >= required)) {
            final int effectiveIndex = count - indexFromCount;
            return RubyArguments.getArgument(frame, effectiveIndex, descriptor);
        } else {
            // CheckArityNode will prevent this case for methods & lambdas, but it is still possible for procs.
            return nil;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " -" + indexFromCount;
    }

}
