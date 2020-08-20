/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.bigdecimal;

import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.objects.IsANode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

/** This node implements the front part of the coercion logic in BigDecimal - the types it handles before going into
 * normal coercion logic. Also see calls to <code>#redo_coerced</code> in nodes in {@link BigDecimalNodes}. */
@NodeChild(value = "value", type = RubyNode.class)
public abstract class BigDecimalCoerceNode extends RubyContextSourceNode {

    @Specialization
    protected Object coerce(int value,
            @Cached(parameters = "PUBLIC") DispatchNode coerce) {
        return coerce.call(
                getContext().getCoreLibrary().bigDecimalOperationsModule,
                "coerce_integer_to_bigdecimal",
                value);
    }

    @Specialization
    protected Object coerce(long value,
            @Cached(parameters = "PUBLIC") DispatchNode coerce) {
        return coerce.call(
                getContext().getCoreLibrary().bigDecimalOperationsModule,
                "coerce_integer_to_bigdecimal",
                value);
    }

    @Specialization
    protected Object coerceBignum(RubyBignum value,
            @Cached(parameters = "PUBLIC") DispatchNode coerce) {
        return coerce.call(
                getContext().getCoreLibrary().bigDecimalOperationsModule,
                "coerce_integer_to_bigdecimal",
                value);
    }

    @Specialization
    protected Object coerce(double value,
            @Cached(parameters = "PUBLIC") DispatchNode coerce) {
        return coerce.call(
                getContext().getCoreLibrary().bigDecimalOperationsModule,
                "coerce_float_to_bigdecimal",
                value);
    }

    @Specialization(guards = "isRubyRational(value)")
    protected Object coerceRational(RubyDynamicObject value,
            @Cached(parameters = "PUBLIC") DispatchNode coerce) {
        return coerce.call(
                getContext().getCoreLibrary().bigDecimalOperationsModule,
                "coerce_rational_to_bigdecimal",
                value);
    }

    @Fallback
    protected Object coerce(Object value) {
        return value;
    }

    @Child private IsANode isANode;

    protected boolean isRubyRational(Object object) {
        if (isANode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            isANode = insert(IsANode.create());
        }

        return isANode.executeIsA(object, getContext().getCoreLibrary().rationalClass);
    }

}
