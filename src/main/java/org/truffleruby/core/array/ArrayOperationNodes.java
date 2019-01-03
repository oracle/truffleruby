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
import org.truffleruby.core.array.ArrayOperationNodesFactory.ArrayBoxedCopyNodeGen;
import org.truffleruby.core.array.ArrayOperationNodesFactory.ArrayCOmmonUnshareStorageNodeGen;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

public class ArrayOperationNodes {

    public static abstract class ArrayLengthNode extends RubyBaseNode {

        public abstract int execute(Object store);
    }

    public static abstract class ArrayGetNode extends RubyBaseNode {

        public abstract Object execute(Object store, int index);
    }

    public static abstract class ArraySetNode extends RubyBaseNode {

        public abstract void execute(Object store, int index, Object vvalue);
    }

    public static abstract class ArrayNewStoreNode extends RubyBaseNode {

        public abstract Object execute(int size);
    }

    public static abstract class ArrayCopyStoreNode extends RubyBaseNode {

        public abstract Object execute(Object store, int size);
    }

    public static abstract class ArrayCopyToNode extends RubyBaseNode {

        public abstract void execute(Object from, Object to, int sourceStart, int destinationStart, int length);
    }

    public static abstract class ArrayExtractRangeNode extends RubyBaseNode {

        public abstract Object execute(Object store, int start, int end);
    }

    public static abstract class ArraySortNode extends RubyBaseNode {

        public abstract void execute(Object store, int size);
    }

    public static abstract class ArrayBoxedCopyNode extends RubyBaseNode {

        protected ArrayStrategy strategy;

        public ArrayBoxedCopyNode(ArrayStrategy strategy) {
            this.strategy = strategy;
        }

        public abstract Object[] execute(Object store, int size);

        @Specialization
        public Object[] boxedCopy(Object store, int size,
                @Cached("strategy.copyToNode()") ArrayCopyToNode copyToNode,
                @Cached("strategy.lengthNode()") ArrayLengthNode lengthNode) {
            final Object[] newStore = new Object[size];
            copyToNode.execute(store, newStore, 0, 0, Math.min(lengthNode.execute(store), size));
            return newStore;
        }

        public static ArrayBoxedCopyNode create(ArrayStrategy strategy) {
            return ArrayBoxedCopyNodeGen.create(strategy);
        }
    }

    public static abstract class ArrayUnshareStorageNode extends RubyBaseNode {

        public abstract Object execute(Object store);
    }

    public static abstract class ArrayCOmmonUnshareStorageNode extends ArrayUnshareStorageNode {

        @Specialization
        public Object unshareStoreNode(DynamicObject array) {
            return Layouts.ARRAY.getStore(array);
        }

        public static ArrayUnshareStorageNode create() {
            return ArrayCOmmonUnshareStorageNodeGen.create();
        }
    }
}
