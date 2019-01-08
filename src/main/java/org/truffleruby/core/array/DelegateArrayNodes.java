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

import org.truffleruby.Layouts;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayExtractRangeCopyOnWriteNode;
import org.truffleruby.core.array.ArrayOperationNodes.ArrayUnshareStorageNode;
import org.truffleruby.core.array.DelegateArrayNodesFactory.DelegateArrayCapacityNodeGen;
import org.truffleruby.core.array.DelegateArrayNodesFactory.DelegateArrayCopyStoreNodeGen;
import org.truffleruby.core.array.DelegateArrayNodesFactory.DelegateArrayCopyToNodeGen;
import org.truffleruby.core.array.DelegateArrayNodesFactory.DelegateArrayExtractRangeCopyOnWriteNodeGen;
import org.truffleruby.core.array.DelegateArrayNodesFactory.DelegateArrayExtractRangeNodeGen;
import org.truffleruby.core.array.DelegateArrayNodesFactory.DelegateArrayGetNodeGen;
import org.truffleruby.core.array.DelegateArrayNodesFactory.DelegateArrayNewStoreNodeGen;
import org.truffleruby.core.array.DelegateArrayNodesFactory.DelegateArrayUnshareStoreNodeGen;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public class DelegateArrayNodes {
    public static abstract class DelegateArrayCapacityNode extends ArrayOperationNodes.ArrayCapacityNode {

        @Specialization
        public int length(DelegatedArrayStorage store) {
            return store.length;
        }

        public static DelegateArrayCapacityNode create() {
            return DelegateArrayCapacityNodeGen.create();
        }
    }

    public static abstract class DelegateArrayGetNode extends ArrayOperationNodes.ArrayGetNode {

        @Specialization(guards = "strategy.matchesStore(store.storage)")
        public Object get(DelegatedArrayStorage store, int index,
                @Cached("ofStore(store.storage)") ArrayStrategy strategy,
                @Cached("strategy.getNode()") ArrayOperationNodes.ArrayGetNode getNode) {
            return getNode.execute(store.storage, index + store.offset);
        }

        public static DelegateArrayGetNode create() {
            return DelegateArrayGetNodeGen.create();
        }
    }

    public static abstract class DelegateArrayNewStoreNode extends ArrayOperationNodes.ArrayNewStoreNode {

        protected final ArrayStrategy strategy;

        public DelegateArrayNewStoreNode(ArrayStrategy strategy) {
            this.strategy = strategy;
        }

        @Specialization
        public DelegatedArrayStorage newStore(int size,
                @Cached("strategy.newStoreNode()") ArrayOperationNodes.ArrayNewStoreNode newStoreNode) {
            Object rawStorage = newStoreNode.execute(size);
            return new DelegatedArrayStorage(rawStorage, 0, size);
        }

        public static DelegateArrayNewStoreNode create(ArrayStrategy strategy) {
            return DelegateArrayNewStoreNodeGen.create(strategy);
        }
    }

    public static abstract class DelegateArrayCopyStoreNode extends ArrayOperationNodes.ArrayCopyStoreNode {

        protected final ArrayStrategy strategy;

        public DelegateArrayCopyStoreNode(ArrayStrategy strategy) {
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

        public static DelegateArrayCopyStoreNode create(ArrayStrategy strategy) {
            return DelegateArrayCopyStoreNodeGen.create(strategy);
        }
    }

    public static abstract class DelegateArrayCopyToNode extends ArrayOperationNodes.ArrayCopyToNode {

        protected final ArrayStrategy strategy;

        public DelegateArrayCopyToNode(ArrayStrategy strategy) {
            this.strategy = strategy;
        }

        @Specialization
        public void copyToOther(DelegatedArrayStorage from, Object to, int sourceStart, int destinationStart, int length,
                @Cached("strategy.copyToNode()") ArrayOperationNodes.ArrayCopyToNode copyToNode) {
            copyToNode.execute(from.storage, to, sourceStart + from.offset, destinationStart, length);
        }

        public static DelegateArrayCopyToNode create(ArrayStrategy strategy) {
            return DelegateArrayCopyToNodeGen.create(strategy);
        }
    }

    public static abstract class DelegateArrayExtractRangeNode extends ArrayOperationNodes.ArrayExtractRangeNode {

        @Specialization
        public Object extractRange(DelegatedArrayStorage store, int start, int end) {
            return new DelegatedArrayStorage(store.storage, store.offset + start, end - start);
        }

        public static DelegateArrayExtractRangeNode create() {
            return DelegateArrayExtractRangeNodeGen.create();
        }
    }

    public static abstract class DelegateArrayUnshareStoreNode extends ArrayOperationNodes.ArrayUnshareStorageNode {

        protected final ArrayStrategy strategy;

        public DelegateArrayUnshareStoreNode(ArrayStrategy strategy) {
            this.strategy = strategy;
        }

        @Specialization
        public Object unshareStore(DynamicObject array,
                @Cached("strategy.newStoreNode()") ArrayOperationNodes.ArrayNewStoreNode newStoreNode,
                @Cached("create(strategy)") DelegateArrayCopyToNode copyToNode) {
            DelegatedArrayStorage store = (DelegatedArrayStorage) Layouts.ARRAY.getStore(array);
            Object newStore = newStoreNode.execute(store.length);
            copyToNode.execute(store, newStore, 0, 0, store.length);
            Layouts.ARRAY.setStore(array, newStore);
            return newStore;
        }

        public static ArrayUnshareStorageNode create(ArrayStrategy strategy) {
            return DelegateArrayUnshareStoreNodeGen.create(strategy);
        }
    }

    public static abstract class DelegateArrayExtractRangeCopyOnWriteNode extends ArrayExtractRangeCopyOnWriteNode {

        @Specialization
        public Object extractCopyOnWrite(DynamicObject array, int start, int end) {
            DelegatedArrayStorage oldStore = (DelegatedArrayStorage) Layouts.ARRAY.getStore(array);
            return new DelegatedArrayStorage(oldStore.storage, start + oldStore.offset, end - start);
        }

        public static ArrayExtractRangeCopyOnWriteNode create() {
            return DelegateArrayExtractRangeCopyOnWriteNodeGen.create();
        }
    }

}
