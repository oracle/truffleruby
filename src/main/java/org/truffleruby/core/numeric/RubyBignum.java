/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.numeric;

import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.interop.messages.RubyBignumMessages;
import org.truffleruby.language.RubyDynamicObject;

import java.math.BigInteger;

public class RubyBignum extends RubyDynamicObject {

    public final BigInteger value;

    public RubyBignum(Shape shape, BigInteger value) {
        super(shape);
        this.value = value;
    }

    @Override
    @ExportMessage
    public Class<?> dispatch() {
        return RubyBignumMessages.class;
    }

}
