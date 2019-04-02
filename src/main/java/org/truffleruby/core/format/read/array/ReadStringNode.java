/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.read.array;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;

import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.array.ArrayOperationNodes;
import org.truffleruby.core.array.ArrayStrategy;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.LiteralFormatNode;
import org.truffleruby.core.format.convert.ToStringNode;
import org.truffleruby.core.format.convert.ToStringNodeGen;
import org.truffleruby.core.format.read.SourceNode;
import org.truffleruby.core.format.write.bytes.WriteByteNodeGen;

@NodeChild(value = "source", type = SourceNode.class)
@ImportStatic(ArrayGuards.class)
public abstract class ReadStringNode extends FormatNode {

    private final boolean convertNumbersToStrings;
    private final String conversionMethod;
    private final boolean inspectOnConversionFailure;
    private final Object valueOnNil;

    @Child private ToStringNode toStringNode;

    public ReadStringNode(boolean convertNumbersToStrings,
                          String conversionMethod, boolean inspectOnConversionFailure,
                          Object valueOnNil) {
        this.convertNumbersToStrings = convertNumbersToStrings;
        this.conversionMethod = conversionMethod;
        this.inspectOnConversionFailure = inspectOnConversionFailure;
        this.valueOnNil = valueOnNil;
    }

    @Specialization(guards = "isNull(source)")
    public Object read(VirtualFrame frame, Object source) {
        advanceSourcePosition(frame);
        throw new IllegalStateException();
    }

    @Specialization
    public Object read(VirtualFrame frame, int[] source) {
        return readAndConvert(frame, source[advanceSourcePosition(frame)]);
    }

    @Specialization
    public Object read(VirtualFrame frame, long[] source) {
        return readAndConvert(frame, source[advanceSourcePosition(frame)]);
    }

    @Specialization
    public Object read(VirtualFrame frame, double[] source) {
        return readAndConvert(frame, source[advanceSourcePosition(frame)]);
    }

    @Specialization(guards = "strategy.matchesStore(source)", limit = "STORAGE_STRATEGIES")
    public Object read(VirtualFrame frame, Object source,
            @Cached("ofStore(source)") ArrayStrategy strategy,
            @Cached("strategy.getNode()") ArrayOperationNodes.ArrayGetNode getNode) {
        return readAndConvert(frame, getNode.execute(source, advanceSourcePosition(frame)));
    }

    private Object readAndConvert(VirtualFrame frame, Object value) {
        if (toStringNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            toStringNode = insert(ToStringNodeGen.create(
                    convertNumbersToStrings,
                    conversionMethod,
                    inspectOnConversionFailure,
                    valueOnNil,
                    WriteByteNodeGen.create(new LiteralFormatNode((byte) 0))));
        }

        return toStringNode.executeToString(frame, value);
    }

}
