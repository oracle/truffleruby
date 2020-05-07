package org.truffleruby.core.array;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.objects.AllocateObjectNode;

import static org.truffleruby.core.array.ArrayHelpers.getSize;

@CoreModule(value = "Truffle::ArrayIndex", isClass = false)
public abstract class ArrayIndexNodes {

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

        // Read within the bounds of an array with actual storage

        @Specialization(
                guards = "isInBounds(array, index)",
                limit = "storageStrategyLimit()")
        protected Object readInBounds(DynamicObject array, int index,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary arrays) {
            return arrays.read(Layouts.ARRAY.getStore(array), index);
        }

        // Reading out of bounds is nil for any array

        @Specialization(guards = "!isInBounds(array, index)")
        protected Object readOutOfBounds(DynamicObject array, int index) {
            return nil;
        }

        // Guards

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

        // Index out of bounds or negative length always gives you nil

        @Specialization(guards = "!indexInBounds(array, index)")
        protected Object readIndexOutOfBounds(DynamicObject array, int index, int length) {
            return nil;
        }

        @Specialization(guards = "length < 0")
        protected Object readNegativeLength(DynamicObject array, int index, int length) {
            return nil;
        }

        // Reading within bounds on an array with actual storage

        @Specialization(
                guards = {
                        "indexInBounds(array, index)",
                        "length >= 0",
                        "endInBounds(array, index, length)" })
        protected DynamicObject readInBounds(DynamicObject array, int index, int length,
                @Cached ArrayCopyOnWriteNode cowNode) {
            final Object slice = cowNode.execute(array, index, length);
            return createArrayOfSameClass(array, slice, length);
        }

        // Reading beyond upper bounds on an array with actual storage needs clamping

        @Specialization(
                guards = {
                        "indexInBounds(array, index)",
                        "length >= 0",
                        "!endInBounds(array, index, length)" })
        protected DynamicObject readOutOfBounds(DynamicObject array, int index, int length,
                @Cached ArrayCopyOnWriteNode cowNode) {
            final int end = Layouts.ARRAY.getSize(array);
            final Object slice = cowNode.execute(array, index, end - index);
            return createArrayOfSameClass(array, slice, end - index);
        }

        // Guards

        protected static boolean indexInBounds(DynamicObject array, int index) {
            return index >= 0 && index <= Layouts.ARRAY.getSize(array);
        }

        protected DynamicObject createArrayOfSameClass(DynamicObject array, Object store, int size) {
            return allocateObjectNode.allocate(Layouts.BASIC_OBJECT.getLogicalClass(array), store, size);
        }

        protected static boolean endInBounds(DynamicObject array, int index, int length) {
            return index + length <= getSize(array);
        }
    }
}

