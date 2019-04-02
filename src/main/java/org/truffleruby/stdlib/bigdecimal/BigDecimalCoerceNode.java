/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.bigdecimal;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.language.RubyNode;

import java.math.BigDecimal;
import java.math.RoundingMode;

@NodeChild(value = "value", type = RubyNode.class)
@NodeChild(value = "roundingMode", type = RoundModeNode.class)
@NodeChild(value = "cast", type = BigDecimalCastNode.class, executeWith = {"value", "roundingMode"})
public abstract class BigDecimalCoerceNode extends RubyNode {

    @Child private CreateBigDecimalNode createBigDecimal;

    public static BigDecimalCoerceNode create(RubyNode value) {
        return BigDecimalCoerceNodeGen.create(value,
                RoundModeNodeFactory.create(),
                BigDecimalCastNodeGen.create(null, null));
    }

    protected DynamicObject createBigDecimal(Object value) {
        if (createBigDecimal == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            createBigDecimal = insert(CreateBigDecimalNodeFactory.create(null, null, null));
        }

        return createBigDecimal.executeCreate(value);
    }

    @Specialization
    public DynamicObject doBigDecimal(Object value, RoundingMode roundingMode, BigDecimal cast) {
        return createBigDecimal(cast);
    }

    @Specialization(guards = { "isRubyBigDecimal(value)", "isNil(cast)" })
    public Object doBigDecimal(DynamicObject value, RoundingMode roundingMode, DynamicObject cast) {
        return value;
    }

    @Specialization(guards = "!isRubyBigDecimal(value)")
    public Object notBigDecimal(Object value, RoundingMode roundingMode, DynamicObject cast) {
        return value;
    }

}
