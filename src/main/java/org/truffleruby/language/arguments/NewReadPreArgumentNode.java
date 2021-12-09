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
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.arguments.keywords.KeywordDescriptor;
import org.truffleruby.utils.Utils;

public class NewReadPreArgumentNode extends RubyContextSourceNode {

    public static final NewReadPreArgumentNode[] EMPTY_ARRAY = new NewReadPreArgumentNode[0];

    private final int index;

    private final BranchProfile outOfRangeProfile = BranchProfile.create();
    private final MissingArgumentBehavior missingArgumentBehavior;

    public NewReadPreArgumentNode(
            int index,
            MissingArgumentBehavior missingArgumentBehavior) {
        this.index = index;
        this.missingArgumentBehavior = missingArgumentBehavior;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (index < RubyArguments
                .getArgumentsCount(frame, RubyArguments.getKeywordArgumentsDescriptorUnsafe(frame))) {
            return RubyArguments.getArgument(frame, index);
        }

        outOfRangeProfile.enter();

        switch (missingArgumentBehavior) {
            case RUNTIME_ERROR:
                throw new IndexOutOfBoundsException();

            case NOT_PROVIDED:
                return NotProvided.INSTANCE;

            case NIL:
                return nil;

            default:
                throw Utils.unsupportedOperation("unknown missing argument behaviour");
        }
    }

    public Object execute(VirtualFrame frame, KeywordDescriptor descriptor, boolean acceptsKeywords) {
        if (index < RubyArguments.getPositionalArgumentsCount(frame, descriptor, acceptsKeywords)) {
            return RubyArguments.getArgument(frame, index, descriptor);
        }

        outOfRangeProfile.enter();

        switch (missingArgumentBehavior) {
            case RUNTIME_ERROR:
                throw new IndexOutOfBoundsException();

            case NOT_PROVIDED:
                return NotProvided.INSTANCE;

            case NIL:
                return nil;

            default:
                throw Utils.unsupportedOperation("unknown missing argument behaviour");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + index;
    }

}
