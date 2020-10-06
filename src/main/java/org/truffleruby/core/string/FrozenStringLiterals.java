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

import org.truffleruby.RubyContext;
import org.truffleruby.collections.WeakValueCache;
import org.truffleruby.core.rope.Rope;

public class FrozenStringLiterals {

    private final RubyContext context;
    private final WeakValueCache<Rope, RubyString> values = new WeakValueCache<>();

    public FrozenStringLiterals(RubyContext context) {
        this.context = context;
    }

    public RubyString getFrozenStringLiteral(Rope rope) {
        final RubyString string = values.get(rope);
        if (string != null) {
            return string;
        } else {
            return values.addInCacheIfAbsent(rope, StringOperations.createFrozenString(context, rope));
        }
    }

    public RubyString getFrozenStringLiteral(RubyString string) {
        assert string.frozen == true;

        final Rope rope = string.rope;

        final RubyString stringCached = values.get(rope);
        if (stringCached != null) {
            return stringCached;
        } else {
            return values.addInCacheIfAbsent(rope, string);
        }
    }

}
