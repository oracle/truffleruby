/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

/** See {@link ToIntNode} for a comparison of different integer conversion nodes. */
@GenerateUncached
public abstract class LongCastNode extends RubyBaseNode {

    public static LongCastNode create() {
        return LongCastNodeGen.create();
    }

    public abstract long executeCastLong(Object value);

    @Specialization
    protected long doInt(int value) {
        return value;
    }

    @Specialization
    protected long doLong(long value) {
        return value;
    }

    @TruffleBoundary
    @Specialization(guards = "!isImplicitLong(value)")
    protected long doBasicObject(Object value) {
        throw new RaiseException(
                getContext(),
                coreExceptions().typeErrorIsNotA(value.toString(), "Integer (fitting in long)", this));
    }
}
