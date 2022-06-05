/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.support;

import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeConstants;
import org.truffleruby.core.rope.RopeGuards;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.extra.ffi.PointerNodes;
import org.truffleruby.extra.ffi.RubyPointer;
import org.truffleruby.language.Visibility;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.ArrayUtils;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;
import org.truffleruby.language.library.RubyStringLibrary;
import org.truffleruby.language.objects.AllocationTracing;

@CoreModule(value = "Truffle::ByteArray", isClass = true)
public abstract class ByteArrayNodes {

    @CoreMethod(names = { "__allocate__", "__layout_allocate__" }, constructor = true, visibility = Visibility.PRIVATE)
    public abstract static class AllocateNode extends UnaryCoreMethodNode {

        @Specialization
        protected RubyByteArray allocate(RubyClass rubyClass) {
            final Shape shape = getLanguage().byteArrayShape;
            final RubyByteArray instance = new RubyByteArray(rubyClass, shape, RopeConstants.EMPTY_BYTES);
            AllocationTracing.trace(instance, this);
            return instance;
        }
    }

    @CoreMethod(names = "initialize", required = 1, lowerFixnum = 1)
    public abstract static class InitializeNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected RubyByteArray initialize(RubyByteArray byteArray, int size) {
            final byte[] bytes = new byte[size];
            byteArray.bytes = bytes;
            return byteArray;
        }

    }

    @CoreMethod(names = "[]", required = 1, lowerFixnum = 1)
    public abstract static class GetByteNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int getByte(RubyByteArray byteArray, int index) {
            return byteArray.bytes[index] & 0xff;
        }

    }

    @CoreMethod(names = "prepend", required = 1)
    public abstract static class PrependNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = "strings.isRubyString(string)")
        protected RubyByteArray prepend(RubyByteArray byteArray, Object string,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary strings,
                @Cached RopeNodes.BytesNode bytesNode) {
            final byte[] bytes = byteArray.bytes;

            final Rope rope = strings.getRope(string);
            final int prependLength = rope.byteLength();
            final int originalLength = bytes.length;
            final int newLength = prependLength + originalLength;
            final byte[] prependedBytes = new byte[newLength];
            System.arraycopy(bytesNode.execute(rope), 0, prependedBytes, 0, prependLength);
            System.arraycopy(bytes, 0, prependedBytes, prependLength, originalLength);
            final RubyByteArray instance = new RubyByteArray(
                    coreLibrary().byteArrayClass,
                    getLanguage().byteArrayShape,
                    prependedBytes);
            AllocationTracing.trace(instance, this);
            return instance;
        }

    }

    @CoreMethod(names = "[]=", required = 2, lowerFixnum = { 1, 2 })
    public abstract static class SetByteNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected int setByte(RubyByteArray byteArray, int index, int value,
                @Cached BranchProfile errorProfile) {
            final byte[] bytes = byteArray.bytes;
            if (index < 0 || index >= bytes.length) {
                errorProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().indexError("index out of bounds", this));
            }

            bytes[index] = (byte) value;
            return value;
        }

    }

    @CoreMethod(names = "fill", required = 4, lowerFixnum = { 1, 3, 4 })
    public abstract static class FillNode extends CoreMethodArrayArgumentsNode {

        @Specialization
        protected Object fillFromString(
                RubyByteArray byteArray, int dstStart, RubyString source, int srcStart, int length,
                @Cached RopeNodes.BytesNode bytesNode) {
            final Rope rope = source.rope;
            final byte[] bytes = byteArray.bytes;

            System.arraycopy(bytesNode.execute(rope), srcStart, bytes, dstStart, length);
            return source;
        }

        @Specialization
        protected Object fillFromPointer(
                RubyByteArray byteArray, int dstStart, RubyPointer source, int srcStart, int length,
                @Cached BranchProfile nullPointerProfile) {
            assert length > 0;

            final Pointer ptr = source.pointer;
            final byte[] bytes = byteArray.bytes;

            PointerNodes.checkNull(ptr, getContext(), this, nullPointerProfile);

            ptr.readBytes(srcStart, bytes, dstStart, length);
            return source;
        }

    }

    @CoreMethod(names = "locate", required = 3, lowerFixnum = { 2, 3 })
    public abstract static class LocateNode extends CoreMethodArrayArgumentsNode {

        @Specialization(
                guards = { "isSingleBytePattern(libPattern.getRope(pattern))" })
        protected Object getByteSingleByte(RubyByteArray byteArray, Object pattern, int start, int length,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached BranchProfile tooSmallStartProfile,
                @Cached BranchProfile tooLargeStartProfile,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libPattern) {

            final byte[] bytes = byteArray.bytes;
            final Rope rope = libPattern.getRope(pattern);
            final byte searchByte = bytesNode.execute(rope)[0];

            if (start >= length) {
                tooLargeStartProfile.enter();
                return nil();
            }

            if (start < 0) {
                tooSmallStartProfile.enter();
                start = 0;
            }

            final int index = ArrayUtils.indexOf(bytes, start, length, searchByte);

            return index == -1 ? nil() : index + 1;
        }

        @Specialization(
                guards = { "!isSingleBytePattern(libPattern.getRope(pattern))" })
        protected Object getByte(RubyByteArray byteArray, Object pattern, int start, int length,
                @Cached RopeNodes.BytesNode bytesNode,
                @Cached RopeNodes.CharacterLengthNode characterLengthNode,
                @Cached ConditionProfile notFoundProfile,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libPattern) {
            final Rope patternRope = libPattern.getRope(pattern);
            final int index = indexOf(
                    byteArray.bytes,
                    start,
                    length,
                    bytesNode.execute(patternRope));

            if (notFoundProfile.profile(index == -1)) {
                return nil();
            } else {
                return index + characterLengthNode.execute(patternRope);
            }
        }

        protected boolean isSingleBytePattern(Rope rope) {
            return RopeGuards.isSingleByteString(rope);
        }

        public int indexOf(byte[] in, int start, int length, byte[] target) {
            int targetCount = target.length;
            int fromIndex = start;
            if (fromIndex >= length) {
                return (targetCount == 0 ? length : -1);
            }
            if (fromIndex < 0) {
                fromIndex = 0;
            }
            if (targetCount == 0) {
                return fromIndex;
            }

            byte first = target[0];
            int max = length - targetCount;

            for (int i = fromIndex; i <= max; i++) {
                if (in[i] != first) {
                    while (++i <= max && in[i] != first) {
                    }
                }

                if (i <= max) {
                    int j = i + 1;
                    int end = j + targetCount - 1;
                    for (int k = 1; j < end && in[j] == target[k]; j++, k++) {
                    }

                    if (j == end) {
                        return i;
                    }
                }
            }
            return -1;
        }

    }

}
