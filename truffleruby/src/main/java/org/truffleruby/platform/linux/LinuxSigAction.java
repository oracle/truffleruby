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

import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.posix.LibC.LibCSignalHandler;

public final class LinuxSigAction extends SigAction {

    public final Function<LibCSignalHandler> sa_handler = new Function<>(LibCSignalHandler.class);
    public final SigSet sa_mask = inner(new SigSet());
    public final Signed32 sa_flags = new Signed32();
    public final PointerField sa_restorer = new Pointer();

    public LinuxSigAction(jnr.ffi.Runtime runtime) {
        super(runtime);
    }

    final static class SigSet extends Struct {

        // 128 bytes

        SignedLong l1 = new SignedLong();
        SignedLong l2 = new SignedLong();
        SignedLong l3 = new SignedLong();
        SignedLong l4 = new SignedLong();
        SignedLong l5 = new SignedLong();
        SignedLong l6 = new SignedLong();
        SignedLong l7 = new SignedLong();
        SignedLong l8 = new SignedLong();
        SignedLong l9 = new SignedLong();
        SignedLong l10 = new SignedLong();
        SignedLong l11 = new SignedLong();
        SignedLong l12 = new SignedLong();
        SignedLong l13 = new SignedLong();
        SignedLong l14 = new SignedLong();
        SignedLong l15 = new SignedLong();
        SignedLong l16 = new SignedLong();

        public SigSet() {
            super(Runtime.getSystemRuntime());
        }

    }

}
