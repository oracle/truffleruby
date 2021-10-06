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
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.profiles.ConditionProfile;

@ImportStatic(ArrayGuards.class)
public abstract class ArrayEnsureCapacityNode extends RubyBaseNode {

    public static ArrayEnsureCapacityNode create() {
        return ArrayEnsureCapacityNodeGen.create();
    }

    public abstract Object executeEnsureCapacity(RubyArray array, int requiredCapacity);

    @Specialization(guards = "!stores.isMutable(store)", limit = "storageStrategyLimit()")
    protected Object ensureCapacityAndMakeMutable(RubyArray array, int requiredCapacity,
            @Bind("array.store") Object store,
            @CachedLibrary("store") ArrayStoreLibrary stores,
            @Cached("createCountingProfile()") ConditionProfile extendProfile) {
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
        return newStore;
    }

    @Specialization(guards = "stores.isMutable(store)", limit = "storageStrategyLimit()")
    protected Object ensureCapacity(RubyArray array, int requiredCapacity,
            @Bind("array.store") Object store,
            @CachedLibrary("store") ArrayStoreLibrary stores,
            @Cached("createCountingProfile()") ConditionProfile extendProfile) {
        final int length = stores.capacity(store);
        if (extendProfile.profile(length < requiredCapacity)) {
            final int capacity = ArrayUtils.capacity(getLanguage(), length, requiredCapacity);
            Object newStore = stores.expand(store, capacity);
            array.store = newStore;
            return newStore;
        } else {
            return store;
        }
    }
}
