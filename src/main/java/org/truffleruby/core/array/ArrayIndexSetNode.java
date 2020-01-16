/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.control.RaiseException;

import static org.truffleruby.core.array.ArrayHelpers.getSize;
import static org.truffleruby.core.array.ArrayHelpers.setSize;

@ReportPolymorphism
public abstract class ArrayIndexSetNode extends ArrayCoreMethodNode {

    @Child private ArrayReadNormalizedNode readNode;
    @Child private ArrayWriteNormalizedNode writeNode;
    @Child private ArrayReadSliceNormalizedNode readSliceNode;

    private final BranchProfile negativeIndexProfile = BranchProfile.create();
    private final BranchProfile negativeLengthProfile = BranchProfile.create();

    public abstract Object executeSet(DynamicObject array, Object index, Object length, Object value);

    // array[index] = object

    @Specialization
    @ReportPolymorphism.Exclude
    protected Object set(DynamicObject array, int index, Object value, NotProvided unused,
            @Cached("createBinaryProfile()") ConditionProfile negativeIndexProfile) {
        final int normalizedIndex = ArrayOperations.normalizeIndex(getSize(array), index, negativeIndexProfile);
        checkIndex(array, index, normalizedIndex);
        return write(array, normalizedIndex, value);
    }

    // array[index] = object with non-int index

    @Specialization(guards = { "!isInteger(indexObject)", "!isRubyRange(indexObject)" })
    @ReportPolymorphism.Exclude
    protected Object set(DynamicObject array, Object indexObject, Object value, NotProvided unused) {
        return fallback(array, indexObject, value, unused);
    }

    // array[start, length] = object

    @Specialization(guards = { "!isRubyArray(value)", "wasProvided(value)" })
    @ReportPolymorphism.Exclude
    protected Object setObject(DynamicObject array, int start, int length, Object value) {
        return fallback(array, start, length, value);
    }

    // array[start, length] = other_array, with length == other_array.size

    @Specialization(guards = { "isRubyArray(replacement)", "length == getArraySize(replacement)" })
    @ReportPolymorphism.Exclude
    protected Object setOtherArraySameLength(DynamicObject array, int start, int length, DynamicObject replacement,
            @Cached("createBinaryProfile()") ConditionProfile negativeIndexProfile) {
        final int normalizedIndex = ArrayOperations.normalizeIndex(getSize(array), start, negativeIndexProfile);
        checkIndex(array, start, normalizedIndex);

        for (int i = 0; i < length; i++) {
            write(array, normalizedIndex + i, read(replacement, i));
        }
        return replacement;
    }

    // array[start, length] = other_array, with length != other_array.size

    @Specialization(
            guards = {
                    "isRubyArray(replacement)",
                    "length != getArraySize(replacement)",
                    "strategy.matches(array)" },
            limit = "STORAGE_STRATEGIES")
    protected Object setOtherArray(DynamicObject array, int rawStart, int length, DynamicObject replacement,
            @Cached("of(array)") ArrayStrategy strategy,
            @Cached("strategy.generalizeForMutation()") ArrayStrategy mutableStrategy,
            @Cached("mutableStrategy.getNode()") ArrayOperationNodes.ArrayGetNode getNode,
            @Cached("mutableStrategy.setNode()") ArrayOperationNodes.ArraySetNode setNode,
            @Cached ArrayEnsureCapacityNode ensureCapacityNode,
            @Cached("createBinaryProfile()") ConditionProfile negativeIndexProfile,
            @Cached("createBinaryProfile()") ConditionProfile recursiveProfile,
            @Cached("createBinaryProfile()") ConditionProfile emptyProfile,
            @Cached("createBinaryProfile()") ConditionProfile tailProfile,
            @Cached("createBinaryProfile()") ConditionProfile moveElementsProfile,
            @Cached("createBinaryProfile()") ConditionProfile moveLeftProfile,
            @Cached("createBinaryProfile()") ConditionProfile emptyReplacementProfile,
            @Cached("createBinaryProfile()") ConditionProfile growProfile,
            @Cached("createBinaryProfile()") ConditionProfile shrinkProfile) {
        checkLengthPositive(length);
        final int start = ArrayOperations.normalizeIndex(getSize(array), rawStart, negativeIndexProfile);
        checkIndex(array, rawStart, start);

        final int arraySize = strategy.getSize(array);
        final int replacementSize = getSize(replacement);

        if (recursiveProfile.profile(array == replacement)) {
            replacement = readSlice(array, 0, arraySize); // Make a copy
        }

        if (moveElementsProfile.profile(start + length <= arraySize)) {
            // ary[start, length] = replacement such that start+length < array.size.
            // Replace some elements in the array with some others.
            // We handle start+length == arraySize here too since we want simpler code for Array#insert

            if (!emptyProfile.profile(arraySize == 0)) {
                final int newSize = arraySize - length + replacementSize;
                ensureCapacityNode.executeEnsureCapacity(array, newSize); // needs a non-empty strategy
                setSize(array, newSize);

                // Move tail
                final int tailSize = arraySize - (start + length);
                if (tailProfile.profile(tailSize > 0)) {
                    final Object store = Layouts.ARRAY.getStore(array);

                    if (moveLeftProfile.profile(replacementSize < length)) {
                        // Moving elements left
                        for (int i = 0; i < tailSize; i++) {
                            setNode.execute(
                                    store,
                                    start + replacementSize + i,
                                    getNode.execute(store, start + length + i));
                        }
                    } else {
                        // Moving elements right
                        for (int i = tailSize - 1; i >= 0; i--) {
                            setNode.execute(
                                    store,
                                    start + replacementSize + i,
                                    getNode.execute(store, start + length + i));
                        }
                    }
                }
            }

            // Write replacement
            for (int i = 0; i < replacementSize; i++) {
                write(array, start + i, read(replacement, i));
            }
        } else {
            assert start + length > arraySize;
            // ary[start, length] = replacement such that start+length >= array.size.
            // Grow the array to be start+repl.size long.
            // We can just overwrite elements from start.
            final int newSize = start + replacementSize;

            // Write replacement
            if (emptyReplacementProfile.profile(replacementSize == 0)) {
                // If no tail and the replacement is empty, the array will grow.
                // We need to append nil from index arraySize to index (start - 1).
                // a = [1,2,3]; a [5,1] = [] # => a == [1,2,3,nil,nil]
                if (growProfile.profile(start > arraySize)) {
                    write(array, newSize - 1, nil());
                }
            } else {
                // Write last element first to grow only once
                write(array, newSize - 1, read(replacement, replacementSize - 1));

                for (int i = 0; i < replacementSize - 1; i++) {
                    write(array, start + i, read(replacement, i));
                }
            }

            if (shrinkProfile.profile(newSize < arraySize)) {
                setSize(array, newSize);
            }
        }

        return replacement;
    }

