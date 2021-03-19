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

import static org.truffleruby.core.array.ArrayHelpers.setSize;
import static org.truffleruby.core.array.ArrayHelpers.setStoreAndSize;
import static org.truffleruby.language.dispatch.DispatchNode.PUBLIC;

import java.util.Arrays;

import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.graalvm.collections.EconomicSet;
import org.graalvm.collections.Equivalence;
import org.truffleruby.Layouts;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.collections.SimpleStack;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.Hashing;
import org.truffleruby.core.array.ArrayBuilderNode.BuilderState;
import org.truffleruby.core.array.ArrayEachIteratorNode.ArrayElementConsumerNode;
import org.truffleruby.core.array.ArrayIndexNodes.ReadNormalizedNode;
import org.truffleruby.core.array.ArrayIndexNodes.ReadSliceNormalizedNode;
import org.truffleruby.core.array.ArrayNodesFactory.ReplaceNodeFactory;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.array.library.DelegatedArrayStorage;
import org.truffleruby.core.array.library.NativeArrayStorage;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.CmpIntNode;
import org.truffleruby.core.cast.ToAryNode;
import org.truffleruby.core.cast.ToAryNodeGen;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.cast.ToLongNode;
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
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.numeric.FixnumLowerNode;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.range.RangeNodes.NormalizedStartLengthNode;
import org.truffleruby.core.range.RubyRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.support.TypeNodes;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.methods.Split;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.objects.shared.PropagateSharingNode;
import org.truffleruby.language.yield.CallBlockNode;
import org.truffleruby.utils.Utils;

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
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.profiles.IntValueProfile;

