/*
 * Copyright (c) 2015, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.cast;

import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.dispatch.DispatchNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
@GenerateInline
@GenerateCached(false)
public abstract class ToStrNode extends RubyBaseNode {

    public abstract Object execute(Node node, Object object);

    @Specialization
    protected static RubyString coerceRubyString(RubyString string) {
        return string;
    }

    @Specialization
    protected static ImmutableRubyString coerceImmutableRubyString(ImmutableRubyString string) {
        return string;
    }

    @Specialization(guards = "isNotRubyString(object)")
    protected static Object coerceObject(Node node, Object object,
            @Cached DispatchNode toStrNode) {
        return toStrNode.call(
                coreLibrary(node).truffleTypeModule,
                "rb_convert_type",
                object,
                coreLibrary(node).stringClass,
                coreSymbols(node).TO_STR);
    }
}
