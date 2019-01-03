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

import org.truffleruby.core.array.EmptyArrayNodesFactory.ArrayGetNodeGen;
import org.truffleruby.core.array.EmptyArrayNodesFactory.ArraySetNodeGen;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.dsl.Specialization;

public class EmptyArrayNodes {
    public static abstract class ArrayLengthNode extends ArrayOperationNodes.ArrayLengthNode {

        @Specialization
        public int length(Object store) {
            return 0;
        }

        public static ArrayLengthNode create() {
            return EmptyArrayNodesFactory.ArrayLengthNodeGen.create();
        }
    }

    public static abstract class ArrayGetNode extends ArrayOperationNodes.ArrayGetNode {

        @Specialization
        public Object get(Object store, int index) {
            throw new RaiseException(getContext(), coreExceptions().rangeError("Array index out of bounds", this));
        }

        public static ArrayGetNode create() {
            return ArrayGetNodeGen.create();
        }
    }

    public static abstract class ArraySetNode extends ArrayOperationNodes.ArraySetNode {

        @Specialization
        public void set(Object store, int index, Object value) {
            throw new RaiseException(getContext(), coreExceptions().rangeError("Array index out of bounds", this));
        }

        public static ArraySetNode create() {
            return ArraySetNodeGen.create();
        }
    }

    public static abstract class ArrayNewStoreNode extends ArrayOperationNodes.ArrayNewStoreNode {

        @Specialization
        public Object newStore(int size) {
            return null;
        }

        public static ArrayNewStoreNode create() {
            return EmptyArrayNodesFactory.ArrayNewStoreNodeGen.create();
        }
    }

    public static abstract class ArrayCopyStoreNode extends ArrayOperationNodes.ArrayCopyStoreNode {

        @Specialization
        public Object newStoreCopying(Object store, int size) {
            return null;
        }

        public static ArrayCopyStoreNode create() {
            return EmptyArrayNodesFactory.ArrayCopyStoreNodeGen.create();
        }
    }

    public static abstract class ArrayCopyToNode extends ArrayOperationNodes.ArrayCopyToNode {

        @Specialization
        public void copyTo(Object from, Object to, int sourceStart, int destinationStart, int length) {
            assert sourceStart == 0 && length == 0;
        }

        public static ArrayCopyToNode create() {
            return EmptyArrayNodesFactory.ArrayCopyToNodeGen.create();
        }
    }

    public static abstract class ArrayExtractRangeNode extends ArrayOperationNodes.ArrayExtractRangeNode {

        @Specialization
        public Object[] extractRange(Object store, int start, int end) {
            assert start == 0 && end == 0;
            return null;
        }

        public static ArrayExtractRangeNode create() {
            return EmptyArrayNodesFactory.ArrayExtractRangeNodeGen.create();
        }
    }

    public static abstract class ArraySortNode extends ArrayOperationNodes.ArraySortNode {

        @Specialization
        public void sort(Object store, int size) {
            // Do nothing.
        }

        public static ArraySortNode create() {
            return EmptyArrayNodesFactory.ArraySortNodeGen.create();
        }
    }
}
