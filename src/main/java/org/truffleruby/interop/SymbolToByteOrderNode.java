/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import org.truffleruby.core.symbol.RubySymbol;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;

import java.nio.ByteOrder;

@NodeChild(value = "valueNode", type = RubyNode.class)
public abstract class SymbolToByteOrderNode extends RubyContextSourceNode {

    public static SymbolToByteOrderNode create(RubyNode value) {
        return SymbolToByteOrderNodeGen.create(value);
    }

    public abstract RubyNode getValueNode();

    @Specialization(guards = "symbol == coreSymbols().BIG")
    protected ByteOrder symbolToByteOrderBig(RubySymbol symbol) {
        return ByteOrder.BIG_ENDIAN;
    }

    @Specialization(guards = "symbol == coreSymbols().LITTLE")
    protected ByteOrder symbolToByteOrderLittle(RubySymbol symbol) {
        return ByteOrder.LITTLE_ENDIAN;
    }

    @Specialization(guards = "symbol == coreSymbols().NATIVE")
    protected ByteOrder symbolToByteOrderNative(RubySymbol symbol) {
        return ByteOrder.nativeOrder();
    }

    @Fallback
    protected ByteOrder invalidByteOrder(Object value) {
        throw new RaiseException(
                getContext(),
                coreExceptions().argumentError("byte order must be :big, :little, or :native symbol", this));
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = SymbolToByteOrderNodeGen.create(getValueNode().cloneUninitialized());
        copy.copyFlags(this);
        return copy;
    }

}
