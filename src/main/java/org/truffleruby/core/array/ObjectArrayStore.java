/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.array;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import org.truffleruby.core.array.ArrayStoreLibrary.ArrayAllocator;

@ExportLibrary(value = ArrayStoreLibrary.class, receiverType = Object[].class)
@GenerateUncached
public class ObjectArrayStore {

    @ExportMessage
    public static Object read(Object[] store, long index) {
        return store[(int) index];
    }

    @ExportMessage
    public static boolean acceptsValue(Object[] store, Object value) {
        return true;
    }

    @ExportMessage
    public static boolean acceptsAllValues(Object[] store, Object otherStore) {
        return true;
    }

    @ExportMessage
    public static boolean isMutable(Object[] store) {
        return true;
    }

    @ExportMessage
    public static boolean isPrimitive(Object[] store) {
        return false;
    }

    @ExportMessage
    public static String toString(Object[] store) {
        return "object[]";
    }

    @ExportMessage
    public static void write(Object[] store, long index, Object value) {
        store[(int) index] = value;
    }

    @ExportMessage
    public static long capacity(Object[] store) {
        return store.length;
    }

    @ExportMessage
    public static Object[] expand(Object[] store, long newCapacity) {
        Object[] newStore = new Object[(int) newCapacity];
        System.arraycopy(store, 0, newStore, 0, store.length);
        return newStore;
    }

    @ExportMessage
    static class CopyContents {

        @Specialization
        static void copyContents(Object[] srcStore, long srcStart, Object[] destStore, long destStart, long length) {
            System.arraycopy(srcStore, (int) srcStart, destStore, (int) destStart, (int) length);
        }

        @Specialization
        static void copyContents(Object[] srcStore, long srcStart, Object destStore, long destStart, long length,
                @CachedLibrary(limit = "5") ArrayStoreLibrary destStores) {
            for (long i = srcStart; i < length; i++) {
                destStores.write(destStore, destStart + i, srcStore[(int) (srcStart + i)]);
            }
        }
    }

    @ExportMessage
    public static Object[] copyStore(Object[] store, long length) {
        return ArrayUtils.grow(store, (int) length);
    }

    @ExportMessage
    @TruffleBoundary
    public static void sort(Object[] store, long size) {
        Arrays.sort(store, 0, (int) size);
    }

    @ExportMessage
    public static Iterable<Object> getIterable(Object[] store, long from, long length) {
        return () -> new Iterator<Object>() {

            private int n = (int) from;

            @Override
            public boolean hasNext() {
                return n < from + length;
            }

            @Override
            public Object next() throws NoSuchElementException {
                if (n >= from + length) {
                    throw new NoSuchElementException();
                }

                final Object object = store[n];
                n++;
                return object;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove");
            }

        };
    }

    @ExportMessage
    public static ArrayAllocator generalizeForValue(Object[] store, Object newValue) {
        return OBJECT_ARRAY_ALLOCATOR;
    }

    @ExportMessage
    public static ArrayAllocator allocator(Object[] store) {
        return OBJECT_ARRAY_ALLOCATOR;
    }

    @ExportMessage
    static class GeneralizeForStore {

        @Specialization
        static ArrayAllocator generalize(Object[] store, int[] newStore) {
            return OBJECT_ARRAY_ALLOCATOR;
        }

        @Specialization
        static ArrayAllocator generalize(Object[] store, long[] newStore) {
            return OBJECT_ARRAY_ALLOCATOR;
        }

        @Specialization
        static ArrayAllocator generalize(Object[] store, double[] newStore) {
            return OBJECT_ARRAY_ALLOCATOR;
        }

        @Specialization
        static ArrayAllocator generalize(Object[] store, Object[] newStore) {
            return OBJECT_ARRAY_ALLOCATOR;
        }

        @Specialization
        static ArrayAllocator generalize(Object[] store, Object newStore,
                @CachedLibrary(limit = "3") ArrayStoreLibrary newStores) {
            return newStores.generalizeForStore(newStore, store);
        }
    }

    public static final ArrayAllocator OBJECT_ARRAY_ALLOCATOR = new ObjectArrayAllocator();

    private static class ObjectArrayAllocator extends ArrayAllocator {

        @Override
        public Object[] allocate(long capacity) {
            return new Object[(int) capacity];
        }

        @Override
        public boolean accepts(Object value) {
            return true;
        }

        @Override
        public boolean specializesFor(Object value) {
            return !(value instanceof Integer) && !(value instanceof Long) && !(value instanceof Double);
        }

        @Override
        public boolean isDefaultValue(Object value) {
            return value == null;
        }
    }
}
