/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.stdlib.bigdecimal;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.IsANode;

/**
 * This node implements the front part of the coercion logic in BigDecimal - the types it handles before going into
 * normal coercion logic. Also see calls to <code>#redo_coerced</code> in nodes in {@link BigDecimalNodes}.
 */
@NodeChild(value = "value", type = RubyNode.class)
public abstract class BigDecimalCoerceNode extends RubyNode {

    @Specialization
    public Object coerce(int value,
                         @Cached("createPublic()") CallDispatchHeadNode coerce) {
        return coerce.call(getContext().getCoreLibrary().getBigDecimalOperationsModule(), "coerce_integer_to_bigdecimal", value);
    }

    @Specialization(guards = "isRubyBignum(value)")
    public Object coerceBignum(DynamicObject value,
                         @Cached("createPublic()") CallDispatchHeadNode coerce) {
        return coerce.call(getContext().getCoreLibrary().getBigDecimalOperationsModule(), "coerce_integer_to_bigdecimal", value);
    }

    @Specialization
    public Object coerce(double value,
                         @Cached("createPublic()") CallDispatchHeadNode coerce) {
        return coerce.call(getContext().getCoreLibrary().getBigDecimalOperationsModule(), "coerce_float_to_bigdecimal", value);
    }

    @Specialization(guards = "isRubyRational(value)")
    public Object coerceRational(DynamicObject value,
                         @Cached("createPublic()") CallDispatchHeadNode coerce) {
        return coerce.call(getContext().getCoreLibrary().getBigDecimalOperationsModule(), "coerce_rational_to_bigdecimal", value);
    }

    @Fallback
    public Object coerce(Object value) {
        return value;
    }

    @Child private IsANode isANode;

    protected boolean isRubyRational(Object object) {
        if (isANode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            isANode = insert(IsANode.create());
        }

        return isANode.executeIsA(object, getContext().getCoreLibrary().getRationalClass());
    }

}
