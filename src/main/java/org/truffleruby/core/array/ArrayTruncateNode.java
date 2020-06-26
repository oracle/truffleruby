/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyBaseNode;

import static org.truffleruby.Layouts.ARRAY;

/** Truncates an array by setting its size and clearing the remainder of the store with default values. */
@ImportStatic(ArrayGuards.class)
public abstract class ArrayTruncateNode extends RubyBaseNode {

    public static ArrayTruncateNode create() {
        return ArrayTruncateNodeGen.create();
    }

    public abstract void execute(DynamicObject array, int size);

    @Specialization(
            guards = { "getSize(array) > size", "stores.isMutable(getStore(array))" },
            limit = "storageStrategyLimit()")
    protected void truncate(DynamicObject array, int size,
            @CachedLibrary("getStore(array)") ArrayStoreLibrary stores) {

        final int oldSize = ARRAY.getSize(array);
        ARRAY.setSize(array, size);
        stores.clear(ARRAY.getStore(array), size, oldSize - size);
    }

    @Specialization(
            guards = { "getSize(array) > size", "!stores.isMutable(array)" },
            limit = "storageStrategyLimit()")
    protected void truncateCopy(DynamicObject array, int size,
            @CachedLibrary("getStore(array)") ArrayStoreLibrary stores) {

        final Object store = ARRAY.getStore(array);
        final Object newStore = stores.allocateForNewStore(store, store, size);
        stores.copyContents(store, 0, newStore, 0, size);
        ARRAY.setStore(array, newStore);
        ARRAY.setSize(array, size);
    }

    @ReportPolymorphism.Exclude
    @Specialization(guards = "getSize(array) <= size")
    protected void doNothing(DynamicObject array, int size) {
    }
}
