/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
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
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.VirtualFrame;

public class RubiniusLastStringReadNode extends RubyNode {
    @Child ReadCallerFrameNode callerFrameNode = new ReadCallerFrameNode(CallerFrameAccess.READ_WRITE);
    @Child ThreadLocalInFrameNode threadLocalNode;

    @Override
    public Object execute(VirtualFrame frame) {
        Frame callerFrame = callerFrameNode.execute(frame);
        if (threadLocalNode == null) {
            CompilerDirectives.transferToInterpreter();
            threadLocalNode = ThreadLocalInFrameNodeGen.create("$_", 5);
        }
        return threadLocalNode.execute(callerFrame.materialize()).get();
    }
}
