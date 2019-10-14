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

import java.util.Arrays;

import org.truffleruby.core.array.ObjectArrayNodesFactory.ObjectArrayCapacityNodeGen;
import org.truffleruby.core.array.ObjectArrayNodesFactory.ObjectArrayCopyStoreNodeGen;
import org.truffleruby.core.array.ObjectArrayNodesFactory.ObjectArrayCopyToNodeGen;
import org.truffleruby.core.array.ObjectArrayNodesFactory.ObjectArrayExtractRangeNodeGen;
import org.truffleruby.core.array.ObjectArrayNodesFactory.ObjectArrayGetNodeGen;
import org.truffleruby.core.array.ObjectArrayNodesFactory.ObjectArrayNewStoreNodeGen;
import org.truffleruby.core.array.ObjectArrayNodesFactory.ObjectArraySetNodeGen;
import org.truffleruby.core.array.ObjectArrayNodesFactory.ObjectArraySortNodeGen;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public class ObjectArrayNodes {
    public static abstract class ObjectArrayCapacityNode extends ArrayOperationNodes.ArrayCapacityNode {

        @Specialization
        protected int length(Object[] store) {
            return store.length;
        }

        public static ObjectArrayCapacityNode create() {
            return ObjectArrayCapacityNodeGen.create();
        }
    }

    public static abstract class ObjectArrayGetNode extends ArrayOperationNodes.ArrayGetNode {

        @Specialization
        protected Object get(Object[] store, int index) {
            return store[index];
        }

        public static ObjectArrayGetNode create() {
            return ObjectArrayGetNodeGen.create();
        }
    }

    public static abstract class ObjectArraySetNode extends ArrayOperationNodes.ArraySetNode {

        @Specialization
        protected void set(Object[] store, int index, Object value) {
            store[index] = value;
        }

        public static ObjectArraySetNode create() {
            return ObjectArraySetNodeGen.create();
        }
    }

    public static abstract class ObjectArrayNewStoreNode extends ArrayOperationNodes.ArrayNewStoreNode {

        @Specialization
        protected Object[] newStore(int size) {
            return new Object[size];
        }

        public static ObjectArrayNewStoreNode create() {
            return ObjectArrayNewStoreNodeGen.create();
        }
    }

    public static abstract class ObjectArrayCopyStoreNode extends ArrayOperationNodes.ArrayCopyStoreNode {

        @Specialization
        protected Object[] newStoreCopying(Object[] store, int size) {
            return ArrayUtils.grow(store, size);
        }

        public static ObjectArrayCopyStoreNode create() {
            return ObjectArrayCopyStoreNodeGen.create();
        }
    }

    public static abstract class ObjectArrayCopyToNode extends ArrayOperationNodes.ArrayCopyToNode {

        @Specialization
        protected void copyIntToInt(Object[] from, Object[] to, int sourceStart, int destinationStart, int length) {
            System.arraycopy(from, sourceStart, to, destinationStart, length);
        }

        @Specialization(guards = "toStrategy.matchesStore(to)")
        protected void copyToOther(Object[] from, Object to, int sourceStart, int destinationStart, int length,
                @Cached("ofStore(to)") ArrayStrategy toStrategy,
                @Cached("toStrategy.setNode()") ArrayOperationNodes.ArraySetNode setNode) {
            for (int i = 0; i < length; i++) {
                setNode.execute(to, destinationStart + i, from[sourceStart + i]);
            }
        }

        public static ObjectArrayCopyToNode create() {
            return ObjectArrayCopyToNodeGen.create();
        }
    }

    public static abstract class ObjectArrayExtractRangeNode extends ArrayOperationNodes.ArrayExtractRangeNode {

        @Specialization
        protected Object[] extractRange(Object[] store, int start, int end) {
            Object[] newStore = new Object[end - start];
            System.arraycopy(store, start, newStore, 0, end - start);
            return newStore;
        }

        public static ObjectArrayExtractRangeNode create() {
            return ObjectArrayExtractRangeNodeGen.create();
        }
    }

    public static abstract class ObjectArraySortNode extends ArrayOperationNodes.ArraySortNode {

        @Specialization
        protected void sort(Object[] store, int size) {
            Arrays.sort(store, 0, size);
        }

        public static ObjectArraySortNode create() {
            return ObjectArraySortNodeGen.create();
        }
    }

}
