/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.objects.AllocateObjectNode;

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
        protected Object readInBounds(DynamicObject array,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary arrays,
                @Cached ConditionProfile isInBounds) {
            final int size = Layouts.ARRAY.getSize(array);
            final int normalizedIndex = index >= 0 ? index : size + index;
            if (isInBounds.profile(0 <= normalizedIndex && normalizedIndex < size)) {
                return arrays.read(Layouts.ARRAY.getStore(array), normalizedIndex);
            } else {
                return nil;
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

        public abstract Object executeRead(DynamicObject array, int index);

        @Specialization(
                guards = "isInBounds(array, index)",
                limit = "storageStrategyLimit()")
        protected Object readInBounds(DynamicObject array, int index,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary arrays) {
            return arrays.read(Layouts.ARRAY.getStore(array), index);
        }

        @Specialization(guards = "!isInBounds(array, index)")
        protected Object readOutOfBounds(DynamicObject array, int index) {
            return nil;
        }

        protected static boolean isInBounds(DynamicObject array, int index) {
            return index >= 0 && index < Layouts.ARRAY.getSize(array);
        }
    }

    @Primitive(name = "array_read_slice_normalized", lowerFixnum = { 1, 2 }, argumentNames = { "index", "length" })
    @ImportStatic(ArrayGuards.class)
    public abstract static class ReadSliceNormalizedNode extends PrimitiveArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        public static ReadSliceNormalizedNode create() {
            return ArrayIndexNodesFactory.ReadSliceNormalizedNodeFactory.create(null);
        }

        public abstract Object executeReadSlice(DynamicObject array, int index, int length);

        @Specialization(guards = "!indexInBounds(array, index)")
        protected Object readIndexOutOfBounds(DynamicObject array, int index, int length) {
            return nil;
        }

        @Specialization(guards = "length < 0")
        protected Object readNegativeLength(DynamicObject array, int index, int length) {
            return nil;
        }

        @Specialization(
                guards = {
                        "indexInBounds(array, index)",
                        "length >= 0" })
        protected DynamicObject readInBounds(DynamicObject array, int index, int length,
                @Cached ArrayCopyOnWriteNode cowNode,
                @Cached ConditionProfile endsInBoundsProfile) {
            final int size = Layouts.ARRAY.getSize(array);
            final int end = endsInBoundsProfile.profile(index + length <= size)
                    ? length
                    : size - index;
            final Object slice = cowNode.execute(array, index, end);
            return createArrayOfSameClass(array, slice, end);
        }

        protected static boolean indexInBounds(DynamicObject array, int index) {
            return index >= 0 && index <= Layouts.ARRAY.getSize(array);
        }

        protected DynamicObject createArrayOfSameClass(DynamicObject array, Object store, int size) {
            return allocateObjectNode.allocate(Layouts.BASIC_OBJECT.getLogicalClass(array), store, size);
        }
    }
}
