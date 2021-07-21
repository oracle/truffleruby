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

@ExportLibrary(value = ArrayStoreLibrary.class, receiverType = int[].class)
@GenerateUncached
public class IntegerArrayStore {

    @ExportMessage
    protected static int read(int[] store, int index) {
        return store[index];
    }

    @ExportMessage
    protected static boolean acceptsValue(int[] store, Object value) {
        return value instanceof Integer;
    }

    @ExportMessage
    static class AcceptsAllValues {

        @Specialization
        protected static boolean acceptsZeroValues(int[] store, ZeroLengthArrayStore otherStore) {
            return true;
        }

        @Specialization
        protected static boolean acceptsIntValues(int[] store, int[] otherStore) {
            return true;
        }

        @Specialization
        protected static boolean acceptsDelegateValues(int[] store, DelegatedArrayStorage otherStore) {
            return otherStore.storage instanceof int[];
        }

        @Fallback
        protected static boolean acceptsOtherValues(int[] store, Object otherStore) {
            return false;
        }
    }

    @ExportMessage
    protected static boolean isMutable(int[] store) {
        return true;
    }

    @ExportMessage
    protected static boolean isPrimitive(int[] store) {
        return true;
    }

    @ExportMessage
    protected static String toString(int[] store) {
        return "int[]";
    }

    @ExportMessage
    protected static void write(int[] store, int index, Object value) {
        store[index] = (int) value;
    }

    @ExportMessage
    protected static int capacity(int[] store) {
        return store.length;
    }

    @ExportMessage
    protected static int[] expand(int[] store, int newCapacity) {
        return ArrayUtils.grow(store, newCapacity);
    }

    @ExportMessage
    protected static Object[] boxedCopyOfRange(int[] store, int start, int length) {
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
        protected static void copyContents(int[] srcStore, int srcStart, int[] destStore, int destStart, int length) {
            System.arraycopy(srcStore, srcStart, destStore, destStart, length);
        }

        @Specialization(guards = "!isIntStore(destStore)", limit = "storageStrategyLimit()")
        protected static void copyContents(int[] srcStore, int srcStart, Object destStore, int destStart, int length,
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

        protected static boolean isIntStore(Object store) {
            return store instanceof int[];
        }
    }

    @ExportMessage
    protected static void fill(int[] store, int start, int length, Object value) {
        Arrays.fill(store, start, start + length, (int) value);
    }

    @ExportMessage
    protected static int[] toJavaArrayCopy(int[] store, int length) {
        return ArrayUtils.extractRange(store, 0, length);
    }

    @ExportMessage
    @TruffleBoundary
    protected static void sort(int[] store, int size) {
        Arrays.sort(store, 0, size);
    }

    @ExportMessage
    protected static Iterable<Object> getIterable(int[] store, int from, int length) {
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
        protected static ArrayAllocator generalize(int[] store, int newValue) {
            return IntegerArrayStore.INTEGER_ARRAY_ALLOCATOR;
        }

        @Specialization
        protected static ArrayAllocator generalize(int[] store, long newValue) {
            return LongArrayStore.LONG_ARRAY_ALLOCATOR;
        }

        @Specialization
        protected static ArrayAllocator generalize(int[] store, double newValue) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }

        @Fallback
        protected static ArrayAllocator generalize(int[] store, Object newValue) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }
    }

    @ExportMessage
    @ImportStatic(ArrayGuards.class)
    static class GeneralizeForStore {

        @Specialization
        protected static ArrayAllocator generalize(int[] store, int[] newStore) {
            return IntegerArrayStore.INTEGER_ARRAY_ALLOCATOR;
        }

        @Specialization
        protected static ArrayAllocator generalize(int[] store, long[] newStore) {
            return LongArrayStore.LONG_ARRAY_ALLOCATOR;
        }

        @Specialization
        protected static ArrayAllocator generalize(int[] store, double[] newStore) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }

        @Specialization
        protected static ArrayAllocator generalize(int[] store, Object[] newStore) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }

        @Specialization(limit = "storageStrategyLimit()")
        protected static ArrayAllocator generalize(int[] store, Object newStore,
                @CachedLibrary("newStore") ArrayStoreLibrary newStores) {
            return newStores.generalizeForStore(newStore, store);
        }
    }

    @ExportMessage
    static class AllocateForNewValue {

        @Specialization
        protected static Object allocateForNewValue(int[] store, int newValue, int length) {
            return IntegerArrayStore.INTEGER_ARRAY_ALLOCATOR.allocate(length);
        }

        @Specialization
        protected static Object allocateForNewValue(int[] store, long newValue, int length) {
            return LongArrayStore.LONG_ARRAY_ALLOCATOR.allocate(length);
        }

        @Specialization
        protected static Object allocateForNewValue(int[] store, double newValue, int length) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR.allocate(length);
        }

        @Fallback
        protected static Object allocateForNewValue(int[] store, Object newValue, int length) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR.allocate(length);
        }

    }

    @ExportMessage
    @ImportStatic(ArrayGuards.class)
    static class AllocateForNewStore {

        @Specialization
        protected static Object allocate(int[] store, int[] newStore, int length) {
            return IntegerArrayStore.INTEGER_ARRAY_ALLOCATOR.allocate(length);
        }

        @Specialization
        protected static Object allocate(int[] store, long[] newStore, int length) {
            return LongArrayStore.LONG_ARRAY_ALLOCATOR.allocate(length);
        }

        @Specialization
        protected static Object allocate(int[] store, double[] newStore, int length) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR.allocate(length);
        }

        @Specialization
        protected static Object allocate(int[] store, Object[] newStore, int length) {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR.allocate(length);
        }

        @Specialization(guards = "!basicStore(newStore)", limit = "storageStrategyLimit()")
        protected static Object allocate(int[] store, Object newStore, int length,
                @CachedLibrary("newStore") ArrayStoreLibrary newStores) {
            return newStores.allocateForNewStore(newStore, store, length);
        }
    }

    @ExportMessage
    protected static class IsDefaultValue {

        @Specialization
        protected static boolean isDefaultValue(int[] store, int value) {
            return value == 0;
        }

        @Fallback
        protected static boolean isDefaultValue(int[] store, Object value) {
            return false;
        }
    }

    @ExportMessage
    protected static ArrayAllocator allocator(int[] store) {
        return INTEGER_ARRAY_ALLOCATOR;
    }

    public static final ArrayAllocator INTEGER_ARRAY_ALLOCATOR = new IntegerArrayAllocator();

    private static class IntegerArrayAllocator extends ArrayAllocator {

        @Override
        public int[] allocate(int capacity) {
            return new int[capacity];
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
