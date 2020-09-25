/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.hash;

import java.util.Set;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.Shape;

import org.truffleruby.RubyContext;
import org.truffleruby.core.klass.RubyClass;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

public class RubyHash extends RubyDynamicObject implements ObjectGraphNode {

    public Object defaultBlock;
    public Object defaultValue;
    public Object store;
    public int size;
    public Entry firstInSequence;
    public Entry lastInSequence;
    public boolean compareByIdentity;

    public RubyHash(
            RubyClass rubyClass,
            Shape shape,
            RubyContext context,
            Object store,
            int size,
            Entry firstInSequence,
            Entry lastInSequence,
            Object defaultBlock,
            Object defaultValue,
            boolean compareByIdentity) {
        super(rubyClass, shape);
        this.defaultBlock = defaultBlock;
        this.defaultValue = defaultValue;
        this.store = store;
        this.size = size;
        this.firstInSequence = firstInSequence;
        this.lastInSequence = lastInSequence;
        this.compareByIdentity = compareByIdentity;

        if (context.isPreInitializing()) {
            context.getPreInitializationManager().addPreInitHash(this);
        }
    }

    @TruffleBoundary
    public void getAdjacentObjects(Set<Object> reachable) {
        if (store instanceof Entry[]) {
            BucketsStrategy.getAdjacentObjects(reachable, firstInSequence);
        } else {
            ObjectGraph.addProperty(reachable, store);
        }

        ObjectGraph.addProperty(reachable, defaultBlock);
        ObjectGraph.addProperty(reachable, defaultValue);
    }

}
