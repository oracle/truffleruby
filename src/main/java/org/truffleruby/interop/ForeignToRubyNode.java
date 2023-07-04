/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.GenerateCached;
import com.oracle.truffle.api.dsl.GenerateInline;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.language.RubyBaseNode;

import com.oracle.truffle.api.dsl.GenerateUncached;
import com.oracle.truffle.api.dsl.Specialization;

@GenerateUncached
@GenerateInline(inlineByDefault = true)
@GenerateCached
public abstract class ForeignToRubyNode extends RubyBaseNode {

    public final Object executeCached(Object value) {
        return execute(this, value);
    }

    public abstract Object execute(Node node, Object value);

    @Specialization
    protected static int convertByte(byte value) {
        return value;
    }

    @Specialization
    protected static int convertShort(short value) {
        return value;
    }

    @Specialization
    protected static double convertFloat(float value) {
        return value;
    }

    @Fallback
    protected static Object convert(Object value) {
        return value;
    }

}
