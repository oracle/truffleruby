/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.regexp;

import java.util.Arrays;
import java.util.Iterator;

import org.jcodings.Encoding;
import org.joni.NameEntry;
import org.joni.Regex;
import org.joni.Region;
import org.joni.exception.ValueException;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.array.ArrayOperations;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.regexp.MatchDataNodesFactory.ValuesNodeFactory;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringSupport;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.language.objects.IsTaintedNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreClass("MatchData")
public abstract class MatchDataNodes {

    @TruffleBoundary
    public static Object begin(RubyContext context, DynamicObject matchData, int index) {
        // Taken from org.jruby.RubyMatchData
        int b = Layouts.MATCH_DATA.getRegion(matchData).beg[index];

        if (b < 0) {
            return context.getCoreLibrary().getNil();
        }

        final Rope rope = StringOperations.rope(Layouts.MATCH_DATA.getSource(matchData));
        if (!rope.isSingleByteOptimizable()) {
            b = getCharOffsets(matchData).beg[index];
        }

        return b;
    }

    @TruffleBoundary
    public static Object end(RubyContext context, DynamicObject matchData, int index) {
        // Taken from org.jruby.RubyMatchData
        int e = Layouts.MATCH_DATA.getRegion(matchData).end[index];

        if (e < 0) {
            return context.getCoreLibrary().getNil();
        }

        final Rope rope = StringOperations.rope(Layouts.MATCH_DATA.getSource(matchData));
        if (!rope.isSingleByteOptimizable()) {
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
        @Child private ValuesNode getValuesNode = ValuesNode.create();
        @Child private RopeNodes.SubstringNode substringNode = RopeNodes.SubstringNode.create();
        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        public static GetIndexNode create(RubyNode... nodes) {
            return MatchDataNodesFactory.GetIndexNodeFactory.create(nodes);
        }

        public abstract Object executeGetIndex(Object matchData, Object index, Object length);

        @Specialization
        public Object getIndex(DynamicObject matchData, int index, NotProvided length,
                @Cached("createBinaryProfile()") ConditionProfile normalizedIndexProfile,
                @Cached("createBinaryProfile()") ConditionProfile indexOutOfBoundsProfile,
                @Cached("createBinaryProfile()") ConditionProfile hasValueProfile) {
            final DynamicObject source = Layouts.MATCH_DATA.getSource(matchData);
            final Rope sourceRope = StringOperations.rope(source);
            final Region region = Layouts.MATCH_DATA.getRegion(matchData);
            final int normalizedIndex = ArrayOperations.normalizeIndex(region.beg.length, index, normalizedIndexProfile);

            if (indexOutOfBoundsProfile.profile((normalizedIndex < 0) || (normalizedIndex >= region.beg.length))) {
                return nil();
            } else {
                final int start = region.beg[normalizedIndex];
                final int end = region.end[normalizedIndex];
                if (hasValueProfile.profile(start > -1 && end > -1)) {
                    Rope rope = substringNode.executeSubstring(sourceRope, start, end - start);
                    return allocateNode.allocate(Layouts.BASIC_OBJECT.getLogicalClass(source), Layouts.STRING.build(false, false, rope));
                } else {
                    return nil();
                }
            }
        }

        @Specialization
        public Object getIndex(DynamicObject matchData, int index, int length) {
            // TODO BJF 15-May-2015 Need to handle negative indexes and lengths and out of bounds
            final Object[] values = getValuesNode.execute(matchData);
            final int normalizedIndex = ArrayOperations.normalizeIndex(values.length, index);
            final Object[] store = Arrays.copyOfRange(values, normalizedIndex, normalizedIndex + length);
            return createArray(store, length);
        }

        @Specialization(guards = { "isRubySymbol(cachedIndex)", "name != null",
                "getRegexp(matchData) == regexp", "cachedIndex == index" })
        public Object getIndexSymbolSingleMatch(DynamicObject matchData, DynamicObject index, NotProvided length,
                @Cached("index") DynamicObject cachedIndex,
                @Cached("getRegexp(matchData)") DynamicObject regexp,
                @Cached("findNameEntry(regexp, index)") NameEntry name,
                @Cached("numBackRefs(name)") int backRefs,
                @Cached("backRefIndex(name)") int backRefIndex) {
            if (backRefs == 1) {
                return executeGetIndex(matchData, backRefIndex, NotProvided.INSTANCE);
            } else {
                final int i = getBackRef(matchData, regexp, name);

                return executeGetIndex(matchData, i, NotProvided.INSTANCE);
            }
        }

        @Specialization(guards = "isRubySymbol(index)")
        public Object getIndexSymbol(DynamicObject matchData, DynamicObject index, NotProvided length,
                @Cached BranchProfile errorProfile) {
            return executeGetIndex(matchData, getBackRefFromSymbol(matchData, index), NotProvided.INSTANCE);
        }

        @Specialization(guards = "isRubyString(index)")
        public Object getIndexString(DynamicObject matchData, DynamicObject index, NotProvided length) {
            return executeGetIndex(matchData, getBackRefFromString(matchData, index), NotProvided.INSTANCE);
        }

        @Specialization(guards = { "!isRubySymbol(index)", "!isRubyString(index)", "!isIntRange(index)" })
        public Object getIndex(DynamicObject matchData, Object index, NotProvided length) {
            if (toIntNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toIntNode = insert(ToIntNode.create());
            }

            return executeGetIndex(matchData, toIntNode.doInt(index), NotProvided.INSTANCE);
        }

        @TruffleBoundary
        @Specialization(guards = "isIntRange(range)")
        public Object getIndex(DynamicObject matchData, DynamicObject range, NotProvided len) {
            final Object[] values = getValuesNode.execute(matchData);
            final int normalizedIndex = ArrayOperations.normalizeIndex(values.length, Layouts.INT_RANGE.getBegin(range));
            final int end = ArrayOperations.normalizeIndex(values.length, Layouts.INT_RANGE.getEnd(range));
            final int exclusiveEnd = ArrayOperations.clampExclusiveIndex(values.length, Layouts.INT_RANGE.getExcludedEnd(range) ? end : end + 1);
            final int length = exclusiveEnd - normalizedIndex;

            final Object[] store = Arrays.copyOfRange(values, normalizedIndex, normalizedIndex + length);
            return createArray(store, length);
        }

        @TruffleBoundary
        protected static NameEntry findNameEntry(DynamicObject regexp, DynamicObject string) {
            Regex regex = Layouts.REGEXP.getRegex(regexp);
            Rope rope = Layouts.SYMBOL.getRope(string);
            if (regex.numberOfNames() > 0) {
                for (Iterator<NameEntry> i = regex.namedBackrefIterator(); i.hasNext();) {
                    final NameEntry e = i.next();
                    if (bytesEqual(rope.getBytes(), rope.byteLength(), e.name, e.nameP, e.nameEnd)) {
                        return e;
                    }
                }
            }
            return null;
        }

        protected static DynamicObject getRegexp(DynamicObject matchData) {
            return Layouts.MATCH_DATA.getRegexp(matchData);
        }

        @TruffleBoundary(transferToInterpreterOnException = false)
        private int getBackRefFromString(DynamicObject matchData, DynamicObject index) {
            final Rope value = Layouts.STRING.getRope(index);
            return getBackRefFromRope(matchData, index, value);
        }

        @TruffleBoundary(transferToInterpreterOnException = false)
        private int getBackRefFromSymbol(DynamicObject matchData, DynamicObject index) {
            final Rope value = Layouts.SYMBOL.getRope(index);
            return getBackRefFromRope(matchData, index, value);
        }

        private int getBackRefFromRope(DynamicObject matchData, DynamicObject index, Rope value) {
            try {
                return Layouts.REGEXP.getRegex(Layouts.MATCH_DATA.getRegexp(matchData)).nameToBackrefNumber(value.getBytes(), 0, value.byteLength(), Layouts.MATCH_DATA.getRegion(matchData));
            } catch (final ValueException e) {
                throw new RaiseException(
                        getContext(), coreExceptions().indexError(StringUtils.format("undefined group name reference: %s", index.toString()), this));
            }
        }

        @TruffleBoundary
        private int getBackRef(DynamicObject matchData, DynamicObject regexp, NameEntry name) {
            return Layouts.REGEXP.getRegex(regexp).nameToBackrefNumber(name.name, name.nameP,
                    name.nameEnd, Layouts.MATCH_DATA.getRegion(matchData));
        }

        @TruffleBoundary
        protected static int numBackRefs(NameEntry name) {
            return name == null ? 0 : name.getBackRefs().length;
        }

        @TruffleBoundary
        protected static int backRefIndex(NameEntry name) {
            return name == null ? 0 : name.getBackRefs()[0];
        }

        @TruffleBoundary
        private static boolean bytesEqual(byte[] bytes, int byteLength, byte[] name, int nameP, int nameEnd) {
            if (bytes == name && nameP == 0 && byteLength == nameEnd) {
                return true;
            } else if (nameEnd - nameP != byteLength) {
                return false;
            } else {
                return ArrayUtils.memcmp(bytes, 0, name, nameP, byteLength) == 0;
            }
        }
    }

    @Primitive(name = "match_data_begin", lowerFixnum = 1)
    public abstract static class BeginNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "inBounds(matchData, index)")
        public Object begin(DynamicObject matchData, int index) {
            return MatchDataNodes.begin(getContext(), matchData, index);
        }

