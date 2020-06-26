/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.range;

import com.oracle.truffle.api.profiles.BranchProfile;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.array.ArrayBuilderNode;
import org.truffleruby.core.array.ArrayBuilderNode.BuilderState;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.cast.BooleanCastWithDefaultNodeGen;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.yield.YieldNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreModule(value = "Range", isClass = true)
public abstract class RangeNodes {

    @Primitive(name = "range_integer_map")
    public abstract static class IntegerMapNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isIntRange(range)")
        protected DynamicObject map(DynamicObject range, DynamicObject block,
                @Cached ArrayBuilderNode arrayBuilder,
                @Cached YieldNode yieldNode,
                @Cached ConditionProfile noopProfile) {
            final int begin = Layouts.INT_RANGE.getBegin(range);
            final int end = Layouts.INT_RANGE.getEnd(range);
            final boolean excludedEnd = Layouts.INT_RANGE.getExcludedEnd(range);
            int exclusiveEnd = excludedEnd ? end : end + 1;
            if (noopProfile.profile(begin >= exclusiveEnd)) {
                return ArrayHelpers.createEmptyArray(getContext());
            }

            final int length = exclusiveEnd - begin;
            BuilderState state = arrayBuilder.start(length);
            int count = 0;

            try {
                for (int n = 0; n < length; n++) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    arrayBuilder.appendValue(state, n, yieldNode.executeDispatch(block, begin + n));
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, count);
                }
            }

            return createArray(arrayBuilder.finish(state, length), length);
        }

        @Fallback
        protected Object mapFallback(Object range, Object block) {
            return FAILURE;
        }

    }

    @CoreMethod(names = "each", needsBlock = true, enumeratorSize = "size")
    public abstract static class EachNode extends YieldingCoreMethodNode {

        @Child private CallDispatchHeadNode eachInternalCall;

        @Specialization(guards = "isIntRange(range)")
        protected Object eachInt(DynamicObject range, DynamicObject block) {
            int result;
            if (Layouts.INT_RANGE.getExcludedEnd(range)) {
                result = Layouts.INT_RANGE.getEnd(range);
            } else {
                result = Layouts.INT_RANGE.getEnd(range) + 1;
            }
            final int exclusiveEnd = result;

            int count = 0;

            try {
                for (int n = Layouts.INT_RANGE.getBegin(range); n < exclusiveEnd; n++) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    yield(block, n);
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, count);
                }
            }

            return range;
        }

        @Specialization(guards = "isLongRange(range)")
        protected Object eachLong(DynamicObject range, DynamicObject block) {
            long result;
            if (Layouts.LONG_RANGE.getExcludedEnd(range)) {
                result = Layouts.LONG_RANGE.getEnd(range);
            } else {
                result = Layouts.LONG_RANGE.getEnd(range) + 1;
            }
            final long exclusiveEnd = result;

            int count = 0;

            try {
                for (long n = Layouts.LONG_RANGE.getBegin(range); n < exclusiveEnd; n++) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    yield(block, n);
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, count);
                }
            }

            return range;
        }

        private Object eachInternal(VirtualFrame frame, DynamicObject range, DynamicObject block) {
            if (eachInternalCall == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                eachInternalCall = insert(CallDispatchHeadNode.createPrivate());
            }

            return eachInternalCall.callWithBlock(range, "each_internal", block);
        }

        @Specialization(guards = "isLongRange(range)")
        protected Object eachObject(VirtualFrame frame, DynamicObject range, NotProvided block) {
            return eachInternal(frame, range, null);
        }

        @Specialization(guards = "isObjectRange(range)")
        protected Object each(VirtualFrame frame, DynamicObject range, NotProvided block) {
            return eachInternal(frame, range, null);
        }

        @Specialization(guards = "isObjectRange(range)")
        protected Object each(VirtualFrame frame, DynamicObject range, DynamicObject block) {
            return eachInternal(frame, range, block);
        }

    }

    @CoreMethod(names = "exclude_end?")
    public abstract static class ExcludeEndNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isIntRange(range)")
        protected boolean excludeEndInt(DynamicObject range) {
            return Layouts.INT_RANGE.getExcludedEnd(range);
        }

        @Specialization(guards = "isLongRange(range)")
        protected boolean excludeEndLong(DynamicObject range) {
            return Layouts.LONG_RANGE.getExcludedEnd(range);
        }

        @Specialization(guards = "isObjectRange(range)")
        protected boolean excludeEndObject(DynamicObject range) {
            return Layouts.OBJECT_RANGE.getExcludedEnd(range);
        }

    }

    @CoreMethod(names = "begin")
    public abstract static class BeginNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isIntRange(range)")
        protected int eachInt(DynamicObject range) {
            return Layouts.INT_RANGE.getBegin(range);
        }

        @Specialization(guards = "isLongRange(range)")
        protected long eachLong(DynamicObject range) {
            return Layouts.LONG_RANGE.getBegin(range);
        }

        @Specialization(guards = "isObjectRange(range)")
        protected Object eachObject(DynamicObject range) {
            return Layouts.OBJECT_RANGE.getBegin(range);
        }

    }

    @CoreMethod(names = { "dup", "clone" })
    public abstract static class DupNode extends UnaryCoreMethodNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization(guards = "isIntRange(range)")
        protected DynamicObject dupIntRange(DynamicObject range) {
            return Layouts.INT_RANGE.createIntRange(
                    coreLibrary().intRangeFactory,
                    Layouts.INT_RANGE.getExcludedEnd(range),
                    Layouts.INT_RANGE.getBegin(range),
                    Layouts.INT_RANGE.getEnd(range));
        }

        @Specialization(guards = "isLongRange(range)")
        protected DynamicObject dupLongRange(DynamicObject range) {
            return Layouts.LONG_RANGE.createLongRange(
                    coreLibrary().intRangeFactory,
                    Layouts.LONG_RANGE.getExcludedEnd(range),
                    Layouts.LONG_RANGE.getBegin(range),
                    Layouts.LONG_RANGE.getEnd(range));
        }

        @Specialization(guards = "isObjectRange(range)")
        protected DynamicObject dup(DynamicObject range) {
            DynamicObject copy = allocateObjectNode.allocate(
                    Layouts.BASIC_OBJECT.getLogicalClass(range),
                    Layouts.OBJECT_RANGE.getExcludedEnd(range),
                    Layouts.OBJECT_RANGE.getBegin(range),
                    Layouts.OBJECT_RANGE.getEnd(range));
            return copy;
        }

    }

    @CoreMethod(names = "end")
    public abstract static class EndNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isIntRange(range)")
        protected int lastInt(DynamicObject range) {
            return Layouts.INT_RANGE.getEnd(range);
        }

        @Specialization(guards = "isLongRange(range)")
        protected long lastLong(DynamicObject range) {
            return Layouts.LONG_RANGE.getEnd(range);
        }

        @Specialization(guards = "isObjectRange(range)")
        protected Object lastObject(DynamicObject range) {
            return Layouts.OBJECT_RANGE.getEnd(range);
        }

    }

    @CoreMethod(names = "step", needsBlock = true, optional = 1, lowerFixnum = 1)
    public abstract static class StepNode extends YieldingCoreMethodNode {

        @Child private CallDispatchHeadNode stepInternalCall;

        @Specialization(guards = { "isIntRange(range)", "step > 0" })
        protected Object stepInt(DynamicObject range, int step, DynamicObject block) {
            int count = 0;

            try {
                int result;
                if (Layouts.INT_RANGE.getExcludedEnd(range)) {
                    result = Layouts.INT_RANGE.getEnd(range);
                } else {
                    result = Layouts.INT_RANGE.getEnd(range) + 1;
                }
                for (int n = Layouts.INT_RANGE.getBegin(range); n < result; n += step) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    yield(block, n);
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, count);
                }
            }

            return range;
        }

        @Specialization(guards = { "isLongRange(range)", "step > 0" })
        protected Object stepLong(DynamicObject range, int step, DynamicObject block) {
            int count = 0;

            try {
                long result;
                if (Layouts.LONG_RANGE.getExcludedEnd(range)) {
                    result = Layouts.LONG_RANGE.getEnd(range);
                } else {
                    result = Layouts.LONG_RANGE.getEnd(range) + 1;
                }
                for (long n = Layouts.LONG_RANGE.getBegin(range); n < result; n += step) {
                    if (CompilerDirectives.inInterpreter()) {
                        count++;
                    }

                    yield(block, n);
                }
            } finally {
                if (CompilerDirectives.inInterpreter()) {
                    LoopNode.reportLoopCount(this, count);
                }
            }

            return range;
        }

        @Fallback
        protected Object stepFallback(VirtualFrame frame, Object range, Object step, Object block) {
            if (stepInternalCall == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                stepInternalCall = insert(CallDispatchHeadNode.createPrivate());
            }

            if (step instanceof NotProvided) {
                step = 1;
            }

            final DynamicObject blockProc;
            if (RubyGuards.wasProvided(block)) {
                blockProc = (DynamicObject) block;
            } else {
                blockProc = null;
            }

            return stepInternalCall.callWithBlock(range, "step_internal", blockProc, step);
        }

    }

    @CoreMethod(names = "to_a")
    public abstract static class ToANode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode toAInternalCall;

        @Specialization(guards = "isIntRange(range)")
        protected DynamicObject toA(DynamicObject range) {
            final int begin = Layouts.INT_RANGE.getBegin(range);
            int result;
            if (Layouts.INT_RANGE.getExcludedEnd(range)) {
                result = Layouts.INT_RANGE.getEnd(range);
            } else {
                result = Layouts.INT_RANGE.getEnd(range) + 1;
            }
            final int length = result - begin;

            if (length < 0) {
                return ArrayHelpers.createEmptyArray(getContext());
            } else {
                final int[] values = new int[length];

                for (int n = 0; n < length; n++) {
                    values[n] = begin + n;
                }

                return createArray(values);
            }
        }

        @Specialization(guards = { "isObjectRange(range)", "!isEndlessRange(range)" })
        protected Object boundedToA(VirtualFrame frame, DynamicObject range) {
            if (toAInternalCall == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toAInternalCall = insert(CallDispatchHeadNode.createPrivate());
            }

            return toAInternalCall.call(range, "to_a_internal");
        }

        @Specialization(guards = { "isObjectRange(range)", "isEndlessRange(range)" })
        protected Object endlessToA(VirtualFrame frame, DynamicObject range) {
            throw new RaiseException(getContext(), coreExceptions().rangeError(
                    "cannot convert endless range to an array",
                    this));
        }

    }

    /** Returns a conversion of the range into an int range, with regard to the supplied array (the array is necessary
     * to handle endless ranges). */
    @Primitive(name = "range_to_int_range")
    public abstract static class ToIntRangeNode extends PrimitiveArrayArgumentsNode {

        @Child private ToIntNode toIntNode;

        @Specialization(guards = "isIntRange(range)")
        protected DynamicObject intRange(DynamicObject range, DynamicObject array) {
            return range;
        }

        @Specialization(guards = "isLongRange(range)")
        protected DynamicObject longRange(DynamicObject range, DynamicObject array) {
            int begin = toInt(Layouts.LONG_RANGE.getBegin(range));
            int end = toInt(Layouts.LONG_RANGE.getEnd(range));
            boolean excludedEnd = Layouts.LONG_RANGE.getExcludedEnd(range);
            return Layouts.INT_RANGE.createIntRange(coreLibrary().intRangeFactory, excludedEnd, begin, end);
        }

        @Specialization(guards = { "isObjectRange(range)", "!isEndlessRange(range)" })
        protected DynamicObject boundedObjectRange(DynamicObject range, DynamicObject array) {
            int begin = toInt(Layouts.OBJECT_RANGE.getBegin(range));
            int end = toInt(Layouts.OBJECT_RANGE.getEnd(range));
            boolean excludedEnd = Layouts.OBJECT_RANGE.getExcludedEnd(range);
            return Layouts.INT_RANGE.createIntRange(coreLibrary().intRangeFactory, excludedEnd, begin, end);
        }

        @Specialization(guards = { "isObjectRange(range)", "isEndlessRange(range)" })
        protected DynamicObject endlessObjectRange(DynamicObject range, DynamicObject array) {
            int begin = toInt(Layouts.OBJECT_RANGE.getBegin(range));
            int end = Layouts.ARRAY.getSize(array);
            boolean excludedEnd = true;
            return Layouts.INT_RANGE.createIntRange(coreLibrary().intRangeFactory, excludedEnd, begin, end);
        }

        private int toInt(Object indexObject) {
            if (toIntNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toIntNode = insert(ToIntNode.create());
            }
            return toIntNode.execute(indexObject);
        }

    }

    @Primitive(name = "range_initialize")
    public abstract static class InitializeNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isObjectRange(range)")
        protected boolean setExcludeEnd(DynamicObject range, Object begin, Object end, boolean excludeEnd) {
            Layouts.OBJECT_RANGE.setBegin(range, begin);
            Layouts.OBJECT_RANGE.setEnd(range, end);
            Layouts.OBJECT_RANGE.setExcludedEnd(range, excludeEnd);
            return excludeEnd;
        }

    }

    @CoreMethod(names = "new", constructor = true, required = 2, optional = 1)
    @NodeChild(value = "rubyClass", type = RubyNode.class)
    @NodeChild(value = "begin", type = RubyNode.class)
    @NodeChild(value = "end", type = RubyNode.class)
    @NodeChild(value = "excludeEnd", type = RubyNode.class)
    public abstract static class NewNode extends CoreMethodNode {

        @Child private CallDispatchHeadNode cmpNode;
        @Child private AllocateObjectNode allocateNode;

        @CreateCast("excludeEnd")
        protected RubyNode coerceToBoolean(RubyNode excludeEnd) {
            return BooleanCastWithDefaultNodeGen.create(false, excludeEnd);
        }

        @Specialization(guards = "rubyClass == getRangeClass()")
        protected DynamicObject intRange(DynamicObject rubyClass, int begin, int end, boolean excludeEnd) {
            return Layouts.INT_RANGE.createIntRange(coreLibrary().intRangeFactory, excludeEnd, begin, end);
        }

        @Specialization(guards = { "rubyClass == getRangeClass()", "fitsIntoInteger(begin)", "fitsIntoInteger(end)" })
        protected DynamicObject longFittingIntRange(DynamicObject rubyClass, long begin, long end, boolean excludeEnd) {
            return Layouts.INT_RANGE.createIntRange(coreLibrary().intRangeFactory, excludeEnd, (int) begin, (int) end);
        }

        @Specialization(guards = { "rubyClass == getRangeClass()", "!fitsIntoInteger(begin) || !fitsIntoInteger(end)" })
        protected DynamicObject longRange(DynamicObject rubyClass, long begin, long end, boolean excludeEnd) {
            return Layouts.LONG_RANGE.createLongRange(coreLibrary().longRangeFactory, excludeEnd, begin, end);
        }

        @Specialization(guards = { "rubyClass != getRangeClass() || (!isIntOrLong(begin) || !isIntOrLong(end))" })
        protected Object objectRange(
                VirtualFrame frame,
                DynamicObject rubyClass,
                Object begin,
                Object end,
                boolean excludeEnd) {
            if (cmpNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                cmpNode = insert(CallDispatchHeadNode.createPrivate());
            }
            if (allocateNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                allocateNode = insert(AllocateObjectNode.create());
            }

            if (cmpNode.call(begin, "<=>", end) == nil && end != nil) {
                throw new RaiseException(getContext(), coreExceptions().argumentError("bad value for range", this));
            }

            return allocateNode.allocate(rubyClass, excludeEnd, begin, end);
        }

        protected DynamicObject getRangeClass() {
            return coreLibrary().rangeClass;
        }

        protected boolean fitsIntoInteger(long value) {
            return CoreLibrary.fitsIntoInteger(value);
        }

        protected boolean isIntOrLong(Object value) {
            return RubyGuards.isInteger(value) || RubyGuards.isLong(value);
        }

    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass) {
            return allocateNode.allocate(rubyClass, false, nil, nil);
        }

    }

    /** Returns an array containing normalized int range parameters {@code [start, length]}, such that both are 32-bits
     * java ints (if conversion is impossible, an error is raised). The method attempts to make the value positive, by
     * adding {@code size} to them if they are negative. They may still be negative after the operation however, as
     * different core Ruby methods have different way of handling negative out-of-bound normalized values.
     * <p>
     * The length will NOT be clamped to size!
     * <p>
     * {@code size} is assumed to be normalized: fitting in an int, and positive. */
    @Primitive(name = "range_normalized_start_length", lowerFixnum = 1)
    @NodeChild(value = "range", type = RubyNode.class)
    @NodeChild(value = "size", type = RubyNode.class)
    public abstract static class NormalizedStartLengthPrimitiveNode extends PrimitiveNode {

        @Child NormalizedStartLengthNode startLengthNode = NormalizedStartLengthNode.create();

        @Specialization
        protected DynamicObject normalize(DynamicObject range, int size) {
            return ArrayHelpers.createArray(getContext(), startLengthNode.execute(range, size));
        }
    }

    /** @see NormalizedStartLengthPrimitiveNode */
    public abstract static class NormalizedStartLengthNode extends RubyContextNode {

        public static NormalizedStartLengthNode create() {
            return RangeNodesFactory.NormalizedStartLengthNodeGen.create();
        }

        public abstract int[] execute(DynamicObject range, int size);

        private final BranchProfile overflow = BranchProfile.create();
        private final ConditionProfile notExcluded = ConditionProfile.create();
        private final ConditionProfile negativeBegin = ConditionProfile.create();
        private final ConditionProfile negativeEnd = ConditionProfile.create();

        @Specialization(guards = "isIntRange(range)")
        protected int[] normalizeIntRange(DynamicObject range, int size) {
            return normalize(
                    Layouts.INT_RANGE.getBegin(range),
                    Layouts.INT_RANGE.getEnd(range),
                    Layouts.INT_RANGE.getExcludedEnd(range),
                    size);
        }

        @Specialization(guards = "isLongRange(range)")
        protected int[] normalizeLongRange(DynamicObject range, int size,
                @Cached ToIntNode toInt) {
            return normalize(
                    toInt.execute(Layouts.LONG_RANGE.getBegin(range)),
                    toInt.execute(Layouts.LONG_RANGE.getEnd(range)),
                    Layouts.LONG_RANGE.getExcludedEnd(range),
                    size);
        }

        @Specialization(guards = "isEndlessObjectRange(range)")
        protected int[] normalizeEndlessRange(DynamicObject range, int size,
                @Cached ToIntNode toInt) {
            int begin = toInt.execute(Layouts.OBJECT_RANGE.getBegin(range));
            return new int[]{ begin >= 0 ? begin : begin + size, size - begin };
        }

        @Specialization(guards = "isBoundedObjectRange(range)")
        protected int[] normalizeObjectRange(DynamicObject range, int size,
                @Cached ToIntNode toInt) {
            return normalize(
                    toInt.execute(Layouts.OBJECT_RANGE.getBegin(range)),
                    toInt.execute(Layouts.OBJECT_RANGE.getEnd(range)),
                    Layouts.OBJECT_RANGE.getExcludedEnd(range),
                    size);
        }

        private int[] normalize(int begin, int end, boolean excludedEnd, int size) {

            if (negativeBegin.profile(begin < 0)) {
                begin += size; // no overflow
            }


            if (negativeEnd.profile(end < 0)) {
                end += size; // no overflow
            }

            final int length;
            try {
                if (notExcluded.profile(!excludedEnd)) {
                    end = Math.incrementExact(end);
                }
                length = Math.subtractExact(end, begin);
            } catch (ArithmeticException e) {
                overflow.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().rangeError("long too big to convert into `int'", this));
            }

            return new int[]{ begin, length };
        }
    }
}
