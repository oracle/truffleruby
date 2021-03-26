/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyContextNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.profiles.ConditionProfile;

@ImportStatic(ArrayGuards.class)
public abstract class ArrayEnsureCapacityNode extends RubyContextNode {

    public static ArrayEnsureCapacityNode create() {
        return ArrayEnsureCapacityNodeGen.create();
    }

    public abstract boolean executeEnsureCapacity(RubyArray array, int requiredCapacity);

    @Specialization(guards = "!stores.isMutable(array.store)", limit = "storageStrategyLimit()")
    protected boolean ensureCapacityAndMakeMutable(RubyArray array, int requiredCapacity,
            @CachedLibrary("array.store") ArrayStoreLibrary stores,
            @Cached("createCountingProfile()") ConditionProfile extendProfile) {
        final Object store = array.store;

        final int currentCapacity = stores.capacity(store);
        final int capacity;
        if (extendProfile.profile(currentCapacity < requiredCapacity)) {
            capacity = ArrayUtils.capacity(getLanguage(), currentCapacity, requiredCapacity);
        } else {
            capacity = currentCapacity;
        }

        final Object newStore = stores.allocator(store).allocate(capacity);
        stores.copyContents(store, 0, newStore, 0, currentCapacity);
        array.store = newStore;
        return true;
    }

    @Specialization(guards = "stores.isMutable(array.store)", limit = "storageStrategyLimit()")
    protected boolean ensureCapacity(RubyArray array, int requiredCapacity,
            @CachedLibrary("array.store") ArrayStoreLibrary stores,
            @Cached("createCountingProfile()") ConditionProfile extendProfile) {
        final Object store = array.store;

        final int length = stores.capacity(store);
        if (extendProfile.profile(length < requiredCapacity)) {
            final int capacity = ArrayUtils.capacity(getLanguage(), length, requiredCapacity);
            array.store = stores.expand(store, capacity);
            return true;
        } else {
            return false;
        }
    }
}
