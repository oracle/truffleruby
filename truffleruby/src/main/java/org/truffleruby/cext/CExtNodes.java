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
import com.oracle.truffle.api.source.SourceSection;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.Log;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreMethodNode;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
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
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.extra.ffi.PointerPrimitiveNodes;
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
import org.truffleruby.language.methods.DeclarationContext;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.IsFrozenNode;
import org.truffleruby.language.objects.IsFrozenNodeGen;
import org.truffleruby.language.objects.MetaClassNode;
import org.truffleruby.language.objects.MetaClassNodeGen;
import org.truffleruby.language.supercall.CallSuperMethodNode;
import org.truffleruby.language.supercall.CallSuperMethodNodeGen;
import org.truffleruby.language.threadlocal.ThreadLocalObject;
import org.truffleruby.parser.Identifiers;
import org.truffleruby.platform.FDSet;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@CoreClass("Truffle::CExt")
public class CExtNodes {

    @CoreMethod(names = "NUM2INT", isModuleFunction = true, required = 1)
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

    @CoreMethod(names = "NUM2UINT", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class NUM2UINTNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int num2uint(int num) {
            // TODO CS 2-May-16 what to do about the fact it's unsigned?
            return num;
        }

    }

    @CoreMethod(names = "NUM2LONG", isModuleFunction = true, required = 1)
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

    @CoreMethod(names = "NUM2ULONG", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class NUM2ULONGNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long num2ulong(int num) {
            // TODO CS 2-May-16 what to do about the fact it's unsigned?
            return num;
        }

    }

    @CoreMethod(names = "NUM2DBL", isModuleFunction = true, required = 1, lowerFixnum = 1)
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

    @CoreMethod(names = "FIX2INT", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class FIX2INTNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int fix2int(int num) {
            return num;
        }

    }

    @CoreMethod(names = "FIX2UINT", isModuleFunction = true, required = 1)
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

    @CoreMethod(names = "FIX2LONG", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class FIX2LONGNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long fix2long(int num) {
            return num;
        }

    }

    @CoreMethod(names = "INT2NUM", isModuleFunction = true, required = 1)
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

    @CoreMethod(names = "INT2FIX", isModuleFunction = true, required = 1)
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

    @CoreMethod(names = "UINT2NUM", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class UINT2NUMNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int uint2num(int num) {
            // TODO CS 2-May-16 what to do about the fact it's unsigned?
            return num;
        }

    }

