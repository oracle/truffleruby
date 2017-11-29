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

import jnr.constants.platform.Signal;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public class Signals {

    private static final Set<String> RUBY_SIGNALS = new HashSet<>(Arrays.asList(new String[]{
            "EXIT",
            "HUP",
            "INT",
            "QUIT",
            "ILL",
            "TRAP",
            "IOT",
            "ABRT",
            "EMT",
            "FPE",
            "KILL",
            "BUS",
            "SEGV",
            "SYS",
            "PIPE",
            "ALRM",
            "TERM",
            "URG",
            "STOP",
            "TSTP",
            "CONT",
            "CHLD",
            "CLD",
            "TTIN",
            "TTOU",
            "IO",
            "XCPU",
            "XFSZ",
            "VTALRM",
            "PROF",
            "WINCH",
            "USR1",
            "USR2",
            "LOST",
            "MSG",
            "PWR",
            "POLL",
            "DANGER",
            "MIGRATE",
            "PRE",
            "GRANT",
            "RETRACT",
            "SOUND",
            "INFO",
    }));

    private static Map<String, Integer> list() {
        Map<String, Integer> signals = new HashMap<>();

        for (Signal s : Signal.values()) {
            if (!s.defined())
                continue;

            String signame = s.description();
            final String name = signame.startsWith("SIG") ? signame.substring(3) : signame;
            if (!RUBY_SIGNALS.contains(name))
                continue;

            int signo = s.intValue();
            // omit unsupported signals
            if (signo >= 20000)
                continue;

            signals.put(name, signo);
        }

        if (!Signal.SIGCLD.defined() && Signal.SIGCHLD.defined()) {
            signals.put("CLD", Signal.SIGCHLD.intValue());
        }

        return signals;
    }

    public static final Map<String, Integer> SIGNALS_LIST = Collections.unmodifiableMap(list());

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
