/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.core.string.StringOperations;

import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A cache from Source.getName() to a Rope. The Rope is kept alive as long as the Source.getName()
 * is reachable and therefore as long as the Source is reachable.
 */
public class PathToRopeCache {

    private final RubyContext context;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final WeakHashMap<String, Rope> javaStringToRope = new WeakHashMap<>();

    public PathToRopeCache(RubyContext context) {
        this.context = context;
    }

    /**
     * This should only be used for trusted input, as there is no random seed involved for hashing.
     * We need to use the String as key to make Source.getName() keep the corresponding Rope alive.
     */
    @TruffleBoundary
    public Rope getCachedPath(String string) {
        final Lock readLock = lock.readLock();

        readLock.lock();
        try {
            final Rope rope = javaStringToRope.get(string);
            if (rope != null) {
                return rope;
            }
        } finally {
            readLock.unlock();
        }

        final Rope cachedRope = context.getRopeCache().getRope(StringOperations.encodeRope(string, UTF8Encoding.INSTANCE));

        final Lock writeLock = lock.writeLock();

        writeLock.lock();
        try {
            javaStringToRope.putIfAbsent(string, cachedRope);
        } finally {
            writeLock.unlock();
        }

        return cachedRope;
    }

}
