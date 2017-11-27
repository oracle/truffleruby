/*
 * Copyright (c) 2013, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.control;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.BranchProfile;

import org.truffleruby.language.RubyNode;
import org.truffleruby.language.arguments.RubyArguments;

public class ReturnNode extends RubyNode {

    private final ReturnID returnID;

    @Child private RubyNode value;

    private final BranchProfile unexpectedReturnProfile = BranchProfile.create();

    public ReturnNode(ReturnID returnID, RubyNode value) {
        this.returnID = returnID;
        this.value = value;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final FrameOnStackMarker marker = RubyArguments.getFrameOnStackMarker(frame);

        if (marker != null && !marker.isOnStack()) {
            unexpectedReturnProfile.enter();
            throw new RaiseException(coreExceptions().unexpectedReturn(this));
        }

        throw new ReturnException(returnID, value.execute(frame));
    }

}
