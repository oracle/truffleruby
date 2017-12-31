/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.tracepoint;

import org.truffleruby.RubyContext;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.language.yield.YieldNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.instrumentation.EventContext;
import com.oracle.truffle.api.instrumentation.ExecutionEventNode;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class TraceBaseEventNode extends ExecutionEventNode {

    protected final ConditionProfile inTraceFuncProfile = ConditionProfile.createBinaryProfile();

    protected final RubyContext context;
    protected final EventContext eventContext;

    @CompilationFinal private DynamicObject file;
    @CompilationFinal private int line;

    @Child private YieldNode yieldNode;

    public TraceBaseEventNode(RubyContext context, EventContext eventContext) {
        this.context = context;
        this.eventContext = eventContext;
    }

    protected DynamicObject getFile() {
        if (file == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            file = StringOperations.createString(context, context.getRopeTable().getRopeUTF8(eventContext.getInstrumentedSourceSection().getSource().getName()));
        }
        return file;
    }

    protected int getLine() {
        if (line == 0) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            line = eventContext.getInstrumentedSourceSection().getStartLine();
        }
        return line;
    }

    protected Object yield(DynamicObject block, Object... arguments) {
        if (yieldNode == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            yieldNode = insert(new YieldNode());
        }

        return yieldNode.dispatch(block, arguments);
    }

}
