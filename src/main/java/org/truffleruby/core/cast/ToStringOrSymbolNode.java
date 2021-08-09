/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.language.library.RubyStringLibrary;

/** Convert objects to a String by calling #to_str, but leave existing Strings or Symbols as they are. */
@GenerateUncached
@NodeChild(value = "child", type = RubyBaseNodeWithExecute.class)
public abstract class ToStringOrSymbolNode extends RubyBaseNodeWithExecute {

    public static ToStringOrSymbolNode create() {
        return ToStringOrSymbolNodeGen.create(null);
    }

    /** Execute with child node */
    public abstract Object execute(VirtualFrame frame);

    /** Execute with given value */
    public abstract Object execute(Object value);

    @Specialization
    protected RubySymbol coerceRubySymbol(RubySymbol symbol) {
        return symbol;
    }

    @Specialization
    protected RubyString coerceRubyString(RubyString string) {
        return string;
    }

    @Specialization
    protected ImmutableRubyString coerceRubyString(ImmutableRubyString string) {
        return string;
    }

    @Specialization(guards = { "!isRubySymbol(object)", "isNotRubyString(object)" })
    protected Object coerceObject(Object object,
            @Cached DispatchNode toStr,
            @Cached BranchProfile errorProfile,
            @CachedLibrary(limit = "2") RubyStringLibrary libString) {
        final Object coerced;
        try {
            coerced = toStr.call(object, "to_str");
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

        if (libString.isRubyString(coerced)) {
            return coerced;
        } else {
            errorProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeErrorBadCoercion(object, "String", "to_str", coerced, this));
        }
    }

}
