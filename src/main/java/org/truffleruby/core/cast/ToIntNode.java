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

import org.truffleruby.core.numeric.IntegerNodes.IntegerLowerNode;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

/** Node used to convert a value into a 32-bits Java int, calling {@code to_int} if the value is not yet a Ruby integer.
 * Use this whenever Ruby allows conversions using {@code to_int} and you need a 32-bits int for implementation reasons.
 *
 * <p>
 * Alternatively, consider:
 * <ul>
 * <li>{@link ToLongNode}: similar, but for 64-bits Java long, often used similar to MRI where functions usually reject
 * integers that do not fit into 64 bits</li>
 * <li>{@link IntegerCastNode}, {@link LongCastNode}: whenever {@code to_int} conversion is not required.</li>
 * <li>{@link ToRubyIntegerNode}: when only {@code to_int} conversion is needed, but the resulting value can be a
 * Bignum.</li>
 * <li>{@link IntegerLowerNode}: to lower {@code long} into {@code int} values if they fit. Can be useful conjointly
 * with {@link ToLongNode}.</li>
 * </ul>
*/
@NodeChild(value = "child", type = RubyNode.class)
public abstract class ToIntNode extends RubyContextSourceNode {

    public static ToIntNode create() {
        return ToIntNodeGen.create(null);
    }

    public static ToIntNode create(RubyNode child) {
        return ToIntNodeGen.create(child);
    }

    public abstract int execute(Object object);

    @Specialization
    protected int coerceInt(int value) {
        return value;
    }

    @Specialization(guards = "fitsInInteger(value)")
    protected int corceFittingLong(long value) {
        return (int) value;
    }

    @Specialization(guards = "!fitsInInteger(value)")
    protected int coerceTooBigLong(long value) {
        // MRI does not have this error
        throw new RaiseException(
                getContext(),
                coreExceptions().argumentError("long too big to convert into `int'", this));
    }

    @Specialization(guards = "isRubyBignum(value)")
    protected int coerceRubyBignum(DynamicObject value) {
        // not `int' to stay as compatible as possible with MRI errors
        throw new RaiseException(
                getContext(),
                coreExceptions().rangeError("bignum too big to convert into `long'", this));
    }

    @Specialization
    protected long coerceNil(Nil value) {
        // MRI hardcodes this specific error message, which is slightly different from the one we would get in the
        // catch-all case.
        throw new RaiseException(
                getContext(),
                coreExceptions().typeError("no implicit conversion form nil into to integer", this));
    }

    // object can't be a DynamicObject, because we must handle doubles and booleans.
    @Specialization(guards = { "!isRubyInteger(object)", "!isNil(object)" })
    protected int coerceObject(Object object,
            @Cached CallDispatchHeadNode toIntNode,
            @Cached ToIntNode fitNode) {
        final Object coerced = toIntNode.call(getContext().getCoreLibrary().truffleTypeModule, "rb_to_int", object);
        return fitNode.execute(coerced);
    }
}
