/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.array.library;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.library.ArrayStoreLibrary.ArrayAllocator;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import org.truffleruby.language.RubyBaseNode;

@ExportLibrary(value = ArrayStoreLibrary.class, receiverType = long[].class)
@GenerateUncached
public class LongArrayStore {

    @ExportMessage
    protected static long read(long[] store, int index) {
        return store[index];
    }

    @ExportMessage
    protected static boolean acceptsValue(long[] store, Object value) {
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
        protected static boolean acceptsDelegateValues(long[] store, DelegatedArrayStorage otherStore) {
            return otherStore.storage instanceof int[] || otherStore.storage instanceof long[];
        }

        @Fallback
        protected static boolean acceptsOtherValues(long[] store, Object otherStore) {
            return false;
        }
    }

    @ExportMessage
    protected static boolean isMutable(long[] store) {
        return true;
    }

    @ExportMessage
    protected static boolean isPrimitive(long[] store) {
        return true;
    }

    @ExportMessage
    protected static String toString(long[] store) {
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
    protected static int capacity(long[] store) {
        return store.length;
    }

    @ExportMessage
    protected static long[] expand(long[] store, int newCapacity) {
        return ArrayUtils.grow(store, newCapacity);
    }

    @ExportMessage
    protected static Object[] boxedCopyOfRange(long[] store, int start, int length) {
        Object[] result = new Object[length];
        for (int i = 0; i < length; i++) {
            result[i] = store[start + i];
        }
        return result;
    }

    @ExportMessage
    @ImportStatic(ArrayGuards.class)
    static class CopyContents {

        @Specialization
        protected static void copyContents(long[] srcStore, int srcStart, long[] destStore, int destStart, int length) {
            System.arraycopy(srcStore, srcStart, destStore, destStart, length);
        }

        @Specialization(guards = "!isLongStore(destStore)", limit = "storageStrategyLimit()")
        protected static void copyContents(long[] srcStore, int srcStart, Object destStore, int destStart, int length,
                @Cached LoopConditionProfile loopProfile,
                @CachedLibrary("destStore") ArrayStoreLibrary destStores) {
            int i = 0;
            try {
                for (; loopProfile.inject(i < length); i++) {
                    destStores.write(destStore, destStart + i, srcStore[srcStart + i]);
                }
            } finally {
                RubyBaseNode.profileAndReportLoopCount(destStores.getNode(), loopProfile, i);
            }
        }

        protected static boolean isLongStore(Object store) {
            return store instanceof long[];
        }
    }

    @ExportMessage
    static class Fill {
        @Specialization
        protected static void fill(long[] store, int start, int length, int value) {
            Arrays.fill(store, start, start + length, value);
        }

        @Specialization
        protected static void write(long[] store, int start, int length, long value) {
            Arrays.fill(store, start, start + length, value);
        }
    }

    @ExportMessage
    protected static long[] toJavaArrayCopy(long[] store, int length) {
        return ArrayUtils.extractRange(store, 0, length);
    }

    @ExportMessage
    @TruffleBoundary
    protected static void sort(long[] store, int size) {
        Arrays.sort(store, 0, size);
    }

    @ExportMessage
    protected static Iterable<Object> getIterable(long[] store, int from, int length) {
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

        @Specialization(limit = "storageStrategyLimit()")
        protected static ArrayAllocator generalize(long[] store, Object newStore,
                @CachedLibrary("newStore") ArrayStoreLibrary newStores) {
            return newStores.generalizeForStore(newStore, store);
        }
    }

    @ExportMessage
    static class AllocateForNewValue {

        @Specialization
        protected static Object allocateForNewStore(long[] store, int newValue, int length) {
            return LongArrayStore.LONG_ARRAY_ALLOCATOR.allocate(length);
        }

        @Specialization
        protected static Object allocateForNewStore(long[] store, long newValue, int length) {
            return LongArrayStore.LONG_ARRAY_ALLOCATOR.allocate(length);
        }

        @Specialization
        protected static Object allocateForNewStore(long[] store, double newValue, int length) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR.allocate(length);
        }

        @Fallback
        protected static Object allocateForNewStore(long[] store, Object newValue, int length) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR.allocate(length);
        }
    }

    @ExportMessage
    @ImportStatic(ArrayGuards.class)
    static class AllocateForNewStore {

        @Specialization
        protected static Object allocate(long[] store, int[] newStore, int length) {
            return LongArrayStore.LONG_ARRAY_ALLOCATOR.allocate(length);
        }

        @Specialization
        protected static Object allocate(long[] store, long[] newStore, int length) {
            return LongArrayStore.LONG_ARRAY_ALLOCATOR.allocate(length);
        }

        @Specialization
        protected static Object allocate(long[] store, double[] newStore, int length) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR.allocate(length);
        }

        @Specialization
        protected static Object allocate(long[] store, Object[] newStore, int length) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR.allocate(length);
        }

        @Specialization(guards = "!basicStore(newStore)", limit = "storageStrategyLimit()")
        protected static Object allocate(long[] store, Object newStore, int length,
                @CachedLibrary("newStore") ArrayStoreLibrary newStores) {
            return newStores.allocateForNewValue(newStore, store, length);
        }
    }

    @ExportMessage
    protected static class IsDefaultValue {

        @Specialization
        protected static boolean isDefaultValue(long[] store, long value) {
            return value == 0L;
        }

        @Fallback
        protected static boolean isDefaultValue(long[] store, Object value) {
            return false;
        }
    }

    @ExportMessage
    protected static ArrayAllocator allocator(long[] store) {
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
