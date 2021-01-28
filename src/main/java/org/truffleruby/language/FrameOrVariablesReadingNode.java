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
import com.oracle.truffle.api.nodes.NodeInterface;

public interface FrameOrVariablesReadingNode extends NodeInterface {

    public static enum Reads {
        NOTHING,       // callees don't need to read the frame
        SELF,      // for most calls
        CALLER;   // for `send` calls
    }

    public Object execute(Frame frame);

    public void startSending(Reads variabless, Reads frame);

    public boolean sendingFrame();
}
