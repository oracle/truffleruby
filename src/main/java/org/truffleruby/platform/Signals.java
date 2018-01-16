/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import sun.misc.Signal;

public class Signals {

    private static final ConcurrentMap<sun.misc.Signal, sun.misc.SignalHandler> DEFAULT_HANDLERS = new ConcurrentHashMap<>();

    public static void registerHandler(Runnable newHandler, String signalName) {
        final Signal signal = new Signal(signalName);
        final sun.misc.SignalHandler oldSunHandler = sun.misc.Signal.handle(signal, s -> newHandler.run());
        DEFAULT_HANDLERS.putIfAbsent(signal, oldSunHandler);
    }

    public static void restoreDefaultHandler(String signalName) {
        final Signal signal = new Signal(signalName);
        final sun.misc.SignalHandler defaultHandler = Signals.DEFAULT_HANDLERS.get(signal);
        if (defaultHandler != null) { // otherwise it is already the default signal
            sun.misc.Signal.handle(signal, defaultHandler);
        }
    }

}
