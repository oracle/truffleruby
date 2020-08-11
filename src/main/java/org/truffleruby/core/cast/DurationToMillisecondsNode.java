/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ConditionProfile;

@NodeChild(value = "duration", type = RubyNode.class)
public abstract class DurationToMillisecondsNode extends RubyContextSourceNode {

    @Child NumericToFloatNode floatCastNode;

    private final ConditionProfile durationLessThanZeroProfile = ConditionProfile.create();
    private final boolean acceptsNil;

    public DurationToMillisecondsNode(boolean acceptsNil) {
        this.acceptsNil = acceptsNil;
    }

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

    @Specialization
    protected long durationNil(Nil duration) {
        if (acceptsNil) {
            return noDuration(NotProvided.INSTANCE);
        } else {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeError("TypeError: can't convert NilClass into time interval", this));
        }
    }

    @Specialization
    protected long duration(RubyDynamicObject duration) {
        if (floatCastNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            floatCastNode = insert(NumericToFloatNodeGen.create());
        }
        return duration(floatCastNode.executeDouble(duration));
    }

    private long validate(long durationInMillis) {
        if (durationLessThanZeroProfile.profile(durationInMillis < 0)) {
            throw new RaiseException(getContext(), coreExceptions().argumentErrorTimeIntervalPositive(this));
        }
        return durationInMillis;
    }

}
