/*
 * Copyright (c) 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;

import org.truffleruby.language.DataSendingNode.SendsData;

public class OwnFrameSendingNode extends RubyBaseNode implements DataSendingNode {

    public Object execute(VirtualFrame frame) {
        return frame.materialize();
    }

    public void startSending(SendsData variables, SendsData frame) {
        if (variables == SendsData.CALLER || frame == SendsData.CALLER) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            CompilerDirectives.shouldNotReachHere();
        }
        if (variables == SendsData.SELF) {
            replace(new OwnFrameAndVariablesSendingNode());
        }
    }

    public boolean sendingFrame() {
        return true;
    }
}
