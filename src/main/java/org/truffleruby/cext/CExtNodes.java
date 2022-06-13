/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.cext;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

import com.oracle.truffle.api.TruffleSafepoint;
import com.oracle.truffle.api.object.DynamicObjectLibrary;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.MutableTruffleString;
import com.oracle.truffle.api.strings.TruffleString;
import com.oracle.truffle.api.strings.TruffleString.GetByteCodeRangeNode;
import org.jcodings.Encoding;
import org.jcodings.IntHolder;
import org.truffleruby.Layouts;
import org.truffleruby.RubyLanguage;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.YieldingCoreMethodNode;
import org.truffleruby.cext.CExtNodesFactory.CallWithCExtLockNodeFactory;
import org.truffleruby.cext.CExtNodesFactory.StringToNativeNodeGen;
import org.truffleruby.cext.UnwrapNode.UnwrapCArrayNode;
import org.truffleruby.core.MarkingService.ExtensionCallStack;
import org.truffleruby.core.MarkingServiceNodes;
import org.truffleruby.core.array.ArrayToObjectArrayNode;
import org.truffleruby.core.array.ArrayUtils;
import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.cast.HashCastNode;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.exception.ErrnoErrorNode;
import org.truffleruby.core.exception.ExceptionOperations;
import org.truffleruby.core.format.BytesResult;
import org.truffleruby.core.format.FormatExceptionTranslator;
import org.truffleruby.core.format.exceptions.FormatException;
import org.truffleruby.core.format.exceptions.InvalidFormatException;
import org.truffleruby.core.format.rbsprintf.RBSprintfCompiler;
import org.truffleruby.core.hash.HashingNodes;
import org.truffleruby.core.hash.RubyHash;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.core.module.ModuleNodes.ConstSetUncheckedNode;
import org.truffleruby.core.module.ModuleNodes.SetMethodVisibilityNode;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.core.mutex.MutexOperations;
import org.truffleruby.core.numeric.BigIntegerOps;
import org.truffleruby.core.numeric.BignumOperations;
import org.truffleruby.core.numeric.FixnumOrBignumNode;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.rope.Bytes;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.NativeRope;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeOperations;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.StringCachingGuards;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringSupport;
import org.truffleruby.core.support.TypeNodes;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.core.thread.ThreadManager;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.extra.ffi.RubyPointer;
import org.truffleruby.interop.InteropNodes;
import org.truffleruby.interop.ToJavaStringNode;
import org.truffleruby.interop.TranslateInteropExceptionNode;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.LexicalScope;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.ArgumentsDescriptor;
import org.truffleruby.language.arguments.EmptyArgumentsDescriptor;
import org.truffleruby.language.arguments.KeywordArgumentsDescriptor;
import org.truffleruby.language.arguments.KeywordArgumentsDescriptorManager;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.constants.GetConstantNode;
import org.truffleruby.language.constants.LookupConstantNode;
import org.truffleruby.language.control.BreakException;
import org.truffleruby.language.control.BreakID;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.DispatchNode;
import org.truffleruby.language.dispatch.LiteralCallNode;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.AllocationTracing;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.supercall.CallSuperMethodNode;
import org.truffleruby.language.yield.CallBlockNode;
import org.truffleruby.parser.IdentifierType;
import org.truffleruby.utils.Utils;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.ReportPolymorphism;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.IndirectCallNode;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.source.SourceSection;

import static com.oracle.truffle.api.strings.TruffleString.CodeRange.BROKEN;

@CoreModule("Truffle::CExt")
public class CExtNodes {

    public static Pointer newNativeStringPointer(int capacity, RubyLanguage language) {
        return Pointer.mallocAutoRelease(capacity + 1, language);
    }

    private static long getNativeStringCapacity(Pointer pointer) {
        final long nativeBufferSize = pointer.getSize();
        assert nativeBufferSize > 0;
        // Do not count the extra byte for \0, like MRI.
        return nativeBufferSize - 1;
    }

    @Primitive(name = "call_with_c_mutex_and_frame")
    public abstract static class CallWithCExtLockAndFrameNode extends PrimitiveArrayArgumentsNode {

        @Child protected CallWithCExtLockNode callCextNode = CallWithCExtLockNodeFactory.create(RubyNode.EMPTY_ARRAY);

        @Specialization
        protected Object callWithCExtLockAndFrame(
                VirtualFrame frame, Object receiver, RubyArray argsArray, Object specialVariables, Object block) {
            final ExtensionCallStack extensionStack = getLanguage()
                    .getCurrentThread()
                    .getCurrentFiber().extensionCallStack;
            final boolean keywordsGiven = RubyArguments.getDescriptor(frame) instanceof KeywordArgumentsDescriptor;
            extensionStack.push(keywordsGiven, specialVariables, block);
            try {
                return callCextNode.execute(receiver, argsArray);
            } finally {
                extensionStack.pop();
            }
        }
    }

    @Primitive(name = "call_with_c_mutex_and_frame_and_unwrap")
    public abstract static class CallWithCExtLockAndFrameAndUnwrapNode extends PrimitiveArrayArgumentsNode {

        @Specialization(limit = "getCacheLimit()")
        protected Object callWithCExtLockAndFrame(
                VirtualFrame frame, Object receiver, RubyArray argsArray, Object specialVariables, Object block,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ArrayToObjectArrayNode arrayToObjectArrayNode,
                @Cached TranslateInteropExceptionNode translateInteropExceptionNode,
                @Cached ConditionProfile ownedProfile,
                @Cached UnwrapNode unwrapNode) {
            final ExtensionCallStack extensionStack = getLanguage()
                    .getCurrentThread()
                    .getCurrentFiber().extensionCallStack;
            final boolean keywordsGiven = RubyArguments.getDescriptor(frame) instanceof KeywordArgumentsDescriptor;
            extensionStack.push(keywordsGiven, specialVariables, block);
            try {
                final Object[] args = arrayToObjectArrayNode.executeToObjectArray(argsArray);

                if (getContext().getOptions().CEXT_LOCK) {
                    final ReentrantLock lock = getContext().getCExtensionsLock();
                    boolean owned = ownedProfile.profile(lock.isHeldByCurrentThread());

                    if (!owned) {
                        MutexOperations.lockInternal(getContext(), lock, this);
                    }
                    try {
                        return unwrapNode.execute(
                                InteropNodes.execute(receiver, args, receivers, translateInteropExceptionNode));
                    } finally {
                        if (!owned) {
                            MutexOperations.unlockInternal(lock);
                        }
                    }
                } else {
                    return unwrapNode
                            .execute(InteropNodes.execute(receiver, args, receivers, translateInteropExceptionNode));
                }

            } finally {
                extensionStack.pop();
            }
        }

        protected int getCacheLimit() {
            return getLanguage().options.DISPATCH_CACHE;
        }
    }

    @Primitive(name = "call_with_c_mutex")
    public abstract static class CallWithCExtLockNode extends PrimitiveArrayArgumentsNode {

        public abstract Object execute(Object receiver, RubyArray argsArray);

        @Specialization(limit = "getCacheLimit()")
        protected Object callWithCExtLock(Object receiver, RubyArray argsArray,
                @CachedLibrary("receiver") InteropLibrary receivers,
                @Cached ArrayToObjectArrayNode arrayToObjectArrayNode,
                @Cached TranslateInteropExceptionNode translateInteropExceptionNode,
                @Cached ConditionProfile ownedProfile) {
            final Object[] args = arrayToObjectArrayNode.executeToObjectArray(argsArray);

            if (getContext().getOptions().CEXT_LOCK) {
                final ReentrantLock lock = getContext().getCExtensionsLock();
                boolean owned = ownedProfile.profile(lock.isHeldByCurrentThread());

                if (!owned) {
                    MutexOperations.lockInternal(getContext(), lock, this);
                }
                try {
                    return InteropNodes.execute(receiver, args, receivers, translateInteropExceptionNode);
                } finally {
                    if (!owned) {
                        MutexOperations.unlockInternal(lock);
                    }
                }
            } else {
                return InteropNodes.execute(receiver, args, receivers, translateInteropExceptionNode);
            }

        }

