/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.ArityException;
import com.oracle.truffle.api.interop.ForeignAccess;
import com.oracle.truffle.api.interop.Message;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.interop.UnsupportedMessageException;
import com.oracle.truffle.api.interop.UnsupportedTypeException;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.cext.CExtNodesFactory.StringToNativeNodeGen;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.MarkingServiceNodes;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.array.ArrayOperationNodes;
import org.truffleruby.core.array.ArrayStrategy;
import org.truffleruby.core.array.ArrayToObjectArrayNode;
import org.truffleruby.core.encoding.EncodingOperations;
import org.truffleruby.core.hash.HashNode;
import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.core.module.ModuleNodesFactory.SetVisibilityNodeGen;
import org.truffleruby.core.mutex.MutexOperations;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.ModuleNodes.ConstSetNode;
import org.truffleruby.core.module.ModuleNodes.SetVisibilityNode;
import org.truffleruby.core.numeric.BignumOperations;
import org.truffleruby.core.numeric.FixnumOrBignumNode;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.NativeRope;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringSupport;
import org.truffleruby.interop.ToJavaStringNodeGen;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.constants.GetConstantNode;
import org.truffleruby.language.constants.LookupConstantNode;
import org.truffleruby.language.control.BreakException;
import org.truffleruby.language.control.BreakID;
import org.truffleruby.language.control.JavaException;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DoesRespondDispatchHeadNode;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.InitializeClassNode;
import org.truffleruby.language.objects.InitializeClassNodeGen;
import org.truffleruby.language.objects.IsFrozenNode;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.language.objects.ObjectIVarGetNode;
import org.truffleruby.language.objects.ObjectIVarGetNodeGen;
import org.truffleruby.language.objects.ObjectIVarSetNode;
import org.truffleruby.language.objects.ObjectIVarSetNodeGen;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.objects.WriteObjectFieldNodeGen;
import org.truffleruby.language.supercall.CallSuperMethodNode;
import org.truffleruby.parser.Identifiers;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import static org.truffleruby.core.string.StringOperations.rope;

@CoreClass("Truffle::CExt")
public class CExtNodes {

    @ImportStatic(Message.class)
    @Primitive(name = "interop_call_c_with_mutex")
    public abstract static class CallCWithMutexNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object callCWithMutex(TruffleObject receiver, DynamicObject argsArray,
                @Cached("create()") ArrayToObjectArrayNode arrayToObjectArrayNode,
                @Cached("EXECUTE.createNode()") Node executeNode,
                @Cached("create()") BranchProfile exceptionProfile,
                @Cached("createBinaryProfile()") ConditionProfile ownedProfile) {
            final Object[] args = arrayToObjectArrayNode.executeToObjectArray(argsArray);

            if (getContext().getOptions().CEXT_LOCK) {
                final ReentrantLock lock = getContext().getCExtensionsLock();
                boolean owned = lock.isHeldByCurrentThread();

                if (ownedProfile.profile(!owned)) {
                    MutexOperations.lockInternal(getContext(), lock, this);
                    try {
                        return execute(receiver, args, executeNode, exceptionProfile);
                    } finally {
                        MutexOperations.unlockInternal(lock);
                    }
                } else {
                    return execute(receiver, args, executeNode, exceptionProfile);
                }
            } else {
                return execute(receiver, args, executeNode, exceptionProfile);
            }

        }

