/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.convert;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.MissingValue;

@NodeChild("value")
public abstract class ReinterpretLongAsDoubleNode extends FormatNode {

    @Specialization
    public MissingValue decode(MissingValue missingValue) {
        return missingValue;
    }

    @Specialization(guards = "isNil(nil)")
    public DynamicObject decode(DynamicObject nil) {
        return nil;
    }

    @Specialization
    public double decode(long value) {
        return Double.longBitsToDouble(value);
    }

}
