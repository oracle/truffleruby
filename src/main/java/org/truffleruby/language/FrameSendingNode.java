/*
 * Copyright (c) 2014, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.Assumption;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.Node;
import com.oracle.truffle.api.utilities.AlwaysValidAssumption;

import org.truffleruby.language.arguments.ReadCallerFrameNode;

public abstract class FrameSendingNode extends RubyBaseNode {

    protected enum SendsFrame {
        NO_FRAME,
        MY_FRAME,
        CALLER_FRAME;
    }

    @CompilationFinal protected SendsFrame sendsFrame = SendsFrame.NO_FRAME;
    @CompilationFinal protected Assumption needsCallerAssumption;

    @Child protected ReadCallerFrameNode readCaller;

    protected boolean sendingFrames() {
        return sendsFrame != SendsFrame.NO_FRAME;
    }

    public void startSendingOwnFrame() {
        if (getContext().getCallStack().callerIsSend()) {
            startSendingFrame(SendsFrame.CALLER_FRAME);
        } else {
            startSendingFrame(SendsFrame.MY_FRAME);
        }
    }

    private synchronized void startSendingFrame(SendsFrame frameToSend) {
        if (sendingFrames()) {
            assert sendsFrame == frameToSend;
            return;
        }
        assert needsCallerAssumption != AlwaysValidAssumption.INSTANCE;
        this.sendsFrame = frameToSend;
        if (frameToSend == SendsFrame.CALLER_FRAME) {
            this.readCaller = insert(new ReadCallerFrameNode());
        }
        Node root = getRootNode();
        if (root instanceof RubyRootNode) {
            ((RubyRootNode) root).invalidateNeedsCallerAssumption();
        } else {
            throw new Error();
        }
    }

    protected synchronized void resetNeedsCallerAssumption() {
        Node root = getRootNode();
        if (root instanceof RubyRootNode && !sendingFrames()) {
            needsCallerAssumption = ((RubyRootNode) root).getNeedsCallerAssumption();
        } else {
            needsCallerAssumption = AlwaysValidAssumption.INSTANCE;
        }
    }

    public MaterializedFrame getFrameIfRequired(VirtualFrame frame) {
        switch (sendsFrame) {
            case MY_FRAME:
                return frame.materialize();
            case CALLER_FRAME:
                return readCaller.execute(frame);
            default:
                return null;
        }
    }

}
