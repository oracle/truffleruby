/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.regexp;

import java.util.Arrays;
import java.util.Iterator;

import com.oracle.truffle.api.nodes.LoopNode;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.jcodings.Encoding;
import org.joni.NameEntry;
import org.joni.Regex;
import org.joni.Region;
import org.joni.exception.ValueException;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.array.ArrayIndexNodes;
import org.truffleruby.core.array.ArrayOperations;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.IntegerCastNode;
import org.truffleruby.core.cast.ToIntNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.range.RubyIntRange;
import org.truffleruby.core.regexp.MatchDataNodesFactory.ValuesNodeFactory;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringSupport;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.library.RubyStringLibrary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.objects.LogicalClassNode;

@CoreModule(value = "MatchData", isClass = true)
public abstract class MatchDataNodes {

    @TruffleBoundary
    private static void updatePairs(Rope source, Encoding encoding, Pair[] pairs) {
        // Taken from org.jruby.RubyMatchData
        Arrays.sort(pairs);

        int length = pairs.length;
        byte[] bytes = source.getBytes();
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

    private static Region getCharOffsetsManyRegs(RubyMatchData matchData, Rope source, Encoding encoding) {
        // Taken from org.jruby.RubyMatchData
        final Region regs = matchData.region;
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

    public static Region getCharOffsets(RubyMatchData matchData, Rope sourceRope) {
        // Taken from org.jruby.RubyMatchData
        final Region charOffsets = matchData.charOffsets;

        if (charOffsets != null) {
            return charOffsets;
        } else {
            return createCharOffsets(matchData, sourceRope);
        }
    }

    @TruffleBoundary
    private static Region createCharOffsets(RubyMatchData matchData, Rope source) {
        final Encoding enc = source.getEncoding();
        final Region charOffsets = getCharOffsetsManyRegs(matchData, source, enc);
        matchData.charOffsets = charOffsets;
        return charOffsets;
    }

    @Primitive(name = "matchdata_create_single_group", lowerFixnum = { 2, 3 })
    public abstract static class MatchDataCreateSingleGroupNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object create(Object regexp, Object string, int start, int end) {
            final Region region = new Region(start, end);
            RubyMatchData matchData = new RubyMatchData(
                    coreLibrary().matchDataClass,
                    getLanguage().matchDataShape,
                    regexp,
                    string,
                    region,
                    null);
            AllocationTracing.trace(matchData, this);
            return matchData;
        }

    }

    @Primitive(name = "matchdata_create")
    public abstract static class MatchDataCreateNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object create(Object regexp, Object string, RubyArray starts, RubyArray ends,
                @Cached ArrayIndexNodes.ReadNormalizedNode readNode,
                @Cached IntegerCastNode integerCastNode) {
            final Region region = new Region(starts.size);
            for (int i = 0; i < region.numRegs; i++) {
                region.beg[i] = integerCastNode.executeCastInt(readNode.executeRead(starts, i));
                region.end[i] = integerCastNode.executeCastInt(readNode.executeRead(ends, i));
            }

            RubyMatchData matchData = new RubyMatchData(
                    coreLibrary().matchDataClass,
                    getLanguage().matchDataShape,
                    regexp,
                    string,
                    region,
                    null);
            AllocationTracing.trace(matchData, this);
            return matchData;
        }

    }

    @CoreMethod(
            names = "[]",
            required = 1,
            optional = 1,
            lowerFixnum = { 1, 2 },
            argumentNames = { "index_start_range_or_name", "length" })
    public abstract static class GetIndexNode extends CoreMethodArrayArgumentsNode {

        @Child private RegexpNode regexpNode;
        @Child private ValuesNode getValuesNode = ValuesNode.create();
        @Child private RopeNodes.SubstringNode substringNode = RopeNodes.SubstringNode.create();

        public static GetIndexNode create(RubyNode... nodes) {
            return MatchDataNodesFactory.GetIndexNodeFactory.create(nodes);
        }

        protected abstract Object executeGetIndex(Object matchData, int index, NotProvided length);

        @Specialization
        protected Object getIndex(RubyMatchData matchData, int index, NotProvided length,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached ConditionProfile normalizedIndexProfile,
                @Cached ConditionProfile indexOutOfBoundsProfile,
                @Cached ConditionProfile hasValueProfile,
                @Cached LogicalClassNode logicalClassNode) {

            final Region region = matchData.region;
            if (normalizedIndexProfile.profile(index < 0)) {
                index += region.beg.length;
            }

            if (indexOutOfBoundsProfile.profile((index < 0) || (index >= region.beg.length))) {
                return nil;
            } else {
                final Object source = matchData.source;
                final Rope sourceRope = strings.getRope(source);
                final int start = region.beg[index];
                final int end = region.end[index];

                if (hasValueProfile.profile(start > -1 && end > -1)) {
                    Rope rope = substringNode.executeSubstring(sourceRope, start, end - start);
                    final RubyClass logicalClass = logicalClassNode.execute(source);
                    final RubyString string = new RubyString(logicalClass, getLanguage().stringShape, false, rope);
                    AllocationTracing.trace(string, this);
                    return string;
                } else {
                    return nil;
                }
            }
        }

