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

public class ThreadLocalObject extends ThreadLocal<Object> {

    private final RubyContext context;
    private final WeakReference<Thread> originalThread;
    private Object originalThreadValue;

    public static ThreadLocalObject wrap(RubyContext context, Object value) {
        final ThreadLocalObject threadLocal = new ThreadLocalObject(context);
        threadLocal.set(value);
        return threadLocal;
    }

    public ThreadLocalObject(RubyContext context) {
        this.context = context;
        originalThread = new WeakReference<>(Thread.currentThread());
        originalThreadValue = initialValue();
    }

    @Override
    public Object get() {
        if (Thread.currentThread() == originalThread.get()) {
            return originalThreadValue;
        } else {
            return fallbackGet();
        }
    }

    @TruffleBoundary
    private Object fallbackGet() {
        return super.get();
    }

    @Override
    public void set(Object value) {
        if (Thread.currentThread() == originalThread.get()) {
            originalThreadValue = value;
        } else {
            fallbackSet(value);
        }
    }

    @TruffleBoundary
    private void fallbackSet(Object value) {
        super.set(value);
    }

    @Override
    protected Object initialValue() {
        return context.getCoreLibrary().getNilObject();
    }

}
