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

import java.util.Set;

import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.ExportLibrary;
import org.truffleruby.core.module.ModuleFields;
import org.truffleruby.core.module.RubyModule;
import org.truffleruby.language.objects.IsANode;
import org.truffleruby.language.objects.ObjectGraph;
import org.truffleruby.language.objects.ObjectGraphNode;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.Shape;

@ExportLibrary(InteropLibrary.class)
public final class RubyClass extends RubyModule implements ObjectGraphNode {

    public final boolean isSingleton;
    public final DynamicObject attached;
    public Shape instanceShape = null;
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

    // region MetaObject
    @ExportMessage
    public boolean isMetaObject() {
        return true;
    }

    @ExportMessage
    public String getMetaQualifiedName() {
        return fields.getName();
    }

    @ExportMessage
    public String getMetaSimpleName() {
        return fields.getSimpleName();
    }

    @ExportMessage
    public boolean isMetaInstance(Object instance,
            @Cached IsANode isANode) {
        return isANode.executeIsA(instance, this);
    }
    // endregion

}
