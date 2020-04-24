/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
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

import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.cext.UnwrapNode;
import org.truffleruby.cext.UnwrapNodeGen.UnwrapNativeNodeGen;
import org.truffleruby.cext.ValueWrapper;
import org.truffleruby.cext.WrapNode;
import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.library.ArrayStoreLibrary.ArrayAllocator;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.objects.ObjectGraphNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;

@ExportLibrary(ArrayStoreLibrary.class)
@GenerateUncached
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
    protected static String toString(NativeArrayStorage storage) {
        return "NativeArrayStorage";
    }

    @ExportMessage
    public static abstract class Read {

        @Specialization
        protected static Object read(NativeArrayStorage storage, int index,
                @Shared("unwrap") @Cached UnwrapNode unwrapNode) {
            return unwrapNode.execute(storage.readElement(index));
        }
    }

    @ExportMessage
    public static abstract class Write {

        @Specialization
        protected static void write(NativeArrayStorage storage, int index, Object value,
                @CachedLibrary(limit = "1") InteropLibrary wrappers,
                @Cached WrapNode wrapNode,
                @Cached ConditionProfile isPointerProfile,
                @Cached BranchProfile errorProfile) {
            final ValueWrapper wrapper = wrapNode.execute(value);
            if (!isPointerProfile.profile(wrappers.isPointer(wrapper))) {
                wrappers.toNative(wrapper);
            }
            try {
                assert wrappers.isPointer(wrapper);
                storage.writeElement(index, wrappers.asPointer(wrapper));
            } catch (UnsupportedMessageException e) {
                errorProfile.enter();
                throw new UnsupportedOperationException();
            }
        }
    }

    @ExportMessage
    protected int capacity() {
        return length;
    }

    @ExportMessage
    public static abstract class Expand {

        @Specialization
        protected static NativeArrayStorage expand(NativeArrayStorage storage, int newCapacity) {
            Pointer newPointer = Pointer.malloc(storage.capacity());
            newPointer.writeBytes(0, storage.pointer, 0, storage.capacity());
            newPointer.writeBytes(storage.capacity(), newCapacity - storage.capacity(), (byte) 0);
            /* We copy the contents of the marked objects to ensure the references will be kept alive even if the old
             * store becomes unreachable. */
            Object[] newMarkedObjects = ArrayUtils.grow(storage.markedObjects, newCapacity);
            return new NativeArrayStorage(newPointer, newCapacity, newMarkedObjects);
        }
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
    @ImportStatic(ArrayGuards.class)
    public static abstract class CopyContents {

        @Specialization
        protected static void copyContents(
                NativeArrayStorage srcStore,
                int srcStart,
                Object destStore,
                int destStart,
                int length,
                @CachedLibrary(limit = "1") ArrayStoreLibrary srcStores,
                @CachedLibrary(limit = "STORAGE_STRATEGIES") ArrayStoreLibrary destStores) {
            for (int i = srcStart; i < length; i++) {
                destStores.write(destStore, destStart + i, srcStores.read(srcStore, (srcStart + i)));
            }
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
    public void getAdjacentObjects(Set<DynamicObject> reachable) {
        for (int i = 0; i < length; i++) {
            final Object value = UnwrapNativeNodeGen.getUncached().execute(readElement(i));
            if (value instanceof DynamicObject) {
                reachable.add((DynamicObject) value);
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
