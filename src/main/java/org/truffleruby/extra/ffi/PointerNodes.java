/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra.ffi;

import java.math.BigInteger;

import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.Shape;
import org.jcodings.specific.ASCIIEncoding;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.numeric.BigIntegerOps;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeConstants;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.NotProvided;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocateHelperNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.objects.AllocationTracing;

@CoreModule(value = "Truffle::FFI::Pointer", isClass = true)
public abstract class PointerNodes {

    public static final BigInteger TWO_POW_64 = BigInteger.valueOf(1).shiftLeft(64);

    public static void checkNull(
            Pointer ptr, RubyContext context, Node currentNode, BranchProfile nullPointerProfile) {

        if (ptr.isNull()) {
            nullPointerProfile.enter();
            throw new RaiseException(
                    context,
                    context.getCoreExceptions().ffiNullPointerError(
                            "invalid memory access at address=0x0",
                            currentNode));
        }
    }

    private static abstract class PointerPrimitiveArrayArgumentsNode extends PrimitiveArrayArgumentsNode {

        private final BranchProfile nullPointerProfile = BranchProfile.create();

        protected void checkNull(Pointer ptr) {
            PointerNodes.checkNull(ptr, getContext(), this, nullPointerProfile);
        }

    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public static abstract class AllocateNode extends UnaryCoreMethodNode {

        @Child private AllocateHelperNode allocateNode = AllocateHelperNode.create();

        @Specialization
        protected RubyPointer allocate(RubyClass pointerClass) {
            final Shape shape = allocateNode.getCachedShape(pointerClass);
            final RubyPointer instance = new RubyPointer(pointerClass, shape, Pointer.NULL);
            AllocationTracing.trace(instance, this);
            return instance;
        }

    }

    @Primitive(name = "pointer_find_type_size")
    public static abstract class PointerFindTypeSizePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected int findTypeSize(RubySymbol type) {
            final String typeString = type.getString();
            final int size = typeSize(typeString);
            if (size > 0) {
                return size;
            } else {
                final Object typedef = getContext()
                        .getTruffleNFI()
                        .resolveTypeRaw(getContext().getNativeConfiguration(), typeString);
                final int typedefSize = typeSize(((RubyString) typedef).getJavaString());
                assert typedefSize > 0 : typedef;
                return typedefSize;
            }
        }

        private static int typeSize(String type) {
            switch (type) {
                case "char":
                case "uchar":
                    return 1;
                case "short":
                case "ushort":
                    return 2;
                case "int":
                case "uint":
                case "int32":
                case "uint32":
                case "float":
                    return 4;
                case "long":
                case "ulong":
                case "int64":
                case "uint64":
                case "long_long":
                case "ulong_long":
                case "double":
                case "pointer":
                    return 8;
                default:
                    return -1;
            }
        }

    }

