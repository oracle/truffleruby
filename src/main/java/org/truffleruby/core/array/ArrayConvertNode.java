/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.dispatch.DispatchNode;

/** Attempts converting its argument to an array by calling #to_ary, or if that doesn't work, by wrapping it inside a
 * one-element array. */
public abstract class ArrayConvertNode extends RubyBaseNode {

    public abstract RubyArray execute(Object value);

    @Specialization
    protected RubyArray castArray(RubyArray array) {
        return array;
    }

    @Specialization(guards = "!isRubyArray(object)")
    protected RubyArray cast(Object object,
            @Cached ConditionProfile canCast,
            @Cached ArrayBuilderNode arrayBuilder,
            @Cached(parameters = "PRIVATE_RETURN_MISSING") DispatchNode toArrayNode) {
        final Object result = toArrayNode.call(object, "to_ary");
        if (canCast.profile(result instanceof RubyArray)) {
            return (RubyArray) result;
        } else {
            return ArrayHelpers.specializedRubyArrayOf(getContext(), getLanguage(), arrayBuilder, object);
        }
    }

}
