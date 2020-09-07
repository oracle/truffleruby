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

import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;

/** See {@link ToIntNode} for a comparison of different integer conversion nodes. */
@NodeChild(value = "child", type = RubyNode.class)
public abstract class ToRubyIntegerNode extends RubyContextSourceNode {

    public static ToRubyIntegerNode create() {
        return ToRubyIntegerNodeGen.create(null);
    }

    public static ToRubyIntegerNode create(RubyNode child) {
        return ToRubyIntegerNodeGen.create(child);
    }

    public abstract Object execute(Object object);

    @Specialization
    protected int coerceInt(int value) {
        return value;
    }

    @Specialization
    protected long coerceLong(long value) {
        return value;
    }

    @Specialization
    protected RubyBignum coerceRubyBignum(RubyBignum value) {
        return value;
    }

    @Specialization(guards = "!isRubyInteger(object)")
    protected Object coerceObject(Object object,
            @Cached DispatchNode toIntNode) {
        return toIntNode.call(getContext().getCoreLibrary().truffleTypeModule, "rb_to_int_fallback", object);
    }
}
