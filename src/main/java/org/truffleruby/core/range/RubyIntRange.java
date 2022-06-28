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

import org.truffleruby.language.ImmutableRubyObject;

public final class RubyIntRange extends ImmutableRubyObject {

    public final int begin;
    public final int end;
    public final boolean excludedEnd;

    public RubyIntRange(boolean excludedEnd, int begin, int end) {
        this.excludedEnd = excludedEnd;
        this.begin = begin;
        this.end = end;

    }

}
