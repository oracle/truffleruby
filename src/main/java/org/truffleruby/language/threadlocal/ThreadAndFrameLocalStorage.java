/*
 * Copyright (c) 2015, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.threadlocal;

import org.truffleruby.RubyContext;
import org.truffleruby.language.Nil;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.profiles.ConditionProfile;
import com.oracle.truffle.api.utilities.TruffleWeakReference;

public class ThreadAndFrameLocalStorage {

    private static final TruffleWeakReference<Thread> EMPTY_WEAK_REF = newTruffleWeakReference(null);

    private final TruffleWeakReference<Thread> originalThread;
    private Object originalThreadValue;
    private volatile ThreadLocal<Object> otherThreadValues = null;

    public ThreadAndFrameLocalStorage(RubyContext context) {
        // Cannot store a Thread instance while pre-initializing
        originalThread = context.isPreInitializing() ? EMPTY_WEAK_REF : newTruffleWeakReference(Thread.currentThread());
        originalThreadValue = Nil.INSTANCE;
    }

    public Object get(ConditionProfile sameThreadProfile) {
        if (sameThreadProfile.profile(Thread.currentThread() == originalThread.get())) {
            return originalThreadValue;
        } else {
            return fallbackGet();
        }
    }

    @TruffleBoundary
    private ThreadLocal<Object> getOtherThreadValues() {
        if (otherThreadValues != null) {
            return otherThreadValues;
        } else {
            synchronized (this) {
                if (otherThreadValues != null) {
                    return otherThreadValues;
                } else {
                    otherThreadValues = ThreadLocal.withInitial(() -> Nil.INSTANCE);
                    return otherThreadValues;
                }
            }
        }
    }

    @TruffleBoundary
    private Object fallbackGet() {
        return getOtherThreadValues().get();
    }

    public void set(Object value, ConditionProfile sameThreadProfile) {
        if (sameThreadProfile.profile(Thread.currentThread() == originalThread.get())) {
            originalThreadValue = value;
        } else {
            fallbackSet(value);
        }
    }

    @TruffleBoundary
    private void fallbackSet(Object value) {
        getOtherThreadValues().set(value);
    }

    @TruffleBoundary // GR-25356
    private static TruffleWeakReference<Thread> newTruffleWeakReference(Thread thread) {
        return new TruffleWeakReference<>(thread);
    }

}
