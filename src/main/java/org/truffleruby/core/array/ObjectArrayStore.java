/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.array;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import org.truffleruby.core.array.ArrayStoreLibrary.ArrayAllocator;

@ExportLibrary(value = ArrayStoreLibrary.class, receiverType = Object[].class)
@GenerateUncached
public class ObjectArrayStore {

    @ExportMessage
    public static Object read(Object[] store, int index) {
        return store[index];
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
    public static void write(Object[] store, int index, Object value) {
        store[index] = value;
    }

    @ExportMessage
    public static int capacity(Object[] store) {
        return store.length;
    }

    @ExportMessage
    public static Object[] expand(Object[] store, int newCapacity) {
        Object[] newStore = new Object[newCapacity];
        System.arraycopy(store, 0, newStore, 0, store.length);
        return newStore;
    }

    @ExportMessage
    @ImportStatic(ArrayGuards.class)
    static class CopyContents {

        @Specialization
        protected static void copyContents(Object[] srcStore, int srcStart, Object[] destStore, int destStart,
                int length) {
            System.arraycopy(srcStore, srcStart, destStore, destStart, length);
        }

        @Specialization(limit = "STORAGE_STRATEGIES")
        protected static void copyContents(Object[] srcStore, int srcStart, Object destStore, int destStart, int length,
                @CachedLibrary("destStore") ArrayStoreLibrary destStores) {
            for (int i = srcStart; i < length; i++) {
                destStores.write(destStore, destStart + i, srcStore[(srcStart + i)]);
            }
        }
    }

    @ExportMessage
    public static Object[] copyStore(Object[] store, int length) {
        return ArrayUtils.grow(store, length);
    }

    @ExportMessage
    @TruffleBoundary
    public static void sort(Object[] store, int size) {
        Arrays.sort(store, 0, size);
    }

    @ExportMessage
    public static Iterable<Object> getIterable(Object[] store, int from, int length) {
        return () -> new Iterator<Object>() {

            private int n = from;

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
    public static ArrayAllocator generalizeForStore(Object[] store, Object newValue) {
        return OBJECT_ARRAY_ALLOCATOR;
    }

    @ExportMessage
    public static ArrayAllocator allocator(Object[] store) {
        return OBJECT_ARRAY_ALLOCATOR;
    }

    public static final ArrayAllocator OBJECT_ARRAY_ALLOCATOR = new ObjectArrayAllocator();

    private static class ObjectArrayAllocator extends ArrayAllocator {

        @Override
        public Object[] allocate(int capacity) {
            return new Object[capacity];
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
