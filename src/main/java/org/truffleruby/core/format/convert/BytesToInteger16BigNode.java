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

import com.oracle.truffle.api.memory.ByteArraySupport;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.MissingValue;

import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.Nil;

@NodeChild("bytes")
public abstract class BytesToInteger16BigNode extends FormatNode {

    private final boolean signed;

    protected BytesToInteger16BigNode(boolean signed) {
        this.signed = signed;
    }

    @Specialization
    protected MissingValue decode(MissingValue missingValue) {
        return missingValue;
    }

    @Specialization
    protected Object decode(Nil nil) {
        return nil;
    }

    @Specialization
    protected int decode(byte[] bytes) { // must return int so Ruby nodes can deal with it
        short value = ByteArraySupport.bigEndian().getShort(bytes, 0);
        if (signed) {
            return value;
        } else {
            return Short.toUnsignedInt(value);
        }
    }

}
