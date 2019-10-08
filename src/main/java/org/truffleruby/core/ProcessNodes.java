/*
 * Copyright (c) 2013, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core;

import org.truffleruby.Layouts;
import org.truffleruby.builtins.CoreModule;
import org.truffleruby.builtins.Primitive;
import org.truffleruby.builtins.PrimitiveArrayArgumentsNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

import sun.misc.Signal;

@CoreModule(value = "Process", isClass = true)
public abstract class ProcessNodes {

    @Primitive(name = "process_time_nanotime", needsSelf = false)
    public abstract static class ProcessTimeNanoTimeNode extends PrimitiveArrayArgumentsNode {

        @Specialization
        protected long nanoTime() {
            return System.nanoTime();
        }

    }

    @Primitive(name = "process_time_currenttimemillis", needsSelf = false)
    public abstract static class ProcessTimeCurrentTimeMillisNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary
        @Specialization
        protected long currentTimeMillis() {
            return System.currentTimeMillis();
        }

    }

    @Primitive(name = "process_kill_raise", needsSelf = false)
    public abstract static class ProcessKillRaiseNode extends PrimitiveArrayArgumentsNode {

        @TruffleBoundary(transferToInterpreterOnException = false)
        @Specialization(guards = "isRubySymbol(signalName)")
        protected int raise(DynamicObject signalName) {
            final Signal signal = new Signal(Layouts.SYMBOL.getString(signalName));
            try {
                Signal.raise(signal);
            } catch (IllegalArgumentException e) {
                // Java does not know the handler, fallback to using kill()
                return -1;
            }
            return 1;
        }

    }

}
