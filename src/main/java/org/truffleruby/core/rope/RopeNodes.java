/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 *
 *
 * Some of the code in this class is modified from org.jruby.util.StringSupport,
 * licensed under the same EPL1.0/GPL 2.0/LGPL 2.1 used throughout.
 */

package org.truffleruby.core.rope;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.SlowPathException;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

import org.jcodings.Encoding;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.USASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.core.string.StringSupport;
import org.truffleruby.core.string.StringUtils;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import static org.truffleruby.core.rope.CodeRange.CR_7BIT;
import static org.truffleruby.core.rope.CodeRange.CR_BROKEN;
import static org.truffleruby.core.rope.CodeRange.CR_UNKNOWN;
import static org.truffleruby.core.rope.CodeRange.CR_VALID;

public abstract class RopeNodes {

    // Preserves encoding of the top-level Rope
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "base"),
            @NodeChild(type = RubyNode.class, value = "byteOffset"),
            @NodeChild(type = RubyNode.class, value = "byteLength")
    })
    public abstract static class MakeSubstringNode extends RubyNode {

        @Child private MakeLeafRopeNode makeLeafRopeNode;

        public static MakeSubstringNode create() {
            return RopeNodesFactory.MakeSubstringNodeGen.create(null, null, null);
        }

        public abstract Rope executeMake(Rope base, int byteOffset, int byteLength);

        @Specialization(guards = "byteLength == 0")
        public Rope substringZeroBytes(Rope base, int byteOffset, int byteLength,
                                        @Cached("createBinaryProfile()") ConditionProfile isUTF8,
                                        @Cached("createBinaryProfile()") ConditionProfile isUSAscii,
                                        @Cached("createBinaryProfile()") ConditionProfile isAscii8Bit,
                                        @Cached("create()") WithEncodingNode withEncodingNode) {
            if (isUTF8.profile(base.getEncoding() == UTF8Encoding.INSTANCE)) {
                return RopeConstants.EMPTY_UTF8_ROPE;
            }

            if (isUSAscii.profile(base.getEncoding() == USASCIIEncoding.INSTANCE)) {
                return RopeConstants.EMPTY_US_ASCII_ROPE;
            }

            if (isAscii8Bit.profile(base.getEncoding() == ASCIIEncoding.INSTANCE)) {
                return RopeConstants.EMPTY_ASCII_8BIT_ROPE;
            }

            return withEncodingNode.executeWithEncoding(RopeConstants.EMPTY_ASCII_8BIT_ROPE, base.getEncoding(), CR_7BIT);
        }

        @Specialization(guards = "byteLength == 1")
        public Rope substringOneByte(Rope base, int byteOffset, int byteLength,
                                        @Cached("createBinaryProfile()") ConditionProfile isUTF8,
                                        @Cached("createBinaryProfile()") ConditionProfile isUSAscii,
                                        @Cached("createBinaryProfile()") ConditionProfile isAscii8Bit,
                                        @Cached("create()") GetByteNode getByteNode) {
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

            return RopeOperations.withEncodingVerySlow(RopeConstants.ASCII_8BIT_SINGLE_BYTE_ROPES[index], base.getEncoding());
        }

        @Specialization(guards = { "byteLength > 1", "sameAsBase(base, byteLength)" })
        public Rope substringSameAsBase(Rope base, int byteOffset, int byteLength) {
            return base;
        }

        @Specialization(guards = { "byteLength > 1", "!sameAsBase(base, byteLength)" })
        public Rope substringLeafRope(LeafRope base, int byteOffset, int byteLength,
                                  @Cached("createBinaryProfile()") ConditionProfile is7BitProfile,
                                  @Cached("createBinaryProfile()") ConditionProfile isBinaryStringProfile) {
            return makeSubstring(base.getEncoding(), base, byteOffset, byteLength, is7BitProfile, isBinaryStringProfile);
        }

        @Specialization(guards = { "byteLength > 1", "!sameAsBase(base, byteLength)" })
        public Rope substringSubstringRope(SubstringRope base, int byteOffset, int byteLength,
                                      @Cached("createBinaryProfile()") ConditionProfile is7BitProfile,
                                      @Cached("createBinaryProfile()") ConditionProfile isBinaryStringProfile) {
            return substringSubstringRopeWithEncoding(base.getEncoding(), base, byteOffset, byteLength, is7BitProfile, isBinaryStringProfile);
        }

        private Rope substringSubstringRopeWithEncoding(Encoding encoding, SubstringRope base, int byteOffset, int byteLength, ConditionProfile is7BitProfile, ConditionProfile isBinaryStringProfile) {
            return makeSubstring(encoding, base.getChild(), byteOffset + base.getByteOffset(), byteLength, is7BitProfile, isBinaryStringProfile);
        }

        @Specialization(guards = { "byteLength > 1", "!sameAsBase(base, byteLength)" })
        public Rope substringRepeatingRope(RepeatingRope base, int byteOffset, int byteLength,
                                          @Cached("createBinaryProfile()") ConditionProfile is7BitProfile,
                                          @Cached("createBinaryProfile()") ConditionProfile isBinaryStringProfile,
                                          @Cached("createBinaryProfile()") ConditionProfile matchesChildProfile) {
            return substringRepeatingRopeWithEncoding(base.getEncoding(), base, byteOffset, byteLength, is7BitProfile, isBinaryStringProfile, matchesChildProfile);
        }

        private Rope substringRepeatingRopeWithEncoding(Encoding encoding, RepeatingRope base, int byteOffset, int byteLength, ConditionProfile is7BitProfile, ConditionProfile isBinaryStringProfile,
                ConditionProfile matchesChildProfile) {
            final boolean offsetFitsChild = byteOffset % base.getChild().byteLength() == 0;
            final boolean byteLengthFitsChild = byteLength == base.getChild().byteLength();

            // TODO (nirvdrum 07-Apr-16) We can specialize any number of children that fit perfectly into the length, not just count == 1. But we may need to create a new RepeatingNode to handle count > 1.
            if (matchesChildProfile.profile(offsetFitsChild && byteLengthFitsChild)) {
                // A RepeatingRope encoding is always the same as its child, so no need to check it
                return base.getChild();
            }

            return makeSubstring(encoding, base, byteOffset, byteLength, is7BitProfile, isBinaryStringProfile);
        }

        @Specialization(guards = { "byteLength > 1", "!sameAsBase(base, byteLength)" })
        public Rope substringLazyRope(LazyRope base, int byteOffset, int byteLength,
                                           @Cached("createBinaryProfile()") ConditionProfile is7BitProfile,
                                           @Cached("createBinaryProfile()") ConditionProfile isBinaryStringProfile) {
            return makeSubstring(base.getEncoding(), base, byteOffset, byteLength, is7BitProfile, isBinaryStringProfile);
        }

        @Specialization(guards = { "byteLength > 1", "!sameAsBase(base, byteLength)" })
        public Rope substringNativeRope(NativeRope base, int byteOffset, int byteLength,
                                        @Cached("createBinaryProfile()") ConditionProfile is7BitProfile,
                                        @Cached("createBinaryProfile()") ConditionProfile isBinaryStringProfile) {
            return makeSubstring(base.getEncoding(), base.toLeafRope(), byteOffset, byteLength, is7BitProfile, isBinaryStringProfile);
        }

        @Specialization(guards = { "byteLength > 1", "!sameAsBase(base, byteLength)" })
        public Rope substringConcatRope(ConcatRope base, int byteOffset, int byteLength,
                                      @Cached("createBinaryProfile()") ConditionProfile is7BitProfile,
                                      @Cached("createBinaryProfile()") ConditionProfile isBinaryStringProfile,
                                      @Cached("createBinaryProfile()") ConditionProfile matchesChildProfile) {
            Rope root = base;

            while (root instanceof ConcatRope) {
                ConcatRope concatRoot = (ConcatRope) root;
                Rope left = concatRoot.getLeft();
                Rope right = concatRoot.getRight();

                // CASE 1: Fits in left.
                if (byteOffset + byteLength <= left.byteLength()) {
                    root = left;
                    continue;
                }

                // CASE 2: Fits in right.
                if (byteOffset >= left.byteLength()) {
                    byteOffset -= left.byteLength();
                    root = right;
                    continue;
                }

                // CASE 3: Spans left and right.
                if (byteLength == root.byteLength()) {
                    return root;
                } else {
                    return makeSubstring(base.getEncoding(), root, byteOffset, byteLength, is7BitProfile, isBinaryStringProfile);
                }
            }

            if (root instanceof SubstringRope) {
                return substringSubstringRopeWithEncoding(base.getEncoding(), (SubstringRope) root, byteOffset, byteLength, is7BitProfile, isBinaryStringProfile);
            } else if (root instanceof RepeatingRope) {
                return substringRepeatingRopeWithEncoding(base.getEncoding(), (RepeatingRope) root, byteOffset, byteLength, is7BitProfile, isBinaryStringProfile, matchesChildProfile);
            }

            return makeSubstring(base.getEncoding(), root, byteOffset, byteLength, is7BitProfile, isBinaryStringProfile);
        }

        private Rope makeSubstring(Encoding encoding, Rope base, int byteOffset, int byteLength, ConditionProfile is7BitProfile, ConditionProfile isBinaryStringProfile) {
            if (is7BitProfile.profile(base.getCodeRange() == CR_7BIT)) {
                if (getContext().getOptions().ROPE_LAZY_SUBSTRINGS) {
                    return new SubstringRope(encoding, base, true, byteOffset, byteLength, byteLength, CR_7BIT);
                } else {
                    return new AsciiOnlyLeafRope(RopeOperations.extractRange(base, byteOffset, byteLength), encoding);
                }
            }

            return makeSubstringNon7Bit(encoding, base, byteOffset, byteLength);
        }

        @TruffleBoundary
        private Rope makeSubstringNon7Bit(Encoding encoding, Rope base, int byteOffset, int byteLength) {
            final long packedLengthAndCodeRange = RopeOperations.calculateCodeRangeAndLength(encoding, base.getBytes(), byteOffset, byteOffset + byteLength);
            final CodeRange codeRange = CodeRange.fromInt(StringSupport.unpackArg(packedLengthAndCodeRange));
            final int characterLength = StringSupport.unpackResult(packedLengthAndCodeRange);
            final boolean singleByteOptimizable = base.isSingleByteOptimizable() || (codeRange == CR_7BIT);

            /*
            if (base.depth() >= 10) {
                System.out.println("SubstringRope depth: " + (base.depth() + 1));
            }
            */

            if (getContext().getOptions().ROPE_LAZY_SUBSTRINGS) {
                return new SubstringRope(encoding, base, singleByteOptimizable, byteOffset, byteLength, characterLength, codeRange);
            } else {
                if (makeLeafRopeNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    makeLeafRopeNode = insert(RopeNodes.MakeLeafRopeNode.create());
                }

                final byte[] bytes = RopeOperations.extractRange(base, byteOffset, byteLength);

                return makeLeafRopeNode.executeMake(bytes, encoding, codeRange, characterLength);
            }
        }

        protected static boolean sameAsBase(Rope base, int byteLength) {
            // A SubstringRope's byte length is not allowed to be larger than its child. Thus, if it has the same
            // byte length as its child, it must be logically equivalent to the child.
            return byteLength == base.byteLength();
        }

    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "left"),
            @NodeChild(type = RubyNode.class, value = "right"),
            @NodeChild(type = RubyNode.class, value = "encoding")
    })
    public abstract static class MakeConcatNode extends RubyNode {

        public static MakeConcatNode create() {
            return RopeNodesFactory.MakeConcatNodeGen.create(null, null, null);
        }

        @Child private FlattenNode flattenNode;

        public abstract Rope executeMake(Rope left, Rope right, Encoding encoding);

        @Specialization
        public Rope concatMutableRope(RopeBuffer left, Rope right, Encoding encoding,
                                      @Cached("createBinaryProfile()") ConditionProfile differentEncodingProfile) {
            try {
                Math.addExact(left.byteLength(), right.byteLength());
            } catch (ArithmeticException e) {
                throw new RaiseException(getContext().getCoreExceptions().argumentError("Result of string concatenation exceeds the system maximum string length", this));
            }

            final RopeBuilder byteList = left.getByteList();

            byteList.append(right.getBytes());

            if (differentEncodingProfile.profile(byteList.getEncoding() != encoding)) {
                byteList.setEncoding(encoding);
            }

            return left;
        }

        @TruffleBoundary
        @Specialization
        public Rope concatNativeRopeLeft(NativeRope left, Rope right, Encoding encoding) {
            return executeMake(left.toLeafRope(), right, encoding);
        }

        @TruffleBoundary
        @Specialization
        public Rope concatNativeRopeRight(Rope left, NativeRope right, Encoding encoding) {
            return executeMake(left, right.toLeafRope(), encoding);
        }

        @Specialization(guards = { "!isMutableRope(left)", "!isNativeRope(left)", "!isNativeRope(right)", "!isCodeRangeBroken(left, right)" })
        public Rope concat(Rope left, Rope right, Encoding encoding,
                           @Cached("createBinaryProfile()") ConditionProfile sameCodeRangeProfile,
                           @Cached("createBinaryProfile()") ConditionProfile brokenCodeRangeProfile,
                           @Cached("createBinaryProfile()") ConditionProfile isLeftSingleByteOptimizableProfile,
                           @Cached("createBinaryProfile()") ConditionProfile shouldRebalanceProfile) {
            try {
                Math.addExact(left.byteLength(), right.byteLength());
            } catch (ArithmeticException e) {
                throw new RaiseException(getContext().getCoreExceptions().argumentError("Result of string concatenation exceeds the system maximum string length", this));
            }

            if (shouldRebalanceProfile.profile(left.depth() >= getContext().getOptions().ROPE_DEPTH_THRESHOLD)) {
                if (flattenNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    flattenNode = insert(FlattenNode.create());
                }

                if (left instanceof ConcatRope) {
                    left = rebalance((ConcatRope) left, getContext().getOptions().ROPE_DEPTH_THRESHOLD, flattenNode);
                }
            }

            if (shouldRebalanceProfile.profile(right.depth() >= getContext().getOptions().ROPE_DEPTH_THRESHOLD)) {
                if (flattenNode == null) {
                    CompilerDirectives.transferToInterpreterAndInvalidate();
                    flattenNode = insert(FlattenNode.create());
                }

                if (right instanceof ConcatRope) {
                    right = rebalance((ConcatRope) right, getContext().getOptions().ROPE_DEPTH_THRESHOLD, flattenNode);
                }
            }

            int depth = depth(left, right);
            /*if (depth >= 10) {
                System.out.println("ConcatRope depth: " + depth);
            }*/

            return new ConcatRope(left, right, encoding,
                    commonCodeRange(left.getCodeRange(), right.getCodeRange(), sameCodeRangeProfile, brokenCodeRangeProfile),
                    isSingleByteOptimizable(left, right, isLeftSingleByteOptimizableProfile),
                    depth, isBalanced(left, right));
        }

        private boolean isBalanced(Rope left, Rope right) {
            // Our definition of balanced is centered around the notion of rebalancing. We could have a simple structure
            // such as ConcatRope(ConcatRope(LeafRope, LeafRope), LeafRope) that is balanced on its own but may contribute
            // to an unbalanced rope when combined with another rope of similar structure. To keep things simple, we only
            // consider ConcatRopes that consist of two non-ConcatRopes balanced as the base case and ConcatRopes that
            // have balanced ConcatRopes for both children are balanced by induction.
            if (left instanceof ConcatRope) {
                if (right instanceof ConcatRope) {
                    return ((ConcatRope) left).isBalanced() && ((ConcatRope) right).isBalanced();
                }

                return false;
            } else {
                // We treat the concatenation of two non-ConcatRopes as balanced, even if their children not balanced.
                // E.g., a SubstringRope whose child is an unbalanced ConcatRope arguable isn't balanced. However,
                // the code is much simpler by handling it this way. Balanced ConcatRopes will never rebalance, but
                // if they become part of a larger subtree that exceeds the depth threshold, they may be flattened.
                return !(right instanceof ConcatRope);
            }
        }

        @TruffleBoundary
        private Rope rebalance(ConcatRope rope, int depthThreshold, FlattenNode flattenNode) {
            Deque<Rope> currentRopeQueue = new ArrayDeque<>();
            Deque<Rope> nextLevelQueue = new ArrayDeque<>();

            linearizeTree(rope.getLeft(), currentRopeQueue);
            linearizeTree(rope.getRight(), currentRopeQueue);

            final int flattenThreshold = depthThreshold / 2;

            Rope root = null;
            while (! currentRopeQueue.isEmpty()) {
                Rope left = currentRopeQueue.pop();

                if (left.depth() >= flattenThreshold) {
                    left = flattenNode.executeFlatten(left);
                }

                if (currentRopeQueue.isEmpty()) {
                    if (nextLevelQueue.isEmpty()) {
                        root = left;
                    } else {
                        // If a rope can't be paired with another rope at the current level (i.e., odd numbers of ropes),
                        // it needs to be promoted to the next level where it be tried again. Since by definition every
                        // rope already present in the next level must have occurred before this rope in the current
                        // level, this rope must be added to the end of the list in the next level to maintain proper
                        // position.
                        nextLevelQueue.add(left);
                    }
                } else {
                    Rope right = currentRopeQueue.pop();

                    if (right.depth() >= flattenThreshold) {
                        right = flattenNode.executeFlatten(right);
                    }

                    final Rope child = new ConcatRope(left, right, rope.getEncoding(),
                                                      commonCodeRange(left.getCodeRange(), right.getCodeRange()),
                                    left.isSingleByteOptimizable() && right.isSingleByteOptimizable(),
                                                      depth(left, right), isBalanced(left, right));

                    nextLevelQueue.add(child);
                }

                if (currentRopeQueue.isEmpty() && !nextLevelQueue.isEmpty()) {
                    currentRopeQueue = nextLevelQueue;
                    nextLevelQueue = new ArrayDeque<>();
                }
            }

            return root;
        }

        @TruffleBoundary
        private void linearizeTree(Rope rope, Deque<Rope> ropeQueue) {
            if (rope instanceof ConcatRope) {
                final ConcatRope concatRope = (ConcatRope) rope;

                // If a rope is known to be balanced, there's no need to rebalance it.
                if (concatRope.isBalanced()) {
                    ropeQueue.add(concatRope);
                } else {
                    linearizeTree(concatRope.getLeft(), ropeQueue);
                    linearizeTree(concatRope.getRight(), ropeQueue);
                }
            } else {
                // We never rebalance non-ConcatRopes since that requires per-rope type logic with likely minimal benefit.
                ropeQueue.add(rope);
            }
        }

        @Specialization(guards = { "!isMutableRope(left)", "!isNativeRope(left)", "!isNativeRope(right)", "isCodeRangeBroken(left, right)" })
        public Rope concatCrBroken(Rope left, Rope right, Encoding encoding,
                                   @Cached("create()") MakeLeafRopeNode makeLeafRopeNode) {
            // This specialization was added to a special case where broken code range(s),
            // may concat to form a valid code range.
            try {
                Math.addExact(left.byteLength(), right.byteLength());
            } catch (ArithmeticException e) {
                throw new RaiseException(getContext().getCoreExceptions().argumentError("Result of string concatenation exceeds the system maximum string length", this));
            }

            final byte[] leftBytes = left.getBytes();
            final byte[] rightBytes = right.getBytes();
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

        private boolean isSingleByteOptimizable(Rope left, Rope right, ConditionProfile isLeftSingleByteOptimizableProfile) {
            if (isLeftSingleByteOptimizableProfile.profile(left.isSingleByteOptimizable())) {
                return right.isSingleByteOptimizable();
            }

            return false;
        }

        private int depth(Rope left, Rope right) {
            return Math.max(left.depth(), right.depth()) + 1;
        }

        protected static boolean isMutableRope(Rope rope) {
            return rope instanceof RopeBuffer;
        }

        protected static boolean isNativeRope(Rope rope) {
            return rope instanceof NativeRope;
        }

        protected static boolean isCodeRangeBroken(Rope first, Rope second) {
            return first.getCodeRange() == CR_BROKEN || second.getCodeRange() == CR_BROKEN;
        }
    }


    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "bytes"),
            @NodeChild(type = RubyNode.class, value = "encoding"),
            @NodeChild(type = RubyNode.class, value = "codeRange"),
            @NodeChild(type = RubyNode.class, value = "characterLength")
    })
    public abstract static class MakeLeafRopeNode extends RubyNode {

        public static MakeLeafRopeNode create() {
            return RopeNodesFactory.MakeLeafRopeNodeGen.create(null, null, null, null);
        }

        public abstract LeafRope executeMake(byte[] bytes, Encoding encoding, CodeRange codeRange, Object characterLength);

        @Specialization(guards = "is7Bit(codeRange)")
        public LeafRope makeAsciiOnlyLeafRope(byte[] bytes, Encoding encoding, CodeRange codeRange, Object characterLength) {
            return new AsciiOnlyLeafRope(bytes, encoding);
        }

        @Specialization(guards = "isValid(codeRange)")
        public LeafRope makeValidLeafRopeWithCharacterLength(byte[] bytes, Encoding encoding, CodeRange codeRange, int characterLength) {
            return new ValidLeafRope(bytes, encoding, characterLength);
        }

        @Specialization(guards = { "isValid(codeRange)", "isFixedWidth(encoding)" })
        public LeafRope makeValidLeafRopeFixedWidthEncoding(byte[] bytes, Encoding encoding, CodeRange codeRange, NotProvided characterLength) {
            final int calculatedCharacterLength = bytes.length / encoding.minLength();

            return new ValidLeafRope(bytes, encoding, calculatedCharacterLength);
        }

        @Specialization(guards = { "isValid(codeRange)", "!isFixedWidth(encoding)", "isAsciiCompatible(encoding)" })
        public LeafRope makeValidLeafRopeAsciiCompat(byte[] bytes, Encoding encoding, CodeRange codeRange, NotProvided characterLength,
                                                     @Cached("create()") BranchProfile errorProfile) {
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
                int delta = StringSupport.encFastMBCLen(bytes, p, e, encoding);

                if (delta < 0) {
                    errorProfile.enter();
                    throw new UnsupportedOperationException("Code range is reported as valid, but is invalid for the given encoding: " + encodingName(encoding));
                }

                p += delta;
                calculatedCharacterLength++;
            }

            return new ValidLeafRope(bytes, encoding, calculatedCharacterLength);
        }

        @Specialization(guards = { "isValid(codeRange)", "!isFixedWidth(encoding)", "!isAsciiCompatible(encoding)" })
        public LeafRope makeValidLeafRope(byte[] bytes, Encoding encoding, CodeRange codeRange, NotProvided characterLength) {
            // Extracted from StringSupport.strLength.

            int calculatedCharacterLength;
            int p = 0;
            int e = bytes.length;

            for (calculatedCharacterLength = 0; p < e; calculatedCharacterLength++) {
                p += StringSupport.length(encoding, bytes, p, e);
            }

            return new ValidLeafRope(bytes, encoding, calculatedCharacterLength);
        }

        @Specialization(guards = "isBroken(codeRange)")
        public LeafRope makeInvalidLeafRope(byte[] bytes, Encoding encoding, CodeRange codeRange, Object characterLength) {
            return new InvalidLeafRope(bytes, encoding, RopeOperations.strLength(encoding, bytes, 0, bytes.length));
        }

        @Specialization(guards = { "isUnknown(codeRange)", "isEmpty(bytes)" })
        public LeafRope makeUnknownLeafRopeEmpty(byte[] bytes, Encoding encoding, CodeRange codeRange, Object characterLength,
                                                 @Cached("createBinaryProfile()") ConditionProfile isUTF8,
                                                 @Cached("createBinaryProfile()") ConditionProfile isUSAscii,
                                                 @Cached("createBinaryProfile()") ConditionProfile isAscii8Bit,
                                                 @Cached("createBinaryProfile()") ConditionProfile isAsciiCompatible) {
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
                return new AsciiOnlyLeafRope(bytes, encoding);
            }

            return new ValidLeafRope(bytes, encoding, 0);
        }

        @Specialization(guards = { "isUnknown(codeRange)", "!isEmpty(bytes)", "isBinaryString(encoding)" })
        public LeafRope makeUnknownLeafRopeBinary(byte[] bytes, Encoding encoding, CodeRange codeRange, Object characterLength,
                                            @Cached("createBinaryProfile()") ConditionProfile discovered7BitProfile) {
            CodeRange newCodeRange = CR_7BIT;
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] < 0) {
                    newCodeRange = CR_VALID;
                    break;
                }
            }

            if (discovered7BitProfile.profile(newCodeRange == CR_7BIT)) {
                return new AsciiOnlyLeafRope(bytes, encoding);
            }

            return new ValidLeafRope(bytes, encoding, bytes.length);
        }

        @Specialization(rewriteOn = NonAsciiCharException.class,
                guards = { "isUnknown(codeRange)", "!isEmpty(bytes)", "!isBinaryString(encoding)", "isAsciiCompatible(encoding)" })
        public LeafRope makeUnknownLeafRopeAsciiCompatible(byte[] bytes, Encoding encoding, CodeRange codeRange, Object characterLength) throws NonAsciiCharException {
            // Optimistically assume this string consists only of ASCII characters. If a non-ASCII character is found,
            // fail over to a more generalized search.
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] < 0) {
                    throw new NonAsciiCharException();
                }
            }

            return new AsciiOnlyLeafRope(bytes, encoding);
        }

        @Specialization(replaces = "makeUnknownLeafRopeAsciiCompatible",
                guards = { "isUnknown(codeRange)", "!isEmpty(bytes)", "!isBinaryString(encoding)", "isAsciiCompatible(encoding)" })
        public LeafRope makeUnknownLeafRopeAsciiCompatibleGeneric(byte[] bytes, Encoding encoding, CodeRange codeRange, Object characterLength,
                                            @Cached("createBinaryProfile()") ConditionProfile discovered7BitProfile,
                                            @Cached("createBinaryProfile()") ConditionProfile discoveredValidProfile) {
            final long packedLengthAndCodeRange = StringSupport.strLengthWithCodeRangeAsciiCompatible(encoding, bytes, 0, bytes.length);
            final CodeRange newCodeRange = CodeRange.fromInt(StringSupport.unpackArg(packedLengthAndCodeRange));
            final int calculatedCharacterLength = StringSupport.unpackResult(packedLengthAndCodeRange);

            if (discovered7BitProfile.profile(newCodeRange == CR_7BIT)) {
                return new AsciiOnlyLeafRope(bytes, encoding);
            }

            if (discoveredValidProfile.profile(newCodeRange == CR_VALID)) {
                return new ValidLeafRope(bytes, encoding, calculatedCharacterLength);
            }

            return new InvalidLeafRope(bytes, encoding, calculatedCharacterLength);
        }

        @Specialization(guards = { "isUnknown(codeRange)", "!isEmpty(bytes)", "!isBinaryString(encoding)", "!isAsciiCompatible(encoding)" })
        public LeafRope makeUnknownLeafRope(byte[] bytes, Encoding encoding, CodeRange codeRange, Object characterLength,
                                            @Cached("createBinaryProfile()") ConditionProfile discovered7BitProfile,
                                            @Cached("createBinaryProfile()") ConditionProfile discoveredValidProfile) {
            final long packedLengthAndCodeRange = StringSupport.strLengthWithCodeRangeNonAsciiCompatible(encoding, bytes, 0, bytes.length);
            final CodeRange newCodeRange = CodeRange.fromInt(StringSupport.unpackArg(packedLengthAndCodeRange));
            final int calculatedCharacterLength = StringSupport.unpackResult(packedLengthAndCodeRange);

            if (discovered7BitProfile.profile(newCodeRange == CR_7BIT)) {
                return new AsciiOnlyLeafRope(bytes, encoding);
            }

            if (discoveredValidProfile.profile(newCodeRange == CR_VALID)) {
                return new ValidLeafRope(bytes, encoding, calculatedCharacterLength);
            }

            return new InvalidLeafRope(bytes, encoding, calculatedCharacterLength);
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

        protected static boolean isBinaryString(Encoding encoding) {
            return encoding == ASCIIEncoding.INSTANCE;
        }

        protected static boolean isEmpty(byte[] bytes) {
            return bytes.length == 0;
        }

        protected static boolean isAsciiCompatible(Encoding encoding) {
            return encoding.isAsciiCompatible();
        }

        protected static boolean isFixedWidth(Encoding encoding) {
            return encoding.isFixedWidth();
        }

        protected static final class NonAsciiCharException extends SlowPathException {
            private static final long serialVersionUID = 5550642254188358382L;
        }

        @TruffleBoundary
        private String encodingName(Encoding encoding) {
            return encoding.toString();
        }

    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "base"),
            @NodeChild(type = RubyNode.class, value = "times")
    })
    @ImportStatic(RopeGuards.class)
    public abstract static class MakeRepeatingNode extends RubyNode {

        public static MakeRepeatingNode create() {
            return RopeNodesFactory.MakeRepeatingNodeGen.create(null, null);
        }

        public abstract Rope executeMake(Rope base, int times);

        @Specialization(guards = "times == 0")
        public Rope repeatZero(Rope base, int times,
                               @Cached("create()") WithEncodingNode withEncodingNode) {
            return withEncodingNode.executeWithEncoding(RopeConstants.EMPTY_UTF8_ROPE, base.getEncoding(), CodeRange.CR_7BIT);
        }

        @Specialization(guards = "times == 1")
        public Rope repeatOne(Rope base, int times,
                              @Cached("create()") WithEncodingNode withEncodingNode) {
            return base;
        }

        @Specialization(guards = "times > 1")
        public Rope multiplyBuffer(RopeBuffer base, int times) {
            final RopeBuilder inputBytes = base.getByteList();
            int len = inputBytes.getLength() * times;
            final RopeBuilder outputBytes = RopeBuilder.createRopeBuilder(len);
            outputBytes.setLength(len);

            int n = inputBytes.getLength();

            System.arraycopy(inputBytes.getUnsafeBytes(), 0, outputBytes.getUnsafeBytes(), 0, n);
            while (n <= len / 2) {
                System.arraycopy(outputBytes.getUnsafeBytes(), 0, outputBytes.getUnsafeBytes(), n, n);
                n *= 2;
            }
            System.arraycopy(outputBytes.getUnsafeBytes(), 0, outputBytes.getUnsafeBytes(), n, len - n);


            outputBytes.setEncoding(inputBytes.getEncoding());

            return new RopeBuffer(outputBytes, base.getCodeRange(), base.isSingleByteOptimizable(), base.characterLength() * times);
        }

        @Specialization(guards = { "!isRopeBuffer(base)", "isSingleByteString(base)", "times > 1" })
        @TruffleBoundary
        public Rope multiplySingleByteString(Rope base, int times,
                                             @Cached("create()") MakeLeafRopeNode makeLeafRopeNode) {
            final byte filler = base.getBytes()[0];

            byte[] buffer = new byte[times];
            Arrays.fill(buffer, filler);

            return makeLeafRopeNode.executeMake(buffer, base.getEncoding(), base.getCodeRange(), times);
        }

        @Specialization(guards = { "!isRopeBuffer(base)", "!isSingleByteString(base)", "times > 1" })
        public Rope repeat(Rope base, int times) {
            try {
                Math.multiplyExact(base.byteLength(), times);
            } catch (ArithmeticException e) {
                throw new RaiseException(getContext().getCoreExceptions().argumentError("Result of repeating string exceeds the system maximum string length", this));
            }

            return new RepeatingRope(base, times);
        }

    }


    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "rope"),
            @NodeChild(type = RubyNode.class, value = "currentLevel"),
            @NodeChild(type = RubyNode.class, value = "printString")
    })
    public abstract static class DebugPrintRopeNode extends RubyNode {

        public abstract DynamicObject executeDebugPrint(Rope rope, int currentLevel, boolean printString);

        @TruffleBoundary
        @Specialization
        public DynamicObject debugPrintLeafRope(LeafRope rope, int currentLevel, boolean printString) {
            printPreamble(currentLevel);

            // Converting a rope to a java.lang.String may populate the byte[], so we need to query for the array status beforehand.
            final boolean bytesAreNull = rope.getRawBytes() == null;

            System.err.println(StringUtils.format("%s (%s; BN: %b; BL: %d; CL: %d; CR: %s; D: %d; E: %s)",
                    printString ? rope.toString() : "<skipped>",
                    rope.getClass().getSimpleName(),
                    bytesAreNull,
                    rope.byteLength(),
                    rope.characterLength(),
                    rope.getCodeRange(),
                    rope.depth(),
                    rope.getEncoding()));

            return nil();
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject debugPrintSubstringRope(SubstringRope rope, int currentLevel, boolean printString) {
            printPreamble(currentLevel);

            // Converting a rope to a java.lang.String may populate the byte[], so we need to query for the array status beforehand.
            final boolean bytesAreNull = rope.getRawBytes() == null;

            System.err.println(StringUtils.format("%s (%s; BN: %b; BL: %d; CL: %d; CR: %s; O: %d; D: %d; E: %s)",
                    printString ? rope.toString() : "<skipped>",
                    rope.getClass().getSimpleName(),
                    bytesAreNull,
                    rope.byteLength(),
                    rope.characterLength(),
                    rope.getCodeRange(),
                    rope.getByteOffset(),
                    rope.depth(),
                    rope.getEncoding()));

            executeDebugPrint(rope.getChild(), currentLevel + 1, printString);

            return nil();
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject debugPrintConcatRope(ConcatRope rope, int currentLevel, boolean printString) {
            printPreamble(currentLevel);

            // Converting a rope to a java.lang.String may populate the byte[], so we need to query for the array status beforehand.
            final boolean bytesAreNull = rope.getRawBytes() == null;

            System.err.println(StringUtils.format("%s (%s; BN: %b; BL: %d; CL: %d; CR: %s; D: %d; LD: %d; RD: %d; E: %s)",
                    printString ? rope.toString() : "<skipped>",
                    rope.getClass().getSimpleName(),
                    bytesAreNull,
                    rope.byteLength(),
                    rope.characterLength(),
                    rope.getCodeRange(),
                    rope.depth(),
                    rope.getLeft().depth(),
                    rope.getRight().depth(),
                    rope.getEncoding()));

            executeDebugPrint(rope.getLeft(), currentLevel + 1, printString);
            executeDebugPrint(rope.getRight(), currentLevel + 1, printString);

            return nil();
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject debugPrintRepeatingRope(RepeatingRope rope, int currentLevel, boolean printString) {
            printPreamble(currentLevel);

            // Converting a rope to a java.lang.String may populate the byte[], so we need to query for the array status beforehand.
            final boolean bytesAreNull = rope.getRawBytes() == null;

            System.err.println(StringUtils.format("%s (%s; BN: %b; BL: %d; CL: %d; CR: %s; T: %d; D: %d; E: %s)",
                    printString ? rope.toString() : "<skipped>",
                    rope.getClass().getSimpleName(),
                    bytesAreNull,
                    rope.byteLength(),
                    rope.characterLength(),
                    rope.getCodeRange(),
                    rope.getTimes(),
                    rope.depth(),
                    rope.getEncoding()));

            executeDebugPrint(rope.getChild(), currentLevel + 1, printString);

            return nil();
        }

        @TruffleBoundary
        @Specialization
        public DynamicObject debugPrintLazyInt(LazyIntRope rope, int currentLevel, boolean printString) {
            printPreamble(currentLevel);

            // Converting a rope to a java.lang.String may populate the byte[], so we need to query for the array status beforehand.
            final boolean bytesAreNull = rope.getRawBytes() == null;

            System.err.println(StringUtils.format("%s (%s; BN: %b; BL: %d; CL: %d; CR: %s; V: %d, D: %d; E: %s)",
                    printString ? rope.toString() : "<skipped>",
                    rope.getClass().getSimpleName(),
                    bytesAreNull,
                    rope.byteLength(),
                    rope.characterLength(),
                    rope.getCodeRange(),
                    rope.getValue(),
                    rope.depth(),
                    rope.getEncoding()));

            return nil();
        }

        private void printPreamble(int level) {
            if (level > 0) {
                for (int i = 0; i < level; i++) {
                    System.err.print("|  ");
                }
            }
        }

    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "rope"),
            @NodeChild(type = RubyNode.class, value = "encoding"),
            @NodeChild(type = RubyNode.class, value = "codeRange")
    })
    public abstract static class WithEncodingNode extends RubyNode {

        public static WithEncodingNode create() {
            return RopeNodesFactory.WithEncodingNodeGen.create(null, null, null);
        }

        public abstract Rope executeWithEncoding(Rope rope, Encoding encoding, CodeRange codeRange);

        @Specialization(guards = "rope.getEncoding() == encoding")
        public Rope withEncodingSameEncoding(Rope rope, Encoding encoding, CodeRange codeRange) {
            return rope;
        }

        @Specialization(guards = {
                "rope.getEncoding() != encoding",
                "rope.getCodeRange() == codeRange"
        })
        public Rope withEncodingSameCodeRange(Rope rope, Encoding encoding, CodeRange codeRange) {
            return rope.withEncoding(encoding, codeRange);
        }

        @Specialization(guards = {
                "rope.getEncoding() != encoding",
                "rope.getCodeRange() != codeRange",
                "isAsciiCompatibleChange(rope, encoding)",
                "rope.getClass() == cachedRopeClass"
        }, limit = "getCacheLimit()")
        public Rope withEncodingCr7Bit(Rope rope, Encoding encoding, CodeRange codeRange,
                                       @Cached("rope.getClass()") Class<? extends Rope> cachedRopeClass) {
            return cachedRopeClass.cast(rope).withEncoding(encoding, CodeRange.CR_7BIT);
        }

        @Specialization(guards = {
                "rope.getEncoding() != encoding",
                "rope.getCodeRange() != codeRange",
                "!isAsciiCompatibleChange(rope, encoding)"
        })
        public Rope withEncoding(Rope rope, Encoding encoding, CodeRange codeRange,
                @Cached("create()") MakeLeafRopeNode makeLeafRopeNode,
                @Cached("create()") RopeNodes.BytesNode bytesNode) {
            return makeLeafRopeNode.executeMake(bytesNode.execute(rope), encoding, codeRange, NotProvided.INSTANCE);
        }

        protected static boolean isAsciiCompatibleChange(Rope rope, Encoding encoding) {
            return rope.getCodeRange() == CR_7BIT && encoding.isAsciiCompatible();
        }

        protected int getCacheLimit() {
            return getContext().getOptions().ROPE_CLASS_CACHE;
        }

    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "rope"),
            @NodeChild(type = RubyNode.class, value = "index")
    })
    public abstract static class GetByteNode extends RubyNode {

        public static GetByteNode create() {
            return RopeNodesFactory.GetByteNodeGen.create(null, null);
        }

        public abstract int executeGetByte(Rope rope, int index);

        @Specialization(guards = "rope.getRawBytes() != null")
        public int getByte(Rope rope, int index) {
            return rope.getRawBytes()[index] & 0xff;
        }

        @Specialization(guards = "rope.getRawBytes() == null")
        public int getByte(NativeRope rope, int index) {
            return rope.getByteSlow(index) & 0xff;
        }

        @Specialization(guards = "rope.getRawBytes() == null")
        public int getByte(LazyRope rope, int index) {
            return rope.getBytes()[index] & 0xff;
        }

        @Specialization(guards = "rope.getRawBytes() == null")
        public int getByteSubstringRope(SubstringRope rope, int index,
                @Cached("createBinaryProfile()") ConditionProfile childRawBytesNullProfile,
                @Cached("create()") ByteSlowNode slowByte) {
            if (childRawBytesNullProfile.profile(rope.getChild().getRawBytes() == null)) {
                return slowByte.execute(rope, index) & 0xff;
            }

            return rope.getChild().getRawBytes()[index + rope.getByteOffset()] & 0xff;
        }

        @Specialization(guards = "rope.getRawBytes() == null")
        public int getByteRepeatingRope(RepeatingRope rope, int index,
                                        @Cached("createBinaryProfile()") ConditionProfile childRawBytesNullProfile) {
            if (childRawBytesNullProfile.profile(rope.getChild().getRawBytes() == null)) {
                return rope.getByteSlow(index) & 0xff;
            }

            return rope.getChild().getRawBytes()[index % rope.getChild().byteLength()] & 0xff;
        }

        @Specialization(guards = "rope.getRawBytes() == null")
        public int getByteConcatRope(ConcatRope rope, int index,
                @Cached("createBinaryProfile()") ConditionProfile chooseLeftChildProfile,
                @Cached("createBinaryProfile()") ConditionProfile leftChildRawBytesNullProfile,
                @Cached("createBinaryProfile()") ConditionProfile rightChildRawBytesNullProfile,
                @Cached("create()") ByteSlowNode byteSlowLeft,
                @Cached("create()") ByteSlowNode byteSlowRight) {
            if (chooseLeftChildProfile.profile(index < rope.getLeft().byteLength())) {
                if (leftChildRawBytesNullProfile.profile(rope.getLeft().getRawBytes() == null)) {
                    return byteSlowLeft.execute(rope.getLeft(), index) & 0xff;
                }

                return rope.getLeft().getRawBytes()[index] & 0xff;
            }

            if (rightChildRawBytesNullProfile.profile(rope.getRight().getRawBytes() == null)) {
                return byteSlowRight.execute(rope.getRight(), index - rope.getLeft().byteLength()) & 0xff;
            }

            return rope.getRight().getRawBytes()[index - rope.getLeft().byteLength()] & 0xff;
        }

    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "rope"),
            @NodeChild(type = RubyNode.class, value = "index")
    })
    public abstract static class GetCodePointNode extends RubyNode {

        public static GetCodePointNode create() {
            return RopeNodesFactory.GetCodePointNodeGen.create(null, null);
        }

        public abstract int executeGetCodePoint(Rope rope, int index);

        @Specialization(guards = "rope.isSingleByteOptimizable()")
        public int getCodePointSingleByte(Rope rope, int index,
                                          @Cached("create()") GetByteNode getByteNode) {
            return getByteNode.executeGetByte(rope, index);
        }

        @Specialization(guards = { "!rope.isSingleByteOptimizable()", "rope.getEncoding().isUTF8()" })
        public int getCodePointUTF8(Rope rope, int index,
                                    @Cached("create()") GetByteNode getByteNode,
                                    @Cached("createBinaryProfile()") ConditionProfile singleByteCharProfile,
                @Cached("create()") BranchProfile errorProfile,
                @Cached("create()") BytesNode bytesNode) {
            final int firstByte = getByteNode.executeGetByte(rope, index);
            if (singleByteCharProfile.profile(firstByte < 128)) {
                return firstByte;
            }

            return getCodePointMultiByte(rope, index, errorProfile, bytesNode);
        }

        @Specialization(guards = { "!rope.isSingleByteOptimizable()", "!rope.getEncoding().isUTF8()" })
        public int getCodePointMultiByte(Rope rope, int index,
                @Cached("create()") BranchProfile errorProfile,
                @Cached("create()") BytesNode bytesNode) {
            final byte[] bytes = bytesNode.execute(rope);
            final Encoding encoding = rope.getEncoding();

            final int characterLength = preciseCharacterLength(encoding, bytes, index, rope.byteLength());
            if (characterLength <= 0) {
                errorProfile.enter();
                throw new RaiseException(getContext().getCoreExceptions().argumentError("invalid byte sequence in " + encoding, null));
            }

            return mbcToCode(encoding, bytes, index, rope.byteLength());
        }

        @TruffleBoundary
        private int preciseCharacterLength(Encoding encoding, byte[] bytes, int start, int end) {
            return StringSupport.preciseLength(encoding, bytes, start, end);
        }

        @TruffleBoundary
        private int mbcToCode(Encoding encoding, byte[] bytes, int start, int end) {
            return encoding.mbcToCode(bytes, start, end);
        }

    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "rope")
    })
    @ImportStatic(RopeGuards.class)
    public abstract static class FlattenNode extends RubyNode {

        @Child private MakeLeafRopeNode makeLeafRopeNode = MakeLeafRopeNode.create();

        public static FlattenNode create() {
            return RopeNodesFactory.FlattenNodeGen.create(null);
        }

        public abstract LeafRope executeFlatten(Rope rope);

        @Specialization
        public LeafRope flattenLeafRope(LeafRope rope) {
            return rope;
        }

        @Specialization(guards = { "!isLeafRope(rope)", "rope.getRawBytes() != null" })
        public LeafRope flattenNonLeafWithBytes(Rope rope) {
            return makeLeafRopeNode.executeMake(rope.getRawBytes(), rope.getEncoding(), rope.getCodeRange(), rope.characterLength());
        }

        @Specialization(guards = { "!isLeafRope(rope)", "rope.getRawBytes() == null" })
        public LeafRope flatten(Rope rope) {
            // NB: We call RopeOperations.flatten here rather than Rope#getBytes so we don't populate the byte[] in
            // the source `rope`. Otherwise, we'll end up a fully populated reference in both the source `rope` and the
            // flattened one, which could adversely affect GC.
            final byte[] bytes = RopeOperations.flattenBytes(rope);

            return makeLeafRopeNode.executeMake(bytes, rope.getEncoding(), rope.getCodeRange(), rope.characterLength());
        }

    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "a"),
            @NodeChild(type = RubyNode.class, value = "b")
    })
    public abstract static class EqualNode extends RubyNode {

        public static EqualNode create() {
            return RopeNodesFactory.EqualNodeGen.create(null, null);
        }

        public abstract boolean execute(Rope a, Rope b);

        @Specialization(guards = "a == b")
        public boolean sameRopeEqual(Rope a, Rope b) {
            return true;
        }

        @Specialization
        public boolean ropesEqual(Rope a, Rope b,
                @Cached("create()") BranchProfile differentEncodingProfile,
                @Cached("create()") BytesEqualNode bytesEqualNode) {
            if (a.getEncoding() != b.getEncoding()) {
                differentEncodingProfile.enter();
                return false;
            }

            return bytesEqualNode.execute(a, b);
        }

    }

    // This node type checks for the equality of the bytes owned by a rope but does not pay
    // attention to the encoding. It matches the behaviour of the equals method on the rope class
    // itself.
    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "a"),
            @NodeChild(type = RubyNode.class, value = "b")
    })
    public abstract static class BytesEqualNode extends RubyNode {

        public static BytesEqualNode create() {
            return RopeNodesFactory.BytesEqualNodeGen.create(null, null);
        }

        public abstract boolean execute(Rope a, Rope b);

        @Specialization(guards = "a == b")
        public boolean sameRopes(Rope a, Rope b) {
            return true;
        }

        @Specialization(guards = {
                "a != b",
                "a.getRawBytes() != null",
                "a.getRawBytes() == b.getRawBytes()" })
        public boolean sameByteArrays(Rope a, Rope b) {
            return true;
        }

        @Specialization(guards = {
                "a != b",
                "a.getRawBytes() != null",
                "b.getRawBytes() != null",
                "a.byteLength() == 1",
                "b.byteLength() == 1" })
        public boolean characterEqual(Rope a, Rope b) {
            return a.getRawBytes()[0] == b.getRawBytes()[0];
        }

        @Specialization(guards = "a != b", replaces = { "sameByteArrays", "characterEqual" })
        public boolean fullRopeEqual(Rope a, Rope b,
                @Cached("createBinaryProfile()") ConditionProfile aRawBytesProfile,
                @Cached("create()") BranchProfile sameByteArraysProfile,
                @Cached("create()") BranchProfile differentLengthProfile,
                @Cached("createBinaryProfile()") ConditionProfile aCalculatedHashProfile,
                @Cached("createBinaryProfile()") ConditionProfile bCalculatedHashProfile,
                @Cached("create()") BranchProfile calculatedHashCodesProfile,
                @Cached("create()") BranchProfile differentHashCodeProfile,
                @Cached("create()") BranchProfile compareBytesProfile,
                @Cached("create()") BytesNode aBytesNode,
                @Cached("create()") BytesNode bBytesNode,
                @Cached("create()") BranchProfile notEqualProfile,
                @Cached("create()") BranchProfile equalProfile) {
            if (aRawBytesProfile.profile(a.getRawBytes() != null) && a.getRawBytes() == b.getRawBytes()) {
                sameByteArraysProfile.enter();
                return true;
            }

            if (a.byteLength() != b.byteLength()) {
                differentLengthProfile.enter();
                return false;
            }

            if (aCalculatedHashProfile.profile(a.isHashCodeCalculated()) &&
                    bCalculatedHashProfile.profile(b.isHashCodeCalculated())) {
                if (a.calculatedHashCode() != b.calculatedHashCode()) {
                    differentHashCodeProfile.enter();
                    return false;
                }
            }

            compareBytesProfile.enter();

            final byte[] aBytes = aBytesNode.execute(a);
            final byte[] bBytes = bBytesNode.execute(b);
            assert aBytes.length == bBytes.length;

            for (int i = 0; i < aBytes.length; i++) {
                if (aBytes[i] != bBytes[i]) {
                    notEqualProfile.enter();
                    return false;
                }
            }

            equalProfile.enter();
            return true;
        }

    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "rope")
    })
    public abstract static class BytesNode extends RubyNode {

        public static BytesNode create() {
            return RopeNodesFactory.BytesNodeGen.create(null);
        }

        public abstract byte[] execute(Rope rope);

        @Specialization(guards = "rope.getRawBytes() != null")
        public byte[] getBytesManaged(ManagedRope rope) {
            return rope.getRawBytes();
        }

        @Specialization
        public byte[] getBytesNative(NativeRope rope) {
            return rope.getBytes();
        }

        @Specialization(guards = "rope.getRawBytes() == null")
        @TruffleBoundary
        public byte[] getBytesFromRope(ManagedRope rope) {
            return rope.getBytes();
        }
    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "rope")
    })
    public abstract static class BytesSlowNode extends RubyNode {

        public static BytesSlowNode create() {
            return RopeNodesFactory.BytesSlowNodeGen.create(null);
        }

        public abstract byte[] execute(Rope rope);

        @Specialization(guards = "rope.getClass() == cachedRopeClass", limit = "getCacheLimit()")
        public byte[] getManagedBytesSlow(Rope rope,
                @Cached("rope.getClass()") Class<? extends Rope> cachedRopeClass) {
            return cachedRopeClass.cast(rope).getBytesSlow();
        }

        @TruffleBoundary
        @Specialization
        public byte[] getBytesFromRope(Rope rope) {
            return rope.getBytesSlow();
        }

        protected int getCacheLimit() {
            return getContext().getOptions().ROPE_CLASS_CACHE;
        }
    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "rope"),
            @NodeChild(type = RubyNode.class, value = "index")
    })
    public abstract static class ByteSlowNode extends RubyNode {

        public static ByteSlowNode create() {
            return RopeNodesFactory.ByteSlowNodeGen.create(null, null);
        }

        public abstract byte execute(Rope rope, int index);

        @Specialization
        public byte getByteFromSubString(SubstringRope rope, int index,
                @Cached("create()") ByteSlowNode childNode) {
            return childNode.execute(rope.getChild(), rope.getByteOffset() + index);
        }

        @Specialization(guards = "rope.getRawBytes() != null")
        public byte fastByte(ManagedRope rope, int index) {
            return rope.getRawBytes()[index];
        }

        @Specialization
        public byte getByteFromNativeRope(NativeRope rope, int index) {
            return rope.getByteSlow(index);
        }

        @TruffleBoundary
        @Specialization(guards = "rope.getRawBytes() == null")
        public byte getByteFromRope(ManagedRope rope, int index) {
            return rope.getByteSlow(index);
        }

        protected static boolean isSubstring(ManagedRope rope) {
            return rope instanceof SubstringRope;
        }
    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "rope")
    })
    public abstract static class BytesCopyNode extends RubyNode {

        public static BytesCopyNode create() {
            return RopeNodesFactory.BytesCopyNodeGen.create(null);
        }

        public abstract byte[] execute(Rope rope);

        @Specialization
        public byte[] getBytesFromNative(NativeRope rope) {
            return rope.getBytes();
        }

        @Specialization(guards = "rope.getRawBytes() != null")
        public byte[] getBytesManaged(ManagedRope rope) {
            return rope.getRawBytes().clone();
        }

        @TruffleBoundary
        @Specialization(guards = "rope.getRawBytes() == null")
        public byte[] getBytesFromRope(ManagedRope rope) {
            return rope.getBytesCopy();
        }
    }

    @NodeChildren({
        @NodeChild(type = RubyNode.class, value = "rope")
    })
    public abstract static class HashNode extends RubyNode {
        public static HashNode create() {
            return RopeNodesFactory.HashNodeGen.create(null);
        }

        public abstract int execute(Rope rope);

        @Specialization(guards = "rope.isHashCodeCalculated()")
        public int executeHashCalculated(Rope rope) {
            return rope.calculatedHashCode();
        }

        @Specialization(guards = "!rope.isHashCodeCalculated()")
        public int executeHashNotCalculated(Rope rope) {
            return rope.hashCode();
        }
    }
}
