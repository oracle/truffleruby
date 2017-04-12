/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */

package org.truffleruby.core.rubinius;

import org.truffleruby.builtins.CallerFrameAccess;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.ReadCallerFrameNode;
import org.truffleruby.language.threadlocal.ThreadLocalInFrameNode;
import org.truffleruby.language.threadlocal.ThreadLocalInFrameNodeGen;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;

@NodeChild(value = "value", type = RubyNode.class)
public abstract class RubiniusLastStringWriteNode extends RubyNode {

    @Child ReadCallerFrameNode callerFrameNode = new ReadCallerFrameNode(CallerFrameAccess.READ_WRITE);
    @Child ThreadLocalInFrameNode threadLocalNode;

    @Specialization
    public Object lastStringWrite(VirtualFrame frame, Object value) {
        // Rubinius expects $_ to be thread-local, rather than frame-local.  If we see it in a method call, we need
        // to look to the caller's frame to get the correct value, otherwise it will be nil.
        Frame callerFrame = callerFrameNode.execute(frame);
        if (threadLocalNode == null) {
            CompilerDirectives.transferToInterpreter();
            threadLocalNode = ThreadLocalInFrameNodeGen.create("$_", 20);
        }
        threadLocalNode.execute(callerFrame.materialize()).set(value);

        return value;
    }

    protected abstract RubyNode getValue();

}
