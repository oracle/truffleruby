/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.regexp;

import java.util.Set;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

import org.joni.Region;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.ObjectGraphNode;

public class RubyMatchData extends RubyDynamicObject implements ObjectGraphNode {

    public DynamicObject regexp;
    public RubyString source;
    public Region region;
    public Region charOffsets;

    public RubyMatchData(Shape shape, DynamicObject regexp, RubyString source, Region region, Region charOffsets) {
        super(shape);
        this.regexp = regexp;
        this.source = source;
        this.region = region;
        this.charOffsets = charOffsets;
    }

    public void getAdjacentObjects(Set<Object> reachable) {
        if (regexp != null) {
            reachable.add(regexp);
        }
        if (source != null) {
            reachable.add(source);
        }
    }

}
