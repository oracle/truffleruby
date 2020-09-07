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

import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.objects.IsANode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;


/** Casts a value into a Ruby Float (double). */
public abstract class NumericToFloatNode extends RubyContextNode {

    @Child private IsANode isANode = IsANode.create();
    @Child private DispatchNode toFloatCallNode;

    public abstract double executeDouble(RubyDynamicObject value);

    private Object callToFloat(RubyDynamicObject value) {
        if (toFloatCallNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toFloatCallNode = insert(DispatchNode.create());
        }
        return toFloatCallNode.call(value, "to_f");
    }

    @Specialization(guards = "isNumeric(value)")
    protected double castNumeric(RubyDynamicObject value,
            @Cached BranchProfile errorProfile) {
        final Object result = callToFloat(value);

        if (result instanceof Double) {
            return (double) result;
        } else {
            errorProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorCantConvertTo(value, "Float", "to_f", result, this));
        }
    }

    @Fallback
    protected double fallback(RubyDynamicObject value) {
        throw new RaiseException(getContext(), coreExceptions().typeErrorCantConvertInto(value, "Float", this));
    }

    protected boolean isNumeric(Object value) {
        return isANode.executeIsA(value, coreLibrary().numericClass);
    }

}
