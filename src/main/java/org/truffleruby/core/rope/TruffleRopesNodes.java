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

import com.oracle.truffle.api.strings.TruffleString;
import org.truffleruby.builtins.CoreMethod;
import org.truffleruby.builtins.CoreMethodArrayArgumentsNode;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.core.encoding.Encodings;
import org.truffleruby.core.string.RubyString;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

@CoreModule("Truffle::Ropes")
public abstract class TruffleRopesNodes {

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
