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

import org.truffleruby.core.array.ArrayAppendOneNode;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyGuards;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class ReadRestArgumentNode extends RubyContextSourceNode {

    private final int startIndex;
    private final int indexFromCount;
    private final boolean keywordArguments;
    private final boolean considerRejectedKWArgs;

    private final BranchProfile noArgumentsLeftProfile = BranchProfile.create();
    private final BranchProfile subsetOfArgumentsProfile = BranchProfile.create();
    private final ConditionProfile hasKeywordsProfile;
    private final ConditionProfile hasRejectedKwargs;

    @Child private ReadUserKeywordsHashNode readUserKeywordsHashNode;
    @Child private ReadRejectedKeywordArgumentsNode readRejectedKeywordArgumentsNode;
    @Child private ArrayAppendOneNode arrayAppendOneNode;

    public ReadRestArgumentNode(
            int startIndex,
            int indexFromCount,
            boolean keywordArguments,
            boolean considerRejectedKWArgs,
            int minimumForKWargs) {
        this.startIndex = startIndex;
        this.indexFromCount = indexFromCount;
        this.keywordArguments = keywordArguments;
        this.considerRejectedKWArgs = considerRejectedKWArgs;

        if (considerRejectedKWArgs) {
            this.readUserKeywordsHashNode = new ReadUserKeywordsHashNode(minimumForKWargs);
        }

        this.hasKeywordsProfile = considerRejectedKWArgs ? ConditionProfile.create() : null;
        this.hasRejectedKwargs = considerRejectedKWArgs ? ConditionProfile.create() : null;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        int endIndex = RubyArguments.getArgumentsCount(frame) - indexFromCount;

        if (keywordArguments) {
            final int argumentCount = RubyArguments.getArgumentsCount(frame);
            final Object lastArgument = argumentCount > 0 ? RubyArguments.getArgument(frame, argumentCount - 1) : null;

            if (RubyGuards.isRubyHash(lastArgument)) {
                endIndex -= 1;
            }
        }

        final int length = endIndex - startIndex;

        final Object resultStore;
        final int resultLength;

        if (startIndex == 0) {
            final Object[] arguments = RubyArguments.getArguments(frame);
            resultStore = arguments;
            resultLength = length;
        } else {
            if (startIndex >= endIndex) {
                noArgumentsLeftProfile.enter();
                resultStore = ArrayStoreLibrary.INITIAL_STORE;
                resultLength = 0;
            } else {
                subsetOfArgumentsProfile.enter();
                final Object[] arguments = RubyArguments.getArguments(frame);
                resultStore = ArrayUtils.extractRange(arguments, startIndex, endIndex);
                resultLength = length;
            }
        }

        final RubyArray rest = createArray(resultStore, resultLength);

        if (considerRejectedKWArgs) {
            final RubyHash kwargsHash = readUserKeywordsHashNode.execute(frame);

            if (hasKeywordsProfile.profile(kwargsHash != null)) {
                if (readRejectedKeywordArgumentsNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    readRejectedKeywordArgumentsNode = insert(new ReadRejectedKeywordArgumentsNode());
                }

                final RubyHash rejectedKwargs = readRejectedKeywordArgumentsNode.extractRejectedKwargs(kwargsHash);

                if (hasRejectedKwargs.profile(rejectedKwargs.size > 0)) {
                    if (arrayAppendOneNode == null) {
                        CompilerDirectives.transferToInterpreterAndInvalidate();
                        arrayAppendOneNode = insert(ArrayAppendOneNode.create());
                    }

                    arrayAppendOneNode.executeAppendOne(rest, rejectedKwargs);
                }
            }
        }

        return rest;
    }
}
