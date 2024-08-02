/*
 * Copyright (c) 2014, 2024 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Contains code modified from JRuby's RubyConverter.java
 */
package org.truffleruby.core.encoding;

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Idempotent;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.profiles.InlinedBranchProfile;
import com.oracle.truffle.api.profiles.InlinedConditionProfile;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.TruffleString;
import org.graalvm.shadowed.org.jcodings.Config;
import org.graalvm.shadowed.org.jcodings.EncodingDB;
import org.truffleruby.annotations.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.annotations.CoreModule;
import org.truffleruby.annotations.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.ToRubyEncodingNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringGuards;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.annotations.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.yield.CallBlockNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Specialization;

import static com.oracle.truffle.api.strings.TruffleString.CodeRange.ASCII;

@CoreModule(value = "Encoding", isClass = true)
public abstract class EncodingNodes {

    @CoreMethod(names = "ascii_compatible?")
    public abstract static class AsciiCompatibleNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        boolean isAsciiCompatible(RubyEncoding encoding) {
            return encoding.isAsciiCompatible;
        }
    }

    // TODO: [GR-53442] Splitting: seems too expensive to split, should try to simplify logic and remove inline caches
    // MRI: enc_compatible_str and enc_compatible_latter
    @ImportStatic(TruffleString.CodeRange.class)
    @GenerateCached(false)
    @GenerateInline
    @GenerateUncached
    public abstract static class NegotiateCompatibleStringEncodingNode extends RubyBaseNode {

        public abstract RubyEncoding execute(Node node, AbstractTruffleString first, RubyEncoding firstEncoding,
                AbstractTruffleString second, RubyEncoding secondEncoding);

        @Specialization(guards = {
                "firstEncoding == cachedEncoding",
                "secondEncoding == cachedEncoding",
        }, limit = "getCacheLimit()")
        static RubyEncoding negotiateSameEncodingCached(
                AbstractTruffleString first,
                RubyEncoding firstEncoding,
                AbstractTruffleString second,
                RubyEncoding secondEncoding,
                @Cached("firstEncoding") RubyEncoding cachedEncoding) {
            assert first.isCompatibleToUncached(firstEncoding.tencoding) &&
                    second.isCompatibleToUncached(secondEncoding.tencoding);
            return cachedEncoding;
        }


        @Specialization(guards = "firstEncoding == secondEncoding", replaces = "negotiateSameEncodingCached")
        static RubyEncoding negotiateSameEncodingUncached(
                AbstractTruffleString first,
                RubyEncoding firstEncoding,
                AbstractTruffleString second,
                RubyEncoding secondEncoding) {
            assert first.isCompatibleToUncached(firstEncoding.tencoding) &&
                    second.isCompatibleToUncached(secondEncoding.tencoding);
            return firstEncoding;
        }

        @Specialization(guards = {
                "firstEncoding != secondEncoding",
                "firstEncoding == cachedEncoding",
                "isStandardEncoding(cachedEncoding)",
                "codeRangeNode.execute(second, secondEncoding.tencoding) == ASCII"
        }, limit = "NUMBER_OF_STANDARD_ENCODINGS")
        static RubyEncoding negotiateStandardEncodingAndCr7Bit(
                AbstractTruffleString first,
                RubyEncoding firstEncoding,
                AbstractTruffleString second,
                RubyEncoding secondEncoding,
                @Cached(inline = false) @Shared TruffleString.GetByteCodeRangeNode codeRangeNode,
                @Cached("firstEncoding") RubyEncoding cachedEncoding) {
            // Encoding negotiation of two strings is most often between strings with the same encoding. The next most
            // frequent case is two strings with different encodings, but each being one of the standard/default runtime
            // encodings. When the second string is CR_7BIT, we can short-circuit the full set of encoding negotiation
            // rules and simply return the encoding of the first string.

            return cachedEncoding;
        }

        @Specialization(
                guards = {
                        "firstEncoding != secondEncoding",
                        "first.isEmpty() == isFirstEmpty",
                        "second.isEmpty() == isSecondEmpty",
                        "cachedFirstEncoding == firstEncoding",
                        "cachedSecondEncoding == secondEncoding",
                        "firstCodeRange == firstCodeRangeCached",
                        "secondCodeRange == secondCodeRangeCached" },
                limit = "getCacheLimit()")
        static RubyEncoding negotiateEncodingCached(
                AbstractTruffleString first,
                RubyEncoding firstEncoding,
                AbstractTruffleString second,
                RubyEncoding secondEncoding,
                @Cached("first.isEmpty()") boolean isFirstEmpty,
                @Cached("second.isEmpty()") boolean isSecondEmpty,
                @Cached(inline = false) @Shared TruffleString.GetByteCodeRangeNode codeRangeNode,
                @Bind("codeRangeNode.execute(first, firstEncoding.tencoding)") TruffleString.CodeRange firstCodeRange,
                @Bind("codeRangeNode.execute(second, secondEncoding.tencoding)") TruffleString.CodeRange secondCodeRange,
                @Cached("firstCodeRange") TruffleString.CodeRange firstCodeRangeCached,
                @Cached("secondCodeRange") TruffleString.CodeRange secondCodeRangeCached,
                @Cached("firstEncoding") RubyEncoding cachedFirstEncoding,
                @Cached("secondEncoding") RubyEncoding cachedSecondEncoding,
                @Cached("compatibleEncodingForStrings(first, firstEncoding, second, secondEncoding, codeRangeNode)") RubyEncoding negotiatedEncoding) {
            assert first.isCompatibleToUncached(firstEncoding.tencoding) &&
                    second.isCompatibleToUncached(secondEncoding.tencoding);
            return negotiatedEncoding;
        }

        @Specialization(guards = "firstEncoding != secondEncoding", replaces = "negotiateEncodingCached")
        static RubyEncoding negotiateEncodingUncached(
                AbstractTruffleString first,
                RubyEncoding firstEncoding,
                AbstractTruffleString second,
                RubyEncoding secondEncoding,
                @Cached(inline = false) @Shared TruffleString.GetByteCodeRangeNode codeRangeNode) {
            assert first.isCompatibleToUncached(firstEncoding.tencoding) &&
                    second.isCompatibleToUncached(secondEncoding.tencoding);
            return compatibleEncodingForStrings(first, firstEncoding, second, secondEncoding, codeRangeNode);
        }

        /** This method returns non-null if either:
         * <ul>
         * <li>one side is empty</li>
         * <li>one side is 7-bit and both encodings are ascii-compatible</li>
         * </ul>
         */
        @TruffleBoundary
        protected static RubyEncoding compatibleEncodingForStrings(AbstractTruffleString first,
                RubyEncoding firstEncoding,
                AbstractTruffleString second, RubyEncoding secondEncoding,
                TruffleString.GetByteCodeRangeNode codeRangeNode) {
            // MRI: enc_compatible_latter
            assert firstEncoding != secondEncoding : "this method assumes the encodings are different";

            if (second.isEmpty()) {
                return firstEncoding;
            }
            if (first.isEmpty()) {
                return (firstEncoding.isAsciiCompatible &&
                        StringGuards.is7Bit(second, secondEncoding, codeRangeNode))
                                ? firstEncoding
                                : secondEncoding;
            }

            if (!firstEncoding.isAsciiCompatible || !secondEncoding.isAsciiCompatible) {
                return null;
            }

            if (StringGuards.is7Bit(second, secondEncoding, codeRangeNode)) {
                return firstEncoding;
            }
            if (StringGuards.is7Bit(first, firstEncoding, codeRangeNode)) {
                return secondEncoding;
            }

            return null;
        }

        protected int getCacheLimit() {
            return getLanguage().options.ENCODING_COMPATIBLE_QUERY_CACHE;
        }

        protected static final int NUMBER_OF_STANDARD_ENCODINGS = 3;

        /** Indicates whether the encoding is one of the runtime-default encodings. Many (most?) applications do not
         * override the default encodings and as such, this set of encodings is used very frequently in real-world Ruby
         * applications. */
        @Idempotent
        protected boolean isStandardEncoding(RubyEncoding encoding) {
            return encoding == Encodings.UTF_8 || encoding == Encodings.US_ASCII || encoding == Encodings.BINARY;
        }
    }

    // MRI: enc_compatible_latter
    // TODO: [GR-53442] Splitting: seems too expensive to split, should try to simplify logic and remove inline caches
    /** Use {@link NegotiateCompatibleStringEncodingNode} instead if both arguments are always Strings, for footprint */
    @GenerateCached(false)
    @GenerateInline
    public abstract static class NegotiateCompatibleEncodingNode extends RubyBaseNode {

        public abstract RubyEncoding execute(Node node, Object first, RubyEncoding firstEncoding, Object second,
                RubyEncoding secondEncoding);

        @Specialization(
                guards = {
                        "firstEncoding == cachedEncoding",
                        "secondEncoding == cachedEncoding",
                        "cachedEncoding != null" },
                limit = "getCacheLimit()")
        static RubyEncoding negotiateSameEncodingCached(
                Object first, RubyEncoding firstEncoding, Object second, RubyEncoding secondEncoding,
                @Cached("firstEncoding") RubyEncoding cachedEncoding) {
            return cachedEncoding;
        }

        @Specialization(
                guards = { "firstEncoding == secondEncoding", "firstEncoding != null" },
                replaces = "negotiateSameEncodingCached")
        static RubyEncoding negotiateSameEncodingUncached(
                Object first, RubyEncoding firstEncoding, Object second, RubyEncoding secondEncoding) {
            return firstEncoding;
        }

        @Specialization(guards = { "libFirst.isRubyString(first)", "libSecond.isRubyString(second)" })
        static RubyEncoding negotiateStringStringEncoding(
                Node node, Object first, RubyEncoding firstEncoding, Object second, RubyEncoding secondEncoding,
                @Cached @Shared RubyStringLibrary libFirst,
                @Cached @Shared RubyStringLibrary libSecond,
                @Cached NegotiateCompatibleStringEncodingNode negotiateNode) {
            return negotiateNode.execute(
                    node,
                    libFirst.getTString(first),
                    firstEncoding,
                    libSecond.getTString(second),
                    secondEncoding);
        }

        @Specialization(
                guards = {
                        "libFirst.isRubyString(first)",
                        "isNotRubyString(second)",
                        "codeRange == codeRangeCached",
                        "firstEncoding == firstEncodingCached",
                        "secondEncoding == secondEncodingCached",
                        "firstEncodingCached != secondEncodingCached" },
                limit = "getCacheLimit()")
        static RubyEncoding negotiateStringObjectCached(
                Object first, RubyEncoding firstEncoding, Object second, RubyEncoding secondEncoding,
                @Cached @Shared RubyStringLibrary libFirst,
                @Cached("firstEncoding") RubyEncoding firstEncodingCached,
                @Cached("secondEncoding") RubyEncoding secondEncodingCached,
                @Cached(inline = false) @Shared TruffleString.GetByteCodeRangeNode codeRangeNode,
                @Bind("getCodeRange(codeRangeNode, first, libFirst)") TruffleString.CodeRange codeRange,
                @Cached("codeRange") TruffleString.CodeRange codeRangeCached,
                @Cached("negotiateStringObjectUncached(first, firstEncoding, second, secondEncoding, codeRangeNode, libFirst)") RubyEncoding negotiatedEncoding) {
            return negotiatedEncoding;
        }

        @Specialization(
                guards = {
                        "libFirst.isRubyString(first)",
                        "firstEncoding != secondEncoding",
                        "isNotRubyString(second)" },
                replaces = "negotiateStringObjectCached")
        static RubyEncoding negotiateStringObjectUncached(
                Object first, RubyEncoding firstEncoding, Object second, RubyEncoding secondEncoding,
                @Cached(inline = false) @Shared TruffleString.GetByteCodeRangeNode codeRangeNode,
                @Cached @Shared RubyStringLibrary libFirst) {

            if (secondEncoding == null) {
                return null;
            }

            if (!firstEncoding.isAsciiCompatible || !secondEncoding.isAsciiCompatible) {
                return null;
            }

            if (secondEncoding == Encodings.US_ASCII) {
                return firstEncoding;
            }

            if (getCodeRange(codeRangeNode, first, libFirst) == ASCII) {
                return secondEncoding;
            }

            return null;
        }

        @Specialization(
                guards = {
                        "libSecond.isRubyString(second)",
                        "firstEncoding != secondEncoding",
                        "isNotRubyString(first)" })
        static RubyEncoding negotiateObjectString(
                Object first, RubyEncoding firstEncoding, Object second, RubyEncoding secondEncoding,
                @Cached @Shared RubyStringLibrary libSecond,
                @Cached(inline = false) @Shared TruffleString.GetByteCodeRangeNode codeRangeNode) {
            return negotiateStringObjectUncached(second, secondEncoding, first, firstEncoding, codeRangeNode,
                    libSecond);
        }

        @Specialization(
                guards = {
                        "firstEncoding != secondEncoding",
                        "isNotRubyString(first)",
                        "isNotRubyString(second)",
                        "firstEncoding != null",
                        "secondEncoding != null",
                        "firstEncoding == firstEncodingCached",
                        "secondEncoding == secondEncodingCached", },
                limit = "getCacheLimit()")
        static RubyEncoding negotiateObjectObjectCached(
                Object first, RubyEncoding firstEncoding, Object second, RubyEncoding secondEncoding,
                @Cached("firstEncoding") RubyEncoding firstEncodingCached,
                @Cached("secondEncoding") RubyEncoding secondEncodingCached,
                @Cached("areCompatible(firstEncodingCached, secondEncodingCached)") RubyEncoding negotiatedEncoding) {
            return negotiatedEncoding;
        }

        @Specialization(
                guards = {
                        "firstEncoding != secondEncoding",
                        "isNotRubyString(first)",
                        "isNotRubyString(second)" },
                replaces = "negotiateObjectObjectCached")
        static RubyEncoding negotiateObjectObjectUncached(
                Object first, RubyEncoding firstEncoding, Object second, RubyEncoding secondEncoding) {
            return areCompatible(firstEncoding, secondEncoding);
        }

        @TruffleBoundary
        protected static RubyEncoding areCompatible(RubyEncoding enc1, RubyEncoding enc2) {
            assert enc1 != enc2;

            if (enc1 == null || enc2 == null) {
                return null;
            }

            if (!enc1.isAsciiCompatible || !enc2.isAsciiCompatible) {
                return null;
            }

            if (enc2 == Encodings.US_ASCII) {
                return enc1;
            }
            if (enc1 == Encodings.US_ASCII) {
                return enc2;
            }

            return null;
        }

        protected static TruffleString.CodeRange getCodeRange(TruffleString.GetByteCodeRangeNode codeRangeNode,
                Object string, RubyStringLibrary libString) {
            return codeRangeNode.execute(libString.getTString(string), libString.getTEncoding(string));
        }


        protected int getCacheLimit() {
            return getLanguage().options.ENCODING_COMPATIBLE_QUERY_CACHE;
        }

    }

    // MRI: rb_enc_compatible
    @Primitive(name = "encoding_compatible?")
    public abstract static class CompatibleQueryNode extends PrimitiveArrayArgumentsNode {

        public static CompatibleQueryNode create() {
            return EncodingNodesFactory.CompatibleQueryNodeFactory.create(null);
        }

        @Specialization
        Object isCompatible(Object first, Object second,
                @Cached NegotiateCompatibleEncodingNode negotiateCompatibleEncodingNode,
                @Cached ToRubyEncodingNode toRubyEncodingNode,
                @Cached InlinedConditionProfile noNegotiatedEncodingProfile) {
            final var firstEncoding = toRubyEncodingNode.execute(this, first);
            final var secondEncoding = toRubyEncodingNode.execute(this, second);
            final RubyEncoding negotiatedEncoding = negotiateCompatibleEncodingNode.execute(this, first,
                    firstEncoding, second, secondEncoding);

            if (noNegotiatedEncodingProfile.profile(this, negotiatedEncoding == null)) {
                return nil;
            }

            return negotiatedEncoding;
        }
    }

    // encoding_compatible? but only accepting Strings for better footprint
    // Like Primitive.encoding_ensure_compatible_str but returns nil if incompatible
    @Primitive(name = "strings_compatible?")
    public abstract static class AreStringsCompatibleNode extends PrimitiveArrayArgumentsNode {
        public static AreStringsCompatibleNode create() {
            return EncodingNodesFactory.AreStringsCompatibleNodeFactory.create(null);
        }

        @Specialization
        Object areCompatible(Object first, Object second,
                @Cached RubyStringLibrary libFirst,
                @Cached RubyStringLibrary libSecond,
                @Cached NegotiateCompatibleStringEncodingNode negotiateCompatibleStringEncodingNode,
                @Cached InlinedConditionProfile noNegotiatedEncodingProfile) {
            final RubyEncoding negotiatedEncoding = negotiateCompatibleStringEncodingNode.execute(this,
                    libFirst.getTString(first), libFirst.getEncoding(first),
                    libSecond.getTString(second), libSecond.getEncoding(second));

            if (noNegotiatedEncodingProfile.profile(this, negotiatedEncoding == null)) {
                return nil;
            }

            return negotiatedEncoding;
        }
    }

    @CoreMethod(names = "list", onSingleton = true)
    public abstract static class ListNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        RubyArray list() {
            return createArray(getContext().getEncodingManager().getEncodingList());
        }
    }


    @CoreMethod(names = "locale_charmap", onSingleton = true)
    public abstract static class LocaleCharacterMapNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        ImmutableRubyString localeCharacterMap() {
            final RubyEncoding rubyEncoding = getContext().getEncodingManager().getLocaleEncoding();
            return rubyEncoding.name;
        }
    }

    @CoreMethod(names = "dummy?")
    public abstract static class DummyNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        boolean isDummy(RubyEncoding encoding) {
            return encoding.isDummy;
        }
    }

    @CoreMethod(names = { "name", "to_s" })
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        ImmutableRubyString toS(RubyEncoding encoding) {
            return encoding.name;
        }
    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        Object allocate(RubyClass rubyClass) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }
    }

    @Primitive(name = "encoding_each_alias")
    public abstract static class EachAliasNode extends PrimitiveArrayArgumentsNode {

        @Child private TruffleString.FromByteArrayNode fromByteArrayNode = TruffleString.FromByteArrayNode.create();

        @TruffleBoundary
        @Specialization
        Object eachAlias(RubyProc block,
                @Cached CallBlockNode yieldNode) {
            var iterator = EncodingDB.getAliases().entryIterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                final RubyString aliasName = createString(
                        fromByteArrayNode,
                        ArrayUtils.extractRange(entry.bytes, entry.p, entry.end),
                        Encodings.US_ASCII); // CR_7BIT
                yieldNode.yield(
                        this,
                        block,
                        aliasName,
                        Encodings.getBuiltInEncoding(entry.value.getEncoding()));
            }
            return nil;
        }
    }

    @Primitive(name = "encoding_define_alias")
    public abstract static class DefineAliasNode extends PrimitiveArrayArgumentsNode {
        @TruffleBoundary
        @Specialization
        RubyEncoding defineAlias(RubyEncoding encoding, RubySymbol aliasName) {
            getContext().getEncodingManager().defineAlias(encoding.jcoding, aliasName.getString());
            return encoding;
        }
    }

    @Primitive(name = "encoding_is_unicode?")
    public abstract static class IsUnicodeNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        boolean isUnicode(RubyEncoding encoding) {
            return encoding.isUnicode;
        }
    }

    @Primitive(name = "get_actual_encoding")
    public abstract static class GetActualEncodingPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "libString.isRubyString(string)", limit = "1")
        static RubyEncoding getActualEncoding(Object string,
                @Cached GetActualEncodingNode getActualEncodingNode,
                @Cached RubyStringLibrary libString,
                @Bind("this") Node node) {
            return getActualEncodingNode.execute(node, libString.getTString(string), libString.getEncoding(string));
        }

    }

    // MRI: get_actual_encoding
    @GenerateInline
    @GenerateCached(false)
    public abstract static class GetActualEncodingNode extends RubyBaseNode {

        public abstract RubyEncoding execute(Node node, AbstractTruffleString tstring, RubyEncoding encoding);

        @Specialization(guards = "!encoding.isDummy")
        static RubyEncoding getActualEncoding(AbstractTruffleString tstring, RubyEncoding encoding) {
            return encoding;
        }

        @TruffleBoundary
        @Specialization(guards = "encoding.isDummy")
        static RubyEncoding getActualEncodingDummy(AbstractTruffleString tstring, RubyEncoding encoding,
                @Cached(inline = false) TruffleString.ReadByteNode readByteNode) {
            if (encoding.isUnicode) {
                var enc = encoding.tencoding;
                var byteLength = tstring.byteLength(enc);

                // handle dummy UTF-16 and UTF-32 by scanning for BOM, as in MRI
                if (encoding == Encodings.UTF16_DUMMY && byteLength >= 2) {
                    int c0 = readByteNode.execute(tstring, 0, enc);
                    int c1 = readByteNode.execute(tstring, 1, enc);

                    if (c0 == 0xFE && c1 == 0xFF) {
                        return Encodings.UTF16BE;
                    } else if (c0 == 0xFF && c1 == 0xFE) {
                        return Encodings.UTF16LE;
                    }
                    return Encodings.BINARY;
                } else if (encoding == Encodings.UTF32_DUMMY && byteLength >= 4) {
                    int c0 = readByteNode.execute(tstring, 0, enc);
                    int c1 = readByteNode.execute(tstring, 1, enc);
                    int c2 = readByteNode.execute(tstring, 2, enc);
                    int c3 = readByteNode.execute(tstring, 3, enc);

                    if (c0 == 0 && c1 == 0 && c2 == 0xFE && c3 == 0xFF) {
                        return Encodings.UTF32BE;
                    } else if (c3 == 0 && c2 == 0 && c1 == 0xFE && c0 == 0xFF) {
                        return Encodings.UTF32LE;
                    }
                    return Encodings.BINARY;
                }
            }

            return encoding;
        }
    }

    @Primitive(name = "encoding_get_default_encoding")
    public abstract static class GetDefaultEncodingNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        Object getDefaultEncoding(Object name) {
            final RubyEncoding encoding = getEncoding(RubyGuards.getJavaString(name));
            if (encoding == null) {
                return nil;
            } else {
                return encoding;
            }
        }

        @TruffleBoundary
        private RubyEncoding getEncoding(String name) {
            switch (name) {
                case "internal":
                    return getContext().getEncodingManager().getDefaultInternalEncoding();
                case "external":
                case "filesystem":
                    return getContext().getEncodingManager().getDefaultExternalEncoding();
                case "locale":
                    return getContext().getEncodingManager().getLocaleEncoding();
                default:
                    throw CompilerDirectives.shouldNotReachHere();
            }
        }
    }

    @Primitive(name = "encoding_set_default_external")
    public abstract static class SetDefaultExternalNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        RubyEncoding setDefaultExternal(RubyEncoding encoding) {
            getContext().getEncodingManager().setDefaultExternalEncoding(encoding);
            return encoding;
        }

        @Specialization
        RubyEncoding noDefaultExternal(Nil encoding) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().argumentError("default external can not be nil", this));
        }

    }

    @Primitive(name = "encoding_set_default_internal")
    public abstract static class SetDefaultInternalNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        RubyEncoding setDefaultInternal(RubyEncoding encoding) {
            getContext().getEncodingManager().setDefaultInternalEncoding(encoding);
            return encoding;
        }

        @Specialization
        Object noDefaultInternal(Nil encoding) {
            getContext().getEncodingManager().setDefaultInternalEncoding(null);
            return nil;
        }

    }

    // MRI: rb_obj_encoding and rb_enc_get_index
    @Primitive(name = "encoding_get_object_encoding")
    public abstract static class EncodingGetObjectEncodingNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        Object getObjectEncoding(Object object,
                @Cached ToRubyEncodingNode toRubyEncodingNode,
                @Cached InlinedConditionProfile nullProfile) {
            var rubyEncoding = toRubyEncodingNode.execute(this, object);
            if (nullProfile.profile(this, rubyEncoding == null)) {
                return nil;
            } else {
                return rubyEncoding;
            }
        }
    }

    public abstract static class EncodingCreationNode extends PrimitiveArrayArgumentsNode {

        public static RubyArray setIndexOrRaiseError(Node node, String name, RubyEncoding newEncoding) {
            if (newEncoding == null) {
                throw new RaiseException(
                        getContext(node),
                        coreExceptions(node).argumentErrorEncodingAlreadyRegistered(name, node));
            }

            final int index = newEncoding.index;
            return createArray(node, new Object[]{ newEncoding, index });
        }

    }

    @Primitive(name = "encoding_replicate")
    public abstract static class EncodingReplicateNode extends EncodingCreationNode {

        @Specialization(guards = "strings.isRubyString(nameObject)", limit = "1")
        static RubyArray encodingReplicate(RubyEncoding object, Object nameObject,
                @Cached RubyStringLibrary strings,
                @Cached ToJavaStringNode toJavaStringNode,
                @Bind("this") Node node) {
            final String name = toJavaStringNode.execute(node, nameObject);

            final RubyEncoding newEncoding = replicate(node, name, object);
            return setIndexOrRaiseError(node, name, newEncoding);
        }

        @TruffleBoundary
        private static RubyEncoding replicate(Node node, String name, RubyEncoding encoding) {
            if (getContext(node).getEncodingManager().getNumberOfEncodings() >= Encodings.MAX_NUMBER_OF_ENCODINGS) {
                throw new RaiseException(
                        getContext(node),
                        coreExceptions(node).encodingErrorTooManyEncodings(Encodings.MAX_NUMBER_OF_ENCODINGS, node));
            }

            return getContext(node).getEncodingManager().replicateEncoding(encoding, name);
        }

    }

    @Primitive(name = "encoding_create_dummy")
    public abstract static class DummyEncodingNode extends EncodingCreationNode {

        @Specialization(guards = "strings.isRubyString(nameObject)", limit = "1")
        static RubyArray createDummyEncoding(Object nameObject,
                @Cached RubyStringLibrary strings,
                @Cached ToJavaStringNode toJavaStringNode,
                @Bind("this") Node node) {
            final String name = toJavaStringNode.execute(node, nameObject);

            final RubyEncoding newEncoding = createDummy(node, name);
            return setIndexOrRaiseError(node, name, newEncoding);
        }

        @TruffleBoundary
        private static RubyEncoding createDummy(Node node, String name) {
            if (getContext(node).getEncodingManager().getNumberOfEncodings() >= Encodings.MAX_NUMBER_OF_ENCODINGS) {
                throw new RaiseException(
                        getContext(node),
                        coreExceptions(node).encodingErrorTooManyEncodings(Encodings.MAX_NUMBER_OF_ENCODINGS, node));
            }

            return getContext(node).getEncodingManager().createDummyEncoding(name);
        }

    }

    @Primitive(name = "encoding_get_encoding_by_index", lowerFixnum = 0)
    public abstract static class GetEncodingObjectByIndexNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "isSingleContext()", "index == cachedIndex" }, limit = "getCacheLimit()")
        RubyEncoding getEncoding(int index,
                @Cached("index") int cachedIndex,
                @Cached("getContext().getEncodingManager().getRubyEncoding(index)") RubyEncoding cachedEncoding) {
            return cachedEncoding;
        }

        @Specialization(replaces = "getEncoding")
        RubyEncoding getEncodingUncached(int index) {
            return getContext().getEncodingManager().getRubyEncoding(index);
        }

        protected int getCacheLimit() {
            return getLanguage().options.ENCODING_LOADED_CLASSES_CACHE;
        }
    }

    @Primitive(name = "encoding_get_encoding_index")
    public abstract static class GetEncodingIndexNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        int getIndex(RubyEncoding encoding) {
            return encoding.index;
        }
    }

    // MRI: rb_enc_check_str / rb_encoding_check (with Ruby String arguments)
    // Like strings_compatible? but raises if incompatible
    @Primitive(name = "encoding_ensure_compatible_str")
    public abstract static class CheckStringEncodingPrimitiveNode extends PrimitiveArrayArgumentsNode {
        @Specialization(guards = { "libFirst.isRubyString(first)", "libSecond.isRubyString(second)", }, limit = "1")
        static RubyEncoding checkEncodingStringString(Object first, Object second,
                @Cached RubyStringLibrary libFirst,
                @Cached RubyStringLibrary libSecond,
                @Cached InlinedBranchProfile errorProfile,
                @Cached NegotiateCompatibleStringEncodingNode negotiateCompatibleStringEncodingNode,
                @Bind("this") Node node) {
            final RubyEncoding firstEncoding = libFirst.getEncoding(first);
            final RubyEncoding secondEncoding = libSecond.getEncoding(second);

            final RubyEncoding negotiatedEncoding = negotiateCompatibleStringEncodingNode
                    .execute(node, libFirst.getTString(first), firstEncoding, libSecond.getTString(second),
                            secondEncoding);

            if (negotiatedEncoding == null) {
                errorProfile.enter(node);
                throw new RaiseException(getContext(node),
                        coreExceptions(node).encodingCompatibilityErrorIncompatible(firstEncoding, secondEncoding,
                                node));
            }

            return negotiatedEncoding;
        }
    }

    // MRI: rb_enc_check_str / rb_encoding_check (with RopeWithEncoding arguments)
    @GenerateInline
    @GenerateCached(false)
    @GenerateUncached
    public abstract static class CheckStringEncodingNode extends RubyBaseNode {

        public abstract RubyEncoding executeCheckEncoding(Node node, AbstractTruffleString first,
                RubyEncoding firstEncoding, AbstractTruffleString second, RubyEncoding secondEncoding);

        @Specialization
        static RubyEncoding checkEncoding(
                Node node,
                AbstractTruffleString first,
                RubyEncoding firstEncoding,
                AbstractTruffleString second,
                RubyEncoding secondEncoding,
                @Cached InlinedBranchProfile errorProfile,
                @Cached NegotiateCompatibleStringEncodingNode negotiateCompatibleEncodingNode) {
            var negotiatedEncoding = negotiateCompatibleEncodingNode.execute(node, first, firstEncoding, second,
                    secondEncoding);

            if (negotiatedEncoding == null) {
                errorProfile.enter(node);
                throw new RaiseException(getContext(node),
                        coreExceptions(node).encodingCompatibilityErrorIncompatible(firstEncoding, secondEncoding,
                                node));
            }

            return negotiatedEncoding;
        }

    }

    // MRI: rb_enc_check / rb_encoding_check
    @Primitive(name = "encoding_ensure_compatible")
    public abstract static class EncodingCheckEncodingNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        RubyEncoding checkEncoding(Object first, Object second,
                @Cached CheckEncodingNode checkEncodingNode) {
            return checkEncodingNode.execute(this, first, second);
        }
    }

    @GenerateInline
    @GenerateCached(false)
    public abstract static class CheckEncodingNode extends RubyBaseNode {

        public abstract RubyEncoding execute(Node node, Object first, Object second);

        @Specialization
        static RubyEncoding checkEncoding(Node node, Object first, Object second,
                @Cached ToRubyEncodingNode toRubyEncodingNode,
                @Cached NegotiateCompatibleEncodingNode negotiateCompatibleEncodingNode,
                @Cached InlinedBranchProfile errorProfile) {
            final var firstEncoding = toRubyEncodingNode.execute(node, first);
            final var secondEncoding = toRubyEncodingNode.execute(node, second);
            final RubyEncoding negotiatedEncoding = negotiateCompatibleEncodingNode.execute(node, first,
                    firstEncoding, second, secondEncoding);

            if (negotiatedEncoding == null) {
                errorProfile.enter(node);
                throw new RaiseException(getContext(node), coreExceptions(node).encodingCompatibilityErrorIncompatible(
                        firstEncoding,
                        secondEncoding,
                        node));
            }

            return negotiatedEncoding;
        }
    }

    @Primitive(name = "encoding_unicode_version")
    public abstract static class UnicodeVersionNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        RubyString getUnicodeVersion(
                @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
            return createString(fromJavaStringNode, Config.UNICODE_VERSION_STRING, Encodings.UTF_8);
        }
    }

    @Primitive(name = "encoding_unicode_emoji_version")
    public abstract static class UnicodeEmojiVersionNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        RubyString getUnicodeEmojiVersion(
                @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
            return createString(fromJavaStringNode, Config.UNICODE_EMOJI_VERSION_STRING, Encodings.UTF_8);
        }
    }
}
