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

import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

/**
 * OrLazyValueDefinedNode is used as the 'or' node for ||=, because we know from idiomatic Ruby usage that this is
 * often used to lazy initialize a value. In that case normal counting profiling gives a misleading result. With the
 * RHS having been executed once (the lazy initialization) it will be compiled expecting it to be used again. We know
 * that it's unlikely to be used again, so only compile it in when it's been used more than once, by using a small
 * saturating counter.
 */
public class OrLazyValueDefinedNode extends RubyNode {

    @Child private RubyNode left;
    @Child private RubyNode right;

    @Child private BooleanCastNode leftCast;

    private enum RightUsage {
        NEVER,
        ONCE,
        MANY;

        public RightUsage next() {
            if (this == NEVER) {
                return ONCE;
            } else {
                return MANY;
            }
        }

    }

    @CompilationFinal private RightUsage rightUsage = RightUsage.NEVER;

    private final ConditionProfile conditionProfile = ConditionProfile.createCountingProfile();

    public OrLazyValueDefinedNode(RubyNode left, RubyNode right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final Object leftValue = left.execute(frame);

        if (conditionProfile.profile(castToBoolean(leftValue))) {
            return leftValue;
        } else {
            if (CompilerDirectives.inInterpreter()) {
                // Count how many times the RHS is used
                rightUsage = rightUsage.next();
            }

            if (rightUsage != RightUsage.MANY) {
                // Don't compile the RHS of a lazy initialization unless it has been used many times
                CompilerDirectives.transferToInterpreterAndInvalidate();
            }

            return right.execute(frame);
        }
    }

    private boolean castToBoolean(final Object value) {
        if (leftCast == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            leftCast = insert(BooleanCastNodeGen.create(null));
        }
        return leftCast.executeToBoolean(value);
    }

}
