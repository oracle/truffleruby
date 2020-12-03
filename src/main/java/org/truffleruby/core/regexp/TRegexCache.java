/*
 * Copyright (c) 2017, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.regexp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.jcodings.Encoding;
import org.truffleruby.collections.ConcurrentOperations;

public class TRegexCache {

    public static class Key {
        public final boolean sticky;
        public final Encoding encoding;

        public Key(boolean sticky, Encoding encoding) {
            this.sticky = sticky;
            this.encoding = encoding;
        }

        @Override
        public int hashCode() {
            return Boolean.hashCode(sticky) * 31 + encoding.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                return false;
            }
            Key other = (Key) obj;
            return this.sticky == other.sticky && this.encoding.equals(other.encoding);
        }
    }

    private final Map<Key, Object> compiledRegexCache;

    @TruffleBoundary
    public TRegexCache() {
        this.compiledRegexCache = new ConcurrentHashMap<>();
    }

    public Object getOrCreate(Key cacheKey, Function<Key, Object> function) {
        return ConcurrentOperations.getOrCompute(compiledRegexCache, cacheKey, function);
    }
}
