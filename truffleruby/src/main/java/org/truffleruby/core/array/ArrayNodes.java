/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.profiles.IntValueProfile;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.Hashing;
import org.truffleruby.core.array.ArrayNodesFactory.ReplaceNodeFactory;
import org.truffleruby.core.cast.ToAryNodeGen;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.cast.ToIntNodeGen;
import org.truffleruby.core.format.BytesResult;
import org.truffleruby.core.format.FormatExceptionTranslator;
import org.truffleruby.core.format.exceptions.FormatException;
import org.truffleruby.core.format.pack.PackCompiler;
import org.truffleruby.core.kernel.KernelNodes;
import org.truffleruby.core.kernel.KernelNodesFactory;
import org.truffleruby.core.kernel.KernelNodes.SameOrEqlNode;
import org.truffleruby.core.kernel.KernelNodes.SameOrEqualNode;
import org.truffleruby.core.kernel.KernelNodesFactory.SameOrEqlNodeFactory;
import org.truffleruby.core.kernel.KernelNodesFactory.SameOrEqualNodeFactory;
import org.truffleruby.core.numeric.FixnumLowerNodeGen;
import org.truffleruby.core.proc.ProcOperations;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.SnippetNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchHeadNodeFactory;
import org.truffleruby.language.dispatch.MissingBehavior;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.objects.IsFrozenNode;
import org.truffleruby.language.objects.IsFrozenNodeGen;
import org.truffleruby.language.objects.PropagateTaintNode;
import org.truffleruby.language.objects.TaintNode;
import org.truffleruby.language.yield.YieldNode;

import java.util.Arrays;

import static org.truffleruby.core.array.ArrayHelpers.getSize;
import static org.truffleruby.core.array.ArrayHelpers.getStore;
import static org.truffleruby.core.array.ArrayHelpers.setSize;
import static org.truffleruby.core.array.ArrayHelpers.setStoreAndSize;

