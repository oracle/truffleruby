/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 *
 *
 * Some of the code in this class is modified from org.jruby.util.StringSupport,
 * licensed under the same EPL 2.0/GPL 2.0/LGPL 2.1 used throughout.
 */

package org.truffleruby.core.rope;

import com.oracle.truffle.api.dsl.Cached.Exclusive;
import com.oracle.truffle.api.strings.TruffleString;
import org.jcodings.Encoding;
import org.truffleruby.core.string.StringSupport;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.profiles.BranchProfile;
import com.oracle.truffle.api.profiles.ConditionProfile;

public abstract class RopeNodes {

    @ImportStatic(TruffleString.CodeRange.class)
    @GenerateUncached
    public abstract static class CalculateCharacterLengthNode extends RubyBaseNode {

        public static CalculateCharacterLengthNode create() {
            return RopeNodesFactory.CalculateCharacterLengthNodeGen.create();
        }

        protected abstract int executeLength(Encoding encoding, TruffleString.CodeRange codeRange, Bytes bytes);

        /** This method returns the byte length for the first character encountered in `bytes`. The validity of a
         * character is defined by the `encoding`. If the `codeRange` for the byte sequence is known for the supplied
         * `encoding`, it should be passed to help short-circuit some validation checks. If the `codeRange` is not known
         * for the supplied `encoding`, then `BROKEN // UNKNOWN` should be passed. If the byte sequence is invalid, a
         * negative value will be returned. See `Encoding#length` for details on how to interpret the return value. */
        public int characterLength(Encoding encoding, TruffleString.CodeRange codeRange, Bytes bytes) {
            return executeLength(encoding, codeRange, bytes);
        }

        @Specialization(guards = "codeRange == ASCII")
        protected int cr7Bit(Encoding encoding, TruffleString.CodeRange codeRange, Bytes bytes) {
            assert bytes.length > 0;
            return 1;
        }

        @Specialization(guards = { "codeRange == VALID", "encoding.isUTF8()" })
        protected int validUtf8(Encoding encoding, TruffleString.CodeRange codeRange, Bytes bytes,
                @Cached @Exclusive BranchProfile oneByteProfile,
                @Cached @Exclusive BranchProfile twoBytesProfile,
                @Cached @Exclusive BranchProfile threeBytesProfile,
                @Cached @Exclusive BranchProfile fourBytesProfile) {
            final byte b = bytes.get(0);
            final int ret;

            if (b >= 0) {
                oneByteProfile.enter();
                ret = 1;
            } else {
                switch (b & 0xf0) {
                    case 0xe0:
                        threeBytesProfile.enter();
                        ret = 3;
                        break;
                    case 0xf0:
                        fourBytesProfile.enter();
                        ret = 4;
                        break;
                    default:
                        twoBytesProfile.enter();
                        ret = 2;
                        break;
                }
            }

            return ret;
        }

        @Specialization(guards = { "codeRange == VALID", "encoding.isAsciiCompatible()" })
        protected int validAsciiCompatible(Encoding encoding, TruffleString.CodeRange codeRange, Bytes bytes,
                @Cached @Exclusive ConditionProfile asciiCharProfile) {
            if (asciiCharProfile.profile(bytes.get(0) >= 0)) {
                return 1;
            } else {
                return encodingLength(encoding, bytes);
            }
        }

        @Specialization(guards = { "codeRange == VALID", "encoding.isFixedWidth()" })
        protected int validFixedWidth(Encoding encoding, TruffleString.CodeRange codeRange, Bytes bytes) {
            final int width = encoding.minLength();
            assert bytes.length >= width;
            return width;
        }

        @Specialization(
                guards = {
                        "codeRange == VALID",
                        /* UTF-8 is ASCII-compatible, so we don't need to check the encoding is not UTF-8 here. */
                        "!encoding.isAsciiCompatible()",
                        "!encoding.isFixedWidth()" })
        protected int validGeneral(Encoding encoding, TruffleString.CodeRange codeRange, Bytes bytes) {
            return encodingLength(encoding, bytes);
        }

        @Specialization(guards = { "codeRange == BROKEN" /* or UNKNOWN */ })
        protected int brokenOrUnknownWithoutRecovery(Encoding encoding, TruffleString.CodeRange codeRange, Bytes bytes,
                @Cached ConditionProfile validCharWidthProfile) {

            final int width = encodingLength(encoding, bytes);

            if (validCharWidthProfile.profile(width <= bytes.length)) {
                return width;
            } else {
                return StringSupport.MBCLEN_NEEDMORE(width - bytes.length);
            }
        }

        @TruffleBoundary
        private int encodingLength(Encoding encoding, Bytes bytes) {
            return encoding.length(bytes.array, bytes.offset, bytes.offset + bytes.length);
        }

    }

}
