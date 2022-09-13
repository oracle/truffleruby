/*
 * Copyright (c) 2014, 2021 Oracle and/or its affiliates. All rights reserved. This
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

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.TruffleString;
import org.jcodings.EncodingDB;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.ToRubyEncodingNode;
import org.truffleruby.core.encoding.EncodingNodesFactory.NegotiateCompatibleEncodingNodeGen;
import org.truffleruby.core.encoding.EncodingNodesFactory.NegotiateCompatibleStringEncodingNodeGen;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringGuards;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.yield.CallBlockNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

import static com.oracle.truffle.api.strings.TruffleString.CodeRange.ASCII;

@CoreModule(value = "Encoding", isClass = true)
public abstract class EncodingNodes {

    @CoreMethod(names = "ascii_compatible?")
    public abstract static class AsciiCompatibleNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected boolean isAsciiCompatible(RubyEncoding encoding) {
            return encoding.isAsciiCompatible;
        }
    }

    // MRI: enc_compatible_str and enc_compatible_latter
    @ImportStatic(TruffleString.CodeRange.class)
    public abstract static class NegotiateCompatibleStringEncodingNode extends RubyBaseNode {

        @Child TruffleString.GetByteCodeRangeNode codeRangeNode;

        public abstract RubyEncoding execute(AbstractTruffleString first, RubyEncoding firstEncoding,
                AbstractTruffleString second, RubyEncoding secondEncoding);

        public static NegotiateCompatibleStringEncodingNode create() {
            return NegotiateCompatibleStringEncodingNodeGen.create();
        }

        @Specialization(guards = {
                "firstEncoding == cachedEncoding",
                "secondEncoding == cachedEncoding",
        }, limit = "getCacheLimit()")
        protected RubyEncoding negotiateSameEncodingCached(
                AbstractTruffleString first,
                RubyEncoding firstEncoding,
                AbstractTruffleString second,
                RubyEncoding secondEncoding,
                @Cached("firstEncoding") RubyEncoding cachedEncoding) {
            assert first.isCompatibleTo(firstEncoding.tencoding) && second.isCompatibleTo(secondEncoding.tencoding);
            return cachedEncoding;
        }


        @Specialization(guards = "firstEncoding == secondEncoding", replaces = "negotiateSameEncodingCached")
        protected RubyEncoding negotiateSameEncodingUncached(
                AbstractTruffleString first,
                RubyEncoding firstEncoding,
                AbstractTruffleString second,
                RubyEncoding secondEncoding) {
            assert first.isCompatibleTo(firstEncoding.tencoding) && second.isCompatibleTo(secondEncoding.tencoding);
            return firstEncoding;
        }

        @Specialization(guards = {
                "firstEncoding != secondEncoding",
                "firstEncoding == cachedEncoding",
                "isStandardEncoding(cachedEncoding)",
                "getCodeRange(second, secondEncoding) == ASCII"
        })
        protected RubyEncoding negotiateStandardEncodingAndCr7Bit(
                AbstractTruffleString first,
                RubyEncoding firstEncoding,
                AbstractTruffleString second,
                RubyEncoding secondEncoding,
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
                        "getCodeRange(first, firstEncoding) == firstCodeRange",
                        "getCodeRange(second, secondEncoding) == secondCodeRange" },
                limit = "getCacheLimit()")
        protected RubyEncoding negotiateRopeRopeCached(
                AbstractTruffleString first,
                RubyEncoding firstEncoding,
                AbstractTruffleString second,
                RubyEncoding secondEncoding,
                @Cached("first.isEmpty()") boolean isFirstEmpty,
                @Cached("second.isEmpty()") boolean isSecondEmpty,
                @Cached("getCodeRange(first, firstEncoding)") TruffleString.CodeRange firstCodeRange,
                @Cached("getCodeRange(second, secondEncoding)") TruffleString.CodeRange secondCodeRange,
                @Cached("firstEncoding") RubyEncoding cachedFirstEncoding,
                @Cached("secondEncoding") RubyEncoding cachedSecondEncoding,
                @Cached("compatibleEncodingForRopes(first, firstEncoding, second, secondEncoding)") RubyEncoding negotiatedEncoding) {
            assert first.isCompatibleTo(firstEncoding.tencoding) && second.isCompatibleTo(secondEncoding.tencoding);
            return negotiatedEncoding;
        }

        @Specialization(guards = "firstEncoding != secondEncoding", replaces = "negotiateRopeRopeCached")
        protected RubyEncoding negotiateRopeRopeUncached(
                AbstractTruffleString first,
                RubyEncoding firstEncoding,
                AbstractTruffleString second,
                RubyEncoding secondEncoding) {
            assert first.isCompatibleTo(firstEncoding.tencoding) && second.isCompatibleTo(secondEncoding.tencoding);
            return compatibleEncodingForRopes(first, firstEncoding, second, secondEncoding);
        }

        /** This method returns non-null if either:
         * <ul>
         * <li>one side is empty</li>
         * <li>one side is 7-bit and both encodings are ascii-compatible</li>
         * </ul>
         */
        @TruffleBoundary
        protected RubyEncoding compatibleEncodingForRopes(AbstractTruffleString firstRope, RubyEncoding firstEncoding,
                AbstractTruffleString secondRope, RubyEncoding secondEncoding) {
            // MRI: enc_compatible_latter
            assert firstEncoding != secondEncoding : "this method assumes the encodings are different";

            if (secondRope.isEmpty()) {
                return firstEncoding;
            }
            if (firstRope.isEmpty()) {
                return (firstEncoding.isAsciiCompatible &&
                        StringGuards.is7Bit(secondRope, secondEncoding, getCodeRangeNode()))
                                ? firstEncoding
                                : secondEncoding;
            }

            if (!firstEncoding.isAsciiCompatible || !secondEncoding.isAsciiCompatible) {
                return null;
            }

            if (StringGuards.is7Bit(secondRope, secondEncoding, getCodeRangeNode())) {
                return firstEncoding;
            }
            if (StringGuards.is7Bit(firstRope, firstEncoding, getCodeRangeNode())) {
                return secondEncoding;
            }

            return null;
        }

        protected int getCacheLimit() {
            return getLanguage().options.ENCODING_COMPATIBLE_QUERY_CACHE;
        }

        protected TruffleString.CodeRange getCodeRange(AbstractTruffleString string, RubyEncoding encoding) {
            return getCodeRangeNode().execute(string, encoding.tencoding);
        }

        private TruffleString.GetByteCodeRangeNode getCodeRangeNode() {
            if (codeRangeNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                codeRangeNode = insert(TruffleString.GetByteCodeRangeNode.create());
            }

            return codeRangeNode;
        }

        /** Indicates whether the encoding is one of the runtime-default encodings. Many (most?) applications do not
         * override the default encodings and as such, this set of encodings is used very frequently in real-world Ruby
         * applications. */
        protected boolean isStandardEncoding(RubyEncoding encoding) {
            return encoding == Encodings.UTF_8 || encoding == Encodings.US_ASCII || encoding == Encodings.BINARY;
        }


    }

    // MRI: enc_compatible_latter
    public abstract static class NegotiateCompatibleEncodingNode extends RubyBaseNode {

        @Child private TruffleString.GetByteCodeRangeNode codeRangeNode;
        @Child private ToRubyEncodingNode getEncodingNode = ToRubyEncodingNode.create();

        public static NegotiateCompatibleEncodingNode create() {
            return NegotiateCompatibleEncodingNodeGen.create();
        }

        public abstract RubyEncoding executeNegotiate(Object first, Object second);

        @Specialization(
                guards = {
                        "getEncoding(first) == cachedEncoding",
                        "getEncoding(second) == cachedEncoding",
                        "cachedEncoding != null" },
                limit = "getCacheLimit()")
        protected RubyEncoding negotiateSameEncodingCached(Object first, Object second,
                @Cached("getEncoding(first)") RubyEncoding cachedEncoding) {
            return cachedEncoding;
        }

        @Specialization(
                guards = { "getEncoding(first) == getEncoding(second)", "getEncoding(first) != null" },
                replaces = "negotiateSameEncodingCached")
        protected RubyEncoding negotiateSameEncodingUncached(Object first, Object second) {
            return getEncoding(first);
        }

        @Specialization(guards = { "libFirst.isRubyString(first)", "libSecond.isRubyString(second)" }, limit = "1")
        protected RubyEncoding negotiateStringStringEncoding(Object first, Object second,
                @Cached RubyStringLibrary libFirst,
                @Cached RubyStringLibrary libSecond,
                @Cached NegotiateCompatibleStringEncodingNode ropeNode) {
            final RubyEncoding firstEncoding = libFirst.getEncoding(first);
            final RubyEncoding secondEncoding = libSecond.getEncoding(second);
            return ropeNode.execute(
                    libFirst.getTString(first),
                    firstEncoding,
                    libSecond.getTString(second),
                    secondEncoding);
        }

        @Specialization(
                guards = {
                        "libFirst.isRubyString(first)",
                        "isNotRubyString(second)",
                        "getCodeRange(first, libFirst) == codeRange",
                        "getEncoding(first) == firstEncoding",
                        "getEncoding(second) == secondEncoding",
                        "firstEncoding != secondEncoding" },
                limit = "getCacheLimit()")
        protected RubyEncoding negotiateStringObjectCached(Object first, Object second,
                @Cached RubyStringLibrary libFirst,
                @Cached RubyStringLibrary libSecond,
                @Cached("getEncoding(first)") RubyEncoding firstEncoding,
                @Cached("getEncoding(second)") RubyEncoding secondEncoding,
                @Cached("getCodeRange(first, libFirst)") TruffleString.CodeRange codeRange,
                @Cached("negotiateStringObjectUncached(first, second, libFirst)") RubyEncoding negotiatedEncoding) {
            return negotiatedEncoding;
        }

        @Specialization(
                guards = {
                        "libFirst.isRubyString(first)",
                        "getEncoding(first) != getEncoding(second)",
                        "isNotRubyString(second)" },
                replaces = "negotiateStringObjectCached", limit = "1")
        protected RubyEncoding negotiateStringObjectUncached(Object first, Object second,
                @Cached RubyStringLibrary libFirst) {
            final RubyEncoding firstEncoding = getEncoding(first);
            final RubyEncoding secondEncoding = getEncoding(second);

            if (secondEncoding == null) {
                return null;
            }

            if (!firstEncoding.isAsciiCompatible || !secondEncoding.isAsciiCompatible) {
                return null;
            }

            if (secondEncoding == Encodings.US_ASCII) {
                return firstEncoding;
            }

            if (getCodeRange(first, libFirst) == ASCII) {
                return secondEncoding;
            }

            return null;
        }

        @Specialization(
                guards = {
                        "libSecond.isRubyString(second)",
                        "getEncoding(first) != getEncoding(second)",
                        "isNotRubyString(first)" },
                limit = "1")
        protected RubyEncoding negotiateObjectString(Object first, Object second,
                @Cached RubyStringLibrary libSecond) {
            return negotiateStringObjectUncached(second, first, libSecond);
        }

        @Specialization(
                guards = {
                        "firstEncoding != secondEncoding",
                        "isNotRubyString(first)",
                        "isNotRubyString(second)",
                        "firstEncoding != null",
                        "secondEncoding != null",
                        "getEncoding(first) == firstEncoding",
                        "getEncoding(second) == secondEncoding", },
                limit = "getCacheLimit()")
        protected RubyEncoding negotiateObjectObjectCached(Object first, Object second,
                @Cached("getEncoding(first)") RubyEncoding firstEncoding,
                @Cached("getEncoding(second)") RubyEncoding secondEncoding,
                @Cached("areCompatible(firstEncoding, secondEncoding)") RubyEncoding negotiatedEncoding) {

            return negotiatedEncoding;
        }

        @Specialization(
                guards = {
                        "getEncoding(first) != getEncoding(second)",
                        "isNotRubyString(first)",
                        "isNotRubyString(second)" },
                replaces = "negotiateObjectObjectCached")
        protected RubyEncoding negotiateObjectObjectUncached(Object first, Object second) {
            final RubyEncoding firstEncoding = getEncoding(first);
            final RubyEncoding secondEncoding = getEncoding(second);

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

        protected TruffleString.CodeRange getCodeRange(Object string, RubyStringLibrary libString) {
            if (codeRangeNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                codeRangeNode = insert(TruffleString.GetByteCodeRangeNode.create());
            }

            return codeRangeNode.execute(libString.getTString(string), libString.getTEncoding(string));
        }

        protected RubyEncoding getEncoding(Object value) {
            return getEncodingNode.executeToEncoding(value);
        }

        protected int getCacheLimit() {
            return getLanguage().options.ENCODING_COMPATIBLE_QUERY_CACHE;
        }

    }

    // MRI: rb_enc_compatible
    @Primitive(name = "encoding_compatible?")
    public abstract static class CompatibleQueryNode extends CoreMethodArrayArgumentsNode {

        @Child private NegotiateCompatibleEncodingNode negotiateCompatibleEncodingNode = NegotiateCompatibleEncodingNode
                .create();

        public static CompatibleQueryNode create() {
            return EncodingNodesFactory.CompatibleQueryNodeFactory.create(null);
        }

        @Specialization
        protected Object isCompatible(Object first, Object second,
                @Cached ConditionProfile noNegotiatedEncodingProfile) {
            final RubyEncoding negotiatedEncoding = negotiateCompatibleEncodingNode.executeNegotiate(first, second);

            if (noNegotiatedEncodingProfile.profile(negotiatedEncoding == null)) {
                return nil;
            }

            return negotiatedEncoding;
        }
    }

    // encoding_compatible? but only accepting Strings for better footprint
    @Primitive(name = "strings_compatible?")
    public abstract static class AreStringsCompatibleNode extends CoreMethodArrayArgumentsNode {
        public static AreStringsCompatibleNode create() {
            return EncodingNodesFactory.AreStringsCompatibleNodeFactory.create(null);
        }

        @Specialization
        protected Object areCompatible(Object first, Object second,
                @Cached RubyStringLibrary libFirst,
                @Cached RubyStringLibrary libSecond,
                @Cached NegotiateCompatibleStringEncodingNode negotiateCompatibleStringEncodingNode,
                @Cached ConditionProfile noNegotiatedEncodingProfile) {
            final RubyEncoding negotiatedEncoding = negotiateCompatibleStringEncodingNode.execute(
                    libFirst.getTString(first), libFirst.getEncoding(first),
                    libSecond.getTString(second), libSecond.getEncoding(second));

            if (noNegotiatedEncodingProfile.profile(negotiatedEncoding == null)) {
                return nil;
            }

            return negotiatedEncoding;
        }
    }

    @CoreMethod(names = "list", onSingleton = true)
    public abstract static class ListNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyArray list() {
            return createArray(getContext().getEncodingManager().getEncodingList());
        }
    }


    @CoreMethod(names = "locale_charmap", onSingleton = true)
    public abstract static class LocaleCharacterMapNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected ImmutableRubyString localeCharacterMap() {
            final RubyEncoding rubyEncoding = getContext().getEncodingManager().getLocaleEncoding();
            return rubyEncoding.name;
        }
    }

    @CoreMethod(names = "dummy?")
    public abstract static class DummyNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected boolean isDummy(RubyEncoding encoding) {
            return encoding.isDummy;
        }
    }

    @CoreMethod(names = { "name", "to_s" })
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected ImmutableRubyString toS(RubyEncoding encoding) {
            return encoding.name;
        }
    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object allocate(RubyClass rubyClass) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }
    }

    @Primitive(name = "encoding_each_alias")
    public abstract static class EachAliasNode extends PrimitiveArrayArgumentsNode {

        @Child private CallBlockNode yieldNode = CallBlockNode.create();
        @Child private TruffleString.FromByteArrayNode fromByteArrayNode = TruffleString.FromByteArrayNode.create();

        @TruffleBoundary
        @Specialization
        protected Object eachAlias(RubyProc block) {
            var iterator = EncodingDB.getAliases().entryIterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                final RubyString aliasName = createString(
                        fromByteArrayNode,
                        ArrayUtils.extractRange(entry.bytes, entry.p, entry.end),
                        Encodings.US_ASCII); // CR_7BIT
                yieldNode.yield(
                        block,
                        aliasName,
                        Encodings.getBuiltInEncoding(entry.value.getEncoding()));
            }
            return nil;
        }
    }

    @Primitive(name = "encoding_is_unicode")
    public abstract static class IsUnicodeNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected boolean isUnicode(RubyEncoding encoding) {
            return encoding.isUnicode;
        }
    }

    @Primitive(name = "get_actual_encoding")
    public abstract static class GetActualEncodingPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "libString.isRubyString(string)", limit = "1")
        protected RubyEncoding getActualEncoding(Object string,
                @Cached GetActualEncodingNode getActualEncodingNode,
                @Cached RubyStringLibrary libString) {
            return getActualEncodingNode.execute(libString.getTString(string), libString.getEncoding(string));
        }

    }

    // MRI: get_actual_encoding
    public abstract static class GetActualEncodingNode extends RubyBaseNode {

        public static GetActualEncodingNode create() {
            return EncodingNodesFactory.GetActualEncodingNodeGen.create();
        }

        public abstract RubyEncoding execute(AbstractTruffleString tstring, RubyEncoding encoding);

        @Specialization(guards = "!encoding.isDummy")
        protected RubyEncoding getActualEncoding(AbstractTruffleString tstring, RubyEncoding encoding) {
            return encoding;
        }

        @TruffleBoundary
        @Specialization(guards = "encoding.isDummy")
        protected RubyEncoding getActualEncodingDummy(AbstractTruffleString tstring, RubyEncoding encoding,
                @Cached TruffleString.ReadByteNode readByteNode) {
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
        protected Object getDefaultEncoding(Object name) {
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
        protected RubyEncoding setDefaultExternal(RubyEncoding encoding) {
            getContext().getEncodingManager().setDefaultExternalEncoding(encoding);
            return encoding;
        }

        @Specialization
        protected RubyEncoding noDefaultExternal(Nil encoding) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().argumentError("default external can not be nil", this));
        }

    }

    @Primitive(name = "encoding_set_default_internal")
    public abstract static class SetDefaultInternalNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyEncoding setDefaultInternal(RubyEncoding encoding) {
            getContext().getEncodingManager().setDefaultInternalEncoding(encoding);
            return encoding;
        }

        @Specialization
        protected Object noDefaultInternal(Nil encoding) {
            getContext().getEncodingManager().setDefaultInternalEncoding(null);
            return nil;
        }

    }

    // MRI: rb_obj_encoding and rb_enc_get_index
    @Primitive(name = "encoding_get_object_encoding")
    public abstract static class EncodingGetObjectEncodingNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object getObjectEncoding(Object object,
                @Cached ToRubyEncodingNode toRubyEncodingNode,
                @Cached ConditionProfile nullProfile) {
            var rubyEncoding = toRubyEncodingNode.executeToEncoding(object);
            if (nullProfile.profile(rubyEncoding == null)) {
                return nil;
            } else {
                return rubyEncoding;
            }
        }
    }

    public abstract static class EncodingCreationNode extends PrimitiveArrayArgumentsNode {

        public RubyArray setIndexOrRaiseError(String name, RubyEncoding newEncoding) {
            if (newEncoding == null) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentErrorEncodingAlreadyRegistered(name, this));
            }

            final int index = newEncoding.index;
            return createArray(new Object[]{ newEncoding, index });
        }

    }

    @Primitive(name = "encoding_replicate")
    public abstract static class EncodingReplicateNode extends EncodingCreationNode {

        @Specialization(guards = "strings.isRubyString(nameObject)", limit = "1")
        protected RubyArray encodingReplicate(RubyEncoding object, Object nameObject,
                @Cached RubyStringLibrary strings,
                @Cached ToJavaStringNode toJavaStringNode) {
            final String name = toJavaStringNode.executeToJavaString(nameObject);

            final RubyEncoding newEncoding = replicate(name, object);
            return setIndexOrRaiseError(name, newEncoding);
        }

        @TruffleBoundary
        private RubyEncoding replicate(String name, RubyEncoding encoding) {
            return getContext().getEncodingManager().replicateEncoding(encoding, name);
        }

    }

    @Primitive(name = "encoding_create_dummy")
    public abstract static class DummyEncodingNode extends EncodingCreationNode {

        @Specialization(guards = "strings.isRubyString(nameObject)", limit = "1")
        protected RubyArray createDummyEncoding(Object nameObject,
                @Cached RubyStringLibrary strings,
                @Cached ToJavaStringNode toJavaStringNode) {
            final String name = toJavaStringNode.executeToJavaString(nameObject);

            final RubyEncoding newEncoding = createDummy(name);
            return setIndexOrRaiseError(name, newEncoding);
        }

        @TruffleBoundary
        private RubyEncoding createDummy(String name) {
            return getContext().getEncodingManager().createDummyEncoding(name);
        }

    }

    @Primitive(name = "encoding_get_encoding_by_index", lowerFixnum = 0)
    public abstract static class GetEncodingObjectByIndexNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = { "isSingleContext()", "index == cachedIndex" }, limit = "getCacheLimit()")
        protected RubyEncoding getEncoding(int index,
                @Cached("index") int cachedIndex,
                @Cached("getContext().getEncodingManager().getRubyEncoding(index)") RubyEncoding cachedEncoding) {
            return cachedEncoding;
        }

        @Specialization(replaces = "getEncoding")
        protected RubyEncoding getEncodingUncached(int index) {
            return getContext().getEncodingManager().getRubyEncoding(index);
        }

        protected int getCacheLimit() {
            return getLanguage().options.ENCODING_LOADED_CLASSES_CACHE;
        }
    }

    @Primitive(name = "encoding_get_encoding_index")
    public abstract static class GetEncodingIndexNode extends PrimitiveArrayArgumentsNode {
        @Specialization
        protected int getIndex(RubyEncoding encoding) {
            return encoding.index;
        }
    }

    // MRI: rb_enc_check_str / rb_encoding_check (with Ruby String arguments)
    @Primitive(name = "encoding_ensure_compatible_str")
    public abstract static class CheckStringEncodingPrimitiveNode extends PrimitiveArrayArgumentsNode {
        @Specialization(guards = { "libFirst.isRubyString(first)", "libSecond.isRubyString(second)", }, limit = "1")
        protected RubyEncoding checkEncodingStringString(Object first, Object second,
                @Cached RubyStringLibrary libFirst,
                @Cached RubyStringLibrary libSecond,
                @Cached BranchProfile errorProfile,
                @Cached NegotiateCompatibleStringEncodingNode negotiateCompatibleStringEncodingNode) {
            final RubyEncoding firstEncoding = libFirst.getEncoding(first);
            final RubyEncoding secondEncoding = libSecond.getEncoding(second);

            final RubyEncoding negotiatedEncoding = negotiateCompatibleStringEncodingNode
                    .execute(
                            libFirst.getTString(first),
                            firstEncoding,
                            libSecond.getTString(second),
                            secondEncoding);

            if (negotiatedEncoding == null) {
                errorProfile.enter();
                throw new RaiseException(getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatible(firstEncoding, secondEncoding, this));
            }

            return negotiatedEncoding;
        }
    }

    // MRI: rb_enc_check_str / rb_encoding_check (with RopeWithEncoding arguments)
    public abstract static class CheckStringEncodingNode extends RubyBaseNode {

        public static CheckStringEncodingNode create() {
            return EncodingNodesFactory.CheckStringEncodingNodeGen.create();
        }

        public abstract RubyEncoding executeCheckEncoding(AbstractTruffleString first, RubyEncoding firstEncoding,
                AbstractTruffleString second, RubyEncoding secondEncoding);

        @Specialization
        protected RubyEncoding checkEncoding(
                AbstractTruffleString first,
                RubyEncoding firstEncoding,
                AbstractTruffleString second,
                RubyEncoding secondEncoding,
                @Cached BranchProfile errorProfile,
                @Cached NegotiateCompatibleStringEncodingNode negotiateCompatibleEncodingNode) {
            var negotiatedEncoding = negotiateCompatibleEncodingNode.execute(first, firstEncoding, second,
                    secondEncoding);

            if (negotiatedEncoding == null) {
                errorProfile.enter();
                throw new RaiseException(getContext(),
                        coreExceptions().encodingCompatibilityErrorIncompatible(firstEncoding, secondEncoding, this));
            }

            return negotiatedEncoding;
        }

    }

    // MRI: rb_enc_check / rb_encoding_check
    @Primitive(name = "encoding_ensure_compatible")
    public abstract static class CheckEncodingNode extends PrimitiveArrayArgumentsNode {

        @Child private NegotiateCompatibleEncodingNode negotiateCompatibleEncodingNode;
        @Child private ToRubyEncodingNode toRubyEncodingNode;

        public static CheckEncodingNode create() {
            return EncodingNodesFactory.CheckEncodingNodeFactory.create(null);
        }

        public abstract RubyEncoding executeCheckEncoding(Object first, Object second);

        @Specialization
        protected RubyEncoding checkEncoding(Object first, Object second,
                @Cached BranchProfile errorProfile) {
            final RubyEncoding negotiatedEncoding = executeNegotiate(first, second);

            if (negotiatedEncoding == null) {
                errorProfile.enter();
                raiseException(first, second);
            }

            return negotiatedEncoding;
        }

        private RubyEncoding executeNegotiate(Object first, Object second) {
            if (negotiateCompatibleEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                negotiateCompatibleEncodingNode = insert(NegotiateCompatibleEncodingNode.create());
            }
            return negotiateCompatibleEncodingNode.executeNegotiate(first, second);
        }

        private void raiseException(Object first, Object second) {
            if (toRubyEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toRubyEncodingNode = insert(ToRubyEncodingNode.create());
            }

            throw new RaiseException(getContext(), coreExceptions().encodingCompatibilityErrorIncompatible(
                    toRubyEncodingNode.executeToEncoding(first),
                    toRubyEncodingNode.executeToEncoding(second),
                    this));
        }

    }

}
