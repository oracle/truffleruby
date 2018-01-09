/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.truffleruby.core.string;

import com.oracle.truffle.api.object.DynamicObject;
import org.truffleruby.RubyContext;
import org.truffleruby.core.rope.Rope;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FrozenStrings {

    private final RubyContext context;

    private final Map<Rope, DynamicObject> frozenStrings = new WeakHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Set<Rope> keys = new HashSet<>();

    public FrozenStrings(RubyContext context) {
        this.context = context;
    }

    public DynamicObject getFrozenString(Rope rope) {
        assert context.getRopeCache().contains(rope);

        DynamicObject string;

        lock.readLock().lock();
        try {
            string = frozenStrings.get(rope);
            if (string != null) {
                return string;
            }
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            string = frozenStrings.get(rope);
            if (string == null) {
                string = StringOperations.createFrozenString(context, rope);
                // TODO CS 8-Jan-18 as in RopeCache the map is weak. So we just hold on to everything forever using this method.
                keys.add(rope);
                frozenStrings.put(rope, string);
            }
        } finally {
            lock.writeLock().unlock();
        }

        return string;
    }

}
