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
import com.oracle.truffle.api.frame.Frame;

public class ReadOwnFrameNode extends RubyBaseNode implements FrameOrVariablesReadingNode {

    public Object execute(Frame frame) {
        return frame.materialize();
    }

    public void startSending(Reads variables, Reads frame) {
        if (variables == Reads.CALLER || frame == Reads.CALLER) {
            CompilerDirectives.shouldNotReachHere();
        }
        if (variables == Reads.SELF) {
            replace(new ReadOwnFrameAndVariablesNode());
        }
    }

    public boolean sendingFrame() {
        return true;
    }
}
