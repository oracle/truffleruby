/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.rubinius;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreClass;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.collections.ByteArrayBuilder;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.AllocateObjectNode;

@CoreClass("Rubinius::ByteArray")
public abstract class ByteArrayNodes {

    public static DynamicObject createByteArray(DynamicObjectFactory factory, ByteArrayBuilder bytes) {
        return Layouts.BYTE_ARRAY.createByteArray(factory, bytes);
    }

    @CoreMethod(names = {"get_byte", "[]"}, required = 1, lowerFixnum = 1)
    public abstract static class GetByteNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int getByte(DynamicObject bytes, int index,
                              @Cached("createBinaryProfile()") ConditionProfile nullByteIndexProfile) {
            final ByteArrayBuilder builder = Layouts.BYTE_ARRAY.getBytes(bytes);

            // Handling out-of-bounds issues like this is non-standard. In Rubinius, it would raise an exception instead.
            // We're modifying the semantics to address a primary use case for this class: Rubinius's @data array
            // in the String class. Rubinius Strings are NULL-terminated and their code working with Strings takes
            // advantage of that fact. So, where they expect to receive a NULL byte, we'd be out-of-bounds and raise
            // an exception. Simply appending a NULL byte may trigger a full copy of the original byte[], which we
            // want to avoid. The compromise is bending on the semantics here.
            if (nullByteIndexProfile.profile(index == builder.getLength())) {
                return 0;
            }

            return builder.get(index) & 0xff;
        }

    }

    @CoreMethod(names = "prepend", required = 1)
    public abstract static class PrependNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyString(string)")
        public DynamicObject prepend(DynamicObject bytes, DynamicObject string,
                @Cached("create()") RopeNodes.BytesNode bytesNode) {
            final Rope rope = StringOperations.rope(string);
            final int prependLength = rope.byteLength();
            final int originalLength = Layouts.BYTE_ARRAY.getBytes(bytes).getUnsafeBytes().length;
            final int newLength = prependLength + originalLength;
            final byte[] prependedBytes = new byte[newLength];
            System.arraycopy(bytesNode.execute(rope), 0, prependedBytes, 0, prependLength);
            System.arraycopy(Layouts.BYTE_ARRAY.getBytes(bytes).getUnsafeBytes(), 0, prependedBytes, prependLength, originalLength);
            return ByteArrayNodes.createByteArray(coreLibrary().getByteArrayFactory(), ByteArrayBuilder.createUnsafeBuilder(prependedBytes));
        }

    }

    @CoreMethod(names = {"set_byte", "[]="}, required = 2, lowerFixnum = { 1, 2 })
    public abstract static class SetByteNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public Object setByte(DynamicObject bytes, int index, int value,
                @Cached("create()") BranchProfile errorProfile) {
            if (index < 0 || index >= Layouts.BYTE_ARRAY.getBytes(bytes).getLength()) {
                errorProfile.enter();
                throw new RaiseException(coreExceptions().indexError("index out of bounds", this));
            }

            Layouts.BYTE_ARRAY.getBytes(bytes).set(index, value);
            return Layouts.BYTE_ARRAY.getBytes(bytes).get(index);
        }

    }

    @CoreMethod(names = "fill", required = 4, lowerFixnum = { 1, 3, 4 })
    public abstract static class FillNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyString(string)")
        public Object fill(DynamicObject byteArray, int dstStart, DynamicObject string, int srcStart, int length,
                @Cached("create()") RopeNodes.BytesNode bytesNode) {
            final Rope rope = StringOperations.rope(string);
            final ByteArrayBuilder bytes = Layouts.BYTE_ARRAY.getBytes(byteArray);

            System.arraycopy(bytesNode.execute(rope), srcStart, bytes.getUnsafeBytes(), dstStart, length);
            return string;
        }

    }

    @CoreMethod(names = "size")
    public abstract static class SizeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int size(DynamicObject bytes) {
            return Layouts.BYTE_ARRAY.getBytes(bytes).getLength();
        }

    }

    @CoreMethod(names = "length=", required = 1)
    public abstract static class SetLengthNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        public int setLength(DynamicObject bytes, int length) {
            Layouts.BYTE_ARRAY.getBytes(bytes).setLength(length);
            return length;
        }

    }

    @CoreMethod(names = "locate", required = 3, lowerFixnum = { 2, 3 })
    public abstract static class LocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "isRubyString(pattern)")
        public Object getByte(DynamicObject bytes, DynamicObject pattern, int start, int length) {
            final Rope patternRope = StringOperations.rope(pattern);
            final int index = indexOf(Layouts.BYTE_ARRAY.getBytes(bytes), start, length, patternRope);

            if (index == -1) {
                return nil();
            } else {
                return index + patternRope.characterLength();
            }
        }

        public int indexOf(ByteArrayBuilder in, int start, int length, Rope find) {
            byte[] target = find.getBytes();
            int targetCount = find.byteLength();
            int fromIndex = start;
            if (fromIndex >= length) return (targetCount == 0 ? length : -1);
            if (fromIndex < 0) fromIndex = 0;
            if (targetCount == 0) return fromIndex;

            byte first  = target[0];
            int max = length - targetCount;

            for (int i = fromIndex; i <= max; i++) {
                if (in.get(i) != first) {
                    while (++i <= max && in.get(i) != first) {
                    }
                }

                if (i <= max) {
                    int j = i + 1;
                    int end = j + targetCount - 1;
                    for (int k = 1; j < end && in.get(j) == target[k]; j++, k++) {
                    }

                    if (j == end) return i;
                }
            }
            return -1;
        }

    }

    @CoreMethod(names = "__allocate__", constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @Child private AllocateObjectNode allocateObjectNode = AllocateObjectNode.create();

        @Specialization
        public DynamicObject allocate(DynamicObject rubyClass) {
            return allocateObjectNode.allocate(rubyClass, new ByteArrayBuilder());
        }

    }

}
