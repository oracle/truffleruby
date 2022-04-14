/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.locals;

import org.truffleruby.core.cast.BooleanCastNode;
import org.truffleruby.core.cast.BooleanCastNodeGen;
import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.frame.VirtualFrame;

public class FlipFlopNode extends RubyContextSourceNode {

    private final boolean exclusive;

    @Child private BooleanCastNode begin;
    @Child private BooleanCastNode end;
    @Child private FlipFlopStateNode stateNode;

    public FlipFlopNode(
            RubyNode begin,
            RubyNode end,
            FlipFlopStateNode stateNode,
            boolean exclusive) {
        this.exclusive = exclusive;
        this.begin = BooleanCastNodeGen.create(begin);
        this.end = BooleanCastNodeGen.create(end);
        this.stateNode = stateNode;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (exclusive) {
            if (stateNode.getState(frame)) {
                if (end.execute(frame)) {
                    stateNode.setState(frame, false);
                }

                return true;
            } else {
                final boolean newState = begin.execute(frame);
                stateNode.setState(frame, newState);
                return newState;
            }
        } else {
            if (stateNode.getState(frame)) {
                if (end.execute(frame)) {
                    stateNode.setState(frame, false);
                }

                return true;
            } else {
                if (begin.execute(frame)) {
                    stateNode.setState(frame, !end.execute(frame));
                    return true;
                }

                return false;
            }
        }
    }

}
