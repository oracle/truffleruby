/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.truffleruby.cext.UnwrapNode;
import org.truffleruby.cext.ValueWrapper;
import org.truffleruby.cext.WrapNode;
import org.truffleruby.cext.UnwrapNodeGen.UnwrapNativeNodeGen;
import org.truffleruby.core.array.ArrayStoreLibrary.ArrayAllocator;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.objects.ObjectGraphNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.dsl.Cached.Shared;
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
        this.pointer = pointer;
        this.length = length;
        this.markedObjects = new Object[length];
    }

    private NativeArrayStorage(Pointer pointer, int length, Object[] markedObjects) {
        this.pointer = pointer;
        this.length = length;
        this.markedObjects = markedObjects;
    }

    @ExportMessage
    public boolean acceptsValue(Object value) {
        return true;
    }

    @ExportMessage
    public boolean acceptsAllValues(Object otherStore) {
        return true;
    }

    @ExportMessage
    public boolean isMutable() {
        return true;
    }

    @ExportMessage
    public boolean isPrimitive() {
        return false;
    }

    @ExportMessage
    public boolean isNative() {
        return true;
    }

    @ExportMessage
    public static String toString(NativeArrayStorage storage) {
        return "NativeArrayStorage";
    }

    @ExportMessage
    public static abstract class Read {

        @Specialization
        static Object read(NativeArrayStorage storage, long index,
                @Shared("unwrap") @Cached UnwrapNode unwrapNode) {
            return unwrapNode.execute(storage.readElement((int) index));
        }
    }

    @ExportMessage
    public static abstract class Write {

        @Specialization
        static void write(NativeArrayStorage storage, long index, Object value,
                @CachedLibrary(limit = "1") InteropLibrary wrappers,
                @Cached() WrapNode wrapNode,
                @Cached BranchProfile errorProfile) {
            ValueWrapper wrapper = wrapNode.execute(value);
            try {
                storage.writeElement((int) index, wrappers.asPointer(wrapper));
            } catch (UnsupportedMessageException e) {
                errorProfile.enter();
                throw new UnsupportedOperationException(e);
            }
        }
    }

    @ExportMessage
    public long capacity() {
        return length;
    }

    @ExportMessage
    public static abstract class Expand {

        @Specialization
        static NativeArrayStorage expand(NativeArrayStorage storage, long newCapacity) {
            Pointer newPointer = Pointer.malloc(storage.capacity());
            newPointer.writeBytes(0, storage.pointer, 0, storage.capacity());
            newPointer.writeBytes(storage.capacity(), newCapacity - storage.capacity(), (byte) 0);
            Object[] newMarkedObjects = new Object[(int) newCapacity];
            /* We copy the contents of the marked objects to ensure the references will be kept alive even if the old
             * store becomes unreachable. */
            System.arraycopy(storage.markedObjects, 0, newMarkedObjects, 0, storage.length);
            return new NativeArrayStorage(newPointer, (int) newCapacity, newMarkedObjects);
        }
    }

    @ExportMessage
    public static abstract class CopyContents {

        @Specialization
        static void copyContents(NativeArrayStorage srcStore, long srcStart, Object destStore, long destStart,
                long length,
                @CachedLibrary("srcStore") ArrayStoreLibrary srcStores,
                @CachedLibrary(limit = "5") ArrayStoreLibrary destStores) {
            for (long i = srcStart; i < length; i++) {
                destStores.write(destStore, destStart + i, srcStores.read(srcStore, (srcStart + i)));
            }
        }
    }

    @ExportMessage
    public Object[] copyStore(long size,
            @Shared("unwrap") @Cached UnwrapNode unwrapNode) {
        Object[] newStore = new Object[(int) size];
        assert size >= length;
        for (int i = 0; i < length; i++) {
            newStore[i] = unwrapNode.execute(readElement(i));
        }
        return newStore;
    }

    @ExportMessage
    public void sort(long size,
            @CachedLibrary(limit = "1") ArrayStoreLibrary stores) {
        Object[] elements = new Object[(int) size];
        for (int i = 0; i < size; i++) {
            elements[i] = stores.read(this, i);
        }
        Arrays.sort(elements, 0, (int) size);
        for (int i = 0; i < size; i++) {
            stores.write(this, i, elements[i]);
        }
    }

    @ExportMessage
    public static Iterable<Object> getIterable(NativeArrayStorage store, long from, long length,
            @CachedLibrary("store") ArrayStoreLibrary stores) {
        return () -> new Iterator<Object>() {

            private int n = (int) from;

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
    public ArrayAllocator allocator() {
        return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
    }

    @ExportMessage
    ArrayAllocator generalizeForValue(Object newValue) {
        return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
    }

    @ExportMessage
    ArrayAllocator generalizeForStore(Object newStore) {
        return ObjectArrayStore.OBJECT_ARRAY_ALLOCATOR;
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
