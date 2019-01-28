/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.interop;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;

import org.truffleruby.language.RubyBaseNode;
import org.truffleruby.language.methods.CallBoundMethodNode;
import org.truffleruby.language.yield.YieldNode;

public abstract class ForeignExecuteHelperNode extends RubyBaseNode {

    public abstract Object executeCall(Object receiver, Object[] arguments);

    @Specialization(guards = "isRubyProc(proc)")
    protected Object callProc(DynamicObject proc, Object[] arguments,
            @Cached("new()") YieldNode yieldNode) {
        return yieldNode.dispatch(proc, arguments);
    }

    @Specialization(guards = "isRubyMethod(method)")
    protected Object callMethod(DynamicObject method, Object[] arguments,
            @Cached("create()") CallBoundMethodNode callBoundMethodNode) {
        return callBoundMethodNode.executeCallBoundMethod(method, arguments, nil());
    }

}
