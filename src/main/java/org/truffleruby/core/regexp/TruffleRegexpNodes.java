/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.regexp;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.joni.Matcher;
import org.joni.Option;
import org.joni.Regex;
import org.joni.Region;
import org.joni.Syntax;
import org.joni.exception.SyntaxException;
import org.joni.exception.ValueException;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.collections.ConcurrentOperations;
import org.truffleruby.core.array.ArrayBuilderNode;
import org.truffleruby.core.array.ArrayBuilderNode.BuilderState;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.TaintResultNode;
import org.truffleruby.core.encoding.EncodingNodes;
import org.truffleruby.core.hash.ReHashable;
import org.truffleruby.core.kernel.KernelNodes.SameOrEqualNode;
import org.truffleruby.core.regexp.RegexpNodes.ToSNode;
import org.truffleruby.core.regexp.TruffleRegexpNodesFactory.MatchNodeGen;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeBuilder;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringNodes.StringAppendPrimitiveNode;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.objects.AllocateHelperNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreModule("Truffle::RegexpOperations")
public class TruffleRegexpNodes {

    @CoreMethod(names = "union", onSingleton = true, required = 2, rest = true)
    public static abstract class RegexpUnionNode extends CoreMethodArrayArgumentsNode {

        @Child StringAppendPrimitiveNode appendNode = StringAppendPrimitiveNode.create();
        @Child ToSNode toSNode = ToSNode.create();
        @Child CallDispatchHeadNode copyNode = CallDispatchHeadNode.createPrivate();
        @Child private SameOrEqualNode sameOrEqualNode = SameOrEqualNode.create();
        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @Specialization(guards = "argsMatch(frame, cachedArgs, args)", limit = "getDefaultCacheLimit()")
        protected Object executeFastUnion(VirtualFrame frame, RubyString str, RubyString sep, Object[] args,
                @Cached(value = "args", dimensions = 1) Object[] cachedArgs,
                @Cached("buildUnion(str, sep, args)") RubyRegexp union) {
            return copyNode.call(union, "clone");
        }

        @Specialization(replaces = "executeFastUnion")
        protected Object executeSlowUnion(RubyString str, RubyString sep, Object[] args) {
            return buildUnion(str, sep, args);
        }

        public RubyRegexp buildUnion(RubyString str, RubyString sep, Object[] args) {
            RubyString regexpString = null;
            for (int i = 0; i < args.length; i++) {
                if (regexpString == null) {
                    regexpString = appendNode.executeStringAppend(str, string(args[i]));
                } else {
                    regexpString = appendNode.executeStringAppend(regexpString, sep);
                    regexpString = appendNode.executeStringAppend(regexpString, string(args[i]));
                }
            }
            return createRegexp(regexpString.rope);
        }

        public RubyString string(Object obj) {
            if (obj instanceof RubyString) {
                final Rope rope = ((RubyString) obj).rope;
                return makeStringNode.fromRope(ClassicRegexp.quote19(rope));
            } else {
                return toSNode.execute((RubyRegexp) obj);
            }
        }

        @ExplodeLoop
        protected boolean argsMatch(VirtualFrame frame, Object[] cachedArgs, Object[] args) {
            if (cachedArgs.length != args.length) {
                return false;
            } else {
                for (int i = 0; i < cachedArgs.length; i++) {
                    if (!sameOrEqualNode.executeSameOrEqual(cachedArgs[i], args[i])) {
                        return false;
                    }
                }
                return true;
            }
        }

        @TruffleBoundary
        public RubyRegexp createRegexp(Rope pattern) {
            final RegexpOptions regexpOptions = RegexpOptions.fromEmbeddedOptions(0);
            final Regex regex = compile(getContext(), pattern, regexpOptions, this);

            return new RubyRegexp(
                    getContext().getCoreLibrary().regexpShape,
                    regex,
                    (Rope) regex.getUserObject(),
                    regexpOptions,
                    new EncodingCache());
        }
    }

    @CoreMethod(names = "search_region", onSingleton = true, required = 5, lowerFixnum = { 3, 4 })
    @ImportStatic(RegexpGuards.class)
    public static abstract class RegexpSearchRegionNode extends CoreMethodArrayArgumentsNode {

        @Child RopeNodes.CodeRangeNode rangeNode = RopeNodes.CodeRangeNode.create();

        @Specialization(guards = { "!isInitialized(regexp)" })
        protected Object notInitialized(RubyRegexp regexp, RubyString string, int start, int end, boolean forward) {
            throw new RaiseException(getContext(), coreExceptions().typeError("uninitialized Regexp", this));
        }

