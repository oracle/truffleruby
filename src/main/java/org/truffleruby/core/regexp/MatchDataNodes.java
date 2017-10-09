/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.regexp;

import java.util.Arrays;

import org.jcodings.Encoding;
import org.joni.Region;
import org.joni.exception.ValueException;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.array.ArrayOperations;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.cast.TaintResultNode;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.regexp.MatchDataNodesFactory.GlobalNodeFactory;
import org.truffleruby.core.regexp.MatchDataNodesFactory.PostMatchNodeFactory;
import org.truffleruby.core.regexp.MatchDataNodesFactory.PreMatchNodeFactory;
import org.truffleruby.core.regexp.MatchDataNodesFactory.ValuesNodeFactory;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.StringGuards;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringSupport;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreClass("MatchData")
public abstract class MatchDataNodes {

    public static Object begin(RubyContext context, DynamicObject matchData, int index) {
        // Taken from org.jruby.RubyMatchData
        int b = Layouts.MATCH_DATA.getRegion(matchData).beg[index];

        if (b < 0) {
            return context.getCoreLibrary().getNil();
        }

        if (!StringGuards.isSingleByteOptimizable(Layouts.MATCH_DATA.getSource(matchData))) {
            b = getCharOffsets(matchData).beg[index];
        }

        return b;
    }

    public static Object end(RubyContext context, DynamicObject matchData, int index) {
        // Taken from org.jruby.RubyMatchData
        int e = Layouts.MATCH_DATA.getRegion(matchData).end[index];

        if (e < 0) {
            return context.getCoreLibrary().getNil();
        }

        if (!StringGuards.isSingleByteOptimizable(Layouts.MATCH_DATA.getSource(matchData))) {
            e = getCharOffsets(matchData).end[index];
        }

        return e;
    }

    private static void updatePairs(Rope source, Encoding encoding, Pair[] pairs) {
        // Taken from org.jruby.RubyMatchData
        Arrays.sort(pairs);

        int length = pairs.length;
        byte[]bytes = source.getBytes();
        int p = 0;
        int s = p;
        int c = 0;

        for (int i = 0; i < length; i++) {
            int q = s + pairs[i].bytePos;
            c += StringSupport.strLength(encoding, bytes, p, q);
            pairs[i].charPos = c;
            p = q;
        }
    }

    private static Region getCharOffsetsManyRegs(DynamicObject matchData, Rope source, Encoding encoding) {
        // Taken from org.jruby.RubyMatchData
        final Region regs = Layouts.MATCH_DATA.getRegion(matchData);
        int numRegs = regs.numRegs;

        final Region charOffsets = new Region(numRegs);

        if (encoding.maxLength() == 1) {
            for (int i = 0; i < numRegs; i++) {
                charOffsets.beg[i] = regs.beg[i];
                charOffsets.end[i] = regs.end[i];
            }
            return charOffsets;
        }

        Pair[] pairs = new Pair[numRegs * 2];
        for (int i = 0; i < pairs.length; i++) {
            pairs[i] = new Pair();
        }

        int numPos = 0;
        for (int i = 0; i < numRegs; i++) {
            if (regs.beg[i] < 0) {
                continue;
            }
            pairs[numPos++].bytePos = regs.beg[i];
            pairs[numPos++].bytePos = regs.end[i];
        }

        updatePairs(source, encoding, pairs);

        Pair key = new Pair();
        for (int i = 0; i < regs.numRegs; i++) {
            if (regs.beg[i] < 0) {
                charOffsets.beg[i] = charOffsets.end[i] = -1;
                continue;
            }
            key.bytePos = regs.beg[i];
            charOffsets.beg[i] = pairs[Arrays.binarySearch(pairs, key)].charPos;
            key.bytePos = regs.end[i];
            charOffsets.end[i] = pairs[Arrays.binarySearch(pairs, key)].charPos;
        }

        return charOffsets;
    }

    public static Region getCharOffsets(DynamicObject matchData) {
        // Taken from org.jruby.RubyMatchData
        Region charOffsets = Layouts.MATCH_DATA.getCharOffsets(matchData);
        if (charOffsets != null) {
            return charOffsets;
        } else {
            return createCharOffsets(matchData);
        }
    }

