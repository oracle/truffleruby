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

import com.oracle.truffle.api.frame.VirtualFrame;

public interface DataSendingNode {

    public static enum SendsData {
        NO_FRAME,       // callees don't need to read the frame
        MY_FRAME,       // for most calls
        CALLER_FRAME;   // for `send` calls
    }

    public Object execute(VirtualFrame frame);

    public boolean replace(SendsData variabless, SendsData frame);
}
