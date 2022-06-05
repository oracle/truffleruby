/*
 * Copyright (c) 2015, 2021 Oracle and/or its affiliates. All rights reserved. This
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

public class ThreadAndFrameLocalStorage {

    // We store a thread id rather than the thread itself. Although
    // this id can theoretically be reused the implementation simply
    // increments a long and has 48 bits that it can use.
    private final long originalThreadId;
    private Object originalThreadValue;
    private volatile ThreadLocal<Object> otherThreadValues = null;

    public ThreadAndFrameLocalStorage(RubyContext context) {
        // Cannot store a Thread id while pre-initializing
        originalThreadId = context.isPreInitializing() ? 0 : Thread.currentThread().getId();
        originalThreadValue = Nil.get();
    }

    public Object get(ConditionProfile sameThreadProfile) {
        if (sameThreadProfile.profile(Thread.currentThread().getId() == originalThreadId)) {
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
                    otherThreadValues = ThreadLocal.withInitial(() -> Nil.get());
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
        if (sameThreadProfile.profile(Thread.currentThread().getId() == originalThreadId)) {
            originalThreadValue = value;
        } else {
            fallbackSet(value);
        }
    }

    @TruffleBoundary
    private void fallbackSet(Object value) {
        getOtherThreadValues().set(value);
    }

}
