/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.string;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.truffleruby.Layouts;
import org.truffleruby.RubyContext;
import org.truffleruby.collections.ConcurrentOperations;
import org.truffleruby.collections.WeakValueCache;
import org.truffleruby.core.rope.Rope;
import org.truffleruby.core.rope.RopeKey;

import com.oracle.truffle.api.object.DynamicObject;

public class FrozenStringLiterals {

    private final RubyContext context;
    private final WeakValueCache<RopeKey, DynamicObject> values = new WeakValueCache<>();

    public FrozenStringLiterals(RubyContext context) {
        this.context = context;
    }

    public DynamicObject getFrozenStringLiteral(Rope rope) {
        final RopeKey key = new RopeKey(rope, context.getHashing(values));
        return values.addInCacheIfAbsent(key, StringOperations.createFrozenString(context, rope));
    }

    public DynamicObject getFrozenStringLiteral(DynamicObject string) {
        final Rope rope = Layouts.STRING.getRope(string);
        final RopeKey key = new RopeKey(rope, context.getHashing(values));
        return values.addInCacheIfAbsent(key, string);
    }

}
