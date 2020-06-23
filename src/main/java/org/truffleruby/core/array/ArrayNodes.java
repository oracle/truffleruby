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

import static org.truffleruby.core.array.ArrayHelpers.getSize;
import static org.truffleruby.core.array.ArrayHelpers.getStore;
import static org.truffleruby.core.array.ArrayHelpers.setSize;
import static org.truffleruby.core.array.ArrayHelpers.setStoreAndSize;

import java.util.Arrays;

import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.Hashing;
import org.truffleruby.core.array.ArrayBuilderNode.BuilderState;
import org.truffleruby.core.array.ArrayEachIteratorNode.ArrayElementConsumerNode;
import org.truffleruby.core.array.ArrayIndexNodes.ReadNormalizedNode;
import org.truffleruby.core.array.ArrayNodesFactory.ReplaceNodeFactory;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.array.library.DelegatedArrayStorage;
import org.truffleruby.core.array.library.NativeArrayStorage;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.CmpIntNode;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.cast.ToLongNode;
import org.truffleruby.core.cast.ToAryNode;
import org.truffleruby.core.cast.ToAryNodeGen;
import org.truffleruby.core.cast.ToStrNodeGen;
import org.truffleruby.core.format.BytesResult;
import org.truffleruby.core.format.FormatExceptionTranslator;
import org.truffleruby.core.format.exceptions.FormatException;
import org.truffleruby.core.format.pack.PackCompiler;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.kernel.KernelNodes.SameOrEqlNode;
import org.truffleruby.core.kernel.KernelNodes.SameOrEqualNode;
import org.truffleruby.core.kernel.KernelNodesFactory;
import org.truffleruby.core.kernel.KernelNodesFactory.SameOrEqlNodeFactory;
import org.truffleruby.core.numeric.FixnumLowerNode;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.support.TypeNodes;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.library.RubyLibrary;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.objects.PropagateTaintNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.objects.shared.PropagateSharingNode;
import org.truffleruby.language.yield.YieldNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.profiles.IntValueProfile;

