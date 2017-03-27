/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.arguments;

import org.truffleruby.language.RubyNode;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.Frame;
import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;
import com.oracle.truffle.api.frame.MaterializedFrame;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class ReadCallerFrameNode extends RubyNode {

    private final ConditionProfile callerFrameProfile = ConditionProfile.createBinaryProfile();

    private final FrameAccess accessMode;

    public ReadCallerFrameNode(String accessModeString) {
        this.accessMode = FrameAccess.valueOf(accessModeString);
    }

    @Override
    public Object execute(VirtualFrame frame) {
        final MaterializedFrame callerFrame = RubyArguments.getCallerFrame(frame);

        if (callerFrameProfile.profile(callerFrame != null)) {
            return callerFrame;
        } else {
            return getCallerFrame();
        }
    }

    @TruffleBoundary
    private Frame getCallerFrame() {
        return Truffle.getRuntime().getCallerFrame().getFrame(accessMode);
    }

}
