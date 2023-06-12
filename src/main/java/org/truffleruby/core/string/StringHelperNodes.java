/*
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.profiles.InlinedBranchProfile;
import com.oracle.truffle.api.profiles.InlinedConditionProfile;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.InternalByteArray;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.ErrorHandling;
import com.oracle.truffle.api.strings.TruffleStringIterator;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.core.encoding.EncodingNodes;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.library.RubyStringLibrary;

public abstract class StringHelperNodes {

    @TruffleBoundary
    static Object trTransHelper(Node node, EncodingNodes.CheckEncodingNode checkEncodingNode, RubyString self,
            RubyStringLibrary libFromStr, Object fromStr,
            RubyStringLibrary libToStr, Object toStr, boolean sFlag) {
        final RubyEncoding e1 = checkEncodingNode.execute(node, self, fromStr);
        final RubyEncoding e2 = checkEncodingNode.execute(node, self, toStr);
        final RubyEncoding enc = e1 == e2 ? e1 : checkEncodingNode.execute(node, fromStr, toStr);

        var selfTStringWithEnc = new ATStringWithEncoding(self.tstring, self.getEncodingUncached());
        var fromStrTStringWithEnc = new ATStringWithEncoding(libFromStr, fromStr);
        var toStrTStringWithEnc = new ATStringWithEncoding(libToStr, toStr);
        final TruffleString ret = StringSupport.trTransHelper(selfTStringWithEnc, fromStrTStringWithEnc,
                toStrTStringWithEnc, e1.jcoding, enc, sFlag, node);
        if (ret == null) {
            return Nil.INSTANCE;
        }

        self.setTString(ret, enc);
        return self;
    }

    @GenerateCached(false)
    @GenerateInline
    public abstract static class SingleByteOptimizableNode extends RubyBaseNode {

        public abstract boolean execute(Node node, AbstractTruffleString string, RubyEncoding encoding);

        @Specialization
        protected static boolean isSingleByteOptimizable(Node node, AbstractTruffleString string, RubyEncoding encoding,
                @Cached InlinedConditionProfile asciiOnlyProfile,
                @Cached(inline = false) TruffleString.GetByteCodeRangeNode getByteCodeRangeNode) {
            if (asciiOnlyProfile.profile(node, StringGuards.is7Bit(string, encoding, getByteCodeRangeNode))) {
                return true;
            } else {
                return encoding.isSingleByte;
            }
        }
    }

    /** The node to use for inline caches to compare if two TruffleString are equal. It behaves the same as String#==,
     * without coercion. Note that the two encodings do no need to be the same for this node to return true. If you need
     * to ensure the encoding is the same, use {@link EqualSameEncodingNode}.
     *
     * Two strings are considered equal if they are the same byte-by-byte and:
     * <ul>
     * <li>Both strings have the same encoding</li>
     * <li>Both strings are 7-bit (and so both have an ASCII-compatible encoding)</li>
     * <li>Both strings are empty (regardless of their encodings)</li>
     * </ul>
     */
    public abstract static class EqualNode extends RubyBaseNode {

        public final boolean execute(RubyStringLibrary libString, Object rubyString,
                TruffleString cachedString, RubyEncoding cachedEncoding) {
            return execute(libString.getTString(rubyString), libString.getEncoding(rubyString),
                    cachedString, cachedEncoding);
        }

        // cachedString is TruffleString to ensure correctness, caching on a MutableTruffleString is incorrect
        public abstract boolean execute(AbstractTruffleString tstring, RubyEncoding encoding,
                TruffleString cachedString, RubyEncoding cachedEncoding);

        @Specialization
        protected boolean equal(AbstractTruffleString a, RubyEncoding encA, TruffleString b, RubyEncoding encB,
                @Cached EncodingNodes.NegotiateCompatibleStringEncodingNode negotiateCompatibleStringEncodingNode,
                @Cached StringEqualInternalNode stringEqualInternalNode) {
            var compatibleEncoding = negotiateCompatibleStringEncodingNode.execute(this, a, encA, b, encB);
            return stringEqualInternalNode.executeInternal(a, b, compatibleEncoding);
        }
    }

    @GenerateUncached
    public abstract static class EqualSameEncodingNode extends RubyBaseNode {

        public final boolean execute(RubyStringLibrary libString, Object rubyString,
                TruffleString cachedString, RubyEncoding cachedEncoding) {
            return execute(libString.getTString(rubyString), libString.getEncoding(rubyString),
                    cachedString, cachedEncoding);
        }

        // cachedString is TruffleString to ensure correctness, caching on a MutableTruffleString is incorrect
        public abstract boolean execute(AbstractTruffleString tstring, RubyEncoding encoding,
                TruffleString cachedString, RubyEncoding cachedEncoding);

        @Specialization(guards = "encA == encB")
        protected boolean same(AbstractTruffleString a, RubyEncoding encA, TruffleString b, RubyEncoding encB,
                @Cached StringEqualInternalNode stringEqualInternalNode) {
            return stringEqualInternalNode.executeInternal(a, b, encA);
        }

        @Specialization(guards = "encA != encB")
        protected boolean diff(AbstractTruffleString a, RubyEncoding encA, TruffleString b, RubyEncoding encB) {
            return false;
        }
    }

    @GenerateUncached
    public abstract static class StringEqualInternalNode extends RubyBaseNode {
        // compatibleEncoding is RubyEncoding or null
        public abstract boolean executeInternal(AbstractTruffleString a, AbstractTruffleString b,
                RubyEncoding compatibleEncoding);

        @Specialization(guards = "a.isEmpty() || b.isEmpty()")
        protected boolean empty(AbstractTruffleString a, AbstractTruffleString b, RubyEncoding compatibleEncoding) {
            assert compatibleEncoding != null;
            return a.isEmpty() && b.isEmpty();
        }

        @Specialization(guards = { "compatibleEncoding != null", "!a.isEmpty()", "!b.isEmpty()" })
        protected boolean equalBytes(AbstractTruffleString a, AbstractTruffleString b, RubyEncoding compatibleEncoding,
                @Cached TruffleString.EqualNode equalNode) {
            return equalNode.execute(a, b, compatibleEncoding.tencoding);
        }

        @Specialization(guards = "compatibleEncoding == null")
        protected boolean notComparable(
                AbstractTruffleString a, AbstractTruffleString b, RubyEncoding compatibleEncoding) {
            return false;
        }
    }

    @ImportStatic(StringGuards.class)
    public abstract static class CountStringsNode extends TrTableNode {

        public abstract int execute(Object string, TStringWithEncoding[] tstringsWithEncs);

        @Specialization(guards = "libString.getTString(string).isEmpty()", limit = "1")
        protected int count(Object string, TStringWithEncoding[] args,
                @Cached @Shared RubyStringLibrary libString) {
            return 0;
        }

        @Specialization(
                guards = {
                        "cachedArgs.length > 0",
                        "!tstring.isEmpty()",
                        "cachedArgs.length == args.length",
                        "argsMatch(cachedArgs, args, equalNode)",
                        "encoding == cachedEncoding" },
                limit = "getDefaultCacheLimit()")
        protected int countFast(Object string, TStringWithEncoding[] args,
                @Cached(value = "args", dimensions = 1) TStringWithEncoding[] cachedArgs,
                @Cached TruffleString.EqualNode equalNode,
                @Cached @Shared RubyStringLibrary libString,
                @Cached @Shared EncodingNodes.CheckStringEncodingNode checkEncodingNode,
                @Bind("libString.getTString(string)") AbstractTruffleString tstring,
                @Bind("libString.getEncoding(string)") RubyEncoding encoding,
                @Cached("libString.getEncoding(string)") RubyEncoding cachedEncoding,
                @Cached(value = "squeeze()", dimensions = 1) boolean[] squeeze,
                @Cached("findEncoding(this, libString.getTString(string), libString.getEncoding(string), cachedArgs, checkEncodingNode)") RubyEncoding compatEncoding,
                @Cached("makeTables(this, cachedArgs, squeeze, compatEncoding)") StringSupport.TrTables tables,
                @Cached @Shared TruffleString.GetInternalByteArrayNode byteArrayNode,
                @Cached @Shared TruffleString.GetByteCodeRangeNode getByteCodeRangeNode) {
            var byteArray = byteArrayNode.execute(tstring, encoding.tencoding);
            var codeRange = getByteCodeRangeNode.execute(tstring, encoding.tencoding);
            return StringSupport.strCount(byteArray, codeRange, squeeze, tables, compatEncoding.jcoding, this);
        }

        @Specialization(guards = "!libString.getTString(string).isEmpty()", limit = "1")
        protected int count(Object string, TStringWithEncoding[] tstringsWithEncs,
                @Cached InlinedBranchProfile errorProfile,
                @Cached @Shared EncodingNodes.CheckStringEncodingNode checkEncodingNode,
                @Cached @Shared RubyStringLibrary libString,
                @Cached @Shared TruffleString.GetInternalByteArrayNode byteArrayNode,
                @Cached @Shared TruffleString.GetByteCodeRangeNode getByteCodeRangeNode) {
            if (tstringsWithEncs.length == 0) {
                errorProfile.enter(this);
                throw new RaiseException(getContext(), coreExceptions().argumentErrorEmptyVarargs(this));
            }

            var tstring = libString.getTString(string);
            var encoding = libString.getEncoding(string);
            var byteArray = byteArrayNode.execute(tstring, encoding.tencoding);
            var codeRange = getByteCodeRangeNode.execute(tstring, encoding.tencoding);

            RubyEncoding enc = findEncoding(this, tstring, encoding, tstringsWithEncs, checkEncodingNode);
            return countSlow(byteArray, codeRange, tstringsWithEncs, enc);
        }

        @TruffleBoundary
        private int countSlow(InternalByteArray byteArray, TruffleString.CodeRange codeRange,
                TStringWithEncoding[] tstringsWithEncs, RubyEncoding enc) {
            final boolean[] table = squeeze();
            final StringSupport.TrTables tables = makeTables(this, tstringsWithEncs, table, enc);
            return StringSupport.strCount(byteArray, codeRange, table, tables, enc.jcoding, this);
        }
    }

    public abstract static class TrTableNode extends RubyBaseNode {

        protected boolean[] squeeze() {
            return new boolean[StringSupport.TRANS_SIZE + 1];
        }

        protected static RubyEncoding findEncoding(Node node, AbstractTruffleString tstring, RubyEncoding encoding,
                TStringWithEncoding[] tstringsWithEncs, EncodingNodes.CheckStringEncodingNode checkEncodingNode) {
            RubyEncoding enc = checkEncodingNode.executeCheckEncoding(node, tstring, encoding,
                    tstringsWithEncs[0].tstring,
                    tstringsWithEncs[0].encoding);
            for (int i = 1; i < tstringsWithEncs.length; i++) {
                enc = checkEncodingNode.executeCheckEncoding(node, tstring, encoding, tstringsWithEncs[i].tstring,
                        tstringsWithEncs[i].encoding);
            }
            return enc;
        }

        protected static StringSupport.TrTables makeTables(Node node, TStringWithEncoding[] tstringsWithEncs,
                boolean[] squeeze, RubyEncoding enc) {
            // The trSetupTable method will consume the bytes from the rope one encoded character at a time and
            // build a TrTable from this. Previously we started with the encoding of rope zero, and at each
            // stage found a compatible encoding to build that TrTable with. Although we now calculate a single
            // encoding with which to build the tables it must be compatible with all ropes, so will not
            // affect the consumption of characters from those ropes.
            StringSupport.TrTables tables = StringSupport.trSetupTable(
                    tstringsWithEncs[0].tstring,
                    tstringsWithEncs[0].encoding,
                    squeeze,
                    null,
                    true,
                    enc.jcoding,
                    node);

            for (int i = 1; i < tstringsWithEncs.length; i++) {
                tables = StringSupport
                        .trSetupTable(tstringsWithEncs[i].tstring, tstringsWithEncs[i].encoding, squeeze, tables, false,
                                enc.jcoding, node);
            }
            return tables;
        }

        @ExplodeLoop
        protected boolean argsMatch(TStringWithEncoding[] cachedStrings, TStringWithEncoding[] strings,
                TruffleString.EqualNode equalNode) {
            for (int i = 0; i < cachedStrings.length; i++) {
                if (cachedStrings[i].encoding != strings[i].encoding) {
                    return false;
                }
                if (!equalNode.execute(cachedStrings[i].tstring, strings[i].tstring,
                        cachedStrings[i].encoding.tencoding)) {
                    return false;
                }
            }
            return true;
        }
    }


    @ImportStatic(StringGuards.class)
    @GenerateCached(false)
    @GenerateInline
    public abstract static class DeleteBangStringsNode extends TrTableNode {

        public abstract Object execute(Node node, RubyString string, TStringWithEncoding[] tstringsWithEncs);

        @Specialization(guards = "string.tstring.isEmpty()")
        protected Object deleteBangEmpty(RubyString string, TStringWithEncoding[] args) {
            return nil;
        }

        @Specialization(
                guards = {
                        "cachedArgs.length > 0",
                        "!string.tstring.isEmpty()",
                        "cachedArgs.length == args.length",
                        "argsMatch(cachedArgs, args, equalNode)",
                        "libString.getEncoding(string) == cachedEncoding" },
                limit = "getDefaultCacheLimit()")
        protected static Object deleteBangFast(Node node, RubyString string, TStringWithEncoding[] args,
                @Cached(value = "args", dimensions = 1) TStringWithEncoding[] cachedArgs,
                @Cached(inline = false) TruffleString.EqualNode equalNode,
                @Cached @Shared EncodingNodes.CheckStringEncodingNode checkEncodingNode,
                @Cached @Shared RubyStringLibrary libString,
                @Cached("libString.getEncoding(string)") RubyEncoding cachedEncoding,
                @Cached(value = "squeeze()", dimensions = 1) boolean[] squeeze,
                @Cached("findEncoding(node, libString.getTString(string), libString.getEncoding(string), cachedArgs, checkEncodingNode)") RubyEncoding compatEncoding,
                @Cached("makeTables(node, cachedArgs, squeeze, compatEncoding)") StringSupport.TrTables tables,
                @Cached @Exclusive InlinedBranchProfile nullProfile) {
            var processedTString = processStr(node, string, squeeze, compatEncoding, tables);
            if (processedTString == null) {
                nullProfile.enter(node);
                return nil;
            }

            string.setTString(processedTString);
            return string;
        }

        @Specialization(guards = "!string.tstring.isEmpty()", replaces = "deleteBangFast")
        protected static Object deleteBangSlow(Node node, RubyString string, TStringWithEncoding[] args,
                @Cached @Shared RubyStringLibrary libString,
                @Cached @Shared EncodingNodes.CheckStringEncodingNode checkEncodingNode,
                @Cached @Exclusive InlinedBranchProfile errorProfile) {
            if (args.length == 0) {
                errorProfile.enter(node);
                throw new RaiseException(getContext(node), coreExceptions(node).argumentErrorEmptyVarargs(node));
            }

            RubyEncoding enc = findEncoding(node, string.tstring, libString.getEncoding(string), args,
                    checkEncodingNode);

            return deleteBangSlow(node, string, args, enc);
        }

        @TruffleBoundary
        private static Object deleteBangSlow(Node node, RubyString string, TStringWithEncoding[] tstringsWithEncs,
                RubyEncoding enc) {
            final boolean[] squeeze = new boolean[StringSupport.TRANS_SIZE + 1];

            final StringSupport.TrTables tables = makeTables(node, tstringsWithEncs, squeeze, enc);

            var processedTString = processStr(node, string, squeeze, enc, tables);
            if (processedTString == null) {
                return nil;
            }

            string.setTString(processedTString);
            // REVIEW encoding set

            return string;
        }

        @TruffleBoundary
        private static TruffleString processStr(Node node, RubyString string, boolean[] squeeze, RubyEncoding enc,
                StringSupport.TrTables tables) {
            return StringSupport.delete_bangCommon19(
                    new ATStringWithEncoding(string.tstring, string.getEncodingUncached()), squeeze, tables, enc, node);
        }
    }

    @GenerateUncached
    @GenerateCached(false)
    @GenerateInline
    public abstract static class HashStringNode extends RubyBaseNode {

        protected static final int CLASS_SALT = 54008340; // random number, stops hashes for similar values but different classes being the same, static because we want deterministic hashes

        public abstract long execute(Node node, Object string);

        @Specialization
        protected static long hash(Node node, Object string,
                @Cached RubyStringLibrary strings,
                @Cached(inline = false) TruffleString.HashCodeNode hashCodeNode) {
            int hashCode = hashCodeNode.execute(strings.getTString(string), strings.getTEncoding(string));
            return getContext(node).getHashing(node).hash(CLASS_SALT, hashCode);
        }
    }

    public abstract static class StringGetAssociatedNode extends RubyBaseNode {

        @NeverDefault
        public static StringGetAssociatedNode create() {
            return StringHelperNodesFactory.StringGetAssociatedNodeGen.create();
        }

        public abstract Object execute(Object string);

        @Specialization(limit = "getDynamicObjectCacheLimit()")
        protected Object getAssociated(RubyString string,
                @CachedLibrary("string") DynamicObjectLibrary objectLibrary) {
            return objectLibrary.getOrDefault(string, Layouts.ASSOCIATED_IDENTIFIER, null);
        }

        @Specialization
        protected Object getAssociatedImmutable(ImmutableRubyString string) {
            return null;
        }

    }

    public abstract static class CheckIndexNode extends RubyBaseNode {

        public abstract int executeCheck(int index, int length);

        @Specialization
        protected int checkIndex(int index, int length,
                @Cached InlinedConditionProfile negativeIndexProfile,
                @Cached InlinedBranchProfile errorProfile) {
            if (index >= length) {
                errorProfile.enter(this);
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().indexErrorOutOfString(index, this));
            }

            if (negativeIndexProfile.profile(this, index < 0)) {
                index += length;
                if (index < 0) {
                    errorProfile.enter(this);
                    throw new RaiseException(
                            getContext(),
                            getContext().getCoreExceptions().indexErrorOutOfString(index, this));
                }
            }

            return index;
        }

    }

    public abstract static class NormalizeIndexNode extends RubyBaseNode {

        @NeverDefault
        public static NormalizeIndexNode create() {
            return StringHelperNodesFactory.NormalizeIndexNodeGen.create();
        }

        public abstract int executeNormalize(int index, int length);

        @Specialization
        protected int normalizeIndex(int index, int length,
                @Cached InlinedConditionProfile negativeIndexProfile) {
            if (negativeIndexProfile.profile(this, index < 0)) {
                return index + length;
            }

            return index;
        }

    }

    public abstract static class InvertAsciiCaseHelperNode extends RubyBaseNode {

        private final boolean lowerToUpper;
        private final boolean upperToLower;

        @NeverDefault
        public static InvertAsciiCaseHelperNode createLowerToUpper() {
            return StringHelperNodesFactory.InvertAsciiCaseHelperNodeGen.create(true, false);
        }

        @NeverDefault
        public static InvertAsciiCaseHelperNode createUpperToLower() {
            return StringHelperNodesFactory.InvertAsciiCaseHelperNodeGen.create(false, true);
        }

        @NeverDefault
        public static InvertAsciiCaseHelperNode createSwapCase() {
            return StringHelperNodesFactory.InvertAsciiCaseHelperNodeGen.create(true, true);
        }

        protected InvertAsciiCaseHelperNode(boolean lowerToUpper, boolean upperToLower) {
            this.lowerToUpper = lowerToUpper;
            this.upperToLower = upperToLower;
        }

        public abstract byte[] executeInvert(RubyString string, TruffleStringIterator iterator, byte[] initialBytes);

        @Specialization
        protected byte[] invert(RubyString string, TruffleStringIterator iterator, byte[] initialBytes,
                @Cached RubyStringLibrary libString,
                @Cached TruffleStringIterator.NextNode nextNode,
                @Cached TruffleString.CopyToByteArrayNode copyToByteArrayNode,
                @Cached InlinedBranchProfile caseSwapProfile) {
            var tstring = string.tstring;
            var encoding = libString.getTEncoding(string);

            byte[] modified = initialBytes;

            while (iterator.hasNext()) {
                int p = iterator.getByteIndex();
                int c = nextNode.execute(iterator);

                if ((lowerToUpper && StringSupport.isAsciiLowercase(c)) ||
                        (upperToLower && StringSupport.isAsciiUppercase(c))) {
                    caseSwapProfile.enter(this);

                    if (modified == null) {
                        modified = copyToByteArrayNode.execute(tstring, encoding);
                    }

                    // Convert lower-case ASCII code point to upper-case or upper-case ASCII code point to lower-case.
                    modified[p] ^= 0x20;
                }
            }

            return modified;
        }
    }

    public abstract static class InvertAsciiCaseNode extends RubyBaseNode {

        @Child private InvertAsciiCaseHelperNode invertNode;

        @NeverDefault
        public static InvertAsciiCaseNode createLowerToUpper() {
            return StringHelperNodesFactory.InvertAsciiCaseNodeGen
                    .create(InvertAsciiCaseHelperNode.createLowerToUpper());
        }

        @NeverDefault
        public static InvertAsciiCaseNode createUpperToLower() {
            return StringHelperNodesFactory.InvertAsciiCaseNodeGen
                    .create(InvertAsciiCaseHelperNode.createUpperToLower());
        }

        @NeverDefault
        public static InvertAsciiCaseNode createSwapCase() {
            return StringHelperNodesFactory.InvertAsciiCaseNodeGen.create(InvertAsciiCaseHelperNode.createSwapCase());
        }

        public InvertAsciiCaseNode(InvertAsciiCaseHelperNode invertNode) {
            this.invertNode = invertNode;
        }

        public abstract Object executeInvert(RubyString string);

        @Specialization
        protected Object invert(RubyString string,
                @Cached RubyStringLibrary libString,
                @Cached TruffleString.CreateCodePointIteratorNode createCodePointIteratorNode,
                @Cached TruffleString.FromByteArrayNode fromByteArrayNode,
                @Cached InlinedConditionProfile noopProfile) {
            var tencoding = libString.getTEncoding(string);
            var iterator = createCodePointIteratorNode.execute(string.tstring, tencoding,
                    ErrorHandling.RETURN_NEGATIVE);
            byte[] modified = invertNode.executeInvert(string, iterator, null);

            if (noopProfile.profile(this, modified == null)) {
                return nil;
            } else {
                string.setTString(fromByteArrayNode.execute(modified, tencoding, false)); // codeRangeNode.execute(rope), codePointLengthNode.execute(rope)
                return string;
            }
        }

    }

    @ImportStatic(StringGuards.class)
    public abstract static class GetCodePointNode extends RubyBaseNode {

        public abstract int executeGetCodePoint(AbstractTruffleString string, RubyEncoding encoding, int byteIndex);

        @Specialization
        protected int getCodePoint(AbstractTruffleString string, RubyEncoding encoding, int byteIndex,
                @Cached TruffleString.CodePointAtByteIndexNode getCodePointNode,
                @Cached InlinedBranchProfile badCodePointProfile) {
            int codePoint = getCodePointNode.execute(string, byteIndex, encoding.tencoding,
                    ErrorHandling.RETURN_NEGATIVE);
            if (codePoint == -1) {
                badCodePointProfile.enter(this);
                throw new RaiseException(getContext(),
                        coreExceptions().argumentErrorInvalidByteSequence(encoding, this));
            }
            return codePoint;
        }

    }

    public abstract static class StringAppendNode extends RubyBaseNode {

        @NeverDefault
        public static StringAppendNode create() {
            return StringHelperNodesFactory.StringAppendNodeGen.create();
        }

        public abstract RubyString executeStringAppend(Object string, Object other);

        @Specialization(guards = "libOther.isRubyString(other)", limit = "1")
        protected static RubyString stringAppend(Object string, Object other,
                @Cached RubyStringLibrary libString,
                @Cached RubyStringLibrary libOther,
                @Cached EncodingNodes.CheckStringEncodingNode checkEncodingNode,
                @Cached TruffleString.ConcatNode concatNode,
                @Bind("this") Node node) {

            var left = libString.getTString(string);
            var leftEncoding = libString.getEncoding(string);
            var right = libOther.getTString(other);
            var rightEncoding = libOther.getEncoding(other);

            final RubyEncoding compatibleEncoding = checkEncodingNode.executeCheckEncoding(node, left, leftEncoding,
                    right, rightEncoding);

            var result = concatNode.execute(left, right, compatibleEncoding.tencoding, true);
            return createString(node, result, compatibleEncoding);
        }
    }

}
