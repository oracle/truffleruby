/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.range;

import com.oracle.truffle.api.object.Shape;
import org.truffleruby.interop.messages.RubyLongRangeMessages;

public final class RubyLongRange extends RubyRange {

    public final long begin;
    public final long end;

    public RubyLongRange(Shape shape, boolean excludedEnd, long begin, long end) {
        super(shape, excludedEnd);
        this.begin = begin;
        this.end = end;
    }

    @Override
    public Class<?> dispatch() {
        return RubyLongRangeMessages.class;
    }
}
