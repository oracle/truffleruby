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

import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class ReadOptionalArgumentNode extends RubyContextSourceNode {

    private final int index;
    private final int minimum;
    private final boolean considerRejectedKWArgs;
    private final boolean reduceMinimumWhenNoKWargs;

    @Child private RubyNode defaultValue;
    @Child private ReadUserKeywordsHashNode readUserKeywordsHashNode;
    @Child private ReadRejectedKeywordArgumentsNode readRejectedKeywordArgumentsNode;

    private final BranchProfile defaultValueProfile = BranchProfile.create();
    private final ConditionProfile hasKeywordsProfile;
    private final ConditionProfile hasRejectedKwargs;

    public ReadOptionalArgumentNode(
            int index,
            int minimum,
            boolean considerRejectedKWArgs,
            boolean reduceMinimumWhenNoKWargs,
            int requiredForKWArgs,
            RubyNode defaultValue) {
        this.index = index;
        this.minimum = minimum;
        this.considerRejectedKWArgs = considerRejectedKWArgs;
        this.defaultValue = defaultValue;
        this.reduceMinimumWhenNoKWargs = reduceMinimumWhenNoKWargs;

        if (reduceMinimumWhenNoKWargs || considerRejectedKWArgs) {
            this.readUserKeywordsHashNode = new ReadUserKeywordsHashNode(requiredForKWArgs);
        }

        this.hasKeywordsProfile = considerRejectedKWArgs ? ConditionProfile.create() : null;
        this.hasRejectedKwargs = considerRejectedKWArgs ? ConditionProfile.create() : null;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        int effectiveMinimum = minimum;

        if (reduceMinimumWhenNoKWargs) {
            if (readUserKeywordsHashNode.execute(frame) == null) {
                effectiveMinimum--;
            }
        }

        if (RubyArguments.getArgumentsCount(frame) >= effectiveMinimum) {
            return RubyArguments.getArgument(frame, index);
        }

        defaultValueProfile.enter();

        if (considerRejectedKWArgs) {
            final RubyHash kwargsHash = readUserKeywordsHashNode.execute(frame);

            if (hasKeywordsProfile.profile(kwargsHash != null)) {
                if (readRejectedKeywordArgumentsNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    readRejectedKeywordArgumentsNode = insert(new ReadRejectedKeywordArgumentsNode());
                }

                final RubyHash rejectedKwargs = readRejectedKeywordArgumentsNode.extractRejectedKwargs(kwargsHash);
                if (hasRejectedKwargs.profile(rejectedKwargs.size > 0)) {
                    return rejectedKwargs;
                }
            }
        }

        return defaultValue.execute(frame);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + index;
    }

}
