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

import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;

/** See {@link ToIntNode} for a comparison of different integer conversion nodes. */
@GenerateUncached
@ImportStatic(RubyGuards.class)
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
    protected int doLongToBig(long value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        throw new RaiseException(
                context,
                context.getCoreExceptions().rangeError("long too big to convert into `int'", this));
    }

    @Specialization(guards = "!isBasicInteger(value)")
    protected int doBasicObject(Object value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        throw new RaiseException(
                context,
                context.getCoreExceptions().typeErrorIsNotA(value.toString(), "Integer (fitting in int)", this));
    }
}
