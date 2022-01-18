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
import org.truffleruby.core.klass.RubyClass;

public final class RubyIntRange extends RubyRange {

    public final int begin;
    public final int end;

    public RubyIntRange(RubyClass rubyClass, Shape shape, boolean excludedEnd, int begin, int end, boolean frozen) {
        super(rubyClass, shape, excludedEnd, frozen);
        this.begin = begin;
        this.end = end;
    }

}
