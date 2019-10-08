/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.rope;

import org.truffleruby.core.Hashing;

public class RopeKey {

    private final Rope key;
    private final Hashing hashing;

    public RopeKey(Rope key, Hashing hashing) {
        assert !(key instanceof NativeRope);

        this.key = key;
        this.hashing = hashing;
    }

    @Override
    public int hashCode() {
        return hashing.hash(key.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RopeKey) {
            return key.equals(((RopeKey) o).key);
        } else {
            return false;
        }
    }

    public Rope getRope() {
        return key;
    }

    @Override
    public String toString() {
        return key.toString();
    }

}