@CoreClass("Array")
public abstract class ArrayNodes {

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            return allocateNode.allocate(rubyClass, null, 0);
        }

    }

    @CoreMethod(names = "+", required = 1)
    @NodeChildren({
        @NodeChild(type = RubyNode.class, value = "a"),
        @NodeChild(type = RubyNode.class, value = "b")
    })
    @ImportStatic(ArrayGuards.class)
    public abstract static class AddNode extends CoreMethodNode {

        @CreateCast("b")
        public RubyNode coerceOtherToAry(RubyNode other) {
            return ToAryNodeGen.create(other);
        }

        // Same storage

        @Specialization(guards = { "strategy.matches(a)", "strategy.matches(b)" }, limit = "ARRAY_STRATEGIES")
        public DynamicObject addSameType(DynamicObject a, DynamicObject b,
                @Cached("of(a)") ArrayStrategy strategy) {
            final int aSize = strategy.getSize(a);
            final int bSize = strategy.getSize(b);
            final int combinedSize = aSize + bSize;
            final ArrayMirror mirror = strategy.newArray(combinedSize);
            strategy.newMirror(a).copyTo(mirror, 0, 0, aSize);
            strategy.newMirror(b).copyTo(mirror, 0, aSize, bSize);
            return createArray(mirror.getArray(), combinedSize);
        }

        // Generalizations

        @Specialization(guards = { "aStrategy.matches(a)", "bStrategy.matches(b)", "aStrategy != bStrategy" }, limit = "ARRAY_STRATEGIES")
        public DynamicObject addGeneralize(DynamicObject a, DynamicObject b,
                @Cached("of(a)") ArrayStrategy aStrategy,
                @Cached("of(b)") ArrayStrategy bStrategy,
                @Cached("aStrategy.generalizeNew(bStrategy)") ArrayStrategy generalized) {
            final int aSize = aStrategy.getSize(a);
            final int bSize = bStrategy.getSize(b);
            final int combinedSize = aSize + bSize;
            final ArrayMirror mirror = generalized.newArray(combinedSize);
            aStrategy.newMirror(a).copyTo(mirror, 0, 0, aSize);
            bStrategy.newMirror(b).copyTo(mirror, 0, aSize, bSize);
            return createArray(mirror.getArray(), combinedSize);
        }

    }

    @Primitive(name = "array_mul", lowerFixnum = 1)
    @ImportStatic(ArrayGuards.class)
    public abstract static class MulNode extends PrimitiveArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();
        @Child private PropagateTaintNode propagateTaintNode = PropagateTaintNode.create();

        @Specialization(guards = "strategy.matches(array)", limit = "ARRAY_STRATEGIES")
        public DynamicObject mulOther(DynamicObject array, int count,
                @Cached("of(array)") ArrayStrategy strategy,
                @Cached("create()") BranchProfile errorProfile) {
            if (count < 0) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().argumentError("negative argument", this));
            }

            final int size = strategy.getSize(array);
            final int newSize = size * count;
            final ArrayMirror store = strategy.newMirror(array);
            final ArrayMirror newStore = strategy.newArray(newSize);
            for (int n = 0; n < count; n++) {
                store.copyTo(newStore, 0, n * size, size);
            }

            final DynamicObject result = allocateObjectNode.allocate(Layouts.BASIC_OBJECT.getLogicalClass(array), newStore.getArray(), newSize);
            propagateTaintNode.propagate(array, result);
            return result;
        }

        @Specialization(guards = "!isInteger(object)")
        public Object fallback(DynamicObject array, Object object) {
            return FAILURE;
        }

    }

    @Primitive(name = "array_aref", lowerFixnum = { 1, 2 })
    @ImportStatic(ArrayGuards.class)
    public abstract static class IndexNode extends PrimitiveArrayArgumentsNode {

        @Child private ArrayReadDenormalizedNode readNode;
        @Child private ArrayReadSliceDenormalizedNode readSliceNode;
        @Child private ArrayReadSliceNormalizedNode readNormalizedSliceNode;
        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        public Object index(DynamicObject array, int index, NotProvided length) {
            if (readNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                readNode = insert(ArrayReadDenormalizedNodeGen.create(null, null));
            }
            return readNode.executeRead(array, index);
        }

        @Specialization
        public DynamicObject slice(VirtualFrame frame, DynamicObject array, int start, int length) {
            if (length < 0) {
                return nil();
            }

            if (readSliceNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                readSliceNode = insert(ArrayReadSliceDenormalizedNodeGen.create(null, null, null));
            }

            return readSliceNode.executeReadSlice(array, start, length);
        }

        @Specialization(guards = "isIntRange(range)")
        public DynamicObject slice(VirtualFrame frame, DynamicObject array, DynamicObject range, NotProvided len,
                @Cached("createBinaryProfile()") ConditionProfile negativeBeginProfile,
                @Cached("createBinaryProfile()") ConditionProfile negativeEndProfile) {
            final int size = getSize(array);
            final int normalizedIndex = ArrayOperations.normalizeIndex(size, Layouts.INT_RANGE.getBegin(range), negativeBeginProfile);

            if (normalizedIndex < 0 || normalizedIndex > size) {
                return nil();
            } else {
                final int end = ArrayOperations.normalizeIndex(size, Layouts.INT_RANGE.getEnd(range), negativeEndProfile);
                final int exclusiveEnd = ArrayOperations.clampExclusiveIndex(size, Layouts.INT_RANGE.getExcludedEnd(range) ? end : end + 1);

                if (exclusiveEnd <= normalizedIndex) {
                    return allocateObjectNode.allocate(Layouts.BASIC_OBJECT.getLogicalClass(array), null, 0);
                }

                final int length = exclusiveEnd - normalizedIndex;

                if (readNormalizedSliceNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    readNormalizedSliceNode = insert(ArrayReadSliceNormalizedNodeGen.create(null, null, null));
                }

                return readNormalizedSliceNode.executeReadSlice(array, normalizedIndex, length);
            }
        }

        @Specialization(guards = { "!isInteger(a)", "!isIntRange(a)" })
        public Object fallbackIndex(VirtualFrame frame, DynamicObject array, Object a, NotProvided length) {
            return FAILURE;
        }

        @Specialization(guards = { "!isIntRange(a)", "wasProvided(b)" })
        public Object fallbackSlice(VirtualFrame frame, DynamicObject array, Object a, Object b) {
            return FAILURE;
        }

    }

    @Primitive(name = "array_aset", lowerFixnum = { 1, 2 }, raiseIfFrozenSelf = true)
    @ImportStatic(ArrayGuards.class)
    public abstract static class IndexSetNode extends PrimitiveArrayArgumentsNode {

        @Child private ArrayReadNormalizedNode readNode;
        @Child private ArrayWriteNormalizedNode writeNode;
        @Child private ArrayReadSliceNormalizedNode readSliceNode;

        private final BranchProfile negativeIndexProfile = BranchProfile.create();
        private final BranchProfile negativeLengthProfile = BranchProfile.create();

        public abstract Object executeSet(DynamicObject array, Object index, Object length, Object value);

        // array[index] = object

        @Specialization
        public Object set(DynamicObject array, int index, Object value, NotProvided unused,
                @Cached("createBinaryProfile()") ConditionProfile negativeIndexProfile) {
            final int normalizedIndex = ArrayOperations.normalizeIndex(getSize(array), index, negativeIndexProfile);
            checkIndex(array, index, normalizedIndex);
            return write(array, normalizedIndex, value);
        }

        // array[index] = object with non-int index

        @Specialization(guards = { "!isInteger(indexObject)", "!isRubyRange(indexObject)" })
        public Object set(DynamicObject array, Object indexObject, Object value, NotProvided unused) {
            return FAILURE;
        }

        // array[start, length] = object

        @Specialization(guards = { "!isRubyArray(value)", "wasProvided(value)" })
        public Object setObject(DynamicObject array, int start, int length, Object value) {
            return FAILURE;
        }

        // array[start, length] = other_array, with length == other_array.size

        @Specialization(guards = {
                "isRubyArray(replacement)",
                "length == getArraySize(replacement)"
        })
        public Object setOtherIntArraySameLength(DynamicObject array, int start, int length, DynamicObject replacement,
                @Cached("createBinaryProfile()") ConditionProfile negativeIndexProfile) {
            final int normalizedIndex = ArrayOperations.normalizeIndex(getSize(array), start, negativeIndexProfile);
            checkIndex(array, start, normalizedIndex);

            for (int i = 0; i < length; i++) {
                write(array, normalizedIndex + i, read(replacement, i));
            }
            return replacement;
        }

        // array[start, length] = other_array, with length != other_array.size

        @Specialization(guards = {
                "isRubyArray(replacement)",
                "length != getArraySize(replacement)"
        })
        public Object setOtherArray(DynamicObject array, int rawStart, int length, DynamicObject replacement,
                @Cached("createBinaryProfile()") ConditionProfile negativeIndexProfile,
                @Cached("createBinaryProfile()") ConditionProfile needCopy,
                @Cached("createBinaryProfile()") ConditionProfile recursive,
                @Cached("createBinaryProfile()") ConditionProfile emptyReplacement,
                @Cached("createBinaryProfile()") ConditionProfile grow) {
            checkLengthPositive(length);
            final int start = ArrayOperations.normalizeIndex(getSize(array), rawStart, negativeIndexProfile);
            checkIndex(array, rawStart, start);

            final int end = start + length;
            final int arraySize = getSize(array);
            final int replacementSize = getSize(replacement);
            final int endOfReplacementInArray = start + replacementSize;

            if (recursive.profile(array == replacement)) {
                final DynamicObject copy = readSlice(array, 0, arraySize);
                return executeSet(array, start, length, copy);
            }

            // Make a copy of what's after "end", as it might be erased or at least needs to be moved
            final int tailSize = arraySize - end;
            DynamicObject tailCopy = null;
            final boolean needsTail = needCopy.profile(tailSize > 0);
            if (needsTail) {
                tailCopy = readSlice(array, end, tailSize);
            }

            // Append the replacement array
            for (int i = 0; i < replacementSize; i++) {
                write(array, start + i, read(replacement, i));
            }

            // Append the saved tail
            if (needsTail) {
                for (int i = 0; i < tailSize; i++) {
                    write(array, endOfReplacementInArray + i, read(tailCopy, i));
                }
            } else if (emptyReplacement.profile(replacementSize == 0)) {
                // If no tail and the replacement is empty, the array will grow.
                // We need to append nil from index arraySize to index (start - 1).
                // E.g. a = [1,2,3]; a[5,1] = []; a == [1,2,3,nil,nil]
                if (grow.profile(arraySize < start)) {
                    for (int i = arraySize; i < start; i++) {
                        write(array, i, nil());
                    }
                }
            }

            // Set size
            if (needsTail) {
                setSize(array, endOfReplacementInArray + tailSize);
            } else {
                setSize(array, endOfReplacementInArray);
            }

            return replacement;
        }

        // array[start, length] = object_or_array with non-int start or length

        @Specialization(guards = { "!isInteger(startObject) || !isInteger(lengthObject)", "wasProvided(value)" })
        public Object setStartLengthNotInt(DynamicObject array, Object startObject, Object lengthObject, Object value) {
            return FAILURE;
        }

        // array[start..end] = object_or_array

        @Specialization(guards = "isIntRange(range)")
        public Object setRange(DynamicObject array, DynamicObject range, Object value, NotProvided unused,
                @Cached("createBinaryProfile()") ConditionProfile negativeBeginProfile,
                @Cached("createBinaryProfile()") ConditionProfile negativeEndProfile,
                @Cached("create()") BranchProfile errorProfile) {
            final int size = getSize(array);
            final int begin = Layouts.INT_RANGE.getBegin(range);
            final int start = ArrayOperations.normalizeIndex(size, begin, negativeBeginProfile);
            if (start < 0) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().rangeError(range, this));
            }
            final int end = ArrayOperations.normalizeIndex(size, Layouts.INT_RANGE.getEnd(range), negativeEndProfile);
            int inclusiveEnd = Layouts.INT_RANGE.getExcludedEnd(range) ? end - 1 : end;
            if (inclusiveEnd < 0) {
                inclusiveEnd = -1;
            }
            final int length = inclusiveEnd - start + 1;
            final int normalizeLength = length > -1 ? length : 0;
            return executeSet(array, start, normalizeLength, value);
        }

        @Specialization(guards = { "!isIntRange(range)", "isRubyRange(range)" })
        public Object setOtherRange(DynamicObject array, DynamicObject range, Object value, NotProvided unused) {
            return FAILURE;
        }

        // Helpers

        private void checkIndex(DynamicObject array, int index, int normalizedIndex) {
            if (normalizedIndex < 0) {
                negativeIndexProfile.enter();
                throw new RaiseException(coreExceptions().indexTooSmallError("array", index, getSize(array), this));
            }
        }

        public void checkLengthPositive(int length) {
            if (length < 0) {
                negativeLengthProfile.enter();
                throw new RaiseException(coreExceptions().negativeLengthError(length, this));
            }
        }

        protected int getArraySize(DynamicObject array) {
            return getSize(array);
        }

        private Object read(DynamicObject array, int index) {
            if (readNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                readNode = insert(ArrayReadNormalizedNodeGen.create(null, null));
            }
            return readNode.executeRead(array, index);
        }

        private Object write(DynamicObject array, int index, Object value) {
            if (writeNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                writeNode = insert(ArrayWriteNormalizedNodeGen.create(null, null, null));
            }
            return writeNode.executeWrite(array, index, value);
        }

        private DynamicObject readSlice(DynamicObject array, int start, int length) {
            if (readSliceNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                readSliceNode = insert(ArrayReadSliceNormalizedNodeGen.create(null, null, null));
            }
            return readSliceNode.executeReadSlice(array, start, length);
        }

    }

    @CoreMethod(names = "at", required = 1, lowerFixnum = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "array"),
            @NodeChild(type = RubyNode.class, value = "index")
    })
    public abstract static class AtNode extends CoreMethodNode {

        @Child private ArrayReadDenormalizedNode readNode;

        @CreateCast("index")
        public RubyNode coerceOtherToInt(RubyNode index) {
            return FixnumLowerNodeGen.create(ToIntNodeGen.create(index));
        }

        @Specialization
        public Object at(DynamicObject array, int index) {
            if (readNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                readNode = insert(ArrayReadDenormalizedNodeGen.create(null, null));
            }
            return readNode.executeRead(array, index);
        }

    }

    @CoreMethod(names = "clear", raiseIfFrozenSelf = true)
    public abstract static class ClearNode extends ArrayCoreMethodNode {

        @Specialization(guards = "strategy.matches(array)", limit = "ARRAY_STRATEGIES")
        public DynamicObject clear(DynamicObject array,
                @Cached("of(array)") ArrayStrategy strategy) {
            strategy.setStoreAndSize(array, null, 0);
            return array;
        }

    }

    @CoreMethod(names = "compact")
    @ImportStatic(ArrayGuards.class)
    public abstract static class CompactNode extends ArrayCoreMethodNode {

        @Specialization(guards = { "strategy.matches(array)", "!strategy.accepts(nil())" }, limit = "ARRAY_STRATEGIES")
        public DynamicObject compactPrimitive(DynamicObject array,
                @Cached("of(array)") ArrayStrategy strategy) {
            final int size = strategy.getSize(array);
            Object store = strategy.newMirror(array).extractRange(0, size).getArray();
            return createArray(store, size);
        }

        @Specialization(guards = { "strategy.matches(array)", "strategy.accepts(nil())" }, limit = "ARRAY_STRATEGIES")
        public Object compactObjects(DynamicObject array,
                @Cached("of(array)") ArrayStrategy strategy) {
            // TODO CS 9-Feb-15 by removing nil we could make this array suitable for a primitive array storage class

            final int size = strategy.getSize(array);
            final ArrayMirror store = strategy.newMirror(array);
            final ArrayMirror newStore = strategy.newArray(size);

            int m = 0;

            for (int n = 0; n < size; n++) {
                if (store.get(n) != nil()) {
                    newStore.set(m, store.get(n));
                    m++;
                }
            }

            return createArray(newStore.getArray(), m);
        }

    }

    @CoreMethod(names = "compact!", raiseIfFrozenSelf = true)
    public abstract static class CompactBangNode extends ArrayCoreMethodNode {

        @Specialization(guards = { "strategy.matches(array)", "!strategy.accepts(nil())" }, limit = "ARRAY_STRATEGIES")
        public DynamicObject compactNotObjects(DynamicObject array,
                @Cached("of(array)") ArrayStrategy strategy) {
            return nil();
        }

        @Specialization(guards = { "strategy.matches(array)", "strategy.accepts(nil())" }, limit = "ARRAY_STRATEGIES")
        public Object compactObjects(DynamicObject array,
                @Cached("of(array)") ArrayStrategy strategy) {
            final int size = strategy.getSize(array);
            final ArrayMirror store = strategy.newMirror(array);

            int m = 0;

            for (int n = 0; n < size; n++) {
                if (store.get(n) != nil()) {
                    store.set(m, store.get(n));
                    m++;
                }
            }

            strategy.setStoreAndSize(array, store.getArray(), m);

            if (m == size) {
                return nil();
            } else {
                return array;
            }
        }

    }

    @CoreMethod(names = "concat", required = 1, raiseIfFrozenSelf = true)
    @NodeChildren({
        @NodeChild(type = RubyNode.class, value = "array"),
        @NodeChild(type = RubyNode.class, value = "other")
    })
    @ImportStatic(ArrayGuards.class)
    public abstract static class ConcatNode extends CoreMethodNode {

        @Child private ArrayAppendManyNode appendManyNode = ArrayAppendManyNodeGen.create(null, null);

        @CreateCast("other")
        public RubyNode coerceOtherToAry(RubyNode other) {
            return ToAryNodeGen.create(other);
        }

        @Specialization
        public DynamicObject concat(DynamicObject array, DynamicObject other) {
            appendManyNode.executeAppendMany(array, other);
            return array;
        }

    }

    @CoreMethod(names = "delete", required = 1, needsBlock = true)
    @ImportStatic(ArrayGuards.class)
    public abstract static class DeleteNode extends YieldingCoreMethodNode {

        @Child private KernelNodes.SameOrEqualNode equalNode = KernelNodesFactory.SameOrEqualNodeFactory.create(null);
        @Child private IsFrozenNode isFrozenNode;

        @Specialization(guards = "strategy.matches(array)", limit = "ARRAY_STRATEGIES")
        public Object delete(VirtualFrame frame, DynamicObject array, Object value, Object maybeBlock,
                @Cached("of(array)") ArrayStrategy strategy) {
            final ArrayMirror store = strategy.newMirror(array);

            Object found = nil();

            int i = 0;
            int n = 0;
            while (n < strategy.getSize(array)) {
                final Object stored = store.get(n);

                if (equalNode.executeSameOrEqual(frame, stored, value)) {
                    checkFrozen(array);
                    found = stored;
                    n++;
                } else {
                    if (i != n) {
                        store.set(i, store.get(n));
                    }

                    i++;
                    n++;
                }
            }

            if (i != n) {
                strategy.setStoreAndSize(array, store.getArray(), i);
                return found;
            } else {
                if (maybeBlock == NotProvided.INSTANCE) {
                    return nil();
                } else {
                    return yield(frame, (DynamicObject) maybeBlock, value);
                }
            }
        }

        public void checkFrozen(Object object) {
            if (isFrozenNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                isFrozenNode = insert(IsFrozenNodeGen.create(null));
            }
            isFrozenNode.raiseIfFrozen(object);
        }

    }

    @CoreMethod(names = "delete_at", required = 1, raiseIfFrozenSelf = true, lowerFixnum = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "array"),
            @NodeChild(type = RubyNode.class, value = "index")
    })
    @ImportStatic(ArrayGuards.class)
    public abstract static class DeleteAtNode extends CoreMethodNode {

        @CreateCast("index")
        public RubyNode coerceOtherToInt(RubyNode index) {
            return ToIntNodeGen.create(index);
        }

        @Specialization(guards = "strategy.matches(array)", limit = "ARRAY_STRATEGIES")
        public Object deleteAt(DynamicObject array, int index,
                @Cached("of(array)") ArrayStrategy strategy,
                @Cached("createBinaryProfile()") ConditionProfile negativeIndexProfile,
                @Cached("createBinaryProfile()") ConditionProfile notInBoundsProfile) {
            final int size = strategy.getSize(array);
            final int i = ArrayOperations.normalizeIndex(size, index, negativeIndexProfile);

            if (notInBoundsProfile.profile(i < 0 || i >= size)) {
                return nil();
            } else {
                final ArrayMirror store = strategy.newMirror(array);
                final Object value = store.get(i);
                store.copyTo(store, i + 1, i, size - i - 1);
                strategy.setStoreAndSize(array, store.getArray(), size - 1);
                return value;
            }
        }

    }

    @CoreMethod(names = "each", needsBlock = true, enumeratorSize = "size")
    @ImportStatic(ArrayGuards.class)
    public abstract static class EachNode extends YieldingCoreMethodNode {

        @Child private CallDispatchHeadNode toEnumNode;

        @Specialization(guards = "strategy.matches(array)", limit = "ARRAY_STRATEGIES")
        public Object eachOther(VirtualFrame frame, DynamicObject array, DynamicObject block,
                @Cached("of(array)") ArrayStrategy strategy) {
            final ArrayMirror store = strategy.newMirror(array);

            int n = 0;
            try {
                for (; n < strategy.getSize(array); n++) {
                    yield(frame, block, store.get(n));
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, n);
                }
            }

            return array;
        }

    }

    @CoreMethod(names = "each_with_index", needsBlock = true, enumeratorSize = "size")
    @ImportStatic(ArrayGuards.class)
    public abstract static class EachWithIndexNode extends YieldingCoreMethodNode {

        @Specialization(guards = "strategy.matches(array)", limit = "ARRAY_STRATEGIES")
        public Object eachWithIndexOther(VirtualFrame frame, DynamicObject array, DynamicObject block,
                @Cached("of(array)") ArrayStrategy strategy) {
            final ArrayMirror store = strategy.newMirror(array);

            int n = 0;
            try {
                for (; n < strategy.getSize(array); n++) {
                    yield(frame, block, store.get(n), n);
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, n);
                }
            }

            return array;
        }

    }

    @Primitive(name = "array_equal")
    @ImportStatic(ArrayGuards.class)
    public abstract static class EqualNode extends PrimitiveArrayArgumentsNode {

        @Child private SameOrEqualNode equalNode = SameOrEqualNodeFactory.create(null);

        @Specialization(guards = { "isRubyArray(b)", "strategy.matches(a)", "strategy.matches(b)",
                "!strategy.accepts(nil())" }, limit = "ARRAY_STRATEGIES")
        protected boolean equalSamePrimitiveType(VirtualFrame frame, DynamicObject a, DynamicObject b,
                @Cached("of(a)") ArrayStrategy strategy,
                @Cached("createBinaryProfile()") ConditionProfile sameProfile,
                @Cached("createIdentityProfile()") IntValueProfile sizeProfile,
                @Cached("createBinaryProfile()") ConditionProfile sameSizeProfile,
                @Cached("create()") BranchProfile trueProfile,
                @Cached("create()") BranchProfile falseProfile) {

            if (sameProfile.profile(a == b)) {
                return true;
            }

            final int aSize = sizeProfile.profile(strategy.getSize(a));
            final int bSize = strategy.getSize(b);

            if (!sameSizeProfile.profile(aSize == bSize)) {
                return false;
            }

            final ArrayMirror aMirror = strategy.newMirror(a);
            final ArrayMirror bMirror = strategy.newMirror(b);

            for (int i = 0; i < aSize; i++) {
                if (!equalNode.executeSameOrEqual(frame, aMirror.get(i), bMirror.get(i))) {
                    falseProfile.enter();
                    return false;
                }
            }

            trueProfile.enter();
            return true;
        }

        @Fallback
        protected Object fallback(Object a, Object b) {
            return FAILURE;
        }

    }

    @Primitive(name = "array_eql")
    @ImportStatic(ArrayGuards.class)
    public abstract static class EqlNode extends PrimitiveArrayArgumentsNode {

        @Child private SameOrEqlNode eqlNode = SameOrEqlNodeFactory.create(null);

        @Specialization(guards = { "isRubyArray(b)", "strategy.matches(a)", "strategy.matches(b)",
                "!strategy.accepts(nil())" }, limit = "ARRAY_STRATEGIES")
        protected boolean eqlSamePrimitiveType(VirtualFrame frame, DynamicObject a, DynamicObject b,
                @Cached("of(a)") ArrayStrategy strategy,
                @Cached("createBinaryProfile()") ConditionProfile sameProfile,
                @Cached("createIdentityProfile()") IntValueProfile sizeProfile,
                @Cached("createBinaryProfile()") ConditionProfile sameSizeProfile,
                @Cached("create()") BranchProfile trueProfile,
                @Cached("create()") BranchProfile falseProfile) {

            if (sameProfile.profile(a == b)) {
                return true;
            }

            final int aSize = sizeProfile.profile(strategy.getSize(a));
            final int bSize = strategy.getSize(b);

            if (!sameSizeProfile.profile(aSize == bSize)) {
                return false;
            }

            final ArrayMirror aMirror = strategy.newMirror(a);
            final ArrayMirror bMirror = strategy.newMirror(b);

            for (int i = 0; i < aSize; i++) {
                if (!eqlNode.executeSameOrEql(frame, aMirror.get(i), bMirror.get(i))) {
                    falseProfile.enter();
                    return false;
                }
            }

            trueProfile.enter();
            return true;
        }

        @Fallback
        protected Object fallback(Object a, Object b) {
            return FAILURE;
        }

    }

    @CoreMethod(names = "fill", rest = true, needsBlock = true, raiseIfFrozenSelf = true)
    public abstract static class FillNode extends ArrayCoreMethodNode {

        @Specialization(guards = { "args.length == 1", "strategy.matches(array)", "strategy.accepts(value(args))" }, limit = "ARRAY_STRATEGIES")
        protected DynamicObject fill(DynamicObject array, Object[] args, NotProvided block,
                @Cached("of(array)") ArrayStrategy strategy) {
            final Object value = args[0];
            final ArrayMirror store = strategy.newMirror(array);
            final int size = strategy.getSize(array);
            for (int i = 0; i < size; i++) {
                store.set(i, value);
            }
            return array;
        }

        protected Object value(Object[] args) {
            return args[0];
        }

        @Specialization
        protected Object fillFallback(VirtualFrame frame, DynamicObject array, Object[] args, NotProvided block,
                @Cached("createMethodCall()") CallDispatchHeadNode callFillInternal) {
            return callFillInternal.call(frame, array, "fill_internal", args);
        }

        @Specialization
        protected Object fillFallback(VirtualFrame frame, DynamicObject array, Object[] args, DynamicObject block,
                @Cached("createMethodCall()") CallDispatchHeadNode callFillInternal) {
            return callFillInternal.callWithBlock(frame, array, "fill_internal", block, args);
        }

    }

    @CoreMethod(names = "hash_internal")
    public abstract static class HashNode extends ArrayCoreMethodNode {

        private static final int MURMUR_ARRAY_SEED = System.identityHashCode(ArrayNodes.class);

        @Child private ToIntNode toIntNode;

        @Specialization(guards = "strategy.matches(array)", limit = "ARRAY_STRATEGIES")
        public long hash(VirtualFrame frame, DynamicObject array,
                @Cached("of(array)") ArrayStrategy strategy,
                @Cached("createMethodCall()") CallDispatchHeadNode toHashNode) {
            final int size = strategy.getSize(array);
            long h = Hashing.start(size);
            h = Hashing.update(h, MURMUR_ARRAY_SEED);
            final ArrayMirror store = strategy.newMirror(array);

            for (int n = 0; n < size; n++) {
                final Object value = store.get(n);
                final long valueHash = toLong(frame, toHashNode.call(frame, value, "hash"));
                h = Hashing.update(h, valueHash);
            }

            return Hashing.end(h);
        }

        private long toLong(VirtualFrame frame, Object indexObject) {
            if (toIntNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toIntNode = insert(ToIntNode.create());
            }
            final Object result = toIntNode.executeIntOrLong(frame, indexObject);
            if (result instanceof Integer) {
                return (int) result;
            } else {
                return (long) result;
            }
        }

    }

    @CoreMethod(names = "include?", required = 1)
    public abstract static class IncludeNode extends ArrayCoreMethodNode {

        @Child private KernelNodes.SameOrEqualNode equalNode = KernelNodesFactory.SameOrEqualNodeFactory.create(null);

        @Specialization(guards = "strategy.matches(array)", limit = "ARRAY_STRATEGIES")
        public boolean include(VirtualFrame frame, DynamicObject array, Object value,
                @Cached("of(array)") ArrayStrategy strategy) {
            final ArrayMirror store = strategy.newMirror(array);

            for (int n = 0; n < strategy.getSize(array); n++) {
                final Object stored = store.get(n);

                if (equalNode.executeSameOrEqual(frame, stored, value)) {
                    return true;
                }
            }

            return false;
        }

    }

    @CoreMethod(names = "initialize", needsBlock = true, optional = 2, raiseIfFrozenSelf = true, lowerFixnum = 1)
    @ImportStatic(ArrayGuards.class)
    public abstract static class InitializeNode extends YieldingCoreMethodNode {

        @Child private ToIntNode toIntNode;
        @Child private CallDispatchHeadNode toAryNode;
        @Child private KernelNodes.RespondToNode respondToToAryNode;

        public abstract DynamicObject executeInitialize(VirtualFrame frame, DynamicObject array, Object size, Object defaultValue, Object block);

        @Specialization
        public DynamicObject initializeNoArgs(DynamicObject array, NotProvided size, NotProvided unusedValue, NotProvided block) {
            setStoreAndSize(array, null, 0);
            return array;
        }

        @Specialization
        public DynamicObject initializeOnlyBlock(DynamicObject array, NotProvided size, NotProvided unusedValue, DynamicObject block) {
            setStoreAndSize(array, null, 0);
            return array;
        }

        @TruffleBoundary
        @Specialization(guards = "size < 0")
        public DynamicObject initializeNegativeIntSize(DynamicObject array, int size, Object unusedValue, Object maybeBlock) {
            throw new RaiseException(coreExceptions().argumentError("negative array size", this));
        }

        @TruffleBoundary
        @Specialization(guards = "size < 0")
        public DynamicObject initializeNegativeLongSize(DynamicObject array, long size, Object unusedValue, Object maybeBlock) {
            throw new RaiseException(coreExceptions().argumentError("negative array size", this));
        }

        protected static final long MAX_INT = Integer.MAX_VALUE;

        @TruffleBoundary
        @Specialization(guards = "size >= MAX_INT")
        public DynamicObject initializeSizeTooBig(DynamicObject array, long size, NotProvided unusedValue, NotProvided block) {
            throw new RaiseException(coreExceptions().argumentError("array size too big", this));
        }

        @Specialization(guards = "size >= 0")
        public DynamicObject initializeWithSizeNoValue(DynamicObject array, int size, NotProvided unusedValue, NotProvided block) {
            final Object[] store = new Object[size];
            Arrays.fill(store, nil());
            setStoreAndSize(array, store, size);
            return array;
        }

        @Specialization(guards = { "size >= 0", "wasProvided(value)", "strategy.specializesFor(value)" }, limit = "ARRAY_STRATEGIES")
        public DynamicObject initializeWithSizeAndValue(DynamicObject array, int size, Object value, NotProvided block,
                @Cached("forValue(value)") ArrayStrategy strategy,
                @Cached("createBinaryProfile()") ConditionProfile needsFill) {
            final ArrayMirror store = strategy.newArray(size);
            if (needsFill.profile(!strategy.isDefaultValue(value))) {
                for (int i = 0; i < size; i++) {
                    store.set(i, value);
                }
            }
            setStoreAndSize(array, store.getArray(), size);
            return array;
        }

        @Specialization(guards = { "wasProvided(sizeObject)", "!isInteger(sizeObject)", "!isLong(sizeObject)", "wasProvided(value)" })
        public DynamicObject initializeSizeOther(VirtualFrame frame, DynamicObject array, Object sizeObject, Object value, NotProvided block) {
            int size = toInt(frame, sizeObject);
            return executeInitialize(frame, array, size, value, block);
        }

        // With block

        @Specialization(guards = "size >= 0")
        public Object initializeBlock(VirtualFrame frame, DynamicObject array, int size, Object unusedValue, DynamicObject block,
                @Cached("create()") ArrayBuilderNode arrayBuilder) {
            Object store = arrayBuilder.start(size);

            int n = 0;
            try {
                for (; n < size; n++) {
                    store = arrayBuilder.appendValue(store, n, yield(frame, block, n));
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, n);
                }
                setStoreAndSize(array, arrayBuilder.finish(store, n), n);
            }

            return array;
        }

        @Specialization(guards = "isRubyArray(copy)")
        public DynamicObject initializeFromArray(DynamicObject array, DynamicObject copy, NotProvided unusedValue, Object maybeBlock,
                @Cached("createReplaceNode()") ReplaceNode replaceNode) {
            replaceNode.executeReplace(array, copy);
            return array;
        }

        @Specialization(guards = { "!isInteger(object)", "!isLong(object)", "wasProvided(object)", "!isRubyArray(object)" })
        public DynamicObject initialize(VirtualFrame frame, DynamicObject array, Object object, NotProvided unusedValue, NotProvided block) {
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
                int size = toInt(frame, object);
                return executeInitialize(frame, array, size, NotProvided.INSTANCE, NotProvided.INSTANCE);
            }
        }

        public boolean respondToToAry(VirtualFrame frame, Object object) {
            if (respondToToAryNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                respondToToAryNode = insert(KernelNodesFactory.RespondToNodeFactory.create(null, null, null));
            }
            return respondToToAryNode.doesRespondToString(frame, object, create7BitString("to_ary", UTF8Encoding.INSTANCE), true);
        }

        protected Object callToAry(VirtualFrame frame, Object object) {
            if (toAryNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toAryNode = insert(DispatchHeadNodeFactory.createMethodCall(true));
            }
            return toAryNode.call(frame, object, "to_ary");
        }

        protected int toInt(VirtualFrame frame, Object value) {
            if (toIntNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toIntNode = insert(ToIntNode.create());
            }
            return toIntNode.doInt(frame, value);
        }

        protected ReplaceNode createReplaceNode() {
            return ReplaceNodeFactory.create(null, null);
        }

    }

    @CoreMethod(names = "initialize_copy", required = 1, raiseIfFrozenSelf = true)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "self"),
            @NodeChild(type = RubyNode.class, value = "from")
    })
    @ImportStatic(ArrayGuards.class)
    public abstract static class InitializeCopyNode extends CoreMethodNode {

        @CreateCast("from")
        public RubyNode coerceOtherToAry(RubyNode other) {
            return ToAryNodeGen.create(other);
        }

        @Specialization
        public DynamicObject initializeCopy(DynamicObject self, DynamicObject from,
                @Cached("createReplaceNode()") ReplaceNode replaceNode) {
            if (self == from) {
                return self;
            }
            replaceNode.executeReplace(self, from);
            return self;
        }

        protected ReplaceNode createReplaceNode() {
            return ReplaceNodeFactory.create(null, null);
        }

    }

    @CoreMethod(names = { "inject", "reduce" }, needsBlock = true, optional = 2)
    @ImportStatic(ArrayGuards.class)
    public abstract static class InjectNode extends YieldingCoreMethodNode {

        @Child private CallDispatchHeadNode dispatch = DispatchHeadNodeFactory.createMethodCall(MissingBehavior.CALL_METHOD_MISSING);

        // With block

        @Specialization(guards = { "isEmptyArray(array)", "wasProvided(initial)" })
        public Object injectEmptyArray(VirtualFrame frame, DynamicObject array, Object initial, NotProvided unused, DynamicObject block) {
            return initial;
        }

        @Specialization(guards = "isEmptyArray(array)")
        public Object injectEmptyArrayNoInitial(VirtualFrame frame, DynamicObject array, NotProvided initial, NotProvided unused, DynamicObject block) {
            return nil();
        }

        @Specialization(guards = { "strategy.matches(array)", "!isEmptyArray(array)", "wasProvided(initial)" }, limit = "ARRAY_STRATEGIES")
        public Object injectWithInitial(VirtualFrame frame, DynamicObject array, Object initial, NotProvided unused, DynamicObject block,
                @Cached("of(array)") ArrayStrategy strategy) {
            final ArrayMirror store = strategy.newMirror(array);
            return injectBlockHelper(frame, array, block, store, initial, 0);
        }

        @Specialization(guards = { "strategy.matches(array)", "!isEmptyArray(array)" }, limit = "ARRAY_STRATEGIES")
        public Object injectNoInitial(VirtualFrame frame, DynamicObject array, NotProvided initial, NotProvided unused, DynamicObject block,
                @Cached("of(array)") ArrayStrategy strategy) {
            final ArrayMirror store = strategy.newMirror(array);
            return injectBlockHelper(frame, array, block, store, store.get(0), 1);
        }

        public Object injectBlockHelper(VirtualFrame frame, DynamicObject array, DynamicObject block, ArrayMirror store, Object initial, int start) {
            Object accumulator = initial;
            int n = start;
            try {
                for (; n < getSize(array); n++) {
                    accumulator = yield(frame, block, accumulator, store.get(n));
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, n);
                }
            }

            return accumulator;
        }

        // With Symbol

        @Specialization(guards = { "isRubySymbol(symbol)", "isEmptyArray(array)", "wasProvided(initial)" })
        public Object injectSymbolEmptyArray(VirtualFrame frame, DynamicObject array, Object initial, DynamicObject symbol, NotProvided block) {
            return initial;
        }

        @Specialization(guards = { "isRubySymbol(symbol)", "isEmptyArray(array)" })
        public Object injectSymbolEmptyArrayNoInitial(VirtualFrame frame, DynamicObject array, DynamicObject symbol, NotProvided unused, NotProvided block) {
            return nil();
        }

        @Specialization(guards = { "isRubySymbol(symbol)", "strategy.matches(array)", "!isEmptyArray(array)", "wasProvided(initial)" }, limit = "ARRAY_STRATEGIES")
        public Object injectSymbolWithInitial(VirtualFrame frame, DynamicObject array, Object initial, DynamicObject symbol, NotProvided block,
                @Cached("of(array)") ArrayStrategy strategy) {
            final ArrayMirror store = strategy.newMirror(array);
            return injectSymbolHelper(frame, array, symbol, store, initial, 0);
        }

        @Specialization(guards = { "isRubySymbol(symbol)", "strategy.matches(array)", "!isEmptyArray(array)" }, limit = "ARRAY_STRATEGIES")
        public Object injectSymbolNoInitial(VirtualFrame frame, DynamicObject array, DynamicObject symbol, NotProvided unused, NotProvided block,
                @Cached("of(array)") ArrayStrategy strategy) {
            final ArrayMirror store = strategy.newMirror(array);
            return injectSymbolHelper(frame, array, symbol, store, store.get(0), 1);
        }

        public Object injectSymbolHelper(VirtualFrame frame, DynamicObject array, DynamicObject symbol, ArrayMirror store, Object initial, int start) {
            Object accumulator = initial;
            int n = start;

            try {
                for (; n < getSize(array); n++) {
                    accumulator = dispatch.call(frame, accumulator, symbol, store.get(n));
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
    public abstract static class MapNode extends YieldingCoreMethodNode {

        @Specialization(guards = "strategy.matches(array)", limit = "ARRAY_STRATEGIES")
        public Object map(VirtualFrame frame, DynamicObject array, DynamicObject block,
                @Cached("of(array)") ArrayStrategy strategy,
                @Cached("create()") ArrayBuilderNode arrayBuilder) {
            final ArrayMirror store = strategy.newMirror(array);
            final int size = strategy.getSize(array);
            Object mappedStore = arrayBuilder.start(size);

            int n = 0;
            try {
                for (; n < strategy.getSize(array); n++) {
                    final Object mappedValue = yield(frame, block, store.get(n));
                    mappedStore = arrayBuilder.appendValue(mappedStore, n, mappedValue);
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, n);
                }
            }

            return createArray(arrayBuilder.finish(mappedStore, size), size);
        }

    }

    @CoreMethod(names = { "map!", "collect!" }, needsBlock = true, enumeratorSize = "size", raiseIfFrozenSelf = true)
    @ImportStatic(ArrayGuards.class)
    public abstract static class MapInPlaceNode extends YieldingCoreMethodNode {

        @Child private ArrayWriteNormalizedNode writeNode;

        @Specialization(guards = "strategy.matches(array)", limit = "ARRAY_STRATEGIES")
        public Object map(VirtualFrame frame, DynamicObject array, DynamicObject block,
                @Cached("of(array)") ArrayStrategy strategy,
                @Cached("createWriteNode()") ArrayWriteNormalizedNode writeNode) {
            final ArrayMirror store = strategy.newMirror(array);

            int n = 0;
            try {
                for (; n < strategy.getSize(array); n++) {
                    writeNode.executeWrite(array, n, yield(frame, block, store.get(n)));
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, n);
                }
            }

            return array;
        }

        protected ArrayWriteNormalizedNode createWriteNode() {
            return ArrayWriteNormalizedNodeGen.create(null, null, null);
        }

    }

    @CoreMethod(names = "pack", required = 1, taintFrom = 1)
    @ImportStatic(StringCachingGuards.class)
    public abstract static class PackNode extends ArrayCoreMethodNode {

        @Child private RopeNodes.MakeLeafRopeNode makeLeafRopeNode;
        @Child private TaintNode taintNode;

        private final BranchProfile exceptionProfile = BranchProfile.create();
        private final ConditionProfile resizeProfile = ConditionProfile.createBinaryProfile();

        @Specialization(guards = {
                "isRubyString(format)",
                "ropesEqual(format, cachedFormat)"
        }, limit = "getCacheLimit()")
        public DynamicObject packCached(
                DynamicObject array,
                DynamicObject format,
                @Cached("privatizeRope(format)") Rope cachedFormat,
                @Cached("ropeLength(cachedFormat)") int cachedFormatLength,
                @Cached("create(compileFormat(format))") DirectCallNode callPackNode) {
            final BytesResult result;

            try {
                result = (BytesResult) callPackNode.call(
                        new Object[] { getStore(array), getSize(array) });
            } catch (FormatException e) {
                exceptionProfile.enter();
                throw FormatExceptionTranslator.translate(this, e);
            }

            return finishPack(cachedFormatLength, result);
        }

        @Specialization(replaces = "packCached", guards = "isRubyString(format)")
        public DynamicObject packUncached(
                DynamicObject array,
                DynamicObject format,
                @Cached("create()") IndirectCallNode callPackNode) {
            final BytesResult result;

            try {
                result = (BytesResult) callPackNode.call(compileFormat(format),
                        new Object[] { getStore(array), getSize(array) });
            } catch (FormatException e) {
                exceptionProfile.enter();
                throw FormatExceptionTranslator.translate(this, e);
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

            final DynamicObject string = createString(makeLeafRopeNode.executeMake(
                    bytes,
                    result.getEncoding().getEncodingForLength(formatLength),
                    result.getStringCodeRange(),
                    result.getStringLength()));

            if (result.isTainted()) {
                if (taintNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    taintNode = insert(TaintNode.create());
                }

                taintNode.executeTaint(string);
            }

            return string;
        }

        @Specialization(guards = {
                "!isRubyString(format)",
                "!isBoolean(format)",
                "!isInteger(format)",
                "!isLong(format)",
                "!isNil(format)"
        })
        public Object pack(
                VirtualFrame frame,
                DynamicObject array,
                Object format,
                @Cached("new()") SnippetNode snippetNode) {
            return snippetNode.execute(frame, "pack(format.to_str)", "format", format);
        }

        @TruffleBoundary
        protected CallTarget compileFormat(DynamicObject format) {
            return new PackCompiler(getContext(), this).compile(format.toString());
        }

        protected int getCacheLimit() {
            return getContext().getOptions().PACK_CACHE;
        }

    }

    @CoreMethod(names = "pop", raiseIfFrozenSelf = true, optional = 1, lowerFixnum = 1)
    public abstract static class PopNode extends ArrayCoreMethodNode {

        @Child private ToIntNode toIntNode;
        @Child private ArrayPopOneNode popOneNode;

        public abstract Object executePop(VirtualFrame frame, DynamicObject array, Object n);

        @Specialization
        public Object pop(DynamicObject array, NotProvided n) {
            if (popOneNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                popOneNode = insert(ArrayPopOneNodeGen.create(null));
            }

            return popOneNode.executePopOne(array);
        }

        @Specialization(guards = "n < 0")
        public Object popNNegative(VirtualFrame frame, DynamicObject array, int n) {
            throw new RaiseException(coreExceptions().argumentErrorNegativeArraySize(this));
        }

        @Specialization(guards = { "n >= 0", "isEmptyArray(array)" })
        public Object popEmpty(VirtualFrame frame, DynamicObject array, int n) {
            return createArray(null, 0);
        }

        @Specialization(guards = { "n == 0", "!isEmptyArray(array)" })
        public Object popZeroNotEmpty(DynamicObject array, int n) {
            return createArray(null, 0);
        }

        @Specialization(guards = { "n > 0", "!isEmptyArray(array)", "strategy.matches(array)" }, limit = "ARRAY_STRATEGIES")
        public Object popNotEmpty(DynamicObject array, int n,
                @Cached("of(array)") ArrayStrategy strategy,
                @Cached("createBinaryProfile()") ConditionProfile minProfile) {
            final int size = strategy.getSize(array);
            final int numPop = minProfile.profile(size < n) ? size : n;
            final ArrayMirror store = strategy.newMirror(array);

            // Extract values in a new array
            final ArrayMirror popped = store.extractRange(size - numPop, size);

            // Null out the popped values from the store
            final ArrayMirror filler = strategy.newArray(numPop);
            filler.copyTo(store, 0, size - numPop, numPop);
            setSize(array, size - numPop);

            return createArray(popped.getArray(), numPop);
        }

        @Specialization(guards = { "wasProvided(n)", "!isInteger(n)", "!isLong(n)" })
        public Object popNToInt(VirtualFrame frame, DynamicObject array, Object n) {
            return executePop(frame, array, toInt(frame, n));
        }

        private int toInt(VirtualFrame frame, Object indexObject) {
            if (toIntNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toIntNode = insert(ToIntNode.create());
            }
            return toIntNode.doInt(frame, indexObject);
        }

    }

    @CoreMethod(names = "<<", raiseIfFrozenSelf = true, required = 1)
    public abstract static class AppendNode extends ArrayCoreMethodNode {

        @Child private ArrayAppendOneNode appendOneNode = ArrayAppendOneNode.create();

        @Specialization
        public DynamicObject append(DynamicObject array, Object value) {
            return appendOneNode.executeAppendOne(array, value);
        }

    }

    @CoreMethod(names = { "push", "__append__" }, rest = true, optional = 1, raiseIfFrozenSelf = true)
    public abstract static class PushNode extends ArrayCoreMethodNode {

        @Child private ArrayAppendOneNode appendOneNode = ArrayAppendOneNode.create();

        @Specialization(guards = "rest.length == 0")
        public DynamicObject pushZero(DynamicObject array, NotProvided unusedValue, Object[] rest) {
            return array;
        }

        @Specialization(guards = { "rest.length == 0", "wasProvided(value)" })
        public DynamicObject pushOne(DynamicObject array, Object value, Object[] rest) {
            return appendOneNode.executeAppendOne(array, value);
        }

        @Specialization(guards = { "rest.length > 0", "wasProvided(value)" })
        public DynamicObject pushMany(VirtualFrame frame, DynamicObject array, Object value, Object[] rest) {
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
    public abstract static class RejectNode extends YieldingCoreMethodNode {

        @Specialization(guards = "strategy.matches(array)", limit = "ARRAY_STRATEGIES")
        public Object rejectOther(VirtualFrame frame, DynamicObject array, DynamicObject block,
                @Cached("of(array)") ArrayStrategy strategy,
                @Cached("create()") ArrayBuilderNode arrayBuilder) {
            final ArrayMirror store = strategy.newMirror(array);

            Object selectedStore = arrayBuilder.start(strategy.getSize(array));
            int selectedSize = 0;

            int n = 0;
            try {
                for (; n < strategy.getSize(array); n++) {
                    final Object value = store.get(n);

                    if (!yieldIsTruthy(frame, block, value)) {
                        selectedStore = arrayBuilder.appendValue(selectedStore, selectedSize, value);
                        selectedSize++;
                    }
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, n);
                }
            }

            return createArray(arrayBuilder.finish(selectedStore, selectedSize), selectedSize);
        }

    }

    @CoreMethod(names = "reject!", needsBlock = true, enumeratorSize = "size", raiseIfFrozenSelf = true)
    @ImportStatic(ArrayGuards.class)
    public abstract static class RejectInPlaceNode extends YieldingCoreMethodNode {

        @Specialization(guards = "strategy.matches(array)", limit = "ARRAY_STRATEGIES")
        public Object rejectInPlaceOther(VirtualFrame frame, DynamicObject array, DynamicObject block,
                @Cached("of(array)") ArrayStrategy strategy) {
            final ArrayMirror store = strategy.newMirror(array);

            int i = 0;
            int n = 0;
            try {
                for (; n < strategy.getSize(array); n++) {
                    final Object value = store.get(n);
                    if (yieldIsTruthy(frame, block, value)) {
                        continue;
                    }

                    if (i != n) {
                        store.set(i, store.get(n));
                    }

                    i++;
                }
            } finally {
                // Null out the elements behind the size
                final ArrayMirror filler = strategy.newArray(n - i);
                filler.copyTo(store, 0, i, n - i);
                setSize(array, i);

                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, n);
                }
            }

            if (i != n) {
                return array;
            } else {
                return nil();
            }
        }

    }

    @CoreMethod(names = "replace", required = 1, raiseIfFrozenSelf = true)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "array"),
            @NodeChild(type = RubyNode.class, value = "other")
    })
    @ImportStatic(ArrayGuards.class)
    public abstract static class ReplaceNode extends CoreMethodNode {

        public abstract DynamicObject executeReplace(DynamicObject array, DynamicObject other);

        @CreateCast("other")
        public RubyNode coerceOtherToAry(RubyNode index) {
            return ToAryNodeGen.create(index);
        }

        @Specialization(guards = {"arrayStrategy.matches(array)", "otherStrategy.matches(other)"}, limit = "ARRAY_STRATEGIES")
        public DynamicObject replace(DynamicObject array, DynamicObject other,
                        @Cached("of(array)") ArrayStrategy arrayStrategy,
                        @Cached("of(other)") ArrayStrategy otherStrategy) {
            final int size = getSize(other);
            final ArrayMirror copy = otherStrategy.newMirror(other).copyArrayAndMirror();
            arrayStrategy.setStoreAndSize(array, copy.getArray(), size);
            return array;
        }

    }

    @CoreMethod(names = "select", needsBlock = true, enumeratorSize = "size")
    @ImportStatic(ArrayGuards.class)
    public abstract static class SelectNode extends YieldingCoreMethodNode {

        @Specialization(guards = "strategy.matches(array)", limit = "ARRAY_STRATEGIES")
        public Object selectOther(VirtualFrame frame, DynamicObject array, DynamicObject block,
                @Cached("of(array)") ArrayStrategy strategy,
                @Cached("create()") ArrayBuilderNode arrayBuilder) {
            final ArrayMirror store = strategy.newMirror(array);

            Object selectedStore = arrayBuilder.start(strategy.getSize(array));
            int selectedSize = 0;

            int n = 0;
            try {
                for (; n < strategy.getSize(array); n++) {
                    final Object value = store.get(n);

                    if (yieldIsTruthy(frame, block, value)) {
                        selectedStore = arrayBuilder.appendValue(selectedStore, selectedSize, value);
                        selectedSize++;
                    }
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, n);
                }
            }

            return createArray(arrayBuilder.finish(selectedStore, selectedSize), selectedSize);
        }

    }

    @CoreMethod(names = "shift", raiseIfFrozenSelf = true, optional = 1, lowerFixnum = 1)
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "array"),
            @NodeChild(type = RubyNode.class, value = "n")
    })
    @ImportStatic(ArrayGuards.class)
    public abstract static class ShiftNode extends CoreMethodNode {

        @Child private ToIntNode toIntNode;

        public abstract Object executeShift(VirtualFrame frame, DynamicObject array, Object n);

        // No n, just shift 1 element and return it

        @Specialization(guards = "isEmptyArray(array)")
        public Object shiftEmpty(DynamicObject array, NotProvided n) {
            return nil();
        }

        @Specialization(guards = { "strategy.matches(array)", "!isEmptyArray(array)" }, limit = "ARRAY_STRATEGIES")
        public Object shiftOther(DynamicObject array, NotProvided n,
                @Cached("of(array)") ArrayStrategy strategy) {
            final ArrayMirror store = strategy.newMirror(array);
            final int size = strategy.getSize(array);
            final Object value = store.get(0);
            store.copyTo(store, 1, 0, size - 1);

            // Null out the element behind the size
            final ArrayMirror filler = strategy.newArray(1);
            filler.copyTo(store, 0, size - 1, 1);
            setSize(array, size - 1);

            return value;
        }

        // n given, shift the first n elements and return them as an Array

        @Specialization(guards = "n < 0")
        public Object shiftNegative(DynamicObject array, int n) {
            throw new RaiseException(coreExceptions().argumentErrorNegativeArraySize(this));
        }

        @Specialization(guards = "n == 0")
        public Object shiftZero(DynamicObject array, int n) {
            return createArray(null, 0);
        }

        @Specialization(guards = { "n > 0", "isEmptyArray(array)" })
        public Object shiftManyEmpty(DynamicObject array, int n) {
            return createArray(null, 0);
        }

        @Specialization(guards = { "n > 0", "strategy.matches(array)", "!isEmptyArray(array)" }, limit = "ARRAY_STRATEGIES")
        public Object shiftMany(DynamicObject array, int n,
                @Cached("of(array)") ArrayStrategy strategy,
                @Cached("createBinaryProfile()") ConditionProfile minProfile) {
            final int size = strategy.getSize(array);
            final int numShift = minProfile.profile(size < n) ? size : n;
            final ArrayMirror store = strategy.newMirror(array);

            // Extract values in a new array
            final ArrayMirror result = store.extractRange(0, numShift);

            // Move elements
            store.copyTo(store, numShift, 0, size - numShift);

            // Null out the element behind the size
            final ArrayMirror filler = strategy.newArray(numShift);
            filler.copyTo(store, 0, size - numShift, numShift);
            setSize(array, size - numShift);

            return createArray(result.getArray(), numShift);
        }

        @Specialization(guards = { "wasProvided(n)", "!isInteger(n)", "!isLong(n)" })
        public Object shiftNToInt(VirtualFrame frame, DynamicObject array, Object n) {
            return executeShift(frame, array, toInt(frame, n));
        }

        private int toInt(VirtualFrame frame, Object indexObject) {
            if (toIntNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toIntNode = insert(ToIntNode.create());
            }
            return toIntNode.doInt(frame, indexObject);
        }

    }

    @CoreMethod(names = { "size", "length" })
    public abstract static class SizeNode extends ArrayCoreMethodNode {

        @Specialization
        public int size(DynamicObject array,
                        @Cached("createIdentityProfile()") IntValueProfile profile) {
            return profile.profile(Layouts.ARRAY.getSize(array));
        }

    }

    @CoreMethod(names = "sort", needsBlock = true)
    public abstract static class SortNode extends ArrayCoreMethodNode {

        @Child private CallDispatchHeadNode compareDispatchNode = DispatchHeadNodeFactory.createMethodCall();
        @Child private YieldNode yieldNode = new YieldNode();

        private final BranchProfile errorProfile = BranchProfile.create();

        @Specialization(guards = "isEmptyArray(array)")
        public DynamicObject sortNull(DynamicObject array, Object unusedBlock) {
            return createArray(null, 0);
        }

        @ExplodeLoop
        @Specialization(guards = { "!isEmptyArray(array)", "isSmall(array)", "strategy.matches(array)" }, limit = "ARRAY_STRATEGIES")
        public DynamicObject sortVeryShort(VirtualFrame frame, DynamicObject array, NotProvided block,
                @Cached("of(array)") ArrayStrategy strategy) {
            final ArrayMirror originalStore = strategy.newMirror(array);
            final ArrayMirror store = strategy.newArray(getContext().getOptions().ARRAY_SMALL);
            final int size = strategy.getSize(array);

            // Copy with a exploded loop for PE

            for (int i = 0; i < getContext().getOptions().ARRAY_SMALL; i++) {
                if (i < size) {
                    store.set(i, originalStore.get(i));
                }
            }

            // Selection sort - written very carefully to allow PE

            for (int i = 0; i < getContext().getOptions().ARRAY_SMALL; i++) {
                if (i < size) {
                    for (int j = i + 1; j < getContext().getOptions().ARRAY_SMALL; j++) {
                        if (j < size) {
                            final Object a = store.get(i);
                            final Object b = store.get(j);
                            if (castSortValue(compareDispatchNode.call(frame, b, "<=>", a)) < 0) {
                                store.set(j, a);
                                store.set(i, b);
                            }
                        }
                    }
                }
            }

            return createArray(store.getArray(), size);
        }

        @Specialization(guards = { "!isEmptyArray(array)", "!isSmall(array)" })
        public Object sortLargeArray(VirtualFrame frame, DynamicObject array, NotProvided block,
                @Cached("new()") SnippetNode snippetNode) {
            return snippetNode.execute(frame,
                    "sorted = dup; Truffle.privately { sorted.mergesort! }; sorted");
        }

        @Specialization(guards = { "!isEmptyArray(array)", "isObjectArray(array)" })
        public Object sortObjectWithBlock(DynamicObject array, DynamicObject block) {
            final int size = getSize(array);
            Object[] copy = ((Object[]) getStore(array)).clone();
            doSort(copy, size, block);
            return createArray(copy, size);
        }

        @TruffleBoundary
        private void doSort(Object[] copy, int size, DynamicObject block) {
            Arrays.sort(copy, 0, size, (a, b) -> castSortValue(ProcOperations.rootCall(block, a, b)));
        }

        @Specialization(guards = { "!isEmptyArray(array)", "!isObjectArray(array)" })
        public Object sortWithBlock(VirtualFrame frame, DynamicObject array, DynamicObject block,
                @Cached("new()") SnippetNode snippet) {
            return snippet.execute(frame,
                    "sorted = dup; Truffle.privately { sorted.mergesort_block!(block) }; sorted",
                    "block", block);
        }

        private int castSortValue(Object value) {
            if (value instanceof Integer) {
                return (int) value;
            }

            errorProfile.enter();
            // TODO CS 14-Mar-15 - what's the error message here?
            throw new RaiseException(coreExceptions().argumentError("expecting a Fixnum to sort", this));
        }

        protected boolean isSmall(DynamicObject array) {
            return getSize(array) <= getContext().getOptions().ARRAY_SMALL;
        }

    }

    @Primitive(name = "array_zip")
    @ImportStatic(ArrayGuards.class)
    public abstract static class ZipNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = {
                "isRubyArray(other)", "aStrategy.matches(array)", "bStrategy.matches(other)"
        }, limit = "ARRAY_STRATEGIES")
        public DynamicObject zipToPairs(DynamicObject array, DynamicObject other,
                @Cached("of(array)") ArrayStrategy aStrategy,
                @Cached("of(other)") ArrayStrategy bStrategy,
                @Cached("aStrategy.generalizeNew(bStrategy)") ArrayStrategy generalized,
                @Cached("createBinaryProfile()") ConditionProfile bNotSmallerProfile) {
            final ArrayMirror a = aStrategy.newMirror(array);
            final ArrayMirror b = bStrategy.newMirror(other);

            final int bSize = bStrategy.getSize(other);
            final int zippedLength = aStrategy.getSize(array);
            final Object[] zipped = new Object[zippedLength];

            for (int n = 0; n < zippedLength; n++) {
                if (bNotSmallerProfile.profile(n < bSize)) {
                    final ArrayMirror pair = generalized.newArray(2);
                    pair.set(0, a.get(n));
                    pair.set(1, b.get(n));
                    zipped[n] = createArray(pair.getArray(), 2);
                } else {
                    zipped[n] = createArray(new Object[] { a.get(n), nil() }, 2);
                }
            }

            return createArray(zipped, zippedLength);
        }

    }

}
