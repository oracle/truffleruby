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
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

/**
 * Convert objects to a String by calling #to_str, but leave existing Strings or Symbols as they are.
 */
@NodeChild(value = "child", type = RubyNode.class)
public abstract class ToStringOrSymbolNode extends RubyNode {

    @Child private CallDispatchHeadNode toStr;

    @Specialization(guards = "isRubySymbol(symbol)")
    protected DynamicObject coerceRubySymbol(DynamicObject symbol) {
        return symbol;
    }

    @Specialization(guards = "isRubyString(string)")
    protected DynamicObject coerceRubyString(DynamicObject string) {
        return string;
    }

    @Specialization(guards = { "!isRubySymbol(object)", "!isRubyString(object)" })
    protected DynamicObject coerceObject(VirtualFrame frame, Object object,
            @Cached BranchProfile errorProfile) {
        final Object coerced;
        try {
            coerced = callToStr(object);
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

    private Object callToStr(Object object) {
        if (toStr == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toStr = insert(CallDispatchHeadNode.createPrivate());
        }
        return toStr.call(object, "to_str");
    }
}
