/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.bigdecimal;

import java.math.BigDecimal;

import org.truffleruby.interop.messages.BigDecimalMessages;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;

public class RubyBigDecimal extends RubyDynamicObject {

    public final BigDecimal value;
    final BigDecimalType type;

    public RubyBigDecimal(Shape shape, BigDecimal value, BigDecimalType type) {
        super(shape);
        this.value = value;
        this.type = type;
    }

    @Override
    @ExportMessage
    public Class<?> dispatch() {
        return BigDecimalMessages.class;
    }

}
