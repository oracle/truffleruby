/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.tracepoint;

import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.interop.messages.RubyTracePointMessages;
import org.truffleruby.language.RubyDynamicObject;

public class RubyTracePoint extends RubyDynamicObject {


    TracePointEvent[] events;
    DynamicObject proc;

    public RubyTracePoint(Shape shape, TracePointEvent[] events, DynamicObject proc) {
        super(shape);
        this.events = events;
        this.proc = proc;
    }

    @Override
    @ExportMessage
    public Class<?> dispatch() {
        return RubyTracePointMessages.class;
    }

}