        private Object execute(TruffleObject receiver, Object[] args, Node executeNode, BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendExecute(executeNode, receiver, args);
            } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }
        }

    }

    @ImportStatic(Message.class)
    @Primitive(name = "interop_call_c_without_mutex")
    public abstract static class CallCWithoutMutexNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public Object callCWithoutMutex(TruffleObject receiver, DynamicObject argsArray,
                @Cached("create()") ArrayToObjectArrayNode arrayToObjectArrayNode,
                @Cached("EXECUTE.createNode()") Node executeNode,
                @Cached("create()") BranchProfile exceptionProfile,
                @Cached("createBinaryProfile()") ConditionProfile ownedProfile) {
            final Object[] args = arrayToObjectArrayNode.executeToObjectArray(argsArray);

            if (getContext().getOptions().CEXT_LOCK) {
                final ReentrantLock lock = getContext().getCExtensionsLock();
                boolean owned = lock.isHeldByCurrentThread();

                if (ownedProfile.profile(owned)) {
                    MutexOperations.unlockInternal(lock);
                    try {
                        return execute(receiver, args, executeNode, exceptionProfile);
                    } finally {
                        MutexOperations.lockInternal(getContext(), lock, this);
                    }
                } else {
                    return execute(receiver, args, executeNode, exceptionProfile);
                }
            } else {
                return execute(receiver, args, executeNode, exceptionProfile);
            }

        }

        private Object execute(TruffleObject receiver, Object[] args, Node executeNode, BranchProfile exceptionProfile) {
            try {
                return ForeignAccess.sendExecute(executeNode, receiver, args);
            } catch (UnsupportedTypeException | ArityException | UnsupportedMessageException e) {
                exceptionProfile.enter();
                throw new JavaException(e);
            }
        }

    }

    @CoreMethod(names = "rb_acquire_cext_mutex")
    public abstract static class AcquireCExtMutexNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        public boolean acquireMutex(VirtualFrame frame,
                @Cached("create()") BranchProfile exceptionProfile,
                @Cached("createBinaryProfile()") ConditionProfile ownedProfile) {
            if (getContext().getOptions().CEXT_LOCK) {
                final ReentrantLock lock = getContext().getCExtensionsLock();
                boolean owned = lock.isHeldByCurrentThread();

                if (ownedProfile.profile(!owned)) {
                    MutexOperations.lockInternal(getContext(), lock, this);
                    return true;
                }
            }
            return false;
        }
    }

    @CoreMethod(names = "rb_release_cext_mutex")
    public abstract static class ReleaseCExtMutexNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        public boolean releaseMutex(VirtualFrame frame,
                @Cached("create()") BranchProfile exceptionProfile,
                @Cached("createBinaryProfile()") ConditionProfile ownedProfile) {
            if (getContext().getOptions().CEXT_LOCK) {
                final ReentrantLock lock = getContext().getCExtensionsLock();
                boolean owned = lock.isHeldByCurrentThread();

                if (ownedProfile.profile(owned)) {
                    MutexOperations.unlockInternal(lock);
                    return true;
                }
            }
            return false;
        }
    }

    @CoreMethod(names = "rb_ulong2num", onSingleton = true, required = 1)
    public abstract static class ULong2NumNode extends CoreMethodArrayArgumentsNode {

        private static final BigInteger TWO_POW_64 = BigInteger.valueOf(1).shiftLeft(64);

        @Specialization
        public Object ulong2num(long num,
                @Cached("createBinaryProfile()") ConditionProfile positiveProfile) {
            if (positiveProfile.profile(num >= 0)) {
                return num;
            } else {
                return BignumOperations.createBignum(getContext(), toUnsigned(num));
            }
        }

        @TruffleBoundary
        private BigInteger toUnsigned(long num) {
            return BigInteger.valueOf(num).add(TWO_POW_64);
        }

    }

    @CoreMethod(names = "rb_integer_bytes", onSingleton = true, lowerFixnum = { 2, 3 }, required = 6)
    public abstract static class IntegerBytesNode extends CoreMethodArrayArgumentsNode {

        // The Ruby MRI API for extracting the contents of a integer
        // fills a provided buffer of words with a representation of
        // the number in the specified format. This allows the users
        // to specify the word order in the buffer, the endianness
        // within the word, and whether to encode the number as a
        // magnitude or in two's complement.
        //
        // The API also returns an integer indicating whether the
        // number was positive or negative and the overflow
        // status. That part is implemented in C while this Java
        // method is purely concerned with extracting the bytes into
        // the buffer and getting them in the right order.
        //
        // If the buffer is too short to hold the entire number then
        // it will be filled with the least significant bytes of the
        // number.
        //
        // If the buffer is longer than is required for encoding the
        // number then the remainder of the buffer must not change the
        // interpretation of the number. I.e. if we are encoding in
        // two's complement then it must be filled with 0xff to
        // preserve the number's sign.
        //
        // The API allows the order of words in the buffer to be
        // specified as well as the order of bytes within those
        // words. We separate the process into two stages to make the
        // code easier to understand, first copying the bytes and
        // adding padding at the start of end, and then reordering the
        // bytes within each word if required.

        @Specialization
        @TruffleBoundary
        public DynamicObject bytes(int num, int num_words, int word_length, boolean msw_first, boolean twosComp, boolean bigEndian) {
            BigInteger bi = BigInteger.valueOf(num);
            return bytes(bi, num_words, word_length, msw_first, twosComp, bigEndian);
        }

        @Specialization
        @TruffleBoundary
        public DynamicObject bytes(long num, int num_words, int word_length, boolean msw_first, boolean twosComp, boolean bigEndian) {
            BigInteger bi = BigInteger.valueOf(num);
            return bytes(bi, num_words, word_length, msw_first, twosComp, bigEndian);
        }

        @Specialization(guards = "isRubyBignum(num)")
        @TruffleBoundary
        public DynamicObject bytes(DynamicObject num, int num_words, int word_length, boolean msw_first, boolean twosComp, boolean bigEndian) {
            BigInteger bi = Layouts.BIGNUM.getValue(num);
            return bytes(bi, num_words, word_length, msw_first, twosComp, bigEndian);
        }

        private DynamicObject bytes(BigInteger bi, int num_words, int word_length, boolean msw_first, boolean twosComp, boolean bigEndian) {
            if (!twosComp) {
                bi = bi.abs();
            }
            int num_bytes = num_words * word_length;
            // We'll put the bytes into ints because we lack a byte array strategy.
            int[] bytes = new int[num_bytes];
            byte[] bi_bytes = bi.toByteArray();
            int bi_length = bi_bytes.length;
            boolean negative = bi.signum() == -1;

            // If we're not giving a twos comp answer then a leading
            // zero byte should be discarded.
            if (!twosComp && bi_bytes[0] == 0) {
                bi_length--;
            }
            int bytes_to_copy = Math.min(bi_length, num_bytes);
            if (msw_first) {
                // We must copy the LSBs if the buffer would overflow,
                // so calculate an offset based on that.
                int offset = bi_bytes.length - bytes_to_copy;

                for (int i = 0; i < bytes_to_copy; i++) {
                    bytes[i] = bi_bytes[offset + i];
                }
                if (negative) {
                    for (int i = 0; i < num_bytes - bytes_to_copy; i++) {
                        bytes[i] = -1;
                    }
                }
            } else {
                for (int i = 0; i < bytes_to_copy; i++) {
                    bytes[i] = bi_bytes[bi_bytes.length - 1 - i];
                }
                if (negative) {
                    for (int i = bytes_to_copy; i < num_bytes; i++) {
                        bytes[i] = -1;
                    }
                }
            }

            // Swap bytes around if they aren't in the right order for
            // the requested endianness.
            if (bigEndian ^ msw_first && word_length > 1) {
                for (int i = 0; i < num_words; i++) {
                    for (int j = 0; j < word_length / 2; j++) {
                        int pos_a = i * word_length + j;
                        int pos_b = (i + 1) * word_length - 1 - j;
                        int a = bytes[pos_a];
                        bytes[pos_a] = bytes[pos_b];
                        bytes[pos_b] = a;
                    }
                }
            }
            return ArrayHelpers.createArray(getContext(), bytes, bytes.length);
        }


    }

    @CoreMethod(names = "rb_absint_bit_length", onSingleton = true, required = 1)
    public abstract static class BignumAbsBitLengthNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        @TruffleBoundary
        public int bitLength(int num) {
            return BigInteger.valueOf(num).abs().bitLength();
        }

        @Specialization
        @TruffleBoundary
        public int bitLength(long num) {
            return BigInteger.valueOf(num).abs().bitLength();
        }

        @Specialization(guards = "isRubyBignum(num)")
        @TruffleBoundary
        public int bitLength(DynamicObject num) {
            return Layouts.BIGNUM.getValue(num).abs().bitLength();
        }
    }

    @CoreMethod(names = "rb_2scomp_bit_length", onSingleton = true, required = 1)
    public abstract static class Bignum2sCompBitLengthNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        @TruffleBoundary
        public int bitLength(int num) {
            return BigInteger.valueOf(num).bitLength();
        }

        @Specialization
        @TruffleBoundary
        public int bitLength(long num) {
            return BigInteger.valueOf(num).bitLength();
        }

        @Specialization(guards = "isRubyBignum(num)")
        @TruffleBoundary
        public int bitLength(DynamicObject num) {
            return Layouts.BIGNUM.getValue(num).bitLength();
        }
    }

    @Primitive(name = "rb_int_singlebit_p")
    public abstract static class IntSinglebitPPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int intSinglebitP(int num) {
            assert num >= 0;
            return Integer.bitCount(num) == 1 ? 1 : 0;
        }

        @Specialization
        public int intSinglebitP(long num) {
            assert num >= 0;
            return Long.bitCount(num) == 1 ? 1 : 0;
        }

        @Specialization(guards = "isRubyBignum(num)")
        @TruffleBoundary
        public int intSinglebitP(DynamicObject num) {
            assert Layouts.BIGNUM.getValue(num).signum() >= 0;
            return Layouts.BIGNUM.getValue(num).bitCount() == 1 ? 1 : 0;
        }
    }

    @CoreMethod(names = "DBL2BIG", onSingleton = true, required = 1)
    public abstract static class DBL2BIGNode extends CoreMethodArrayArgumentsNode {

        @Child private FixnumOrBignumNode fixnumOrBignum = new FixnumOrBignumNode();

        @Specialization
        @TruffleBoundary
        public Object dbl2big(double num,
                              @Cached("create()") BranchProfile errorProfile) {
            if (Double.isInfinite(num)) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().floatDomainError("Infinity", this));
            }

            if (Double.isNaN(num)) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().floatDomainError("NaN", this));
            }

            return fixnumOrBignum.fixnumOrBignum(num);
        }

    }

    @CoreMethod(names = "rb_class_of", onSingleton = true, required = 1)
    public abstract static class RBClassOfNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject rb_class_of(Object object,
                                      @Cached("create()") MetaClassNode metaClassNode) {
            return metaClassNode.executeMetaClass(object);
        }

    }

    @CoreMethod(names = "rb_long2int", onSingleton = true, required = 1)
    public abstract static class Long2Int extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int long2fix(int num) {
            return num;
        }

        @Specialization(guards = "fitsIntoInteger(num)")
        public int long2fixInRange(long num) {
            return (int) num;
        }

        @Specialization(guards = "!fitsIntoInteger(num)")
        public int long2fixOutOfRange(long num) {
            throw new RaiseException(getContext(), coreExceptions().rangeErrorConvertToInt(num, this));
        }

        protected boolean fitsIntoInteger(long num) {
            return CoreLibrary.fitsIntoInteger(num);
        }

    }

    @CoreMethod(names = "rb_enc_coderange_clear", onSingleton = true, required = 1)
    public abstract static class RbEncCodeRangeClear extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject clearCodeRange(DynamicObject string,
                @Cached("create()") StringToNativeNode stringToNativeNode) {
            final NativeRope nativeRope = stringToNativeNode.executeToNative(string);
            nativeRope.clearCodeRange();
            StringOperations.setRope(string, nativeRope);

            return string;
        }

    }

    @CoreMethod(names = "rb_enc_codepoint_len", onSingleton = true, required = 2)
    public abstract static class RbEncCodePointLenNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject rbEncCodePointLen(DynamicObject string, DynamicObject encoding,
                @Cached("create()") RopeNodes.BytesNode bytesNode,
                @Cached("create()") RopeNodes.CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached("create()") RopeNodes.CodeRangeNode codeRangeNode,
                @Cached("createBinaryProfile()") ConditionProfile sameEncodingProfile,
                @Cached("create()") BranchProfile errorProfile) {
            final Rope rope = rope(string);
            final byte[] bytes = bytesNode.execute(rope);
            final CodeRange ropeCodeRange = codeRangeNode.execute(rope);
            final Encoding enc = Layouts.ENCODING.getEncoding(encoding);

            final CodeRange cr;
            if (sameEncodingProfile.profile(enc == rope.getEncoding())) {
                cr = ropeCodeRange;
            } else {
                cr = CodeRange.CR_UNKNOWN;
            }

            final int r = calculateCharacterLengthNode.characterLength(enc, cr, bytes, 0, bytes.length);

            if (!StringSupport.MBCLEN_CHARFOUND_P(r)) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentError("invalid byte sequence in " + enc, this));
            }

            final int len_p = StringSupport.MBCLEN_CHARFOUND_LEN(r);
            final int codePoint = StringSupport.preciseCodePoint(enc, ropeCodeRange, bytes, 0, bytes.length);

            return createArray(new Object[]{len_p, codePoint}, 2);
        }

    }

    @CoreMethod(names = "rb_str_new_nul", onSingleton = true, required = 1, lowerFixnum = 1)
    public abstract static class RbStrNewNulNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject rbStrNewNul(int byteLength,
                @Cached("create()") StringNodes.MakeStringNode makeStringNode) {
            final Rope rope = NativeRope.newBuffer(getContext().getFinalizationService(), byteLength, byteLength);

            return makeStringNode.fromRope(rope);
        }

    }

    @CoreMethod(names = "rb_str_capacity", onSingleton = true, required = 1)
    public abstract static class RbStrCapacityNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long capacity(DynamicObject string,
                @Cached("create()") StringToNativeNode stringToNativeNode) {
            return stringToNativeNode.executeToNative(string).getCapacity();
        }

    }

    @CoreMethod(names = "rb_str_set_len", onSingleton = true, required = 2, lowerFixnum = 2)
    public abstract static class RbStrSetLenNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject strSetLen(DynamicObject string, int newByteLength,
                @Cached("create()") StringToNativeNode stringToNativeNode,
                @Cached("createBinaryProfile()") ConditionProfile asciiOnlyProfile) {
            final NativeRope nativeRope = stringToNativeNode.executeToNative(string);

            final CodeRange newCodeRange;
            final int newCharacterLength;
            if (asciiOnlyProfile.profile(nativeRope.getRawCodeRange() == CodeRange.CR_7BIT)) {
                newCodeRange = CodeRange.CR_7BIT;
                newCharacterLength = newByteLength;
            } else {
                newCodeRange = CodeRange.CR_UNKNOWN;
                newCharacterLength = NativeRope.UNKNOWN_CHARACTER_LENGTH;
            }

            final NativeRope newNativeRope = nativeRope.withByteLength(newByteLength, newCharacterLength, newCodeRange);
            StringOperations.setRope(string, newNativeRope);

            return string;
        }

    }

    @CoreMethod(names = "rb_str_resize", onSingleton = true, required = 2, lowerFixnum = 2)
    public abstract static class RbStrResizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject rbStrResize(DynamicObject string, int newByteLength,
                @Cached("create()") StringToNativeNode stringToNativeNode) {
            final NativeRope nativeRope = stringToNativeNode.executeToNative(string);

            if (nativeRope.byteLength() == newByteLength) {
                // Like MRI's rb_str_resize()
                nativeRope.clearCodeRange();
                return string;
            } else {
                final NativeRope newRope = nativeRope.resize(getContext().getFinalizationService(), newByteLength);

                // Like MRI's rb_str_resize()
                newRope.clearCodeRange();

                StringOperations.setRope(string, newRope);
                return string;
            }
        }
    }

    @CoreMethod(names = "rb_block_proc", onSingleton = true)
    public abstract static class BlockProcNode extends CoreMethodArrayArgumentsNode {

        // TODO (pitr-ch 04-Dec-2017): needs optimising
        @TruffleBoundary
        @Specialization
        public DynamicObject blockProc() {
            return Truffle.getRuntime().iterateFrames(frameInstance -> {
                final Node callNode = frameInstance.getCallNode();

                if (callNode != null) {
                    final RootNode rootNode = callNode.getRootNode();
                    // Skip Ruby frames in cext.rb file since they are implementing methods which are implemented
                    // with C in MRI, and therefore are also implicitly skipped when when looking up the block passed
                    // to a C API function.
                    if (rootNode instanceof RubyRootNode &&
                            rootNode.getSourceSection().isAvailable() &&
                            !rootNode.getSourceSection().getSource().getName().endsWith("truffle/cext.rb")) {

                        final DynamicObject block = RubyArguments.getBlock(frameInstance.getFrame(FrameAccess.READ_ONLY));
                        return block == null ? nil() : block;
                    }
                }

                return null;
            });
        }

    }

    @CoreMethod(names = "rb_check_frozen", onSingleton = true, required = 1)
    public abstract static class CheckFrozenNode extends CoreMethodArrayArgumentsNode {

        @Child private IsFrozenNode isFrozenNode = IsFrozenNode.create();

        @Specialization
        public boolean rb_check_frozen(Object object) {
            isFrozenNode.raiseIfFrozen(object);
            return true;
        }

    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name")
    })
    @CoreMethod(names = "rb_const_get", onSingleton = true, required = 2)
    public abstract static class RbConstGetNode extends CoreMethodNode {

        @Child private LookupConstantNode lookupConstantNode = LookupConstantNode.create(true, false, true);
        @Child private GetConstantNode getConstantNode = GetConstantNode.create();

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return ToJavaStringNodeGen.create(name);
        }

        @Specialization
        public Object rbConstGet(DynamicObject module, String name) {
            return getConstantNode.lookupAndResolveConstant(LexicalScope.IGNORE, module, name, lookupConstantNode);
        }

    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name")
    })
    @CoreMethod(names = "rb_const_get_from", onSingleton = true, required = 2)
    public abstract static class RbConstGetFromNode extends CoreMethodNode {

        @Child private LookupConstantNode lookupConstantNode = LookupConstantNode.create(true, false, false);
        @Child private GetConstantNode getConstantNode = GetConstantNode.create();

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return ToJavaStringNodeGen.create(name);
        }

        @Specialization
        public Object rbConstGetFrom(DynamicObject module, String name) {
            return getConstantNode.lookupAndResolveConstant(LexicalScope.IGNORE, module, name, lookupConstantNode);
        }

    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name"),
            @NodeChild(type = RubyNode.class, value = "value")
    })
    @CoreMethod(names = "rb_const_set", onSingleton = true, required = 3)
    public abstract static class RbConstSetNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return ToJavaStringNodeGen.create(name);
        }

        @Specialization
        public Object rbConstSet(DynamicObject module, String name, Object value,
                @Cached("create()") ConstSetNode constSetNode) {
            return constSetNode.setConstantNoCheckName(module, name, value);
        }

    }

    @CoreMethod(names = "cext_module_function", onSingleton = true, required = 2)
    public abstract static class CextModuleFunctionNode extends CoreMethodArrayArgumentsNode {

        @Child SetVisibilityNode setVisibilityNode = SetVisibilityNodeGen.create(Visibility.MODULE_FUNCTION);

        @Specialization(guards = {"isRubyModule(module)", "isRubySymbol(name)"})
        public DynamicObject cextModuleFunction(VirtualFrame frame, DynamicObject module, DynamicObject name) {
            return setVisibilityNode.executeSetVisibility(frame, module, new Object[]{name});
        }

    }

    @CoreMethod(names = "caller_frame_visibility", onSingleton = true, required = 1)
    public abstract static class CallerFrameVisibilityNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubySymbol(visibility)")
        public boolean checkCallerVisibility(DynamicObject visibility) {
            final Frame callerFrame = getContext().getCallStack().getCallerFrameIgnoringSend().getFrame(FrameAccess.READ_ONLY);
            final Visibility callerVisibility = DeclarationContext.findVisibility(callerFrame);

            switch (Layouts.SYMBOL.getString(visibility)) {
                case "private":
                    return callerVisibility.isPrivate();
                case "protected":
                    return callerVisibility.isProtected();
                case "module_function":
                    return callerVisibility.isModuleFunction();
                default:
                    throw new UnsupportedOperationException();
            }
        }

    }

    @CoreMethod(names = "rb_iter_break_value", onSingleton = true, required = 1)
    public abstract static class IterBreakValueNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object iterBreakValue(Object value) {
            throw new BreakException(BreakID.ANY_BLOCK, value);
        }

    }

    @CoreMethod(names = "rb_sourcefile", onSingleton = true)
    public abstract static class SourceFileNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        public DynamicObject sourceFile() {
            final SourceSection sourceSection = getTopUserSourceSection("rb_sourcefile");
            final String file = getContext().getPath(sourceSection.getSource());

            return makeStringNode.executeMake(file, UTF8Encoding.INSTANCE, CodeRange.CR_UNKNOWN);
        }

        public static SourceSection getTopUserSourceSection(String...  methodNames) {
            return Truffle.getRuntime().iterateFrames(frameInstance -> {
                final Node callNode = frameInstance.getCallNode();

                if (callNode != null) {
                    final RootNode rootNode = callNode.getRootNode();

                    if (rootNode instanceof RubyRootNode && rootNode.getSourceSection().isAvailable() &&
                            !nameMatches(((RubyRootNode) rootNode).getSharedMethodInfo().getName(), methodNames)) {
                        return frameInstance.getCallNode().getEncapsulatingSourceSection();
                    }
                }

                return null;
            });
        }

        private static boolean nameMatches(String name, String...methodNames) {
            for (String methodName : methodNames) {
                if (methodName.equals(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    @CoreMethod(names = "rb_sourceline", onSingleton = true)
    public abstract static class SourceLineNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int sourceLine() {
            final SourceSection sourceSection = SourceFileNode.getTopUserSourceSection("rb_sourceline");
            return sourceSection.getStartLine();
        }

    }

    @CoreMethod(names = "rb_is_instance_id", onSingleton = true, required = 1)
    public abstract static class IsInstanceIdNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbol(symbol)")
        public boolean isInstanceId(DynamicObject symbol) {
            return Identifiers.isValidInstanceVariableName(Layouts.SYMBOL.getString(symbol));
        }

    }

    @CoreMethod(names = "rb_is_const_id", onSingleton = true, required = 1)
    public abstract static class IsConstIdNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbol(symbol)")
        public boolean isConstId(DynamicObject symbol) {
            return Identifiers.isValidConstantName(Layouts.SYMBOL.getString(symbol));
        }

    }

    @CoreMethod(names = "rb_is_class_id", onSingleton = true, required = 1)
    public abstract static class IsClassVariableIdNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbol(symbol)")
        public boolean isClassVariableId(DynamicObject symbol) {
            return Identifiers.isValidClassVariableName(Layouts.SYMBOL.getString(symbol));
        }

    }

    @CoreMethod(names = "ruby_object?", onSingleton = true, required = 1)
    public abstract static class RubyObjectNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isBoxedPrimitive(object)")
        public boolean rubyObjectPrimitive(Object object) {
            return true;
        }

        @Specialization(guards = "!isBoxedPrimitive(object)")
        public boolean rubyObject(Object object) {
            return RubyGuards.isRubyBasicObject(object);
        }

    }

    @CoreMethod(names = "rb_call_super_splatted", onSingleton = true, rest = true)
    public abstract static class CallSuperNode extends CoreMethodArrayArgumentsNode {

        @Child private CallSuperMethodNode callSuperMethodNode = CallSuperMethodNode.create();
        @Child private MetaClassNode metaClassNode = MetaClassNode.create();

        @Specialization
        public Object callSuper(VirtualFrame frame, Object[] args) {
            final Frame callingMethodFrame = findCallingMethodFrame();
            final InternalMethod callingMethod = RubyArguments.getMethod(callingMethodFrame);
            final Object callingSelf = RubyArguments.getSelf(callingMethodFrame);
            final DynamicObject callingMetaclass = metaClassNode.executeMetaClass(callingSelf);
            final MethodLookupResult superMethodLookup = ModuleOperations.lookupSuperMethod(callingMethod, callingMetaclass);
            final InternalMethod superMethod = superMethodLookup.getMethod();
            return callSuperMethodNode.executeCallSuperMethod(frame, callingSelf, superMethod, args, null);
        }

        @TruffleBoundary
        private static Frame findCallingMethodFrame() {
            return Truffle.getRuntime().iterateFrames(frameInstance -> {
                final Frame frame = frameInstance.getFrame(FrameAccess.READ_ONLY);

                final InternalMethod method = RubyArguments.tryGetMethod(frame);

                if (method == null) {
                    return null;
                } else if (method.getName().equals(/* Truffle::CExt. */ "rb_call_super")
                        || method.getName().equals(/* Truffle::Interop. */ "execute_without_conversion")
                        || method.getName().equals(/* Truffle::CExt. */ "rb_call_super_splatted")) {
                    // TODO CS 11-Mar-17 must have a more precise check to skip these methods
                    return null;
                } else {
                    return frame;
                }
            });
        }

    }

    @CoreMethod(names = "rb_frame_this_func", onSingleton = true, rest = true)
    public abstract static class FrameThisFunctionNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object frameThisFunc(VirtualFrame frame, Object[] args) {
            final Frame callingMethodFrame = findCallingMethodFrame();
            final InternalMethod callingMethod = RubyArguments.getMethod(callingMethodFrame);
            return getSymbol(callingMethod.getName());
        }

        @TruffleBoundary
        private static Frame findCallingMethodFrame() {
            return Truffle.getRuntime().iterateFrames(frameInstance -> {
                final Frame frame = frameInstance.getFrame(FrameAccess.READ_ONLY);

                final InternalMethod method = RubyArguments.tryGetMethod(frame);

                if (method == null) {
                    return null;
                } else if (method.getName().equals(/* Truffle::CExt. */ "rb_frame_this_func")
                        || method.getName().equals(/* Truffle::Interop  */ "execute_without_conversion")) {
                    // TODO CS 11-Mar-17 must have a more precise check to skip these methods
                    return null;
                } else {
                    return frame;
                }
            });
        }

    }

    @CoreMethod(names = "rb_syserr_fail", onSingleton = true, required = 2, lowerFixnum = 1)
    public abstract static class RbSysErrFail extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isNil(nil)")
        public Object rbSysErrFailNoMessage(int errno, DynamicObject nil) {
            throw new RaiseException(getContext(), coreExceptions().errnoError(errno, "", this));
        }

        @Specialization(guards = "isRubyString(message)")
        public Object rbSysErrFail(int errno, DynamicObject message) {
            throw new RaiseException(getContext(), coreExceptions().errnoError(errno, StringOperations.getString(message), this));
        }

    }

    @CoreMethod(names = "rb_hash", onSingleton = true, required = 1)
    public abstract static class RbHashNode extends CoreMethodArrayArgumentsNode {

        @Child private HashNode hash;

        @Specialization
        public Object rbHash(Object object) {
            if (hash == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                hash = insert(new HashNode());
            }

            return hash.hash(object, false);
        }
    }

    @CoreMethod(names = "string_pointer_size", onSingleton = true, required = 1)
    public abstract static class StringPointerSizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyString(string)")
        public int size(DynamicObject string) {
            final Rope rope = rope(string);
            final int byteLength = rope.byteLength();
            int i = 0;
            for (; i < byteLength; i++) {
                if (rope.get(i) == 0) {
                    return i;
                }
            }
            return byteLength;
        }

    }

    public abstract static class StringToNativeNode extends RubyBaseNode {

        public static StringToNativeNode create() {
            return StringToNativeNodeGen.create();
        }

        public abstract NativeRope executeToNative(DynamicObject string);

        @Specialization(guards = "isRubyString(string)")
        protected NativeRope toNative(DynamicObject string,
                @Cached("createBinaryProfile()") ConditionProfile convertProfile,
                @Cached("create()") RopeNodes.BytesNode bytesNode,
                @Cached("create()") RopeNodes.CharacterLengthNode characterLengthNode,
                @Cached("create()") RopeNodes.CodeRangeNode codeRangeNode) {
            final Rope currentRope = rope(string);

            final NativeRope nativeRope;

            if (convertProfile.profile(currentRope instanceof NativeRope)) {
                nativeRope = (NativeRope) currentRope;
            } else {
                nativeRope = new NativeRope(getContext().getFinalizationService(),
                        bytesNode.execute(currentRope),
                        currentRope.getEncoding(),
                        characterLengthNode.execute(currentRope),
                        codeRangeNode.execute(currentRope));
                StringOperations.setRope(string, nativeRope);
            }

            return nativeRope;
        }

    }

    @CoreMethod(names = "string_pointer_to_native", onSingleton = true, required = 1)
    public abstract static class StringPointerToNativeNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyString(string)")
        public long toNative(DynamicObject string,
                @Cached("create()") StringToNativeNode stringToNativeNode) {
            final NativeRope nativeRope = stringToNativeNode.executeToNative(string);

            return nativeRope.getNativePointer().getAddress();
        }

    }

    @CoreMethod(names = "string_pointer_is_native?", onSingleton = true, required = 1)
    public abstract static class StringPointerIsNativeNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyString(string)")
        public boolean isNative(DynamicObject string) {
            return rope(string) instanceof NativeRope;
        }

    }

    @CoreMethod(names = "string_pointer_read", onSingleton = true, required = 2, lowerFixnum = 2)
    public abstract static class StringPointerReadNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyString(string)")
        public Object read(DynamicObject string, int index,
                @Cached("createBinaryProfile()") ConditionProfile nativeRopeProfile,
                @Cached("createBinaryProfile()") ConditionProfile inBoundsProfile,
                @Cached("create()") RopeNodes.GetByteNode getByteNode) {
            final Rope rope = rope(string);

            if (nativeRopeProfile.profile(rope instanceof NativeRope) || inBoundsProfile.profile(index < rope.byteLength())) {
                return getByteNode.executeGetByte(rope, index);
            } else {
                return 0;
            }
        }

    }

    @CoreMethod(names = "string_pointer_write", onSingleton = true, required = 3, lowerFixnum = {2, 3})
    public abstract static class StringPointerWriteNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyString(string)")
        public int write(DynamicObject string, int index, int value,
                @Cached("createBinaryProfile()") ConditionProfile newRopeProfile,
                @Cached("create()") RopeNodes.SetByteNode setByteNode) {
            final Rope rope = rope(string);

            final Rope newRope = setByteNode.executeSetByte(rope, index, value);
            if (newRopeProfile.profile(newRope != rope)) {
                StringOperations.setRope(string, newRope);
            }

            return value;
        }

    }

    @CoreMethod(names = "rb_class_new", onSingleton = true, required = 1)
    public abstract static class ClassNewNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode allocateNode;
        @Child private InitializeClassNode initializeClassNode;

        @Specialization
        public DynamicObject classNew(DynamicObject superclass) {
            if (allocateNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                allocateNode = insert(CallDispatchHeadNode.createPrivate());
                initializeClassNode = insert(InitializeClassNodeGen.create(false));
            }

            DynamicObject klass = (DynamicObject) allocateNode.call(getContext().getCoreLibrary().getClassClass(), "__allocate__");
            return initializeClassNode.executeInitialize(klass, superclass, NotProvided.INSTANCE);
        }

    }

    @CoreMethod(names = "rb_tr_debug", onSingleton = true, rest = true)
    public abstract static class DebugNode extends CoreMethodArrayArgumentsNode {

        @Child CallDispatchHeadNode toSCall;

        @TruffleBoundary
        @Specialization
        public Object debug(Object... objects) {
            if (objects.length > 1) {
                System.err.printf("Printing %d values%n", objects.length);
            }

            for (Object object : objects) {
                final String representation;

                if (RubyGuards.isRubyString(object)) {
                    final Rope rope = StringOperations.rope((DynamicObject) object);
                    final byte[] bytes = rope.getBytes();
                    final StringBuilder builder = new StringBuilder();

                    for (int i = 0; i < bytes.length; i++) {
                        if (i % 4 == 0 && i != 0 && i != bytes.length - 1) {
                            builder.append(" ");
                        }
                        builder.append(String.format("%02x", bytes[i]));
                    }

                    representation = RopeOperations.decodeRope(rope) + " (" + builder.toString() + ")";
                } else if (RubyGuards.isRubyBasicObject(object)) {
                    representation = object.toString() + " (" + StringOperations.getString(callToS(object)) + ")";
                } else {
                    representation = object.toString();
                }

                System.err.printf("%s @ %s: %s%n", object.getClass(), System.identityHashCode(object), representation);
            }
            return nil();
        }

        private DynamicObject callToS(Object object) {
            if (toSCall == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toSCall = insert(CallDispatchHeadNode.createPrivate());
            }

            return (DynamicObject) toSCall.call(object, "to_s");
        }

    }

    // Those primitives store a Symbol as key, so they effectively have
    // different namespace than normal ivars which use java.lang.String.
    @CoreMethod(names = "hidden_variable_get", onSingleton = true, required = 2)
    public abstract static class HiddenVariableGetNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbol(name)")
        public Object hiddenVariableGet(DynamicObject object, DynamicObject name,
                @Cached("createObjectIVarGetNode()") ObjectIVarGetNode iVarGetNode) {
            return iVarGetNode.executeIVarGet(object, name);
        }

        protected ObjectIVarGetNode createObjectIVarGetNode() {
            return ObjectIVarGetNodeGen.create(false);
        }

    }

    @CoreMethod(names = "hidden_variable_set", onSingleton = true, required = 3)
    public abstract static class HiddenVariableSetNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbol(name)")
        public Object hiddenVariableSet(DynamicObject object, DynamicObject name, Object value,
                @Cached("createObjectIVarSetNode()") ObjectIVarSetNode iVarSetNode) {
            return iVarSetNode.executeIVarSet(object, name, value);
        }

        protected ObjectIVarSetNode createObjectIVarSetNode() {
            return ObjectIVarSetNodeGen.create(false);
        }

    }

    @CoreMethod(names = "capture_exception", onSingleton = true, needsBlock = true)
    public abstract static class CaptureExceptionNode extends YieldingCoreMethodNode {

        @Specialization
        public TruffleObject executeWithProtect(DynamicObject block,
                                                @Cached("create()") BranchProfile exceptionProfile,
                                                @Cached("create()") BranchProfile noExceptionProfile) {
            try {
                yield(block);
                noExceptionProfile.enter();
                return nil();
            } catch (Throwable e) {
                exceptionProfile.enter();
                return new CapturedException(e);
            }
        }
    }

    @CoreMethod(names = "raise_exception", onSingleton = true, required = 1)
    public abstract static class RaiseExceptionNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object executeThrow(CapturedException captured,
                @Cached("createBinaryProfile()") ConditionProfile runtimeExceptionProfile,
                @Cached("createBinaryProfile()") ConditionProfile errorProfile) {
            final Throwable e = captured.getException();
            if (runtimeExceptionProfile.profile(e instanceof RuntimeException)) {
                throw (RuntimeException) e;
            } else if (errorProfile.profile(e instanceof Error)) {
                throw (Error) e;
            } else {
                throw new JavaException(e);
            }
        }
    }

    @CoreMethod(names = "linker", onSingleton = true, required = 3)
    public abstract static class LinkerNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = { "isRubyString(outputFileName)", "isRubyArray(libraries)", "isRubyArray(bitcodeFiles)", "libraryStrategy.matches(libraries)", "fileStrategy.matches(bitcodeFiles)" })
        public Object linker(DynamicObject outputFileName, DynamicObject libraries, DynamicObject bitcodeFiles,
                @Cached("of(libraries)") ArrayStrategy libraryStrategy,
                @Cached("of(bitcodeFiles)") ArrayStrategy fileStrategy,
                @Cached("libraryStrategy.boxedCopyNode()") ArrayOperationNodes.ArrayBoxedCopyNode libBoxCopyNode,
                @Cached("fileStrategy.boxedCopyNode()") ArrayOperationNodes.ArrayBoxedCopyNode fileBoxCopyNode) {
            try {
                Linker.link(
                        StringOperations.getString(outputFileName),
                        array2StringList(libBoxCopyNode.execute(Layouts.ARRAY.getStore(libraries), Layouts.ARRAY.getSize(libraries))),
                        array2StringList(fileBoxCopyNode.execute(Layouts.ARRAY.getStore(bitcodeFiles), Layouts.ARRAY.getSize(bitcodeFiles))));
            } catch (IOException e) {
                throw new JavaException(e);
            }
            return outputFileName;
        }

        private static List<String> array2StringList(Object[] objectArray) {
            List<String> list = new ArrayList<>(objectArray.length);
            for (int i = 0; i < objectArray.length; i++) {
                list.add(StringOperations.getString((DynamicObject) objectArray[i]));
            }
            return list;
        }
    }

    @CoreMethod(names = "MBCLEN_NEEDMORE_P", onSingleton = true, required = 1, lowerFixnum = 1)
    public abstract static class MBCLEN_NEEDMORE_PNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object mbclenNeedMoreP(int r) {
            return StringSupport.MBCLEN_NEEDMORE_P(r);
        }

    }

    @CoreMethod(names = "MBCLEN_NEEDMORE_LEN", onSingleton = true, required = 1, lowerFixnum = 1)
    public abstract static class MBCLEN_NEEDMORE_LENNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object mbclenNeedMoreLen(int r) {
            return StringSupport.MBCLEN_NEEDMORE_LEN(r);
        }

    }

    @CoreMethod(names = "MBCLEN_CHARFOUND_P", onSingleton = true, required = 1, lowerFixnum = 1)
    public abstract static class MBCLEN_CHARFOUND_PNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object mbclenCharFoundP(int r) {
            return StringSupport.MBCLEN_CHARFOUND_P(r);
        }

    }

    @CoreMethod(names = "MBCLEN_CHARFOUND_LEN", onSingleton = true, required = 1, lowerFixnum = 1)
    public abstract static class MBCLEN_CHARFOUND_LENNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object mbclenCharFoundLen(int r) {
            return StringSupport.MBCLEN_CHARFOUND_LEN(r);
        }

    }

    @CoreMethod(names = "rb_enc_mbmaxlen", onSingleton = true, required = 1)
    public abstract static class RbEncMaxLenNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyEncoding(value)")
        public Object rbEncMaxLen(DynamicObject value) {
            return EncodingOperations.getEncoding(value).maxLength();
        }

    }

    @CoreMethod(names = "rb_enc_mbminlen", onSingleton = true, required = 1)
    public abstract static class RbEncMinLenNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyEncoding(value)")
        public Object rbEncMinLen(DynamicObject value) {
            return EncodingOperations.getEncoding(value).minLength();
        }

    }

    @CoreMethod(names = "rb_enc_mbclen", onSingleton = true, required = 4, lowerFixnum = { 3, 4 })
    public abstract static class RbEncMbLenNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = { "isRubyEncoding(enc)", "isRubyString(str)" })
        public Object rbEncMbLen(DynamicObject enc, DynamicObject str, int p, int e,
                @Cached("create()") RopeNodes.CodeRangeNode codeRangeNode,
                @Cached("createBinaryProfile()") ConditionProfile sameEncodingProfile) {
            final Encoding encoding = EncodingOperations.getEncoding(enc);
            final Rope rope = StringOperations.rope(str);
            final Encoding ropeEncoding = rope.getEncoding();

            return StringSupport.characterLength(
                    encoding,
                    sameEncodingProfile.profile(encoding == ropeEncoding) ? codeRangeNode.execute(rope) : CodeRange.CR_UNKNOWN,
                    StringOperations.rope(str).getBytes(),
                    p,
                    e,
                    true);
        }

    }

    @CoreMethod(names = "rb_enc_left_char_head", onSingleton = true, required = 5, lowerFixnum = { 3, 4, 5 })
    public abstract static class RbEncLeftCharHeadNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = { "isRubyEncoding(enc)", "isRubyString(str)" })
        public Object rbEncLeftCharHead(DynamicObject enc, DynamicObject str, int start, int p, int end) {
            return EncodingOperations.getEncoding(enc).leftAdjustCharHead(
                    StringOperations.rope(str).getBytes(), start, p, end);
        }

    }

    @CoreMethod(names = "rb_enc_precise_mbclen", onSingleton = true, required = 4, lowerFixnum = { 3, 4 })
    public abstract static class RbEncPreciseMbclenNode extends CoreMethodArrayArgumentsNode {

        @Child private RopeNodes.CodeRangeNode codeRangeNode;

        @Specialization(guards = { "isRubyEncoding(enc)", "isRubyString(str)" })
        public Object rbEncPreciseMbclen(DynamicObject enc, DynamicObject str, int p, int end,
                @Cached("create()") RopeNodes.CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached("createBinaryProfile()") ConditionProfile sameEncodingProfile) {
            final Encoding encoding = EncodingOperations.getEncoding(enc);
            final Rope rope = StringOperations.rope(str);
            final CodeRange cr;
            if (sameEncodingProfile.profile(encoding == rope.getEncoding())) {
                cr = codeRange(rope);
            } else {
                cr = CodeRange.CR_UNKNOWN;
            }

            return calculateCharacterLengthNode.characterLength(encoding, cr, rope.getBytes(), p, end);
        }

        private CodeRange codeRange(Rope rope) {
            if (codeRangeNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                codeRangeNode = insert(RopeNodes.CodeRangeNode.create());
            }

            return codeRangeNode.execute(rope);
        }

    }

    @CoreMethod(names = "rb_tr_new_managed_struct", onSingleton = true, required = 1)
    public abstract static class RbTrNewManagedStructNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject newManagedStruct(Object type) {
            return ManagedStructObjectType.createManagedStruct(type);
        }

    }

    @CoreMethod(names = "rb_tr_wrap", onSingleton = true, required = 1)
    public abstract static class WrapValueNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public TruffleObject wrapInt(Object value,
                @Cached("createWrapNode()") WrapNode wrapNode) {
            return wrapNode.execute(value);
        }

        protected WrapNode createWrapNode() {
            return WrapNodeGen.create();
        }
    }

    @CoreMethod(names = "rb_tr_unwrap", onSingleton = true, required = 1)
    public abstract static class UnwrapValueNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object unwrap(TruffleObject value,
                @Cached("create()") BranchProfile exceptionProfile,
                @Cached("createUnwrapNode()") UnwrapNode unwrapNode) {
            Object object = unwrapNode.execute(value);
            if (object == null) {
                exceptionProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().runtimeError(exceptionMessage(value), this));
            } else {
                return object;
            }
        }

        @TruffleBoundary
        private String exceptionMessage(Object value) {
            return String.format("native handle not found (%s)", value);
        }

        protected UnwrapNode createUnwrapNode() {
            return UnwrapNodeGen.create();
        }
    }

    private static final ThreadLocal<ArrayList<Object>> markList = new ThreadLocal<>();

    @CoreMethod(names = "create_mark_list", onSingleton = true, required = 0)
    public abstract static class NewMarkerList extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject createNewMarkList(VirtualFrame frmae) {
            setThreadLocal();
            return nil();
        }

        @TruffleBoundary
        protected void setThreadLocal() {
            markList.set(new ArrayList<>());
        }
    }

    @CoreMethod(names = "rb_gc_mark", onSingleton = true, required = 1)
    public abstract static class AddToMarkList extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject addToMarkList(VirtualFrame frmae, TruffleObject markedObject,
                @Cached("create()") BranchProfile exceptionProfile,
                @Cached("create()") BranchProfile noExceptionProfile,
                @Cached("create()") UnwrapNode.ToWrapperNode toWrapperNode) {
            ValueWrapper wrappedValue = toWrapperNode.execute(markedObject);
            if (wrappedValue != null) {
                noExceptionProfile.enter();
                getList().add(wrappedValue);
            }
            // We do nothing here if the handle cannot be resolved. If we are marking an object
            // which is only reachable via weak refs then the handles of objects it is iteself
            // marking may have already been removed from the handle map. }
            return nil();
        }

        @TruffleBoundary
        protected ArrayList<Object> getList() {
            return markList.get();
        }

        protected UnwrapNode createUnwrapNode() {
            return UnwrapNodeGen.create();
        }
    }

    @CoreMethod(names = "rb_tr_gc_guard", onSingleton = true, required = 1)
    public abstract static class GCGuardNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject addToMarkList(VirtualFrame frame, TruffleObject guardedObject,
                @Cached("create()") MarkingServiceNodes.KeepAliveNode keepAliveNode,
                @Cached("create()") BranchProfile noExceptionProfile,
                @Cached("create()") UnwrapNode.ToWrapperNode toWrapperNode) {
            ValueWrapper wrappedValue = toWrapperNode.execute(guardedObject);
            if (wrappedValue != null) {
                noExceptionProfile.enter();
                keepAliveNode.execute(frame, guardedObject);
            }
            return nil();
        }

        @Fallback
        public DynamicObject addToMarkList(VirtualFrame frmae, Object guardedObject) {
            // Do nothing for unexpected objects, no matter how unexpected. This can occur inside
            // macros that guard a variable which may not have been initialized.
            return nil();
        }

        protected UnwrapNode createUnwrapNode() {
            return UnwrapNodeGen.create();
        }
    }

    @CoreMethod(names = "set_mark_list_on_object", onSingleton = true, required = 1)
    public abstract static class SetMarkList extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject setMarkList(VirtualFrame frame, DynamicObject structOwwer,
                @Cached("createWriter()") WriteObjectFieldNode writeMarkedNode) {
            writeMarkedNode.write(structOwwer, getArray());
            return nil();
        }

        @TruffleBoundary
        protected Object[] getArray() {
            return markList.get().toArray();
        }

        public WriteObjectFieldNode createWriter() {
            return WriteObjectFieldNodeGen.create(Layouts.MARKED_OBJECTS_IDENTIFIER);
        }
    }

    @CoreMethod(names = "define_marker", onSingleton = true, required = 2)
    public abstract static class CreateMarkerNode extends CoreMethodArrayArgumentsNode {

        @Child private DoesRespondDispatchHeadNode respondToCallNode = DoesRespondDispatchHeadNode.create();

        @Specialization
        public DynamicObject createMarker(VirtualFrame frame, DynamicObject object, DynamicObject marker,
                @Cached("create()") BranchProfile errorProfile) {
            if (respondToCallNode.doesRespondTo(frame, "call", marker)) {
                addObjectToMarkingService(object, marker);
                return nil();
            } else {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().argumentErrorWrongArgumentType(marker, "callable", this));
            }
        }

        @TruffleBoundary
        protected void addObjectToMarkingService(DynamicObject object, DynamicObject marker) {
            RubyContext aContext = getContext();
            /*
             * The code here has to be a little subtle. The marker must be associated with the
             * object it will act on, but the lambda must not capture the object (and prevent
             * garbage collection). So the marking function is a lambda that will take the
             * object as an argument 'o' which will be provided when the marking function is
             * called by the marking service.
             */
            getContext().getMarkingService().addMarker(object, (o) -> aContext.send(marker, "call", o));
        }
    }

    @CoreMethod(names = "push_preserving_frame", onSingleton = true, required = 0)
    public abstract static class PushPreservingFrame extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject pushFrame(VirtualFrame frame,
                @Cached("create()") MarkingServiceNodes.GetMarkerThreadLocalDataNode getDataNode) {
            getDataNode.execute(frame).getPreservationStack().push();
            return nil();
        }
    }

    @CoreMethod(names = "pop_preserving_frame", onSingleton = true, required = 0)
    public abstract static class PopPreservingFrame extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject popFrame(VirtualFrame frame,
                @Cached("create()") MarkingServiceNodes.GetMarkerThreadLocalDataNode getDataNode) {
            getDataNode.execute(frame).getPreservationStack().pop();
            return nil();
        }
    }

}
