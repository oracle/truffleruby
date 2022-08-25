/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.control;

import org.truffleruby.language.RubyContextSourceNode;
import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

public class BreakNode extends RubyContextSourceNode {

    private final BreakID breakID;
    private final boolean ignoreMarker;

    @Child private RubyNode child;

    private final BranchProfile breakFromProcClosureProfile = BranchProfile.create();

    public BreakNode(BreakID breakID, boolean ignoreMarker, RubyNode child) {
        this.breakID = breakID;
        this.ignoreMarker = ignoreMarker;
        this.child = child;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        if (!ignoreMarker) {
            final FrameOnStackMarker marker = RubyArguments.getFrameOnStackMarker(frame);

            if (marker != null && !marker.isOnStack()) {
                breakFromProcClosureProfile.enter();
                throw new RaiseException(getContext(), coreExceptions().breakFromProcClosure(this));
            }
        }

        throw new BreakException(breakID, child.execute(frame));
    }

    @Override
    public RubyNode cloneUninitialized() {
        var copy = new BreakNode(
                breakID,
                ignoreMarker,
                child.cloneUninitialized());
        copy.copyFlags(this);
        return copy;
    }

}
