/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.tracepoint;

import org.truffleruby.RubyContext;
import org.truffleruby.shared.TruffleRuby;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.instrumentation.EventBinding;
import com.oracle.truffle.api.instrumentation.ExecutionEventNodeFactory;
import com.oracle.truffle.api.instrumentation.SourceSectionFilter;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.object.DynamicObject;

public final class TracePointEvent {

    public final Class<? extends Tag> tagClass;
    public final DynamicObject eventSymbol;

    private EventBinding<ExecutionEventNodeFactory> eventBinding;

    public TracePointEvent(Class<? extends Tag> tagClass, DynamicObject eventSymbol) {
        this.tagClass = tagClass;
        this.eventSymbol = eventSymbol;
    }

    public boolean hasEventBinding() {
        return eventBinding != null;
    }

    @TruffleBoundary
    public void setupEventBinding(RubyContext context, DynamicObject tracePoint) {
        assert eventBinding == null;

        final SourceSectionFilter sourceSectionFilter = SourceSectionFilter.newBuilder().mimeTypeIs(TruffleRuby.MIME_TYPE).tagIs(tagClass).includeInternal(false).build();
        this.eventBinding = context.getInstrumenter().attachExecutionEventFactory(
                sourceSectionFilter,
                eventContext -> new TracePointEventNode(context, eventContext, tracePoint, eventSymbol));
    }

    @TruffleBoundary
    public void diposeEventBinding() {
        this.eventBinding.dispose();
        this.eventBinding = null;
    }

}
