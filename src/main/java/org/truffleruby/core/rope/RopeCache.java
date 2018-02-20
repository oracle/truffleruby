/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.rope;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.jcodings.Encoding;
import org.jcodings.specific.UTF8Encoding;
import org.truffleruby.core.Hashing;
import org.truffleruby.core.string.StringOperations;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RopeCache {

    private final Hashing hashing;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final WeakHashMap<String, Rope> javaStringToRope = new WeakHashMap<>();
    private final WeakHashMap<BytesKey, WeakReference<Rope>> bytesToRope = new WeakHashMap<>();

    private final Set<BytesKey> keys = new HashSet<>();

    private int byteArrayReusedCount;
    private int ropesReusedCount;
    private int ropeBytesSaved;

    public RopeCache(Hashing hashing) {
        this.hashing = hashing;
    }

    public Rope getRope(Rope string) {
        return getRope(string.getBytes(), string.getEncoding(), string.getCodeRange());
    }

    public Rope getRope(Rope string, CodeRange codeRange) {
        return getRope(string.getBytes(), string.getEncoding(), codeRange);
    }

    /**
     * This should only be used for trusted input, as there is no random seed involved for hashing.
     * We need to use the String as key to make Source.getName() keep the corresponding Rope alive.
     */
    @TruffleBoundary
    public Rope getCachedPath(String string) {
        lock.readLock().lock();
        try {
            final Rope rope = javaStringToRope.get(string);
            if (rope != null) {
                return rope;
            }
        } finally {
            lock.readLock().unlock();
        }

        final Rope cachedRope = getRope(StringOperations.encodeRope(string, UTF8Encoding.INSTANCE));

        lock.writeLock().lock();
        try {
            javaStringToRope.putIfAbsent(string, cachedRope);
        } finally {
            lock.writeLock().unlock();
        }

        return cachedRope;
    }

    @TruffleBoundary
    public Rope getRope(byte[] bytes, Encoding encoding, CodeRange codeRange) {
        assert encoding != null;

        final BytesKey key = new BytesKey(bytes, encoding, hashing);

        lock.readLock().lock();
        try {
            final Rope rope = readRef(bytesToRope, key);
            if (rope != null) {
                ++ropesReusedCount;
                ropeBytesSaved += rope.byteLength();

                return rope;
            }
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            final Rope ropeInCache = readRef(bytesToRope, key);
            if (ropeInCache != null) {
                return ropeInCache;
            }

            // At this point, we were unable to find a rope with the same bytes and encoding (i.e., a direct match).
            // However, there may still be a rope with the same byte[] and sharing a direct byte[] can still allow some
            // reference equality optimizations. So, do another search but with a marker encoding. The only guarantee
            // we can make about the resulting rope is that it would have the same logical byte[], but that's good enough
            // for our purposes.
            final Rope ropeWithSameBytesButDifferentEncoding = readRef(bytesToRope, new BytesKey(bytes, null, hashing));

            final Rope rope;
            if (ropeWithSameBytesButDifferentEncoding != null) {
                rope = RopeOperations.create(ropeWithSameBytesButDifferentEncoding.getBytes(), encoding, codeRange);

                ++byteArrayReusedCount;
                ropeBytesSaved += rope.byteLength();
            } else {
                rope = RopeOperations.create(bytes, encoding, codeRange);
            }

            bytesToRope.put(key, new WeakReference<>(rope));

            // TODO (nirvdrum 30-Mar-16): Revisit this. The purpose is to keep all keys live so the weak rope table never expunges results. We don't want that -- we want something that naturally ties to lifetime. Unfortunately, the old approach expunged live values because the key is synthetic. See also FrozenStrings
            keys.add(key);

            return rope;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean contains(Rope rope) {
        final BytesKey key = new BytesKey(rope.getBytes(), rope.getEncoding(), hashing);

        lock.readLock().lock();
        try {
            return bytesToRope.containsKey(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    private <K, V> V readRef(Map<K, WeakReference<V>> map, K key) {
        final WeakReference<V> reference = map.get(key);
        return reference == null ? null : reference.get();
    }

    public int getByteArrayReusedCount() {
        return byteArrayReusedCount;
    }

    public int getRopesReusedCount() {
        return ropesReusedCount;
    }

    public int getRopeBytesSaved() {
        return ropeBytesSaved;
    }

    public int totalRopes() {
        return bytesToRope.size();
    }

}
