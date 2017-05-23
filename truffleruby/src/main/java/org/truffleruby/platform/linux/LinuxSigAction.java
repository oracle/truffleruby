/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform.linux;

import org.truffleruby.platform.posix.SigAction;

import jnr.posix.LibC.LibCSignalHandler;

public final class LinuxSigAction extends SigAction {

    public final Function<LibCSignalHandler> sa_handler = new Function<>(LibCSignalHandler.class);
    public final SignedLong[] sa_mask = array(new SignedLong[16]); // 128 bytes
    public final Signed32 sa_flags = new Signed32();
    public final PointerField sa_restorer = new Pointer();

    public LinuxSigAction(jnr.ffi.Runtime runtime) {
        super(runtime);
    }

}
