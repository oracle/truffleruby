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

import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;


/** Convert objects to a String by calling #to_str, but leave existing Strings or Symbols as they are. */
@NodeChild(value = "child", type = RubyNode.class)
public abstract class ToStringOrSymbolNode extends RubyContextSourceNode {

    @Child private DispatchNode toStr;

    @Specialization
    protected RubySymbol coerceRubySymbol(RubySymbol symbol) {
        return symbol;
    }

    @Specialization
    protected RubyString coerceRubyString(RubyString string) {
        return string;
    }

    @Specialization(guards = { "!isRubySymbol(object)", "!isRubyString(object)" })
    protected RubyString coerceObject(VirtualFrame frame, Object object,
            @Cached BranchProfile errorProfile) {
        final Object coerced;
        try {
            coerced = callToStr(object);
        } catch (RaiseException e) {
            errorProfile.enter();
            if (e.getException().getLogicalClass() == coreLibrary().noMethodErrorClass) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().typeErrorNoImplicitConversion(object, "String", this));
            } else {
                throw e;
            }
        }

        if (coerced instanceof RubyString) {
            return (RubyString) coerced;
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
            toStr = insert(DispatchNode.create());
        }
        return toStr.call(object, "to_str");
    }
}
