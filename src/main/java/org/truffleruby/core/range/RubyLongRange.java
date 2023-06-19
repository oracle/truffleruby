/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.range;

import org.truffleruby.core.string.StringUtils;

public final class RubyLongRange extends RubyIntOrLongRange {

    public final long begin;
    public final long end;

    public RubyLongRange(boolean excludedEnd, long begin, long end) {
        super(excludedEnd);
        this.begin = begin;
        this.end = end;
    }

    @Override
    public String toString() {
        String suffix = StringUtils.format("(begin = %s, end = %s, excludedEnd = %s)", begin, end, excludedEnd);
        return super.toString() + suffix;
    }

}
