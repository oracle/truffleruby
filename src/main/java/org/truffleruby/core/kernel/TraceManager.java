/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.kernel;

import java.util.ArrayList;
import java.util.Collection;

import org.truffleruby.RubyContext;
import org.truffleruby.core.binding.BindingNodes;
import org.truffleruby.core.string.StringOperations;
import org.truffleruby.core.tracepoint.TraceBaseEventNode;
import org.truffleruby.language.RubyGuards;
import org.truffleruby.language.arguments.RubyArguments;
import org.truffleruby.language.objects.LogicalClassNode;
import org.truffleruby.shared.TruffleRuby;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.EventBinding;
import com.oracle.truffle.api.instrumentation.EventContext;
import com.oracle.truffle.api.instrumentation.Instrumenter;
import com.oracle.truffle.api.instrumentation.SourceSectionFilter;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.utilities.CyclicAssumption;

public class TraceManager {
    public static class LineTag extends Tag {
    }

    public static class CallTag extends Tag {
    }

    public static class ClassTag extends Tag {
    }

    private final RubyContext context;
    private final Instrumenter instrumenter;
    private final CyclicAssumption unusedAssumption;

    private Collection<EventBinding<?>> instruments;
    private boolean isInTraceFunc = false;

    public TraceManager(RubyContext context, Instrumenter instrumenter) {
        this.context = context;
        this.instrumenter = instrumenter;
        this.unusedAssumption = new CyclicAssumption("set_trace_func is not used");
    }

    public Assumption getUnusedAssumption() {
        return unusedAssumption.getAssumption();
    }

    @TruffleBoundary
    public void setTraceFunc(DynamicObject traceFunc) {
        assert traceFunc == null || RubyGuards.isRubyProc(traceFunc);

        if (instruments != null) {
            for (EventBinding<?> instrument : instruments) {
                instrument.dispose();
            }
        }

        if (traceFunc == null) {
            // Update to a new valid assumption
            unusedAssumption.invalidate();
            instruments = null;
            return;
        }

        // Invalidate current assumption
        unusedAssumption.getAssumption().invalidate();

        instruments = new ArrayList<>();

        instruments.add(instrumenter.attachExecutionEventFactory(SourceSectionFilter.newBuilder().mimeTypeIs(TruffleRuby.MIME_TYPE).tagIs(LineTag.class).build(),
                eventContext -> new BaseEventEventNode(context, eventContext, traceFunc, context.getCoreStrings().LINE.createInstance())));

        instruments.add(instrumenter.attachExecutionEventFactory(SourceSectionFilter.newBuilder().mimeTypeIs(TruffleRuby.MIME_TYPE).tagIs(ClassTag.class).build(),
                eventContext -> new BaseEventEventNode(context, eventContext, traceFunc, context.getCoreStrings().CLASS.createInstance())));

        if (context.getOptions().TRACE_CALLS) {
            instruments.add(instrumenter.attachExecutionEventFactory(SourceSectionFilter.newBuilder().mimeTypeIs(TruffleRuby.MIME_TYPE).tagIs(CallTag.class).includeInternal(false).build(),
                    eventContext -> new CallEventEventNode(context, eventContext, traceFunc, context.getCoreStrings().CALL.createInstance())));
        }
    }

    private class BaseEventEventNode extends TraceBaseEventNode {

        protected final ConditionProfile inTraceFuncProfile = ConditionProfile.createBinaryProfile();

        protected final DynamicObject traceFunc;
        protected final Object event;

        public BaseEventEventNode(RubyContext context, EventContext eventContext, DynamicObject traceFunc, Object event) {
            super(context, eventContext);
            this.traceFunc = traceFunc;
            this.event = event;
        }

        @Override
        protected void onEnter(VirtualFrame frame) {
            if (inTraceFuncProfile.profile(isInTraceFunc)) {
                return;
            }

            isInTraceFunc = true;
            try {
                yield(traceFunc, event,
                        getFile(),
                        getLine(),
                        context.getCoreLibrary().getNil(),
                        BindingNodes.createBinding(context, frame.materialize(), eventContext.getInstrumentedSourceSection()),
                        context.getCoreLibrary().getNil());
            } finally {
                isInTraceFunc = false;
            }
        }

    }

    private class CallEventEventNode extends BaseEventEventNode {

        @Child private LogicalClassNode logicalClassNode;

        public CallEventEventNode(RubyContext context, EventContext eventContext, DynamicObject traceFunc, Object event) {
            super(context, eventContext, traceFunc, event);
        }

        @Override
        protected void onEnter(VirtualFrame frame) {
            if (inTraceFuncProfile.profile(isInTraceFunc)) {
                return;
            }

            final SourceSection sourceSection = context.getCallStack().getTopMostUserSourceSection();

            final String file;
            final int line;

            if (sourceSection != null && sourceSection.getSource() != null) {
                file = getFile(sourceSection);
                line = getLine(sourceSection);
            } else {
                file = "<internal>";
                line = -1;
            }

            isInTraceFunc = true;

            try {
                yield(traceFunc, event,
                        getFile(file),
                        line,
                        context.getSymbolTable().getSymbol(RubyArguments.getMethod(frame).getName()),
                        BindingNodes.createBinding(context, frame.materialize(), eventContext.getInstrumentedSourceSection()),
                        getLogicalClass(RubyArguments.getSelf(frame)));
            } finally {
                isInTraceFunc = false;
            }
        }

        @TruffleBoundary
        private String getFile(SourceSection sourceSection) {
            return context.getPath(sourceSection.getSource());
        }

        @TruffleBoundary
        private int getLine(SourceSection sourceSection) {
            return sourceSection.getStartLine();
        }

        @TruffleBoundary
        private DynamicObject getFile(String file) {
            return StringOperations.createString(context, context.getPathToRopeCache().getCachedPath(file));
        }

        private DynamicObject getLogicalClass(Object object) {
            if (logicalClassNode == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                logicalClassNode = insert(LogicalClassNode.create());
            }

            return logicalClassNode.executeLogicalClass(object);
        }

    }

}