    // array[start, length] = object_or_array with non-int start or length

    @Specialization(guards = { "!isInteger(startObject) || !isInteger(lengthObject)", "wasProvided(value)" })
    protected Object setStartLengthNotInt(DynamicObject array, Object startObject, Object lengthObject,
            Object value) {
        return fallback(array, startObject, lengthObject, value);
    }

    // array[start..end] = array

    @Specialization(guards = { "isIntRange(range)", "isRubyArray(value)" })
    protected Object setRange(DynamicObject array, DynamicObject range, Object value, NotProvided unused,
            @Cached("createBinaryProfile()") ConditionProfile negativeBeginProfile,
            @Cached("createBinaryProfile()") ConditionProfile negativeEndProfile,
            @Cached BranchProfile errorProfile) {
        final int size = getSize(array);
        final int begin = Layouts.INT_RANGE.getBegin(range);
        final int start = ArrayOperations.normalizeIndex(size, begin, negativeBeginProfile);
        if (start < 0) {
            errorProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().rangeError(range, this));
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

    // array[start..end] = object (not array)

    @Specialization(guards = { "isIntRange(range)", "!isRubyArray(value)" })
    protected Object setRangeWithNonArray(DynamicObject array, DynamicObject range, Object value,
            NotProvided unused) {
        // the fallback will recurse after converting the object to an array
        return fallback(array, range, value, unused);
    }

    // array[start..end] = object_or_array (non-int range)

    @Specialization(guards = { "!isIntRange(range)", "isRubyRange(range)" })
    protected Object setOtherRange(DynamicObject array, DynamicObject range, Object value, NotProvided unused) {
        // the fallback will recurse after converting the range to an int range
        return fallback(array, range, value, unused);
    }

    // Helpers

    private void checkIndex(DynamicObject array, int index, int normalizedIndex) {
        if (normalizedIndex < 0) {
            negativeIndexProfile.enter();
            throw new RaiseException(
                    getContext(),
                    coreExceptions().indexTooSmallError("array", index, getSize(array), this));
        }
    }

    private void checkLengthPositive(int length) {
        if (length < 0) {
            negativeLengthProfile.enter();
            throw new RaiseException(getContext(), coreExceptions().negativeLengthError(length, this));
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
            writeNode = insert(ArrayWriteNormalizedNodeGen.create());
        }
        return writeNode.executeWrite(array, index, value);
    }

    private DynamicObject readSlice(DynamicObject array, int start, int length) {
        if (readSliceNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            readSliceNode = insert(ArrayReadSliceNormalizedNodeGen.create());
        }
        return readSliceNode.executeReadSlice(array, start, length);
    }

    protected Object fallback(DynamicObject array, Object index, Object length, Object value) {
        throw new AbstractMethodError();
    }

}
