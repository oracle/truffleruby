/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
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

import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.InteropLibrary;
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
import org.truffleruby.core.array.ArrayOperations;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.RubyArray;
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

@CoreModule(value = "MatchData", isClass = true)
public abstract class MatchDataNodes {

    private static int getGroupBound(InteropLibrary interop, RubyMatchData matchData, String member, int group) {
        try {
            final int offset = (int) interop.invokeMember(matchData.tRegexResult, member, group);
            assert offset >= 0 || offset == RubyMatchData.MISSING;
            return offset;
        } catch (InteropException e) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            throw CompilerDirectives.shouldNotReachHere(e);
        }
    }

    private static int getStart(RubyMatchData matchData, int index, ConditionProfile lazyProfile,
            InteropLibrary interop) {
        int start = matchData.region.beg[index];
        if (lazyProfile.profile(start == RubyMatchData.LAZY)) {
            return matchData.region.beg[index] = getGroupBound(interop, matchData, "getStart", index);
        } else {
            return start;
        }
    }

    private static int getEnd(RubyMatchData matchData, int index, ConditionProfile lazyProfile,
            InteropLibrary interop) {
        int end = matchData.region.end[index];
        if (lazyProfile.profile(end == RubyMatchData.LAZY)) {
            return matchData.region.end[index] = getGroupBound(interop, matchData, "getEnd", index);
        } else {
            return end;
        }
    }

    private static void forceLazyMatchData(RubyMatchData matchData, InteropLibrary interop) {
        for (int i = 0; i < matchData.region.numRegs; i++) {
            getStart(matchData, i, ConditionProfile.getUncached(), interop);
            getEnd(matchData, i, ConditionProfile.getUncached(), interop);
        }
    }

    @TruffleBoundary
    private static Region getCharOffsetsManyRegs(RubyMatchData matchData, Rope source, Encoding encoding) {
        // Taken from org.jruby.RubyMatchData

        assert !encoding.isSingleByte() : "Should be checked by callers";

        final Region regs = matchData.region;
        int numRegs = regs.numRegs;

        if (matchData.tRegexResult != null) {
            forceLazyMatchData(matchData, InteropLibrary.getUncached(matchData.tRegexResult));
        }

        final Region charOffsets = new Region(numRegs);

        final Pair[] pairs = new Pair[numRegs * 2];
        for (int i = 0; i < pairs.length; i++) {
            pairs[i] = new Pair();
        }

        int numPos = 0;
        for (int i = 0; i < numRegs; i++) {
            if (regs.beg[i] != RubyMatchData.MISSING) {
                pairs[numPos++].bytePos = regs.beg[i];
                pairs[numPos++].bytePos = regs.end[i];
            }
        }

        updatePairs(source, encoding, pairs);

        Pair key = new Pair();
        for (int i = 0; i < regs.numRegs; i++) {
            if (regs.beg[i] == RubyMatchData.MISSING) {
                charOffsets.beg[i] = charOffsets.end[i] = RubyMatchData.MISSING;
            } else {
                key.bytePos = regs.beg[i];
                charOffsets.beg[i] = pairs[Arrays.binarySearch(pairs, key)].charPos;
                key.bytePos = regs.end[i];
                charOffsets.end[i] = pairs[Arrays.binarySearch(pairs, key)].charPos;
            }
        }

        return charOffsets;
    }

    @TruffleBoundary
    private static void updatePairs(Rope source, Encoding encoding, Pair[] pairs) {
        // Taken from org.jruby.RubyMatchData
        Arrays.sort(pairs);

        byte[] bytes = source.getBytes();
        int p = 0;
        int s = p;
        int c = 0;

        for (Pair pair : pairs) {
            int q = s + pair.bytePos;
            c += StringSupport.strLength(encoding, bytes, p, q);
            pair.charPos = c;
            p = q;
        }
    }

    @TruffleBoundary
    private static Region createCharOffsets(RubyMatchData matchData, Rope source) {
        final Encoding enc = source.getEncoding();
        final Region charOffsets = getCharOffsetsManyRegs(matchData, source, enc);
        matchData.charOffsets = charOffsets;
        return charOffsets;
    }

    private static Region getCharOffsets(RubyMatchData matchData, Rope sourceRope) {
        final Region charOffsets = matchData.charOffsets;
        if (charOffsets != null) {
            return charOffsets;
        } else {
            return createCharOffsets(matchData, sourceRope);
        }
    }

    @TruffleBoundary
    private static void fixupMatchDataForStart(RubyMatchData matchData, int startPos) {
        assert startPos != 0;
        Region regs = matchData.region;
        for (int i = 0; i < regs.beg.length; i++) {
            assert regs.beg[i] != RubyMatchData.LAZY &&
                    regs.end[i] != RubyMatchData.LAZY : "Group bounds must be computed before fixupMatchDataForStart()";
            if (regs.beg[i] >= 0) {
                regs.beg[i] += startPos;
                regs.end[i] += startPos;
            }
        }
    }

    @Primitive(name = "matchdata_fixup_positions", lowerFixnum = { 1 })
    public abstract static class FixupMatchData extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyMatchData fixupMatchData(RubyMatchData matchData, int startPos,
                @Cached ConditionProfile nonZeroPos,
                @Cached ConditionProfile lazyProfile,
                @CachedLibrary(limit = "getInteropCacheLimit()") InteropLibrary interop) {
            if (nonZeroPos.profile(startPos != 0)) {
                if (lazyProfile.profile(matchData.tRegexResult != null)) {
                    forceLazyMatchData(matchData, interop);
                }
                fixupMatchDataForStart(matchData, startPos);
            }
            return matchData;
        }
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
                    region);
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
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached ConditionProfile normalizedIndexProfile,
                @Cached ConditionProfile indexOutOfBoundsProfile,
                @Cached ConditionProfile lazyProfile,
                @CachedLibrary(limit = "getInteropCacheLimit()") InteropLibrary interop,
                @Cached ConditionProfile hasValueProfile) {

            final Region region = matchData.region;
            if (normalizedIndexProfile.profile(index < 0)) {
                index += region.numRegs;
            }

            if (indexOutOfBoundsProfile.profile(index < 0 || index >= region.numRegs)) {
                return nil();
            } else {
                final int start = getStart(matchData, index, lazyProfile, interop);
                final int end = getEnd(matchData, index, lazyProfile, interop);

                if (hasValueProfile.profile(start >= 0 && end >= 0)) {
                    final Object source = matchData.source;
                    final Rope sourceRope = strings.getRope(source);
                    final Rope rope = substringNode.executeSubstring(sourceRope, start, end - start);
                    final RubyString string = new RubyString(
                            coreLibrary().stringClass,
                            getLanguage().stringShape,
                            false,
                            rope,
                            strings.getEncoding(source));
                    AllocationTracing.trace(string, this);
                    return string;
                } else {
                    return nil();
                }
            }
        }

        @Specialization
        protected Object getIndex(RubyMatchData matchData, int index, int length,
                @Cached ConditionProfile normalizedIndexProfile,
                @Cached ConditionProfile indexOutOfBoundsProfile,
                @Cached ConditionProfile tooLargeTotalProfile) {
            final Object[] values = getValuesNode.execute(matchData);

            if (normalizedIndexProfile.profile(index < 0)) {
                index += values.length;

                if (indexOutOfBoundsProfile.profile(index < 0)) {
                    return nil();
                }
            }

            int endIndex = index + length;
            if (tooLargeTotalProfile.profile(endIndex > values.length)) {
                endIndex = values.length;
            }

            final Object[] store = Arrays.copyOfRange(values, index, endIndex);

            return createArray(store);
        }

        @Specialization(
                guards = {
                        "nameEntry != null",
                        "getRegexp(matchData) == cachedRegexp",
                        "symbol == cachedSymbol" })
        protected Object getIndexSymbolKnownRegexp(RubyMatchData matchData, RubySymbol symbol, NotProvided length,
                @Cached("symbol") RubySymbol cachedSymbol,
                @Cached("getRegexp(matchData)") RubyRegexp cachedRegexp,
                @Cached("findNameEntry(cachedRegexp, cachedSymbol)") NameEntry nameEntry,
                @Cached("numBackRefs(nameEntry)") int backRefs,
                @Cached("backRefIndex(nameEntry)") int backRefIndex,
                @Cached ConditionProfile lazyProfile,
                @CachedLibrary(limit = "getInteropCacheLimit()") InteropLibrary libInterop) {
            if (backRefs == 1) {
                return executeGetIndex(matchData, backRefIndex, NotProvided.INSTANCE);
            } else {
                final int i = getBackRef(matchData, cachedRegexp, cachedSymbol.getRope(), lazyProfile, libInterop);
                return executeGetIndex(matchData, i, NotProvided.INSTANCE);
            }
        }

        @Specialization
        protected Object getIndexSymbol(RubyMatchData matchData, RubySymbol symbol, NotProvided length,
                @Cached ConditionProfile lazyProfile,
                @CachedLibrary(limit = "getInteropCacheLimit()") InteropLibrary libInterop) {
            return executeGetIndex(
                    matchData,
                    getBackRef(matchData, getRegexp(matchData), symbol.getRope(), lazyProfile, libInterop),
                    NotProvided.INSTANCE);
        }

        @Specialization(guards = "libIndex.isRubyString(index)")
        protected Object getIndexString(RubyMatchData matchData, Object index, NotProvided length,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libIndex,
                @Cached ConditionProfile lazyProfile,
                @CachedLibrary(limit = "getInteropCacheLimit()") InteropLibrary libInterop) {
            return executeGetIndex(
                    matchData,
                    getBackRef(matchData, getRegexp(matchData), libIndex.getRope(index), lazyProfile, libInterop),
                    NotProvided.INSTANCE);
        }

        @Specialization(
                guards = {
                        "!isInteger(index)",
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

        private int getBackRef(RubyMatchData matchData, RubyRegexp regexp, Rope name,
                ConditionProfile lazyProfile, InteropLibrary libInterop) {
            if (lazyProfile.profile(matchData.tRegexResult != null)) {
                // force the calculation of lazy capture group results before invoking nameToBackrefNumber()
                forceLazyMatchData(matchData, libInterop);
            }
            return nameToBackrefNumber(matchData, regexp, name);
        }

        @TruffleBoundary
        private int nameToBackrefNumber(RubyMatchData matchData, RubyRegexp regexp, Rope name) {
            try {
                return regexp.regex.nameToBackrefNumber(
                        name.getBytes(),
                        0,
                        name.byteLength(),
                        matchData.region);
            } catch (ValueException e) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().indexError(
                                StringUtils
                                        .format("undefined group name reference: %s", RopeOperations.decodeRope(name)),
                                this));
            }
        }

        @TruffleBoundary
        protected static int numBackRefs(NameEntry nameEntry) {
            return nameEntry == null ? 0 : nameEntry.getBackRefs().length;
        }

        @TruffleBoundary
        protected static int backRefIndex(NameEntry nameEntry) {
            return nameEntry == null ? 0 : nameEntry.getBackRefs()[0];
        }

        @TruffleBoundary
        private static boolean bytesEqual(byte[] bytes, int byteLength, byte[] name, int nameP, int nameEnd) {
            if (bytes == name && nameP == 0 && byteLength == nameEnd) {
                return true;
            } else if (nameEnd - nameP != byteLength) {
                return false;
            } else {
                return ArrayUtils.regionEquals(bytes, 0, name, nameP, byteLength);
            }
        }
    }

    @Primitive(name = "match_data_begin", lowerFixnum = 1)
    public abstract static class BeginNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "inBounds(matchData, index)")
        protected Object begin(RubyMatchData matchData, int index,
                @Cached ConditionProfile lazyProfile,
                @Cached ConditionProfile negativeBeginProfile,
                @Cached ConditionProfile multiByteCharacterProfile,
                @Cached RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @CachedLibrary(limit = "getInteropCacheLimit()") InteropLibrary interop) {
            final int begin = getStart(matchData, index, lazyProfile, interop);

            if (negativeBeginProfile.profile(begin < 0)) {
                return nil();
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
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached ConditionProfile lazyProfile,
                @CachedLibrary(limit = "getInteropCacheLimit()") InteropLibrary interop,
                @Cached ConditionProfile hasValueProfile,
                @Cached LoopConditionProfile loopProfile) {
            final Object source = matchData.source;
            final Rope sourceRope = strings.getRope(source);
            final Region region = matchData.region;
            final Object[] values = new Object[region.numRegs];

            int n = 0;
            try {
                for (; loopProfile.inject(n < region.numRegs); n++) {
                    final int start = getStart(matchData, n, lazyProfile, interop);
                    final int end = getEnd(matchData, n, lazyProfile, interop);

                    if (hasValueProfile.profile(start >= 0 && end >= 0)) {
                        final Rope rope = substringNode.executeSubstring(sourceRope, start, end - start);
                        final RubyString string = new RubyString(
                                coreLibrary().stringClass,
                                getLanguage().stringShape,
                                false,
                                rope,
                                strings.getEncoding(source));
                        AllocationTracing.trace(string, this);
                        values[n] = string;
                    } else {
                        values[n] = nil();
                    }

                    TruffleSafepoint.poll(this);
                }
            } finally {
                profileAndReportLoopCount(loopProfile, n);
            }

            return values;
        }

    }

    @Primitive(name = "match_data_end", lowerFixnum = 1)
    public abstract static class EndNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "inBounds(matchData, index)")
        protected Object end(RubyMatchData matchData, int index,
                @Cached ConditionProfile lazyProfile,
                @Cached ConditionProfile negativeEndProfile,
                @Cached ConditionProfile multiByteCharacterProfile,
                @Cached RopeNodes.SingleByteOptimizableNode singleByteOptimizableNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @CachedLibrary(limit = "getInteropCacheLimit()") InteropLibrary interop) {
            final int end = getEnd(matchData, index, lazyProfile, interop);

            if (negativeEndProfile.profile(end < 0)) {
                return nil();
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
        protected Object byteBegin(RubyMatchData matchData, int index,
                @Cached ConditionProfile lazyProfile,
                @Cached ConditionProfile negativeBeginProfile,
                @CachedLibrary(limit = "getInteropCacheLimit()") InteropLibrary interop) {
            final int begin = getStart(matchData, index, lazyProfile, interop);

            if (negativeBeginProfile.profile(begin < 0)) {
                return nil();
            } else {
                return begin;
            }
        }

        protected boolean inBounds(RubyMatchData matchData, int index) {
            return index >= 0 && index < matchData.region.numRegs;
        }
    }

    @Primitive(name = "match_data_byte_end", lowerFixnum = 1)
    public abstract static class ByteEndNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "inBounds(matchData, index)")
        protected Object byteEnd(RubyMatchData matchData, int index,
                @Cached ConditionProfile lazyProfile,
                @Cached ConditionProfile negativeEndProfile,
                @CachedLibrary(limit = "getInteropCacheLimit()") InteropLibrary interop) {
            final int end = getEnd(matchData, index, lazyProfile, interop);

            if (negativeEndProfile.profile(end < 0)) {
                return nil();
            } else {
                return end;
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
                @Cached ConditionProfile lazyProfile,
                @CachedLibrary(limit = "getInteropCacheLimit()") InteropLibrary interop,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            Object source = matchData.source;
            Rope sourceRope = strings.getRope(source);
            final int length = getStart(matchData, 0, lazyProfile, interop);
            final Rope rope = substringNode.executeSubstring(sourceRope, 0, length);
            final RubyString string = new RubyString(
                    coreLibrary().stringClass,
                    getLanguage().stringShape,
                    false,
                    rope,
                    strings.getEncoding(source));
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
                @Cached ConditionProfile lazyProfile,
                @CachedLibrary(limit = "getInteropCacheLimit()") InteropLibrary interop,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            Object source = matchData.source;
            Rope sourceRope = strings.getRope(source);
            final int start = getEnd(matchData, 0, lazyProfile, interop);
            int length = sourceRope.byteLength() - start;
            Rope rope = substringNode.executeSubstring(sourceRope, start, length);
            final RubyString string = new RubyString(
                    coreLibrary().stringClass,
                    getLanguage().stringShape,
                    false,
                    rope,
                    strings.getEncoding(source));
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
            RubyMatchData matchData = new RubyMatchData(rubyClass, shape, null, null, null);
            AllocationTracing.trace(matchData, this);
            return matchData;
        }

    }

    @CoreMethod(names = "initialize_copy", required = 1, raiseIfFrozenSelf = true)
    public abstract static class InitializeCopyNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyMatchData initializeCopy(RubyMatchData self, RubyMatchData from,
                @Cached ConditionProfile copyFromSelfProfile) {
            if (copyFromSelfProfile.profile(self == from)) {
                return self;
            }

            self.source = from.source;
            self.regexp = from.regexp;
            self.region = from.region;
            self.charOffsets = from.charOffsets;
            self.tRegexResult = from.tRegexResult;
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
