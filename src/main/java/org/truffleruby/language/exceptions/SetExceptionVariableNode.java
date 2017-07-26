/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.exceptions;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.RubyContext;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.control.RaiseException;
import org.truffleruby.language.objects.ReadObjectFieldNode;
import org.truffleruby.language.objects.ReadObjectFieldNodeGen;
import org.truffleruby.language.objects.WriteObjectFieldNode;
import org.truffleruby.language.objects.WriteObjectFieldNodeGen;
import org.truffleruby.language.threadlocal.GetThreadLocalsObjectNode;
import org.truffleruby.language.threadlocal.GetThreadLocalsObjectNodeGen;

public class SetExceptionVariableNode extends Node {

    private final RubyContext context;

    @Child private GetThreadLocalsObjectNode getThreadLocalsObjectNode;
    @Child private ReadObjectFieldNode readDollarBang;
    @Child private WriteObjectFieldNode writeDollarBang;

    public SetExceptionVariableNode(RubyContext context) {
        this.context = context;
    }

    public Object setLastExceptionAndRun(VirtualFrame frame, RaiseException exception, RubyNode node) {
        final DynamicObject threadLocals = getThreadLocalsObject(frame);

        final Object lastException = readDollarBang(threadLocals);
        writeDollarBang(threadLocals, exception.getException());

        try {
            return node.execute(frame);
        } finally {
            writeDollarBang(threadLocals, lastException);
        }
    }

    public void setLastException(VirtualFrame frame, DynamicObject exception) {
        final DynamicObject threadLocals = getThreadLocalsObject(frame);
        writeDollarBang(threadLocals, exception);
    }

    private DynamicObject getThreadLocalsObject(VirtualFrame frame) {
        if (getThreadLocalsObjectNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            getThreadLocalsObjectNode = insert(GetThreadLocalsObjectNodeGen.create());
        }

        return getThreadLocalsObjectNode.executeGetThreadLocalsObject(frame);
    }

    private void writeDollarBang(DynamicObject threadLocals, Object value) {
        if (writeDollarBang == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            writeDollarBang = insert(WriteObjectFieldNodeGen.create("$!"));
        }

        writeDollarBang.write(threadLocals, value);
    }

    private Object readDollarBang(DynamicObject threadLocals) {
        if (readDollarBang == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            readDollarBang = insert(ReadObjectFieldNodeGen.create("$!", context.getCoreLibrary().getNil()));
        }

        return readDollarBang.execute(threadLocals);
    }

}
