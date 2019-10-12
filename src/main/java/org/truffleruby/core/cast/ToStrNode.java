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
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@NodeChild(value = "child", type = RubyNode.class)
public abstract class ToStrNode extends RubyNode {

    public abstract DynamicObject executeToStr(VirtualFrame frame, Object object);

    public static ToStrNode create() {
        return ToStrNodeGen.create(null);
    }

    @Specialization(guards = "isRubyString(string)")
    protected DynamicObject coerceRubyString(DynamicObject string) {
        return string;
    }

    @Specialization(guards = "!isRubyString(object)")
    protected DynamicObject coerceObject(VirtualFrame frame, Object object,
            @Cached BranchProfile errorProfile,
            @Cached("createPrivate()") CallDispatchHeadNode toStrNode) {
        final Object coerced;
        try {
            coerced = toStrNode.call(object, "to_str");
        } catch (RaiseException e) {
            errorProfile.enter();
            if (Layouts.BASIC_OBJECT.getLogicalClass(e.getException()) == coreLibrary().getNoMethodErrorClass()) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeErrorNoImplicitConversion(object, "String", this));
            } else {
                throw e;
            }
        }

        if (RubyGuards.isRubyString(coerced)) {
            return (DynamicObject) coerced;
        } else {
            errorProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorBadCoercion(object, "String", "to_str", coerced, this));
        }
    }

}
