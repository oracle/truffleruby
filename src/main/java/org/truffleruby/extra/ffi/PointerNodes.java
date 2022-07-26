/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.extra.ffi;

import java.math.BigInteger;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.RubyContext;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.numeric.BigIntegerOps;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.TStringConstants;
import org.truffleruby.core.support.RubyByteArray;
import org.truffleruby.core.numeric.RubyBignum;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.library.RubyStringLibrary;

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

    private abstract static class PointerPrimitiveArrayArgumentsNode extends PrimitiveArrayArgumentsNode {

        private final BranchProfile nullPointerProfile = BranchProfile.create();

        protected void checkNull(Pointer ptr) {
            PointerNodes.checkNull(ptr, getContext(), this, nullPointerProfile);
        }

    }

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @Specialization
        protected RubyPointer allocate(RubyClass pointerClass) {
            final Shape shape = getLanguage().truffleFFIPointerShape;
            final RubyPointer instance = new RubyPointer(pointerClass, shape, Pointer.NULL);
            AllocationTracing.trace(instance, this);
            return instance;
        }

    }

    @Primitive(name = "pointer_find_type_size")
    public abstract static class PointerFindTypeSizePrimitiveNode extends PrimitiveArrayArgumentsNode {

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
                final int typedefSize = typeSize(RubyGuards.getJavaString(typedef));
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
    public abstract static class PointerSetAddressNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected long setAddress(RubyPointer pointer, long address) {
            pointer.pointer = new Pointer(address);
            return address;
        }

    }

    @CoreMethod(names = { "address", "to_i" })
    public abstract static class PointerAddressNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected long address(RubyPointer pointer) {
            return pointer.pointer.getAddress();
        }

    }

    @Primitive(name = "pointer_size")
    public abstract static class PointerSizeNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected long size(RubyPointer pointer) {
            return pointer.pointer.getSize();
        }

    }

    @CoreMethod(names = "total=", required = 1)
    public abstract static class PointerSetSizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected long setSize(RubyPointer pointer, long size) {
            final Pointer old = pointer.pointer;
            pointer.pointer = new Pointer(old.getAddress(), size);
            return size;
        }

    }

    @CoreMethod(names = "autorelease?")
    public abstract static class PointerIsAutoreleaseNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected boolean isAutorelease(RubyPointer pointer) {
            return pointer.pointer.isAutorelease();
        }

    }

    @CoreMethod(names = "autorelease=", required = 1)
    public abstract static class PointerSetAutoreleaseNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "autorelease")
        protected boolean enableAutorelease(RubyPointer pointer, boolean autorelease) {
            pointer.pointer.enableAutorelease(getLanguage());
            return autorelease;
        }

        @Specialization(guards = "!autorelease")
        protected boolean disableAutorelease(RubyPointer pointer, boolean autorelease) {
            pointer.pointer.disableAutorelease();
            return autorelease;
        }

    }

    @Primitive(name = "pointer_malloc")
    public abstract static class PointerMallocPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyPointer malloc(RubyPointer pointer, long size) {
            pointer.pointer = Pointer.malloc(size);
            return pointer;
        }

    }

    @CoreMethod(names = "free")
    public abstract static class PointerFreeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyPointer free(RubyPointer pointer) {
            pointer.pointer.free();
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
    public abstract static class PointerCopyMemoryNode extends PointerPrimitiveArrayArgumentsNode {

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
    public abstract static class PointerReadStringToNullNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization(guards = "limit == 0")
        protected RubyString readNullPointer(long address, long limit) {
            return createString(TStringConstants.EMPTY_BINARY, Encodings.BINARY);
        }

        @Specialization(guards = "limit != 0")
        protected RubyString readStringToNull(long address, long limit,
                @Cached TruffleString.FromByteArrayNode fromByteArrayNode,
                @CachedLibrary(limit = "1") InteropLibrary interop) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            final byte[] bytes = ptr.readZeroTerminatedByteArray(getContext(), interop, 0, limit);
            return createString(fromByteArrayNode, bytes, Encodings.BINARY);
        }

        @Specialization
        protected RubyString readStringToNull(long address, Nil limit,
                @CachedLibrary(limit = "1") InteropLibrary interop,
                @Cached TruffleString.FromByteArrayNode fromByteArrayNode) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            final byte[] bytes = ptr.readZeroTerminatedByteArray(getContext(), interop, 0);
            return createString(fromByteArrayNode, bytes, Encodings.BINARY);
        }

    }

    @Primitive(name = "pointer_read_bytes_to_byte_array", lowerFixnum = { 1, 3 })
    public abstract static class PointerReadBytesToArrayNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected Object readBytes(RubyByteArray array, int arrayOffset, long address, int length,
                @Cached ConditionProfile zeroProfile) {
            final Pointer ptr = new Pointer(address);
            if (zeroProfile.profile(length == 0)) {
                // No need to check the pointer address if we read nothing
                return nil;
            } else {
                checkNull(ptr);
                final byte[] bytes = array.bytes;
                ptr.readBytes(0, bytes, arrayOffset, length);
                return nil;
            }
        }

    }

    @Primitive(name = "pointer_read_bytes", lowerFixnum = 1)
    public abstract static class PointerReadBytesNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected RubyString readBytes(long address, int length,
                @Cached ConditionProfile zeroProfile,
                @Cached TruffleString.FromByteArrayNode fromByteArrayNode) {
            final Pointer ptr = new Pointer(address);
            if (zeroProfile.profile(length == 0)) {
                // No need to check the pointer address if we read nothing
                return createString(TStringConstants.EMPTY_BINARY, Encodings.BINARY);
            } else {
                checkNull(ptr);
                final byte[] bytes = new byte[length];
                ptr.readBytes(0, bytes, 0, length);
                return createString(fromByteArrayNode, bytes, Encodings.BINARY);
            }
        }

    }

    @Primitive(name = "pointer_write_bytes", lowerFixnum = { 2, 3 })
    public abstract static class PointerWriteBytesNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization(guards = "libString.isRubyString(string)", limit = "1")
        protected Object writeBytes(long address, Object string, int index, int length,
                @Cached ConditionProfile nonZeroProfile,
                @Cached TruffleString.CopyToNativeMemoryNode copyToNativeMemoryNode,
                @Cached RubyStringLibrary libString) {
            Pointer ptr = new Pointer(address);
            var tstring = libString.getTString(string);
            var encoding = libString.getTEncoding(string);

            assert index + length <= tstring.byteLength(encoding);

            if (nonZeroProfile.profile(length != 0)) {
                // No need to check the pointer address if we write nothing
                checkNull(ptr);

                copyToNativeMemoryNode.execute(tstring, index, ptr, 0, length, encoding);
            }

            return string;
        }

    }

    // Reads and writes of number types

    @Primitive(name = "pointer_read_char")
    public abstract static class PointerReadCharNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected int readCharSigned(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return ptr.readByte(0);
        }

    }

    @Primitive(name = "pointer_read_uchar")
    public abstract static class PointerReadUCharNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected int readCharUnsigned(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return Byte.toUnsignedInt(ptr.readByte(0));
        }

    }

    @Primitive(name = "pointer_read_short")
    public abstract static class PointerReadShortNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected int readShortSigned(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return ptr.readShort(0);
        }
    }

    @Primitive(name = "pointer_read_ushort")
    public abstract static class PointerReadUShortNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected int readShortUnsigned(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return Short.toUnsignedInt(ptr.readShort(0));
        }

    }

    @Primitive(name = "pointer_read_int")
    public abstract static class PointerReadIntNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected int readIntSigned(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return ptr.readInt(0);
        }

    }

    @Primitive(name = "pointer_read_uint")
    public abstract static class PointerReadUIntNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected long readIntUnsigned(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return Integer.toUnsignedLong(ptr.readInt(0));
        }

    }

    @Primitive(name = "pointer_read_long")
    public abstract static class PointerReadLongNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected long readLongSigned(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return ptr.readLong(0);
        }

    }

    @Primitive(name = "pointer_read_ulong")
    public abstract static class PointerReadULongNode extends PointerPrimitiveArrayArgumentsNode {

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
    public abstract static class PointerReadFloatNode extends PointerPrimitiveArrayArgumentsNode {

        // must return double so Ruby nodes can deal with it
        @Specialization
        protected double readFloat(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return ptr.readFloat(0);
        }

    }

    @Primitive(name = "pointer_read_double")
    public abstract static class PointerReadDoubleNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected double readDouble(long address) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            return ptr.readDouble(0);
        }

    }

    @Primitive(name = "pointer_read_pointer")
    public abstract static class PointerReadPointerNode extends PointerPrimitiveArrayArgumentsNode {

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
    public abstract static class PointerWriteCharNode extends PointerPrimitiveArrayArgumentsNode {

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
    public abstract static class PointerWriteUCharNode extends PointerPrimitiveArrayArgumentsNode {

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
    public abstract static class PointerWriteShortNode extends PointerPrimitiveArrayArgumentsNode {

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
    public abstract static class PointerWriteUShortNode extends PointerPrimitiveArrayArgumentsNode {

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
    public abstract static class PointerWriteIntNode extends PointerPrimitiveArrayArgumentsNode {

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
    public abstract static class PointerWriteUIntNode extends PointerPrimitiveArrayArgumentsNode {

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
    public abstract static class PointerWriteLongNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected Object writeLong(long address, long value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            ptr.writeLong(0, value);
            return nil;
        }

    }

    @Primitive(name = "pointer_write_ulong")
    public abstract static class PointerWriteULongNode extends PointerPrimitiveArrayArgumentsNode {

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
    public abstract static class PointerWriteFloatNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected Object writeFloat(long address, double value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            ptr.writeFloat(0, (float) value);
            return nil;
        }

    }

    @Primitive(name = "pointer_write_double")
    public abstract static class PointerWriteDoubleNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected Object writeDouble(long address, double value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            ptr.writeDouble(0, value);
            return nil;
        }

    }

    @Primitive(name = "pointer_write_pointer", lowerFixnum = 2)
    public abstract static class PointerWritePointerNode extends PointerPrimitiveArrayArgumentsNode {

        @Specialization
        protected Object writePointer(long address, long value) {
            final Pointer ptr = new Pointer(address);
            checkNull(ptr);
            ptr.writePointer(0, value);
            return nil;
        }

    }

    @Primitive(name = "pointer_raw_malloc")
    public abstract static class PointerRawMallocPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected long realloc(long size) {
            return Pointer.rawMalloc(size);
        }

    }

    @Primitive(name = "pointer_raw_realloc")
    public abstract static class PointerRawReallocPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected long malloc(long address, long size) {
            return Pointer.rawRealloc(address, size);
        }

    }

    @Primitive(name = "pointer_raw_free")
    public abstract static class PointerRawFreePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected long free(long address) {
            Pointer.rawFree(address);
            return address;
        }

    }

}
