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

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;

import org.truffleruby.Layouts;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyBaseNode;

@ImportStatic(ArrayGuards.class)
public abstract class ArrayCopyOnWriteNode extends RubyBaseNode {

    public static ArrayCopyOnWriteNode create() {
        return ArrayCopyOnWriteNodeGen.create();
    }

    public abstract Object execute(DynamicObject array, int start, int length);

    @Specialization(guards = "stores.isMutable(getStore(array))", limit = "STORAGE_STRATEGIES")
    protected Object extractFromMutableArray(DynamicObject array, int start, int length,
            @CachedLibrary("getStore(array)") ArrayStoreLibrary stores) {
        Object store = Layouts.ARRAY.getStore(array);
        int size = Layouts.ARRAY.getSize(array);
        Object cowStore = stores.extractRange(store, 0, size);
        Object range = stores.extractRange(store, start, start + length);
        Layouts.ARRAY.setStore(array, cowStore);
        return range;
    }

    @Specialization(guards = "!stores.isMutable(getStore(array))", limit = "STORAGE_STRATEGIES")
    protected Object extractFromNonMutableArray(DynamicObject array, int start, int length,
            @CachedLibrary("getStore(array)") ArrayStoreLibrary stores) {
        Object store = Layouts.ARRAY.getStore(array);
        Object range = stores.extractRange(store, start, start + length);
        return range;
    }
}
