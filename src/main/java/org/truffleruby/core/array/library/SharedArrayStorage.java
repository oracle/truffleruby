/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array.library;

import java.util.Set;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.library.ArrayStoreLibrary.ArrayAllocator;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(ArrayStoreLibrary.class)
@GenerateUncached
@ImportStatic(ArrayGuards.class)
public class SharedArrayStorage implements ObjectGraphNode {

    public final Object storage;

    public SharedArrayStorage(Object backingStore) {
        this.storage = backingStore;
    }

    @ExportMessage
    protected static boolean accepts(SharedArrayStorage store,
            @CachedLibrary(limit = "1") ArrayStoreLibrary backingStores) {
        return backingStores.accepts(store.storage);
    }

    @ExportMessage
    protected Object read(int index,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.read(storage, index);
    }

    @ExportMessage
    protected boolean isPrimitive(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.isPrimitive(storage);
    }

    @ExportMessage
    @TruffleBoundary
    protected String toString(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return String.format("Shared storage of (%s)", stores.toString(storage));
    }

    @ExportMessage
    protected int capacity(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.capacity(storage);
    }

    @ExportMessage
    protected Object expand(int capacity,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return new SharedArrayStorage(stores.expand(storage, capacity));
    }

    @ExportMessage
    protected Object extractRange(int start, int end,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.extractRange(storage, start, end);
    }

    @ExportMessage
    protected Object[] boxedCopyOfRange(int start, int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.boxedCopyOfRange(storage, start, length);
    }

    @ExportMessage
    protected void copyContents(int srcStart, Object destStore, int destStart, int length,
            @CachedLibrary("this") ArrayStoreLibrary node,
            @Cached LoopConditionProfile loopProfile,
            @CachedLibrary(limit = "1") ArrayStoreLibrary srcStores,
            @CachedLibrary(limit = "storageStrategyLimit()") ArrayStoreLibrary destStores) {
        int i = 0;
        try {
            for (; loopProfile.inject(i < length); i++) {
                destStores.write(destStore, i + destStart, srcStores.read(storage, srcStart + i));
                TruffleSafepoint.poll(destStores);
            }
        } finally {
            RubyBaseNode.profileAndReportLoopCount(node.getNode(), loopProfile, i);
        }
    }

    @ExportMessage
    protected Object toJavaArrayCopy(int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.toJavaArrayCopy(storage, length);
    }

    @ExportMessage
    protected void sort(int size,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        stores.sort(storage, size);
    }

    @ExportMessage
    protected Iterable<Object> getIterable(int from, int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.getIterable(storage, from, length);
    }

    @ExportMessage
    protected ArrayAllocator generalizeForValue(Object newValue,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.generalizeForValue(storage, newValue);
    }

    @ExportMessage
    protected ArrayAllocator generalizeForStore(Object newStore,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.generalizeForStore(newStore, storage);
    }

    @ExportMessage
    protected Object allocateForNewValue(Object newValue, int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.allocateForNewValue(storage, newValue, length);
    }

    @ExportMessage
    protected Object allocateForNewStore(Object newStore, int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.allocateForNewStore(storage, newStore, length);
    }

    @ExportMessage
    protected boolean isDefaultValue(Object value,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.isDefaultValue(storage, value);
    }

    @ExportMessage
    protected ArrayAllocator allocator(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.allocator(storage);
    }

    public boolean hasObjectArrayStorage() {
        return storage != null && storage.getClass() == Object[].class;
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        if (hasObjectArrayStorage()) {
            final Object[] objectArray = (Object[]) storage;

            for (int i = 0; i < objectArray.length; i++) {
                final Object value = objectArray[i];
                if (ObjectGraph.isRubyObject(value)) {
                    reachable.add(value);
                }
            }
        } else if (storage instanceof ObjectGraphNode) {
            ((ObjectGraphNode) storage).getAdjacentObjects(reachable);
        }
    }
}
