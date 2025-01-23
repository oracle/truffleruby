/*
 * Copyright (c) 2015, 2025 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.convert;

import com.oracle.truffle.api.CompilerDirectives;
import org.truffleruby.core.format.FormatNode;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

@NodeChild("value")
public abstract class ReinterpretAsLongNode extends FormatNode {

    private final int bits;

    protected ReinterpretAsLongNode(int bits) {
        this.bits = bits;
    }

    @Specialization
    long asLong(double object) {
        if (bits == 32) {
            return Float.floatToRawIntBits((float) object);
        } else if (bits == 64) {
            return Double.doubleToRawLongBits(object);
        } else {
            throw CompilerDirectives.shouldNotReachHere();
        }
    }

}