@CoreModule(value = "Array", isClass = true)
public abstract class ArrayNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass) {
            return allocateNode.allocate(rubyClass, ArrayStoreLibrary.INITIAL_STORE, 0);
        }

    }

    @CoreMethod(names = "+", required = 1)
    @NodeChild(value = "a", type = RubyNode.class)
    @NodeChild(value = "b", type = RubyNode.class)
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class AddNode extends CoreMethodNode {

        @CreateCast("b")
        protected RubyNode coerceOtherToAry(RubyNode other) {
            return ToAryNodeGen.create(other);
        }

        @Specialization(
                limit = "storageStrategyLimit()")
        protected DynamicObject addGeneralize(DynamicObject a, DynamicObject b,
                @CachedLibrary("getStore(a)") ArrayStoreLibrary as,
                @CachedLibrary("getStore(b)") ArrayStoreLibrary bs) {
            final int aSize = Layouts.ARRAY.getSize(a);
            final int bSize = Layouts.ARRAY.getSize(b);
            final int combinedSize = aSize + bSize;
            Object newStore = as.allocateForNewStore(getStore(a), getStore(b), combinedSize);
            as.copyContents(getStore(a), 0, newStore, 0, aSize);
            bs.copyContents(getStore(b), 0, newStore, aSize, bSize);
            return createArray(newStore, combinedSize);
        }

    }

    @Primitive(name = "array_mul", lowerFixnum = 1)
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class MulNode extends PrimitiveArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();
        @Child private PropagateTaintNode propagateTaintNode = PropagateTaintNode.create();

        @Specialization(guards = "count == 0")
        protected DynamicObject mulZero(DynamicObject array, int count) {
            final DynamicObject result = allocateObjectNode
                    .allocate(Layouts.BASIC_OBJECT.getLogicalClass(array), ArrayStoreLibrary.INITIAL_STORE, 0);
            propagateTaintNode.executePropagate(array, result);
            return result;
        }

        @Specialization(
                guards = { "!isEmptyArray(array)", "count > 0" },
                limit = "storageStrategyLimit()")
        protected DynamicObject mulOther(DynamicObject array, int count,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary arrays) {

            final int size = Layouts.ARRAY.getSize(array);
            final int newSize;
            try {
                newSize = Math.multiplyExact(size, count);
            } catch (ArithmeticException e) {
                throw new RaiseException(getContext(), coreExceptions().rangeError("new array size too large", this));
            }
            final Object store = Layouts.ARRAY.getStore(array);
            final Object newStore = arrays.allocator(store).allocate(newSize);
            for (int n = 0; n < count; n++) {
                arrays.copyContents(store, 0, newStore, n * size, size);
            }

            final DynamicObject result = allocateObjectNode
                    .allocate(Layouts.BASIC_OBJECT.getLogicalClass(array), newStore, newSize);
            propagateTaintNode.executePropagate(array, result);
            return result;
        }

        @Specialization(guards = "count < 0")
        protected DynamicObject mulNeg(DynamicObject array, long count) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("negative argument", this));
        }

        @Specialization(guards = { "!isEmptyArray(array)", "count >= 0", "!fitsInInteger(count)" })
        protected DynamicObject mulLong(DynamicObject array, long count) {
            throw new RaiseException(getContext(), coreExceptions().rangeError("array size too big", this));
        }

        @Specialization(guards = { "isEmptyArray(array)" })
        protected DynamicObject mulEmpty(DynamicObject array, long count) {
            final DynamicObject result = allocateObjectNode
                    .allocate(Layouts.BASIC_OBJECT.getLogicalClass(array), ArrayStoreLibrary.INITIAL_STORE, 0);
            propagateTaintNode.executePropagate(array, result);
            return result;
        }

        @Specialization(guards = { "!isInteger(count)", "!isLong(count)" })
        protected Object fallback(DynamicObject array, Object count) {
            return FAILURE;
        }
    }

    @CoreMethod(names = { "at" }, required = 1, lowerFixnum = 1)
    @NodeChild(value = "array", type = RubyNode.class)
    @NodeChild(value = "index", type = RubyNode.class)
    public abstract static class AtNode extends CoreMethodNode {

        abstract Object executeAt(DynamicObject array, Object index);

        public static AtNode create() {
            return ArrayNodesFactory.AtNodeFactory.create(null, null);
        }

        @Specialization
        protected Object at(DynamicObject array, int index,
                @Cached ReadNormalizedNode readNormalizedNode,
                @Cached ConditionProfile denormalized) {
            if (denormalized.profile(index < 0)) {
                index += Layouts.ARRAY.getSize(array);
            }
            return readNormalizedNode.executeRead(array, index);
        }

        @Specialization
        protected Object at(DynamicObject array, long index) {
            assert !CoreLibrary.fitsIntoInteger(index);
            return nil;
        }

        @Specialization(guards = "!isBasicInteger(index)")
        protected Object at(DynamicObject array, Object index,
                @Cached ToLongNode toLongNode,
                @Cached FixnumLowerNode lowerNode,
                @Cached AtNode atNode) {
            return atNode.executeAt(array, lowerNode.executeLower(toLongNode.execute(index)));
        }
    }

    @CoreMethod(
            names = { "[]", "slice" },
            required = 1,
            optional = 1,
            lowerFixnum = { 1, 2 },
            argumentNames = { "index_start_or_range", "length" })
    public abstract static class IndexNode extends ArrayCoreMethodNode {

        @Specialization
        protected Object index(DynamicObject array, int index, NotProvided length,
                @Cached ConditionProfile negativeIndexProfile,
                @Cached ReadNormalizedNode readNode) {
            final int normalizedIndex = ArrayOperations
                    .normalizeIndex(Layouts.ARRAY.getSize(array), index, negativeIndexProfile);
            return readNode.executeRead(array, normalizedIndex);
        }

        @Specialization
        protected Object slice(DynamicObject array, int start, int length,
                @Cached ArrayIndexNodes.ReadSliceNormalizedNode readSliceNode,
                @Cached ConditionProfile negativeIndexProfile) {
            if (length < 0) {
                return nil;
            }
            final int normalizedStart = ArrayOperations
                    .normalizeIndex(Layouts.ARRAY.getSize(array), start, negativeIndexProfile);
            return readSliceNode.executeReadSlice(array, normalizedStart, length);
        }

        @Specialization(guards = "eitherNotInteger(index, maybeLength)")
        protected Object fallbackIndex(DynamicObject array, Object index, Object maybeLength,
                @Cached CallDispatchHeadNode fallbackNode) {
            return fallbackNode.call(array, "element_reference_fallback", index, maybeLength);
        }

        protected boolean eitherNotInteger(Object index, Object length) {
            return !RubyGuards.isInteger(index) || RubyGuards.wasProvided(length) && !RubyGuards.isInteger(length);
        }
    }

    @CoreMethod(
            names = "[]=",
            required = 2,
            optional = 1,
            lowerFixnum = { 1, 2 },
            raiseIfFrozenSelf = true,
            argumentNames = { "index_start_or_range", "length_or_value", "value" })
    @ImportStatic(ArrayHelpers.class)
    public abstract static class IndexSetNode extends ArrayCoreMethodNode {

        public static IndexSetNode create() {
            return ArrayNodesFactory.IndexSetNodeFactory.create(null);
        }

        abstract Object executeIntIndices(DynamicObject array, int index, int length, Object replacement);

        // array[index] = object

        @Specialization
        @ReportPolymorphism.Exclude
        protected Object set(DynamicObject array, int index, Object value, NotProvided unused,
                @Cached ArrayWriteNormalizedNode writeNode,
                @Cached ConditionProfile negativeDenormalizedIndex,
                @Cached BranchProfile negativeNormalizedIndex) {
            final int size = Layouts.ARRAY.getSize(array);
            final int nIndex = normalize(size, index, negativeDenormalizedIndex, negativeNormalizedIndex);
            return writeNode.executeWrite(array, nIndex, value);
        }
        
        @Specialization(guards = "!isInteger(start)")
        protected Object fallbackUnary(DynamicObject array, Object start, Object value, NotProvided unused,
                @Cached CallDispatchHeadNode fallbackNode) {
            return fallbackNode.call(array, "element_set_index_fallback", start, value);
        }

        // array[start, length] = array2

        @Specialization(guards = { "wasProvided(replacement)", "length < 0" })
        protected Object negativeLength(DynamicObject array, int start, int length, Object replacement) {
            throw new RaiseException(getContext(), coreExceptions().negativeLengthError(length, this));
        }

        @Specialization(guards = {
                "isRubyArray(replacement)",
                "length >= 0" })
        protected Object setTernary(DynamicObject array, int start, int length, DynamicObject replacement,
                @Cached ConditionProfile negativeDenormalizedIndex,
                @Cached BranchProfile negativeNormalizedIndex,
                @Cached ConditionProfile moveNeeded,
                @Cached ConditionProfile differentLength,
                @Cached ArrayPrepareForCopyNode prepareToCopy,
                @Cached ArrayCopyCompatibleRangeNode shift,
                @Cached ArrayCopyCompatibleRangeNode copyRange,
                @Cached ArrayTruncateNode truncate) {

            final int size = Layouts.ARRAY.getSize(array);
            start = normalize(size, start, negativeDenormalizedIndex, negativeNormalizedIndex);
            final int replacementSize = Layouts.ARRAY.getSize(replacement);

            if (moveNeeded.profile(start + length < size)) {
                // There is a tail (the part of the array to the right of the overwritten area) to be moved.

                final int originalSize = Layouts.ARRAY.getSize(array);
                final int overwrittenAreaEnd = start + length;
                final int tailSize = originalSize - overwrittenAreaEnd;
                final int writtenAreaEnd = start + replacementSize;
                final int newSize = originalSize - length + replacementSize;
                final int requiredLength = newSize - start;

                prepareToCopy.execute(array, replacement, start, requiredLength);
                shift.execute(array, array, writtenAreaEnd, overwrittenAreaEnd, tailSize);
                copyRange.execute(array, replacement, start, 0, replacementSize);
                truncate.execute(array, newSize);

            } else {
                // The array is overwriten from `start` to end, there is no tail to be moved.

                prepareToCopy.execute(array, replacement, start, replacementSize);
                copyRange.execute(array, replacement, start, 0, replacementSize);
                truncate.execute(array, start + replacementSize);
            }

            return replacement;
        }

        @Specialization(guards = {
                "wasProvided(replacement)",
                "fallbackGuard(start, length, replacement)" })
        protected Object fallbackBinary(DynamicObject array, Object start, Object length, Object replacement,
                @Cached CallDispatchHeadNode fallbackNode) {
            return fallbackNode.call(array, "element_set_range_fallback", start, length, replacement);
        }

        // Helpers

        protected int normalize(int arraySize, int index,
                ConditionProfile negativeDenormalizedIndex, BranchProfile negativeNormalizedIndex) {
            if (negativeDenormalizedIndex.profile(index < 0)) {
                index = arraySize + index;
                if (index < 0) {
                    negativeNormalizedIndex.enter();
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().indexTooSmallError("array", index, arraySize, this));
                }
            }
            return index;
        }

        protected static boolean fallbackGuard(Object start, Object length, Object replacement) {
            return !RubyGuards.isInteger(start) || !RubyGuards.isInteger(length) ||
                    ((int) length) >= 0 && !RubyGuards.isRubyArray(replacement);
        }
    }

    @CoreMethod(names = "clear", raiseIfFrozenSelf = true)
    @ReportPolymorphism
    public abstract static class ClearNode extends ArrayCoreMethodNode {

        @Specialization
        protected DynamicObject clear(DynamicObject array) {
            setStoreAndSize(array, ArrayStoreLibrary.INITIAL_STORE, 0);
            return array;
        }

    }

    @CoreMethod(names = "compact")
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class CompactNode extends ArrayCoreMethodNode {

        @Specialization(guards = "stores.isPrimitive(getStore(array))", limit = "storageStrategyLimit()")
        protected DynamicObject compactPrimitive(DynamicObject array,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @Cached ArrayCopyOnWriteNode cowNode) {
            final int size = Layouts.ARRAY.getSize(array);
            return createArray(cowNode.execute(array, 0, size), size);
        }

        @Specialization(guards = "!stores.isPrimitive(getStore(array))", limit = "storageStrategyLimit()")
        protected Object compactObjectsNonMutable(DynamicObject array,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @Cached ArrayBuilderNode arrayBuilder) {
            final int size = Layouts.ARRAY.getSize(array);
            final Object store = Layouts.ARRAY.getStore(array);
            BuilderState state = arrayBuilder.start(size);

            int m = 0;

            for (int n = 0; n < size; n++) {
                Object v = stores.read(store, n);
                if (v != nil) {
                    arrayBuilder.appendValue(state, m, v);
                    m++;
                }
            }

            return createArray(arrayBuilder.finish(state, m), m);
        }

    }

    @CoreMethod(names = "compact!", raiseIfFrozenSelf = true)
    @ReportPolymorphism
    public abstract static class CompactBangNode extends ArrayCoreMethodNode {

        @Specialization(guards = "stores.isPrimitive(getStore(array))", limit = "storageStrategyLimit()")
        @ReportPolymorphism.Exclude
        protected Object compactNotObjects(DynamicObject array,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores) {
            return nil;
        }

        @Specialization(guards = "!stores.isPrimitive(getStore(array))", limit = "storageStrategyLimit()")
        protected Object compactObjectsNonMutable(DynamicObject array,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @CachedLibrary(limit = "1") ArrayStoreLibrary mutableStores) {
            final int size = Layouts.ARRAY.getSize(array);
            final Object oldStore = Layouts.ARRAY.getStore(array);
            final Object newStore;
            if (!stores.isMutable(oldStore)) {
                newStore = stores.allocator(oldStore).allocate(size);
            } else {
                newStore = oldStore;
            }

            int m = 0;

            for (int n = 0; n < size; n++) {
                Object v = stores.read(oldStore, n);
                if (v != nil) {
                    mutableStores.write(newStore, m, v);
                    m++;
                }
            }

            Layouts.ARRAY.setStore(array, newStore);
            Layouts.ARRAY.setSize(array, m);

            if (m == size) {
                return nil;
            } else {
                return array;
            }
        }

    }

    @CoreMethod(names = "concat", optional = 1, rest = true, raiseIfFrozenSelf = true)
    @ImportStatic(ArrayGuards.class)
    @NodeChild(value = "array", type = RubyNode.class)
    @NodeChild(value = "first", type = RubyNode.class)
    @NodeChild(value = "rest", type = RubyNode.class)
    public abstract static class ConcatNode extends CoreMethodNode {

        @Specialization(guards = "rest.length == 0")
        protected DynamicObject concatZero(DynamicObject array, NotProvided first, Object[] rest) {
            return array;
        }

        @Specialization(guards = "rest.length == 0")
        protected DynamicObject concatOne(DynamicObject array, DynamicObject first, Object[] rest,
                @Cached("createInternal()") ToAryNode toAryNode,
                @Cached ArrayAppendManyNode appendManyNode) {
            appendManyNode.executeAppendMany(array, toAryNode.executeToAry(first));
            return array;
        }

        @ExplodeLoop
        @Specialization(
                guards = {
                        "wasProvided(first)",
                        "rest.length > 0",
                        "rest.length == cachedLength",
                        "cachedLength <= 8" })
        protected Object concatMany(DynamicObject array, DynamicObject first, Object[] rest,
                @Cached("rest.length") int cachedLength,
                @Cached("createInternal()") ToAryNode toAryNode,
                @Cached ArrayAppendManyNode appendManyNode,
                @Cached ArrayCopyOnWriteNode cowNode,
                @Cached ConditionProfile selfArgProfile) {
            int size = Layouts.ARRAY.getSize(array);
            DynamicObject copy = createArray(cowNode.execute(array, 0, size), size);
            DynamicObject result = appendManyNode.executeAppendMany(array, toAryNode.executeToAry(first));
            for (int i = 0; i < cachedLength; ++i) {
                final DynamicObject argOrCopy = selfArgProfile.profile(rest[i] == array)
                        ? copy
                        : toAryNode.executeToAry(rest[i]);
                result = appendManyNode.executeAppendMany(array, argOrCopy);
            }
            return result;
        }

        /** Same implementation as {@link #concatMany}, safe for the use of {@code cachedLength} */
        @Specialization(
                guards = { "wasProvided(first)", "rest.length > 0" },
                replaces = "concatMany")
        protected Object concatManyGeneral(DynamicObject array, DynamicObject first, Object[] rest,
                @Cached("createInternal()") ToAryNode toAryNode,
                @Cached ArrayAppendManyNode appendManyNode,
                @Cached ArrayCopyOnWriteNode cowNode,
                @Cached ConditionProfile selfArgProfile) {
            final int size = Layouts.ARRAY.getSize(array);
            Object store = cowNode.execute(array, 0, size);

            DynamicObject result = appendManyNode.executeAppendMany(array, toAryNode.executeToAry(first));
            for (Object arg : rest) {
                if (selfArgProfile.profile(arg == array)) {
                    result = appendManyNode.executeAppendMany(array, createArray(store, size));
                } else {
                    result = appendManyNode.executeAppendMany(array, toAryNode.executeToAry(arg));
                }
            }
            return result;
        }
    }

    @CoreMethod(names = "delete", required = 1, needsBlock = true)
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class DeleteNode extends YieldingCoreMethodNode {

        @Child private SameOrEqualNode sameOrEqualNode = SameOrEqualNode.create();
        @Child private TypeNodes.CheckFrozenNode raiseIfFrozenNode;

        @Specialization(
                guards = { "stores.isMutable(getStore(array))" },
                limit = "storageStrategyLimit()")
        protected Object delete(VirtualFrame frame, DynamicObject array, Object value, Object maybeBlock,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores) {
            final int size = Layouts.ARRAY.getSize(array);
            final Object store = Layouts.ARRAY.getStore(array);

            Object found = nil;

            int i = 0;
            int n = 0;
            while (n < size) {
                final Object stored = stores.read(store, n);

                if (sameOrEqualNode.executeSameOrEqual(stored, value)) {
                    checkFrozen(array);
                    found = stored;
                    n++;
                } else {
                    if (i != n) {
                        stores.write(store, i, stores.read(store, n));
                    }

                    i++;
                    n++;
                }
            }

            if (i != n) {
                Layouts.ARRAY.setStore(array, store);
                Layouts.ARRAY.setSize(array, i);
                return found;
            } else {
                if (maybeBlock == NotProvided.INSTANCE) {
                    return nil;
                } else {
                    return yield((DynamicObject) maybeBlock, value);
                }
            }
        }

        @Specialization(
                guards = "!oldStores.isMutable(getStore(array))",
                limit = "storageStrategyLimit()")
        protected Object delete(VirtualFrame frame, DynamicObject array, Object value, Object maybeBlock,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary oldStores,
                @CachedLibrary(limit = "1") ArrayStoreLibrary newStores) {
            final int size = Layouts.ARRAY.getSize(array);
            final Object oldStore = Layouts.ARRAY.getStore(array);
            final Object newStore = oldStores.allocator(oldStore).allocate(size);

            Object found = nil;

            int i = 0;
            int n = 0;
            while (n < size) {
                final Object stored = oldStores.read(oldStore, n);

                if (sameOrEqualNode.executeSameOrEqual(stored, value)) {
                    checkFrozen(array);
                    found = stored;
                    n++;
                } else {
                    newStores.write(newStore, i, oldStores.read(oldStore, n));

                    i++;
                    n++;
                }
            }

            if (i != n) {
                Layouts.ARRAY.setStore(array, newStore);
                Layouts.ARRAY.setSize(array, i);
                return found;
            } else {
                if (maybeBlock == NotProvided.INSTANCE) {
                    return nil;
                } else {
                    return yield((DynamicObject) maybeBlock, value);
                }
            }
        }

        public void checkFrozen(Object object) {
            if (raiseIfFrozenNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                raiseIfFrozenNode = insert(TypeNodes.CheckFrozenNode.create());
            }
            raiseIfFrozenNode.execute(object);
        }

    }

    @CoreMethod(names = "delete_at", required = 1, raiseIfFrozenSelf = true, lowerFixnum = 1)
    @NodeChild(value = "array", type = RubyNode.class)
    @NodeChild(value = "index", type = RubyNode.class)
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class DeleteAtNode extends CoreMethodNode {

        @CreateCast("index")
        protected RubyNode coerceOtherToInt(RubyNode index) {
            return ToIntNode.create(index);
        }

        @Specialization(
                guards = "stores.isMutable(getStore(array))",
                limit = "storageStrategyLimit()")
        protected Object deleteAt(DynamicObject array, int index,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @Cached ConditionProfile negativeIndexProfile,
                @Cached ConditionProfile notInBoundsProfile) {
            final int size = Layouts.ARRAY.getSize(array);
            final int i = ArrayOperations.normalizeIndex(size, index, negativeIndexProfile);

            if (notInBoundsProfile.profile(i < 0 || i >= size)) {
                return nil;
            } else {
                final Object store = Layouts.ARRAY.getStore(array);
                final Object value = stores.read(store, i);
                stores.copyContents(store, i + 1, store, i, size - i - 1);
                Layouts.ARRAY.setStore(array, store);
                Layouts.ARRAY.setSize(array, size - 1);
                return value;
            }
        }

        @Specialization(
                guards = "!stores.isMutable(getStore(array))",
                limit = "storageStrategyLimit()")
        protected Object deleteAtCopying(DynamicObject array, int index,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @Cached ConditionProfile negativeIndexProfile,
                @Cached ConditionProfile notInBoundsProfile) {
            final int size = Layouts.ARRAY.getSize(array);
            final int i = ArrayOperations.normalizeIndex(size, index, negativeIndexProfile);

            if (notInBoundsProfile.profile(i < 0 || i >= size)) {
                return nil;
            } else {
                final Object store = Layouts.ARRAY.getStore(array);
                final Object mutableStore = stores.allocator(store).allocate(size - 1);
                stores.copyContents(store, 0, mutableStore, 0, i);
                final Object value = stores.read(store, i);
                stores.copyContents(store, i + 1, mutableStore, i, size - i - 1);
                Layouts.ARRAY.setStore(array, mutableStore);
                Layouts.ARRAY.setSize(array, size - 1);
                return value;
            }
        }
    }

    @CoreMethod(names = "each", needsBlock = true, enumeratorSize = "size")
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class EachNode extends YieldingCoreMethodNode implements ArrayElementConsumerNode {

        @Specialization
        protected Object each(DynamicObject array, DynamicObject block,
                @Cached ArrayEachIteratorNode iteratorNode) {
            return iteratorNode.execute(array, block, 0, this);
        }

        @Override
        public void accept(DynamicObject array, DynamicObject block, Object element, int index) {
            yield(block, element);
        }

    }

    @Primitive(name = "array_each_with_index")
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class EachWithIndexNode extends PrimitiveArrayArgumentsNode
            implements ArrayElementConsumerNode {

        @Child private YieldNode dispatchNode = YieldNode.create();

        @Specialization
        protected Object eachOther(DynamicObject array, DynamicObject block,
                @Cached ArrayEachIteratorNode iteratorNode) {
            return iteratorNode.execute(array, block, 0, this);
        }

        @Override
        public void accept(DynamicObject array, DynamicObject block, Object element, int index) {
            dispatchNode.executeDispatch(block, element, index);
        }

    }

    @Primitive(name = "array_equal")
    @ImportStatic(ArrayGuards.class)
    public abstract static class EqualNode extends PrimitiveArrayArgumentsNode {

        @Child private SameOrEqualNode sameOrEqualNode = SameOrEqualNode.create();

        @Specialization(
                guards = { "isRubyArray(b)", "stores.accepts(getStore(b))", "stores.isPrimitive(getStore(a))" },
                limit = "storageStrategyLimit()")
        protected boolean equalSamePrimitiveType(VirtualFrame frame, DynamicObject a, DynamicObject b,
                @CachedLibrary("getStore(a)") ArrayStoreLibrary stores,
                @Cached ConditionProfile sameProfile,
                @Cached("createIdentityProfile()") IntValueProfile sizeProfile,
                @Cached ConditionProfile sameSizeProfile,
                @Cached BranchProfile trueProfile,
                @Cached BranchProfile falseProfile) {

            if (sameProfile.profile(a == b)) {
                return true;
            }

            final int aSize = sizeProfile.profile(Layouts.ARRAY.getSize(a));
            final int bSize = Layouts.ARRAY.getSize(b);

            if (!sameSizeProfile.profile(aSize == bSize)) {
                return false;
            }

            final Object aStore = Layouts.ARRAY.getStore(a);
            final Object bStore = Layouts.ARRAY.getStore(b);

            for (int i = 0; i < aSize; i++) {
                if (!sameOrEqualNode
                        .executeSameOrEqual(stores.read(aStore, i), stores.read(bStore, i))) {
                    falseProfile.enter();
                    return false;
                }
            }

            trueProfile.enter();
            return true;
        }

        @Specialization(
                guards = { "isRubyArray(b)", "!stores.accepts(getStore(b))", "stores.isPrimitive(getStore(a))" },
                limit = "storageStrategyLimit()")
        protected Object equalDifferentPrimitiveType(DynamicObject a, DynamicObject b,
                @CachedLibrary("getStore(a)") ArrayStoreLibrary stores) {
            return FAILURE;
        }

        @Specialization(
                guards = { "isRubyArray(b)", "stores.accepts(getStore(a))", "!stores.isPrimitive(getStore(a))" },
                limit = "storageStrategyLimit()")
        protected Object equalNotPrimitiveType(DynamicObject a, DynamicObject b,
                @CachedLibrary("getStore(a)") ArrayStoreLibrary stores) {
            return FAILURE;
        }

        @Specialization(guards = "!isRubyArray(b)")
        protected Object equalNotArray(DynamicObject a, Object b) {
            return FAILURE;
        }

    }

    @Primitive(name = "array_eql")
    @ImportStatic(ArrayGuards.class)
    public abstract static class EqlNode extends PrimitiveArrayArgumentsNode {

        @Child private SameOrEqlNode eqlNode = SameOrEqlNodeFactory.create(null);

        @Specialization(
                guards = { "isRubyArray(b)", "stores.accepts(getStore(b))", "stores.isPrimitive(getStore(a))" },
                limit = "storageStrategyLimit()")
        protected boolean eqlSamePrimitiveType(DynamicObject a, DynamicObject b,
                @CachedLibrary("getStore(a)") ArrayStoreLibrary stores,
                @Cached ConditionProfile sameProfile,
                @Cached("createIdentityProfile()") IntValueProfile sizeProfile,
                @Cached ConditionProfile sameSizeProfile,
                @Cached BranchProfile trueProfile,
                @Cached BranchProfile falseProfile) {

            if (sameProfile.profile(a == b)) {
                return true;
            }

            final int aSize = sizeProfile.profile(Layouts.ARRAY.getSize(a));
            final int bSize = Layouts.ARRAY.getSize(b);

            if (!sameSizeProfile.profile(aSize == bSize)) {
                return false;
            }

            final Object aStore = Layouts.ARRAY.getStore(a);
            final Object bStore = Layouts.ARRAY.getStore(b);

            for (int i = 0; i < aSize; i++) {
                if (!eqlNode.executeSameOrEql(stores.read(aStore, i), stores.read(bStore, i))) {
                    falseProfile.enter();
                    return false;
                }
            }

            trueProfile.enter();
            return true;
        }

        @Specialization(
                guards = { "isRubyArray(b)", "!stores.accepts(getStore(b))", "stores.isPrimitive(getStore(a))" },
                limit = "storageStrategyLimit()")
        protected Object eqlDifferentPrimitiveType(DynamicObject a, DynamicObject b,
                @CachedLibrary("getStore(a)") ArrayStoreLibrary stores) {
            return FAILURE;
        }

        @Specialization(
                guards = { "isRubyArray(b)", "!stores.isPrimitive(getStore(a))" },
                limit = "storageStrategyLimit()")
        protected Object eqlNotPrimitiveType(DynamicObject a, DynamicObject b,
                @CachedLibrary("getStore(a)") ArrayStoreLibrary stores) {
            return FAILURE;
        }

        @Specialization(guards = "!isRubyArray(b)")
        protected Object eqlNotArray(DynamicObject a, Object b) {
            return FAILURE;
        }

    }

    @CoreMethod(names = "fill", rest = true, needsBlock = true, raiseIfFrozenSelf = true)
    @ReportPolymorphism
    public abstract static class FillNode extends ArrayCoreMethodNode {

        @Specialization(
                guards = { "args.length == 1", "stores.acceptsValue(getStore(array), value(args))" },
                limit = "storageStrategyLimit()")
        protected DynamicObject fill(DynamicObject array, Object[] args, NotProvided block,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @Cached PropagateSharingNode propagateSharingNode) {
            final Object value = args[0];
            propagateSharingNode.executePropagate(array, value);

            final Object store = Layouts.ARRAY.getStore(array);
            final int size = Layouts.ARRAY.getSize(array);
            for (int i = 0; i < size; i++) {
                stores.write(store, i, value);
            }
            return array;
        }

        protected Object value(Object[] args) {
            return args[0];
        }

        @Specialization
        protected Object fillFallback(VirtualFrame frame, DynamicObject array, Object[] args, NotProvided block,
                @Cached("createPrivate()") CallDispatchHeadNode callFillInternal) {
            return callFillInternal.call(array, "fill_internal", args);
        }

        @Specialization
        protected Object fillFallback(VirtualFrame frame, DynamicObject array, Object[] args, DynamicObject block,
                @Cached("createPrivate()") CallDispatchHeadNode callFillInternal) {
            return callFillInternal.callWithBlock(array, "fill_internal", block, args);
        }

    }

    @CoreMethod(names = "hash_internal", visibility = Visibility.PRIVATE)
    @ReportPolymorphism
    public abstract static class HashNode extends ArrayCoreMethodNode {

        private static final int CLASS_SALT = 42753062; // random number, stops hashes for similar values but different classes being the same, static because we want deterministic hashes

        @Specialization(limit = "storageStrategyLimit()")
        protected long hash(VirtualFrame frame, DynamicObject array,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @Cached("createPrivate()") CallDispatchHeadNode toHashNode,
                @Cached ToLongNode toLongNode) {
            final int size = Layouts.ARRAY.getSize(array);
            long h = getContext().getHashing(this).start(size);
            h = Hashing.update(h, CLASS_SALT);
            final Object store = Layouts.ARRAY.getStore(array);

            for (int n = 0; n < size; n++) {
                final Object value = stores.read(store, n);
                final long valueHash = toLongNode.execute(toHashNode.call(value, "hash"));
                h = Hashing.update(h, valueHash);
            }

            return Hashing.end(h);
        }

    }

    @CoreMethod(names = "include?", required = 1)
    @ReportPolymorphism
    public abstract static class IncludeNode extends ArrayCoreMethodNode {

        @Child private SameOrEqualNode sameOrEqualNode = SameOrEqualNode.create();

        @Specialization(limit = "storageStrategyLimit()")
        protected boolean include(DynamicObject array, Object value,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores) {
            final Object store = Layouts.ARRAY.getStore(array);

            for (int n = 0; n < getSize(array); n++) {
                final Object stored = stores.read(store, n);

                if (sameOrEqualNode.executeSameOrEqual(stored, value)) {
                    return true;
                }
            }

            return false;
        }

    }

    @CoreMethod(
            names = "initialize",
            needsBlock = true,
            optional = 2,
            raiseIfFrozenSelf = true,
            lowerFixnum = 1,
            argumentNames = { "size_or_copy", "filling_value", "block" })
    @ImportStatic({ ArrayGuards.class, ArrayStoreLibrary.class })
    public abstract static class InitializeNode extends YieldingCoreMethodNode {

        @Child private ToIntNode toIntNode;
        @Child private CallDispatchHeadNode toAryNode;
        @Child private KernelNodes.RespondToNode respondToToAryNode;

        public abstract DynamicObject executeInitialize(VirtualFrame frame, DynamicObject array, Object size,
                Object fillingValue, Object block);

        @Specialization
        protected DynamicObject initializeNoArgs(
                DynamicObject array,
                NotProvided size,
                NotProvided fillingValue,
                NotProvided block) {
            setStoreAndSize(array, ArrayStoreLibrary.INITIAL_STORE, 0);
            return array;
        }

        @Specialization
        protected DynamicObject initializeOnlyBlock(
                DynamicObject array,
                NotProvided size,
                NotProvided fillingValue,
                DynamicObject block) {
            setStoreAndSize(array, ArrayStoreLibrary.INITIAL_STORE, 0);
            return array;
        }

        @TruffleBoundary
        @Specialization(guards = "size < 0")
        protected DynamicObject initializeNegativeIntSize(
                DynamicObject array,
                int size,
                Object unusedFillingValue,
                Object unusedBlock) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("negative array size", this));
        }

        @TruffleBoundary
        @Specialization(guards = "size < 0")
        protected DynamicObject initializeNegativeLongSize(
                DynamicObject array,
                long size,
                Object unusedFillingValue,
                Object unusedBlock) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("negative array size", this));
        }

        protected static final long MAX_INT = Integer.MAX_VALUE;

        @TruffleBoundary
        @Specialization(guards = "size >= MAX_INT")
        protected DynamicObject initializeSizeTooBig(
                DynamicObject array,
                long size,
                NotProvided fillingValue,
                NotProvided block) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("array size too big", this));
        }

        @Specialization(guards = "size >= 0")
        protected DynamicObject initializeWithSizeNoValue(
                DynamicObject array,
                int size,
                NotProvided fillingValue,
                NotProvided block) {
            final Object[] store = new Object[size];
            Arrays.fill(store, nil);
            setStoreAndSize(array, store, size);
            return array;
        }

        @Specialization(
                guards = { "size >= 0", "wasProvided(fillingValue)" },
                limit = "storageStrategyLimit()")
        protected DynamicObject initializeWithSizeAndValue(
                DynamicObject array,
                int size,
                Object fillingValue,
                NotProvided block,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @CachedLibrary(limit = "1") ArrayStoreLibrary allocatedStores,
                @Cached ConditionProfile needsFill,
                @Cached PropagateSharingNode propagateSharingNode) {
            final Object allocatedStore = stores.allocateForNewValue(getStore(array), fillingValue, size);
            if (needsFill.profile(!allocatedStores.isDefaultValue(allocatedStore, fillingValue))) {
                propagateSharingNode.executePropagate(array, fillingValue);
                for (int i = 0; i < size; i++) {
                    allocatedStores.write(allocatedStore, i, fillingValue);
                }
            }
            setStoreAndSize(array, allocatedStore, size);
            return array;
        }

        @Specialization(
                guards = { "wasProvided(size)", "!isInteger(size)", "!isLong(size)", "wasProvided(fillingValue)" })
        protected DynamicObject initializeSizeOther(
                VirtualFrame frame,
                DynamicObject array,
                Object size,
                Object fillingValue,
                NotProvided block) {
            int intSize = toInt(size);
            return executeInitialize(frame, array, intSize, fillingValue, block);
        }

        // With block

        @Specialization(guards = "size >= 0")
        protected Object initializeBlock(DynamicObject array, int size, Object unusedFillingValue, DynamicObject block,
                @Cached ArrayBuilderNode arrayBuilder,
                @Cached PropagateSharingNode propagateSharingNode) {
            BuilderState state = arrayBuilder.start(size);

            int n = 0;
            try {
                for (; n < size; n++) {
                    final Object value = yield(block, n);
                    propagateSharingNode.executePropagate(array, value);
                    arrayBuilder.appendValue(state, n, value);
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, n);
                }
                setStoreAndSize(array, arrayBuilder.finish(state, n), n);
            }

            return array;
        }

        @Specialization(guards = "isRubyArray(copy)")
        protected DynamicObject initializeFromArray(
                DynamicObject array,
                DynamicObject copy,
                NotProvided unusedValue,
                Object maybeBlock,
                @Cached ReplaceNode replaceNode) {
            replaceNode.executeReplace(array, copy);
            return array;
        }

        @Specialization(
                guards = { "!isInteger(object)", "!isLong(object)", "wasProvided(object)", "!isRubyArray(object)" })
        protected DynamicObject initialize(
                VirtualFrame frame,
                DynamicObject array,
                Object object,
                NotProvided unusedValue,
                NotProvided block) {
            DynamicObject copy = null;
            if (respondToToAry(frame, object)) {
                Object toAryResult = callToAry(frame, object);
                if (RubyGuards.isRubyArray(toAryResult)) {
                    copy = (DynamicObject) toAryResult;
                }
            }

            if (copy != null) {
                return executeInitialize(frame, array, copy, NotProvided.INSTANCE, NotProvided.INSTANCE);
            } else {
                int size = toInt(object);
                return executeInitialize(frame, array, size, NotProvided.INSTANCE, NotProvided.INSTANCE);
            }
        }

        public boolean respondToToAry(VirtualFrame frame, Object object) {
            if (respondToToAryNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                respondToToAryNode = insert(KernelNodesFactory.RespondToNodeFactory.create(null, null, null));
            }
            return respondToToAryNode.executeDoesRespondTo(frame, object, coreStrings().TO_ARY.createInstance(), true);
        }

        protected Object callToAry(VirtualFrame frame, Object object) {
            if (toAryNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toAryNode = insert(CallDispatchHeadNode.createPrivate());
            }
            return toAryNode.call(object, "to_ary");
        }

        protected int toInt(Object value) {
            if (toIntNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toIntNode = insert(ToIntNode.create());
            }
            return toIntNode.execute(value);
        }

    }

    @CoreMethod(names = "initialize_copy", required = 1, raiseIfFrozenSelf = true)
    @NodeChild(value = "self", type = RubyNode.class)
    @NodeChild(value = "from", type = RubyNode.class)
    @ImportStatic(ArrayGuards.class)
    public abstract static class InitializeCopyNode extends CoreMethodNode {

        @CreateCast("from")
        protected RubyNode coerceOtherToAry(RubyNode other) {
            return ToAryNodeGen.create(other);
        }

        @Specialization
        protected DynamicObject initializeCopy(DynamicObject self, DynamicObject from,
                @Cached ReplaceNode replaceNode) {
            if (self == from) {
                return self;
            }
            replaceNode.executeReplace(self, from);
            return self;
        }

    }

    @Primitive(name = "array_inject")
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class InjectNode extends YieldingCoreMethodNode {

        @Child private CallDispatchHeadNode dispatch = CallDispatchHeadNode.createPublic();

        // With block

        @Specialization(guards = { "isEmptyArray(array)", "wasProvided(initialOrSymbol)" })
        @ReportPolymorphism.Exclude
        protected Object injectEmptyArray(
                DynamicObject array,
                Object initialOrSymbol,
                NotProvided symbol,
                DynamicObject block) {
            return initialOrSymbol;
        }

        @Specialization(guards = { "isEmptyArray(array)" })
        @ReportPolymorphism.Exclude
        protected Object injectEmptyArrayNoInitial(
                DynamicObject array,
                NotProvided initialOrSymbol,
                NotProvided symbol,
                DynamicObject block) {
            return nil;
        }

        @Specialization(
                guards = {
                        "!isEmptyArray(array)",
                        "wasProvided(initialOrSymbol)" },
                limit = "storageStrategyLimit()")
        protected Object injectWithInitial(
                DynamicObject array,
                Object initialOrSymbol,
                NotProvided symbol,
                DynamicObject block,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores) {
            final Object store = Layouts.ARRAY.getStore(array);
            return injectBlockHelper(stores, array, block, store, initialOrSymbol, 0);
        }

        @Specialization(
                guards = { "!isEmptyArray(array)" },
                limit = "storageStrategyLimit()")
        protected Object injectNoInitial(
                DynamicObject array,
                NotProvided initialOrSymbol,
                NotProvided symbol,
                DynamicObject block,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores) {
            final Object store = Layouts.ARRAY.getStore(array);
            return injectBlockHelper(stores, array, block, store, stores.read(store, 0), 1);
        }

        public Object injectBlockHelper(ArrayStoreLibrary stores, DynamicObject array,
                DynamicObject block, Object store, Object initial, int start) {
            Object accumulator = initial;
            int n = start;
            try {
                for (; n < getSize(array); n++) {
                    accumulator = yield(block, accumulator, stores.read(store, n));
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, n);
                }
            }

            return accumulator;
        }

        // With Symbol

        @Specialization(
                guards = {
                        "isEmptyArray(array)",
                        "wasProvided(initialOrSymbol)",
                        "isNil(block)" })
        protected Object injectSymbolEmptyArray(
                DynamicObject array,
                Object initialOrSymbol,
                RubySymbol symbol,
                Object block) {
            return initialOrSymbol;
        }

        @Specialization(guards = { "isEmptyArray(array)", "isNil(block)" })
        protected Object injectSymbolEmptyArrayNoInitial(
                DynamicObject array,
                RubySymbol initialOrSymbol,
                NotProvided symbol,
                Object block) {
            return nil;
        }

        @Specialization(
                guards = {
                        "!isEmptyArray(array)",
                        "wasProvided(initialOrSymbol)",
                        "isNil(block)" },
                limit = "storageStrategyLimit()")
        protected Object injectSymbolWithInitial(
                VirtualFrame frame,
                DynamicObject array,
                Object initialOrSymbol,
                RubySymbol symbol,
                Object block,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores) {
            final Object store = Layouts.ARRAY.getStore(array);
            return injectSymbolHelper(frame, array, symbol, stores, store, initialOrSymbol, 0);
        }

        @Specialization(
                guards = {
                        "!isEmptyArray(array)",
                        "isNil(block)" },
                limit = "storageStrategyLimit()")
        protected Object injectSymbolNoInitial(
                VirtualFrame frame,
                DynamicObject array,
                RubySymbol initialOrSymbol,
                NotProvided symbol,
                Object block,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores) {
            final Object store = Layouts.ARRAY.getStore(array);
            return injectSymbolHelper(frame, array, initialOrSymbol, stores, store, stores.read(store, 0), 1);
        }

        public Object injectSymbolHelper(VirtualFrame frame, DynamicObject array, RubySymbol symbol,
                ArrayStoreLibrary stores, Object store, Object initial, int start) {
            Object accumulator = initial;
            int n = start;

            try {
                for (; n < getSize(array); n++) {
                    accumulator = dispatch
                            .dispatch(frame, accumulator, symbol, null, new Object[]{ stores.read(store, n) });
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, n);
                }
            }
            return accumulator;
        }

    }

    @CoreMethod(names = { "map", "collect" }, needsBlock = true, enumeratorSize = "size")
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class MapNode extends YieldingCoreMethodNode {

        @Specialization(limit = "storageStrategyLimit()")
        protected Object map(DynamicObject array, DynamicObject block,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @Cached ArrayBuilderNode arrayBuilder) {
            final Object store = Layouts.ARRAY.getStore(array);
            final int size = Layouts.ARRAY.getSize(array);
            BuilderState state = arrayBuilder.start(size);

            int n = 0;
            try {
                for (; n < getSize(array); n++) {
                    final Object mappedValue = yield(block, stores.read(store, n));
                    arrayBuilder.appendValue(state, n, mappedValue);
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, n);
                }
            }

            return createArray(arrayBuilder.finish(state, size), size);
        }

    }

    @CoreMethod(names = { "map!", "collect!" }, needsBlock = true, enumeratorSize = "size", raiseIfFrozenSelf = true)
    @ImportStatic(ArrayGuards.class)
    public abstract static class MapInPlaceNode extends YieldingCoreMethodNode implements ArrayElementConsumerNode {

        @Child private ArrayWriteNormalizedNode writeNode = ArrayWriteNormalizedNodeGen.create();

        @Specialization
        protected Object map(DynamicObject array, DynamicObject block,
                @Cached ArrayEachIteratorNode iteratorNode) {
            return iteratorNode.execute(array, block, 0, this);
        }

        @Override
        public void accept(DynamicObject array, DynamicObject block, Object element, int index) {
            writeNode.executeWrite(array, index, yield(block, element));
        }

    }

    @NodeChild(value = "array", type = RubyNode.class)
    @NodeChild(value = "format", type = RubyNode.class)
    @CoreMethod(names = "pack", required = 1, taintFrom = 1)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
    @ReportPolymorphism
    public abstract static class PackNode extends CoreMethodNode {

        @Child private RopeNodes.MakeLeafRopeNode makeLeafRopeNode;
        @Child private StringNodes.MakeStringNode makeStringNode;
        @Child private RubyLibrary rubyLibrary;
        @Child private WriteObjectFieldNode writeAssociatedNode;

        private final BranchProfile exceptionProfile = BranchProfile.create();
        private final ConditionProfile resizeProfile = ConditionProfile.create();

        @CreateCast("format")
        protected RubyNode coerceFormat(RubyNode format) {
            return ToStrNodeGen.create(format);
        }

        @Specialization(guards = "equalNode.execute(rope(format), cachedFormat)", limit = "getCacheLimit()")
        protected DynamicObject packCached(DynamicObject array, DynamicObject format,
                @Cached("privatizeRope(format)") Rope cachedFormat,
                @Cached("ropeLength(cachedFormat)") int cachedFormatLength,
                @Cached("create(compileFormat(format))") DirectCallNode callPackNode,
                @Cached RopeNodes.EqualNode equalNode) {
            final BytesResult result;

            try {
                result = (BytesResult) callPackNode.call(
                        new Object[]{ getStore(array), getSize(array), false, null });
            } catch (FormatException e) {
                exceptionProfile.enter();
                throw FormatExceptionTranslator.translate(getContext(), this, e);
            }

            return finishPack(cachedFormatLength, result);
        }

        @Specialization(replaces = "packCached")
        protected DynamicObject packUncached(DynamicObject array, DynamicObject format,
                @Cached IndirectCallNode callPackNode) {
            final BytesResult result;

            try {
                result = (BytesResult) callPackNode.call(
                        compileFormat(format),
                        new Object[]{ getStore(array), getSize(array), false, null });
            } catch (FormatException e) {
                exceptionProfile.enter();
                throw FormatExceptionTranslator.translate(getContext(), this, e);
            }

            return finishPack(Layouts.STRING.getRope(format).byteLength(), result);
        }

        private DynamicObject finishPack(int formatLength, BytesResult result) {
            byte[] bytes = result.getOutput();

            if (resizeProfile.profile(bytes.length != result.getOutputLength())) {
                bytes = Arrays.copyOf(bytes, result.getOutputLength());
            }

            if (makeLeafRopeNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                makeLeafRopeNode = insert(RopeNodes.MakeLeafRopeNode.create());
            }

            if (makeStringNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                makeStringNode = insert(StringNodes.MakeStringNode.create());
            }

            final DynamicObject string = makeStringNode.fromRope(makeLeafRopeNode.executeMake(
                    bytes,
                    result.getEncoding().getEncodingForLength(formatLength),
                    result.getStringCodeRange(),
                    result.getStringLength()));

            if (result.isTainted()) {
                if (rubyLibrary == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    rubyLibrary = insert(RubyLibrary.getFactory().createDispatched(getRubyLibraryCacheLimit()));
                }
                rubyLibrary.taint(string);
            }

            if (result.getAssociated() != null) {
                if (writeAssociatedNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    writeAssociatedNode = insert(WriteObjectFieldNode.create());
                }

                writeAssociatedNode.write(string, Layouts.ASSOCIATED_IDENTIFIER, result.getAssociated());
            }

            return string;
        }

        @TruffleBoundary
        protected RootCallTarget compileFormat(DynamicObject format) {
            return new PackCompiler(getContext(), this).compile(format.toString());
        }

        protected int getCacheLimit() {
            return getContext().getOptions().PACK_CACHE;
        }

    }

    @CoreMethod(names = "pop", raiseIfFrozenSelf = true, optional = 1, lowerFixnum = 1)
    @ReportPolymorphism
    public abstract static class PopNode extends ArrayCoreMethodNode {

        public abstract Object executePop(DynamicObject array, Object n);

        @Specialization
        @ReportPolymorphism.Exclude
        protected Object pop(DynamicObject array, NotProvided n,
                @Cached ArrayPopOneNode popOneNode) {
            return popOneNode.executePopOne(array);
        }

        @Specialization(guards = "n < 0")
        @ReportPolymorphism.Exclude
        protected Object popNNegative(DynamicObject array, int n) {
            throw new RaiseException(getContext(), coreExceptions().argumentErrorNegativeArraySize(this));
        }

        @Specialization(guards = { "n >= 0", "isEmptyArray(array)" })
        @ReportPolymorphism.Exclude
        protected Object popEmpty(DynamicObject array, int n) {
            return ArrayHelpers.createEmptyArray(getContext());
        }

        @Specialization(guards = { "n == 0", "!isEmptyArray(array)" })
        @ReportPolymorphism.Exclude
        protected Object popZeroNotEmpty(DynamicObject array, int n) {
            return ArrayHelpers.createEmptyArray(getContext());
        }

        @Specialization(
                guards = { "n > 0", "!isEmptyArray(array)", "!stores.isMutable(getStore(array))" },
                limit = "storageStrategyLimit()")
        protected Object popNotEmptySharedStorage(DynamicObject array, int n,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @Cached ConditionProfile minProfile) {
            final int size = Layouts.ARRAY.getSize(array);
            final int numPop = minProfile.profile(size < n) ? size : n;
            final Object store = Layouts.ARRAY.getStore(array);

            // Extract values in a new array
            final Object popped = stores.extractRange(store, size - numPop, size);

            // Remove the end from the original array.
            setStoreAndSize(array, stores.extractRange(store, 0, size - numPop), size - numPop);

            return createArray(popped, numPop);
        }

        @Specialization(
                guards = { "n > 0", "!isEmptyArray(array)", "stores.isMutable(getStore(array))" },
                limit = "storageStrategyLimit()")
        protected Object popNotEmptyUnsharedStorage(DynamicObject array, int n,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @Cached ConditionProfile minProfile) {
            final int size = Layouts.ARRAY.getSize(array);
            final int numPop = minProfile.profile(size < n) ? size : n;
            final Object store = Layouts.ARRAY.getStore(array);

            // Extract values in a new array
            final Object popped = stores.allocator(store).allocate(numPop);
            stores.copyContents(store, size - numPop, popped, 0, numPop);

            // Remove the end from the original array.
            final Object filler = stores.allocator(store).allocate(numPop);
            stores.copyContents(filler, 0, store, size - numPop, numPop);
            setSize(array, size - numPop);

            return createArray(popped, numPop);
        }

        @Specialization(guards = { "wasProvided(n)", "!isInteger(n)", "!isLong(n)" })
        protected Object popNToInt(DynamicObject array, Object n,
                @Cached ToIntNode toIntNode) {
            return executePop(array, toIntNode.execute(n));
        }

    }

    @CoreMethod(names = "<<", raiseIfFrozenSelf = true, required = 1)
    public abstract static class AppendNode extends ArrayCoreMethodNode {

        @Child private ArrayAppendOneNode appendOneNode = ArrayAppendOneNode.create();

        @Specialization
        protected DynamicObject append(DynamicObject array, Object value) {
            return appendOneNode.executeAppendOne(array, value);
        }

    }

    @CoreMethod(names = { "push", "append" }, rest = true, optional = 1, raiseIfFrozenSelf = true)
    public abstract static class PushNode extends ArrayCoreMethodNode {

        @Child private ArrayAppendOneNode appendOneNode = ArrayAppendOneNode.create();

        @Specialization(guards = "rest.length == 0")
        protected DynamicObject pushZero(DynamicObject array, NotProvided value, Object[] rest) {
            return array;
        }

        @Specialization(guards = { "rest.length == 0", "wasProvided(value)" })
        protected DynamicObject pushOne(DynamicObject array, Object value, Object[] rest) {
            return appendOneNode.executeAppendOne(array, value);
        }

        @Specialization(guards = { "rest.length > 0", "wasProvided(value)" })
        protected DynamicObject pushMany(VirtualFrame frame, DynamicObject array, Object value, Object[] rest) {
            // NOTE (eregon): Appending one by one here to avoid useless generalization to Object[]
            // if the arguments all fit in the current storage
            appendOneNode.executeAppendOne(array, value);
            for (int i = 0; i < rest.length; i++) {
                appendOneNode.executeAppendOne(array, rest[i]);
            }
            return array;
        }

    }

    @CoreMethod(names = "reject", needsBlock = true, enumeratorSize = "size")
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class RejectNode extends YieldingCoreMethodNode {

        @Specialization(limit = "storageStrategyLimit()")
        protected Object rejectOther(DynamicObject array, DynamicObject block,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @Cached ArrayBuilderNode arrayBuilder,
                @Cached BooleanCastNode booleanCastNode) {
            final Object store = Layouts.ARRAY.getStore(array);
            final int size = Layouts.ARRAY.getSize(array);

            BuilderState state = arrayBuilder.start(size);
            int selectedSize = 0;

            int n = 0;
            try {
                for (; n < size; n++) {
                    final Object value = stores.read(store, n);

                    if (!booleanCastNode.executeToBoolean(yield(block, value))) {
                        arrayBuilder.appendValue(state, selectedSize, value);
                        selectedSize++;
                    }
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, n);
                }
            }

            return createArray(arrayBuilder.finish(state, selectedSize), selectedSize);
        }

    }

    @CoreMethod(names = "reject!", needsBlock = true, enumeratorSize = "size", raiseIfFrozenSelf = true)
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class RejectInPlaceNode extends YieldingCoreMethodNode {

        @Child private BooleanCastNode booleanCastNode = BooleanCastNode.create();

        @Specialization(guards = "stores.isMutable(getStore(array))", limit = "storageStrategyLimit()")
        protected Object rejectInPlaceMutable(DynamicObject array, DynamicObject block,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @CachedLibrary(limit = "1") ArrayStoreLibrary mutablestores) {
            return rejectInPlaceInternal(array, block, mutablestores, getStore(array));
        }

        @Specialization(guards = "!stores.isMutable(getStore(array))", limit = "storageStrategyLimit()")
        protected Object rejectInPlaceImmutable(DynamicObject array, DynamicObject block,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @CachedLibrary(limit = "1") ArrayStoreLibrary mutablestores) {
            final Object mutableStore = stores.allocator(getStore(array)).allocate(getSize(array));
            stores.copyContents(getStore(array), 0, mutableStore, 0, getSize(array));
            Layouts.ARRAY.setStore(array, mutableStore);
            return rejectInPlaceInternal(array, block, mutablestores, mutableStore);
        }

        private Object rejectInPlaceInternal(DynamicObject array, DynamicObject block, ArrayStoreLibrary stores,
                Object store) {
            int i = 0;
            int n = 0;
            try {
                for (; n < getSize(array); n++) {
                    final Object value = stores.read(store, n);
                    if (booleanCastNode.executeToBoolean(yield(block, value))) {
                        continue;
                    }

                    if (i != n) {
                        stores.write(store, i, stores.read(store, n));
                    }

                    i++;
                }
            } finally {
                // Ensure we've iterated to the end of the array.
                for (; n < Layouts.ARRAY.getSize(array); n++) {
                    if (i != n) {
                        stores.write(store, i, stores.read(store, n));
                    }
                    i++;
                }

                // Null out the elements behind the size
                final Object filler = stores.allocator(store).allocate(n - i);
                stores.copyContents(filler, 0, store, i, n - i);
                setSize(array, i);

                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, n);
                }
            }

            if (i != n) {
                return array;
            } else {
                return nil;
            }
        }

    }

    @CoreMethod(names = "replace", required = 1, raiseIfFrozenSelf = true)
    @NodeChild(value = "array", type = RubyNode.class)
    @NodeChild(value = "other", type = RubyNode.class)
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class ReplaceNode extends CoreMethodNode {

        public static ReplaceNode create() {
            return ReplaceNodeFactory.create(null, null);
        }

        @Child private PropagateSharingNode propagateSharingNode = PropagateSharingNode.create();

        public abstract DynamicObject executeReplace(DynamicObject array, DynamicObject other);

        @CreateCast("other")
        protected RubyNode coerceOtherToAry(RubyNode index) {
            return ToAryNodeGen.create(index);
        }

        @Specialization
        protected DynamicObject replace(DynamicObject array, DynamicObject other,
                @Cached ArrayCopyOnWriteNode cowNode) {
            propagateSharingNode.executePropagate(array, other);

            final int size = getSize(other);

            Layouts.ARRAY.setStore(array, cowNode.execute(other, 0, size));
            Layouts.ARRAY.setSize(array, size);
            return array;
        }

    }

    @Primitive(name = "array_rotate", lowerFixnum = 1)
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class RotateNode extends PrimitiveArrayArgumentsNode {

        @Specialization(limit = "storageStrategyLimit()")
        protected DynamicObject rotate(DynamicObject array, int rotation,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary arrays,
                @Cached("createIdentityProfile()") IntValueProfile sizeProfile,
                @Cached("createIdentityProfile()") IntValueProfile rotationProfile) {
            final int size = sizeProfile.profile(Layouts.ARRAY.getSize(array));
            rotation = rotationProfile.profile(rotation);
            assert 0 < rotation && rotation < size;

            final Object original = Layouts.ARRAY.getStore(array);
            final Object rotated = arrays.allocator(original).allocate(size);
            rotateArrayCopy(rotation, size, arrays, original, rotated);
            return createArray(rotated, size);
        }
    }

    protected static void rotateArrayCopy(int rotation, int size, ArrayStoreLibrary arrays,
            Object original, Object rotated) {
        arrays.copyContents(original, rotation, rotated, 0, size - rotation);
        arrays.copyContents(original, 0, rotated, size - rotation, rotation);
    }

    @Primitive(name = "array_rotate_inplace", lowerFixnum = 1)
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class RotateInplaceNode extends PrimitiveArrayArgumentsNode {

        @Specialization(
                guards = "arrays.isMutable(getStore(array))",
                limit = "storageStrategyLimit()")
        protected DynamicObject rotate(DynamicObject array, int rotation,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary arrays,
                @Cached("createIdentityProfile()") IntValueProfile sizeProfile,
                @Cached("createIdentityProfile()") IntValueProfile rotationProfile) {
            final int size = sizeProfile.profile(Layouts.ARRAY.getSize(array));
            rotation = rotationProfile.profile(rotation);
            assert 0 < rotation && rotation < size;
            final Object store = Layouts.ARRAY.getStore(array);

            if (CompilerDirectives.isPartialEvaluationConstant(size) &&
                    CompilerDirectives.isPartialEvaluationConstant(rotation) &&
                    size <= ArrayGuards.ARRAY_MAX_EXPLODE_SIZE) {
                rotateSmallExplode(arrays, rotation, size, store);
            } else {
                rotateReverse(arrays, rotation, size, store);
            }

            return array;
        }

        @Specialization(
                guards = { "!arrays.isMutable(getStore(array))" },
                limit = "storageStrategyLimit()")
        protected DynamicObject rotateStorageNotMutable(DynamicObject array, int rotation,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary arrays,
                @Cached("createIdentityProfile()") IntValueProfile sizeProfile,
                @Cached("createIdentityProfile()") IntValueProfile rotationProfile) {
            final int size = sizeProfile.profile(Layouts.ARRAY.getSize(array));
            rotation = rotationProfile.profile(rotation);
            assert 0 < rotation && rotation < size;

            final Object original = Layouts.ARRAY.getStore(array);
            final Object rotated = arrays.allocator(original).allocate(size);
            rotateArrayCopy(rotation, size, arrays, original, rotated);
            setStoreAndSize(array, rotated, size);
            return array;
        }

        @ExplodeLoop
        protected void rotateSmallExplode(ArrayStoreLibrary stores, int rotation, int size, Object store) {
            Object[] copy = new Object[size];
            for (int i = 0; i < size; i++) {
                copy[i] = stores.read(store, i);
            }
            for (int i = 0; i < size; i++) {
                int j = i + rotation;
                if (j >= size) {
                    j -= size;
                }
                stores.write(store, i, copy[j]);
            }
        }

        protected void rotateReverse(ArrayStoreLibrary stores, int rotation, int size, Object store) {
            // Rotating by rotation in-place is equivalent to
            // replace([rotation..-1] + [0...rotation])
            // which is the same as reversing the whole array and
            // reversing each of the two parts so that elements are in the same order again.
            // This trick avoids constantly checking if indices are within array bounds
            // and accesses memory sequentially, even though it does perform 2*size reads and writes.
            // This is also what MRI and JRuby do.
            reverse(stores, store, rotation, size);
            reverse(stores, store, 0, rotation);
            reverse(stores, store, 0, size);
        }

        private void reverse(ArrayStoreLibrary stores,
                Object store, int from, int until) {
            int to = until - 1;
            while (from < to) {
                final Object tmp = stores.read(store, from);
                stores.write(store, from, stores.read(store, to));
                stores.write(store, to, tmp);
                from++;
                to--;
            }
        }

    }

    @CoreMethod(names = { "select", "filter" }, needsBlock = true, enumeratorSize = "size")
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class SelectNode extends YieldingCoreMethodNode {

        @Specialization(limit = "storageStrategyLimit()")
        protected Object selectOther(DynamicObject array, DynamicObject block,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @Cached ArrayBuilderNode arrayBuilder,
                @Cached BooleanCastNode booleanCastNode) {
            final Object store = Layouts.ARRAY.getStore(array);

            BuilderState state = arrayBuilder.start(Layouts.ARRAY.getSize(array));
            int selectedSize = 0;

            int n = 0;
            try {
                for (; n < Layouts.ARRAY.getSize(array); n++) {
                    final Object value = stores.read(store, n);

                    if (booleanCastNode.executeToBoolean(yield(block, value))) {
                        arrayBuilder.appendValue(state, selectedSize, value);
                        selectedSize++;
                    }
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, n);
                }
            }

            return createArray(arrayBuilder.finish(state, selectedSize), selectedSize);
        }

    }

    @CoreMethod(names = "shift", raiseIfFrozenSelf = true, optional = 1, lowerFixnum = 1)
    @NodeChild(value = "array", type = RubyNode.class)
    @NodeChild(value = "n", type = RubyNode.class)
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class ShiftNode extends CoreMethodNode {

        @Child private ToIntNode toIntNode;

        public abstract Object executeShift(DynamicObject array, Object n);

        // No n, just shift 1 element and return it

        @Specialization(guards = "isEmptyArray(array)")
        @ReportPolymorphism.Exclude
        protected Object shiftEmpty(DynamicObject array, NotProvided n) {
            return nil;
        }

        @Specialization(guards = "!isEmptyArray(array)", limit = "storageStrategyLimit()")
        protected Object shiftOther(DynamicObject array, NotProvided n,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores) {
            final int size = Layouts.ARRAY.getSize(array);
            final Object store = Layouts.ARRAY.getStore(array);
            final Object value = stores.read(store, 0);
            final Object cowStore = stores.extractRange(store, 1, size);
            Layouts.ARRAY.setStore(array, cowStore);
            setSize(array, size - 1);

            return value;
        }

        // n given, shift the first n elements and return them as an Array

        @Specialization(guards = "n < 0")
        protected Object shiftNegative(DynamicObject array, int n) {
            throw new RaiseException(getContext(), coreExceptions().argumentErrorNegativeArraySize(this));
        }

        @Specialization(guards = "n == 0")
        protected Object shiftZero(DynamicObject array, int n) {
            return ArrayHelpers.createEmptyArray(getContext());
        }

        @Specialization(guards = { "n > 0", "isEmptyArray(array)" })
        protected Object shiftManyEmpty(DynamicObject array, int n) {
            return ArrayHelpers.createEmptyArray(getContext());
        }

        @Specialization(
                guards = { "n > 0", "!isEmptyArray(array)" },
                limit = "storageStrategyLimit()")
        protected Object shiftMany(DynamicObject array, int n,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @Cached ConditionProfile minProfile) {
            final int size = Layouts.ARRAY.getSize(array);
            final int numShift = minProfile.profile(size < n) ? size : n;
            final Object store = Layouts.ARRAY.getStore(array);
            // Extract values in a new array
            final Object result = stores.extractRange(store, 0, numShift);
            final Object cowStore = stores.extractRange(store, numShift, size);
            Layouts.ARRAY.setStore(array, cowStore);
            Layouts.ARRAY.setSize(array, size - numShift);

            return createArray(result, numShift);
        }

        @Specialization(guards = { "wasProvided(n)", "!isInteger(n)", "!isLong(n)" })
        protected Object shiftNToInt(DynamicObject array, Object n,
                @Cached ToIntNode toIntNode) {
            return executeShift(array, toIntNode.execute(n));
        }

    }

    @CoreMethod(names = { "size", "length" })
    public abstract static class SizeNode extends ArrayCoreMethodNode {

        @Specialization
        protected int size(DynamicObject array,
                @Cached("createIdentityProfile()") IntValueProfile profile) {
            return profile.profile(Layouts.ARRAY.getSize(array));
        }

    }

    @CoreMethod(names = "sort", needsBlock = true)
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class SortNode extends ArrayCoreMethodNode {

        @Specialization(guards = "isEmptyArray(array)")
        @ReportPolymorphism.Exclude
        protected DynamicObject sortEmpty(DynamicObject array, Object unusedBlock) {
            return ArrayHelpers.createEmptyArray(getContext());
        }

        @ExplodeLoop
        @Specialization(
                guards = { "!isEmptyArray(array)", "isSmall(array)" },
                limit = "storageStrategyLimit()")
        protected DynamicObject sortVeryShort(VirtualFrame frame, DynamicObject array, NotProvided block,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary originalStores,
                @CachedLibrary(limit = "1") ArrayStoreLibrary stores,
                @Cached("createPrivate()") CallDispatchHeadNode compareDispatchNode,
                @Cached CmpIntNode cmpIntNode) {
            final Object originalStore = Layouts.ARRAY.getStore(array);
            final Object store = originalStores
                    .allocator(originalStore)
                    .allocate(getContext().getOptions().ARRAY_SMALL);
            final int size = Layouts.ARRAY.getSize(array);

            // Copy with a exploded loop for PE

            for (int i = 0; i < getContext().getOptions().ARRAY_SMALL; i++) {
                if (i < size) {
                    stores.write(store, i, originalStores.read(originalStore, i));
                }
            }

            // Selection sort - written very carefully to allow PE

            for (int i = 0; i < getContext().getOptions().ARRAY_SMALL; i++) {
                if (i < size) {
                    for (int j = i + 1; j < getContext().getOptions().ARRAY_SMALL; j++) {
                        if (j < size) {
                            final Object a = stores.read(store, i);
                            final Object b = stores.read(store, j);
                            final Object comparisonResult = compareDispatchNode.call(b, "<=>", a);
                            if (cmpIntNode.executeCmpInt(comparisonResult, b, a) < 0) {
                                stores.write(store, j, a);
                                stores.write(store, i, b);
                            }
                        }
                    }
                }
            }

            return createArray(store, size);
        }

        @Specialization(
                guards = {
                        "!isEmptyArray(array)",
                        "!isSmall(array)",
                        "stores.isPrimitive(getStore(array))" },
                assumptions = {
                        "getContext().getCoreMethods().integerCmpAssumption",
                        "getContext().getCoreMethods().floatCmpAssumption" },
                limit = "storageStrategyLimit()")
        protected Object sortPrimitiveArrayNoBlock(DynamicObject array, NotProvided block,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @CachedLibrary(limit = "1") ArrayStoreLibrary mutableStores) {
            final int size = getSize(array);
            Object oldStore = Layouts.ARRAY.getStore(array);
            Object newStore = stores.allocator(oldStore).allocate(size);
            stores.copyContents(oldStore, 0, newStore, 0, size);
            mutableStores.sort(newStore, size);
            return createArray(newStore, size);
        }

        @Specialization(
                guards = { "!isEmptyArray(array)", "!isSmall(array)" },
                limit = "storageStrategyLimit()")
        protected Object sortArrayWithoutBlock(DynamicObject array, NotProvided block,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores,
                @Cached("createPrivate()") CallDispatchHeadNode fallbackNode) {
            return fallbackNode.call(array, "sort_fallback");
        }

        @Specialization(guards = "!isEmptyArray(array)")
        protected Object sortGenericWithBlock(DynamicObject array, DynamicObject block,
                @Cached("createPrivate()") CallDispatchHeadNode fallbackNode) {
            return fallbackNode.callWithBlock(array, "sort_fallback", block);
        }

        protected boolean isSmall(DynamicObject array) {
            return getSize(array) <= getContext().getOptions().ARRAY_SMALL;
        }

    }

    @Primitive(name = "steal_array_storage")
    @ImportStatic(ArrayGuards.class)
    public abstract static class StealArrayStorageNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "array == other")
        protected DynamicObject stealStorageNoOp(DynamicObject array, DynamicObject other) {
            return array;
        }

        @Specialization(guards = "array != other")
        protected DynamicObject stealStorage(DynamicObject array, DynamicObject other,
                @Cached PropagateSharingNode propagateSharingNode) {
            propagateSharingNode.executePropagate(array, other);

            final int size = getSize(other);
            final Object store = getStore(other);
            Layouts.ARRAY.setStore(array, store);
            Layouts.ARRAY.setSize(array, size);
            Layouts.ARRAY.setStore(other, ArrayStoreLibrary.INITIAL_STORE);
            Layouts.ARRAY.setSize(other, 0);

            return array;
        }

    }

    @Primitive(name = "array_storage_equal?")
    public abstract static class ArrayStorageEqualNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean storageEqual(DynamicObject array, DynamicObject other) {
            final Object arrayStore = Layouts.ARRAY.getStore(array);
            final Object otherStore = Layouts.ARRAY.getStore(other);
            return arrayStore instanceof DelegatedArrayStorage && otherStore instanceof DelegatedArrayStorage &&
                    ((DelegatedArrayStorage) arrayStore).storage == ((DelegatedArrayStorage) otherStore).storage;
        }

    }

    @Primitive(name = "array_zip")
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class ZipNode extends PrimitiveArrayArgumentsNode {

        @Specialization(
                guards = { "isRubyArray(other)" },
                limit = "storageStrategyLimit()")
        protected DynamicObject zipToPairs(DynamicObject array, DynamicObject other,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary aStores,
                @CachedLibrary("getStore(other)") ArrayStoreLibrary bStores,
                @CachedLibrary(limit = "1") ArrayStoreLibrary pairs,
                @Cached ConditionProfile bNotSmallerProfile) {
            final Object a = Layouts.ARRAY.getStore(array);
            final Object b = Layouts.ARRAY.getStore(other);

            final int bSize = Layouts.ARRAY.getSize(other);
            final int zippedLength = Layouts.ARRAY.getSize(array);

            final Object[] zipped = new Object[zippedLength];

            for (int n = 0; n < zippedLength; n++) {
                if (bNotSmallerProfile.profile(n < bSize)) {
                    final Object pair = aStores.allocateForNewStore(a, b, 2);
                    pairs.write(pair, 0, aStores.read(a, n));
                    pairs.write(pair, 1, bStores.read(b, n));
                    zipped[n] = createArray(pair, 2);
                } else {
                    zipped[n] = createArray(new Object[]{ aStores.read(a, n), nil });
                }
            }

            return createArray(zipped, zippedLength);
        }

    }

    @Primitive(name = "array_store_to_native")
    @ImportStatic(ArrayGuards.class)
    public abstract static class StoreToNativeNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "!oldStores.isNative(getStore(array))", limit = "storageStrategyLimit()")
        protected DynamicObject storeToNative(DynamicObject array,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary oldStores) {
            int size = Layouts.ARRAY.getSize(array);
            Object oldStore = Layouts.ARRAY.getStore(array);
            Pointer pointer = Pointer.malloc(size * Pointer.SIZE);
            pointer.enableAutorelease(getContext().getFinalizationService());
            NativeArrayStorage newStore = new NativeArrayStorage(pointer, size);
            oldStores.copyContents(oldStore, 0, newStore, 0, size);
            getContext().getMarkingService().addMarker(
                    newStore,
                    (aStore) -> ((NativeArrayStorage) aStore).preserveMembers());
            Layouts.ARRAY.setStore(array, newStore);
            return array;
        }

        @Specialization(guards = "oldStores.isNative(getStore(array))", limit = "storageStrategyLimit()")
        protected DynamicObject storeIsNative(DynamicObject array,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary oldStores) {
            return array;
        }
    }

    @Primitive(name = "array_store_address")
    @ImportStatic(ArrayGuards.class)
    public abstract static class StoreAddressNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "oldStores.isNative(getStore(array))", limit = "storageStrategyLimit()")
        protected long storeIsNative(DynamicObject array,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary oldStores) {
            NativeArrayStorage storage = (NativeArrayStorage) Layouts.ARRAY.getStore(array);
            return storage.getAddress();
        }
    }

    @Primitive(name = "array_store_native?")
    @ImportStatic(ArrayGuards.class)
    public abstract static class IsStoreNativeNode extends PrimitiveArrayArgumentsNode {

        @Specialization(limit = "storageStrategyLimit()")
        protected boolean IsStoreNative(DynamicObject array,
                @CachedLibrary("getStore(array)") ArrayStoreLibrary stores) {
            return stores.isNative(getStore(array));
        }
    }
}
