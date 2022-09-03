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
import com.oracle.truffle.api.dsl.Fallback;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.dispatch.DispatchNode;

import java.util.concurrent.TimeUnit;

public abstract class DurationToNanoSecondsNode extends RubyBaseNode {

    private final ConditionProfile durationLessThanZeroProfile = ConditionProfile.create();

    public abstract long execute(Object duration);

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

    @Fallback
    protected long duration(Object duration,
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
