/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.queue;

import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.interop.messages.RubySizedQueueMessages;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import java.util.Set;


public class RubySizedQueue extends RubyDynamicObject implements ObjectGraphNode {

    SizedQueue queue;

    public RubySizedQueue(Shape shape, SizedQueue queue) {
        super(shape);
        this.queue = queue;
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        for (Object element : queue.getContents()) {
            if (ObjectGraph.isSymbolOrDynamicObject(element)) {
                reachable.add(element);
            }
        }
    }

    @Override
    @ExportMessage
    public Class<?> dispatch() {
        return RubySizedQueueMessages.class;
    }

}
