/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.objects.shared;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Property;
import org.truffleruby.language.RubyBaseWithoutContextNode;

public abstract class ReadAndShareFieldNode extends RubyBaseWithoutContextNode {

    private final Property property;

    @Child private WriteBarrierNode writeBarrierNode;

    public static final ReadAndShareFieldNode[] EMPTY_ARRAY = new ReadAndShareFieldNode[]{};

    public ReadAndShareFieldNode(Property property, int depth) {
        this.property = property;
        this.writeBarrierNode = WriteBarrierNodeGen.create(depth);
    }

    public abstract void executeReadFieldAndShare(DynamicObject object);

    @Specialization
    protected void readFieldAndShare(DynamicObject object) {
        final Object value = property.get(object, true);
        writeBarrierNode.executeWriteBarrier(value);
    }

}
