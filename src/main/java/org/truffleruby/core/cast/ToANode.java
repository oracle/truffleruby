/*
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

// Casting of enumerable that is supposed to respond to the #to_a method to RubyArray
@NodeChild(value = "childNode", type = RubyBaseNodeWithExecute.class)
public abstract class ToANode extends RubyBaseNodeWithExecute {

    @NeverDefault
    public static ToANode create() {
        return ToANodeGen.create(null);
    }

    public static ToANode create(RubyBaseNodeWithExecute child) {
        return ToANodeGen.create(child);
    }

    public abstract RubyArray executeToA(Object object);

    abstract RubyBaseNodeWithExecute getChildNode();

    @Specialization
    protected RubyArray toA(RubyArray array) {
        return array;
    }

    @Specialization(guards = "!isRubyArray(enumerable)")
    protected RubyArray toA(Object enumerable,
            @Cached BranchProfile errorProfile,
            @Cached DispatchNode toANode) {
        final Object coerced;

        try {
            coerced = toANode.call(enumerable, "to_a");
        } catch (RaiseException e) {
            errorProfile.enter();
            if (e.getException().getLogicalClass() == coreLibrary().noMethodErrorClass) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeErrorCantConvertInto(enumerable, "Array", this));
            } else {
                throw e;
            }
        }

        if (!(coerced instanceof RubyArray)) {
            errorProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorCantConvertTo(enumerable, "Array", "to_a", coerced, this));
        }

        return (RubyArray) coerced;
    }

    @Override
    public RubyBaseNodeWithExecute cloneUninitialized() {
        return create(getChildNode().cloneUninitialized());
    }

}
