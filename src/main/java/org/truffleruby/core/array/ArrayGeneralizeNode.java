/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.Layouts;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyContextNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

@ImportStatic(ArrayGuards.class)
public abstract class ArrayGeneralizeNode extends RubyContextNode {

    public static ArrayGeneralizeNode create() {
        return ArrayGeneralizeNodeGen.create();
    }

    public abstract Object[] executeGeneralize(DynamicObject array, int requiredCapacity);

    @Specialization(limit = "STORAGE_STRATEGIES")
    protected Object[] generalize(DynamicObject array, int requiredCapacity,
            @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
            @Cached("createCountingProfile()") ConditionProfile extendProfile) {
        assert !ArrayGuards.isObjectArray(array);
        final Object store = Layouts.ARRAY.getStore(array);
        final int capacity;
        final int length = stores.capacity(store);
        if (extendProfile.profile(length < requiredCapacity)) {
            capacity = ArrayUtils.capacity(getContext(), length, requiredCapacity);
        } else {
            capacity = length;
        }
        final Object[] newStore = new Object[capacity];
        stores.copyContents(store, 0, newStore, 0, length);
        Layouts.ARRAY.setStore(array, newStore);
        return newStore;
    }
}
