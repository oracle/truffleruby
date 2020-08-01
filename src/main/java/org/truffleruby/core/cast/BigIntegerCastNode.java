/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.cast;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.CachedContext;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.RubyContext;
import org.truffleruby.RubyLanguage;
import org.truffleruby.core.exception.RubyException;
import org.truffleruby.core.numeric.BigIntegerOps;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubySourceNode;
import org.truffleruby.language.control.RaiseException;

import java.math.BigInteger;

/** Casts a value into a BigInteger. */
@GenerateUncached
@ImportStatic(RubyGuards.class)
@NodeChild(value = "value", type = RubyNode.class)
public abstract class BigIntegerCastNode extends RubySourceNode {

    public static BigIntegerCastNode create() {
        return BigIntegerCastNodeGen.create(null);
    }

    public static BigIntegerCastNode create(RubyNode value) {
        return BigIntegerCastNodeGen.create(value);
    }

    public abstract BigInteger executeCastBigInteger(Object value);

    @Specialization
    protected BigInteger doInt(int value) {
        return BigIntegerOps.valueOf(value);
    }

    @Specialization
    protected BigInteger doLong(long value) {
        return BigIntegerOps.valueOf(value);
    }

    @Specialization
    protected BigInteger doBignum(RubyBignum value) {
        return value.value;
    }

    @Specialization(guards = "!isRubyInteger(value)")
    protected BigInteger doBasicObject(Object value,
            @CachedContext(RubyLanguage.class) RubyContext context) {
        throw new RaiseException(context, notAnInteger(context, value));
    }

    @TruffleBoundary
    private RubyException notAnInteger(RubyContext context, Object object) {
        return context.getCoreExceptions().typeErrorIsNotA(
                object.toString(),
                "Integer",
                this);
    }

}
