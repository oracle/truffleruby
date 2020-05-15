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


import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.collections.WeakValueCache;
import org.truffleruby.core.rope.Rope;

import com.oracle.truffle.api.object.DynamicObject;

public class FrozenStringLiterals {

    private final RubyContext context;
    private final WeakValueCache<Rope, DynamicObject> values = new WeakValueCache<>();

    public FrozenStringLiterals(RubyContext context) {
        this.context = context;
    }

    public DynamicObject getFrozenStringLiteral(Rope rope) {
        final DynamicObject string = values.get(rope);
        if (string != null) {
            return string;
        } else {
            return values.addInCacheIfAbsent(rope, StringOperations.createFrozenString(context, rope));
        }
    }

    public DynamicObject getFrozenStringLiteral(DynamicObject string) {
        assert Layouts.STRING.getFrozen(string) == true;

        final Rope rope = Layouts.STRING.getRope(string);

        final DynamicObject stringCached = values.get(rope);
        if (stringCached != null) {
            return stringCached;
        } else {
            return values.addInCacheIfAbsent(rope, string);
        }
    }

}
