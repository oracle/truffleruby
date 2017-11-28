/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform.solaris;

import org.truffleruby.RubyContext;
import org.truffleruby.platform.DefaultRubiniusConfiguration;
import org.truffleruby.platform.NativePlatform;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.platform.RubiniusConfiguration;
import org.truffleruby.platform.TruffleNFIPlatform;

public class SolarisPlatform implements NativePlatform {

    private final TruffleNFIPlatform nfi;
    private final RubiniusConfiguration rubiniusConfiguration;

    public SolarisPlatform(RubyContext context) {
        nfi = context.getOptions().NATIVE_INTERRUPT ? new TruffleNFIPlatform(context) : null;
        rubiniusConfiguration = new RubiniusConfiguration();
        DefaultRubiniusConfiguration.load(rubiniusConfiguration, context);
        SolarisSparcV9RubiniusConfiguration.load(rubiniusConfiguration, context);
    }

    @Override
    public TruffleNFIPlatform getTruffleNFI() {
        return nfi;
    }

    @Override
    public RubiniusConfiguration getRubiniusConfiguration() {
        return rubiniusConfiguration;
    }

    @Override
    public Pointer createSigAction(long handler) {
        Pointer structSigAction = Pointer.calloc(32); // sizeof(struct sigaction)
        structSigAction.writeLong(8, handler); // offsetof(struct sigaction, sa_handler)
        return structSigAction;
    }

}
