/*
 * Copyright (c) 2015, 2020 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 2.0, or
 * GNU General Public License version 2, or
 * GNU Lesser General Public License version 2.1.
 */
package org.truffleruby.core.klass;

import org.truffleruby.core.module.ModuleFields;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.interop.messages.RubyClassMessages;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.DynamicObjectFactory;
import com.oracle.truffle.api.object.Shape;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import java.util.Set;

public final class RubyClass extends RubyModule implements ObjectGraphNode {

    public final boolean isSingleton;
    public final DynamicObject attached;
    public DynamicObjectFactory instanceFactory = null;
    /* a RubyClass, or nil for BasicObject, or null when not yet initialized */
    public Object superclass = null;

    public RubyClass(Shape shape, ModuleFields fields, boolean isSingleton, DynamicObject attached) {
        super(shape, fields);
        this.isSingleton = isSingleton;
        this.attached = attached;
    }

    @Override
    public void getAdjacentObjects(Set<Object> reachable) {
        super.getAdjacentObjects(reachable);
        ObjectGraph.addProperty(reachable, attached);
        ObjectGraph.addProperty(reachable, superclass);
    }

    @Override
    public Class<?> dispatch() {
        return RubyClassMessages.class;
    }

}
