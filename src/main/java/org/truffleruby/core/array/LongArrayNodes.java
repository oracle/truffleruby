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

import org.truffleruby.core.array.LongArrayNodesFactory.LongArrayCapacityNodeGen;
import org.truffleruby.core.array.LongArrayNodesFactory.LongArrayCopyStoreNodeGen;
import org.truffleruby.core.array.LongArrayNodesFactory.LongArrayCopyToNodeGen;
import org.truffleruby.core.array.LongArrayNodesFactory.LongArrayExtractRangeNodeGen;
import org.truffleruby.core.array.LongArrayNodesFactory.LongArrayGetNodeGen;
import org.truffleruby.core.array.LongArrayNodesFactory.LongArrayNewStoreNodeGen;
import org.truffleruby.core.array.LongArrayNodesFactory.LongArraySetNodeGen;
import org.truffleruby.core.array.LongArrayNodesFactory.LongArraySortNodeGen;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public class LongArrayNodes {
    public static abstract class LongArrayCapacityNode extends ArrayOperationNodes.ArrayCapacityNode {

        @Specialization
        protected int length(long[] store) {
            return store.length;
        }

        public static LongArrayCapacityNode create() {
            return LongArrayCapacityNodeGen.create();
        }
    }

    public static abstract class LongArrayGetNode extends ArrayOperationNodes.ArrayGetNode {

        @Specialization
        protected long get(long[] store, int index) {
            return store[index];
        }

        public static LongArrayGetNode create() {
            return LongArrayGetNodeGen.create();
        }
    }

    public static abstract class LongArraySetNode extends ArrayOperationNodes.ArraySetNode {

        @Specialization
        protected void set(long[] store, int index, long value) {
            store[index] = value;
        }

        public static LongArraySetNode create() {
            return LongArraySetNodeGen.create();
        }
    }

    public static abstract class LongArrayNewStoreNode extends ArrayOperationNodes.ArrayNewStoreNode {

        @Specialization
        protected long[] newStore(int size) {
            return new long[size];
        }

        public static LongArrayNewStoreNode create() {
            return LongArrayNewStoreNodeGen.create();
        }
    }

    public static abstract class LongArrayCopyStoreNode extends ArrayOperationNodes.ArrayCopyStoreNode {

        @Specialization
        protected long[] newStoreCopying(long[] store, int size) {
            return ArrayUtils.grow(store, size);
        }

        public static LongArrayCopyStoreNode create() {
            return LongArrayCopyStoreNodeGen.create();
        }
    }

    public static abstract class LongArrayCopyToNode extends ArrayOperationNodes.ArrayCopyToNode {

        @Specialization
        protected void copyIntToInt(long[] from, long[] to, int sourceStart, int destinationStart, int length) {
            System.arraycopy(from, sourceStart, to, destinationStart, length);
        }

        @Specialization(guards = "toStrategy.matchesStore(to)")
        protected void copyToOther(long[] from, Object to, int sourceStart, int destinationStart, int length,
                @Cached("ofStore(to)") ArrayStrategy toStrategy,
                @Cached("toStrategy.setNode()") ArrayOperationNodes.ArraySetNode setNode) {
            for (int i = 0; i < length; i++) {
                setNode.execute(to, destinationStart + i, from[sourceStart + i]);
            }
        }

        public static LongArrayCopyToNode create() {
            return LongArrayCopyToNodeGen.create();
        }
    }

    public static abstract class LongArrayExtractRangeNode extends ArrayOperationNodes.ArrayExtractRangeNode {

        @Specialization
        protected long[] extractRange(long[] store, int start, int end) {
            long[] newStore = new long[end - start];
            System.arraycopy(store, start, newStore, 0, end - start);
            return newStore;
        }

        public static LongArrayExtractRangeNode create() {
            return LongArrayExtractRangeNodeGen.create();
        }
    }

    public static abstract class LongArraySortNode extends ArrayOperationNodes.ArraySortNode {

        @Specialization
        protected void sort(long[] store, int size) {
            Arrays.sort(store, 0, size);
        }

        public static LongArraySortNode create() {
            return LongArraySortNodeGen.create();
        }
    }

}
