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

import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.Nil;

import com.oracle.truffle.api.object.Shape;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import java.util.Set;

public final class RubyObjectRange extends RubyRange implements ObjectGraphNode {

    public Object begin;
    public Object end;

    public RubyObjectRange(
            RubyClass rubyClass,
            Shape shape,
            boolean excludedEnd,
            Object begin,
            Object end,
            boolean frozen) {
        super(rubyClass, shape, excludedEnd, frozen);
        this.begin = begin;
        this.end = end;
    }

    public boolean isBoundless() {
        return begin == Nil.get() && end == Nil.get();
    }

    public boolean isEndless() {
        return begin != Nil.get() && end == Nil.get();
    }

    public boolean isBeginless() {
        return begin == Nil.get() && end != Nil.get();
    }

    public boolean isBounded() {
        return begin != Nil.get() && end != Nil.get();
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        ObjectGraph.addProperty(reachable, begin);
        ObjectGraph.addProperty(reachable, end);
    }
}
