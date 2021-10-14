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

import java.lang.reflect.Array;

import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.array.library.DelegatedArrayStorage;
import org.truffleruby.core.array.library.NativeArrayStorage;
import org.truffleruby.core.array.library.SharedArrayStorage;
import org.truffleruby.language.objects.shared.SharedObjects;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public abstract class ArrayOperations {

    public static boolean isPrimitiveStorage(RubyArray array) {
        Object store = getBackingStore(array);
        return store == ArrayStoreLibrary.INITIAL_STORE || store instanceof int[] || store instanceof long[] ||
                store instanceof double[];
    }

    public static boolean verifyStore(RubyArray array) {
        final Object backingStore = getBackingStore(array);
        assert backingStore == ArrayStoreLibrary.INITIAL_STORE ||
                backingStore instanceof NativeArrayStorage ||
                backingStore instanceof int[] || backingStore instanceof long[] || backingStore instanceof double[] ||
                backingStore.getClass() == Object[].class : backingStore;

        if (SharedObjects.isShared(array)) {
            final Object store = array.store;

            if (store.getClass() == Object[].class) {
                final Object[] objectArray = (Object[]) store;

                for (Object element : objectArray) {
                    assert SharedObjects.assertPropagateSharing(
                            array,
                            element) : "unshared element in shared Array: " + element;
                }
            } else if (store instanceof DelegatedArrayStorage &&
                    ((DelegatedArrayStorage) store).hasObjectArrayStorage()) {
                final DelegatedArrayStorage delegated = (DelegatedArrayStorage) store;
                final Object[] objectArray = (Object[]) delegated.storage;

                for (int i = delegated.offset; i < delegated.offset + delegated.length; i++) {
                    final Object element = objectArray[i];
                    assert SharedObjects.assertPropagateSharing(
                            array,
                            element) : "unshared element in shared copy-on-write Array: " + element;
                }
            } else {
                assert isPrimitiveStorage(array);
            }
        }

        return true;
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
        final Object store = array.store;
        if (store instanceof DelegatedArrayStorage) {
            return ((DelegatedArrayStorage) store).storage;
        } else {
            return store;
        }
    }

    @TruffleBoundary
    public static int getStoreCapacity(RubyArray array) {
        Object store = array.store;
        if (store == ArrayStoreLibrary.INITIAL_STORE) {
            return 0;
        } else {
            if (store instanceof DelegatedArrayStorage) {
                DelegatedArrayStorage delegate = (DelegatedArrayStorage) store;
                return Array.getLength(delegate.storage) - delegate.offset;
            } else if (store instanceof SharedArrayStorage) {
                return Array.getLength(((SharedArrayStorage) store).storage);
            } else {
                return Array.getLength(store);
            }
        }
    }

}
