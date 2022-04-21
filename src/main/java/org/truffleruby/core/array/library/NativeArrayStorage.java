/*
 * Copyright (c) 2019, 2021 Oracle and/or its affiliates. All rights reserved. This
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
import java.util.Set;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.truffleruby.cext.UnwrapNode;
import org.truffleruby.cext.UnwrapNodeGen.UnwrapNativeNodeGen;
import org.truffleruby.cext.ValueWrapper;
import org.truffleruby.cext.WrapNode;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.library.ArrayStoreLibrary.ArrayAllocator;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;
import org.truffleruby.language.objects.shared.WriteBarrierNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;

@ExportLibrary(ArrayStoreLibrary.class)
@GenerateUncached
@ImportStatic(ArrayGuards.class)
public final class NativeArrayStorage implements ObjectGraphNode {

    private final Pointer pointer;
    /** Used to keep elements alive */
    private final Object[] markedObjects;
    public final int length;

    public NativeArrayStorage(Pointer pointer, int length) {
        this(pointer, length, new Object[length]);
    }

    private NativeArrayStorage(Pointer pointer, int length, Object[] markedObjects) {
        this.pointer = pointer;
        this.length = length;
        this.markedObjects = markedObjects;
    }

    @ExportMessage
    protected boolean acceptsValue(Object value) {
        return true;
    }

    @ExportMessage
    protected boolean acceptsAllValues(Object otherStore) {
        return true;
    }

    @ExportMessage
    protected boolean isMutable() {
        return true;
    }

    @ExportMessage
    protected boolean isNative() {
        return true;
    }

    @ExportMessage
    static class IsSameStorage {

        @Specialization
        protected static boolean sameNativeStore(NativeArrayStorage store, NativeArrayStorage other) {
            return store == other;
        }

        @Specialization
        protected static boolean sameDelegated(NativeArrayStorage store, DelegatedArrayStorage other,
                @CachedLibrary(limit = "1") ArrayStoreLibrary others) {
            return others.isSameStorage(other, store);
        }

        @Specialization
        protected static boolean sameShared(NativeArrayStorage store, SharedArrayStorage other,
                @CachedLibrary(limit = "1") ArrayStoreLibrary others) {
            return others.isSameStorage(other, store);
        }

        @Fallback
        protected static boolean sameShared(NativeArrayStorage store, Object other) {
            return false;
        }

    }

    @ExportMessage
    protected static String toString(NativeArrayStorage storage) {
        return "NativeArrayStorage";
    }

    @ExportMessage
    protected Object read(int index,
            @Shared("unwrap") @Cached UnwrapNode unwrapNode) {
        return unwrapNode.execute(readElement(index));
    }

    @ExportMessage
    protected void write(int index, Object value,
            @CachedLibrary(limit = "1") InteropLibrary wrappers,
            @Cached WrapNode wrapNode,
            @Cached ConditionProfile isPointerProfile) {
        final ValueWrapper wrapper = wrapNode.execute(value);
        if (!isPointerProfile.profile(wrappers.isPointer(wrapper))) {
            wrappers.toNative(wrapper);
        }

        final long address;
        try {
            assert wrappers.isPointer(wrapper);
            address = wrappers.asPointer(wrapper);
        } catch (UnsupportedMessageException e) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw new UnsupportedOperationException();
        }

        writeElement(index, address);
    }

    @ExportMessage
    protected int capacity() {
        return length;
    }

    @ExportMessage
    protected NativeArrayStorage expand(int newCapacity) {
        final int capacity = this.length;
        Pointer newPointer = Pointer.malloc(capacity);
        newPointer.writeBytes(0, pointer, 0, capacity);
        newPointer.writeBytes(capacity, newCapacity - capacity, (byte) 0);
        /* We copy the contents of the marked objects to ensure the references will be kept alive even if the old store
         * becomes unreachable. */
        Object[] newMarkedObjects = ArrayUtils.grow(markedObjects, newCapacity);
        return new NativeArrayStorage(newPointer, newCapacity, newMarkedObjects);
    }

    @ExportMessage
    protected Object[] boxedCopyOfRange(int start, int length,
            @Shared("unwrap") @Cached UnwrapNode unwrapNode) {
        Object[] newStore = new Object[length];
        for (int i = 0; i < length; i++) {
            newStore[i] = unwrapNode.execute(readElement(start + i));
        }
        return newStore;
    }

    @ExportMessage
    protected static Object makeShared(NativeArrayStorage store,
            @CachedLibrary("store") ArrayStoreLibrary stores) {
        stores.shareElements(store);
        return new SharedArrayStorage(store);
    }

    @ExportMessage
    static class ShareElements {

        @Specialization
        protected static void shareElements(NativeArrayStorage store,
                @CachedLibrary("store") ArrayStoreLibrary node,
                @Cached @Exclusive LoopConditionProfile loopProfile,
                @Cached WriteBarrierNode writeBarrierNode) {
            int i = 0;
            try {
                for (; i < store.length; i++) {
                    writeBarrierNode.executeWriteBarrier(node.read(store, i));
                }
            } finally {
                RubyBaseNode.profileAndReportLoopCount(node, loopProfile, i);
            }
        }
    }

    @ExportMessage
    protected void copyContents(int srcStart, Object destStore, int destStart, int length,
            @CachedLibrary("this") ArrayStoreLibrary srcStores,
            @Cached @Exclusive LoopConditionProfile loopProfile,
            @CachedLibrary(limit = "storageStrategyLimit()") ArrayStoreLibrary destStores) {
        int i = 0;
        try {
            for (; loopProfile.inject(i < length); i++) {
                destStores.write(destStore, destStart + i, srcStores.read(this, srcStart + i));
                TruffleSafepoint.poll(destStores);
            }
        } finally {
            RubyBaseNode.profileAndReportLoopCount(srcStores.getNode(), loopProfile, i);
        }
    }

    @ExportMessage
    protected void clear(int start, int length) {
        pointer.writeBytes(start * Pointer.SIZE, length * Pointer.SIZE, (byte) 0);
    }

    @ExportMessage
    protected static void fill(NativeArrayStorage store, int start, int length, Object value,
            @CachedLibrary("store") ArrayStoreLibrary stores) {
        for (int i = start; i < length; ++i) {
            stores.write(store, i, value);
        }
    }

    @ExportMessage
    protected Object[] toJavaArrayCopy(int size,
            @Shared("unwrap") @Cached UnwrapNode unwrapNode) {
        Object[] newStore = new Object[size];
        assert size >= length;
        for (int i = 0; i < length; i++) {
            newStore[i] = unwrapNode.execute(readElement(i));
        }
        return newStore;
    }

    @ExportMessage
    protected static Iterable<Object> getIterable(NativeArrayStorage store, int from, int length,
            @CachedLibrary("store") ArrayStoreLibrary stores) {
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

                final Object object = stores.read(store, n);
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
    protected boolean isDefaultValue(Object value) {
        return false;
    }

    @ExportMessage
    protected ArrayAllocator allocator() {
        return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
    }

    @ExportMessage
    protected ArrayAllocator generalizeForValue(Object newValue) {
        return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
    }

    @ExportMessage
    protected ArrayAllocator generalizeForStore(Object newStore) {
        return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
    }

    @ExportMessage
    public ArrayAllocator generalizeForSharing() {
        return SharedArrayStorage.SHARED_OBJECT_ARRAY_ALLOCATOR;
    }

    @ExportMessage
    protected Object allocateForNewValue(Object newValue, int length) {
        return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR.allocate(length);
    }

    @ExportMessage
    protected Object allocateForNewStore(Object newValue, int length) {
        return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR.allocate(length);
    }

    public long readElement(int index) {
        return pointer.readLong(index * Pointer.SIZE);
    }

    public void writeElement(int index, long value) {
        pointer.writeLong(index * Pointer.SIZE, value);
    }

    public long getAddress() {
        return pointer.getAddress();
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        for (int i = 0; i < length; i++) {
            final Object value = UnwrapNativeNodeGen.getUncached().execute(readElement(i));
            if (ObjectGraph.isRubyObject(value)) {
                reachable.add(value);
            }
        }
    }

    public void preserveMembers() {
        for (int i = 0; i < length; i++) {
            final Object value = UnwrapNativeNodeGen.getUncached().execute(readElement(i));
            markedObjects[i] = value;
        }
    }
}
