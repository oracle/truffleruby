/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import java.util.Arrays;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.library.CachedLibrary;

import org.truffleruby.cext.WrapNode;
import org.truffleruby.cext.UnwrapNode.UnwrapNativeNode;
import org.truffleruby.core.array.NativeArrayNodesFactory.NativeArrayCapacityNodeGen;
import org.truffleruby.core.array.NativeArrayNodesFactory.NativeArrayCopyStoreNodeGen;
import org.truffleruby.core.array.NativeArrayNodesFactory.NativeArrayCopyToNodeGen;
import org.truffleruby.core.array.NativeArrayNodesFactory.NativeArrayExtractRangeNodeGen;
import org.truffleruby.core.array.NativeArrayNodesFactory.NativeArrayGetNodeGen;
import org.truffleruby.core.array.NativeArrayNodesFactory.NativeArrayNewStoreNodeGen;
import org.truffleruby.core.array.NativeArrayNodesFactory.NativeArraySetNodeGen;
import org.truffleruby.core.array.NativeArrayNodesFactory.NativeArraySortNodeGen;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.control.RaiseException;

public class NativeArrayNodes {
    public static abstract class NativeArrayCapacityNode extends ArrayOperationNodes.ArrayCapacityNode {

        @Specialization
        protected int length(NativeArrayStorage storage) {
            return storage.length;
        }

        public static NativeArrayCapacityNode create() {
            return NativeArrayCapacityNodeGen.create();
        }
    }

    public static abstract class NativeArrayGetNode extends ArrayOperationNodes.ArrayGetNode {

        @Specialization
        protected Object get(NativeArrayStorage storage, int index,
                @Cached UnwrapNativeNode unwrapNode) {
            return unwrapNode.execute(storage.pointer.readLong(8 * index));
        }

        public static NativeArrayGetNode create() {
            return NativeArrayGetNodeGen.create();
        }
    }

    public static abstract class NativeArraySetNode extends ArrayOperationNodes.ArraySetNode {

        @Specialization(rewriteOn = UnsupportedMessageException.class)
        protected void set(NativeArrayStorage storage, int index, Object object,
                @Cached WrapNode wrapNode,
                @CachedLibrary(limit = "1") InteropLibrary values) throws UnsupportedMessageException {
            long value = values.asPointer(wrapNode.execute(object));
            storage.pointer.writeLong(8 * index, value);
        }

        @Specialization(replaces = "set")
        protected void setGeneric(NativeArrayStorage storage, int index, Object object,
                @Cached WrapNode wrapNode,
                @CachedLibrary(limit = "1") InteropLibrary values) {
            try {
                storage.pointer.writeLong(8 * index, values.asPointer(wrapNode.execute(object)));
            } catch (UnsupportedMessageException e) {
                throw new RaiseException(getContext(), getContext().getCoreExceptions().argumentError("Could not convert value for native storage", this));
            }
        }

        public static NativeArraySetNode create() {
            return NativeArraySetNodeGen.create();
        }
    }

    public static abstract class NativeArrayNewStoreNode extends ArrayOperationNodes.ArrayNewStoreNode {

        @Specialization
        protected NativeArrayStorage newStore(int size) {
            Pointer pointer = Pointer.malloc(size);
            pointer.enableAutorelease(getContext().getFinalizationService());
            return new NativeArrayStorage(pointer, size);
        }

        public static NativeArrayNewStoreNode create() {
            return NativeArrayNewStoreNodeGen.create();
        }
    }

    public static abstract class NativeArrayCopyStoreNode extends ArrayOperationNodes.ArrayCopyStoreNode {

        @Specialization
        protected Object[] newStoreCopying(NativeArrayStorage store, int size,
                @Cached UnwrapNativeNode unwrapNode) {
            Object[] newStore = new Object[size];
            assert size >= store.length;
            for (int i = 0; i < store.length; i++) {
                newStore[i] = unwrapNode.execute(store.pointer.readLong(8 * i));
            }
            return newStore;
        }

        public static NativeArrayCopyStoreNode create() {
            return NativeArrayCopyStoreNodeGen.create();
        }
    }

    public static abstract class NativeArrayCopyToNode extends ArrayOperationNodes.ArrayCopyToNode {

        @Specialization(guards = "toStrategy.matchesStore(to)")
        protected void copyToOther(NativeArrayStorage from, Object to, int sourceStart, int destinationStart, int length,
                @Cached("ofStore(to)") ArrayStrategy toStrategy,
                @Cached("toStrategy.setNode()") ArrayOperationNodes.ArraySetNode setNode,
                @Cached UnwrapNativeNode unwrapNode) {
            for (int i = 0; i < length; i++) {
                setNode.execute(to, destinationStart + i, unwrapNode.execute(from.pointer.readLong(8 * (sourceStart + i))));
            }
        }

        public static NativeArrayCopyToNode create() {
            return NativeArrayCopyToNodeGen.create();
        }
    }

    public static abstract class NativeArrayExtractRangeNode extends ArrayOperationNodes.ArrayExtractRangeNode {

        @Specialization
        protected Object[] extractRange(NativeArrayStorage store, int start, int end,
                @Cached UnwrapNativeNode unwrapNode) {
            Object[] newStore = new Object[end - start];
            for (int i = start; i < end; i++) {
                newStore[i] = unwrapNode.execute(store.pointer.readLong(8 * (i)));
            }
            return newStore;
        }

        public static NativeArrayExtractRangeNode create() {
            return NativeArrayExtractRangeNodeGen.create();
        }
    }

    public static abstract class NativeArraySortNode extends ArrayOperationNodes.ArraySortNode {

        @Specialization
        protected void sort(NativeArrayStorage store, int size,
                @Cached NativeArrayGetNode getNode,
                @Cached NativeArraySetNode setNode) {
            Object[] elements = new Object[size];
            for (int i = 0; i < size; i++) {
                elements[i] = getNode.execute(store, i);
            }
            Arrays.sort(elements, 0, size);
            for (int i = 0; i < size; i++) {
                setNode.execute(store, i, elements[i]);
            }
        }

        public static NativeArraySortNode create() {
            return NativeArraySortNodeGen.create();
        }
    }
}
