/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.cast;

import org.truffleruby.Layouts;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

public abstract class ToFNode extends RubyBaseNode {

    @Child private CallDispatchHeadNode toFNode;

    public static ToFNode create() {
        return ToFNodeGen.create();
    }

    public abstract double executeToDouble(Object value);

    @Specialization
    protected double coerceInt(int value) {
        return value;
    }

    @Specialization
    protected double coerceLong(long value) {
        return value;
    }

    @Specialization
    protected double coerceDouble(double value) {
        return value;
    }

    @Specialization
    protected double coerceBoolean(boolean value,
            @Cached BranchProfile errorProfile) {
        return coerceObject(value, errorProfile);
    }

    @Specialization
    protected double coerceDynamicObject(DynamicObject object,
            @Cached BranchProfile errorProfile) {
        return coerceObject(object, errorProfile);
    }

    private double coerceObject(Object object, BranchProfile errorProfile) {
        if (toFNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toFNode = insert(CallDispatchHeadNode.createPrivate());
        }

        final Object coerced;
        try {
            coerced = toFNode.call(object, "to_f");
        } catch (RaiseException e) {
            if (Layouts.BASIC_OBJECT.getLogicalClass(e.getException()) == coreLibrary().getNoMethodErrorClass()) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().typeErrorNoImplicitConversion(object, "Float", this));
            } else {
                throw e;
            }
        }

        if (coreLibrary().getLogicalClass(coerced) == coreLibrary().getFloatClass()) {
            return (double) coerced;
        } else {
            errorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().typeErrorBadCoercion(object, "Float", "to_f", coerced, this));
        }
    }

}
