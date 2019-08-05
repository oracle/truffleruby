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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.IsANode;

/**
 * Casts a value into a Ruby Float (double).
 */
public abstract class NumericToFloatNode extends RubyBaseNode {

    @Child private IsANode isANode = IsANode.create();
    @Child private CallDispatchHeadNode toFloatCallNode;

    public abstract double executeDouble(VirtualFrame frame, DynamicObject value);

    private Object callToFloat(DynamicObject value) {
        if (toFloatCallNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toFloatCallNode = insert(CallDispatchHeadNode.createPrivate());
        }
        return toFloatCallNode.call(value, "to_f");
    }

    @Specialization(guards = "isNumeric(frame, value)")
    protected double castNumeric(VirtualFrame frame, DynamicObject value,
            @Cached BranchProfile errorProfile) {
        final Object result = callToFloat(value);

        if (result instanceof Double) {
            return (double) result;
        } else {
            errorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().typeErrorCantConvertTo(value, "Float", "to_f", result, this));
        }
    }

    @Fallback
    protected double fallback(DynamicObject value) {
        throw new RaiseException(getContext(), coreExceptions().typeErrorCantConvertInto(value, "Float", this));
    }

    protected boolean isNumeric(VirtualFrame frame, Object value) {
        return isANode.executeIsA(value, coreLibrary().getNumericClass());
    }

}
