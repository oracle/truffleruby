/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
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

import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.library.ArrayStoreLibrary.ArrayAllocator;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.shared.WriteBarrierNode;

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
    static class IsStorageSame {

        @Specialization
        protected static boolean sameZeroLength(Object[] store, Object[] other) {
            return store == other;
        }

        @Specialization
        protected static boolean sameDelegated(Object[] store, DelegatedArrayStorage other,
                @CachedLibrary(limit = "1") ArrayStoreLibrary others) {
            return others.isStorageSame(other, store);
        }

        @Specialization
        protected static boolean sameShared(Object[] store, SharedArrayStorage other,
                @CachedLibrary(limit = "1") ArrayStoreLibrary others) {
            return others.isStorageSame(other, store);
        }

        @Specialization
        protected static boolean sameShared(Object[] store, Object other) {
            return false;
        }

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
    protected static Object makeShared(Object[] store,
            @CachedLibrary("store") ArrayStoreLibrary stores) {
        stores.shareChildren(store);
        return new SharedArrayStorage(store);
    }

    @ExportMessage
    static class ShareChildren {

        @Specialization
        protected static void shareChildren(Object[] store,
                @CachedLibrary("store") ArrayStoreLibrary node,
                @Cached @Exclusive LoopConditionProfile loopProfile,
                @Cached WriteBarrierNode writeBarrierNode) {
            int i = 0;
            try {
                for (; i < store.length; i++) {
                    writeBarrierNode.executeWriteBarrier(store[i]);
                }
            } finally {
                RubyBaseNode.profileAndReportLoopCount(node, loopProfile, i);
            }
        }
    }

    @ExportMessage
    @ImportStatic(ArrayGuards.class)
    static class CopyContents {

        @Specialization
        protected static void copyContents(
                Object[] srcStore, int srcStart, Object[] destStore, int destStart, int length) {
            System.arraycopy(srcStore, srcStart, destStore, destStart, length);
        }

        @Specialization(guards = "!isObjectStore(destStore)", limit = "storageStrategyLimit()")
        protected static void copyContents(Object[] srcStore, int srcStart, Object destStore, int destStart, int length,
                @Cached @Exclusive LoopConditionProfile loopProfile,
                @CachedLibrary("destStore") ArrayStoreLibrary destStores) {
            int i = 0;
            try {
                for (; loopProfile.inject(i < length); i++) {
                    destStores.write(destStore, destStart + i, srcStore[srcStart + i]);
                    TruffleSafepoint.poll(destStores);
                }
            } finally {
                RubyBaseNode.profileAndReportLoopCount(destStores.getNode(), loopProfile, i);
            }
        }

        protected static boolean isObjectStore(Object store) {
            return store instanceof Object[];
        }
    }

    @ExportMessage
    protected static void clear(Object[] store, int start, int length,
            @CachedLibrary("store") ArrayStoreLibrary node,
            @Cached @Exclusive LoopConditionProfile profile) {
        ArrayUtils.fill(store, start, start + length, null, node, profile);
    }

    @ExportMessage
    protected static void fill(Object[] store, int start, int length, Object value,
            @CachedLibrary("store") ArrayStoreLibrary node,
            @Cached @Exclusive LoopConditionProfile profile) {
        ArrayUtils.fill(store, start, start + length, value, node, profile);
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
    protected static ArrayAllocator generalizeForSharing(Object[] store) {
        return SharedArrayStorage.SHARED_OBJECT_ARRAY_ALLOCATOR;
    }

    @ExportMessage
    protected static Object allocateForNewValue(Object[] store, Object newValue, int length) {
        return OBJECT_ARRAY_ALLOCATOR.allocate(length);
    }

    @ExportMessage
    protected static Object allocateForNewStore(Object[] store, Object newValue, int length) {
        return OBJECT_ARRAY_ALLOCATOR.allocate(length);
    }

    @ExportMessage
    protected static boolean isDefaultValue(Object[] store, Object value) {
        return value == null;
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
