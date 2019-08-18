/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
/*
 * Copyright (c) 2013, 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
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
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.joni.Matcher;
import org.joni.NameEntry;
import org.joni.Regex;
import org.joni.Region;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.NonStandard;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.cast.ToStrNode;
import org.truffleruby.core.regexp.RegexpNodesFactory.ToSNodeFactory;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeBuilder;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocateObjectNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreClass("Regexp")
public abstract class RegexpNodes {

    @TruffleBoundary
    public static Matcher createMatcher(RubyContext context, DynamicObject regexp, Rope stringRope, byte[] stringBytes, boolean encodingConversion, int start) {
        final Encoding enc = checkEncoding(regexp, stringRope.getEncoding(), stringRope.getCodeRange(), true);
        Regex regex = Layouts.REGEXP.getRegex(regexp);

        if (encodingConversion && regex.getEncoding() != enc) {
            EncodingCache encodingCache = Layouts.REGEXP.getCachedEncodings(regexp);
            regex = encodingCache.getOrCreate(enc, e -> makeRegexpForEncoding(context, regexp, e));
        }

        return regex.matcher(stringBytes, start, stringBytes.length);
    }

    private static Regex makeRegexpForEncoding(RubyContext context, DynamicObject regexp, final Encoding enc) {
        Regex regex;
        final Encoding[] fixedEnc = new Encoding[]{ null };
        final Rope sourceRope = Layouts.REGEXP.getSource(regexp);
        final RopeBuilder preprocessed = ClassicRegexp.preprocess(context, sourceRope, enc, fixedEnc, RegexpSupport.ErrorMode.RAISE);
        final RegexpOptions options = Layouts.REGEXP.getOptions(regexp);
        regex = new Regex(preprocessed.getUnsafeBytes(), 0, preprocessed.getLength(),
                options.toJoniOptions(), enc, new RegexWarnCallback(context));
        return regex;
    }

    public static void setRegex(DynamicObject regexp, Regex regex) {
        Layouts.REGEXP.setRegex(regexp, regex);
        Layouts.REGEXP.setCachedEncodings(regexp, new EncodingCache());
    }

    public static void setSource(DynamicObject regexp, Rope source) {
        Layouts.REGEXP.setSource(regexp, source);
    }

    public static void setOptions(DynamicObject regexp, RegexpOptions options) {
        Layouts.REGEXP.setOptions(regexp, options);
    }

    @TruffleBoundary
    public static Encoding checkEncoding(DynamicObject regexp, Encoding strEnc, CodeRange codeRange, boolean warn) {
        assert RubyGuards.isRubyRegexp(regexp);

        final Encoding regexEnc = Layouts.REGEXP.getRegex(regexp).getEncoding();

        if (strEnc == regexEnc) {
            return regexEnc;
        } else if (regexEnc == USASCIIEncoding.INSTANCE && codeRange == CodeRange.CR_7BIT) {
            return regexEnc;
        } else if (strEnc.isAsciiCompatible() && Layouts.REGEXP.getOptions(regexp).isFixed()) {
            return regexEnc;
        }
        return strEnc;
    }

    public static void initialize(RubyContext context, DynamicObject regexp, Node currentNode, Rope setSource, int options) {
        assert RubyGuards.isRubyRegexp(regexp);
        final RegexpOptions regexpOptions = RegexpOptions.fromEmbeddedOptions(options);
        final Regex regex = TruffleRegexpNodes.compile(currentNode, context, setSource, regexpOptions);

        // The RegexpNodes.compile operation may modify the encoding of the source rope. This modified copy is stored
        // in the Regex object as the "user object". Since ropes are immutable, we need to take this updated copy when
        // constructing the final regexp.
        setSource(regexp, (Rope) regex.getUserObject());
        setOptions(regexp, regexpOptions);
        setRegex(regexp, regex);
    }

    public static void initialize(DynamicObject regexp, Regex setRegex, Rope setSource) {
        assert RubyGuards.isRubyRegexp(regexp);
        setRegex(regexp, setRegex);
        setSource(regexp, setSource);
    }

    public static DynamicObject createRubyRegexp(RubyContext context, Node currentNode, DynamicObjectFactory factory, Rope source, RegexpOptions options) {
        final Regex regexp = TruffleRegexpNodes.compile(currentNode, context, source, options);

        // The RegexpNodes.compile operation may modify the encoding of the source rope. This modified copy is stored
        // in the Regex object as the "user object". Since ropes are immutable, we need to take this updated copy when
        // constructing the final regexp.
        return Layouts.REGEXP.createRegexp(factory, regexp, (Rope) regexp.getUserObject(), options, new EncodingCache());
    }

    @TruffleBoundary
    public static DynamicObject createRubyRegexp(DynamicObjectFactory factory, Regex regex, Rope source, RegexpOptions options) {
        final DynamicObject regexp = Layouts.REGEXP.createRegexp(factory, null, null, RegexpOptions.NULL_OPTIONS, null);
        RegexpNodes.setOptions(regexp, options);
        RegexpNodes.initialize(regexp, regex, source);
        return regexp;
    }

    @CoreMethod(names = "hash")
    public abstract static class HashNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int hash(DynamicObject regexp) {
            int options = Layouts.REGEXP.getRegex(regexp).getOptions() & ~32 /* option n, NO_ENCODING in common/regexp.rb */;
            return options ^ Layouts.REGEXP.getSource(regexp).hashCode();
        }

    }

    @NonStandard
    @CoreMethod(names = "match_onwards", required = 3, lowerFixnum = 2)
    public abstract static class MatchOnwardsNode extends CoreMethodArrayArgumentsNode {

        @Child private TruffleRegexpNodes.MatchNode matchNode = TruffleRegexpNodes.MatchNode.create();
        @Child private RopeNodes.BytesNode bytesNode = RopeNodes.BytesNode.create();

        @Specialization(guards = "isRubyString(string)")
        protected Object matchOnwards(DynamicObject regexp, DynamicObject string, int startPos, boolean atStart) {
            final Rope rope = StringOperations.rope(string);
            final Matcher matcher = createMatcher(getContext(), regexp, rope, bytesNode.execute(rope), true, startPos);
            int range = rope.byteLength();
            DynamicObject result = matchNode.execute(regexp, string, matcher, startPos, range, atStart);
            if (result != nil()) {
                fixupMatchDataForStart(result, startPos);
            }
            return result;
        }
    }

    @CoreMethod(names = { "quote", "escape" }, onSingleton = true, required = 1)
    public abstract static class QuoteNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode;
        @Child private ToStrNode toStrNode;

        abstract public DynamicObject executeQuote(VirtualFrame frame, Object raw);

        @TruffleBoundary
        @Specialization(guards = "isRubyString(raw)")
        protected DynamicObject quoteString(DynamicObject raw) {
            final Rope rope = StringOperations.rope(raw);
            boolean isAsciiOnly = rope.getEncoding().isAsciiCompatible() && rope.getCodeRange() == CodeRange.CR_7BIT;
            return getMakeStringNode().fromRope(ClassicRegexp.quote19(rope, isAsciiOnly));
        }

        @Specialization(guards = "isRubySymbol(raw)")
        protected DynamicObject quoteSymbol(DynamicObject raw) {
            return quoteString(getMakeStringNode().executeMake(Layouts.SYMBOL.getString(raw), UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN));
        }

        @Fallback
        public DynamicObject quote(VirtualFrame frame, Object raw) {
            if (toStrNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toStrNode = insert(ToStrNode.create());
            }

            return executeQuote(frame, toStrNode.executeToStr(frame, raw));
        }

        private StringNodes.MakeStringNode getMakeStringNode() {
            if (makeStringNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                makeStringNode = insert(StringNodes.MakeStringNode.create());
            }

            return makeStringNode;
        }
    }

    @TruffleBoundary
    private static DynamicObject fixupMatchDataForStart(DynamicObject matchData, int startPos) {
        Region regs = Layouts.MATCH_DATA.getRegion(matchData);
        if (startPos != 0) {
            for (int i = 0; i < regs.beg.length; i++) {
                if (regs.beg[i] != -1) {
                    regs.beg[i] += startPos;
                    regs.end[i] += startPos;
                }
            }
        }
        return matchData;
    }

    @Primitive(name = "regexp_search_from_binary", lowerFixnum = 2)
    public abstract static class SearchFromBinaryNode extends CoreMethodArrayArgumentsNode {

        @Child private TruffleRegexpNodes.MatchNode matchNode = TruffleRegexpNodes.MatchNode.create();
        @Child private RopeNodes.BytesNode bytesNode = RopeNodes.BytesNode.create();

        @Specialization(guards = "isRubyString(string)")
        protected Object searchFrom(DynamicObject regexp, DynamicObject string, int startPos) {
            final Rope rope = StringOperations.rope(string);
            final Matcher matcher = createMatcher(getContext(), regexp, rope, bytesNode.execute(rope), false, startPos);
            final int endPos = rope.byteLength();
            DynamicObject result = matchNode.execute(regexp, string, matcher, startPos, endPos, false);
            if (result != nil()) {
                fixupMatchDataForStart(result, startPos);
            }
            return result;
        }
    }

    @CoreMethod(names = "source")
    public abstract static class SourceNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected DynamicObject source(DynamicObject regexp,
                @Cached StringNodes.MakeStringNode makeStringNode) {
            return makeStringNode.fromRope(Layouts.REGEXP.getSource(regexp));
        }

    }

    @CoreMethod(names = "to_s")
    @ImportStatic(RegexpGuards.class)
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        public static ToSNode create() {
            return ToSNodeFactory.create(null);
        }

        public abstract DynamicObject execute(DynamicObject regexp);

        @Specialization(guards = "isSameRegexp(regexp,cachedRegexp)")
        protected DynamicObject toSCached(DynamicObject regexp,
                @Cached("regexp") DynamicObject cachedRegexp,
                @Cached("createRope(regexp)") Rope rope) {
            return makeStringNode.fromRope(rope);
        }

        @Specialization
        protected DynamicObject toS(DynamicObject regexp) {
            final Rope rope = createRope(regexp);
            return makeStringNode.fromRope(rope);
        }

        @TruffleBoundary
        protected Rope createRope(DynamicObject regexp) {
            final ClassicRegexp classicRegexp = ClassicRegexp.newRegexp(getContext(), Layouts.REGEXP.getSource(regexp), Layouts.REGEXP.getRegex(regexp).getOptions());
            return classicRegexp.toRopeBuilder().toRope();
        }
    }

    @Primitive(name = "regexp_names")
    public abstract static class RegexpNamesNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object regexpNames(DynamicObject regexp) {
            final int size = Layouts.REGEXP.getRegex(regexp).numberOfNames();
            if (size == 0) {
                return createArray(null, size);
            }

            final Object[] names = new Object[size];
            int i = 0;
            for (final Iterator<NameEntry> iter = Layouts.REGEXP.getRegex(regexp).namedBackrefIterator(); iter.hasNext();) {
                final NameEntry e = iter.next();
                final byte[] bytes = Arrays.copyOfRange(e.name, e.nameP, e.nameEnd);

                final Rope rope = getContext().getRopeCache().getRope(bytes, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
                final DynamicObject name = getContext().getSymbolTable().getSymbol(rope);

                final int[] backrefs = e.getBackRefs();
                final DynamicObject backrefsRubyArray = createArray(backrefs, backrefs.length);
                names[i++] = createArray(new Object[]{ name, backrefsRubyArray }, 2);
            }

            return createArray(names, size);
        }

    }

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Child private AllocateObjectNode allocateNode = AllocateObjectNode.create();

        @Specialization
        protected DynamicObject allocate(DynamicObject rubyClass) {
            return allocateNode.allocate(rubyClass, null, null, RegexpOptions.NULL_OPTIONS, null);
        }

    }

    @Primitive(name = "regexp_fixed_encoding_p")
    public static abstract class RegexpFixedEncodingPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean fixedEncoding(DynamicObject regexp) {
            return Layouts.REGEXP.getOptions(regexp).isFixed();
        }

    }

    @Primitive(name = "regexp_initialize", lowerFixnum = 2)
    @ImportStatic(RegexpGuards.class)
    public static abstract class RegexpInitializePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "isRegexpLiteral(regexp)", "isRubyString(pattern)" })
        protected DynamicObject initializeRegexpLiteral(DynamicObject regexp, DynamicObject pattern, int options) {
            throw new RaiseException(getContext(), coreExceptions().securityError("can't modify literal regexp", this));
        }

        @Specialization(guards = { "!isRegexpLiteral(regexp)", "isInitialized(regexp)", "isRubyString(pattern)" })
        protected DynamicObject initializeAlreadyInitialized(DynamicObject regexp, DynamicObject pattern, int options) {
            throw new RaiseException(getContext(), coreExceptions().typeError("already initialized regexp", this));
        }

        @Specialization(guards = { "!isRegexpLiteral(regexp)", "!isInitialized(regexp)", "isRubyString(pattern)" })
        protected DynamicObject initialize(DynamicObject regexp, DynamicObject pattern, int options) {
            RegexpNodes.initialize(getContext(), regexp, this, StringOperations.rope(pattern), options);
            return regexp;
        }
    }

    @CoreMethod(names = "initialize_copy", required = 1, needsSelf = true)
    @ImportStatic(RegexpGuards.class)
    public static abstract class RegexpInitializeCopyPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = { "isRegexpLiteral(regexp)", "isRubyRegexp(other)" })
        protected DynamicObject initializeRegexpLiteral(DynamicObject regexp, DynamicObject other) {
            throw new RaiseException(getContext(), coreExceptions().securityError("can't modify literal regexp", this));
        }

        @Specialization(guards = { "!isRegexpLiteral(regexp)", "isInitialized(regexp)", "isRubyRegexp(other)" })
        protected DynamicObject initializeAlreadyInitialized(DynamicObject regexp, DynamicObject other) {
            throw new RaiseException(getContext(), coreExceptions().typeError("already initialized regexp", this));
        }

        @Specialization(guards = { "!isRegexpLiteral(regexp)", "!isInitialized(regexp)", "isRubyRegexp(other)" })
        protected DynamicObject initialize(DynamicObject regexp, DynamicObject other) {
            Layouts.REGEXP.setRegex(regexp, Layouts.REGEXP.getRegex(other));
            Layouts.REGEXP.setSource(regexp, Layouts.REGEXP.getSource(other));
            Layouts.REGEXP.setOptions(regexp, Layouts.REGEXP.getOptions(other));
            Layouts.REGEXP.setCachedEncodings(regexp, Layouts.REGEXP.getCachedEncodings(other));
            return regexp;
        }
    }

    @Primitive(name = "regexp_options")
    @ImportStatic(RegexpGuards.class)
    public static abstract class RegexpOptionsPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isInitialized(regexp)")
        protected int options(DynamicObject regexp) {
            return Layouts.REGEXP.getOptions(regexp).toOptions();
        }

        @Specialization(guards = "!isInitialized(regexp)")
        protected int optionsNotInitialized(DynamicObject regexp) {
            throw new RaiseException(getContext(), coreExceptions().typeError("uninitialized Regexp", this));
        }

    }

    @Primitive(name = "regexp_search_region", lowerFixnum = { 2, 3 })
    @ImportStatic(RegexpGuards.class)
    public static abstract class RegexpSearchRegionPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "!isInitialized(regexp)", "isRubyString(string)" })
        protected Object searchRegionNotInitialized(DynamicObject regexp, DynamicObject string, int start, int end, boolean forward) {
            throw new RaiseException(getContext(), coreExceptions().typeError("uninitialized Regexp", this));
        }

        @Specialization(guards = { "isRubyString(string)", "!isValidEncoding(string)" })
        protected Object searchRegionInvalidEncoding(DynamicObject regexp, DynamicObject string, int start, int end, boolean forward) {
            throw new RaiseException(getContext(), coreExceptions().argumentError(formatError(string), this));
        }

        @TruffleBoundary
        private String formatError(DynamicObject string) {
            return StringUtils.format("invalid byte sequence in %s", Layouts.STRING.getRope(string).getEncoding());
        }

        @Specialization(guards = { "isInitialized(regexp)", "isRubyString(string)", "isValidEncoding(string)" })
        protected Object searchRegion(DynamicObject regexp, DynamicObject string, int start, int end, boolean forward,
                @Cached("createBinaryProfile()") ConditionProfile forwardSearchProfile,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached TruffleRegexpNodes.MatchNode matchNode) {
            final Rope rope = StringOperations.rope(string);
            final Matcher matcher = RegexpNodes.createMatcher(getContext(), regexp, rope, bytesNode.execute(rope), true, 0);


            if (forwardSearchProfile.profile(forward)) {
                // Search forward through the string.
                return matchNode.execute(regexp, string, matcher, start, end, false);
            } else {
                // Search backward through the string.
                return matchNode.execute(regexp, string, matcher, end, start, false);
            }
        }

    }
}
