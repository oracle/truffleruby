package org.truffleruby.core.array;

import java.util.Arrays;

import org.truffleruby.core.array.ObjectArrayNodesFactory.ArrayCopyToNodeGen;
import org.truffleruby.core.array.ObjectArrayNodesFactory.ArrayNewStoreNodeGen;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

public class ObjectArrayNodes {
    public static abstract class ArrayLengthNode extends ArrayOperationNodes.ArrayLengthNode {

        @Specialization
        public int length(Object[] store) {
            return store.length;
        }

        public static ArrayLengthNode create() {
            return ObjectArrayNodesFactory.ArrayLengthNodeGen.create();
        }
    }

    public static abstract class ArrayGetNode extends ArrayOperationNodes.ArrayGetNode {

        @Specialization
        public Object get(Object[] store, int index) {
            return store[index];
        }

        public static ArrayGetNode create() {
            return ObjectArrayNodesFactory.ArrayGetNodeGen.create();
        }
    }

    public static abstract class ArraySetNode extends ArrayOperationNodes.ArraySetNode {

        @Specialization
        public void set(Object[] store, int index, Object value) {
            store[index] = value;
        }

        public static ArraySetNode create() {
            return ObjectArrayNodesFactory.ArraySetNodeGen.create();
        }
    }

    public static abstract class ArrayNewStoreNode extends ArrayOperationNodes.ArrayNewStoreNode {

        @Specialization
        public Object[] newStore(int size) {
            return new Object[size];
        }

        public static ArrayNewStoreNode create() {
            return ArrayNewStoreNodeGen.create();
        }
    }

    public static abstract class ArrayCopyStoreNode extends ArrayOperationNodes.ArrayCopyStoreNode {

        @Specialization
        public Object[] newStoreCopying(Object[] store, int size) {
            return ArrayUtils.grow(store, size);
        }

        public static ArrayCopyStoreNode create() {
            return ObjectArrayNodesFactory.ArrayCopyStoreNodeGen.create();
        }
    }

    public static abstract class ArrayCopyToNode extends ArrayOperationNodes.ArrayCopyToNode {

        @Specialization
        public void copyIntToInt(Object[] from, Object[] to, int sourceStart, int destinationStart, int length) {
            System.arraycopy(from, sourceStart, to, destinationStart, length);
        }

        @Specialization(guards = "toStrategy.matchesStore(to)")
        public void copyToOther(Object[] from, Object to, int sourceStart, int destinationStart, int length,
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
        public Object[] extractRange(Object[] store, int start, int end) {
            Object[] newStore = new Object[end - start];
            System.arraycopy(store, start, newStore, 0, end - start);
            return newStore;
        }
    }

    public static abstract class ArraySortNode extends ArrayOperationNodes.ArraySortNode {

        @Specialization
        public void sort(Object[] store, int size) {
            Arrays.sort(store, 0, size);
        }

        public static ArraySortNode create() {
            return ObjectArrayNodesFactory.ArraySortNodeGen.create();
        }
    }

}
