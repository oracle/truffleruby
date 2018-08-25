/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 * Contains code modified from JRuby's RubyConverter.java
 */
package org.truffleruby.core.encoding;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.jcodings.Encoding;
import org.jcodings.EncodingDB;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.util.CaseInsensitiveBytesHash;
import org.jcodings.util.Hash;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.cast.ToEncodingNode;
import org.truffleruby.core.encoding.EncodingNodesFactory.CheckRopeEncodingNodeGen;
import org.truffleruby.core.encoding.EncodingNodesFactory.GetRubyEncodingNodeGen;
import org.truffleruby.core.encoding.EncodingNodesFactory.NegotiateCompatibleEncodingNodeGen;
import org.truffleruby.core.encoding.EncodingNodesFactory.NegotiateCompatibleRopeEncodingNodeGen;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.StringNodes.MakeStringNode;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.yield.YieldNode;

@CoreClass("Encoding")
public abstract class EncodingNodes {

    @CoreMethod(names = "ascii_compatible?")
    public abstract static class AsciiCompatibleNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "encoding == cachedEncoding", limit = "getCacheLimit()")
        public boolean isAsciiCompatibleCached(DynamicObject encoding,
                                          @Cached("encoding") DynamicObject cachedEncoding,
                                          @Cached("isAsciiCompatible(cachedEncoding)") boolean isAsciiCompatible) {
            return isAsciiCompatible;
        }

        @Specialization(replaces = "isAsciiCompatibleCached")
        public boolean isAsciiCompatibleUncached(DynamicObject encoding) {
            return isAsciiCompatible(encoding);
        }

        protected int getCacheLimit() {
            return getContext().getOptions().ENCODING_LOADED_CLASSES_CACHE;
        }

        protected static boolean isAsciiCompatible(DynamicObject encoding) {
            assert RubyGuards.isRubyEncoding(encoding);

            return EncodingOperations.getEncoding(encoding).isAsciiCompatible();
        }
    }

    public abstract static class GetRubyEncodingNode extends RubyBaseNode {

        public static GetRubyEncodingNode create() {
            return GetRubyEncodingNodeGen.create();
        }

        public abstract DynamicObject executeGetRubyEncoding(Encoding encoding);

        @Specialization(guards = "isSameEncoding(encoding, cachedRubyEncoding)", limit = "getCacheLimit()")
        protected DynamicObject getRubyEncodingCached(Encoding encoding,
                                                      @Cached("getRubyEncodingUncached(encoding)") DynamicObject cachedRubyEncoding) {
            return cachedRubyEncoding;
        }

        @Specialization(replaces = "getRubyEncodingCached")
        protected DynamicObject getRubyEncodingUncached(Encoding encoding) {
            return getContext().getEncodingManager().getRubyEncoding(encoding);
        }

        protected boolean isSameEncoding(Encoding encoding, DynamicObject rubyEncoding) {
            return encoding == Layouts.ENCODING.getEncoding(rubyEncoding);
        }

        protected int getCacheLimit() {
            return getContext().getOptions().ENCODING_LOADED_CLASSES_CACHE;
        }

    }

    public static abstract class NegotiateCompatibleRopeEncodingNode extends RubyBaseNode {

        public abstract Encoding executeNegotiate(Rope first, Rope second);

        public static NegotiateCompatibleRopeEncodingNode create() {
            return NegotiateCompatibleRopeEncodingNodeGen.create();
        }

        @Specialization(guards = "first.getEncoding() == second.getEncoding()")
        protected Encoding negotiateSameEncodingUncached(Rope first, Rope second) {
            return first.getEncoding();
        }

        @Specialization(guards = {
                "firstEncoding != secondEncoding",
                "first.isEmpty() == isFirstEmpty",
                "second.isEmpty() == isSecondEmpty",
                "first.getCodeRange() == firstCodeRange",
                "second.getCodeRange() == secondCodeRange",
                "first.getEncoding() == firstEncoding",
                "second.getEncoding() == secondEncoding"
        }, limit = "getCacheLimit()")
        protected Encoding negotiateRopeRopeCached(Rope first, Rope second,
                @Cached("first.getEncoding()") Encoding firstEncoding,
                @Cached("second.getEncoding()") Encoding secondEncoding,
                @Cached("first.isEmpty()") boolean isFirstEmpty,
                @Cached("second.isEmpty()") boolean isSecondEmpty,
                @Cached("first.getCodeRange()") CodeRange firstCodeRange,
                @Cached("second.getCodeRange()") CodeRange secondCodeRange,
                @Cached("negotiateRopeRopeUncached(first, second)") Encoding negotiatedEncoding) {
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
                return firstEncoding.isAsciiCompatible() && (secondRope.getCodeRange() == CodeRange.CR_7BIT) ? firstEncoding : secondEncoding;
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

    public static abstract class NegotiateCompatibleEncodingNode extends RubyBaseNode {

        @Child private ToEncodingNode getEncodingNode = ToEncodingNode.create();

        public static NegotiateCompatibleEncodingNode create() {
            return NegotiateCompatibleEncodingNodeGen.create();
        }

        public abstract Encoding executeNegotiate(Object first, Object second);

        @Specialization(guards = {
                "getEncoding(first) == getEncoding(second)",
                "getEncoding(first) == cachedEncoding"
        }, limit = "getCacheLimit()")
        protected Encoding negotiateSameEncodingCached(DynamicObject first, DynamicObject second,
                                                       @Cached("getEncoding(first)") Encoding cachedEncoding) {
            return cachedEncoding;
        }

        @Specialization(guards = "getEncoding(first) == getEncoding(second)", replaces = "negotiateSameEncodingCached")
        protected Encoding negotiateSameEncodingUncached(DynamicObject first, DynamicObject second) {
            return getEncoding(first);
        }

        @Specialization(guards = { "isRubyString(first)", "isRubyString(second)" })
        protected Encoding negotiateStringStringEncoding(DynamicObject first, DynamicObject second,
                @Cached("create()") NegotiateCompatibleRopeEncodingNode ropeNode) {
            return ropeNode.executeNegotiate(
                    StringOperations.rope(first),
                    StringOperations.rope(second));
        }

        @Specialization(guards = {
                "isRubyString(first)",
                "!isRubyString(second)",
                "getCodeRange(first) == codeRange",
                "getEncoding(first) == firstEncoding",
                "getEncoding(second) == secondEncoding",
                "firstEncoding != secondEncoding"
        }, limit = "getCacheLimit()")
        protected Encoding negotiateStringObjectCached(DynamicObject first, Object second,
                                                       @Cached("getEncoding(first)") Encoding firstEncoding,
                                                       @Cached("getEncoding(second)") Encoding secondEncoding,
                                                       @Cached("getCodeRange(first)") CodeRange codeRange,
                                                       @Cached("negotiateStringObjectUncached(first, second)") Encoding negotiatedEncoding) {
            return negotiatedEncoding;
        }

        @Specialization(guards = {
                "getEncoding(first) != getEncoding(second)",
                "isRubyString(first)",
                "!isRubyString(second)"
        }, replaces = "negotiateStringObjectCached")
        protected Encoding negotiateStringObjectUncached(DynamicObject first, Object second) {
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

        @Specialization(guards = {
                "getEncoding(first) != getEncoding(second)",
                "!isRubyString(first)",
                "isRubyString(second)"
        })
        protected Encoding negotiateObjectString(Object first, DynamicObject second) {
            return negotiateStringObjectUncached(second, first);
        }

        @Specialization(guards = {
                "firstEncoding != secondEncoding",
                "!isRubyString(first)",
                "!isRubyString(second)",
                "firstEncoding != null",
                "secondEncoding != null",
                "getEncoding(first) == firstEncoding",
                "getEncoding(second) == secondEncoding",
        }, limit = "getCacheLimit()")
        protected Encoding negotiateObjectObjectCached(Object first, Object second,
                                                            @Cached("getEncoding(first)") Encoding firstEncoding,
                                                            @Cached("getEncoding(second)") Encoding secondEncoding,
                                                            @Cached("areCompatible(firstEncoding, secondEncoding)") Encoding negotiatedEncoding) {

            return negotiatedEncoding;
        }

        @Specialization(guards = {
                "getEncoding(first) != getEncoding(second)",
                "!isRubyString(first)",
                "!isRubyString(second)"
        }, replaces = "negotiateObjectObjectCached")
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
            if (RubyGuards.isRubyString(string)) {
                return StringOperations.rope((DynamicObject) string).getCodeRange();
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

    @CoreMethod(names = "compatible?", onSingleton = true, required = 2)
    public abstract static class CompatibleQueryNode extends CoreMethodArrayArgumentsNode {

        @Child private GetRubyEncodingNode getRubyEncodingNode = EncodingNodesFactory.GetRubyEncodingNodeGen.create();
        @Child private NegotiateCompatibleEncodingNode negotiateCompatibleEncodingNode = NegotiateCompatibleEncodingNode.create();

        public abstract DynamicObject executeCompatibleQuery(Object first, Object second);

        @Specialization
        protected DynamicObject isCompatible(Object first, Object second,
                                             @Cached("createBinaryProfile()") ConditionProfile noNegotiatedEncodingProfile) {
            final Encoding negotiatedEncoding = negotiateCompatibleEncodingNode.executeNegotiate(first, second);

            if (noNegotiatedEncodingProfile.profile(negotiatedEncoding == null)) {
                return nil();
            }

            return getRubyEncodingNode.executeGetRubyEncoding(negotiatedEncoding);
        }
    }

    @CoreMethod(names = "list", onSingleton = true)
    public abstract static class ListNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject list() {
            final Object[] encodingsList = getContext().getEncodingManager().getEncodingList();
            return createArray(encodingsList, encodingsList.length);
        }
    }


    @CoreMethod(names = "locale_charmap", onSingleton = true)
    public abstract static class LocaleCharacterMapNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject localeCharacterMap(
                @Cached("create()") GetRubyEncodingNode getRubyEncodingNode) {
            final DynamicObject rubyEncoding = getRubyEncodingNode.executeGetRubyEncoding(getContext().getEncodingManager().getLocaleEncoding());

            return Layouts.ENCODING.getName(rubyEncoding);
        }
    }

    @CoreMethod(names = "dummy?")
    public abstract static class DummyNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "encoding == cachedEncoding", limit = "getCacheLimit()")
        public boolean isDummyCached(DynamicObject encoding,
                                     @Cached("encoding") DynamicObject cachedEncoding,
                                     @Cached("isDummy(cachedEncoding)") boolean isDummy) {
            return isDummy;
        }

        @Specialization(replaces = "isDummyCached")
        public boolean isDummyUncached(DynamicObject encoding) {
            return isDummy(encoding);
        }

        protected int getCacheLimit() {
            return getContext().getOptions().ENCODING_LOADED_CLASSES_CACHE;
        }

        protected static boolean isDummy(DynamicObject encoding) {
            assert RubyGuards.isRubyEncoding(encoding);

            return Layouts.ENCODING.getDummy(encoding);
        }
    }

    @CoreMethod(names = { "name", "to_s" })
    public abstract static class ToSNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject toS(DynamicObject encoding) {
            return Layouts.ENCODING.getName(encoding);
        }

    }

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            throw new RaiseException(getContext(), coreExceptions().typeErrorAllocatorUndefinedFor(rubyClass, this));
        }

    }

    @Primitive(name = "encoding_each_alias", needsSelf = false)
    public abstract static class EachAliasNode extends PrimitiveArrayArgumentsNode {

        @Child private YieldNode yieldNode = new YieldNode();
        @Child private MakeStringNode makeStringNode = MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        public DynamicObject eachAlias(DynamicObject block) {
            for (Hash.HashEntry<EncodingDB.Entry> entry : EncodingDB.getAliases().entryIterator()) {
                final CaseInsensitiveBytesHash.CaseInsensitiveBytesHashEntry<EncodingDB.Entry> e = (CaseInsensitiveBytesHash.CaseInsensitiveBytesHashEntry<EncodingDB.Entry>) entry;
                final DynamicObject aliasName = makeStringNode.executeMake(ArrayUtils.extractRange(e.bytes, e.p, e.end), USASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
                yieldNode.dispatch(block, aliasName, entry.value.getEncoding().getIndex());
            }
            return nil();
        }
    }

    @Primitive(name = "encoding_is_unicode", needsSelf = false)
    public abstract static class IsUnicodeNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyEncoding(encoding)")
        public boolean isUnicode(DynamicObject encoding) {
            return Layouts.ENCODING.getEncoding(encoding).isUnicode();
        }

    }

    @Primitive(name = "encoding_get_default_encoding", needsSelf = false)
    public abstract static class GetDefaultEncodingNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubyString(name)")
        public DynamicObject getDefaultEncoding(DynamicObject name) {
            final Encoding encoding = getEncoding(StringOperations.getString(name));
            if (encoding == null) {
                return nil();
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
                    throw new UnsupportedOperationException();
            }
        }
    }

    @Primitive(name = "encoding_set_default_external", needsSelf = false)
    public abstract static class SetDefaultExternalNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyEncoding(encoding)")
        public DynamicObject setDefaultExternal(DynamicObject encoding) {
            getContext().getEncodingManager().setDefaultExternalEncoding(EncodingOperations.getEncoding(encoding));
            return encoding;
        }

        @Specialization(guards = "isNil(nil)")
        public DynamicObject noDefaultExternal(DynamicObject nil) {
            throw new RaiseException(getContext(), coreExceptions().argumentError("default external can not be nil", this));
        }

    }

    @Primitive(name = "encoding_set_default_internal", needsSelf = false)
    public abstract static class SetDefaultInternalNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyEncoding(encoding)")
        public DynamicObject setDefaultInternal(DynamicObject encoding) {
            getContext().getEncodingManager().setDefaultInternalEncoding(EncodingOperations.getEncoding(encoding));
            return encoding;
        }

        @Specialization(guards = "isNil(encoding)")
        public DynamicObject noDefaultInternal(DynamicObject encoding) {
            getContext().getEncodingManager().setDefaultInternalEncoding(null);
            return nil();
        }

    }

    @Primitive(name = "encoding_enc_find_index", needsSelf = false)
    public static abstract class EncodingFindIndexNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyString(nameObject)")
        public int encodingFindIndex(DynamicObject nameObject) {
            final String name = StringOperations.getString(nameObject);
            final DynamicObject encodingObject = getContext().getEncodingManager().getRubyEncoding(name);
            return encodingObject != null ? Layouts.ENCODING.getEncoding(encodingObject).getIndex() : -1;
        }

    }

    @Primitive(name = "encoding_get_object_encoding", needsSelf = false)
    public static abstract class EncodingGetObjectEncodingNode extends PrimitiveArrayArgumentsNode {

        @Child private GetRubyEncodingNode getRubyEncodingNode = EncodingNodesFactory.GetRubyEncodingNodeGen.create();

        @Specialization(guards = "isRubyString(string)")
        public DynamicObject encodingGetObjectEncodingString(DynamicObject string) {
            return getRubyEncodingNode.executeGetRubyEncoding(Layouts.STRING.getRope(string).getEncoding());
        }

        @Specialization(guards = "isRubySymbol(symbol)")
        public DynamicObject encodingGetObjectEncodingSymbol(DynamicObject symbol) {
            return getRubyEncodingNode.executeGetRubyEncoding(Layouts.SYMBOL.getRope(symbol).getEncoding());
        }

        @Specialization(guards = "isRubyEncoding(encoding)")
        public DynamicObject encodingGetObjectEncoding(DynamicObject encoding) {
            return encoding;
        }

        @Specialization(guards = "isRubyRegexp(regexp)")
        public DynamicObject encodingGetObjectEncodingRegexp(DynamicObject regexp) {
            return getRubyEncodingNode.executeGetRubyEncoding(Layouts.REGEXP.getSource(regexp).getEncoding());
        }

        @Specialization(guards = {"!isRubyString(object)", "!isRubySymbol(object)", "!isRubyEncoding(object)", "!isRubyRegexp(object)"})
        public DynamicObject encodingGetObjectEncodingNil(DynamicObject object) {
            // TODO(CS, 26 Jan 15) something to do with __encoding__ here?
            return nil();
        }

    }

    @Primitive(name = "encoding_replicate")
    public static abstract class EncodingReplicateNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyString(nameObject)")
        public DynamicObject encodingReplicate(DynamicObject self, DynamicObject nameObject) {
            final String name = StringOperations.getString(nameObject);
            final Encoding encoding = EncodingOperations.getEncoding(self);

            final DynamicObject newEncoding = replicate(name, encoding);
            if (newEncoding == null) {
                throw new RaiseException(getContext(), coreExceptions().argumentErrorEncodingAlreadyRegistered(name, this));
            }

            final int index = Layouts.ENCODING.getEncoding(newEncoding).getIndex();
            return createArray(new Object[]{ newEncoding, index }, 2);
        }

        @TruffleBoundary
        private DynamicObject replicate(String name, Encoding encoding) {
            return getContext().getEncodingManager().replicateEncoding(encoding, name);
        }

    }

    @Primitive(name = "encoding_get_encoding_by_index", needsSelf = false, lowerFixnum = 1)
    public static abstract class GetEncodingObjectByIndexNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public DynamicObject getEncoding(int index) {
            return getContext().getEncodingManager().getRubyEncoding(index);
        }

    }

    @Primitive(name = "encoding_get_encoding_index", needsSelf = false)
    public static abstract class GetEncodingIndexNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public int getIndex(DynamicObject encoding) {
            return Layouts.ENCODING.getEncoding(encoding).getIndex();
        }

    }

    public static abstract class CheckRopeEncodingNode extends RubyBaseNode {

        @Child private NegotiateCompatibleRopeEncodingNode negotiateCompatibleEncodingNode = NegotiateCompatibleRopeEncodingNode.create();

        public static CheckRopeEncodingNode create() {
            return CheckRopeEncodingNodeGen.create();
        }

        public abstract Encoding executeCheckEncoding(Rope first, Rope second);

        @Specialization
        public Encoding checkEncoding(Rope first, Rope second,
                @Cached("create()") BranchProfile errorProfile) {
            final Encoding negotiatedEncoding = negotiateCompatibleEncodingNode.executeNegotiate(first, second);

            if (negotiatedEncoding == null) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().encodingCompatibilityErrorIncompatible(
                        first.getEncoding(), second.getEncoding(), this));
            }

            return negotiatedEncoding;
        }

    }

    public static abstract class CheckEncodingNode extends RubyBaseNode {

        @Child private NegotiateCompatibleEncodingNode negotiateCompatibleEncodingNode;
        @Child private ToEncodingNode toEncodingNode;

        public static CheckEncodingNode create() {
            return EncodingNodesFactory.CheckEncodingNodeGen.create();
        }

        public abstract Encoding executeCheckEncoding(Object first, Object second);

        @Specialization
        public Encoding checkEncoding(Object first, Object second,
                                      @Cached("create()") BranchProfile errorProfile) {
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
                    toEncodingNode.executeToEncoding(first), toEncodingNode.executeToEncoding(second), this));
        }

    }

}
