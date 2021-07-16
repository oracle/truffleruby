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
import org.truffleruby.parser.ReOptions;

public final class RegexpCacheKey {

    private final Rope rope;
    private final RubyEncoding encoding;
    private final int options;
    private final Hashing hashing;

    public RegexpCacheKey(Rope rope, RubyEncoding encoding, int options, Hashing hashing) {
        assert !(rope instanceof NativeRope);
        this.rope = rope;
        this.encoding = encoding;
        this.options = options;
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
            return rope.equals(other.rope) && encoding == other.encoding && options == other.options;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('/').append(rope.toString()).append('/');
        if ((options & ReOptions.RE_OPTION_MULTILINE) != 0) {
            builder.append('m');
        }
        if ((options & ReOptions.RE_OPTION_IGNORECASE) != 0) {
            builder.append('i');
        }
        if ((options & ReOptions.RE_OPTION_EXTENDED) != 0) {
            builder.append('x');
        }
        return builder.toString();
    }
}
