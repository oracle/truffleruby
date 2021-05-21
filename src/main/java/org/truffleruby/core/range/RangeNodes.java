/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.range;

import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.PrimitiveNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.core.array.ArrayBuilderNode;
import org.truffleruby.core.array.ArrayBuilderNode.BuilderState;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.BooleanCastWithDefaultNodeGen;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.yield.CallBlockNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreModule(value = "Range", isClass = true)
public abstract class RangeNodes {

    @Primitive(name = "range_integer_map")
    public abstract static class IntegerMapNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyArray map(RubyIntRange range, RubyProc block,
                @Cached ArrayBuilderNode arrayBuilder,
                @Cached CallBlockNode yieldNode,
                @Cached ConditionProfile noopProfile,
                @Cached LoopConditionProfile loopProfile) {
            final int begin = range.begin;
            final int end = range.end;
            final boolean excludedEnd = range.excludedEnd;
            int exclusiveEnd = excludedEnd ? end : end + 1;
            if (noopProfile.profile(begin >= exclusiveEnd)) {
                return createEmptyArray();
            }

            final int length = exclusiveEnd - begin;
            BuilderState state = arrayBuilder.start(length);

            int n = 0;
            try {
                loopProfile.profileCounted(length);
                for (; loopProfile.inject(n < length); n++) {
                    arrayBuilder.appendValue(state, n, yieldNode.yield(block, begin + n));
                }
            } finally {
                LoopNode.reportLoopCount(this, n);
            }

            return createArray(arrayBuilder.finish(state, length), length);
        }

