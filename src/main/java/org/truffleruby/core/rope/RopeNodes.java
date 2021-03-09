/*
 * Copyright (c) 2016, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 *
 * Some of the code in this class is modified from org.jruby.util.StringSupport,
 * licensed under the same EPL 2.0/GPL 2.0/LGPL 2.1 used throughout.
 */

package org.truffleruby.core.rope;

import static org.truffleruby.core.rope.CodeRange.CR_7BIT;
import static org.truffleruby.core.rope.CodeRange.CR_BROKEN;
import static org.truffleruby.core.rope.CodeRange.CR_UNKNOWN;
import static org.truffleruby.core.rope.CodeRange.CR_VALID;

import java.util.Arrays;

import com.oracle.truffle.api.dsl.Bind;
import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.SuppressFBWarnings;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.encoding.EncodingNodes;
import org.truffleruby.core.rope.ConcatRope.ConcatState;
import org.truffleruby.core.rope.RopeNodesFactory.AreComparableRopesNodeGen;
import org.truffleruby.core.rope.RopeNodesFactory.CompareRopesNodeGen;
import org.truffleruby.core.rope.RopeNodesFactory.SetByteNodeGen;
import org.truffleruby.core.string.StringAttributes;
import org.truffleruby.core.string.StringSupport;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyContextNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.utils.Utils;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.SlowPathException;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class RopeNodes {

    // Preserves encoding of the top-level Rope
    @GenerateUncached
    public abstract static class SubstringNode extends RubyBaseNode {

        public static SubstringNode create() {
            return RopeNodesFactory.SubstringNodeGen.create();
        }

        public abstract Rope executeSubstring(Rope base, int byteOffset, int byteLength);

        @Specialization(guards = "byteLength == 0")
        protected Rope substringZeroBytes(Rope base, int byteOffset, int byteLength,
                @Cached MakeLeafRopeNode makeLeafRopeNode) {
            return makeLeafRopeNode.executeMake(RopeConstants.EMPTY_BYTES, base.getEncoding(), CR_UNKNOWN, 0);
        }

        @Specialization(guards = "byteLength == 1")
        protected Rope substringOneByte(Rope base, int byteOffset, int byteLength,
                @Cached ConditionProfile isUTF8,
                @Cached ConditionProfile isUSAscii,
                @Cached ConditionProfile isAscii8Bit,
                @Cached GetByteNode getByteNode,
                @Cached WithEncodingNode withEncodingNode) {
            final int index = getByteNode.executeGetByte(base, byteOffset);

            if (isUTF8.profile(base.getEncoding() == UTF8Encoding.INSTANCE)) {
                return RopeConstants.UTF8_SINGLE_BYTE_ROPES[index];
            }

            if (isUSAscii.profile(base.getEncoding() == USASCIIEncoding.INSTANCE)) {
                return RopeConstants.US_ASCII_SINGLE_BYTE_ROPES[index];
            }

            if (isAscii8Bit.profile(base.getEncoding() == ASCIIEncoding.INSTANCE)) {
                return RopeConstants.ASCII_8BIT_SINGLE_BYTE_ROPES[index];
            }

            return withEncodingNode
                    .executeWithEncoding(RopeConstants.ASCII_8BIT_SINGLE_BYTE_ROPES[index], base.getEncoding());
        }

        @Specialization(guards = { "byteLength > 1", "sameAsBase(base, byteLength)" })
        protected Rope substringSameAsBase(Rope base, int byteOffset, int byteLength) {
            return base;
        }

        @Specialization(guards = { "byteLength > 1", "!sameAsBase(base, byteLength)" })
        protected Rope substringLeafRope(LeafRope base, int byteOffset, int byteLength,
                @Cached MakeSubstringRopeNode makeSubstringRopeNode) {
            return makeSubstringRopeNode.executeMake(base.getEncoding(), base, byteOffset, byteLength);
        }

        @Specialization(guards = { "byteLength > 1", "!sameAsBase(base, byteLength)" })
        protected Rope substringSubstringRope(SubstringRope base, int byteOffset, int byteLength,
                @Cached MakeSubstringRopeNode makeSubstringRopeNode) {
            return substringSubstringRopeWithEncoding(
                    base.getEncoding(),
                    base,
                    byteOffset,
                    byteLength,
                    makeSubstringRopeNode);
        }

        private Rope substringSubstringRopeWithEncoding(Encoding encoding, SubstringRope rope, int byteOffset,
                int byteLength, MakeSubstringRopeNode makeSubstringRopeNode) {
            return makeSubstringRopeNode
                    .executeMake(encoding, rope.getChild(), byteOffset + rope.getByteOffset(), byteLength);
        }

        @Specialization(guards = { "byteLength > 1", "!sameAsBase(base, byteLength)" })
        protected Rope substringRepeatingRope(RepeatingRope base, int byteOffset, int byteLength,
                @Cached WithEncodingNode withEncodingNode,
                @Cached MakeSubstringRopeNode makeSubstringRopeNode,
                @Cached ConditionProfile matchesChildProfile) {
            return substringRepeatingRopeWithEncoding(
                    base.getEncoding(),
                    base,
                    byteOffset,
                    byteLength,
                    matchesChildProfile,
                    makeSubstringRopeNode,
                    withEncodingNode);
        }

        private Rope substringRepeatingRopeWithEncoding(Encoding encoding, RepeatingRope rope, int byteOffset,
                int byteLength, ConditionProfile matchesChildProfile, MakeSubstringRopeNode makeSubstringRopeNode,
                WithEncodingNode withEncodingNode) {
            final boolean offsetFitsChild = byteOffset % rope.getChild().byteLength() == 0;
            final boolean byteLengthFitsChild = byteLength == rope.getChild().byteLength();

            // TODO (nirvdrum 07-Apr-16) We can specialize any number of children that fit perfectly into the length, not just count == 1. But we may need to create a new RepeatingNode to handle count > 1.
            if (matchesChildProfile.profile(offsetFitsChild && byteLengthFitsChild)) {
                return withEncodingNode.executeWithEncoding(rope.getChild(), encoding);
            }

            return makeSubstringRopeNode.executeMake(encoding, rope, byteOffset, byteLength);
        }

        @Specialization(guards = { "byteLength > 1", "!sameAsBase(base, byteLength)" })
        protected Rope substringLazyRope(LazyIntRope base, int byteOffset, int byteLength,
                @Cached MakeSubstringRopeNode makeSubstringRopeNode) {
            return makeSubstringRopeNode.executeMake(base.getEncoding(), base, byteOffset, byteLength);
        }

        @Specialization(guards = { "byteLength > 1", "!sameAsBase(base, byteLength)" })
        protected Rope substringNativeRope(NativeRope base, int byteOffset, int byteLength,
                @Cached MakeLeafRopeNode makeLeafRopeNode) {
            return makeLeafRopeNode.executeMake(
                    base.getBytes(byteOffset, byteLength),
                    base.getEncoding(),
                    CR_UNKNOWN,
                    NotProvided.INSTANCE);
        }

        @Specialization(guards = { "byteLength > 1", "!sameAsBase(base, byteLength)" })
        protected Rope substringConcatRope(ConcatRope base, int byteOffset, int byteLength,
                @Cached BytesNode bytesNode,
                @Cached MakeSubstringRopeNode makeSubstringRopeNode) {
            // NOTE(norswap, 19 Nov 2020):
            //  We flatten the rope here. This avoids issue in the (fairly common) case where the rope tree is basically
            //  a linked list. In that case, reading successive substrings causes increasingly bigger concat ropes
            //  to be flattened. So better to preventively flatten at the top. This is also generally beneficial if
            //  we shift from a write-heavy load (rope tree creation) to a read-heavy load.
            bytesNode.execute(base); // flatten rope
            return makeSubstringRopeNode.executeMake(base.getEncoding(), base, byteOffset, byteLength);
        }

        protected static boolean sameAsBase(Rope base, int byteLength) {
            // A SubstringRope's byte length is not allowed to be larger than its child. Thus, if it has the same
            // byte length as its child, it must be logically equivalent to the child.
            return byteLength == base.byteLength();
        }

    }

    @GenerateUncached
    public abstract static class MakeSubstringRopeNode extends RubyBaseNode {

        public static MakeSubstringRopeNode create() {
            return RopeNodesFactory.MakeSubstringRopeNodeGen.create();
        }

        public abstract Rope executeMake(Encoding encoding, Rope base, int byteOffset, int byteLength);

        @Specialization(guards = "base.isAsciiOnly()")
        protected Rope makeSubstring7Bit(Encoding encoding, ManagedRope base, int byteOffset, int byteLength) {
            return new SubstringRope(encoding, base, byteOffset, byteLength, byteLength, CR_7BIT);
        }

        @Specialization(guards = "!base.isAsciiOnly()")
        protected Rope makeSubstringNon7Bit(Encoding encoding, ManagedRope base, int byteOffset, int byteLength,
                @Cached GetBytesObjectNode getBytesObject,
                @Cached CalculateAttributesNode calculateAttributes) {

            final StringAttributes attributes = calculateAttributes
                    .executeCalculateAttributes(encoding, getBytesObject.execute(base, byteOffset, byteLength));

            final CodeRange codeRange = attributes.getCodeRange();
            final int characterLength = attributes.getCharacterLength();

            return new SubstringRope(encoding, base, byteOffset, byteLength, characterLength, codeRange);
        }

        @Specialization
        protected Rope makeSubstringNativeRope(Encoding encoding, NativeRope base, int byteOffset, int byteLength,
                @Cached ConditionProfile asciiOnlyProfile,
                @Cached AsciiOnlyNode asciiOnlyNode,
                @Cached MakeLeafRopeNode makeLeafRopeNode) {
            final byte[] bytes = new byte[byteLength];
            base.copyTo(byteOffset, bytes, 0, byteLength);

            final CodeRange codeRange;
            final Object characterLength;

            if (asciiOnlyProfile.profile(asciiOnlyNode.execute(base))) {
                codeRange = CR_7BIT;
                characterLength = byteLength;
            } else {
                codeRange = CR_UNKNOWN;
                characterLength = NotProvided.INSTANCE;
            }

            return makeLeafRopeNode.executeMake(bytes, encoding, codeRange, characterLength);
        }

    }

    @ImportStatic(RopeGuards.class)
    @GenerateUncached
    public abstract static class CalculateAttributesNode extends RubyBaseNode {

        public static CalculateAttributesNode create() {
            return RopeNodesFactory.CalculateAttributesNodeGen.create();
        }

        abstract StringAttributes executeCalculateAttributes(Encoding encoding, Bytes bytes);

        @Specialization(guards = "bytes.isEmpty()")
        protected StringAttributes calculateAttributesEmpty(Encoding encoding, Bytes bytes,
                @Cached ConditionProfile isAsciiCompatible) {
            return new StringAttributes(
                    0,
                    isAsciiCompatible.profile(encoding.isAsciiCompatible()) ? CR_7BIT : CR_VALID);
        }

        @Specialization(guards = { "!bytes.isEmpty()", "isBinaryString(encoding)" })
        protected StringAttributes calculateAttributesBinaryString(Encoding encoding, Bytes bytes,
                @Cached BranchProfile nonAsciiStringProfile) {
            CodeRange codeRange = CR_7BIT;

            for (int i = 0; i < bytes.length; i++) {
                if (bytes.get(i) < 0) {
                    nonAsciiStringProfile.enter();
                    codeRange = CR_VALID;
                    break;
                }
            }

            return new StringAttributes(bytes.length, codeRange);
        }

        @Specialization(
                rewriteOn = NonAsciiCharException.class,
                guards = { "!bytes.isEmpty()", "!isBinaryString(encoding)", "isAsciiCompatible(encoding)" })
        protected StringAttributes calculateAttributesAsciiCompatible(Encoding encoding, Bytes bytes)
                throws NonAsciiCharException {
            // Optimistically assume this string consists only of ASCII characters. If a non-ASCII character is found,
            // fail over to a more generalized search.
            for (int i = 0; i < bytes.length; i++) {
                if (bytes.get(i) < 0) {
                    throw new NonAsciiCharException();
                }
            }

            return new StringAttributes(bytes.length, CR_7BIT);
        }

        @Specialization(
                replaces = "calculateAttributesAsciiCompatible",
                guards = { "!bytes.isEmpty()", "!isBinaryString(encoding)", "isAsciiCompatible(encoding)" })
        protected StringAttributes calculateAttributesAsciiCompatibleGeneric(Encoding encoding, Bytes bytes,
                @Cached CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached ConditionProfile validCharacterProfile) {
            // Taken from StringSupport.strLengthWithCodeRangeAsciiCompatible.

            CodeRange codeRange = CR_7BIT;
            int characters = 0;
            int p = 0;
            final int end = bytes.length;

            while (p < end) {
                if (Encoding.isAscii(bytes.get(p))) {
                    final int multiByteCharacterPosition = StringSupport.searchNonAscii(bytes, p, end);
                    if (multiByteCharacterPosition == -1) {
                        return new StringAttributes(characters + (end - p), codeRange);
                    }

                    characters += multiByteCharacterPosition - p;
                    p = multiByteCharacterPosition;
                }

                final int lengthOfCurrentCharacter = calculateCharacterLengthNode
                        .characterLength(encoding, CR_UNKNOWN, bytes, p, end);

                if (validCharacterProfile.profile(lengthOfCurrentCharacter > 0)) {
                    if (codeRange != CR_BROKEN) {
                        codeRange = CR_VALID;
                    }

                    p += lengthOfCurrentCharacter;
                } else {
                    codeRange = CR_BROKEN;
                    p++;
                }

                characters++;
            }

            return new StringAttributes(characters, codeRange);
        }


        @Specialization(guards = { "!bytes.isEmpty()", "!isBinaryString(encoding)", "!isAsciiCompatible(encoding)" })
        protected StringAttributes calculateAttributesGeneric(Encoding encoding, Bytes bytes,
                @Cached CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached ConditionProfile validCharacterProfile,
                @Cached ConditionProfile fixedWidthProfile) {
            // Taken from StringSupport.strLengthWithCodeRangeNonAsciiCompatible.

            CodeRange codeRange = CR_VALID;
            int characters;
            int p = 0;
            final int end = bytes.length;

            for (characters = 0; p < end; characters++) {
                final int lengthOfCurrentCharacter = calculateCharacterLengthNode
                        .characterLength(encoding, CR_UNKNOWN, bytes, p, end);

                if (validCharacterProfile.profile(lengthOfCurrentCharacter > 0)) {
                    p += lengthOfCurrentCharacter;
                } else {
                    codeRange = CR_BROKEN;

                    // If a string is detected as broken and we already know the character length due to a
                    // fixed width encoding, there's no value in visiting any more bytes.
                    if (fixedWidthProfile.profile(encoding.isFixedWidth())) {
                        characters = (bytes.length + encoding.minLength() - 1) / encoding.minLength();

                        return new StringAttributes(characters, CR_BROKEN);
                    } else {
                        p++;
                    }
                }
            }

            return new StringAttributes(characters, codeRange);
        }

        protected static final class NonAsciiCharException extends SlowPathException {
            private static final long serialVersionUID = 5550642254188358382L;
        }

    }

    public abstract static class ConcatNode extends RubyContextNode {

        public static ConcatNode create() {
            return RopeNodesFactory.ConcatNodeGen.create();
        }

        public abstract Rope executeConcat(Rope left, Rope right, Encoding encoding);

        @Specialization
        protected Rope concatNativeRopeLeft(NativeRope left, Rope right, Encoding encoding,
                @Cached NativeToManagedNode nativeToManagedNode,
                @Cached ConditionProfile emptyNativeRopeProfile) {
            if (emptyNativeRopeProfile.profile(left.isEmpty())) {
                return right;
            } else {
                return executeConcat(nativeToManagedNode.execute(left), right, encoding);
            }
        }

        @Specialization
        protected Rope concatNativeRopeRight(Rope left, NativeRope right, Encoding encoding,
                @Cached NativeToManagedNode nativeToManagedNode,
                @Cached ConditionProfile emptyNativeRopeProfile) {
            if (emptyNativeRopeProfile.profile(right.isEmpty())) {
                return left;
            } else {
                return executeConcat(left, nativeToManagedNode.execute(right), encoding);
            }
        }

        @Specialization(guards = "left.isEmpty()")
        protected Rope concatLeftEmpty(Rope left, ManagedRope right, Encoding encoding,
                @Cached WithEncodingNode withEncodingNode) {
            return withEncodingNode.executeWithEncoding(right, encoding);
        }

        @Specialization(guards = "right.isEmpty()")
        protected Rope concatRightEmpty(ManagedRope left, Rope right, Encoding encoding,
                @Cached WithEncodingNode withEncodingNode) {
            return withEncodingNode.executeWithEncoding(left, encoding);
        }

        @SuppressFBWarnings("RV")
        @Specialization(guards = { "!left.isEmpty()", "!right.isEmpty()", "!isCodeRangeBroken(left, right)" })
        protected Rope concat(ManagedRope left, ManagedRope right, Encoding encoding,
                @Cached ConditionProfile sameCodeRangeProfile,
                @Cached ConditionProfile brokenCodeRangeProfile) {
            try {
                Math.addExact(left.byteLength(), right.byteLength());
            } catch (ArithmeticException e) {
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().argumentErrorTooLargeString(this));
            }

            return new ConcatRope(
                    left,
                    right,
                    encoding,
                    commonCodeRange(
                            left.getCodeRange(),
                            right.getCodeRange(),
                            sameCodeRangeProfile,
                            brokenCodeRangeProfile));
        }

        @SuppressFBWarnings("RV")
        @Specialization(guards = { "!left.isEmpty()", "!right.isEmpty()", "isCodeRangeBroken(left, right)" })
        protected Rope concatCrBroken(ManagedRope left, ManagedRope right, Encoding encoding,
                @Cached MakeLeafRopeNode makeLeafRopeNode,
                @Cached BytesNode leftBytesNode,
                @Cached BytesNode rightBytesNode) {
            // This specialization was added to a special case where broken code range(s),
            // may concat to form a valid code range.
            try {
                Math.addExact(left.byteLength(), right.byteLength());
            } catch (ArithmeticException e) {
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().argumentErrorTooLargeString(this));
            }

            final byte[] leftBytes = leftBytesNode.execute(left);
            final byte[] rightBytes = rightBytesNode.execute(right);
            final byte[] bytes = new byte[leftBytes.length + rightBytes.length];
            System.arraycopy(leftBytes, 0, bytes, 0, leftBytes.length);
            System.arraycopy(rightBytes, 0, bytes, leftBytes.length, rightBytes.length);
            return makeLeafRopeNode.executeMake(bytes, encoding, CR_UNKNOWN, NotProvided.INSTANCE);
        }

        public static CodeRange commonCodeRange(CodeRange first, CodeRange second,
                ConditionProfile sameCodeRangeProfile,
                ConditionProfile brokenCodeRangeProfile) {
            if (sameCodeRangeProfile.profile(first == second)) {
                return first;
            }

            if (brokenCodeRangeProfile.profile((first == CR_BROKEN) || (second == CR_BROKEN))) {
                return CR_BROKEN;
            }

            // If we get this far, one must be CR_7BIT and the other must be CR_VALID, so promote to the more general code range.
            return CR_VALID;
        }

        public static CodeRange commonCodeRange(CodeRange first, CodeRange second) {
            if (first == second) {
                return first;
            }

            if ((first == CR_BROKEN) || (second == CR_BROKEN)) {
                return CR_BROKEN;
            }

            // If we get this far, one must be CR_7BIT and the other must be CR_VALID, so promote to the more general code range.
            return CR_VALID;
        }

        protected static boolean isCodeRangeBroken(ManagedRope first, ManagedRope second) {
            return first.getCodeRange() == CR_BROKEN || second.getCodeRange() == CR_BROKEN;
        }
    }

    @ImportStatic(RopeGuards.class)
    @GenerateUncached
    public abstract static class MakeLeafRopeNode extends RubyBaseNode {

        public static MakeLeafRopeNode create() {
            return RopeNodesFactory.MakeLeafRopeNodeGen.create();
        }

        public abstract LeafRope executeMake(byte[] bytes, Encoding encoding, CodeRange codeRange,
                Object characterLength);

        @Specialization(guards = "is7Bit(codeRange)")
        protected LeafRope makeAsciiOnlyLeafRope(
                byte[] bytes, Encoding encoding, CodeRange codeRange, Object characterLength) {
            return new AsciiOnlyLeafRope(bytes, encoding);
        }

        @Specialization(guards = "isValid(codeRange)")
        protected LeafRope makeValidLeafRopeWithCharacterLength(
                byte[] bytes, Encoding encoding, CodeRange codeRange, int characterLength) {
            return new ValidLeafRope(bytes, encoding, characterLength);
        }

        @Specialization(guards = { "isValid(codeRange)", "isFixedWidth(encoding)" })
        protected LeafRope makeValidLeafRopeFixedWidthEncoding(
                byte[] bytes, Encoding encoding, CodeRange codeRange, NotProvided characterLength) {
            final int calculatedCharacterLength = bytes.length / encoding.minLength();

            return new ValidLeafRope(bytes, encoding, calculatedCharacterLength);
        }

        @Specialization(guards = { "isValid(codeRange)", "!isFixedWidth(encoding)", "isAsciiCompatible(encoding)" })
        protected LeafRope makeValidLeafRopeAsciiCompat(
                byte[] bytes, Encoding encoding, CodeRange codeRange, NotProvided characterLength,
                @Cached BranchProfile errorProfile,
                @Cached CalculateCharacterLengthNode calculateCharacterLengthNode) {
            // Extracted from StringSupport.strLength.

            int calculatedCharacterLength = 0;
            int p = 0;
            int e = bytes.length;

            while (p < e) {
                if (Encoding.isAscii(bytes[p])) {
                    int q = StringSupport.searchNonAscii(bytes, p, e);
                    if (q == -1) {
                        calculatedCharacterLength += (e - p);
                        break;
                    }
                    calculatedCharacterLength += q - p;
                    p = q;
                }

                final int delta = calculateCharacterLengthNode
                        .characterLengthWithRecovery(encoding, CR_VALID, bytes, p, e);
                if (delta < 0) {
                    errorProfile.enter();
                    throw Utils.unsupportedOperation(
                            "Code range is reported as valid, but is invalid for the given encoding: ",
                            encoding);
                }

                p += delta;
                calculatedCharacterLength++;
            }

            return new ValidLeafRope(bytes, encoding, calculatedCharacterLength);
        }

        @Specialization(guards = { "isValid(codeRange)", "!isFixedWidth(encoding)", "!isAsciiCompatible(encoding)" })
        protected LeafRope makeValidLeafRope(
                byte[] bytes, Encoding encoding, CodeRange codeRange, NotProvided characterLength) {
            // Extracted from StringSupport.strLength.

            int calculatedCharacterLength;
            int p = 0;
            int e = bytes.length;

            for (calculatedCharacterLength = 0; p < e; calculatedCharacterLength++) {
                p += StringSupport.characterLength(encoding, codeRange, bytes, p, e);
            }

            return new ValidLeafRope(bytes, encoding, calculatedCharacterLength);
        }

        @Specialization(guards = "isBroken(codeRange)")
        protected LeafRope makeInvalidLeafRope(
                byte[] bytes, Encoding encoding, CodeRange codeRange, Object characterLength) {
            return new InvalidLeafRope(bytes, encoding, RopeOperations.strLength(encoding, bytes, 0, bytes.length));
        }

        @Specialization(guards = { "isUnknown(codeRange)", "isEmpty(bytes)" })
        protected LeafRope makeUnknownLeafRopeEmpty(
                byte[] bytes, Encoding encoding, CodeRange codeRange, Object characterLength,
                @Cached ConditionProfile isUTF8,
                @Cached ConditionProfile isUSAscii,
                @Cached ConditionProfile isAscii8Bit,
                @Cached ConditionProfile isAsciiCompatible) {
            if (isUTF8.profile(encoding == UTF8Encoding.INSTANCE)) {
                return RopeConstants.EMPTY_UTF8_ROPE;
            }

            if (isUSAscii.profile(encoding == USASCIIEncoding.INSTANCE)) {
                return RopeConstants.EMPTY_US_ASCII_ROPE;
            }

            if (isAscii8Bit.profile(encoding == ASCIIEncoding.INSTANCE)) {
                return RopeConstants.EMPTY_ASCII_8BIT_ROPE;
            }

            if (isAsciiCompatible.profile(encoding.isAsciiCompatible())) {
                return new AsciiOnlyLeafRope(RopeConstants.EMPTY_BYTES, encoding);
            }

            return new ValidLeafRope(RopeConstants.EMPTY_BYTES, encoding, 0);
        }

        @Specialization(guards = { "isUnknown(codeRange)", "!isEmpty(bytes)" })
        protected LeafRope makeUnknownLeafRopeGeneric(
                byte[] bytes, Encoding encoding, CodeRange codeRange, Object characterLength,
                @Cached CalculateAttributesNode calculateAttributesNode,
                @Cached BranchProfile asciiOnlyProfile,
                @Cached BranchProfile validProfile,
                @Cached BranchProfile brokenProfile,
                @Cached BranchProfile errorProfile) {
            final StringAttributes attributes = calculateAttributesNode
                    .executeCalculateAttributes(encoding, new Bytes(bytes));

            switch (attributes.getCodeRange()) {
                case CR_7BIT: {
                    asciiOnlyProfile.enter();
                    return new AsciiOnlyLeafRope(bytes, encoding);
                }

                case CR_VALID: {
                    validProfile.enter();
                    return new ValidLeafRope(bytes, encoding, attributes.getCharacterLength());
                }

                case CR_BROKEN: {
                    brokenProfile.enter();
                    return new InvalidLeafRope(bytes, encoding, attributes.getCharacterLength());
                }

                default: {
                    errorProfile.enter();
                    throw Utils.unsupportedOperation(
                            "CR_UNKNOWN encountered, but code range should have been calculated");
                }
            }
        }

        protected static boolean is7Bit(CodeRange codeRange) {
            return codeRange == CR_7BIT;
        }

        protected static boolean isValid(CodeRange codeRange) {
            return codeRange == CR_VALID;
        }

        protected static boolean isBroken(CodeRange codeRange) {
            return codeRange == CR_BROKEN;
        }

        protected static boolean isUnknown(CodeRange codeRange) {
            return codeRange == CodeRange.CR_UNKNOWN;
        }

        protected static boolean isFixedWidth(Encoding encoding) {
            return encoding.isFixedWidth();
        }

    }

    @ImportStatic(RopeGuards.class)
    public abstract static class RepeatNode extends RubyContextNode {

        public static RepeatNode create() {
            return RopeNodesFactory.RepeatNodeGen.create();
        }

        public abstract Rope executeRepeat(Rope base, int times);

        @Specialization(guards = "times == 0")
        protected Rope repeatZero(Rope base, int times,
                @Cached WithEncodingNode withEncodingNode) {
            return withEncodingNode.executeWithEncoding(RopeConstants.EMPTY_UTF8_ROPE, base.getEncoding());
        }

        @Specialization(guards = "times == 1")
        protected Rope repeatOne(Rope base, int times) {
            return base;
        }

        @TruffleBoundary
        @Specialization(guards = { "isSingleByteString(base)", "times > 1" })
        protected Rope multiplySingleByteString(Rope base, int times,
                @Cached MakeLeafRopeNode makeLeafRopeNode) {
            final byte filler = base.getBytes()[0];

            byte[] buffer = new byte[times];
            Arrays.fill(buffer, filler);

            return makeLeafRopeNode.executeMake(buffer, base.getEncoding(), base.getCodeRange(), times);
        }

        @Specialization(guards = { "!isSingleByteString(base)", "times > 1" })
        protected Rope repeatManaged(ManagedRope base, int times) {
            int byteLength;
            try {
                byteLength = Math.multiplyExact(base.byteLength(), times);
            } catch (ArithmeticException e) {
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().argumentError(
                                "Result of repeating string exceeds the system maximum string length",
                                this));
            }

            return new RepeatingRope(base, times, byteLength);
        }

        @Specialization(guards = { "!isSingleByteString(base)", "times > 1" })
        protected Rope repeatNative(NativeRope base, int times,
                @Cached NativeToManagedNode nativeToManagedNode) {
            return executeRepeat(nativeToManagedNode.execute(base), times);
        }

    }

    public abstract static class DebugPrintRopeNode extends RubyContextNode {

        public abstract Object executeDebugPrint(Rope rope, int currentLevel, boolean printString);

        @TruffleBoundary
        @Specialization
        protected Object debugPrintLeafRope(LeafRope rope, int currentLevel, boolean printString) {
            printPreamble(currentLevel);

            // Converting a rope to a java.lang.String may populate the byte[], so we need to query for the array status beforehand.
            final boolean bytesAreNull = rope.getRawBytes() == null;

            System.err.println(StringUtils.format(
                    "%s (%s; BN: %b; BL: %d; CL: %d; CR: %s; E: %s)",
                    printString ? rope.toString() : "<skipped>",
                    rope.getClass().getSimpleName(),
                    bytesAreNull,
                    rope.byteLength(),
                    rope.characterLength(),
                    rope.getCodeRange(),
                    rope.getEncoding()));

            return nil;
        }

        @TruffleBoundary
        @Specialization
        protected Object debugPrintSubstringRope(SubstringRope rope, int currentLevel, boolean printString) {
            printPreamble(currentLevel);

            // Converting a rope to a java.lang.String may populate the byte[], so we need to query for the array status beforehand.
            final boolean bytesAreNull = rope.getRawBytes() == null;

            System.err.println(StringUtils.format(
                    "%s (%s; BN: %b; BL: %d; CL: %d; CR: %s; O: %d; E: %s)",
                    printString ? rope.toString() : "<skipped>",
                    rope.getClass().getSimpleName(),
                    bytesAreNull,
                    rope.byteLength(),
                    rope.characterLength(),
                    rope.getCodeRange(),
                    rope.getByteOffset(),
                    rope.getEncoding()));

            executeDebugPrint(rope.getChild(), currentLevel + 1, printString);

            return nil;
        }

        @TruffleBoundary
        protected Object debugPrintConcatRopeBytes(ConcatRope rope, int currentLevel, boolean printString) {
            printPreamble(currentLevel);

            final ConcatState state = rope.getState();

            // Before the print, as `toString()` may cause the bytes to become populated.
            final boolean bytesAreNull = rope.getRawBytes() == null;

            if (state.isBytes()) {
                System.err.println(StringUtils.format(
                        "%s (%s; BN: %b; BL: %d; CL: %d; CR: %s; E: %s)",
                        printString ? rope.toString() : "<skipped>",
                        rope.getClass().getSimpleName(),
                        bytesAreNull,
                        rope.byteLength(),
                        rope.characterLength(),
                        rope.getCodeRange(),
                        rope.getEncoding()));
            } else {
                System.err.println(StringUtils.format(
                        "%s (%s; BN: %b; BL: %d; CL: %d; CR: %s; E: %s)",
                        printString ? rope.toString() : "<skipped>",
                        rope.getClass().getSimpleName(),
                        bytesAreNull,
                        rope.byteLength(),
                        rope.characterLength(),
                        rope.getCodeRange(),
                        rope.getEncoding()));

                executeDebugPrint(state.left, currentLevel + 1, printString);
                executeDebugPrint(state.right, currentLevel + 1, printString);
            }

            return nil;
        }

        @TruffleBoundary
        @Specialization
        protected Object debugPrintRepeatingRope(RepeatingRope rope, int currentLevel, boolean printString) {
            printPreamble(currentLevel);

            // Converting a rope to a java.lang.String may populate the byte[], so we need to query for the array status beforehand.
            final boolean bytesAreNull = rope.getRawBytes() == null;

            System.err.println(StringUtils.format(
                    "%s (%s; BN: %b; BL: %d; CL: %d; CR: %s; T: %d; D: %d; E: %s)",
                    printString ? rope.toString() : "<skipped>",
                    rope.getClass().getSimpleName(),
                    bytesAreNull,
                    rope.byteLength(),
                    rope.characterLength(),
                    rope.getCodeRange(),
                    rope.getTimes(),
                    rope.getEncoding()));

            executeDebugPrint(rope.getChild(), currentLevel + 1, printString);

            return nil;
        }

        @TruffleBoundary
        @Specialization
        protected Object debugPrintLazyInt(LazyIntRope rope, int currentLevel, boolean printString) {
            printPreamble(currentLevel);

            // Converting a rope to a java.lang.String may populate the byte[], so we need to query for the array status beforehand.
            final boolean bytesAreNull = rope.getRawBytes() == null;

            System.err.println(StringUtils.format(
                    "%s (%s; BN: %b; BL: %d; CL: %d; CR: %s; V: %d, D: %d; E: %s)",
                    printString ? rope.toString() : "<skipped>",
                    rope.getClass().getSimpleName(),
                    bytesAreNull,
                    rope.byteLength(),
                    rope.characterLength(),
                    rope.getCodeRange(),
                    rope.getValue(),
                    rope.getEncoding()));

            return nil;
        }

        private void printPreamble(int level) {
            if (level > 0) {
                for (int i = 0; i < level; i++) {
                    System.err.print("|  ");
                }
            }
        }

    }

    @GenerateUncached
    public abstract static class WithEncodingNode extends RubyBaseNode {

        public static WithEncodingNode create() {
            return RopeNodesFactory.WithEncodingNodeGen.create();
        }

        public abstract Rope executeWithEncoding(Rope rope, Encoding encoding);

        @Specialization(guards = "rope.getEncoding() == encoding")
        protected Rope withEncodingSameEncoding(Rope rope, Encoding encoding) {
            return rope;
        }

        @Specialization(guards = "rope.getEncoding() != encoding")
        protected Rope nativeRopeWithEncoding(NativeRope rope, Encoding encoding) {
            return rope.withEncoding(encoding);
        }

        @Specialization(
                guards = { "rope.getEncoding() != encoding", "rope.getClass() == cachedRopeClass", },
                limit = "getCacheLimit()")
        protected Rope withEncodingAsciiCompatible(ManagedRope rope, Encoding encoding,
                @Cached("rope.getClass()") Class<? extends Rope> cachedRopeClass,
                @Cached ConditionProfile asciiCompatibleProfile,
                @Cached ConditionProfile asciiOnlyProfile,
                @Cached ConditionProfile binaryEncodingProfile,
                @Cached ConditionProfile bytesNotNull,
                @Cached BytesNode bytesNode,
                @Cached MakeLeafRopeNode makeLeafRopeNode) {

            if (asciiCompatibleProfile.profile(encoding.isAsciiCompatible())) {
                if (asciiOnlyProfile.profile(rope.isAsciiOnly())) {
                    // ASCII-only strings can trivially convert to other ASCII-compatible encodings.
                    return cachedRopeClass.cast(rope).withEncoding7bit(encoding, bytesNotNull);
                } else if (binaryEncodingProfile.profile(encoding == ASCIIEncoding.INSTANCE &&
                        rope.getCodeRange() == CR_VALID &&
                        rope.getEncoding().isAsciiCompatible())) {
                    // ASCII-compatible CR_VALID strings are also CR_VALID in binary, but they might change character length.
                    final Rope binary = cachedRopeClass.cast(rope).withBinaryEncoding(bytesNotNull);
                    assert binary.getCodeRange() == CR_VALID;
                    return binary;
                } else {
                    // The rope either has a broken code range or isn't ASCII-compatible. In the case of a broken
                    // code range, we must perform a new code range scan with the target encoding to see if it's still
                    // broken. In the case of a non-ASCII-compatible encoding we don't have a quick way to reinterpret
                    // the byte sequence.
                    return rescanBytesForEncoding(rope, encoding, bytesNode, makeLeafRopeNode);
                }
            } else {
                // We don't know of any good way to quickly reinterpret bytes from two different encodings, so we
                // must perform a full code range scan and character length calculation.
                return rescanBytesForEncoding(rope, encoding, bytesNode, makeLeafRopeNode);
            }
        }

        private Rope rescanBytesForEncoding(Rope rope, Encoding encoding, BytesNode bytesNode,
                MakeLeafRopeNode makeLeafRopeNode) {
            return makeLeafRopeNode.executeMake(bytesNode.execute(rope), encoding, CR_UNKNOWN, NotProvided.INSTANCE);
        }

        protected int getCacheLimit() {
            return Rope.NUMBER_OF_CONCRETE_CLASSES;
        }

    }

    @GenerateUncached
    public abstract static class GetByteNode extends RubyBaseNode {

        public static GetByteNode create() {
            return RopeNodesFactory.GetByteNodeGen.create();
        }

        public abstract int executeGetByte(Rope rope, int index);

        @Specialization(guards = "rope.getRawBytes() != null")
        protected int getByte(Rope rope, int index) {
            return rope.getRawBytes()[index] & 0xff;
        }

        @Specialization(guards = "rope.getRawBytes() == null")
        protected int getByte(NativeRope rope, int index) {
            return rope.get(index) & 0xff;
        }

        @TruffleBoundary
        @Specialization(guards = "rope.getRawBytes() == null")
        protected int getByte(LazyIntRope rope, int index) {
            return rope.getBytes()[index] & 0xff;
        }

        @Specialization(guards = "rope.getRawBytes() == null")
        protected int getByteSubstringRope(SubstringRope rope, int index,
                @Cached ConditionProfile childRawBytesNullProfile,
                @Cached ByteSlowNode slowByte) {
            if (childRawBytesNullProfile.profile(rope.getChild().getRawBytes() == null)) {
                return slowByte.execute(rope, index) & 0xff;
            }

            return rope.getChild().getRawBytes()[index + rope.getByteOffset()] & 0xff;
        }

        @Specialization(guards = "rope.getRawBytes() == null")
        protected int getByteRepeatingRope(RepeatingRope rope, int index,
                @Cached ConditionProfile childRawBytesNullProfile,
                @Cached ByteSlowNode slowByte) {
            if (childRawBytesNullProfile.profile(rope.getChild().getRawBytes() == null)) {
                return slowByte.execute(rope, index) & 0xff;
            }

            return rope.getChild().getRawBytes()[index % rope.getChild().byteLength()] & 0xff;
        }

        // NOTE(norswap, 12 Jan 2021): The order of the two next specialization is significant.
        //   Normally, @Bind expressions should only be run per node, but that's not the case currently (GR-28671).
        //   Therefore it's important to test isChildren first, as it's possible to transition from children to bytes
        //   but not the other way around.

        @Specialization(guards = "state.isChildren()")
        protected int getByteConcatRope(ConcatRope rope, int index,
                @Cached ConditionProfile stateBytesNotNull,
                @Bind("rope.getState(stateBytesNotNull)") ConcatState state,
                @Cached ConditionProfile chooseLeftChildProfile,
                @Cached ConditionProfile leftChildRawBytesNullProfile,
                @Cached ConditionProfile rightChildRawBytesNullProfile,
                @Cached ByteSlowNode byteSlowLeft,
                @Cached ByteSlowNode byteSlowRight) {
            if (chooseLeftChildProfile.profile(index < state.left.byteLength())) {
                if (leftChildRawBytesNullProfile.profile(state.left.getRawBytes() == null)) {
                    return byteSlowLeft.execute(state.left, index) & 0xff;
                }

                return state.left.getRawBytes()[index] & 0xff;
            }

            if (rightChildRawBytesNullProfile.profile(state.right.getRawBytes() == null)) {
                return byteSlowRight.execute(state.right, index - state.left.byteLength()) & 0xff;
            }

            return state.right.getRawBytes()[index - state.left.byteLength()] & 0xff;
        }

        // Necessary because getRawBytes() might return null, but then be populated and the children nulled
        // before we get to run the other getByteConcatRope.
        @Specialization(guards = "state.isBytes()")
        protected int getByteConcatRope(ConcatRope rope, int index,
                @Cached ConditionProfile stateBytesNotNull,
                @Bind("rope.getState(stateBytesNotNull)") ConcatState state) {
            return state.bytes[index] & 0xff;
        }
    }

    public abstract static class SetByteNode extends RubyContextNode {

        @Child private ConcatNode composedConcatNode = ConcatNode.create();
        @Child private ConcatNode middleConcatNode = ConcatNode.create();
        @Child private MakeLeafRopeNode makeLeafRopeNode = MakeLeafRopeNode.create();
        @Child private SubstringNode leftSubstringNode = SubstringNode.create();
        @Child private SubstringNode rightSubstringNode = SubstringNode.create();

        public static SetByteNode create() {
            return SetByteNodeGen.create();
        }

        public abstract Rope executeSetByte(Rope string, int index, int value);

        @Specialization
        protected Rope setByte(ManagedRope rope, int index, int value) {
            assert 0 <= index && index < rope.byteLength();

            final Rope left = leftSubstringNode.executeSubstring(rope, 0, index);
            final Rope right = rightSubstringNode.executeSubstring(rope, index + 1, rope.byteLength() - index - 1);
            final Rope middle = makeLeafRopeNode.executeMake(
                    new byte[]{ (byte) value },
                    rope.getEncoding(),
                    CodeRange.CR_UNKNOWN,
                    NotProvided.INSTANCE);
            final Rope composed = composedConcatNode.executeConcat(
                    middleConcatNode.executeConcat(left, middle, rope.getEncoding()),
                    right,
                    rope.getEncoding());

            return composed;
        }

        @Specialization
        protected Rope setByte(NativeRope rope, int index, int value) {
            rope.set(index, value);
            return rope;
        }

    }

    public abstract static class GetCodePointNode extends RubyContextNode {

        @Child private CalculateCharacterLengthNode calculateCharacterLengthNode;
        @Child SingleByteOptimizableNode singleByteOptimizableNode = SingleByteOptimizableNode.create();

        public static GetCodePointNode create() {
            return RopeNodesFactory.GetCodePointNodeGen.create();
        }

        public abstract int executeGetCodePoint(Rope rope, int index);

        @Specialization(guards = "singleByteOptimizableNode.execute(rope)")
        protected int getCodePointSingleByte(Rope rope, int index,
                @Cached GetByteNode getByteNode) {
            return getByteNode.executeGetByte(rope, index);
        }

        @Specialization(guards = { "!singleByteOptimizableNode.execute(rope)", "rope.getEncoding().isUTF8()" })
        protected int getCodePointUTF8(Rope rope, int index,
                @Cached GetByteNode getByteNode,
                @Cached BytesNode bytesNode,
                @Cached CodeRangeNode codeRangeNode,
                @Cached EncodingNodes.GetActualEncodingNode getActualEncodingNode,
                @Cached ConditionProfile singleByteCharProfile,
                @Cached BranchProfile errorProfile) {
            final int firstByte = getByteNode.executeGetByte(rope, index);
            if (singleByteCharProfile.profile(firstByte < 128)) {
                return firstByte;
            }

            return getCodePointMultiByte(rope, index, errorProfile, bytesNode, codeRangeNode, getActualEncodingNode);
        }

        @Specialization(guards = { "!singleByteOptimizableNode.execute(rope)", "!rope.getEncoding().isUTF8()" })
        protected int getCodePointMultiByte(Rope rope, int index,
                @Cached BranchProfile errorProfile,
                @Cached BytesNode bytesNode,
                @Cached CodeRangeNode codeRangeNode,
                @Cached EncodingNodes.GetActualEncodingNode getActualEncodingNode) {
            final byte[] bytes = bytesNode.execute(rope);
            final Encoding encoding = rope.getEncoding();
            final Encoding actualEncoding = getActualEncodingNode.execute(rope);
            final CodeRange codeRange = codeRangeNode.execute(rope);

            final int characterLength = characterLength(actualEncoding, codeRange, bytes, index, rope.byteLength());
            if (characterLength <= 0) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        getContext().getCoreExceptions().argumentError(
                                Utils.concat("invalid byte sequence in ", encoding),
                                null));
            }

            return mbcToCode(actualEncoding, bytes, index, rope.byteLength());
        }

        @TruffleBoundary
        private int mbcToCode(Encoding encoding, byte[] bytes, int start, int end) {
            return encoding.mbcToCode(bytes, start, end);
        }

        private int characterLength(Encoding encoding, CodeRange codeRange, byte[] bytes, int start, int end) {
            if (calculateCharacterLengthNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                calculateCharacterLengthNode = insert(CalculateCharacterLengthNode.create());
            }

            return calculateCharacterLengthNode.characterLength(encoding, codeRange, bytes, start, end);
        }

    }

    @ImportStatic(RopeGuards.class)
    public abstract static class FlattenNode extends RubyContextNode {

        @Child private MakeLeafRopeNode makeLeafRopeNode = MakeLeafRopeNode.create();

        public static FlattenNode create() {
            return RopeNodesFactory.FlattenNodeGen.create();
        }

        public abstract LeafRope executeFlatten(Rope rope);

        @Specialization
        protected LeafRope flattenLeafRope(LeafRope rope) {
            return rope;
        }

        @Specialization
        protected LeafRope flattenNativeRope(NativeRope rope,
                @Cached NativeToManagedNode nativeToManagedNode) {
            return nativeToManagedNode.execute(rope);
        }

        @Specialization(guards = { "!isLeafRope(rope)", "rope.getRawBytes() != null" })
        protected LeafRope flattenNonLeafWithBytes(ManagedRope rope) {
            return makeLeafRopeNode
                    .executeMake(rope.getRawBytes(), rope.getEncoding(), rope.getCodeRange(), rope.characterLength());
        }

        @Specialization(guards = { "!isLeafRope(rope)", "rope.getRawBytes() == null" })
        protected LeafRope flatten(ManagedRope rope) {
            // NB: We call RopeOperations.flatten here rather than Rope#getBytes so we don't populate the byte[] in
            // the source `rope`. Otherwise, we'll end up a fully populated reference in both the source `rope` and the
            // flattened one, which could adversely affect GC.
            final byte[] bytes = RopeOperations.flattenBytes(rope);

            return makeLeafRopeNode.executeMake(bytes, rope.getEncoding(), rope.getCodeRange(), rope.characterLength());
        }

    }

    public abstract static class EqualNode extends RubyContextNode {

        public static EqualNode create() {
            return RopeNodesFactory.EqualNodeGen.create();
        }

        public abstract boolean execute(Rope a, Rope b);

        @Specialization(guards = "a == b")
        protected boolean sameRopeEqual(Rope a, Rope b) {
            return true;
        }

        @Specialization
        protected boolean ropesEqual(Rope a, Rope b,
                @Cached BranchProfile differentEncodingProfile,
                @Cached BytesEqualNode bytesEqualNode) {
            if (a.getEncoding() != b.getEncoding()) {
                differentEncodingProfile.enter();
                return false;
            }

            return bytesEqualNode.execute(a, b);
        }

    }

    // This node type checks for the equality of the bytes owned by a rope but does not pay
    // attention to the encoding.
    public abstract static class BytesEqualNode extends RubyContextNode {

        public static BytesEqualNode create() {
            return RopeNodesFactory.BytesEqualNodeGen.create();
        }

        public abstract boolean execute(Rope a, Rope b);

        @Specialization(guards = "a == b")
        protected boolean sameRopes(Rope a, Rope b) {
            return true;
        }

        @Specialization(guards = { "a == cachedA", "b == cachedB", "canBeCached" }, limit = "getIdentityCacheLimit()")
        protected boolean cachedRopes(Rope a, Rope b,
                @Cached("a") Rope cachedA,
                @Cached("b") Rope cachedB,
                @Cached("canBeCached(cachedA, cachedB)") boolean canBeCached,
                @Cached("cachedA.bytesEqual(cachedB)") boolean equal) {
            return equal;
        }

        @Specialization(guards = { "a != b", "a.getRawBytes() != null", "a.getRawBytes() == b.getRawBytes()" })
        protected boolean sameByteArrays(Rope a, Rope b) {
            return true;
        }

        @Specialization(
                guards = {
                        "a != b",
                        "a.getRawBytes() != null",
                        "b.getRawBytes() != null",
                        "a.byteLength() == 1",
                        "b.byteLength() == 1" })
        protected boolean characterEqual(Rope a, Rope b) {
            return a.getRawBytes()[0] == b.getRawBytes()[0];
        }

        @Specialization(guards = "a != b", replaces = { "cachedRopes", "sameByteArrays", "characterEqual" })
        protected boolean fullRopeEqual(Rope a, Rope b,
                @Cached ConditionProfile aRawBytesProfile,
                @Cached BranchProfile sameByteArraysProfile,
                @Cached BranchProfile differentLengthProfile,
                @Cached ConditionProfile aCalculatedHashProfile,
                @Cached ConditionProfile bCalculatedHashProfile,
                @Cached ConditionProfile differentHashProfile,
                @Cached BytesNode aBytesNode,
                @Cached BytesNode bBytesNode) {
            if (aRawBytesProfile.profile(a.getRawBytes() != null) && a.getRawBytes() == b.getRawBytes()) {
                sameByteArraysProfile.enter();
                return true;
            }

            if (a.byteLength() != b.byteLength()) {
                differentLengthProfile.enter();
                return false;
            }

            if (aCalculatedHashProfile.profile(a.isHashCodeCalculated()) &&
                    bCalculatedHashProfile.profile(b.isHashCodeCalculated()) &&
                    differentHashProfile.profile(a.calculatedHashCode() != b.calculatedHashCode())) {
                return false;
            }

            final byte[] aBytes = aBytesNode.execute(a);
            final byte[] bBytes = bBytesNode.execute(b);

            // Fold the a.length == b.length condition at compilation in Arrays.equals() since we already know it holds
            if (aBytes.length != bBytes.length) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                throw new Error("unreachable");
            }
            return Arrays.equals(aBytes, bBytes);
        }

        protected boolean canBeCached(Rope a, Rope b) {
            if (getContext().isPreInitializing()) {
                final String home = getContext().getRubyHome();
                return !RopeOperations.anyChildContains(a, home) && !RopeOperations.anyChildContains(b, home);
            } else {
                return true;
            }
        }

    }

    @GenerateUncached
    public abstract static class BytesNode extends RubyBaseNode {

        public static BytesNode create() {
            return RopeNodesFactory.BytesNodeGen.create();
        }

        public abstract byte[] execute(Rope rope);

        @Specialization(guards = "rope.getRawBytes() != null")
        protected byte[] getBytesManaged(ManagedRope rope) {
            return rope.getRawBytes();
        }

        @TruffleBoundary
        @Specialization(guards = "rope.getRawBytes() == null")
        protected byte[] getBytesManagedAndFlatten(ManagedRope rope) {
            return rope.getBytes();
        }

        @Specialization
        protected byte[] getBytesNative(NativeRope rope) {
            return rope.getBytes();
        }
    }

    @GenerateUncached
    public abstract static class ByteSlowNode extends RubyBaseNode {

        public static ByteSlowNode create() {
            return RopeNodesFactory.ByteSlowNodeGen.create();
        }

        public abstract byte execute(Rope rope, int index);

        @Specialization
        protected byte getByteFromSubString(SubstringRope rope, int index,
                @Cached ByteSlowNode childNode) {
            return childNode.execute(rope.getChild(), rope.getByteOffset() + index);
        }

        @Specialization(guards = "rope.getRawBytes() != null")
        protected byte fastByte(ManagedRope rope, int index) {
            return rope.getRawBytes()[index];
        }

        @Specialization
        protected byte getByteFromNativeRope(NativeRope rope, int index) {
            return rope.getByteSlow(index);
        }

        @TruffleBoundary
        @Specialization(guards = "rope.getRawBytes() == null")
        protected byte getByteFromRope(ManagedRope rope, int index) {
            return rope.getByteSlow(index);
        }
    }

    @GenerateUncached
    public abstract static class AsciiOnlyNode extends RubyBaseNode {

        public static AsciiOnlyNode create() {
            return RopeNodesFactory.AsciiOnlyNodeGen.create();
        }

        public abstract boolean execute(Rope rope);

        @Specialization
        protected boolean asciiOnly(Rope rope,
                @Cached CodeRangeNode codeRangeNode) {
            return codeRangeNode.execute(rope) == CR_7BIT;
        }

    }

    @GenerateUncached
    public abstract static class CodeRangeNode extends RubyBaseNode {

        public static CodeRangeNode create() {
            return RopeNodesFactory.CodeRangeNodeGen.create();
        }

        public abstract CodeRange execute(Rope rope);

        @Specialization
        protected CodeRange getCodeRangeManaged(ManagedRope rope) {
            return rope.getCodeRange();
        }

        @Specialization
        protected CodeRange getCodeRangeNative(NativeRope rope,
                @Cached BytesNode getBytes,
                @Cached CalculateAttributesNode calculateAttributesNode,
                @Cached ConditionProfile unknownCodeRangeProfile,
                @Cached GetBytesObjectNode getBytesObject) {
            if (unknownCodeRangeProfile.profile(rope.getRawCodeRange() == CR_UNKNOWN)) {
                final StringAttributes attributes = calculateAttributesNode
                        .executeCalculateAttributes(rope.getEncoding(), getBytesObject.getBytes(rope));
                rope.updateAttributes(attributes);
                return attributes.getCodeRange();
            } else {
                return rope.getRawCodeRange();
            }
        }

    }

    public abstract static class HashNode extends RubyContextNode {

        public static HashNode create() {
            return RopeNodesFactory.HashNodeGen.create();
        }

        public abstract int execute(Rope rope);

        @Specialization(guards = "rope.isHashCodeCalculated()")
        protected int executeHashCalculated(Rope rope) {
            return rope.calculatedHashCode();
        }

        @Specialization(guards = "!rope.isHashCodeCalculated()")
        protected int executeHashNotCalculated(Rope rope) {
            return rope.hashCode();
        }

    }

    public abstract static class CharacterLengthNode extends RubyContextNode {

        public static CharacterLengthNode create() {
            return RopeNodesFactory.CharacterLengthNodeGen.create();
        }

        public abstract int execute(Rope rope);

        @Specialization
        protected int getCharacterLengthManaged(ManagedRope rope) {
            return rope.characterLength();
        }

        @Specialization
        protected int getCharacterLengthNative(NativeRope rope,
                @Cached BytesNode getBytes,
                @Cached CalculateAttributesNode calculateAttributesNode,
                @Cached ConditionProfile unknownCharacterLengthProfile,
                @Cached GetBytesObjectNode getBytesObjectNode) {
            if (unknownCharacterLengthProfile
                    .profile(rope.rawCharacterLength() == NativeRope.UNKNOWN_CHARACTER_LENGTH)) {
                final StringAttributes attributes = calculateAttributesNode
                        .executeCalculateAttributes(rope.getEncoding(), getBytesObjectNode.getBytes(rope));
                rope.updateAttributes(attributes);
                return attributes.getCharacterLength();
            } else {
                return rope.rawCharacterLength();
            }
        }

    }

    public abstract static class SingleByteOptimizableNode extends RubyContextNode {

        public static SingleByteOptimizableNode create() {
            return RopeNodesFactory.SingleByteOptimizableNodeGen.create();
        }

        public abstract boolean execute(Rope rope);

        @Specialization
        protected boolean isSingleByteOptimizable(Rope rope,
                @Cached AsciiOnlyNode asciiOnlyNode,
                @Cached ConditionProfile asciiOnlyProfile) {
            final boolean asciiOnly = asciiOnlyNode.execute(rope);

            if (asciiOnlyProfile.profile(asciiOnly)) {
                return true;
            } else {
                return rope.getEncoding().isSingleByte();
            }
        }

    }

    @ImportStatic(CodeRange.class)
    @GenerateUncached
    public abstract static class CalculateCharacterLengthNode extends RubyBaseNode {

        public static CalculateCharacterLengthNode create() {
            return RopeNodesFactory.CalculateCharacterLengthNodeGen.create();
        }

        protected abstract int executeLength(Encoding encoding, CodeRange codeRange, Bytes bytes,
                boolean recoverIfBroken);

        // TODO callers should pass a Bytes object which they bound themselves
        //      many of them should not use clipping, but some (in particular using Encoding#maxLength) must clip

        /** This method returns the byte length for the first character encountered in `bytes`, starting at `byteOffset`
         * and ending at `byteEnd`. The validity of a character is defined by the `encoding`. If the `codeRange` for the
         * byte sequence is known for the supplied `encoding`, it should be passed to help short-circuit some validation
         * checks. If the `codeRange` is not known for the supplied `encoding`, then `CodeRange.CR_UNKNOWN` should be
         * passed. If the byte sequence is invalid, a negative value will be returned. See `Encoding#length` for details
         * on how to interpret the return value. */
        public int characterLength(Encoding encoding, CodeRange codeRange, Bytes bytes, int byteOffset, int byteEnd) {
            return executeLength(encoding, codeRange, bytes.clampedRange(byteOffset, byteEnd), false);
        }

        public int characterLength(Encoding encoding, CodeRange codeRange, byte[] bytes, int byteOffset, int byteEnd) {
            // TODO switch callers over to Bytes version
            return executeLength(encoding, codeRange, Bytes.fromRangeClamped(bytes, byteOffset, byteEnd), false);
        }

        /** This method works very similarly to `characterLength` and maintains the same invariants on inputs. Where it
         * differs is in the treatment of invalid byte sequences. Whereas `characterLength` will return a negative
         * value, this method will always return a positive value. MRI provides an arbitrary, but deterministic,
         * algorithm for returning a byte length for invalid byte sequences. This method is to be used when the
         * `codeRange` might be `CodeRange.CR_BROKEN` and the caller must handle the case without raising an error.
         * E.g., if `String#each_char` is called on a String that is `CR_BROKEN`, you wouldn't want negative byte
         * lengths to be returned because it would break iterating through the bytes. */
        public int characterLengthWithRecovery(Encoding encoding, CodeRange codeRange, Bytes bytes, int byteOffset,
                int byteEnd) {
            return executeLength(encoding, codeRange, bytes.clampedRange(byteOffset, byteEnd), true);
        }

        public int characterLengthWithRecovery(Encoding encoding, CodeRange codeRange, byte[] bytes, int byteOffset,
                int byteEnd) {
            // TODO switch callers over to Bytes version
            return executeLength(encoding, codeRange, Bytes.fromRangeClamped(bytes, byteOffset, byteEnd), true);
        }

        @Specialization(guards = "codeRange == CR_7BIT")
        protected int cr7Bit(Encoding encoding, CodeRange codeRange, Bytes bytes, boolean recoverIfBroken) {
            assert bytes.length > 0;
            return 1;
        }

        @Specialization(guards = { "codeRange == CR_VALID", "encoding.isUTF8()" })
        protected int validUtf8(Encoding encoding, CodeRange codeRange, Bytes bytes, boolean recoverIfBroken,
                @Cached BranchProfile oneByteProfile,
                @Cached BranchProfile twoBytesProfile,
                @Cached BranchProfile threeBytesProfile,
                @Cached BranchProfile fourBytesProfile) {
            final byte b = bytes.get(0);
            final int ret;

            if (b >= 0) {
                oneByteProfile.enter();
                ret = 1;
            } else {
                switch (b & 0xf0) {
                    case 0xe0:
                        threeBytesProfile.enter();
                        ret = 3;
                        break;
                    case 0xf0:
                        fourBytesProfile.enter();
                        ret = 4;
                        break;
                    default:
                        twoBytesProfile.enter();
                        ret = 2;
                        break;
                }
            }

            return ret;
        }

        @Specialization(guards = { "codeRange == CR_VALID", "encoding.isAsciiCompatible()" })
        protected int validAsciiCompatible(Encoding encoding, CodeRange codeRange, Bytes bytes, boolean recoverIfBroken,
                @Cached ConditionProfile asciiCharProfile) {
            if (asciiCharProfile.profile(bytes.get(0) >= 0)) {
                return 1;
            } else {
                return encodingLength(encoding, bytes);
            }
        }

        @Specialization(guards = { "codeRange == CR_VALID", "encoding.isFixedWidth()" })
        protected int validFixedWidth(Encoding encoding, CodeRange codeRange, Bytes bytes, boolean recoverIfBroken) {
            final int width = encoding.minLength();
            assert bytes.length >= width;
            return width;
        }

        @Specialization(
                guards = {
                        "codeRange == CR_VALID",
                        /* UTF-8 is ASCII-compatible, so we don't need to check the encoding is not UTF-8 here. */
                        "!encoding.isAsciiCompatible()",
                        "!encoding.isFixedWidth()" })
        protected int validGeneral(Encoding encoding, CodeRange codeRange, Bytes bytes, boolean recoverIfBroken) {
            return encodingLength(encoding, bytes);
        }

        @Specialization(guards = { "codeRange == CR_BROKEN || codeRange == CR_UNKNOWN", "recoverIfBroken" })
        protected int brokenOrUnknownWithRecovery(
                Encoding encoding, CodeRange codeRange, Bytes bytes, boolean recoverIfBroken,
                @Cached ConditionProfile validCharWidthProfile,
                @Cached ConditionProfile minEncodingWidthUsedProfile) {
            final int width = encodingLength(encoding, bytes);

            if (validCharWidthProfile.profile(width > 0 && width <= bytes.length)) {
                return width;
            } else {
                final int minEncodingWidth = encoding.minLength();

                if (minEncodingWidthUsedProfile.profile(minEncodingWidth <= bytes.length)) {
                    return minEncodingWidth;
                } else {
                    return bytes.length;
                }
            }
        }

        @Specialization(guards = { "codeRange == CR_BROKEN || codeRange == CR_UNKNOWN", "!recoverIfBroken" })
        protected int brokenOrUnknownWithoutRecovery(
                Encoding encoding, CodeRange codeRange, Bytes bytes, boolean recoverIfBroken,
                @Cached ConditionProfile validCharWidthProfile) {

            final int width = encodingLength(encoding, bytes);

            if (validCharWidthProfile.profile(width <= bytes.length)) {
                return width;
            } else {
                return StringSupport.MBCLEN_NEEDMORE(width - bytes.length);
            }
        }

        @TruffleBoundary
        private int encodingLength(Encoding encoding, Bytes bytes) {
            return encoding.length(bytes.array, bytes.offset, bytes.offset + bytes.length);
        }

    }

    public abstract static class NativeToManagedNode extends RubyContextNode {

        public static NativeToManagedNode create() {
            return RopeNodesFactory.NativeToManagedNodeGen.create();
        }

        public abstract LeafRope execute(NativeRope rope);

        @Specialization
        protected LeafRope nativeToManaged(NativeRope rope,
                @Cached BytesNode bytesNode,
                @Cached MakeLeafRopeNode makeLeafRopeNode) {
            // Ideally, a NativeRope would always have an accurate code range and character length. However, in practice,
            // it's possible for a bad code range to be associated with the rope due to native memory being updated by
            // 3rd party libraries. So, we must re-calculate the code range and character length values upon conversion
            // to a ManagedRope.
            return makeLeafRopeNode
                    .executeMake(bytesNode.execute(rope), rope.getEncoding(), CR_UNKNOWN, NotProvided.INSTANCE);
        }

    }

    @ImportStatic(RopeGuards.class)
    public abstract static class AreComparableRopesNode extends RubyContextNode {

        public static AreComparableRopesNode create() {
            return AreComparableRopesNodeGen.create();
        }

        @Child CodeRangeNode codeRangeNode = RopeNodes.CodeRangeNode.create();

        public abstract boolean execute(Rope firstRope, Rope secondRope);

        @Specialization(guards = "a.getEncoding() == b.getEncoding()")
        protected boolean sameEncoding(Rope a, Rope b) {
            return true;
        }

        @Specialization(guards = "a.isEmpty()")
        protected boolean firstEmpty(Rope a, Rope b) {
            return true;
        }

        @Specialization(guards = "b.isEmpty()")
        protected boolean secondEmpty(Rope a, Rope b) {
            return true;
        }

        @Specialization(guards = { "is7Bit(a, codeRangeNode)", "is7Bit(b, codeRangeNode)" })
        protected boolean bothCR7bit(Rope a, Rope b) {
            return true;
        }

        @Specialization(guards = { "is7Bit(a, codeRangeNode)", "isAsciiCompatible(b)" })
        protected boolean CR7bitASCII(Rope a, Rope b) {
            return true;
        }

        @Specialization(guards = { "isAsciiCompatible(a)", "is7Bit(b, codeRangeNode)" })
        protected boolean ASCIICR7bit(Rope a, Rope b) {
            return true;
        }

        @Fallback
        protected boolean notCompatible(Rope a, Rope b) {
            return false;
        }

    }

    public abstract static class CompareRopesNode extends RubyContextNode {

        public static CompareRopesNode create() {
            return CompareRopesNodeGen.create();
        }

        public abstract int execute(Rope firstRope, Rope secondRope);

        @Specialization
        protected int compareRopes(Rope firstRope, Rope secondRope,
                @Cached ConditionProfile equalSubsequenceProfile,
                @Cached ConditionProfile equalLengthProfile,
                @Cached ConditionProfile firstStringShorterProfile,
                @Cached ConditionProfile greaterThanProfile,
                @Cached ConditionProfile equalProfile,
                @Cached ConditionProfile notComparableProfile,
                @Cached ConditionProfile encodingIndexGreaterThanProfile,
                @Cached BytesNode firstBytesNode,
                @Cached BytesNode secondBytesNode,
                @Cached AreComparableRopesNode areComparableRopesNode) {
            final boolean firstRopeShorter = firstStringShorterProfile
                    .profile(firstRope.byteLength() < secondRope.byteLength());
            final int memcmpLength;
            if (firstRopeShorter) {
                memcmpLength = firstRope.byteLength();
            } else {
                memcmpLength = secondRope.byteLength();
            }

            final byte[] bytes = firstBytesNode.execute(firstRope);
            final byte[] otherBytes = secondBytesNode.execute(secondRope);

            final int ret;
            final int cmp = ArrayUtils.memcmp(bytes, 0, otherBytes, 0, memcmpLength);
            if (equalSubsequenceProfile.profile(cmp == 0)) {
                if (equalLengthProfile.profile(firstRope.byteLength() == secondRope.byteLength())) {
                    ret = 0;
                } else {
                    if (firstRopeShorter) {
                        ret = -1;
                    } else {
                        ret = 1;
                    }
                }
            } else {
                ret = greaterThanProfile.profile(cmp > 0) ? 1 : -1;
            }

            if (equalProfile.profile(ret == 0)) {
                if (notComparableProfile.profile(!areComparableRopesNode.execute(firstRope, secondRope))) {
                    if (encodingIndexGreaterThanProfile
                            .profile(firstRope.getEncoding().getIndex() > secondRope.getEncoding().getIndex())) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            }

            return ret;

        }
    }

    @GenerateUncached
    public abstract static class GetBytesObjectNode extends RubyBaseNode {

        public static GetBytesObjectNode create() {
            return RopeNodesFactory.GetBytesObjectNodeGen.create();
        }

        public Bytes getBytes(Rope rope) {
            return execute(rope, 0, rope.byteLength());
        }

        public abstract Bytes execute(Rope rope, int offset, int length);

        @Specialization(guards = "rope.getRawBytes() != null")
        protected Bytes getBytesObjectFromRaw(Rope rope, int offset, int length) {
            return new Bytes(rope.getRawBytes(), offset, length);
        }

        @Specialization(guards = "rope.getRawBytes() == null")
        protected Bytes getBytesObject(RepeatingRope rope, int offset, int length) {
            int offsetInChild = offset % rope.getChild().byteLength();
            return offsetInChild + length < rope.getChild().byteLength()
                    ? getChildBytesObject(rope.getChild(), offsetInChild, length)
                    : new Bytes(rope.getBytes(), offset, length);
        }

        @Specialization(guards = "rope.getRawBytes() == null")
        protected Bytes getBytesObject(SubstringRope rope, int offset, int length) {
            return getChildBytesObject(rope.getChild(), rope.getByteOffset() + offset, length);
        }

        @Specialization(guards = "rope.getRawBytes() == null")
        protected Bytes getBytesObject(ConcatRope rope, int offset, int length,
                @Cached ConditionProfile bytesNotNull) {

            final ConcatState state = rope.getState(bytesNotNull);
            if (state.bytes != null) {
                return new Bytes(state.bytes, offset, length);
            }

            final ManagedRope left = state.left;
            final ManagedRope right = state.right;

            if (offset + length <= left.byteLength()) {
                return getChildBytesObject(left, offset, length);
            } else if (offset > left.byteLength()) {
                return getChildBytesObject(right, offset - left.byteLength(), length);
            } else {
                return new Bytes(rope.getBytes(), offset, length);
            }
        }

        @Specialization(guards = { "rope.getRawBytes() == null", "!isSpecializedManagedRope(rope)" })
        protected Bytes getBytesObject(ManagedRope rope, int offset, int length) {
            return new Bytes(rope.getBytes(), offset, length);
        }

        @Specialization(guards = "rope.getRawBytes() == null")
        protected Bytes getBytesObject(NativeRope rope, int offset, int length) {
            return new Bytes(rope.getBytes(offset, length));
        }

        @TruffleBoundary
        protected Bytes getChildBytesObject(Rope child, int offset, int length) {
            return execute(child, offset, length);
        }

        protected static boolean isSpecializedManagedRope(ManagedRope rope) {
            return rope instanceof ConcatRope || rope instanceof RepeatingRope || rope instanceof SubstringRope;
        }
    }
}
