/*
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.library.ArrayStoreLibrary.ArrayAllocator;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(ArrayStoreLibrary.class)
@GenerateUncached
@ImportStatic(ArrayGuards.class)
public class DelegatedArrayStorage implements ObjectGraphNode {

    public final Object storage;
    public final int offset;
    public final int length;

    @ExportMessage
    protected static boolean accepts(DelegatedArrayStorage store,
            @CachedLibrary(limit = "1") ArrayStoreLibrary backingStores) {
        return backingStores.accepts(store.storage);
    }

    @ExportMessage
    protected Object read(int index,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.read(storage, index + offset);
    }

    @ExportMessage
    protected boolean isPrimitive(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.isPrimitive(storage);
    }

    @ExportMessage
    @ImportStatic(ArrayGuards.class)
    static class IsSameStorage {

        @Specialization
        protected static boolean sameDelegated(DelegatedArrayStorage store, DelegatedArrayStorage other,
                @CachedLibrary(limit = "storageStrategyLimit()") ArrayStoreLibrary stores) {
            return store.offset == other.offset && stores.isSameStorage(store.storage, other.storage);
        }

        @Specialization
        protected static boolean sameShared(DelegatedArrayStorage store, SharedArrayStorage other,
                @CachedLibrary(limit = "storageStrategyLimit()") ArrayStoreLibrary stores) {
            return stores.isSameStorage(other.storage, store);
        }

        @Fallback
        protected static boolean sameOther(DelegatedArrayStorage store, Object other,
                @CachedLibrary(limit = "storageStrategyLimit()") ArrayStoreLibrary stores) {
            return store.offset == 0 && stores.isSameStorage(store.storage, other);
        }

    }

    @ExportMessage
    public Object backingStore(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.backingStore(storage);
    }

    @ExportMessage
    protected Object makeShared(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        stores.shareElements(this);
        return new SharedArrayStorage(this);
    }

    @ExportMessage
    protected void shareElements(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        stores.shareElements(storage);
    }

    @ExportMessage
    @TruffleBoundary
    protected String toString(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return String.format("Delegate of (%s)", stores.toString(storage));
    }

    @ExportMessage
    protected int capacity() {
        return length;
    }

    @ExportMessage
    protected Object expand(int capacity) {
        return new DelegatedArrayStorage(storage, offset, capacity);
    }

    @ExportMessage
    protected Object extractRange(int start, int end) {
        return new DelegatedArrayStorage(storage, (offset + start), (end - start));
    }

    @ExportMessage
    protected Object[] boxedCopyOfRange(int start, int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.boxedCopyOfRange(storage, offset + start, length);
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
                destStores.write(destStore, i + destStart, srcStores.read(storage, srcStart + offset + i));
                TruffleSafepoint.poll(destStores);
            }
        } finally {
            RubyBaseNode.profileAndReportLoopCount(node.getNode(), loopProfile, i);
        }
    }

    @ExportMessage
    protected Object toJavaArrayCopy(int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        Object newStore = stores.allocator(storage).allocate(length);
        stores.copyContents(storage, offset, newStore, 0, length);
        return newStore;
    }

    @ExportMessage
    protected void sort(int size) {
        CompilerDirectives.transferToInterpreterAndInvalidate();
        throw new UnsupportedOperationException();
    }

    @ExportMessage
    protected Iterable<Object> getIterable(int from, int length,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.getIterable(storage, from + offset, length);
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
    public ArrayAllocator generalizeForSharing(
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        return stores.generalizeForSharing(storage);
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
    public void getAdjacentObjects(Set<Object> reachable) {
        if (hasObjectArrayStorage()) {
            final Object[] objectArray = (Object[]) storage;

            for (int i = offset; i < offset + length; i++) {
                final Object value = objectArray[i];
                if (ObjectGraph.isRubyObject(value)) {
                    reachable.add(value);
                }
            }
        }
    }

    public boolean isEquivalentTo(DelegatedArrayStorage other) {
        return storage == other.storage && offset == other.offset && length == other.length;
    }

}
