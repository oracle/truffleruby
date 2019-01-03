package org.truffleruby.core.array;

import org.truffleruby.Layouts;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayUnshareStorageNode;
import org.truffleruby.core.array.DelegateArrayNodesFactory.ArrayCopyToNodeGen;
import org.truffleruby.core.array.DelegateArrayNodesFactory.ArrayUnshareStoreNodeGen;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public class DelegateArrayNodes {
    public static abstract class ArrayLengthNode extends ArrayOperationNodes.ArrayLengthNode {

        @Specialization
        public int length(DelegatedArrayStorage store) {
            return store.length;
        }

        public static ArrayLengthNode create() {
            return DelegateArrayNodesFactory.ArrayLengthNodeGen.create();
        }
    }

    public static abstract class ArrayGetNode extends ArrayOperationNodes.ArrayGetNode {

        @Specialization(guards = "strategy.matchesStore(store.storage)")
        public Object get(DelegatedArrayStorage store, int index,
                @Cached("ofStore(store.storage)") ArrayStrategy strategy,
                @Cached("strategy.getNode()") ArrayOperationNodes.ArrayGetNode getNode) {
            return getNode.execute(store.storage, index + store.offset);
        }

        public static ArrayGetNode create() {
            return DelegateArrayNodesFactory.ArrayGetNodeGen.create();
        }
    }

    public static abstract class ArrayNewStoreNode extends ArrayOperationNodes.ArrayNewStoreNode {

        protected final ArrayStrategy strategy;

        public ArrayNewStoreNode(ArrayStrategy strategy) {
            this.strategy = strategy;
        }

        @Specialization
        public DelegatedArrayStorage newStore(int size,
                @Cached("strategy.newStoreNode()") ArrayOperationNodes.ArrayNewStoreNode newStoreNode) {
            Object rawStorage = newStoreNode.execute(size);
            return new DelegatedArrayStorage(rawStorage, 0, size);
        }

        public static ArrayNewStoreNode create(ArrayStrategy strategy) {
            return DelegateArrayNodesFactory.ArrayNewStoreNodeGen.create(strategy);
        }
    }

    public static abstract class ArrayCopyStoreNode extends ArrayOperationNodes.ArrayCopyStoreNode {

        protected final ArrayStrategy strategy;

        public ArrayCopyStoreNode(ArrayStrategy strategy) {
            this.strategy = strategy;
        }

        @Specialization
        public Object newStoreCopying(DelegatedArrayStorage store, int size,
                @Cached("strategy.newStoreNode()") ArrayOperationNodes.ArrayNewStoreNode newStoreNode,
                @Cached("strategy.copyToNode()") ArrayOperationNodes.ArrayCopyToNode copyToNode) {
            Object newStore = newStoreNode.execute(size);
            copyToNode.execute(store.storage, newStore, store.offset, 0, size);
            return newStore;
        }

        public static ArrayCopyStoreNode create(ArrayStrategy strategy) {
            return DelegateArrayNodesFactory.ArrayCopyStoreNodeGen.create(strategy);
        }
    }

    public static abstract class ArrayCopyToNode extends ArrayOperationNodes.ArrayCopyToNode {

        protected final ArrayStrategy strategy;

        public ArrayCopyToNode(ArrayStrategy strategy) {
            this.strategy = strategy;
        }

        @Specialization
        public void copyToOther(DelegatedArrayStorage from, Object to, int sourceStart, int destinationStart, int length,
                @Cached("strategy.copyToNode()") ArrayOperationNodes.ArrayCopyToNode copyToNode) {
            copyToNode.execute(from.storage, to, sourceStart + from.offset, destinationStart, length);
        }

        public static ArrayCopyToNode create(ArrayStrategy strategy) {
            return ArrayCopyToNodeGen.create(strategy);
        }
    }

    public static abstract class ArrayExtractRangeNode extends ArrayOperationNodes.ArrayExtractRangeNode {

        @Specialization
        public Object extractRange(DelegatedArrayStorage store, int start, int end) {
            return new DelegatedArrayStorage(store.storage, store.offset + start, end - start);
        }

        public static ArrayExtractRangeNode create() {
            return DelegateArrayNodesFactory.ArrayExtractRangeNodeGen.create();
        }
    }

    public static abstract class ArraySortNode extends ArrayOperationNodes.ArraySortNode {

        @Specialization
        public void sort(DelegatedArrayStorage store, int size) {
            throw new UnsupportedOperationException();
        }

        public static ArraySortNode create() {
            return DelegateArrayNodesFactory.ArraySortNodeGen.create();
        }
    }

    public static abstract class ArrayUnshareStoreNode extends ArrayOperationNodes.ArrayUnshareStorageNode {

        protected final ArrayStrategy strategy;

        public ArrayUnshareStoreNode(ArrayStrategy strategy) {
            this.strategy = strategy;
        }

        @Specialization
        public Object unshareStore(DynamicObject array,
                @Cached("strategy.newStoreNode()") ArrayOperationNodes.ArrayNewStoreNode newStoreNode,
                @Cached("create(strategy)") ArrayCopyToNode copyToNode) {
            DelegatedArrayStorage store = (DelegatedArrayStorage) Layouts.ARRAY.getStore(array);
            Object newStore = newStoreNode.execute(store.length);
            copyToNode.execute(store, newStore, 0, 0, store.length);
            Layouts.ARRAY.setStore(array, newStore);
            return newStore;
        }

        public static ArrayUnshareStorageNode create(ArrayStrategy strategy) {
            return ArrayUnshareStoreNodeGen.create(strategy);
        }
    }
}
