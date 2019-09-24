/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import java.util.Set;

import org.truffleruby.core.array.ArrayStoreLibrary.ArrayAllocator;
import org.truffleruby.language.objects.ObjectGraphNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;

@ExportLibrary(ArrayStoreLibrary.class)
@GenerateUncached
public class DelegatedArrayStorage implements ObjectGraphNode {

    public final Object storage;
    public final int offset;
    public final int length;

    @ExportMessage
    public Object read(long index,
            @CachedLibrary(limit = "5") ArrayStoreLibrary stores) {
        return stores.read(storage, index + offset);
    }

    @ExportMessage
    public boolean acceptsValue(Object value) {
        return false;
    }

    @ExportMessage
    public boolean acceptsAllValues(Object otherStore) {
        return false;
    }

    @ExportMessage
    public boolean isMutable() {
        return false;
    }

    @ExportMessage
    public boolean isPrimitive(@CachedLibrary(limit = "5") ArrayStoreLibrary stores) {
        return stores.isPrimitive(storage);
    }

    @ExportMessage
    @TruffleBoundary
    public String toString(@CachedLibrary(limit = "5") ArrayStoreLibrary stores) {
        return String.format("Delegate of (%s)", stores.toString(storage));
    }

    @ExportMessage
    public void write(long index, Object value) {
    }

    @ExportMessage
    public long capacity() {
        return length;
    }

    @ExportMessage
    public Object expand(long capacity) {
        return DelegatedArrayStorage.create(storage, offset, (int) capacity);
    }

    @ExportMessage
    public Object extractRange(long start, long end) {
        return DelegatedArrayStorage.create(storage, (int) (offset + start), (int) (end - start));
    }

    @ExportMessage
    public void copyContents(long srcStart, Object destStore, long destStart, long length,
            @CachedLibrary(limit = "5") ArrayStoreLibrary srcStores,
            @CachedLibrary(limit = "5") ArrayStoreLibrary destStores) {
        for (int i = 0; i < length; i++) {
            destStores.write(destStore, i + destStart, srcStores.read(storage, srcStart + offset + i));
        }
    }

    @ExportMessage
    public Object copyStore(long length,
            @CachedLibrary(limit = "5") ArrayStoreLibrary stores) {
        Object newStore = stores.allocator(storage).allocate(length);
        stores.copyContents(storage, 0, newStore, offset, length);
        return newStore;
    }

    @ExportMessage
    public void sort(long size) {
    }

    @ExportMessage
    public Iterable<Object> getIterable(long from, long length,
            @CachedLibrary(limit = "5") ArrayStoreLibrary stores) {
        return stores.getIterable(storage, from + offset, length);
    }

    @ExportMessage
    ArrayAllocator generalizeForValue(Object newValue,
            @CachedLibrary(limit = "4") ArrayStoreLibrary stores) {
        return stores.generalizeForValue(storage, newValue);
    }

    @ExportMessage
    ArrayAllocator generalizeForStore(Object newStore,
            @CachedLibrary(limit = "4") ArrayStoreLibrary stores) {
        return stores.generalizeForStore(newStore, storage);
    }

    @ExportMessage
    ArrayAllocator allocator(@CachedLibrary(limit = "4") ArrayStoreLibrary stores) {
        return stores.allocator(storage);
    }

    protected DelegatedArrayStorage(Object storage, int offset, int length) {
        assert offset >= 0;
        assert length >= 0;
        this.storage = storage;
        this.offset = offset;
        this.length = length;
    }

    public static DelegatedArrayStorage create(Object storage, int offset, int length) {
        assert !(storage instanceof DelegatedArrayStorage);
        return new DelegatedArrayStorage(storage, offset, length);
    }

    public boolean hasObjectArrayStorage() {
        return storage != null && storage.getClass() == Object[].class;
    }

    @Override
    public void getAdjacentObjects(Set<DynamicObject> reachable) {
        if (hasObjectArrayStorage()) {
            final Object[] objectArray = (Object[]) storage;

            for (int i = offset; i < offset + length; i++) {
                final Object value = objectArray[i];
                if (value instanceof DynamicObject) {
                    reachable.add((DynamicObject) value);
                }
            }
        }
    }

}
