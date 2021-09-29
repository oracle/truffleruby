/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.convert;

import org.truffleruby.core.format.FormatNode;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.Nil;

@NodeChild("value")
public abstract class ReinterpretByteAsIntegerNode extends FormatNode {

    private final boolean signed;

    public ReinterpretByteAsIntegerNode(boolean signed) {
        this.signed = signed;
    }

    @Specialization
    protected Object decode(Nil nil) {
        return nil;
    }

    @Specialization
    protected int decode(byte value) {
        if (signed) {
            return value;
        } else {
            return value & 0xff;
        }
    }

}
