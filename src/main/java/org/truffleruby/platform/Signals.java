/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
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
import java.util.function.Consumer;

public class Signals {

    private static final ConcurrentMap<sun.misc.Signal, sun.misc.SignalHandler> DEFAULT_HANDLERS = new ConcurrentHashMap<>();

    public static void registerHandler(Consumer<sun.misc.Signal> newHandler, sun.misc.Signal signal) {
        final sun.misc.SignalHandler oldSunHandler =
                sun.misc.Signal.handle(signal, wrappedSignal -> newHandler.accept(signal));
        DEFAULT_HANDLERS.putIfAbsent(signal, oldSunHandler);
    }

    public static void restoreDefaultHandler(sun.misc.Signal signal) {
        final sun.misc.SignalHandler defaultHandler = Signals.DEFAULT_HANDLERS.get(signal);
        if (defaultHandler != null) { // otherwise it is already the default signal
            sun.misc.Signal.handle(signal, defaultHandler);
        }
    }

}
