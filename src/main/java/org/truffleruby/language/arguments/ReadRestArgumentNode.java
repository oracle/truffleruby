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

import org.truffleruby.Layouts;
import org.truffleruby.core.array.ArrayAppendOneNode;
import org.truffleruby.core.array.ArrayStrategy;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class ReadRestArgumentNode extends RubyNode {

    private final int startIndex;
    private final int indexFromCount;
    private final boolean keywordArguments;

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
            int minimumForKWargs) {
        this.startIndex = startIndex;
        this.indexFromCount = indexFromCount;
        this.keywordArguments = keywordArguments;

        if (keywordArguments) {
            this.readUserKeywordsHashNode = new ReadUserKeywordsHashNode(minimumForKWargs);
        }

        this.hasKeywordsProfile = keywordArguments ? ConditionProfile.createBinaryProfile() : null;
        this.hasRejectedKwargs = keywordArguments ? ConditionProfile.createBinaryProfile() : null;
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
                resultStore = ArrayStrategy.NULL_ARRAY_STORE;
                resultLength = 0;
            } else {
                subsetOfArgumentsProfile.enter();
                final Object[] arguments = RubyArguments.getArguments(frame);
                resultStore = ArrayUtils.extractRange(arguments, startIndex, endIndex);
                resultLength = length;
            }
        }

        final DynamicObject rest = createArray(resultStore, resultLength);

        if (keywordArguments) {
            final DynamicObject kwargsHash = readUserKeywordsHashNode.execute(frame);

            if (hasKeywordsProfile.profile(kwargsHash != null)) {
                if (readRejectedKeywordArgumentsNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    readRejectedKeywordArgumentsNode = insert(new ReadRejectedKeywordArgumentsNode());
                }

                final DynamicObject rejectedKwargs = readRejectedKeywordArgumentsNode
                        .extractRejectedKwargs(frame, kwargsHash);

                if (hasRejectedKwargs.profile(Layouts.HASH.getSize(rejectedKwargs) > 0)) {
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
