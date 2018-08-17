/*
 * Copyright (c) 2015, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.platform;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import sun.misc.Signal;

public class Signals {

    // Use String and not Signal as key to work around SVM not allowing new Signal("PROF")
    private static final ConcurrentMap<String, sun.misc.SignalHandler> DEFAULT_HANDLERS = new ConcurrentHashMap<>();

    public static void registerHandler(Runnable newHandler, String signalName) {
        final Signal signal = new Signal(signalName);
        final sun.misc.SignalHandler oldSunHandler;
        if (newHandler == null) {
            oldSunHandler = Signal.handle(signal, sun.misc.SignalHandler.SIG_IGN);
        } else {
            oldSunHandler = Signal.handle(signal, s -> newHandler.run());
        }
        DEFAULT_HANDLERS.putIfAbsent(signalName, oldSunHandler);
    }

    public static boolean restoreDefaultHandler(String signalName) {
        final sun.misc.SignalHandler defaultHandler = Signals.DEFAULT_HANDLERS.get(signalName);
        if (defaultHandler == null) {
            // it is already the default signal
            return false;
        } else {
            final Signal signal = new Signal(signalName);
            Signal.handle(signal, defaultHandler);
            return true;
        }
    }

    public static void restoreSystemHandler(String signalName) {
        final Signal signal = new Signal(signalName);
        Signal.handle(signal, sun.misc.SignalHandler.SIG_DFL);
    }

}
