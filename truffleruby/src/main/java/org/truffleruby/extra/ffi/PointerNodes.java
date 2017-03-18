/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.extra.ffi;

import com.kenai.jffi.MemoryIO;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.TruffleOptions;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import jnr.ffi.Pointer;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeBuilder;
import org.truffleruby.core.rope.RopeConstants;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.objects.AllocateObjectNode;
import org.truffleruby.platform.RubiniusTypes;

@CoreClass("Rubinius::FFI::Pointer")
public abstract class PointerNodes {

    public static final Pointer NULL_POINTER = jnr.ffi.Runtime.getSystemRuntime().getMemoryManager().newOpaquePointer(0);

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public static abstract class AllocateNode extends UnaryCoreMethodNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject allocate(DynamicObject pointerClass) {
            return allocateObjectNode.allocate(pointerClass, NULL_POINTER);
        }

    }

    @Primitive(name = "pointer_malloc")
    public static abstract class PointerMallocPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject malloc(DynamicObject pointerClass, long size) {
            return allocateObjectNode.allocate(pointerClass, memoryManager().newPointer(getContext().getNativePlatform().getMallocFree().malloc(size)));
        }

    }

    @Primitive(name = "pointer_free")
    public static abstract class PointerFreePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public DynamicObject free(DynamicObject pointer) {
            getContext().getNativePlatform().getMallocFree().free(Layouts.POINTER.getPointer(pointer).address());
            return pointer;
        }

    }

    @Primitive(name = "pointer_set_address")
    public static abstract class PointerSetAddressPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public long setAddress(DynamicObject pointer, long address) {
            Layouts.POINTER.setPointer(pointer, memoryManager().newPointer(address));
            return address;
        }

    }

    @Primitive(name = "pointer_add")
    public static abstract class PointerAddPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject add(DynamicObject a, long b) {
            return allocateObjectNode.allocate(Layouts.BASIC_OBJECT.getLogicalClass(a), memoryManager().newPointer(Layouts.POINTER.getPointer(a).address() + b));
        }

    }

    @Primitive(name = "pointer_read_char")
    public static abstract class PointerReadCharPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "signed")
        public int readCharSigned(DynamicObject pointer, boolean signed) {
            return Layouts.POINTER.getPointer(pointer).getByte(0);
        }

        @Specialization(guards = "!signed")
        public int readCharUnsigned(DynamicObject pointer, boolean signed) {
            return Byte.toUnsignedInt(Layouts.POINTER.getPointer(pointer).getByte(0));
        }

    }

    @Primitive(name = "pointer_read_int")
    public static abstract class PointerReadIntPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "signed")
        public int readIntSigned(DynamicObject pointer, boolean signed) {
            return Layouts.POINTER.getPointer(pointer).getInt(0);
        }

        @Specialization(guards = "!signed")
        public long readIntUnsigned(DynamicObject pointer, boolean signed) {
            return Integer.toUnsignedLong(Layouts.POINTER.getPointer(pointer).getInt(0));
        }

    }

    @Primitive(name = "pointer_read_string", lowerFixnum = 1)
    public static abstract class PointerReadStringPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public DynamicObject readString(DynamicObject pointer, int length) {
            final byte[] bytes = new byte[length];
            Layouts.POINTER.getPointer(pointer).get(0, bytes, 0, length);
            return createString(RopeBuilder.createRopeBuilder(bytes));
        }

    }

    @Primitive(name = "pointer_set_autorelease")
    public static abstract class PointerSetAutoreleasePrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public boolean setAutorelease(DynamicObject pointer, boolean autorelease) {
            // TODO CS 24-April-2015 let memory leak
            return autorelease;
        }

    }

    @Primitive(name = "pointer_set_at_offset", lowerFixnum = { 1, 2 })
    @ImportStatic(RubiniusTypes.class)
    public static abstract class PointerSetAtOffsetPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "type == TYPE_INT")
        public int setAtOffsetInt(DynamicObject pointer, int offset, int type, int value) {
            Layouts.POINTER.getPointer(pointer).putInt(offset, value);
            return value;
        }

        @Specialization(guards = "type == TYPE_LONG")
        public long setAtOffsetLong(DynamicObject pointer, int offset, int type, long value) {
            Layouts.POINTER.getPointer(pointer).putLong(offset, value);
            return value;
        }

        @Specialization(guards = "type == TYPE_ULONG")
        public long setAtOffsetULong(DynamicObject pointer, int offset, int type, long value) {
            Layouts.POINTER.getPointer(pointer).putLong(offset, value);
            return value;
        }

        @Specialization(guards = "type == TYPE_ULL")
        public long setAtOffsetULL(DynamicObject pointer, int offset, int type, long value) {
            Layouts.POINTER.getPointer(pointer).putLongLong(offset, value);
            return value;
        }

    }

    @Primitive(name = "pointer_read_pointer")
    public static abstract class PointerReadPointerPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject readPointer(DynamicObject pointer) {
            Pointer ptr = Layouts.POINTER.getPointer(pointer);
            assert ptr.address() != 0 : "Attempt to dereference a null pointer";
            Pointer readPointer = ptr.getPointer(0);

            if (readPointer == null) {
                readPointer = NULL_POINTER;
            }

            return allocateObjectNode.allocate(Layouts.BASIC_OBJECT.getLogicalClass(pointer), readPointer);
        }

    }

    @Primitive(name = "pointer_address")
    public static abstract class PointerAddressPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        public long address(DynamicObject pointer) {
            return Layouts.POINTER.getPointer(pointer).address();
        }

    }

    @Primitive(name = "pointer_get_at_offset", lowerFixnum = { 1, 2 })
    @ImportStatic(RubiniusTypes.class)
    public static abstract class PointerGetAtOffsetPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Child private AllocateObjectNode allocateObjectNode;

        @Specialization(guards = "type == TYPE_CHAR")
        public int getAtOffsetChar(DynamicObject pointer, int offset, int type) {
            return Layouts.POINTER.getPointer(pointer).getByte(offset);
        }

        @Specialization(guards = "type == TYPE_UCHAR")
        public int getAtOffsetUChar(DynamicObject pointer, int offset, int type) {
            return Layouts.POINTER.getPointer(pointer).getByte(offset);
        }

        @Specialization(guards = "type == TYPE_INT")
        public int getAtOffsetInt(DynamicObject pointer, int offset, int type) {
            return Layouts.POINTER.getPointer(pointer).getInt(offset);
        }

        @Specialization(guards = "type == TYPE_SHORT")
        public int getAtOffsetShort(DynamicObject pointer, int offset, int type) {
            return Layouts.POINTER.getPointer(pointer).getShort(offset);
        }

        @Specialization(guards = "type == TYPE_USHORT")
        public int getAtOffsetUShort(DynamicObject pointer, int offset, int type) {
            return Layouts.POINTER.getPointer(pointer).getShort(offset);
        }

        @Specialization(guards = "type == TYPE_LONG")
        public long getAtOffsetLong(DynamicObject pointer, int offset, int type) {
            return Layouts.POINTER.getPointer(pointer).getLong(offset);
        }

        @Specialization(guards = "type == TYPE_ULONG")
        public long getAtOffsetULong(DynamicObject pointer, int offset, int type) {
            return Layouts.POINTER.getPointer(pointer).getLong(offset);
        }

        @Specialization(guards = "type == TYPE_ULL")
        public long getAtOffsetULL(DynamicObject pointer, int offset, int type) {
            return Layouts.POINTER.getPointer(pointer).getLongLong(offset);
        }

        @TruffleBoundary
        @Specialization(guards = "type == TYPE_STRING")
        public DynamicObject getAtOffsetString(DynamicObject pointer, int offset, int type) {
            return createString(StringOperations.encodeRope(Layouts.POINTER.getPointer(pointer).getString(offset), UTF8Encoding.INSTANCE));
        }

        @Specialization(guards = "type == TYPE_PTR")
        public DynamicObject getAtOffsetPointer(DynamicObject pointer, int offset, int type) {
            if (allocateObjectNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                allocateObjectNode = insert(AllocateObjectNode.create());
            }

            final Pointer readPointer = Layouts.POINTER.getPointer(pointer).getPointer(offset);

            if (readPointer == null) {
                return nil();
            } else {
                return allocateObjectNode.allocate(Layouts.BASIC_OBJECT.getLogicalClass(pointer), readPointer);
            }
        }

    }

    @Primitive(name = "pointer_write_string", lowerFixnum = 2)
    public static abstract class PointerWriteStringPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isRubyString(string)")
        public DynamicObject address(DynamicObject pointer, DynamicObject string, int maxLength) {
            final Rope rope = StringOperations.rope(string);
            final int length = Math.min(rope.byteLength(), maxLength);
            Layouts.POINTER.getPointer(pointer).put(0, rope.getBytes(), 0, length);
            return pointer;
        }

    }

    @Primitive(name = "pointer_read_string_to_null")
    public static abstract class PointerReadStringToNullPrimitiveNode extends PrimitiveArrayArgumentsNode {

        @Specialization(guards = "isNullPointer(pointer)")
        public DynamicObject readNullPointer(DynamicObject pointer) {
            return createString(RopeConstants.EMPTY_ASCII_8BIT_ROPE);
        }

        @TruffleBoundary
        @Specialization(guards = "!isNullPointer(pointer)")
        public DynamicObject readStringToNull(DynamicObject pointer) {
            if (TruffleOptions.AOT) {
                final jnr.ffi.Pointer ptr = Layouts.POINTER.getPointer(pointer);
                final int nullOffset = ptr.indexOf(0, (byte) 0);
                final byte[] bytes = new byte[nullOffset];

                ptr.get(0, bytes, 0, bytes.length);

                return StringOperations.createString(getContext(), RopeBuilder.createRopeBuilder(bytes));
            }

            return createString(MemoryIO.getInstance().getZeroTerminatedByteArray(Layouts.POINTER.getPointer(pointer).address()), ASCIIEncoding.INSTANCE);
        }

    }

    @Primitive(name = "pointer_write_short", lowerFixnum = 1)
    public static abstract class PointerWriteShortPrimitiveNode extends PrimitiveArrayArgumentsNode {

        static final int MIN_SHORT = Short.MIN_VALUE;
        static final int MAX_SHORT = Short.MAX_VALUE;

        @Specialization(guards = { "MIN_SHORT <= value", "value <= MAX_SHORT" })
        public DynamicObject writeShort(DynamicObject pointer, int value) {
            short shortValue = (short) value;
            Layouts.POINTER.getPointer(pointer).putShort(0, shortValue);
            return pointer;
        }

        @Specialization(guards = "value > MAX_SHORT")
        public DynamicObject writeUnsignedSort(DynamicObject pointer, int value) {
            assert value < (1L << Short.SIZE);
            short signed = (short) value; // Same as value - 2^16
            Layouts.POINTER.getPointer(pointer).putShort(0, signed);
            return pointer;
        }

    }

    @Primitive(name = "pointer_write_int", lowerFixnum = 1)
    public static abstract class PointerWriteIntPrimitiveNode extends PrimitiveArrayArgumentsNode {

        static final long MAX_INT = Integer.MAX_VALUE;

        @Specialization
        public DynamicObject writeInt(DynamicObject pointer, int value) {
            Layouts.POINTER.getPointer(pointer).putInt(0, value);
            return pointer;
        }

        // Rubinius also uses this primitive for uint32.
        // Values from 0 to MAX_INT are encoded the same as a signed int32.
        // Larger values need to be converted to the corresponding signed negative int.
        @Specialization(guards = "value > MAX_INT")
        public DynamicObject writeUnsigned(DynamicObject pointer, long value) {
            assert value < (1L << Integer.SIZE);
            int signed = (int) value; // Same as value - 2^32
            Layouts.POINTER.getPointer(pointer).putInt(0, signed);
            return pointer;
        }

    }

}
