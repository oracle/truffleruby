/*
 * Copyright (c) 2013, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.locals;

import com.oracle.truffle.api.frame.Frame;
import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;
import org.truffleruby.language.arguments.RubyArguments;

public class FlipFlopNode extends RubyContextSourceNode {

    private final boolean exclusive;

    @Child private BooleanCastNode begin;
    @Child private BooleanCastNode end;
    private final int frameLevel;
    private final int frameSlot;

    public FlipFlopNode(
            RubyNode begin,
            RubyNode end,
            boolean exclusive,
            int frameLevel,
            int frameSlot) {
        this.exclusive = exclusive;
        this.begin = BooleanCastNodeGen.create(begin);
        this.end = BooleanCastNodeGen.create(end);
        this.frameLevel = frameLevel;
        this.frameSlot = frameSlot;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (exclusive) {
            if (getState(frame)) {
                if (end.execute(frame)) {
                    setState(frame, false);
                }

                return true;
            } else {
                final boolean newState = begin.execute(frame);
                setState(frame, newState);
                return newState;
            }
        } else {
            if (getState(frame)) {
                if (end.execute(frame)) {
                    setState(frame, false);
                }

                return true;
            } else {
                if (begin.execute(frame)) {
                    setState(frame, !end.execute(frame));
                    return true;
                }

                return false;
            }
        }
    }

    private boolean getState(VirtualFrame frame) {
        final Frame declarationFrame = RubyArguments.getDeclarationFrame(frame, frameLevel);
        return declarationFrame.getBoolean(frameSlot);
    }

    private void setState(VirtualFrame frame, boolean state) {
        final Frame declarationFrame = RubyArguments.getDeclarationFrame(frame, frameLevel);
        declarationFrame.setBoolean(frameSlot, state);
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new FlipFlopNode(
                begin.getValueNode().cloneUninitialized(),
                end.getValueNode().cloneUninitialized(),
                exclusive,
                frameLevel,
                frameSlot);
        return copy.copyFlags(this);
    }

}
