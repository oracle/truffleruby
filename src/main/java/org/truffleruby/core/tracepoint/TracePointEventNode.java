/*
 * Copyright (c) 2016, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.tracepoint;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.core.binding.BindingNodes;
import org.truffleruby.core.thread.GetCurrentRubyThreadNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.EventContext;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;

class TracePointEventNode extends TraceBaseEventNode {

    @Child private GetCurrentRubyThreadNode getCurrentRubyThreadNode = GetCurrentRubyThreadNode.create();

    private final ConditionProfile inTracePointProfile = ConditionProfile.create();

    private final DynamicObject tracePoint;
    private final DynamicObject proc;
    private final DynamicObject event;

    public TracePointEventNode(
            RubyContext context,
            EventContext eventContext,
            DynamicObject tracePoint,
            DynamicObject event) {
        super(context, eventContext);
        this.tracePoint = tracePoint;
        this.proc = Layouts.TRACE_POINT.getProc(tracePoint);
        this.event = event;
    }

    @Override
    protected void onEnter(VirtualFrame frame) {
        final DynamicObject rubyThread = getCurrentRubyThreadNode.execute();
        final TracePointState state = Layouts.THREAD.getTracePointState(rubyThread);

        if (inTracePointProfile.profile(state.insideProc)) {
            return;
        }

        state.event = event;
        state.path = getFile();
        state.line = getLine();
        state.binding = BindingNodes
                .createBinding(context, frame.materialize(), eventContext.getInstrumentedSourceSection());

        state.insideProc = true;
        try {
            yield(proc, tracePoint);
        } finally {
            state.insideProc = false;
            // Reset the binding so it can be escaped analyzed. This is also semantically correct
            // because the TracePoint information is reset outside the TracePoint block in MRI.
            state.binding = null;
        }
    }

}
