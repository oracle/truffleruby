/*
 * Copyright (c) 2018, 2019 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import org.truffleruby.collections.WeakValueCache;
import org.truffleruby.core.rope.LeafRope;
import org.truffleruby.core.rope.Rope;

public class FrozenStringLiterals {

    private final WeakValueCache<Rope, ImmutableRubyString> values = new WeakValueCache<>();

    public ImmutableRubyString getFrozenStringLiteral(LeafRope rope) {
        final ImmutableRubyString string = values.get(rope);
        if (string != null) {
            return string;
        } else {
            return values.addInCacheIfAbsent(rope, new ImmutableRubyString(rope));
        }
    }

}
