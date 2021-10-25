/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.core.array.library.ArrayStoreLibrary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public abstract class ArrayOperations {

    public static boolean isPrimitiveStorage(RubyArray array) {
        ArrayStoreLibrary stores = ArrayStoreLibrary.getFactory().getUncached();
        Object store = stores.backingStore(array.store);
        return stores.isPrimitive(store);
    }

    public static int clampExclusiveIndex(int length, int index) {
        if (CompilerDirectives.injectBranchProbability(CompilerDirectives.UNLIKELY_PROBABILITY, index < 0)) {
            return 0;
        } else if (CompilerDirectives
                .injectBranchProbability(CompilerDirectives.UNLIKELY_PROBABILITY, index > length)) {
            return length;
        } else {
            return index;
        }
    }

    @TruffleBoundary
    public static Iterable<Object> toIterable(RubyArray array) {
        return ArrayStoreLibrary
                .getFactory()
                .getUncached()
                .getIterable(array.store, 0, array.size);
    }

    @TruffleBoundary
    private static Object getBackingStore(RubyArray array) {
        ArrayStoreLibrary stores = ArrayStoreLibrary.getFactory().getUncached();
        return stores.backingStore(array.store);
    }

    @TruffleBoundary
    public static int getStoreCapacity(RubyArray array) {
        ArrayStoreLibrary stores = ArrayStoreLibrary.getFactory().getUncached();
        return stores.capacity(array.store);
    }

}
