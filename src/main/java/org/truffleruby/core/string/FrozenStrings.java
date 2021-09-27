/*
 * Copyright (c) 2021, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import org.jcodings.specific.ASCIIEncoding;
import org.truffleruby.core.rope.CodeRange;
import org.truffleruby.core.rope.LeafRope;

import java.util.ArrayList;
import java.util.List;

import static org.truffleruby.core.encoding.Encodings.BINARY;

public class FrozenStrings {

    public static final List<LeafRope> ROPES = new ArrayList<>();
    public static final ImmutableRubyString YIELD = createFrozenStaticBinaryString("yield");
    public static final ImmutableRubyString ASSIGNMENT = createFrozenStaticBinaryString("assignment");
    public static final ImmutableRubyString CLASS_VARIABLE = createFrozenStaticBinaryString("class variable");
    public static final ImmutableRubyString CONSTANT = createFrozenStaticBinaryString("constant");
    public static final ImmutableRubyString EXPRESSION = createFrozenStaticBinaryString("expression");
    public static final ImmutableRubyString FALSE = createFrozenStaticBinaryString("false");
    public static final ImmutableRubyString GLOBAL_VARIABLE = createFrozenStaticBinaryString("global-variable");
    public static final ImmutableRubyString INSTANCE_VARIABLE = createFrozenStaticBinaryString("instance-variable");
    public static final ImmutableRubyString LOCAL_VARIABLE = createFrozenStaticBinaryString("local-variable");
    public static final ImmutableRubyString METHOD = createFrozenStaticBinaryString("method");
    public static final ImmutableRubyString NIL = createFrozenStaticBinaryString("nil");
    public static final ImmutableRubyString SELF = createFrozenStaticBinaryString("self");
    public static final ImmutableRubyString SUPER = createFrozenStaticBinaryString("super");
    public static final ImmutableRubyString TRUE = createFrozenStaticBinaryString("true");

    private static ImmutableRubyString createFrozenStaticBinaryString(String string) {
        // defined?(...) returns frozen strings with a binary encoding
        final LeafRope rope = StringOperations.encodeRope(string, ASCIIEncoding.INSTANCE, CodeRange.CR_7BIT);
        ROPES.add(rope);
        return FrozenStringLiterals.createStringAndCacheLater(rope, BINARY);
    }


}
