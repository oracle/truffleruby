/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.arguments;

import com.oracle.truffle.api.frame.Frame;
import org.truffleruby.core.kernel.TruffleKernelNodes;
import org.truffleruby.language.FrameOrVariablesReadingNode.Reads;
import org.truffleruby.language.FrameAndVariables;
import org.truffleruby.language.FrameAndVariablesSendingNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.MaterializedFrame;

public class ReadCallerFrameAndVariablesNode extends ReadCallerDataNode {

    public static ReadCallerFrameAndVariablesNode create() {
        return new ReadCallerFrameAndVariablesNode();
    }

    @Override
    public FrameAndVariables execute(Frame frame) {
        return (FrameAndVariables) super.execute(frame);
    }

    @Override
    protected FrameAndVariables getData(Frame frame) {
        return RubyArguments.getCallerFrameAndVariables(frame);
    }

    @Override
    protected void startSending(FrameAndVariablesSendingNode node) {
        node.startSendingOwnFrame();
    }

    @Override
    protected Object getDataFromFrame(MaterializedFrame frame) {
        return new FrameAndVariables(TruffleKernelNodes.GetSpecialVariableStorage.getSlow(frame), frame);
    }

    public void startSending(Reads variables, Reads frame) {
        if (variables == Reads.SELF || frame == Reads.SELF) {
            CompilerDirectives.shouldNotReachHere();
        }
    }

    public boolean sendingFrame() {
        return true;
    }
}
