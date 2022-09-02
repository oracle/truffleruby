/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.Cached;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.dispatch.DispatchNode;

import java.util.concurrent.TimeUnit;

@NodeChild(value = "duration", type = RubyBaseNodeWithExecute.class)
public abstract class DurationToNanoSecondsNode extends RubyBaseNodeWithExecute {

    private final ConditionProfile durationLessThanZeroProfile = ConditionProfile.create();
    private final boolean acceptsNil;

    public DurationToNanoSecondsNode(boolean acceptsNil) {
        this.acceptsNil = acceptsNil;
    }

    @Specialization
    protected long noDuration(NotProvided duration) {
        return Long.MAX_VALUE;
    }

    @Specialization
    protected long duration(long duration) {
        return validate(TimeUnit.SECONDS.toNanos(duration));
    }

    @Specialization
    protected long duration(double duration) {
        return validate((long) (duration * 1e9));
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
    protected Object duration(RubyDynamicObject duration,
            @Cached DispatchNode durationToNanoSeconds,
            @Cached ToLongNode toLongNode) {
        final Object nanoseconds = durationToNanoSeconds.call(
                coreLibrary().truffleKernelOperationsModule,
                "convert_duration_to_nanoseconds",
                duration);
        return validate(toLongNode.execute(nanoseconds));
    }

    private long validate(long durationInNanos) {
        if (durationLessThanZeroProfile.profile(durationInNanos < 0)) {
            throw new RaiseException(getContext(), coreExceptions().argumentErrorTimeIntervalPositive(this));
        }
        return durationInNanos;
    }

}
