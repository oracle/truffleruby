package org.truffleruby.core.array;

import java.util.Arrays;

import org.truffleruby.core.array.LongArrayNodesFactory.ArrayCopyToNodeGen;
import org.truffleruby.core.array.LongArrayNodesFactory.ArrayNewStoreNodeGen;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public class LongArrayNodes {
    public static abstract class ArrayLengthNode extends ArrayOperationNodes.ArrayLengthNode {

        @Specialization
        public int length(long[] store) {
            return store.length;
        }

        public static ArrayLengthNode create() {
            return LongArrayNodesFactory.ArrayLengthNodeGen.create();
        }
    }

    public static abstract class ArrayGetNode extends ArrayOperationNodes.ArrayGetNode {

        @Specialization
        public long get(long[] store, int index) {
            return store[index];
        }

        public static ArrayGetNode create() {
            return LongArrayNodesFactory.ArrayGetNodeGen.create();
        }
    }

    public static abstract class ArraySetNode extends ArrayOperationNodes.ArraySetNode {

        @Specialization
        public void set(long[] store, int index, long value) {
            store[index] = value;
        }

        public static ArraySetNode create() {
            return LongArrayNodesFactory.ArraySetNodeGen.create();
        }
    }

    public static abstract class ArrayNewStoreNode extends ArrayOperationNodes.ArrayNewStoreNode {

        @Specialization
        public long[] newStore(int size) {
            return new long[size];
        }

        public static ArrayNewStoreNode create() {
            return ArrayNewStoreNodeGen.create();
        }
    }

    public static abstract class ArrayCopyStoreNode extends ArrayOperationNodes.ArrayCopyStoreNode {

        @Specialization
        public long[] newStoreCopying(long[] store, int size) {
            return ArrayUtils.grow(store, size);
        }

        public static ArrayCopyStoreNode create() {
            return LongArrayNodesFactory.ArrayCopyStoreNodeGen.create();
        }
    }

    public static abstract class ArrayCopyToNode extends ArrayOperationNodes.ArrayCopyToNode {

        @Specialization
        public void copyIntToInt(long[] from, long[] to, int sourceStart, int destinationStart, int length) {
            System.arraycopy(from, sourceStart, to, destinationStart, length);
        }

        @Specialization(guards = "toStrategy.matchesStore(to)")
        public void copyToOther(long[] from, Object to, int sourceStart, int destinationStart, int length,
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
        public long[] extractRange(long[] store, int start, int end) {
            long[] newStore = new long[end - start];
            System.arraycopy(store, start, newStore, 0, end - start);
            return newStore;
        }
    }

    public static abstract class ArraySortNode extends ArrayOperationNodes.ArraySortNode {

        @Specialization
        public void sort(long[] store, int size) {
            Arrays.sort(store, 0, size);
        }

        public static ArraySortNode create() {
            return LongArrayNodesFactory.ArraySortNodeGen.create();
        }
    }

}