    @CoreMethod(names = "address=", required = 1)
    public static abstract class PointerSetAddressNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected long setAddress(RubyPointer pointer, long address) {
            pointer.pointer = new Pointer(address);
            return address;
        }

    }

    @CoreMethod(names = { "address", "to_i" })
    public static abstract class PointerAddressNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected long address(RubyPointer pointer) {
            return pointer.pointer.getAddress();
        }

    }

    @Primitive(name = "pointer_size")
    public static abstract class PointerSizeNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected long size(RubyPointer pointer) {
            return pointer.pointer.getSize();
        }

    }

    @CoreMethod(names = "total=", required = 1)
    public static abstract class PointerSetSizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected long setSize(RubyPointer pointer, long size) {
            final Pointer old = pointer.pointer;
            pointer.pointer = new Pointer(old.getAddress(), size);
            return size;
        }

    }

    @CoreMethod(names = "autorelease?")
    public static abstract class PointerIsAutoreleaseNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isAutorelease(RubyPointer pointer) {
            return pointer.pointer.isAutorelease();
        }

    }

    @CoreMethod(names = "autorelease=", required = 1)
    public static abstract class PointerSetAutoreleaseNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "autorelease")
        protected boolean enableAutorelease(RubyPointer pointer, boolean autorelease) {
            pointer.pointer.enableAutorelease(getContext().getFinalizationService());
            return autorelease;
        }

        @Specialization(guards = "!autorelease")
        protected boolean disableAutorelease(RubyPointer pointer, boolean autorelease) {
            pointer.pointer.disableAutorelease(getContext().getFinalizationService());
            return autorelease;
        }

    }

    @Primitive(name = "pointer_malloc")
    public static abstract class PointerMallocPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyPointer malloc(RubyPointer pointer, long size) {
            pointer.pointer = Pointer.malloc(size);
            return pointer;
        }

    }

    @CoreMethod(names = "free")
    public static abstract class PointerFreeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyPointer free(RubyPointer pointer) {
            pointer.pointer.free(getContext().getFinalizationService());
            return pointer;
        }

    }

    @Primitive(name = "pointer_clear", lowerFixnum = 1)
    public abstract static class PointerClearNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyPointer clear(RubyPointer pointer, long length) {
            pointer.pointer.writeBytes(0, length, (byte) 0);
            return pointer;
        }

    }

    @Primitive(name = "pointer_copy_memory")
    public static abstract class PointerCopyMemoryNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected Object copyMemory(long to, long from, long size) {
            final Pointer ptr = new Pointer(to);
            checkNull(ptr);
            ptr.writeBytes(0, new Pointer(from), 0, size);
            return nil;
        }

    }

    // Special reads and writes

    @Primitive(name = "pointer_read_string_to_null")
    public static abstract class PointerReadStringToNullNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization(guards = "limit == 0")
        protected RubyString readNullPointer(long address, long limit) {
            final RubyString instance = new RubyString(
                    coreLibrary().stringClass,
                    getLanguage().stringShape,
                    false,
                    true,
                    RopeConstants.EMPTY_ASCII_8BIT_ROPE);
            AllocationTracing.trace(instance, this);
            return instance;
        }

        @Specialization(guards = "limit != 0")
        protected RubyString readStringToNull(long address, long limit,
                @Cached RopeNodes.MakeLeafRopeNode makeLeafRopeNode) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            final byte[] bytes = ptr.readZeroTerminatedByteArray(getContext(), 0, limit);
            final Rope rope = makeLeafRopeNode
                    .executeMake(bytes, ASCIIEncoding.INSTANCE, CodeRange.CR_UNKNOWN, NotProvided.INSTANCE);

            final RubyString instance = new RubyString(
                    coreLibrary().stringClass,
                    getLanguage().stringShape,
                    false,
                    true,
                    rope);
            AllocationTracing.trace(instance, this);
            return instance;
        }

        @Specialization
        protected RubyString readStringToNull(long address, Nil limit,
                @Cached RopeNodes.MakeLeafRopeNode makeLeafRopeNode) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            final byte[] bytes = ptr.readZeroTerminatedByteArray(getContext(), 0);
            final Rope rope = makeLeafRopeNode
                    .executeMake(bytes, ASCIIEncoding.INSTANCE, CodeRange.CR_UNKNOWN, NotProvided.INSTANCE);

            final RubyString instance = new RubyString(
                    coreLibrary().stringClass,
                    getLanguage().stringShape,
                    false,
                    true,
                    rope);
            AllocationTracing.trace(instance, this);
            return instance;
        }

    }

    @Primitive(name = "pointer_read_bytes", lowerFixnum = 1)
    public static abstract class PointerReadBytesNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyString readBytes(long address, int length,
                @Cached ConditionProfile zeroProfile,
                @Cached RopeNodes.MakeLeafRopeNode makeLeafRopeNode) {
            final Pointer ptr = new Pointer(address);
            if (zeroProfile.profile(length == 0)) {
                // No need to check the pointer address if we read nothing
                final RubyString instance = new RubyString(
                        coreLibrary().stringClass,
                        getLanguage().stringShape,
                        false,
                        false,
                        RopeConstants.EMPTY_ASCII_8BIT_ROPE);
                AllocationTracing.trace(instance, this);
                return instance;
            } else {
                checkNull(ptr);
                final byte[] bytes = new byte[length];
                ptr.readBytes(0, bytes, 0, length);
                final Rope rope = makeLeafRopeNode
                        .executeMake(bytes, ASCIIEncoding.INSTANCE, CodeRange.CR_UNKNOWN, NotProvided.INSTANCE);
                final RubyString instance = new RubyString(
                        coreLibrary().stringClass,
                        getLanguage().stringShape,
                        false,
                        true,
                        rope);
                AllocationTracing.trace(instance, this);
                return instance;
            }
        }

    }

    @Primitive(name = "pointer_write_bytes", lowerFixnum = { 2, 3 })
    public static abstract class PointerWriteBytesNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyString writeBytes(long address, RubyString string, int index, int length,
                @Cached RopeNodes.BytesNode bytesNode) {
            final Pointer ptr = new Pointer(address);
            final Rope rope = string.rope;
            assert index + length <= rope.byteLength();
            if (length != 0) {
                // No need to check the pointer address if we write nothing
                checkNull(ptr);
            }

            ptr.writeBytes(0, bytesNode.execute(rope), index, length);
            return string;
        }

    }

    // Reads and writes of number types

    @Primitive(name = "pointer_read_char")
    public static abstract class PointerReadCharNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected int readCharSigned(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return ptr.readByte(0);
        }

    }

    @Primitive(name = "pointer_read_uchar")
    public static abstract class PointerReadUCharNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected int readCharUnsigned(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return Byte.toUnsignedInt(ptr.readByte(0));
        }

    }

    @Primitive(name = "pointer_read_short")
    public static abstract class PointerReadShortNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected int readShortSigned(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return ptr.readShort(0);
        }
    }

    @Primitive(name = "pointer_read_ushort")
    public static abstract class PointerReadUShortNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected int readShortUnsigned(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return Short.toUnsignedInt(ptr.readShort(0));
        }

    }

    @Primitive(name = "pointer_read_int")
    public static abstract class PointerReadIntNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected int readIntSigned(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return ptr.readInt(0);
        }

    }

    @Primitive(name = "pointer_read_uint")
    public static abstract class PointerReadUIntNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected long readIntUnsigned(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return Integer.toUnsignedLong(ptr.readInt(0));
        }

    }

    @Primitive(name = "pointer_read_long")
    public static abstract class PointerReadLongNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected long readLongSigned(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return ptr.readLong(0);
        }

    }

    @Primitive(name = "pointer_read_ulong")
    public static abstract class PointerReadULongNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected Object readLongUnsigned(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return readUnsignedLong(ptr, 0);
        }

        @TruffleBoundary
        private static Object readUnsignedLong(Pointer ptr, int offset) {
            long signedValue = ptr.readLong(offset);
            return BigIntegerOps.asUnsignedFixnumOrBignum(signedValue);
        }
    }

    @Primitive(name = "pointer_read_float")
    public static abstract class PointerReadFloatNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected double readFloat(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return ptr.readFloat(0);
        }

    }

    @Primitive(name = "pointer_read_double")
    public static abstract class PointerReadDoubleNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected double readDouble(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return ptr.readDouble(0);
        }

    }

    @Primitive(name = "pointer_read_pointer")
    public static abstract class PointerReadPointerNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyPointer readPointer(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            final Pointer readPointer = ptr.readPointer(0);
            final RubyPointer instance = new RubyPointer(
                    coreLibrary().truffleFFIPointerClass,
                    getLanguage().truffleFFIPointerShape,
                    readPointer);
            AllocationTracing.trace(instance, this);
            return instance;
        }

    }

    @Primitive(name = "pointer_write_char", lowerFixnum = 1)
    @ImportStatic(Byte.class)
    public static abstract class PointerWriteCharNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization(guards = { "MIN_VALUE <= value", "value <= MAX_VALUE" })
        protected Object writeChar(long address, int value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            byte byteValue = (byte) value;
            ptr.writeByte(0, byteValue);
            return nil;
        }

    }

    @Primitive(name = "pointer_write_uchar", lowerFixnum = 1)
    @ImportStatic(Byte.class)
    public static abstract class PointerWriteUCharNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization(guards = { "0 <= value", "value <= MAX_VALUE" })
        protected Object writeChar(long address, int value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            byte byteValue = (byte) value;
            ptr.writeByte(0, byteValue);
            return nil;
        }

        @Specialization(guards = { "value > MAX_VALUE", "value < 256" })
        protected Object writeUnsignedChar(long address, int value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            byte signed = (byte) value; // Same as value - 2^8
            ptr.writeByte(0, signed);
            return nil;
        }

    }

    @Primitive(name = "pointer_write_short", lowerFixnum = 1)
    @ImportStatic(Short.class)
    public static abstract class PointerWriteShortNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization(guards = { "MIN_VALUE <= value", "value <= MAX_VALUE" })
        protected Object writeShort(long address, int value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            short shortValue = (short) value;
            ptr.writeShort(0, shortValue);
            return nil;
        }

    }

    @Primitive(name = "pointer_write_ushort", lowerFixnum = 1)
    @ImportStatic(Short.class)
    public static abstract class PointerWriteUShortNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization(guards = { "0 <= value", "value <= MAX_VALUE" })
        protected Object writeShort(long address, int value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            short shortValue = (short) value;
            ptr.writeShort(0, shortValue);
            return nil;
        }

        @Specialization(guards = { "value > MAX_VALUE", "value < 65536" })
        protected Object writeUnsignedSort(long address, int value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            short signed = (short) value; // Same as value - 2^16
            ptr.writeShort(0, signed);
            return nil;
        }

    }

    @Primitive(name = "pointer_write_int", lowerFixnum = 1)
    public static abstract class PointerWriteIntNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected Object writeInt(long address, int value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            ptr.writeInt(0, value);
            return nil;
        }

    }

    @Primitive(name = "pointer_write_uint", lowerFixnum = 1)
    @ImportStatic(Integer.class)
    public static abstract class PointerWriteUIntNode extends PointerPrimitiveArrayArgumentsNode {

        static final long MAX_UNSIGNED_INT_PLUS_ONE = 1L << Integer.SIZE;

        @Specialization(guards = "value >= 0")
        protected Object writeInt(long address, int value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            ptr.writeInt(0, value);
            return nil;
        }

        @Specialization(guards = { "value > MAX_VALUE", "value < MAX_UNSIGNED_INT_PLUS_ONE" })
        protected Object writeUnsignedInt(long address, long value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            int signed = (int) value; // Same as value - 2^32
            ptr.writeInt(0, signed);
            return nil;
        }

    }

    @Primitive(name = "pointer_write_long")
    public static abstract class PointerWriteLongNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected Object writeLong(long address, long value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            ptr.writeLong(0, value);
            return nil;
        }

    }

    @Primitive(name = "pointer_write_ulong")
    public static abstract class PointerWriteULongNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization(guards = "value >= 0")
        protected Object writeLong(long address, long value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            ptr.writeLong(0, value);
            return nil;
        }

        @Specialization
        protected Object writeUnsignedLong(long address, RubyBignum value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            writeUnsignedLong(ptr, 0, value);
            return nil;
        }

        @TruffleBoundary
        private static void writeUnsignedLong(Pointer ptr, int offset, RubyBignum value) {
            BigInteger v = value.value;
            assert v.signum() >= 0;
            assert v.compareTo(TWO_POW_64) < 0;
            BigInteger signed = v.subtract(TWO_POW_64);
            ptr.writeLong(offset, signed.longValueExact());
        }

    }

    @Primitive(name = "pointer_write_float")
    public static abstract class PointerWriteFloatNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected Object writeFloat(long address, double value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            ptr.writeFloat(0, (float) value);
            return nil;
        }

    }

    @Primitive(name = "pointer_write_double")
    public static abstract class PointerWriteDoubleNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected Object writeDouble(long address, double value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            ptr.writeDouble(0, value);
            return nil;
        }

    }

    @Primitive(name = "pointer_write_pointer", lowerFixnum = 2)
    public static abstract class PointerWritePointerNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected Object writePointer(long address, long value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            ptr.writePointer(0, value);
            return nil;
        }

    }

    @Primitive(name = "pointer_raw_malloc")
    public static abstract class PointerRawMallocPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected long realloc(long size) {
            return Pointer.rawMalloc(size);
        }

    }

    @Primitive(name = "pointer_raw_realloc")
    public static abstract class PointerRawReallocPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected long malloc(long address, long size) {
            return Pointer.rawRealloc(address, size);
        }

    }

    @Primitive(name = "pointer_raw_free")
    public static abstract class PointerRawFreePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected long free(long address) {
            Pointer.rawFree(address);
            return address;
        }

    }

}
