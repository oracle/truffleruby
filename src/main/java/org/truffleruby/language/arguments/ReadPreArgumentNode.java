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

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.NotProvided;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.language.arguments.keywords.EmptyKeywordDescriptor;
import org.truffleruby.language.arguments.keywords.KeywordDescriptor;
import org.truffleruby.language.arguments.keywords.NonEmptyKeywordDescriptor;
import org.truffleruby.language.arguments.keywords.ReadKeywordDescriptorNode;
import org.truffleruby.utils.Utils;

@NodeChild("descriptor")
public abstract class ReadPreArgumentNode extends RubyContextSourceNode {

    private final int index;

    private final BranchProfile outOfRangeProfile = BranchProfile.create();
    private final MissingArgumentBehavior missingArgumentBehavior;

    protected ReadPreArgumentNode(
            int index,
            MissingArgumentBehavior missingArgumentBehavior) {
        this.index = index;
        this.missingArgumentBehavior = missingArgumentBehavior;
    }

    public static ReadPreArgumentNode create(
            int index,
            MissingArgumentBehavior missingArgumentBehavior) {
        return ReadPreArgumentNodeGen.create(
                index,
                missingArgumentBehavior,
                new ReadKeywordDescriptorNode());
    }

    public abstract Object execute(VirtualFrame frame, KeywordDescriptor descriptor);

    @Specialization
    protected Object nonEmpty(VirtualFrame frame, NonEmptyKeywordDescriptor descriptor) {
        if (index < RubyArguments.getArgumentsCount(frame, descriptor)) {
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

    @Specialization
    protected Object empty(VirtualFrame frame, EmptyKeywordDescriptor descriptor) {
        if (index < RubyArguments.getArgumentsCount(frame, descriptor)) {
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + index;
    }

}
