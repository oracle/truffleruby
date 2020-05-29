/*
 * Copyright (c) 2013, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.core.string.StringOperations;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.source.Source;

/** A cache from {@link RubyContext#getPath(Source)} to a Rope. The Rope is kept alive as long as the Source.getPath()
 * or Source.getName() is reachable and therefore as long as the Source is reachable. */
public class PathToRopeCache {

    private final RubyContext context;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final WeakHashMap<String, Rope> javaStringToRope = new WeakHashMap<>();

    public PathToRopeCache(RubyContext context) {
        this.context = context;
    }

    public Rope getCachedPath(Source source) {
        return getCachedPath(RubyContext.getPath(source));
    }

    /** This should only be used for trusted input, as there is no random seed involved for hashing. We need to use the
     * String as key to make Source.getPath() or Source.getName() keep the corresponding Rope alive. */
    @TruffleBoundary
    public Rope getCachedPath(String path) {
        final Lock readLock = lock.readLock();
        readLock.lock();
        try {
            final Rope rope = javaStringToRope.get(path);
            if (rope != null) {
                return rope;
            }
        } finally {
            readLock.unlock();
        }

        final Rope cachedRope = context
                .getRopeCache()
                .getRope(StringOperations.encodeRope(path, UTF8Encoding.INSTANCE));

        final Lock writeLock = lock.writeLock();
        writeLock.lock();
        try {
            javaStringToRope.putIfAbsent(path, cachedRope);
        } finally {
            writeLock.unlock();
        }

        return cachedRope;
    }

}
