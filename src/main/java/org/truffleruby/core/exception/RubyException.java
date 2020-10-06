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

import java.util.Set;

import org.truffleruby.core.array.RubyArray;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.core.proc.RubyProc;
import org.truffleruby.core.string.RubyString;
import org.truffleruby.language.Nil;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.backtrace.Backtrace;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.Shape;

public class RubyException extends RubyDynamicObject implements ObjectGraphNode {

    public Object message;
    public Backtrace backtrace;
    public Object cause;

    public RubyProc formatter = null;
    public RubyArray backtraceStringArray = null;
    /** null (not yet computed), RubyArray, or nil (empty) */
    public Object backtraceLocations = null;
    /** null (not set), RubyArray of Strings, or nil (empty) */
    public Object customBacktrace = null;

    public RubyException(RubyClass rubyClass, Shape shape, Object message, Backtrace backtrace, Object cause) {
        super(rubyClass, shape);
        // TODO (eregon, 9 Aug 2020): it should probably be null or RubyString, not nil, and the field can then be typed
        assert message == null || message == Nil.INSTANCE || message instanceof RubyString;
        assert cause != null;
        this.message = message;
        this.backtrace = backtrace;
        this.cause = cause;
    }

    @TruffleBoundary
    @Override
    public String toString() {
        return message.toString();
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        ObjectGraph.addProperty(reachable, message);
        ObjectGraph.addProperty(reachable, cause);
    }

}
