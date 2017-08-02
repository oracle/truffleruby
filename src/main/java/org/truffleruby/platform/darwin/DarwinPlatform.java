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

import jnr.ffi.LibraryLoader;
import jnr.posix.POSIXFactory;
import org.truffleruby.RubyContext;
import org.truffleruby.platform.DefaultRubiniusConfiguration;
import org.truffleruby.platform.FDSet;
import org.truffleruby.platform.NativePlatform;
import org.truffleruby.extra.ffi.Pointer;
import org.truffleruby.platform.ProcessName;
import org.truffleruby.platform.RubiniusConfiguration;
import org.truffleruby.platform.TruffleNFIPlatform;
import org.truffleruby.platform.java.JavaClockGetTime;
import org.truffleruby.platform.posix.ClockGetTime;
import org.truffleruby.platform.posix.JNRTrufflePosix;
import org.truffleruby.platform.posix.NoopThreads;
import org.truffleruby.platform.posix.PosixFDSet4Bytes;
import org.truffleruby.platform.posix.Sockets;
import org.truffleruby.platform.posix.Threads;
import org.truffleruby.platform.posix.TrufflePosix;
import org.truffleruby.platform.posix.TrufflePosixHandler;
import org.truffleruby.platform.signal.SignalManager;
import org.truffleruby.platform.sunmisc.SunMiscSignalManager;

public class DarwinPlatform implements NativePlatform {

    private final TruffleNFIPlatform nfi;
    private final TrufflePosix posix;
    private final SignalManager signalManager;
    private final ProcessName processName;
    private final Sockets sockets;
    private final ClockGetTime clockGetTime;
    private final Threads threads;
    private final RubiniusConfiguration rubiniusConfiguration;

    public DarwinPlatform(RubyContext context) {
        nfi = context.getOptions().NATIVE_INTERRUPT ? new TruffleNFIPlatform(context) : null;
        posix = new JNRTrufflePosix(context, POSIXFactory.getNativePOSIX(new TrufflePosixHandler(context)));
        signalManager = new SunMiscSignalManager();
        processName = new DarwinProcessName();
        sockets = LibraryLoader.create(Sockets.class).library("c").load();
        threads = context.getOptions().NATIVE_INTERRUPT ? LibraryLoader.create(Threads.class).library("c").library("pthread").load() : new NoopThreads();
        clockGetTime = new JavaClockGetTime();
        rubiniusConfiguration = new RubiniusConfiguration();
        DefaultRubiniusConfiguration.load(rubiniusConfiguration, context);
        DarwinRubiniusConfiguration.load(rubiniusConfiguration, context);
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
    public ClockGetTime getClockGetTime() {
        return clockGetTime;
    }

    @Override
    public Threads getThreads() {
        return threads;
    }

    @Override
    public RubiniusConfiguration getRubiniusConfiguration() {
        return rubiniusConfiguration;
    }

    @Override
    public FDSet createFDSet() {
        return new PosixFDSet4Bytes();
    }

    @Override
    public Pointer createSigAction(long handler) {
        Pointer structSigAction = Pointer.malloc(16); // sizeof(struct sigaction)
        structSigAction.writeBytes(0, 16, (byte) 0);
        structSigAction.writeLong(0, handler);
        return structSigAction;
    }

}
