/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.builtins;

import com.oracle.truffle.api.frame.FrameInstance.FrameAccess;

public enum CallerFrameAccess {

    NONE(null),
    ARGUMENTS(FrameAccess.READ_ONLY),
    READ_ONLY(FrameAccess.READ_ONLY),
    READ_WRITE(FrameAccess.READ_WRITE),
    MATERIALIZE(FrameAccess.MATERIALIZE);

    private final FrameAccess frameAccess;

    CallerFrameAccess(FrameAccess frameAccess) {
        this.frameAccess = frameAccess;
    }

    public FrameAccess getFrameAccess() {
        return frameAccess;
    }

}
