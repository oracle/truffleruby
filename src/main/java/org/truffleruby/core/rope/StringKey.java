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

public class StringKey {

    private final String key;
    private final Hashing hashing;

    public StringKey(String key, Hashing hashing) {
        this.key = key;
        this.hashing = hashing;
    }

    @Override
    public int hashCode() {
        return hashing.hash(key.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StringKey) {
            return key.equals(((StringKey) o).key);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return key;
    }

}
