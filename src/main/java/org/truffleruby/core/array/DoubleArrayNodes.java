/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import java.util.Arrays;

import org.truffleruby.core.array.DoubleArrayNodesFactory.DoubleArrayCapacityNodeGen;
import org.truffleruby.core.array.DoubleArrayNodesFactory.DoubleArrayCopyStoreNodeGen;
import org.truffleruby.core.array.DoubleArrayNodesFactory.DoubleArrayCopyToNodeGen;
import org.truffleruby.core.array.DoubleArrayNodesFactory.DoubleArrayExtractRangeNodeGen;
import org.truffleruby.core.array.DoubleArrayNodesFactory.DoubleArrayGetNodeGen;
import org.truffleruby.core.array.DoubleArrayNodesFactory.DoubleArrayNewStoreNodeGen;
import org.truffleruby.core.array.DoubleArrayNodesFactory.DoubleArraySetNodeGen;
import org.truffleruby.core.array.DoubleArrayNodesFactory.DoubleArraySortNodeGen;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public class DoubleArrayNodes {
    public static abstract class DoubleArrayCapacityNode extends ArrayOperationNodes.ArrayCapacityNode {

        @Specialization
        protected int length(double[] store) {
            return store.length;
        }

        public static DoubleArrayCapacityNode create() {
            return DoubleArrayCapacityNodeGen.create();
        }
    }

    public static abstract class DoubleArrayGetNode extends ArrayOperationNodes.ArrayGetNode {

        @Specialization
        protected double get(double[] store, int index) {
            return store[index];
        }

        public static DoubleArrayGetNode create() {
            return DoubleArrayGetNodeGen.create();
        }
    }

    public static abstract class DoubleArraySetNode extends ArrayOperationNodes.ArraySetNode {

        @Specialization
        protected void set(double[] store, int index, double value) {
            store[index] = value;
        }

        public static DoubleArraySetNode create() {
            return DoubleArraySetNodeGen.create();
        }
    }

    public static abstract class DoubleArrayNewStoreNode extends ArrayOperationNodes.ArrayNewStoreNode {

        @Specialization
        protected double[] newStore(int size) {
            return new double[size];
        }

        public static DoubleArrayNewStoreNode create() {
            return DoubleArrayNewStoreNodeGen.create();
        }
    }

    public static abstract class DoubleArrayCopyStoreNode extends ArrayOperationNodes.ArrayCopyStoreNode {

        @Specialization
        protected double[] newStoreCopying(double[] store, int size) {
            return ArrayUtils.grow(store, size);
        }

        public static DoubleArrayCopyStoreNode create() {
            return DoubleArrayCopyStoreNodeGen.create();
        }
    }

    public static abstract class DoubleArrayCopyToNode extends ArrayOperationNodes.ArrayCopyToNode {

        @Specialization
        protected void copyIntToInt(double[] from, double[] to, int sourceStart, int destinationStart, int length) {
            System.arraycopy(from, sourceStart, to, destinationStart, length);
        }

        @Specialization(guards = "toStrategy.matchesStore(to)")
        protected void copyToOther(double[] from, Object to, int sourceStart, int destinationStart, int length,
                @Cached("ofStore(to)") ArrayStrategy toStrategy,
                @Cached("toStrategy.setNode()") ArrayOperationNodes.ArraySetNode setNode) {
            for (int i = 0; i < length; i++) {
                setNode.execute(to, destinationStart + i, from[sourceStart + i]);
            }
        }

        public static DoubleArrayCopyToNode create() {
            return DoubleArrayCopyToNodeGen.create();
        }
    }

    public static abstract class DoubleArrayExtractRangeNode extends ArrayOperationNodes.ArrayExtractRangeNode {

        @Specialization
        protected double[] extractRange(double[] store, int start, int end) {
            double[] newStore = new double[end - start];
            System.arraycopy(store, start, newStore, 0, end - start);
            return newStore;
        }

        public static DoubleArrayExtractRangeNode create() {
            return DoubleArrayExtractRangeNodeGen.create();
        }
    }

    public static abstract class DoubleArraySortNode extends ArrayOperationNodes.ArraySortNode {

        @Specialization
        protected void sort(double[] store, int size) {
            Arrays.sort(store, 0, size);
        }

        public static DoubleArraySortNode create() {
            return DoubleArraySortNodeGen.create();
        }
    }

}
