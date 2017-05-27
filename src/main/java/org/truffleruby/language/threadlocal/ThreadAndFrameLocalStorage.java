/*
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.language.threadlocal;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.truffleruby.RubyContext;

import java.lang.ref.WeakReference;

public class ThreadAndFrameLocalStorage {

    private final RubyContext context;
    private final WeakReference<Thread> originalThread;
    private Object originalThreadValue;
    private volatile ThreadLocal<Object> otherThreadValues = null;

    public static ThreadAndFrameLocalStorage wrap(RubyContext context, Object value) {
        final ThreadAndFrameLocalStorage storage = new ThreadAndFrameLocalStorage(context);
        storage.set(value);
        return storage;
    }

    public ThreadAndFrameLocalStorage(RubyContext context) {
        this.context = context;
        originalThread = new WeakReference<>(Thread.currentThread());
        originalThreadValue = initialValue();
    }

    public Object get() {
        if (Thread.currentThread() == originalThread.get()) {
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
                    otherThreadValues = new ThreadLocal<Object>() {
                        @Override
                        protected Object initialValue() {
                            return ThreadAndFrameLocalStorage.this.initialValue();
                        }

                    };
                    return otherThreadValues;
                }
            }
        }
    }

    @TruffleBoundary
    private Object fallbackGet() {
        return getOtherThreadValues().get();
    }

    public void set(Object value) {
        if (Thread.currentThread() == originalThread.get()) {
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
        return context.getCoreLibrary().getNilObject();
    }

}
