/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.core.cast.ArrayCastNode;
import org.truffleruby.language.RubyContextNode;

/** Attempts converting its argument to an array, via {@link ArrayCastNode} (i.e. calling "to_ary"), or if that doesn't
 * work, by wrapping it inside a one-element array. */
public final class ArrayConvertNode extends RubyContextNode {

    @Child ArrayCastNode arrayCast = ArrayCastNode.create();
    @Child ArrayBuilderNode arrayBuilder = ArrayBuilderNode.create();
    private final ConditionProfile cantCast = ConditionProfile.create();

    public static ArrayConvertNode create() {
        return new ArrayConvertNode();
    }

    public RubyArray execute(Object object) {
        Object converted = arrayCast.execute(object);
        if (cantCast.profile(converted == nil)) {
            return ArrayHelpers.specializedRubyArrayOf(getContext(), arrayBuilder, object);
        }
        return (RubyArray) converted;
    }
}