        protected int getCacheLimit() {
            return getLanguage().options.DISPATCH_CACHE;
        }
    }

    public abstract static class SendWithoutCExtLockBaseNode extends PrimitiveArrayArgumentsNode {
        public Object sendWithoutCExtLock(VirtualFrame frame, Object receiver, RubySymbol method, Object block,
                ArgumentsDescriptor descriptor, Object[] args,
                DispatchNode dispatchNode, ConditionProfile ownedProfile) {
            if (getContext().getOptions().CEXT_LOCK) {
                final ReentrantLock lock = getContext().getCExtensionsLock();
                boolean owned = ownedProfile.profile(lock.isHeldByCurrentThread());

                if (owned) {
                    MutexOperations.unlockInternal(lock);
                }
                try {
                    return dispatchNode.callWithFrameAndBlock(frame, receiver, method.getString(), block,
                            descriptor, args);
                } finally {
                    if (owned) {
                        MutexOperations.internalLockEvenWithException(getContext(), lock, this);
                    }
                }
            } else {
                return dispatchNode.callWithFrameAndBlock(frame, receiver, method.getString(), block, descriptor, args);
            }
        }
    }

    @Primitive(name = "send_without_cext_lock")
    public abstract static class SendWithoutCExtLockNode extends SendWithoutCExtLockBaseNode {
        @Specialization
        protected Object sendWithoutCExtLock(
                VirtualFrame frame, Object receiver, RubySymbol method, RubyArray argsArray, Object block,
                @Cached ArrayToObjectArrayNode arrayToObjectArrayNode,
                @Cached DispatchNode dispatchNode,
                @Cached ConditionProfile ownedProfile) {
            final Object[] args = arrayToObjectArrayNode.executeToObjectArray(argsArray);
            return sendWithoutCExtLock(frame, receiver, method, block, EmptyArgumentsDescriptor.INSTANCE, args,
                    dispatchNode, ownedProfile);
        }

    }

    @Primitive(name = "send_argv_without_cext_lock")
    public abstract static class SendARGVWithoutCExtLockNode extends SendWithoutCExtLockBaseNode {
        @Specialization
        protected Object sendWithoutCExtLock(
                VirtualFrame frame, Object receiver, RubySymbol method, Object argv, Object block,
                @Cached UnwrapCArrayNode unwrapCArrayNode,
                @Cached DispatchNode dispatchNode,
                @Cached ConditionProfile ownedProfile) {
            final Object[] args = unwrapCArrayNode.execute(argv);
            return sendWithoutCExtLock(frame, receiver, method, block, EmptyArgumentsDescriptor.INSTANCE, args,
                    dispatchNode, ownedProfile);
        }
    }

    @Primitive(name = "send_argv_keywords_without_cext_lock")
    public abstract static class SendARGVKeywordsWithoutCExtLockNode extends SendWithoutCExtLockBaseNode {
        @Specialization
        protected Object sendWithoutCExtLock(
                VirtualFrame frame, Object receiver, RubySymbol method, Object argv, Object block,
                @Cached UnwrapCArrayNode unwrapCArrayNode,
                @Cached HashCastNode hashCastNode,
                @Cached ConditionProfile emptyProfile,
                @Cached DispatchNode dispatchNode,
                @Cached ConditionProfile ownedProfile) {
            Object[] args = unwrapCArrayNode.execute(argv);

            // Remove empty kwargs in the caller, so the callee does not need to care about this special case
            final RubyHash keywords = hashCastNode.execute(ArrayUtils.getLast(args));
            if (emptyProfile.profile(keywords.empty())) {
                args = LiteralCallNode.removeEmptyKeywordArguments(args);
                return sendWithoutCExtLock(frame, receiver, method, block, EmptyArgumentsDescriptor.INSTANCE, args,
                        dispatchNode, ownedProfile);
            } else {
                return sendWithoutCExtLock(frame, receiver, method, block,
                        KeywordArgumentsDescriptorManager.EMPTY, args,
                        dispatchNode, ownedProfile);
            }
        }
    }

    @Primitive(name = "public_send_argv_without_cext_lock", lowerFixnum = 2)
    public abstract static class PublicSendARGVWithoutCExtLockNode extends SendWithoutCExtLockBaseNode {
        @Specialization
        protected Object publicSendWithoutLock(
                VirtualFrame frame, Object receiver, RubySymbol method, Object argv, Object block,
                @Cached UnwrapCArrayNode unwrapCArrayNode,
                @Cached(parameters = "PUBLIC") DispatchNode dispatchNode,
                @Cached ConditionProfile ownedProfile) {
            final Object[] args = unwrapCArrayNode.execute(argv);
            return sendWithoutCExtLock(frame, receiver, method, block, EmptyArgumentsDescriptor.INSTANCE, args,
                    dispatchNode, ownedProfile);
        }
    }

