/*
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.language.threadlocal;

import java.lang.ref.WeakReference;

import org.truffleruby.RubyContext;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.profiles.ConditionProfile;

public class ThreadAndFrameLocalStorage {

    private final RubyContext context;
    private final WeakReference<Thread> originalThread;
    private Object originalThreadValue;
    private volatile ThreadLocal<Object> otherThreadValues = null;

    public ThreadAndFrameLocalStorage(RubyContext context) {
        this.context = context;
        // Cannot store a Thread instance while pre-initializing
        originalThread = new WeakReference<>(context.isPreInitializing() ? null : Thread.currentThread());
        originalThreadValue = initialValue();
    }

    public Object get(ConditionProfile sameThreadProfile) {
        if (sameThreadProfile.profile(Thread.currentThread() == originalThread.get())) {
            return originalThreadValue;
        } else {
            return fallbackGet();
        }
    }

    private ThreadLocal<Object> getOtherThreadValues() {
        if (otherThreadValues != null) {
            return otherThreadValues;
        } else {
            synchronized (this) {
                if (otherThreadValues != null) {
                    return otherThreadValues;
                } else {
                    otherThreadValues = ThreadLocal.withInitial(this::initialValue);
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

    protected Object initialValue() {
        return context.getCoreLibrary().getNil();
    }

}
