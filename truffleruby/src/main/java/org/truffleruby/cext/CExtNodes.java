/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.cext;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.CreateCast;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeChildren;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.Log;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.core.CoreLibrary;
import org.truffleruby.core.array.ArrayHelpers;
import org.truffleruby.core.cast.NameToJavaStringNodeGen;
import org.truffleruby.core.module.MethodLookupResult;
import org.truffleruby.core.module.ModuleNodes;
import org.truffleruby.core.module.ModuleNodesFactory;
import org.truffleruby.core.module.ModuleOperations;
import org.truffleruby.core.numeric.BignumOperations;
import org.truffleruby.core.numeric.FixnumOrBignumNode;
import org.truffleruby.core.regexp.RegexpNodes;
import org.truffleruby.core.rope.NativeRope;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeConstants;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.rope.RopeNodesFactory;
import org.truffleruby.core.rope.SubstringRope;
import org.truffleruby.core.string.StringNodes;
import org.truffleruby.core.string.StringNodesFactory;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.string.StringSupport;
import org.truffleruby.extra.ffi.PointerNodes;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.RubyConstant;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.RubyRootNode;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.constants.GetConstantNode;
import org.truffleruby.language.constants.LookupConstantNode;
import org.truffleruby.language.control.BreakException;
import org.truffleruby.language.control.BreakID;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.dispatch.CallDispatchHeadNode;
import org.truffleruby.language.dispatch.DispatchHeadNodeFactory;
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.InitializeClassNode;
import org.truffleruby.language.objects.InitializeClassNodeGen;
import org.truffleruby.language.objects.IsFrozenNode;
import org.truffleruby.language.objects.IsFrozenNodeGen;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.language.objects.MetaClassNodeGen;
import org.truffleruby.language.objects.ObjectIVarGetNode;
import org.truffleruby.language.objects.ObjectIVarGetNodeGen;
import org.truffleruby.language.objects.ObjectIVarSetNode;
import org.truffleruby.language.objects.ObjectIVarSetNodeGen;
import org.truffleruby.language.supercall.CallSuperMethodNode;
import org.truffleruby.language.supercall.CallSuperMethodNodeGen;
import org.truffleruby.language.threadlocal.ThreadAndFrameLocalStorage;
import org.truffleruby.parser.Identifiers;
import org.truffleruby.platform.FDSet;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static org.truffleruby.core.string.StringOperations.rope;

@CoreClass("Truffle::CExt")
public class CExtNodes {

    // TODO CS 19-Mar-17 many of these builtins could just be identify functions with a cast in C

    @CoreMethod(names = "NUM2INT", onSingleton = true, required = 1)
    public abstract static class NUM2INTNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int num2int(int num) {
            return num;
        }