        @TruffleBoundary
        @Specialization(guards = "!inBounds(matchData, index)")
        public Object beginError(DynamicObject matchData, int index) {
            throw new RaiseException(getContext(), coreExceptions().indexError(StringUtils.format("index %d out of matches", index), this));
        }

        protected boolean inBounds(DynamicObject matchData, int index) {
            return index >= 0 && index < Layouts.MATCH_DATA.getRegion(matchData).numRegs;
        }
    }


    public abstract static class ValuesNode extends CoreMethodArrayArgumentsNode {

        @Child private RopeNodes.SubstringNode substringNode = RopeNodes.SubstringNode.create();
        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();
        @Child private IsTaintedNode isTaintedNode = IsTaintedNode.create();

        public static ValuesNode create() {
            return ValuesNodeFactory.create(null);
        }

        public abstract Object[] execute(DynamicObject matchData);

        @TruffleBoundary
        @Specialization
        public Object[] getValuesSlow(DynamicObject matchData) {
            final DynamicObject source = Layouts.MATCH_DATA.getSource(matchData);
            final Rope sourceRope = StringOperations.rope(source);
            final Region region = Layouts.MATCH_DATA.getRegion(matchData);
            final Object[] values = new Object[region.numRegs];
            boolean isTainted = isTaintedNode.executeIsTainted(source);

            for (int n = 0; n < region.numRegs; n++) {
                final int start = region.beg[n];
                final int end = region.end[n];

                if (start > -1 && end > -1) {
                    Rope rope = substringNode.executeSubstring(sourceRope, start, end - start);
                    DynamicObject string = allocateNode.allocate(Layouts.BASIC_OBJECT.getLogicalClass(source), Layouts.STRING.build(false, isTainted, rope));
                    values[n] = string;
                } else {
                    values[n] = nil();
                }
            }

            return values;
        }

    }

    @CoreMethod(names = "captures")
    public abstract static class CapturesNode extends CoreMethodArrayArgumentsNode {

        @Child private ValuesNode valuesNode = ValuesNode.create();

        @Specialization
        public DynamicObject toA(VirtualFrame frame, DynamicObject matchData) {
            Object[] objects = getCaptures(valuesNode.execute(matchData));
            return createArray(objects, objects.length);
        }

        private static Object[] getCaptures(Object[] values) {
            return ArrayUtils.extractRange(values, 1, values.length);
        }
    }

    @Primitive(name = "match_data_end", lowerFixnum = 1)
    public abstract static class EndNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "inBounds(matchData, index)")
        public Object end(DynamicObject matchData, int index) {
            return MatchDataNodes.end(getContext(), matchData, index);
        }

        @TruffleBoundary
        @Specialization(guards = "!inBounds(matchData, index)")
        public Object endError(DynamicObject matchData, int index) {
            throw new RaiseException(getContext(), coreExceptions().indexError(StringUtils.format("index %d out of matches", index), this));
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

    @CoreMethod(names = { "length", "size" })
    public abstract static class LengthNode extends CoreMethodArrayArgumentsNode {

        @Child private ValuesNode getValues = ValuesNode.create();

        @Specialization
        public int length(DynamicObject matchData) {
            return getValues.execute(matchData).length;
        }

    }

    @CoreMethod(names = "pre_match", taintFrom = 0)
    public abstract static class PreMatchNode extends CoreMethodArrayArgumentsNode {

        @Child private RopeNodes.SubstringNode substringNode = RopeNodes.SubstringNode.create();
        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        public abstract DynamicObject execute(DynamicObject matchData);

        @Specialization
        public Object preMatch(DynamicObject matchData) {
            DynamicObject source = Layouts.MATCH_DATA.getSource(matchData);
            Rope sourceRope = StringOperations.rope(source);
            Region region = Layouts.MATCH_DATA.getRegion(matchData);
            int start = 0;
            int length = region.beg[0];
            Rope rope = substringNode.executeSubstring(sourceRope, start, length);
            DynamicObject string = allocateNode.allocate(Layouts.BASIC_OBJECT.getLogicalClass(source), Layouts.STRING.build(false, false, rope));
            return string;
        }
    }

    @CoreMethod(names = "post_match", taintFrom = 0)
    public abstract static class PostMatchNode extends CoreMethodArrayArgumentsNode {

        @Child private RopeNodes.SubstringNode substringNode = RopeNodes.SubstringNode.create();
        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        public abstract DynamicObject execute(DynamicObject matchData);

        @Specialization
        public Object postMatch(DynamicObject matchData) {
            DynamicObject source = Layouts.MATCH_DATA.getSource(matchData);
            Rope sourceRope = StringOperations.rope(source);
            Region region = Layouts.MATCH_DATA.getRegion(matchData);
            int start = region.end[0];
            int length = sourceRope.byteLength() - region.end[0];
            Rope rope = substringNode.executeSubstring(sourceRope, start, length);
            DynamicObject string = allocateNode.allocate(Layouts.BASIC_OBJECT.getLogicalClass(source), Layouts.STRING.build(false, false, rope));
            return string;
        }
    }

    @CoreMethod(names = "to_a")
    public abstract static class ToANode extends CoreMethodArrayArgumentsNode {

        @Child ValuesNode valuesNode = ValuesNode.create();

        @Specialization
        public DynamicObject toA(DynamicObject matchData) {
            Object[] objects = ArrayUtils.copy(valuesNode.execute(matchData));
            return createArray(objects, objects.length);
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
            throw new RaiseException(getContext(), coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
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
