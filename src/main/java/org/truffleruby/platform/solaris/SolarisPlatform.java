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

import jnr.ffi.LibraryLoader;
import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;
import org.truffleruby.RubyContext;
import org.truffleruby.platform.DefaultRubiniusConfiguration;
import org.truffleruby.platform.FDSet;
import org.truffleruby.platform.NativePlatform;
import org.truffleruby.platform.Pointer;
import org.truffleruby.platform.ProcessName;
import org.truffleruby.platform.RubiniusConfiguration;
import org.truffleruby.platform.TruffleNFIPlatform;
import org.truffleruby.platform.java.JavaProcessName;
import org.truffleruby.platform.posix.ClockGetTime;
import org.truffleruby.platform.posix.JNRTrufflePosix;
import org.truffleruby.platform.posix.NoopThreads;
import org.truffleruby.platform.posix.PosixFDSet8Bytes;
import org.truffleruby.platform.posix.Sockets;
import org.truffleruby.platform.posix.Threads;
import org.truffleruby.platform.posix.TrufflePosix;
import org.truffleruby.platform.posix.TrufflePosixHandler;
import org.truffleruby.platform.signal.SignalManager;
import org.truffleruby.platform.sunmisc.SunMiscSignalManager;

public class SolarisPlatform implements NativePlatform {

    private final TruffleNFIPlatform nfi;
    private final TrufflePosix posix;
    private final SignalManager signalManager;
    private final ProcessName processName;
    private final Sockets sockets;
    private final Threads threads;
    private final ClockGetTime clockGetTime;
    private final RubiniusConfiguration rubiniusConfiguration;

    public SolarisPlatform(RubyContext context) {
        nfi = context.getOptions().NATIVE_INTERRUPT ? new TruffleNFIPlatform(context) : null;
        POSIX _posix = POSIXFactory.getNativePOSIX(new TrufflePosixHandler(context));
        posix = new JNRTrufflePosix(context, _posix);
        signalManager = new SunMiscSignalManager();
        processName = new JavaProcessName();
        sockets = LibraryLoader.create(Sockets.class).library("socket").load();
        threads = context.getOptions().NATIVE_INTERRUPT ? new SolarisThreadsImplementation(LibraryLoader.create(SolarisThreads.class).library("libpthread.so.1").load()) : new NoopThreads();
        clockGetTime = LibraryLoader.create(ClockGetTime.class).library("c").library("rt").load();
        rubiniusConfiguration = new RubiniusConfiguration();
        DefaultRubiniusConfiguration.load(rubiniusConfiguration, context);
        SolarisSparcV9RubiniusConfiguration.load(rubiniusConfiguration, context);
    }

    @Override
    public TruffleNFIPlatform getTruffleNFI() {
        return nfi;
    }

    @Override
    public TrufflePosix getPosix() {
        return posix;
    }

    @Override
    public SignalManager getSignalManager() {
        return signalManager;
    }

    @Override
    public ProcessName getProcessName() {
        return processName;
    }

    @Override
    public Sockets getSockets() {
        return sockets;
    }

    @Override
    public Threads getThreads() {
        return threads;
    }

    @Override
    public ClockGetTime getClockGetTime() {
        return clockGetTime;
    }

    @Override
    public RubiniusConfiguration getRubiniusConfiguration() {
        return rubiniusConfiguration;
    }

    @Override
    public FDSet createFDSet() {
        return new PosixFDSet8Bytes();
    }

    @Override
    public Pointer createSigAction(long handler) {
        Pointer structSigAction = Pointer.malloc(32); // sizeof(struct sigaction)
        structSigAction.add(8).putLong(handler); // offsetof(struct sigaction, sa_handler)
        return structSigAction;
    }

}
