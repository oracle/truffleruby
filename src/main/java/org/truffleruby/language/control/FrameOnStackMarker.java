/*
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.control;

/**
 * An object denoting if the lexical frame is still on stack, so that e.g., we know whether can
 * break from the current frame in that lexical frame.
 */
public final class FrameOnStackMarker {

    private boolean onStack = true;

    public boolean isOnStack() {
        return onStack;
    }

    public void setNoLongerOnStack() {
        onStack = false;
    }

}
