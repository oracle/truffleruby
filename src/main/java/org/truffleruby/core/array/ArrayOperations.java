/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;

import java.lang.reflect.Array;

public abstract class ArrayOperations {

    public static int normalizeIndex(int length, int index, ConditionProfile negativeIndexProfile) {
        if (negativeIndexProfile.profile(index < 0)) {
            return length + index;
        } else {
            return index;
        }
    }

    public static int normalizeIndex(int length, int index) {
        if (CompilerDirectives.injectBranchProbability(CompilerDirectives.SLOWPATH_PROBABILITY, index < 0)) {
            return length + index;
        } else {
            return index;
        }
    }

    public static int clampExclusiveIndex(int length, int index) {
        if (CompilerDirectives.injectBranchProbability(CompilerDirectives.UNLIKELY_PROBABILITY, index < 0)) {
            return 0;
        } else if (CompilerDirectives.injectBranchProbability(CompilerDirectives.UNLIKELY_PROBABILITY, index > length)) {
            return length;
        } else {
            return index;
        }
    }

    @TruffleBoundary
    public static Iterable<Object> toIterable(DynamicObject array) {
        return ArrayStrategy.of(array).getIterable(array, Layouts.ARRAY.getSize(array));
    }

    @TruffleBoundary
    public static Object getBackingStore(DynamicObject array) {
        return Layouts.ARRAY.getStore(array);
    }

    @TruffleBoundary
    public static int getStoreCapacity(DynamicObject array) {
        Object store = getBackingStore(array);
        if (store == null) {
            return 0;
        } else {
            if (store instanceof DelegatedArrayStorage) {
                DelegatedArrayStorage delegate = (DelegatedArrayStorage) store;
                return Array.getLength(delegate.storage) - delegate.offset;
            } else {
                return Array.getLength(store);
            }
        }
    }

}
