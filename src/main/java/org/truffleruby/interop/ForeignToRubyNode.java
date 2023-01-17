/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.dsl.Fallback;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
public abstract class ForeignToRubyNode extends RubyBaseNode {

    public abstract Object executeConvert(Object value);

    @Specialization
    protected int convertByte(byte value) {
        return value;
    }

    @Specialization
    protected int convertShort(short value) {
        return value;
    }

    @Specialization
    protected double convertFloat(float value) {
        return value;
    }

    @Fallback
    protected Object convert(Object value) {
        return value;
    }

}
