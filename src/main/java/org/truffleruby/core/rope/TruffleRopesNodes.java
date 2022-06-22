/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.encoding.TStringUtils;
import org.truffleruby.core.string.RubyString;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.language.library.RubyStringLibrary;

@CoreModule("Truffle::Ropes")
public abstract class TruffleRopesNodes {

    @CoreMethod(names = "flatten_rope", onSingleton = true, required = 1)
    public abstract static class FlattenRopeNode extends CoreMethodArrayArgumentsNode {

        // Also flattens the original String, but that one might still have an offset
        @TruffleBoundary
        @Specialization(guards = "libString.isRubyString(string)")
        protected RubyString flattenRope(Object string,
                @Cached TruffleString.FromByteArrayNode fromByteArrayNode,
                @CachedLibrary(limit = "LIBSTRING_CACHE") RubyStringLibrary libString) {
            final RubyEncoding rubyEncoding = libString.getEncoding(string);
            var tstring = libString.getTString(string);
            // Use GetInternalByteArrayNode as a way to flatten the TruffleString.
            // Ensure the result has offset = 0 and length = byte[].length for image build time checks
            byte[] byteArray = TStringUtils.getBytesOrCopy(tstring, rubyEncoding);
            return createString(fromByteArrayNode, byteArray, rubyEncoding);
        }

    }

    /* Truffle.create_simple_string creates a string 'test' without any part of the string escaping. Useful for testing
     * compilation of String because most other ways to construct a string can currently escape. */
    @CoreMethod(names = "create_simple_string", onSingleton = true)
    public abstract static class CreateSimpleStringNode extends CoreMethodArrayArgumentsNode {
        @Specialization
        protected RubyString createSimpleString(
                @Cached TruffleString.FromByteArrayNode fromByteArrayNode) {
            return createString(fromByteArrayNode, new byte[]{ 't', 'e', 's', 't' }, Encodings.UTF_8);
        }
    }

}
