/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import org.truffleruby.core.rope.Rope;

public abstract class StringCachingGuards {

    public static Rope privatizeRope(RubyString string) {
        // TODO (nirvdrum 25-Jan-16) Should we flatten the rope to avoid caching a potentially deep rope tree?
        return string.rope;
    }

}
