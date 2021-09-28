/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.string.FrozenStrings;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.utils.RunTwiceBranchProfile;

/** OrLazyValueDefinedNode is used as the 'or' node for ||=, because we know from idiomatic Ruby usage that this is
 * often used to lazy initialize a value. In that case normal counting profiling gives a misleading result. With the RHS
 * having been executed once (the lazy initialization) it will be compiled expecting it to be used again. We know that
 * it's unlikely to be used again, so only compile it in when it's been used more than once, by using a
 * {@link RunTwiceBranchProfile}. */
public class OrLazyValueDefinedNode extends RubyContextSourceNode {

    @Child private RubyNode left;
    @Child private RubyNode right;

    @Child private BooleanCastNode leftCast = BooleanCastNode.create();

    private final RunTwiceBranchProfile rightTwiceProfile = new RunTwiceBranchProfile();
    private final ConditionProfile countingProfile = ConditionProfile.createCountingProfile();

    public OrLazyValueDefinedNode(RubyNode left, RubyNode right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object leftValue = left.execute(frame);

        if (countingProfile.profile(leftCast.executeToBoolean(leftValue))) {
            return leftValue;
        } else {
            rightTwiceProfile.enter();
            return right.execute(frame);
        }
    }

    @Override
    public Object isDefined(VirtualFrame frame, RubyLanguage language, RubyContext context) {
        return FrozenStrings.ASSIGNMENT;
    }
}
