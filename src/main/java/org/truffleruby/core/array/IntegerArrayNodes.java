/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import java.util.Arrays;

import org.truffleruby.core.array.IntegerArrayNodesFactory.IntArrayCapacityNodeGen;
import org.truffleruby.core.array.IntegerArrayNodesFactory.IntArrayCopyStoreNodeGen;
import org.truffleruby.core.array.IntegerArrayNodesFactory.IntArrayCopyToNodeGen;
import org.truffleruby.core.array.IntegerArrayNodesFactory.IntArrayExtractRangeNodeGen;
import org.truffleruby.core.array.IntegerArrayNodesFactory.IntArrayGetNodeGen;
import org.truffleruby.core.array.IntegerArrayNodesFactory.IntArrayNewStoreNodeGen;
import org.truffleruby.core.array.IntegerArrayNodesFactory.IntArraySetNodeGen;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public class IntegerArrayNodes {
    public static abstract class IntArrayCapacityNode extends ArrayOperationNodes.ArrayCapacityNode {

        @Specialization
        protected int length(int[] store) {
            return store.length;
        }

        public static IntArrayCapacityNode create() {
            return IntArrayCapacityNodeGen.create();
        }
    }

    public static abstract class IntArrayGetNode extends ArrayOperationNodes.ArrayGetNode {

        @Specialization
        protected int get(int[] store, int index) {
            return store[index];
        }

        public static IntArrayGetNode create() {
            return IntArrayGetNodeGen.create();
        }
    }

    public static abstract class IntArraySetNode extends ArrayOperationNodes.ArraySetNode {

        @Specialization
        protected void set(int[] store, int index, int value) {
            store[index] = value;
        }

        public static IntArraySetNode create() {
            return IntArraySetNodeGen.create();
        }
    }

    public static abstract class IntArrayNewStoreNode extends ArrayOperationNodes.ArrayNewStoreNode {

        @Specialization
        protected int[] newStore(int size) {
            return new int[size];
        }

        public static IntArrayNewStoreNode create() {
            return IntArrayNewStoreNodeGen.create();
        }
    }

    public static abstract class IntArrayCopyStoreNode extends ArrayOperationNodes.ArrayCopyStoreNode {

        @Specialization
        protected int[] newStoreCopying(int[] store, int size) {
            return ArrayUtils.grow(store, size);
        }

        public static IntArrayCopyStoreNode create() {
            return IntArrayCopyStoreNodeGen.create();
        }
    }

    public static abstract class IntArrayCopyToNode extends ArrayOperationNodes.ArrayCopyToNode {

        @Specialization
        protected void copyIntToInt(int[] from, int[] to, int sourceStart, int destinationStart, int length) {
            System.arraycopy(from, sourceStart, to, destinationStart, length);
        }

        @Specialization
        protected void copyIntToLong(int[] from, long[] to, int sourceStart, int destinationStart, int length) {
            for (int i = 0; i < length; i++) {
                to[destinationStart + i] = from[sourceStart + i];
            }
        }

        @Specialization(guards = "toStrategy.matchesStore(to)")
        protected void copyToOther(int[] from, Object to, int sourceStart, int destinationStart, int length,
                @Cached("ofStore(to)") ArrayStrategy toStrategy,
                @Cached("toStrategy.setNode()") ArrayOperationNodes.ArraySetNode setNode) {
            for (int i = 0; i < length; i++) {
                setNode.execute(to, destinationStart + i, from[sourceStart + i]);
            }
        }

        public static IntArrayCopyToNode create() {
            return IntArrayCopyToNodeGen.create();
        }
    }

    public static abstract class IntArrayExtractRangeNode extends ArrayOperationNodes.ArrayExtractRangeNode {

        @Specialization
        protected int[] extractRange(int[] store, int start, int end) {
            int[] newStore = new int[end - start];
            System.arraycopy(store, start, newStore, 0, end - start);
            return newStore;
        }

        public static IntArrayExtractRangeNode create() {
            return IntArrayExtractRangeNodeGen.create();
        }
    }

    public static abstract class ArraySortNode extends ArrayOperationNodes.ArraySortNode {

        @Specialization
        protected void sort(int[] store, int size) {
            Arrays.sort(store, 0, size);
        }

        public static ArraySortNode create() {
            return IntegerArrayNodesFactory.ArraySortNodeGen.create();
        }
    }
}
