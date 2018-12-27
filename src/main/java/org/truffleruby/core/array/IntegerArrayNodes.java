package org.truffleruby.core.array;

import java.util.Arrays;

import org.truffleruby.core.array.IntegerArrayNodesFactory.ArrayCopyToNodeGen;
import org.truffleruby.core.array.IntegerArrayNodesFactory.ArrayGetNodeGen;
import org.truffleruby.core.array.IntegerArrayNodesFactory.ArrayNewStoreNodeGen;
import org.truffleruby.core.array.IntegerArrayNodesFactory.ArraySetNodeGen;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public class IntegerArrayNodes {
    public static abstract class ArrayLengthNode extends ArrayOperationNodes.ArrayLengthNode {

        @Specialization
        public int length(int[] store) {
            return store.length;
        }

        public static ArrayLengthNode create() {
            return IntegerArrayNodesFactory.ArrayLengthNodeGen.create();
        }
    }

    public static abstract class ArrayGetNode extends ArrayOperationNodes.ArrayGetNode {

        @Specialization
        public int get(int[] store, int index) {
            return store[index];
        }

        public static ArrayGetNode create() {
            return ArrayGetNodeGen.create();
        }
    }

    public static abstract class ArraySetNode extends ArrayOperationNodes.ArraySetNode {

        @Specialization
        public void set(int[] store, int index, int value) {
            store[index] = value;
        }

        public static ArraySetNode create() {
            return ArraySetNodeGen.create();
        }
    }

    public static abstract class ArrayNewStoreNode extends ArrayOperationNodes.ArrayNewStoreNode {

        @Specialization
        public int[] newStore(int size) {
            return new int[size];
        }

        public static ArrayNewStoreNode create() {
            return ArrayNewStoreNodeGen.create();
        }
    }

    public static abstract class ArrayCopyStoreNode extends ArrayOperationNodes.ArrayCopyStoreNode {

        @Specialization
        public int[] newStoreCopying(int[] store, int size) {
            return ArrayUtils.grow(store, size);
        }

        public static ArrayCopyStoreNode create() {
            return IntegerArrayNodesFactory.ArrayCopyStoreNodeGen.create();
        }
    }

    public static abstract class ArrayCopyToNode extends ArrayOperationNodes.ArrayCopyToNode {

        @Specialization
        public void copyIntToInt(int[] from, int[] to, int sourceStart, int destinationStart, int length) {
            System.arraycopy(from, sourceStart, to, destinationStart, length);
        }

        @Specialization
        public void copyIntToLong(int[] from, long[] to, int sourceStart, int destinationStart, int length) {
            for (int i = 0; i < length; i++) {
                to[destinationStart + i] = from[sourceStart + i];
            }
        }

        @Specialization(guards = "toStrategy.matchesStore(to)")
        public void copyToOther(int[] from, Object to, int sourceStart, int destinationStart, int length,
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
        public int[] extractRange(int[] store, int start, int end) {
            int[] newStore = new int[end - start];
            System.arraycopy(store, start, newStore, 0, end - start);
            return newStore;
        }

        public static ArrayExtractRangeNode create() {
            return IntegerArrayNodesFactory.ArrayExtractRangeNodeGen.create();
        }
    }

    public static abstract class ArraySortNode extends ArrayOperationNodes.ArraySortNode {

        @Specialization
        public void sort(int[] store, int size) {
            Arrays.sort(store, 0, size);
        }

        public static ArraySortNode create() {
            return IntegerArrayNodesFactory.ArraySortNodeGen.create();
        }
    }
}