@CoreModule(value = "Array", isClass = true)
public abstract class ArrayNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyArray allocate(RubyClass rubyClass) {
            final Shape shape = getLanguage().arrayShape;
            final RubyArray array = new RubyArray(rubyClass, shape, ArrayStoreLibrary.INITIAL_STORE, 0);
            AllocationTracing.trace(array, this);
            return array;
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
        protected RubyArray addGeneralize(RubyArray a, RubyArray b,
                @CachedLibrary("a.store") ArrayStoreLibrary as,
                @CachedLibrary("b.store") ArrayStoreLibrary bs) {
            final int aSize = a.size;
            final int bSize = b.size;
            final int combinedSize = aSize + bSize;
            Object newStore = as.allocateForNewStore(a.store, b.store, combinedSize);
            as.copyContents(a.store, 0, newStore, 0, aSize);
            bs.copyContents(b.store, 0, newStore, aSize, bSize);
            return createArray(newStore, combinedSize);
        }

    }

    @Primitive(name = "array_mul", lowerFixnum = 1)
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class MulNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "count == 0")
        protected RubyArray mulZero(RubyArray array, int count) {
            final RubyClass logicalClass = array.getLogicalClass();
            return new RubyArray(
                    logicalClass,
                    getLanguage().arrayShape,
                    ArrayStoreLibrary.INITIAL_STORE,
                    0);
        }

        @Specialization(
                guards = { "!isEmptyArray(array)", "count > 0" },
                limit = "storageStrategyLimit()")
        protected RubyArray mulOther(RubyArray array, int count,
                @CachedLibrary("array.store") ArrayStoreLibrary arrays,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {

            final int size = array.size;
            final int newSize;
            try {
                newSize = Math.multiplyExact(size, count);
            } catch (ArithmeticException e) {
                throw new RaiseException(getContext(), coreExceptions().rangeError("new array size too large", this));
            }
            final Object store = array.store;
            final Object newStore = arrays.allocator(store).allocate(newSize);
            loopProfile.profileCounted(count);
            for (int n = 0; loopProfile.inject(n < count); n++) {
                arrays.copyContents(store, 0, newStore, n * size, size);
            }

            final RubyClass logicalClass = array.getLogicalClass();
            return new RubyArray(
                    logicalClass,
                    getLanguage().arrayShape,
                    newStore,
                    newSize);
        }

        @Specialization(guards = "count < 0")
        protected RubyArray mulNeg(RubyArray array, long count) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("negative argument", this));
        }

        @Specialization(guards = { "!isEmptyArray(array)", "count >= 0", "!fitsInInteger(count)" })
        protected RubyArray mulLong(RubyArray array, long count) {
            throw new RaiseException(getContext(), coreExceptions().rangeError("array size too big", this));
        }

        @Specialization(guards = { "isEmptyArray(array)" })
        protected RubyArray mulEmpty(RubyArray array, long count) {
            final RubyClass logicalClass = array.getLogicalClass();
            return new RubyArray(
                    logicalClass,
                    getLanguage().arrayShape,
                    ArrayStoreLibrary.INITIAL_STORE,
                    0);
        }

        @Specialization(guards = { "!isImplicitLong(count)" })
        protected Object fallback(RubyArray array, Object count) {
            return FAILURE;
        }
    }

    @CoreMethod(names = { "at" }, required = 1, lowerFixnum = 1)
    @NodeChild(value = "array", type = RubyNode.class)
    @NodeChild(value = "index", type = RubyNode.class)
    public abstract static class AtNode extends CoreMethodNode {

        abstract Object executeAt(RubyArray array, Object index);

        public static AtNode create() {
            return ArrayNodesFactory.AtNodeFactory.create(null, null);
        }

        @Specialization
        protected Object at(RubyArray array, int index,
                @Cached ReadNormalizedNode readNormalizedNode,
                @Cached ConditionProfile denormalized) {
            if (denormalized.profile(index < 0)) {
                index += array.size;
            }
            return readNormalizedNode.executeRead(array, index);
        }

        @Specialization
        protected Object at(RubyArray array, long index) {
            assert !CoreLibrary.fitsIntoInteger(index);
            return nil;
        }

        @Specialization(guards = "!isImplicitLong(index)")
        protected Object at(RubyArray array, Object index,
                @Cached ToLongNode toLongNode,
                @Cached FixnumLowerNode lowerNode,
                @Cached AtNode atNode) {
            return atNode.executeAt(array, lowerNode.executeLower(toLongNode.execute(index)));
        }
    }

    @CoreMethod(
            names = { "[]", "slice" },
            split = Split.ALWAYS,
            required = 1,
            optional = 1,
            lowerFixnum = { 1, 2 },
            argumentNames = { "index_start_or_range", "length" })
    public abstract static class IndexNode extends ArrayCoreMethodNode {

        abstract Object executeIntIndices(RubyArray array, int start, int length);

        @Specialization
        protected Object index(RubyArray array, int index, NotProvided length,
                @Cached ConditionProfile negativeIndexProfile,
                @Cached ReadNormalizedNode readNode) {
            if (negativeIndexProfile.profile(index < 0)) {
                index += array.size;
            }
            return readNode.executeRead(array, index);
        }

        @Specialization
        protected Object indexRange(RubyArray array, RubyRange range, NotProvided length,
                @Cached NormalizedStartLengthNode startLengthNode,
                @Cached ReadSliceNormalizedNode readSlice) {
            final int[] startLength = startLengthNode.execute(range, array.size);
            final int len = Math.max(startLength[1], 0); // negative range ending maps to zero length
            return readSlice.executeReadSlice(array, startLength[0], len);
        }

        @Specialization(guards = { "!isImplicitInteger(index)", "!isRubyRange(index)" })
        protected Object indexFallback(RubyArray array, Object index, NotProvided length,
                @Cached AtNode accessWithIndexConversion) {
            return accessWithIndexConversion.executeAt(array, index);
        }

        @Specialization
        protected Object slice(RubyArray array, int start, int length,
                @Cached ReadSliceNormalizedNode readSliceNode,
                @Cached ConditionProfile negativeIndexProfile) {
            if (length < 0) {
                return nil;
            }
            if (negativeIndexProfile.profile(start < 0)) {
                start += array.size;
            }
            return readSliceNode.executeReadSlice(array, start, length);
        }

        @Specialization(guards = { "wasProvided(length)", "!isImplicitInteger(start) || !isImplicitInteger(length)" })
        protected Object sliceFallback(RubyArray array, Object start, Object length,
                @Cached ToIntNode indexToInt,
                @Cached ToIntNode lengthToInt) {
            return executeIntIndices(array, indexToInt.execute(start), lengthToInt.execute(length));
        }
    }

    @CoreMethod(
            names = "[]=",
            split = Split.ALWAYS,
            required = 2,
            optional = 1,
            lowerFixnum = { 1, 2 },
            raiseIfFrozenSelf = true,
            argumentNames = { "index_start_or_range", "length_or_value", "value" })
    @ImportStatic(ArrayHelpers.class)
    public abstract static class SetIndexNode extends ArrayCoreMethodNode {

        public static SetIndexNode create() {
            return ArrayNodesFactory.SetIndexNodeFactory.create(null);
        }

        abstract Object executeIntIndex(RubyArray array, int index, Object value, NotProvided unused);

        abstract Object executeIntIndices(RubyArray array, int index, int length, Object replacement);

        // array[index] = object

        @Specialization
        @ReportPolymorphism.Exclude
        protected Object set(RubyArray array, int index, Object value, NotProvided unused,
                @Cached ArrayWriteNormalizedNode writeNode,
                @Cached ConditionProfile negativeDenormalizedIndex,
                @Cached BranchProfile negativeNormalizedIndex) {
            final int size = array.size;
            final int nIndex = normalize(size, index, negativeDenormalizedIndex, negativeNormalizedIndex);
            return writeNode.executeWrite(array, nIndex, value);
        }

        @Specialization
        protected Object setRange(RubyArray array, RubyRange range, Object value, NotProvided unused,
                @Cached NormalizedStartLengthNode normalizedStartLength,
                @Cached BranchProfile negativeStart) {
            final int[] startLength = normalizedStartLength.execute(range, array.size);
            final int start = startLength[0];
            if (start < 0) {
                negativeStart.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().rangeError(Utils.concat("index ", start, " out of bounds"), this));
            }
            final int length = Math.max(startLength[1], 0); // negative range ending maps to zero length
            return executeIntIndices(array, start, length, value);
        }

        @Specialization(guards = { "!isImplicitInteger(start)", "!isRubyRange(start)" })
        protected Object fallbackBinary(RubyArray array, Object start, Object value, NotProvided unused,
                @Cached ToIntNode toInt) {
            return executeIntIndex(array, toInt.execute(start), value, unused);
        }

        // array[start, length] = array2

        @Specialization(guards = { "wasProvided(replacement)", "length < 0" })
        protected Object negativeLength(RubyArray array, int start, int length, Object replacement) {
            throw new RaiseException(getContext(), coreExceptions().negativeLengthError(length, this));
        }

        @Specialization(guards = "length >= 0")
        protected Object setTernary(RubyArray array, int start, int length, RubyArray replacement,
                @Cached ConditionProfile negativeDenormalizedIndex,
                @Cached BranchProfile negativeNormalizedIndex,
                @Cached ConditionProfile moveNeeded,
                @Cached ArrayPrepareForCopyNode prepareToCopy,
                @Cached ArrayCopyCompatibleRangeNode shift,
                @Cached ArrayCopyCompatibleRangeNode copyRange,
                @Cached ArrayTruncateNode truncate) {

            final int originalSize = array.size;
            start = normalize(originalSize, start, negativeDenormalizedIndex, negativeNormalizedIndex);
            final int replacementSize = replacement.size;
            final int overwrittenAreaEnd = start + length;
            final int tailSize = originalSize - overwrittenAreaEnd;

            if (moveNeeded.profile(tailSize > 0)) {
                // There is a tail (the part of the array to the right of the overwritten area) to be moved.
                // Possibly, this is a move of size 0 (replacement size == length), which is optimized in the copy node.

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
                "!isRubyArray(replacement)",
                "wasProvided(replacement)",
                "length >= 0" })
        protected Object setTernary(RubyArray array, int start, int length, Object replacement,
                @Cached ArrayConvertNode convert,
                @Cached SetIndexNode recurse) {
            recurse.executeIntIndices(array, start, length, convert.execute(replacement));
            return replacement;
        }

        @Specialization(
                guards = { "!isImplicitInteger(start) || !isImplicitInteger(length)", "wasProvided(replacement)" })
        protected Object fallbackTernary(RubyArray array, Object start, Object length, Object replacement,
                @Cached ToIntNode startToInt,
                @Cached ToIntNode lengthToInt) {
            return executeIntIndices(array, startToInt.execute(start), lengthToInt.execute(length), replacement);
        }

        // Helpers

        protected int normalize(int arraySize, int index,
                ConditionProfile negativeDenormalizedIndex, BranchProfile negativeNormalizedIndex) {
            if (negativeDenormalizedIndex.profile(index < 0)) {
                index += arraySize;
                if (index < 0) {
                    negativeNormalizedIndex.enter();
                    throw new RaiseException(
                            getContext(),
                            coreExceptions().indexTooSmallError("array", index, arraySize, this));
                }
            }
            return index;
        }
    }

    @CoreMethod(names = "clear", raiseIfFrozenSelf = true)
    public abstract static class ClearNode extends ArrayCoreMethodNode {

        @Specialization
        protected RubyArray clear(RubyArray array) {
            setStoreAndSize(array, ArrayStoreLibrary.INITIAL_STORE, 0);
            return array;
        }

    }

    @CoreMethod(names = "compact")
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class CompactNode extends ArrayCoreMethodNode {

        @Specialization(guards = "stores.isPrimitive(array.store)", limit = "storageStrategyLimit()")
        protected RubyArray compactPrimitive(RubyArray array,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached ArrayCopyOnWriteNode cowNode) {
            final int size = array.size;
            return createArray(cowNode.execute(array, 0, size), size);
        }

        @Specialization(guards = "!stores.isPrimitive(array.store)", limit = "storageStrategyLimit()")
        protected Object compactObjectsNonMutable(RubyArray array,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached ArrayBuilderNode arrayBuilder,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
            final int size = array.size;
            final Object store = array.store;
            BuilderState state = arrayBuilder.start(size);

            int m = 0;

            loopProfile.profileCounted(size);
            for (int n = 0; loopProfile.inject(n < size); n++) {
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

        @Specialization(guards = "stores.isPrimitive(array.store)", limit = "storageStrategyLimit()")
        @ReportPolymorphism.Exclude
        protected Object compactNotObjects(RubyArray array,
                @CachedLibrary("array.store") ArrayStoreLibrary stores) {
            return nil;
        }

        @Specialization(guards = "!stores.isPrimitive(array.store)", limit = "storageStrategyLimit()")
        protected Object compactObjectsNonMutable(RubyArray array,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @CachedLibrary(limit = "1") ArrayStoreLibrary mutableStores,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
            final int size = array.size;
            final Object oldStore = array.store;
            final Object newStore;
            if (!stores.isMutable(oldStore)) {
                newStore = stores.allocator(oldStore).allocate(size);
            } else {
                newStore = oldStore;
            }

            int m = 0;

            loopProfile.profileCounted(size);
            for (int n = 0; loopProfile.inject(n < size); n++) {
                Object v = stores.read(oldStore, n);
                if (v != nil) {
                    mutableStores.write(newStore, m, v);
                    m++;
                }
            }

            stores.clear(oldStore, m, size - m);

            array.store = newStore;
            array.size = m;

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
        protected RubyArray concatZero(RubyArray array, NotProvided first, Object[] rest) {
            return array;
        }

        @Specialization(guards = { "wasProvided(first)", "rest.length == 0" })
        protected RubyArray concatOne(RubyArray array, Object first, Object[] rest,
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
        protected RubyArray concatMany(RubyArray array, Object first, Object[] rest,
                @Cached("rest.length") int cachedLength,
                @Cached("createInternal()") ToAryNode toAryNode,
                @Cached ArrayAppendManyNode appendManyNode,
                @Cached ArrayCopyOnWriteNode cowNode,
                @Cached ConditionProfile selfArgProfile) {
            int size = array.size;
            RubyArray copy = createArray(cowNode.execute(array, 0, size), size);
            RubyArray result = appendManyNode.executeAppendMany(array, toAryNode.executeToAry(first));
            for (int i = 0; i < cachedLength; ++i) {
                final RubyArray argOrCopy = selfArgProfile.profile(rest[i] == array)
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
        protected RubyArray concatManyGeneral(RubyArray array, Object first, Object[] rest,
                @Cached("createInternal()") ToAryNode toAryNode,
                @Cached ArrayAppendManyNode appendManyNode,
                @Cached ArrayCopyOnWriteNode cowNode,
                @Cached ConditionProfile selfArgProfile,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
            final int size = array.size;
            Object store = cowNode.execute(array, 0, size);

            RubyArray result = appendManyNode.executeAppendMany(array, toAryNode.executeToAry(first));
            loopProfile.profileCounted(rest.length);
            for (int i = 0; loopProfile.inject(i < rest.length); i++) {
                Object arg = rest[i];
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
                guards = "stores.isMutable(array.store)",
                limit = "storageStrategyLimit()")
        protected Object delete(VirtualFrame frame, RubyArray array, Object value, Object maybeBlock,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {

            return delete(frame, array, value, maybeBlock, true, array.store, array.store, stores, stores, loopProfile);
        }

        @Specialization(
                guards = "!oldStores.isMutable(array.store)",
                limit = "storageStrategyLimit()")
        protected Object delete(VirtualFrame frame, RubyArray array, Object value, Object maybeBlock,
                @CachedLibrary("array.store") ArrayStoreLibrary oldStores,
                @CachedLibrary(limit = "1") ArrayStoreLibrary newStores,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {

            final Object oldStore = array.store;
            final Object newStore = oldStores.allocator(oldStore).allocate(array.size);
            return delete(
                    frame,
                    array,
                    value,
                    maybeBlock,
                    false,
                    oldStore,
                    newStore,
                    oldStores,
                    newStores,
                    loopProfile);
        }

        private Object delete(VirtualFrame frame, RubyArray array, Object value, Object maybeBlock,
                boolean sameStores,
                Object oldStore,
                Object newStore,
                ArrayStoreLibrary oldStores,
                ArrayStoreLibrary newStores,
                LoopConditionProfile loopProfile) {

            assert !sameStores || (oldStore == newStore && oldStores == newStores);

            final int size = array.size;
            Object found = nil;

            int i = 0;
            int n = 0;
            loopProfile.profileCounted(size);
            while (loopProfile.inject(n < size)) {
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
                if (sameStores) {
                    oldStores.clear(oldStore, i, size - i);
                }
                array.store = newStore;
                array.size = i;
                return found;
            } else {
                if (maybeBlock == nil) {
                    return nil;
                } else {
                    return yield((RubyProc) maybeBlock, value);
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
                guards = "stores.isMutable(array.store)",
                limit = "storageStrategyLimit()")
        protected Object deleteAt(RubyArray array, int index,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached ConditionProfile negativeIndexProfile,
                @Cached ConditionProfile notInBoundsProfile) {
            final int size = array.size;
            int i = index;
            if (negativeIndexProfile.profile(index < 0)) {
                i += size;
            }

            if (notInBoundsProfile.profile(i < 0 || i >= size)) {
                return nil;
            } else {
                final Object store = array.store;
                final Object value = stores.read(store, i);
                stores.copyContents(store, i + 1, store, i, size - i - 1);
                stores.clear(store, size - 1, 1);
                array.store = store;
                array.size = size - 1;
                return value;
            }
        }

        @Specialization(
                guards = "!stores.isMutable(array.store)",
                limit = "storageStrategyLimit()")
        protected Object deleteAtCopying(RubyArray array, int index,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached ConditionProfile negativeIndexProfile,
                @Cached ConditionProfile notInBoundsProfile) {
            final int size = array.size;
            int i = index;
            if (negativeIndexProfile.profile(index < 0)) {
                i += size;
            }

            if (notInBoundsProfile.profile(i < 0 || i >= size)) {
                return nil;
            } else {
                final Object store = array.store;
                final Object mutableStore = stores.allocator(store).allocate(size - 1);
                stores.copyContents(store, 0, mutableStore, 0, i);
                final Object value = stores.read(store, i);
                stores.copyContents(store, i + 1, mutableStore, i, size - i - 1);
                array.store = mutableStore;
                array.size = size - 1;
                return value;
            }
        }
    }

    @CoreMethod(names = "each", needsBlock = true, enumeratorSize = "size")
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class EachNode extends YieldingCoreMethodNode implements ArrayElementConsumerNode {

        @Specialization
        protected Object each(RubyArray array, RubyProc block,
                @Cached ArrayEachIteratorNode iteratorNode) {
            return iteratorNode.execute(array, block, 0, this);
        }

        @Override
        public void accept(RubyArray array, RubyProc block, Object element, int index) {
            yield(block, element);
        }

    }

    @Primitive(name = "array_each_with_index")
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class EachWithIndexNode extends PrimitiveArrayArgumentsNode
            implements ArrayElementConsumerNode {

        @Child private CallBlockNode yieldNode = CallBlockNode.create();

        @Specialization
        protected Object eachOther(RubyArray array, RubyProc block,
                @Cached ArrayEachIteratorNode iteratorNode) {
            return iteratorNode.execute(array, block, 0, this);
        }

        @Override
        public void accept(RubyArray array, RubyProc block, Object element, int index) {
            yieldNode.yield(block, element, index);
        }

    }

    @Primitive(name = "array_equal")
    @ImportStatic(ArrayGuards.class)
    public abstract static class EqualNode extends PrimitiveArrayArgumentsNode {

        @Child private SameOrEqualNode sameOrEqualNode = SameOrEqualNode.create();

        @Specialization(
                guards = { "stores.accepts(b.store)", "stores.isPrimitive(a.store)" },
                limit = "storageStrategyLimit()")
        protected boolean equalSamePrimitiveType(RubyArray a, RubyArray b,
                @CachedLibrary("a.store") ArrayStoreLibrary stores,
                @Cached ConditionProfile sameProfile,
                @Cached("createIdentityProfile()") IntValueProfile sizeProfile,
                @Cached ConditionProfile sameSizeProfile,
                @Cached BranchProfile trueProfile,
                @Cached BranchProfile falseProfile,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {

            if (sameProfile.profile(a == b)) {
                return true;
            }

            final int aSize = sizeProfile.profile(a.size);
            final int bSize = b.size;

            if (!sameSizeProfile.profile(aSize == bSize)) {
                return false;
            }

            final Object aStore = a.store;
            final Object bStore = b.store;

            loopProfile.profileCounted(aSize);
            for (int i = 0; loopProfile.inject(i < aSize); i++) {
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
                guards = { "!stores.accepts(b.store)", "stores.isPrimitive(a.store)" },
                limit = "storageStrategyLimit()")
        protected Object equalDifferentPrimitiveType(RubyArray a, RubyArray b,
                @CachedLibrary("a.store") ArrayStoreLibrary stores) {
            return FAILURE;
        }

        @Specialization(
                guards = { "stores.accepts(a.store)", "!stores.isPrimitive(a.store)" },
                limit = "storageStrategyLimit()")
        protected Object equalNotPrimitiveType(RubyArray a, RubyArray b,
                @CachedLibrary("a.store") ArrayStoreLibrary stores) {
            return FAILURE;
        }

        @Specialization(guards = "!isRubyArray(b)")
        protected Object equalNotArray(RubyArray a, Object b) {
            return FAILURE;
        }

    }

    @Primitive(name = "array_eql")
    @ImportStatic(ArrayGuards.class)
    public abstract static class EqlNode extends PrimitiveArrayArgumentsNode {

        @Child private SameOrEqlNode eqlNode = SameOrEqlNodeFactory.create(null);

        @Specialization(
                guards = { "stores.accepts(b.store)", "stores.isPrimitive(a.store)" },
                limit = "storageStrategyLimit()")
        protected boolean eqlSamePrimitiveType(RubyArray a, RubyArray b,
                @CachedLibrary("a.store") ArrayStoreLibrary stores,
                @Cached ConditionProfile sameProfile,
                @Cached("createIdentityProfile()") IntValueProfile sizeProfile,
                @Cached ConditionProfile sameSizeProfile,
                @Cached BranchProfile trueProfile,
                @Cached BranchProfile falseProfile,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {

            if (sameProfile.profile(a == b)) {
                return true;
            }

            final int aSize = sizeProfile.profile(a.size);
            final int bSize = b.size;

            if (!sameSizeProfile.profile(aSize == bSize)) {
                return false;
            }

            final Object aStore = a.store;
            final Object bStore = b.store;

            loopProfile.profileCounted(aSize);
            for (int i = 0; loopProfile.inject(i < aSize); i++) {
                if (!eqlNode.executeSameOrEql(stores.read(aStore, i), stores.read(bStore, i))) {
                    falseProfile.enter();
                    return false;
                }
            }

            trueProfile.enter();
            return true;
        }

        @Specialization(
                guards = { "!stores.accepts(b.store)", "stores.isPrimitive(a.store)" },
                limit = "storageStrategyLimit()")
        protected Object eqlDifferentPrimitiveType(RubyArray a, RubyArray b,
                @CachedLibrary("a.store") ArrayStoreLibrary stores) {
            return FAILURE;
        }

        @Specialization(
                guards = { "!stores.isPrimitive(a.store)" },
                limit = "storageStrategyLimit()")
        protected Object eqlNotPrimitiveType(RubyArray a, RubyArray b,
                @CachedLibrary("a.store") ArrayStoreLibrary stores) {
            return FAILURE;
        }

        @Specialization(guards = "!isRubyArray(b)")
        protected Object eqlNotArray(RubyArray a, Object b) {
            return FAILURE;
        }

    }

    @CoreMethod(names = "fill", rest = true, needsBlock = true, raiseIfFrozenSelf = true)
    @ReportPolymorphism
    public abstract static class FillNode extends ArrayCoreMethodNode {

        @Specialization(
                guards = { "args.length == 1", "stores.acceptsValue(array.store, value(args))" },
                limit = "storageStrategyLimit()")
        protected RubyArray fill(RubyArray array, Object[] args, Nil block,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached PropagateSharingNode propagateSharingNode,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
            final Object value = args[0];
            propagateSharingNode.executePropagate(array, value);

            final Object store = array.store;
            final int size = array.size;

            loopProfile.profileCounted(size);
            for (int i = 0; loopProfile.inject(i < size); i++) {
                stores.write(store, i, value);
            }
            return array;
        }

        protected Object value(Object[] args) {
            return args[0];
        }

        @Specialization
        protected Object fillFallback(VirtualFrame frame, RubyArray array, Object[] args, Nil block,
                @Cached DispatchNode callFillInternal) {
            return callFillInternal.call(array, "fill_internal", args);
        }

        @Specialization
        protected Object fillFallback(VirtualFrame frame, RubyArray array, Object[] args, RubyProc block,
                @Cached DispatchNode callFillInternal) {
            return callFillInternal.callWithBlock(array, "fill_internal", block, args);
        }

    }

    @CoreMethod(names = "hash_internal", visibility = Visibility.PRIVATE)
    @ReportPolymorphism
    public abstract static class HashNode extends ArrayCoreMethodNode {

        private static final int CLASS_SALT = 42753062; // random number, stops hashes for similar values but different classes being the same, static because we want deterministic hashes

        @Specialization(limit = "storageStrategyLimit()")
        protected long hash(VirtualFrame frame, RubyArray array,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached DispatchNode toHashNode,
                @Cached ToLongNode toLongNode,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
            final int size = array.size;
            long h = getContext().getHashing(this).start(size);
            h = Hashing.update(h, CLASS_SALT);
            final Object store = array.store;

            loopProfile.profileCounted(size);
            for (int n = 0; loopProfile.inject(n < size); n++) {
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
        protected boolean include(RubyArray array, Object value,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
            final Object store = array.store;

            loopProfile.profileCounted(array.size);
            for (int n = 0; loopProfile.inject(n < array.size); n++) {
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
        @Child private DispatchNode toAryNode;
        @Child private KernelNodes.RespondToNode respondToToAryNode;

        protected abstract RubyArray executeInitialize(RubyArray array, Object size, Object fillingValue,
                Nil block);

        @Specialization
        protected RubyArray initializeNoArgs(RubyArray array, NotProvided size, NotProvided fillingValue, Nil block) {
            setStoreAndSize(array, ArrayStoreLibrary.INITIAL_STORE, 0);
            return array;
        }

        @Specialization
        protected RubyArray initializeOnlyBlock(
                RubyArray array, NotProvided size, NotProvided fillingValue, RubyProc block) {
            setStoreAndSize(array, ArrayStoreLibrary.INITIAL_STORE, 0);
            return array;
        }

        @TruffleBoundary
        @Specialization(guards = "size < 0")
        protected RubyArray initializeNegativeIntSize(
                RubyArray array, int size, Object unusedFillingValue, Object unusedBlock) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("negative array size", this));
        }

        @TruffleBoundary
        @Specialization(guards = "size < 0")
        protected RubyArray initializeNegativeLongSize(
                RubyArray array, long size, Object unusedFillingValue, Object unusedBlock) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("negative array size", this));
        }

        protected static final long MAX_INT = Integer.MAX_VALUE;

        @TruffleBoundary
        @Specialization(guards = "size >= MAX_INT")
        protected RubyArray initializeSizeTooBig(RubyArray array, long size, NotProvided fillingValue, Nil block) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("array size too big", this));
        }

        @Specialization(guards = "size >= 0")
        protected RubyArray initializeWithSizeNoValue(RubyArray array, int size, NotProvided fillingValue, Nil block) {
            final Object[] store = new Object[size];
            Arrays.fill(store, nil);
            setStoreAndSize(array, store, size);
            return array;
        }

        @Specialization(
                guards = { "size >= 0", "wasProvided(fillingValue)" },
                limit = "storageStrategyLimit()")
        protected RubyArray initializeWithSizeAndValue(RubyArray array, int size, Object fillingValue, Nil block,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @CachedLibrary(limit = "1") ArrayStoreLibrary allocatedStores,
                @Cached ConditionProfile needsFill,
                @Cached PropagateSharingNode propagateSharingNode,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
            final Object allocatedStore = stores.allocateForNewValue(array.store, fillingValue, size);
            if (needsFill.profile(!allocatedStores.isDefaultValue(allocatedStore, fillingValue))) {
                propagateSharingNode.executePropagate(array, fillingValue);
                loopProfile.profileCounted(size);
                for (int i = 0; loopProfile.inject(i < size); i++) {
                    allocatedStores.write(allocatedStore, i, fillingValue);
                }
            }
            setStoreAndSize(array, allocatedStore, size);
            return array;
        }

        @Specialization(
                guards = { "wasProvided(size)", "!isImplicitLong(size)", "wasProvided(fillingValue)" })
        protected RubyArray initializeSizeOther(RubyArray array, Object size, Object fillingValue, Nil block) {
            int intSize = toInt(size);
            return executeInitialize(array, intSize, fillingValue, block);
        }

        // With block

        @Specialization(guards = "size >= 0")
        protected Object initializeBlock(RubyArray array, int size, Object unusedFillingValue, RubyProc block,
                @Cached ArrayBuilderNode arrayBuilder,
                @Cached PropagateSharingNode propagateSharingNode,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
            BuilderState state = arrayBuilder.start(size);

            int n = 0;
            try {
                loopProfile.profileCounted(size);
                for (; loopProfile.inject(n < size); n++) {
                    final Object value = yield(block, n);
                    propagateSharingNode.executePropagate(array, value);
                    arrayBuilder.appendValue(state, n, value);
                }
            } finally {
                LoopNode.reportLoopCount(this, n);
                setStoreAndSize(array, arrayBuilder.finish(state, n), n);
            }

            return array;
        }

        @Specialization
        protected RubyArray initializeFromArray(
                RubyArray array, RubyArray copy, NotProvided unusedValue, Object maybeBlock,
                @Cached ReplaceNode replaceNode) {
            replaceNode.executeReplace(array, copy);
            return array;
        }

        @Specialization(
                guards = { "!isImplicitLong(object)", "wasProvided(object)", "!isRubyArray(object)" })
        protected RubyArray initialize(RubyArray array, Object object, NotProvided unusedValue, Nil block) {
            RubyArray copy = null;
            if (respondToToAry(getLanguage(), object)) {
                Object toAryResult = callToAry(object);
                if (toAryResult instanceof RubyArray) {
                    copy = (RubyArray) toAryResult;
                }
            }

            if (copy != null) {
                return executeInitialize(array, copy, NotProvided.INSTANCE, nil);
            } else {
                int size = toInt(object);
                return executeInitialize(array, size, NotProvided.INSTANCE, nil);
            }
        }

        public boolean respondToToAry(RubyLanguage language, Object object) {
            if (respondToToAryNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                respondToToAryNode = insert(KernelNodesFactory.RespondToNodeFactory.create(null, null, null));
            }
            return respondToToAryNode
                    .executeDoesRespondTo(null, object, language.coreStrings.TO_ARY.createInstance(getContext()), true);
        }

        protected Object callToAry(Object object) {
            if (toAryNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toAryNode = insert(DispatchNode.create());
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
        protected RubyArray initializeCopy(RubyArray self, RubyArray from,
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

        @Child private DispatchNode dispatch = DispatchNode.create(PUBLIC);

        // With block

        @Specialization(guards = { "isEmptyArray(array)", "wasProvided(initialOrSymbol)" })
        @ReportPolymorphism.Exclude
        protected Object injectEmptyArray(RubyArray array, Object initialOrSymbol, NotProvided symbol, RubyProc block) {
            return initialOrSymbol;
        }

        @Specialization(guards = { "isEmptyArray(array)" })
        @ReportPolymorphism.Exclude
        protected Object injectEmptyArrayNoInitial(
                RubyArray array, NotProvided initialOrSymbol, NotProvided symbol, RubyProc block) {
            return nil;
        }

        @Specialization(
                guards = {
                        "!isEmptyArray(array)",
                        "wasProvided(initialOrSymbol)" },
                limit = "storageStrategyLimit()")
        protected Object injectWithInitial(RubyArray array, Object initialOrSymbol, NotProvided symbol, RubyProc block,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
            final Object store = array.store;
            return injectBlockHelper(stores, array, block, store, initialOrSymbol, 0, loopProfile);
        }

        @Specialization(
                guards = { "!isEmptyArray(array)" },
                limit = "storageStrategyLimit()")
        protected Object injectNoInitial(
                RubyArray array, NotProvided initialOrSymbol, NotProvided symbol, RubyProc block,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
            final Object store = array.store;
            return injectBlockHelper(stores, array, block, store, stores.read(store, 0), 1, loopProfile);
        }

        public Object injectBlockHelper(ArrayStoreLibrary stores, RubyArray array,
                RubyProc block, Object store, Object initial, int start,
                LoopConditionProfile loopProfile) {
            Object accumulator = initial;
            int n = start;
            try {
                loopProfile.profileCounted(array.size - n);
                for (; loopProfile.inject(n < array.size); n++) {
                    accumulator = yield(block, accumulator, stores.read(store, n));
                }
            } finally {
                LoopNode.reportLoopCount(this, n);
            }

            return accumulator;
        }

        // With Symbol

        @Specialization(
                guards = {
                        "isEmptyArray(array)",
                        "wasProvided(initialOrSymbol)" })
        protected Object injectSymbolEmptyArray(RubyArray array, Object initialOrSymbol, RubySymbol symbol, Nil block) {
            return initialOrSymbol;
        }

        @Specialization(guards = { "isEmptyArray(array)" })
        protected Object injectSymbolEmptyArrayNoInitial(
                RubyArray array, RubySymbol initialOrSymbol, NotProvided symbol, Nil block) {
            return nil;
        }

        @Specialization(
                guards = {
                        "!isEmptyArray(array)",
                        "wasProvided(initialOrSymbol)" },
                limit = "storageStrategyLimit()")
        protected Object injectSymbolWithInitial(
                VirtualFrame frame, RubyArray array, Object initialOrSymbol, RubySymbol symbol, Nil block,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile,
                @Cached ToJavaStringNode toJavaString) {
            final Object store = array.store;
            return injectSymbolHelper(
                    frame,
                    array,
                    toJavaString.executeToJavaString(symbol),
                    stores,
                    store,
                    initialOrSymbol,
                    0,
                    loopProfile);
        }

        @Specialization(
                guards = { "!isEmptyArray(array)" },
                limit = "storageStrategyLimit()")
        protected Object injectSymbolNoInitial(
                VirtualFrame frame, RubyArray array, RubySymbol initialOrSymbol, NotProvided symbol, Nil block,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile,
                @Cached ToJavaStringNode toJavaString) {
            final Object store = array.store;
            return injectSymbolHelper(
                    frame,
                    array,
                    toJavaString.executeToJavaString(initialOrSymbol),
                    stores,
                    store,
                    stores.read(store, 0),
                    1,
                    loopProfile);
        }

        public Object injectSymbolHelper(VirtualFrame frame, RubyArray array, String symbol,
                ArrayStoreLibrary stores, Object store, Object initial, int start,
                LoopConditionProfile loopProfile) {
            Object accumulator = initial;
            int n = start;
            try {
                loopProfile.profileCounted(array.size - n);
                for (; loopProfile.inject(n < array.size); n++) {
                    accumulator = dispatch
                            .dispatch(frame, accumulator, symbol, nil, new Object[]{ stores.read(store, n) });
                }
            } finally {
                LoopNode.reportLoopCount(this, n);
            }
            return accumulator;
        }

    }

    @CoreMethod(names = { "map", "collect" }, needsBlock = true, enumeratorSize = "size")
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class MapNode extends YieldingCoreMethodNode {

        @Specialization(limit = "storageStrategyLimit()")
        protected Object map(RubyArray array, RubyProc block,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached ArrayBuilderNode arrayBuilder,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
            final Object store = array.store;
            final int size = array.size;
            BuilderState state = arrayBuilder.start(size);

            int n = 0;
            try {
                loopProfile.profileCounted(array.size);
                for (; loopProfile.inject(n < array.size); n++) {
                    final Object mappedValue = yield(block, stores.read(store, n));
                    arrayBuilder.appendValue(state, n, mappedValue);
                }
            } finally {
                LoopNode.reportLoopCount(this, n);
            }

            return createArray(arrayBuilder.finish(state, size), size);
        }

    }

    @CoreMethod(names = { "map!", "collect!" }, needsBlock = true, enumeratorSize = "size", raiseIfFrozenSelf = true)
    @ImportStatic(ArrayGuards.class)
    public abstract static class MapInPlaceNode extends YieldingCoreMethodNode implements ArrayElementConsumerNode {

        @Child private ArrayWriteNormalizedNode writeNode = ArrayWriteNormalizedNodeGen.create();

        @Specialization
        protected Object map(RubyArray array, RubyProc block,
                @Cached ArrayEachIteratorNode iteratorNode) {
            return iteratorNode.execute(array, block, 0, this);
        }

        @Override
        public void accept(RubyArray array, RubyProc block, Object element, int index) {
            writeNode.executeWrite(array, index, yield(block, element));
        }

    }

    @NodeChild(value = "array", type = RubyNode.class)
    @NodeChild(value = "format", type = RubyNode.class)
    @CoreMethod(names = "pack", required = 1)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
    @ReportPolymorphism
    public abstract static class PackNode extends CoreMethodNode {

        @Child private RopeNodes.MakeLeafRopeNode makeLeafRopeNode;
        @Child private StringNodes.MakeStringNode makeStringNode;
        @Child private WriteObjectFieldNode writeAssociatedNode;

        private final BranchProfile exceptionProfile = BranchProfile.create();
        private final ConditionProfile resizeProfile = ConditionProfile.create();

        @CreateCast("format")
        protected RubyNode coerceFormat(RubyNode format) {
            return ToStrNodeGen.create(format);
        }

        @Specialization(
                guards = {
                        "libFormat.isRubyString(format)",
                        "equalNode.execute(libFormat.getRope(format), cachedFormat)" },
                limit = "getCacheLimit()")
        protected RubyString packCached(RubyArray array, Object format,
                @CachedLibrary(limit = "2") RubyStringLibrary libFormat,
                @Cached("libFormat.getRope(format)") Rope cachedFormat,
                @Cached("cachedFormat.byteLength()") int cachedFormatLength,
                @Cached("create(compileFormat(libFormat.getRope(format)))") DirectCallNode callPackNode,
                @Cached RopeNodes.EqualNode equalNode) {
            final BytesResult result;

            try {
                result = (BytesResult) callPackNode.call(
                        new Object[]{ array.store, array.size, false, null });
            } catch (FormatException e) {
                exceptionProfile.enter();
                throw FormatExceptionTranslator.translate(getContext(), this, e);
            }

            return finishPack(cachedFormatLength, result);
        }

        @Specialization(guards = { "libFormat.isRubyString(format)" }, replaces = "packCached")
        protected RubyString packUncached(RubyArray array, Object format,
                @CachedLibrary(limit = "2") RubyStringLibrary libFormat,
                @Cached IndirectCallNode callPackNode) {
            final BytesResult result;

            final Rope formatRope = libFormat.getRope(format);
            try {
                result = (BytesResult) callPackNode.call(
                        compileFormat(formatRope),
                        new Object[]{ array.store, array.size, false, null });
            } catch (FormatException e) {
                exceptionProfile.enter();
                throw FormatExceptionTranslator.translate(getContext(), this, e);
            }

            return finishPack(formatRope.byteLength(), result);
        }

        private RubyString finishPack(int formatLength, BytesResult result) {
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

            final RubyString string = makeStringNode.fromRope(makeLeafRopeNode.executeMake(
                    bytes,
                    result.getEncoding().getEncodingForLength(formatLength),
                    result.getStringCodeRange(),
                    result.getStringLength()));

            if (result.getAssociated() != null) {
                if (writeAssociatedNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    writeAssociatedNode = insert(WriteObjectFieldNode.create());
                }

                writeAssociatedNode.execute(string, Layouts.ASSOCIATED_IDENTIFIER, result.getAssociated());
            }

            return string;
        }

        @TruffleBoundary
        protected RootCallTarget compileFormat(Rope rope) {
            final String javaString = RopeOperations.decodeRope(rope);
            return new PackCompiler(getContext(), this).compile(javaString);
        }

        protected int getCacheLimit() {
            return getLanguage().options.PACK_CACHE;
        }

    }

    @CoreMethod(names = "pop", raiseIfFrozenSelf = true, optional = 1, lowerFixnum = 1)
    @ReportPolymorphism
    public abstract static class PopNode extends ArrayCoreMethodNode {

        public abstract Object executePop(RubyArray array, Object n);

        @Specialization
        @ReportPolymorphism.Exclude
        protected Object pop(RubyArray array, NotProvided n,
                @Cached ArrayPopOneNode popOneNode) {
            return popOneNode.executePopOne(array);
        }

        @Specialization(guards = "n < 0")
        @ReportPolymorphism.Exclude
        protected Object popNNegative(RubyArray array, int n) {
            throw new RaiseException(getContext(), coreExceptions().argumentErrorNegativeArraySize(this));
        }

        @Specialization(guards = { "n >= 0", "isEmptyArray(array)" })
        @ReportPolymorphism.Exclude
        protected RubyArray popEmpty(RubyArray array, int n) {
            return createEmptyArray();
        }

        @Specialization(guards = { "n == 0", "!isEmptyArray(array)" })
        @ReportPolymorphism.Exclude
        protected RubyArray popZeroNotEmpty(RubyArray array, int n) {
            return createEmptyArray();
        }

        @Specialization(
                guards = { "n > 0", "!isEmptyArray(array)", "!stores.isMutable(array.store)" },
                limit = "storageStrategyLimit()")
        protected RubyArray popNotEmptySharedStorage(RubyArray array, int n,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached ConditionProfile minProfile) {
            final int size = array.size;
            final int numPop = minProfile.profile(size < n) ? size : n;
            final Object store = array.store;

            final Object popped = stores.extractRange(store, size - numPop, size); // copy on write
            final Object prefix = stores.extractRange(store, 0, size - numPop);    // copy on write
            // NOTE(norswap): if one of these two arrays outlives the other, you get a memory leak

            setStoreAndSize(array, prefix, size - numPop);
            return createArray(popped, numPop);
        }

        @Specialization(
                guards = { "n > 0", "!isEmptyArray(array)", "stores.isMutable(array.store)" },
                limit = "storageStrategyLimit()")
        protected RubyArray popNotEmptyUnsharedStorage(RubyArray array, int n,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached ConditionProfile minProfile) {
            final int size = array.size;
            final int numPop = minProfile.profile(size < n) ? size : n;
            final Object store = array.store;

            // Extract values in a new array
            final Object popped = stores.allocator(store).allocate(numPop);
            stores.copyContents(store, size - numPop, popped, 0, numPop);

            // Remove the end from the original array.
            stores.clear(store, size - numPop, numPop);
            setSize(array, size - numPop);

            return createArray(popped, numPop);
        }

        @Specialization(guards = { "wasProvided(n)", "!isImplicitInteger(n)" })
        protected Object popNToInt(RubyArray array, Object n,
                @Cached ToIntNode toIntNode) {
            return executePop(array, toIntNode.execute(n));
        }

    }

    @CoreMethod(names = "<<", raiseIfFrozenSelf = true, required = 1)
    public abstract static class AppendNode extends ArrayCoreMethodNode {

        @Child private ArrayAppendOneNode appendOneNode = ArrayAppendOneNode.create();

        @Specialization
        protected RubyArray append(RubyArray array, Object value) {
            return appendOneNode.executeAppendOne(array, value);
        }

    }

    @CoreMethod(names = { "push", "append" }, rest = true, optional = 1, raiseIfFrozenSelf = true)
    public abstract static class PushNode extends ArrayCoreMethodNode {

        @Child private ArrayAppendOneNode appendOneNode = ArrayAppendOneNode.create();

        @Specialization(guards = "rest.length == 0")
        protected RubyArray pushZero(RubyArray array, NotProvided value, Object[] rest) {
            return array;
        }

        @Specialization(guards = { "rest.length == 0", "wasProvided(value)" })
        protected RubyArray pushOne(RubyArray array, Object value, Object[] rest) {
            return appendOneNode.executeAppendOne(array, value);
        }

        @Specialization(guards = { "rest.length > 0", "wasProvided(value)" })
        protected RubyArray pushMany(VirtualFrame frame, RubyArray array, Object value, Object[] rest,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
            // NOTE (eregon): Appending one by one here to avoid useless generalization to Object[]
            // if the arguments all fit in the current storage
            appendOneNode.executeAppendOne(array, value);
            loopProfile.profileCounted(rest.length);
            for (int i = 0; loopProfile.inject(i < rest.length); i++) {
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
        protected Object rejectOther(RubyArray array, RubyProc block,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached ArrayBuilderNode arrayBuilder,
                @Cached BooleanCastNode booleanCastNode,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
            final Object store = array.store;
            final int size = array.size;

            BuilderState state = arrayBuilder.start(size);
            int selectedSize = 0;

            int n = 0;
            try {
                loopProfile.profileCounted(size);
                for (; loopProfile.inject(n < size); n++) {
                    final Object value = stores.read(store, n);

                    if (!booleanCastNode.executeToBoolean(yield(block, value))) {
                        arrayBuilder.appendValue(state, selectedSize, value);
                        selectedSize++;
                    }
                }
            } finally {
                LoopNode.reportLoopCount(this, n);
            }

            return createArray(arrayBuilder.finish(state, selectedSize), selectedSize);
        }

    }

    @CoreMethod(names = "reject!", needsBlock = true, enumeratorSize = "size", raiseIfFrozenSelf = true)
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class RejectInPlaceNode extends YieldingCoreMethodNode {

        @Child private BooleanCastNode booleanCastNode = BooleanCastNode.create();

        @Specialization(guards = "array.size == 0")
        protected Object rejectEmpty(RubyArray array, RubyProc block) {
            return nil;
        }

        @Specialization(
                guards = { "array.size > 0", "stores.isMutable(array.store)" },
                limit = "storageStrategyLimit()")
        protected Object rejectInPlaceMutableStore(RubyArray array, RubyProc block,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @CachedLibrary(limit = "1") ArrayStoreLibrary mutablestores,
                @Cached("createCountingProfile()") LoopConditionProfile loop1Profile,
                @Cached("createCountingProfile()") LoopConditionProfile loop2Profile) {
            return rejectInPlaceInternal(array, block, mutablestores, array.store, loop1Profile, loop2Profile);
        }

        @Specialization(
                guards = { "array.size > 0", "!stores.isMutable(array.store)" },
                limit = "storageStrategyLimit()")
        protected Object rejectInPlaceImmutableStore(RubyArray array, RubyProc block,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @CachedLibrary(limit = "1") ArrayStoreLibrary mutablestores,
                @Cached("createCountingProfile()") LoopConditionProfile loop1Profile,
                @Cached("createCountingProfile()") LoopConditionProfile loop2Profile) {
            final Object mutableStore = stores.allocator(array.store).allocate(array.size);
            stores.copyContents(array.store, 0, mutableStore, 0, array.size);
            array.store = mutableStore;
            return rejectInPlaceInternal(array, block, mutablestores, mutableStore, loop1Profile, loop2Profile);
        }

        private Object rejectInPlaceInternal(RubyArray array, RubyProc block, ArrayStoreLibrary stores,
                Object store, LoopConditionProfile loop1Profile, LoopConditionProfile loop2Profile) {
            int i = 0;
            int n = 0;
            try {
                loop1Profile.profileCounted(array.size);
                for (; loop1Profile.inject(n < array.size); n++) {
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
                loop2Profile.profileCounted(array.size - n);
                for (; loop2Profile.inject(n < array.size); n++) {
                    if (i != n) {
                        stores.write(store, i, stores.read(store, n));
                    }
                    i++;
                }

                // Null out the elements behind the size
                stores.clear(store, i, n - i);
                setSize(array, i);

                LoopNode.reportLoopCount(this, n);
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

        public abstract RubyArray executeReplace(RubyArray array, RubyArray other);

        @CreateCast("other")
        protected RubyNode coerceOtherToAry(RubyNode index) {
            return ToAryNodeGen.create(index);
        }

        @Specialization
        protected RubyArray replace(RubyArray array, RubyArray other,
                @Cached ArrayCopyOnWriteNode cowNode) {
            propagateSharingNode.executePropagate(array, other);

            final int size = other.size;

            array.store = cowNode.execute(other, 0, size);
            array.size = size;
            return array;
        }

    }

    @Primitive(name = "array_rotate", lowerFixnum = 1)
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class RotateNode extends PrimitiveArrayArgumentsNode {

        @Specialization(limit = "storageStrategyLimit()")
        protected RubyArray rotate(RubyArray array, int rotation,
                @CachedLibrary("array.store") ArrayStoreLibrary arrays,
                @Cached("createIdentityProfile()") IntValueProfile sizeProfile,
                @Cached("createIdentityProfile()") IntValueProfile rotationProfile) {
            final int size = sizeProfile.profile(array.size);
            rotation = rotationProfile.profile(rotation);
            assert 0 < rotation && rotation < size;

            final Object original = array.store;
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
                guards = "arrays.isMutable(array.store)",
                limit = "storageStrategyLimit()")
        protected RubyArray rotate(RubyArray array, int rotation,
                @CachedLibrary("array.store") ArrayStoreLibrary arrays,
                @Cached("createIdentityProfile()") IntValueProfile sizeProfile,
                @Cached("createIdentityProfile()") IntValueProfile rotationProfile,
                @Cached("createCountingProfile()") LoopConditionProfile loop1Profile,
                @Cached("createCountingProfile()") LoopConditionProfile loop2Profile,
                @Cached("createCountingProfile()") LoopConditionProfile loop3Profile) {
            final int size = sizeProfile.profile(array.size);
            rotation = rotationProfile.profile(rotation);
            assert 0 < rotation && rotation < size;
            final Object store = array.store;

            if (CompilerDirectives.isPartialEvaluationConstant(size) &&
                    CompilerDirectives.isPartialEvaluationConstant(rotation) &&
                    size <= ArrayGuards.ARRAY_MAX_EXPLODE_SIZE) {
                rotateSmallExplode(arrays, rotation, size, store);
            } else {
                rotateReverse(
                        arrays,
                        rotation,
                        size,
                        store,
                        loop1Profile,
                        loop2Profile,
                        loop3Profile);
            }

            return array;
        }

        @Specialization(
                guards = { "!arrays.isMutable(array.store)" },
                limit = "storageStrategyLimit()")
        protected RubyArray rotateStorageNotMutable(RubyArray array, int rotation,
                @CachedLibrary("array.store") ArrayStoreLibrary arrays,
                @Cached("createIdentityProfile()") IntValueProfile sizeProfile,
                @Cached("createIdentityProfile()") IntValueProfile rotationProfile) {
            final int size = sizeProfile.profile(array.size);
            rotation = rotationProfile.profile(rotation);
            assert 0 < rotation && rotation < size;

            final Object original = array.store;
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

        protected void rotateReverse(ArrayStoreLibrary stores, int rotation, int size, Object store,
                LoopConditionProfile loop1Profile,
                LoopConditionProfile loop2Profile,
                LoopConditionProfile loop3Profile) {
            // Rotating by rotation in-place is equivalent to
            // replace([rotation..-1] + [0...rotation])
            // which is the same as reversing the whole array and
            // reversing each of the two parts so that elements are in the same order again.
            // This trick avoids constantly checking if indices are within array bounds
            // and accesses memory sequentially, even though it does perform 2*size reads and writes.
            // This is also what MRI and JRuby do.
            reverse(stores, store, rotation, size, loop1Profile);
            reverse(stores, store, 0, rotation, loop2Profile);
            reverse(stores, store, 0, size, loop3Profile);
        }

        private void reverse(ArrayStoreLibrary stores,
                Object store, int from, int until, LoopConditionProfile loopProfile) {
            int to = until - 1;
            final int loopCount = (until - from) >> 1;
            loopProfile.profileCounted(loopCount);
            while (loopProfile.inject(from < to)) {
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
        protected Object select(RubyArray array, RubyProc block,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile,
                @Cached ArrayBuilderNode arrayBuilder,
                @Cached BooleanCastNode booleanCastNode) {
            final Object store = array.store;

            BuilderState state = arrayBuilder.start(array.size);
            int selectedSize = 0;

            int n = 0;
            loopProfile.profileCounted(array.size);
            try {
                for (; loopProfile.inject(n < array.size); n++) {
                    final Object value = stores.read(store, n);

                    if (booleanCastNode.executeToBoolean(yield(block, value))) {
                        arrayBuilder.appendValue(state, selectedSize, value);
                        selectedSize++;
                    }
                }
            } finally {
                LoopNode.reportLoopCount(this, n);
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

        public abstract Object executeShift(RubyArray array, Object n);

        // No n, just shift 1 element and return it

        @Specialization(guards = "isEmptyArray(array)")
        @ReportPolymorphism.Exclude
        protected Object shiftEmpty(RubyArray array, NotProvided n) {
            return nil;
        }

        @Specialization(guards = "!isEmptyArray(array)", limit = "storageStrategyLimit()")
        protected Object shiftOther(RubyArray array, NotProvided n,
                @CachedLibrary("array.store") ArrayStoreLibrary stores) {
            final int size = array.size;
            final Object store = array.store;
            final Object value = stores.read(store, 0);
            stores.clear(store, 0, 1);
            array.store = stores.extractRange(store, 1, size);
            setSize(array, size - 1);
            return value;
        }

        // n given, shift the first n elements and return them as an Array

        @Specialization(guards = "n < 0")
        protected Object shiftNegative(RubyArray array, int n) {
            throw new RaiseException(getContext(), coreExceptions().argumentErrorNegativeArraySize(this));
        }

        @Specialization(guards = "n == 0")
        protected Object shiftZero(RubyArray array, int n) {
            return createEmptyArray();
        }

        @Specialization(guards = { "n > 0", "isEmptyArray(array)" })
        protected Object shiftManyEmpty(RubyArray array, int n) {
            return createEmptyArray();
        }

        @Specialization(
                guards = { "n > 0", "!isEmptyArray(array)" },
                limit = "storageStrategyLimit()")
        protected Object shiftMany(RubyArray array, int n,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached ConditionProfile minProfile) {
            final int size = array.size;
            final int numShift = minProfile.profile(size < n) ? size : n;
            final Object store = array.store;
            final Object result = stores.extractRange(store, 0, numShift);
            array.store = stores.extractRange(store, numShift, size);
            setSize(array, size - numShift);
            return createArray(result, numShift);
        }

        @Specialization(guards = { "wasProvided(n)", "!isImplicitInteger(n)" })
        protected Object shiftNToInt(RubyArray array, Object n,
                @Cached ToIntNode toIntNode) {
            return executeShift(array, toIntNode.execute(n));
        }
    }

    @CoreMethod(names = { "size", "length" })
    public abstract static class SizeNode extends ArrayCoreMethodNode {

        @Specialization
        protected int size(RubyArray array,
                @Cached("createIdentityProfile()") IntValueProfile profile) {
            return profile.profile(array.size);
        }

    }

    @CoreMethod(names = "sort", needsBlock = true)
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class SortNode extends ArrayCoreMethodNode {

        @Specialization(guards = "isEmptyArray(array)")
        @ReportPolymorphism.Exclude
        protected RubyArray sortEmpty(RubyArray array, Object unusedBlock) {
            return createEmptyArray();
        }

        @ExplodeLoop
        @Specialization(
                guards = { "!isEmptyArray(array)", "isSmall(array)" },
                limit = "storageStrategyLimit()")
        protected RubyArray sortVeryShort(VirtualFrame frame, RubyArray array, Nil block,
                @CachedLibrary("array.store") ArrayStoreLibrary originalStores,
                @CachedLibrary(limit = "1") ArrayStoreLibrary stores,
                @Cached DispatchNode compareDispatchNode,
                @Cached CmpIntNode cmpIntNode) {
            final Object originalStore = array.store;
            final Object store = originalStores
                    .allocator(originalStore)
                    .allocate(getContext().getOptions().ARRAY_SMALL);
            final int size = array.size;

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
                        "stores.isPrimitive(array.store)" },
                assumptions = {
                        "getLanguage().coreMethodAssumptions.integerCmpAssumption",
                        "getLanguage().coreMethodAssumptions.floatCmpAssumption" },
                limit = "storageStrategyLimit()")
        protected Object sortPrimitiveArrayNoBlock(RubyArray array, Nil block,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @CachedLibrary(limit = "1") ArrayStoreLibrary mutableStores) {
            final int size = array.size;
            Object oldStore = array.store;
            Object newStore = stores.allocator(oldStore).allocate(size);
            stores.copyContents(oldStore, 0, newStore, 0, size);
            mutableStores.sort(newStore, size);
            return createArray(newStore, size);
        }

        @Specialization(
                guards = { "!isEmptyArray(array)", "!isSmall(array)" },
                limit = "storageStrategyLimit()")
        protected Object sortArrayWithoutBlock(RubyArray array, Nil block,
                @CachedLibrary("array.store") ArrayStoreLibrary stores,
                @Cached DispatchNode fallbackNode) {
            return fallbackNode.call(array, "sort_fallback");
        }

        @Specialization(guards = "!isEmptyArray(array)")
        protected Object sortGenericWithBlock(RubyArray array, RubyProc block,
                @Cached DispatchNode fallbackNode) {
            return fallbackNode.callWithBlock(array, "sort_fallback", block);
        }

        protected boolean isSmall(RubyArray array) {
            return array.size <= getContext().getOptions().ARRAY_SMALL;
        }

    }

    @Primitive(name = "steal_array_storage")
    @ImportStatic(ArrayGuards.class)
    public abstract static class StealArrayStorageNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "array == other")
        protected RubyArray stealStorageNoOp(RubyArray array, RubyArray other) {
            return array;
        }

        @Specialization(guards = "array != other")
        protected RubyArray stealStorage(RubyArray array, RubyArray other,
                @Cached PropagateSharingNode propagateSharingNode) {
            propagateSharingNode.executePropagate(array, other);

            final int size = other.size;
            final Object store = other.store;
            array.store = store;
            array.size = size;
            other.store = ArrayStoreLibrary.INITIAL_STORE;
            other.size = 0;

            return array;
        }

    }

    @Primitive(name = "array_storage_equal?")
    public abstract static class ArrayStorageEqualNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean storageEqual(RubyArray array, RubyArray other) {
            final Object arrayStore = array.store;
            final Object otherStore = other.store;
            // Array#shift and #pop do not modify the underlying storage but still mutate the Array
            return arrayStore instanceof DelegatedArrayStorage && otherStore instanceof DelegatedArrayStorage &&
                    ((DelegatedArrayStorage) arrayStore).isEquivalentTo((DelegatedArrayStorage) otherStore) &&
                    array.size == other.size;
        }

    }

    @Primitive(name = "array_zip")
    @ImportStatic(ArrayGuards.class)
    @ReportPolymorphism
    public abstract static class ZipNode extends PrimitiveArrayArgumentsNode {

        @Specialization(limit = "storageStrategyLimit()")
        protected RubyArray zipToPairs(RubyArray array, RubyArray other,
                @CachedLibrary("array.store") ArrayStoreLibrary aStores,
                @CachedLibrary("other.store") ArrayStoreLibrary bStores,
                @CachedLibrary(limit = "1") ArrayStoreLibrary pairs,
                @Cached ConditionProfile bNotSmallerProfile,
                @Cached("createCountingProfile()") LoopConditionProfile loopProfile) {
            final Object a = array.store;
            final Object b = other.store;

            final int bSize = other.size;
            final int zippedLength = array.size;

            final Object[] zipped = new Object[zippedLength];

            loopProfile.profileCounted(zippedLength);
            for (int n = 0; loopProfile.inject(n < zippedLength); n++) {
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

        @Specialization(guards = "!oldStores.isNative(array.store)", limit = "storageStrategyLimit()")
        protected RubyArray storeToNative(RubyArray array,
                @CachedLibrary("array.store") ArrayStoreLibrary oldStores) {
            int size = array.size;
            Object oldStore = array.store;
            Pointer pointer = Pointer.malloc(size * Pointer.SIZE);
            pointer.enableAutorelease(getContext().getFinalizationService());
            NativeArrayStorage newStore = new NativeArrayStorage(pointer, size);
            oldStores.copyContents(oldStore, 0, newStore, 0, size);
            getContext().getMarkingService().addMarker(
                    newStore,
                    (aStore) -> ((NativeArrayStorage) aStore).preserveMembers());
            array.store = newStore;
            return array;
        }

        @Specialization(guards = "oldStores.isNative(array.store)", limit = "storageStrategyLimit()")
        protected RubyArray storeIsNative(RubyArray array,
                @CachedLibrary("array.store") ArrayStoreLibrary oldStores) {
            return array;
        }
    }

    @Primitive(name = "array_store_address")
    @ImportStatic(ArrayGuards.class)
    public abstract static class StoreAddressNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "stores.isNative(array.store)", limit = "storageStrategyLimit()")
        protected long storeIsNative(RubyArray array,
                @CachedLibrary("array.store") ArrayStoreLibrary stores) {
            NativeArrayStorage storage = (NativeArrayStorage) array.store;
            return storage.getAddress();
        }

        /** See {@link RubyDynamicObject#asPointer} **/
        @Specialization(guards = "!stores.isNative(array.store)", limit = "storageStrategyLimit()")
        protected Object storeNotNative(RubyArray array,
                @CachedLibrary("array.store") ArrayStoreLibrary stores) {
            return DispatchNode.MISSING; // for UnsupportedMessageException
        }
    }

    @Primitive(name = "array_store_native?")
    @ImportStatic(ArrayGuards.class)
    public abstract static class IsStoreNativeNode extends PrimitiveArrayArgumentsNode {

        @Specialization(limit = "storageStrategyLimit()")
        protected boolean isStoreNative(RubyArray array,
                @CachedLibrary("array.store") ArrayStoreLibrary stores) {
            return stores.isNative(array.store);
        }
    }

    @Primitive(name = "array_flatten_helper", lowerFixnum = 2)
    public abstract static class FlattenHelperNode extends PrimitiveArrayArgumentsNode {

        static class Entry {
            final RubyArray array;
            final int index;

            private Entry(RubyArray array, int index) {
                this.array = array;
                this.index = index;
            }
        }

        @Specialization(guards = "!canContainObject.execute(array)")
        protected boolean flattenHelperPrimitive(RubyArray array, RubyArray out, int maxLevels,
                @Cached ArrayAppendManyNode concat,
                @Cached TypeNodes.CanContainObjectNode canContainObject) {
            concat.executeAppendMany(out, array);
            return false;
        }

        @Specialization(replaces = "flattenHelperPrimitive")
        protected boolean flattenHelper(RubyArray array, RubyArray out, int maxLevels,
                @Cached TypeNodes.CanContainObjectNode canContainObject,
                @Cached ArrayAppendManyNode concat,
                @Cached AtNode at,
                @Cached DispatchNode convert,
                @Cached ArrayAppendOneNode append) {

            boolean modified = false;
            final EconomicSet<RubyArray> visited = EconomicSet.create(Equivalence.IDENTITY);
            final SimpleStack<Entry> workStack = new SimpleStack<>();
            workStack.push(new Entry(array, 0));

            while (!workStack.isEmpty()) {
                final Entry e = workStack.pop();

                if (e.index == 0) {
                    if (!canContainObject.execute(e.array)) {
                        concat.executeAppendMany(out, e.array);
                        continue;
                    } else if (contains(visited, e.array)) {
                        throw new RaiseException(
                                getContext(),
                                coreExceptions().argumentError("tried to flatten recursive array", this));
                    } else if (maxLevels == workStack.size()) {
                        concat.executeAppendMany(out, e.array);
                        continue;
                    }
                    add(visited, e.array);
                }

                int i = e.index;
                for (; i < e.array.size; ++i) {
                    final Object obj = at.executeAt(e.array, i);
                    final Object converted = convert.call(
                            coreLibrary().truffleTypeModule,
                            "rb_check_convert_type",
                            obj,
                            coreLibrary().arrayClass,
                            coreSymbols().TO_ARY);
                    if (converted == nil) {
                        append.executeAppendOne(out, obj);
                    } else {
                        modified = true;
                        workStack.push(new Entry(e.array, i + 1));
                        workStack.push(new Entry((RubyArray) converted, 0));
                        break;
                    }
                }
                if (i == e.array.size) {
                    remove(visited, e.array);
                }
            }

            return modified;
        }

        @TruffleBoundary
        private static boolean contains(EconomicSet<RubyArray> set, RubyArray array) {
            return set.contains(array);
        }

        @TruffleBoundary
        private static void remove(EconomicSet<RubyArray> set, RubyArray array) {
            set.remove(array);
        }

        @TruffleBoundary
        private static void add(EconomicSet<RubyArray> set, RubyArray array) {
            set.add(array);
        }
    }
}