    @TruffleBoundary
    private static Region createCharOffsets(DynamicObject matchData) {
        final Rope source = StringOperations.rope(Layouts.MATCH_DATA.getSource(matchData));
        final Encoding enc = source.getEncoding();
        final Region charOffsets = getCharOffsetsManyRegs(matchData, source, enc);
        Layouts.MATCH_DATA.setCharOffsets(matchData, charOffsets);
        return charOffsets;
    }

    @CoreMethod(names = "[]", required = 1, optional = 1, lowerFixnum = { 1, 2 }, taintFrom = 0)
    public abstract static class GetIndexNode extends CoreMethodArrayArgumentsNode {

        @Child private ToIntNode toIntNode;
        @Child private ValuesNode getValues = ValuesNode.create();

        public static GetIndexNode create() {
            return MatchDataNodesFactory.GetIndexNodeFactory.create(null);
        }

        public abstract Object executeGetIndex(VirtualFrame frame, Object matchData, Object index, Object length);

        @Specialization
        public Object getIndex(DynamicObject matchData, int index, NotProvided length,
                               @Cached("createBinaryProfile()") ConditionProfile indexOutOfBoundsProfile) {
            final Object[] values = getValues.execute(matchData);
            final int normalizedIndex = ArrayOperations.normalizeIndex(values.length, index);

            if (indexOutOfBoundsProfile.profile((normalizedIndex < 0) || (normalizedIndex >= values.length))) {
                return nil();
            } else {
                return values[normalizedIndex];
            }
        }

        @TruffleBoundary
        @Specialization
        public Object getIndex(DynamicObject matchData, int index, int length) {
            // TODO BJF 15-May-2015 Need to handle negative indexes and lengths and out of bounds
            final Object[] values = getValues.execute(matchData);
            final int normalizedIndex = ArrayOperations.normalizeIndex(values.length, index);
            final Object[] store = Arrays.copyOfRange(values, normalizedIndex, normalizedIndex + length);
            return createArray(store, length);
        }

        @TruffleBoundary(throwsControlFlowException = true)
        @Specialization(guards = "isRubySymbol(index)")
        public Object getIndexSymbol(DynamicObject matchData, DynamicObject index, NotProvided length,
                @Cached("create()") BranchProfile errorProfile,
                @Cached("createBinaryProfile()") ConditionProfile indexOutOfBoundsProfile) {
            try {
                final Rope value = Layouts.SYMBOL.getRope(index);
                final int i = Layouts.REGEXP.getRegex(Layouts.MATCH_DATA.getRegexp(matchData)).nameToBackrefNumber(value.getBytes(), 0, value.byteLength(), Layouts.MATCH_DATA.getRegion(matchData));

                return getIndex(matchData, i, NotProvided.INSTANCE, indexOutOfBoundsProfile);
            } catch (final ValueException e) {
                throw new RaiseException(
                        coreExceptions().indexError(StringUtils.format("undefined group name reference: %s", Layouts.SYMBOL.getString(index)), this));
            }
        }

        @TruffleBoundary(throwsControlFlowException = true)
        @Specialization(guards = "isRubyString(index)")
        public Object getIndexString(DynamicObject matchData, DynamicObject index, NotProvided length,
                                     @Cached("createBinaryProfile()") ConditionProfile indexOutOfBoundsProfile) {
            try {
                final Rope value = StringOperations.rope(index);
                final int i = Layouts.REGEXP.getRegex(Layouts.MATCH_DATA.getRegexp(matchData)).nameToBackrefNumber(value.getBytes(), 0, value.byteLength(), Layouts.MATCH_DATA.getRegion(matchData));

                return getIndex(matchData, i, NotProvided.INSTANCE, indexOutOfBoundsProfile);
            }
            catch (final ValueException e) {
                throw new RaiseException(
                        coreExceptions().indexError(StringUtils.format("undefined group name reference: %s", index.toString()), this));
            }
        }

        @Specialization(guards = { "!isRubySymbol(index)", "!isRubyString(index)", "!isIntRange(index)" })
        public Object getIndex(VirtualFrame frame, DynamicObject matchData, Object index, NotProvided length,
                               @Cached("createBinaryProfile()") ConditionProfile indexOutOfBoundsProfile) {
            if (toIntNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toIntNode = insert(ToIntNode.create());
            }

            return getIndex(matchData, toIntNode.doInt(frame, index), NotProvided.INSTANCE, indexOutOfBoundsProfile);
        }

