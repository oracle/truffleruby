/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform.linux;

import org.truffleruby.RubyContext;
import org.truffleruby.platform.DefaultRubiniusConfiguration;
import org.truffleruby.platform.NativePlatform;
import org.truffleruby.extra.ffi.Pointer;

public class LinuxPlatform implements NativePlatform {

    public LinuxPlatform(RubyContext context) {
        DefaultRubiniusConfiguration.load(context.getRubiniusConfiguration(), context);
        LinuxRubiniusConfiguration.load(context.getRubiniusConfiguration(), context);
    }

    @Override
    public Pointer createSigAction(long handler) {
        Pointer structSigAction = Pointer.calloc(152); // sizeof(struct sigaction)
        structSigAction.writeLong(0, handler);
        return structSigAction;
    }

}