        @Specialization(guards = { "!isValidEncoding(string, rangeNode)" })
        protected Object invalidEncoding(RubyRegexp regexp, RubyString string, int start, int end, boolean forward) {
            throw new RaiseException(getContext(), coreExceptions().argumentError(formatError(string), this));
        }

        @TruffleBoundary
        private String formatError(RubyString string) {
            return StringUtils.format("invalid byte sequence in %s", string.rope.getEncoding());
        }

        @Specialization(
                guards = { "isInitialized(regexp)", "isValidEncoding(string, rangeNode)" })
        protected Object searchRegion(RubyRegexp regexp, RubyString string, int start, int end, boolean forward,
                @Cached ConditionProfile forwardSearchProfile,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached TruffleRegexpNodes.MatchNode matchNode,
                @Cached EncodingNodes.CheckEncodingNode checkEncodingNode) {
            checkEncodingNode.executeCheckEncoding(regexp, string);

            final Rope rope = string.rope;
            final Matcher matcher = RegexpNodes
                .createMatcher(getContext(), regexp, rope, bytesNode.execute(rope), true, 0, this);

            if (forwardSearchProfile.profile(forward)) {
                // Search forward through the string.
                return matchNode.execute(regexp, string, matcher, start, end, false);
            } else {
                // Search backward through the string.
                return matchNode.execute(regexp, string, matcher, end, start, false);
            }
        }

    }

