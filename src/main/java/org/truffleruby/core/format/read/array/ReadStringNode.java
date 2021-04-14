/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.format.read.array;

import org.truffleruby.core.array.ArrayGuards;
import org.truffleruby.core.format.TruncateStringNode;
import org.truffleruby.core.format.TruncateStringNodeGen;
import org.truffleruby.core.array.library.ArrayStoreLibrary;
import org.truffleruby.core.format.FormatNode;
import org.truffleruby.core.format.LiteralFormatNode;
import org.truffleruby.core.format.convert.ToStringNode;
import org.truffleruby.core.format.convert.ToStringNodeGen;
import org.truffleruby.core.format.read.SourceNode;
import org.truffleruby.core.format.write.bytes.WriteByteNodeGen;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.library.CachedLibrary;

@NodeChild(value = "source", type = SourceNode.class)
@ImportStatic(ArrayGuards.class)
public abstract class ReadStringNode extends FormatNode {

    private final boolean convertNumbersToStrings;
    private final String conversionMethod;
    private final boolean inspectOnConversionFailure;
    private final Object valueOnNil;
    private final Integer precision;

    @Child private ToStringNode toStringNode;
    @Child private TruncateStringNode truncateStringNode;

    public ReadStringNode(
            boolean convertNumbersToStrings,
            String conversionMethod,
            boolean inspectOnConversionFailure,
            Object valueOnNil,
            Integer precision) {
        this.convertNumbersToStrings = convertNumbersToStrings;
        this.conversionMethod = conversionMethod;
        this.inspectOnConversionFailure = inspectOnConversionFailure;
        this.valueOnNil = valueOnNil;
        this.precision = precision;
    }

    @Specialization(limit = "storageStrategyLimit()")
    protected Object read(VirtualFrame frame, Object source,
            @CachedLibrary("source") ArrayStoreLibrary sources) {
        return readAndConvert(frame, sources.read(source, advanceSourcePosition(frame)));
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

        if (this.precision != null) {
            if (truncateStringNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                this.truncateStringNode = insert(TruncateStringNodeGen.create(precision, null));
            }
            value = truncateStringNode.execute(value);
        }

        return toStringNode.executeToString(frame, value);
    }

}
