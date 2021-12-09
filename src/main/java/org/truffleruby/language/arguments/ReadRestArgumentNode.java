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

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.arguments.keywords.EmptyKeywordDescriptor;
import org.truffleruby.language.arguments.keywords.KeywordDescriptor;
import org.truffleruby.language.arguments.keywords.NonEmptyKeywordDescriptor;
import org.truffleruby.language.arguments.keywords.ReadKeywordDescriptorNode;

@NodeChild("descriptor")
public abstract class ReadRestArgumentNode extends RubyContextSourceNode {

    private final int startIndex;
    private final int indexFromCount;
    private final boolean keywordArguments;
    private final boolean considerRejectedKWArgs;

    private final BranchProfile noArgumentsLeftProfile = BranchProfile.create();
    private final BranchProfile subsetOfArgumentsProfile = BranchProfile.create();

    protected ReadRestArgumentNode(
            int startIndex,
            int indexFromCount,
            boolean keywordArguments,
            boolean considerRejectedKWArgs) {
        this.startIndex = startIndex;
        this.indexFromCount = indexFromCount;
        this.keywordArguments = keywordArguments;
        this.considerRejectedKWArgs = considerRejectedKWArgs;
    }

    public static ReadRestArgumentNode create(
            int startIndex,
            int indexFromCount,
            boolean keywordArguments,
            boolean considerRejectedKWArgs,
            int minimumForKWargs) {
        return ReadRestArgumentNodeGen.create(
                startIndex,
                indexFromCount,
                keywordArguments,
                considerRejectedKWArgs,
                new ReadKeywordDescriptorNode());
    }

    public abstract Object execute(VirtualFrame frame, KeywordDescriptor descriptor);

    @Specialization
    protected Object nonEmpty(VirtualFrame frame, NonEmptyKeywordDescriptor descriptor) {
        return readRestArgument(frame, descriptor);
    }

    @Specialization
    protected Object empty(VirtualFrame frame, EmptyKeywordDescriptor descriptor) {
        return readRestArgument(frame, descriptor);
    }

    public Object readRestArgument(VirtualFrame frame, KeywordDescriptor descriptor) {
        int endIndex = RubyArguments.getPositionalArgumentsCount(frame, descriptor, keywordArguments) - indexFromCount;
        final int length = endIndex - startIndex;

        Object resultStore;
        int resultLength;
        if (startIndex == 0) {
            final Object[] arguments = RubyArguments.getArguments(frame, descriptor);
            resultStore = arguments;
            resultLength = length;
        } else {
            if (startIndex >= endIndex) {
                noArgumentsLeftProfile.enter();
                resultStore = ArrayStoreLibrary.INITIAL_STORE;
                resultLength = 0;
            } else {
                subsetOfArgumentsProfile.enter();
                final Object[] arguments = RubyArguments.getArguments(frame, descriptor);
                resultStore = ArrayUtils.extractRange(arguments, startIndex, endIndex);
                resultLength = length;
            }
        }

        return createArray(resultStore, resultLength);
    }
}
