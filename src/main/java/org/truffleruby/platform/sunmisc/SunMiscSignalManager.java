/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.platform.sunmisc;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import jnr.constants.platform.Signal;
import org.truffleruby.platform.signal.SignalHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("restriction")
public class SunMiscSignalManager {

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

    public static final Map<String, Integer> SIGNALS_LIST = Collections.unmodifiableMap(list());

    public static final SignalHandler IGNORE_HANDLER = signal -> {
        // Just ignore the signal.
    };

    private static Map<String, Integer> list() {
        Map<String, Integer> signals = new HashMap<>();

        for (Signal s : Signal.values()) {
            if (!s.defined())
                continue;

            final String name = signameWithoutPrefix(s.description());
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

    private static String signameWithoutPrefix(String signame) {
        return signame.startsWith("SIG") ? signame.substring(3) : signame;
    }

    private final ConcurrentMap<sun.misc.Signal, sun.misc.SignalHandler> DEFAULT_HANDLERS = new ConcurrentHashMap<>();

    public SunMiscSignal createSignal(String name) {
        return new SunMiscSignal(name);
    }

    public void watchSignal(SunMiscSignal signal, SignalHandler newHandler) throws IllegalArgumentException {
        handle(signal, newHandler);
    }

    public void watchDefaultForSignal(SunMiscSignal signal) throws IllegalArgumentException {
        handleDefault(signal);
    }

    @TruffleBoundary
    public void handle(final SunMiscSignal signal, final SignalHandler newHandler) throws IllegalArgumentException {
        final sun.misc.SignalHandler oldSunHandler = sun.misc.Signal.handle(
                signal.getSunMiscSignal(), wrapHandler(signal, newHandler));

        DEFAULT_HANDLERS.putIfAbsent(signal.getSunMiscSignal(), oldSunHandler);
    }

    @TruffleBoundary
    public void handleDefault(final SunMiscSignal signal) throws IllegalArgumentException {
        final sun.misc.SignalHandler defaultHandler = DEFAULT_HANDLERS.get(signal.getSunMiscSignal());
        if (defaultHandler != null) { // otherwise it is already the default signal
            sun.misc.Signal.handle(signal.getSunMiscSignal(), defaultHandler);
        }
    }

    @TruffleBoundary
    private sun.misc.SignalHandler wrapHandler(final SunMiscSignal signal, final SignalHandler newHandler) {
        return wrappedSignal -> newHandler.handle(signal);
    }

    @TruffleBoundary
    public void raise(SunMiscSignal signal) throws IllegalArgumentException {
        sun.misc.Signal.raise((signal).getSunMiscSignal());
    }

}
