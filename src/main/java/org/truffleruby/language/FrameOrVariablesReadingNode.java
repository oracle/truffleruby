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

/** See {@link FrameAndVariablesSendingNode}. Nodes implementing this interface are used to read the data from the
 * current frame, before a call. */
public interface FrameOrVariablesReadingNode extends NodeInterface {

    public Object execute(Frame frame);

    public void startSending(boolean variables, boolean frame);

    public boolean sendingFrame();
}
