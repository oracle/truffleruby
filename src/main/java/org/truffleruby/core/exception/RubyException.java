/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.exception;

import org.truffleruby.interop.messages.ExceptionMessages;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.backtrace.Backtrace;

import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import java.util.Set;


public class RubyException extends RubyDynamicObject implements ObjectGraphNode {

    public Object message;
    public Backtrace backtrace;
    public Object cause;

    public DynamicObject formatter = null;
    public DynamicObject backtraceStringArray = null;
    public Object backtraceLocations = null;

    public RubyException(Shape shape, Object message, Backtrace backtrace, Object cause) {
        super(shape);
        assert cause != null;
        this.message = message;
        this.backtrace = backtrace;
        this.cause = cause;
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        if (ObjectGraph.isSymbolOrDynamicObject(message)) {
            reachable.add(message);
        }

        if (ObjectGraph.isSymbolOrDynamicObject(cause)) {
            reachable.add(cause);
        }
    }

    @Override
    @ExportMessage
    public Class<?> dispatch() {
        return ExceptionMessages.class;
    }
}
