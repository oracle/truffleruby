/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.utils.Utils;

/** See {@link ToIntNode} for a comparison of different integer conversion nodes. */
@GenerateUncached
public abstract class IntegerCastNode extends RubyBaseNode {

    public static IntegerCastNode create() {
        return IntegerCastNodeGen.create();
    }

    public abstract int executeCastInt(Object value);

    @Specialization
    protected int doInt(int value) {
        return value;
    }

    @Specialization(guards = "fitsInInteger(value)")
    protected int doLong(long value) {
        return (int) value;
    }

    @Specialization(guards = "!fitsInInteger(value)")
    protected int doLongToBig(long value) {
        throw new RaiseException(
                getContext(),
                coreExceptions().rangeError("long too big to convert into `int'", this));
    }

    @Specialization(guards = "!isImplicitLong(value)")
    protected int doBasicObject(Object value) {
        throw new RaiseException(
                getContext(),
                coreExceptions().typeErrorIsNotA(Utils.toString(value), "Integer (fitting in int)", this));
    }
}
