/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

import org.truffleruby.language.RubyBaseNode;

@ImportStatic(ArrayGuards.class)
public abstract class ArrayGeneralizeNode extends RubyBaseNode {

    public static ArrayGeneralizeNode create() {
        return ArrayGeneralizeNodeGen.create();
    }

    public abstract Object[] executeGeneralize(DynamicObject array, int requiredCapacity);

    @Specialization(guards = "strategy.matches(array)", limit = "STORAGE_STRATEGIES")
    public Object[] generalize(DynamicObject array, int requiredCapacity,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("createCountingProfile()") ConditionProfile extendProfile) {
        assert !ArrayGuards.isObjectArray(array);
        final ArrayMirror mirror = strategy.newMirror(array);
        final int capacity;
        if (extendProfile.profile(mirror.getLength() < requiredCapacity)) {
            capacity = ArrayUtils.capacity(getContext(), mirror.getLength(), requiredCapacity);
        } else {
            capacity = mirror.getLength();
        }
        final Object[] store = mirror.getBoxedCopy(capacity);
        strategy.setStore(array, store);
        return store;
    }

}
