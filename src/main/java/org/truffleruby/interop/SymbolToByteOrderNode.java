/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.Node;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.control.RaiseException;

import java.nio.ByteOrder;

@GenerateInline
@GenerateCached(false)
public abstract class SymbolToByteOrderNode extends RubyBaseNode {

    public abstract ByteOrder execute(Node node, Object value);

    @Specialization(guards = "symbol == coreSymbols().BIG")
    protected static ByteOrder symbolToByteOrderBig(RubySymbol symbol) {
        return ByteOrder.BIG_ENDIAN;
    }

    @Specialization(guards = "symbol == coreSymbols().LITTLE")
    protected static ByteOrder symbolToByteOrderLittle(RubySymbol symbol) {
        return ByteOrder.LITTLE_ENDIAN;
    }

    @Specialization(guards = "symbol == coreSymbols().NATIVE")
    protected static ByteOrder symbolToByteOrderNative(RubySymbol symbol) {
        return ByteOrder.nativeOrder();
    }

    @Fallback
    protected static ByteOrder invalidByteOrder(Node node, Object value) {
        throw new RaiseException(
                getContext(node),
                coreExceptions(node).argumentError("byte order must be :big, :little, or :native symbol", node));
    }
}
