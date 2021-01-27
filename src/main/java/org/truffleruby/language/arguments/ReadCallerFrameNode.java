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
import org.truffleruby.language.DataSendingNode.SendsData;
import org.truffleruby.language.FrameOrVariablesReadingNode.Reads;
import org.truffleruby.language.FrameAndVariablesSendingNode;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.MaterializedFrame;

public class ReadCallerFrameNode extends ReadCallerDataNode {

    public static ReadCallerFrameNode create() {
        return new ReadCallerFrameNode();
    }

    @Override
    public MaterializedFrame execute(Frame frame) {
        return (MaterializedFrame) super.execute(frame);
    }

    @Override
    protected MaterializedFrame getData(Frame frame) {
        return RubyArguments.getCallerFrame(frame);
    }

    @Override
    protected void startSending(FrameAndVariablesSendingNode node) {
        node.startSendingOwnFrame();
    }

    @Override
    protected Object getDataFromFrame(MaterializedFrame frame) {
        return frame;
    }

    public void startSending(Reads variables, Reads frame) {
        if (variables == Reads.SELF || frame == Reads.SELF) {
            CompilerDirectives.shouldNotReachHere();
        }
        if (variables == Reads.SELF) {
            replace(new ReadCallerFrameAndVariablesNode());
        }
    }


    public boolean sendingFrame() {
        return true;
    }
}