        @Specialization
        protected RubyArray getIndex(RubyMatchData matchData, int index, int length,
                @Cached ConditionProfile normalizedIndexProfile) {
            // TODO BJF 15-May-2015 Need to handle negative indexes and lengths and out of bounds
            final Object[] values = getValuesNode.execute(matchData);
            if (normalizedIndexProfile.profile(index < 0)) {
                index += values.length;
            }
            final Object[] store = Arrays.copyOfRange(values, index, index + length);
            return createArray(store);
        }

        @Specialization(
                guards = {
                        "name != null",
                        "getRegexp(matchData) == regexp",
                        "cachedIndex == index" })
        protected Object getIndexSymbolSingleMatch(RubyMatchData matchData, RubySymbol index, NotProvided length,
                @Cached("index") RubySymbol cachedIndex,
                @Cached("getRegexp(matchData)") RubyRegexp regexp,
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

        @Specialization
        protected Object getIndexSymbol(RubyMatchData matchData, RubySymbol index, NotProvided length) {
            return executeGetIndex(matchData, getBackRefFromSymbol(matchData, index), NotProvided.INSTANCE);
        }

        @Specialization(guards = "libIndex.isRubyString(index)")
        protected Object getIndexString(RubyMatchData matchData, Object index, NotProvided length,
                @CachedLibrary(limit = "2") RubyStringLibrary libIndex) {
            return executeGetIndex(
                    matchData,
                    getBackRefFromRope(matchData, libIndex.getRope(index)),
                    NotProvided.INSTANCE);
        }

        @Specialization(
                guards = {
                        "!isImplicitInteger(index)",
                        "!isRubySymbol(index)",
                        "isNotRubyString(index)",
                        "!isIntRange(index)" })
        protected Object getIndexCoerce(RubyMatchData matchData, Object index, NotProvided length,
                @Cached ToIntNode toIntNode) {
            return executeGetIndex(matchData, toIntNode.execute(index), NotProvided.INSTANCE);
        }

        @TruffleBoundary
        @Specialization
        protected RubyArray getIndex(RubyMatchData matchData, RubyIntRange range, NotProvided len) {
            final Object[] values = getValuesNode.execute(matchData);
            int index = range.begin;
            if (range.begin < 0) {
                index += values.length;
            }
            int end = range.end;
            if (end < 0) {
                end += values.length;
            }
            final int exclusiveEnd = ArrayOperations
                    .clampExclusiveIndex(values.length, range.excludedEnd ? end : end + 1);
            final int length = exclusiveEnd - index;

            return createArray(Arrays.copyOfRange(values, index, index + length));
        }

        @TruffleBoundary
        protected static NameEntry findNameEntry(RubyRegexp regexp, RubySymbol symbol) {
            Regex regex = regexp.regex;

            if (regex.numberOfNames() > 0) {
                Rope rope = symbol.getRope();

                for (Iterator<NameEntry> i = regex.namedBackrefIterator(); i.hasNext();) {
                    final NameEntry e = i.next();

                    if (bytesEqual(rope.getBytes(), rope.byteLength(), e.name, e.nameP, e.nameEnd)) {
                        return e;
                    }
                }
            }
            return null;
        }

        protected RubyRegexp getRegexp(RubyMatchData matchData) {
            if (regexpNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                regexpNode = insert(RegexpNode.create());
            }
            return regexpNode.executeGetRegexp(matchData);
        }

        @TruffleBoundary
        private int getBackRefFromSymbol(RubyMatchData matchData, RubySymbol index) {
            return getBackRefFromRope(matchData, index.getRope());
        }

        @TruffleBoundary
        private int getBackRefFromRope(RubyMatchData matchData, Rope value) {
            try {
                return getRegexp(matchData).regex.nameToBackrefNumber(
                        value.getBytes(),
                        0,
                        value.byteLength(),
                        matchData.region);
            } catch (ValueException e) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().indexError(
                                StringUtils
                                        .format("undefined group name reference: %s", RopeOperations.decodeRope(value)),
                                this));
            }
        }

