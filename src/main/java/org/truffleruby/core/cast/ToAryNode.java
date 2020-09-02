/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.core.array.RubyArray;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;


@NodeChild(value = "child", type = RubyNode.class)
public abstract class ToAryNode extends RubyContextSourceNode {

    @Child private DispatchNode toAryNode;

    public static ToAryNode createInternal() {
        return ToAryNodeGen.create(null);
    }

    public abstract RubyArray executeToAry(Object object);

    @Specialization
    protected RubyArray coerceRubyArray(RubyArray array) {
        return array;
    }

    @Specialization(guards = "!isRubyArray(object)")
    protected RubyArray coerceObject(Object object,
            @Cached BranchProfile errorProfile) {
        if (toAryNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toAryNode = insert(DispatchNode.create());
        }

        final Object coerced;
        try {
            coerced = toAryNode.call(object, "to_ary");
        } catch (RaiseException e) {
            errorProfile.enter();
            if (e.getException().getLogicalClass() == coreLibrary().noMethodErrorClass) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeErrorNoImplicitConversion(object, "Array", this));
            } else {
                throw e;
            }
        }

        if (coerced instanceof RubyArray) {
            return (RubyArray) coerced;
        } else {
            errorProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorBadCoercion(object, "Array", "to_ary", coerced, this));
        }
    }
}
