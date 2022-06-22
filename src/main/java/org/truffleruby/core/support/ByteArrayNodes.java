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

import com.oracle.truffle.api.dsl.Bind;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.object.Shape;
import com.oracle.truffle.api.strings.AbstractTruffleString;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.UnaryCoreMethodNode;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.rope.RopeConstants;
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
                @Cached TruffleString.GetInternalByteArrayNode byteArrayNode) {
            final byte[] bytes = byteArray.bytes;

            var tstring = strings.getTString(string);
            var encoding = strings.getTEncoding(string);
            var stringByteArray = byteArrayNode.execute(tstring, encoding);

            final int prependLength = stringByteArray.getLength();
            final int originalLength = bytes.length;
            final int newLength = prependLength + originalLength;
            final byte[] prependedBytes = new byte[newLength];

            System.arraycopy(stringByteArray.getArray(), stringByteArray.getOffset(), prependedBytes, 0, prependLength);
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
                RubyByteArray destByteArray, int dstStart, RubyString source, int srcStart, int length,
                @Cached TruffleString.CopyToByteArrayNode copyToByteArrayNode) {
            var tstring = source.tstring;
            var encoding = source.encoding.tencoding;
            copyToByteArrayNode.execute(tstring, srcStart, destByteArray.bytes, dstStart, length, encoding);
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
                guards = { "isSingleBytePattern(patternTString, patternEncoding)" })
        protected Object getByteSingleByte(RubyByteArray byteArray, Object pattern, int start, int length,
                @Cached TruffleString.ReadByteNode readByteNode,
                @Cached BranchProfile tooSmallStartProfile,
                @Cached BranchProfile tooLargeStartProfile,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libPattern,
                @Bind("libPattern.getTString(pattern)") AbstractTruffleString patternTString,
                @Bind("libPattern.getTEncoding(pattern)") TruffleString.Encoding patternEncoding) {

            byte[] bytes = byteArray.bytes;
            int searchByte = readByteNode.execute(patternTString, 0, patternEncoding);

            if (start >= length) {
                tooLargeStartProfile.enter();
                return nil;
            }

            if (start < 0) {
                tooSmallStartProfile.enter();
                start = 0;
            }

            final int index = ArrayUtils.indexOf(bytes, start, length, (byte) searchByte);

            return index == -1 ? nil : index + 1;
        }

        @Specialization(
                guards = { "!isSingleBytePattern(patternTString, patternEncoding)" })
        protected Object getByte(RubyByteArray byteArray, Object pattern, int start, int length,
                @Cached TruffleString.CodePointLengthNode codePointLengthNode,
                @Cached TruffleString.CopyToByteArrayNode copyToByteArrayNode,
                @Cached ConditionProfile notFoundProfile,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libPattern,
                @Bind("libPattern.getTString(pattern)") AbstractTruffleString patternTString,
                @Bind("libPattern.getTEncoding(pattern)") TruffleString.Encoding patternEncoding) {
            // TODO (nirvdrum 09-June-2022): Copying the byte array here is wasteful, but ArrayUtils doesn't have a method that works with an offset or length.
            final byte[] patternBytes = copyToByteArrayNode.execute(patternTString, patternEncoding);

            final int index = ArrayUtils.indexOfWithOrMask(byteArray.bytes, start, length, patternBytes, null);

            if (notFoundProfile.profile(index == -1)) {
                return nil;
            } else {
                return index + codePointLengthNode.execute(patternTString, patternEncoding);
            }
        }

        protected boolean isSingleBytePattern(AbstractTruffleString string, TruffleString.Encoding encoding) {
            return string.byteLength(encoding) == 1;
        }
    }

}
