/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.Layouts;
import org.truffleruby.core.numeric.FloatNodes;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@NodeChild(value = "child", type = RubyNode.class)
public abstract class ToRubyIntegerNode extends RubyContextSourceNode {

    public static ToRubyIntegerNode create() {
        return ToRubyIntegerNodeGen.create(null);
    }

    public abstract Object execute(Object object);

    @Specialization
    protected int coerceInt(int value) {
        return value;
    }

    @Specialization
    protected long coerceLong(long value) {
        return value;
    }

    @Specialization(guards = "isRubyBignum(value)")
    protected DynamicObject coerceRubyBignum(DynamicObject value) {
        return value;
    }

    @Specialization
    protected Object coerceDouble(double value,
            @Cached FloatNodes.ToINode toINode) {
        return toINode.executeToI(value);
    }

    @Specialization(guards = "!isRubyBignum(object)")
    protected Object coerceObject(Object object,
            @Cached CallDispatchHeadNode toIntNode,
            @Cached ToIntNode fitNode,
            @Cached BranchProfile errorProfile) {
        final Object coerced;
        try {
            coerced = toIntNode.call(object, "to_int");
        } catch (RaiseException e) {
            errorProfile.enter();
            if (Layouts.BASIC_OBJECT.getLogicalClass(e.getException()) == coreLibrary().noMethodErrorClass) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeErrorNoImplicitConversion(object, "Integer", this));
            } else {
                throw e;
            }
        }

        if (coreLibrary().getLogicalClass(coerced) != coreLibrary().integerClass) {
            errorProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorBadCoercion(object, "Integer", "to_int", coerced, this));
        }

        return coerced;
    }
}
