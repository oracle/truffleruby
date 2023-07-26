/*
 * Copyright (c) 2016, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.InlinedConditionProfile;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.dispatch.DispatchNode;

import java.util.concurrent.TimeUnit;

public abstract class DurationToNanoSecondsNode extends RubyBaseNode {

    public abstract long execute(Object duration);

    @Specialization
    protected long noDuration(NotProvided duration) {
        return Long.MAX_VALUE;
    }

    @Specialization
    protected long duration(long duration,
            @Cached @Shared InlinedConditionProfile durationLessThanZeroProfile) {
        return validate(this, TimeUnit.SECONDS.toNanos(duration), durationLessThanZeroProfile);
    }

    @Specialization
    protected long duration(double duration,
            @Cached @Shared InlinedConditionProfile durationLessThanZeroProfile) {
        return validate(this, (long) (duration * 1e9), durationLessThanZeroProfile);
    }

    @Fallback
    protected static long duration(Object duration,
            @Cached DispatchNode durationToNanoSeconds,
            @Cached @Shared InlinedConditionProfile durationLessThanZeroProfile,
            @Cached ToLongNode toLongNode,
            @Bind("this") Node node) {
        final Object nanoseconds = durationToNanoSeconds.call(
                coreLibrary(node).truffleKernelOperationsModule,
                "convert_duration_to_nanoseconds",
                duration);
        return validate(node, toLongNode.execute(node, nanoseconds), durationLessThanZeroProfile);
    }

    private static long validate(Node node, long durationInNanos, InlinedConditionProfile durationLessThanZeroProfile) {
        if (durationLessThanZeroProfile.profile(node, durationInNanos < 0)) {
            throw new RaiseException(getContext(node), coreExceptions(node).argumentErrorTimeIntervalPositive(node));
        }
        return durationInNanos;
    }
}
