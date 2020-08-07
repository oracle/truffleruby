/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.module;

import org.truffleruby.interop.messages.RubyModuleMessages;
import org.truffleruby.language.RubyDynamicObject;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.language.objects.ObjectGraphNode;

import java.util.Set;

public class RubyModule extends RubyDynamicObject implements ObjectGraphNode {

    public static final RubyModule[] EMPTY_ARRAY = new RubyModule[0];

    public final ModuleFields fields;

    public RubyModule(Shape shape, ModuleFields fields) {
        super(shape);
        this.fields = fields;
    }

    @TruffleBoundary
    @Override
    public String toString() {
        return fields.getName();
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        fields.getAdjacentObjects(reachable);
    }

    @Override
    public Class<?> dispatch() {
        return RubyModuleMessages.class;
    }
}
