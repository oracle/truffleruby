/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language;

import java.util.concurrent.locks.ReentrantLock;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.truffleruby.language.library.RubyStringLibrary;

public class AutoloadConstant {

    private final Object feature;
    private final String autoloadPath;
    private volatile ReentrantLock autoloadLock;

    AutoloadConstant(Object feature) {
        assert RubyStringLibrary.getUncached().isRubyString(feature);
        this.feature = feature;
        this.autoloadPath = RubyStringLibrary.getUncached().getJavaString(this.feature);
    }

    public String getAutoloadPath() {
        return autoloadPath;
    }

    public Object getFeature() {
        return feature;
    }

    private ReentrantLock getAutoloadLock() {
        synchronized (this) {
            if (autoloadLock == null) {
                autoloadLock = new ReentrantLock();
            }
        }
        return autoloadLock;
    }

    @TruffleBoundary
    public void startAutoLoad() {
        getAutoloadLock().lock();
    }

    @TruffleBoundary
    public void stopAutoLoad() {
        getAutoloadLock().unlock();
    }

    public boolean isAutoloading() {
        return autoloadLock != null && autoloadLock.isLocked();
    }

    public boolean isAutoloadingThread() {
        return autoloadLock != null && autoloadLock.isHeldByCurrentThread();
    }

}
