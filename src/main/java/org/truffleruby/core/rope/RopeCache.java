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

import org.jcodings.Encoding;
import org.truffleruby.RubyContext;
import org.truffleruby.collections.WeakValueCache;
import org.truffleruby.core.Hashing;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class RopeCache {

    private final Hashing hashing;

    private final WeakValueCache<BytesKey, Rope> bytesToRope = new WeakValueCache<>();

    private int byteArrayReusedCount;
    private int ropesReusedCount;
    private int ropeBytesSaved;

    public RopeCache(RubyContext context) {
        this.hashing = context.getHashing(bytesToRope);
    }

    public Rope getRope(Rope string) {
        return getRope(string.getBytes(), string.getEncoding(), string.getCodeRange());
    }

    public Rope getRope(Rope string, CodeRange codeRange) {
        return getRope(string.getBytes(), string.getEncoding(), codeRange);
    }

    @TruffleBoundary
    public Rope getRope(byte[] bytes, Encoding encoding, CodeRange codeRange) {
        assert encoding != null;

        final BytesKey key = new BytesKey(bytes, encoding, hashing);

        final Rope rope = bytesToRope.get(key);
        if (rope != null) {
            ++ropesReusedCount;
            ropeBytesSaved += rope.byteLength();

            return rope;
        }

        // At this point, we were unable to find a rope with the same bytes and encoding (i.e., a direct match).
        // However, there may still be a rope with the same byte[] and sharing a direct byte[] can still allow some
        // reference equality optimizations. So, do another search but with a marker encoding. The only guarantee
        // we can make about the resulting rope is that it would have the same logical byte[], but that's good enough
        // for our purposes.
        final Rope ropeWithSameBytesButDifferentEncoding = bytesToRope.get(new BytesKey(bytes, null, hashing));

        final Rope newRope;
        if (ropeWithSameBytesButDifferentEncoding != null) {
            newRope = RopeOperations.create(ropeWithSameBytesButDifferentEncoding.getBytes(), encoding, codeRange);

            ++byteArrayReusedCount;
            ropeBytesSaved += newRope.byteLength();
        } else {
            newRope = RopeOperations.create(bytes, encoding, codeRange);
        }

        // Use the new Rope bytes in the cache, so we do not keep bytes alive unnecessarily.
        final BytesKey newKey = new BytesKey(newRope.getBytes(), newRope.getEncoding(), hashing);
        return bytesToRope.addInCacheIfAbsent(newKey, newRope);
    }

    public boolean contains(Rope rope) {
        final BytesKey key = new BytesKey(rope.getBytes(), rope.getEncoding(), hashing);

        return bytesToRope.get(key) != null;
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
