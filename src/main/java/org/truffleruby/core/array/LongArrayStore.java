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

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

import org.truffleruby.core.array.ArrayStoreLibrary.ArrayAllocator;

@ExportLibrary(value = ArrayStoreLibrary.class, receiverType = long[].class)
@GenerateUncached
public class LongArrayStore {

    @ExportMessage
    public static long read(long[] store, int index) {
        return store[index];
    }

    @ExportMessage
    public static boolean acceptsValue(long[] store, Object value) {
        return value instanceof Long || value instanceof Integer;
    }

    @ExportMessage
    static class AcceptsAllValues {

        @Specialization
        protected static boolean acceptsZeroValues(long[] store, ZeroLengthArrayStore otherStore) {
            return true;
        }

        @Specialization
        protected static boolean acceptsIntValues(long[] store, int[] otherStore) {
            return true;
        }

        @Specialization
        protected static boolean acceptsLongValues(long[] store, long[] otherStore) {
            return true;
        }

        @Specialization
        protected static boolean acceptsDelegateValues(long[] store, DelegatedArrayStorage otherStore,
                @CachedLibrary("store") ArrayStoreLibrary stores) {
            return stores.acceptsAllValues(store, otherStore.storage);
        }

        @Fallback
        protected static boolean acceptsOtherValues(long[] store, Object otherStore) {
            return false;
        }
    }

    @ExportMessage
    public static boolean isMutable(long[] store) {
        return true;
    }

    @ExportMessage
    public static boolean isPrimitive(long[] store) {
        return true;
    }

    @ExportMessage
    public static String toString(long[] store) {
        return "long[]";
    }

    @ExportMessage
    static class Write {
        @Specialization
        protected static void write(long[] store, int index, int value) {
            store[index] = value;
        }

        @Specialization
        protected static void write(long[] store, int index, long value) {
            store[index] = value;
        }
    }

    @ExportMessage
    public static int capacity(long[] store) {
        return store.length;
    }

    @ExportMessage
    public static long[] expand(long[] store, int newCapacity) {
        long[] newStore = new long[newCapacity];
        System.arraycopy(store, 0, newStore, 0, store.length);
        return newStore;
    }

    @ExportMessage
    @ImportStatic(ArrayGuards.class)
    static class CopyContents {

        @Specialization
        protected static void copyContents(long[] srcStore, int srcStart, long[] destStore, int destStart, int length) {
            System.arraycopy(srcStore, srcStart, destStore, destStart, length);
        }

        @Specialization(limit = "STORAGE_STRATEGIES")
        protected static void copyContents(long[] srcStore, int srcStart, Object destStore, int destStart, int length,
                @CachedLibrary("destStore") ArrayStoreLibrary destStores) {
            for (int i = srcStart; i < length; i++) {
                destStores.write(destStore, destStart + i, srcStore[(srcStart + i)]);
            }
        }
    }

    @ExportMessage
    public static long[] copyStore(long[] store, int length) {
        return ArrayUtils.grow(store, length);
    }

    @ExportMessage
    @TruffleBoundary
    public static void sort(long[] store, int size) {
        Arrays.sort(store, 0, size);
    }

    @ExportMessage
    public static Iterable<Object> getIterable(long[] store, int from, int length) {
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
    static class GeneralizeForValue {

        @Specialization
        protected static ArrayAllocator generalize(long[] store, int newValue) {
            return LongArrayStore.LONG_ARRAY_ALLOCATOR;
        }

        @Specialization
        protected static ArrayAllocator generalize(long[] store, long newValue) {
            return LongArrayStore.LONG_ARRAY_ALLOCATOR;
        }

        @Specialization
        protected static ArrayAllocator generalize(long[] store, double newValue) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }

        @Fallback
        protected static ArrayAllocator generalize(long[] store, Object newValue) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }
    }

    @ExportMessage
    @ImportStatic(ArrayGuards.class)
    static class GeneralizeForStore {

        @Specialization
        protected static ArrayAllocator generalize(long[] store, int[] newStore) {
            return LongArrayStore.LONG_ARRAY_ALLOCATOR;
        }

        @Specialization
        protected static ArrayAllocator generalize(long[] store, long[] newStore) {
            return LongArrayStore.LONG_ARRAY_ALLOCATOR;
        }

        @Specialization
        protected static ArrayAllocator generalize(long[] store, double[] newStore) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }

        @Specialization
        protected static ArrayAllocator generalize(long[] store, Object[] newStore) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }

        @Specialization(limit = "STORAGE_STRATEGIES")
        protected static ArrayAllocator generalize(long[] store, Object newStore,
                @CachedLibrary("newStore") ArrayStoreLibrary newStores) {
            return newStores.generalizeForStore(newStore, store);
        }
    }

    @ExportMessage
    public static ArrayAllocator allocator(long[] store) {
        return LONG_ARRAY_ALLOCATOR;
    }

    public static final ArrayAllocator LONG_ARRAY_ALLOCATOR = new LongArrayAllocator();

    private static class LongArrayAllocator extends ArrayAllocator {

        @Override
        public long[] allocate(int capacity) {
            return new long[capacity];
        }

        @Override
        public boolean accepts(Object value) {
            return value instanceof Integer || value instanceof Long;
        }

        @Override
        public boolean specializesFor(Object value) {
            return value instanceof Long;
        }

        @Override
        public boolean isDefaultValue(Object value) {
            return (long) value == 0;
        }
    }
}
