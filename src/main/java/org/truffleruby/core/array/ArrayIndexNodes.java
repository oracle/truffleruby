/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreModule(value = "Truffle::ArrayIndex", isClass = false)
public abstract class ArrayIndexNodes {

    @NodeChild(value = "array", type = RubyNode.class)
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class ReadConstantIndexNode extends RubyContextSourceNode {

        private final int index;

        public static ReadConstantIndexNode create(RubyNode array, int index) {
            return ArrayIndexNodesFactory.ReadConstantIndexNodeGen.create(index, array);
        }

        protected ReadConstantIndexNode(int index) {
            this.index = index;
        }

        @Specialization(limit = "storageStrategyLimit()")
        protected Object readInBounds(RubyArray array,
                @Bind("array.store") Object store,
                @CachedLibrary("store") ArrayStoreLibrary stores,
                @Cached ConditionProfile isInBounds) {
            final int size = array.size;
            final int normalizedIndex = index >= 0 ? index : size + index;
            if (isInBounds.profile(0 <= normalizedIndex && normalizedIndex < size)) {
                return stores.read(store, normalizedIndex);
            } else {
                return nil();
            }
        }
    }

    @Primitive(name = "array_read_normalized", lowerFixnum = { 1 }, argumentNames = { "index" })
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class ReadNormalizedNode extends PrimitiveArrayArgumentsNode {

        public static ReadNormalizedNode create() {
            return ArrayIndexNodesFactory.ReadNormalizedNodeFactory.create(null);
        }

        public static ReadNormalizedNode create(RubyNode array, RubyNode index) {
            return ArrayIndexNodesFactory.ReadNormalizedNodeFactory.create(new RubyNode[]{ array, index });
        }

        public abstract Object executeRead(RubyArray array, int index);

        @Specialization(
                guards = "isInBounds(array, index)",
                limit = "storageStrategyLimit()")
        protected Object readInBounds(RubyArray array, int index,
                @Bind("array.store") Object store,
                @CachedLibrary("store") ArrayStoreLibrary stores) {
            return stores.read(store, index);
        }

        @Specialization(guards = "!isInBounds(array, index)")
        protected Object readOutOfBounds(RubyArray array, int index) {
            return nil();
        }

        protected static boolean isInBounds(RubyArray array, int index) {
            return index >= 0 && index < array.size;
        }
    }

    @Primitive(name = "array_read_slice_normalized", lowerFixnum = { 1, 2 }, argumentNames = { "index", "length" })
    @ImportStatic(ArrayGuards.class)
    public abstract static class ReadSliceNormalizedNode extends PrimitiveArrayArgumentsNode {

        public static ReadSliceNormalizedNode create() {
            return ArrayIndexNodesFactory.ReadSliceNormalizedNodeFactory.create(null);
        }

        public abstract Object executeReadSlice(RubyArray array, int index, int length);

        @Specialization(guards = "!indexInBounds(array, index)")
        protected Object readIndexOutOfBounds(RubyArray array, int index, int length) {
            return nil();
        }

        @Specialization(guards = "length < 0")
        protected Object readNegativeLength(RubyArray array, int index, int length) {
            return nil();
        }

        @Specialization(guards = { "indexInBounds(array, index)", "length >= 0" })
        protected RubyArray readInBounds(RubyArray array, int index, int length,
                @Cached ArrayCopyOnWriteNode cowNode,
                @Cached ConditionProfile endsInBoundsProfile) {
            final int size = array.size;
            final int end = endsInBoundsProfile.profile(index + length <= size)
                    ? length
                    : size - index;
            final Object slice = cowNode.execute(array, index, end);
            return createArray(slice, end);
        }

        protected static boolean indexInBounds(RubyArray array, int index) {
            return index >= 0 && index <= array.size;
        }
    }
}
