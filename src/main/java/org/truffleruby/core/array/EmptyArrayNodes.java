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

import org.truffleruby.core.array.ArrayOperationNodes.ArrayExtractRangeCopyOnWriteNode;
import org.truffleruby.core.array.EmptyArrayNodesFactory.EmptyArrayCapacityNodeGen;
import org.truffleruby.core.array.EmptyArrayNodesFactory.EmptyArrayCopyStoreNodeGen;
import org.truffleruby.core.array.EmptyArrayNodesFactory.EmptyArrayCopyToNodeGen;
import org.truffleruby.core.array.EmptyArrayNodesFactory.EmptyArrayExtractRangeCopyOnWriteNodeGen;
import org.truffleruby.core.array.EmptyArrayNodesFactory.EmptyArrayExtractRangeNodeGen;
import org.truffleruby.core.array.EmptyArrayNodesFactory.EmptyArrayGetNodeGen;
import org.truffleruby.core.array.EmptyArrayNodesFactory.EmptyArrayNewStoreNodeGen;
import org.truffleruby.core.array.EmptyArrayNodesFactory.EmptyArraySetNodeGen;
import org.truffleruby.core.array.EmptyArrayNodesFactory.EmptyArraySortNodeGen;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public class EmptyArrayNodes {
    public static abstract class EmptyArrayCapacityNode extends ArrayOperationNodes.ArrayCapacityNode {

        @Specialization
        public int length(Object store) {
            return 0;
        }

        public static EmptyArrayCapacityNode create() {
            return EmptyArrayCapacityNodeGen.create();
        }
    }

    public static abstract class EmptyArrayGetNode extends ArrayOperationNodes.ArrayGetNode {

        @Specialization
        public Object get(Object store, int index) {
            throw new RaiseException(getContext(), coreExceptions().rangeError("Array index out of bounds", this));
        }

        public static EmptyArrayGetNode create() {
            return EmptyArrayGetNodeGen.create();
        }
    }

    public static abstract class EmptyArraySetNode extends ArrayOperationNodes.ArraySetNode {

        @Specialization
        public void set(Object store, int index, Object value) {
            throw new RaiseException(getContext(), coreExceptions().rangeError("Array index out of bounds", this));
        }

        public static EmptyArraySetNode create() {
            return EmptyArraySetNodeGen.create();
        }
    }

    public static abstract class EmptyArrayNewStoreNode extends ArrayOperationNodes.ArrayNewStoreNode {

        @Specialization
        public Object newStore(int size) {
            return null;
        }

        public static EmptyArrayNewStoreNode create() {
            return EmptyArrayNewStoreNodeGen.create();
        }
    }

    public static abstract class EmptyArrayCopyStoreNode extends ArrayOperationNodes.ArrayCopyStoreNode {

        @Specialization
        public Object newStoreCopying(Object store, int size) {
            return null;
        }

        public static EmptyArrayCopyStoreNode create() {
            return EmptyArrayCopyStoreNodeGen.create();
        }
    }

    public static abstract class EmptyArrayCopyToNode extends ArrayOperationNodes.ArrayCopyToNode {

        @Specialization
        public void copyTo(Object from, Object to, int sourceStart, int destinationStart, int length) {
            assert sourceStart == 0 && length == 0;
        }

        public static EmptyArrayCopyToNode create() {
            return EmptyArrayCopyToNodeGen.create();
        }
    }

    public static abstract class EmptyArrayExtractRangeNode extends ArrayOperationNodes.ArrayExtractRangeNode {

        @Specialization
        public Object[] extractRange(Object store, int start, int end) {
            assert start == 0 && end == 0;
            return null;
        }

        public static EmptyArrayExtractRangeNode create() {
            return EmptyArrayExtractRangeNodeGen.create();
        }
    }

    public static abstract class EmptyArraySortNode extends ArrayOperationNodes.ArraySortNode {

        @Specialization
        public void sort(Object store, int size) {
            // Do nothing.
        }

        public static EmptyArraySortNode create() {
            return EmptyArraySortNodeGen.create();
        }
    }

    public static abstract class EmptyArrayExtractRangeCopyOnWriteNode extends ArrayExtractRangeCopyOnWriteNode {

        @Specialization
        public Object extractCopyOnWrite(DynamicObject array, int start, int end) {
            assert start == 0 && end == 0;
            return null;
        }

        public static ArrayExtractRangeCopyOnWriteNode create() {
            return EmptyArrayExtractRangeCopyOnWriteNodeGen.create();
        }
    }

}
