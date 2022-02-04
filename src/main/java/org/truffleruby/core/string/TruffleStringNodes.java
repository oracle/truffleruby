/*
 * Copyright (c) 2016, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeNodes;
import org.truffleruby.language.control.RaiseException;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

@CoreModule("Truffle::StringOperations")
public class TruffleStringNodes {

    @CoreMethod(names = "truncate", onSingleton = true, required = 2, lowerFixnum = 2)
    public abstract static class TruncateNode extends CoreMethodArrayArgumentsNode {

        @Specialization(guards = { "newByteLength < 0" })
        @TruffleBoundary
        protected RubyString truncateLengthNegative(RubyString string, int newByteLength) {
            throw new RaiseException(
                    getContext(),
                    getContext().getCoreExceptions().argumentError(formatNegativeError(newByteLength), this));
        }

        @Specialization(
                guards = { "newByteLength >= 0", "isNewLengthTooLarge(string, newByteLength)" })
        @TruffleBoundary
        protected RubyString truncateLengthTooLong(RubyString string, int newByteLength) {
            throw new RaiseException(
                    getContext(),
                    coreExceptions().argumentError(formatTooLongError(newByteLength, string.rope), this));
        }

        @Specialization(
                guards = {
                        "newByteLength >= 0",
                        "!isNewLengthTooLarge(string, newByteLength)" })
        protected RubyString stealStorage(RubyString string, int newByteLength,
                @Cached RopeNodes.SubstringNode substringNode) {
            string.setRope(substringNode.executeSubstring(string.rope, 0, newByteLength));
            return string;
        }

        protected static boolean isNewLengthTooLarge(RubyString string, int newByteLength) {
            return newByteLength > string.rope.byteLength();
        }

        @TruffleBoundary
        private String formatNegativeError(int count) {
            return StringUtils.format("Invalid byte count: %d is negative", count);
        }

        @TruffleBoundary
        private String formatTooLongError(int count, final Rope rope) {
            return StringUtils
                    .format("Invalid byte count: %d exceeds string size of %d bytes", count, rope.byteLength());
        }

    }
}
