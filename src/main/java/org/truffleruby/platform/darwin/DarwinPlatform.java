/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform.darwin;

import org.truffleruby.RubyContext;
import org.truffleruby.platform.DefaultRubiniusConfiguration;
import org.truffleruby.platform.NativePlatform;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.platform.RubiniusConfiguration;
import org.truffleruby.platform.TruffleNFIPlatform;
import org.truffleruby.platform.signal.SignalManager;
import org.truffleruby.platform.sunmisc.SunMiscSignalManager;

public class DarwinPlatform implements NativePlatform {

    private final TruffleNFIPlatform nfi;
    private final SignalManager signalManager;
    private final RubiniusConfiguration rubiniusConfiguration;

    public DarwinPlatform(RubyContext context) {
        nfi = context.getOptions().NATIVE_INTERRUPT ? new TruffleNFIPlatform(context) : null;
        signalManager = new SunMiscSignalManager();
        rubiniusConfiguration = new RubiniusConfiguration();
        DefaultRubiniusConfiguration.load(rubiniusConfiguration, context);
        DarwinRubiniusConfiguration.load(rubiniusConfiguration, context);
    }

    @Override
    public TruffleNFIPlatform getTruffleNFI() {
        return nfi;
    }

    @Override
    public SignalManager getSignalManager() {
        return signalManager;
    }

    @Override
    public RubiniusConfiguration getRubiniusConfiguration() {
        return rubiniusConfiguration;
    }

    @Override
    public Pointer createSigAction(long handler) {
        Pointer structSigAction = Pointer.calloc(16); // sizeof(struct sigaction)
        structSigAction.writeLong(0, handler);
        return structSigAction;
    }

}
