/*
 * Copyright (c) 2016, 2017 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core;

import java.util.Random;

public class Hashing {

    private static final boolean CONSISTENT_HASHING_ENABLED = false;

    private static final int MURMUR2_MAGIC = 0x5bd1e995;

    private static final long SEED;

    static {
        if (CONSISTENT_HASHING_ENABLED) {
            SEED = 7114160726623585955L;
        } else {
            final Random random = new Random();
            SEED = random.nextLong();
        }
    }

    public static long hash(long seed, long value) {
        return end(update(start(seed), value));
    }

    public static long start(long value) {
        return value + SEED;
    }

    public static long update(long hash, long value) {
        hash += value;
        return murmur1(murmur1(hash) + (hash >>> 32));
    }

    public static long end(long hash) {
        return murmur_step(murmur_step(hash, 10), 17);
    }

    private static long murmur1(long h) {
        return murmur_step(h, 16);
    }

    private static long murmur_step(long h, long k) {
        h += k;
        h *= MURMUR2_MAGIC;
        h ^= h >> 16;
        return h;
    }

}
