/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved. This
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
import java.util.function.BiFunction;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.graalvm.collections.Pair;
import org.jcodings.Encoding;
import org.truffleruby.collections.ConcurrentOperations;

public class TRegexCache {

    private final Map<Pair<Boolean, Encoding>, Object> compiledRegexCache;

    @TruffleBoundary
    public TRegexCache() {
        this.compiledRegexCache = new ConcurrentHashMap<>();
    }

    public Object getOrCreate(boolean atStart, Encoding encoding, BiFunction<Boolean, Encoding, Object> function) {
        return ConcurrentOperations.getOrCompute(
                compiledRegexCache,
                Pair.create(atStart, encoding),
                (key) -> function.apply(key.getLeft(), key.getRight()));
    }
}
