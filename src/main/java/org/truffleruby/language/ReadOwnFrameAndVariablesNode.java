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

import com.oracle.truffle.api.frame.Frame;
import org.truffleruby.core.kernel.TruffleKernelNodes.GetSpecialVariableStorage;

public class ReadOwnFrameAndVariablesNode extends RubyBaseNode implements FrameOrVariablesReadingNode {

    @Child GetSpecialVariableStorage readVariablesNode = GetSpecialVariableStorage.create();

    public Object execute(Frame frame) {
        return new FrameAndVariables(readVariablesNode.execute(frame), frame.materialize());
    }

    @Override
    public void startSending(boolean variables, boolean frame) {
    }

    @Override
    public boolean sendingFrame() {
        return true;
    }
}