        @TruffleBoundary
        @Specialization(guards = "isIntRange(range)")
        public Object getIndex(DynamicObject matchData, DynamicObject range, NotProvided len) {
            final Object[] values = getValues.execute(matchData);
            final int normalizedIndex = ArrayOperations.normalizeIndex(values.length, Layouts.INT_RANGE.getBegin(range));
            final int end = ArrayOperations.normalizeIndex(values.length, Layouts.INT_RANGE.getEnd(range));
            final int exclusiveEnd = ArrayOperations.clampExclusiveIndex(values.length, Layouts.INT_RANGE.getExcludedEnd(range) ? end : end + 1);
            final int length = exclusiveEnd - normalizedIndex;

            final Object[] store = Arrays.copyOfRange(values, normalizedIndex, normalizedIndex + length);
            return createArray(store, length);
        }

    }

    @CoreMethod(names = "begin", required = 1, lowerFixnum = 1)
    public abstract static class BeginNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "inBounds(matchData, index)")
        public Object begin(DynamicObject matchData, int index) {
            return MatchDataNodes.begin(getContext(), matchData, index);
        }

        @TruffleBoundary
        @Specialization(guards = "!inBounds(matchData, index)")
        public Object beginError(DynamicObject matchData, int index) {
            throw new RaiseException(coreExceptions().indexError(StringUtils.format("index %d out of matches", index), this));
        }

        protected boolean inBounds(DynamicObject matchData, int index) {
            return index >= 0 && index < Layouts.MATCH_DATA.getRegion(matchData).numRegs;
        }
    }


    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "matchData")
    })
    public abstract static class ValuesNode extends CoreMethodNode {

        @Child private RopeNodes.MakeSubstringNode makeSubstringNode = RopeNodes.MakeSubstringNode.create();

        public static ValuesNode create() {
            return ValuesNodeFactory.create(null);
        }

        public abstract Object[] execute(DynamicObject matchData);

        @Specialization(guards = "hasValues(matchData)")
        public Object[] getValuesFast(DynamicObject matchData) {
            return Layouts.MATCH_DATA.getValues(matchData);
        }

        @TruffleBoundary
        @Specialization(guards = "!hasValues(matchData)")
        public Object[] getValuesSlow(DynamicObject matchData) {
            DynamicObject source = Layouts.MATCH_DATA.getSource(matchData);
            Rope sourceRope = StringOperations.rope(source);
            Region region = Layouts.MATCH_DATA.getRegion(matchData);
            final Object[] values = new Object[region.numRegs];

            for (int n = 0; n < region.numRegs; n++) {
                final int start = region.beg[n];
                final int end = region.end[n];

                if (start > -1 && end > -1) {
                    Rope rope = makeSubstringNode.executeMake(sourceRope, start, end - start);
                    values[n] = createSubstring(source, rope);
                } else {
                    values[n] = nil();
                }
            }

            Layouts.MATCH_DATA.setValues(matchData, values);
            return values;
        }

        public static boolean hasValues(DynamicObject matchData) {
            return Layouts.MATCH_DATA.getValues(matchData) != null;
        }
    }

    @CoreMethod(names = "captures")
    public abstract static class CapturesNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode dupNode = CallDispatchHeadNode.create();
        @Child private ValuesNode getValues = ValuesNode.create();

        @Specialization
        public DynamicObject toA(VirtualFrame frame, DynamicObject matchData) {
            Object[] objects = getCaptures(getValues.execute(matchData));
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] != nil()) {
                    objects[i] = dupNode.call(frame, objects[i], "dup");
                }
            }
            return createArray(objects, objects.length);
        }

        @TruffleBoundary
        private static Object[] getCaptures(Object[] values) {
            return ArrayUtils.extractRange(values, 1, values.length);
        }
    }

    @CoreMethod(names = "end", required = 1, lowerFixnum = 1)
    public abstract static class EndNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "inBounds(matchData, index)")
        public Object end(DynamicObject matchData, int index) {
            return MatchDataNodes.end(getContext(), matchData, index);
        }

        @TruffleBoundary
        @Specialization(guards = "!inBounds(matchData, index)")
        public Object endError(DynamicObject matchData, int index) {
            throw new RaiseException(coreExceptions().indexError(StringUtils.format("index %d out of matches", index), this));
        }

        protected boolean inBounds(DynamicObject matchData, int index) {
            return index >= 0 && index < Layouts.MATCH_DATA.getRegion(matchData).numRegs;
        }
    }

    @NonStandard
    @CoreMethod(names = "byte_begin", required = 1, lowerFixnum = 1)
    public abstract static class ByteBeginNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "inBounds(matchData, index)")
        public Object byteBegin(DynamicObject matchData, int index) {
            int b = Layouts.MATCH_DATA.getRegion(matchData).beg[index];
            if (b < 0) {
                return nil();
            } else {
                return b;
            }
        }

        protected boolean inBounds(DynamicObject matchData, int index) {
            return index >= 0 && index < Layouts.MATCH_DATA.getRegion(matchData).numRegs;
        }
    }

    @NonStandard
    @CoreMethod(names = "byte_end", required = 1, lowerFixnum = 1)
    public abstract static class ByteEndNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "inBounds(matchData, index)")
        public Object byteEnd(DynamicObject matchData, int index) {
            int e = Layouts.MATCH_DATA.getRegion(matchData).end[index];
            if (e < 0) {
                return nil();
            } else {
                return e;
            }
        }

        protected boolean inBounds(DynamicObject matchData, int index) {
            return index >= 0 && index < Layouts.MATCH_DATA.getRegion(matchData).numRegs;
        }
    }

    // because the factory is not constant
    @TruffleBoundary
    private static DynamicObject createSubstring(DynamicObject source, Rope rope) {
        return Layouts.CLASS.getInstanceFactory(Layouts.BASIC_OBJECT.getLogicalClass(source)).newInstance(Layouts.STRING.build(false, false, rope));
    }

    @CoreMethod(names = { "length", "size" })
    public abstract static class LengthNode extends CoreMethodArrayArgumentsNode {

        @Child private ValuesNode getValues = ValuesNode.create();

        @Specialization
        public int length(DynamicObject matchData) {
            return getValues.execute(matchData).length;
        }

    }

    @CoreMethod(names = "pre_match")
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "matchData")
    })
    public abstract static class PreMatchNode extends CoreMethodNode {

        @Child private TaintResultNode taintResultNode = new TaintResultNode();
        @Child private RopeNodes.MakeSubstringNode makeSubstringNode = RopeNodes.MakeSubstringNode.create();

        public static PreMatchNode create(RubyNode getMatchDataNode) {
            return PreMatchNodeFactory.create(getMatchDataNode);
        }

        public abstract DynamicObject execute(DynamicObject matchData);

        @Specialization(guards = "hasBennConstructed(matchData)")
        public Object preMatchFast(DynamicObject matchData) {
            return Layouts.MATCH_DATA.getPre(matchData);
        }

        @Specialization(guards = "!hasBennConstructed(matchData)")
        public Object preMatch(DynamicObject matchData) {
            DynamicObject source = Layouts.MATCH_DATA.getSource(matchData);
            Rope sourceRope = StringOperations.rope(source);
            Region region = Layouts.MATCH_DATA.getRegion(matchData);
            int start = 0;
            int length = region.beg[0];
            Rope rope = makeSubstringNode.executeMake(sourceRope, start, length);
            DynamicObject newStr = createSubstring(source, rope);
            Layouts.MATCH_DATA.setPre(matchData, newStr);
            return taintResultNode.maybeTaint(source, newStr);
        }

        protected boolean hasBennConstructed(DynamicObject matchData) {
            return Layouts.MATCH_DATA.getPre(matchData) != null;
        }
    }

    @CoreMethod(names = "post_match")
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "matchData")
    })
    public abstract static class PostMatchNode extends CoreMethodNode {

        @Child private TaintResultNode taintResultNode = new TaintResultNode();
        @Child private RopeNodes.MakeSubstringNode makeSubstringNode = RopeNodes.MakeSubstringNode.create();

        public static PostMatchNode create(RubyNode getMatchDataNode) {
            return PostMatchNodeFactory.create(getMatchDataNode);
        }

        public abstract DynamicObject execute(DynamicObject matchData);

        @Specialization(guards = "hasBennConstructed(matchData)")
        public Object postMatchFast(DynamicObject matchData) {
            return Layouts.MATCH_DATA.getPost(matchData);
        }

        @Specialization(guards = "!hasBennConstructed(matchData)")
        public Object postMatch(DynamicObject matchData) {
            DynamicObject source = Layouts.MATCH_DATA.getSource(matchData);
            Rope sourceRope = StringOperations.rope(source);
            Region region = Layouts.MATCH_DATA.getRegion(matchData);
            int start = region.end[0];
            int length = sourceRope.byteLength() - region.end[0];
            Rope rope = makeSubstringNode.executeMake(sourceRope, start, length);
            DynamicObject newStr = createSubstring(source, rope);
            Layouts.MATCH_DATA.setPost(matchData, newStr);
            return taintResultNode.maybeTaint(source, newStr);
        }

        protected boolean hasBennConstructed(DynamicObject matchData) {
            return Layouts.MATCH_DATA.getPost(matchData) != null;
        }
    }

    @CoreMethod(names = "to_a")
    public abstract static class ToANode extends CoreMethodArrayArgumentsNode {

        @Child ValuesNode getValues = ValuesNode.create();

        @Specialization
        public DynamicObject toA(DynamicObject matchData) {
            Object[] objects = ArrayUtils.copy(getValues.execute(matchData));
            return createArray(objects, objects.length);
        }
    }

    @CoreMethod(names = "to_s")
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "matchData")
    })
    public abstract static class ToSNode2 extends CoreMethodNode {

        @Child GlobalNode globalNode = GlobalNode.create(null);

        @Specialization
        public DynamicObject executeToS(DynamicObject matchData) {
            return createString(StringOperations.rope(globalNode.execute(matchData)));
        }
    }
    
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "matchData")
    })
    public abstract static class GlobalNode extends CoreMethodNode {

        @Child private RopeNodes.MakeSubstringNode makeSubstringNode = RopeNodes.MakeSubstringNode.create();

        public static GlobalNode create(RubyNode getMatchDataNode) {
            return GlobalNodeFactory.create(getMatchDataNode);
        }

        public abstract DynamicObject execute(DynamicObject matchData);

        @Specialization(guards = "hasBennConstructed(matchData)")
        public Object postMatchFast(DynamicObject matchData) {
            return Layouts.MATCH_DATA.getGlobal(matchData);
        }

        @Specialization(guards = "!hasBennConstructed(matchData)")
        public DynamicObject toS(DynamicObject matchData) {
            DynamicObject source = Layouts.MATCH_DATA.getSource(matchData);
            Rope sourceRope = StringOperations.rope(source);
            Region region = Layouts.MATCH_DATA.getRegion(matchData);
            int start = region.beg[0];
            int length = region.end[0] - region.beg[0];
            Rope rope = makeSubstringNode.executeMake(sourceRope, start, length);
            DynamicObject global = createSubstring(source, rope);
            Layouts.MATCH_DATA.setGlobal(matchData, global);

            return global;
        }

        protected boolean hasBennConstructed(DynamicObject matchData) {
            return Layouts.MATCH_DATA.getGlobal(matchData) != null;
        }
    }

    @CoreMethod(names = "regexp")
    public abstract static class RegexpNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject regexp(DynamicObject matchData) {
            return Layouts.MATCH_DATA.getRegexp(matchData);
        }
    }

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        // MatchData can be allocated in MRI but it does not seem to be any useful
        @TruffleBoundary
        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            throw new RaiseException(coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }

    }

    @Primitive(name = "match_data_get_source")
    public abstract static class GetSourceNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public DynamicObject getSource(DynamicObject matchData) {
            return Layouts.MATCH_DATA.getSource(matchData);
        }
    }

    public static final class Pair implements Comparable<Pair> {
        int bytePos, charPos;

        @Override
        public int compareTo(Pair pair) {
            return bytePos - pair.bytePos;
        }
    }

}