    @CoreMethod(names = "cext_start_new_handle_block", onSingleton = true)
    public abstract static class StartNewHandleBlockNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected boolean isCExtLockOwned() {
            ValueWrapperManager.allocateNewBlock(getContext(), getLanguage());
            return true;
        }
    }

    @CoreMethod(names = "cext_lock_owned?", onSingleton = true)
    public abstract static class IsCExtLockOwnedNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected boolean isCExtLockOwned() {
            final ReentrantLock lock = getContext().getCExtensionsLock();
            return lock.isHeldByCurrentThread();
        }
    }

    @CoreMethod(names = "rb_ulong2num", onSingleton = true, required = 1)
    public abstract static class ULong2NumNode extends CoreMethodArrayArgumentsNode {

        private static final BigInteger TWO_POW_64 = BigInteger.valueOf(1).shiftLeft(64);

        @Specialization
        protected Object ulong2num(long num,
                @Cached ConditionProfile positiveProfile) {
            if (positiveProfile.profile(num >= 0)) {
                return num;
            } else {
                return BignumOperations.createBignum(toUnsigned(num));
            }
        }

        private BigInteger toUnsigned(long num) {
            return BigIntegerOps.add(TWO_POW_64, num);
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
        protected RubyArray bytes(
                int num, int num_words, int word_length, boolean msw_first, boolean twosComp, boolean bigEndian) {
            BigInteger bi = BigInteger.valueOf(num);
            return bytes(bi, num_words, word_length, msw_first, twosComp, bigEndian);
        }

        @Specialization
        @TruffleBoundary
        protected RubyArray bytes(
                long num, int num_words, int word_length, boolean msw_first, boolean twosComp, boolean bigEndian) {
            BigInteger bi = BigInteger.valueOf(num);
            return bytes(bi, num_words, word_length, msw_first, twosComp, bigEndian);
        }

        @Specialization
        @TruffleBoundary
        protected RubyArray bytes(
                RubyBignum num,
                int num_words,
                int word_length,
                boolean msw_first,
                boolean twosComp,
                boolean bigEndian) {
            BigInteger bi = num.value;
            return bytes(bi, num_words, word_length, msw_first, twosComp, bigEndian);
        }

        private RubyArray bytes(BigInteger bi, int num_words, int word_length, boolean msw_first, boolean twosComp,
                boolean bigEndian) {
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
            return createArray(bytes);
        }


    }

    @CoreMethod(names = "rb_absint_bit_length", onSingleton = true, required = 1)
    public abstract static class BignumAbsBitLengthNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        @TruffleBoundary
        protected int bitLength(int num) {
            return BigInteger.valueOf(num).abs().bitLength();
        }

        @Specialization
        @TruffleBoundary
        protected int bitLength(long num) {
            return BigInteger.valueOf(num).abs().bitLength();
        }

        @Specialization
        @TruffleBoundary
        protected int bitLength(RubyBignum num) {
            return num.value.abs().bitLength();
        }
    }

    @CoreMethod(names = "rb_2scomp_bit_length", onSingleton = true, required = 1)
    public abstract static class Bignum2sCompBitLengthNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        @TruffleBoundary
        protected int bitLength(int num) {
            return BigInteger.valueOf(num).bitLength();
        }

        @Specialization
        @TruffleBoundary
        protected int bitLength(long num) {
            return BigInteger.valueOf(num).bitLength();
        }

        @Specialization
        @TruffleBoundary
        protected int bitLength(RubyBignum num) {
            return num.value.bitLength();
        }
    }

    @Primitive(name = "rb_int_singlebit_p")
    public abstract static class IntSinglebitPPrimitiveNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int intSinglebitP(int num) {
            assert num >= 0;
            return Integer.bitCount(num) == 1 ? 1 : 0;
        }

        @Specialization
        protected int intSinglebitP(long num) {
            assert num >= 0;
            return Long.bitCount(num) == 1 ? 1 : 0;
        }

        @Specialization
        @TruffleBoundary
        protected int intSinglebitP(RubyBignum num) {
            assert num.value.signum() >= 0;
            return num.value.bitCount() == 1 ? 1 : 0;
        }
    }

    @CoreMethod(names = "DBL2BIG", onSingleton = true, required = 1)
    public abstract static class DBL2BIGNode extends CoreMethodArrayArgumentsNode {

        @Child private FixnumOrBignumNode fixnumOrBignum = new FixnumOrBignumNode();

        @Specialization
        @TruffleBoundary
        protected Object dbl2big(double num,
                @Cached BranchProfile errorProfile) {
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

    @CoreMethod(names = "rb_long2int", onSingleton = true, required = 1)
    public abstract static class Long2Int extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int long2fix(int num) {
            return num;
        }

        @Specialization(guards = "fitsInInteger(num)")
        protected int long2fixInRange(long num) {
            return (int) num;
        }

        @Specialization(guards = "!fitsInInteger(num)")
        protected int long2fixOutOfRange(long num) {
            throw new RaiseException(getContext(), coreExceptions().rangeErrorConvertToInt(num, this));
        }
    }

    @CoreMethod(names = "rb_enc_coderange_clear", onSingleton = true, required = 1)
    public abstract static class RbEncCodeRangeClear extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString clearCodeRange(RubyString string,
                @Cached StringToNativeNode stringToNativeNode) {
            stringToNativeNode.executeToNative(string);
            string.clearCodeRange();

            return string;
        }

    }

    @CoreMethod(names = "code_to_mbclen", onSingleton = true, required = 2, lowerFixnum = 1)
    public abstract static class CodeToMbcLenNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int codeToMbcLen(int code, RubyEncoding encoding) {
            return StringSupport.codeLength(encoding.jcoding, code);
        }

    }

    @CoreMethod(names = "rb_enc_codepoint_len", onSingleton = true, required = 2)
    public abstract static class RbEncCodePointLenNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "strings.isRubyString(string)")
        protected RubyArray rbEncCodePointLen(Object string, RubyEncoding encoding,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached RopeNodes.CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached GetByteCodeRangeNode codeRangeNode,
                @Cached ConditionProfile sameEncodingProfile,
                @Cached BranchProfile errorProfile) {
            final Rope rope = strings.getRope(string);
            var tstring = strings.getTString(string);
            final byte[] bytes = bytesNode.execute(rope);
            var stringCodeRange = codeRangeNode.execute(tstring, strings.getTEncoding(string));
            final Encoding enc = encoding.jcoding;

            final TruffleString.CodeRange cr;
            if (sameEncodingProfile.profile(enc == rope.getEncoding())) {
                cr = stringCodeRange;
            } else {
                cr = BROKEN /* UNKNOWN */;
            }

            final int r = calculateCharacterLengthNode.characterLength(enc, cr, new Bytes(bytes));

            if (!StringSupport.MBCLEN_CHARFOUND_P(r)) {
                errorProfile.enter();
                throw new RaiseException(
                        getContext(),
                        coreExceptions().argumentError(Utils.concat("invalid byte sequence in ", enc), this));
            }

            final int len_p = StringSupport.MBCLEN_CHARFOUND_LEN(r);
            final int codePoint = StringSupport.preciseCodePoint(enc, stringCodeRange, bytes, 0, bytes.length);

            return createArray(new Object[]{ len_p, codePoint });
        }
    }

    @CoreMethod(names = "rb_enc_isalnum", onSingleton = true, required = 2, lowerFixnum = 1)
    public abstract static class RbEncIsAlNumNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected boolean rbEncIsAlNum(int code, RubyEncoding value) {
            return value.jcoding.isAlnum(code);
        }

    }

    @CoreMethod(names = "rb_enc_isspace", onSingleton = true, required = 2, lowerFixnum = 1)
    public abstract static class RbEncIsSpaceNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected boolean rbEncIsSpace(int code, RubyEncoding value) {
            return value.jcoding.isSpace(code);
        }

    }

    @CoreMethod(names = "rb_str_new_nul", onSingleton = true, required = 1, lowerFixnum = 1)
    public abstract static class RbStrNewNulNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString rbStrNewNul(int byteLength,
                @Cached MutableTruffleString.FromNativePointerNode fromNativePointerNode) {
            final Pointer pointer = Pointer.callocAutoRelease(byteLength + 1, getLanguage());
            var nativeTString = fromNativePointerNode.execute(pointer, 0, byteLength, Encodings.BINARY.tencoding,
                    false);
            return createMutableString(nativeTString, Encodings.BINARY);
        }

    }

    @CoreMethod(names = "rb_str_capacity", onSingleton = true, required = 1)
    public abstract static class RbStrCapacityNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected long capacity(Object string,
                @Cached StringToNativeNode stringToNativeNode) {
            return getNativeStringCapacity(stringToNativeNode.executeToNative(string));
        }
    }

    @CoreMethod(names = "rb_str_set_len", onSingleton = true, required = 2, lowerFixnum = 2)
    public abstract static class RbStrSetLenNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString strSetLen(RubyString string, int newByteLength,
                @Cached StringToNativeNode stringToNativeNode,
                @Cached MutableTruffleString.FromNativePointerNode fromNativePointerNode) {
            var pointer = stringToNativeNode.executeToNative(string);

            pointer.writeByte(newByteLength, (byte) 0); // Like MRI

            var newNativeTString = fromNativePointerNode.execute(pointer, 0, newByteLength, string.getTEncoding(),
                    false);
            string.setTString(newNativeTString);

            return string;
        }
    }

    @CoreMethod(names = "rb_str_resize", onSingleton = true, required = 2, lowerFixnum = 2)
    public abstract static class RbStrResizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString rbStrResize(RubyString string, int newByteLength,
                @Cached StringToNativeNode stringToNativeNode,
                @Cached MutableTruffleString.FromNativePointerNode fromNativePointerNode) {
            var pointer = stringToNativeNode.executeToNative(string);
            var tencoding = string.getTEncoding();
            int byteLength = string.tstring.byteLength(tencoding);

            if (byteLength == newByteLength) {
                // Like MRI's rb_str_resize()
                string.clearCodeRange();
                return string;
            } else {
                var newNativeTString = TrStrCapaResizeNode.resize(pointer, newByteLength, newByteLength, tencoding,
                        fromNativePointerNode, getLanguage());
                string.setTString(newNativeTString);

                // Like MRI's rb_str_resize()
                string.clearCodeRange();

                return string;
            }
        }
    }

    @CoreMethod(names = "rb_tr_str_capa_resize", onSingleton = true, required = 2, lowerFixnum = 2)
    public abstract static class TrStrCapaResizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString trStrCapaResize(RubyString string, int newCapacity,
                @Cached StringToNativeNode stringToNativeNode,
                @Cached MutableTruffleString.FromNativePointerNode fromNativePointerNode) {
            var pointer = stringToNativeNode.executeToNative(string);
            var tencoding = string.getTEncoding();

            if (getNativeStringCapacity(pointer) == newCapacity) {
                return string;
            } else {
                int byteLength = string.tstring.byteLength(tencoding);
                var newNativeTString = resize(pointer, newCapacity, byteLength, tencoding, fromNativePointerNode,
                        getLanguage());
                string.setTString(newNativeTString);

                return string;
            }
        }

        static MutableTruffleString resize(Pointer pointer, int newCapacity, int newByteLength,
                TruffleString.Encoding tencoding, MutableTruffleString.FromNativePointerNode fromNativePointerNode,
                RubyLanguage language) {
            final Pointer newPointer = newNativeStringPointer(newCapacity, language);
            newPointer.writeBytes(0, pointer, 0, Math.min(pointer.getSize(), newCapacity));
            newPointer.writeByte(newCapacity, (byte) 0); // Like MRI

            return fromNativePointerNode.execute(newPointer, 0, newByteLength, tencoding, false);
        }
    }

    @CoreMethod(names = "rb_keyword_given_p", onSingleton = true)
    public abstract static class RbKeywordGivenNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean keywordGiven() {
            return getLanguage().getCurrentThread().getCurrentFiber().extensionCallStack.areKeywordsGiven();
        }
    }

    @CoreMethod(names = "rb_block_proc", onSingleton = true)
    public abstract static class BlockProcNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object block() {
            return getLanguage().getCurrentThread().getCurrentFiber().extensionCallStack.getBlock();
        }
    }

    @Primitive(name = "cext_special_variables_from_stack")
    public abstract static class VarsFromStackNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object variables() {
            return getLanguage().getCurrentThread().getCurrentFiber().extensionCallStack.getSpecialVariables();
        }
    }

    @CoreMethod(names = "rb_check_frozen", onSingleton = true, required = 1)
    public abstract static class CheckFrozenNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean rb_check_frozen(Object object,
                @Cached TypeNodes.CheckFrozenNode raiseIfFrozenNode) {
            raiseIfFrozenNode.execute(object);
            return true;
        }

    }

    @CoreMethod(names = "rb_str_locktmp", onSingleton = true, required = 1)
    public abstract static class RbStrLockTmpNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString rbStrLockTmp(RubyString string,
                @Cached BranchProfile errorProfile) {
            if (string.locked) {
                errorProfile.enter();
                throw new RaiseException(getContext(),
                        coreExceptions().runtimeError("temporal locking already locked string", this));
            }
            string.locked = true;
            return string;
        }

        @Specialization
        protected RubyString rbStrLockTmpImmutable(ImmutableRubyString string) {
            throw new RaiseException(getContext(),
                    coreExceptions().runtimeError("temporal locking immutable string", this));
        }

    }

    @CoreMethod(names = "rb_str_unlocktmp", onSingleton = true, required = 1)
    public abstract static class RbStrUnlockTmpNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyString rbStrUnlockTmp(RubyString string,
                @Cached BranchProfile errorProfile) {
            if (!string.locked) {
                errorProfile.enter();
                throw new RaiseException(getContext(),
                        coreExceptions().runtimeError("temporal unlocking already unlocked string", this));
            }
            string.locked = false;
            return string;
        }

        @Specialization
        protected ImmutableRubyString rbStrUnlockTmpImmutable(ImmutableRubyString string,
                @Cached BranchProfile errorProfile) {
            throw new RaiseException(getContext(),
                    coreExceptions().runtimeError("temporal unlocking immutable string", this));
        }

    }

    @CoreMethod(names = "rb_const_get", onSingleton = true, required = 2)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    public abstract static class RbConstGetNode extends CoreMethodNode {

        @Child private LookupConstantNode lookupConstantNode = LookupConstantNode.create(true, true);
        @Child private GetConstantNode getConstantNode = GetConstantNode.create();

        @CreateCast("name")
        protected RubyNode coerceToString(RubyNode name) {
            return ToJavaStringNode.create(name);
        }

        @Specialization
        protected Object rbConstGet(RubyModule module, String name) {
            return getConstantNode
                    .lookupAndResolveConstant(LexicalScope.IGNORE, module, name, false, lookupConstantNode);
        }

    }

    @CoreMethod(names = "rb_const_get_from", onSingleton = true, required = 2)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    public abstract static class RbConstGetFromNode extends CoreMethodNode {

        @Child private LookupConstantNode lookupConstantNode = LookupConstantNode.create(true, false);
        @Child private GetConstantNode getConstantNode = GetConstantNode.create();

        @CreateCast("name")
        protected RubyNode coerceToString(RubyNode name) {
            return ToJavaStringNode.create(name);
        }

        @Specialization
        protected Object rbConstGetFrom(RubyModule module, String name) {
            return getConstantNode
                    .lookupAndResolveConstant(LexicalScope.IGNORE, module, name, false, lookupConstantNode);
        }

    }

    @CoreMethod(names = "rb_const_set", onSingleton = true, required = 3)
    @NodeChild(value = "module", type = RubyNode.class)
    @NodeChild(value = "name", type = RubyNode.class)
    @NodeChild(value = "value", type = RubyNode.class)
    public abstract static class RbConstSetNode extends CoreMethodNode {

        @CreateCast("name")
        protected RubyNode coerceToString(RubyNode name) {
            return ToJavaStringNode.create(name);
        }

        @Specialization
        protected Object rbConstSet(RubyModule module, String name, Object value,
                @Cached ConstSetUncheckedNode constSetUncheckedNode) {
            return constSetUncheckedNode.execute(module, name, value);
        }

    }

    @CoreMethod(names = "cext_module_function", onSingleton = true, required = 2)
    public abstract static class CextModuleFunctionNode extends CoreMethodArrayArgumentsNode {

        @Child SetMethodVisibilityNode setMethodVisibilityNode = SetMethodVisibilityNode.create();

        @Specialization
        protected RubyModule cextModuleFunction(RubyModule module, RubySymbol name) {
            setMethodVisibilityNode.execute(module, name, Visibility.MODULE_FUNCTION);
            return module;
        }

    }

    @CoreMethod(names = "caller_frame_visibility", onSingleton = true, required = 1)
    public abstract static class CallerFrameVisibilityNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected boolean checkCallerVisibility(RubySymbol visibility) {
            final Frame callerFrame = getContext().getCallStack().getCallerFrame(FrameAccess.READ_ONLY);
            final Visibility callerVisibility = DeclarationContext.findVisibility(callerFrame);

            switch (visibility.getString()) {
                case "private":
                    return callerVisibility == Visibility.PRIVATE;
                case "protected":
                    return callerVisibility == Visibility.PROTECTED;
                case "module_function":
                    return callerVisibility == Visibility.MODULE_FUNCTION;
                default:
                    throw CompilerDirectives.shouldNotReachHere();
            }
        }
    }

    @CoreMethod(names = "rb_iter_break_value", onSingleton = true, required = 1)
    public abstract static class IterBreakValueNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object iterBreakValue(Object value) {
            throw new BreakException(BreakID.ANY_BLOCK, value);
        }

    }

    @CoreMethod(names = "rb_sourcefile", onSingleton = true)
    public abstract static class SourceFileNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode = StringNodes.MakeStringNode.create();

        @TruffleBoundary
        @Specialization
        protected RubyString sourceFile() {
            final SourceSection sourceSection = getTopUserSourceSection("rb_sourcefile");
            final String file = getLanguage().getSourcePath(sourceSection.getSource());

            return makeStringNode.executeMake(file, Encodings.UTF_8, CodeRange.CR_UNKNOWN);
        }

        @TruffleBoundary
        public static SourceSection getTopUserSourceSection(String... methodNames) {
            return Truffle.getRuntime().iterateFrames(frameInstance -> {
                final Node callNode = frameInstance.getCallNode();

                if (callNode != null) {
                    final RootNode rootNode = callNode.getRootNode();

                    if (rootNode instanceof RubyRootNode && rootNode.getSourceSection().isAvailable() &&
                            !nameMatches(
                                    ((RubyRootNode) rootNode).getSharedMethodInfo().getMethodName(),
                                    methodNames)) {
                        return frameInstance.getCallNode().getEncapsulatingSourceSection();
                    }
                }

                return null;
            });
        }

        private static boolean nameMatches(String name, String... methodNames) {
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
        protected int sourceLine() {
            final SourceSection sourceSection = SourceFileNode.getTopUserSourceSection("rb_sourceline");
            return sourceSection.getStartLine();
        }

    }

    @CoreMethod(names = "rb_is_instance_id", onSingleton = true, required = 1)
    public abstract static class IsInstanceIdNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isInstanceId(RubySymbol symbol) {
            return symbol.getType() == IdentifierType.INSTANCE;
        }

    }

    @CoreMethod(names = "rb_is_const_id", onSingleton = true, required = 1)
    public abstract static class IsConstIdNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isConstId(RubySymbol symbol) {
            return symbol.getType() == IdentifierType.CONST;
        }

    }

    @CoreMethod(names = "rb_is_class_id", onSingleton = true, required = 1)
    public abstract static class IsClassVariableIdNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isClassVariableId(RubySymbol symbol) {
            return symbol.getType() == IdentifierType.CLASS;
        }

    }

    @CoreMethod(names = "rb_call_super_splatted", onSingleton = true, rest = true)
    public abstract static class CallSuperNode extends CoreMethodArrayArgumentsNode {

        @Child private CallSuperMethodNode callSuperMethodNode = CallSuperMethodNode.create();
        @Child private MetaClassNode metaClassNode = MetaClassNode.create();

        @Specialization
        protected Object callSuper(VirtualFrame frame, Object[] args) {
            final Frame callingMethodFrame = findCallingMethodFrame();
            final InternalMethod callingMethod = RubyArguments.getMethod(callingMethodFrame);
            final Object callingSelf = RubyArguments.getSelf(callingMethodFrame);
            final RubyClass callingMetaclass = metaClassNode.execute(callingSelf);
            final MethodLookupResult superMethodLookup = ModuleOperations
                    .lookupSuperMethod(callingMethod, callingMetaclass);
            final InternalMethod superMethod = superMethodLookup.getMethod();
            // This C API only passes positional arguments, but maybe it should be influenced by ruby2_keywords hashes?
            return callSuperMethodNode.execute(
                    frame, callingSelf, superMethod, EmptyArgumentsDescriptor.INSTANCE, args, nil, null);
        }

        @TruffleBoundary
        private static Frame findCallingMethodFrame() {
            return Truffle.getRuntime().iterateFrames(frameInstance -> {
                final Frame frame = frameInstance.getFrame(FrameAccess.READ_ONLY);

                final InternalMethod method = RubyArguments.tryGetMethod(frame);

                if (method == null) {
                    return null;
                } else if (method.getName().equals(/* Truffle::CExt. */ "rb_call_super") ||
                        method.getName().equals(/* Truffle::Interop. */ "execute_without_conversion") ||
                        method.getName().equals(/* Truffle::CExt. */ "rb_call_super_splatted")) {
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
        protected Object frameThisFunc(VirtualFrame frame, Object[] args) {
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
                } else if (method.getName().equals(/* Truffle::CExt. */ "rb_frame_this_func") ||
                        method.getName().equals(/* Truffle::Interop */ "execute_without_conversion")) {
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

        @Specialization
        protected Object rbSysErrFail(int errno, Object string,
                @Cached ErrnoErrorNode errnoErrorNode) {
            final Backtrace backtrace = getContext().getCallStack().getBacktrace(this);
            throw new RaiseException(getContext(), errnoErrorNode.execute(null, errno, string, backtrace));
        }

    }

    @CoreMethod(names = "rb_hash", onSingleton = true, required = 1)
    public abstract static class RbHashNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int rbHash(Object object,
                @Cached HashingNodes.ToHashByHashCode toHashByHashCode) {
            return toHashByHashCode.execute(object);
        }
    }

    public abstract static class StringToNativeNode extends RubyBaseNode {

        public static StringToNativeNode create() {
            return StringToNativeNodeGen.create();
        }

        public abstract Pointer executeToNative(Object string);

        @Specialization
        protected Pointer toNative(RubyString string,
                @Cached ConditionProfile convertProfile,
                @Cached TruffleString.CopyToNativeMemoryNode copyToNativeMemoryNode,
                @Cached MutableTruffleString.FromNativePointerNode fromNativePointerNode,
                @Cached TruffleString.GetInternalNativePointerNode getInternalNativePointerNode) {
            var tstring = string.tstring;
            var tencoding = string.encoding.tencoding;

            final Pointer pointer;

            if (convertProfile.profile(tstring.isNative())) {
                assert tstring.isMutable();
                pointer = (Pointer) getInternalNativePointerNode.execute(tstring, tencoding);
            } else {
                int byteLength = tstring.byteLength(tencoding);
                pointer = allocateAndCopyToNative(tstring, tencoding, byteLength, copyToNativeMemoryNode,
                        getLanguage());

                var nativeTString = fromNativePointerNode.execute(pointer, 0, byteLength, tencoding, false);
                string.setTString(nativeTString);
            }

            return pointer;
        }

        @Specialization
        protected Pointer toNativeImmutable(ImmutableRubyString string) {
            return string.getNativeString(getLanguage());
        }

        public static Pointer allocateAndCopyToNative(AbstractTruffleString tstring, TruffleString.Encoding tencoding,
                int capacity, TruffleString.CopyToNativeMemoryNode copyToNativeMemoryNode, RubyLanguage language) {
            final Pointer pointer = newNativeStringPointer(capacity, language);
            copyToNativeMemoryNode.execute(tstring, 0, pointer, 0, capacity, tencoding);
            pointer.writeByte(capacity, (byte) 0); // Like MRI
            return pointer;
        }

    }

    @Primitive(name = "string_pointer_to_native")
    public abstract static class StringPointerToNativeNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected long toNative(Object string,
                @Cached StringToNativeNode stringToNativeNode) {
            return stringToNativeNode.executeToNative(string).getAddress();
        }
    }

    @CoreMethod(names = "string_to_ffi_pointer", onSingleton = true, required = 1)
    public abstract static class StringToFFIPointerNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyPointer toNative(Object string,
                @Cached StringToNativeNode stringToNativeNode) {
            var pointer = stringToNativeNode.executeToNative(string);

            final RubyPointer instance = new RubyPointer(
                    coreLibrary().truffleFFIPointerClass,
                    getLanguage().truffleFFIPointerShape,
                    pointer);
            AllocationTracing.trace(instance, this);
            return instance;
        }
    }

    @Primitive(name = "string_is_native?")
    public abstract static class StringPointerIsNativeNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean isNative(RubyString string) {
            return string.rope instanceof NativeRope;
        }

        @TruffleBoundary
        @Specialization
        protected boolean isNative(ImmutableRubyString string) {
            return string.isNative();
        }

    }

    @CoreMethod(names = "rb_tr_debug", onSingleton = true, rest = true)
    public abstract static class DebugNode extends CoreMethodArrayArgumentsNode {

        @Child DispatchNode toSCall;

        @TruffleBoundary
        @Specialization
        protected Object debug(Object... objects) {
            if (objects.length > 1) {
                System.err.printf("Printing %d values%n", objects.length);
            }

            final RubyStringLibrary libString = RubyStringLibrary.getUncached();
            for (Object object : objects) {
                final String representation;

                if (libString.isRubyString(object)) {
                    final Rope rope = libString.getRope(object);
                    final byte[] bytes = rope.getBytes();
                    final StringBuilder builder = new StringBuilder();

                    for (int i = 0; i < bytes.length; i++) {
                        if (i % 4 == 0 && i != 0 && i != bytes.length - 1) {
                            builder.append(" ");
                        }
                        builder.append(String.format("%02x", bytes[i]));
                    }

                    representation = RopeOperations.decodeRope(rope) + " (" + builder.toString() + ")";
                } else if (RubyGuards.isRubyValue(object)) {
                    representation = object.toString() + " (" +
                            RubyStringLibrary.getUncached().getJavaString(callToS(object)) + ")";
                } else {
                    representation = object.toString();
                }

                System.err.printf("%s @ %s: %s%n", object.getClass(), System.identityHashCode(object), representation);
            }
            return nil;
        }

        private Object callToS(Object object) {
            if (toSCall == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                toSCall = insert(DispatchNode.create());
            }

            return toSCall.call(object, "to_s");
        }

    }

    @CoreMethod(names = "capture_exception", onSingleton = true, needsBlock = true)
    public abstract static class CaptureExceptionNode extends YieldingCoreMethodNode {

        @Specialization
        protected Object executeWithProtect(RubyProc block,
                @Cached BranchProfile exceptionProfile,
                @Cached BranchProfile noExceptionProfile) {
            try {
                callBlock(block);
                noExceptionProfile.enter();
                return nil;
            } catch (Throwable e) {
                exceptionProfile.enter();
                return new CapturedException(e);
            }
        }
    }

    @CoreMethod(names = "extract_ruby_exception", onSingleton = true, required = 1)
    public abstract static class ExtractRubyException extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object executeThrow(CapturedException captured,
                @Cached ConditionProfile rubyExceptionProfile) {
            final Throwable e = captured.getException();
            if (rubyExceptionProfile.profile(e instanceof RaiseException)) {
                return ((RaiseException) e).getException();
            } else {
                return nil;
            }
        }
    }

    @CoreMethod(names = "raise_exception", onSingleton = true, required = 1)
    public abstract static class RaiseExceptionNode extends CoreMethodArrayArgumentsNode {

        /** Profiled version of {@link ExceptionOperations#rethrow(Throwable)} */
        @Specialization
        protected Object executeThrow(CapturedException captured,
                @Cached ConditionProfile runtimeExceptionProfile,
                @Cached ConditionProfile errorProfile) {
            final Throwable e = captured.getException();
            if (runtimeExceptionProfile.profile(e instanceof RuntimeException)) {
                throw (RuntimeException) e;
            } else if (errorProfile.profile(e instanceof Error)) {
                throw (Error) e;
            } else {
                throw CompilerDirectives.shouldNotReachHere("Checked Java Throwable rethrown", e);
            }
        }
    }

    @CoreMethod(names = "rb_tr_enc_mbc_case_fold", onSingleton = true, required = 5, lowerFixnum = 2)
    public abstract static class RbTrMbcCaseFoldNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "strings.isRubyString(string)", limit = "getCacheLimit()")
        protected Object rbTrEncMbcCaseFold(RubyEncoding enc, int flags, Object string, Object write_p, Object p,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @CachedLibrary("write_p") InteropLibrary receivers,
                @Cached RopeNodes.BytesNode getBytes,
                @Cached TranslateInteropExceptionNode translateInteropExceptionNode,
                @Cached TruffleString.FromByteArrayNode fromByteArrayNode) {
            final byte[] bytes = getBytes.execute(strings.getRope(string));
            final byte[] to = new byte[bytes.length];
            final IntHolder intHolder = new IntHolder();
            intHolder.value = 0;
            final int resultLength = enc.jcoding.mbcCaseFold(flags, bytes, intHolder, bytes.length, to);
            InteropNodes.execute(write_p, new Object[]{ p, intHolder.value }, receivers, translateInteropExceptionNode);
            final byte[] result = new byte[resultLength];
            if (resultLength > 0) {
                System.arraycopy(to, 0, result, 0, resultLength);
            }
            return createString(fromByteArrayNode, result, Encodings.US_ASCII);
        }

        protected int getCacheLimit() {
            return getLanguage().options.DISPATCH_CACHE;
        }
    }

    @CoreMethod(names = "rb_tr_code_to_mbc", onSingleton = true, required = 2, lowerFixnum = 2)
    public abstract static class RbTrMbcPutNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object rbTrEncMbcPut(RubyEncoding enc, int code,
                @Cached TruffleString.FromByteArrayNode fromByteArrayNode) {
            final Encoding encoding = enc.jcoding;
            final byte buf[] = new byte[org.jcodings.Config.ENC_CODE_TO_MBC_MAXLEN];
            final int resultLength = encoding.codeToMbc(code, buf, 0);
            final byte result[] = new byte[resultLength];
            if (resultLength > 0) {
                System.arraycopy(buf, 0, result, 0, resultLength);
            }
            return createString(fromByteArrayNode, result, Encodings.US_ASCII);
        }
    }

    @CoreMethod(names = "rb_enc_mbmaxlen", onSingleton = true, required = 1)
    public abstract static class RbEncMaxLenNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object rbEncMaxLen(RubyEncoding value) {
            return value.jcoding.maxLength();
        }
    }

    @CoreMethod(names = "rb_enc_mbminlen", onSingleton = true, required = 1)
    public abstract static class RbEncMinLenNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected Object rbEncMinLen(RubyEncoding value) {
            return value.jcoding.minLength();
        }
    }

    @CoreMethod(names = "rb_enc_mbclen", onSingleton = true, required = 4, lowerFixnum = { 3, 4 })
    public abstract static class RbEncMbLenNode extends CoreMethodArrayArgumentsNode {
        @Specialization(guards = "strings.isRubyString(string)")
        protected Object rbEncMbLen(RubyEncoding enc, Object string, int p, int e,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached RopeNodes.BytesNode getBytes,
                @Cached GetByteCodeRangeNode codeRangeNode,
                @Cached ConditionProfile sameEncodingProfile) {
            final Encoding encoding = enc.jcoding;
            final Rope rope = strings.getRope(string);
            final Encoding ropeEncoding = rope.getEncoding();

            return StringSupport.characterLength(
                    encoding,
                    sameEncodingProfile.profile(encoding == ropeEncoding)
                            ? codeRangeNode.execute(strings.getTString(string), strings.getTEncoding(string))
                            : BROKEN /* UNKNOWN */,
                    getBytes.execute(strings.getRope(string)),
                    p,
                    e,
                    true);
        }
    }

    @CoreMethod(names = "rb_enc_left_char_head", onSingleton = true, required = 5, lowerFixnum = { 3, 4, 5 })
    public abstract static class RbEncLeftCharHeadNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "strings.isRubyString(string)")
        protected Object rbEncLeftCharHead(RubyEncoding enc, Object string, int start, int p, int end,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            return enc.jcoding.leftAdjustCharHead(
                    strings.getRope(string).getBytes(),
                    start,
                    p,
                    end);
        }

    }

    @CoreMethod(names = "rb_enc_mbc_to_codepoint", onSingleton = true, required = 3, lowerFixnum = 3)
    public abstract static class RbEncMbcToCodepointNode extends CoreMethodArrayArgumentsNode {
        @Specialization(guards = "strings.isRubyString(string)")
        protected int rbEncMbcToCodepoint(RubyEncoding enc, Object string, int end,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            final Rope rope = strings.getRope(string);
            return StringSupport.mbcToCode(enc.jcoding, rope, 0, end);
        }
    }

    @CoreMethod(names = "rb_enc_precise_mbclen", onSingleton = true, required = 4, lowerFixnum = { 3, 4 })
    public abstract static class RbEncPreciseMbclenNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "strings.isRubyString(string)")
        protected int rbEncPreciseMbclen(RubyEncoding enc, Object string, int p, int end,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached RopeNodes.CalculateCharacterLengthNode calculateCharacterLengthNode,
                @Cached RopeNodes.GetBytesObjectNode getBytesObject,
                @Cached GetByteCodeRangeNode codeRangeNode,
                @Cached ConditionProfile sameEncodingProfile) {
            final Encoding encoding = enc.jcoding;
            final Rope rope = strings.getRope(string);
            final TruffleString.CodeRange cr;
            if (sameEncodingProfile.profile(encoding == rope.getEncoding())) {
                cr = codeRangeNode.execute(strings.getTString(string), strings.getTEncoding(string));
            } else {
                cr = BROKEN /* UNKNOWN */;
            }

            final int length = calculateCharacterLengthNode
                    .characterLength(encoding, cr, getBytesObject.getRange(rope, p, end));
            assert end - p >= length; // assert this condition not reached: https://github.com/ruby/ruby/blob/46a5d1b4a63f624f2c5c5b6f710cc1a176c88b02/encoding.c#L1046
            return length;
        }

    }

    @Primitive(name = "cext_wrap")
    public abstract static class WrapValueNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object wrapInt(Object value,
                @Cached WrapNode wrapNode) {
            return wrapNode.execute(value);
        }

    }

    @Primitive(name = "cext_unwrap")
    public abstract static class UnwrapValueNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object unwrap(Object value,
                @Cached BranchProfile exceptionProfile,
                @Cached UnwrapNode unwrapNode) {
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
    }

    @CoreMethod(names = "create_mark_list", onSingleton = true, required = 1)
    public abstract static class NewMarkerList extends CoreMethodArrayArgumentsNode {

        @Specialization(limit = "getDynamicObjectCacheLimit()")
        protected Object createNewMarkList(RubyDynamicObject object,
                @CachedLibrary("object") DynamicObjectLibrary objectLibrary) {
            getContext().getMarkingService().startMarking(
                    (Object[]) objectLibrary.getOrDefault(object, Layouts.MARKED_OBJECTS_IDENTIFIER, null));
            return nil;
        }
    }

    @CoreMethod(names = "rb_gc_mark", onSingleton = true, required = 1)
    public abstract static class AddToMarkList extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object addToMarkList(Object markedObject,
                @Cached BranchProfile noExceptionProfile,
                @Cached UnwrapNode.ToWrapperNode toWrapperNode) {
            ValueWrapper wrappedValue = toWrapperNode.execute(markedObject);
            if (wrappedValue != null) {
                noExceptionProfile.enter();
                getContext().getMarkingService().addMark(wrappedValue);
            }
            // We do nothing here if the handle cannot be resolved. If we are marking an object
            // which is only reachable via weak refs then the handles of objects it is itself
            // marking may have already been removed from the handle map.
            return nil;
        }

    }

    @CoreMethod(names = "rb_tr_gc_guard", onSingleton = true, required = 1)
    public abstract static class GCGuardNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object addToMarkList(Object guardedObject,
                @Cached MarkingServiceNodes.KeepAliveNode keepAliveNode,
                @Cached BranchProfile noExceptionProfile,
                @Cached UnwrapNode.ToWrapperNode toWrapperNode) {
            ValueWrapper wrappedValue = toWrapperNode.execute(guardedObject);
            if (wrappedValue != null) {
                noExceptionProfile.enter();
                keepAliveNode.execute(wrappedValue);
            }
            return nil;
        }

    }

    @CoreMethod(names = "set_mark_list_on_object", onSingleton = true, required = 1)
    public abstract static class SetMarkList extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object setMarkList(RubyDynamicObject structOwner,
                @Cached WriteObjectFieldNode writeMarkedNode) {
            writeMarkedNode.execute(
                    structOwner,
                    Layouts.MARKED_OBJECTS_IDENTIFIER,
                    getContext().getMarkingService().finishMarking());
            return nil;
        }
    }

    @CoreMethod(names = "define_marker", onSingleton = true, required = 2)
    public abstract static class CreateMarkerNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected Object createMarker(RubyDynamicObject object, RubyProc marker) {
            /* The code here has to be a little subtle. The marker must be associated with the object it will act on,
             * but the lambda must not capture the object (and prevent garbage collection). So the marking function is a
             * lambda that will take the object as an argument 'o' which will be provided when the marking function is
             * called by the marking service. */
            getContext()
                    .getMarkingService()
                    .addMarker(object, (o) -> CallBlockNode.getUncached().yield(marker, o));
            return nil;
        }

    }

    @CoreMethod(names = "rb_thread_check_ints", onSingleton = true, required = 0)
    public abstract static class CheckThreadInterrupt extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object checkInts() {
            TruffleSafepoint.pollHere(this);
            return nil;
        }
    }

    @CoreMethod(names = "rb_tr_is_native_object_function", onSingleton = true, required = 0)
    public abstract static class IsNativeObjectFunctionNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object isNativeObjectFunction() {
            return new ValueWrapperManager.IsNativeObjectFunction();
        }
    }

    @CoreMethod(names = "rb_tr_unwrap_function", onSingleton = true, required = 0)
    public abstract static class UnwrapperFunctionNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object unwrapFunction() {
            return new ValueWrapperManager.UnwrapperFunction();
        }
    }

    @CoreMethod(names = "rb_tr_id2sym_function", onSingleton = true, required = 0)
    public abstract static class UnwrapperIDFunctionNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object unwrapFunction() {
            return new ValueWrapperManager.ID2SymbolFunction();
        }
    }

    @CoreMethod(names = "rb_tr_sym2id_function", onSingleton = true, required = 0)
    public abstract static class Sym2IDFunctionNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object unwrapFunction() {
            return new ValueWrapperManager.Symbol2IDFunction();
        }
    }

    @CoreMethod(names = "rb_tr_wrap_function", onSingleton = true, required = 0)
    public abstract static class WrapperFunctionNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object wrapFunction() {
            return new ValueWrapperManager.WrapperFunction();
        }
    }

    @CoreMethod(names = "rb_tr_force_native_function", onSingleton = true, required = 0)
    public abstract static class ToNativeFunctionNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object wrapFunction() {
            return new ValueWrapperManager.ToNativeObjectFunction();
        }
    }

    @CoreMethod(names = "rb_check_symbol_cstr", onSingleton = true, required = 1)
    public abstract static class RbCheckSymbolCStrNode extends CoreMethodArrayArgumentsNode {
        @Specialization(guards = "strings.isRubyString(string)")
        protected Object checkSymbolCStr(Object string,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings) {
            final RubySymbol sym = getLanguage().symbolTable.getSymbolIfExists(
                    strings.getTString(string),
                    strings.getEncoding(string));
            return sym == null ? nil : sym;
        }
    }

    @CoreMethod(names = "rb_ary_new_from_values", onSingleton = true, required = 1)
    public abstract static class RbAryNewFromValues extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected RubyArray rbAryNewFromValues(Object cArray,
                @Cached UnwrapCArrayNode unwrapCArrayNode) {
            final Object[] values = unwrapCArrayNode.execute(cArray);
            return createArray(values);
        }
    }

    @CoreMethod(names = "rb_tr_sprintf_types", onSingleton = true, required = 1)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
    @ReportPolymorphism
    public abstract static class RBSprintfFormatNode extends CoreMethodArrayArgumentsNode {

        @Child protected TruffleString.GetInternalByteArrayNode byteArrayNode = TruffleString.GetInternalByteArrayNode
                .create();

        @Specialization(
                guards = {
                        "libFormat.isRubyString(format)",
                        "equalNode.execute(libFormat.getRope(format), cachedFormatRope)" },
                limit = "2")
        protected Object typesCached(VirtualFrame frame, Object format,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libFormat,
                @Cached("libFormat.getRope(format)") Rope cachedFormatRope,
                @Cached("compileArgTypes(format, libFormat, byteArrayNode)") RubyArray cachedTypes,
                @Cached RopeNodes.EqualNode equalNode) {
            return cachedTypes;
        }

        @Specialization(guards = "libFormat.isRubyString(format)")
        protected RubyArray typesUncachd(VirtualFrame frame, Object format,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libFormat) {
            return compileArgTypes(format, libFormat, byteArrayNode);
        }

        @TruffleBoundary
        protected RubyArray compileArgTypes(Object format, RubyStringLibrary libFormat,
                TruffleString.GetInternalByteArrayNode byteArrayNode) {
            try {
                return new RBSprintfCompiler(getLanguage(), this)
                        .typeList(format, libFormat, byteArrayNode, getContext(), getLanguage());
            } catch (InvalidFormatException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
        }
    }

    @CoreMethod(names = "rb_tr_sprintf", onSingleton = true, required = 3)
    @ImportStatic({ StringCachingGuards.class, StringOperations.class })
    @ReportPolymorphism
    public abstract static class RBSprintfNode extends CoreMethodArrayArgumentsNode {

        @Child private StringNodes.MakeStringNode makeStringNode;

        private final BranchProfile exceptionProfile = BranchProfile.create();
        private final ConditionProfile resizeProfile = ConditionProfile.create();

        @Specialization(
                guards = {
                        "libFormat.isRubyString(format)",
                        "equalNode.execute(libFormat.getRope(format), cachedFormatRope)" },
                limit = "2")
        protected RubyString formatCached(Object format, Object stringReader, RubyArray argArray,
                @Cached TranslateInteropExceptionNode translateInteropExceptionNode,
                @Cached ArrayToObjectArrayNode arrayToObjectArrayNode,
                @Cached TruffleString.GetInternalByteArrayNode byteArrayNode,
                @Cached WrapNode wrapNode,
                @Cached UnwrapNode unwrapNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libFormat,
                @Cached("libFormat.getRope(format)") Rope cachedFormatRope,
                @Cached("cachedFormatRope.byteLength()") int cachedFormatLength,
                @Cached("create(compileFormat(format, libFormat, byteArrayNode, stringReader))") DirectCallNode formatNode,
                @Cached RopeNodes.EqualNode equalNode) {
            final BytesResult result;
            final Object[] arguments = arrayToObjectArrayNode.executeToObjectArray(argArray);
            try {
                result = (BytesResult) formatNode
                        .call(
                                new Object[]{ arguments, arguments.length, null });
            } catch (FormatException e) {
                exceptionProfile.enter();
                throw FormatExceptionTranslator.translate(getContext(), this, e);
            }

            return finishFormat(cachedFormatLength, result);
        }

        @Specialization(
                guards = "libFormat.isRubyString(format)",
                replaces = "formatCached")
        protected RubyString formatUncached(Object format, Object stringReader, RubyArray argArray,
                @Cached TranslateInteropExceptionNode translateInteropExceptionNode,
                @Cached WrapNode wrapNode,
                @Cached UnwrapNode unwrapNode,
                @Cached IndirectCallNode formatNode,
                @Cached ArrayToObjectArrayNode arrayToObjectArrayNode,
                @Cached TruffleString.GetInternalByteArrayNode byteArrayNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libFormat) {
            final BytesResult result;
            final Object[] arguments = arrayToObjectArrayNode.executeToObjectArray(argArray);
            try {
                result = (BytesResult) formatNode
                        .call(
                                compileFormat(format, libFormat, byteArrayNode, stringReader),
                                new Object[]{ arguments, arguments.length, null });
            } catch (FormatException e) {
                exceptionProfile.enter();
                throw FormatExceptionTranslator.translate(getContext(), this, e);
            }

            return finishFormat(libFormat.getRope(format).byteLength(), result);
        }

        private RubyString finishFormat(int formatLength, BytesResult result) {
            byte[] bytes = result.getOutput();

            if (resizeProfile.profile(bytes.length != result.getOutputLength())) {
                bytes = Arrays.copyOf(bytes, result.getOutputLength());
            }

            if (makeStringNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                makeStringNode = insert(StringNodes.MakeStringNode.create());
            }

            return makeStringNode
                    .executeMake(
                            bytes,
                            result.getEncoding().getEncodingForLength(formatLength),
                            result.getStringCodeRange());
        }

        @TruffleBoundary
        protected RootCallTarget compileFormat(Object format, RubyStringLibrary libFormat,
                TruffleString.GetInternalByteArrayNode byteArrayNode, Object stringReader) {
            try {
                return new RBSprintfCompiler(getLanguage(), this)
                        .compile(format, libFormat, byteArrayNode, stringReader);
            } catch (InvalidFormatException e) {
                throw new RaiseException(getContext(), coreExceptions().argumentError(e.getMessage(), this));
            }
        }
    }

    @CoreMethod(names = "ruby_native_thread_p", onSingleton = true)
    public abstract static class RubyThreadNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isRubyThread(VirtualFrame frame) {
            ThreadManager threadManager = getContext().getThreadManager();
            return Thread.currentThread() == threadManager.getRootJavaThread() ||
                    threadManager.isRubyManagedThread(Thread.currentThread());
        }
    }

    @CoreMethod(names = "zlib_get_crc_table", onSingleton = true)
    public abstract static class ZLibGetCRCTable extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected RubyArray zlibGetCRCTable() {
            return createArray(ZLibCRCTable.TABLE.clone());
        }
    }

    @Primitive(name = "data_holder_create")
    public abstract static class DataHolderCreate extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected DataHolder create(Object address) {
            return new DataHolder(address);
        }
    }

    @Primitive(name = "data_holder_get_data")
    public abstract static class DataHolderGetData extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object getData(DataHolder data) {
            return data.getPointer();
        }
    }

    @Primitive(name = "data_holder_set_data")
    public abstract static class DataHolderSetData extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected Object setData(DataHolder data, Object address) {
            data.setPointer(address);
            return nil;
        }
    }

    @Primitive(name = "data_holder_is_holder?")
    public abstract static class DataHolderIsHolder extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected boolean setData(Object dataHolder) {
            return dataHolder instanceof DataHolder;
        }
    }
}
