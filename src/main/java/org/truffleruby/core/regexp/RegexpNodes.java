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

import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.profiles.BranchProfile;
import org.graalvm.collections.Pair;
import org.jcodings.specific.UTF8Encoding;
import org.joni.NameEntry;
import org.joni.Regex;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.ToStrNode;
import org.truffleruby.core.encoding.EncodingNodes;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.regexp.RegexpNodesFactory.ToSNodeFactory;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.DeferredRaiseException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.library.RubyStringLibrary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;

@CoreModule(value = "Regexp", isClass = true)
public abstract class RegexpNodes {

    public static void initialize(RubyLanguage language, RubyRegexp regexp, Rope setSource, int options,
            Node currentNode) throws DeferredRaiseException {
        final RegexpOptions regexpOptions = RegexpOptions.fromEmbeddedOptions(options);
        final Regex regex = TruffleRegexpNodes.compile(language, null, setSource, regexpOptions, currentNode);

        // The RegexpNodes.compile operation may modify the encoding of the source rope. This modified copy is stored
        // in the Regex object as the "user object". Since ropes are immutable, we need to take this updated copy when
        // constructing the final regexp.
        regexp.source = (Rope) regex.getUserObject();
        regexp.options = regexpOptions;
        regexp.regex = regex;
        regexp.cachedEncodings = new EncodingCache();
        regexp.tregexCache = new TRegexCache();
    }

    public static RubyRegexp createRubyRegexp(Regex regex, Rope source,
            RegexpOptions options, EncodingCache cache, TRegexCache tregexCache) {
        return new RubyRegexp(regex, source, options, cache, tregexCache);
    }

