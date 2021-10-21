/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */

package org.truffleruby.core.cast;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.jcodings.Encoding;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.string.ImmutableRubyString;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.language.RubyBaseNodeWithExecute;

@NodeChild(value = "child", type = RubyBaseNodeWithExecute.class)
public abstract class ToRopeNode extends RubyBaseNodeWithExecute {

    @Specialization
    protected Rope coerceRubyString(RubyString string) {
        return string.rope;
    }

    @Specialization
    protected Rope coerceImmutableRubyString(ImmutableRubyString string) {
        return string.rope;
    }

    @Fallback
    protected Encoding failure(Object value) {
        throw CompilerDirectives.shouldNotReachHere();
    }
}
