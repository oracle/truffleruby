/*
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved. This
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

import org.jcodings.Encoding;
import org.joni.Regex;
import org.truffleruby.collections.ConcurrentOperations;

public class EncodingCache {

    private final Map<Encoding, Regex> encodings = new ConcurrentHashMap<>();

    public Regex getOrCreate(Encoding encoding, Function<Encoding, Regex> function) {
        return ConcurrentOperations.getOrCompute(encodings, encoding, function);
    }
}