    @CoreMethod(names = "LONG2NUM", isModuleFunction = true, required = 1)
    public abstract static class LONG2NUMNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long long2num(long num) {
            return num;
        }

    }

    @CoreMethod(names = "ULONG2NUM", isModuleFunction = true, required = 1)
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

    @CoreMethod(names = "rb_integer_bytes", isModuleFunction = true, lowerFixnum = {2,3}, required = 6)
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

    @CoreMethod(names = "rb_absint_bit_length", isModuleFunction = true, required = 1)
    public abstract static class BignumAbsBitLengthNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyBignum(num)")
        @TruffleBoundary
        public int bitLength(DynamicObject num) {
            return Layouts.BIGNUM.getValue(num).abs().bitLength();
        }
    }

    @CoreMethod(names = "rb_2scomp_bit_length", isModuleFunction = true, required = 1)
    public abstract static class Bignum2sCompBitLengthNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyBignum(num)")
        @TruffleBoundary
        public int bitLength(DynamicObject num) {
            return Layouts.BIGNUM.getValue(num).bitLength();
        }
    }

    @CoreMethod(names = "DBL2BIG", isModuleFunction = true, required = 1)
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

    @CoreMethod(names = "LONG2FIX", isModuleFunction = true, required = 1)
    public abstract static class LONG2FIXNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public long long2fix(long num) {
            return num;
        }

    }

    @CoreMethod(names = "CLASS_OF", isModuleFunction = true, required = 1)
    public abstract static class CLASSOFNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject class_of(DynamicObject object,
                                      @Cached("create()") MetaClassNode metaClassNode) {
            return metaClassNode.executeMetaClass(object);
        }

    }

    @CoreMethod(names = "rb_long2int", isModuleFunction = true, required = 1)
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

    @CoreMethod(names = "RSTRING_PTR", isModuleFunction = true, required = 1)
    public abstract static class StringPointerNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyString(string)")
        public StringCharPointerAdapter stringPointer(DynamicObject string) {
            return new StringCharPointerAdapter(string);
        }

    }

    @CoreMethod(names = "adapted_string_pointer?", isModuleFunction = true, required = 1)
    public abstract static class AdapatedStringPointerNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public boolean adaptedStringPointer(Object string) {
            return string instanceof StringCharPointerAdapter;
        }

    }

    @CoreMethod(names = "unadapt_string_pointer", isModuleFunction = true, required = 1)
    public abstract static class UnadapatStringPointerNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject unadaptStringPointer(StringCharPointerAdapter adapter) {
            return adapter.getString();
        }

    }

    @CoreMethod(names = "to_ruby_string", isModuleFunction = true, required = 1)
    public abstract static class ToRubyStringNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject toRubyString(StringCharPointerAdapter stringCharPointerAdapter) {
            return stringCharPointerAdapter.getString();
        }

        @Specialization(guards = "isRubyString(string)")
        public DynamicObject toRubyString(DynamicObject string) {
            return string;
        }

    }

    @Primitive(name = "rb_to_encoding", needsSelf = false)
    public abstract static class RbToEncodingPointer extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyEncoding(encoding)")
        public EncodingPointerAdapter encodingPointer(DynamicObject encoding) {
            return new EncodingPointerAdapter(encoding);
        }

    }

    @CoreMethod(names = "rb_enc_from_encoding", isModuleFunction = true, required = 1)
    public abstract static class RbEncFromEncodingNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject unadaptEncodingPointer(EncodingPointerAdapter adapter) {
            return adapter.getEncoding();
        }

    }

    @CoreMethod(names = "rb_block_proc", isModuleFunction = true)
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

    @CoreMethod(names = "rb_check_frozen", isModuleFunction = true, required = 1)
    public abstract static class CheckFrozenNode extends CoreMethodArrayArgumentsNode {

        @Child private IsFrozenNode isFrozenNode = IsFrozenNodeGen.create(null);

        @Specialization
        public boolean rb_check_frozen(Object object) {
            isFrozenNode.raiseIfFrozen(object);
            return true;
        }

    }

    @CoreMethod(names = "get_block", isModuleFunction = true)
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
    @CoreMethod(names = "rb_const_get_from", isModuleFunction = true, required = 2)
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

    @CoreMethod(names = "cext_module_function", isModuleFunction = true, required = 2)
    public abstract static class CextModuleFunctionNode extends CoreMethodArrayArgumentsNode {

        @Child
        ModuleNodes.SetVisibilityNode setVisibilityNode = ModuleNodesFactory.SetVisibilityNodeGen.create(Visibility.MODULE_FUNCTION, null, null);

        @Specialization(guards = {"isRubyModule(module)", "isRubySymbol(name)"})
        public DynamicObject cextModuleFunction(VirtualFrame frame, DynamicObject module, DynamicObject name) {
            return setVisibilityNode.executeSetVisibility(frame, module, new Object[]{name});
        }

    }

    @CoreMethod(names = "caller_frame_visibility", isModuleFunction = true, required = 1)
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

    @CoreMethod(names = "rb_tr_adapt_rdata", isModuleFunction = true, required = 1)
    public abstract static class AdaptRDataNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object adaptRData(DynamicObject object) {
            return new DataAdapter(object);
        }

    }

    protected static final Object handlesLock = new Object();
    protected static final Map<DynamicObject, Long> toNative = new HashMap<>();
    protected static final Map<Long, DynamicObject> toManaged = new HashMap<>();

    @CoreMethod(names = "rb_tr_to_native_handle", isModuleFunction = true, required = 1)
    public abstract static class ToNativeHandleNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public long toNativeHandle(DynamicObject object) {
            synchronized (handlesLock) {
                return toNative.computeIfAbsent(object, (k) -> {
                    final long handle = getContext().getNativePlatform().getMallocFree().malloc(Long.BYTES);
                    memoryManager().newPointer(handle).putLong(0, 0xdeadbeef);
                    Log.LOGGER.info(String.format("native handle 0x%x -> %s", handle, object));
                    toManaged.put(handle, object);
                    return handle;
                });
            }
        }

    }

    @CoreMethod(names = "rb_tr_from_native_handle", isModuleFunction = true, required = 1)
    public abstract static class FromNativeHandleNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject fromNativeHandle(long handle) {
            synchronized (handlesLock) {
                final DynamicObject object = toManaged.get(handle);

                if (object == null) {
                    throw new UnsupportedOperationException();
                }

                return object;
            }
        }

    }

    @CoreMethod(names = "rb_iter_break", isModuleFunction = true)
    public abstract static class IterBreakNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object iterBreak() {
            throw new BreakException(BreakID.ANY, nil());
        }

    }

    @CoreMethod(names = "rb_sourcefile", isModuleFunction = true)
    public abstract static class SourceFileNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public DynamicObject sourceFile() {
            final SourceSection sourceSection = getTopUserSourceSection("rb_sourcefile");
            final String file = sourceSection.getSource().getPath();
            return createString(StringOperations.encodeRope(file, UTF8Encoding.INSTANCE));
        }

        public static SourceSection getTopUserSourceSection(String methodName) {
            return Truffle.getRuntime().iterateFrames(frameInstance -> {
                final Node callNode = frameInstance.getCallNode();

                if (callNode != null) {
                    final RootNode rootNode = callNode.getRootNode();

                    if (rootNode instanceof RubyRootNode && rootNode.getSourceSection().isAvailable() && !methodName.equals(rootNode.getName())) {
                        return frameInstance.getCallNode().getEncapsulatingSourceSection();
                    }
                }

                return null;
            });
        }

    }

    @CoreMethod(names = "rb_sourceline", isModuleFunction = true)
    public abstract static class SourceLineNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        public int sourceLine() {
            final SourceSection sourceSection = SourceFileNode.getTopUserSourceSection("rb_sourceline");
            return sourceSection.getStartLine();
        }

    }

    @CoreMethod(names = "rb_is_instance_id", isModuleFunction = true, required = 1)
    public abstract static class IsInstanceIdNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbol(symbol)")
        public boolean isInstanceId(DynamicObject symbol) {
            return Identifiers.isValidInstanceVariableName(Layouts.SYMBOL.getString(symbol));
        }

    }

    @CoreMethod(names = "rb_is_const_id", isModuleFunction = true, required = 1)
    public abstract static class IsConstIdNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbol(symbol)")
        public boolean isConstId(DynamicObject symbol) {
            return Identifiers.isValidConstantName19(Layouts.SYMBOL.getString(symbol));
        }

    }

    @CoreMethod(names = "rb_is_class_id", isModuleFunction = true, required = 1)
    public abstract static class IsClassVariableIdNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubySymbol(symbol)")
        public boolean isClassVariableId(DynamicObject symbol) {
            return Identifiers.isValidClassVariableName(Layouts.SYMBOL.getString(symbol));
        }

    }

    @CoreMethod(names = "ruby_object?", isModuleFunction = true, required = 1)
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

    @CoreMethod(names = "rb_call_super_splatted", isModuleFunction = true, rest = true)
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

    @CoreMethod(names = "rb_thread_wait_fd", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class ThreadWaitFDNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public DynamicObject threadWaitFDNode(int fd) {
            final FDSet fdSet = getContext().getNativePlatform().createFDSet();
            fdSet.set(fd);

            getContext().getThreadManager().runUntilResult(this, () -> {
                final int result = nativeSockets().select(fd + 1,
                        fdSet.getPointer(),
                        PointerPrimitiveNodes.NULL_POINTER,
                        PointerPrimitiveNodes.NULL_POINTER,
                        null);

                if (result == 0) {
                    return null;
                }

                return result;
            });

            return nil();
        }

    }

    @CoreMethod(names = "rb_thread_fd_writable", isModuleFunction = true, required = 1, lowerFixnum = 1)
    public abstract static class ThreadFDWritableNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int threadFDWritableNode(int fd) {
            final FDSet fdSet = getContext().getNativePlatform().createFDSet();
            fdSet.set(fd);

            return getContext().getThreadManager().runUntilResult(this, () -> {
                final int result = nativeSockets().select(fd + 1,
                        PointerPrimitiveNodes.NULL_POINTER,
                        fdSet.getPointer(),
                        PointerPrimitiveNodes.NULL_POINTER,
                        null);

                if (result == 0) {
                    return null;
                }

                return result;
            });
        }

    }

    @CoreMethod(names = "rb_backref_get", isModuleFunction = true)
    public abstract static class BackRefGet extends CoreMethodNode {

        @Specialization
        @TruffleBoundary
        public Object backRefGet() {
            final ThreadLocalObject storage = RegexpNodes.getMatchDataThreadLocalSearchingStack(getContext());

            if (storage == null) {
                return nil();
            } else {
                return storage.get();
            }
        }

    }

    @CoreMethod(names = "rb_define_hooked_variable_inner", isModuleFunction = true, required = 3)
    public abstract static class DefineHookedVariableInnerNode extends CoreMethodArrayArgumentsNode {

        @TruffleBoundary
        @Specialization(guards = {"isRubySymbol(name)", "isRubyProc(getter)", "isRubyProc(setter)"})
        public DynamicObject defineHookedVariableInnerNode(DynamicObject name, DynamicObject getter, DynamicObject setter) {
            getContext().getCoreLibrary().getGlobalVariables().put(name.toString(), getter, setter);
            return nil();
        }

    }

    @CoreMethod(names = "rb_hash", isModuleFunction = true, required = 1)
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

}
