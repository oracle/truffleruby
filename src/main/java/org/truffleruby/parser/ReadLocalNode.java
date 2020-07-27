/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.parser;

import org.truffleruby.RubyContext;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.locals.LocalVariableType;
import org.truffleruby.language.locals.ReadFrameSlotNode;
import org.truffleruby.utils.Utils;

import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;

public abstract class ReadLocalNode extends RubyContextSourceNode {

    protected final FrameSlot frameSlot;
    protected final LocalVariableType type;

    @Child protected ReadFrameSlotNode readFrameSlotNode;

    public ReadLocalNode(FrameSlot frameSlot, LocalVariableType type) {
        this.frameSlot = frameSlot;
        this.type = type;
    }

    protected abstract Object readFrameSlot(VirtualFrame frame);

    public abstract RubyNode makeWriteNode(RubyNode rhs);

    @Override
    public Object isDefined(VirtualFrame frame, RubyContext context) {
        switch (type) {
            case FRAME_LOCAL:
                return coreStrings().LOCAL_VARIABLE.createInstance();

            case FRAME_LOCAL_GLOBAL:
                if (readFrameSlot(frame) != nil) {
                    return coreStrings().GLOBAL_VARIABLE.createInstance();
                } else {
                    return nil;
                }

            default:
                throw Utils.unsupportedOperation("didn't expect local type ", type);
        }
    }

}
