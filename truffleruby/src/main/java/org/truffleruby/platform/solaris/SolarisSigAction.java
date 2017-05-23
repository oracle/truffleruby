/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform.solaris;

import jnr.ffi.Runtime;
import jnr.posix.LibC;
import org.truffleruby.platform.posix.SigAction;

public class SolarisSigAction extends SigAction {

    public final Signed32 sa_flags = new Signed32();
    public final Function<LibC.LibCSignalHandler> sa_handler = new Function<>(LibC.LibCSignalHandler.class);
    public final Unsigned32[] sa_mask = array(new Unsigned32[4]); // 16 bytes

    public SolarisSigAction(Runtime runtime) {
        super(runtime);
    }

}
