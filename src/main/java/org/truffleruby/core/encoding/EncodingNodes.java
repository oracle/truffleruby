/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
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

import org.jcodings.Encoding;
import org.jcodings.EncodingDB;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF16BEEncoding;
import org.jcodings.specific.UTF16LEEncoding;
import org.jcodings.specific.UTF32BEEncoding;
import org.jcodings.specific.UTF32LEEncoding;
import org.jcodings.unicode.UnicodeEncoding;
import org.jcodings.util.CaseInsensitiveBytesHash;
import org.jcodings.util.Hash;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.ToEncodingNode;
import org.truffleruby.core.encoding.EncodingNodesFactory.CheckRopeEncodingNodeGen;
import org.truffleruby.core.encoding.EncodingNodesFactory.GetRubyEncodingNodeGen;
import org.truffleruby.core.encoding.EncodingNodesFactory.NegotiateCompatibleEncodingNodeGen;
import org.truffleruby.core.encoding.EncodingNodesFactory.NegotiateCompatibleRopeEncodingNodeGen;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.regexp.RubyRegexp;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringNodes.MakeStringNode;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.yield.YieldNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

@CoreModule(value = "Encoding", isClass = true)
public abstract class EncodingNodes {

    @ReportPolymorphism
    @CoreMethod(names = "ascii_compatible?")
    public abstract static class AsciiCompatibleNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "encoding == cachedEncoding", limit = "getIdentityCacheLimit()")
        protected boolean isAsciiCompatibleCached(RubyEncoding encoding,
                @Cached("encoding") RubyEncoding cachedEncoding,
                @Cached("isAsciiCompatible(cachedEncoding)") boolean isAsciiCompatible) {
            return isAsciiCompatible;
        }

        @Specialization(replaces = "isAsciiCompatibleCached")
        protected boolean isAsciiCompatibleUncached(RubyEncoding encoding) {
            return isAsciiCompatible(encoding);
        }

        protected static boolean isAsciiCompatible(RubyEncoding encoding) {
            return encoding.encoding.isAsciiCompatible();
        }
    }

    public abstract static class GetRubyEncodingNode extends RubyContextNode {

        public static GetRubyEncodingNode create() {
            return GetRubyEncodingNodeGen.create();
        }

        public abstract RubyEncoding executeGetRubyEncoding(Encoding encoding);

        @Specialization(guards = "isSameEncoding(encoding, cachedRubyEncoding)", limit = "getCacheLimit()")
        protected RubyEncoding getRubyEncodingCached(Encoding encoding,
                @Cached("getRubyEncodingUncached(encoding)") RubyEncoding cachedRubyEncoding) {
            return cachedRubyEncoding;
        }

        @Specialization(replaces = "getRubyEncodingCached")
        protected RubyEncoding getRubyEncodingUncached(Encoding encoding) {
            return getContext().getEncodingManager().getRubyEncoding(encoding);
        }

        protected boolean isSameEncoding(Encoding encoding, RubyEncoding rubyEncoding) {
            return encoding == rubyEncoding.encoding;
        }

        protected int getCacheLimit() {
            return getContext().getOptions().ENCODING_LOADED_CLASSES_CACHE;
        }

    }

    public static abstract class NegotiateCompatibleRopeEncodingNode extends RubyContextNode {

        public abstract Encoding executeNegotiate(Rope first, Rope second);

        public static NegotiateCompatibleRopeEncodingNode create() {
            return NegotiateCompatibleRopeEncodingNodeGen.create();
        }

        @Specialization(guards = "first.getEncoding() == second.getEncoding()")
        protected Encoding negotiateSameEncodingUncached(Rope first, Rope second) {
            return first.getEncoding();
        }

        @Specialization(
                guards = {
                        "firstEncoding != secondEncoding",
                        "first.isEmpty() == isFirstEmpty",
                        "second.isEmpty() == isSecondEmpty",
                        "first.getEncoding() == firstEncoding",
                        "second.getEncoding() == secondEncoding",
                        "codeRangeNode.execute(first) == firstCodeRange",
                        "codeRangeNode.execute(second) == secondCodeRange" },
                limit = "getCacheLimit()")
        protected Encoding negotiateRopeRopeCached(Rope first, Rope second,
                @Cached("first.getEncoding()") Encoding firstEncoding,
                @Cached("second.getEncoding()") Encoding secondEncoding,
                @Cached("first.isEmpty()") boolean isFirstEmpty,
                @Cached("second.isEmpty()") boolean isSecondEmpty,
                @Cached("first.getCodeRange()") CodeRange firstCodeRange,
                @Cached("second.getCodeRange()") CodeRange secondCodeRange,
                @Cached("negotiateRopeRopeUncached(first, second)") Encoding negotiatedEncoding,
                @Cached RopeNodes.CodeRangeNode codeRangeNode) {
            return negotiatedEncoding;
        }

        @Specialization(guards = "first.getEncoding() != second.getEncoding()", replaces = "negotiateRopeRopeCached")
        protected Encoding negotiateRopeRopeUncached(Rope first, Rope second) {
            return compatibleEncodingForRopes(first, second);
        }

        @TruffleBoundary
        private static Encoding compatibleEncodingForRopes(Rope firstRope, Rope secondRope) {
            // Taken from org.jruby.RubyEncoding#areCompatible.

            final Encoding firstEncoding = firstRope.getEncoding();
            final Encoding secondEncoding = secondRope.getEncoding();

            if (secondRope.isEmpty()) {
                return firstEncoding;
            }
            if (firstRope.isEmpty()) {
                return firstEncoding.isAsciiCompatible() && (secondRope.getCodeRange() == CodeRange.CR_7BIT)
                        ? firstEncoding
                        : secondEncoding;
            }

            if (!firstEncoding.isAsciiCompatible() || !secondEncoding.isAsciiCompatible()) {
                return null;
            }

            if (firstRope.getCodeRange() != secondRope.getCodeRange()) {
                if (firstRope.getCodeRange() == CodeRange.CR_7BIT) {
                    return secondEncoding;
                }
                if (secondRope.getCodeRange() == CodeRange.CR_7BIT) {
                    return firstEncoding;
                }
            }
            if (secondRope.getCodeRange() == CodeRange.CR_7BIT) {
                return firstEncoding;
            }
            if (firstRope.getCodeRange() == CodeRange.CR_7BIT) {
                return secondEncoding;
            }

            return null;
        }

        protected int getCacheLimit() {
            return getContext().getOptions().ENCODING_COMPATIBLE_QUERY_CACHE;
        }

    }

    public static abstract class NegotiateCompatibleEncodingNode extends RubyContextNode {

        @Child private RopeNodes.CodeRangeNode codeRangeNode;
        @Child private ToEncodingNode getEncodingNode = ToEncodingNode.create();

        public static NegotiateCompatibleEncodingNode create() {
            return NegotiateCompatibleEncodingNodeGen.create();
        }

        public abstract Encoding executeNegotiate(Object first, Object second);

        @Specialization(
                guards = {
                        "getEncoding(first) == cachedEncoding",
                        "getEncoding(second) == cachedEncoding",
                        "cachedEncoding != null" },
                limit = "getCacheLimit()")
        protected Encoding negotiateSameEncodingCached(Object first, Object second,
                @Cached("getEncoding(first)") Encoding cachedEncoding) {
            return cachedEncoding;
        }

        @Specialization(
                guards = { "getEncoding(first) == getEncoding(second)", "getEncoding(first) != null" },
                replaces = "negotiateSameEncodingCached")
        protected Encoding negotiateSameEncodingUncached(Object first, Object second) {
            return getEncoding(first);
        }

        @Specialization
        protected Encoding negotiateStringStringEncoding(RubyString first, RubyString second,
                @Cached NegotiateCompatibleRopeEncodingNode ropeNode) {
            return ropeNode.executeNegotiate(
                    first.rope,
                    second.rope);
        }

        @Specialization(
                guards = {
                        "!isRubyString(second)",
                        "getCodeRange(first) == codeRange",
                        "getEncoding(first) == firstEncoding",
                        "getEncoding(second) == secondEncoding",
                        "firstEncoding != secondEncoding" },
                limit = "getCacheLimit()")
        protected Encoding negotiateStringObjectCached(RubyString first, Object second,
                @Cached("getEncoding(first)") Encoding firstEncoding,
                @Cached("getEncoding(second)") Encoding secondEncoding,
                @Cached("getCodeRange(first)") CodeRange codeRange,
                @Cached("negotiateStringObjectUncached(first, second)") Encoding negotiatedEncoding) {
            return negotiatedEncoding;
        }

        @Specialization(
                guards = {
                        "getEncoding(first) != getEncoding(second)",
                        "!isRubyString(second)" },
                replaces = "negotiateStringObjectCached")
        protected Encoding negotiateStringObjectUncached(RubyString first, Object second) {
            final Encoding firstEncoding = getEncoding(first);
            final Encoding secondEncoding = getEncoding(second);

            if (secondEncoding == null) {
                return null;
            }

            if (!firstEncoding.isAsciiCompatible() || !secondEncoding.isAsciiCompatible()) {
                return null;
            }

            if (secondEncoding == USASCIIEncoding.INSTANCE) {
                return firstEncoding;
            }

            if (getCodeRange(first) == CodeRange.CR_7BIT) {
                return secondEncoding;
            }

            return null;
        }

        @Specialization(
                guards = {
                        "getEncoding(first) != getEncoding(second)",
                        "!isRubyString(first)" })
        protected Encoding negotiateObjectString(Object first, RubyString second) {
            return negotiateStringObjectUncached(second, first);
        }

        @Specialization(
                guards = {
                        "firstEncoding != secondEncoding",
                        "!isRubyString(first)",
                        "!isRubyString(second)",
                        "firstEncoding != null",
                        "secondEncoding != null",
                        "getEncoding(first) == firstEncoding",
                        "getEncoding(second) == secondEncoding", },
                limit = "getCacheLimit()")
        protected Encoding negotiateObjectObjectCached(Object first, Object second,
                @Cached("getEncoding(first)") Encoding firstEncoding,
                @Cached("getEncoding(second)") Encoding secondEncoding,
                @Cached("areCompatible(firstEncoding, secondEncoding)") Encoding negotiatedEncoding) {

            return negotiatedEncoding;
        }

        @Specialization(
                guards = {
                        "getEncoding(first) != getEncoding(second)",
                        "!isRubyString(first)",
                        "!isRubyString(second)" },
                replaces = "negotiateObjectObjectCached")
        protected Encoding negotiateObjectObjectUncached(Object first, Object second) {
            final Encoding firstEncoding = getEncoding(first);
            final Encoding secondEncoding = getEncoding(second);

            return areCompatible(firstEncoding, secondEncoding);
        }

        @TruffleBoundary
        protected static Encoding areCompatible(Encoding enc1, Encoding enc2) {
            assert enc1 != enc2;

            if (enc1 == null || enc2 == null) {
                return null;
            }

            if (!enc1.isAsciiCompatible() || !enc2.isAsciiCompatible()) {
                return null;
            }

            if (enc2 instanceof USASCIIEncoding) {
                return enc1;
            }
            if (enc1 instanceof USASCIIEncoding) {
                return enc2;
            }

            return null;
        }

        protected CodeRange getCodeRange(Object string) {
            // The Truffle DSL generator will calculate @Cached values used in guards above all guards. In practice,
            // this guard is only used on Ruby strings, but the method must handle any object type because of the form
            // of the generated code. If the object is not a Ruby string, the resulting value is never used.
            if (string instanceof RubyString) {
                if (codeRangeNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    codeRangeNode = insert(RopeNodes.CodeRangeNode.create());
                }

                return codeRangeNode.execute(((RubyString) string).rope);
            }

            return CodeRange.CR_UNKNOWN;
        }

        protected Encoding getEncoding(Object value) {
            return getEncodingNode.executeToEncoding(value);
        }

        protected int getCacheLimit() {
            return getContext().getOptions().ENCODING_COMPATIBLE_QUERY_CACHE;
        }

    }

    @Primitive(name = "encoding_compatible?")
    public abstract static class CompatibleQueryNode extends CoreMethodArrayArgumentsNode {

        @Child private GetRubyEncodingNode getRubyEncodingNode = EncodingNodesFactory.GetRubyEncodingNodeGen.create();
        @Child private NegotiateCompatibleEncodingNode negotiateCompatibleEncodingNode = NegotiateCompatibleEncodingNode
                .create();

        public static CompatibleQueryNode create() {
            return EncodingNodesFactory.CompatibleQueryNodeFactory.create(null);
        }

        @Specialization
        protected Object isCompatible(Object first, Object second,
                @Cached ConditionProfile noNegotiatedEncodingProfile) {
            final Encoding negotiatedEncoding = negotiateCompatibleEncodingNode.executeNegotiate(first, second);

            if (noNegotiatedEncodingProfile.profile(negotiatedEncoding == null)) {
                return nil;
            }

            return getRubyEncodingNode.executeGetRubyEncoding(negotiatedEncoding);
        }
    }

    @Primitive(name = "encoding_ensure_compatible")
    public abstract static class EnsureCompatibleNode extends CoreMethodArrayArgumentsNode {

        @Child private CheckEncodingNode checkEncodingNode = CheckEncodingNode.create();
        @Child private GetRubyEncodingNode getRubyEncodingNode = EncodingNodesFactory.GetRubyEncodingNodeGen.create();

        public static EnsureCompatibleNode create() {
            return EncodingNodesFactory.EnsureCompatibleNodeFactory.create(null);
        }

        @Specialization
        protected Object ensureCompatible(Object first, Object second) {
            return getRubyEncodingNode.executeGetRubyEncoding(checkEncodingNode.executeCheckEncoding(first, second));
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
        protected RubyString localeCharacterMap(
                @Cached GetRubyEncodingNode getRubyEncodingNode) {
            final RubyEncoding rubyEncoding = getRubyEncodingNode
                    .executeGetRubyEncoding(getContext().getEncodingManager().getLocaleEncoding());
            return rubyEncoding.name;
        }
    }

    @ReportPolymorphism
    @CoreMethod(names = "dummy?")
    public abstract static class DummyNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "encoding == cachedEncoding", limit = "getIdentityCacheLimit()")
        protected boolean isDummyCached(RubyEncoding encoding,
                @Cached("encoding") RubyEncoding cachedEncoding,
                @Cached("isDummyUncached(cachedEncoding)") boolean isDummy) {
            return isDummy;
        }

        @Specialization(replaces = "isDummyCached")
        protected boolean isDummyUncached(RubyEncoding encoding) {
            return encoding.encoding.isDummy();
        }

    }

    @CoreMethod(names = { "name", "to_s" })
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString toS(RubyEncoding encoding) {
            return encoding.name;
        }

    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        protected Object allocate(RubyClass rubyClass) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }

    }

    @Primitive(name = "encoding_each_alias")
    public abstract static class EachAliasNode extends PrimitiveArrayArgumentsNode {

        @Child private YieldNode yieldNode = YieldNode.create();
        @Child private MakeStringNode makeStringNode = MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected Object eachAlias(RubyProc block) {
            for (Hash.HashEntry<EncodingDB.Entry> entry : EncodingDB.getAliases().entryIterator()) {
                final CaseInsensitiveBytesHash.CaseInsensitiveBytesHashEntry<EncodingDB.Entry> e = (CaseInsensitiveBytesHash.CaseInsensitiveBytesHashEntry<EncodingDB.Entry>) entry;
                final RubyString aliasName = makeStringNode.executeMake(
                        ArrayUtils.extractRange(e.bytes, e.p, e.end),
                        USASCIIEncoding.INSTANCE,
                        CodeRange.CR_7BIT);
                yieldNode.executeDispatch(block, aliasName, entry.value.getEncoding().getIndex());
            }
            return nil;
        }
    }

    @Primitive(name = "encoding_is_unicode")
    public abstract static class IsUnicodeNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean isUnicode(RubyEncoding encoding) {
            return encoding.encoding.isUnicode();
        }

    }

    @Primitive(name = "get_actual_encoding")
    public abstract static class GetActualEncodingPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyEncoding getActualEncoding(RubyString string,
                @Cached GetActualEncodingNode getActualEncodingNode,
                @Cached GetRubyEncodingNode getRubyEncodingNode) {
            final Rope rope = string.rope;
            final Encoding actualEncoding = getActualEncodingNode.execute(rope);

            return getRubyEncodingNode.executeGetRubyEncoding(actualEncoding);
        }

    }

    // Port of MRI's `get_actual_encoding`.
    public abstract static class GetActualEncodingNode extends RubyContextNode {

        protected static final Encoding UTF16Dummy = EncodingDB
                .getEncodings()
                .get(RopeOperations.encodeAsciiBytes("UTF-16"))
                .getEncoding();
        protected static final Encoding UTF32Dummy = EncodingDB
                .getEncodings()
                .get(RopeOperations.encodeAsciiBytes("UTF-32"))
                .getEncoding();

        public static GetActualEncodingNode create() {
            return EncodingNodesFactory.GetActualEncodingNodeGen.create();
        }

        public abstract Encoding execute(Rope rope);

        @Specialization(guards = "!rope.getEncoding().isDummy()")
        protected Encoding getActualEncoding(Rope rope) {
            return rope.getEncoding();
        }

        @TruffleBoundary
        @Specialization(guards = "rope.getEncoding().isDummy()")
        protected Encoding getActualEncodingDummy(Rope rope) {
            final Encoding encoding = rope.getEncoding();

            if (encoding instanceof UnicodeEncoding) {
                // handle dummy UTF-16 and UTF-32 by scanning for BOM, as in MRI
                if (encoding == UTF16Dummy && rope.byteLength() >= 2) {
                    int c0 = rope.get(0) & 0xff;
                    int c1 = rope.get(1) & 0xff;

                    if (c0 == 0xFE && c1 == 0xFF) {
                        return UTF16BEEncoding.INSTANCE;
                    } else if (c0 == 0xFF && c1 == 0xFE) {
                        return UTF16LEEncoding.INSTANCE;
                    }
                    return ASCIIEncoding.INSTANCE;
                } else if (encoding == UTF32Dummy && rope.byteLength() >= 4) {
                    int c0 = rope.get(0) & 0xff;
                    int c1 = rope.get(1) & 0xff;
                    int c2 = rope.get(2) & 0xff;
                    int c3 = rope.get(3) & 0xff;

                    if (c0 == 0 && c1 == 0 && c2 == 0xFE && c3 == 0xFF) {
                        return UTF32BEEncoding.INSTANCE;
                    } else if (c3 == 0 && c2 == 0 && c1 == 0xFE && c0 == 0xFF) {
                        return UTF32LEEncoding.INSTANCE;
                    }
                    return ASCIIEncoding.INSTANCE;
                }
            }

            return encoding;
        }


    }

    @Primitive(name = "encoding_get_default_encoding")
    public abstract static class GetDefaultEncodingNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object getDefaultEncoding(RubyString name) {
            final Encoding encoding = getEncoding(name.getJavaString());
            if (encoding == null) {
                return nil;
            } else {
                return getContext().getEncodingManager().getRubyEncoding(encoding);
            }
        }

        @TruffleBoundary
        private Encoding getEncoding(String name) {
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
            getContext().getEncodingManager().setDefaultExternalEncoding(encoding.encoding);
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
            getContext().getEncodingManager().setDefaultInternalEncoding(encoding.encoding);
            return encoding;
        }

        @Specialization
        protected Object noDefaultInternal(Nil encoding) {
            getContext().getEncodingManager().setDefaultInternalEncoding(null);
            return nil;
        }

    }

    @Primitive(name = "encoding_enc_find_index")
    public static abstract class EncodingFindIndexNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected int encodingFindIndex(RubyString nameObject) {
            final String name = nameObject.getJavaString();
            final RubyEncoding encodingObject = getContext().getEncodingManager().getRubyEncoding(name);
            return encodingObject != null ? encodingObject.encoding.getIndex() : -1;
        }

    }

    @Primitive(name = "encoding_get_object_encoding")
    public static abstract class EncodingGetObjectEncodingNode extends PrimitiveArrayArgumentsNode {

        @Child private GetRubyEncodingNode getRubyEncodingNode = EncodingNodesFactory.GetRubyEncodingNodeGen.create();

        @Specialization
        protected RubyEncoding encodingGetObjectEncodingString(RubyString object) {
            return getRubyEncodingNode.executeGetRubyEncoding(object.rope.getEncoding());
        }

        @Specialization
        protected RubyEncoding encodingGetObjectEncodingSymbol(RubySymbol object) {
            return getRubyEncodingNode.executeGetRubyEncoding(object.getRope().getEncoding());
        }

        @Specialization
        protected RubyEncoding encodingGetObjectEncoding(RubyEncoding object) {
            return object;
        }

        @Specialization
        protected Object encodingGetObjectEncodingRegexp(RubyRegexp object,
                @Cached ConditionProfile hasRegexpSource) {
            final Rope regexpSource = object.source;

            if (hasRegexpSource.profile(regexpSource != null)) {
                return getRubyEncodingNode.executeGetRubyEncoding(regexpSource.getEncoding());
            } else {
                return getRubyEncodingNode.executeGetRubyEncoding(ASCIIEncoding.INSTANCE);
            }
        }

        @Fallback
        protected Object encodingGetObjectEncodingNil(Object object) {
            // TODO(CS, 26 Jan 15) something to do with __encoding__ here?
            return nil;
        }

    }

    public static abstract class EncodingCreationNode extends PrimitiveArrayArgumentsNode {

        public RubyArray setIndexOrRaiseError(String name, RubyEncoding newEncoding) {
            if (newEncoding == null) {
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentErrorEncodingAlreadyRegistered(name, this));
            }

            final int index = newEncoding.encoding.getIndex();
            return createArray(new Object[]{ newEncoding, index });
        }

    }

    @Primitive(name = "encoding_replicate")
    public static abstract class EncodingReplicateNode extends EncodingCreationNode {

        @Specialization
        protected RubyArray encodingReplicate(RubyEncoding object, RubyString nameObject) {
            final String name = nameObject.getJavaString();
            final Encoding encoding = object.encoding;

            final RubyEncoding newEncoding = replicate(name, encoding);
            return setIndexOrRaiseError(name, newEncoding);
        }

        @TruffleBoundary
        private RubyEncoding replicate(String name, Encoding encoding) {
            return getContext().getEncodingManager().replicateEncoding(encoding, name);
        }

    }

    @Primitive(name = "encoding_create_dummy")
    public static abstract class DummyEncodingeNode extends EncodingCreationNode {

        @Specialization
        protected RubyArray createDummyEncoding(RubyString nameObject) {
            final String name = nameObject.getJavaString();

            final RubyEncoding newEncoding = createDummy(name);
            return setIndexOrRaiseError(name, newEncoding);
        }

        @TruffleBoundary
        private RubyEncoding createDummy(String name) {
            return getContext().getEncodingManager().createDummyEncoding(name);
        }

    }

    @Primitive(name = "encoding_get_encoding_by_index", lowerFixnum = 0)
    public static abstract class GetEncodingObjectByIndexNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyEncoding getEncoding(int index) {
            return getContext().getEncodingManager().getRubyEncoding(index);
        }

    }

    @Primitive(name = "encoding_get_encoding_index")
    public static abstract class GetEncodingIndexNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected int getIndex(RubyEncoding encoding) {
            return encoding.encoding.getIndex();
        }

    }

    public static abstract class CheckRopeEncodingNode extends RubyContextNode {

        @Child private NegotiateCompatibleRopeEncodingNode negotiateCompatibleEncodingNode = NegotiateCompatibleRopeEncodingNode
                .create();

        public static CheckRopeEncodingNode create() {
            return CheckRopeEncodingNodeGen.create();
        }

        public abstract Encoding executeCheckEncoding(Rope first, Rope second);

        @Specialization
        protected Encoding checkEncoding(Rope first, Rope second,
                @Cached BranchProfile errorProfile) {
            final Encoding negotiatedEncoding = negotiateCompatibleEncodingNode.executeNegotiate(first, second);

            if (negotiatedEncoding == null) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().encodingCompatibilityErrorIncompatible(
                        first.getEncoding(),
                        second.getEncoding(),
                        this));
            }

            return negotiatedEncoding;
        }

    }

    public static abstract class CheckEncodingNode extends RubyContextNode {

        @Child private NegotiateCompatibleEncodingNode negotiateCompatibleEncodingNode;
        @Child private ToEncodingNode toEncodingNode;

        public static CheckEncodingNode create() {
            return EncodingNodesFactory.CheckEncodingNodeGen.create();
        }

        public abstract Encoding executeCheckEncoding(Object first, Object second);

        @Specialization
        protected Encoding checkEncoding(Object first, Object second,
                @Cached BranchProfile errorProfile) {
            final Encoding negotiatedEncoding = executeNegotiate(first, second);

            if (negotiatedEncoding == null) {
                errorProfile.enter();
                raiseException(first, second);
            }

            return negotiatedEncoding;
        }

        private Encoding executeNegotiate(Object first, Object second) {
            if (negotiateCompatibleEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                negotiateCompatibleEncodingNode = insert(NegotiateCompatibleEncodingNode.create());
            }
            return negotiateCompatibleEncodingNode.executeNegotiate(first, second);
        }

        private void raiseException(Object first, Object second) {
            if (toEncodingNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toEncodingNode = insert(ToEncodingNode.create());
            }

            throw new RaiseException(getContext(), coreExceptions().encodingCompatibilityErrorIncompatible(
                    toEncodingNode.executeToEncoding(first),
                    toEncodingNode.executeToEncoding(second),
                    this));
        }

    }

}
