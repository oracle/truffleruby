/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
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

@ExportLibrary(value = ArrayStoreLibrary.class, receiverType = int[].class)
@GenerateUncached
public class IntegerArrayStore {

    @ExportMessage
    public static int read(int[] store, long index) {
        return store[(int) index];
    }

    @ExportMessage
    public static boolean acceptsValue(int[] store, Object value) {
        return value instanceof Integer;
    }

    @ExportMessage
    static class AcceptsAllValues {

        @Specialization
        static boolean acceptsZeroValues(int[] store, ZeroLengthArrayStore otherStore) {
            return true;
        }

        @Specialization
        static boolean acceptsIntValues(int[] store, int[] otherStore) {
            return true;
        }

        @Specialization
        static boolean acceptsDelegateValues(int[] store, DelegatedArrayStorage otherStore,
                @CachedLibrary("store") ArrayStoreLibrary stores) {
            return stores.acceptsAllValues(store, otherStore.storage);
        }

        @Specialization
        static boolean acceptsOtherValues(int[] store, Object otherStore) {
            return false;
        }
    }

    @ExportMessage
    public static boolean isMutable(int[] store) {
        return true;
    }

    @ExportMessage
    public static boolean isPrimitive(int[] store) {
        return true;
    }

    @ExportMessage
    public static String toString(int[] store) {
        return "int[]";
    }

    @ExportMessage
    public static void write(int[] store, long index, Object value) {
        store[(int) index] = (int) value;
    }

    @ExportMessage
    public static long capacity(int[] store) {
        return store.length;
    }

    @ExportMessage
    public static int[] expand(int[] store, long newCapacity) {
        int[] newStore = new int[(int) newCapacity];
        System.arraycopy(store, 0, newStore, 0, store.length);
        return newStore;
    }

    @ExportMessage
    static class CopyContents {

        @Specialization
        static void copyContents(int[] srcStore, long srcStart, int[] destStore, long destStart, long length) {
            System.arraycopy(srcStore, (int) srcStart, destStore, (int) destStart, (int) length);
        }

        @Specialization
        static void copyContents(int[] srcStore, long srcStart, Object destStore, long destStart, long length,
                @CachedLibrary(limit = "5") ArrayStoreLibrary destStores) {
            for (long i = srcStart; i < length; i++) {
                destStores.write(destStore, destStart + i, srcStore[(int) (srcStart + i)]);
            }
        }
    }

    @ExportMessage
    public static int[] copyStore(int[] store, long length) {
        return ArrayUtils.grow(store, (int) length);
    }

    @ExportMessage
    @TruffleBoundary
    public static void sort(int[] store, long size) {
        Arrays.sort(store, 0, (int) size);
    }

    @ExportMessage
    public static Iterable<Object> getIterable(int[] store, long from, long length) {
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
    static class GeneralizeForValue {

        @Specialization
        static ArrayAllocator generalize(int[] store, int newValue) {
            return IntegerArrayStore.INTEGER_ARRAY_ALLOCATOR;
        }

        @Specialization
        static ArrayAllocator generalize(int[] store, long newValue) {
            return LongArrayStore.LONG_ARRAY_ALLOCATOR;
        }

        @Specialization
        static ArrayAllocator generalize(int[] store, double newValue) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }

        @Specialization
        static ArrayAllocator generalize(int[] store, Object newValue) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }
    }

    @ExportMessage
    static class GeneralizeForStore {

        @Specialization
        static ArrayAllocator generalize(int[] store, int[] newStore) {
            return IntegerArrayStore.INTEGER_ARRAY_ALLOCATOR;
        }

        @Specialization
        static ArrayAllocator generalize(int[] store, long[] newStore) {
            return LongArrayStore.LONG_ARRAY_ALLOCATOR;
        }

        @Specialization
        static ArrayAllocator generalize(int[] store, double[] newStore) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }

        @Specialization
        static ArrayAllocator generalize(int[] store, Object[] newStore) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }

        @Specialization
        static ArrayAllocator generalize(int[] store, Object newStore,
                @CachedLibrary(limit = "3") ArrayStoreLibrary newStores) {
            return newStores.generalizeForStore(newStore, store);
        }
    }

    @ExportMessage
    public static ArrayAllocator allocator(int[] store) {
        return INTEGER_ARRAY_ALLOCATOR;
    }

    public static final ArrayAllocator INTEGER_ARRAY_ALLOCATOR = new IntegerArrayAllocator();

    private static class IntegerArrayAllocator extends ArrayAllocator {

        @Override
        public int[] allocate(long capacity) {
            return new int[(int) capacity];
        }

        @Override
        public boolean accepts(Object value) {
            return value instanceof Integer;
        }

        @Override
        public boolean specializesFor(Object value) {
            return value instanceof Integer;
        }

        @Override
        public boolean isDefaultValue(Object value) {
            return (int) value == 0;
        }
    }
}
