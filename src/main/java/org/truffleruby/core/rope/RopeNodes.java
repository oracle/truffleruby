/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
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

import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.profiles.LoopConditionProfile;
import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.string.StringAttributes;
import org.truffleruby.core.string.StringSupport;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.utils.Utils;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.SlowPathException;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class RopeNodes {

    /** See {@link RopeOperations#calculateCodeRangeAndLength} */
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
        protected StringAttributes calculateAttributesAsciiCompatible(Encoding encoding, Bytes bytes,
                @Cached LoopConditionProfile loopProfile)
                throws NonAsciiCharException {
            // Optimistically assume this string consists only of ASCII characters. If a non-ASCII character is found,
            // fail over to a more generalized search.

            int i = 0;
            try {
                for (; loopProfile.inject(i < bytes.length); i++) {
                    if (bytes.get(i) < 0) {
                        throw new NonAsciiCharException();
                    }
                    TruffleSafepoint.poll(this);
                }
            } finally {
                profileAndReportLoopCount(loopProfile, i);
            }

            return new StringAttributes(bytes.length, CR_7BIT);
        }

        /** See {@link StringSupport#strLengthWithCodeRangeAsciiCompatible} */
        @Specialization(
                replaces = "calculateAttributesAsciiCompatible",
                guards = { "!bytes.isEmpty()", "!isBinaryString(encoding)", "isAsciiCompatible(encoding)" })
        protected StringAttributes calculateAttributesAsciiCompatibleGeneric(Encoding encoding, Bytes bytes,
                @Cached CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached ConditionProfile validCharacterProfile) {
            CodeRange codeRange = CR_7BIT;
            int characters = 0;
            int p = 0;
            final int end = bytes.length;

            while (p < end) {
                if (Encoding.isAscii(bytes.get(p))) {
                    final int multiByteCharacterPosition = StringSupport.searchNonAscii(bytes.sliceRange(p, end));

                    if (multiByteCharacterPosition == -1) {
                        return new StringAttributes(characters + (end - p), codeRange);
                    }

                    characters += multiByteCharacterPosition;
                    p += multiByteCharacterPosition;
                }

                final int lengthOfCurrentCharacter = calculateCharacterLengthNode
                        .characterLength(encoding, CR_UNKNOWN, bytes.sliceRange(p, end));

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

        /** See {@link StringSupport#strLengthWithCodeRangeNonAsciiCompatible} */
        @Specialization(guards = { "!bytes.isEmpty()", "!isBinaryString(encoding)", "!isAsciiCompatible(encoding)" })
        protected StringAttributes calculateAttributesNonAsciiCompatible(Encoding encoding, Bytes bytes,
                @Cached CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached ConditionProfile validCharacterProfile,
                @Cached ConditionProfile fixedWidthProfile) {
            CodeRange codeRange = CR_VALID;
            int characters;
            int p = 0;
            final int end = bytes.length;

            for (characters = 0; p < end; characters++) {
                final int lengthOfCurrentCharacter = calculateCharacterLengthNode
                        .characterLength(encoding, CR_UNKNOWN, bytes.sliceRange(p, end));

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
                        p += encoding.minLength();
                    }
                }
            }

            return new StringAttributes(characters, codeRange);
        }

        protected static final class NonAsciiCharException extends SlowPathException {
            private static final long serialVersionUID = 5550642254188358382L;
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
                        .characterLengthWithRecovery(encoding, CR_VALID, Bytes.fromRange(bytes, p, e));
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

    public abstract static class DebugPrintRopeNode extends RubyBaseNode {

        public abstract Object executeDebugPrint(Rope rope, int currentLevel, boolean printString);

        @TruffleBoundary
        @Specialization
        protected Object debugPrintLeafRope(LeafRope rope, int currentLevel, boolean printString) {
            printPreamble(currentLevel);

            // Converting a rope to a java.lang.String may populate the byte[], so we need to query for the array status beforehand.
            final boolean bytesAreNull = rope.getRawBytes() == null;

            System.err.println(String.format(
                    "%s (%s; BN: %b; BL: %d; CL: %d; CR: %s; E: %s)",
                    printString ? RopeOperations.escape(rope) : "<skipped>",
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
        protected Object debugPrintNative(NativeRope rope, int currentLevel, boolean printString) {
            printPreamble(currentLevel);

            System.err.println(String.format(
                    "%s (%s; BL: %d; CL: %d; CR: %s; P: 0x%x, S: %d; E: %s)",
                    printString ? RopeOperations.escape(rope) : "<skipped>",
                    rope.getClass().getSimpleName(),
                    rope.byteLength(),
                    rope.characterLength(),
                    rope.getCodeRange(),
                    rope.getNativePointer().getAddress(),
                    rope.getNativePointer().getSize(),
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

    public abstract static class EqualNode extends RubyBaseNode {

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
    public abstract static class BytesEqualNode extends RubyBaseNode {

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
                final String home = getLanguage().getRubyHome();
                return !RopeOperations.anyChildContains(a, home) && !RopeOperations.anyChildContains(b, home);
            } else {
                return true;
            }
        }

    }

    // @Deprecated // Use TruffleString.GetInternalByteArrayNode instead
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

    public abstract static class SingleByteOptimizableNode extends RubyBaseNode {

        public static SingleByteOptimizableNode create() {
            return RopeNodesFactory.SingleByteOptimizableNodeGen.create();
        }

        public abstract boolean execute(Rope rope, RubyEncoding encoding);

        @Specialization
        protected boolean isSingleByteOptimizable(Rope rope, RubyEncoding encoding,
                @Cached ConditionProfile asciiOnlyProfile) {

            if (asciiOnlyProfile.profile(rope.isAsciiOnly())) {
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

        /** This method returns the byte length for the first character encountered in `bytes`. The validity of a
         * character is defined by the `encoding`. If the `codeRange` for the byte sequence is known for the supplied
         * `encoding`, it should be passed to help short-circuit some validation checks. If the `codeRange` is not known
         * for the supplied `encoding`, then `CodeRange.CR_UNKNOWN` should be passed. If the byte sequence is invalid, a
         * negative value will be returned. See `Encoding#length` for details on how to interpret the return value. */
        public int characterLength(Encoding encoding, CodeRange codeRange, Bytes bytes) {
            return executeLength(encoding, codeRange, bytes, false);
        }

        /** This method works very similarly to `characterLength` and maintains the same invariants on inputs. Where it
         * differs is in the treatment of invalid byte sequences. Whereas `characterLength` will return a negative
         * value, this method will always return a positive value. MRI provides an arbitrary, but deterministic,
         * algorithm for returning a byte length for invalid byte sequences. This method is to be used when the
         * `codeRange` might be `CodeRange.CR_BROKEN` and the caller must handle the case without raising an error.
         * E.g., if `String#each_char` is called on a String that is `CR_BROKEN`, you wouldn't want negative byte
         * lengths to be returned because it would break iterating through the characters. */
        public int characterLengthWithRecovery(Encoding encoding, CodeRange codeRange, Bytes bytes) {
            return executeLength(encoding, codeRange, bytes, true);
        }

        @Specialization(guards = "codeRange == CR_7BIT")
        protected int cr7Bit(Encoding encoding, CodeRange codeRange, Bytes bytes, boolean recoverIfBroken) {
            assert bytes.length > 0;
            return 1;
        }

        @Specialization(guards = { "codeRange == CR_VALID", "encoding.isUTF8()" })
        protected int validUtf8(Encoding encoding, CodeRange codeRange, Bytes bytes, boolean recoverIfBroken,
                @Cached @Exclusive BranchProfile oneByteProfile,
                @Cached @Exclusive BranchProfile twoBytesProfile,
                @Cached @Exclusive BranchProfile threeBytesProfile,
                @Cached @Exclusive BranchProfile fourBytesProfile) {
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
                @Cached @Exclusive ConditionProfile asciiCharProfile) {
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
                @Cached @Shared("validCharWidthProfile") ConditionProfile validCharWidthProfile,
                @Cached @Exclusive ConditionProfile minEncodingWidthUsedProfile) {
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
                @Cached @Shared("validCharWidthProfile") ConditionProfile validCharWidthProfile) {

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

    /** Returns a {@link Bytes} object for the given rope and bounds. This will simply get the bytes for the rope and
     * build the object, except in the case of SubstringRope which is optimized to use the bytes of the child rope
     * instead - which is better for footprint. */
    @GenerateUncached
    public abstract static class GetBytesObjectNode extends RubyBaseNode {

        public static GetBytesObjectNode create() {
            return RopeNodesFactory.GetBytesObjectNodeGen.create();
        }

        public static GetBytesObjectNode getUncached() {
            return RopeNodesFactory.GetBytesObjectNodeGen.getUncached();
        }

        public Bytes getBytes(Rope rope) {
            return execute(rope, 0, rope.byteLength());
        }

        public abstract Bytes execute(Rope rope, int offset, int length);

        public Bytes getClamped(Rope rope, int offset, int length) {
            return execute(rope, offset, Math.min(length, rope.byteLength() - offset));
        }

        public Bytes getRange(Rope rope, int start, int end) {
            return execute(rope, start, end - start);
        }

        public Bytes getClampedRange(Rope rope, int start, int end) {
            return execute(rope, start, Math.min(rope.byteLength(), end) - start);
        }

        @Specialization(guards = "rope.getRawBytes() != null")
        protected Bytes getBytesObjectFromRaw(Rope rope, int offset, int length) {
            return new Bytes(rope.getRawBytes(), offset, length);
        }

        @Specialization(guards = { "rope.getRawBytes() == null" })
        protected Bytes getBytesObject(ManagedRope rope, int offset, int length,
                @Cached BytesNode bytes) {
            return new Bytes(bytes.execute(rope), offset, length);
        }

        @Specialization(guards = "rope.getRawBytes() == null")
        protected Bytes getBytesObject(NativeRope rope, int offset, int length) {
            return new Bytes(rope.getBytes(offset, length));
        }
    }
}
