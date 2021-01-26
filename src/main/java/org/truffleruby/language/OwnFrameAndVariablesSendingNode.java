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
import org.truffleruby.core.kernel.TruffleKernelNodes.GetSpecialVariableStorage;

public class OwnFrameAndVariablesSendingNode extends RubyBaseNode implements DataSendingNode {

    @Child GetSpecialVariableStorage readVariablesNode = GetSpecialVariableStorage.create();

    public Object execute(VirtualFrame frame) {
        return new FrameAndVariables(readVariablesNode.execute(frame), frame.materialize());
    }

    public void startSending(SendsData variables, SendsData frame) {
        if (variables == SendsData.CALLER || frame == SendsData.CALLER) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            CompilerDirectives.shouldNotReachHere();
        }
    }

    public boolean sendingFrame() {
        return true;
    }
}