        @TruffleBoundary
        private int getBackRef(RubyMatchData matchData, RubyRegexp regexp, NameEntry name) {
            return regexp.regex.nameToBackrefNumber(
                    name.name,
                    name.nameP,
                    name.nameEnd,
                    matchData.region);
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
        protected Object begin(RubyMatchData matchData, int index,
                @Cached ConditionProfile negativeBeginProfile,
                @Cached ConditionProfile multiByteCharacterProfile,
                @Cached RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            // Taken from org.jruby.RubyMatchData.

            final int begin = matchData.region.beg[index];
            if (negativeBeginProfile.profile(begin < 0)) {
                return nil;
            }

            final Rope matchDataSourceRope = strings.getRope(matchData.source);
            if (multiByteCharacterProfile.profile(!singleByteOptimizableNode.execute(matchDataSourceRope))) {
                return getCharOffsets(matchData, matchDataSourceRope).beg[index];
            }

            return begin;
        }

        @TruffleBoundary
        @Specialization(guards = "!inBounds(matchData, index)")
        protected Object beginError(RubyMatchData matchData, int index) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().indexError(StringUtils.format("index %d out of matches", index), this));
        }

        protected boolean inBounds(RubyMatchData matchData, int index) {
            return index >= 0 && index < matchData.region.numRegs;
        }
    }


    public abstract static class ValuesNode extends CoreMethodArrayArgumentsNode {

        @Child private RopeNodes.SubstringNode substringNode = RopeNodes.SubstringNode.create();

        public static ValuesNode create() {
            return ValuesNodeFactory.create(null);
        }

        public abstract Object[] execute(RubyMatchData matchData);

        @Specialization
        protected Object[] getValues(RubyMatchData matchData,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached ConditionProfile hasValueProfile,
                @Cached LoopConditionProfile loopProfile,
                @Cached LogicalClassNode logicalClassNode) {
            final Object source = matchData.source;
            final Rope sourceRope = strings.getRope(source);
            final Region region = matchData.region;
            final Object[] values = new Object[region.numRegs];

            try {
                loopProfile.profileCounted(region.numRegs);

                for (int n = 0; loopProfile.inject(n < region.numRegs); n++) {
                    final int start = region.beg[n];
                    final int end = region.end[n];

                    if (hasValueProfile.profile(start > -1 && end > -1)) {
                        final Rope rope = substringNode.executeSubstring(sourceRope, start, end - start);
                        final RubyClass logicalClass = logicalClassNode.execute(source);
                        final RubyString string = new RubyString(logicalClass, getLanguage().stringShape, false, rope);
                        AllocationTracing.trace(string, this);
                        values[n] = string;
                    } else {
                        values[n] = nil;
                    }
                }
            } finally {
                LoopNode.reportLoopCount(this, region.numRegs);
            }

            return values;
        }

    }

    @Primitive(name = "match_data_end", lowerFixnum = 1)
    public abstract static class EndNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "inBounds(matchData, index)")
        protected Object end(RubyMatchData matchData, int index,
                @Cached ConditionProfile negativeEndProfile,
                @Cached ConditionProfile multiByteCharacterProfile,
                @Cached RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode,
                @CachedLibrary(limit = "2") RubyStringLibrary strings) {
            // Taken from org.jruby.RubyMatchData.

            final int end = matchData.region.end[index];
            if (negativeEndProfile.profile(end < 0)) {
                return nil;
            }

            final Rope matchDataSourceRope = strings.getRope(matchData.source);
            if (multiByteCharacterProfile.profile(!singleByteOptimizableNode.execute(matchDataSourceRope))) {
                return getCharOffsets(matchData, matchDataSourceRope).end[index];
            }

            return end;
        }

        @TruffleBoundary
        @Specialization(guards = "!inBounds(matchData, index)")
        protected Object endError(RubyMatchData matchData, int index) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().indexError(StringUtils.format("index %d out of matches", index), this));
        }

        protected boolean inBounds(RubyMatchData matchData, int index) {
            return index >= 0 && index < matchData.region.numRegs;
        }
    }

    @Primitive(name = "match_data_byte_begin", lowerFixnum = 1)
    public abstract static class ByteBeginNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "inBounds(matchData, index)")
        protected Object byteBegin(RubyMatchData matchData, int index) {
            int b = matchData.region.beg[index];
            if (b < 0) {
                return nil;
            } else {
                return b;
            }
        }

        protected boolean inBounds(RubyMatchData matchData, int index) {
            return index >= 0 && index < matchData.region.numRegs;
        }
    }

    @Primitive(name = "match_data_byte_end", lowerFixnum = 1)
    public abstract static class ByteEndNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "inBounds(matchData, index)")
        protected Object byteEnd(RubyMatchData matchData, int index) {
            int e = matchData.region.end[index];
            if (e < 0) {
                return nil;
            } else {
                return e;
            }
        }

        protected boolean inBounds(RubyMatchData matchData, int index) {
            return index >= 0 && index < matchData.region.numRegs;
        }
    }

    @CoreMethod(names = { "length", "size" })
    public abstract static class LengthNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int length(RubyMatchData matchData) {
            return matchData.region.numRegs;
        }

    }

    @CoreMethod(names = "pre_match")
    public abstract static class PreMatchNode extends CoreMethodArrayArgumentsNode {

        @Child private RopeNodes.SubstringNode substringNode = RopeNodes.SubstringNode.create();

        public abstract RubyString execute(RubyMatchData matchData);

        @Specialization
        protected RubyString preMatch(RubyMatchData matchData,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached LogicalClassNode logicalClassNode) {
            Object source = matchData.source;
            Rope sourceRope = strings.getRope(source);
            Region region = matchData.region;
            int start = 0;
            int length = region.beg[0];
            Rope rope = substringNode.executeSubstring(sourceRope, start, length);
            final RubyClass logicalClass = logicalClassNode.execute(source);
            final RubyString string = new RubyString(logicalClass, getLanguage().stringShape, false, rope);
            AllocationTracing.trace(string, this);
            return string;
        }
    }

    @CoreMethod(names = "post_match")
    public abstract static class PostMatchNode extends CoreMethodArrayArgumentsNode {

        @Child private RopeNodes.SubstringNode substringNode = RopeNodes.SubstringNode.create();

        public abstract RubyString execute(RubyMatchData matchData);

        @Specialization
        protected RubyString postMatch(RubyMatchData matchData,
                @CachedLibrary(limit = "2") RubyStringLibrary strings,
                @Cached LogicalClassNode logicalClassNode) {
            Object source = matchData.source;
            Rope sourceRope = strings.getRope(source);
            Region region = matchData.region;
            int start = region.end[0];
            int length = sourceRope.byteLength() - region.end[0];
            Rope rope = substringNode.executeSubstring(sourceRope, start, length);
            final RubyClass logicalClass = logicalClassNode.execute(source);
            final RubyString string = new RubyString(logicalClass, getLanguage().stringShape, false, rope);
            AllocationTracing.trace(string, this);
            return string;
        }
    }

    @CoreMethod(names = "to_a")
    public abstract static class ToANode extends CoreMethodArrayArgumentsNode {

        @Child ValuesNode valuesNode = ValuesNode.create();

        @Specialization
        protected RubyArray toA(RubyMatchData matchData) {
            Object[] objects = valuesNode.execute(matchData);
            return createArray(objects);
        }
    }

    @CoreMethod(names = "regexp")
    public abstract static class RegexpNode extends CoreMethodArrayArgumentsNode {

        public static RegexpNode create() {
            return MatchDataNodesFactory.RegexpNodeFactory.create(null);
        }

        public abstract RubyRegexp executeGetRegexp(RubyMatchData matchData);

        @Specialization
        protected RubyRegexp regexp(RubyMatchData matchData,
                @Cached ConditionProfile profile,
                @Cached DispatchNode stringToRegexp) {
            final Object value = matchData.regexp;

            if (profile.profile(value instanceof RubyRegexp)) {
                return (RubyRegexp) value;
            } else {
                final RubyRegexp regexp = (RubyRegexp) stringToRegexp.call(
                        coreLibrary().truffleTypeModule,
                        "coerce_to_regexp",
                        value,
                        true);
                matchData.regexp = regexp;
                return regexp;
            }
        }

    }

    // Defined only so that #initialize_copy works for #dup and #clone.
    // MatchData.allocate is undefined, see regexp.rb.
    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class InternalAllocateNode extends UnaryCoreMethodNode {

        @Specialization
        protected RubyMatchData allocate(RubyClass rubyClass) {
            final Shape shape = getLanguage().matchDataShape;
            RubyMatchData matchData = new RubyMatchData(rubyClass, shape, null, null, null, null);
            AllocationTracing.trace(matchData, this);
            return matchData;
        }

    }

    @CoreMethod(names = "initialize_copy", required = 1, raiseIfFrozenSelf = true)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyMatchData initializeCopy(RubyMatchData self, RubyMatchData from) {
            if (self == from) {
                return self;
            }

            self.source = from.source;
            self.regexp = from.regexp;
            self.region = from.region;
            self.charOffsets = from.charOffsets;
            return self;
        }

        @Specialization(guards = "!isRubyMatchData(from)")
        protected RubyMatchData initializeCopy(RubyMatchData self, Object from) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().typeError("initialize_copy should take same class object", this));
        }
    }

    @Primitive(name = "match_data_get_source")
    public abstract static class GetSourceNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object getSource(RubyMatchData matchData) {
            return matchData.source;
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
