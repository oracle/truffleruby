/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform.darwin;

import org.truffleruby.platform.posix.SigAction;

import jnr.posix.LibC.LibCSignalHandler;

public final class DarwinSigAction extends SigAction {

    public final Function<LibCSignalHandler> sa_handler = new Function<>(LibCSignalHandler.class);
    public final Signed32 sa_mask = new Signed32();
    public final Signed32 sa_flags = new Signed32();

    public DarwinSigAction(jnr.ffi.Runtime runtime) {
        super(runtime);
    }

}
