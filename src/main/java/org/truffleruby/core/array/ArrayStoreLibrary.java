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

import com.oracle.truffle.api.library.GenerateLibrary;
import com.oracle.truffle.api.library.Library;
import com.oracle.truffle.api.library.LibraryFactory;
import com.oracle.truffle.api.library.GenerateLibrary.Abstract;
import com.oracle.truffle.api.library.GenerateLibrary.DefaultExport;

@GenerateLibrary
@DefaultExport(IntegerArrayStore.class)
@DefaultExport(LongArrayStore.class)
@DefaultExport(DoubleArrayStore.class)
@DefaultExport(ObjectArrayStore.class)
public abstract class ArrayStoreLibrary extends Library {

    public static final Object INITIAL_STORE = ZeroLengthArrayStore.ZERO_LENGTH_STORE;

    private static final LibraryFactory<ArrayStoreLibrary> FACTORY = LibraryFactory.resolve(ArrayStoreLibrary.class);

    public static LibraryFactory<ArrayStoreLibrary> getFactory() {
        return FACTORY;
    }

    // Read a value from the store
    public abstract Object read(Object store, int index);

    // Return whether the store can accept this value.
    @Abstract(ifExported = { "write", "acceptsAllValues", "isMutable", "sort" })
    public boolean acceptsValue(Object store, Object value) {
        return false;
    }

    // Return whether the store can accept all values that could be held in otherStore.
    @Abstract(ifExported = { "write", "acceptsValue", "isMutable", "sort" })
    public boolean acceptsAllValues(Object store, Object otherStore) {
        return false;
    }

    @Abstract(ifExported = { "write", "acceptsValue", "acceptsAllValues", "sort" })
    public boolean isMutable(Object store) {
        return false;
    }

    public boolean isNative(Object store) {
        return false;
    }

    public abstract boolean isPrimitive(Object store);

    public abstract String toString(Object store);

    @Abstract(ifExported = { "acceptsValue", "acceptsAllValues", "isMutable", "sort" })
    public void write(Object store, int index, Object value) {
        throw new UnsupportedOperationException();
    }

    public abstract int capacity(Object store);

    public abstract Object expand(Object store, int capacity);

    public Object extractRange(Object store, int start, int end) {
        return DelegatedArrayStorage.create(store, start, (end - start));
    }

    public abstract void copyContents(Object store, int srcStart, Object dest, int destStart, int length);

    public abstract Object copyStore(Object store, int length);

    @Abstract(ifExported = { "acceptsValue", "acceptsAllValues", "isMutable", "write" })
    public void sort(Object store, int size) {
        throw new UnsupportedOperationException();
    }

    public abstract Iterable<Object> getIterable(Object store, int start, int length);

    public abstract ArrayAllocator generalizeForValue(Object store, Object newValue);

    public abstract ArrayAllocator generalizeForStore(Object store, Object newStore);

    public abstract ArrayAllocator allocator(Object store);

    public static ArrayAllocator basicAllocator(Object value) {
        if (value instanceof Integer) {
            return IntegerArrayStore.INTEGER_ARRAY_ALLOCATOR;
        } else if (value instanceof Long) {
            return LongArrayStore.LONG_ARRAY_ALLOCATOR;
        } else if (value instanceof Double) {
            return DoubleArrayStore.DOUBLE_ARRAY_ALLOCATOR;
        } else {
            return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
        }
    }

    public static abstract class ArrayAllocator {

        public abstract Object allocate(int capacity);

        public abstract boolean accepts(Object value);

        public abstract boolean specializesFor(Object value);

        public abstract boolean isDefaultValue(Object value);

    }
}
