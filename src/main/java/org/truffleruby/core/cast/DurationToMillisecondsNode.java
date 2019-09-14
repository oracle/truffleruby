/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

@NodeChild("duration")
public abstract class DurationToMillisecondsNode extends RubyNode {

    @Child NumericToFloatNode floatCastNode;

    private final ConditionProfile durationLessThanZeroProfile = ConditionProfile.createBinaryProfile();
    private final boolean acceptsNil;

    public DurationToMillisecondsNode(boolean acceptsNil) {
        this.acceptsNil = acceptsNil;
    }

    public abstract long executeDurationToMillis(VirtualFrame frame, Object duration);

    @Specialization
    protected long noDuration(NotProvided duration) {
        return Long.MAX_VALUE;
    }

    @Specialization
    protected long duration(int duration) {
        return validate(duration * 1000L);
    }

    @Specialization
    protected long duration(long duration) {
        return validate(duration * 1000);
    }

    @Specialization
    protected long duration(double duration) {
        return validate((long) (duration * 1000));
    }

    @Specialization(guards = "isNil(duration)")
    protected long durationNil(DynamicObject duration) {
        if (acceptsNil) {
            return noDuration(NotProvided.INSTANCE);
        } else {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeError("TypeError: can't convert NilClass into time interval", this));
        }
    }

    @Specialization(guards = "!isNil(duration)")
    protected long duration(VirtualFrame frame, DynamicObject duration) {
        if (floatCastNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            floatCastNode = insert(NumericToFloatNodeGen.create());
        }
        return duration(floatCastNode.executeDouble(frame, duration));
    }

    private long validate(long durationInMillis) {
        if (durationLessThanZeroProfile.profile(durationInMillis < 0)) {
            throw new RaiseException(getContext(), coreExceptions().argumentErrorTimeIntervalPositive(this));
        }
        return durationInMillis;
    }

}
