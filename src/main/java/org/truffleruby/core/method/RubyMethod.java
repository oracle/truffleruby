/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.method;

import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.interop.messages.RubyMethodMessages;
import org.truffleruby.language.RubyDynamicObject;
import org.truffleruby.language.methods.InternalMethod;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import java.util.Set;

public class RubyMethod extends RubyDynamicObject implements ObjectGraphNode {

    public RubyMethod(Shape shape, Object receiver, InternalMethod method) {
        super(shape);
        this.receiver = receiver;
        this.method = method;
    }

    public final Object receiver;
    public final InternalMethod method;

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        if (ObjectGraph.isSymbolOrDynamicObject(receiver)) {
            reachable.add(receiver);
        }
        method.getAdjacentObjects(reachable);
    }

    @Override
    @ExportMessage
    public Class<?> dispatch() {
        return RubyMethodMessages.class;
    }

}
