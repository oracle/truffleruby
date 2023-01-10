/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.signal;

import org.truffleruby.platform.Platform;

public abstract class LibRubySignal {

    public static void loadLibrary(String rubyHome) {
        final String path = rubyHome + "/lib/cext/librubysignal" + Platform.LIB_SUFFIX;
        System.load(path);
    }

    public static native int setupSIGVTALRMEmptySignalHandler();

    public static native long threadID();

    public static native int sendSIGVTALRMToThread(long thread);

}
