/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.GenerateUncached;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.language.library.RubyStringLibrary;

@GenerateUncached
@NodeChild(value = "childNode", type = RubyBaseNodeWithExecute.class)
public abstract class ToStrNode extends RubyBaseNodeWithExecute {

    public static ToStrNode create() {
        return ToStrNodeGen.create(null);
    }

    public static ToStrNode create(RubyBaseNodeWithExecute child) {
        return ToStrNodeGen.create(child);
    }

    public abstract Object execute(Object object);

    abstract RubyBaseNodeWithExecute getChildNode();

    @Specialization
    protected RubyString coerceRubyString(RubyString string) {
        return string;
    }

    @Specialization
    protected ImmutableRubyString coerceImmutableRubyString(ImmutableRubyString string) {
        return string;
    }

    @Specialization(guards = "isNotRubyString(object)")
    protected Object coerceObject(Object object,
            @Cached BranchProfile errorProfile,
            @Cached DispatchNode toStrNode,
            @Cached RubyStringLibrary libString) {
        final Object coerced;
        try {
            coerced = toStrNode.call(object, "to_str");
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

    @Override
    public RubyBaseNodeWithExecute cloneUninitialized() {
        return create(getChildNode().cloneUninitialized());
    }
}