        @Specialization(guards = "!isIntRange(range)")
        protected Object mapFallback(RubyRange range, Object block) {
            return FAILURE;
        }
    }

    @CoreMethod(names = "each", needsBlock = true, enumeratorSize = "size")
    public abstract static class EachNode extends YieldingCoreMethodNode {

        @Child private DispatchNode eachInternalCall;

        @Specialization
        protected RubyIntRange eachInt(RubyIntRange range, RubyProc block,
                @Cached LoopConditionProfile loopProfile) {
            int result;
            if (range.excludedEnd) {
                result = range.end;
            } else {
                result = range.end + 1;
            }
            final int exclusiveEnd = result;

            int n = range.begin;
            try {
                loopProfile.profileCounted(exclusiveEnd - range.begin);
                for (; loopProfile.inject(n < exclusiveEnd); n++) {
                    callBlock(block, n);
                }
            } finally {
                LoopNode.reportLoopCount(this, n - range.begin);
            }

            return range;
        }

        @Specialization
        protected RubyLongRange eachLong(RubyLongRange range, RubyProc block,
                @Cached LoopConditionProfile loopProfile) {
            long result;
            if (range.excludedEnd) {
                result = range.end;
            } else {
                result = range.end + 1;
            }
            final long exclusiveEnd = result;

            long n = range.begin;
            try {
                loopProfile.profileCounted(exclusiveEnd - range.begin);
                for (; loopProfile.inject(n < exclusiveEnd); n++) {
                    callBlock(block, n);
                }
            } finally {
                reportLongLoopCount(n - range.begin);
            }

            return range;
        }

        @Specialization
        protected Object eachObject(RubyObjectRange range, RubyProc block) {
            if (eachInternalCall == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                eachInternalCall = insert(DispatchNode.create());
            }

            return eachInternalCall.callWithBlock(range, "each_internal", block);
        }
    }

    @CoreMethod(names = "exclude_end?")
    public abstract static class ExcludeEndNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean excludeEnd(RubyRange range) {
            return range.excludedEnd;
        }

    }

    @CoreMethod(names = "begin")
    public abstract static class BeginNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int eachInt(RubyIntRange range) {
            return range.begin;
        }

        @Specialization
        protected long eachLong(RubyLongRange range) {
            return range.begin;
        }

        @Specialization
        protected Object eachObject(RubyObjectRange range) {
            return range.begin;
        }

    }

    @CoreMethod(names = { "dup", "clone" })
    public abstract static class DupNode extends UnaryCoreMethodNode {

        // NOTE(norswap): This is a hack, as it doesn't copy the ivars.
        //   We do copy the logical class (but not the singleton class, to be MRI compatible).

        @Specialization
        protected RubyIntRange dupIntRange(RubyIntRange range) {
            // RubyIntRange means this isn't a Range subclass (cf. NewNode), we can use the shape directly.
            final Shape shape = getLanguage().intRangeShape;
            final RubyIntRange copy = new RubyIntRange(
                    coreLibrary().rangeClass,
                    shape,
                    range.excludedEnd,
                    range.begin,
                    range.end);
            AllocationTracing.trace(copy, this);
            return copy;
        }

        @Specialization
        protected RubyLongRange dupLongRange(RubyLongRange range) {
            // RubyLongRange means this isn't a Range subclass (cf. NewNode), we can use the shape directly.
            final Shape shape = getLanguage().longRangeShape;
            final RubyLongRange copy = new RubyLongRange(
                    coreLibrary().rangeClass,
                    shape,
                    range.excludedEnd,
                    range.begin,
                    range.end);
            AllocationTracing.trace(copy, this);
            return copy;
        }

        @Specialization
        protected RubyObjectRange dup(RubyObjectRange range) {
            final RubyClass logicalClass = range.getLogicalClass();
            final RubyObjectRange copy = new RubyObjectRange(
                    logicalClass,
                    getLanguage().objectRangeShape,
                    range.excludedEnd,
                    range.begin,
                    range.end);
            AllocationTracing.trace(copy, this);
            return copy;
        }
    }

    @CoreMethod(names = "end")
    public abstract static class EndNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int lastInt(RubyIntRange range) {
            return range.end;
        }

        @Specialization
        protected long lastLong(RubyLongRange range) {
            return range.end;
        }

        @Specialization
        protected Object lastObject(RubyObjectRange range) {
            return range.end;
        }
    }

    @CoreMethod(names = "step", needsBlock = true, optional = 1, lowerFixnum = 1)
    public abstract static class StepNode extends YieldingCoreMethodNode {

        @Child private DispatchNode stepInternalCall;

        @Specialization(guards = "step > 0")
        protected Object stepInt(RubyIntRange range, int step, RubyProc block,
                @Cached LoopConditionProfile loopProfile) {
            int result;
            if (range.excludedEnd) {
                result = range.end;
            } else {
                result = range.end + 1;
            }

            int n = range.begin;
            try {
                loopProfile.profileCounted((result - range.begin - 1) / step + 1);
                for (; loopProfile.inject(n < result); n += step) {
                    callBlock(block, n);
                }
            } finally {
                LoopNode.reportLoopCount(this, n - range.begin);
            }

            return range;
        }

        @Specialization(guards = "step > 0")
        protected Object stepLong(RubyLongRange range, int step, RubyProc block) {
            long result;
            if (range.excludedEnd) {
                result = range.end;
            } else {
                result = range.end + 1;
            }

            long n = range.begin;
            try {
                for (; n < result; n += step) {
                    callBlock(block, n);
                }
            } finally {
                reportLongLoopCount(n - range.begin);
            }

            return range;
        }

        @Fallback
        protected Object stepFallback(VirtualFrame frame, Object range, Object step, Object block) {
            if (stepInternalCall == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                stepInternalCall = insert(DispatchNode.create());
            }

            if (RubyGuards.wasNotProvided(step)) {
                step = 1;
            }

            final Object blockArgument = RubyArguments.getBlock(frame);
            return stepInternalCall.callWithBlock(range, "step_internal", blockArgument, step);
        }
    }

    @CoreMethod(names = "to_a")
    public abstract static class ToANode extends CoreMethodArrayArgumentsNode {

        private final BranchProfile overflow = BranchProfile.create();
        private final ConditionProfile emptyProfile = ConditionProfile.create();

        @Child private DispatchNode toAInternalCall;

        @Specialization
        protected RubyArray toA(RubyIntRange range) {
            final int begin = range.begin;
            int result;
            if (range.excludedEnd) {
                result = range.end;
            } else {
                result = range.end + 1;
            }
            final int length = result - begin;

            if (emptyProfile.profile(length < 0)) {
                return createEmptyArray();
            } else {
                final int[] values = new int[length];

                for (int n = 0; n < length; n++) {
                    values[n] = begin + n;
                }

                return createArray(values);
            }
        }

        @Specialization
        protected RubyArray toA(RubyLongRange range) {
            final long begin = range.begin;
            long result;
            if (range.excludedEnd) {
                result = range.end;
            } else {
                result = range.end + 1;
            }

            final int length;
            try {
                length = Math.toIntExact(result - begin);
            } catch (ArithmeticException e) {
                overflow.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().rangeError("long too big to convert into `int'", this));
            }

            if (emptyProfile.profile(length < 0)) {
                return createEmptyArray();
            } else {
                final long[] values = new long[length];

                for (int n = 0; n < length; n++) {
                    values[n] = begin + n;
                }

                return createArray(values);
            }
        }

        @Specialization(guards = "range.isBounded()")
        protected Object boundedToA(RubyObjectRange range) {
            if (toAInternalCall == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toAInternalCall = insert(DispatchNode.create());
            }

            return toAInternalCall.call(range, "to_a_internal");
        }

        @Specialization(guards = "range.isEndless() || range.isBoundless()")
        protected Object endlessToA(RubyObjectRange range) {
            throw new RaiseException(getContext(), coreExceptions().rangeError(
                    "cannot convert endless range to an array",
                    this));
        }

        @Specialization(guards = "range.isBeginless()")
        protected Object beginlessToA(RubyObjectRange range) {
            throw new RaiseException(getContext(), coreExceptions().typeError(
                    "can't iterate from NilClass",
                    this));
        }
    }

    /** Returns a conversion of the range into an int range, with regard to the supplied array (the array is necessary
     * to handle endless ranges). */
    @Primitive(name = "range_to_int_range")
    public abstract static class ToIntRangeNode extends PrimitiveArrayArgumentsNode {

        @Child private ToIntNode toIntNode;

        @Specialization
        protected RubyIntRange intRange(RubyIntRange range, RubyArray array) {
            return range;
        }

        @Specialization
        protected RubyIntRange longRange(RubyLongRange range, RubyArray array) {
            int begin = toInt(range.begin);
            int end = toInt(range.end);
            return new RubyIntRange(
                    coreLibrary().rangeClass,
                    getLanguage().intRangeShape,
                    range.excludedEnd,
                    begin,
                    end);
        }

        @Specialization(guards = "range.isBounded()")
        protected RubyIntRange boundedObjectRange(RubyObjectRange range, RubyArray array) {
            int begin = toInt(range.begin);
            int end = toInt(range.end);
            return new RubyIntRange(
                    coreLibrary().rangeClass,
                    getLanguage().intRangeShape,
                    range.excludedEnd,
                    begin,
                    end);
        }

        @Specialization(guards = "range.isEndless()")
        protected RubyIntRange endlessObjectRange(RubyObjectRange range, RubyArray array) {
            int end = array.size;
            return new RubyIntRange(
                    coreLibrary().rangeClass,
                    getLanguage().intRangeShape,
                    true,
                    toInt(range.begin),
                    end);
        }

        @Specialization(guards = "range.isBeginless()")
        protected RubyIntRange beginlessObjectRange(RubyObjectRange range, RubyArray array) {
            int begin = 0;
            int end = toInt(range.end);
            return new RubyIntRange(
                    coreLibrary().rangeClass,
                    getLanguage().intRangeShape,
                    range.excludedEnd,
                    begin,
                    end);
        }

        @Specialization(guards = "range.isBoundless()")
        protected RubyIntRange nilNilObjectRange(RubyObjectRange range, RubyArray array) {
            int begin = 0;
            int end = array.size;
            return new RubyIntRange(
                    coreLibrary().rangeClass,
                    getLanguage().intRangeShape,
                    false,
                    begin,
                    end);
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

        @Specialization
        protected RubyObjectRange initialize(RubyObjectRange range, Object begin, Object end, boolean excludeEnd) {
            range.excludedEnd = excludeEnd;
            range.begin = begin;
            range.end = end;
            return range;
        }
    }

    @CoreMethod(names = "new", constructor = true, required = 2, optional = 1)
    @NodeChild(value = "rubyClass", type = RubyNode.class)
    @NodeChild(value = "begin", type = RubyNode.class)
    @NodeChild(value = "end", type = RubyNode.class)
    @NodeChild(value = "excludeEnd", type = RubyNode.class)
    public abstract static class NewNode extends CoreMethodNode {

        @CreateCast("excludeEnd")
        protected RubyNode coerceToBoolean(RubyNode excludeEnd) {
            return BooleanCastWithDefaultNodeGen.create(false, excludeEnd);
        }

        @Specialization(guards = "rubyClass == getRangeClass()")
        protected RubyIntRange intRange(RubyClass rubyClass, int begin, int end, boolean excludeEnd) {
            // Not a Range subclass, we can use the shape directly.
            final RubyIntRange range = new RubyIntRange(
                    coreLibrary().rangeClass,
                    getLanguage().intRangeShape,
                    excludeEnd,
                    begin,
                    end);
            AllocationTracing.trace(range, this);
            return range;
        }

        @Specialization(guards = { "rubyClass == getRangeClass()", "fitsInInteger(begin)", "fitsInInteger(end)" })
        protected RubyIntRange longFittingIntRange(RubyClass rubyClass, long begin, long end, boolean excludeEnd) {
            // Not a Range subclass, we can use the shape directly.
            final Shape shape = getLanguage().intRangeShape;
            final RubyIntRange range = new RubyIntRange(
                    coreLibrary().rangeClass,
                    shape,
                    excludeEnd,
                    (int) begin,
                    (int) end);
            AllocationTracing.trace(range, this);
            return range;
        }

        @Specialization(guards = { "rubyClass == getRangeClass()", "!fitsInInteger(begin) || !fitsInInteger(end)" })
        protected RubyLongRange longRange(RubyClass rubyClass, long begin, long end, boolean excludeEnd) {
            // Not a Range subclass, we can use the shape directly.
            final RubyLongRange range = new RubyLongRange(
                    coreLibrary().rangeClass,
                    getLanguage().longRangeShape,
                    excludeEnd,
                    begin,
                    end);
            AllocationTracing.trace(range, this);
            return range;
        }

        @Specialization(guards = { "rubyClass != getRangeClass() || (!isImplicitLong(begin) || !isImplicitLong(end))" })
        protected RubyObjectRange objectRange(RubyClass rubyClass, Object begin, Object end, boolean excludeEnd,
                @Cached DispatchNode compare) {

            if (compare.call(begin, "<=>", end) == nil && end != nil && begin != nil) {
                throw new RaiseException(getContext(), coreExceptions().argumentError("bad value for range", this));
            }

            final Shape shape = getLanguage().objectRangeShape;
            final RubyObjectRange range = new RubyObjectRange(rubyClass, shape, excludeEnd, begin, end);
            AllocationTracing.trace(range, this);
            return range;
        }

        protected RubyClass getRangeClass() {
            return coreLibrary().rangeClass;
        }
    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @Specialization
        protected RubyObjectRange allocate(RubyClass rubyClass) {
            final Shape shape = getLanguage().objectRangeShape;
            final RubyObjectRange range = new RubyObjectRange(rubyClass, shape, false, nil, nil);
            AllocationTracing.trace(range, this);
            return range;
        }
    }

    /** Returns an array containing normalized int range parameters {@code [start, length]}, such that both are 32-bits
     * java ints (if conversion is impossible, an error is raised). The method attempts to make the values positive, by
     * adding {@code size} to them if they are negative. They may still be negative after the operation however, as
     * different core Ruby methods have different way of handling negative out-of-bound normalized values.
     * <p>
     * The values will NOT be clamped to represent a valid array range, excepted the length for endless ranges.
     * <p>
     * {@code size} is assumed to be normalized: fitting in an int, and positive. */
    @Primitive(name = "range_normalized_start_length", lowerFixnum = 1)
    @NodeChild(value = "range", type = RubyNode.class)
    @NodeChild(value = "size", type = RubyNode.class)
    public abstract static class NormalizedStartLengthPrimitiveNode extends PrimitiveNode {

        @Child NormalizedStartLengthNode startLengthNode = NormalizedStartLengthNode.create();

        @Specialization
        protected RubyArray normalize(RubyRange range, int size) {
            return createArray(startLengthNode.execute(range, size));
        }
    }

    /** @see NormalizedStartLengthPrimitiveNode */
    public abstract static class NormalizedStartLengthNode extends RubyContextNode {

        public static NormalizedStartLengthNode create() {
            return RangeNodesFactory.NormalizedStartLengthNodeGen.create();
        }

        public abstract int[] execute(RubyRange range, int size);

        private final BranchProfile overflow = BranchProfile.create();
        private final ConditionProfile notExcluded = ConditionProfile.create();
        private final ConditionProfile negativeBegin = ConditionProfile.create();
        private final ConditionProfile negativeEnd = ConditionProfile.create();

        @Specialization
        protected int[] normalizeIntRange(RubyIntRange range, int size) {
            return normalize(range.begin, range.end, range.excludedEnd, size);
        }

        @Specialization
        protected int[] normalizeLongRange(RubyLongRange range, int size,
                @Cached ToIntNode toInt) {
            return normalize(toInt.execute(range.begin), toInt.execute(range.end), range.excludedEnd, size);
        }

        @Specialization(guards = "range.isEndless()")
        protected int[] normalizeEndlessRange(RubyObjectRange range, int size,
                @Cached ToIntNode toInt) {
            int begin = toInt.execute(range.begin);
            return new int[]{ begin >= 0 ? begin : begin + size, size - begin };
        }

        @Specialization(guards = "range.isBounded()")
        protected int[] normalizeObjectRange(RubyObjectRange range, int size,
                @Cached ToIntNode toInt) {
            return normalize(toInt.execute(range.begin), toInt.execute(range.end), range.excludedEnd, size);
        }

        @Specialization(guards = "range.isBeginless()")
        protected int[] normalizeBeginlessRange(RubyObjectRange range, int size,
                @Cached ToIntNode toInt) {
            return normalize(0, toInt.execute(range.end), range.excludedEnd, size);
        }

        @Specialization(guards = "range.isBoundless()")
        protected int[] normalizeNilNilRange(RubyObjectRange range, int size,
                @Cached ToIntNode toInt) {
            return new int[]{ 0, size };
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
