/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.regexp;

import org.truffleruby.core.Hashing;
import org.truffleruby.core.encoding.RubyEncoding;
import org.truffleruby.core.rope.NativeRope;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeOperations;

public final class RegexpCacheKey {

    private final Rope rope;
    private final RubyEncoding encoding;
    private final int joniOptions;
    private final Hashing hashing;

    public RegexpCacheKey(Rope rope, RubyEncoding encoding, RegexpOptions options, Hashing hashing) {
        assert !(rope instanceof NativeRope);
        this.rope = rope;
        this.encoding = encoding;
        this.joniOptions = options.toJoniOptions();
        this.hashing = hashing;
    }

    @Override
    public int hashCode() {
        return hashing.hash(rope.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof RegexpCacheKey) {
            final RegexpCacheKey other = (RegexpCacheKey) o;
            return rope.equals(other.rope) && encoding == other.encoding && joniOptions == other.joniOptions;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return '/' + RopeOperations.decodeOrEscapeBinaryRope(rope) + '/' +
                RegexpOptions.fromJoniOptions(joniOptions).toOptionsString() +
                " -- " + RopeOperations.decodeOrEscapeBinaryRope(encoding.name.rope);
    }
}
