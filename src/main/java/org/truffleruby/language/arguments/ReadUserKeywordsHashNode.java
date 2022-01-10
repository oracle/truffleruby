/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.dispatch.InternalRespondToNode;

public final class ReadUserKeywordsHashNode extends RubyBaseNode {

    private final int minArgumentCount;

    @Child private InternalRespondToNode respondToToHashNode;
    @Child private DispatchNode callToHashNode;

    private final ConditionProfile notEnoughArgumentsProfile = ConditionProfile.create();
    private final ConditionProfile lastArgumentIsHashProfile = ConditionProfile.create();
    private final ConditionProfile respondsToToHashProfile = ConditionProfile.create();
    private final ConditionProfile convertedIsHashProfile = ConditionProfile.create();

    public ReadUserKeywordsHashNode(int minArgumentCount) {
        this.minArgumentCount = minArgumentCount;
    }

    public RubyHash execute(VirtualFrame frame) {
        final int argumentCount = RubyArguments.getArgumentsCount(frame);

        if (notEnoughArgumentsProfile.profile(argumentCount <= minArgumentCount)) {
            return null;
        }

        final Object lastArgument = RubyArguments.getArgument(frame, argumentCount - 1);

        if (lastArgumentIsHashProfile.profile(RubyGuards.isRubyHash(lastArgument))) {
            return (RubyHash) lastArgument;
        } else {
            return tryConvertToHash(frame, argumentCount, lastArgument);
        }
    }

    private RubyHash tryConvertToHash(VirtualFrame frame, int argumentCount, Object lastArgument) {
        if (respondsToToHashProfile.profile(respondToToHash(frame, lastArgument))) {
            final Object converted = callToHash(frame, lastArgument);

            if (convertedIsHashProfile.profile(RubyGuards.isRubyHash(converted))) {
                RubyArguments.setArgument(frame, argumentCount - 1, converted);
                return (RubyHash) converted;
            }
        }

        return null;
    }

    private boolean respondToToHash(VirtualFrame frame, Object lastArgument) {
        if (respondToToHashNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            respondToToHashNode = insert(InternalRespondToNode.create());
        }
        return respondToToHashNode.execute(frame, lastArgument, "to_hash");
    }

    private Object callToHash(VirtualFrame frame, Object lastArgument) {
        if (callToHashNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            callToHashNode = insert(DispatchNode.create());
        }
        return callToHashNode.call(lastArgument, "to_hash");
    }

}
