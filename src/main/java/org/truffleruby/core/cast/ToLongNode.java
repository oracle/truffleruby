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

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNodeWithExecute;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.utils.Utils;

/** See {@link ToIntNode} for a comparison of different integer conversion nodes. */
@GenerateUncached
@NodeChild(value = "child", type = RubyBaseNodeWithExecute.class)
public abstract class ToLongNode extends RubyBaseNodeWithExecute {

    public static ToLongNode create() {
        return ToLongNodeGen.create(null);
    }

    public static ToLongNode create(RubyBaseNodeWithExecute child) {
        return ToLongNodeGen.create(child);
    }

    public abstract long execute(Object object);

    @Specialization
    protected long coerceInt(int value) {
        return value;
    }

    @Specialization
    protected long coerceLong(long value) {
        return value;
    }

    @Specialization
    protected long coerceRubyBignum(RubyBignum value) {
        throw new RaiseException(
                getContext(),
                coreExceptions().rangeError("bignum too big to convert into `long'", this));
    }

    @Specialization
    protected long coerceDouble(double value,
            @Cached BranchProfile errorProfile) {
        // emulate MRI logic
        // We check for `value < MAX_VALUE` because casting Long.MAX_VALUE to double yields a double value of 2^63 which is >
        // Long.MAX_VALUE.
        if (Long.MIN_VALUE <= value && value < Long.MAX_VALUE) {
            return (long) value;
        } else {
            errorProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().rangeError(Utils.concat("float ", value, " out of range of integer"), this));
        }
    }

    @Specialization
    protected long coerceNil(Nil nil) {
        // MRI hardcodes this specific error message, which is slightly different from the one we would get in the
        // catch-all case.
        throw new RaiseException(
                getContext(),
                coreExceptions().typeError("no implicit conversion from nil to integer", this));
    }

    @Specialization(guards = { "!isRubyInteger(object)", "!isImplicitDouble(object)", "!isNil(object)" })
    protected long coerceObject(Object object,
            @Cached DispatchNode toIntNode,
            @Cached ToLongNode fitNode) {
        final Object coerced = toIntNode
                .call(coreLibrary().truffleTypeModule, "rb_to_int_fallback", object);
        return fitNode.execute(coerced);
    }
}
