/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array.library;

import java.util.Set;

import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.library.ArrayStoreLibrary.ArrayAllocator;
import org.truffleruby.language.objects.ObjectGraphNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;

@ExportLibrary(ArrayStoreLibrary.class)
@GenerateUncached
@ImportStatic(ArrayGuards.class)
public class DelegatedArrayStorage implements ObjectGraphNode {

    public final Object storage;
    public final int offset;
    public final int length;

    @ExportMessage
    public Object read(int index,
            @CachedLibrary(limit = "STORAGE_STRATEGIES") ArrayStoreLibrary stores) {
        return stores.read(storage, index + offset);
    }

    @ExportMessage
    public boolean isPrimitive(@CachedLibrary(limit = "STORAGE_STRATEGIES") ArrayStoreLibrary stores) {
        return stores.isPrimitive(storage);
    }

    @ExportMessage
    @TruffleBoundary
    public String toString(@CachedLibrary(limit = "STORAGE_STRATEGIES") ArrayStoreLibrary stores) {
        return String.format("Delegate of (%s)", stores.toString(storage));
    }

    @ExportMessage
    public int capacity() {
        return length;
    }

    @ExportMessage
    public Object expand(int capacity) {
        return new DelegatedArrayStorage(storage, offset, capacity);
    }

    @ExportMessage
    public Object extractRange(int start, int end) {
        return new DelegatedArrayStorage(storage, (offset + start), (end - start));
    }

    @ExportMessage
    public void copyContents(int srcStart, Object destStore, int destStart, int length,
            @CachedLibrary(limit = "STORAGE_STRATEGIES") ArrayStoreLibrary srcStores,
            @CachedLibrary(limit = "STORAGE_STRATEGIES") ArrayStoreLibrary destStores) {
        for (int i = 0; i < length; i++) {
            destStores.write(destStore, i + destStart, srcStores.read(storage, srcStart + offset + i));
        }
    }

    @ExportMessage
    public Object toJavaArrayCopy(int length,
            @CachedLibrary(limit = "STORAGE_STRATEGIES") ArrayStoreLibrary stores) {
        Object newStore = stores.allocator(storage).allocate(length);
        stores.copyContents(storage, 0, newStore, offset, length);
        return newStore;
    }

    @ExportMessage
    public void sort(int size) {
        throw new UnsupportedOperationException();
    }

    @ExportMessage
    public Iterable<Object> getIterable(int from, int length,
            @CachedLibrary(limit = "STORAGE_STRATEGIES") ArrayStoreLibrary stores) {
        return stores.getIterable(storage, from + offset, length);
    }

    @ExportMessage
    ArrayAllocator generalizeForValue(Object newValue,
            @CachedLibrary(limit = "STORAGE_STRATEGIES") ArrayStoreLibrary stores) {
        return stores.generalizeForValue(storage, newValue);
    }

    @ExportMessage
    ArrayAllocator generalizeForStore(Object newStore,
            @CachedLibrary(limit = "STORAGE_STRATEGIES") ArrayStoreLibrary stores) {
        return stores.generalizeForStore(newStore, storage);
    }

    @ExportMessage
    ArrayAllocator allocator(@CachedLibrary(limit = "STORAGE_STRATEGIES") ArrayStoreLibrary stores) {
        return stores.allocator(storage);
    }

    public DelegatedArrayStorage(Object storage, int offset, int length) {
        assert offset >= 0;
        assert length >= 0;
        assert !(storage instanceof DelegatedArrayStorage);
        this.storage = storage;
        this.offset = offset;
        this.length = length;
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
