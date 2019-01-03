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

import org.truffleruby.core.array.DoubleArrayNodesFactory.ArrayCopyToNodeGen;
import org.truffleruby.core.array.DoubleArrayNodesFactory.ArrayNewStoreNodeGen;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public class DoubleArrayNodes {
    public static abstract class ArrayLengthNode extends ArrayOperationNodes.ArrayLengthNode {

        @Specialization
        public int length(double[] store) {
            return store.length;
        }

        public static ArrayLengthNode create() {
            return DoubleArrayNodesFactory.ArrayLengthNodeGen.create();
        }
    }

    public static abstract class ArrayGetNode extends ArrayOperationNodes.ArrayGetNode {

        @Specialization
        public double get(double[] store, int index) {
            return store[index];
        }

        public static ArrayGetNode create() {
            return DoubleArrayNodesFactory.ArrayGetNodeGen.create();
        }
    }

    public static abstract class ArraySetNode extends ArrayOperationNodes.ArraySetNode {

        @Specialization
        public void set(double[] store, int index, double value) {
            store[index] = value;
        }

        public static ArraySetNode create() {
            return DoubleArrayNodesFactory.ArraySetNodeGen.create();
        }
    }

    public static abstract class ArrayNewStoreNode extends ArrayOperationNodes.ArrayNewStoreNode {

        @Specialization
        public double[] newStore(int size) {
            return new double[size];
        }

        public static ArrayNewStoreNode create() {
            return ArrayNewStoreNodeGen.create();
        }
    }

    public static abstract class ArrayCopyStoreNode extends ArrayOperationNodes.ArrayCopyStoreNode {

        @Specialization
        public double[] newStoreCopying(double[] store, int size) {
            return ArrayUtils.grow(store, size);
        }

        public static ArrayCopyStoreNode create() {
            return DoubleArrayNodesFactory.ArrayCopyStoreNodeGen.create();
        }
    }

    public static abstract class ArrayCopyToNode extends ArrayOperationNodes.ArrayCopyToNode {

        @Specialization
        public void copyIntToInt(double[] from, double[] to, int sourceStart, int destinationStart, int length) {
            System.arraycopy(from, sourceStart, to, destinationStart, length);
        }

        @Specialization(guards = "toStrategy.matchesStore(to)")
        public void copyToOther(double[] from, Object to, int sourceStart, int destinationStart, int length,
                @Cached("ofStore(to)") ArrayStrategy toStrategy,
                @Cached("toStrategy.setNode()") ArrayOperationNodes.ArraySetNode setNode) {
            for (int i = 0; i < length; i++) {
                setNode.execute(to, destinationStart + i, from[sourceStart + i]);
            }
        }

        public static ArrayCopyToNode create() {
            return ArrayCopyToNodeGen.create();
        }
    }

    public static abstract class ArrayExtractRangeNode extends ArrayOperationNodes.ArrayExtractRangeNode {

        @Specialization
        public double[] extractRange(double[] store, int start, int end) {
            double[] newStore = new double[end - start];
            System.arraycopy(store, start, newStore, 0, end - start);
            return newStore;
        }

        public static ArrayExtractRangeNode create() {
            return DoubleArrayNodesFactory.ArrayExtractRangeNodeGen.create();
        }
    }

    public static abstract class ArraySortNode extends ArrayOperationNodes.ArraySortNode {

        @Specialization
        public void sort(double[] store, int size) {
            Arrays.sort(store, 0, size);
        }

        public static ArraySortNode create() {
            return DoubleArrayNodesFactory.ArraySortNodeGen.create();
        }
    }

}
