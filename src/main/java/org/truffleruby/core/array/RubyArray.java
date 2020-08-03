/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.array;

import org.truffleruby.interop.messages.ArrayMessages;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import java.util.Set;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;

public final class RubyArray extends RubyDynamicObject implements ObjectGraphNode {

    public Object store;
    public int size;

    public RubyArray(Shape shape, Object store, int size) {
        super(shape);
        this.store = store;
        this.size = size;
    }

    @Override
    @ExportMessage
    public Class<?> dispatch() {
        return ArrayMessages.class;
    }

    @TruffleBoundary
    public void getAdjacentObjects(Set<Object> reachable) {
        ObjectGraph.addProperty(reachable, store);
    }
}
