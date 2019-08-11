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

import java.lang.reflect.Array;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.language.objects.shared.SharedObjects;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class ArrayOperations {

    public static boolean isPrimitiveStorage(DynamicObject array) {
        Object store = getBackingStore(array);
        return store == null || store instanceof int[] || store instanceof long[] || store instanceof double[];
    }

    public static boolean verifyStore(DynamicObject array) {
        final Object backingStore = getBackingStore(array);
        assert backingStore == null || backingStore instanceof int[] || backingStore instanceof long[] || backingStore instanceof double[] ||
                backingStore.getClass() == Object[].class : backingStore;

        final RubyContext context = Layouts.MODULE.getFields(Layouts.ARRAY.getLogicalClass(array)).getContext();
        if (SharedObjects.isShared(context, array)) {
            final Object store = Layouts.ARRAY.getStore(array);

            if (store != null && store.getClass() == Object[].class) {
                final Object[] objectArray = (Object[]) store;

                for (Object element : objectArray) {
                    assert SharedObjects.assertPropagateSharing(context, array, element) : "unshared element in shared Array: " + element;
                }
            } else if (store instanceof DelegatedArrayStorage && ((DelegatedArrayStorage) store).hasObjectArrayStorage()) {
                final DelegatedArrayStorage delegated = (DelegatedArrayStorage) store;
                final Object[] objectArray = (Object[]) delegated.storage;

                for (int i = delegated.offset; i < delegated.offset + delegated.length; i++) {
                    final Object element = objectArray[i];
                    assert SharedObjects.assertPropagateSharing(context, array, element) : "unshared element in shared copy-on-write Array: " + element;
                }
            } else {
                assert isPrimitiveStorage(array);
            }
        }

        return true;
    }

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
    private static Object getBackingStore(DynamicObject array) {
        final Object store = Layouts.ARRAY.getStore(array);
        if (store instanceof DelegatedArrayStorage) {
            return ((DelegatedArrayStorage) store).storage;
        } else {
            return store;
        }
    }

    @TruffleBoundary
    public static int getStoreCapacity(DynamicObject array) {
        Object store = Layouts.ARRAY.getStore(array);
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