    public static abstract class RegexpStatsNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        protected <T> RubyArray fillinInstrumentData(Map<T, AtomicInteger> map, ArrayBuilderNode arrayBuilderNode,
                RubyContext context) {
            BuilderState state = arrayBuilderNode.start(compiledRegexps.size() * 2);
            int n = 0;
            for (Entry<T, AtomicInteger> e : map.entrySet()) {
                Rope key = StringOperations.encodeRope(e.getKey().toString(), UTF8Encoding.INSTANCE);
                arrayBuilderNode.appendValue(state, n++, StringOperations.createString(context, key));
                arrayBuilderNode.appendValue(state, n++, e.getValue().get());
            }
            return createArray(arrayBuilderNode.finish(state, n), n);
        }
    }

    @CoreMethod(names = "compilation_stats_array", onSingleton = true, required = 0)
    public static abstract class CompilationStatsArrayNode extends RegexpStatsNode {

        @Specialization
        protected Object buildStatsArray(
                @Cached ArrayBuilderNode arrayBuilderNode) {
            return fillinInstrumentData(compiledRegexps, arrayBuilderNode, getContext());
        }
    }

    @CoreMethod(names = "match_stats_array", onSingleton = true, required = 0)
    public static abstract class MatchStatsArrayNode extends RegexpStatsNode {

        @Specialization
        protected Object buildStatsArray(
                @Cached ArrayBuilderNode arrayBuilderNode) {
            return fillinInstrumentData(matchedRegexps, arrayBuilderNode, getContext());
        }
    }

    public static abstract class MatchNode extends RubyContextNode {

        @Child private TaintResultNode taintResultNode = new TaintResultNode();
        @Child private AllocateHelperNode allocateNode = AllocateHelperNode.create();
        @Child private CallDispatchHeadNode dupNode = CallDispatchHeadNode.createPrivate();

        public static MatchNode create() {
            return MatchNodeGen.create();
        }

        public abstract Object execute(RubyRegexp regexp, RubyString string, Matcher matcher,
                int startPos, int range, boolean onlyMatchAtStart);

        // Creating a MatchData will store a copy of the source string. It's tempting to use a rope here, but a bit
        // inconvenient because we can't work with ropes directly in Ruby and some MatchData methods are nicely
        // implemented using the source string data. Likewise, we need to taint objects based on the source string's
        // taint state. We mustn't allow the source string's contents to change, however, so we must ensure that we have
        // a private copy of that string. Since the source string would otherwise be a reference to string held outside
        // the MatchData object, it would be possible for the source string to be modified externally.
        //
        // Ex. x = "abc"; x =~ /(.*)/; x.upcase!
        //
        // Without a private copy, the MatchData's source could be modified to be upcased when it should remain the
        // same as when the MatchData was created.
        @Specialization
        protected Object executeMatch(
                RubyRegexp regexp,
                RubyString string,
                Matcher matcher,
                int startPos,
                int range,
                boolean onlyMatchAtStart,
                @Cached ConditionProfile matchesProfile) {
            if (getContext().getOptions().REGEXP_INSTRUMENT_MATCH) {
                instrument(regexp, string, onlyMatchAtStart);
            }

            int match = runMatch(matcher, startPos, range, onlyMatchAtStart);

            if (matchesProfile.profile(match == -1)) {
                return nil;
            }

            assert match >= 0;

            final Region region = matcher.getEagerRegion();
            final RubyString dupedString = (RubyString) dupNode.call(string, "dup");
            RubyMatchData result = new RubyMatchData(coreLibrary().matchDataShape, regexp, dupedString, region, null);
            allocateNode.trace(result, this);
            return taintResultNode.maybeTaint(string, result);
        }

        @TruffleBoundary
        private int runMatch(Matcher matcher, int startPos, int range, boolean onlyMatchAtStart) {
            // Keep status as RUN because MRI has an uninterruptible Regexp engine
            if (onlyMatchAtStart) {
                return getContext().getThreadManager().runUntilResultKeepStatus(
                        this,
                        () -> matcher.matchInterruptible(startPos, range, Option.DEFAULT));
            } else {
                return getContext().getThreadManager().runUntilResultKeepStatus(
                        this,
                        () -> matcher.searchInterruptible(startPos, range, Option.DEFAULT));
            }
        }

        @TruffleBoundary
        protected void instrument(RubyRegexp regexp, RubyString string, boolean fromStart) {
            Rope source = regexp.source;
            Encoding enc = string.rope.getEncoding();
            RegexpOptions options = regexp.options;
            MatchInfo matchInfo = new MatchInfo(
                    new RegexpCacheKey(
                            source,
                            enc,
                            options.toJoniOptions(),
                            getContext().getHashing(REHASH_MATCHED_REGEXPS)),
                    fromStart);
            ConcurrentOperations.getOrCompute(matchedRegexps, matchInfo, x -> new AtomicInteger()).incrementAndGet();
        }
    }

    private static class MatchInfo {
        private final RegexpCacheKey regexpInfo;
        private final boolean matchStart;

        MatchInfo(RegexpCacheKey regexpInfo, boolean matchStart) {
            assert regexpInfo != null;
            this.regexpInfo = regexpInfo;
            this.matchStart = matchStart;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Boolean.hashCode(matchStart);
            result = prime * result + regexpInfo.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            MatchInfo other = (MatchInfo) obj;
            if (matchStart != other.matchStart) {
                return false;
            }
            if (!regexpInfo.equals(other.regexpInfo)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return String.format("Match (%s, fromStart = %s)", regexpInfo, matchStart);
        }

    }

    private static ConcurrentHashMap<RegexpCacheKey, AtomicInteger> compiledRegexps = new ConcurrentHashMap<>();
    private static final ReHashable REHASH_COMPILED_REGEXPS = () -> {
        compiledRegexps = new ConcurrentHashMap<>(compiledRegexps);
    };

    private static ConcurrentHashMap<MatchInfo, AtomicInteger> matchedRegexps = new ConcurrentHashMap<>();
    private static final ReHashable REHASH_MATCHED_REGEXPS = () -> {
        matchedRegexps = new ConcurrentHashMap<>(matchedRegexps);
    };

    /** WARNING: computeRegexpEncoding() mutates options, so the caller should make sure it's a copy */
    @TruffleBoundary
    public static Regex compile(RubyContext context, Rope bytes, RegexpOptions options, Node currentNode) {
        try {
            if (options.isEncodingNone()) {
                bytes = RopeOperations.withEncoding(bytes, ASCIIEncoding.INSTANCE);
            }
            Encoding enc = bytes.getEncoding();
            Encoding[] fixedEnc = new Encoding[]{ null };
            RopeBuilder unescaped = ClassicRegexp
                    .preprocess(context, bytes, enc, fixedEnc, RegexpSupport.ErrorMode.RAISE);
            enc = ClassicRegexp.computeRegexpEncoding(options, enc, fixedEnc, context);

            Regex regexp = new Regex(
                    unescaped.getUnsafeBytes(),
                    0,
                    unescaped.getLength(),
                    options.toJoniOptions(),
                    enc,
                    Syntax.RUBY,
                    new RegexWarnCallback(context));
            regexp.setUserObject(RopeOperations.withEncoding(bytes, enc));

            if (context.getOptions().REGEXP_INSTRUMENT_CREATION) {
                final RegexpCacheKey key = new RegexpCacheKey(
                        bytes,
                        enc,
                        options.toJoniOptions(),
                        context.getHashing(REHASH_COMPILED_REGEXPS));
                ConcurrentOperations.getOrCompute(compiledRegexps, key, x -> new AtomicInteger()).incrementAndGet();
            }

            return regexp;
        } catch (ValueException e) {
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().regexpError(
                            e.getMessage() + ": " + '/' + RopeOperations.decodeRope(bytes) + '/',
                            currentNode));
        } catch (SyntaxException e) {
            throw new RaiseException(context, context.getCoreExceptions().regexpError(e.getMessage(), currentNode));
        }
    }

}
