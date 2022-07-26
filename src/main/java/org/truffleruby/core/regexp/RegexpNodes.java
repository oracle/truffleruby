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

import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.strings.TruffleString;
import org.joni.NameEntry;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.ToStrNode;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.TStringUtils;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.regexp.RegexpNodesFactory.ToSNodeFactory;
import org.truffleruby.core.string.ATStringWithEncoding;
import org.truffleruby.core.string.TStringWithEncoding;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.DeferredRaiseException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.library.RubyStringLibrary;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.Specialization;

@CoreModule(value = "Regexp", isClass = true)
public abstract class RegexpNodes {

    @CoreMethod(names = "hash")
    public abstract static class HashNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected int hash(RubyRegexp regexp) {
            int options = regexp.regex.getOptions() & ~32 /* option n, NO_ENCODING in common/regexp.rb */;
            return options ^ regexp.source.hashCode();
        }
    }

    @CoreMethod(names = { "quote", "escape" }, onSingleton = true, required = 1)
    public abstract static class QuoteNode extends CoreMethodArrayArgumentsNode {

        @Child private ToStrNode toStrNode;
        @Child private QuoteNode quoteNode;

        public abstract RubyString execute(Object raw);

        public static QuoteNode create() {
            return RegexpNodesFactory.QuoteNodeFactory.create(null);
        }

        @Specialization(guards = "libRaw.isRubyString(raw)", limit = "1")
        protected RubyString quoteString(Object raw,
                @Cached RubyStringLibrary libRaw) {
            return createString(ClassicRegexp.quote19(new ATStringWithEncoding(libRaw, raw)));
        }

        @Specialization
        protected RubyString quoteSymbol(RubySymbol raw) {
            return doQuoteString(createString(raw.tstring, raw.encoding));
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
    }

    @CoreMethod(names = "source")
    public abstract static class SourceNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected RubyString source(RubyRegexp regexp) {
            return createString(regexp.source, regexp.encoding);
        }
    }

    @CoreMethod(names = "to_s")
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {

        public static ToSNode create() {
            return ToSNodeFactory.create(null);
        }

        public abstract RubyString execute(RubyRegexp regexp);

        @Specialization(guards = "regexp.regex == cachedRegexp.regex")
        protected RubyString toSCached(RubyRegexp regexp,
                @Cached("regexp") RubyRegexp cachedRegexp,
                @Cached("createTString(cachedRegexp)") TStringWithEncoding string) {
            return createString(string);
        }

        @Specialization
        protected RubyString toS(RubyRegexp regexp) {
            return createString(createTString(regexp));
        }

        @TruffleBoundary
        protected TStringWithEncoding createTString(RubyRegexp regexp) {
            final ClassicRegexp classicRegexp;

            try {
                classicRegexp = new ClassicRegexp(
                        getContext(),
                        new TStringWithEncoding(regexp.source, regexp.encoding),
                        RegexpOptions.fromEmbeddedOptions(regexp.regex.getOptions()));
            } catch (DeferredRaiseException dre) {
                throw dre.getException(getContext());
            }

            return classicRegexp.toByteArrayBuilder().toTStringWithEnc(regexp.encoding);
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

                var tstring = TStringUtils.fromByteArray(bytes, Encodings.UTF_8);
                final RubySymbol name = getSymbol(tstring, Encodings.UTF_8);

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
            throw new RaiseException(getContext(), coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }

    }

    @CoreMethod(names = "fixed_encoding?")
    public abstract static class RegexpIsFixedEncodingNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean fixedEncoding(RubyRegexp regexp) {
            return regexp.options.isFixed();
        }

    }

    @Primitive(name = "regexp_compile", lowerFixnum = 1)
    public abstract static class RegexpCompileNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "libPattern.isRubyString(pattern)", limit = "1")
        protected RubyRegexp initialize(Object pattern, int options,
                @Cached BranchProfile errorProfile,
                @Cached TruffleString.AsTruffleStringNode asTruffleStringNode,
                @Cached RubyStringLibrary libPattern) {
            var encoding = libPattern.getEncoding(pattern);
            try {
                return RubyRegexp.create(
                        getLanguage(),
                        asTruffleStringNode.execute(libPattern.getTString(pattern), encoding.tencoding),
                        encoding,
                        RegexpOptions.fromEmbeddedOptions(options),
                        this);
            } catch (DeferredRaiseException dre) {
                errorProfile.enter();
                throw dre.getException(getContext());
            }
        }
    }

    @CoreMethod(names = "options")
    public abstract static class RegexpOptionsNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected int options(RubyRegexp regexp) {
            return regexp.options.toOptions();
        }
    }
}
