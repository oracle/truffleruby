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

import org.jcodings.Encoding;
import org.truffleruby.collections.WeakValueCache;
import org.truffleruby.core.symbol.CoreSymbols;
import org.truffleruby.core.symbol.RubySymbol;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public class RopeCache {

    private final WeakValueCache<BytesKey, Rope> bytesToRope = new WeakValueCache<>();

    private int byteArrayReusedCount;
    private int ropesReusedCount;
    private int ropeBytesSaved;

    public RopeCache(CoreSymbols coreSymbols) {
        addRopeConstants();
        addCoreSymbolRopes(coreSymbols);
    }

    private void addRopeConstants() {
        for (Rope rope : RopeConstants.UTF8_SINGLE_BYTE_ROPES) {
            register(rope);
        }
        for (Rope rope : RopeConstants.US_ASCII_SINGLE_BYTE_ROPES) {
            register(rope);
        }
        for (Rope rope : RopeConstants.ASCII_8BIT_SINGLE_BYTE_ROPES) {
            register(rope);
        }
        for (Rope rope : RopeConstants.ROPE_CONSTANTS.values()) {
            register(rope);
        }
    }

    private void addCoreSymbolRopes(CoreSymbols coreSymbols) {
        for (RubySymbol symbol : coreSymbols.CORE_SYMBOLS) {
            register(symbol.getRope());
        }
    }

    private void register(Rope rope) {
        final BytesKey key = new BytesKey(rope.getBytes(), rope.getEncoding());
        final Rope existing = bytesToRope.put(key, rope);
        if (existing != null && existing != rope) {
            throw new AssertionError("Duplicate Rope in RopeCache: " + existing.getString());
        }
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

        final BytesKey key = new BytesKey(bytes, encoding);

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
        final Rope ropeWithSameBytesButDifferentEncoding = bytesToRope.get(new BytesKey(bytes, null));

        final Rope newRope;
        if (ropeWithSameBytesButDifferentEncoding != null) {
            newRope = RopeOperations.create(ropeWithSameBytesButDifferentEncoding.getBytes(), encoding, codeRange);

            ++byteArrayReusedCount;
            ropeBytesSaved += newRope.byteLength();
        } else {
            newRope = RopeOperations.create(bytes, encoding, codeRange);
        }

        // Use the new Rope bytes in the cache, so we do not keep bytes alive unnecessarily.
        final BytesKey newKey = new BytesKey(newRope.getBytes(), newRope.getEncoding());
        return bytesToRope.addInCacheIfAbsent(newKey, newRope);
    }

    public boolean contains(Rope rope) {
        final BytesKey key = new BytesKey(rope.getBytes(), rope.getEncoding());

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