        @Specialization
        public int num2int(long num) {
            return (int) num;
        }

    }

    @CoreMethod(names = "NUM2UINT", onSingleton = true, required = 1, lowerFixnum = 1)
    public abstract static class NUM2UINTNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int num2uint(int num) {
            // TODO CS 2-May-16 what to do about the fact it's unsigned?
            return num;
        }

    }

    @CoreMethod(names = "NUM2LONG", onSingleton = true, required = 1)
    public abstract static class NUM2LONGNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long num2long(int num) {
            return num;
        }


        @Specialization
        public long num2long(long num) {
            return num;
        }
    }

    @CoreMethod(names = "NUM2ULONG", onSingleton = true, required = 1, lowerFixnum = 1)
    public abstract static class NUM2ULONGNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long num2ulong(int num) {
            // TODO CS 2-May-16 what to do about the fact it's unsigned?
            return num;
        }

    }

    @CoreMethod(names = "NUM2DBL", onSingleton = true, required = 1, lowerFixnum = 1)
    public abstract static class NUM2DBLNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public double num2dbl(int num) {
            return num;
        }

        @Specialization
        public double num2dbl(long num) {
            return num;
        }

        @Specialization
        public double num2dbl(double num) {
            return num;
        }

        @Specialization(guards = "isRubyBignum(num)")
        public double num2dbl(DynamicObject num) {
            return Layouts.BIGNUM.getValue(num).doubleValue();
        }
    }

    @CoreMethod(names = "FIX2INT", onSingleton = true, required = 1, lowerFixnum = 1)
    public abstract static class FIX2INTNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int fix2int(int num) {
            return num;
        }

    }

    @CoreMethod(names = "FIX2UINT", onSingleton = true, required = 1)
    public abstract static class FIX2UINTNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int fix2uint(int num) {
            // TODO CS 2-May-16 what to do about the fact it's unsigned?
            return num;
        }

        @Specialization
        public long fix2uint(long num) {
            // TODO CS 2-May-16 what to do about the fact it's unsigned?
            return num;
        }

    }

    @CoreMethod(names = "FIX2LONG", onSingleton = true, required = 1, lowerFixnum = 1)
    public abstract static class FIX2LONGNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long fix2long(int num) {
            return num;
        }

    }

    @CoreMethod(names = "INT2NUM", onSingleton = true, required = 1)
    public abstract static class INT2NUMNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int int2num(int num) {
            return num;
        }

        @Specialization
        public long int2num(long num) {
            return num;
        }

    }

    @CoreMethod(names = "INT2FIX", onSingleton = true, required = 1)
    public abstract static class INT2FIXNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int int2fix(int num) {
            return num;
        }

        @Specialization
        public long int2fix(long num) {
            return num;
        }

    }

    @CoreMethod(names = "UINT2NUM", onSingleton = true, required = 1, lowerFixnum = 1)
    public abstract static class UINT2NUMNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int uint2num(int num) {
            // TODO CS 2-May-16 what to do about the fact it's unsigned?
            return num;
        }

    }

    @CoreMethod(names = "LONG2NUM", onSingleton = true, required = 1)
    public abstract static class LONG2NUMNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long long2num(long num) {
            return num;
        }

    }

    @CoreMethod(names = "LL2NUM", onSingleton = true, required = 1)
    public abstract static class LL2NUMNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long longlong2num(long num) {
            return num;
        }

    }

    @CoreMethod(names = "ULONG2NUM", onSingleton = true, required = 1)
    public abstract static class ULONG2NUMNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        @TruffleBoundary
        public Object ulong2num(long num) {
            if (num >= 0) {
                return num;
            } else {
                // The l at the end of the constant is crucial,
                // otherwise the constant will be interpreted as an
                // int (-1) and then converted to a long (still -1,
                // 0xffffffffffffffff), and have completely the wrong
                // effect.
                BigInteger lsi = BigInteger.valueOf(num & 0xffffffffl);
                BigInteger msi = BigInteger.valueOf(num >>> 32).shiftLeft(32);
                BigInteger res = msi.add(lsi);
                return BignumOperations.createBignum(getContext(), res);
            }
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

    @CoreMethod(names = "DBL2BIG", onSingleton = true, required = 1)
    public abstract static class DBL2BIGNode extends CoreMethodArrayArgumentsNode {

        @Child private FixnumOrBignumNode fixnumOrBignum = new FixnumOrBignumNode();

        @Specialization
        @TruffleBoundary
        public Object dbl2big(double num,
                              @Cached("create()") BranchProfile errorProfile) {
            if (Double.isInfinite(num)) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().floatDomainError("Infinity", this));
            }

            if (Double.isNaN(num)) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().floatDomainError("NaN", this));
            }

            return fixnumOrBignum.fixnumOrBignum(num);
        }

    }

    @CoreMethod(names = "LONG2FIX", onSingleton = true, required = 1)
    public abstract static class LONG2FIXNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long long2fix(long num) {
            return num;
        }

    }

    @CoreMethod(names = "CLASS_OF", onSingleton = true, required = 1)
    public abstract static class CLASSOFNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject class_of(DynamicObject object,
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
            throw new RaiseException(coreExceptions().rangeErrorConvertToInt(num, this));
        }

        protected boolean fitsIntoInteger(long num) {
            return CoreLibrary.fitsIntoInteger(num);
        }

    }

    @CoreMethod(names = "rb_enc_codepoint_len", onSingleton = true, required = 2)
    public abstract static class RbEncCodePointLenNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject rbEncCodePointLen(DynamicObject string, DynamicObject encoding) {
            final byte[] bytes = StringOperations.rope(string).getBytes();
            final Encoding enc = Layouts.ENCODING.getEncoding(encoding);
            final int r = StringSupport.preciseLength(enc, bytes, 0, bytes.length);
            if (!StringSupport.MBCLEN_CHARFOUND_P(r)) {
                throw new RaiseException(coreExceptions().argumentError("invalid byte sequence in " + enc, this));
            }
            final int len_p = StringSupport.MBCLEN_CHARFOUND_LEN(r);
            final int codePoint = StringSupport.preciseCodePoint(enc, bytes, 0, bytes.length);
            return createArray(new Object[]{len_p, codePoint}, 2);
        }

    }


    @CoreMethod(names = "rb_str_resize", onSingleton = true, required = 2, lowerFixnum = 2)
    public abstract static class RbStrResizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "shouldNoop(string, len)")
        public DynamicObject rbStrResizeSame(DynamicObject string, int len) {
            return string;
        }

        @Specialization(guards = "shouldShrink(string, len)")
        public DynamicObject rbStrResizeShrink(DynamicObject string, int len,
                                               @Cached("create()") RopeNodes.MakeSubstringNode makeSubstringNode) {
            StringOperations.setRope(string, makeSubstringNode.executeMake(rope(string), 0, len));
            return string;
        }

        @TruffleBoundary
        @Specialization(guards = { "!shouldNoop(string, len)", "!shouldShrink(string, len)" })
        public DynamicObject rbStrResizeGrow(DynamicObject string, int len,
                                             @Cached("create()") RopeNodes.MakeSubstringNode makeSubstringNode,
                                             @Cached("create()") RopeNodes.MakeConcatNode makeConcatNode,
                                             @Cached("create()") RopeNodes.MakeRepeatingNode makeRepeatingNode) {
            final Rope rope = rope(string);

            if (rope instanceof SubstringRope) {
                final Rope nullAppended = makeConcatNode.executeMake(rope, RopeConstants.UTF8_SINGLE_BYTE_ROPES[0], rope.getEncoding());

                if (nullAppended.byteLength() == len) {
                    StringOperations.setRope(string, nullAppended);
                } else {
                    final SubstringRope substringRope = (SubstringRope) rope;
                    final Rope base = substringRope.getChild();

                    final int lenFromBase = base.byteLength() <= len ? len - base.byteLength() : len - nullAppended.byteLength();
                    final Rope fromBase = makeSubstringNode.executeMake(base, nullAppended.byteLength(), lenFromBase);
                    final Rope withBase = makeConcatNode.executeMake(nullAppended, fromBase, nullAppended.getEncoding());

                    if (withBase.byteLength() == len) {
                        StringOperations.setRope(string, withBase);
                    } else {
                        final Rope filler = makeRepeatingNode.executeMake(RopeConstants.UTF8_SINGLE_BYTE_ROPES[0], len - withBase.byteLength());
                        StringOperations.setRope(string, makeConcatNode.executeMake(withBase, filler, rope.getEncoding()));
                    }
                }
            } else {
                final Rope filler = makeRepeatingNode.executeMake(RopeConstants.UTF8_SINGLE_BYTE_ROPES[0], len - rope.byteLength());
                StringOperations.setRope(string, makeConcatNode.executeMake(rope, filler, rope.getEncoding()));
            }

            return string;
        }

        protected static boolean shouldNoop(DynamicObject string, long len) {
            return rope(string).byteLength() == len;
        }

        protected static boolean shouldShrink(DynamicObject string, long len) {
            return rope(string).byteLength() > len;
        }
    }

    @CoreMethod(names = "rb_block_proc", onSingleton = true)
    public abstract static class BlockProcNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject blockProc() {
            return Truffle.getRuntime().iterateFrames(frameInstance -> {
                final Node callNode = frameInstance.getCallNode();

                if (callNode != null) {
                    final RootNode rootNode = callNode.getRootNode();

                    if (rootNode instanceof RubyRootNode && rootNode.getSourceSection().isAvailable()) {
                        final DynamicObject block = RubyArguments.getBlock(frameInstance.getFrame(FrameAccess.READ_ONLY));

                        if (block == null) {
                            return nil();
                        } else {
                            return block;
                        }
                    }
                }

                return null;
            });
        }

    }

    @CoreMethod(names = "rb_check_frozen", onSingleton = true, required = 1)
    public abstract static class CheckFrozenNode extends CoreMethodArrayArgumentsNode {

        @Child private IsFrozenNode isFrozenNode = IsFrozenNodeGen.create(null);

        @Specialization
        public boolean rb_check_frozen(Object object) {
            isFrozenNode.raiseIfFrozen(object);
            return true;
        }

    }

    @CoreMethod(names = "get_block", onSingleton = true)
    public abstract static class GetBlockNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject getBlock() {
            return Truffle.getRuntime().iterateFrames(frameInstance -> {
                Frame frame = frameInstance.getFrame(FrameAccess.READ_ONLY);
                return RubyArguments.tryGetBlock(frame);
            });
        }

    }

    @NodeChildren({
            @NodeChild(type = RubyNode.class, value = "module"),
            @NodeChild(type = RubyNode.class, value = "name")
    })
    @CoreMethod(names = "rb_const_get_from", onSingleton = true, required = 2)
    public abstract static class ConstGetFromNode extends CoreMethodNode {

        @CreateCast("name")
        public RubyNode coerceToString(RubyNode name) {
            return NameToJavaStringNodeGen.create(name);
        }

        @Child private LookupConstantNode lookupConstantNode = LookupConstantNode.create(true, false);
        @Child private GetConstantNode getConstantNode = GetConstantNode.create();

        @Specialization
        public Object constGetFrom(VirtualFrame frame, DynamicObject module, String name) {
            final RubyConstant constant = lookupConstantNode.lookupConstant(frame, module, name);
            return getConstantNode.executeGetConstant(frame, module, name, constant, lookupConstantNode);
        }

    }

    @CoreMethod(names = "cext_module_function", onSingleton = true, required = 2)
    public abstract static class CextModuleFunctionNode extends CoreMethodArrayArgumentsNode {

        @Child
        ModuleNodes.SetVisibilityNode setVisibilityNode = ModuleNodesFactory.SetVisibilityNodeGen.create(Visibility.MODULE_FUNCTION, null, null);

        @Specialization(guards = {"isRubyModule(module)", "isRubySymbol(name)"})
        public DynamicObject cextModuleFunction(VirtualFrame frame, DynamicObject module, DynamicObject name) {
            return setVisibilityNode.executeSetVisibility(frame, module, new Object[]{name});
        }

    }

    @CoreMethod(names = "caller_frame_visibility", onSingleton = true, required = 1)
    public abstract static class CallerFrameVisibilityNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = "isRubySymbol(visibility)")
        public boolean toRubyString(DynamicObject visibility) {
            final Frame callerFrame = getContext().getCallStack().getCallerFrameIgnoringSend().getFrame(FrameAccess.MATERIALIZE);
            final Visibility callerVisibility = DeclarationContext.findVisibility(callerFrame);

            switch (visibility.toString()) {
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

    @CoreMethod(names = "rb_iter_break", onSingleton = true)
    public abstract static class IterBreakNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object iterBreak() {
            throw new BreakException(BreakID.ANY, nil());
        }

    }

    @CoreMethod(names = "rb_sourcefile", onSingleton = true)
    public abstract static class SourceFileNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject sourceFile() {
            final SourceSection sourceSection = getTopUserSourceSection("rb_sourcefile", "execute_with_mutex");
            final String file = sourceSection.getSource().getPath();
            return createString(StringOperations.encodeRope(file, UTF8Encoding.INSTANCE));
        }

        public static SourceSection getTopUserSourceSection(String...  methodNames) {
            return Truffle.getRuntime().iterateFrames(frameInstance -> {
                final Node callNode = frameInstance.getCallNode();

                if (callNode != null) {
                    final RootNode rootNode = callNode.getRootNode();

                    if (rootNode instanceof RubyRootNode && rootNode.getSourceSection().isAvailable() && !nameMatches(rootNode.getName(), methodNames)) {
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
            return Identifiers.isValidConstantName19(Layouts.SYMBOL.getString(symbol));
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

        @Child private CallSuperMethodNode callSuperMethodNode = CallSuperMethodNodeGen.create(null, null, null);
        @Child private MetaClassNode metaClassNode = MetaClassNodeGen.create(null);

        @Specialization
        public Object callSuper(VirtualFrame frame, Object[] args) {
            final Frame callingMethodFrame = findCallingMethodFrame();
            final InternalMethod callingMethod = RubyArguments.getMethod(callingMethodFrame);
            final Object callingSelf = RubyArguments.getSelf(callingMethodFrame);
            final DynamicObject callingMetaclass = metaClassNode.executeMetaClass(callingSelf);
            final MethodLookupResult superMethodLookup = ModuleOperations.lookupSuperMethod(callingMethod, callingMetaclass);
            final InternalMethod superMethod = superMethodLookup.getMethod();
            return callSuperMethodNode.callSuperMethod(frame, superMethod, args, null);
        }

        @TruffleBoundary
        private static Frame findCallingMethodFrame() {
            return Truffle.getRuntime().iterateFrames(frameInstance -> {
                final Frame frame = frameInstance.getFrame(FrameAccess.READ_ONLY);

                final InternalMethod method = RubyArguments.tryGetMethod(frame);

                if (method == null) {
                    return null;
                } else if (method.getName().equals(/* Truffle::Cext. */ "rb_call_super")
                        || method.getName().equals(/* Truffle::CExt. */ "execute_with_mutex")
                        || method.getName().equals(/* Truffle::Interop. */ "execute")
                        || method.getName().equals(/* Truffle::Cext. */ "rb_call_super_splatted")) {
                    // TODO CS 11-Mar-17 must have a more precise check to skip these methods
                    return null;
                } else {
                    return frame;
                }
            });
        }

    }

    @CoreMethod(names = "rb_thread_wait_fd", onSingleton = true, required = 1, lowerFixnum = 1)
    public abstract static class ThreadWaitFDNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject threadWaitFDNode(int fd) {
            final FDSet fdSet = getContext().getNativePlatform().createFDSet();

            getContext().getThreadManager().runUntilResult(this, () -> {
                fdSet.set(fd);
                final int result = nativeSockets().select(fd + 1,
                        fdSet.getPointer(),
                        PointerNodes.NULL_POINTER,
                        PointerNodes.NULL_POINTER,
                        null);

                if (result == 0) {
                    return null;
                }

                return result;
            });

            return nil();
        }

    }

    @CoreMethod(names = "rb_thread_fd_writable", onSingleton = true, required = 1, lowerFixnum = 1)
    public abstract static class ThreadFDWritableNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int threadFDWritableNode(int fd) {
            final FDSet fdSet = getContext().getNativePlatform().createFDSet();
            fdSet.set(fd);

            return getContext().getThreadManager().runUntilResult(this, () -> {
                final int result = nativeSockets().select(fd + 1,
                        PointerNodes.NULL_POINTER,
                        fdSet.getPointer(),
                        PointerNodes.NULL_POINTER,
                        null);

                if (result == 0) {
                    return null;
                }

                return result;
            });
        }

    }

    @CoreMethod(names = "rb_backref_get", onSingleton = true)
    public abstract static class BackRefGet extends CoreMethodNode {

        @Specialization
        @TruffleBoundary
        public Object backRefGet() {
            final ThreadAndFrameLocalStorage storage = RegexpNodes.getMatchDataThreadLocalSearchingStack(getContext());

            if (storage == null) {
                return nil();
            } else {
                return storage.get();
            }
        }

    }

    @CoreMethod(names = "rb_define_hooked_variable_inner", onSingleton = true, required = 3)
    public abstract static class DefineHookedVariableInnerNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = {"isRubySymbol(name)", "isRubyProc(getter)", "isRubyProc(setter)"})
        public DynamicObject defineHookedVariableInnerNode(DynamicObject name, DynamicObject getter, DynamicObject setter) {
            getContext().getCoreLibrary().getGlobalVariables().put(name.toString(), getter, setter);
            return nil();
        }

    }

    @CoreMethod(names = "rb_hash", onSingleton = true, required = 1)
    public abstract static class HashNode extends CoreMethodArrayArgumentsNode {
        @Child private org.truffleruby.core.hash.HashNode hash;

        @Specialization
        public Object hash(VirtualFrame frame, Object object) {
            if (hash == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                hash = insert(new org.truffleruby.core.hash.HashNode());
            }

            return hash.hash(frame, object, false);
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

    @CoreMethod(names = "string_pointer_to_native", onSingleton = true, required = 1)
    public abstract static class StringPointerToNativeNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyString(string)")
        public long toNative(DynamicObject string,
                          @Cached("createBinaryProfile()") ConditionProfile convertProfile) {
            final Rope currentRope = rope(string);

            final NativeRope nativeRope;

            if (convertProfile.profile(currentRope instanceof NativeRope)) {
                nativeRope = (NativeRope) currentRope;
            } else {
                nativeRope = new NativeRope(getContext().getNativePlatform().getMemoryManager(), currentRope.getBytes(), currentRope.getEncoding(), currentRope.characterLength());
                Layouts.STRING.setRope(string, nativeRope);
            }

            return nativeRope.getNativePointer().address();
        }

    }

    @CoreMethod(names = "string_pointer_read", onSingleton = true, required = 2, lowerFixnum = 2)
    public abstract static class StringPointerReadNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyString(string)")
        public Object read(DynamicObject string, int index,
                           @Cached("createBinaryProfile()") ConditionProfile beyondEndProfile,
                           @Cached("getHelperNode()") RopeNodes.GetByteNode getByteNode) {
            final Rope rope = rope(string);

            if (beyondEndProfile.profile(index >= rope.byteLength())) {
                return 0;
            } else {
                return getByteNode.executeGetByte(rope, index);
            }
        }

        protected RopeNodes.GetByteNode getHelperNode() {
            return RopeNodesFactory.GetByteNodeGen.create(null, null);
        }

    }

    @CoreMethod(names = "string_pointer_write", onSingleton = true, required = 3, lowerFixnum = {2, 3})
    public abstract static class StringPointerWriteNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyString(string)")
        public Object write(DynamicObject string, int index, int value,
                            @Cached("getHelperNode()") StringNodes.SetByteNode setByteNode) {
            return setByteNode.executeSetByte(string, index, value);
        }

        protected StringNodes.SetByteNode getHelperNode() {
            return StringNodesFactory.SetByteNodeFactory.create(null, null, null);
        }

    }

    @CoreMethod(names = "rb_class_new", onSingleton = true, required = 1)
    public abstract static class ClassNewNode extends CoreMethodArrayArgumentsNode {

        @Child private CallDispatchHeadNode allocateNode;
        @Child private InitializeClassNode initializeClassNode;

        @Specialization
        public DynamicObject classNew(VirtualFrame frame, DynamicObject superclass) {
            if (allocateNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                allocateNode = insert(DispatchHeadNodeFactory.createMethodCallOnSelf());
                initializeClassNode = insert(InitializeClassNodeGen.create(false, null, null, null));
            }

            DynamicObject klass = (DynamicObject) allocateNode.call(frame, getContext().getCoreLibrary().getClassClass(), "__allocate__");
            return initializeClassNode.executeInitialize(frame, klass, superclass, NotProvided.INSTANCE);
        }

    }

    @CoreMethod(names = "rb_tr_debug", onSingleton = true, required = 1)
    public abstract static class DebugNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public Object debug(Object object) {
            System.err.printf("%s @ %s: %s%n", object.getClass(), System.identityHashCode(object), object);
            return nil();
        }
    }

    @CoreMethod(names = "hidden_variable_get", onSingleton = true, required = 2)
    public abstract static class HiddenVariableGetNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbol(name)")
        public Object hiddenVariableGet(DynamicObject object, DynamicObject name,
                @Cached("createObjectIVarGetNode()") ObjectIVarGetNode iVarGetNode) {
            return iVarGetNode.executeIVarGet(object, name);
        }

        protected ObjectIVarGetNode createObjectIVarGetNode() {
            return ObjectIVarGetNodeGen.create(false, null, null);
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
            return ObjectIVarSetNodeGen.create(false, null, null, null);
        }

    }

}
