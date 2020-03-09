/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.array.library;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.library.ArrayStoreLibrary.ArrayAllocator;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(value = ArrayStoreLibrary.class, receiverType = Object[].class)
@GenerateUncached
public class ObjectArrayStore {

    @ExportMessage
    protected static Object read(Object[] store, int index) {
        return store[index];
    }

    @ExportMessage
    protected static boolean acceptsValue(Object[] store, Object value) {
        return true;
    }

    @ExportMessage
    protected static boolean acceptsAllValues(Object[] store, Object otherStore) {
        return true;
    }

    @ExportMessage
    protected static boolean isMutable(Object[] store) {
        return true;
    }

    @ExportMessage
    protected static String toString(Object[] store) {
        return "Object[]";
    }

    @ExportMessage
    protected static void write(Object[] store, int index, Object value) {
        store[index] = value;
    }

    @ExportMessage
    protected static int capacity(Object[] store) {
        return store.length;
    }

    @ExportMessage
    protected static Object[] expand(Object[] store, int newCapacity) {
        return ArrayUtils.grow(store, newCapacity);
    }

    @ExportMessage
    protected static Object[] boxedCopyOfRange(Object[] store, int start, int length) {
        Object[] result = new Object[length];
        System.arraycopy(store, start, result, 0, length);
        return result;
    }

    @ExportMessage
    @ImportStatic(ArrayGuards.class)
    static class CopyContents {

        @Specialization
        protected static void copyContents(
                Object[] srcStore,
                int srcStart,
                Object[] destStore,
                int destStart,
                int length) {
            System.arraycopy(srcStore, srcStart, destStore, destStart, length);
        }

        @Specialization(guards = "!isObjectStore(destStore)", limit = "STORAGE_STRATEGIES")
        protected static void copyContents(Object[] srcStore, int srcStart, Object destStore, int destStart, int length,
                @CachedLibrary("destStore") ArrayStoreLibrary destStores) {
            for (int i = srcStart; i < length; i++) {
                destStores.write(destStore, destStart + i, srcStore[(srcStart + i)]);
            }
        }

        protected static boolean isObjectStore(Object store) {
            return store instanceof Object[];
        }
    }

    @ExportMessage
    protected static Object[] toJavaArrayCopy(Object[] store, int length) {
        return ArrayUtils.extractRange(store, 0, length);
    }

    @ExportMessage
    protected static Iterable<Object> getIterable(Object[] store, int from, int length) {
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
    protected static ArrayAllocator generalizeForValue(Object[] store, Object newValue) {
        return OBJECT_ARRAY_ALLOCATOR;
    }

    @ExportMessage
    protected static ArrayAllocator generalizeForStore(Object[] store, Object newValue) {
        return OBJECT_ARRAY_ALLOCATOR;
    }

    @ExportMessage
    protected static ArrayAllocator allocator(Object[] store) {
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