    @CoreMethod(names = "hash")
    public abstract static class HashNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int hash(RubyRegexp regexp) {
            int options = regexp.regex.getOptions() &
                    ~32 /* option n, NO_ENCODING in common/regexp.rb */;
            return options ^ regexp.source.hashCode();
        }

    }

    @CoreMethod(names = { "quote", "escape" }, onSingleton = true, required = 1)
    public abstract static class QuoteNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode;
        @Child private ToStrNode toStrNode;
        @Child private QuoteNode quoteNode;

        public abstract RubyString execute(Object raw);

        public static QuoteNode create() {
            return RegexpNodesFactory.QuoteNodeFactory.create(null);
        }

        @Specialization(guards = "libRaw.isRubyString(raw)")
        protected RubyString quoteString(Object raw,
                @CachedLibrary(limit = "2") RubyStringLibrary libRaw) {
            final Rope rope = libRaw.getRope(raw);
            final Pair<Rope, RubyEncoding> ropeQuotedResult = ClassicRegexp.quote19(rope, libRaw.getEncoding(raw));
            return getMakeStringNode().fromRope(ropeQuotedResult.getLeft(), ropeQuotedResult.getRight());
        }

        @Specialization
        protected RubyString quoteSymbol(RubySymbol raw) {
            return doQuoteString(
                    getMakeStringNode()
                            .executeMake(raw.getString(), Encodings.UTF_8, CodeRange.CR_UNKNOWN));
        }

        @Fallback
        protected RubyString quote(Object raw) {
            if (toStrNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toStrNode = insert(ToStrNode.create());
            }

            return doQuoteString(toStrNode.execute(raw));
        }

        private RubyString doQuoteString(Object raw) {
            if (quoteNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                quoteNode = insert(QuoteNode.create());
            }
            return quoteNode.execute(raw);
        }

        private StringNodes.MakeStringNode getMakeStringNode() {
            if (makeStringNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                makeStringNode = insert(StringNodes.MakeStringNode.create());
            }

            return makeStringNode;
        }
    }

    @CoreMethod(names = "source")
    public abstract static class SourceNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString source(RubyRegexp regexp,
                @Cached StringNodes.MakeStringNode makeStringNode,
                @Cached EncodingNodes.GetRubyEncodingNode getRubyEncodingNode) {
            final RubyEncoding rubyEncoding = getRubyEncodingNode.executeGetRubyEncoding(regexp.source.encoding);
            return makeStringNode.fromRope(regexp.source, rubyEncoding);
        }

    }

    @CoreMethod(names = "to_s")
    @ImportStatic(RegexpGuards.class)
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        public static ToSNode create() {
            return ToSNodeFactory.create(null);
        }

        public abstract RubyString execute(RubyRegexp regexp);

        @Specialization(guards = "isSameRegexp(regexp, cachedRegexp)")
        protected RubyString toSCached(RubyRegexp regexp,
                @Cached("regexp") RubyRegexp cachedRegexp,
                @Cached("createRope(cachedRegexp)") Rope rope,
                @Cached EncodingNodes.GetRubyEncodingNode getRubyEncodingNode) {
            final RubyEncoding rubyEncoding = getRubyEncodingNode.executeGetRubyEncoding(rope.encoding);
            return makeStringNode.fromRope(rope, rubyEncoding);
        }

        @Specialization
        protected RubyString toS(RubyRegexp regexp,
                @Cached EncodingNodes.GetRubyEncodingNode getRubyEncodingNode) {
            final Rope rope = createRope(regexp);
            final RubyEncoding rubyEncoding = getRubyEncodingNode.executeGetRubyEncoding(rope.encoding);
            return makeStringNode.fromRope(rope, rubyEncoding);
        }

        @TruffleBoundary
        protected Rope createRope(RubyRegexp regexp) {
            final ClassicRegexp classicRegexp;
            try {
                classicRegexp = new ClassicRegexp(
                        getContext(),
                        regexp.source,
                        RegexpOptions.fromEmbeddedOptions(regexp.regex.getOptions()));
            } catch (DeferredRaiseException dre) {
                throw dre.getException(getContext());
            }
            return classicRegexp.toRopeBuilder().toRope();
        }
    }

    @Primitive(name = "regexp_names")
    public abstract static class RegexpNamesNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected RubyArray regexpNames(RubyRegexp regexp) {
            final int size = regexp.regex.numberOfNames();
            if (size == 0) {
                return createEmptyArray();
            }

            final Object[] names = new Object[size];
            int i = 0;
            for (Iterator<NameEntry> iter = regexp.regex.namedBackrefIterator(); iter.hasNext();) {
                final NameEntry e = iter.next();
                final byte[] bytes = Arrays.copyOfRange(e.name, e.nameP, e.nameEnd);

                final Rope rope = RopeOperations.create(bytes, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
                final RubySymbol name = getSymbol(rope, Encodings.UTF_8);

                final int[] backrefs = e.getBackRefs();
                final RubyArray backrefsRubyArray = createArray(backrefs);
                names[i++] = createArray(new Object[]{ name, backrefsRubyArray });
            }

            return createArray(names);
        }

    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyRegexp allocate(RubyClass rubyClass) {
            return new RubyRegexp(
                    null,
                    null,
                    RegexpOptions.NULL_OPTIONS,
                    null,
                    null);
        }

    }

    @CoreMethod(names = "fixed_encoding?")
    public abstract static class RegexpIsFixedEncodingNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean fixedEncoding(RubyRegexp regexp) {
            return regexp.options.isFixed();
        }

    }

    @CoreMethod(names = "compile", required = 2, lowerFixnum = 2, visibility = Visibility.PRIVATE)
    @ImportStatic(RegexpGuards.class)
    public abstract static class RegexpCompileNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRegexpLiteral(regexp)")
        protected RubyRegexp initializeRegexpLiteral(RubyRegexp regexp, Object pattern, int options) {
            throw new RaiseException(getContext(), coreExceptions().securityError("can't modify literal regexp", this));
        }

        @Specialization(
                guards = { "!isRegexpLiteral(regexp)", "isInitialized(regexp)" })
        protected RubyRegexp initializeAlreadyInitialized(RubyRegexp regexp, Object pattern, int options) {
            throw new RaiseException(getContext(), coreExceptions().typeError("already initialized regexp", this));
        }

        @Specialization(
                guards = { "libPattern.isRubyString(pattern)", "!isRegexpLiteral(regexp)", "!isInitialized(regexp)" })
        protected RubyRegexp initialize(RubyRegexp regexp, Object pattern, int options,
                @Cached BranchProfile errorProfile,
                @CachedLibrary(limit = "2") RubyStringLibrary libPattern) {
            try {
                RegexpNodes.initialize(getLanguage(), regexp, libPattern.getRope(pattern), options, this);
            } catch (DeferredRaiseException dre) {
                errorProfile.enter();
                throw dre.getException(getContext());
            }
            return regexp;
        }
    }

    @CoreMethod(names = "initialize_copy", required = 1, needsSelf = true)
    @ImportStatic(RegexpGuards.class)
    public abstract static class RegexpInitializeCopyNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRegexpLiteral(regexp)")
        protected RubyRegexp initializeRegexpLiteral(RubyRegexp regexp, RubyRegexp other) {
            throw new RaiseException(getContext(), coreExceptions().securityError("can't modify literal regexp", this));
        }

        @Specialization(guards = { "!isRegexpLiteral(regexp)", "isInitialized(regexp)" })
        protected RubyRegexp initializeAlreadyInitialized(RubyRegexp regexp, RubyRegexp other) {
            throw new RaiseException(getContext(), coreExceptions().typeError("already initialized regexp", this));
        }

        @Specialization(guards = { "!isRegexpLiteral(regexp)", "!isInitialized(regexp)" })
        protected RubyRegexp initialize(RubyRegexp regexp, RubyRegexp other) {
            regexp.regex = other.regex;
            regexp.source = other.source;
            regexp.options = other.options;
            regexp.cachedEncodings = other.cachedEncodings;
            regexp.tregexCache = other.tregexCache;
            return regexp;
        }
    }

    @CoreMethod(names = "options")
    @ImportStatic(RegexpGuards.class)
    public abstract static class RegexpOptionsNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isInitialized(regexp)")
        protected int options(RubyRegexp regexp) {
            return regexp.options.toOptions();
        }

        @Specialization(guards = "!isInitialized(regexp)")
        protected int optionsNotInitialized(RubyRegexp regexp) {
            throw new RaiseException(getContext(), coreExceptions().typeError("uninitialized Regexp", this));
        }

    }
}
